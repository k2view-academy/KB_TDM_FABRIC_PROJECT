package com.k2view.cdbms.usercode.common.TDM;

import com.google.gson.Gson;
import com.k2view.cdbms.exceptions.InstanceNotFoundException;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.ResultSetWrapper;
import com.k2view.cdbms.shared.logging.LogEntry;
import com.k2view.cdbms.shared.logging.MsgId;
import com.k2view.fabric.common.Util;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.k2view.cdbms.shared.user.UserCode.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class TdmSharedUtils {
    private static final String TDM = "TDM";
    private static final String REF = "REF";
    private static final String TASK_EXECUTION_LIST = "task_execution_list";
    private static final String TASK_REF_TABLES = "TASK_REF_TABLES";
    private static final String PRODUCT_LOGICAL_UNITS = "product_logical_units";
    private static final String TASK_REF_EXE_STATS = "TASK_REF_EXE_STATS";
    private static final String TASKS_LOGICAL_UNITS = "tasks_logical_units";
    private static final String TDM_COPY_REFERENCE = "fnTdmCopyReference";
    private static final String PENDING = "pending";
    private static final String RUNNING = "running";
    private static final String WAITING = "waiting";
    private static final String STOPPED = "stopped";
    private static final String RESUME = "resume";
    private static final String FAILED = "failed";
    private static final String COMPLETED = "completed";
    private static final String PARENTS_SQL = "SELECT lu_name  FROM product_logical_units WHERE be_id=? AND lu_parent_id is null";
    private static final String GET_CHILDREN_SQL = "WITH RECURSIVE children AS ( " +
            "SELECT lu_name,lu_id,lu_parent_id,lu_parent_name FROM product_logical_units WHERE lu_name=? and be_id=? " +
            "UNION ALL SELECT a.lu_name, a.lu_id, a.lu_parent_id,a.lu_parent_name " +
            "FROM product_logical_units a " +
            "INNER JOIN children b ON a.lu_parent_id = b.lu_id) " +
            "SELECT  string_agg('''' ||  unnest || '''' , ',') FROM children ,unnest(string_to_array(children.lu_name, ',')); ";

    static void setBroadwayActorFlags(String key, String value) throws SQLException {
        //TDM 7.1 - Masking and Sequence Broadway Actors have special flags to enable/disable them, and they need
        // to be set based on the input globals of the task

        // Masking Actor - If MASK_FLAG is set to false set the indicator of masking actor to false to suppress the masking in
        // both Load and Extract tasks. If MASK_FLAG is not set or set to true then nothing to do as the Masking is enabled by default.
        if (key.contains("MASK_FLAG") && "0".equals(value)) {
            //log.info("setBroadawayActorFlags - Disabling Masking");
            fabric().execute("set enable_masking = false");
        }
        // Sequence Actor - If TDM_REPLACE_SEQUENCES is set, used its value to disable/enable the sequence actor
        if (key.contains("TDM_REPLACE_SEQUENCES")) {
            //log.info("setBroadawayActorFlags - Setting Sequence Actor to: " + value);
            fabric().execute("set enable_sequences = ?", value);
        }
    }

    public static Integer getRetention(String retentionPeriodType, Float retentionPeriodValue) {
        Integer retention_in_seconds = 0;

        switch (retentionPeriodType) {
            case "Minutes":
                retention_in_seconds = Math.round(retentionPeriodValue * 60);
                break;
            case "Hours":
                retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60);
                break;
            case "Days":
                retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60 * 24);
                break;
            case "Weeks":
                retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60 * 24 * 7);
                break;
            case "Years":
                retention_in_seconds = Math.round(retentionPeriodValue * 60 * 60 * 24 * 7 * 365);
                break;
        }
        return retention_in_seconds;
    }

    public static Object fnBatchStats(String i_batchId, String i_runMode) throws Exception {
        Object response;
        switch (i_runMode) {
            case "S":
                response = getFabricResponse("batch_summary '" + i_batchId + "'");
                break;
            case "D":
                response = getFabricResponse("batch_details '" + i_batchId + "'");
                break;
            case "H":
                ResultSetWrapper rs = null;
                String migCommand = null;
                String sql1 = null;
                String migrateDesc = null;
                Map<String, String> migHeader = new LinkedHashMap<>();
                String clusterID = (String) ludb().fetch("clusterid").firstValue();

                if (clusterID == null || clusterID.isEmpty()) {
                    sql1 = "select command from k2batchprocess.batchprocess_list where bid='" + i_batchId + "'";
                } else {
                    sql1 = "select command from k2batchprocess_" + clusterID + ".batchprocess_list where bid='" + i_batchId + "'";
                }

                migrateDesc = (String) db("DB_CASSANDRA").fetch(sql1).firstValue();
                migHeader.put("Migration Command", migrateDesc);
                return migHeader;
            default:
                response = new HashMap() {{
                    put("errorCode", "FAIL");
                    put("message", "Unknown run mode '" + i_runMode + "'. Available modes are 'S' for batch summary, 'D' for the details and 'H'.");
                }};
                break;
        }
        return response;
    }

    public static Object getFabricResponse(String fabricCommand) throws SQLException {
        List objects = new ArrayList();
        fabric().fetch(fabricCommand).forEach(row -> {
            Map rowMap = new HashMap<String, Object>();
            rowMap.putAll(row);
            objects.add(rowMap);
        });
        return objects;
    }

    public static String generateListOfMatchingEntitiesQuery(Long beID, String whereStmt, String sourceEnv) throws SQLException {

        final String where = !Util.isEmpty(whereStmt) ? "where " + whereStmt : "";
        Db tdmDB = db("TDM");
        Db.Rows rows = tdmDB.fetch("SELECT be_name FROM public.business_entities WHERE be_id=?;", beID);
        String beName = (String) rows.firstValue();
        if (Util.isEmpty(beName)) {
            throw new RuntimeException("business entity does not exist");
        }
        String rootLu = "";
        String luRelationsView = "lu_relations_" + beName + "_" + sourceEnv;

        AtomicInteger parentsCount = new AtomicInteger();

        Db.Rows parents = tdmDB.fetch(PARENTS_SQL, beID);
        Map<String, String> setTypesSql = new HashMap<>();
        Map<String, String> getParamsSqlMap = new HashMap<>();
        List<String> getEntitiesSqlList = new ArrayList<>();
        StringBuilder setTypesQuery = new StringBuilder();
        for (Db.Row luRow : parents) {
            ResultSet resultSet = luRow.resultSet();
            rootLu = resultSet.getString("lu_name");
            String rootLuStrL = rootLu.toLowerCase();
            String jsonTypeName = sourceEnv + "_" + beName + "_" + rootLu + "_json_type";
            parentsCount.getAndIncrement();

            Db.Rows children = tdmDB.fetch(GET_CHILDREN_SQL, rootLu, beID);

            String childrenList = (String) children.firstValue();
            setTypesQuery.append(getSetTypesQuery(sourceEnv, rootLu, jsonTypeName, childrenList));


            String paramsSql = getParamsSql(sourceEnv, rootLu, rootLuStrL, jsonTypeName, childrenList);

            getParamsSqlMap.put(rootLu, paramsSql);
            getEntitiesSqlList.add(" SELECT DISTINCT " + rootLuStrL + "_root_id as entity_id FROM \"" + luRelationsView + "\" " + where);
        }

        String getParamsSql = "";

        int count = parentsCount.get();
        if (count > 0) {
            if (count == 1) {
                getParamsSql = getParamsSqlMap.get(rootLu); //todo check key
            } else {
                final String firstLu = getParamsSqlMap.entrySet().stream().findFirst().get().getKey();
                String sql = getParamsSqlMap.remove(firstLu);
                getParamsSql += "(" + sql + ") as " + firstLu + "_final " + getParamsSqlMap.entrySet().stream().map((entry) -> " FULL JOIN (" + entry.getValue() + ") as " + entry.getKey() + "_final" + " ON " + firstLu + "_final." + firstLu + "_root_id=" + entry.getKey().toLowerCase() + "_final." + entry.getKey().toLowerCase() + "_root_id ")
                        .collect(Collectors.joining(" "));
            }
        }

        String createViewSql = createViewSql(luRelationsView, setTypesQuery.toString(), getParamsSql);
        tdmDB.execute(createViewSql);
        return getEntitiesSqlList.stream().map(Object::toString).collect(Collectors.joining(" UNION "));

    }


    private static String createViewSql(String luRelationsView, String setTypesQuery, String getParamsSql) {
        return "DO $$ " +
                "DECLARE " +
                "type_var text; " +
                "BEGIN " +
                "IF NOT EXISTS (SELECT 1 FROM pg_matviews WHERE matviewname =  '" + luRelationsView + "'" +
                ") THEN " + setTypesQuery +
                "CREATE MATERIALIZED VIEW \"" + luRelationsView + "\" AS SELECT * FROM  ( " + getParamsSql + " ) AS mergedTables;" +
                "END IF; " +
                "END $$ ";
    }

    private static String getParamsSql(String sourceEnv, String rootLu, String rootLuStrL, String jsonTypeName, String childrenList) {

        String str = "IN(" + childrenList.replace("'", "''") + ")";
        return "WITH RECURSIVE relations AS(" +
            "SELECT lu_type_1, lu_type_2, entity_id as lu_type1_eid, lu_type2_eid, entity_id as root_id," +
            " param_values('" + rootLu +"',entity_id,lower(base.lu_type_1) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(base.lu_type_1) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment'),'" + str + "', 'lu_type1_eid', '-1')::text as p1," +
            " param_values('" + rootLu + "',entity_id,lower(base.lu_type_2) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(base.lu_type_2) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment')," +
            "'" + str + "', 'lu_type2_eid'" +
            ",base.lu_type_2)::text as p2 " +
            "FROM ( " +
            "SELECT entity_id, '" + rootLu + "'::text as lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid " +
            "FROM " + rootLu + "_params " +
            " LEFT JOIN (SELECT * FROM tdm_lu_type_relation_eid WHERE lu_type_1= '" + rootLu + "' AND source_env='" + sourceEnv +"' AND lu_type_2 IN(" + childrenList + ") AND version_name='' " +
			" AND (EXISTS (SELECT 1 FROM information_schema.columns WHERE columns.table_name::name = (lower(tdm_lu_type_relation_eid.lu_type_2::text) || '_params'::text) AND columns.column_name::name <> 'entity_id'::name AND columns.column_name::name <> 'source_environment'::name LIMIT 1))  )" +
            " AS rel_base ON " + rootLu + "_params.entity_id=rel_base.lu_type1_eid" +
            " WHERE source_environment='" + sourceEnv + "') AS base " +
            " UNION ALL" +
            " SELECT a.lu_type_1, a.lu_type_2, a.lu_type1_eid, a.lu_type2_eid, root_id," +
            " param_values('" + rootLu + "',a.lu_type1_eid,lower(a.lu_type_1) || '_params','" + sourceEnv +"'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(a.lu_type_1) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment'), '" + str + "', 'lu_type1_eid', '-1')::text as p1," +
            " param_values('" + rootLu + "',a.lu_type1_eid,lower(a.lu_type_2) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(a.lu_type_2) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment')," +
            "'" + str + "', 'lu_type2_eid'" +
            ",a.lu_type_2)::text as p2 " +
            "FROM tdm_lu_type_relation_eid a " +
            "INNER JOIN relations b ON b.lu_type2_eid = a.lu_type1_eid  AND b.lu_type_2 = a.lu_type_1 AND source_env='" + sourceEnv + "' AND a.lu_type_2 IN("+ childrenList +")  AND b.lu_type_2 IN(" + childrenList + ") AND a.lu_type_1 IN(" + childrenList + ") AND b.lu_type_1 IN("+ childrenList +"))" +
            " SELECT * FROM ( " +
            " SELECT root_id AS " + rootLuStrL + "_root_id, (json_populate_recordset(null::\"" + jsonTypeName +"\", json_agg(path))).*" +
            " FROM (" +
            " SELECT root_id,  json_append(p1::json, (replace(string_agg(p2, ', '), '}, {', ','))::json, '{}') AS path from relations GROUP BY root_id, p1) colsMerged GROUP BY root_id) AS final";
    }

    private static String getSetTypesQuery(String sourceEnv, String rootLu, String json_type_name, String childrenList) {
        return " " +
                "DROP TYPE IF EXISTS \"" + json_type_name + "\";" +
                "WITH RECURSIVE cols AS " +
                "(SELECT lu_type as lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid, concat(table_params1, table_params2) AS table_params FROM (" +
                "SELECT * FROM (SELECT '" + rootLu + "'::text as lu_type, (SELECT string_agg(lu_param_col::text, ',') AS param FROM (select '\"' || column_name || '\"' AS lu_param_col FROM information_schema.columns WHERE table_name=concat(LOWER('" + rootLu + "'),'_params') AND column_name <> 'source_environment' AND column_name <> 'be_id' AND column_name <> 'entity_id') AS params) AS table_params1) AS t1 " +
                "LEFT JOIN (" +
                "SELECT lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid," +
                "(SELECT CASE WHEN lu_type_2 IN(" + childrenList + ") THEN ',' || string_agg(lu_param_col::text, ',')  ELSE '' END AS param FROM (SELECT '\"' || column_name || '\"' AS lu_param_col FROM information_schema.columns WHERE table_name=concat(LOWER(lu_type_2),'_params') AND column_name <> 'source_environment' AND column_name <> 'be_id' AND column_name <> 'entity_id') AS params) AS table_params2 " +

                "FROM tdm_lu_type_relation_eid WHERE lu_type_1= '" + rootLu + "' AND source_env='" + sourceEnv + "' AND lu_type_2 IN(" + childrenList + ")) AS rel_base ON t1.lu_type=rel_base.lu_type_1 ) as base " +

                "UNION ALL SELECT a.lu_type_1, a.lu_type_2, a.lu_type1_eid, a.lu_type2_eid," +
                "concat(table_params,',',(SELECT string_agg(lu_param_col::text, ',') as param from (select '\"' || column_name || '\"' AS lu_param_col from information_schema.columns where table_name=concat(LOWER(a.lu_type_1),'_params') AND column_name <> 'source_environment' AND column_name <> 'be_id' AND column_name <> 'entity_id') AS params),','," +
                "(SELECT string_agg(lu_param_col::text, ',') AS param from (select '\"' || column_name || '\"' AS lu_param_col from information_schema.columns where table_name=concat(LOWER(a.lu_type_2),'_params') AND column_name <> 'source_environment' AND column_name <> 'be_id' AND column_name <> 'entity_id') as params))" +

                "FROM tdm_lu_type_relation_eid a " +
                "INNER JOIN cols b ON b.lu_type2_eid = a.lu_type1_eid  AND b.lu_type_2 = a.lu_type_1 AND a.lu_type_2 IN(" + childrenList + "))" +

                "select ' CREATE type \"" + json_type_name + "\" AS (' || string_agg(distinct unnest || ' text[] ' , ',') || ');' into type_var from cols ,unnest(string_to_array(cols.table_params, ','));" +

                "IF type_var IS NOT NULL " +
                "THEN EXECUTE type_var;" +
                "ELSE EXECUTE ' CREATE type \"" + json_type_name + "\" AS (" + rootLu + "_dummy text);';" +
                "END IF; ";
    }

    public static void fnTdmUpdateTaskExecutionEntities(String taskExecutionId, Long luId, String luName) throws Exception {
        // TALI- 5-May-20- add a select of selection_method  + fabric_Execution_uid columns.
        //Remove the condition of fabric_execution_id is not null to support reference only task

        //String taskExeListSql = "SELECT L.FABRIC_EXECUTION_ID, L.SOURCE_ENV_NAME, L.CREATION_DATE, L.START_EXECUTION_TIME, " +
        //	"L.END_EXECUTION_TIME, L.ENVIRONMENT_ID, T.VERSION_IND, T.TASK_TITLE, L.VERSION_DATETIME, T.SELECTION_METHOD, COALESCE(FABRIC_EXECUTION_ID, '') AS FABRIC_EXECUTION_ID " +
        //    "FROM TASK_EXECUTION_LIST L, TASKS T " +
        //	"WHERE TASK_EXECUTION_ID = ? AND LU_ID = ? AND L.TASK_ID = T.TASK_ID";

        String taskExeListSql = "SELECT L.SOURCE_ENV_NAME, L.CREATION_DATE, L.START_EXECUTION_TIME, " +
                "L.END_EXECUTION_TIME, L.ENVIRONMENT_ID, T.VERSION_IND, T.TASK_TITLE, to_char(L.VERSION_DATETIME,'YYYY-MM-DD HH24:MI:SS') AS VERSION_DATETIME, " +
                "T.SELECTION_METHOD, COALESCE(FABRIC_EXECUTION_ID, '') AS FABRIC_EXECUTION_ID " +
                "FROM TASK_EXECUTION_LIST L, TASKS T " +
                "WHERE TASK_EXECUTION_ID = ? AND LU_ID = ? AND L.TASK_ID = T.TASK_ID";

        String fabricExecID = "";
        String srcEnvName = "";
        String creationDate = "";
        String startExecDate = "";
        String endExecDate = "";
        String envID = "";
        String entityID = "";
        String targetEntityID = "";
        String execStatus = "";
        String idType = "ENTITY";
        String IID = "";
        String versionInd = "";
        String versionName = "";
        String verstionDateTime = "";
        // Add selectionMethod
        String selectionMethod = "";

        final String UIDLIST = "UIDList";

        String insertSql = "INSERT INTO TASK_EXECUTION_ENTITIES(" +
                "TASK_EXECUTION_ID, LU_NAME, ENTITY_ID, TARGET_ENTITY_ID, ENV_ID, EXECUTION_STATUS, ID_TYPE, " +
                "FABRIC_EXECUTION_ID, IID, SOURCE_ENV";
        String insertBinding = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

        Db.Row taskData = db("TDM").fetch(taskExeListSql, taskExecutionId, luId).firstRow();


        //log.info("tdmUpdateTaskExecutionEntities: TASK_EXECUTION_ID: " + taskExecutionId + ", LU_ID: " + luId + ", LU_NAME: " + luName);
        if (!taskData.isEmpty()) {
            fabricExecID = "" + taskData.get("fabric_execution_id");
            srcEnvName = "" + taskData.get("source_env_name");
            creationDate = "" + taskData.get("creation_date");
            startExecDate = "" + taskData.get("start_execution_time");
            endExecDate = "" + taskData.get("end_execution_time");
            envID = "" + taskData.get("environment_id");
            versionInd = "" + taskData.get("version_ind");
            versionName = "" + taskData.get("task_title");
            verstionDateTime = "" + taskData.get("version_datetime");

            // Add selection method and fabric_execution_id
            selectionMethod = "" + taskData.get("selection_method");

            //log.info("creationDate: " + creationDate + ", startExecDate: " + startExecDate + ", endExecDate: " + endExecDate + ", SELECTION METHOD: " + selectionMethod);

            if (!"null".equals(creationDate) && !"".equals(creationDate)) {
                insertSql += ", CREATION_DATE";
                creationDate = creationDate.substring(1);
                insertBinding += ", ?";
            }

            if (!"null".equals(startExecDate) && !"".equals(startExecDate)) {
                insertSql += ", ENTITY_START_TIME";
                insertBinding += ", ?";
            }

            if (!"null".equals(endExecDate) && !"".equals(endExecDate)) {
                insertSql += ", ENTITY_END_TIME";
                insertBinding += ", ?";
            }

            if ("true".equals(versionInd)) {
                insertSql += ", VERSION_NAME,VERSION_DATETIME";
                insertBinding += ", ?, ?";
            }

            insertBinding += ")";
            insertSql += ") " + insertBinding;
            insertSql += " ON CONFLICT ON CONSTRAINT task_execution_entities_pkey Do update set execution_status = ?";

            // TALI- 5-May-20 - add a check of the sectionMethod. Do not get the list of IIDs for reference only task

            Map<String, Map> migrationList = new LinkedHashMap<String, Map>();
            //Map<String, Map> migrationList = (Map<String, Map>) fnGetIIDListForMigration(fabricExecID, null);
            if (!selectionMethod.equals("REF") && !fabricExecID.equals("")) {
                migrationList = (Map<String, Map>) SharedLogic.fnGetIIDListForMigration(fabricExecID, null);
            }

            //log.info ("tdmUpdateTaskExecutionEntities - insertSql: " + insertSql);
            if (migrationList.containsKey("Copied entities per execution")) {
                LinkedHashMap<String, Object> m1 = (LinkedHashMap<String, Object>) migrationList.get("Copied entities per execution");

                if (m1.containsKey(UIDLIST)) {
                    List<Object> copied_UID_list = (List<Object>) m1.get(UIDLIST);
                    //log.info("Size of copied_UID_list: " + copied_UID_list.size());
                    for (Object UID : copied_UID_list) {
                        Map<Object, Object> innerCopiedUIDMap = (Map<java.lang.Object, java.lang.Object>) UID;

                        for (Map.Entry<Object, Object> copiedUID : innerCopiedUIDMap.entrySet()) {
                            targetEntityID = (String) copiedUID.getKey();
                            IID = (String) copiedUID.getKey();
                            entityID = (String) copiedUID.getValue();
                            execStatus = COMPLETED;

                            ArrayList<String> paramList = new ArrayList<>();

                            paramList.add(taskExecutionId);
                            paramList.add(luName);
                            paramList.add(entityID);
                            paramList.add(targetEntityID);
                            paramList.add(envID);
                            paramList.add(execStatus);
                            paramList.add(idType);
                            paramList.add(fabricExecID);
                            paramList.add(IID);
                            paramList.add(srcEnvName);

                            //log.info("Inserting Copied: LU_NAME: " + LU_NAME + ", TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", entityID: " + entityID);
                            //In postgres, timestamp fields cannot be set to empty string,
                            //therefore date fields should be insterted only if they have value
                            //log.info("Inserting: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_NAME: " + LU_NAME + ", entityID: " + entityID);
                            if (!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
                            if (!"null".equals(startExecDate) && !"".equals(startExecDate))
                                paramList.add(startExecDate);
                            if (!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
                            if ("true".equals(versionInd)) {
                                paramList.add(versionName);
                                paramList.add(verstionDateTime);
                            }

                            //Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
                            //in that case only the status will be updated,such case can happen in case of cancel resume
                            paramList.add(execStatus);

                            Object[] params = paramList.toArray();

                            //log.info ("insertSql - Copied Entities: " + insertSql);
                            db("TDM").execute(insertSql, params);
                        }

                    }
                }
            }

            if (migrationList.containsKey("Failed entities per execution")) {
                LinkedHashMap<String, Object> m2 = (LinkedHashMap<String, Object>) migrationList.get("Failed entities per execution");
                if (m2.containsKey(UIDLIST)) {
                    List<Object> failed_UID_list = (List<Object>) m2.get(UIDLIST);
                    for (Object UID : failed_UID_list) {
                        Map<Object, Object> innerFailedUIDMap = (Map<java.lang.Object, java.lang.Object>) UID;

                        for (Map.Entry<Object, Object> failedUID : innerFailedUIDMap.entrySet()) {
                            targetEntityID = (String) failedUID.getKey();
                            IID = (String) failedUID.getKey();
                            entityID = (String) failedUID.getValue();
                            execStatus = FAILED;

                            ArrayList<String> paramList = new ArrayList<>();

                            paramList.add(taskExecutionId);
                            paramList.add(luName);
                            paramList.add(entityID);
                            paramList.add(targetEntityID);
                            paramList.add(envID);
                            paramList.add(execStatus);
                            paramList.add(idType);
                            paramList.add(fabricExecID);
                            paramList.add(IID);
                            paramList.add(srcEnvName);

                            //log.info("Inserting Failed: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_NAME: " + LU_NAME + ", entityID: " + entityID);
                            if (!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
                            if (!"null".equals(startExecDate) && !"".equals(startExecDate))
                                paramList.add(startExecDate);
                            if (!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
                            if ("true".equals(versionInd)) {
                                paramList.add(versionName);
                                paramList.add(verstionDateTime);
                            }
                            //Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
                            //in that case only the status will be updated,such case can happen in case of cancel resume
                            paramList.add(execStatus);

                            Object[] params = paramList.toArray();

                            //log.info ("insertSql - Failed Entities: " + insertSql);
                            db("TDM").execute(insertSql, params);
                        }

                    }
                }
            }

            //Add reference Entities to TASK_EXECUTION_ENTITIES table
            String refListSql = "SELECT REF_TABLE_NAME, EXECUTION_STATUS FROM TASK_REF_EXE_STATS ES WHERE " +
                    "TASK_EXECUTION_ID = ? AND TASK_REF_TABLE_ID IN (SELECT TASK_REF_TABLE_ID FROM TASK_REF_TABLES RT " +
                    "WHERE RT.TASK_ID = ES.TASK_ID AND RT.TASK_REF_TABLE_ID = ES.TASK_REF_TABLE_ID AND RT.LU_NAME = ?)";

            idType = "REFERENCE";

            Db.Rows refList = db("TDM").fetch(refListSql, taskExecutionId, luName);

            for (Db.Row refTable : refList) {
                entityID = "" + refTable.get("ref_table_name");
                targetEntityID = entityID;
                execStatus = "" + refTable.get("execution_status");
                IID = entityID;

                ArrayList<String> paramList = new ArrayList<>();

                paramList.add(taskExecutionId);
                paramList.add(luName);
                paramList.add(entityID);
                paramList.add(targetEntityID);
                paramList.add(envID);
                paramList.add(execStatus);
                paramList.add(idType);
                paramList.add(fabricExecID);
                paramList.add(IID);
                paramList.add(srcEnvName);

                if (!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
                if (!"null".equals(startExecDate) && !"".equals(startExecDate)) paramList.add(startExecDate);
                if (!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
                if ("true".equals(versionInd)) {
                    paramList.add(versionName);
                    paramList.add(verstionDateTime);
                }
                //Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
                //in that case only the status will be updated,such case can happen in case of cancel resume
                paramList.add(execStatus);

                Object[] params = paramList.toArray();

                db("TDM").execute(insertSql, params);
            }

            if (refList != null) {
                refList.close();
            }
        }
    }

    public static boolean checkWsResponse(Map<String, Object> response) {
        if (response != null && response.get("errorCode") != null && response.get("errorCode").equals("SUCCESS")) {
            return true;
        } else {
            return false;
        }
    }

    public static Map<String, Object> wrapWebServiceResults(String errorCode, Object message, Object result) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("result", result);
        return response;
    }

    public static void fnTdmCopyReference(String taskExecutionID, String taskType) throws Exception {
        //log.info("-- START Reference JOB for Task Type: " + taskType + " Task Execution ID: " + taskExecutionID + "---");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();

        Db.Rows refTabLst = null;

        String refQuery = "Select ES.* from TASK_REF_EXE_STATS ES, TASKS T where execution_status  in ('" + WAITING + "', '" + PENDING + "', '" + RESUME + "', '" + STOPPED + "') " +
                "and ES.TASK_ID = T.TASK_ID and lower(T.TASK_TYPE) = ? and ES.TASK_EXECUTION_ID = ? order by task_execution_id, task_id";

        refTabLst = db(TDM).fetch(refQuery, taskType, taskExecutionID);

        int refCount = 0;

        for (Db.Row row : refTabLst) {
            refCount++;
            String taskRefTableID = "" + row.get("task_ref_table_id");
            String refTableName = "" + row.get("ref_table_name");
            //log.info("fnTdmCopyReference - refTableName: " + refTableName);

            if (!StringUtils.isEmpty(taskRefTableID) && !StringUtils.isEmpty(taskExecutionID)) {

                String luName = "" + db(TDM).fetch("Select lu_name from " + TASK_REF_TABLES + " where task_ref_table_id = ?", taskRefTableID).firstValue();

                String taskID = "" + row.get("task_id");

                String execStatus = "" + row.get("execution_status");

                //log.info("fnTdmCopyReference - execution_status: " + execStatus);

                Db.Row taskParams = db(TDM).fetch("Select l.source_env_name, e.environment_name as target_env_name, t.version_ind, " +
                                "t.retention_period_type, t.retention_period_value, t.selection_method, t.selected_ref_version_task_name, " +
                                "t.selected_ref_version_datetime, t.selected_ref_version_task_exe_id " +
                                "from TASKS t, TASK_EXECUTION_LIST l, ENVIRONMENTS e " +
                                "where task_execution_id = ? and l.task_id = ? and t.task_id = l.task_id and l.environment_id = e.environment_id",
                        taskExecutionID, taskID).firstRow();

                String versionInd = "false";
                if (taskParams.get("version_ind") != null) {
                    versionInd = "" + taskParams.get("version_ind");
                }
                Object retPeriodType = taskParams.get("retention_period_type");
                String retType = (retPeriodType != null) ? "" + retPeriodType : "";

                Object retPeriodVal = taskParams.get("retention_period_value");
                Float retVal = (retPeriodVal != null) ? Float.valueOf(retPeriodVal.toString()) : 0;
                Integer ttl = getRetention(retType, retVal);

                Db.Row luData = db(TDM).fetch("select p.lu_id, p.lu_dc_name from " + TASKS_LOGICAL_UNITS + " t, "
                                + PRODUCT_LOGICAL_UNITS + " p where t.lu_name = ? and t.task_id = ? and t.lu_name = p.lu_name and t.lu_id = p.lu_id",
                        luName, taskID).firstRow();

                String luID = "" + luData.get("lu_id");
                String luDCName = "" + luData.get("lu_dc_name");

                String affinity = "";

                if (luDCName != null && !"".equals(luDCName) && !"null".equals(luDCName)) {
                    affinity = " AFFINITY='" + luDCName + "'";
                }

                String uid = "" + row.get("job_uid");

                Object selectionMethod = "" + taskParams.get("selection_method");

                if (selectionMethod != null && selectionMethod.toString().equals(REF)) {
                    Long unixTime = System.currentTimeMillis();
                    Long unixTime_plus_retention;

                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
                    Integer retention_in_seconds = getRetention(retType, retVal);

                    unixTime_plus_retention = (unixTime / 1000L + retention_in_seconds) * 1000;
                    String versionExpirationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(unixTime_plus_retention);

                    Object[] param;
                    if (versionInd.equals("true")) {
                        param = new Object[]{RUNNING, timeStamp, versionExpirationDate, taskExecutionID, taskID, luID};
                    } else {
                        param = new Object[]{RUNNING, null, null, taskExecutionID, taskID, luID};
                    }

                    //log.info("fnTdmCopyReference - Updating task to running for - task_execution_id: " + taskExecutionID +
                    //		", task_id: " + taskID + ", lu_id: " + luID);

                    db(TDM).execute("update " + TASK_EXECUTION_LIST + " set execution_status = ?, " +
                            "version_datetime = ?, version_expiration_date = ? " +
                            "where task_execution_id = ? and task_id = ? and lu_id = ?", param);
                }
                //int count = 0;
                //int retries = 5;

                String sourceEnv = "" + taskParams.get("source_env_name");
                String targetEnv = "" + taskParams.get("target_env_name");
                //log.info("fnTdmCopyReference - sourceEnv: " + sourceEnv + ", targetEnv: " + targetEnv);

                if (taskType.equalsIgnoreCase("extract")) {

                    String args = " ARGS='{\"sourceEnv\":\"" + sourceEnv + "\",\"versionID\":\"" + versionInd +
                            "\",\"ttl\":\"" + ttl + "\", \"taskExecID\":\"" + taskExecutionID +
                            "\",\"taskRefTableID\":\"" + taskRefTableID + "\",\"luName\":\"" + luName + "\"}'";

                    //while(!Thread.currentThread().isInterrupted()) {
                    try {
                        switch (execStatus) {
                            case WAITING:
                            case PENDING:
                                //log.info("fnTdmCopyReference -inside PENDING/WAITING status: " + execStatus);

                                fabric().execute("startjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + taskExecutionID + "_" + taskRefTableID + "' " + affinity + args + ";");
                                break;
                            case STOPPED:
                                //log.info("fnTdmCopyReference -inside STOPPED status: " + execStatus);
                                fabric().execute("stopjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "';");
                                break;
                            case RESUME:
                                //log.info("fnTdmCopyReference -inside RESUME status: " + execStatus);
                                //log.info("fnTdmCopyReference - UID: " + uid + ", ARGS: " + args + ", affinity: " + affinity);
                                if (Util.isEmpty(uid)) {
                                    //log.info("fnTdmCopyReference - Creating UID");
                                    uid = taskExecutionID + "_" + taskRefTableID;
                                    fabric().execute("startjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "' " + affinity + args + ";");
                                } else {
                                    //log.info("fnTdmCopyReference - Resuming UID");
										/*db(TDM).execute("update " + TASK_EXECUTION_LIST + " set execution_status = ? " +
											"where task_execution_id = ? and task_id = ? and lu_id = ?; ",
											RUNNING, taskExecutionID, taskID,luID);*/
                                    fabric().execute("resumejob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "'" + ";");
                                    //log.info("------------------------------------- After resume case ------------------------------------");
                                }
                                break;
                        }
                    } catch (Exception e) {
                        if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                            e.printStackTrace();
                            throw e;
                        }
                        //if (++count >= retries) {
                        db(TDM).execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
                                        "where task_execution_id = ? and task_ref_table_id = ?; ",
                                FAILED, e.getMessage(), TDM_COPY_REFERENCE, taskExecutionID, taskRefTableID);
                        fabric().execute("stopjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "';");
                        //retries = 0;
                        //break;
                        //} else {
                        //log.info(e.getMessage() + "; this is retry number " + count);
                        //Thread.sleep(5000);
                        //}
                    }
                    //}
                } else {// In case of Load Task
                    //	while(!Thread.currentThread().isInterrupted()) {
                    //log.info("fnTdmCopyReference - Handling Load Task");
                    try {
                        switch (execStatus) {
                            case WAITING:
                            case PENDING:
                                String selectedRefVersionTaskName = "''";
                                String selectedRefVersionDateTime = "19700101000000";
                                String selectedRefVersionTaskExeId = "''";
                                if (versionInd.equals("true")) {
                                    selectedRefVersionTaskName = "" + taskParams.get("selected_ref_version_task_name");
                                    selectedRefVersionDateTime = "" + taskParams.get("selected_ref_version_datetime");
                                    selectedRefVersionTaskExeId = "" + taskParams.get("selected_ref_version_task_exe_id");
                                }
                                String loadRefCmd = "BATCH " + luName + ".('" + taskExecutionID + "_" + refTableName + "') " +
                                        "fabric_command=\"broadway " + luName + ".TDMReferenceLoader " +
                                        "iid=?, taskExecutionID=" + taskExecutionID + ", luName=" + luName + ", refTableName=" + refTableName +
                                        ", sourceEnvName=" + sourceEnv + ", targetEnvName=" + targetEnv +
                                        ", selectedRefVersionTaskName='" + selectedRefVersionTaskName + "', selectedRefVersionDateTime=" +
                                        selectedRefVersionDateTime + ", selectedRefVersionTaskExeId=" + selectedRefVersionTaskExeId + "\" with async='true'";

                                //log.info("fnTdmCopyReference - Running batch command: " + loadRefCmd);
                                String batchID = Util.rte(() -> "" + fabric().fetch(loadRefCmd).firstValue());

                                //log.info("fnTdmCopyReference - Updating batch ID to: " + batchID);
                                db(TDM).execute("update " + TASK_REF_EXE_STATS + " set job_uid= ?, execution_status = ?, " +
                                                "start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', updated_by = ?" +
                                                "where task_execution_id = ? and task_ref_table_id = ?; ",
                                        batchID, RUNNING, "TDMReferenceLoader", taskExecutionID, taskRefTableID);
                                break;
                            case STOPPED:
                                fabric().execute("batch_pause '" + uid + "';");
                                break;
                            case RESUME:
                                fabric().execute("batch_retry '" + uid + "'" + ";");
                                //log.info("------------------------------------- After resume case ------------------------------------");
                                break;
                        }
                    } catch (Exception e) {
                        log.error("Reference Table handling had an exception: " + e);
                        if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                            throw e;
                        }
                        //if (++count >= retries) {
                        db(TDM).execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
                                        "where task_execution_id = ? and task_ref_table_id = ?; ",
                                FAILED, e.getMessage(), TDM_COPY_REFERENCE, taskExecutionID, taskRefTableID);
                        //log.info("uid to cancel: " + uid);
                        if (!"null".equals(uid)) {
                            fabric().execute("cancel batch '" + uid + "';");
                        }
                        //	retries = 0;
                        //break;
                        //} else {
                        //log.info(e.getMessage() + "; this is retry number " + count);
                        //	Thread.sleep(5000);
                        //}
                    }
                }

            }
            //log.info("fnTdmCopyReference - Handling next Ref table #" + refCount);
        }
        //log.info("fnTdmCopyReference - After the loop on ref tables, refCount: " + refCount);
        //In case the task type was REF, but included LUs that do not have reference,or none of their reference were chosen, update the status to COMPLETE
        String lusWithoutRefSql = "select l.task_id, l.task_execution_id, l.lu_id from task_execution_list l, tasks t, tasks_logical_units lu " +
                "where l.task_id = t.task_id and t.selection_method = 'REF' and l.task_id = lu.task_id and l.lu_id = lu.lu_id and " +
                "lower(l.execution_status) in ('pending', 'stopped') and " +
                "not exists (select 1 from task_ref_tables r, task_ref_exe_stats s where r.task_id = lu.task_id and r.lu_name = lu.lu_name " +
                "and r.task_ref_table_id = s.task_ref_table_id and s.task_execution_id = l.task_execution_id " +
                "and lower(s.execution_status) not in ('completed', 'failed'))";

        String updateLuWithoutRefSql = "UPDATE task_execution_list SET execution_status = ?, num_of_processed_entities = 0, " +
                "start_execution_time = current_timestamp at time zone 'utc', " +
                "end_execution_time = current_timestamp at time zone 'utc', num_of_copied_entities = 0, num_of_failed_entities = 0, " +
                "num_of_processed_ref_tables = 0, num_of_copied_ref_tables = 0, num_of_failed_ref_tables = 0 " +
                "WHERE  task_id = ? AND task_execution_id = ? and lu_id = ?";

        Db.Rows lusWithoutRef = db(TDM).fetch(lusWithoutRefSql);

        for (Db.Row luWithoutRef : lusWithoutRef) {
            String taskId = "" + luWithoutRef.get("task_id");
            String taskExecID = "" + luWithoutRef.get("task_execution_id");
            String luId = "" + luWithoutRef.get("lu_id");
            //log.info("tdmCopyReferenceListener - Setting status to completed for LU without reference to be handled. Task Execution ID: " + taskExecID + ", lu id: " + luId);
            db(TDM).execute(updateLuWithoutRefSql, COMPLETED, taskId, taskExecID, luId);
        }

        if (refTabLst != null) {
            refTabLst.close();
        }

        if (lusWithoutRef != null) {
            lusWithoutRef.close();
        }
    }
}
