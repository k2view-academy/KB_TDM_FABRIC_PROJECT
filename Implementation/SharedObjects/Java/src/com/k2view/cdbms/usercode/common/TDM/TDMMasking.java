package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.cdbms.lut.LUType;
import com.k2view.cdbms.shared.user.UserCode;

import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.io.IoCommand;
import com.k2view.fabric.common.io.IoSession;
import com.k2view.broadway.actors.masking.Masking;

import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getFieldInfoRow;
import static com.k2view.fabric.common.Util.safeClose;
import static com.k2view.cdbms.shared.user.UserCode.getLuType;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
public class TDMMasking extends Masking {
    public static final Log log = Log.a(UserCode.class);

    private static String queryInsert(String tdmdbSchema) {
        return "INSERT INTO " + tdmdbSchema + ".tdm_seq_mapping (" +
        "task_execution_id," +
        "lu_type," +
        "source_env," +
        "entity_target_id," +
        "seq_name," +
        "table_name," +
        "column_name," +
        "source_id," +
        "target_id," +
        "is_instance_id" +
        ") VALUES " +
        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
    }

    private static final String ERROR_CATEGORY_MASKING = "Masking";
    private static final String ERROR_CATEGORY_SEQUENCES = "Sequences";

    private static String queryInsertMaskingError(String tdmdbSchema) {
        return "INSERT INTO " + tdmdbSchema + ".task_exe_error_detailed (" +
        "task_execution_id," +
        "lu_name," +
        "entity_id," +
        "iid," +
        "target_entity_id," +
        "error_category," +
        "error_code," +
        "error_message," +
        "flow_name," +
        "actor_name," +
        "actor_parameters" +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private static String luName;
    private static String taskExecutionId;
    private static String sourceEnv;
    private static String entityTargetId;
    private static String rootTableName;
    private static String rootFieldName;
    public static Boolean TDM_SEQ_REPORT;
    public static Boolean TDM_USING_CATALOG_SEQUENCES;
    //private static IoSession TDMSession;

    // Caches the check task type isn't Extract
    private Boolean skipGlobalsForNonExtract;

    @Override
    public void action(Data input, Data output, Context ctx) throws Exception {

        // Outside a TDM task execution (e.g. Broadway debug or an LU sync not run by a task) there is
        // no TDM_TASK_EXE_ID, and every TDM report written below is keyed by it - just mask.
        if (ctx.globals().get("TDM_TASK_EXE_ID") == null
                && TDMSetBroadwayGlobals.fetchFabricGlobal(ctx, "TDM_TASK_EXE_ID") == null) {
            super.action(input, output, ctx);
            return;
        }

        String tableName = input.get("dataset") != null ? input.string("dataset") : "" ;
        String fieldName = input.get("field") != null ? input.string("field") : "";
        String category = input.get("category") != null ? input.string("category") : "";
        String dataPlatform = input.get("dataPlatform") != null ? input.string("dataPlatform") : "";
        String schemaName = input.get("schema") != null ? input.string("schema") : "" ;
        String errCategory = "enable_sequences".equals(category) ? ERROR_CATEGORY_SEQUENCES : ERROR_CATEGORY_MASKING;
        ensureBroadwayGlobals(ctx);

        try {
            super.action(input, output, ctx);
        } catch (Exception e) {
            Object taskExecObj = ctx.globals().get("TDM_TASK_EXE_ID");
            String dataGeneratorForError = resolveDataGenerator(ctx, dataPlatform, schemaName, tableName, fieldName);
            insertMaskingError(ctx, taskExecObj != null ? taskExecObj.toString() : null, tableName, "action",
                    "table=" + tableName + ";field=" + fieldName + ";category=" + category +
                            ";data_generator=" + (dataGeneratorForError != null ? dataGeneratorForError : ""), errCategory, e);
            throw e;
        }

        Boolean newTask = false;
        String currentTaskExecId = ctx.globals().get("TDM_TASK_EXE_ID").toString();
        if (!currentTaskExecId.equals(taskExecutionId)) {
            // New task execution (or first call in this JVM) - the cached values below are
            // static and otherwise never expire, so they must be refreshed here or a later
            // execution would silently inherit a previous execution's identifiers. Within a
            // single execution entityTargetId is deliberately carried across field calls
            // (set once the root instance field is seen, reused for every other field), so it
            // is only reset on an execution change, not on every call.
            taskExecutionId = currentTaskExecId;
            newTask = true;
            Object obj;
            obj = ctx.globals().get("TDM_USING_CATALOG_SEQUENCES");
            if (obj != null) {
                TDM_USING_CATALOG_SEQUENCES = Boolean.parseBoolean(obj.toString());
            } else {
                TDM_USING_CATALOG_SEQUENCES = false;
            }

        }

        if (!TDM_USING_CATALOG_SEQUENCES) {
            return;
        }

        //log.info("TDMMasking - Input table: " + tableName);
        if (!tableName.isEmpty()) {
            tdmMasking(input,output, ctx, newTask);
        }
    }

    /**
     * ctx.globals() only has TDM_TASK_EXE_ID and the two catalog lookup maps below when
     * TDMSetBroadwayGlobals has already run in this SAME Broadway Context. Per-table
     * "<table>.population" flows get their own separate Context from whatever orchestrator normally
     * calls that actor (Fabric's LU population engine gives each one a fresh, isolated globals map)
     */
    private boolean hasBroadwayGlobals(Context ctx) {
        return ctx.globals().get("TDM_TASK_EXE_ID") != null
                && ctx.globals().get(TDMSetBroadwayGlobals.SYSTEM_BY_INTERFACE_MAP_KEY) instanceof Map
                && ctx.globals().get(TDMSetBroadwayGlobals.CLASSIFICATION_GENERATOR_MAP_KEY) instanceof Map;
    }

    private void ensureBroadwayGlobals(Context ctx) throws Exception {
        if (hasBroadwayGlobals(ctx)) {
            return;
        }
        if (Boolean.TRUE.equals(skipGlobalsForNonExtract)) {
            return;
        }
        Object taskType = ctx.globals().get("TASK_TYPE");
        if (taskType == null) {
            taskType = TDMSetBroadwayGlobals.fetchFabricGlobal(ctx, "TASK_TYPE");
        }
        boolean isExtract = taskType == null || "EXTRACT".equalsIgnoreCase(taskType.toString());
        if (!isExtract && !isResyncingFromSource(ctx)) {
            skipGlobalsForNonExtract = true;
            return;
        }
        TDMSetBroadwayGlobals.setGlobals(ctx, null);
    }

    private boolean isResyncingFromSource(Context ctx) {
        Object syncSourceData = ctx.globals().get("TDM_SYNC_SOURCE_DATA");
        if (syncSourceData == null) {
            syncSourceData = TDMSetBroadwayGlobals.fetchFabricGlobal(ctx, "TDM_SYNC_SOURCE_DATA");
        }
        return syncSourceData != null && Boolean.parseBoolean(syncSourceData.toString());
    }

    /**
     * Insert masking failure into task_exe_error_detailed error_category is always ERROR_CATEGORY_MASKING so masking
     * failures can be filtered independently of every other TDM error source.
     * Never throws - a failure here must not fail, retry, or otherwise disturb the masking/data
     * pipeline that is already in the middle of handling its own error.
     */
    private void insertMaskingError(Context ctx, String taskExecId, String entityRef, String actorName, String actorParameters, String category, Throwable t) {
        if (taskExecId == null || taskExecId.isEmpty()) {
            log.warn("TDMMasking - insertMaskingError skipped, no TDM_TASK_EXE_ID in context (flow=" +
                    ctx.flowName() + ", actor=" + actorName + ", entity=" + entityRef + "): " + t.getMessage());
            return;
        }

        String luName = "";
        try {
            Object luNameObj = ctx.globals().get("LU_TYPE");
            if (luNameObj != null) {
                luName = luNameObj.toString();
            }
        } catch (Exception ignored) {
            // fall back to "" below
        }

        String entity = entityRef != null ? entityRef : "";
        String message = t.getMessage() != null ? t.getMessage() : String.valueOf(t);

        String tdmdbSchema = TDMSetBroadwayGlobals.resolveTdmdbSchema(ctx);

        IoSession session = null;
        IoCommand.Statement statement = null;
        try {
            session = ctx.ioProvider().createSession("TDM");
            statement = session.prepareStatement(queryInsertMaskingError(tdmdbSchema));
            statement.execute(taskExecId, luName, entity, entity, entity,
                    category, t.getClass().getSimpleName(), message,
                    ctx.flowName(), actorName, actorParameters);
            log.info("TDMMasking - recorded masking error into task_exe_error_detailed (task_execution_id=" +
                    taskExecId + ", actor=" + actorName + ", entity=" + entity + "): " + message);
        } catch (Exception insertEx) {
            log.error("TDMMasking - failed to record masking error (task_execution_id=" + taskExecId + "): " + insertEx.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception closeEx) {
                    log.error("TDMMasking - failed to close statement: " + closeEx.getMessage());
                }
            }
            safeClose(session);
        }
    }

    @SuppressWarnings("unchecked")
    private String resolveDataGenerator(Context ctx, String interfaceName, String schemaName, String tableName, String fieldName) {
        try {
            Map<String, Object> fieldRow =  getFieldInfoRow(interfaceName, schemaName, tableName, fieldName);
           
            String classification = fieldRow != null && fieldRow.get("classification") != null
                    ? fieldRow.get("classification").toString() : null;
            if (classification == null) {
                return null;
            }
            Map<String, Map<String, String>> classificationGenerators =
                    (Map<String, Map<String, String>>) ctx.globals().get(TDMSetBroadwayGlobals.CLASSIFICATION_GENERATOR_MAP_KEY);
            Map<String, String> generatorInfo = classificationGenerators != null
                    ? classificationGenerators.get(classification.toUpperCase())
                    : null;
            String dataGenerator = generatorInfo != null ? generatorInfo.get("generator") : null;
            if (dataGenerator != null && dataGenerator.toLowerCase().endsWith(".actor")) {
                dataGenerator = dataGenerator.substring(0, dataGenerator.length() - ".actor".length());
            }
            return dataGenerator;
        } catch (Exception e) {
            return null;
        }
    }

    private void tdmMasking(Data input, Data output, Context ctx, Boolean newTask) throws Exception {
        //IoSession fabricSession = ctx.ioProvider().createSession("fabric");
        String category = input.get("category") != null ? input.string("category") : "";
        //log.info("TDMMasking - category: " + category);
        if (!"enable_sequences".equalsIgnoreCase(category)) {
            return;
        }

        Object obj;
        if (newTask) {
            obj = ctx.globals().get("TDM_SEQ_REPORT");
            if (obj != null) {
                TDM_SEQ_REPORT = Boolean.parseBoolean(obj.toString());
            } else {
                TDM_SEQ_REPORT = false;
            }
        }
        
        //log.info("TDM_SEQ_REPORT#TDM_SEQ_REPORT: " + TDM_SEQ_REPORT);
        if (!TDM_SEQ_REPORT) {
            return;
        }

        String enableSeq = "false";
        obj = ctx.globals().get("enable_sequences");
        if (obj != null) {
            enableSeq = obj.toString();
        }

        if ("true".equalsIgnoreCase(enableSeq)) {
            String tableName = input.get("dataset") != null ? input.string("dataset") : "";
            String fieldName = input.get("field") != null ? input.string("field") : "";

            IoSession session = null;
            IoCommand.Statement statement = null;
            try {
                session = ctx.ioProvider().createSession("TDM");
 
                String tdmdbSchema = TDMSetBroadwayGlobals.resolveTdmdbSchema(ctx);
                statement = session.prepareStatement(queryInsert(tdmdbSchema));

                if (newTask) {
                    // New task execution (or first call in this JVM) - the cached values below are
                    // static and otherwise never expire, so they must be refreshed here or a later
                    // execution would silently inherit a previous execution's identifiers. Within a
                    // single execution entityTargetId is deliberately carried across field calls
                    // (set once the root instance field is seen, reused for every other field), so it
                    // is only reset on an execution change, not on every call.
                    sourceEnv = ctx.globals().get("TDM_SOURCE_ENVIRONMENT_NAME").toString();
                    entityTargetId = ctx.globals().get("TARGET_ENTITY_ID").toString();
                    luName = ctx.globals().get("LU_TYPE").toString();
                    rootTableName = ctx.globals().get("ROOT_TABLE_NAME").toString();
                    rootFieldName = ctx.globals().get("ROOT_COLUMN_NAME").toString();
                }

                String isInstance = "N";
                if (tableName.equalsIgnoreCase(rootTableName) && fieldName.equalsIgnoreCase(rootFieldName)) {
                    entityTargetId = output.string("value");
                    isInstance = "Y";
                }

                if (!"".equals(entityTargetId) && !"".equals(luName)) {
                    statement.execute(taskExecutionId,
                        luName,
                        sourceEnv,
                        entityTargetId,
                        input.string("sequenceId"),
                        tableName,
                        fieldName,
                        input.string("value"),
                        output.string("value"),
                        isInstance);
                }

            } catch (Exception e) {
                log.error("TDMMasking - failed to record sequence mapping (table=" + tableName + ", field=" + fieldName + ")", e);
                insertMaskingError(ctx, taskExecutionId, tableName, "tdmMasking",
                        "table=" + tableName + ";field=" + fieldName, ERROR_CATEGORY_SEQUENCES, e);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception closeEx) {
                        log.error("TDMMasking - failed to close statement: " + closeEx.getMessage());
                    }
                }
                safeClose(session);
            }
        }
    }

}
