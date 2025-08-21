package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.io.IoCommand;
import com.k2view.fabric.common.io.IoSession;
import com.k2view.broadway.actors.masking.Masking;

import static com.k2view.cdbms.shared.user.UserCode.fabric;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.fabric.common.Util.safeClose;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class TDMMasking extends Masking {
    public static final Log log = Log.a(UserCode.class);

    public static final String QUERY_INSERT = "INSERT INTO " + TDMDB_SCHEMA + ".tdm_seq_mapping (" +
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

    private static String luName;
    private static String taskExecutionId;
    private static String sourceEnv;
    private static String entityTargetId;
    private static String rootTableName;
    private static String rootFieldName;
    public static Boolean TDM_SEQ_REPORT;
    public static Boolean TDM_USING_CATALOG_SEQUENCES;
    //private static IoSession TDMSession;

    @Override
    public void action(Data input, Data output, Context ctx) throws Exception {
        
        super.action(input, output, ctx);

        if (TDM_USING_CATALOG_SEQUENCES == null) {
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
        
        String tableName = input.get("dataset") != null ? input.string("dataset") : "" ;
        //log.info("TDMMasking - Input table: " + tableName);
        if (!tableName.isEmpty()) {
            tdmMasking(input,output, ctx);
        }
    }

    private void tdmMasking(Data input, Data output, Context ctx) throws Exception {
        //IoSession fabricSession = ctx.ioProvider().createSession("fabric");
        String category = input.string("category");
        //log.info("TDMMasking - category: " + category);
        if (!"enable_sequences".equalsIgnoreCase(category)) {
            return;
        }

        Object obj;
        obj = ctx.globals().get("TDM_SEQ_REPORT");
        if (obj != null) {
            TDM_SEQ_REPORT = Boolean.parseBoolean(obj.toString());
        } else {
            TDM_SEQ_REPORT = false;
        }
        //log.info("TDM_SEQ_REPORT#TDM_SEQ_REPORT: " +TDM_SEQ_REPORT);
        if (!TDM_SEQ_REPORT) {
            return;
        }

        //String enableSeq = fabricSession.prepareStatement("set enable_sequences").execute().iterator().next().get("value").toString();
        String enableSeq = "false";
        obj = ctx.globals().get("enable_sequences");
        if (obj != null) {
            enableSeq = obj.toString();
        }

        if ("true".equalsIgnoreCase(enableSeq)) {
            IoSession session = ctx.ioProvider().createSession("TDM");
            /*if (TDMSession == null){
                log.info("TDMSession1:" + input.get("dataset"));
                TDMSession = ctx.ioProvider().createSession("TDM");
            }*/
            IoCommand.Statement statement = session.prepareStatement(QUERY_INSERT);
            //IoCommand.Statement statement = TDMSession.prepareStatement(QUERY_INSERT);
            //String taskExecutionId = fabricSession.prepareStatement("set TDM_TASK_EXE_ID").execute().iterator().next().get("value").toString();            
            //String sourceEnv = fabricSession.prepareStatement("set TDM_SOURCE_ENVIRONMENT_NAME").execute().iterator().next().get("value").toString();
            //String entityTargetId = "";
            //Object entityObj = fabricSession.prepareStatement("set TARGET_ENTITY_ID").execute().iterator().next().get("value");
            //if (entityObj != null) {
            //    entityTargetId = entityObj.toString();
            //}

            //String luName = "";
            //Object luNameObj = fabricSession.prepareStatement("set LU_TYPE").execute().iterator().next().get("value");
            //if (luNameObj != null) {
            //    luName = luNameObj.toString();
            //}
            //String rootTableName = fabricSession.prepareStatement("set " + luName + ".ROOT_TABLE_NAME").execute().iterator().next().get("value").toString();
            //String rootFieldName = fabricSession.prepareStatement("set " + luName + ".ROOT_COLUMN_NAME").execute().iterator().next().get("value").toString();

            if (taskExecutionId == null) {
                taskExecutionId = ctx.globals().get("TDM_TASK_EXE_ID").toString();
            }

            if (sourceEnv == null) {
                sourceEnv = ctx.globals().get("TDM_SOURCE_ENVIRONMENT_NAME").toString();
            }
            
            if (entityTargetId == null) {
                entityTargetId = ctx.globals().get("TARGET_ENTITY_ID").toString();
            }

            if (luName == null) {
                luName = ctx.globals().get("LU_TYPE").toString();
            }
            
            if (rootTableName == null) {
                rootTableName = ctx.globals().get("ROOT_TABLE_NAME").toString();
            }
            if (rootFieldName == null) {
                rootFieldName = ctx.globals().get("ROOT_COLUMN_NAME").toString();
            }

            String tableName = input.string("dataset");
            String fieldName = input.string("field");
                        
            String isInstance = "N";
            if (tableName.equalsIgnoreCase(rootTableName) && fieldName.equalsIgnoreCase(rootFieldName)) {
                entityTargetId = output.string("value");
                isInstance = "Y";
            }

            /*log.info("FFFFFFFFF: " + taskExecutionId +", " + luName + ", " + sourceEnv + ", " + 
                    entityTargetId + ", " +
                    input.string("sequenceId") + ", " +
                    tableName + ", " +
                    fieldName + ", " +
                    input.string("value") + ", " +
                    output.string("value") + ", " +
                    isInstance);*/
        
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

            session.close();
            statement.close();
        }

        //fabricSession.close();

    }

    /*@Override
    public void close() {
        safeClose(TDMSession);
        TDMSession = null;
    }*/
}
