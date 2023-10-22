package com.k2view.cdbms.usercode.common.TdmSharedUtils;

//import com.google.gson.Gson;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.ResultSetWrapper;
import com.k2view.cdbms.shared.Utils;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.cdbms.utils.K2TimestampWithTimeZone;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.SharedGlobals.TDM_REF_UPD_SIZE;
import static com.k2view.cdbms.usercode.common.TaskExecutionUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TaskValidationsUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetIIDListForMigration;
import static com.k2view.cdbms.usercode.common.TaskValidationsUtils.SharedLogic.fnValidateNumberOfReserveEntities;
import static com.k2view.cdbms.usercode.common.TemplateUtils.SharedLogic.getDBCollection;

import java.sql.*;
import java.math.*;
import java.io.*;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import static java.lang.Math.min;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class SharedLogic {
    private static final Map<String, Integer> PERMISSION_GROUPS = new HashMap() {{
        put("admin", 3);
        put("owner", 2);
        put("tester", 1);
    }};
    private static final String TDM = "TDM";
    private static final String REF = "REF";
    private static final String TASK_EXECUTION_LIST = TDMDB_SCHEMA + ".task_execution_list";
    private static final String TASK_REF_TABLES = TDMDB_SCHEMA + ".TASK_REF_TABLES";
    private static final String PRODUCT_LOGICAL_UNITS = TDMDB_SCHEMA + ".product_logical_units";
    private static final String TASK_REF_EXE_STATS = TDMDB_SCHEMA + ".TASK_REF_EXE_STATS";
    private static final String TASKS_LOGICAL_UNITS = TDMDB_SCHEMA + ".tasks_logical_units";
    private static final String TDM_REFERENCE = "fnTdmReference";
    private static final String PENDING = "pending";
    private static final String RUNNING = "running";
    private static final String WAITING = "waiting";
    private static final String STOPPED = "stopped";
    private static final String RESUME = "resume";
    private static final String FAILED = "failed";
    private static final String COMPLETED = "completed";
    private static final String PARENTS_SQL = "SELECT lu_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE be_id=? AND lu_parent_id is null";
    private static final String GET_CHILDREN_SQL = "WITH RECURSIVE children AS ( " +
            "SELECT lu_name,lu_id,lu_parent_id,lu_parent_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE lu_name=? and be_id=? " +
            "UNION ALL SELECT a.lu_name, a.lu_id, a.lu_parent_id,a.lu_parent_name " +
            "FROM " + TDMDB_SCHEMA + ".product_logical_units a " +
            "INNER JOIN children b ON a.lu_parent_id = b.lu_id) " +
            "SELECT  string_agg('''' ||  unnest || '''' , ',') FROM children ,unnest(string_to_array(children.lu_name, ',')); ";

    public static void setBroadwayActorFlags(String key, String value) throws SQLException {
        //TDM 7.1 - Masking and Sequence Broadway Actors have special flags to enable/disable them, and they need
        // to be set based on the input globals of the task

        // Masking Actor - If MASKING_FLAG is set to false set the indicator of masking actor to false to suppress the masking in
        // both Load and Extract tasks. If MASKING_FLAG is not set or set to true then nothing to do as the Masking is enabled by default.
        if (key.contains("MASKING_FLAG") && "false".equals(value)) {
            //log.info("setBroadawayActorFlags - Disabling Masking");
            fabric().execute("set enable_masking = false");
        }
        // Sequence Actor - If TDM_REPLACE_SEQUENCES is set, used its value to disable/enable the sequence actor
        if (key.contains("TDM_REPLACE_SEQUENCES")) {
            //log.info("setBroadawayActorFlags - Setting Sequence Actor to: " + value);
            fabric().execute("set enable_sequences = ?", value);
        }
    }

    public static Object fnBatchStatistics(String i_batchId, String i_runMode) throws Exception {
        Object response;
        switch (i_runMode) {
            case "S":
                response = getFabricResponse("batch_summary '" + i_batchId + "'");
                break;
            case "D":
                response = getFabricResponse("batch_details '" + i_batchId + "'");
                break;
            case "H":
                Map<String, String> migHeader = new LinkedHashMap<>();
                fabric().fetch("batch_info ?", i_batchId).forEach(row -> {
                    if ("Batch command".equalsIgnoreCase(row.get("key").toString())) {
                        migHeader.put("Migration Command", row.get("value").toString());
                    }
                });

                return migHeader;
            default:
                response = new HashMap() {{
                    put("errorCode", "FAILED");
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

	@out(name = "result", type = String.class, desc = "")
	public static String generateListOfMatchingEntitiesQuery(Long beID, String whereStmt, String sourceEnv) throws Exception {
				
		//UserCode.log.info("generateListOfMatchingEntitiesQuery - whereStmt: " + whereStmt);
		String rootLUsSql = "SELECT ARRAY_AGG(lu_name) FROM product_logical_units WHERE " +
		    "be_id = ? AND lu_parent_id is null";
		
		String rootLUs = "" + db(TDM).fetch(rootLUsSql, beID).firstValue();
		
		String paramsSql = !Util.isEmpty(whereStmt) ? whereStmt : "";
		paramsSql = paramsSql.replaceAll("FROM " , "FROM " + TDMDB_SCHEMA + ".");
		paramsSql = paramsSql.replaceAll("WHERE ", "WHERE ROOT_LU_NAME = ANY('" + rootLUs + "') AND SOURCE_ENVIRONMENT = '" + sourceEnv + "' AND ");
		paramsSql = "SELECT distinct root_iid as entity_id FROM (" + paramsSql + ") p";
				
		//UserCode.log.info("generateListOfMatchingEntitiesQuery - paramsSql: " + paramsSql);
		return paramsSql;
	}


    private static String createViewSql(String luRelationsView, String setTypesQuery, String getParamsSql) {
        return "DO $$ " +
                "DECLARE " +
                "type_var text; " +
                "BEGIN " +
                "IF NOT EXISTS (SELECT 1 FROM pg_matviews WHERE matviewname =  '" + luRelationsView + "'" +
                ") THEN " + setTypesQuery +
                "CREATE MATERIALIZED VIEW " + TDMDB_SCHEMA + ".\"" + luRelationsView + "\" AS SELECT * FROM  ( " + getParamsSql + " ) AS mergedTables;" +
                "END IF; " +
                "END $$ ";
    }

    private static String getParamsSql(String sourceEnv, String rootLu, String rootLuStrL, String jsonTypeName, String childrenList, Long beID) {

        String str = "IN(" + childrenList.replace("'", "''") + ")";
		// TDM 7.5.1 - Fix the query to support hierarchy with more than 2 levels
        return "WITH RECURSIVE relations AS(" +
            "SELECT lu_type_1, lu_type_2, entity_id as lu_type1_eid, lu_type2_eid, entity_id as root_id," +
            TDMDB_SCHEMA + ".param_values('" + rootLu +"',entity_id,lower(base.lu_type_1) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(base.lu_type_1) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment'),'" + str + "', 'lu_type1_eid', '-1', '" + TDMDB_SCHEMA + "')::text as p1," +
            TDMDB_SCHEMA + ".param_values('" + rootLu + "',entity_id,lower(base.lu_type_2) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(base.lu_type_2) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment')," +
            "'" + str + "', 'lu_type2_eid'" +
            ",base.lu_type_2, '" + TDMDB_SCHEMA + "')::text as p2 " +
            "FROM ( " +
            "SELECT entity_id, '" + rootLu + "'::text as lu_type_1, lu_type_2, lu_type1_eid, lu_type2_eid " +
            "FROM " + TDMDB_SCHEMA + "." + rootLu + "_params " +
            " LEFT JOIN (SELECT * FROM " + TDMDB_SCHEMA + ".tdm_lu_type_relation_eid WHERE lu_type_1= '" + rootLu + "' AND source_env='" + sourceEnv +
            "' AND lu_type_2 IN(" + childrenList + ") AND version_name='' " +
			" AND (EXISTS (SELECT 1 FROM information_schema.columns WHERE " +
            "columns.table_name::name = (lower(tdm_lu_type_relation_eid.lu_type_2::text) || '_params'::text) " +
            "AND columns.column_name::name <> 'entity_id'::name AND columns.column_name::name <> 'source_environment'::name LIMIT 1)) " +
            " AND (EXISTS (SELECT 1 FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE be_id = " + beID + " AND lu_name = lu_type_2 AND lu_parent_name = lu_type_1)) )" +
            " AS rel_base ON " + rootLu + "_params.entity_id=rel_base.lu_type1_eid" +
            " WHERE source_environment='" + sourceEnv + "') AS base " +
            " UNION ALL" +
            " SELECT a.lu_type_1, a.lu_type_2, a.lu_type1_eid, a.lu_type2_eid, root_id," +
            TDMDB_SCHEMA + ".param_values(a.lu_type_1,a.lu_type1_eid,lower(a.lu_type_1) || '_params','" + sourceEnv +"'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(a.lu_type_1) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment'), '" + str + "', 'lu_type1_eid', '-1', '" + TDMDB_SCHEMA + "')::text as p1," +
            TDMDB_SCHEMA + ".param_values(a.lu_type_1,a.lu_type1_eid,lower(a.lu_type_2) || '_params','" + sourceEnv + "'," +
            " (select string_agg('replace(string_agg(\"' || column_name || '\", '',''), ''},{'', '','') as \"' || column_name, '\" , ') || '\"' as coln FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = lower(a.lu_type_2) || '_params' and column_name <> 'entity_id' and column_name <> 'source_environment')," +
            "'" + str + "', 'lu_type2_eid'" +
            ",a.lu_type_2, '" + TDMDB_SCHEMA + "')::text as p2 " +
            "FROM " + TDMDB_SCHEMA + ".tdm_lu_type_relation_eid a " +
            "INNER JOIN relations b ON b.lu_type2_eid = a.lu_type1_eid  AND b.lu_type_2 = a.lu_type_1 AND source_env='" + sourceEnv +
            "' AND a.lu_type_2 IN("+ childrenList +")  AND b.lu_type_2 IN(" + childrenList + ") AND a.lu_type_1 IN(" + childrenList +
            ") AND b.lu_type_1 IN("+ childrenList +") " +
             " AND (EXISTS (SELECT 1 FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE be_id = " + beID + " AND lu_name = a.lu_type_2 AND lu_parent_name = a.lu_type_1)) " +
            " AND (EXISTS (SELECT 1 FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE be_id = " + beID + " AND lu_name = b.lu_type_2 AND lu_parent_name = b.lu_type_1)) ) " +
			" SELECT * FROM ( " +
            " SELECT root_id AS " + rootLuStrL + "_root_id, (json_populate_recordset(null::" + TDMDB_SCHEMA + ".\"" + jsonTypeName +"\", json_agg(path))).*" +
            " FROM (" +
            " SELECT root_id, " + TDMDB_SCHEMA + ".json_append((replace(string_agg(p1, ', '), '}, {', ','))::json, (replace(string_agg(p2, ', '), '}, {', ','))::json, '{}') AS path " + 
			" from relations GROUP BY root_id) colsMerged GROUP BY root_id) AS final";
    }

    private static String getSetTypesQuery(String sourceEnv, String rootLu, String json_type_name, String childrenList) {
		// TDM 7.5.1 - Change the creation of the TYPE to make it match simpler
		String[] paramTablesArray = childrenList.split(",");
        for (int i = 0; i < paramTablesArray.length; i++) {
            String tableName = paramTablesArray[i].toLowerCase();
            tableName = tableName.substring(0,tableName.length() - 1) + "_params'";
            paramTablesArray[i] = tableName;
        }
		String paramTablesList = String.join(",", paramTablesArray);
        return " " +
                "DROP TYPE IF EXISTS " + TDMDB_SCHEMA + ".\"" + json_type_name + "\";" +
	            "select ' CREATE type " + TDMDB_SCHEMA + ".\"" + json_type_name + "\" AS (' || string_agg(concat('\"' || column_name || '\"', ' TEXT[]'), ',')  || ');' into type_var " +
                "from information_schema.columns where table_name in (" + paramTablesList + ") AND column_name <> 'source_environment' AND column_name <> 'entity_id' ; " +
                "IF type_var IS NOT NULL " +
                "THEN EXECUTE type_var;" +
                "ELSE EXECUTE ' CREATE type " + TDMDB_SCHEMA + ".\"" + json_type_name + "\" AS (" + rootLu + "_dummy text);';" +
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
		        "FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_LIST L, " + TDMDB_SCHEMA + ".TASKS T " +
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
		
		String insertSql = "INSERT INTO " + TDMDB_SCHEMA + ".TASK_EXECUTION_ENTITIES(" +
		        "TASK_EXECUTION_ID, LU_NAME, ENTITY_ID, TARGET_ENTITY_ID, ENV_ID, EXECUTION_STATUS, ID_TYPE, " +
		        "FABRIC_EXECUTION_ID, IID, SOURCE_ENV, ROOT_ENTITY_ID, ROOT_LU_NAME";
		String insertBinding = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
		
		Db.Row taskData = db(TDM).fetch(taskExeListSql, taskExecutionId, luId).firstRow();
		
		
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
		        migrationList = (Map<String, Map>) fnGetIIDListForMigration(fabricExecID, null);
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
                            
                            Map<String, String> rootEntityInfo = fnGetRootEntityId(luId, luName, IID, taskExecutionId, srcEnvName);
                            paramList.add(rootEntityInfo.get("rootEntityId"));
                            paramList.add(rootEntityInfo.get("rootLuName"));
		
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
		                    db(TDM).execute(insertSql, params);
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

                            Map<String, String> rootEntityInfo = fnGetRootEntityId(luId, luName, IID, taskExecutionId, srcEnvName);
                            paramList.add(rootEntityInfo.get("rootEntityId"));
                            paramList.add(rootEntityInfo.get("rootLuName"));
		
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
		                    db(TDM).execute(insertSql, params);
		                }
		
		            }
		        }
		    }
		
		    //Add reference Entities to TASK_EXECUTION_ENTITIES table
		    String refListSql = "SELECT REF_TABLE_NAME, EXECUTION_STATUS FROM " + TDMDB_SCHEMA + ".TASK_REF_EXE_STATS ES WHERE " +
		            "TASK_EXECUTION_ID = ? AND TASK_REF_TABLE_ID IN (SELECT TASK_REF_TABLE_ID FROM " + TDMDB_SCHEMA + ".TASK_REF_TABLES RT " +
		            "WHERE RT.TASK_ID = ES.TASK_ID AND RT.TASK_REF_TABLE_ID = ES.TASK_REF_TABLE_ID AND RT.LU_NAME = ?)";
		
		    idType = "REFERENCE";
		
		    Db.Rows refList = db(TDM).fetch(refListSql, taskExecutionId, luName);
		
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

                paramList.add("REF");
                paramList.add(luName);
		
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
		
		        db(TDM).execute(insertSql, params);
		    }
		
		    if (refList != null) {
		        refList.close();
		    }
		}
	}
	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,String> fnGetRootEntityId(Long luId, String luName, String iid, String taskExecId, String sourceEnv) throws Exception {
		Map<String, String> rootEntityInfo = new HashMap<>();
		
		String rootEntityId = iid;
		String rootLuName = luName;
		String parentLuSql = "SELECT u.lu_name as parent_lu_name " +
		        "FROM " + TDMDB_SCHEMA + ".task_execution_list l, " + TDMDB_SCHEMA + ".tasks_logical_units u " +
		        "WHERE l.task_execution_id = ? AND l.lu_id = ? " +
		        "AND l.task_id = u.task_id AND l.parent_lu_id = u.lu_id";
		
		String rootEntityIdSql = "SELECT e.root_lu_name, e.root_entity_id " +
		        "FROM " + TDMDB_SCHEMA + ".task_execution_entities e, " + TDMDB_SCHEMA + ".tdm_lu_type_relation_eid t " +
		        "WHERE e.task_execution_id = ? AND e.lu_name = ? " +
		        "AND e.iid = t.lu_type1_eid AND t.lu_type_1 = ? " +
		        "AND t.lu_type_2 =  ? AND t.lu_type2_eid = ? " +
		        "AND t.source_env = ?";
		
		Object parenLUName = db(TDM).fetch(parentLuSql, taskExecId, luId).firstValue();
		
		if (parenLUName != null) {
		    Db.Row row = db(TDM).fetch(rootEntityIdSql,
		        taskExecId, parenLUName.toString(),
		        parenLUName.toString(), luName, iid, sourceEnv).firstRow();
		
		    rootLuName = "" + row.get("root_lu_name");
		    rootEntityId = "" + row.get("root_entity_id");
		}
		
		rootEntityInfo.put("rootLuName", rootLuName);
		rootEntityInfo.put("rootEntityId", rootEntityId);
		return rootEntityInfo;
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

	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,Object> fnGetRetentionPeriod() throws Exception {
		Map<String, Object> map;
		try {
			String sql = "select * from " + TDMDB_SCHEMA + ".tdm_general_parameters where tdm_general_parameters.param_name = 'tdm_gui_params'";

			Object params = db(TDM).fetch(sql).firstRow().get("param_value");
			Map result = Json.get().fromJson((String) params, Map.class);

			map = new HashMap<>();

			Object retentionDefaultPeriod = result.get("retentionDefaultPeriod");
			if (retentionDefaultPeriod != null) {
				map.put("retentionDefaultPeriod", retentionDefaultPeriod);
			}
			Object retentionPeriodTypes = result.get("retentionPeriodTypes");
			if (retentionPeriodTypes != null) {
				map.put("retentionPeriodTypes", retentionPeriodTypes);
			}
			Object reserveDefaultPeriod = result.get("reservationDefaultPeriod");
			if (reserveDefaultPeriod != null) {
				map.put("reservationDefaultPeriod", reserveDefaultPeriod);
			}
			Object reservationPeriodTypes = result.get("reservationPeriodTypes");
			if (reservationPeriodTypes != null) {
				map.put("reservationPeriodTypes", reservationPeriodTypes);
			}
			Object versioningRetentionPeriod = result.get("versioningRetentionPeriod");
			if (versioningRetentionPeriod != null) {
				map.put("versioningRetentionPeriod", versioningRetentionPeriod);
			}
			Object versioningRetentionPeriodForTesters = result.get("versioningRetentionPeriodForTesters");
			if (versioningRetentionPeriodForTesters != null) {
				map.put("versioningRetentionPeriodForTesters", versioningRetentionPeriodForTesters);
			}
			sql = "SELECT param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'MAX_RETENTION_DAYS_FOR_TESTER'";
			Object maxRetentionDays = db(TDM).fetch(sql).firstValue();
			if (maxRetentionDays != null) {
				Map<String, Object> testers = new HashMap<>();
				testers.put("units", "Days");
				testers.put("value",  Long.valueOf((String) maxRetentionDays));
				map.put("maxRetentionPeriodForTesters", testers);
			}
			sql = "SELECT param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'MAX_RESERVATION_DAYS_FOR_TESTER'";
			Object maxReserveDays = db(TDM).fetch(sql).firstValue();
			if (maxReserveDays != null) {
				Map<String, Object> testers = new HashMap<>();
				testers.put("units", "Days");
				testers.put("value",  Long.valueOf((String) maxReserveDays));
				map.put("maxReservationPeriodForTesters", testers);
			}
			return map;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to get retention Period Definitions from tdm_general_parameters TDMDB");
		}
	}
	
	public static String fnGetUserPermissionGroup(String userName) {
		try {
   
			String fabricRoles = "";
			if (userName == null || "".equals(userName) || userName.equalsIgnoreCase(sessionUser().name())) {
				fabricRoles = String.join(",", sessionUser().roles());
										
			} else {
				final String user = userName;
				List<String> roles=new ArrayList<>();
				fabric().fetch("list users;").
				forEach(r -> {
					if(user.equals(r.get("user"))) {
						roles.addAll(Arrays.asList(((String) r.get("roles")).split(",")));
					}
				});
				fabricRoles = String.join(",", roles);
			}
            return fnGetPermissionGroupByRoles(fabricRoles);
    } catch (Throwable t) {
			throw new RuntimeException( t.getMessage());
		}
	}
	
	//checks if the registered user is an owner of the given environment
    public static Boolean fnIsOwner(String envId) throws Exception {
        List<Map<String, Object>> envsList = fnGetUserEnvs("");
        boolean found = false;
        for (Map<String, Object> envsGroup : envsList) {
            Map.Entry<String, Object> entry = envsGroup.entrySet().iterator().next();
            List<Map<String, Object>> groupByType = (List<Map<String, Object>>) entry.getValue();
            for (Map<String, Object> env : groupByType) {
                if ("owner".equals(env.get("role_id").toString())) {
                    if (env.get("environment_id").toString().equals(envId)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        return found;
    }

	//checks if the registered user is an owner of the given environment or admin
    public static Boolean fnIsAdminOrOwner(String envId, String userName) throws Exception {
		String permissionGroup = fnGetUserPermissionGroup(userName);
		if("admin".equalsIgnoreCase(permissionGroup)) {
			return true;
		}
        List<Map<String, Object>> envsList = fnGetListOfEnvsByUser(userName);
        boolean found = false;
        for (Map<String, Object> envsGroup : envsList) {
            Map.Entry<String, Object> entry = envsGroup.entrySet().iterator().next();
            List<Map<String, Object>> groupByType = (List<Map<String, Object>>) entry.getValue();
            for (Map<String, Object> env : groupByType) {
                if ("owner".equals(env.get("role_id").toString())) {
                    if (env.get("environment_id").toString().equals(envId)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        return found;
    }
    public static Boolean fnIsAdminByRole(String role) throws Exception {
        String permissionGroup = fnGetPermissionGroupByRoles(role);
        return "admin".equalsIgnoreCase(permissionGroup);
    }

	public static List<Map<String, Object>> fnGetListOfEnvsByUser(String userName) {
		List<Map<String, Object>> rowsList = new ArrayList<>();

		//Check the permission group of the user.
		//If the permission group is Admin => select all the active environments
		String permissionGroup = fnGetUserPermissionGroup(userName);
		if ("admin".equalsIgnoreCase(permissionGroup)){
			String allEnvs = "Select env.environment_id,env.environment_name,\n" +
				"  Case When env.allow_read = True And env.allow_write = True Then 'BOTH'\n" +
				"    When env.allow_write = True Then 'TARGET' Else 'SOURCE'\n" +
				"  End As environment_type,\n" +
				"  'admin' As role_id,\n" +
				"  'admin' As assignment_type\n" +
				"From " + TDMDB_SCHEMA + ".environments env\n" +
				"Where env.environment_status = 'Active'";
			Db.Rows rows = Util.rte(() -> db(TDM).fetch(allEnvs));
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					Util.rte(() -> rowMap.put(columnName, resultSet.getObject(columnName)));
				}
			rowsList.add(rowMap);
			}
			if (rows != null) {
				rows.close();
			}

		} else {
			Util.rte(() -> rowsList.addAll(fnGetEnvsByUser(userName)));
		}

		List<Map<String, Object>> result = new ArrayList<>();
		List<Map<String, Object>> sourceEnvs = new ArrayList<>();
		List<Map<String, Object>> targetEnvs = new ArrayList<>();

		for(Map<String, Object> row:rowsList){
			Map<String, Object> envData=new HashMap<>();
			envData.put("environment_id",row.get("environment_id"));
			envData.put("environment_name",row.get("environment_name"));
			envData.put("role_id",row.get("role_id"));
			envData.put("assignment_type",row.get("assignment_type"));

			if("SOURCE".equals(row.get("environment_type"))||"BOTH".equals(row.get("environment_type"))){
				sourceEnvs.add(envData);
			}
			if("TARGET".equals(row.get("environment_type"))||"BOTH".equals(row.get("environment_type"))){
				targetEnvs.add(envData);
			}
		}

		Map<String, Object> sourceEnvsMap=new HashMap<>();
		sourceEnvsMap.put("source environments",sourceEnvs);
		result.add(sourceEnvsMap);
		Map<String, Object> targetEnvsMap=new HashMap<>();
		targetEnvsMap.put("target environments",targetEnvs);
		result.add(targetEnvsMap);

		return result;
	}
    public static String fnGetPermissionGroupByRoles(String roles) throws SQLException {
        Integer[] weight = {0};
        weight[0]= fnGetPermissionGroupWeight(roles);
        if (weight[0] == 0) {
            UserCode.log.error("Can't find permission group for the user");
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
    public static Integer fnGetPermissionGroupWeight(String roles) throws SQLException {
        Integer[] weight = {0};
        String sql = "select permission_group from " + TDMDB_SCHEMA + ".permission_groups_mapping where fabric_role = ANY (string_to_array(?, ','))";
        Util.rte(() -> db(TDM).fetch(sql, roles).forEach(row -> {
            Integer nextWeight = PERMISSION_GROUPS.get(row.get("permission_group"));
            if (nextWeight != null && nextWeight > weight[0]) {
                weight[0] = nextWeight;
            }
        }));
        return weight[0];
    }

    public static List<Map<String, Object>> fnGetEnvsByUser(String userName) throws Exception {
        List<Map<String, Object>> rowsList = new ArrayList<>();
		List<String> roles=new ArrayList<>();
        String fabricRoles="";
        try{
            if(userName==null||"".equals(userName)||userName.equalsIgnoreCase(sessionUser().name())){
                fabricRoles=String.join(",",sessionUser().roles());
        }else {
                fabric().fetch("list users;").
                        forEach(r -> {
                            if (userName.equals(r.get("user"))) {
                                roles.addAll(Arrays.asList(((String) r.get("roles")).split(",")));
                            }
                        });
                fabricRoles = String.join(",", roles);
            }
        } catch(Throwable t) {
            throw new RuntimeException(t.getMessage());
        }

        //get the environments where the user is the owner
        String query1 = "select *, " +
                "CASE when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, 'owner' as role_id, 'owner' as assignment_type " +
                "from " + TDMDB_SCHEMA + ".environments env, " + TDMDB_SCHEMA + ".environment_owners o " +
                "where env.environment_id = o.environment_id " +
                "and (o.user_id = (?) or o.user_id = ANY(string_to_array(?, ',')))" +
                "and env.environment_status = 'Active'";

        //log.info("fnGetEnvsByuser - query 1 for user Name " + userName + "is: " + query1);
        Db.Rows rows = db(TDM).fetch(query1, userName, fabricRoles);

        List<String> columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            rowsList.add(rowMap);
        }

        String envIds = "(";
        if (!rowsList.isEmpty()) {
            for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
            envIds = envIds.substring(0, envIds.length() - 1);
        }
        envIds += ")";

        //get the environments where the user is assigned to a role by their username
        String query2 = "select *, " +
                "CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
                "from " + TDMDB_SCHEMA + ".environments env, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
                "where env.environment_id = r.environment_id " +
                "and lower(r.role_status) = 'active' " +
                "and r.role_id = u.role_id " +
                "and u.user_id = (?) " +
				"and u.user_type = 'ID' " +
                "and env.environment_status = 'Active'";
        // remove the list of environments returned by query 1;
        query2 += "()".equals(envIds) ? "" : "and env.environment_id not in " + envIds;
        rows = db(TDM).fetch(query2, userName);

        //log.info("fnGetEnvsByuser - query 2 for user Name " + userName + "is: " + query2);

        columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            rowsList.add(rowMap);
        }

        envIds = "(";
        if (!rowsList.isEmpty()) {
            for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
            envIds = envIds.substring(0, envIds.length() - 1);
        }
        envIds += ")";

        //get the environments where the user id is one of the Fabric Roles
        String query3 = "select *, " +
                "CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
                "from " + TDMDB_SCHEMA + ".environments env, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
                "where env.environment_id = r.environment_id " +
                "and lower(r.role_status) = 'active' " +
                "and r.role_id = u.role_id " +
                "and u.user_id = ANY(string_to_array(?, ',')) " +
				"and u.user_type = 'GROUP' " +
                "and env.environment_status = 'Active'";
        // remove the list of environments returned by query 1+2;
        query3 += "()".equals(envIds) ? "" : "and env.environment_id not in " + envIds;
        rows = db(TDM).fetch(query3, fabricRoles);

        //log.info("fnGetEnvsByuser - query 3 for Fabric Roles < " + fabricRoles + "> is: " + query3);

        columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            rowsList.add(rowMap);
        }

        envIds = "(";
        if (!rowsList.isEmpty()) {
            for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
            envIds = envIds.substring(0, envIds.length() - 1);
        }
        envIds += ")";

        //get the environments where the user is assigned to a role by 'ALL' assignment
        String query4 = "select *, " +
                "CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type " +
                ", r.role_id, 'all' as assignment_type " +
                "from " + TDMDB_SCHEMA + ".environments env, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
                "where env.environment_id = r.environment_id " +
                "and lower(r.role_status) = 'active' " +
                "and r.role_id = u.role_id " +
                "and lower(u.username) = 'all' " +
                "and env.environment_status = 'Active'";
        // remove the list of environments returned by queries 1+2+3;
        query4 += "()".equals(envIds) ? "" : "and env.environment_id not in " + envIds;
        rows = db(TDM).fetch(query4);

        //log.info(" fnGetEnvsByuser - query 4 (get ALL roles) is: " + query4);

        columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            rowsList.add(rowMap);
        }
		if (rows != null) {
			rows.close();
		}
        return rowsList;
    }

	//TDM 7.2 - This function gets the Override Attributes supplied when the task was executed.
	public static Map<String, Object> fnGetTaskExecOverrideAttrs(Long taskId, Long taskExecutionId) {
		
		Map<String, Object> overrideAttrubtes = new HashMap<>();
		String sql = "SELECT override_parameters FROM " + TDMDB_SCHEMA + ".task_execution_override_attrs WHERE task_id = ? and task_execution_id = ?";
		//log.info("getTaskExecOverrideAttrs - Starting");
		try {
			Object overrideAttrVal = db(TDM).fetch(sql, taskId, taskExecutionId).firstValue();
			String overrideAttrStr = overrideAttrVal != null ?  overrideAttrVal.toString() : "";
			//log.info("getTaskExecOverrideAttrs - overrideAttrStr: " + overrideAttrStr);
			if (!"".equals(overrideAttrStr)) {
				// Replace gson with K2view Json
				//Gson gson = new Gson();
				//overrideAttrubtes = gson.fromJson(overrideAttrStr, mapType);
				overrideAttrubtes = Json.get().fromJson(overrideAttrStr);
			}
		} catch (SQLException e) {
			UserCode.log.error("Failed to get override attributes for task_execution_id: " + taskExecutionId);
			return null;
		}
		
		//log.info("getTaskExecOverrideAttrs - overrideAttrubtes: " + overrideAttrubtes);
		return overrideAttrubtes;
	}
	
	static public List<Map<String, Object>> fnGetUserEnvs(String userName) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;
		List<Map<String, Object>> rowsList = new ArrayList<>();
		String userId = sessionUser().name();
		//log.info("wsGetListOfEnvsByUser after defining userId");
		//Check the permission group of the user.
		//If the permission group is Admin => select all the active environments
		String permissionGroup = fnGetUserPermissionGroup(userName);
		//log.info("wsGetListOfEnvsByUser after calling wsGetUserPermissionGroup");
		if ("admin".equalsIgnoreCase(permissionGroup)){
			String allEnvs = "Select env.environment_id,env.environment_name,\n" +
					"  Case When env.allow_read = True And env.allow_write = True Then 'BOTH'\n" +
					"    When env.allow_write = True Then 'TARGET' Else 'SOURCE'\n" +
					"  End As environment_type,\n" +
					"  'admin' As role_id,\n" +
					"  'admin' As assignment_type\n" +
					"From " + TDMDB_SCHEMA + ".environments env\n" +
					"Where env.environment_status = 'Active'";
			Db.Rows rows= db(TDM).fetch(allEnvs);
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				rowsList.add(rowMap);
			}
			
			if (rows != null) {
				rows.close();
			}
	
		} else {
			rowsList.addAll(fnGetEnvsByUser(userId));
		}
	
		List<Map<String, Object>> result = new ArrayList<>();
		List<Map<String, Object>> sourceEnvs = new ArrayList<>();
		List<Map<String, Object>> targetEnvs = new ArrayList<>();
		//log.info("wsGetListOfEnvsByUser - rowsList: " + rowsList);
		for(Map<String, Object> row:rowsList){
			Map<String, Object> envData=new HashMap<>();
			envData.put("environment_id",row.get("environment_id"));
			envData.put("environment_name",row.get("environment_name"));
			envData.put("role_id",row.get("role_id"));
			envData.put("assignment_type",row.get("assignment_type"));
	
			if("SOURCE".equals(row.get("environment_type"))||"BOTH".equals(row.get("environment_type"))){
				sourceEnvs.add(envData);
			}
			if("TARGET".equals(row.get("environment_type"))||"BOTH".equals(row.get("environment_type"))){
				targetEnvs.add(envData);
			}
		}
	
		Map<String, Object> sourceEnvsMap=new HashMap<>();
		sourceEnvsMap.put("source environments",sourceEnvs);
		result.add(sourceEnvsMap);
		Map<String, Object> targetEnvsMap=new HashMap<>();
		targetEnvsMap.put("target environments",targetEnvs);
		result.add(targetEnvsMap);

		return result;
	}

	@out(name = "result", type = JSONArray.class, desc = "")
	public static JSONArray createJsonArrayFromTableRecords(String taskExecID, String tableName, ResultSet refTableRS, int colsCount, int recordsCount) throws SQLException {
        JSONArray tableRecords = new JSONArray();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        int processedCounter = 0;
        int updateStatsSize = Integer.parseInt(TDM_REF_UPD_SIZE);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean failed = new AtomicBoolean(false);
        fabric().execute("set TDM_TASK_EXE_ID = " + taskExecID);
        int tdm_rec_id = 1;
        while (refTableRS.next()) {
            if (failed.get()) {
                throw new RuntimeException("failed to insert... ");
            }

            try {
                // build a JSON object with the record's columns + values
                String JSONObject;
                Map<String, Object> dataRec = new LinkedHashMap<String, Object>();

                for (int j = 1; j <= colsCount; j++) {
                    //log.info("type: " + refTableRS.getMetaData().getColumnTypeName(j).toLowerCase());
                    Object fieldVal =  typeCheck(refTableRS.getObject(j));
                    String colName = refTableRS.getMetaData().getColumnName(j);
                    dataRec.put(colName, fieldVal);
                }

                JSONObject = Json.get(Json.Feature.SERIALIZE_NULLS).toJson(dataRec);
                tableRecords.put(JSONObject);

            }
            catch(Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            processedCounter++;
            if(processedCounter%updateStatsSize == 0) {
                db(TDM).execute("Update " + TASK_REF_EXE_STATS + " set number_of_processed_records = ?, number_of_records_to_process = ? " +
                       "where task_execution_id = ? and trim(lower(ref_table_name)) = ?; ", processedCounter, recordsCount, taskExecID, tableName.toLowerCase());
            }
        }

        db(TDM).execute("Update " + TASK_REF_EXE_STATS + " set number_of_processed_records = ?, number_of_records_to_process = ? " +
                "where task_execution_id = ? and trim(lower(ref_table_name)) = ?; ", processedCounter, recordsCount, taskExecID, tableName.toLowerCase());
        return tableRecords;
    }

    private static boolean isCommonlyUsedType(Object val) {
        // Cover all cases of Integer, Decimal, Double, Float etc...
        if (val instanceof Number) {
            return true;
        }
        if (val instanceof String) {
            return true;
        }
        return false;
    }

    public static Object typeCheck(Object val) throws Exception {

        try {

            if (val instanceof Utils.NullType || val == null) {
                return null;
            }

            // Check first for the commonly used types to save all other 'instanceof'.
            if (isCommonlyUsedType(val)) {
                Class cls = val.getClass();
                //log.info("val instanceof " + cls.getName());
                if (val instanceof java.math.BigDecimal){
                    return  ((BigDecimal) val).doubleValue();
                }else{
                    return val;
                }
            }

            if (val instanceof K2TimestampWithTimeZone) {
                //log.info("val " + val + "instanceof K2TimestampWithTimeZone ");
                //return ((Date) val).getTime();
				/*return StringUtils.isBlank(TypeConversion.DATETIME_WITH_TIMEZONE_FORMAT) ? val.toString()
						:((K2TimestampWithTimeZone) val).formatWithTZ(TypeConversion.DATETIME_WITH_TIMEZONE_FORMAT);*/
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Timestamp) {
                //return ((Date) val).getTime();
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Date) {
                //return ((Date) val).getTime();
				/*return StringUtils.isBlank(DATE_FORMAT) ? val.toString()
						: new SimpleDateFormat(DATE_FORMAT).format((java.sql.Date) val);*/
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Time) {
                //return ((Date) val).getTime();
				/*return StringUtils.isBlank(TIME_FORMAT) ? val.toString()
						: new SimpleDateFormat(TIME_FORMAT).format((java.sql.Time) val);*/
                return ((Date) val).toString();
            }

            if (val instanceof java.util.Date) {
                //log.info("val " + val + " instanceof java.util.Date");
                //return ((Date) val).getTime();
				/*return StringUtils.isBlank(DATETIME_FORMAT) ? val.toString()
						: new SimpleDateFormat(DATETIME_FORMAT).format((java.util.Date) val);*/
                return ((Date) val).toString();
            }

            if (val instanceof Blob) {
                //return ((Blob) val).getBytes(1, (int) ((Blob) val).length());
                return ((ByteBuffer)val).array();
            }

            if (val instanceof Clob) {
                return Utils.clobToString(((Clob) val));
            }

            if (val instanceof ByteBuffer){
                return ((ByteBuffer)val).array();
            }

            return val;
        } catch (Exception e) {
            UserCode.log.warn(e);
            return null;
        }
    }

	@desc("Get Resource File of LU")
	@out(name = "result", type = Object.class, desc = "")
	public static Object loadFromLUResource(String path) throws Exception {
		return loadResource(path);
	}
    @out(name = "result", type = Map.class, desc = "")
    public static Map<String,Object> getDbTableColumnsAndTypes(String dbInterfaceName, String catalogSchema, String table) throws Exception {
        ResultSet rs = null;
        ResultSet rs1 = null;
        String[] types = {"TABLE"};
        String targetTableName = table;
        Map<String, Object> tableData = new LinkedHashMap<String, Object>();

        try {
            DatabaseMetaData md = getConnection(dbInterfaceName).getMetaData();
            String[] dbSchemaType = getDBCollection(md, catalogSchema);
            String catalog = dbSchemaType[0];
            String schema = dbSchemaType[1];
            rs = md.getTables(catalog, schema, "%", types);
            while (rs.next()) {
                if (table.equalsIgnoreCase(rs.getString(3))) {
                    targetTableName = rs.getString(3);
                    break;
                }
            }
            rs1 = md.getColumns(catalog, schema, targetTableName, null);
            while (rs1.next()){
                tableData.put(rs1.getString("COLUMN_NAME"), rs1.getString("TYPE_NAME"));
            }
            return tableData;

        } finally {
            if (rs != null)
                rs.close();
            if (rs1 != null)
                rs1.close();
        }
    }


	@out(name = "result", type = Object.class, desc = "")
	public static Object fnStartTask(Long taskId, Boolean forced, String entitieslist, String sourceEnvironmentName, String targetEnvironmentName, Map<String,String> taskGlobals, Integer numberOfEntities, Long dataVersionExecId, Map<String,String> dataVersionRetentionPeriod, Boolean reserveInd, Map<String,String> reserveRetention, String executionNote) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode;
		
		boolean sourceEnvValidation = false;
		boolean targetEnvValidation = false;
		boolean srcEnvFound = false;
		boolean trgEnvFound = false;
		
		LUType luType = LUType.getTypeByName("TDM");
				
		List<LudbJobs.LudbJob> jobList = luType.ludbUserJobs;
		String downJobsList = "";
		
		Map<String, String> jobDownError = new HashMap<>();
		
		for (LudbJobs.LudbJob job : jobList) {
			String executionMode = Util.rte(() -> "" + job.executionMode);
			String activeInd = Util.rte(() -> "" + job.active);
			String functionName = Util.rte(() -> "" + job.functionName);
			String uid = Util.rte(() -> "" + job.uid);
			String affinity = Util.rte(() -> "" + job.affinity);
			if ("null".equals(affinity)) {
				affinity = "";
			}
			
			String jobStatus = "";
			if ("true".equalsIgnoreCase(activeInd) && "automatically".equalsIgnoreCase(executionMode)) {
		
				Db.Row jobDetails = fabric().fetch("jobstatus user_job 'TDM." + 
						functionName + "' WITH UID='" + uid + "'").firstRow();
				jobStatus = "" + jobDetails.get("Status");
				//log.info("Job Status: " + jobStatus);
				
				if (!"IN_PROCESS".equalsIgnoreCase(jobStatus) && !"SCHEDULED".equalsIgnoreCase(jobStatus) && !"WAITING".equalsIgnoreCase(jobStatus)) {
					if ("tdmExecuteTask".equalsIgnoreCase(functionName) || "fnCheckMigrateAndUpdateTDMDB".equalsIgnoreCase(functionName)) {
						String errMsg = "" + jobDetails.get("Notes");
						UserCode.log.error("Job " + functionName + " is down, cannot run task. The Error Messge: " + errMsg);
						String jobDownMsg = "Job " + functionName + " is down, cannot run task!";
						jobDownError.put(functionName, jobDownMsg);
						if ("".equals(downJobsList)) {
							downJobsList = functionName;
						} else {
							downJobsList += ", " + functionName;
						}
					} else {
		                      UserCode.log.warn("Job " + functionName + " is down, and it is an automatic job, please check why it is down");
					}
				}
							
			}
		}
		if (jobDownError.size() > 0) {
			return wrapWebServiceResults("FAILED", "Mandatory Job(s): " + downJobsList + " Down!", jobDownError);
		}
		
		//Map<String, Object> taskData;
		Db.Rows taskRows;
		try {
			//taskData = ((List<Map<String, Object>>) ((Map<String, Object>) wsGetTasks(taskId.toString())).get("result")).get(0);
			
			taskRows = (fnGetTasks(taskId.toString()));
			
		} catch(Exception e) {
			throw new Exception("Task is not found");
		}
		for (Db.Row taskRow : taskRows) {
			ResultSet taskData = taskRow.resultSet();
			if(!fnIsTaskActive(taskId)) throw new Exception("Task is not active");
			String taskType = "" + taskData.getString("task_type");
		          String createdBy = taskData.getString("task_created_by");
			Boolean deleteBeforeLoad = taskData.getBoolean("delete_before_load");
			Boolean insertToTarget = taskData.getBoolean("load_entity");
			Boolean entityListInd = false;
			Integer entityListSize = 0;
			Boolean versionInd = taskData.getBoolean("version_ind");
			if (entitieslist != null) {
				entityListSize = (entitieslist.split(",")).length;
				entityListInd = true;
			} else if (taskData.getString("selection_param_value") != null && "L".equalsIgnoreCase(taskData.getString("selection_method"))) {
				String[] entityList = ((String) taskData.getString("selection_param_value")).split(",");
				entityListSize = entityList.length;
			}
			//log.info("Entity list is given?: " + entityListInd);
			Map<String,Object> overrideParams=new HashMap<>();
			String selectionMethodOrig = "" + taskData.getString("selection_method");
			String selectionMethod = selectionMethodOrig;
			if (entitieslist!=null) {
				if (selectionMethodOrig.toUpperCase().contains("CLONE")) {
					if (entityListSize > 1) {
						throw new Exception("This is a Data Cloning Task, only one entity can be in the entity list");
					} 
				} else {
					selectionMethod = "L";
				}
			}
			
			if (sourceEnvironmentName!=null) overrideParams.put("SOURCE_ENVIRONMENT_NAME",sourceEnvironmentName);
			if (targetEnvironmentName!=null) overrideParams.put("TARGET_ENVIRONMENT_NAME",targetEnvironmentName);
			if (entitieslist!=null) overrideParams.put("ENTITY_LIST",entitieslist);
			if (!selectionMethod.equals(selectionMethodOrig)) overrideParams.put("SELECTION_METHOD",selectionMethod);
			// If entity_list is given, then ignore the given no_of_entities unless in case of cloning
			if (numberOfEntities!=null && (!entityListInd  || (selectionMethod.toUpperCase().contains("CLONE")))) {
				//log.info("setting the number of entities to: " + numberOfEntities);
				overrideParams.put("NO_OF_ENTITIES",numberOfEntities);
			}
			if (taskGlobals!=null) overrideParams.put("TASK_GLOBALS",taskGlobals);
			
			if(overrideParams.get("ENTITY_LIST")!=null){
				String[] entityList=((String)overrideParams.get("ENTITY_LIST")).split(",");
				Arrays.sort(entityList);
				overrideParams.put("ENTITY_LIST",String.join(",",entityList));
			}
			
			//TDM 7.4 - Support override for reserved entities
			if(reserveInd!=null){
				overrideParams.put("RESERVE_IND", reserveInd);
			}
			else{
				reserveInd = taskData.getBoolean("reserve_ind");
			}
			
			if (!fnValidateParallelExecutions(taskId, overrideParams)) {
				throw new Exception("Task already running");
			}
			
			List<String> taskLogicalUnitsIds=new ArrayList<>();
			
			Db.Rows rows = db(TDM).fetch("SELECT lu_id FROM " + TDMDB_SCHEMA + ".tasks_logical_units WHERE task_id = ?", taskId);
			for (Db.Row row : rows) {
				taskLogicalUnitsIds.add("" + row.get("lu_id"));
			}
			
			if (rows != null) {
				rows.close();
			}
			Map<String,Object> be_lus=new HashMap<>();
			be_lus.put("be_id",taskData.getString("be_id"));
			be_lus.put("LU List",taskLogicalUnitsIds);
			//log.info("selectionMethod: " + selectionMethod);
			//String sourceEnvName = sourceEnvironmentName != null ? sourceEnvironmentName : taskData.getString("source_env_name");
			String sourceEnvName = (sourceEnvironmentName != null && !sourceEnvironmentName .trim().isEmpty()) ? sourceEnvironmentName : taskData.getString("source_env_name");
			//log.info("Source Env: " + sourceEnvName);
			
			if (dataVersionExecId!=null) {
				Map<String, String> validateVersionID = fnValidateVersionExecIdAndGetDetails(dataVersionExecId, be_lus, sourceEnvName);
				if (validateVersionID.get("errorMessage") == null) {
					overrideParams.put("SELECTED_VERSION_TASK_EXE_ID", dataVersionExecId);
					overrideParams.put("SELECTED_VERSION_TASK_NAME", validateVersionID.get("versionName"));
					overrideParams.put("SELECTED_VERSION_DATETIME", validateVersionID.get("versionDatetime"));
				} else {
					return wrapWebServiceResults("FAILED", "versioningtask", validateVersionID.get("errorMessage"));
				}
			}
			Integer numberOfRequestedEntities = 0;
			if (numberOfEntities != null) {
				if (entityListInd && !selectionMethod.toUpperCase().contains("CLONE") && numberOfEntities != entityListSize) {
					numberOfRequestedEntities = entityListSize;
					message = "The number of entities for execution is set based on the entity list";
					overrideParams.put("NO_OF_ENTITIES",numberOfRequestedEntities);
				} else {
					numberOfRequestedEntities = numberOfEntities;
					entityListSize = numberOfEntities;
				}
			} else {
				if (entityListInd && !selectionMethod.toUpperCase().contains("CLONE")) {
					numberOfRequestedEntities = entityListSize;
				} else {
					numberOfRequestedEntities =  (taskData.getInt("num_of_entities"));
				}
			}
			if ("CLONE".equalsIgnoreCase(selectionMethod) && numberOfRequestedEntities > 0) {
				entityListSize = numberOfRequestedEntities;
			}
			
			// 7-Nov-21- fix the validation of the target env. Get it from the task if the target enn is not overridden
			String targetExeEnvName = (targetEnvironmentName != null &&  !targetEnvironmentName .trim().isEmpty())? targetEnvironmentName : taskData.getString("environment_name");
		
			Map<String, String> validateMessages ;
			if (dataVersionRetentionPeriod!=null) {
				validateMessages = fnValidateRetentionPeriodParams(dataVersionRetentionPeriod,
						"retention", targetExeEnvName,versionInd);
				if (validateMessages != null && !validateMessages.isEmpty()) {
					return wrapWebServiceResults("FAILED", "RetentionPeriod", validateMessages.get("retention"));
				}
				overrideParams.put("DATAFLUX_RETENTION_PARAMS",dataVersionRetentionPeriod);
			} else{
                if (!"reserve".equalsIgnoreCase(taskType) && (!deleteBeforeLoad || insertToTarget)) {
			        Map<String, String> dataRetentionPeriod = new HashMap<>();
                    dataRetentionPeriod.put("units", taskData.getString("retention_period_type"));
                    dataRetentionPeriod.put("value", String.valueOf(taskData.getLong("retention_period_value")));
                    validateMessages = fnValidateRetentionPeriodParams(dataRetentionPeriod,
                            "retention", targetExeEnvName, versionInd);
                    if (validateMessages != null && !validateMessages.isEmpty()) {
                        return wrapWebServiceResults("FAILED", "RetentionPeriod", validateMessages.get("retention"));
                    }
                }
			}
			if(reserveInd) {
				if (reserveRetention != null) {
					validateMessages = fnValidateRetentionPeriodParams(reserveRetention,
							"reserve", targetExeEnvName, false);
					if (validateMessages != null && !validateMessages.isEmpty()) {
						return wrapWebServiceResults("FAILED", "ReservationPeriod", validateMessages.get("reservation"));
					}
					overrideParams.put("RESERVE_RETENTION_PARAMS", reserveRetention);
				} else {
					Map<String, String> dataReservePeriod = new HashMap<>();
					dataReservePeriod.put("units", taskData.getString("reserve_retention_period_type"));
					dataReservePeriod.put("value", String.valueOf(taskData.getLong("reserve_retention_period_value")));
					validateMessages = fnValidateRetentionPeriodParams(dataReservePeriod,
							"reserve", targetExeEnvName, false);
					if (validateMessages != null && !validateMessages.isEmpty()) {
						return wrapWebServiceResults("FAILED", "ReservationPeriod", validateMessages.get("reservation"));
					}
				}
			}
			List<Map<String,Object>> sourceRolesList = new ArrayList<>();
			List<Map<String,Object>> targetRolesList = new ArrayList<>();
		          List<Map<String,Object>> rolesList;
		          if ("TDM.tdmTaskScheduler".equalsIgnoreCase(sessionUser().name())) {
		              rolesList = fnGetUserEnvs(createdBy);
		         }else{
		             rolesList = fnGetUserEnvs("");
		         }
			
			//log.info("------- Size: " + rolesList.size());
			for (Map<String, Object> envType : rolesList) {
				if (envType.get("source environments") != null) {
					sourceRolesList = (List<Map<String, Object>>) (envType.get("source environments"));
				}
				if (envType.get("target environments") != null) {
					targetRolesList = (List<Map<String, Object>>) (envType.get("target environments"));
				}
			}
			//log.info("------- Size of sourceRolesList: " + sourceRolesList.size());
			//log.info("------- Size of targetRolesList: " + targetRolesList.size());
			
			List<Map<String, String>> validationsErrorMessagesByRole = new ArrayList<>();
			Long validateReadNumber = -1L;
			Long validateReserveNumber=-1L;
			Long validateWriteNumber=-1L;
			Long validateNumber =-1L;
			String permission = "";
		
			if (!"reserve".equalsIgnoreCase(taskType) && (!deleteBeforeLoad || insertToTarget)) {
				if (sourceRolesList == null || sourceRolesList.isEmpty()) {
					throw new Exception("Environment does not exist or user has no read permission on this environment");
				}
				for (Map<String, Object> role : sourceRolesList) {
					//Check if the current role is related to input environment, and not to other environment
					if (sourceEnvName.equals(role.get("environment_name"))) {
						srcEnvFound = true;
						int allowedEntitySize = getAllowedEntitySize(entityListSize, numberOfRequestedEntities);
						if ("tester".equalsIgnoreCase(fnGetUserPermissionGroup(""))) { // extract || generate
							validateReadNumber = (long) fnValidateNumberOfReadEntities(role.get("role_id").toString(), sourceEnvName);
							permission = "read" ;
						}
						Map<String, String> sourceValidationsErrorMessages = fnValidateSourceEnvForTask(be_lus, taskData.getInt("refcount"),
								selectionMethod,
								taskData.getString("sync_mode"), taskData.getBoolean("version_ind"), taskType, role);
						//log.info("validateNumber: " + validateNumber);
		
						if (validateReadNumber!=-1 && (allowedEntitySize > validateReadNumber)) {
							sourceValidationsErrorMessages.put("Number of entity", "The number of entities exceeds the number of entities in the " + permission + " permission");
						} else if (sourceValidationsErrorMessages.isEmpty()) {
							if ("extract".equalsIgnoreCase(taskType) && (numberOfEntities!=null || entityListInd)) {
								overrideParams.put("NO_OF_ENTITIES",allowedEntitySize);
							}
							sourceEnvValidation = true;
							break;
						}
		
						validationsErrorMessagesByRole.add(sourceValidationsErrorMessages);
					}
				}
			} else {// No Source validation
				sourceEnvValidation = true;
			}
		
			if("load".equalsIgnoreCase(taskType) || "reserve".equalsIgnoreCase(taskType)) {
		
				if(targetRolesList == null || targetRolesList.isEmpty()) {
					throw new Exception("Environment does not exist or user has no write permission on this environment");
				}
		
				for (Map<String, Object> role : targetRolesList) {
					if (targetExeEnvName.equals(role.get("environment_name"))) {
						trgEnvFound = true;
						Map<String, String> targetValidationsErrorMessages=new HashMap<>();
		
						int allowedEntitySize = getAllowedEntitySize(entityListSize, numberOfRequestedEntities);
		
						if ("tester".equalsIgnoreCase(fnGetUserPermissionGroup(""))) {
							validateReserveNumber = (long) fnValidateNumberOfReserveEntities(role.get("role_id").toString(), targetExeEnvName);
							validateWriteNumber = (long) fnValidateNumberOfCopyEntities(role.get("role_id").toString(), targetExeEnvName);
							if ("load".equalsIgnoreCase(taskType)) {
								if (reserveInd!=null && reserveInd) { //load+reserve || load+extract+reserve ||  load+extract+reserve+delete
									Long reserved = fnGetReservedEntitiesNumber("" + role.get("environment_id"), "" + be_lus.get("be_id"),sessionUser().name());
									validateNumber =  min(validateReadNumber,min((validateReserveNumber-reserved),validateWriteNumber));
									permission = "read write reserve";
		
								} else if(!insertToTarget && deleteBeforeLoad) {
									validateNumber=validateWriteNumber;// delete only
									permission="write";
								}else { // load only || load + delete || load + extract || load+extract+delete
									validateNumber=min(validateWriteNumber, validateReadNumber);
									permission="read write";
								}
							}else { //reserve only
								Long reserved = fnGetReservedEntitiesNumber("" + role.get("environment_id"), "" + be_lus.get("be_id"),sessionUser().name());
								validateNumber=validateReserveNumber-reserved;
								permission="reserve";
							}
						}
						targetValidationsErrorMessages = fnValidateTargetEnvForTask(be_lus, taskData.getInt("refcount"),
								selectionMethod,
								taskData.getBoolean("version_ind"),
								taskData.getBoolean("replace_sequences"), taskData.getBoolean("delete_before_load"), taskType,
								reserveInd != null ? reserveInd : taskData.getBoolean("reserve_ind"), allowedEntitySize, role);
						//log.info("targetValidationsErrorMesssages: " + targetValidationsErrorMesssages);
						if (validateNumber != -1 && (allowedEntitySize>validateNumber)) {
							targetValidationsErrorMessages.put("Number of entity", "The number of entities exceeds the number of entities in the "+ permission+ " permission");
						} else if ( targetValidationsErrorMessages.isEmpty()) {
							if (numberOfEntities!=null || entityListInd) {
								overrideParams.put("NO_OF_ENTITIES",allowedEntitySize);
							}
							targetEnvValidation = true;
							break;
						}
						validationsErrorMessagesByRole.add(targetValidationsErrorMessages);
					}
				}
				
			} else{
				//In case of Extract task, there are not target Env validations
				targetEnvValidation = true;
			}
			//log.info("wsStartTask - targetEnvValidation: " + targetEnvValidation + ", sourceEnvValidation: " + sourceEnvValidation);
			if (!sourceEnvValidation && !srcEnvFound) {
				Map<String, String> sourceValidationsErrorMessages=new HashMap<>();
				sourceValidationsErrorMessages.put("SourceEnvironment", "No Source Environment was found For User");
				validationsErrorMessagesByRole.add(sourceValidationsErrorMessages);
			}
			
			if (!targetEnvValidation && !trgEnvFound) {
				Map<String, String> targetValidationsErrorMessages=new HashMap<>();
				targetValidationsErrorMessages.put("TargetEnvironment", "No Target Environment was found For User");
				validationsErrorMessagesByRole.add(targetValidationsErrorMessages);
			}
		
			if (!targetEnvValidation || !sourceEnvValidation) {
				Object error= validationsErrorMessagesByRole.get(validationsErrorMessagesByRole.size()-1);
				return wrapWebServiceResults("FAILED", "validation failure", error);
			}
		
			try {
				String envIdByName_sql= "select environment_id from " + TDMDB_SCHEMA + ".environments where environment_name=(?) and environment_status = 'Active'";
				Long overridenSrcEnvId=(Long)db(TDM).fetch(envIdByName_sql,sourceEnvironmentName).firstValue();
				Long overridenTarEnvId=(Long)db(TDM).fetch(envIdByName_sql,targetEnvironmentName).firstValue();
			
				fnTestTaskInterfaces(taskId,forced,overridenSrcEnvId,overridenTarEnvId);
			
				List<Map<String,Object>> taskExecutions = fnGetActiveTaskForActivation(taskId);
				if (taskExecutions == null || taskExecutions.size() == 0) {
					throw new Exception("Failed to execute Task");
				}
			
				Long taskExecutionId = (Long) fnGetNextTaskExecution(taskId);
				if ((taskExecutions.get(0).get("selection_method") != null && (Long) taskExecutions.get(0).get("refcount") != null) && taskExecutions.get(0).get("selection_method").toString().equals("REF") ||
						(Long) taskExecutions.get(0).get("refcount") > 0) {
					fnSaveRefExeTablestoTask((Long) taskExecutions.get(0).get("task_id"), taskExecutionId);
				}
			
				fnStartTaskExecutions(taskExecutions,taskExecutionId,sourceEnvironmentName!=null?sourceEnvironmentName:null,
						overridenTarEnvId!=null?overridenTarEnvId:null,
						overridenSrcEnvId!=null?overridenSrcEnvId:null,
						executionNote);
			
				if(!overrideParams.isEmpty()){
					try{
						fnSaveTaskOverrideParameters(taskId,overrideParams,taskExecutionId);
					}catch(Exception e){
						throw new Exception ("A problem occurs when trying to save override parameters: " + e.getMessage());
					}
				}
			
				fnCreateSummaryRecord(taskExecutions.get(0), taskExecutionId,sourceEnvironmentName!=null?sourceEnvironmentName:null,
						overridenTarEnvId!=null?overridenTarEnvId:null,
						overridenSrcEnvId!=null?overridenSrcEnvId:null);
			
				try {
					String activityDesc = "Execution list of task " + taskData.getString("task_title");
					fnInsertActivity("update", "Tasks", activityDesc);
				} catch(Exception e){
		                  UserCode.log.error(e.getMessage());
				}
			
			
				Map<String,Object> map=new HashMap<>();
				map.put("taskExecutionId",taskExecutionId);
				response.put("result",map);
				errorCode="SUCCESS";
			} catch(Exception e){
				message=e.getMessage();
		              UserCode.log.error(message);
				errorCode="FAILED";
			}
				
			response.put("errorCode",errorCode);
			response.put("message", message);
			break;
		}
		
		if (taskRows != null) {
			taskRows.close();
		}
		return response;
	}

	@desc("Get the tables of give LU without TDM Tables add to LU for TDM mechanisms")
	@out(name = "result", type = List.class, desc = "")
	public static List<String> getLuTablesList(String luName) throws Exception {
		List<String> tablesList = new ArrayList<>();
		
		LUType luType = LUType.getTypeByName(luName);
		
		for (String tableName : luType.ludbTables.keySet()) {
		    Db.Rows checkTable = fabric().fetch("broadway " + luType.luName + ".filterOutTDMTables tableName='" +
		        tableName + "', luName=" + luType.luName + ", RESULT_STRUCTURE=ROW");
		
		    if (checkTable != null && checkTable.firstValue() != null) {
		        tablesList.add(tableName);
		    }
			
			if (checkTable != null) {
				checkTable.close();
			}
		
		}
		
		
		return tablesList;
	}
    
    @out(name="result", type = Boolean.class, desc = "")
    public static Boolean fnIsJSONValid(String jsonInString) {
        try {
            JSONObject jsonObjOne = new JSONObject(jsonInString);
            return true;
        } catch(Exception ex) { 
            return false;
        }
  }

	public static Long fnGetReservedEntitiesNumber (String envId,String beId,String userId) throws SQLException {
		try {
			String getUserReserveCnt_sql = "select count(1) from " + TDMDB_SCHEMA + ".tdm_reserved_entities where env_id = ? and be_id = ? and reserve_owner = ? and " +
					"end_datetime > CURRENT_TIMESTAMP";
			Long entCount = (Long) UserCode.db(TDM).fetch(getUserReserveCnt_sql, envId, beId, userId).firstValue();
			return entCount;
		}catch (Exception e){
			throw new RuntimeException(e);
		}
	}
}
