/////////////////////////////////////////////////////////////////////////
// Project Shared Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.Json;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.shared.user.WebServiceUserCode.graphit;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.TDMRef.SharedLogic.fnTdmReference;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;


@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class SharedLogic {

    public static final String TDM = "TDM";
    private static final String DB_FABRIC = "fabric";
    public static String schema = TDMDB_SCHEMA;

    public static void fnAddTaskExecutionProcess(List<Map<String, Object>> postExecutionProcesses, Long taskId, String processType) throws Exception {
        String sql = "DELETE FROM " + schema + ".TASKS_EXE_PROCESS WHERE process_type = ? AND task_id = ? ";
        db(TDM).execute(sql,processType,taskId);
        if (postExecutionProcesses == null) return;

        for (Map<String, Object> postExecutionProcess : postExecutionProcesses) {
            String paramsJson = null;
            Map<String, Object> params = postExecutionProcess.get("parameters") == null ? null : (Map<String, Object>)postExecutionProcess.get("parameters");
            if (params != null) {
                paramsJson = Json.get().toJson(params);
            }
            
            db(TDM).execute("INSERT INTO " + schema + ".TASKS_EXE_PROCESS (task_id, process_id,process_name, execution_order,process_type, parameters) VALUES (?,?,?,?,?,?)",
                    taskId,
                    postExecutionProcess.get("process_id"),
                    postExecutionProcess.get("process_name"),
                    postExecutionProcess.get("execution_order"),
                    processType,
                    paramsJson
            );
        }
    }

    public static Object fnUpdateExecutionForBusinessEntity(Long beId,String beName,Long process_id, String process_name, Integer execution_order, String process_description, String process_type) throws Exception {
        HashMap<String,Object> response=new HashMap<>();
        String errorCode="";
        String message=null;

        try {
            String sql= "UPDATE " + schema + ".TDM_BE_EXE_PROCESS " +
                    "SET process_name=(?), execution_order=(?), process_description=(?), process_type=(?)" +
                    "WHERE process_id = ?";
            db(TDM).execute(sql, process_name, execution_order, process_description, process_type, process_id);

            try {
                String activityDesc = process_type.equals("pre") ? "Pre Execution Order " : "Post Execution Order ";
                activityDesc += process_name + " was updated in business entity " + beName;
                fnInsertActivity("update", "Business entities", activityDesc);
            } catch(Exception e){
                log.error(e.getMessage());
            }

            errorCode="SUCCESS";

        } catch(Exception e){
            message=e.getMessage();
            log.error(message);
            errorCode="FAILED";
        }

        response.put("message", message);
        response.put("errorCode", errorCode);
        return response;
    }

    public static Map<String, Object> createProcess(String name, String description, int id, int order) {
        Map<String, Object> process = new HashMap<>();
        process.put("process_name", name);
        process.put("process_description", description);
        process.put("process_id", id);
        process.put("execution_order", order);
        return process;
    }

    public static List<Map<String, Object>> fnAddAIExecutionProcess(List<Map<String, Object>> executionProcesses, String processName1, String processDescription1, int processId1, String processName2, String processDescription2, int processId2) {
        Map<String, Object> process1= new HashMap<>();
        Map<String, Object> process2 = new HashMap<>();

        if(processId1!=0){
            process1 = createProcess(processName1, processDescription1, processId1, processId1);
        }
        if(processId2!=0){
            process2 = createProcess(processName2, processDescription2, processId2, processId2);
        }
        if (executionProcesses == null) {
            executionProcesses = new ArrayList<>();
        }

        boolean process1Exists = false;
        boolean process2Exists = false;

        for (Map<String, Object> existingProcess : executionProcesses) {
            String existingProcessName = (String) existingProcess.get("process_name");
            if (existingProcessName.equals(processName1) && !("".equals(processName2))) {
                process1Exists = true;
            } else if (existingProcessName.equals(processName2) && !("".equals(processName2))) {
                process2Exists = true;
            }
        }

        if (!process1Exists && processId1!=0) {
            executionProcesses.add(process1);
        }
        if (!process2Exists  && processId2!=0) {
            executionProcesses.add(process2);
        }

        return executionProcesses;
    }

    public static Object fnAddExecutionProcessForBusinessEntity(Long beId, String beName, String process_name, Integer execution_order, String process_description, String process_type) throws Exception {
        HashMap<String,Object> response=new HashMap<>();
        String message=null;
        String errorCode="";
        try{
            String sql= "INSERT INTO " + schema + ".TDM_BE_EXE_PROCESS " +
                    "(process_name, be_id, execution_order, process_description, process_type) " +
                    "VALUES (?, ?, ?, ?, ?) RETURNING process_id";
            Db.Row row= db(TDM).fetch(sql, process_name, beId, execution_order, process_description, process_type).firstRow();
            HashMap<String,Object> result=new HashMap<>();
            result.put("id", Long.parseLong(row.get("process_id").toString()));
            try{
                String activityDesc = process_type.equals("pre") ? "Pre Execution Process " : "Post Execution Process ";
                activityDesc += process_name + " was added to business entity " + beName;
                fnInsertActivity("update", "Business entities", activityDesc);
            }catch(Exception e){
                log.error(e.getMessage());
            }
            errorCode="SUCCESS";
            response.put("result",result);
        }catch(Exception e){
            message = e.getMessage();
            log.error(message);
            errorCode="FAILED";
        }

        response.put("message",message);
        response.put("errorCode",errorCode);
        return response;
    }
    public static Object fnDeletePostExecutionForBusinessEntity(Long beId, String beName, Long process_id, String process_name, String process_type)throws Exception{
        HashMap<String,Object> response=new HashMap<>();
        String message=null;
        String errorCode="";

        try{
            String sql= "DELETE FROM " + schema + ".TDM_BE_EXE_PROCESS WHERE process_id = (?) And process_name = (?) And process_type= (?) And be_id =(?) " +
                    " RETURNING process_id";
            db(TDM).execute(sql, process_id, process_name, process_type, beId);

            try{
                String activityDesc = (process_type.equals("post") ? "Post" : "Pre") + " Execution Process " + process_name + " of business entity " + beName + " was deleted";
                fnInsertActivity("update", "Business entities", activityDesc);
            }
            catch(Exception e){
                log.error(e.getMessage());
            }

            try{
                String updateTasksSql= "UPDATE \"public\".tasks " +
                        "SET task_status= (?) " +
                        "FROM ( SELECT " + schema + ".TASKS_EXE_PROCESS.task_id FROM " + schema + ".TASKS_EXE_PROCESS " +
                        "WHERE " + schema + ".TASKS_EXE_PROCESS.process_id = " + process_id + " ) AS TaskPostExec " +
                        "WHERE TaskPostExec.task_id = tasks.task_id ";
                db(TDM).execute(updateTasksSql,"Inactive");
            }
            catch(Exception e){
                log.error(e.getMessage());
            }

            errorCode="SUCCESS";

        } catch(Exception e){
            message=e.getMessage();
            log.error(message);
            errorCode="FAILED";
        }

        response.put("message", message);
        response.put("errorCode", errorCode);
        return response;
    }

    public static Object fnGetTaskExecutionProcesses( Long taskId, String process_type) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".TASKS_EXE_PROCESS  WHERE process_id > 0 AND process_type = '" + process_type +"' AND task_id =" + taskId;
            Db.Rows rows = db(TDM).fetch(sql);
            List<Map<String, Object>> result = new ArrayList<>();
            List<String> columnNames = rows.getColumnNames();
            for (Db.Row row : rows) {
                ResultSet resultSet = row.resultSet();
                Map<String, Object> rowMap = new HashMap<>();
                for (String columnName : columnNames) {
                    rowMap.put(columnName, resultSet.getObject(columnName));
                }
                result.add(rowMap);
            }
            response.put("result", result);
            errorCode = "SUCCESS";
            if (rows != null) {
                rows.close();
            }
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

	@out(name = "result", type = Object.class, desc = "")
	public static Object fnExecutionSummaryReport(String i_taskExecutionId, String i_luName) throws Exception {
		fabric().execute("get TDM.?", i_taskExecutionId);
		
		String taskType = "" + fabric().fetch("select task_type from tasks limit 1").firstValue();
		String numOfRecsLimit = "" +  fabric().fetch("set TDM.TDM_SUMMARY_REPORT_LIMIT").firstValue();
		Object response;
		
		if ("LOAD".equalsIgnoreCase(taskType) || "RESERVE".equalsIgnoreCase(taskType) || "DELETE".equalsIgnoreCase(taskType) ) {
		    if ("ALL".equalsIgnoreCase(i_luName)) {
		        //log.info("Creating report for Load Task");
		        Map<String, Object> temp = new HashMap<>();
		        temp.put("i_numOfRecsLimit", numOfRecsLimit);
		        response = graphit("LoadSummaryReport.graphit", temp);
		    } else {
		        Map<String, Object> temp = new HashMap<>();
		        temp.put("i_luName", i_luName);
		        temp.put("i_numOfRecsLimit", numOfRecsLimit);
		        response = graphit("LoadSummaryReportPerLu.graphit", temp);
		    }
		
		} else {
			//log.info("Creating report for Extract Task");
			if ("ALL".equalsIgnoreCase(i_luName)) {
				//log.info("Creating report for Load Task");
		        Map<String, Object> temp = new HashMap<>();
		        temp.put("i_numOfRecsLimit", numOfRecsLimit);
				response = graphit("ExtractSummaryReport.graphit", temp);
			} else {
				Map<String, Object> temp = new HashMap<>();
				temp.put("i_luName", i_luName);
		        temp.put("i_numOfRecsLimit", numOfRecsLimit);
				response = graphit("ExtractSummaryReportPerLu.graphit", temp);
				}
		}
		
		return wrapWebServiceResults("SUCCESS", null, response);
	}


	public static Object fnGetNextTaskExecution(Long taskId) throws Exception {
		String query = "SELECT nextval('" + schema + ".tasks_task_execution_id_seq')";
		    Db.Rows rows = db(TDM).fetch(query);
		    List<String> columnNames = rows.getColumnNames();
		    Db.Row row = rows.firstRow();
			
		    if (!row.isEmpty()) {
				rows.close();
		        return row.cell(0);
		    }
		    return null;
	}


	@out(name = "result", type = Object.class, desc = "")
	public static Object fnStopTaskExecution(Long task_execution_id) throws Exception {
		Db.Rows batchIdList = null;
		//log.info("fnStopTaskExecution - task_execution_id: " + task_execution_id);
        try {
            //TDM 6.0 - Get the list of migration IDs based on task execution ID, instead of getting one migrate_id as input
            String sql = "select fabric_execution_id, execution_status, l.lu_id, l.task_type, " +
            "case when  selection_method = 'TABLES' then 'TDM_TableLevel' else u.lu_name end as lu_name " +
            "from " + schema + ".task_execution_list l, " + schema + ".tasks_logical_units u, " + schema + ".tasks t where task_execution_id = ? " +
            "and (fabric_execution_id is not null) and UPPER(execution_status) IN " +
            "('RUNNING','EXECUTING','STARTED','PENDING','PAUSED','STARTEXECUTIONREQUESTED') " +
            "and (selection_method = 'TABLES' or (l.task_id = u.task_id and l.lu_id = u.lu_id)) and l.task_id = t.task_id";
            log.info("AAAA - sql: " + sql);
            batchIdList = db(TDM).fetch(sql, task_execution_id);

            db(TDM).execute("UPDATE " + schema + ".task_execution_list SET execution_status='stopped',end_execution_time=current_timestamp at time zone 'utc'," +
                            " start_execution_time = CASE WHEN start_execution_time is NULL THEN current_timestamp at time zone 'utc' ELSE start_execution_time END" +
                            " WHERE task_execution_id = ? AND execution_status NOT IN ('completed', 'failed')",
                            task_execution_id);
            // TDM 5.1- add a reference handling- update the status of the reference tables to 'stopped'.

            db(TDM).execute("UPDATE " + schema + ".task_ref_exe_stats set execution_status='stopped', end_time=current_timestamp at time zone 'utc', number_of_processed_records = 0," +
					" start_time = CASE WHEN start_time is NULL THEN current_timestamp at time zone 'utc' ELSE start_time END" +
					" WHERE task_execution_id = ?" +
                    " AND execution_status NOT IN ('completed', 'failed')", task_execution_id);

            // TDM 7, set the execution summary to stopped also
            db(TDM).execute("UPDATE " + schema + ".task_execution_summary SET execution_status='stopped', end_execution_time=current_timestamp at time zone 'utc'," +
							" start_execution_time = CASE WHEN start_execution_time is NULL THEN current_timestamp at time zone 'utc' ELSE start_execution_time END" +
							" WHERE task_execution_id = ? AND execution_status NOT IN ('completed', 'failed')",task_execution_id);

            // TDM 5.1- cancel the migrate only if the input migration id is not null
            //TDM 6.0 - Loop over the list of migrate IDs
            for (Db.Row batchInfo : batchIdList) {
                String taskType = ("" + batchInfo.get("task_type")).toLowerCase();
                Long luID = (Long) batchInfo.get("lu_id");
                String luName = "" + batchInfo.get("lu_name");
                String taskExecutionID = "" + task_execution_id;
				//log.info("fabricExecID: <" + fabricExecID + ">");
				if(batchInfo.get("fabric_execution_id") != null) {
	                ludb().execute("batch_cancel '" + batchInfo.get("fabric_execution_id") + "'");
				}
                // TDM 7.1 Fix, stop execution of reference tables.
                //log.info("fnStopTaskExecution - Stopping the reference Handling for task_execution_id: " + task_execution_id + ", task_type: " + taskType);
                fnTdmReference(String.valueOf(task_execution_id), taskType);

               /* if (luID > 0 && ("extract".equals(taskType))) {
                    //log.info("wsStopTaskExecution - Updating task_execution_entities");
                    fnTdmUpdateTaskExecutionEntities(taskExecutionID, luID, luName);
                }*/
            }
            return wrapWebServiceResults("SUCCESS", null, null);
        } catch (Exception e) {
            log.error("wsStopTaskExecution", e);
            return wrapWebServiceResults("FAILED", null, null);

        } finally {
            if (batchIdList != null) {
                batchIdList.close();
            }
        }
	}


	public static List<Map<String, Object>> fnGetTaskPostExecutionProcesses(Long taskId) throws Exception {
        String query = "SELECT * FROM " + schema + ".TASKS_EXE_PROCESS  WHERE task_id =" + taskId;
        Db.Rows rows = db(TDM).fetch(query);

        List<Map<String, Object>> result = new ArrayList<>();
        List<String> columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            result.add(rowMap);
        }
		
		if (rows != null) {
			rows.close();
		}
        return result;
    }


	public static void fnSaveRefExeTablestoTask(Long task_id, Long taskExecutionId) throws Exception {
        List<Map<String, Object>> refs = (List<Map<String, Object>>) fnGetTaskReferenceTable(task_id);
        List<Map<String, Object>> refData = refs;
        if (refData.size() == 0) {
            return;
        }
        for (Map<String, Object> ref : refData) {
            String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now());

            String query = "INSERT INTO " + schema + 
                    ".task_ref_exe_stats (task_id,task_execution_id,task_ref_table_id, ref_table_name, update_date, execution_status, number_of_processed_records) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            db(TDM).execute(query,
                    task_id,
                    taskExecutionId,
                    ref.get("task_ref_table_id"),
                    ref.get("ref_table_name"),
                    now,
                    "pending",
                    0);
        }

    }


	public static Object fnGetTaskReferenceTable(Long task_id) throws Exception {
		String query = "SELECT * FROM " + schema + ".task_ref_tables where task_id = " + task_id;
        Db.Rows rows = db(TDM).fetch(query);
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            result.add(rowMap);
        }
		if (rows != null) {
			rows.close();
		}
        return result;
    }


	private static Object fnGetTaskExecSeqVal(String task_execution_id) throws Exception {
		 //Sereen - fix : tdm_seq_mapping PG table is deleted so we fetch the data from tdm_seq_mapping fabric table
        //DBExecute(DB_FABRIC, "set sync off", null);
        //DBExecute(DB_FABRIC, "get TDM." + task_execution_id, null);
        ludb().execute("get TDM." + task_execution_id);
        String sql = "SELECT entity_target_id , lu_type, source_env, table_name, column_name, source_id, target_id, is_instance_id FROM tdm_seq_mapping";
        Db.Rows rows = db(DB_FABRIC).fetch(sql);
        return rows;
    }


	public static Object fnResumeTaskExecution(Long task_execution_id) throws Exception {
        Boolean success_ind = true;
        Db.Rows batchIdList = null;
        try {
            //log.info("fnResumeTaskExecution - Starting");
            //TDM 6.0 - Get the list of migration IDs based on task execution ID, instead of getting one migrate_id as input
            batchIdList = db(TDM).fetch("select fabric_execution_id, execution_status, selection_method, l.task_type from " + 
					schema + ".task_execution_list l, " + schema + ".tasks t " +
                    "where task_execution_id = ? and l.task_id = t.task_id " +
                    "and (fabric_execution_id is not null or  selection_method = 'TABLES') and UPPER(execution_status)= 'STOPPED'", task_execution_id);

            db(TDM).execute("UPDATE " + schema + ".task_execution_list SET execution_status='running', end_execution_time=null where " + 
							"fabric_execution_id is not null " +
                            "and lower(execution_status) = 'stopped' and task_execution_id = ?",
                    task_execution_id);

            // TDM 7, set the status in execution summary to running
            db(TDM).execute("UPDATE " + schema + ".task_execution_summary SET execution_status='running', end_execution_time=null where task_execution_id = ? and execution_status = 'stopped'",
                    task_execution_id);
            db(TDM).execute("UPDATE " + schema + ".task_execution_list SET execution_status='pending' where fabric_execution_id is null and task_execution_id = ? " +
                            "and lower(execution_status) = 'stopped'",
                    task_execution_id);

            // TDM 5.1- add a reference handling- update the status of the reference tables to 'resume'.

            db(TDM).execute("UPDATE " + schema + ".task_ref_exe_stats set execution_status= 'resume', end_time=null " + 
                "where task_execution_id = ? and lower(execution_status) = 'stopped'", task_execution_id);

            // TDM 5.1- cancel the migrate only if the input migration id is not null
            //TDM 6.0 - Loop over the list of migrate IDs
            for (Db.Row batchInfo : batchIdList) {
                fabric().execute("delete instance TDM.?", task_execution_id);
                db(TDM).execute("UPDATE " + schema + ".task_execution_list SET synced_to_fabric = FALSE WHERE task_execution_id = ?", task_execution_id);
				if(batchInfo.get("fabric_execution_id") != null) {
	                fabric().execute("batch_retry '" + batchInfo.get("fabric_execution_id") + "' allow_cancelled=true");
				}
                // TDM 7.1 Fix, resume execution of reference tables.
                //log.info("fnResumeTaskExecution - Resume Reference");
                String taskType = ("" + batchInfo.get("task_type")).toLowerCase();
                fnTdmReference(String.valueOf(task_execution_id), taskType);

            }
        } catch (Exception e) {
            success_ind = false;
			e.printStackTrace();
            log.error("wsResumeTaskExecution: " + e);

        } finally {
            if (batchIdList != null) {
                batchIdList.close();
            }
        }

        return wrapWebServiceResults((success_ind ? "SUCCESS" : "FAILED"), null, success_ind);
    }


	public static void fnStartTaskExecutions(List<Map<String, Object>> taskExecutions, Long taskExecutionId, String srcEnvName, Long tarEnvId, Long srcEnvId, String executionNote) throws Exception {
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        for (Map<String, Object> entry : taskExecutions) {
            Long version_task_execution_id = 0L ;
            Long selected_ref_version_id = Long.parseLong("" +entry.get("selected_ref_version_task_exe_id")) ; 
            Long selected_version_id = Long.parseLong("" +entry.get("selected_version_task_exe_id")) ; 
            String query = "INSERT INTO " + schema + ".task_execution_list " +
                    "(task_id, task_execution_id, creation_date, be_id, environment_id, product_id, product_version, lu_id, " +
                    "data_center_name ,execution_status,parent_lu_id,source_env_name, task_executed_by, task_type, version_task_execution_id, " +
					"subset_task_execution_id, source_environment_id, process_id, execution_note) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
            if ("true".equalsIgnoreCase( "" + entry.get("version_ind"))) {
                if (selected_version_id != 0) {
                    version_task_execution_id = selected_version_id;
                } else if (selected_ref_version_id != 0) {
                    version_task_execution_id = selected_ref_version_id;
                } else {
                    version_task_execution_id = taskExecutionId;
                }
            }         
            String username = sessionUser().name();
                        Set<String> tmpRoles = new HashSet<>();
            for (String role : sessionUser().roles()) {
                if (!"Everybody".equalsIgnoreCase(role)) {
                    tmpRoles.add(role);
				}
            }
            
			if ("".equals(username)) {
				String userRoles = String.join(TDM_PARAMETERS_SEPARATOR, sessionUser().roles());
                if (!fnIsAdminByRole(userRoles)) {
                    throw new RuntimeException("Executing With Token Without Admin Privileges");
                }
            }
			
			String executedBy = new StringBuilder()
					.append(username)
					.append("##")
					.append(String.join(TDM_PARAMETERS_SEPARATOR, tmpRoles)) // Join roles with "<#>"
					.toString();

			//log.info("fnStartTaskExecutions - executedBy: " + executedBy);
            boolean includeProductInfo = Long.parseLong("" + entry.get("process_id")) == 0;
            db(TDM).execute(query,
                    entry.get("task_id"),
                    taskExecutionId,
                    now,
                    entry.get("be_id"),
                    tarEnvId != null ? tarEnvId : entry.get("environment_id"),
                    includeProductInfo ? entry.get("product_id") : null,
                    includeProductInfo ? entry.get("product_version") : null,
                    entry.get("lu_id"),
                    entry.get("data_center_name"),
                    "pending",
                    entry.get("lu_parent_id"),
                    srcEnvName != null ? srcEnvName : entry.get("source_env_name"),
                    executedBy,
                    entry.get("task_type"),
                    version_task_execution_id,
					Long.parseLong("" + entry.get("selected_subset_task_exe_id")),
					srcEnvId != null ? srcEnvId : entry.get("source_environment_id"),
                    entry.get("process_id"),
                    executionNote != null ? executionNote : null
            );
        }
    }


	@out(name = "result", type = Object.class, desc = "")
	public static Object fnGetNumberOfMatchingEntities(String whereStmt, String queryJson, String sourceEnvName, String targetEnvName, Long beID, String filteroutReserved,boolean analysisCount) throws Exception {
		String sourceEnv = !Util.isEmpty(sourceEnvName) ? sourceEnvName : "_dev";
        String paramsQuery = "";
        String iidFieldName = "root_iid";
        Boolean paramCoupling = isParamsCoupling();
        //TDM 9.1 - Params Coupling
        if (paramCoupling) {
            paramsQuery = generateListOfMatchingEntitiesQuery(beID, paramCoupling, queryJson, whereStmt, sourceEnvName, false,analysisCount);
            iidFieldName = "iid";
        } else {
            //String getEntitiesSql = generateListOfMatchingEntitiesQuery(beID, whereStmt, sourceEnv);
            String rootLUsList = db(TDM).fetch("select ARRAY_AGG(lu_name) from " + TDMDB_SCHEMA +
                ".product_logical_units where lu_parent_id is null and be_id = ?", beID).firstValue().toString();
            paramsQuery = whereStmt.replaceAll("WHERE ", "WHERE root_lu_name = ANY('" + rootLUsList + "') AND source_environment = '" +
                    sourceEnvName + "' AND (");
            paramsQuery = paramsQuery.replaceAll("FROM " , "FROM " + TDMDB_SCHEMA + ".");
            paramsQuery = paramsQuery.replaceAll("INTERSECT ", ") INTERSECT ");
            paramsQuery = paramsQuery.replaceAll("UNION ", ") UNION ");
            paramsQuery += ")";
        }

        String userID = sessionUser().name();
		log.error(userID);
        String tarEnvID = "" + db(TDM).fetch("SELECT environment_id FROM " + TDMDB_SCHEMA + ".environments WHERE environment_name = ? AND environment_status = 'Active'", targetEnvName).firstValue();
        Db.Rows rows =  null;
        String countClause = paramCoupling ? "COUNT(*)" : "COUNT(distinct " + iidFieldName + ")";
		String ExceptClause = paramCoupling ? "iid": "root_iid";
		String query = "WITH params_condition AS (" + paramsQuery + ") "
					+ "SELECT " + countClause + " FROM params_condition "
					+ "WHERE " + ExceptClause + " NOT IN ("
					+ "SELECT entity_id FROM " + TDMDB_SCHEMA + ".tdm_reserved_entities tr "
					+ "WHERE tr.env_id = ? "
					+ "AND tr.be_id = ? "
					+ "AND (tr.end_datetime IS NULL OR tr.end_datetime > CURRENT_TIMESTAMP)"
					+ ")";
        //log.info("fnGetNumberOfMatchingEntities1 - query: " + query);													
        if ("OTHERS".equalsIgnoreCase(filteroutReserved)) {
        	query = query.replace(" WHERE tr.env_id = ?", 
                        " WHERE tr.env_id = ? and tr.reserve_owner != ?");
			log.info("fnGetNumberOfMatchingEntities2 - query: " + query);	
            rows = db(TDM).fetch(query, tarEnvID, userID, beID);
        } else if ("ALL".equalsIgnoreCase(filteroutReserved)) {
			log.info("fnGetNumberOfMatchingEntities3 - query: " + query);
            rows = db(TDM).fetch(query, tarEnvID, beID);
        } else {
            query = paramCoupling 
					? "SELECT COUNT(*) FROM (" + paramsQuery + ") AS final_count" 
					: "SELECT COUNT(distinct " + iidFieldName + ") FROM (" + paramsQuery + ") AS final_count";
        	log.info("fnGetNumberOfMatchingEntities4 - query: " + query);													
            rows = db(TDM).fetch(query);
        }
		Object numberOfMatches = rows.firstValue();
		if (rows != null) {
		rows.close();
				}
		return wrapWebServiceResults("SUCCESS", null, numberOfMatches);
	}


	public static HashMap<String, Object> fnMigrateStatusWs(String migrateId, List<String> runModes) throws Exception {
		HashMap<String, Object> result = new HashMap<>();
        for (String runMode : runModes) {
            Object data = fnGetBatchStats(migrateId, runMode);
            List<HashMap<String, Object>> results = new ArrayList<>();
            HashMap<String, Object> newData = new HashMap<>();
            if (runMode.equals("S")) {
                String[] columnName = new String[]{"Status", "Ent./sec (avg.)", "Ent./sec (pace)", "Start time (UTC)", "Duration", "End time (UTC)",
                        "Name", "Succeeded", "Failed", "Added", "Updated", "Unchanged", "Total", "Level", "Remaining dur.", "Remaining", "% Completed"};
                for (HashMap<String, Object> row : (List<HashMap>) ((HashMap<String, Object>) data).get("result")) {
                    row.put("Start time (UTC)", row.get("Start time"));
                    row.put("End time (UTC)", row.get("End time"));
                    if (!row.containsKey("Added")) {
                        row.put("Added", row.get("Succeeded"));
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("columns", row);
                    results.add(map);
                }
                newData.put("results", results);
                newData.put("columnsNames", columnName);
                result.put(runMode, newData);
            } else {
                result.put(runMode, ((HashMap<String, Object>) data).get("result"));
            }
        }
        return result;
    }


	private static Object fnGetBatchStats(String i_batchId, String i_runMode) throws Exception {
		return wrapWebServiceResults("SUCCESS", null, fnBatchStatistics(i_batchId, i_runMode));
	}


	public static Object fnExtractRefStats(String i_taskExecutionId, String i_runMode) throws Exception {
		Object rs = null;

        if (i_runMode.equals("S")) {

            //log.info("call to fnGetReferenceSummaryData");
            Map<String, Object> refSummaryStatsBuf = fnGetReferenceSummaryData(i_taskExecutionId);

            // Convert the dates to strings
            refSummaryStatsBuf.forEach((key, value) -> {
                String minStartDate = "";
                String maxEndDate = "";
                Map<String, Object> refSummaryStats = (HashMap) value;

                if (refSummaryStats.get("minStartExecutionDate") != null)
                    minStartDate = refSummaryStats.get("minStartExecutionDate").toString();

                if (refSummaryStats.get("maxEndExecutionDate") != null)
                    maxEndDate = refSummaryStats.get("maxEndExecutionDate").toString();

                refSummaryStats.put("minStartExecutionDate", minStartDate);
                refSummaryStats.put("maxEndExecutionDate", maxEndDate);

                //log.info("after calling fnGetReferenceSummaryData");
            });
            rs = refSummaryStatsBuf;
        } else if (i_runMode.equals("D")) {
            rs = fnGetReferenceDetailedData(i_taskExecutionId);
        }

        // convert iterable to serializable object
        if (rs instanceof Db.Rows) {
            ArrayList<Map> rows = new ArrayList<>();
            ((Db.Rows) rs).forEach(row -> {
                HashMap copy = new HashMap();
                copy.putAll(row);
                rows.add(copy);
            });
            rs = rows;
        }

        return wrapWebServiceResults("SUCCESS", null, rs);
    }



	public static int getAllowedEntitySize(Integer entityListSize, Integer numberOfRequestedEntites) {
		int allowedEntitySize = -1;
        if (entityListSize > 0) {
            allowedEntitySize = entityListSize;
            if (numberOfRequestedEntites > 0 && numberOfRequestedEntites != entityListSize) {
                log.warn("The number of entities is set based on the overridden entity list, the given number of entities will be ignored.");
            }
        } else if (numberOfRequestedEntites > 0) {
            allowedEntitySize = numberOfRequestedEntites;
        }
        return allowedEntitySize;
    }


	public static Map<String, Object> fnCheckMigrateStatusForEntitiesList(String entities_list, String task_execution_id, String lu_list) throws Exception  {
        Map<String, Object> entity_mig_status = new LinkedHashMap<String, Object>();
        String[] entities_list_arr = entities_list.split(",");
        String[] lu_list_arr = lu_list.split(",");
        String entities_list_for_qry = "";
        String lu_list_for_qry = "";
		String getStatusesSql = "";

        //create a string with added single quotes to each entity in the entities list + preliminary mark every entity in the list as exists,
        //since the entity was already validated against the root LU in the GUI
        for (int i = 0; i < entities_list_arr.length; i++) {
            entity_mig_status.put(entities_list_arr[i].trim(), "true");
            entities_list_for_qry += "'" + entities_list_arr[i].trim() + "',";
        }
        // remove last ,
        entities_list_for_qry = entities_list_for_qry.substring(0, entities_list_for_qry.length() - 1);

        for (int i = 0; i < lu_list_arr.length; i++) {
            lu_list_for_qry += "'" + lu_list_arr[i].trim() + "',";
        }
        // remove last ,
        lu_list_for_qry = lu_list_for_qry.substring(0, lu_list_for_qry.length() - 1);

        fabric().execute("GET TDM.?", task_execution_id);

        boolean isFullHierarchyEnabled = "true".equalsIgnoreCase(fabric().fetch("Set POP_FULL_LU_HIERARCHY_IN_TDM_LU").firstValue().toString());
        if(isFullHierarchyEnabled){
             getStatusesSql = "select distinct be_root_entity_id from task_execution_link_entities " +
                "where be_root_entity_id in (" + entities_list_for_qry + ") and lu_name in (" + lu_list_for_qry + ") and execution_status <> 'completed'";
		}else{
             getStatusesSql = "select distinct be_root_entity_id from task_execution_link_entities " +
            "where be_root_entity_id in (" + entities_list_for_qry + ") and root_entity_status  <> 'completed'";
        }
        Db.Rows getStatuses = fabric().fetch(getStatusesSql);
        //Only entities that failed on any of the LUs will be returned, therefore for all the returned entities set the status to false
        for (Db.Row entityStatus : getStatuses) {
            entity_mig_status.put("" + entityStatus.cell(0), "false");
        }
		
		if (getStatuses != null)
		{
			getStatuses.close();
		}
        return wrapWebServiceResults("SUCCESS", null, entity_mig_status);
    }


	public static Boolean fnTestTaskInterfaces(Long task_id, Boolean forced, Long srcEnvId, Long tarEnvId) throws Exception {
		if (task_id != null && forced == true) return true;
        if (tarEnvId != null) {
		    fnTestEnvTargetInterfaces(task_id, tarEnvId, forced);
        }
		fnTestEnvSourceInterfaces(task_id, srcEnvId);
		return true;
	}


	public static Boolean fnTestEnvTargetInterfaces(Long taskId, Long tarEnvId, Boolean forced) throws Exception {
		String envName = null;
        Db.Row taskRec = db(TDM).fetch("select task_type, selection_method, source_env_name as environment_name from " +
            TDMDB_SCHEMA + ".tasks where task_id = ?", taskId).firstRow();
        
        if (taskRec.get("task_type").equals("EXTRACT")) return true;

        if ("TABLES".equals(taskRec.get("selection_method"))) {
            envName = taskRec.get("environment_name").toString();
        } else {

            List<Map<String, Object>> taskData;
		    try {
            
			    taskData = fnGetTaskEnvData(taskId, tarEnvId, false);
                envName = taskData.get(0).get("environment_name") != null ? taskData.get(0).get("environment_name").toString() : null;
		    } catch (Exception e) {
			    throw new Exception("Failed to get Task data for target env");
		    }
        }
		

		try {
			return fnTestInterfacesForEnvProduct(envName);
		} catch (Exception e) {
			if (e.getMessage().indexOf("interfaceFailed;") == 0) {
				throw new Exception("The test connection of " + e.getMessage().substring(16) + " failed. Please check the connection details of target environment: " + envName);
			} else throw new Exception("Failed to test target env interfaces");
		}
	}


	public static Boolean fnTestInterfacesForEnvProduct(String source) throws Exception {
		Map<String, String> data;
		try {
			data = (Map<String, String>) ((Map<String, Object>) fnTestConnectionForEnv(source)).get("result");
		} catch (Exception e) {
			throw new Exception("Failed to get interfaces for env from fabric");
		}

		List<String> errorInterfaces = new ArrayList<>();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (data.get(entry.getKey()).equals("false"))
				errorInterfaces.add(entry.getKey());
		}

		if (errorInterfaces.size() == 0) {
			return true;
		} else throw new Exception("interfaceFailed;" + errorInterfaces.toString());
	}


	private static List<Map<String, Object>> fnGetTaskEnvData(Long task_id, Long envId, Boolean source) throws Exception {
        log.info("fnGetTaskEnvData - task_id: " + task_id + ", env_id: " + envId +  ", source: " + source);
		String sql = "SELECT environments.environment_name, * FROM " + schema + ".tasks " +
				"INNER JOIN " + schema + ".tasks_logical_units " +
				"ON (tasks.task_id = tasks_logical_units.task_id) " +
				"INNER JOIN " + schema + ".product_logical_units " +
				"ON (product_logical_units.lu_id = tasks_logical_units.lu_id ) " +
				"INNER JOIN " + schema + ".environments " +
				(source ?
						"ON (environments.environment_id =" + (envId != null ? envId + ") " : "tasks.source_environment_id ) ") :
						"ON (environments.environment_id =" + (envId != null ? envId + ") " : "tasks.environment_id ) ")) +
				"INNER JOIN " + schema + ".environment_products " +
				"ON (environment_products.status = \'Active\' " +
				"AND environment_products.product_id = product_logical_units.product_id " +
				(source ?
						"AND (environment_products.environment_id =" + (envId != null ? envId + ")) " : "tasks.source_environment_id )) ") :
						"AND (environment_products.environment_id =" + (envId != null ? envId + ")) " : "tasks.environment_id )) ")) +
				"WHERE tasks.task_id = " + task_id;
		Db.Rows rows = db(TDM).fetch(sql);

		List<Map<String, Object>> result = new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		
		if (rows != null) {
			rows.close();
		}
		return result;
	}


	public static Boolean fnTestEnvSourceInterfaces(Long task_id, Long srcEnvId) throws Exception {
        String envName = null;
        Long envId = null;

        Db.Row taskRec = db(TDM).fetch("select selection_method, source_environment_id as environment_id, source_env_name as environment_name from " +
            TDMDB_SCHEMA + ".tasks where task_id = ?", task_id).firstRow();

        if ("TABLES".equals(taskRec.get("selection_method"))) {
            envName = taskRec.get("environment_name").toString();
            envId = (Long) taskRec.get("environment_id");
            try {
                fnTestInterfacesForEnvProduct(envName);
            } catch (Exception e) {
                if (e.getMessage().indexOf("interfaceFailed;") == 0) {
                    String errMessage = "The test connection of " + e.getMessage().substring(16) + " failed. Please check the connection details of source environment " + envName;
                    //if (taskData.get("task_type").toString().equals("EXTRACT"))  throw new Exception(errMessage);
                    try {
                        return fnTestTaskEnvGlobals(task_id, envId);
                    } catch (Exception exc) {
                        throw new Exception(errMessage);
                    }
                }
            }
        } else {
		    List<Map<String, Object>> taskData = null;
            log.info("fnTestEnvSourceInterfaces");
		    try {
			    taskData = fnGetTaskEnvData(task_id, srcEnvId, true);
		    } catch (Exception e) {
			    throw new Exception("Failed to get task source env data");
		    }

            if (taskData != null) {
                for (int i = 0; i < taskData.size(); i++) {
                    try {
                        fnTestInterfacesForEnvProduct(taskData.get(i).get("environment_name").toString());
                    } catch (Exception e) {
                        if (e.getMessage().indexOf("interfaceFailed;") == 0) {
                            String errMessage = "The test connection of " + e.getMessage().substring(16) + " failed. Please check the connection details of source environment " + taskData.get(0).get("environment_name");
                            //if (taskData.get("task_type").toString().equals("EXTRACT"))  throw new Exception(errMessage);
                            try {
                                return fnTestTaskEnvGlobals(task_id, (Long) taskData.get(0).get("environment_id"));
                            } catch (Exception exc) {
                                throw new Exception(errMessage);
                            }
                        }
                    }
                }
            }
        }
		return true;
	}


	public static Boolean fnTestTaskEnvGlobals(Long task_id, Long environment_id) throws Exception {
		Map<String, Object> data = null;
		try {
			data = fnGetTaskEnvGlobals(task_id, environment_id);
		} catch (Exception e) {
			throw new Exception("Failed to get globals for task");
		}
		if (data == null) throw new Exception("tdm_set_sync_off doesn't exist in task/env globals");

		Map<String, Object> tdmSetGlobalTask = null;
		for (Map<String, Object> global : (List<Map<String, Object>>) data.get("globals")) {
			if (global.get("global_name") != null && ((String) global.get("global_name")).equals("tdm_set_sync_off")) {
				tdmSetGlobalTask = global;
				break;
			}
		}

		if (tdmSetGlobalTask != null) {
			if (tdmSetGlobalTask.get("global_value") != null && ((String) tdmSetGlobalTask.get("global_value")).equals("true")) {
				return true;
			} else {
				throw new Exception("tdm_set_sync_off is not true in task globals");
			}
		}


		Map<String, Object> tdmSetGlobalEnv = null;
		for (Map<String, Object> global : (List<Map<String, Object>>) data.get("env_globals")) {
			if (global.get("global_name") != null && ((String) global.get("global_name")).equals("tdm_set_sync_off")) {
				tdmSetGlobalEnv = global;
				break;
			}
		}

		if (tdmSetGlobalEnv != null) {
			if (tdmSetGlobalEnv.get("global_value") != null && ((String) tdmSetGlobalEnv.get("global_value")).equals("true")) {
				return true;
			} else {
				throw new Exception("tdm_set_sync_off is not true in env globals");
			}
		}

		throw new Exception("tdm_set_sync_off doesn't exist in task/env globals");
	}


	public static Map<String, Object> fnGetTaskEnvGlobals(Long task_id, Long env_id) throws Exception {
		Map<String, Object> ans = new HashMap<>();

		String query = "SELECT * FROM " + schema + ".task_globals " +
				"WHERE task_globals.task_id = " + task_id;
		Db.Rows rows = db(TDM).fetch(query);

		List<Map<String, Object>> result = new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		
		if (rows != null) {
				rows.close();
		}

		for (Map<String, Object> global : result) {
			global.put("global_name", ((String) global.get("global_name")).toLowerCase());
			global.put("global_value", ((String) global.get("global_value")).toLowerCase());
		}

		ans.put("globals", result);

		query = "SELECT * FROM " + schema + ".tdm_env_globals " +
				"WHERE tdm_env_globals.environment_id = " + env_id;

		Db.Rows envGlobalsrows = db(TDM).fetch(query);

		List<Map<String, Object>> envResult = new ArrayList<>();
		columnNames = envGlobalsrows.getColumnNames();
		for (Db.Row row : envGlobalsrows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			envResult.add(rowMap);
		}
		
		if (envGlobalsrows != null) {
			envGlobalsrows.close();
		}

		for (Map<String, Object> global : envResult) {
			global.put("global_name", ((String) global.get("global_name")).toLowerCase());
			global.put("global_value", ((String) global.get("global_value")).toLowerCase());
		}

		ans.put("env_globals", envResult);
		return ans;
	}


	public static Object fnIsTaskRunning(Long taskId) throws Exception {
		String sql = "SELECT count(*) FROM " + schema + ".task_execution_list " +
				"WHERE task_id = " + taskId + " AND " +
				"(lower(execution_status) <> \'failed\' AND lower(execution_status) <> \'completed\' AND " +
				"lower(execution_status) <> \'stopped\' AND lower(execution_status) <> \'killed\')";
		Db.Rows rows = db(TDM).fetch(sql);
		Object result = rows.firstRow().cell(0);
		if (rows != null) {
			rows.close();
		}
		return result;
	}


	public static Boolean fnIsTaskActive(Long taskId) throws Exception {
		String query = "SELECT * FROM " + schema + ".tasks " +
				"WHERE task_id = " + taskId + " AND " +
				"task_status = \'Active\'";
		Db.Rows rows = db(TDM).fetch(query);
		if (!rows.firstRow().isEmpty()) {
			rows.close();
			return true;
		} else {
			throw new Exception("This task was changed and is currently inactive. Please refresh the page first to execute the task.");
		}
	}
    public static Boolean fnValidateBELogicalUnits(Long be_id, List<Map<String, Object>> logicalUnits) throws Exception {
        String query =  "Select lu_name , lu_id , lu_parent_name from " + schema + ".product_logical_units where be_id=?" ; 
        Db.Rows rows = db("TDM").fetch(query, be_id);
        List<Map<String, Object>> BElogicalUnits=new ArrayList<>();
        for ( Db.Row row : rows){
            Map<String, Object> map = new HashMap<>();
            map.put("lu_name", row.get("lu_name")); 
            map.put("lu_id",row.get("lu_id"));
            map.put("lu_parent_name",row.get("lu_parent_name"));
            BElogicalUnits.add(map);
        }  
        return checkParentExists(logicalUnits,BElogicalUnits);
    }

    public static Boolean checkParentExists(List<Map<String, Object>> logicalUnits, List<Map<String, Object>> BElogicalUnits) throws Exception {
        Boolean valid = true;
        Set<String> logicalUnitNames = new HashSet<>();
        for (Map<String, Object> lu : logicalUnits) {
            logicalUnitNames.add(lu.get("lu_name").toString());
        }
        for (Map<String, Object> beLu : BElogicalUnits) {
            if (beLu.containsKey("lu_parent_name")) {
                Object parentNameObj = beLu.get("lu_parent_name");
                if (parentNameObj != null) {
if (logicalUnitNames.contains(beLu.get("lu_name"))) {
                    String parentName = parentNameObj.toString();
                    if (!logicalUnitNames.contains(parentName)) {
                        valid = false;
                        throw new Exception("Missing parent for child Lu: " + beLu.get("lu_name"));
}
                    }
                }
            }
        }
        return valid;
    }

	public static List<Map<String, Object>> fnGetActiveTaskForActivation(Long taskId, String selectionMethod) throws Exception {
		Long lu_id = null;
        String clientQuery = "";
        if (!"TABLES".equals(selectionMethod)) {
		    clientQuery = "SELECT *, " +
				"(SELECT COUNT(*) FROM " + schema + ".task_ref_tables WHERE task_ref_tables.task_id = tasks.task_id) AS refcount " +
				"FROM " + schema + ".tasks " +
				"INNER JOIN " + schema + ".tasks_logical_units " +
				"ON (tasks.task_id = tasks_logical_units.task_id) " +
				"INNER JOIN " + schema + ".product_logical_units " +
				"ON (product_logical_units.lu_id = tasks_logical_units.lu_id ) " +
				"INNER JOIN " + schema + ".environment_products " +
				"ON (environment_products.status = \'Active\' " +
				"AND environment_products.product_id = product_logical_units.product_id " +
				"AND (environment_products.environment_id = tasks.environment_id " +
				"OR (tasks.environment_id IS NULL " +
				"AND environment_products.environment_id = tasks.source_environment_id ))) " +
				"WHERE tasks.task_id = ?";
        } else {
            clientQuery = "SELECT *, " +
				"(SELECT COUNT(*) FROM " + schema + ".task_ref_tables WHERE task_ref_tables.task_id = tasks.task_id) AS refcount, '-1' AS lu_id " +
				"FROM " + schema + ".tasks " + 
                "WHERE tasks.task_id = ?";
        }
		log.info(clientQuery);
		Db.Rows rows = db(TDM).fetch(clientQuery, taskId);

		List<Map<String, Object>> executions = new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> execution = new HashMap<>();
			for (String columnName : columnNames) {
				execution.put(columnName, resultSet.getObject(columnName));
				if(columnName.equals("lu_id")){
					lu_id=Long.valueOf("" + resultSet.getObject(columnName));
				}
			}
			execution.put("process_id", 0);
			executions.add(execution);
		}
		
		if (rows != null) {
			rows.close();
		}

		try {
			List<Map<String, Object>> data = fnGetTaskPostExecutionProcesses(taskId);
			Map<String, Object> execution = new HashMap(executions.get(0));
            Set<String> subsetProcesses = new HashSet<String>(Arrays.asList("Training Data Subset", "Exporting Data Subset", "Generating Data Subset", "Importing Data Subset"));
            for (Map<String, Object> item : data) {
                Map<String, Object> newItem = new HashMap<String, Object>(execution);
				if (subsetProcesses.contains(item.get("process_name"))){
					newItem.put("lu_id", lu_id);
				}else{
					newItem.put("lu_id", 0);

				}
				newItem.put("process_id", item.get("process_id"));
				newItem.put("process_name", item.get("process_name"));
				newItem.put("execution_order", item.get("execution_order"));
                newItem.put("lu_name", null);
				executions.add(newItem);
			}
			execution.put("product_id", 0);
			execution.put("product_version", 0);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
        
		return executions;
	}


	public static void fnCreateSummaryRecord(Map<String, Object> taskExecution, Long taskExecutionId, String srcEnvName, Long tarEnvId, Long srcEnvId) throws Exception {
		Map<String, Object> entry = taskExecution;
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());

		String username = sessionUser().name();
        Long version_task_execution_id = 0L ;
        Long selected_ref_version_id = Long.parseLong("" +entry.get("selected_ref_version_task_exe_id")) ; 
        Long selected_version_id = Long.parseLong("" +entry.get("selected_version_task_exe_id")) ; 

		String query = "INSERT INTO " + schema + ".task_execution_summary " +
				"(task_execution_id, task_id , task_type, creation_date, be_id, environment_id, execution_status, start_execution_time, end_execution_time," +
				" tot_num_of_processed_root_entities, tot_num_of_copied_root_entities, tot_num_of_failed_root_entities, tot_num_of_processed_ref_tables, tot_num_of_copied_ref_tables," +
				" tot_num_of_failed_ref_tables, source_env_name, source_environment_id, task_executed_by, version_task_execution_id, subset_task_execution_id, expiration_date, update_date) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        if ("true".equalsIgnoreCase( "" + entry.get("version_ind"))) {
            if (selected_version_id != 0) {
                version_task_execution_id = selected_version_id;
            } else if (selected_ref_version_id != 0) {
                version_task_execution_id = selected_ref_version_id;
            } else {
                version_task_execution_id = taskExecutionId;
            }
        }     
		db(TDM).execute(query,
				taskExecutionId,
				entry.get("task_id"),
				entry.get("task_type"),
				now,
				entry.get("be_id"),
				tarEnvId != null ? tarEnvId : entry.get("environment_id"),
				"pending",
				entry.get("start_execution_time"),
				entry.get("end_execution_time"),
				entry.get("tot_num_of_processed_root_entities"),
				entry.get("tot_num_of_copied_root_entities"),
				entry.get("tot_num_of_failed_root_entities"),
				entry.get("tot_num_of_processed_ref_tables"),
				entry.get("tot_num_of_copied_ref_tables"),
				entry.get("tot_num_of_failed_ref_tables"),
				srcEnvName != null ? srcEnvName : entry.get("source_env_name"),
				srcEnvId != null ? srcEnvId : entry.get("source_environment_id"),
				username,
				version_task_execution_id,
				Long.parseLong("" +entry.get("selected_subset_task_exe_id")),
				entry.get("expiration_date"),
				entry.get("update_date"));
	}

	public static void fnInsertActivity(String action, String entity, String description) throws Exception {
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		String username = sessionUser().name();
		String userId = username;
		String sql = "INSERT INTO " + schema + ".activities " +
				"(date, action, entity, user_id, username, description) " +
				"VALUES (?, ?, ?, ?, ?, ?)";
		db(TDM).execute(sql, now, action, entity, userId, username, description);
	}


	public static void fnPostTaskLogicalUnits(Long taskId, Long envId, List<Map<String, Object>> logicalUnits) throws Exception {
		String validateSql = "SELECT lu.lu_id FROM " + TDMDB_SCHEMA + ".ENVIRONMENT_PRODUCTS ep, " + TDMDB_SCHEMA + ".PRODUCT_LOGICAL_UNITS lu " +
            "WHERE ep.environment_id = ? and lu.lu_name  = ? AND ep.product_id = lu.product_id AND ep.status = 'Active'";
        String sql = "DELETE FROM " + schema + ".tasks_logical_units WHERE task_id = " + taskId;
		db(TDM).execute(sql);
		if (logicalUnits != null) {

            for (Map<String, Object> logicalUnit : logicalUnits) {
				Object luId = db(TDM).fetch(validateSql, envId, logicalUnit.get("lu_name"));
                if (luId == null) {
                    throw new Exception("LU: " + logicalUnit.get("lu_name") + " is not connected to Environemnt Id: " + envId);
                }
			}
			for (Map<String, Object> logicalUnit : logicalUnits) {
				db(TDM).execute("INSERT INTO " + schema + ".tasks_logical_units (task_id, lu_id,lu_name) VALUES ( ?, ?, ?)",
						taskId, logicalUnit.get("lu_id"), logicalUnit.get("lu_name"));
			}
		}
	}


	public static void fnSaveRefTablestoTask(Long taskId, List<Map<String, Object>> tableList) throws SQLException{
		//try {
            //log.info("fnSaveRefTablestoTask - tableList: " + tableList);
			for (Map<String, Object> ref : tableList) {
                String filterParamsStr = null;
                List<Object> filterParams = new ArrayList<>();
                if ( ref.get("filter_parameters") != null) {
                    //filterParams = ref.get("filter_parameters").toString();
                    Object obj = ref.get("filter_parameters");
                    if (obj.getClass().isArray()) {
                        filterParams = Arrays.asList((Object[])obj);
                    } else if (obj instanceof Collection) {
                        filterParams = new ArrayList<>((Collection<?>)obj);
                    }
                    filterParamsStr = filterParams.stream().map(Object::toString)
                    .collect(Collectors.joining(TDM_PARAMETERS_SEPARATOR));
                }

				String filterFieldsStr = null;
				if ( ref.get("filter_fields") != null) {
					Object obj = ref.get("filter_fields");
					filterFieldsStr = obj.toString();
				}
				
				String sql = "INSERT INTO " + schema + ".task_ref_tables " + 
                    "(task_id, ref_table_name, lu_name, schema_name, interface_name, update_date, table_filter, filter_type, " +
                    "target_table_prefix, target_table_suffix, version_task_execution_id, version_task_name, gui_filter, filter_parameters, filter_fields) " + 
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				db(TDM).execute(sql,
						taskId, ref.get("reference_table_name") != null ? ref.get("reference_table_name") : ref.get("ref_table_name"),
						ref.get("lu_name") != null ? ref.get("lu_name") : "TDM_TableLevel",
						ref.get("schema_name"),
						ref.get("interface_name"),
						DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
								.withZone(ZoneOffset.UTC)
								.format(Instant.now()),
						ref.get("table_filter"),
						ref.get("filter_type"),
                        ref.get("target_table_prefix"),
                        ref.get("target_table_suffix"),
                        ref.get("version_task_execution_id"),
                        ref.get("version_task_name"),
                        ref.get("gui_filter"),
                        filterParamsStr, 
						filterFieldsStr);
			}
		//} catch (Exception e) {
		//	log.error(e.getMessage());
		//}
	}


	public static Db.Rows fnGetRefTableForLoadWithoutVersion(List<String> logicalUnits) throws Exception {
		String lus = "";
		for (String lu : logicalUnits) lus += "'" + lu + "',";
		lus = lus.substring(0, lus.length() - 1);
		String sql = "Select distinct ref.lu_name, ref.ref_table_name, ref.schema_name, ref.interface_name " +
				"from " + schema + ".TASK_REF_TABLES ref, " + schema + ".TASK_REF_EXE_STATS exe, " + schema + ".task_execution_list l " +
				"Where ref.task_ref_table_id  = exe.task_ref_table_id " +
				"And ref.lu_name in (" + lus + ") " +
				"And exe.execution_status = \'completed\' " +
				"and exe.task_execution_id = l.task_execution_id " +
				"and lower(l.execution_status) = \'completed\' " +
				"and l.expiration_date is null;";
		Db.Rows rows = db(TDM).fetch(sql);
		return rows;
	}


	public static List<Map<String, Object>> fnGetRefTableForLoadWithVersion(List<String> logicalUnits) throws Exception {
		String lus = "";
		for (String lu : logicalUnits) lus += "'" + lu + "',";
		lus = lus.substring(0, lus.length() - 1);
		String sql = "Select distinct ref.lu_name, ref.ref_table_name, ref.schema_name, ref.interface_name , exe.execution_status , exe.task_execution_id " +
				"from " + schema + ".task_ref_tables ref, " + schema + ".task_ref_exe_stats exe , " + schema + ".task_execution_list l " +
				"Where ref.ref_table_name  = exe.ref_table_name " +
				"And ref.lu_name in (" + lus + ") " +
				"And exe.execution_status = \'completed\' " +
				"and exe.task_execution_id = l.task_execution_id " +
				"and lower(l.execution_status) = \'completed\' " +
				"and l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE \'UTC\'";
		Db.Rows rows = db(TDM).fetch(sql);

		List<Map<String, Object>> result = new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		
		if (rows != null) {
			rows.close();
		}

		for (Map<String, Object> row : result) {
			String id = row.get("lu_name") + "_" + row.get("ref_table_name") + "_" +
					row.get("schema_name") + "_" + row.get("interface_name");
			row.put("tId", id);
		}


		for (int i = 0; i < result.size(); i++) {
			for (int j = i + 1; j < result.size(); j++) {
				if (result.get(i).get("tId").toString().equals(result.get(j).get("tId").toString())) {
					result.remove(j);
					j--;
				}
			}
		}

		return result;
	}


	public static List<Map<String, Object>> fnGetVersionForLoadRef(List<String> refList, String source_env_name, String fromDate, String toDate) throws Exception {

		String versionDatesCond = "";
		if (fromDate != null && !"".equals(fromDate) && toDate != null && "".equals(toDate)) {
			versionDatesCond = "and l.version_datetime::date >= '" + fromDate + "' and l.version_datetime::date <= '" + toDate + "' ";
		}

		String query = "SELECT t.task_title version_name, t.task_id, l.task_execution_id, t.task_last_updated_by, l.task_execution_id, l.fabric_execution_id,  " +
				"CASE when t.selection_method= \'ALL\' then  \'ALL\' " +
				"when t.selection_method= \'REF\' then \'REF\' " +
				"else \'Selected Entities\' END version_Type, " +
				"l.version_datetime, lu.lu_name, l.num_of_copied_entities as num_of_succeeded_entities, l.num_of_failed_entities, l.execution_note " +
				"FROM " + schema + ".tasks t, " + schema + ".task_execution_list l, " + schema + ".tasks_logical_units lu, " +
				"(select  array_agg(lower(e.ref_table_name)) ref_list, array_agg(distinct lower(t.lu_name))  lu_list, task_execution_id " +
				"from " + schema + ".task_ref_exe_stats e, " + schema + ".task_ref_tables t where e.task_ref_table_id = t.task_ref_table_id and e.execution_status = \'completed\' " +
				"group by task_execution_id) ref " +
				"where lower(t.task_Type) = \'extract\'  " +
				"and t.task_id = l.task_id " +
				"and t.source_env_name = \'" + source_env_name + "\' " +
				"and lower(l.execution_status) = \'completed\' " +
				versionDatesCond +
				"and l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE \'UTC\' " +
				"and l.task_execution_id = ref.task_execution_id " +
				"and l.lu_id = lu.lu_id and l.task_id = lu.task_id ";

		for (String ref : refList) {
			query = query + "and lower(\'" + ref.toLowerCase() + "\') = ANY(ref_list) ";
		}
		Db.Rows rows = db(TDM).fetch(query);

		List<Map<String, Object>> result = new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		
		if (rows != null) {
			rows.close();
		}
		return result;
	}


	public static List<Map<String, Object>> fnGetLogicalUnitsForSourceAndTargetEnv(Long targetEnvId, Long srcEnvId) throws Exception {

		List<Map<String, Object>> result = new ArrayList<>();
		String query = "SELECT * FROM " + schema + ".product_logical_units " +
				"INNER JOIN " + schema + ".products " +
				"ON (product_logical_units.product_id = products.product_id AND products.product_status = \'Active\') " +
				"INNER JOIN " + schema + ".environment_products " +
				"ON (product_logical_units.product_id = environment_products.product_id AND environment_products.status = \'Active\') " +
				"WHERE environment_id = " + srcEnvId + " OR environment_id = " + targetEnvId;
		Db.Rows rows = db(TDM).fetch(query);

		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		
		if (rows != null) {
			rows.close();
		}

		for (int i = 0; i < result.size(); i++) {
			Map<String, Object> row = result.get(i);
			searchEqual:
			for (int j = i + 1; j < result.size(); j++) {
				Map<String, Object> otherRow = result.get(j);
				if (otherRow.get("lu_name").equals(row.get("lu_name"))) {
					for (String columnName : columnNames) {
						if (row.get(columnName) == null && otherRow.get(columnName) == null) continue;
						if (row.get(columnName) == null || otherRow.get(columnName) == null) continue searchEqual;
						if (!"lu_name".equals(columnName) && (!otherRow.get(columnName).toString().equals(row.get(columnName).toString())))
							continue searchEqual;
					}
					result.remove(j);
					j--;
				}
			}
		}


		return result;
	}


	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,Object> fnGetVersionsForLoad(String entitiesList, Long be_id, String source_env_name, String fromDate, String toDate, List<String> lu_list, String target_env_name,String filterout_reserved) throws Exception {
		Map<String, Object> result = new HashMap<>();
		String clientQuery = "";
		String logicalUnitList = "";
		
		for (String lu : lu_list) {
			logicalUnitList += ("'" + lu + "',");
		}
		if (logicalUnitList != "") logicalUnitList = logicalUnitList.substring(0, logicalUnitList.length() - 1);
		
		String logicalUnitListEqual = "";
		for (String lu : lu_list) {
			logicalUnitListEqual = logicalUnitListEqual + " and lower('" + lu + "') = any(lu_list) ";
		}
		
		String versionDatesCond = "";
		if (fromDate != null && !"".equals(fromDate) && toDate != null && !"".equals(toDate)) {
			versionDatesCond = "and l.creation_date::date >= '" + fromDate + "' and l.creation_date::date <= '" + toDate + "' ";
		}
		
		clientQuery = "with lu_list as (select l.task_execution_id, array_agg(lower(lu.lu_name)) lu_list " +
				"from " + schema + ".task_execution_list l, " + schema + ".product_logical_units lu where l.lu_id = lu.lu_id and lower(l.execution_status) = 'completed'  " +
				"group by task_execution_id) " +
				"select distinct t1.task_title version_name, t1.task_id, l1.task_execution_id, t1.task_last_updated_by, " +
				"CASE when t1.selection_method='ALL' then  'ALL' else  'Selected Entities' END version_Type, " +
				"l1.num_of_processed_entities number_of_extracted_entities, l1.creation_date as version_datetime , l1.task_execution_id, tlu.lu_name, l1.fabric_execution_id, " +
				"CASE when plu.lu_parent_id is null then 'Y' else 'N' END root_indicator, " +
				"l1.num_of_copied_entities as num_of_succeeded_entities, l1.num_of_failed_entities, l1.execution_note, " +
				"ROW_NUMBER () OVER (PARTITION BY t1.task_title, l1.lu_id ORDER BY l1.task_execution_id) version_no " +
				"from " + schema + ".task_execution_list l1, " + schema + ".tasks t1, " + schema + ".tasks_logical_units tlu, " + schema + ".product_logical_units plu where  " +
				"(t1.task_title, t1.task_id, l1.creation_date, l1.task_execution_id, l1.lu_id) in  " +
				"(SELECT distinct t.task_title as version_name, t.task_id, l.creation_date as version_datetime , l.task_execution_id, l.lu_id " +
				"from " + schema + ".tasks t, " + schema + ".task_execution_list l, lu_list  " +
				" where lower(t.task_Type) = 'extract' and t.task_id = l.task_id " +
				"and t.source_env_name = '" + source_env_name + "' " +
				"and lower(l.execution_status) = 'completed' " +
                "and t.version_ind = true " +
				versionDatesCond +
				"and l.expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC' " +
				"and l.lu_id in (select lu_id from " + schema + ".tasks_logical_units u " +
				"where l.task_id = u.task_id and u.lu_name in " +
				"(" + logicalUnitList + "))" +
				"and l.task_execution_id = lu_list.task_execution_id" +
				logicalUnitListEqual + ")" +
				"and t1.task_id = l1.task_id and lower(l1.execution_status) = 'completed' " +
				"and l1.task_id = tlu.task_id and l1.lu_id = tlu.lu_id " +
				"and l1.lu_id = plu.lu_id " +
				"and (plu.lu_parent_id is not null or l1.num_of_copied_entities > 0 ";
		
		if (entitiesList != null && entitiesList.length() > 0) {
            int  numOfEntities = entitiesList.length() - entitiesList.replace(",", "").length() + 1;

			clientQuery +=
					" and (" + be_id + " != plu.be_id or (";
			clientQuery += numOfEntities + " = (select count(1) from " + schema + ".task_execution_entities te " +
					"where te.task_execution_id = l1.task_execution_id and te.lu_name = plu.lu_name and plu.lu_parent_id is null " +
					"and te.iid = ANY(string_to_array('" + entitiesList + "', ',')) and execution_status = 'completed'))) ) ";
		} else {
			clientQuery += ")";
		}
		// End of TDM 7.4 change
		
		clientQuery += " order by l1.creation_date DESC ;";
		//log.info("fnGetVersionsForLoad - clientQuery: " + clientQuery);
		Db.Rows rows = db(TDM).fetch(clientQuery);
		      List<String> columnNames = rows.getColumnNames();
		      List<Map<String, Object>> rowsList = new ArrayList<>();
		
		      for (Db.Row row : rows) {
		          ResultSet resultSet = row.resultSet();
		          Map<String, Object> rowMap = new HashMap<>();
		          for (String columnName : columnNames) {
		              rowMap.put(columnName, resultSet.getObject(columnName));
		          }
		          rowsList.add(rowMap);
		      }
		//TDM7.4 - 26/1/2022 - Check for each entity if it is reserved by other user.
		
		result.put("ListOfVersions", rowsList);
		Map<String, Object> validation = new HashMap<>();
		if (entitiesList != null && entitiesList.length() > 0) {
			//log.info("target_env_name: " + target_env_name);
			String envID = "" + db(TDM).fetch("SELECT environment_id from " + schema + ".environments where environment_name = ? and environment_status = 'Active'",
					target_env_name).firstValue();
			String[] strings = entitiesList.split(",");
			ArrayList<String> entities = new ArrayList<>();
			Collections.addAll(entities, strings);
			String userName = ""; //The function will use calling user name
		
			if (!"null".equalsIgnoreCase(envID) && !"NA".equalsIgnoreCase(filterout_reserved)) {
				validation = fnValidateReservedEntities(Long.toString(be_id), envID, entities,filterout_reserved);
			}		
		      }
		
		if (rows != null) {
			rows.close();
		}
		
		result.put("EntityReservationValidations", validation);
		return result;
	}


	public static List<Map<String, Object>> fnGetRootLUs(String taskExecutionId) throws Exception {
		String sql = "select distinct t.task_execution_id, lu_name ,l.lu_id, " +
				"(select count(*) from " + schema + ".task_Execution_list t " +
				"where parent_lu_id = l.lu_id and t.task_execution_id = \'" + taskExecutionId + "\')," +
				"case when (num_of_failed_entities>0 or num_of_failed_ref_tables> 0) " +
				"then \'failed\' else \'completed\' end lu_status from " + schema + ".task_Execution_list t, " +
				"(select lu_id, lu_name from " + schema + ".product_logical_units) l " +
				"where t.task_execution_id =\'" + taskExecutionId + "\' and " +
				"t.parent_lu_id is null and t.lu_id = l.lu_id";
		Db.Rows rows = db(TDM).fetch(sql);

		List<Map<String, Object>> result = new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		
		if (rows != null) {
			rows.close();
		}
		return result;
	}


	public static List<Map<String, Object>> fnUpdateFailedLUsInTree(List<Map<String, Object>> rootLUs, Map<String, Object> failedEntities) {
		List<Map<String, Object>> tree = new ArrayList<>();

		Object entitiesList = failedEntities.get("entitiesList");
		if (failedEntities != null && ((List) entitiesList).size() > 0) {
			for (Map<String, Object> entity : (List<Map<String, Object>>) entitiesList) {
				List<Object> fullPathError = (List<Object>) entity.get("Full Error Path");
				tree = fnBuildTreeFromFullPathError(fullPathError, tree);
			}
		}

		for (Map<String, Object> rootLU : rootLUs) {
			Map<String, Object> found = null;
			for (Map<String, Object> e : tree) {
				if (e.get("lu_name").toString().equals(rootLU.get("lu_name").toString())) {
					found = e;
					break;
				}
			}

			if (found != null) {
				found.put("isRoot", true);
				found.put("count", rootLU.get("count"));
				found.put("errorInPath", true);
			} else {
				rootLU.put("isRoot", true);
				tree.add(rootLU);
			}
		}

		for (Map<String, Object> node : tree) {
			fnTreeIterate(node);
		}
		return tree;
	}


	public static void fnTreeIterate(Map<String, Object> current) {
		List<Map<String, Object>> currentChildren = (List<Map<String, Object>>) current.get("children");
		if (currentChildren == null || currentChildren.size() == 0) {
			current.put("collapsed", true);
			if (current.get("count") != null && Long.parseLong(current.get("count").toString()) > 0) {
				current.put("hasChildren", true);
			} else {
				current.put("hasChildren", false);
			}
			return;
		}
		for (int i = 0, len = currentChildren.size(); i < len; i++) {
			fnTreeIterate(currentChildren.get(i));
		}
	}


	public static List<Map<String, Object>> fnBuildTreeFromFullPathError(List<Object> list, List<Map<String, Object>> roots) {
		Map<String, Integer> map = new HashMap<>();
		Map<String, Object> node;

		for (int i = 0; i < list.size(); i += 1) {
			map.put(((Map<String, Object>) list.get(i)).get("luName").toString(), i); // initialize the map
			((Map<String, Object>) list.get(i)).put("children", fnGetNodeChildren(roots, list, i)); // initialize the children
		}

		for (int i = 0; i < list.size(); i += 1) {
			node = ((Map<String, Object>) list.get(i));

			if (node.get("parentLuName") != null && !node.get("parentLuName").toString().equals("") && !node.get("parentLuName").toString().equals(node.get("luName").toString())) {
				// if you have dangling branches check that map[node.parentId] exists
				List<Map<String, Object>> children = (List<Map<String, Object>>) ((Map<String, Object>) list.get(map.get(node.get("parentLuName")))).get("children");

				Map<String, Object> found = null;
				for (Map<String, Object> e : children) {
					if (e.get("lu_name").toString().equals(node.get("luName").toString())) {
						found = e;
						break;
					}
				}

				if (found == null) {
					HashMap<String, Object> nodeMap = new HashMap<>();
					nodeMap.put("lu_name", node.get("luName"));
					nodeMap.put("children", node.get("children") != null ? node.get("children") : new ArrayList<>());
					nodeMap.put("collapsed", true);
					nodeMap.put("hasChildren", true);
					children.add(nodeMap);
				}
			} else {
				Map<String, Object> found = null;
				for (Map<String, Object> e : roots) {
					if (e.get("lu_name").toString().equals(node.get("luName"))) {
						found = e;
						break;
					}
				}
				if (found == null) {
					HashMap<String, Object> nodeMap = new HashMap<>();
					nodeMap.put("lu_name", node.get("luName"));
					nodeMap.put("children", node.get("children") != null ? node.get("children") : new ArrayList<>());
					nodeMap.put("collapsed", true);
					nodeMap.put("hasChildren", true);
					roots.add(nodeMap);
				}
			}
		}

		node = roots.get(0);
		while (node != null) {
			List<Map<String, Object>> nodeChildren = (List<Map<String, Object>>) node.get("children");
			if (nodeChildren.size() > 0 && list.size() > 1) {
				node = nodeChildren.get(0);
			} else {
				node.put("lu_status", "failed");
				node = null;
			}
		}
		return roots;
	}


	public static List<Map<String, Object>> fnGetNodeChildren(List<Map<String, Object>> roots, List<Object> list, int index) {
		List<Map<String, Object>> treeNode = roots;
		for (int i = 0; i <= index; i++) {
			Map<String, Object> treeNodeMap = null;
			for (Map<String, Object> e : treeNode) {
				if (e.get("lu_name").toString().equals(((Map<String, Object>) list.get(i)).get("luName")))
					treeNodeMap = e;
			}
			if (treeNodeMap == null) return new ArrayList<>();
			treeNode = treeNodeMap.get("children") != null ? (List<Map<String, Object>>) treeNodeMap.get("children") : new ArrayList<>();
		}
		return treeNode != null ? treeNode : new ArrayList<>();
	}


	private  static Object fnTestConnectionForEnv(String env) throws Exception {
		//Log log = Log.a(com.k2view.cdbms.usercode.lu.k2_ws.TDM_Tasks.Logic.class);
		if (Util.isEmpty(env)) {
			env = "_dev";
		}

		fabric().execute("set environment='" + env + "';");

		Map<Object, Object> connResMap = new HashMap<>();
		fabric().fetch("test_connection active=true;").forEach(i -> {
			if (!"custom".equalsIgnoreCase("" + i.get("type"))) {
				connResMap.put(i.get("interface"), "" + i.get("passed"));
			}
		});

		return wrapWebServiceResults("SUCCESS", null, connResMap);
	}


	public static List<Map<String, Object>> fnGetUserRoleIdsAndEnvTypeByEnvName(String envName) throws Exception {
		List<Map<String, Object>> results = new ArrayList<>();
		String userId = sessionUser().name();
		String permissionGroup = fnGetUserPermissionGroup("");

		String fabricRoles = String.join(",", sessionUser().roles());

		//log.info("fnGetUserRoleIdsAndEnvTypeByEnvName - fabricRoles: " + fabricRoles);
		if ("admin".equalsIgnoreCase(permissionGroup)) {
			String allEnvs = "Select env.environment_id,env.environment_name,\n" +
					"  Case When env.allow_read = True And env.allow_write = True Then 'BOTH'\n" +
					"    When env.allow_write = True Then 'TARGET' Else 'SOURCE'\n" +
					"  End As environment_type,\n" +
					"  'admin' As role_id,\n" +
					"  'admin' As assignment_type\n" +
					"From " + schema + ".environments env\n" +
					"Where env.environment_status = 'Active' and env.environment_name=(?)";
			Db.Rows rows = db(TDM).fetch(allEnvs, envName);
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				results.add(rowMap);
			}
			
			if (rows != null) {
				rows.close();
			}

		} else {
			String sql = "select env.environment_id, env.environment_name, " +
					"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, " +
					"r.role_id, 'user' as assignment_type " +
					"from " + schema + ".environments env, " + schema + ".environment_roles r, " + schema + ".environment_role_users u " +
					"where env.environment_id = r.environment_id " +
					"and lower(r.role_status) = 'active' " +
					"and r.role_id = u.role_id " +
					"and ( (u.user_id = (?) and u.user_type = 'ID' or (lower(u.username) = 'all' and u.environment_id not in " +
					"(select u1.environment_id from " + schema + ".environment_role_users u1 where u1.user_id= (?) and u.user_type = 'ID' ))) " +
					"or (u.user_id = ANY (string_to_array(?, ',')) and u.user_type = 'GROUP' or (lower(u.username) = 'all' and u.environment_id not in " +
					"(select u1.environment_id from " + schema + ".environment_role_users u1 where u1.user_id = ANY (string_to_array(?, ',')) and u.user_type = 'GROUP' ))) ) " +
					"and env.environment_status = 'Active' and env.environment_name=(?)";
			Db.Rows rows = db(TDM).fetch(sql, userId, userId, fabricRoles, fabricRoles, envName);

			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				results.add(rowMap);
			}
			
			if (rows != null) {
				rows.close();
			}

			String query1 = "select env.environment_id, env.environment_name, " +
					"CASE when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, " +
					"'owner' as role_id, 'owner' as assignment_type " +
					"from " + schema + ".environments env, " + schema + ".environment_owners o " +
					"where env.environment_id = o.environment_id " +
					"and ( (o.user_id = (?) and o.user_type ='ID') " +
					"or (o.user_id = ANY (string_to_array(?, ',')) and o.user_type ='GROUP') ) " +
					"and env.environment_status = 'Active' and env.environment_name=(?)";
			rows = db(TDM).fetch(query1, userId, fabricRoles, envName);
			columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				results.add(rowMap);
			}
			
			if (rows != null) {
				rows.close();
			}
		}
		return results;
	}


	public static void fnSaveTaskOverrideParameters(Long taskId, Map<String, Object> overrideParameters, Long taskExecutionId) throws Exception {
		String sql = "INSERT INTO " + schema + ".task_execution_override_attrs (task_id,override_parameters,task_execution_id) VALUES (?,?,?)";
		String params_str = new JSONObject(overrideParameters).toString();
		db(TDM).execute(sql, taskId, params_str, taskExecutionId);
	}


	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,Object> fnValidateReservedEntities(String beID, String envID, ArrayList<String> entitiesList,String filterout_reserved) throws Exception {
		Map<String, Object> result = new HashMap<>();
		List<Map<String, Object>> entityList = new ArrayList<>();
		String userName = sessionUser().name();
		Set<String> fabricRolesSet = sessionUser().roles();
		//Boolean adminOrOwner = fnIsAdminOrOwner(envID, userName);
		String message  = "";

		//log.info("The user: " + userName + " is tester");
		String entities_list_for_qry = "";
		//ArrayList<String> releasedEntitiesList = entitiesList;
		
		for (int i = 0; i < entitiesList.size(); i++) {
			entities_list_for_qry += "'" + entitiesList.get(i).trim() + "',";
		}
		// remove last ,
		entities_list_for_qry = entities_list_for_qry.substring(0, entities_list_for_qry.length() - 1);
		
		//log.info("fnSaveTaskOverrideParameters - beID: " + beID + ", envID: " + envID);
		String baseQuery = "SELECT * FROM " + schema + ".TDM_RESERVED_ENTITIES WHERE " +
				"be_id = ? AND env_id = ? AND entity_id IN (" + entities_list_for_qry + ") " +
				"AND CURRENT_TIMESTAMP >= start_datetime " +
                        "AND (end_datetime IS NULL OR CURRENT_TIMESTAMP < end_datetime)";
		
String query="";
        Db.Rows reservedEntities;
        try {
            if ("OTHER".equalsIgnoreCase(filterout_reserved)) {
            query = baseQuery + " AND reserve_owner != ?";
            reservedEntities = db(TDM).fetch(query, beID, envID, userName);
            } else {
            query = baseQuery;
            reservedEntities = db(TDM).fetch(query, beID, envID);
            }		
		//log.info("-------- query: " + query);
					for (Db.Row row : reservedEntities) {
				String entityID = "" + row.get("entity_id");
				String owner = "" + row.get("reserve_owner");
				//remove entity from released Entities as it is not reserved
				//releasedEntitiesList.remove(entityID);
		
				Map<String, Object> reservedRec = new HashMap<>();
				reservedRec.put("entity_id", entityID);
				reservedRec.put("reserve_owner", owner);
				reservedRec.put("start_datetime", row.get("start_datetime"));
				reservedRec.put("end_datetime", row.get("end_datetime"));
				message += ("".equals(message)) ? entityID : "," + entityID;
		
				entityList.add(reservedRec);
			}
			
			if (reservedEntities != null) {
				reservedEntities.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception("Validation of entity list had failed with error: " + e.getMessage());
		}
		
		if (!"".equals(message)) {
			message = "Entities reserved: " + message;
		}
		result.put("listOfEntities", entityList);
		result.put("message", message);
		return result;
	}


	public static void createTaskGlobals(Long taskId, String luName, String globalName, String globalValue) throws Exception {
		if (luName != null && !"".equals(luName) && !"ALL".equals(luName)) {
			globalName = luName + "." + globalName;
		}
		String sql = "INSERT INTO " + schema + ".task_globals (task_id, global_name, global_value) VALUES (?, ?, ?)";
		db(TDM).execute(sql, taskId, globalName, globalValue);
	}


    //TDM 8.0 New function to populate the tdm_generate_task_field_mappings table.
	public static void createTaskGEnerateParams(Long taskId, HashMap<String,Object> params) throws Exception {
		String luName = getLuType().luName;
		
		JSONObject JSONObject = new JSONObject(params);
		for (String paramName : params.keySet()) {
			JSONObject paramValue = JSONObject.getJSONObject(paramName);
			Object value = null;
			if (paramValue.has("value")) {
				value = paramValue.get("value");
				String type = "" + paramValue.get("type");
				Long order = (Long)paramValue.get("order");
				if (value != null) {
					if (paramValue.has("default")) {
						Object defaultVal = paramValue.get("default");
						if(value.toString().equals(defaultVal.toString())) {
							continue;
						}
					}
		
		        	String insertSql  = "broadway " + luName + ".InsertIntoGenDataParamMappings task_id=?" +
						", param_name=?, param_type=?, param_value=?, param_order=?";
		
		        	fabric().execute(insertSql,taskId, paramName, type, value.toString(), order);
		    	}
			}
		}
	}


	public static Db.Rows fnGetTasks(String task_ids, String mode) throws Exception {
		String taskFilterCondition = "";
		// If task_ids are provided, we add the first filtering condition
		if (task_ids != null && !task_ids.isEmpty()) {
			taskFilterCondition = " WHERE task_id IN (" + task_ids + ") ";
		}
		// If mode is not "both", we add filtering for task status
		if (!"both".equalsIgnoreCase(mode)) {
			if (!taskFilterCondition.isEmpty()) {
				taskFilterCondition += " AND "; 
			} else {
				taskFilterCondition = " WHERE ";
			}
			taskFilterCondition += "task_status = '" + (mode.equalsIgnoreCase("active") ? "Active" : "Inactive") + "'";
		}
		String baseQuery =
			"WITH pre_filtered_tasks AS ( " +
			"   SELECT * FROM " + schema + ".tasks " + taskFilterCondition +
			"), " +
			"task_execution AS ( " +
			"   SELECT task_id, 1 AS executioncount FROM " + schema + ".task_execution_summary " +
			"   WHERE UPPER(execution_status) IN ('RUNNING','EXECUTING','STARTED','PENDING','PAUSED','STARTEXECUTIONREQUESTED') " +
			"), " +
			"task_references AS ( " +
			"   SELECT DISTINCT task_id, 1 AS refcount FROM " + schema + ".task_ref_tables " +
			"), " +
			"task_processes AS ( " +
			"   SELECT task_id, STRING_AGG(process_name::TEXT, ',') AS processnames " +
			"   FROM " + schema + ".TASKS_EXE_PROCESS GROUP BY task_id " +
			") " +
			"SELECT t.*, e.*, be.*, eo.user_name AS owner, eo.user_type AS owner_type, " +
			"eru.username AS tester, eru.user_type AS tester_type, eru.role_id AS role_id_orig, " +
			"t.sync_mode, te.executioncount, tr.refcount, tp.processnames " +
			"FROM pre_filtered_tasks t " +
			"LEFT JOIN " + schema + ".environments e ON t.environment_id = e.environment_id " +
			"LEFT JOIN " + schema + ".business_entities be ON t.be_id = be.be_id " +
			"LEFT JOIN " + schema + ".environment_owners eo ON t.environment_id = eo.environment_id " +
			"LEFT JOIN " + schema + ".environment_role_users eru ON t.environment_id = eru.environment_id " +
			"LEFT JOIN task_execution te ON t.task_id = te.task_id " +
			"LEFT JOIN task_references tr ON t.task_id = tr.task_id " +
			"LEFT JOIN task_processes tp ON t.task_id = tp.task_id " +
			"ORDER BY t.task_id DESC";	
		return db(TDM).fetch(baseQuery);
	}
	
	

public static List<HashMap<String, Object>> fnGetExecutionProcesses (Long be_id, String process_type) throws SQLException {
        try {
            String sql = "SELECT * FROM " + schema + ".TDM_BE_EXE_PROCESS  WHERE process_type= '" + process_type + "' AND be_id = " + be_id;
            Db.Rows rows = db(TDM).fetch(sql);

            List<HashMap<String, Object>> result = new ArrayList<>();
            HashMap<String, Object> process;
            for (Db.Row row : rows) {
                process = new HashMap<>();
                process.put("process_id", Long.parseLong(row.get("process_id").toString()));
                process.put("process_name", row.get("process_name"));
                process.put("process_description", row.get("process_description"));
                process.put("be_id", row.get("be_id") != null ? Long.parseLong(row.get("be_id").toString()) : null);
                process.put("execution_order", row.get("execution_order") != null ? Long.parseLong(row.get("execution_order").toString()) : null);
                process.put("process_type", row.get("process_type"));
                result.add(process);
            }
            if (rows != null) {
                rows.close();
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
	 private static Map<String, Object> fnGetChildHierarchy(String i_luName, String i_targetEntityID, String i_taskExecutionID, String sqlGetEntityData, String sqlGetChildren ) throws Exception {
		LinkedHashMap<String, Object> o_childHirarchyData = new LinkedHashMap<>();
		LinkedHashMap<String, Object> innerChild;
		List<Object> childData = new ArrayList<>();

		Db.Rows childRecs;
        if (i_taskExecutionID != null) {
            childRecs = db(TDM).fetch(sqlGetChildren, i_luName, i_targetEntityID, i_taskExecutionID);
        } else {
            childRecs = fabric().fetch(sqlGetChildren, i_luName, i_targetEntityID);
		}

		ArrayList<String[]> childrenRecs = new ArrayList<>();

		// Get the list of direct children of the current (input) entity if such exists
		for (Db.Row childRec : childRecs) {
			String[] childInfo = {"" + childRec.get("lu_name"), "" + childRec.get("target_entity_id")};
			childrenRecs.add(childInfo);
		}

		if (childRecs != null) {
			childRecs.close();
		}
		
			// Recursively process each child
        for (String[] child : childrenRecs) {
			String childLuName = child[0];
			String childTargetId = child[1];

						if (childLuName != null && !childLuName.isEmpty()) {
				innerChild = (LinkedHashMap<String, Object>) fnGetChildHierarchy(childLuName, childTargetId, i_taskExecutionID, sqlGetEntityData,sqlGetChildren);
				childData.add(innerChild);
			}
		}
		
        // Fetch entity details
        Db.Row entityRec;
        if (i_taskExecutionID != null) {
            entityRec = db(TDM).fetch(sqlGetEntityData, i_luName, i_targetEntityID, i_taskExecutionID).firstRow();
        } else {
            entityRec = fabric().fetch(sqlGetEntityData, i_luName, i_targetEntityID).firstRow();
		}

		o_childHirarchyData.put("luName", "" + entityRec.get("luName"));
		o_childHirarchyData.put("targetId", "" + entityRec.get("targetId"));

		// Get instance ID from entity ID
		Object[] splitId = fnSplitUID("" + entityRec.get("sourceId"));
		String instanceId = "" + splitId[0];
		o_childHirarchyData.put("sourceId", "" + instanceId);

		o_childHirarchyData.put("entityStatus", "" + entityRec.get("entityStatus"));
		o_childHirarchyData.put("parentLuName", "" + entityRec.get("parentLuName"));
		o_childHirarchyData.put("parentTargetId", "" + entityRec.get("parentTargetId"));

		if (childData.size() > 0) {
			o_childHirarchyData.put("children", childData);
		}

		o_childHirarchyData.put("luStatus", "" + entityRec.get("entityStatus"));

		return o_childHirarchyData;
	}

private static LinkedHashMap<String, Object> fnGetParentHierarchy(String i_luName, String i_targetEntityID, Object i_children, String sqlGetEntityData, String sqlGetParent, String i_taskExecutionID) throws Exception {
		LinkedHashMap<String, Object> currentEntity = new LinkedHashMap<>();
		LinkedHashMap<String, Object> upperParent = new LinkedHashMap<>();
		List<Object> childrenRecs = new ArrayList<>();

		if (i_children != null) {
			childrenRecs.add(i_children);
		}
Db.Row entityRec;
        // Get the data of the current (input) entity
        if (i_taskExecutionID != null) {
            entityRec = db(TDM).fetch(sqlGetEntityData, i_luName, i_targetEntityID, i_taskExecutionID).firstRow();
        } else {
            entityRec = fabric().fetch(sqlGetEntityData, i_luName, i_targetEntityID).firstRow();
		}

		currentEntity.put("luName", "" + entityRec.get("luName"));
		currentEntity.put("targetId", "" + entityRec.get("targetId"));

		// Get instance ID from entity id
				Object[] splitId = fnSplitUID("" + entityRec.get("sourceId"));
		String instanceId = "" + splitId[0];
		currentEntity.put("sourceId", "" + instanceId);

		currentEntity.put("entityStatus", "" + entityRec.get("entityStatus"));
		currentEntity.put("parentLuName", "" + entityRec.get("parentLuName"));
		currentEntity.put("parentTargetId", "" + entityRec.get("parentTargetId"));
currentEntity.put("children", childrenRecs);
        currentEntity.put("luStatus", "" + entityRec.get("luStatus"));
    
        String parentLuName = "";
        String parentTargetId = "";
    
        // Get the parent record, as each entity can have only one parent
        Db.Row parentRec;
        if (i_taskExecutionID != null) {
            parentRec = db(TDM).fetch(sqlGetParent, i_luName, i_targetEntityID, i_taskExecutionID).firstRow();
        } else {
            parentRec = fabric().fetch(sqlGetParent, i_luName, i_targetEntityID).firstRow();
        }
        if (!parentRec.isEmpty()) {
            parentLuName = "" + parentRec.get("parent_lu_name");
            parentTargetId = "" + parentRec.get("target_parent_id");
        }
    
        // Recursive call to get parent hierarchy if a parent exists
        if (parentLuName != null && !"".equals(parentLuName)) {
            upperParent = (LinkedHashMap<String, Object>) fnGetParentHierarchy(parentLuName, parentTargetId, currentEntity, sqlGetEntityData, sqlGetParent, i_taskExecutionID);
        }
    
        // If no parent exists, return the current entity as it's a root
        if (upperParent == null || upperParent.isEmpty()) {
            return currentEntity;
        }
    
        // Return the parent, which includes all hierarchy data
        return upperParent;
    }
    private static Map<String, Object> fnGetTaskExeStatsForEntity(String taskExecutionId, String luName, String targetId, boolean isTDMDB,String sqlGetEntityData, String sqlGetParent ) throws Exception {
        Map<String, Object> mainOutput = new HashMap<>();
        Map<String, Object> childHierarchyDetails;
        Map<String, Object> parentHierarchyDetails = new HashMap<>();
    
        if (isTDMDB) {
            childHierarchyDetails = fnGetChildHierarchyFromTDMDB(luName, targetId, taskExecutionId);
        } else {
            childHierarchyDetails = fnGetChildHierarchyFromTDMLU(luName, targetId);
        }

		String parentLuName = "";
		String parentTargetId = "";

		// Get the parent of the given LU
        Db.Row parentRec = isTDMDB 
            ? db(TDM).fetch(sqlGetParent, luName, targetId, taskExecutionId).firstRow() 
            : fabric().fetch(sqlGetParent, luName, targetId).firstRow();

		if (!parentRec.isEmpty()) {
			parentLuName = "" + parentRec.get("parent_lu_name");
			parentTargetId = "" + parentRec.get("target_parent_id");
		}
		
        // Get parent hierarchy if the input entity has parents
        if (parentLuName != null && !"".equals(parentLuName)) {
            parentHierarchyDetails = isTDMDB 
                ? fnGetParentHierarchyFromTDMDB(parentLuName, parentTargetId, childHierarchyDetails, taskExecutionId) 
                : fnGetParentHierarchyFromTDMLU(parentLuName, parentTargetId, childHierarchyDetails);
        } else {
            parentHierarchyDetails = childHierarchyDetails;
        }
    
        String rootLUName = "" + parentHierarchyDetails.get("luName");
        String rootTargetID = "" + parentHierarchyDetails.get("targetId");
        String rootSourceID = "" + parentHierarchyDetails.get("sourceId");
    
        mainOutput.put(rootLUName, parentHierarchyDetails);
    
        // Fetch other root entities with the same root entity ID
        Db.Rows otherRootRecs = isTDMDB 
            ? db(TDM).fetch(sqlGetEntityData, rootLUName, rootTargetID, rootSourceID, taskExecutionId) 
            : fabric().fetch(sqlGetEntityData, rootLUName, rootTargetID, rootSourceID);
    
        for (Db.Row rootRec : otherRootRecs) {
            Map<String, Object> rootDetails = new HashMap<>();
            String currRootLuName = "" + rootRec.get("luName");
            rootDetails.put("luName", currRootLuName);
            rootDetails.put("targetId", "" + rootRec.get("targetId"));
    
            // Get instance ID from entity ID
            Object[] splitId = fnSplitUID("" + rootRec.get("sourceId"));
            String instanceId = "" + splitId[0];
            rootDetails.put("sourceId", "" + instanceId);
            rootDetails.put("entityStatus", "" + rootRec.get("luStatus"));
    
            mainOutput.put(currRootLuName, rootDetails);
        }
    
        if (otherRootRecs != null) {
            otherRootRecs.close();
        }
    
        return mainOutput;
    }
    
    private static Map<String, Object> fnGetParentHierarchyFromTDMLU(String i_luName, String i_targetEntityID, Object i_children) throws Exception {
        String sqlGetEntityData = "select lu_name luName, target_entity_id targetId, entity_id sourceId, " +
                "execution_status entityStatus, parent_lu_name parentLuName, TARGET_PARENT_ID parentTargetId, root_entity_status luStatus " +
                "from TDM.task_Execution_link_entities where lu_name= ? and target_entity_id = ?";
    
        String sqlGetParent = "select parent_lu_name, target_parent_id from TDM.task_Execution_link_entities " +
                "where lu_name= ? and target_entity_id = ? limit 1";
    
        return fnGetParentHierarchy(i_luName, i_targetEntityID, i_children, sqlGetEntityData, sqlGetParent, null);
    }
    
    private static Map<String, Object> fnGetParentHierarchyFromTDMDB(String i_luName, String i_targetEntityID, Object i_children, String i_taskExecutionID) throws Exception {
        String sqlGetEntityData = "select lu_name luName, target_entity_id targetId, entity_id sourceId, " +
                "execution_status entityStatus, parent_lu_name parentLuName, parent_target_entity_id parentTargetId, " +
                "CASE WHEN t.execution_status <> 'completed' THEN 'failed' " +
                     "WHEN t.execution_status IS NULL THEN 'completed' " +
                     "ELSE t.execution_status END AS luStatus " +
                "from " + TDMDB_SCHEMA + ".task_execution_entities t where lu_name= ? and target_entity_id = ? and task_execution_id= ?";
    
        String sqlGetParent = "select parent_lu_name, parent_target_entity_id target_parent_id from " + TDMDB_SCHEMA + ".task_execution_entities " +
                "where lu_name= ? and target_entity_id = ? and task_execution_id= ? limit 1";
    
        return fnGetParentHierarchy(i_luName, i_targetEntityID, i_children, sqlGetEntityData, sqlGetParent, i_taskExecutionID);
    }

    private static Map<String, Object> fnGetChildHierarchyFromTDMLU(String i_luName, String i_targetEntityID) throws Exception {

        String sqlGetEntityData = "SELECT lu_name luName, target_entity_id targetId, entity_id sourceId, " +
                              "execution_status entityStatus, parent_lu_name parentLuName, TARGET_PARENT_ID parentTargetId, root_entity_status luStatus " +
                              "FROM TDM.task_Execution_link_entities WHERE lu_name= ? AND target_entity_id = ?";

        String sqlGetChildren = "SELECT lu_name, target_entity_id FROM TDM.task_Execution_link_entities " +
                            "WHERE parent_lu_name= ? AND target_parent_id = ?";
        return fnGetChildHierarchy(i_luName, i_targetEntityID, null,sqlGetEntityData,sqlGetChildren );
    }

    private static Map<String, Object> fnGetChildHierarchyFromTDMDB(String i_luName, String i_targetEntityID, String i_taskExecutionID) throws Exception {

        String sqlGetEntityData = "SELECT lu_name luName, target_entity_id targetId, entity_id sourceId, " +
            "execution_status entityStatus, parent_lu_name parentLuName, parent_target_entity_id parentTargetId, " +
            "CASE WHEN t.execution_status <> 'completed' THEN 'failed' " +
            "WHEN t.execution_status IS NULL THEN 'completed' ELSE t.execution_status END AS luStatus " +
            "FROM " + TDMDB_SCHEMA + ".task_execution_entities t WHERE lu_name= ? AND target_entity_id = ? AND task_execution_id = ?";

        String sqlGetChildren = "SELECT lu_name, target_entity_id FROM " + TDMDB_SCHEMA + ".task_execution_entities " +
            "WHERE parent_lu_name= ? AND parent_target_entity_id = ? AND task_execution_id = ?";
        return fnGetChildHierarchy(i_luName, i_targetEntityID, i_taskExecutionID,sqlGetEntityData,sqlGetChildren );

    }

   
    public static Map<String, Object> fnGetTaskExeStatsForEntityFromTDMLU(String taskExecutionId, String luName, String targetId) throws Exception {
        
        String sqlGetEntityData = "select lu_name luName, target_entity_id targetId, entity_id sourceId, " + "execution_status luStatus from TDM.task_Execution_link_entities  " + "where lu_name <> ? and target_entity_id = ? and entity_id = ?";
        String sqlGetParent = "select parent_lu_name, target_parent_id from TDM.task_Execution_link_entities " + "where lu_name= ? and target_entity_id = ? and parent_lu_name <> ''";
        return fnGetTaskExeStatsForEntity(taskExecutionId, luName, targetId, false,sqlGetEntityData,sqlGetParent);

    }

    public static Map<String, Object> fnGetTaskExeStatsForEntityFromTDMDB(String taskExecutionId, String luName, String targetId) throws Exception {
        String sqlGetEntityData = "select lu_name luName, target_entity_id targetId, entity_id sourceId, execution_status luStatus from " + TDMDB_SCHEMA +".task_execution_entities where lu_name <> ? and target_entity_id = ? and entity_id = ? and task_execution_id=?";
        String sqlGetParent = "select parent_lu_name, parent_target_entity_id target_parent_id from " + TDMDB_SCHEMA + ".task_execution_entities where lu_name= ? and target_entity_id = ? and parent_lu_name <> '' and task_execution_id=?";
        return fnGetTaskExeStatsForEntity(taskExecutionId, luName, targetId, true,sqlGetEntityData,sqlGetParent);

    }
}
