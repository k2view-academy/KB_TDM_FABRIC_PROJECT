package com.k2view.cdbms.usercode.lu.TDM.TDM;

import com.k2view.cdbms.shared.Db;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.management.RuntimeErrorException;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.AI_ENVIRONMENT;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.SYNTHETIC_ENVIRONMENT;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;

import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDM;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.Logic.*;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.TASK_PROPERTIES.*;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.TASK_TYPES.EXTRACT;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.TASK_TYPES.LOAD;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class TdmExecuteTask {
    public static final Log log = Log.a(TdmExecuteTask.class);
    public static final String TDMDB = "TDM";
    public static final String TABLE_LEVEL_LU = "TDM_TableLevel";
    public static final String FABRIC = "fabric";
    public static final String INTERFACE = "interface";
    public static final String PARENT_LU = "parentLU";
    public static final String ROOT_LU = "rootLU";
    public static final String TABLES_TASK = "TABLES";

    public static final String PRE_EXECUTIONS = "Select t.process_id , t.process_name, t.execution_order, t.process_type from " +
            TDMDB_SCHEMA + ".tasks_exe_process t, " + TDMDB_SCHEMA + ".task_execution_list l " +
            "where l.task_execution_id = ? and l.process_id = t.process_id and upper(l.execution_status) = 'PENDING' and l.task_id = t.task_id and t.process_type = 'pre' " +
            "order by t.execution_order;";
    public static final String POST_EXECUTIONS = "Select t.process_id , t.process_name, t.execution_order, t.process_type from " +
            TDMDB_SCHEMA + ".tasks_exe_process t, " + TDMDB_SCHEMA + ".task_execution_list l " +
            "where l.task_execution_id = ? and l.process_id = t.process_id and upper(l.execution_status) = 'PENDING' and l.task_id = t.task_id and t.process_type = 'post' " +
            "order by t.execution_order;";
    public static final String EXECUTIONS_COUNT =  "select count(*) from " + TDMDB_SCHEMA + ".tasks_exe_process tt inner join " + TDMDB_SCHEMA + ".task_execution_list ll on tt.task_id=ll.task_id " +
            "where ll.task_execution_id =? and ll.process_id = tt.process_id and (? = -100 or tt.execution_order < ?) and tt.process_type = ? " +
            "and ll.execution_status NOT IN ('stopped','completed','failed','killed');";
    public static Map<String, String> entityInclusions = new HashMap<>();
    public static String sessionGlobals = "";
    public static String OriginalSyncMode = "";
    private static String loadAndReplace(String resourcePath) {
        try {
            return new String(getLuType().loadResource(resourcePath))
                    .replace("${@TDMDB_SCHEMA}", TDMDB_SCHEMA);
        } catch (Exception e) {
            log.error("Error loading resource: " + resourcePath, e);
            throw new RuntimeException("Error loading resource: " + resourcePath, e);
        }
    } 
    enum TASK_TYPES {
        GENERATE(() -> Util.rte(() -> new String(loadAndReplace("TDM/fnTdmExecuteTask/query_get_extract_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties)}),
        EXTRACT(() -> Util.rte(() -> new String(loadAndReplace("TDM/fnTdmExecuteTask/query_get_extract_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties)}),
        LOAD(() -> Util.rte(() -> new String(loadAndReplace("TDM/fnTdmExecuteTask/query_get_load_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties)}),
        RESERVE(() -> Util.rte(() -> new String(loadAndReplace("TDM/fnTdmExecuteTask/query_get_load_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties)}),
        TRAINING(() -> Util.rte(() -> new String(loadAndReplace("TDM/fnTdmExecuteTask/query_get_load_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties)}),
        AI_GENERATED(() -> Util.rte(() -> new String(loadAndReplace("TDM/fnTdmExecuteTask/query_get_load_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties)});

        Supplier<String> query;
        Function<Map<String, Object>, Object[]> params;

        TASK_TYPES(Supplier<String> query, Function<Map<String, Object>, Object[]> params) {
            this.query = query;
            this.params = params;
        }

        public String query() {
            return this.query.get();
        }

        public Object[] params(Map<String, Object> args) {
            return this.params.apply(args);
        }
    }

    public static void fnTdmExecuteTask() throws Exception {
        log.info("----------------- Starting tdmExecuteTask -------------------");
        String query = new String(loadAndReplace("TDM/fnTdmExecuteTask/query_get_tasks.sql"));
        Db tdmDB = db(TDM);
        tdmDB.fetch(query).forEach(row -> {
            // Get task properties
            Map<String, Object> taskProperties = Util.rte(() -> getTaskProperties(row));
            Long taskExecutionID = (Long) taskProperties.get("task_execution_id");
            Long taskID = (Long) taskProperties.get("task_id");
            Long luID = (Long) LU_ID.get(taskProperties);
            Long processID = (Long) taskProperties.get("process_id");
            OriginalSyncMode = SYNC_MODE.get(taskProperties);
            Boolean verticalExecution = "VERTICAL".equalsIgnoreCase(EXECUTION_MODE.get(taskProperties)) ? true : false; 
            //log.info("tdmExecuteTask - taskExecutionID: " + taskExecutionID + ", luID: " + luID + ", processID: " + processID);
            
            String startTime = "" + Util.rte(() -> db(TDM).fetch("select current_timestamp at time zone 'utc' ").firstValue());
            try {
                db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
                                "start_execution_time = ? " +
                                "WHERE task_execution_id = ? AND lu_id = ? AND process_id = ? AND LOWER(execution_status) = 'pending' and start_execution_time is null",
                                startTime, taskExecutionID, luID, processID);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Check for child LU- if the parent LU execution failed- do not execute the child LU. Instead- update the execution_status of the child LU by the status of the parent LU and continue to the next LU
            String parentLUStatus = PARENT_LU_STATUS.get(taskProperties);
            if (isChildLU(taskProperties) && !parentLUStatus.toUpperCase().equals("COMPLETED")) {
                updateTaskExecutionStatus(verticalExecution, parentLUStatus, taskExecutionID, luID, null, startTime, "19700101000000", null, null, null, null);
                return;
            }
            //updateTaskExecutionStatus("running", taskExecutionID,luID, "", "0", "0", "0", null);
            // Update task execution summary
            updateTaskExecutionSummary(taskExecutionID, "running");
            String selectionMethod = SELECTION_METHOD.get(taskProperties);
            String taskType = TASK_TYPE.get(taskProperties).toString().toLowerCase();
            // TDM 7.3 - The sync mode should be set at beginning based on task/env
            setSyncMode(taskProperties);

            //TDM 9.0 - Check if pre execution processes exist and run them
            try {
                Boolean preExecutions = RunExecutionsJob(taskExecutionID, taskType, "pre", taskProperties);
                if (preExecutions) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            // TDM 9.0 - Check if all pre execution processes where handled
            long count = (long)  Util.rte(() ->db(TDM).fetch(EXECUTIONS_COUNT, taskExecutionID, -100, -100, "pre").firstValue());
            if (count > 0) {
                return;
            }

            if ((long) LU_ID.get(taskProperties) != 0 && processID == 0) {
                String luName = LU_NAME.get(taskProperties).toString();
                if (Boolean.valueOf(PARAMS_COUPLING.get(taskProperties).toString()) 
                    && !TABLES_TASK.equalsIgnoreCase(selectionMethod) && !"".equalsIgnoreCase(luName)) {
                        if (!"VERTICAL".equalsIgnoreCase(EXECUTION_MODE.get(taskProperties))) {
                            executeMDBExportSchema(verticalExecution, luName, taskExecutionID, luID ,taskID, TDMDB_SCHEMA, "broadway " + luName + ".CreateMDBExportSchemaForParams luName=?, taskID = ?", "MDB SCHEMA EXPORT");
                        } else {
                            String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
                            if (Util.isEmpty(entityInclusion)) {
                                Db.Rows rows = Util.rte(() -> db(TDM).fetch("select lu_id, lu_name from " + TDMDB_SCHEMA + ".tasks_logical_units where task_id = "  + TASK_ID.get(taskProperties)));
                                for (Db.Row luRow : rows) {
                                    String luNameForMDB = luRow.get("lu_name").toString();
                                    Long luIdForMDB = Long.parseLong(luRow.get("lu_id").toString());
                                    String command = "broadway " + luNameForMDB + ".CreateMDBExportSchemaForParams luName=?";
                                    executeMDBExportSchema(verticalExecution, luNameForMDB, taskExecutionID, luIdForMDB ,taskID, TDMDB_SCHEMA, command, "MDB SCHEMA EXPORT");
                                }
                            }

                        }

                }
                if (("TRAINING".equalsIgnoreCase(taskType) || "ai_generated".equalsIgnoreCase(taskType)) && !"".equalsIgnoreCase(luName)) {
                    executeMDBExportSchema(verticalExecution, luName, taskExecutionID, luID,taskID, TDMDB_SCHEMA, "broadway " + luName + ".CreateMDBExportSchemaForAI luName=?", "MDB SCHEMA EXPORT");
                }
                switch (taskType) {
                    case "extract":
                    case "training":
                        log.info("----------------- extract task -------------------");
                        String versionExpDate = null;
                        try {
                            if (!selectionMethod.equalsIgnoreCase(TABLES_TASK)) {
                                Map<String, String> executionStatus = Util.rte(() -> executeExtractBatch(taskProperties));
                                String fabricExecutionId = executionStatus != null ? executionStatus.get("fabric_execution_id") : null;

                                if (!Util.isEmpty(fabricExecutionId)) {
                                    versionExpDate = executionStatus.get("expiration_date");
                                    updateTaskExecutionStatus(verticalExecution, "running", taskExecutionID, luID, fabricExecutionId, startTime, versionExpDate, null, null, null, null);
                                    //log.info("TdmExecuteTask - Calling executeTableLevelBatch");
                                    Map<String, String> tableExecutionStatus = Util.rte(() ->executeTableLevelBatch(taskProperties, false)); 

                                } else {
                                    // rollback LU and task status
                                    updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                                    updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
                                }
                            } else {
                                //log.info("TdmExecuteTask - Calling executeTableLevelBatch");
                                Map<String, String> tableExecutionStatus = Util.rte(()-> executeTableLevelBatch(taskProperties, true));
                                updateTaskExecutionBatchID(taskExecutionID, luID, tableExecutionStatus);
                            }

                        } catch (Exception e) {
                            log.error("TdmExecuteTask - Update extract task status to failed");
                            updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                            updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
                        }
                        break;
                    case "load":
                        log.info("----------------- load task -------------------");
                        try {
                            if (!selectionMethod.equalsIgnoreCase(TABLES_TASK)) {
                                Map<String, String> executionStatus = Util.rte(() -> executeLoadBatch(taskProperties));
                                String executionId = executionStatus != null ? executionStatus.get("fabric_execution_id") : null;
                                if (!executionId.isEmpty()) {
                                    String subsetExpDate = executionStatus.get("expiration_date");
                                    updateTaskExecutionStatus(verticalExecution, "running", taskExecutionID, luID, executionId, startTime, subsetExpDate, null, null, null, null);
                                    //log.info("TdmExecuteTask - Calling executeTableLevelBatch For Entities with REF");
                                    Map<String, String> tableExecutionStatus = Util.rte(() ->executeTableLevelBatch(taskProperties, false)); 
                                } else {
                                    // rollback LU and task status
                                    updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                                    log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
                                }
                            } else {
                                //log.info("TdmExecuteTask - Calling executeTableLevelBatch for Load REF");
                                Map<String, String> tableExecutionStatus = Util.rte(()-> executeTableLevelBatch(taskProperties, true));
                                updateTaskExecutionBatchID(taskExecutionID, luID, tableExecutionStatus);
                            }
                        } catch (Exception e) {
                            // rollback LU and task status
                            updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                            updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
                            log.error("Execution failed for task execution: " + taskExecutionID + " due to " + e.getMessage(), e);
                        }
                        break;
                    // TDM 9.0 - Support new task type delete
                    case "delete":
                        log.info("----------------- delete task -------------------");
                        try {
                            Map<String, String> executionStatus = Util.rte(() -> executeLoadBatch(taskProperties));
                            String executionId = executionStatus != null ? executionStatus.get("fabric_execution_id") : null;
                            if (!executionId.isEmpty()) {
                                updateTaskExecutionStatus(verticalExecution, "running", taskExecutionID, luID, executionId, startTime, "19700101000000", null, null, null, null);

                            } else {
                                // rollback LU and task status
                                updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                                log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
                            }
                        } catch (Exception e) {
                            // rollback LU and task status
                            updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                            log.error("Execution failed for task execution: " + taskExecutionID + " due to " + e.getMessage(), e);
                        }
                        break;
                    // TDM 7.4 - Support new task type revese
                    case "reserve":
                        log.info("----------------- reserve task -------------------");
                        try {
                            // run broadway flow
                            Map<String, String> executionStatus = Util.rte(() -> executeReserveBatch(taskProperties));
                            String executionId = executionStatus != null ? executionStatus.get("fabric_execution_id") : null;
                            if (!executionId.isEmpty()) {
                                String reserveExpDate = executionStatus.get("expiration_date");
                                // In case of hierarchy only one LU (root) will be reserved, any Child LU will be ignored
                                if ("NA".equals(executionId)) {
                                    updateTaskExecutionStatus(verticalExecution, "completed", taskExecutionID, luID, null, startTime,reserveExpDate, null, null, null, startTime);
                                    return;
                                }
                                updateTaskExecutionStatus(verticalExecution, "running", taskExecutionID, luID, executionId, startTime, reserveExpDate, null, null, null, null);

                            } else {
                                // rollback LU and task status
                                updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                                log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
                            }
                        } catch (Exception e) {
                            // rollback LU and task status
                            updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                            updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
                            log.error("Execution failed for task execution: " + taskExecutionID + " due to " + e.getMessage(), e);
                        }
                        break;
                    // TDM 8.0 - Support new task type generate synthetic entites
                    case "generate":
                        log.info("----------------- generate synthetic task -------------------");
                        try {
                            Map<String, String> executionStatus = Util.rte(() -> executeGenerateBatch(taskProperties));
                            String fabricExecutionId = executionStatus != null ? executionStatus.get("fabric_execution_id") : null;

                            if (!Util.isEmpty(fabricExecutionId)) {
                                String subsetExpDate = executionStatus.get("expiration_date");
                                updateTaskExecutionStatus(verticalExecution, "running", taskExecutionID, luID, fabricExecutionId, startTime, subsetExpDate, null, null, null, null);

                            } else {
                                // rollback LU and task status
                                updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                                log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
                            }

                        } catch (Exception e) {
                            // rollback LU and task status
                            updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                            updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
                            log.error("Execution failed for task execution: " + taskExecutionID + " due to " + e.getMessage(), e);
                        }
                        break;
                    case "ai_generated":
                        log.info("----------------- generate data using AI-ML -------------------");
                        try {
                            // Execute generate subset
                            Map<String, String> executionStatus = Util.rte(() -> executeGenerateSubset(taskProperties));
                            String fabricExecutionId = executionStatus != null ? executionStatus.get("fabric_execution_id") : null;

                            if (Util.isEmpty(fabricExecutionId)) {
                                // Rollback LU and task status if execution failed
                                updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                                log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
                                break;
                            }
                            // Update task execution status
                            String subsetExpDate = executionStatus.get("expiration_date");
                            
                            updateTaskExecutionStatus(verticalExecution, "running", taskExecutionID, luID, fabricExecutionId, startTime, subsetExpDate, null, null, null, null);

                        } catch (Exception e) {
                            // Rollback LU and task status if an exception occurred
                            updatedFailedStatus(verticalExecution, taskExecutionID, luID);
                            updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
                            log.error("Execution failed for task execution: " + taskExecutionID + " due to " + e.getMessage(), e);
                        }
                        break;
                    default:
                        log.error("Unknown task type '" + taskType + "'");
                }

            } else {

                long luCount = (long) Util.rte(() -> db(TDM).fetch("SELECT COUNT(lu_id) FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? AND (lu_id != 0 AND process_id=0) AND execution_status NOT IN ('stopped','completed','failed','killed');", taskExecutionID).firstValue());
                if (luCount == 0) {
                    //log.info("************* lu count = 0 starting post executions *************");
                    // TDM 7.4 - 19.01.22 - Set globals at task level instead of instance level
                    try {
                        setGlobalsForTask(taskType, taskProperties);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Boolean postExecutions = RunExecutionsJob(taskExecutionID, taskType, "post", taskProperties);
                        if (postExecutions) {
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

            }
        });
    }

    private static Map<String, String> executeExtractBatch(Map<String, Object> taskProperties) throws Exception {
        try {
            // TDM 7.4 - 19.01.22 - Set globals at task level instead of instance level
            String version_exp_date = setTTL(taskProperties);
            Map<String, String> ExecutionInfo = new LinkedHashMap<>();
            if ("true".equals(VERSION_IND.get(taskProperties)) || "TRAINING".equalsIgnoreCase("" + TASK_TYPE.get(taskProperties))) {
                ExecutionInfo.put("expiration_date", version_exp_date);
                db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
                               "expiration_date = TO_TIMESTAMP(COALESCE(?, '19700101000000'), 'YYYYMMDDHH24MISS') where task_execution_id = ? AND lu_id =? AND process_id <= 0 " , version_exp_date , "" + TASK_EXECUTION_ID.get(taskProperties), "" + LU_ID.get(taskProperties));
            } else {
                ExecutionInfo.put("expiration_date", "19700101000000");
            }
            setGlobalsForTask("extract", taskProperties);
            String syncMode = SYNC_MODE.get(taskProperties);
            String sourceEnvName = SOURCE_ENVIRONMENT_NAME.get(taskProperties);
            String selectionMethod = SELECTION_METHOD.get(taskProperties);
            String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
            String luName = LU_NAME.get(taskProperties);
            boolean isParamCoupling = Boolean.TRUE.equals(PARAMS_COUPLING.get(taskProperties));

            //log.info("executeExtractBatch - luName: " + LU_NAME.get(taskProperties) + ", isChild: " + isChildLU(taskProperties));
            //log.info("executeExtractBatch - TDM_DATAFLUX_TASK: " + fabric().fetch("set TDM_DATAFLUX_TASK").firstValue());
            String entityInclusionOverride = "";

            if (!isChildLU(taskProperties)) {
                String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
                if (Util.isEmpty(entityInclusion)) {
                    entityInclusionOverride = getEntityInclusion(taskProperties);
                } else { //the task execution has several root LUs, and if the entity inclusion was already populated for the previous root LU it will be reused
                    entityInclusionOverride = entityInclusion;
                }
            } else {// the parent id is populated- handle the child luID
                entityInclusionOverride = getEntityInclusionForChildLU(taskProperties, luName);
            }
            String entityInclusionInterface = entityInclusions.getOrDefault(INTERFACE, TDMDB);
            String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
            String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";

            String batchCommand = "";
            if ("L".equalsIgnoreCase(selectionMethod) && !isChildLU(taskProperties)) {
                batchCommand = "BATCH " + luName + ".(?) fabric_command=? with " + affinity + " async=true";
            } else {
                batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
            }

            if (!"ON".equalsIgnoreCase(syncMode)) {
                fabric().execute("SET SYNC " + syncMode);
            }
            String parentLuName = entityInclusions.getOrDefault(PARENT_LU, "");

            String broadwayCommand = "broadway " + luName + ".TDMExtractOrchestrator " + "iid=?, luName=" + luName +
                ", syncMode=\"" + syncMode + "\", isParamCoupling=" +isParamCoupling + ", taskExecutionId = " + taskExecutionID + ", parentLuName=\"" + parentLuName + "\"";
            //log.info("batchCommand: " + batchCommand + ", broadwayCommand: " + broadwayCommand);
            // TDM 8.1 Call function to set TTL
            //Calculate retention date + set TTL

            //Check if param table does not exist and create it, and if it exists, check if its structure is correct
            //It will check only if it is not a versioning task
            if(!isParamCoupling && "false".equalsIgnoreCase(VERSION_IND.get(taskProperties))) {
                fnCreateUpdateLUParamsForTask(taskProperties);
            }

            String batchID = (String) fabric().fetch(batchCommand, entityInclusionOverride, broadwayCommand).firstValue();
            ExecutionInfo.put("fabric_execution_id", batchID);

            return ExecutionInfo;
        } catch (Exception e) {
            log.error("Can't run extract for task_execution_id=" + TASK_EXECUTION_ID.get(taskProperties), e);
            return null;
        }
    }

    private static Map<String, String> executeLoadBatch(Map<String, Object> taskProperties) throws Exception {
        //log.info("----------------- preparing for load execution -------------------");

        // TDM 7.4 - 19.01.22 - Set globals at task level instead of instance level
        //String srcSyncData = getSrcSyncDataVal(taskProperties);
        //Map<String,Object> globals = getGlobals(LOAD.query(), taskProperties, Util.map("TDM_SYNC_SOURCE_DATA", srcSyncData), LOAD.params(taskProperties));
        setGlobalsForTask("load", taskProperties);
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        String selectionMethod = SELECTION_METHOD.get(taskProperties);
        String luName = LU_NAME.get(taskProperties);
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String selectedSubsetExeID = "" + SELECTED_SUBSET_TASK_EXE_ID.get(taskProperties);
        String selectedVersionExeID = "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
        String envName = "" + SOURCE_ENVIRONMENT_NAME.get(taskProperties);
        String entityInclusionOverride = "";
        boolean isParamCoupling = Boolean.TRUE.equals(PARAMS_COUPLING.get(taskProperties));
        String syncMode = getSyncModeForLoad(taskProperties);
        Boolean cloneInd = CLONE_IND.get(taskProperties);
        String luID = "" + LU_ID.get(taskProperties);
        boolean isReserve = "true".equalsIgnoreCase(RESERVE_IND.get(taskProperties).toString());

        // TDM 8.1 Call fucntion to set TTL
        //Calculate retention date + set TTL

        String expDate = setTTL(taskProperties);
        if (isReserve || ((SYNTHETIC_ENVIRONMENT.equalsIgnoreCase(envName)) || AI_ENVIRONMENT.equalsIgnoreCase(envName)) && !("GENERATE_SUBSET".equalsIgnoreCase(selectionMethod))) {
            ExecutionInfo.put("expiration_date", expDate);
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
            "expiration_date = TO_TIMESTAMP(COALESCE(?, '19700101000000'), 'YYYYMMDDHH24MISS') where task_execution_id = ? AND lu_id =? AND process_id <= 0 " , expDate , taskExecutionID, luID);
        } else {
            ExecutionInfo.put("expiration_date", "19700101000000");
        }
        Boolean reserveInd = true;
        //log.info("executeFabricBatch - luName: " + luName + ", isChild: " + isChildLU(taskProperties));
        // check the selection method only for root LUs. Build only once the root selection method per task execution
        if (!isChildLU(taskProperties)) {
            String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
            // TDM 7.4 - In case of reservation, the reservation will be done in one LU only
            //entityInclusionOverride = Util.isEmpty(entityInclusion) ? getEntityInclusion(taskProperties) : entityInclusion; //the task execution has several root LUs, and if the entity inclusion was already populated for the previous root LU it will be reused
            if (Util.isEmpty(entityInclusion)) {
                entityInclusionOverride = getEntityInclusion(taskProperties);
            } else { //the task execution has several root LUs, and if the entity inclusion was already populated for the previous root LU it will be reused
                entityInclusionOverride = entityInclusion;
                reserveInd = false;

            }
        } else {// the parent id is populated- handle the child luID
            entityInclusionOverride = getEntityInclusionForChildLU(taskProperties, luName);
            // TDM 7.4 - Reservation will be done only at Root LU
            reserveInd = false;
        }

        String parentLuName = entityInclusions.getOrDefault(PARENT_LU, "");
         //log.info("parentLuName - " + parentLuName);
        //Check if param table does not exist and create it, and if it exists, check if its structure is correct
        //It will be check only if the task may get new data from source and it is not a versioning task
        if(!isParamCoupling && (!"OFF".equalsIgnoreCase(syncMode) && "false".equalsIgnoreCase(VERSION_IND.get(taskProperties)))) {
            fnCreateUpdateLUParamsForTask(taskProperties);
        }
        //log.info(" entity inclusion: " + entityInclusionOverride);
        // TDM 7.4 - For Custom Logic the source DB is Cassandra
        //TDM 8.1 - TDMDB is used for all entity list tables.
        String entityInclusionInterface = entityInclusions.getOrDefault(INTERFACE, TDMDB);

        // TDM 7.3 - If the selection method is cloning data , then the sync of entities will be executed before calling the broadway to make sure each orignal instance is sync at most once
        if (cloneInd) {
            //log.info("executeFabricBatch - Handling Cloning Entity");
            syncInstanceForCloning(entityInclusionOverride, taskProperties);
            syncMode = "off";
        }

        String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
        String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";

        //TDM 7.4 - For Custom Logic the source DB is Cassandra
        // In case of entity list, the batch command will be different as it gets a entity list and not an SQL statement
        //String batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
        String batchCommand = "";
        if ("L".equalsIgnoreCase(selectionMethod) && !isChildLU(taskProperties) && !cloneInd) {
            batchCommand = "BATCH " + luName + ".(?) fabric_command=? with " + affinity + " async=true";
        } else {
            batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
        }

        String broadwayCommand = "broadway " + luName + ".TDMOrchestrator " + "iid=?, luName=" + luName +
            ", syncMode=\"" + syncMode + "\"" + ", reserveInd=" + reserveInd + 
            ", isParamCoupling=" + isParamCoupling + ", taskExecutionId = " + taskExecutionID + ", parentLuName=\"" + parentLuName + "\"";

        //log.info("Starting batch command: " + batchCommand);
        //log.info("Starting broadway command: " + broadwayCommand);

        String batchID = "" + fabric().fetch(batchCommand, entityInclusionOverride, broadwayCommand).firstValue();
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;
    }

    private static Map<String, String> executeReserveBatch(Map<String, Object> taskProperties) throws Exception {
        //log.info("----------------- preparing for reserve execution -------------------");

        // TDM 7.4 - 19.01.22 - Set globals at task level instead of instance level

        setGlobalsForTask("reserve", taskProperties);
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        String selectionMethod = SELECTION_METHOD.get(taskProperties);
        String luName = LU_NAME.get(taskProperties);
        Long luID = (Long) LU_ID.get(taskProperties);
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String entityInclusionOverride = "";
        //log.info("executeFabricBatch - luName: " + luName + ", isChild: " + isChildLU(taskProperties));
        // check the selection method only for root LUs. Build only once the root selection method per task execution
        String expDate = setTTL(taskProperties);
        ExecutionInfo.put("expiration_date", expDate);
        //TDM 7.4 - In case of Reserve only on Root LU will be processed, and all child LUs will be ignored
        if (!isChildLU(taskProperties)) {
            String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
            if (Util.isEmpty(entityInclusion)) {
                entityInclusionOverride = getEntityInclusion(taskProperties);
            } else {// in case the Lu has two roots
                ExecutionInfo.put("fabric_execution_id", entityInclusions.getOrDefault("batchID", null));
                return ExecutionInfo;
            }
        } else {// the parent id is already populated-handle the child luID
            // No need to run the LU
            ExecutionInfo.put("fabric_execution_id", "NA");
            return ExecutionInfo;
        }
        String entityInclusionInterface = entityInclusions.getOrDefault(INTERFACE, TDMDB);

        //log.info(" entity inclusion: " + entityInclusionOverride);

        String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
        String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";

        // In case of entity list, the batch command will be different as it gets a entity list and not an SQL statement
        //String batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
        String batchCommand = "";
        Boolean cloneInd = CLONE_IND.get(taskProperties);
        if ("L".equalsIgnoreCase(selectionMethod) && !isChildLU(taskProperties) && !cloneInd) {
            batchCommand = "BATCH " + luName + ".(?) fabric_command=? with " + affinity + " async=true";
        } else {
            batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
        }
        String parentLuName = "";
        String broadwayCommand = "broadway " + luName + ".TDMReserveOrchestrator " + "iid=?, luName=" + luName +
        ", taskExecutionId=" + taskExecutionID + ", parentLuName=\"" + parentLuName + "\"";

        //log.info("Starting batch command: " + batchCommand);
        //log.info("Starting broadway command: " + broadwayCommand);
        //log.info("entityInclusionOverride: " + entityInclusionOverride);

        String batchID = (String) fabric().fetch(batchCommand, entityInclusionOverride, broadwayCommand).firstValue();
        entityInclusions.put("batchID", batchID);
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;
    }

    private static Map<String, String> executeGenerateBatch(Map<String, Object> taskProperties) throws Exception {

        setGlobalsForTask("generate", taskProperties);
        String luName = LU_NAME.get(taskProperties);
        String entityInclusionOverride = "";
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String batchDB = TDM;
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        boolean isParamCoupling = Boolean.TRUE.equals(PARAMS_COUPLING.get(taskProperties));
        //entityInclusionOverride = getEntityInclusion(taskProperties);

        if (!isChildLU(taskProperties)) {
            String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
            if (Util.isEmpty(entityInclusion)) {
                entityInclusionOverride = getEntityInclusion(taskProperties);
            } else { //the task execution has several root LUs, and if the entity inclusion was already populated for the previous root LU it will be reused
                entityInclusionOverride = entityInclusion;
                batchDB = entityInclusions.getOrDefault(INTERFACE, TDMDB);
            }
        } else { // the parent id is populated-handle the child luID
            entityInclusionOverride = getEntityInclusionForChildLU(taskProperties, luName);
            batchDB = entityInclusions.getOrDefault(INTERFACE, TDMDB);
        }
        String subset_expiration_date = setTTL(taskProperties);

        String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
        String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";
        String batchCommand = "BATCH " + luName + " FROM " + batchDB + " USING(?) fabric_command=? with " + affinity + " async=true";
        String parentLuName = entityInclusions.getOrDefault(PARENT_LU, "");
        String broadwayCommand = "broadway " + luName + ".TDMGenerateOrchestrator " + "iid=?, luName=" + luName
                + ", syncMode=FORCE , isParamCoupling = " + isParamCoupling +
                ", taskExecutionId=" + taskExecutionID + ", parentLuName=\"" + parentLuName + "\"";
        //log.info("batchCommand: " + batchCommand + " ,broadwayCommand: " + broadwayCommand);

        //Check if param table does not exist and create it, and if it exists, check if its structure is correct
        if(!isParamCoupling){
            fnCreateUpdateLUParamsForTask(taskProperties);
        }
        String batchID = (String) fabric().fetch(batchCommand, entityInclusionOverride, broadwayCommand).firstValue();
        ExecutionInfo.put("fabric_execution_id", batchID);
        ExecutionInfo.put("expiration_date", subset_expiration_date);
        return ExecutionInfo;
    }
    
     private static Map<String, String> executeTableLevelBatch(Map<String, Object> taskProperties, Boolean tableLevelInd) throws Exception {
        try {
            Map<String, String> ExecutionInfo = new LinkedHashMap<>();
            // TDM9.1 check if the task includes table level
            String selectionMethod = SELECTION_METHOD.get(taskProperties);
            if (!TABLES_TASK.equalsIgnoreCase(selectionMethod)) {
                Object handleTables = db(TDM).fetch("SELECT task_ref_table_id from " + TDMDB_SCHEMA + ".task_ref_tables where task_id = " + 
                TASK_ID.get(taskProperties) + " limit 1").firstValue();

                if(handleTables == null) {
                    return null;
                }
            }

            //TDM 9.0 - HF1, check if the Table Level already ran or not
            String filter = "TableLevelJob iid=?, taskExecutionId=" + TASK_EXECUTION_ID.get(taskProperties);

            Object batchId = fabric().fetch("batch_list status='ALL' filter = '" + filter + "'").firstRow().get("Id");

            if (batchId != null) {
                return null;
            }

            String taskType = TASK_TYPE.get(taskProperties).toString().toLowerCase();
            if ("extract".equals(taskType)) {
                setGlobalsForTask("extract", taskProperties);
            } else {
                setGlobalsForTask("load", taskProperties);
            }
            // TDM 9.1.6 set TTL for table level 
            String version_exp_date = setTTL(taskProperties);
            ExecutionInfo.put("expiration_date", version_exp_date);
            String batchCommand = "BATCH " + TABLE_LEVEL_LU + ".(?) fabric_command=? with async=true";
            String broadwayCommand = "broadway " + TABLE_LEVEL_LU + ".TableLevelJob iid=?, " + 
                "taskExecutionId=" + TASK_EXECUTION_ID.get(taskProperties) +",syncMode=\"" + OriginalSyncMode + 
                "\", taskType=" + taskType + ", deleteBeforeLoad=" + DELETE_BEFORE_LOAD.get(taskProperties) +
                ", loadEntity=" + LOAD_ENTITY.get(taskProperties) + ", tableLevelInd=" + tableLevelInd + 
                ", retentionPeriodValue=\"" + RETENTION_PERIOD_VALUE.get(taskProperties) + "\"";


            //log.info("executeTableLevelBatch - batchCommand: " + batchCommand);
            //log.info("executeTableLevelBatch - broadwayCommand: " + broadwayCommand);
            String batchID = (String)fabric().fetch(batchCommand, TABLE_LEVEL_LU + "_" + TASK_EXECUTION_ID.get(taskProperties), broadwayCommand).firstValue();
            ExecutionInfo.put("fabric_execution_id", batchID);
            return ExecutionInfo ;
        } catch (Exception e) {
            log.error("Can't run Table Level for task_execution_id=" +  TASK_EXECUTION_ID.get(taskProperties), e);
            return null;
        }
    }

    private static String buildEntityListForTables(Map<String, Object> taskProperties) throws Exception{
        String taskType = TASK_TYPE.get(taskProperties).toString().toLowerCase();
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String selectedVersionTaskExeId = "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
        String sourceEnvName = SOURCE_ENVIRONMENT_NAME.get(taskProperties);
        String instanceList = "";

        // TDM 9.0 - Set the environment before.
        fabric().execute("set environment " + sourceEnvName);

        if ("extract".equals(taskType) || "0".equals(selectedVersionTaskExeId)) {
            selectedVersionTaskExeId = taskExecutionID;
        }
        
        String sql = "SELECT '" + sourceEnvName + "_'" + "||es.task_ref_table_id||'_" + selectedVersionTaskExeId + "' as entity_id " +
            "FROM " + TDMDB_SCHEMA + ".TASK_REF_EXE_STATS es, " + TDMDB_SCHEMA + ".TASKS t " + 
            "WHERE lower(es.execution_status) = 'pending' " +
            "AND es.task_id = t.task_id AND lower(t.task_type) = ? AND es.task_execution_id = ?";
        
        //log.info("buildEntityListForTables - sql: " + sql);
        Db.Rows tableInsances = db(TDM).fetch(sql, taskType, taskExecutionID);
        String separator = "";

        for (Db.Row table : tableInsances) {
            instanceList += separator + table.get("entity_id");
            separator = ",";
        }
        return instanceList;
    }

    private static String getCLBroadwayCmd(String luName, String clFlowName, Map<String, Object> taskProperties) {
        //String luName = LU_NAME.get(taskProperties);
        Long entitiesLimit = 0L;
        if (NUM_OF_ENTITIES.get(taskProperties) instanceof Long) {
            entitiesLimit = NUM_OF_ENTITIES.get(taskProperties);
        } else {
            entitiesLimit = Long.valueOf(NUM_OF_ENTITIES.get(taskProperties));
        }
        //String clFlowName = SELECTION_PARAM_VALUE.get(taskProperties);
        String clFlowParams = PARAMETERS.get(taskProperties);

        clFlowParams = clFlowParams.replaceAll("\\\\n", "").replaceAll("\\\\t", "");
        //log.info("clFlowParams after replace: " + clFlowParams);
        // Replace gson with K2view Json
        //Gson gson = new Gson();
        //Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>(){}.getType();
        //Map<String, List<Map <String, Object>>> clFlowParamJson = gson.fromJson(clFlowParams, mapType);
        Map<String, List<Map<String, Object>>> clFlowParamJson = "".equals(clFlowParams) ? Collections.emptyMap() : Json.get().fromJson(clFlowParams);

        String fabricCommandParams = " LU_NAME='" + luName + "', NUM_OF_ENTITIES=" + entitiesLimit;
        if (!(clFlowParamJson==null || clFlowParamJson.isEmpty())) {
            List<Map<String, Object>> clFlowParamList = clFlowParamJson.get("inputs");
            for (Map<String, Object> clFlowParamMap : clFlowParamList) {
                String paramValue = "" + clFlowParamMap.get("value");
                fabricCommandParams += ", " + clFlowParamMap.get("name") + "=\"" + paramValue + "\"";
                //fabricCommandParams += ", " + clFlowParamMap.get("name") + "=" + paramValue;
            }
        }

        return fabricCommandParams;

    }

    private static Map<String, String> executeGenerateSubset(Map<String, Object> taskProperties) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        setGlobalsForTask("ai_generated", taskProperties);
        String luName = LU_NAME.get(taskProperties);
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String dcName = "" + DATA_CENTER_NAME.get(taskProperties);
        String LuID = "" + LU_ID.get(taskProperties);
        String generation_exp_date = setTTL(taskProperties);
        boolean isParamCoupling = Boolean.TRUE.equals(PARAMS_COUPLING.get(taskProperties));
        db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
            "expiration_date = TO_TIMESTAMP(COALESCE(?, '19700101000000'), 'YYYYMMDDHH24MISS') where task_execution_id = ? AND lu_id =? AND process_id <= 0 " , generation_exp_date , taskExecutionID, LuID);
        String loadIndicator= "" + LOAD_ENTITY.get(taskProperties);
        String beID= "" + BE_ID.get(taskProperties);

        String broadwayCommand = "broadway TDM.ImportDataSubset " + "luName = '" + luName + "'" +
                ", dcName='" + dcName + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", loadIndicator='" + loadIndicator + "'" +
                ", beID='" + beID + "'" +
                ", LuID='" + LuID +"' ,isParamCoupling = " + isParamCoupling ;

        //Check if param table does not exist and create it, and if it exists, check if its structure is correct
        if(!isParamCoupling){
            fnCreateUpdateLUParamsForTask(taskProperties);
        }
        Db.Rows rows = fabric().fetch(broadwayCommand);
        String batchID = null;
        for(Db.Row row:rows){
            batchID="" + row.get("batchID");
        }
        ExecutionInfo.put("fabric_execution_id", batchID);
        ExecutionInfo.put("expiration_date", generation_exp_date);
        return ExecutionInfo;
    } 

    private static Boolean RunExecutionsJob(Long taskExecutionID, String taskType, String processType, Map<String, Object> taskProperties) throws Exception {
        //log.info("RunExecutionsJob Started with processType: " + processType);
        Integer numberOfPending = 0;
        Integer numberOFRunning = 0;
        String env = isDeleteOnlyMode(taskProperties) ? TARGET_ENVIRONMENT_NAME.get(taskProperties) : SOURCE_ENVIRONMENT_NAME.get(taskProperties);
        fabric().execute("set environment " + env);

        String sql =  "select ll.execution_status, count(*) as cnt from " + TDMDB_SCHEMA + ".tasks_exe_process tt inner join " +
            TDMDB_SCHEMA + ".task_execution_list ll on tt.task_id=ll.task_id " +
            "where ll.task_execution_id =? and ll.process_id = tt.process_id and tt.process_type = ? " +
            "and ll.execution_status NOT IN ('stopped','completed','failed','killed') " +
            "group by ll.execution_status";

        Db.Rows rows = db(TDM).fetch(sql, taskExecutionID, processType);
        for (Db.Row row : rows) {
            String executionStatus = row.get("execution_status").toString().toLowerCase();
            switch (executionStatus) {
                case "pending":
                    numberOfPending = Integer.parseInt(row.get("cnt").toString());
                    break;
                case "running":
                    numberOFRunning = Integer.parseInt(row.get("cnt").toString());
                    break;
                default:
                    break;
            }
        }

        if (rows != null) {
            rows.close();
        }

        Db.Row jobSts = fabric().fetch("jobstatus USER_JOB 'TDM.tdmProcessExecution'").firstRow();
        Boolean stillRunning = true;

        if (jobSts == null || jobSts.isEmpty()) {
            //log.info("JOB NOT RUNNING");
            stillRunning = false;
        }
        //log.info("RunExecutionsJob - numberOfPending: " + numberOfPending + ", numberOFRunning: " + numberOFRunning);
        if (numberOfPending == 0 && numberOFRunning == 0) {// No process to handle and nothing is currently running
            return false;
        }

        if ( stillRunning) {// Still running
            return true;
        }
        
        //Run the batch to handle all process type executions
       
        String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
        String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";
        setGlobalsForTask(taskType, taskProperties);
        Map<?,?> globals = Json.get().fromJson(sessionGlobals, Map.class);
        String globalsStr = globals.toString();
        globalsStr = globalsStr.replaceAll("\\\\n","").replaceAll("\\\\t","").replaceAll(", ", ",");
        String args = " ARGS ='{\"taskExecutionID\":\"" + taskExecutionID + "\",\"processType\":\"" + processType +
            "\",\"sessionGlobals\":\"" + globalsStr + "\",\"numOfEntities\":\"" + NUM_OF_ENTITIES.get(taskProperties) +  
            "\",\"subsetID\":\"" + SELECTED_SUBSET_TASK_EXE_ID.get(taskProperties) + "\",\"luID\":\"" + LU_ID.get(taskProperties) + "\"}'";

        String jobCommand = "startjob USER_JOB NAME='TDM.tdmProcessExecution' UID='tdmProcessExecution_" + taskExecutionID + "' " + affinity  + args;
        fabric().execute(jobCommand);

        return true;
    }
    
    public static void updatedFailedStatus(Boolean verticalExecution, Long taskExecutionID, Long luID) {
        Timestamp endTime = (Timestamp) Util.rte(() -> db(TDM).fetch("select current_timestamp at time zone 'utc' ").firstValue());
        updateTaskExecutionStatus(verticalExecution, "failed",taskExecutionID, luID, null,endTime, "19700101000000",0,0,0,endTime);
        updateTaskExecutionSummary(taskExecutionID, "failed");
    }
    public static void updatedAIFailedStatus(String status, Long taskExecutionID, Long luID) {
        updateAITaskExecutionStatus( status,taskExecutionID, luID, null, null,0,0,0);
        updateTaskExecutionSummary(taskExecutionID, status);
        updateTaskExecutionsK2system(taskExecutionID,status);
    }
    private static boolean isChildLU(Map<String, Object> taskProperties) {
        Long parentID = PARENT_LU_ID.get(taskProperties);
        return parentID != null && parentID > 0;
    }

   private static String setTTL(Map<String, Object> taskProperties) throws Exception {
        String retentionPeriodType;
        float retentionPeriodValue;
        Long unixTime = System.currentTimeMillis();
        Long unixTime_plus_retention;
        String version_exp_date = null;
        String timeStamp = null;
        String query = "select TO_CHAR(creation_date, 'YYYYMMDDHH24MISS') AS formatted_creation_date from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id=? limit 1 ";
        Object date = db(TDM).fetch(query, " " + TASK_EXECUTION_ID.get(taskProperties)).firstValue();
        String versionDateTime = "" + date;
        String taskType = TASK_TYPE.get(taskProperties);
        boolean isReserve = "true".equalsIgnoreCase(RESERVE_IND.get(taskProperties).toString());
        if(isReserve){
            retentionPeriodType = RESERVE_RETENTION_PERIOD_TYPE.get(taskProperties);
            retentionPeriodValue= Float.parseFloat(RESERVE_RETENTION_PERIOD_VALUE.get(taskProperties).toString());
        }else{
            retentionPeriodType = RETENTION_PERIOD_TYPE.get(taskProperties);
            retentionPeriodValue = Float.parseFloat(RETENTION_PERIOD_VALUE.get(taskProperties).toString());
        }

        if (retentionPeriodType != null && !retentionPeriodType.isEmpty()) {
            Integer retention_in_seconds = getRetention(retentionPeriodType, retentionPeriodValue);
            if (versionDateTime != null && !versionDateTime.isEmpty()) {
                timeStamp = versionDateTime;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                Date timeStampDate = sdf.parse(timeStamp);
                long millis = timeStampDate.getTime();
                unixTime_plus_retention = (millis / 1000L + retention_in_seconds) * 1000;
            } else {
                timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime);
                unixTime_plus_retention = (unixTime / 1000L + retention_in_seconds) * 1000;
            }
            version_exp_date = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime_plus_retention);
            //Set TTL
            boolean isRetention = retention_in_seconds != -1;
            boolean isLoadTask = "LOAD".equalsIgnoreCase(taskType);
            boolean isVersionTask = "true".equalsIgnoreCase(VERSION_IND.get(taskProperties).toString());
            if (isRetention && !(isLoadTask && isVersionTask)) {
                ludb().execute("SET INSTANCE_TTL = " + retention_in_seconds);
            } else {
                version_exp_date = "999912310000";
            }
        }
        return version_exp_date;
    }

    private static String getEntityInclusionForChildLU(Map<String, Object> taskProperties, String luName) throws Exception {
        //log.info("getEntityInclusionForChildLU - handling Child LU: " + luName);
        String parentLU = "" + db(TDM).fetch("SELECT lu_parent_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE lu_id=?", (Object) LU_ID.get(taskProperties)).firstValue();
        String entityIdSelectChildID = "t.entity_id";

        String versionClause;
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String selectedVersionTaskExeId = "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
        String versionInd = "" + VERSION_IND.get(taskProperties);
        String entityInclusion;
        String selectionMethod = ((String) SELECTION_METHOD.get(taskProperties)).toUpperCase();
        Boolean cloneInd = CLONE_IND.get(taskProperties);
        String versionTaskExeID = "0";
        String env = isDeleteOnlyMode(taskProperties) ? TARGET_ENVIRONMENT_NAME.get(taskProperties) : SOURCE_ENVIRONMENT_NAME.get(taskProperties);
        // TDM 9.0 - Set the environment before to make sure in case of BF to have the right interfaces are set.
        fabric().execute("set environment " + env);

        if ("true".equalsIgnoreCase(versionInd)) {
            if ("0".equalsIgnoreCase(selectedVersionTaskExeId)) {
                //entityIdSelectChildID += "||'" + SEPARATOR.get(taskProperties) + "'||t.task_execution_id";
                versionTaskExeID = taskExecutionID;
            } else {
                //entityIdSelectChildID += "||'" + SEPARATOR.get(taskProperties) + "'||" + selectedVersionTaskExeId;
                versionTaskExeID = selectedVersionTaskExeId ;

            }
        }
        versionClause = " and t.version_task_execution_id = " + versionTaskExeID;

        if (cloneInd) {
            entityIdSelectChildID = entityIdSelectChildID + "||'#params#{\"clone_id\" : '||generate_series(1, " + NUM_OF_ENTITIES.get(taskProperties) + " )||'}'";
        }

        entityInclusion = "select distinct " + entityIdSelectChildID + " child_entity_id FROM " + TDMDB_SCHEMA + ".task_execution_entities t " +
                "where task_execution_id=" + taskExecutionID + " and execution_status = 'pending' and lu_name = '" + luName + 
                "' and parent_lu_name = '" + parentLU + "' " + versionClause;
        
        //log.info("getEntityInclusionForChildLU - entityInclusion: " + entityInclusion);
        entityInclusions.put(INTERFACE, TDMDB);
        entityInclusions.put(PARENT_LU, parentLU);
        return entityInclusion;
    }

    private static boolean isDeleteOnlyMode(Map<String, Object> taskProperties) throws Exception {
        String insetToTarget = LOAD_ENTITY.get(taskProperties);
        String deleteBeforeLoad = DELETE_BEFORE_LOAD.get(taskProperties);
        return !Util.isEmpty(deleteBeforeLoad) && deleteBeforeLoad.equals("true") && (Util.isEmpty(insetToTarget) || !Util.isEmpty(insetToTarget) && insetToTarget.equals("false"));
    }

    private static boolean isDeleteAndLoad(Map<String, Object> taskProperties) {
        String insetToTarget = LOAD_ENTITY.get(taskProperties);
        String deleteBeforeLoad = DELETE_BEFORE_LOAD.get(taskProperties);
        return !Util.isEmpty(deleteBeforeLoad) && deleteBeforeLoad.equals("true") && !Util.isEmpty(insetToTarget) && insetToTarget.equals("true");
    }

    private static String getEntityInclusion(Map<String, Object> taskProperties) throws Exception {
        try {
            String entitiesList = SELECTION_PARAM_VALUE.get(taskProperties);
            //log.info("creating entity inclusion for root lu with entitiesList: " + entitiesList);
            String listOfMatchingEntities;
            String env = isDeleteOnlyMode(taskProperties) ? TARGET_ENVIRONMENT_NAME.get(taskProperties) : SOURCE_ENVIRONMENT_NAME.get(taskProperties);
            String selectionMethod = ("" + SELECTION_METHOD.get(taskProperties)).toUpperCase();
            String replaceSequences = "" + REPLACE_SEQUENCES.get(taskProperties);
            String taskType = "" + TASK_TYPE.get(taskProperties);
            String taskExecutionId = "" + TASK_EXECUTION_ID.get(taskProperties);
            String selectedSubsetTaskExeId = "" + SELECTED_SUBSET_TASK_EXE_ID.get(taskProperties);
            String luName = LU_NAME.get(taskProperties);
            Boolean cloneInd = CLONE_IND.get(taskProperties);
            String dcName = DATA_CENTER_NAME.get(taskProperties).toString();

            String entityExclusionListWhere = "";
            String broadwayCommand = "";
            //TDM 7.6 - The entity list will be checked against reserved entities only if requested in the task
            String filterOutReserved = "" + FILTEROUT_RESERVED.get(taskProperties);
            String selectedVersionTaskExeId = "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
            fabric().execute("set FILTEROUT_RESERVED" + filterOutReserved);
            // Reservation is not relevant in case of replace sequence.
            // And in case of entity list the reservation will be checked by the batch process to fail the entity like any other failure
            if (!"NA".equals(filterOutReserved) && !cloneInd && !"true".equals(replaceSequences) && !"extract".equalsIgnoreCase(taskType)) {
                entityExclusionListWhere = getReserveCondition(taskProperties);
            }
            String entityInclusion = "";
            String versionParams = "";
            //Initiate interface for batch query to TDMDB
            entityInclusions.put(INTERFACE, TDMDB);
            entityInclusions.put(PARENT_LU, "");

            // TDM 9.0 - Set the environment before to make sure in case of BF to have the right interfaces are set.
            fabric().execute("set environment " + env);

            switch (selectionMethod) {
                case "L": // In case the task lists the entities to run
                    if ("true".equals(VERSION_IND.get(taskProperties))) {
                        if ("0".equalsIgnoreCase(selectedVersionTaskExeId)) {
                            versionParams = SEPARATOR.get(taskProperties).toString() + TASK_EXECUTION_ID.get(taskProperties);
                        } else {
                            versionParams = SEPARATOR.get(taskProperties).toString() + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
                        }
                    }
                    //entitiesList = entitiesList.replaceAll("\\s+","");
                    String[] entitiesListArray = !Util.isEmpty(entitiesList) ? entitiesList.split(",") : new String[]{};
                    if(!"".equalsIgnoreCase(entityExclusionListWhere)){
                        entityExclusionListWhere=entityExclusionListWhere.replace("'", "''");
                        String command = "broadway " + luName + ".FilterOutReservedFromEntityList sqlQuery = '" + entityExclusionListWhere + "'" + ", entityList = '" + entitiesList + "'";
                        String filterOutReserveList = fabric().fetch(command).firstValue().toString();
                        if(!"".equalsIgnoreCase(filterOutReserveList)){
                            entitiesListArray = filterOutReserveList.split(",");
                        }else{
                            entitiesListArray=new String[]{};
                            log.error("No matching instances were found");
                        }
                    }
                    //TDM 9.0 - There is no longer CLONE selection method
                    if (cloneInd) {
                        entityInclusion = "SELECT '" + env + SEPARATOR.get(taskProperties) + addSeparators(entitiesListArray[0]) +
                            "#params#{\"clone_id\" : '||generate_series(1, " + NUM_OF_ENTITIES.get(taskProperties) + " )||'}' as entity_id ";
                        break;
                    } else {
                       for (String entityID : entitiesListArray) {
                        
                            // TDM 7.6 - Add separtors to the entity ID if they are in use
                            entityID = addSeparators(entityID.trim());
                            entityInclusion += "'" + env + SEPARATOR.get(taskProperties) + entityID + versionParams + "',";

                        }
                        if (entitiesListArray.length != 0){
                            entityInclusion = entityInclusion.substring(0, entityInclusion.length() - 1);
                        }
                    }
                    //log.info("getEntityInclusion: entityInclusion For L: " + entityInclusion);
                    break;
                case "R": // In case the task requests a random list of entities
                    boolean isAIEnvironment = AI_ENVIRONMENT.equals(SOURCE_ENVIRONMENT_NAME.get(taskProperties));
                    boolean isParamCoupling = Boolean.TRUE.equals(PARAMS_COUPLING.get(taskProperties));
                    String selectClause = isParamCoupling ? "iid" : "entity_id";
                    String entityId = getEntityIDSelect(selectClause, SEPARATOR.get(taskProperties));
                    String lowerCaseLu = ((String) LU_NAME.get(taskProperties)).toLowerCase();
                    if ("true".equals(VERSION_IND.get(taskProperties))) {
                        String versionTaskExeId = "0".equalsIgnoreCase(selectedVersionTaskExeId) ? "" + TASK_EXECUTION_ID.get(taskProperties) : "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
                        entityId += "||'" + SEPARATOR.get(taskProperties) + "'||" + versionTaskExeId;
                    }     
                    if ("".equals(entityExclusionListWhere)) {
                        entityExclusionListWhere = " WHERE " + (isParamCoupling ? "source_env" : "source_environment") + "='" + SOURCE_ENVIRONMENT_NAME.get(taskProperties) + "' ";
                    } else {
                        entityExclusionListWhere += " " + (isParamCoupling ? "AND source_env" : "AND source_environment") + "='" + SOURCE_ENVIRONMENT_NAME.get(taskProperties) + "' ";
                    }
                    if(isAIEnvironment && isParamCoupling) {
                        entityId += "||'" + SEPARATOR.get(taskProperties) + "'||" + "task_execution_id";
                    }
                    String luParamsTable = isParamCoupling ? lowerCaseLu + ".fabric_tdm_root" : TDMDB_SCHEMA + "." + lowerCaseLu + "_params";
                    String subQuery = "";
                    String randomEntity =  "SELECT distinct '" + env + "'" + entityId + " AS entity_id, md5(" +selectClause+ " || '" + CREATION_DATE.get(taskProperties) + "') as md5_hash FROM " + luParamsTable + entityExclusionListWhere +" ORDER BY md5_hash LIMIT " ;
                    String cloneIdParam = "#params#{\"clone_id\" : '||generate_series(1, " + NUM_OF_ENTITIES.get(taskProperties) + " )||'}' as entity_id " ;
                    if(cloneInd) {
                        if (isAIEnvironment) {
                            subQuery = "WITH entity_ids AS (" + randomEntity + 1 + ") " +
                                        "SELECT ai.root_imported_lui AS root_lui " +
                                        "FROM " + TDMDB_SCHEMA + ".tdm_ai_gen_iid_mapping ai " +
                                        "JOIN entity_ids e ON ai.root_imported_lui = e.entity_id;";
                            entitiesList = "" + db(TDM).fetch(subQuery).firstValue(); 
                            entityInclusion = "SELECT '" +entitiesList +  cloneIdParam;
                        }else{
                            entityInclusion = "with subQuery as (SELECT " + selectClause + " FROM " + luParamsTable + entityExclusionListWhere + " LIMIT 1) " +
                            "SELECT '" + env + "'" + entityId + "||'" + cloneIdParam + " FROM subQuery";
                        }
                    } else {
                         subQuery = randomEntity + NUM_OF_ENTITIES.get(taskProperties);    
                        if (AI_ENVIRONMENT.equals(SOURCE_ENVIRONMENT_NAME.get(taskProperties))) { // TDM 9.0 support Loading Random AI generated entities
                            entityInclusion = "WITH entity_ids AS (" + subQuery + ") " +
                                "SELECT ai.root_imported_lui AS root_lui " +
                                "FROM " + TDMDB_SCHEMA + ".tdm_ai_gen_iid_mapping ai " +
                                "JOIN entity_ids e ON ai.root_imported_lui = e.entity_id;";
                        } else {
                            entityInclusion = "SELECT distinct entity_id FROM (" + subQuery + ") AS ALIAS1";
                        }
                    }
                    //log.info("getEntityInclusion: entityInclusion For R: " + entityInclusion);
                    break;
                case "P": // In case the task has criteria based on parameters
                    isParamCoupling = Boolean.TRUE.equals(PARAMS_COUPLING.get(taskProperties));
                    selectClause = isParamCoupling ? "iid" : "entity_id";
                    entityId = getEntityIDSelect(selectClause, SEPARATOR.get(taskProperties));                   
                    isAIEnvironment = AI_ENVIRONMENT.equals(SOURCE_ENVIRONMENT_NAME.get(taskProperties));
                    cloneIdParam = "#params#{\"clone_id\" : '||generate_series(1, " + NUM_OF_ENTITIES.get(taskProperties) + " )||'}' as entity_id ";
                    String query ="";
                    if ("true".equals(VERSION_IND.get(taskProperties))) {
                        String versionTaskExeId = "0".equalsIgnoreCase(selectedVersionTaskExeId) ? "" + TASK_EXECUTION_ID.get(taskProperties) : "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
                        entityId += "||'" + SEPARATOR.get(taskProperties) + "'||" + versionTaskExeId;
                    } 
                    if(isAIEnvironment && isParamCoupling) {
                        entityId += "||'" + SEPARATOR.get(taskProperties) + "'||" + "task_execution_id";
                    }
                    if (cloneInd) {
                        listOfMatchingEntities = generateListOfMatchingEntitiesQuery(BE_ID.get(taskProperties),
                                PARAMS_COUPLING.get(taskProperties), PARAMETERS.get(taskProperties),
                                entitiesList, SOURCE_ENVIRONMENT_NAME.get(taskProperties),true,false) + " limit 1";
                        //log.info("Parameters - listOfMatchingEntities: " + listOfMatchingEntities);
                        if (isAIEnvironment) {
                            subQuery="SELECT distinct '" + env + "'" + entityId + " AS entity_id " +
                                     "FROM (" + listOfMatchingEntities + ") AS ALIAS0 ";
                            query = "WITH entity_ids AS (" + subQuery + ") " +
                                        "SELECT ai.root_imported_lui AS root_lui " +
                                        "FROM " + TDMDB_SCHEMA + ".tdm_ai_gen_iid_mapping ai " +
                                        "JOIN entity_ids e ON ai.root_imported_lui = e.entity_id;";
                            entitiesList = "" + db(TDM).fetch(query).firstValue();            
                            entityInclusion = "SELECT '" +entitiesList +  cloneIdParam;
                        }else{
                            entitiesList = "" + db(TDM).fetch(listOfMatchingEntities).firstValue();
                            entityInclusion = "SELECT distinct '" + env + SEPARATOR.get(taskProperties) + addSeparators(entitiesList) + cloneIdParam;
                        }
                    } else {
                        entitiesList = entitiesList.replaceAll("'", "''");
                        listOfMatchingEntities = generateListOfMatchingEntitiesQuery(BE_ID.get(taskProperties),
                                PARAMS_COUPLING.get(taskProperties), PARAMETERS.get(taskProperties),
                                entitiesList, SOURCE_ENVIRONMENT_NAME.get(taskProperties),false,false);
                        listOfMatchingEntities = isParamCoupling ? listOfMatchingEntities.replaceAll("''", "'''") : listOfMatchingEntities ; //support empty string in case param value='' 
                        subQuery = "SELECT distinct '" + env + "'" + entityId + " AS entity_id " +
                                    "FROM (" + listOfMatchingEntities + ") AS ALIAS0 " + entityExclusionListWhere +
                                    " LIMIT " + NUM_OF_ENTITIES.get(taskProperties);
                        if (isAIEnvironment) {
                                        // TDM 9.0 support Loading AI generated entities with parameters
                            entityInclusion = "WITH entity_ids AS (" + subQuery + ") " +
                                            "SELECT ai.root_imported_lui AS root_lui " +
                                            "FROM " + TDMDB_SCHEMA + ".tdm_ai_gen_iid_mapping ai " +
                                            "JOIN entity_ids e ON ai.root_imported_lui = e.entity_id;";
                        } else {
                            entityInclusion = "SELECT distinct entity_id FROM (" + subQuery + ") AS ALIAS1";
                        }
                    }
                    //log.info("getEntityInclusion: entityInclusion For P: " + entityInclusion);
                    break;
                    case "PR":
                        isParamCoupling = Boolean.TRUE.equals(PARAMS_COUPLING.get(taskProperties));
                        selectClause = isParamCoupling ? "iid" : "entity_id";
                        entityId = getEntityIDSelect(selectClause, SEPARATOR.get(taskProperties));                    
                        isAIEnvironment = AI_ENVIRONMENT.equals(SOURCE_ENVIRONMENT_NAME.get(taskProperties));
                        cloneIdParam = "#params#{\"clone_id\" : '||generate_series(1, " + NUM_OF_ENTITIES.get(taskProperties) + " )||'}' as entity_id ";
                        if ("true".equals(VERSION_IND.get(taskProperties))) {
                            String versionTaskExeId = "0".equalsIgnoreCase(selectedVersionTaskExeId) ? "" + TASK_EXECUTION_ID.get(taskProperties) : "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
                        entityId += "||'" + SEPARATOR.get(taskProperties) + "'||" + versionTaskExeId;
                        }
                        if(isAIEnvironment && isParamCoupling) {
                            entityId += "||'" + SEPARATOR.get(taskProperties) + "'||" + "task_execution_id";
                        }
                        if (cloneInd) {
                            String entitiesListQuery = generateListOfMatchingEntitiesQuery(BE_ID.get(taskProperties),
                                                    PARAMS_COUPLING.get(taskProperties), PARAMETERS.get(taskProperties),
                                                    entitiesList, SOURCE_ENVIRONMENT_NAME.get(taskProperties),true,false);
                            if (isAIEnvironment) {
                                subQuery = "SELECT distinct entity_id, md5(entity_id || '" + CREATION_DATE.get(taskProperties) +"') as md5_hash " +
                                            "FROM (" +
                                            "  SELECT '" + env + "'" + entityId + " as entity_id " +
                                            "  FROM (" + entitiesListQuery + ") AS ALIAS0 " +
                                            entityExclusionListWhere +
                                            ") AS ALIAS1 " +
                                            "  ORDER BY md5_hash " +
                                            "LIMIT " + NUM_OF_ENTITIES.get(taskProperties);
                                query = "WITH entity_ids AS (" + subQuery + ") " +
                                                "SELECT ai.root_imported_lui AS root_lui " +
                                                "FROM " + TDMDB_SCHEMA + ".tdm_ai_gen_iid_mapping ai " +
                                                "JOIN entity_ids e ON ai.root_imported_lui = e.entity_id;";
                                entitiesList = "" + db(TDM).fetch(query).firstValue();            
                                entityInclusion = "SELECT '" + entitiesList + cloneIdParam;
                            } else {
                                entitiesList = "" + db(TDM).fetch(entitiesListQuery).firstValue();
                                entityInclusion = "SELECT distinct '" + env + SEPARATOR.get(taskProperties) + addSeparators(entitiesList) + cloneIdParam ;
                            }
                        } else {
                            entitiesList = entitiesList.replaceAll("'", "''");
                        String listOfMatchingEntitiesQuery = generateListOfMatchingEntitiesQuery(BE_ID.get(taskProperties),
                                PARAMS_COUPLING.get(taskProperties), PARAMETERS.get(taskProperties),
                                entitiesList, SOURCE_ENVIRONMENT_NAME.get(taskProperties),false,false);
                            listOfMatchingEntitiesQuery = isParamCoupling ? listOfMatchingEntitiesQuery.replaceAll("''", "'''") : listOfMatchingEntitiesQuery; //support empty string in case param value='' 
                            subQuery = "SELECT distinct entity_id, md5(entity_id || '" + CREATION_DATE.get(taskProperties) +"') as md5_hash " +
                                            "FROM (" +
                                            "  SELECT '" + env + "'" + entityId + " as entity_id " +
                                            "  FROM (" + listOfMatchingEntitiesQuery + ") AS ALIAS0 " +
                                            entityExclusionListWhere +
                                            ") AS ALIAS1 " +
                                            "  ORDER BY md5_hash " +
                                            "LIMIT " + NUM_OF_ENTITIES.get(taskProperties);
                    
                            if (isAIEnvironment) {
                                entityInclusion = "WITH entity_ids AS (" + subQuery + ") " +
                                                "SELECT ai.root_imported_lui AS root_lui " +
                                                "FROM " + TDMDB_SCHEMA + ".tdm_ai_gen_iid_mapping ai " +
                                                "JOIN entity_ids e ON ai.root_imported_lui = e.entity_id;";
                            } else {
                                entityInclusion = "SELECT distinct entity_id FROM (" + subQuery + ") AS ALIAS2";
                            }
                        }
                    //log.info("getEntityInclusion: entityInclusion For PR: " + entityInclusion);
                    break;
                case "ALL":
                    if (taskType.equalsIgnoreCase("load") && (VERSION_IND.get(taskProperties).equals("true"))) {
                        entityExclusionListWhere.replace("WHERE", "AND");
                        // The entity list should be taken from TDMDB and it should consider the status of the entities, only entities extracted successfully should be loaded
                        entityInclusion = "SELECT entity_id FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_ENTITIES " + ("".equals(entityExclusionListWhere) ? "WHERE " : entityExclusionListWhere) + " AND " + "task_execution_id=" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties) +
                                " and lu_name='" + LU_NAME.get(taskProperties) + "' and lower(execution_status) = 'completed' and id_type = 'ENTITY' ";

                    } else {
                        Object[] iidSeparators = fnGetIIdSeparatorsFromTDM();
                        String openSeparator = iidSeparators[0].toString();
                        String closeSeparator = iidSeparators[1].toString();
                        Map<String, String> batchStrings = getCommandForAll("" + LU_NAME.get(taskProperties), "" + TASK_EXECUTION_ID.get(taskProperties), "" +
                                        SOURCE_ENVIRONMENT_NAME.get(taskProperties), "" + VERSION_IND.get(taskProperties), SEPARATOR.get(taskProperties), openSeparator, closeSeparator,
                                "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties), "" + DATA_CENTER_NAME.get(taskProperties),
                                Long.valueOf("" + LU_ID.get(taskProperties)), sessionGlobals);
                        String entityInclusionInterface = batchStrings.get(INTERFACE);
                        entityInclusion = batchStrings.get("usingClause");
                        entityInclusions.put(INTERFACE, entityInclusionInterface);
                        entityInclusions.put("mode", batchStrings.get("mode"));
                    }
                    break;
                // TDM 7.4 - New selection method - Custom Logic
                case "C":
                    // TDM 7.6 - Get the Lu Name of the Custom Logic flow from the SELECTION_PARAM_VALUE
                    //String luName = LU_NAME.get(taskProperties);
                    luName = "" + CUSTOM_LOGIC_LU_NAME.get(taskProperties);
                    if (luName == null || Util.isEmpty(luName) || "null".equalsIgnoreCase(luName)) {
                        luName = LU_NAME.get(taskProperties).toString();
                    }
                    String customLogicFlow = "" + SELECTION_PARAM_VALUE.get(taskProperties);

                    Map<String,Object> globals = Json.get().fromJson(sessionGlobals, Map.class);
                    globals.put("environment", env);
                    
                    String globalsJson = Json.get().toJson(globals);

                    Map<String, String> BFCmdAndInterface = getCustomLogicBatch(luName, customLogicFlow,
                            "" + TASK_EXECUTION_ID.get(taskProperties), LU_ID.get(taskProperties), dcName,
                            Long.parseLong("" + NUM_OF_ENTITIES.get(taskProperties).toString()), PARAMETERS.get(taskProperties),
                            globalsJson, cloneInd);
                    entityInclusion = BFCmdAndInterface.get("batchQuery");
                    entityInclusions.put(INTERFACE, BFCmdAndInterface.get("batchInterface"));
                    //log.info("getEntityInclusion: entityInclusion For C: " + entityInclusion);
                    break;

                // TDM 8.0 - New selection method - Generate Synthetic
                case "GENERATE":
                    // In case the task requests to generate synthetic entities
                    entityInclusions.put(INTERFACE, FABRIC);
                    broadwayCommand = "broadway TDM.GenerateIIDsForRoleBase iid=? ,source_env_name = " + env + ", lu_name=" + luName +
                            ", num_of_entities= " + Long.parseLong("" + NUM_OF_ENTITIES.get(taskProperties)) + ", task_execution_id= " + taskExecutionId;

                    Map<String, String> listOfInstances = getEntityListByBF(luName, broadwayCommand, taskExecutionId, LU_ID.get(taskProperties), 
                        dcName, NUM_OF_ENTITIES.get(taskProperties), cloneInd);
                    
                        entityInclusion = listOfInstances.get("batchQuery");
                        entityInclusions.put(INTERFACE, listOfInstances.get("batchInterface"));
                    break;

                case "AI_GENERATED":
                    broadwayCommand = "broadway " + luName + ".getEntityList " + " taskExecutionID =" + taskExecutionId +
                                        ", luName=" + luName;
                    entityInclusions.put(INTERFACE, FABRIC);
                    entityInclusion = broadwayCommand;
                    break;
                case "GENERATE_SUBSET":
                    if (SYNTHETIC_ENVIRONMENT.equalsIgnoreCase(env)) {
                        broadwayCommand = "broadway TDM.GetGeneratedSubsetEntities env_name= " + env + " selected_subset_exe_id= " +
                                selectedSubsetTaskExeId + " lu_name= " + luName;

                    } else {
                        broadwayCommand = "broadway " + luName + ".getEntityList " + " taskExecutionID =" + selectedSubsetTaskExeId +
                                ", luName=" + luName;
                    }
                    entityInclusions.put(INTERFACE, FABRIC);
                    entityInclusion = broadwayCommand;
                    break;
                default: // This column is populated automatically by the application and should not include any other options
                    break;

            }
            if(selectionMethod.contains("P")){
                String query = "UPDATE " +TDMDB_SCHEMA+ ".task_execution_list set entity_inclusion_query= ? where task_execution_id = ? AND " +
                "lu_id = ?";
                db(TDM).execute(query,entityInclusion,TASK_EXECUTION_ID.get(taskProperties), LU_ID.get(taskProperties));
            }
            entityInclusions.put("" + TASK_EXECUTION_ID.get(taskProperties), entityInclusion);
            return entityInclusion;

        }catch (Exception e){
            log.error("Entity Inclusion Failed due to: " + e.getMessage());
            throw e ;
        }
        //log.info("getEntityInclusion - entityInclusion: " + entityInclusion);
    }
    private static String getReserveCondition(Map<String, Object> taskProperties) throws Exception {
        String env = isDeleteOnlyMode(taskProperties) ? TARGET_ENVIRONMENT_NAME.get(taskProperties) : SOURCE_ENVIRONMENT_NAME.get(taskProperties);
        String envID = "" + ENVIRONMENT_ID.get(taskProperties);
        String beID = "" + BE_ID.get(taskProperties);
        String taskID="" + TASK_ID.get(taskProperties);
        String userID = "" + TASK_EXECUTED_BY.get(taskProperties);
        if ("TDM.tdmTaskScheduler".equalsIgnoreCase(userID)) {
            userID=fnGetTaskCreatedBy(userID,taskID);
        }
        String taskType = "" + TASK_TYPE.get(taskProperties);
        String selectionMethod = "" + SELECTION_METHOD.get(taskProperties);
        boolean isParamCoupling = Boolean.TRUE.equals(PARAMS_COUPLING.get(taskProperties));
        String selectClause = isParamCoupling ? "iid" : "entity_id";
        if(selectionMethod.equalsIgnoreCase("ALL")){
            selectClause="target_entity_id";
        }
        String filterOutReserved = "" + FILTEROUT_RESERVED.get(taskProperties);
        String reservedExclusionListWhere = "";
        // In version 9.2, reserve will include two modes , by other and by all 
        if ("ALL".equalsIgnoreCase(filterOutReserved)) {
            reservedExclusionListWhere = " WHERE cast(" + selectClause + " as text) NOT IN ("
                    + "SELECT entity_id FROM " + TDMDB_SCHEMA + ".tdm_reserved_entities "
                    + "WHERE env_id = " + envID + " AND be_id = " + beID
                    + " AND (end_datetime IS NULL OR end_datetime > CURRENT_TIMESTAMP))";
        } else {
            reservedExclusionListWhere = " WHERE cast(" + selectClause + " as text) NOT IN ("
                    + "SELECT entity_id FROM " + TDMDB_SCHEMA + ".tdm_reserved_entities "
                    + "WHERE env_id = " + envID + " AND be_id = " + beID
                    + " AND reserve_owner != '" + userID + "'"
                    + " AND (end_datetime IS NULL OR end_datetime > CURRENT_TIMESTAMP))";
        }
    
        return reservedExclusionListWhere;
    }

    private static Map<String, Object> getGlobals(String globalsQuery, Map<String, Object> taskProperties, Map<String, Object> args, Object... params) {
        String selectionMethod = SELECTION_METHOD.get(taskProperties);

        Map<String, Object> globals = new HashMap<>();
        String VERSION_TASK_EXE_ID = "0";
        Boolean cloneInd = CLONE_IND.get(taskProperties);
        String versionInd = "" + VERSION_IND.get(taskProperties);
        boolean isAIEnvironment = AI_ENVIRONMENT.equals(SOURCE_ENVIRONMENT_NAME.get(taskProperties));
        boolean isSynthEnvironment = SYNTHETIC_ENVIRONMENT.equals(SOURCE_ENVIRONMENT_NAME.get(taskProperties));
        if("true".equalsIgnoreCase(versionInd)){
            if("0".equalsIgnoreCase("" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties))){
                VERSION_TASK_EXE_ID = "" + TASK_EXECUTION_ID.get(taskProperties);
            }else {
                VERSION_TASK_EXE_ID = "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties) ;
            }
        }else if(isAIEnvironment || isSynthEnvironment){
             if("0".equalsIgnoreCase("" + SELECTED_SUBSET_TASK_EXE_ID.get(taskProperties))){
                VERSION_TASK_EXE_ID = "" + TASK_EXECUTION_ID.get(taskProperties);
            }else {
                VERSION_TASK_EXE_ID = "" + SELECTED_SUBSET_TASK_EXE_ID.get(taskProperties) ;
            }
        }
        // TDM 7.4 - Support Reserved Entities
        String userName = "" + TASK_EXECUTED_BY.get(taskProperties);
        String taskID= "" +TASK_ID.get(taskProperties);
        if ("TDM.tdmTaskScheduler".equalsIgnoreCase(userName)) {
            userName=fnGetTaskCreatedBy(userName,taskID);
        }
        String userRoles = USER_ROLES.get(taskProperties);
        //log.info("ExecuteTask - userName: " + userName + ", userRoles: " + userRoles);
        String permissionGroup = getPermissionGroupByRoles(userRoles);
        globals.put("USER_NAME", userName);
        globals.put("USER_FABRIC_ROLES", String.join(TDM_PARAMETERS_SEPARATOR,userRoles));
        globals.put("USER_PERMISSION_GROUP", permissionGroup);
        globals.put("TDM_RESERVE_IND", RESERVE_IND.get(taskProperties));
        globals.put("RESERVE_RETENTION_PERIOD_TYPE", RESERVE_RETENTION_PERIOD_TYPE.get(taskProperties));
        globals.put("RESERVE_RETENTION_PERIOD_VALUE", RESERVE_RETENTION_PERIOD_VALUE.get(taskProperties));
        globals.put("RESERVE_NOTE", RESERVE_NOTE.get(taskProperties));
        globals.put("BE_ID", "" + BE_ID.get(taskProperties));
        globals.put("LU_ID", "" + LU_ID.get(taskProperties));
        globals.put("PARENT_LU_NAME", "" + PARENT_LU_NAME.get(taskProperties));

        globals.put("TDM_TARGET_PRODUCT_VERSION", TDM_TARGET_PRODUCT_VERSION.get(taskProperties));
        globals.put("TDM_SOURCE_PRODUCT_VERSION", TDM_SOURCE_PRODUCT_VERSION.get(taskProperties));
        globals.put("TDM_SOURCE_ENVIRONMENT_NAME", SOURCE_ENVIRONMENT_NAME.get(taskProperties));
        globals.put("TDM_TAR_ENV_NAME", TARGET_ENVIRONMENT_NAME.get(taskProperties));
        globals.put("TDM_TASK_ID", "" + TASK_ID.get(taskProperties));
        globals.put("TDM_TASK_EXE_ID", "" + TASK_EXECUTION_ID.get(taskProperties));
        globals.put("execution_id", "" + TASK_EXECUTION_ID.get(taskProperties));
        globals.put("clone_id", "0");
        globals.put("TDM_REPLACE_SEQUENCES", cloneInd ? true : REPLACE_SEQUENCES.get(taskProperties));
        globals.put("TASK_TYPE", TASK_TYPE.get(taskProperties).toString().toUpperCase());
        globals.put("TDM_VERSION_TASK_EXECUTION_ID", VERSION_TASK_EXE_ID);
        globals.put("TDM_DELETE_ONLY_TASK", Util.rte(() -> isDeleteOnlyMode(taskProperties)));
        globals.put("SELECTION_METHOD", SELECTION_METHOD.get(taskProperties));
        globals.put("CHILD_LU_IND", isChildLU(taskProperties));
        globals.put("EXECUTION_MODE", EXECUTION_MODE.get(taskProperties));

        globals.put("TABLE_DEFAULT_DISTRIBUTION_MIN", TABLE_DEFAULT_DISTRIBUTION_MIN.get(taskProperties));

        globals.put("TABLE_DEFAULT_DISTRIBUTION_MAX", TABLE_DEFAULT_DISTRIBUTION_MAX.get(taskProperties));

        globals.put("PARAMS_COUPLING", PARAMS_COUPLING.get(taskProperties));

        globals.putAll(args);

        Util.rte(() -> db(TDM).fetch(globalsQuery, params).forEach(res -> Util.rte(() -> globals.put(res.resultSet().getString("global_name"), res.resultSet().getString("global_value")))));
        // Replace gson with K2view Json
        //Gson gson = new Gson();

        //TDM 7.2 - Get task execution override globals and add them to the task's globals.
        Map<String, Object> taskOverrideAttrs = fnGetTaskExecOverrideAttrs(TASK_ID.get(taskProperties), TASK_EXECUTION_ID.get(taskProperties));
        String overrideGlobalsStr = "" + taskOverrideAttrs.get("TASK_GLOBALS");
        //log.info("TdmExecuteTask - overrideGlobalsStr : " + overrideGlobalsStr);

        if (!"".equals(overrideGlobalsStr) && !"null".equals(overrideGlobalsStr)) {
            //Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            //Map <String, Object> overrideGlobals = gson.fromJson(overrideGlobalsStr, mapType);
            Map<String, Object> overrideGlobals = Json.get().fromJson(overrideGlobalsStr);
            globals.putAll(overrideGlobals);
        }
        
        if (cloneInd) {
            globals.keySet().removeIf(key -> key.contains("LOAD_MASKING_FLAG"));
            globals.put("LOAD_MASKING_FLAG", "true");
        }

        globals.put("TDM_CLONING_DATA", cloneInd);
        //log.info("getGlobals - VERSION_IND: <" + VERSION_IND.get(taskProperties) + ">");

        if (VERSION_IND.get(taskProperties).equals("true")) {
            globals.keySet().removeIf(key -> key.contains("MASKING_FLAG"));
            globals.put("LOAD_MASKING_FLAG", "false");

            globals.put("TDM_REPLACE_SEQUENCES", "false");

            globals.put("enable_masking", "false");

            //TDM 7.3 - Add global to mark dataflux tasks
            globals.put("TDM_DATAFLUX_TASK", "true");
            if ("load".equalsIgnoreCase("" + TASK_TYPE.get(taskProperties))) {
                globals.put("TDM_DELETE_BEFORE_LOAD", "true");
            }

        } else {
            //TDM 7.3 - Add global to mark dataflux tasks
            globals.put("TDM_DATAFLUX_TASK", "false");
        }


        if (Integer.parseInt(SOURCE_ENVIRONMENT_ID.get(taskProperties).toString()) < 0) {
            globals.put("TDM_REPLACE_SEQUENCES", "true");
            globals.put("REPLACE_SEQ_BY_LUI_SYNC", "true");
        }

        //TDM 9.3 - The MASK_SENSITIVE_DATA is managed at environment level only
        //if ("true".equalsIgnoreCase(MASK_SENSITIVE_DATA.get(taskProperties).toString())){
		//	globals.put("enable_masking", "true");
        //} else {
        //TDM 9.0 - Check if the environment settings was changed since the task was created
        Object sensitiveDataInd = Util.rte(() ->db(TDM).fetch("select mask_sensitive_data from " + TDMDB_SCHEMA + ".environments where environment_id = ?", 
            Integer.parseInt(SOURCE_ENVIRONMENT_ID.get(taskProperties).toString())).firstValue());
            
        if((Boolean)sensitiveDataInd) {
            globals.put("enable_masking", "true");
        } else {
            globals.put("enable_masking", "false");
        }
        //}

        if ("Synthetic".equalsIgnoreCase(SELECTION_METHOD.get(taskProperties)) || "Generate".equalsIgnoreCase(SELECTION_METHOD.get(taskProperties))) {
            globals.put("ROWS_GENERATOR", "true");
        }

        //Disable DEBUG MODE, as it is not relevant in case of task execution
        globals.put("TDM.TDM_DEBUG_MODE", "false");
        return globals;
    }

    private static void setGlobalsForTask(String taskType, Map<String, Object> taskProperties) throws Exception {

        Map<String, Object> globals = new HashMap<>();
        Map<String, Object> additionalGlobals = new HashMap<>();
        switch (taskType) {
            case "load":
            case "reserve":
            case "delete":
                additionalGlobals.put("TDM_SYNC_SOURCE_DATA", getSrcSyncDataVal(taskProperties));
                additionalGlobals.put("TDM_DELETE_BEFORE_LOAD", DELETE_BEFORE_LOAD.get(taskProperties));
                additionalGlobals.put("TDM_INSERT_TO_TARGET", LOAD_ENTITY.get(taskProperties));
                additionalGlobals.put("TARGET_ENVIRONMENT_ID", "" + ENVIRONMENT_ID.get(taskProperties));
                additionalGlobals.put("FILTEROUT_RESERVED", FILTEROUT_RESERVED.get(taskProperties));

                // TDM 7.4 - Get MAX_RESERVATION_DAYS_FOR_TESTER
                String maxReserveTester = "";
                String executed_by = TASK_EXECUTED_BY.get(taskProperties);
                String roles = USER_ROLES.get(taskProperties);
                String task_id = TASK_ID.get(taskProperties).toString();
                String created_by;
                try {
                    created_by = "" + db(TDM).fetch("SELECT task_created_by FROM " + TDMDB_SCHEMA + ".tasks WHERE task_id=?", task_id).firstValue();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                boolean adminOrOwner = false;
                if ("TDM.tdmTaskScheduler".equalsIgnoreCase(executed_by)) {
                    adminOrOwner = Util.rte(() -> fnIsAdminOrOwner("" + ENVIRONMENT_ID.get(taskProperties), created_by));
                } else {
                    adminOrOwner = Util.rte(() -> fnIsAdminOrOwnerByRoles(roles));
                }
                if (!adminOrOwner) {
                    Map<String, Object> retentionInfo = Util.rte(() -> fnGetRetentionPeriod());
                    maxReserveTester = String.valueOf(retentionInfo.get("maxReservationPeriodForTesters"));
                }
                //log.info("MAX_RESERVATION_DAYS_FOR_TESTER: " +  maxReserveTester);
                additionalGlobals.put("MAX_RESERVATION_DAYS_FOR_TESTER", maxReserveTester);

                //String syntheticData = getSyntheticData(selectionMethod);
                globals = getGlobals(LOAD.query(), taskProperties, additionalGlobals, LOAD.params(taskProperties));
                break;

            case "extract":
            case "generate":
                additionalGlobals.put("TDM_SYNC_SOURCE_DATA", getSrcSyncDataVal(taskProperties));
                additionalGlobals.put("TDM_DELETE_BEFORE_LOAD", "false");
                additionalGlobals.put("CHILD_LU_IND", isChildLU(taskProperties));
                if ("generate".equals(taskType)) {
                    additionalGlobals.put("generate_consistent", "true");
                }
                globals = getGlobals(EXTRACT.query(), taskProperties, additionalGlobals, EXTRACT.params(taskProperties));

                if ("true".equalsIgnoreCase("" + VERSION_IND.get(taskProperties))) {
                    additionalGlobals.put("TDM_VERSION_TASK_EXECUTION_ID", TASK_EXECUTION_ID.get(taskProperties));
                }
                break;
            default:
                TASK_TYPES type = TASK_TYPES.valueOf(TASK_TYPE.get(taskProperties).toString().toUpperCase());
                globals = getGlobals(type.query(), taskProperties, new HashMap<>(), type.params(taskProperties));
                break;
        }

        // Replace gson with K2view Json
        //Gson gson = new Gson();
        //String globalsJson = gson.toJson(globals, new TypeToken<HashMap>(){}.getType());
        String globalsJson = Json.get().toJson(globals);
        //log.info("globalsJson: " + globalsJson);
        Util.rte(() -> setGlobals(globalsJson));
        sessionGlobals = globalsJson;

    }

    private static String getSrcSyncDataVal(Map<String, Object> taskProperties) {
        String syncSrcData = "true";
        //log.info("SYNC_MODE: " + SYNC_MODE.get(taskProperties) + ", TASK_TYPE: " + TASK_TYPE.get(taskProperties) + ", VERSION_IND: " + VERSION_IND.get(taskProperties) + ", LOAD_ENTITY: " + LOAD_ENTITY.get(taskProperties));
        if ("off".equalsIgnoreCase(SYNC_MODE.get(taskProperties))) {
            syncSrcData = "false";
        } else {
            if (("load".equalsIgnoreCase(TASK_TYPE.get(taskProperties)) &&
                    "true".equalsIgnoreCase(VERSION_IND.get(taskProperties))) ||
                    "delete".equalsIgnoreCase(TASK_TYPE.get(taskProperties))) {
                syncSrcData = "false";
            }
        }
        //log.info("getSrcSyncDataVal - syncSrcData: " + syncSrcData);
        return syncSrcData;
    }

    private static void setSyncMode(Map<String, Object> taskProperties) {
        String syncMode = SYNC_MODE.get(taskProperties);
        if (Util.isEmpty(syncMode)) {
            String sourceEnv = SOURCE_ENVIRONMENT_NAME.get(taskProperties);
            syncMode = "" + Util.rte(() -> db(TDM).fetch("SELECT sync_mode FROM " + TDMDB_SCHEMA + ".environments where environment_status = 'Active' and environment_name=?", sourceEnv).firstValue());
            //log.info("TdmExecuteTask - syncMode Environments: <" + syncMode + ">, for Env: " + sourceEnv);
            taskProperties.put("sync_mode", syncMode);
            //log.info("setSyncMode - SYNC_MODE: " + SYNC_MODE.get(taskProperties));
        }
    }

    private static String getSyncModeForLoad(Map<String, Object> taskProperties) {
        String syncMode = SYNC_MODE.get(taskProperties);
        // In case of Load and sync mode is set to OFF and the deleteBeforeLoad is set to TRUE or it is dataflux load task (therefore requires delete before load),
        // then the sync mode should be set to ON, to allow population of delete tables.

        if ("off".equalsIgnoreCase(syncMode) && "load".equalsIgnoreCase(TASK_TYPE.get(taskProperties)) &&
                ("true".equalsIgnoreCase(DELETE_BEFORE_LOAD.get(taskProperties)) || "true".equalsIgnoreCase(VERSION_IND.get(taskProperties)))) {
            syncMode = "ON";
        }
        // TDM 9.1 - If task type is delete, then the sync mode should be set to ON
        if ("off".equalsIgnoreCase(syncMode) && "delete".equalsIgnoreCase(TASK_TYPE.get(taskProperties))) {
            syncMode = "ON";
        }
        //In case of sync mode is force and the task is dataflux load, the sync mode must be set to ON,
        // to prevent resyncing the data of the LU but to allow populating the Delete tables.
        if ("force".equalsIgnoreCase(syncMode) && "load".equalsIgnoreCase(TASK_TYPE.get(taskProperties)) &&
                "true".equalsIgnoreCase(VERSION_IND.get(taskProperties))) {
            syncMode = "ON";
        }

        taskProperties.put("sync_mode", syncMode);
        return syncMode;
    }

    private static void syncInstanceForCloning(String entityInclusion, Map<String, Object> taskProperties) throws SQLException {
        String luName = LU_NAME.get(taskProperties);
        String taskExeId = "" + TASK_EXECUTION_ID.get(taskProperties);
        Set<String> entityList = new HashSet<>();

        String getSrcDCSQL = "select p.data_center_name from " + TDMDB_SCHEMA + ".environment_products p, " + TDMDB_SCHEMA + ".task_execution_list l, " + TDMDB_SCHEMA + ".tasks_logical_units u " +
                "where l.task_execution_id=? and l.task_id = u.task_id and l.lu_id = u.lu_id and u.lu_name = ? " +
                "and l.source_environment_id= p.environment_id and l.product_id = p.product_id ";

        String srcDC = "" + db(TDMDB).fetch(getSrcDCSQL, taskExeId, luName).firstValue();

        //log.info("syncInstanceForCloning - entityInclusion: " + entityInclusion);
        //log.info("syncInstanceForCloning - sync:" + SYNC_MODE.get(taskProperties));

        fabric().execute("set sync " + SYNC_MODE.get(taskProperties));
        //27-01-22 - The env should be set to the source env of the task before syncing the entity from source
        fabric().execute("set environment " + SOURCE_ENVIRONMENT_NAME.get(taskProperties));

        //log.info("syncInstanceForCloning - srcDC: " + srcDC + ", luName: " + luName + ", taskExeId: " + taskExeId);

        String getCmd = "get " + luName + ".? WITH PARALLEL=false STOP_ON_ERROR=true";

        if (srcDC != null && !Util.isEmpty(srcDC) && !srcDC.equals("null")) {
            getCmd = "get " + luName + ".? @'" + srcDC + "' WITH PARALLEL=false STOP_ON_ERROR=true";
        }
        //log.info("syncInstanceForCloning - getCmd: " + getCmd);

        // The query for batch may include '''' for the batch command to process the query, we need to replace it with ''
        String cleanedEntityInclusion = entityInclusion.replaceAll("''''", "''");

        //log.info("syncInstanceForCloning - cleanedEntityInclusion: " + cleanedEntityInclusion);

        Db.Rows rows = db(TDMDB).fetch(cleanedEntityInclusion);

        for (Db.Row row : rows) {
            //log.info("syncInstanceForCloning - entity_id: " + row.cell(0));
            String intsanceID = "" + row.cell(0);
            //Object[] splitCloneId = intsanceID.split("#params#");

            Db.Rows instanceSplitted = fabric().fetch("broadway " + luName + ".SplitIIDAndCloneNumber iid='" + intsanceID + "', RESULT_STRUCTURE=ROW");

            for (Db.Row instanceField : instanceSplitted) {
                if ("UID".equals(instanceField.get("column"))) {
                    intsanceID = "" + instanceField.get("value");
                }

                if ("cloneNo".equals(instanceField.get("column"))) {
                    fabric().execute("set clone_id " + instanceField.get("value"));
                }

            }

            if (instanceSplitted != null) {
                instanceSplitted.close();
            }

            if (!entityList.contains(intsanceID)) {
                //log.info("syncInstanceForCloning - intsanceID: " + intsanceID);
                entityList.add(intsanceID);
                try {
                    fabric().execute(getCmd, intsanceID);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

        taskProperties.put("sync_mode", "off");
        if (rows != null) {
            rows.close();
        }
    }

    private static String getGeneratedcData(String selectionMethod) {
        return selectionMethod.equals("GEN") ? "true" : "false";
    }

    private static String buildEntityReserved(String environment, Map<String, Object> taskProperties) throws Exception {
        String selectionMethod = ("" + SELECTION_METHOD.get(taskProperties)).toUpperCase();
        String versionInd = "" + VERSION_IND.get(taskProperties);
        String selectedVersionExeID = "" + SELECTED_VERSION_TASK_EXE_ID.get(taskProperties);
        String taskExeID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String fieldName = "entity_id";


        if (!"R".equals(selectionMethod)) {
            Object[] separators = fnGetIIdSeparatorsFromTDM();
            String open = (String) separators[0];
            String close = (String) separators[1];

            String reservedEntitySelect = "'" + environment + "'||'" + SEPARATOR.get(taskProperties) + "'||" + (!Util.isEmpty(open) ? "'" + open + "'||" + fieldName : fieldName);
            reservedEntitySelect = !Util.isEmpty(close) ? reservedEntitySelect + "||'" + close + "'" : reservedEntitySelect;
            if (versionInd.equals("true")) {
                if ("0".equalsIgnoreCase(selectedVersionExeID)) {
                    reservedEntitySelect += "||'" + SEPARATOR.get(taskProperties) + taskExeID.trim() + "'";

                } else {
                    reservedEntitySelect += "||'" + SEPARATOR.get(taskProperties) + selectedVersionExeID.trim() + "'";

                }
            }
            //log.info("reservedEntitySelect: " + reservedEntitySelect);
            return reservedEntitySelect;
        } else {
            return fieldName.trim();
        }
    }

    private static String getEntityIDSelect(String name, String iidSeparartor) throws Exception {
        Object[] separators = fnGetIIdSeparatorsFromTDM();
        String open = (String) separators[0];
        String close = (String) separators[1];

        String entityIdSelect = "||'" + iidSeparartor + "'||" + (!Util.isEmpty(open) ? "'" + open + "'||" + name : name);
        entityIdSelect = !Util.isEmpty(close) ? entityIdSelect + "||'" + close + "'" : entityIdSelect;

        return entityIdSelect;
    }

    

    private static Map<String, Object> getTaskProperties(Db.Row row) throws Exception{
        Map<String, Object> taskProperties = new HashMap<>();

        try {
            // get task props
            taskProperties.putAll(row);
            Long taskId = (Long) row.get("task_id");
            taskProperties.putAll(Objects.requireNonNull(getTaskProperties(taskId)));
            taskProperties.put("task_id", taskId);
            taskProperties.put("be_id", row.get("be_id"));
            taskProperties.put("environment_id", row.get("environment_id"));
            taskProperties.put("creation_date", row.get("creation_date"));
            // get LU properties
            Db.Row luProperties = getLuProperties((Long) taskProperties.get("lu_id"));
            taskProperties.put("lu_name", luProperties.get("lu_name"));
            taskProperties.put("parent_lu_name", getLuName((Long) taskProperties.get("parent_lu_id")));
        } catch (Exception e) {
            log.error("Can't get task properties for task_execution_id=" + row.get("task_execution_id"), e);
        }

        // TDM 9.2 - If the task is reserve only, clone or Generate then it cannot be Vertical.
        String executionMode = fnGetTaskExecutionMode(EXECUTION_MODE.get(taskProperties), TASK_TYPE.get(taskProperties), 
            BE_ID.get(taskProperties), CLONE_IND.get(taskProperties));
        if (!executionMode.equalsIgnoreCase(EXECUTION_MODE.get(taskProperties))) {
            taskProperties.put("execution_mode", executionMode);
        }
    
        // TDM 9.1 - Get data from tdm_general_parameters and set it.
        String sql = "Select param_name, param_value from " +
                TDMDB_SCHEMA + ".tdm_general_parameters where " +
                "upper(param_name) in ('IID_SEPARATOR', 'TABLE_DEFAULT_DISTRIBUTION_MIN', 'TABLE_DEFAULT_DISTRIBUTION_MAX', 'PARAMS_COUPLING')";


        Util.rte(() -> db(TDM).fetch(sql).forEach(record -> {
            String paramName = record.get("param_name").toString().toUpperCase();
            String paramValue = record.get("param_value").toString();
            switch (paramName)  {
                case "IID_SEPARATOR" :
                    taskProperties.put("separator", paramValue);
                    break;
                case "TABLE_DEFAULT_DISTRIBUTION_MIN" :
                    taskProperties.put("table_default_distribution_min", paramValue);
                    break;
                case "TABLE_DEFAULT_DISTRIBUTION_MAX" :
                    taskProperties.put("table_default_distribution_max", paramValue);
                    break;
                case "PARAMS_COUPLING" :
                    taskProperties.put("params_coupling", Boolean.parseBoolean(paramValue));
                    break;
            }
        }));
        //TDM 7.2 - Get task execution override attributes and use them to override the task's attributes
        Map<String, Object> taskOverrideAttrs = fnGetTaskExecOverrideAttrs((Long) row.get("task_id"), (Long) row.get("task_execution_id"));
        String overrideValue = "";
        try {
            for (String attrName : taskOverrideAttrs.keySet()) {
                boolean entityListFlag = false;

                if (!"task_globals".equalsIgnoreCase(attrName)) {
                    overrideValue = "" + taskOverrideAttrs.get(attrName);
                    //log.info("getTaskProperties - attrName: " + attrName + ", overrideValue: " + overrideValue);
                    attrName = attrName.toLowerCase();
                    switch (attrName) {
                        case "selection_method":
                            taskProperties.put(attrName, overrideValue);
                            break;
                        case "entity_list":
                            taskProperties.put("selection_param_value", overrideValue);
                            int numberOfEntities = overrideValue.split(",", -1).length;
                            taskProperties.put("num_of_entities", numberOfEntities);
                            entityListFlag = true;
                            break;
                        case "no_of_entities":
                            if (!entityListFlag) {
                                taskProperties.put("num_of_entities", overrideValue);
                            }
                            break;
                        case "source_environment_name":
                            taskProperties.put(attrName, overrideValue);
                            Db.Row envData = db(TDM).fetch("select environment_id, mask_sensitive_data from " +
                                    TDMDB_SCHEMA + ".environments where environment_name = ? and lower(environment_status) = 'active'", overrideValue).firstRow();
                            String srcEnvId = "" + envData.get("environment_id");
                            String maskSenData = "" + envData.get("mask_sensitive_data");
                            taskProperties.put("source_environment_id", srcEnvId);
                            if ("true".equalsIgnoreCase(maskSenData)) {
                                taskProperties.put("mask_sensitive_data", true);
                            }

                            break;
                        case "target_environment_name":
                            taskProperties.put(attrName, overrideValue);
                            String tarEnvId = "" + db(TDM).fetch("select environment_id from " + TDMDB_SCHEMA + ".environments where environment_name = ? and lower(environment_status) = 'active'", overrideValue).firstValue();
                            taskProperties.put("environment_id", tarEnvId);
                            break;
                        // TDM 7.4 - 16-Jan-22 - Add support for overriding DataFlux parameters
                        case "selected_version_task_exe_id":
                            taskProperties.put(attrName, overrideValue);
                            break;
                        case "dataflux_retention_params":
                            Map rentionPeriodInfo = Json.get().fromJson((String) overrideValue, Map.class);
                            taskProperties.put("retention_period_type", "" + rentionPeriodInfo.get("units"));
                            taskProperties.put("retention_period_value", "" + rentionPeriodInfo.get("value"));
                            break;
                        case "reserve_ind":
                            taskProperties.put("reserve_ind", overrideValue);
                            break;
                        case "reserve_retention_params":
                            Map reserveRentionPeriodInfo = Json.get().fromJson((String) overrideValue, Map.class);
                            taskProperties.put("reserve_retention_period_type", "" + reserveRentionPeriodInfo.get("units"));
                            taskProperties.put("reserve_retention_period_value", "" + reserveRentionPeriodInfo.get("value"));
                            break;
                        // TDM 7.4 - End of Change
                        default:
                            taskProperties.put(attrName, overrideValue);
                            break;
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Can't Get Environment ID of Environment Name: " + overrideValue, e);
        }

        return taskProperties;
    }

    private static Db.Row getTaskProperties(Long taskId) {
        try {
            String query = new String(loadAndReplace("TDM/fnTdmExecuteTask/query_get_tasks_properties.sql"));
            return db(TDM).fetch(query,taskId).firstRow();
        } catch (Exception e) {
            log.error("Can't get properties for task_id=" + taskId, e);
            return null;
        }
    }

    private static String getLuName(Long luId) throws SQLException {
        return (String) getLuProperties(luId).get("lu_name");
    }

    private static Db.Row getLuProperties(Long luId) throws SQLException {
        return db(TDM).fetch("SELECT lu_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE lu_id = ?", luId).firstRow();
    }

    private static void updateLuExecutionStatus(Long taskExecutionId, Long luID, String status, String fabricExecutionId, String versionExpDate) {
        try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=?, fabric_execution_id=?," +
                            "start_execution_time = COALESCE(start_execution_time, current_timestamp AT TIME ZONE 'utc')," +
                            "expiration_date = TO_TIMESTAMP(COALESCE(?, '19700101000000'), 'YYYYMMDDHH24MISS')" +
                            " WHERE task_execution_id=? AND lu_id=?"
                    , status, fabricExecutionId, versionExpDate, taskExecutionId, luID);
        } catch (SQLException e) {
            log.error("Can't update status in task_execution_list for task_execution_id=" + taskExecutionId, e);
        }
    }


    private static void updateLuRefExeFailedStatus(Long taskExecutionId, String luName, String status) {
        try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_ref_exe_stats s SET execution_status=?" +
                            " WHERE task_execution_id=? and task_ref_table_id in (SELECT task_ref_table_id FROM " + TDMDB_SCHEMA + ".task_ref_tables t " +
                            " WHERE s.task_id = t.task_id AND t.lu_name=? )"
                    , status, taskExecutionId, luName);
        } catch (SQLException e) {
            log.error("Can't update status in task_ref_exe_stats for task_execution_id=" + taskExecutionId, e);
        }
    }

    private static void updateTaskExecutionStatus(Boolean verticalExecution, String status, Long taskExecutionID, Long luID, Object... params) {
        try {

            if (!verticalExecution) {
                db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
                                "execution_status=?, " +
                                "fabric_execution_id=?, " +
                                "start_execution_time = (case when start_execution_time is null then ? else start_execution_time end), " +
                                "expiration_date = TO_TIMESTAMP(COALESCE(?, '19700101000000'), 'YYYYMMDDHH24MISS'), " +
                                "num_of_processed_entities = ?, " +
                                "num_of_copied_entities = ?, " +
                                "num_of_failed_entities = ?, " +
                                "end_execution_time = ? " +
                                "WHERE task_execution_id=? and lu_id = ? and process_id=0"
                        , status, params[0], params[1], params[2], params[3], params[4],params[5],params[6], taskExecutionID, luID);
            } else {
                String updateSQL = "WITH RECURSIVE ph AS " +
                    "(SELECT lu_id, parent_lu_id FROM " + TDMDB_SCHEMA + ".task_execution_list " +
                    "WHERE task_execution_id = ? and lu_id = ? " +
                    "UNION ALL " +
                    "SELECT task_execution_list.lu_id, task_execution_list.parent_lu_id " +
                    "FROM " + TDMDB_SCHEMA + ".task_execution_list, ph " +
                    "WHERE ph.lu_id = task_execution_list.parent_lu_id " +
                    "AND task_execution_list.task_execution_id = ?) " +
                    "UPDATE " + TDMDB_SCHEMA + ".task_execution_list " +
                    "SET execution_status = ?, " +
                    "fabric_execution_id= ?," +
                    "start_execution_time = (case when start_execution_time is null then ? else start_execution_time end), " +
                    "expiration_date = TO_TIMESTAMP(COALESCE(?, '19700101000000'), 'YYYYMMDDHH24MISS'), " +
                    "num_of_processed_entities = ?, " +
                    "num_of_copied_entities = ?, " +
                    "num_of_failed_entities = ?, " +
                    "end_execution_time = ? " +
                    "FROM ph " +
                    "WHERE task_execution_id = ? AND " +
                    "(ph.lu_id = task_execution_list.parent_lu_id OR (task_execution_list.parent_lu_id is null AND task_execution_list.lu_id = ?))";
                
                db(TDM).execute(updateSQL, taskExecutionID, luID, taskExecutionID, status,
                    params[0], params[1], params[2], params[3], params[4],params[5],params[6], taskExecutionID, luID);
            }
        } catch (SQLException e) {
            log.error("Can't update status in task_execution_list table for task_execution_id=" + taskExecutionID + ", lu_id: " + luID, e);
        }
    }
    private static void updateAITaskExecutionStatus(String status, Long taskExecutionID, Long luID, Object... params) {
        try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
                            "execution_status=?, " +
                            "expiration_date = TO_TIMESTAMP(COALESCE(?, '19700101000000'), 'YYYYMMDDHH24MISS'), " +
                            "num_of_processed_entities = ?, " +
                            "num_of_copied_entities = ?, " +
                            "num_of_failed_entities = ?, " +
                            "end_execution_time = current_timestamp at time zone 'utc' " +
                            "WHERE task_execution_id=? and lu_id = ? and process_id<0"
                    , status, params[0], params[1], params[2], params[3],taskExecutionID, luID);
        } catch (SQLException e) {
            log.error("Can't update status in task_execution_list table for task_execution_id=" + taskExecutionID + ", lu_id: " + luID, e);
        }
    }

    private static void updateTaskExecutionBatchID(Long taskExecutionID, Long luID, Map<String,String> tableExecutionStatus) {
        try {
            String fabricExecutionId = tableExecutionStatus != null ? tableExecutionStatus.get("fabric_execution_id") : null;
            String expiration_date = tableExecutionStatus != null ? tableExecutionStatus.get("expiration_date") : null;
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
                            "fabric_execution_id=?, execution_status = 'running', expiration_date = TO_TIMESTAMP(COALESCE(?, '19700101000000'), 'YYYYMMDDHH24MISS') " +
                            "WHERE task_execution_id=? and lu_id = ?"
                    , fabricExecutionId,expiration_date, taskExecutionID, luID);
        } catch (SQLException e) {
            log.error("Can't update batch ID in task_execution_list table for task_execution_id=" + taskExecutionID + ", lu_id: " + luID, e);
        }
    }

    private static void updateTaskExecutionSummary(Long taskExecutionId, String status) {
        try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_summary SET execution_status=? WHERE task_execution_id = ?", status, taskExecutionId);
        } catch (SQLException e) {
            log.error("Can't update status in task summary table for task_execution_id=" + taskExecutionId, e);
        }
    }
    private static void updateTaskExecutionsK2system(Long taskExecutionId, String status) {
        try {
            String AI_Interface = (String) fabric().fetch("set AI_DB_INTERFACE").firstValue();
            String k2systemSchema = "k2system";
            // Object clusterId = fabric().fetch("clusterid").firstValue();
            // if (clusterId != null && !"".equals(clusterId)) {
            //     k2systemSchema = k2systemSchema + "_" + clusterId;
            // }
            db(AI_Interface).execute("UPDATE " + k2systemSchema + ".task_executions SET status=? WHERE id = ?", status.toUpperCase(), taskExecutionId);
        } catch (SQLException e) {
            log.error("Can't update status in task summary table for task_execution_id=" + taskExecutionId, e);
        }
    }

    //TDM 7.4 - This function gets the fabric roles of the given user
    private static String getFabricRolesByUser(String userName) {
        List<String> roles = new ArrayList<>();
        Util.rte(() -> fabric().fetch("list users;").
                forEach(r -> {
                    if (userName.equals(r.get("user"))) {
                        roles.addAll(Arrays.asList(((String) r.get("roles")).split(",")));
                    }
                }));

        return String.join(TDM_PARAMETERS_SEPARATOR, roles);
    }

    //TDM 7.4 - This function gets the highest permission group of roles of the user
    private static String getPermissionGroupByRoles(String roles) {
        Map<String, Integer> PERMISSION_GROUPS = new HashMap() {{
            put("admin", 3);
            put("owner", 2);
            put("tester", 1);
        }};

        Integer[] weight = {0};
        String sql = "select permission_group from " + TDMDB_SCHEMA + ".permission_groups_mapping where fabric_role = ANY (string_to_array(?, ?))";
        Util.rte(() -> db(TDM).fetch(sql, roles,TDM_PARAMETERS_SEPARATOR).forEach(row -> {
            Integer nextWeight = PERMISSION_GROUPS.get(row.get("permission_group"));
            if (nextWeight != null && nextWeight > weight[0]) {
                weight[0] = nextWeight;
            }
        }));
        //log.info("This role is " + roles);
        if (weight[0] == 0) {
            log.error("TdmExecuteTask - Can't find permission group for the user!");
            return "";
        } else {
            String permissionGroup = null;
            for (Map.Entry<String, Integer> e : PERMISSION_GROUPS.entrySet()) {
                if (e.getValue().equals(weight[0])) {
                    permissionGroup = e.getKey();
                    break;
                }
            }

            return permissionGroup;
        }
    }
    //tdm 9.1 export schema in two modes in ParamsCoupling and AI
    private static void executeMDBExportSchema(Boolean verticalExecution, String luName, Long taskExecutionID, Long luID,Long taskID, String TDMDB_SCHEMA, String command, String errorCategory) {
        try {
            fabric().execute(command, luName);
        } catch (Exception e) {
            updatedFailedStatus(verticalExecution, taskExecutionID, luID);
            String insertSql = "insert into " + TDMDB_SCHEMA + ".TASK_EXE_ERROR_DETAILED (TASK_EXECUTION_ID,LU_NAME,ENTITY_ID,IID,TARGET_ENTITY_ID, " +
                                "ERROR_CATEGORY, ERROR_MESSAGE) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try {
                db(TDM).execute(insertSql, taskExecutionID, luName, " ", " ", " ", errorCategory, e.getMessage());
            } catch (SQLException e1) {
                log.error(e1);
                throw new RuntimeException(e1.getMessage());
            }
            throw new RuntimeException(e.getMessage());
        }
    }
    public enum TASK_PROPERTIES {
        TASK_ID(null),
        TASK_EXECUTION_ID(""),
        TASK_TYPE(""),
        TASK_TITLE(""),
        DATA_CENTER_NAME(""),
        LU_NAME(""),
        LU_ID(""),
        BE_ID(null),
        PARENT_LU_ID(null),
        PARENT_LU_NAME(""),
        PARENT_LU_STATUS(""),
        SELECTION_PARAM_VALUE(""),//EntitiesLIst,
        CUSTOM_LOGIC_LU_NAME(""),
        ENVIRONMENT_ID(null),
        SOURCE_ENVIRONMENT_ID(null),
        SOURCE_ENVIRONMENT_NAME(""),
        TARGET_ENVIRONMENT_NAME(""),
        LOAD_ENTITY(""),
        REPLACE_SEQUENCES("false"),
        REFRESH_REFERENCE_DATA("false"),
        SYNC_MODE(""),
        CREATION_DATE(""),
        NUM_OF_ENTITIES(null),
        TDM_SOURCE_PRODUCT_VERSION(""),
        TDM_TARGET_PRODUCT_VERSION(""),
        SELECTION_METHOD(""),
        DELETE_BEFORE_LOAD("false"),
        SELECTED_VERSION_TASK_EXE_ID(0),
        SELECTED_SUBSET_TASK_EXE_ID(0),
        RETENTION_PERIOD_TYPE(""),
        RETENTION_PERIOD_VALUE("0"),
        VERSION_IND("false"),
        RESERVE_IND("false"),
        RESERVE_RETENTION_PERIOD_TYPE(""),
        RESERVE_RETENTION_PERIOD_VALUE("0"),
        TASK_EXECUTED_BY(""),
        USER_ROLES(""),
        PARAMETERS(""),
        RESERVE_NOTE(""),
        FILTEROUT_RESERVED("OTHERS"),
        MASK_SENSITIVE_DATA("true"),
        CLONE_IND("false"),
        SEPARATOR("_"),
        TABLE_DEFAULT_DISTRIBUTION_MIN("1"),
        TABLE_DEFAULT_DISTRIBUTION_MAX("3"),
        PARAMS_COUPLING(false),
        EXECUTION_MODE("HORIZONTAL");
        private Object def;

        TASK_PROPERTIES(Object def) {
            this.def = def;
        }

        public String getName() {
            return this.name().toLowerCase();
        }

        //        @SuppressWarnings("unused")
        public <T> T get(Map<String, Object> args) {
            if (args == null) {
                return (T) this.def;
            } else {
                Object value = args.get(this.getName());
                return (T) (value == null ? this.def : value);
            }
        }
    }

    private static void fnCreateUpdateLUParamsForTask(Map<String, Object> taskProperties) throws Exception {

        String luName = LU_NAME.get(taskProperties);
        String executionMode = EXECUTION_MODE.get(taskProperties);
        String taskExecutionID = TASK_EXECUTION_ID.get(taskProperties).toString();
        if (!"VERTICAL".equalsIgnoreCase(executionMode)) {
            fnCreateUpdateLUParams(luName);
        } else {
            String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
            if (Util.isEmpty(entityInclusion)) {
                Db.Rows rows = Util.rte(() -> db(TDM).fetch("select lu_id, lu_name from " + TDMDB_SCHEMA + ".tasks_logical_units where task_id = "  + TASK_ID.get(taskProperties)));
                for (Db.Row luRow : rows) {
                    String luNameForVertical = luRow.get("lu_name").toString();
                    fnCreateUpdateLUParams(luNameForVertical);
                }
            }
        }
    }

    private static String fnGetTaskCreatedBy(String userID, String taskID) {
        try {
            String createdBy = "" + db(TDM).fetch("SELECT task_created_by FROM " + TDMDB_SCHEMA + ".tasks WHERE task_id=?", taskID).firstValue();
            String userName = createdBy.split("##")[0];
            return userName;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
