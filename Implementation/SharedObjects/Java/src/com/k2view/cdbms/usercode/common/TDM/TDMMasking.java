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
import static com.k2view.cdbms.shared.user.UserCode.getLuType;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class TDMMasking extends Masking {
    public static final Log log = Log.a(UserCode.class);
    public static Boolean TDM_SEQ_REPORT;
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
        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void action(Data input, Data output, Context ctx) throws Exception {
        
        super.action(input, output, ctx);
        
        String tableName = input.get("dataset") != null ? input.string("dataset") : "" ;
        //log.info("TDMMasking - Input table: " + tableName);
        if (!tableName.isEmpty()) {
            tdmMasking(input,output, ctx);
        }
    }

    private void tdmMasking(Data input, Data output, Context ctx) throws Exception {
        IoSession fabricSession = ctx.ioProvider().createSession("fabric");
        if (TDM_SEQ_REPORT == null) {
            Object tdmSeqReport = fabricSession.prepareStatement("set TDM_SEQ_REPORT").execute().iterator().next().get("value");
            TDM_SEQ_REPORT = tdmSeqReport != null ? Boolean.parseBoolean(tdmSeqReport.toString()) : false;
        }
        String category = input.string("category");
        //log.info("TDMMasking - category: " + category + ", TDM_SEQ_REPORT: " + TDM_SEQ_REPORT);
        if (!"enable_sequences".equalsIgnoreCase(category) || !TDM_SEQ_REPORT) {
            fabricSession.close();
            return;
        }

        String enableSeq = fabricSession.prepareStatement("set enable_sequences").execute().iterator().next().get("value").toString();

        if ("true".equalsIgnoreCase(enableSeq)) {
            IoSession session = ctx.ioProvider().createSession("TDM");
            IoCommand.Statement statement = session.prepareStatement(QUERY_INSERT);
            String taskExecutionId = fabricSession.prepareStatement("set TDM_TASK_EXE_ID").execute().iterator().next().get("value").toString();
            String sourceEnv = fabricSession.prepareStatement("set TDM_SOURCE_ENVIRONMENT_NAME").execute().iterator().next().get("value").toString();
            String entityTargetId = "";
            Object entityObj = fabricSession.prepareStatement("set TARGET_ENTITY_ID").execute().iterator().next().get("value");
            if (entityObj != null) {
                entityTargetId = entityObj.toString();
            }

            String luName = "";
            Object luNameObj = fabricSession.prepareStatement("set LU_TYPE").execute().iterator().next().get("value");
            if (luNameObj != null) {
                luName = luNameObj.toString();
            }
            
            String tableName = input.string("dataset");
            String fieldName = input.string("field");
            String rootTableName = fabricSession.prepareStatement("set " + luName + ".ROOT_TABLE_NAME").execute().iterator().next().get("value").toString();
            String rootFieldName = fabricSession.prepareStatement("set " + luName + ".ROOT_COLUMN_NAME").execute().iterator().next().get("value").toString();
            
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

            session.close();
            statement.close();
        }

        fabricSession.close();

    }
}
