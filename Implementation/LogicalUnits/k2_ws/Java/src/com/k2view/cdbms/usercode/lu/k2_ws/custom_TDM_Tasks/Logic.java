/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.custom_TDM_Tasks;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.common.TDM.SharedLogic;
import com.k2view.cdbms.usercode.lu.k2_ws.*;
import com.k2view.fabric.api.endpoint.Endpoint.*;
import org.json.JSONObject;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.wrapWebServiceResults;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.fnGetUserPermissionGroup;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM_Tasks.Logic.wsGetTasks;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM_Tasks.TaskExecutionUtils.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends WebServiceUserCode {

	@desc("Gets regular (version_ind is false) active tasks (task_status and task_execution_status columns are 'Active') for a user based on the user's permission group (admin, owner, or tester) and based on the user's TDM environment roles:\r\n" +
			"\r\n" +
			"Admin Users:\r\n" +
			"- Get all active tasks.\r\n" +
			"\r\n" +
			"Owner Users:\r\n" +
			"- Get all active extract tasks if the user is the owner of at least one source environment.\r\n" +
			"- Get all active load tasks if the user is the owner of at least one source environment and one target environment.\r\n" +
			"- Get all active extract tasks that do not require special permissions if the user has at least one Read TDM Environment role.\r\n" +
			"- Get all active extract tasks that require special permissions if the user has at least one Read TDM Environment role with these permissions.\r\n" +
			"- Get all active load tasks that do not require special permissions if the user has at least one Read TDM Environment role and one Write TDM Environment role.\r\n" +
			"- Get all active load tasks that require special permissions if the user has at least one Read TDM Environment role, and one Write TDM Environment role with these permissions.\r\n" +
			"\r\n" +
			"Tester Users:\r\n" +
			"- Get all active extract tasks that do not require special permissions if the user has at least one Read TDM Environment role.\r\n" +
			"- Get all active extract tasks that require special permissions if the user has at least one Read TDM Environment role with these permissions.\r\n" +
			"- Get all active load tasks that do not require special permissions if the user has at least one Read TDM Environment role and one Write TDM Environment role.\r\n" +
			"- Get all active load tasks that require special permissions if the user has at least one Read TDM environment role, and one Write TDM Environment role with these permissions.")
	@webService(path = "regularTasksByUser", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"task_title\": \"testTask\",\r\n" +
			"      \"task_id\": 10\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"task_title\": \"testTask2\",\r\n" +
			"      \"task_id\": 13\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"task_title\": \"testTask3\",\r\n" +
			"      \"task_id\": 15\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsRegularTasksByUser() throws Exception {
		Boolean versionInd = false;
		try {
			List<Map<String, Object>> returnedResult = fnGetTasksByUser(versionInd);
		
			return wrapWebServiceResults("SUCCESS",null,returnedResult);
		}catch(Exception e){
			e.printStackTrace();
			return wrapWebServiceResults("FAILED",e.getMessage(),null);
		}
	}

	static List<Map<String, Object>> fnGetTasksByUser(Boolean versionInd) {
		List<Map<String, Object>> result = new ArrayList<>();
		//log.info("wsRegularTasksByUser - Starting");
		try {
			String permissionGroup = fnGetUserPermissionGroup("");
			
			String sql = "select * from tasks where lower(task_status) = 'active' and lower(task_execution_status) = 'active' and version_ind = ?";
			Db.Rows rows = db("TDM").fetch(sql, versionInd);
			List<String> columnNames = rows.getColumnNames();
			
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				
				if ("admin".equals(permissionGroup)){
					//log.info("wsRegularTasksByUser - ADMIN");
					for (String columnName : columnNames) {
						if(columnName.equals("task_title")|| columnName.equals("task_id")) {
							rowMap.put(columnName, resultSet.getObject(columnName));
						}
		
					}
					result.add(rowMap);
				} else {// The user is not admin
					//log.info("wsRegularTasksByUser - is not ADMIN");
					String taskID = "" + row.get("task_id");
					String taskTitle = "" + row.get("task_title");
					String beID = "" + row.get("be_id");
					String luList = "" + db("TDM").fetch("select string_agg( distinct lu_id::text, ',') from tasks_logical_units where task_id = ?", taskID).firstValue();
					//log.info("wsRegularTasksByUser - luList: " + luList); 	
					String refCnt = "" + db("TDM").fetch("select count(1) as cnt from task_ref_tables where task_id = ?", taskID).firstValue();
					String selectionMethod = "" + row.get("selection_method");
					String syncMode = "" + row.get("sync_mode");
					Boolean replaceSequences = (Boolean)row.get("replace_sequences");
					Boolean deleteBeforeLoad = (Boolean)row.get("delete_before_load");
					String taskType = "" + row.get("task_type");
					Boolean reserveInd = (Boolean) row.get("reserve_ind");
					//log.info("wsRegularTasksByUser - taskID: " + taskID + ", taskTitle: " + taskTitle + ", taskType: " + taskType);
					
					Integer refCount = Integer.parseInt(refCnt);
				
					ArrayList<HashMap<String, Object>> envsList = (ArrayList<HashMap<String, Object>>) ((HashMap<String, Object>) 
					  com.k2view.cdbms.usercode.lu.k2_ws.TDM_Tasks.Logic.wsGetEnvironmentsByTaskFilteringParams(
						beID,
						luList,
						refCount,
						selectionMethod,
						syncMode,
						false,
						replaceSequences,
						deleteBeforeLoad, //row.get("delete_before_load"),
						taskType,
						reserveInd)).get("result");
					
					//log.info("wsRegularTasksByUser - envsList: " + envsList);
					
					ArrayList<HashMap<String, Object>> allSourceEnvs = new ArrayList<>();
					ArrayList<HashMap<String, Object>> allTargetEnvs = new ArrayList<>();
					
					// If the user does not have any env for the task, filter it out. Else- add it to the result.
					
					for (HashMap<String, Object> envsMap : envsList) {
						for (String key: envsMap.keySet()) {
							//log.info("wsRegularTasksByUser - key: " + key);
							if ("source environments".equals(key)) {
								//log.info("wsRegularTasksByUser - envsMap: " + envsMap);
								ArrayList<HashMap<String, Object>>  newEnv = (ArrayList<HashMap<String, Object>>) envsMap.get(key);
								allSourceEnvs = newEnv;
								break;
							}
							if ("target environments".equals(key)) {
								//log.info("wsRegularTasksByUser - envsMap 2: " + envsMap);
								ArrayList<HashMap<String, Object>>  newEnv = (ArrayList<HashMap<String, Object>>) envsMap.get(key);
								allTargetEnvs = newEnv;
								break;
							}
						}
					}
					
					Boolean sourceEnvFound = false;
					if(allSourceEnvs != null) {
						// loop over user source envs
						for (Map<String, Object> sourceEnvMap : allSourceEnvs) {
							String envId = "" + sourceEnvMap.get("environment_id");
							//log.info("wsRegularTasksByUser - envId 1: " + envId);
							if (!"null".equals(envId) && !"".equals(envId)) {
								//log.info("wsRegularTasksByUser - Found source Env");
								sourceEnvFound = true;
							}
						}
					}
					Boolean targetEnvFound = false;
					if ("load".equalsIgnoreCase(taskType) && allTargetEnvs != null) {
						// loop over user source envs
						for (Map<String, Object> targetEnvMap : allTargetEnvs) {
							String envId = "" + targetEnvMap.get("environment_id");
							//log.info("wsRegularTasksByUser - envId 2: " + envId);
							if (!"null".equals(envId) && !"".equals(envId)) {
								//log.info("wsRegularTasksByUser - Found Target Env");
								targetEnvFound = true;
							}
						}
					} 
					 if ("extract".equalsIgnoreCase(taskType)){//Extract Task therfore there is no targetEnv to check
						targetEnvFound = true;
					}
					
					if(sourceEnvFound && targetEnvFound) {
						rowMap.put("task_title",taskTitle);
						rowMap.put("task_id",Integer.parseInt(taskID));
						result.add(rowMap);
					}
				}
		
			}
			List<Map<String, Object>> returnedResult = new ArrayList<>();
			for(Map<String, Object> row:result){
				Map<String, Object> Data=new HashMap<>();
				Data.put("task_title",row.get("task_title"));
				Data.put("task_id",row.get("task_id"));
				returnedResult.add(Data);
			}
		
			return returnedResult;
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Failed to get task list");
		}
	}

	@desc("Filters the tasks, returned for the user according the the input filtering parameters. The input is a dynamic JSON string. Currently it supports the following filtering parameters:\r\n" +
			"\r\n" +
			"- task_type : LOAD/EXTRACT\r\n" +
			"- version_ind : true/false\r\n" +
			"- load_entity : true/false\r\n" +
			"- delete_before_load : true/false\r\n" +
			"- selection_method : below is the list of the valid values:\r\n" +
			"    - 'L' (Entity list)\r\n" +
			"    - 'P' or 'PR' (Parameters),\r\n" +
			"    - 'S' (Synthetic),\r\n" +
			"    - 'R' (Random)\r\n" +
			"\r\n" +
			"- sync_mode : OFF/FORCE/ON\r\n" +
			"\r\n" +
			"The JSON filtering parameter is optional. If is it not populated, the API returns all user's tasks.\r\n" +
			"\r\n" +
			"Input examples:\r\n" +
			"\r\n" +
			"{\"task_type\":\"EXTRACT\", \"version_ind\":true, \"selection_method\":\"L\", \"sync_mode\":\"OFF\"}\r\n" +
			"\r\n" +
			"{\"task_type\":\"EXTRACT\", \"version_ind\": false, \"load_entity\": true, \"delete_before_load\": true, \"selection_method\":\"L\", \"sync_mode\":\"FORCE\"}")
	@webService(path = "getTasksByParams", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    \r\n" +
			"        {\r\n" +
			"          \"task_id\": \"4\",\r\n" +
			"          \"task_title\": \"Task1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"task_id\": \"1\",\r\n" +
			"          \"task_title\": \"Task2\"\r\n" +
			"        }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTasksByParams(@param(description="Json string containg the filtering patams set as key and value. Example in method description.") String filteringParams) throws Exception {
		String message = null;
		String errorCode = "";
		String task_type = null, selection_method = null, sync_mode = null;
		Boolean version_ind = null, load_entity = null, delete_before_load = null;
		HashMap<String, Object> response = new HashMap<>();
		List<Map<String, Object>> finalTasksList = new ArrayList<>();
		
		try {
		
			if (filteringParams != null) {
				JSONObject jsonInput = new JSONObject(filteringParams);
				task_type = jsonInput.has("task_type") ? jsonInput.get("task_type").toString() : null;
				selection_method = jsonInput.has("selection_method") ? jsonInput.get("selection_method").toString() : null;
				sync_mode = jsonInput.has("sync_mode") ? jsonInput.get("sync_mode").toString() : null;
				version_ind = jsonInput.has("version_ind") ? (Boolean) jsonInput.get("version_ind") : false;
				load_entity = jsonInput.has("load_entity") ? (Boolean) jsonInput.get("load_entity") : null;
				delete_before_load = jsonInput.has("delete_before_load") ? (Boolean) jsonInput.get("delete_before_load") : null;
			}
		
			HashMap<String, Object> wsUserTasks = new HashMap<>();
			if (!version_ind) {// Version Ind is set to false, get regular tasks
				wsUserTasks = (HashMap<String, Object>) wsRegularTasksByUser();
			} else {// Version Ind is set to true, get Data Flux tasks
				wsUserTasks = (HashMap<String, Object>) wsDataVersionTaskByUser();
			}
			
			List<Map<String, Object>> allUserTasks = (List<Map<String, Object>>) wsUserTasks.get("result");
			for (Map<String, Object> task : allUserTasks) {
				HashMap<String, Object> wsTaskDetails = (HashMap<String, Object>) wsGetTasks(task.get("task_id").toString());
				List<Map<String, Object>> allTaskDetails = (List<Map<String, Object>>) wsTaskDetails.get("result");
				if (task_type != null && !task_type.equalsIgnoreCase(allTaskDetails.get(0).get("task_type").toString()))
					continue;
				if (load_entity != null && load_entity != allTaskDetails.get(0).get("load_entity"))
					continue;
				if (delete_before_load != null && delete_before_load != allTaskDetails.get(0).get("delete_before_load"))
					continue;
				if (selection_method != null && allTaskDetails.get(0).get("selection_method") != null && !selection_method.equalsIgnoreCase(allTaskDetails.get(0).get("selection_method").toString()))
					continue;
				String syncModeFromTaskDetails = (allTaskDetails.get(0).get("sync_mode") != null) ? allTaskDetails.get(0).get("sync_mode").toString() : "ON";
				if (sync_mode != null && !sync_mode.equalsIgnoreCase(syncModeFromTaskDetails))
					continue;
		
				HashMap<String, Object> finalTaskMap = new HashMap<>();
				finalTaskMap.put("task_id", task.get("task_id").toString());
				finalTaskMap.put("task_title", task.get("task_title").toString());
				finalTasksList.add(finalTaskMap);
			}
			response.put("result", finalTasksList);
			errorCode = "SUCCESS";
		} catch (Exception e) {
			message = e.getMessage();
			errorCode = "FAILED";
		}
		
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets data versioning (Data Flux) tasks with version_ind set to true. Only active tasks (task_status and task_execution_status columns are 'Active') are taken, The task list is returned for the user based on the user's permission group (admin, owner, or tester) and based on the user's TDM environment permissions: \r\n" +
			"\r\n" +
			"Admin Users:\r\n" +
			"- Get all active tasks.\r\n" +
			"\r\n" +
			"Owner Users:\r\n" +
			"- Get all active extract tasks if the user is the owner of at least one source environment.\r\n" +
			"- Get all active load tasks if the user is the owner of at least one source environment and one target environment.\r\n" +
			"- Get all active extract tasks if the user has at least one Read TDM Environment permission set that enables a data versioning.\r\n" +
			"- Get all active load tasks if the user has at least one Read TDM Environment permission set and one Write TDM Environment permission set that enable a data versioning.\r\n" +
			"\r\n" +
			"Tester Users:\r\n" +
			"- Get all active extract tasks if the user has at least one Read TDM Environment permission set that enables a data versioning.\r\n" +
			"- Get all active load tasks if the user has at least one Read TDM Environment permission set and one Write TDM Environment permission set that enable a data versioning.")
	@webService(path = "VersionTasksByUser", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"task_title\": \"testTask\",\r\n" +
			"      \"task_id\": 10\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"task_title\": \"testTask2\",\r\n" +
			"      \"task_id\": 13\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"task_title\": \"testTask3\",\r\n" +
			"      \"task_id\": 15\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDataVersionTaskByUser() throws Exception {
		Boolean versionInd = true;
		try {
			List<Map<String, Object>> returnedResult = fnGetTasksByUser(versionInd);
		
			return wrapWebServiceResults("SUCCESS",null,returnedResult);
		}catch(Exception e){
			e.printStackTrace();
			return wrapWebServiceResults("FAILED",e.getMessage(),null);
		}
	}
}
