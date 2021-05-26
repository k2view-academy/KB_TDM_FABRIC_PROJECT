/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM_Tasks;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import java.util.stream.Collectors;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.k2_ws.*;
import com.k2view.fabric.api.endpoint.Endpoint.*;
import com.k2view.cdbms.usercode.lu.k2_ws.*;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import org.apache.avro.generic.GenericData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.transform.Result;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import com.k2view.cdbms.usercode.common.TDM.SharedLogic;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {
	public static String schema="public";
	public static final String DB_FABRIC = "fabric";

	@desc("Gets the list of available Business Entities that can be selected in the task based on the related Products of the task's environment. The API checks the source environment of Extract tasks and the target environment of Load tasks.")
	@webService(path = "environment/{envId}/businessEntitiesForEnvProducts", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"be_name\": \"BE\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"be_id\": 2,\r\n" +
			"      \"be_name\": \"BE2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetBusinessEntitiesForEnvProducts(@param(required=true) Long envId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		List<HashMap<String,Object>> result=new ArrayList<>();
		String message=null;
		String errorCode="";
		try{
			String sql= "SELECT * FROM \"" + schema + "\".product_logical_units " +
					"INNER JOIN \"" + schema + "\".products " +
					"ON (\"" + schema + "\".product_logical_units.product_id = \"" + schema + "\".products.product_id AND \"" + schema + "\".products.product_status = \'Active\') " +
					"INNER JOIN \"" + schema + "\".environment_products " +
					"ON (\"" + schema + "\".product_logical_units.product_id = \"" + schema + "\".environment_products.product_id AND \"" + schema + "\".environment_products.status = \'Active\') " +
					"INNER JOIN \"" + schema + "\".business_entities " +
					"ON (\"" + schema + "\".business_entities.be_id = \"" + schema + "\".product_logical_units.be_id) " +
					"WHERE environment_id = " + envId + " AND be_status = \'Active\'";
			Db.Rows rows= db("TDM").fetch(sql);
		
		
			HashMap<String,Object> be;
			for(Db.Row row:rows) {
				ResultSet resultSet = row.resultSet();
				be = new HashMap<>();
				be.put("be_id", resultSet.getInt("be_id"));
				be.put("be_name", resultSet.getString("be_name"));
				result.add(be);
			}
		
			HashMap<String,Object> be2;
			for(int i=0;i<result.size();i++){
				be=result.get(i);
				for(int j=i+1;j<result.size();j++){
					be2=result.get(j);
					if(be.get("be_id").toString().equals(be2.get("be_id").toString())
							&& be.get("be_name").toString().equals(be2.get("be_name").toString())){
						result.remove(j); j--;
					}
				}
			}
		
			errorCode="SUCCESS";
			response.put("result",result);
		
		} catch(Exception e){
			errorCode="FAIL";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("Gets the list of all TDM tasks\n")
	@webService(path = "tasks", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "[\r\n" +
			"      {\r\n" +
			"      \"task_last_updated_date\": \"date\",\r\n" +
			"      \"be_id\": beId,\r\n" +
			"      \"selected_version_task_name\": null,\r\n" +
			"      \"environment_id\": envId,\r\n" +
			"      \"selection_method\": \"selectionMethod\",\r\n" +
			"      \"selected_ref_version_task_name\": null,\r\n" +
			"      \"refresh_reference_data\": \"f\",\r\n" +
			"      \"tester\": \"tester\",\r\n" +
			"      \"be_last_updated_date\": \"date\",\r\n" +
			"      \"owners\": [\r\n" +
			"        null\r\n" +
			"      ],\r\n" +
			"      \"refcount\": 2,\r\n" +
			"      \"keep_ownership_of_data_for_days\": 0,\r\n" +
			"      \"load_entity\": \"f\",\r\n" +
			"      \"selected_version_task_exe_id\": 0,\r\n" +
			"      \"task_created_by\": \"k2view\",\r\n" +
			"      \"be_last_updated_by\": \"K2View\",\r\n" +
			"      \"scheduling_end_date\": \"Date\",\r\n" +
			"      \"retention_period_type\": \"Days\",\r\n" +
			"      \"environment_point_of_contact_phone1\": null,\r\n" +
			"      \"processnames\": \"name1,name2\",\r\n" +
			"      \"testers\": [\r\n" +
			"        {\r\n" +
			"          \"role_id\": [\r\n" +
			"            \"id\"\r\n" +
			"          ],\r\n" +
			"          \"tester\": \"teaster1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"role_id\": [\r\n" +
			"            \"id\"\r\n" +
			"          ],\r\n" +
			"          \"tester\": \"tester2\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"selection_param_value\": null,\r\n" +
			"      \"request_of_fresh_data\": \"t\",\r\n" +
			"      \"environment_status\": \"Active\",\r\n" +
			"      \"be_status\": \"Active\",\r\n" +
			"      \"selected_version_datetime\": null,\r\n" +
			"      \"task_last_updated_by\": \"k2view\",\r\n" +
			"      \"selected_ref_version_task_exe_id\": 0,\r\n" +
			"      \"task_execution_status\": \"Active\",\r\n" +
			"      \"sync_mode\": null,\r\n" +
			"      \"replace_sequences\": \"f\",\r\n" +
			"      \"entity_exclusion_list\": null,\r\n" +
			"      \"environment_point_of_contact_last_name\": null,\r\n" +
			"      \"environment_point_of_contact_email\": null,\r\n" +
			"      \"be_description\": null,\r\n" +
			"      \"parameters\": null,\r\n" +
			"      \"environment_expiration_date\": null,\r\n" +
			"      \"environment_point_of_contact_phone2\": null,\r\n" +
			"      \"environment_created_by\": \"k2view\",\r\n" +
			"      \"roles\": [\r\n" +
			"        [\r\n" +
			"          {\r\n" +
			"            \"role_id\": roleId,\r\n" +
			"            \"allowed_test_conn_failure\": \"f\"\r\n" +
			"          }\r\n" +
			"        ]\r\n" +
			"      ],\r\n" +
			"      \"environment_last_updated_by\": \"k2view\",\r\n" +
			"      \"be_creation_date\": \"date\",\r\n" +
			"      \"task_id\": taskId,\r\n" +
			"      \"allow_read\": \"t\",\r\n" +
			"      \"be_created_by\": \"k2view\",\r\n" +
			"      \"source_environment_id\": srcEnvId,\r\n" +
			"      \"role_id_orig\": id,\r\n" +
			"      \"scheduler\": \"immediate\",\r\n" +
			"      \"environment_description\": \"Descriptoin\",\r\n" +
			"      \"selected_ref_version_datetime\": null,\r\n" +
			"      \"source_env_name\": \"srcEnvName\",\r\n" +
			"      \"task_title\": \"taskTitle\",\r\n" +
			"      \"fabric_environment_name\": null,\r\n" +
			"      \"environment_name\": \"envName\",\r\n" +
			"      \"delete_before_load\": \"f\",\r\n" +
			"      \"allow_write\": \"t\",\r\n" +
			"      \"owner\": null,\r\n" +
			"      \"task_status\": \"Inactive\",\r\n" +
			"      \"retention_period_value\": \"5\",\r\n" +
			"      \"executioncount\": 0,\r\n" +
			"      \"environment_last_updated_date\": \"date\",\r\n" +
			"      \"be_name\": \"BE\",\r\n" +
			"      \"version_ind\": \"f\",\r\n" +
			"      \"number_of_entities_to_copy\": 0,\r\n" +
			"      \"task_creation_date\": \"date\",\r\n" +
			"      \"task_globals\": \"f\",\r\n" +
			"      \"environment_point_of_contact_first_name\": null,\r\n" +
			"      \"task_type\": \"LOAD\",\r\n" +
			"      \"environment_creation_date\": \"date\"\r\n" +
			"    }\r\n" +
			"]")
	public static Object wsGetTasks() throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		
		try{
			String sql= "SELECT tasks.*,environments.*,business_entities.*,environment_owners.user_name  as owner,environment_role_users.username  as tester,environment_role_users.role_id  as role_id_orig, tasks.sync_mode," +
					"( SELECT COUNT(*) FROM task_execution_list WHERE task_execution_list.task_id = tasks.task_id AND" +
					" ( UPPER(task_execution_list.execution_status)" +
					"  IN ('RUNNING','EXECUTING','STARTED','PENDING','PAUSED','STARTEXECUTIONREQUESTED'))) AS executioncount, " +
					" ( SELECT COUNT(*) FROM task_ref_tables WHERE task_ref_tables.task_id = tasks.task_id ) AS refcount,  " +
					" ( SELECT string_agg(process_name::text, ',') FROM TASKS_POST_EXE_PROCESS WHERE TASKS_POST_EXE_PROCESS.task_id = tasks.task_id ) AS processnames " +
					" FROM tasks LEFT JOIN environments" +
					" ON (tasks.environment_id = environments.environment_id) LEFT JOIN business_entities ON" +
					" (tasks.be_id = business_entities.be_id) LEFT JOIN environment_owners ON" +
					" (tasks.environment_id = environment_owners.environment_id) " +
					" LEFT JOIN environment_role_users ON" +
					" (tasks.environment_id = environment_role_users.environment_id)";
			Db.Rows result = db("TDM").fetch(sql);
		
			String q = "SELECT * FROM ENVIRONMENT_ROLES";
			Db.Rows rolesResult = db("TDM").fetch(q);
		
			//modified newRow will be added to newResult list
			List<Map<String,Object>> newResult=new ArrayList<>();

			for (Db.Row row:result) {
				HashMap<String, Object> newRow = new HashMap<>();
				ResultSet resultSet = row.resultSet();
				newRow.put("task_id",resultSet.getInt("task_id"));
				newRow.put("task_title", resultSet.getString("task_title"));
				newRow.put("task_status", resultSet.getString("task_status"));
				newRow.put("task_execution_status", resultSet.getString("task_execution_status"));
				newRow.put("number_of_entities_to_copy", resultSet.getInt("number_of_entities_to_copy"));
				newRow.put("environment_id", resultSet.getInt("environment_id"));
				newRow.put("be_id", resultSet.getInt("be_id"));
				newRow.put("selection_method", resultSet.getString("selection_method"));
				newRow.put("selection_param_value", resultSet.getString("selection_param_value"));
				newRow.put("entity_exclusion_list", resultSet.getString("entity_exclusion_list"));
				newRow.put("parameters", resultSet.getString("parameters"));
				newRow.put("refresh_reference_data", resultSet.getBoolean("refresh_reference_data"));
				newRow.put("delete_before_load", resultSet.getBoolean("delete_before_load"));
				newRow.put("replace_sequences", resultSet.getBoolean("replace_sequences"));
				newRow.put("scheduler", resultSet.getString("scheduler"));
				newRow.put("task_created_by", resultSet.getString("task_created_by"));
				newRow.put("task_creation_date", resultSet.getString("task_creation_date"));
				newRow.put("task_last_updated_date", resultSet.getString("task_last_updated_date"));
				newRow.put("task_last_updated_by", resultSet.getString("task_last_updated_by"));
				newRow.put("source_env_name", resultSet.getString("source_env_name"));
				newRow.put("source_environment_id", resultSet.getInt("source_environment_id"));
				newRow.put("fabric_environment_name", resultSet.getString("fabric_environment_name"));
				newRow.put("load_entity", resultSet.getBoolean("load_entity"));
				newRow.put("task_type", resultSet.getString("task_type"));
				newRow.put("version_ind", resultSet.getBoolean("version_ind"));
				newRow.put("retention_period_type", resultSet.getString("retention_period_type"));
				newRow.put("retention_period_value", resultSet.getString("retention_period_value"));
				newRow.put("selected_version_task_name", resultSet.getString("selected_version_task_name"));
				newRow.put("selected_version_datetime", resultSet.getString("selected_version_datetime"));
				newRow.put("selected_version_task_exe_id", resultSet.getInt("selected_version_task_exe_id"));
				newRow.put("scheduling_end_date", resultSet.getString("scheduling_end_date"));
				newRow.put("selected_ref_version_task_name", resultSet.getString("selected_ref_version_task_name"));
				newRow.put("selected_ref_version_datetime", resultSet.getString("selected_ref_version_datetime"));
				newRow.put("selected_ref_version_task_exe_id", resultSet.getInt("selected_ref_version_task_exe_id"));
				newRow.put("task_globals", resultSet.getBoolean("task_globals"));
				newRow.put("sync_mode", resultSet.getString("sync_mode"));
				newRow.put("environment_name", resultSet.getString("environment_name"));
				newRow.put("environment_description", resultSet.getString("environment_description"));
				newRow.put("environment_expiration_date", resultSet.getString("environment_expiration_date"));
				newRow.put("environment_point_of_contact_first_name", resultSet.getString("environment_point_of_contact_first_name"));
				newRow.put("environment_point_of_contact_last_name", resultSet.getString("environment_point_of_contact_last_name"));
				newRow.put("environment_point_of_contact_phone1", resultSet.getString("environment_point_of_contact_phone1"));
				newRow.put("environment_point_of_contact_phone2", resultSet.getString("environment_point_of_contact_phone2"));
				newRow.put("environment_point_of_contact_email", resultSet.getString("environment_point_of_contact_email"));
				newRow.put("environment_created_by", resultSet.getString("environment_created_by"));
				newRow.put("environment_creation_date", resultSet.getString("environment_creation_date"));
				newRow.put("environment_last_updated_date", resultSet.getString("environment_last_updated_date"));
				newRow.put("environment_last_updated_by", resultSet.getString("environment_last_updated_by"));
				newRow.put("environment_status", resultSet.getString("environment_status"));
				newRow.put("allow_write", resultSet.getBoolean("allow_write"));
				newRow.put("be_name", resultSet.getString("be_name"));
				newRow.put("be_description", resultSet.getString("be_description"));
				newRow.put("be_created_by", resultSet.getString("be_created_by"));
				newRow.put("be_creation_date", resultSet.getString("be_creation_date"));
				newRow.put("be_last_updated_date", resultSet.getString("be_last_updated_date"));
				newRow.put("be_last_updated_by", resultSet.getString("be_last_updated_by"));
				newRow.put("be_status", resultSet.getString("be_status"));
				newRow.put("owner", resultSet.getString("owner"));
				newRow.put("tester", resultSet.getString("tester"));
				newRow.put("role_id_orig", resultSet.getInt("role_id_orig"));
				newRow.put("executioncount", resultSet.getInt("executioncount"));
				newRow.put("refcount", resultSet.getInt("refcount"));
				newRow.put("processnames", resultSet.getString("processnames"));
		
				Map<String, Object> task = null;
				for (Map<String, Object> e : newResult) {
					if (Integer.parseInt(e.get("task_id").toString()) == resultSet.getInt("task_id")) {
						task = e;
						break;
					}
				}
		
				List<Map<String, Object>> roleArr = new ArrayList<>();
		
				for (Db.Row role : rolesResult) {
					ResultSet roleResultSet = role.resultSet();
					if (roleResultSet.getInt("environment_id") == resultSet.getInt("environment_id")) {
						HashMap<String, Object> roleMap = new HashMap<>();
						roleMap.put("role_id", roleResultSet.getInt("role_id"));
						roleMap.put("allowed_test_conn_failure", roleResultSet.getBoolean("allowed_test_conn_failure"));
						roleArr.add(roleMap);
					}
				}
		
		
				if (task != null) {
					List<String> owners = (List<String>) task.get("owners");
					if (!owners.contains(resultSet.getString("owner"))) {
						owners.add(resultSet.getString("owner"));
					}
		
					List<Map<String, Object>> testers = (List<Map<String, Object>>) task.get("testers");
					Map<String, Object> tester = null;
					for (Map<String, Object> _tester : testers) {
						if (_tester.get("tester").toString().equals(resultSet.getString("tester"))) {
							tester = _tester;
						}
					}
					if (tester != null) {  //add role_id_orig to role_id list
						List<String> roleId = (List<String>) tester.get("role_id");
						if (!roleId.contains(resultSet.getString("role_id_orig"))) {
							roleId.add(resultSet.getString("role_id_orig"));
						}
					} else { //add new tester to task testers
						HashMap<String, Object> testerMap = new HashMap<>();
						testerMap.put("tester", resultSet.getString("tester"));
						List<String> roleIdList= new ArrayList<>();
						roleIdList.add(resultSet.getString("role_id_orig"));
						testerMap.put("role_id", roleIdList);
						testers.add(testerMap);
					}
		
				} else {
					List<String> owners = new ArrayList<>();
					newRow.put("owners", owners);
					if (resultSet.getString("owner") != null) {
						owners.add(resultSet.getString("owner"));
					}
		
					List<Map<String, Object>> testers = new ArrayList<>();
					newRow.put("testers", testers);
					if (resultSet.getString("tester") != null) {
						HashMap<String, Object> testerMap = new HashMap<>();
						testerMap.put("tester", resultSet.getString("tester"));
						List<String> roleIdList= new ArrayList<>();
						roleIdList.add(resultSet.getString("role_id_orig"));
						testerMap.put("role_id", roleIdList);
						testers.add(testerMap);
					}
		
					List<List<Map<String, Object>>> roles = new ArrayList<>();
					newRow.put("roles", roles);
					if (roleArr != null && roleArr.size() > 0) {
						roles.add(roleArr);
					}
		
					newResult.add(newRow);
				}
		
			}
		
			errorCode="SUCCESS";
			response.put("result",newResult);
		
		} catch(Exception e){
			errorCode="FAIL";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("Gets Running Tasks IDs")
	@webService(path = "runningTasks", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    \"82\",\r\n" +
			"    \"83\",\r\n" +
			"    \"68\",\r\n" +
			"    \"72\",\r\n" +
			"    \"69\",\r\n" +
			"    \"67\",\r\n" +
			"    \"87\",\r\n" +
			"    \"114\"\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetRunningTasks() throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try{
			String sql= "select DISTINCT task_id from task_execution_list " +
					"where  (lower(execution_status) <> 'failed' AND lower(execution_status) <> 'completed' " +
					"AND lower(execution_status) <> 'stopped' AND lower(execution_status) <> 'killed')";
			Db.Rows rows= db("TDM").fetch(sql);
			List<String> result = new ArrayList<>();
			for(Db.Row row:rows){
				result.add(row.cell(0).toString());
			}
			errorCode="SUCCESS";
			response.put("result",result);
		
		} catch(Exception e){
			errorCode="FAIL";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("Gets the Logical Units list of the task's Business Entity (BE) and the task's environment products. The user can select part or all of the LUs in the list in the task. For extract task, the source environment ID is sent to the API, and for load task the target environment ID is sent to the API.")
	@webService(path = "businessentity/{beId}/environment/{envId}/logicalunits", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"lu_parent_name\": \"parentName\",\r\n" +
			"      \"lu_name\": \"name\",\r\n" +
			"      \"lu_id\": 23\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_parent_name\": \"PATIENT_LU\",\r\n" +
			"      \"lu_name\": \"PATIENT_VISITS\",\r\n" +
			"      \"lu_id\": 12\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_parent_name\": \"PATIENT_VISITS\",\r\n" +
			"      \"lu_name\": \"VISIT_LAB_RESULTS\",\r\n" +
			"      \"lu_id\": 16\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetLogicalUnitsByEnvironmentAndBusinessentity(@param(required=true) Long beId, @param(required=true) Long envId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try{
			String sql= "SELECT * FROM \"" + schema + "\".product_logical_units " +
					"INNER JOIN \"" + schema + "\".products " +
					"ON (\"" + schema + "\".product_logical_units.product_id = \"" + schema + "\".products.product_id) " +
					"INNER JOIN \"" + schema + "\".environment_products " +
					"ON (\"" + schema + "\".product_logical_units.product_id = \"" + schema + "\".environment_products.product_id " +
					"AND environment_products.status = \'Active\') " +
					"WHERE be_id = " + beId + " AND environment_id = " + envId;
			Db.Rows rows= db("TDM").fetch(sql);
			List<HashMap<String,Object>> result = new ArrayList<>();
		
			HashMap<String,Object> lU;
			for(Db.Row row:rows){
				ResultSet resultSet=row.resultSet();
				lU=new HashMap<>();
				lU.put("lu_id",resultSet.getInt("lu_id"));
				lU.put("lu_parent_name",resultSet.getString("lu_parent_name"));
				lU.put("lu_name",resultSet.getString("lu_name"));
				result.add(lU);
			}
		
			errorCode="SUCCESS";
			response.put("result",result);
		
		} catch(Exception e){
			errorCode="FAIL";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("Creates Task.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"\r\n" +
			"{\r\n" +
			"  \"be_id\": 3,\r\n" +
			"  \"environment_id\": 1,\r\n" +
			"  \"source_environment_id\": 1,\r\n" +
			"  \"scheduler\": \"immediate\",\r\n" +
			"  \"delete_before_load\": true,\r\n" +
			"  \"request_of_fresh_data\": true,\r\n" +
			"  \"number_of_entities_to_copy\": 0,\r\n" +
			"  \"selection_method\": \"R\",\r\n" +
			"  \"selection_param_value\": null,\r\n" +
			"  \"entity_exclusion_list\": \"5,9,28\",\r\n" +
			"  \"task_title\": \"taskTitle\",\r\n" +
			"  \"parameters\": null,\r\n" +
			"  \"refresh_reference_data\": true,\r\n" +
			"  \"replace_sequences\": true,\r\n" +
			"  \"source_env_name\": \"env1\",\r\n" +
			"  \"load_entity\": true,\r\n" +
			"  \"task_type\": \"LOAD\",\r\n" +
			"  \"scheduling_end_date\": \"2021-02-04 14:20:59.454\",\r\n" +
			"  \"version_ind\": true,\r\n" +
			"  \"retention_period_type\": \"Days\",\r\n" +
			"  \"retention_period_value\": 5,\r\n" +
			"  \"selected_version_task_name\": \"TaskName\",\r\n" +
			"  \"selected_version_datetime\": \"2021-02-04\",\r\n" +
			"  \"selected_version_task_exe_id\": 0,\r\n" +
			"  \"task_globals\": true,\r\n" +
			"  \"selected_ref_version_task_exe_id\": 0,\r\n" +
			"  \"selected_ref_version_datetime\": \"2021-02-04\",\r\n" +
			"  \"selected_ref_version_task_name\": null,\r\n" +
			"  \"sync_mode\": null,\r\n" +
			"  \"selectAllEntites\": true,\r\n" +
			"  \"refList\": [\r\n" +
			"    {\r\n" +
			"        \"reference_table_name\":\t\"RefT\",\r\n" +
			"        \"logical_unit_name\": \"RefLU\",\r\n" +
			"        \"schema_name\": \"RefSchema\",\r\n" +
			"        \"interface_name\": \"RefInterface\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"        \"reference_table_name\":\t\"RefT2\",\r\n" +
			"        \"logical_unit_name\": \"RefLU2\",\r\n" +
			"        \"schema_name\": \"RefSchema2\",\r\n" +
			"        \"interface_name\": \"RefInterface2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"globals\": [\r\n" +
			"    {\r\n" +
			"      \"global_name\":\"globalName1\",\r\n" +
			"      \"global_value\":\"globalValue1\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"global_name\":\"globalName2\",\r\n" +
			"      \"global_value\":\"globalValue2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"reference\": \"ref\"\r\n" +
			"}")
	@webService(path = "task", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 145\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateTask(Long be_id, Long environment_id, Long source_environment_id, String scheduler, Boolean delete_before_load, Integer number_of_entities_to_copy, String selection_method, String selection_param_value, String entity_exclusion_list, String task_title, String parameters, Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, Boolean load_entity, String task_type, String scheduling_end_date, Boolean version_ind, String retention_period_type, Integer retention_period_value, String selected_version_task_name, String selected_version_datetime, Integer selected_version_task_exe_id, Boolean task_globals, Integer selected_ref_version_task_exe_id, String selected_ref_version_datetime, String selected_ref_version_task_name, String sync_mode, Boolean selectAllEntites, List<Map<String,Object>> refList, List<Map<String,Object>> globals, String reference) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		Map<String,Object> result=new HashMap<>();
		
		if(refList!=null) {
			Iterator<Map<String, Object>> iter = refList.iterator();
			while (iter.hasNext()) {
				Map<String, Object> ref = iter.next();
				if (ref.get("selected") == null || (boolean) ref.get("selected") != true) {
					iter.remove();
				}
			}
		}
		
		if (reference!=null&&reference.equals("refernceOnly")) {
			if (refList!=null ) {
				if(refList.size() > 0){
					selection_method = "REF";
					delete_before_load = false;
					load_entity = false;
				}
			}
			number_of_entities_to_copy = 0;
			selectAllEntites = false;
		}
		
		if (selectAllEntites!=null&&selectAllEntites==true) {
			selection_method = "ALL";
		}
		
		try{
			String sql= "INSERT INTO \"" + schema + "\".tasks (be_id, environment_id, scheduler, delete_before_load," +
					"number_of_entities_to_copy,selection_method,selection_param_value,entity_exclusion_list, task_execution_status, " +
					"task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, task_status, task_title, parameters, refresh_reference_data,replace_sequences, " +
					"source_environment_id, source_env_name, load_entity, task_type, scheduling_end_date, version_ind, retention_period_type, retention_period_value, selected_version_task_name, " +
					"selected_version_datetime, selected_version_task_exe_id,task_globals, " +
					"selected_ref_version_task_exe_id, selected_ref_version_datetime, selected_ref_version_task_name, sync_mode) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING task_id";
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			Db.Row row = db("TDM").fetch(sql,be_id, ((environment_id!=null) ? environment_id: source_environment_id),
					scheduler, delete_before_load,
					number_of_entities_to_copy, selection_method,
					selection_param_value, entity_exclusion_list, "Active",
					username, now, now, username, "Active", task_title,
					parameters, refresh_reference_data, replace_sequences, source_environment_id,
					source_env_name, load_entity, task_type, scheduling_end_date,
					(version_ind!=null? version_ind == true:false), retention_period_type, retention_period_value,
					selected_version_task_name, selected_version_datetime,
					selected_version_task_exe_id, task_globals, selected_ref_version_task_exe_id,
					selected_ref_version_datetime, selected_ref_version_task_name, sync_mode).firstRow();
			Long taskId=Long.parseLong(row.cell(0).toString());
		
			if (refList!=null ) {
				if(refList.size() > 0){
					fnSaveRefTablestoTask(taskId, refList);
				}
			}
		
			if (globals != null ) {
				if(globals.size() > 0 && task_globals!=null && task_globals){
					try {String insertGlobalSql = "INSERT INTO \"" + schema + "\".task_globals (task_id, global_name,global_value) VALUES (?, ?,?)";
						for(Map<String,Object> global:globals){
							db("TDM").execute(insertGlobalSql,taskId, global.get("global_name").toString(), global.get("global_value").toString());
						}
					} catch(Exception e){
						log.error(e.getMessage());
					}
				}
			}
		
			try {
				String activityDesc = "Task " + task_title + " was created";
				fnInsertActivity("create", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}
		
			result.put("id",taskId);
			errorCode="SUCCESS";
			response.put("result",result);
		
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Updates Task. The task update creates a new version of the task and set the status of the previous task version to Inactive.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\r\n" +
			"  \"copy\": false,\r\n" +
			"  \"task_status\": \"complete\",\r\n" +
			"  \"be_id\": 3,\r\n" +
			"  \"environment_id\": 1,\r\n" +
			"  \"source_environment_id\": 1,\r\n" +
			"  \"scheduler\": \"immediate\",\r\n" +
			"  \"delete_before_load\": true,\r\n" +
			"  \"request_of_fresh_data\": true,\r\n" +
			"  \"number_of_entities_to_copy\": 0,\r\n" +
			"  \"selection_method\": \"R\",\r\n" +
			"  \"selection_param_value\": null,\r\n" +
			"  \"entity_exclusion_list\": \"9,8,123\",\r\n" +
			"  \"task_title\": \"taskTitle\",\r\n" +
			"  \"parameters\": null,\r\n" +
			"  \"refresh_reference_data\": true,\r\n" +
			"  \"replace_sequences\": true,\r\n" +
			"  \"source_env_name\": \"env1\",\r\n" +
			"  \"load_entity\": true,\r\n" +
			"  \"task_type\": \"LOAD\",\r\n" +
			"  \"scheduling_end_date\": \"2021-02-04 14:20:59.454\",\r\n" +
			"  \"version_ind\": true,\r\n" +
			"  \"retention_period_type\": \"Days\",\r\n" +
			"  \"retention_period_value\": 0,\r\n" +
			"  \"selected_version_task_name\": \"TaskName\",\r\n" +
			"  \"selected_version_datetime\": \"2021-02-04\",\r\n" +
			"  \"selected_version_task_exe_id\": 0,\r\n" +
			"  \"task_globals\": true,\r\n" +
			"  \"selected_ref_version_task_exe_id\": 0,\r\n" +
			"  \"selected_ref_version_datetime\": \"2021-02-04\",\r\n" +
			"  \"selected_ref_version_task_name\": null,\r\n" +
			"  \"sync_mode\": null,\r\n" +
			"  \"selectAllEntites\": true,\r\n" +
			"  \"refList\": [\r\n" +
			"    {\r\n" +
			"        \"reference_table_name\":\t\"RefT\",\r\n" +
			"        \"logical_unit_name\": \"RefLU\",\r\n" +
			"        \"schema_name\": \"RefSchema\",\r\n" +
			"        \"interface_name\": \"RefInterface\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"        \"reference_table_name\":\t\"RefT2\",\r\n" +
			"        \"logical_unit_name\": \"RefLU2\",\r\n" +
			"        \"schema_name\": \"RefSchema2\",\r\n" +
			"        \"interface_name\": \"RefInterface2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"globals\": [\r\n" +
			"    {\r\n" +
			"      \"global_name\":\"globalName1\",\r\n" +
			"      \"global_value\":\"globalValue1\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"global_name\":\"globalName2\",\r\n" +
			"      \"global_value\":\"globalValue2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"reference\": \"ref\",\r\n" +
			"  \"task_created_by\": \"test\",\r\n" +
			"  \"task_creation_date\": \"2021-02-04 14:20:59.454\"\r\n" +
			"}")
	@webService(path = "task/{taskId}", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 146\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdateTask(@param(required=true) Long taskId, Boolean copy, String task_status, Long be_id, Long environment_id, Long source_environment_id, String scheduler, Boolean delete_before_load, Integer number_of_entities_to_copy, String selection_method, String selection_param_value, String entity_exclusion_list, String task_title, String parameters, Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, Boolean load_entity, String task_type, String scheduling_end_date, Boolean version_ind, String retention_period_type, Integer retention_period_value, String selected_version_task_name, String selected_version_datetime, Integer selected_version_task_exe_id, Boolean task_globals, Integer selected_ref_version_task_exe_id, String selected_ref_version_datetime, String selected_ref_version_task_name, String sync_mode, Boolean selectAllEntites, List<Map<String,Object>> refList, List<Map<String,Object>> globals, String reference, String task_created_by, String task_creation_date) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		Map<String,Object> result=new HashMap<>();
		
		try {
			db("TDM").execute("UPDATE \"" + schema + "\".tasks SET " +
							"task_status=(?) WHERE task_id = " + taskId, copy != null && copy ? task_status : "Inactive");
		
		
			if(refList!=null) {
				Iterator<Map<String, Object>> iter = refList.iterator();
				while (iter.hasNext()) {
					Map<String, Object> ref = iter.next();
					if (ref.get("selected") == null || (boolean) ref.get("selected") != true) {
						iter.remove();
					}
				}
			}
		
			if (reference!=null&&reference.equals("refernceOnly")) {
				if(refList!=null && refList.size() > 0){
					selection_method = "REF";
					delete_before_load = false;
					load_entity = false;
				}
				number_of_entities_to_copy = 0;
				selectAllEntites = false;
			}
		
		
		
			if (selectAllEntites != null && selectAllEntites) {
				selection_method = "ALL";
			}
			String sql = "INSERT INTO \"" + schema + "\".tasks (be_id, environment_id, scheduler, delete_before_load," +
					"number_of_entities_to_copy, selection_method,selection_param_value,entity_exclusion_list, task_execution_status," +
					"task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, task_status, " +
					"task_title, parameters,refresh_reference_data, replace_sequences, source_environment_id, source_env_name, load_entity, task_type, " +
					"scheduling_end_date, version_ind, retention_period_type, retention_period_value, selected_version_task_name, " +
					"selected_version_datetime, selected_version_task_exe_id, task_globals, " +
					"selected_ref_version_task_exe_id, selected_ref_version_datetime, selected_ref_version_task_name, sync_mode) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?) RETURNING task_id";
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			Db.Row row = db("TDM").fetch(sql, be_id, environment_id!=null?environment_id:source_environment_id,
					scheduler,
					delete_before_load,
					number_of_entities_to_copy,
					selection_method,
					selection_param_value,
					entity_exclusion_list,
					"Active",
					copy != null && copy ? username : task_created_by,
					task_creation_date,
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
							.withZone(ZoneOffset.UTC)
							.format(Instant.now()),
					username, "Active", task_title, parameters, refresh_reference_data, replace_sequences,
					source_environment_id, source_env_name, load_entity, task_type,
					scheduling_end_date, version_ind, retention_period_type,
					retention_period_value, selected_version_task_name, selected_version_datetime,
					selected_version_task_exe_id, task_globals, selected_ref_version_task_exe_id,
					selected_ref_version_datetime, selected_ref_version_task_name, sync_mode).firstRow();
		
			Long id = Long.parseLong(row.cell(0).toString());
		
			if (refList!=null ) {
				if(refList.size() > 0){
					fnSaveRefTablestoTask(id, refList);
				}
			}
		
		
			if(globals != null && globals.size() > 0 && task_globals){
				try {
					String insertGlobalSql = "INSERT INTO \"" + schema + "\".task_globals (task_id, global_name,global_value) VALUES (?, ?,?)";
					for(Map<String,Object> global:globals){
						db("TDM").execute(insertGlobalSql,id, global.get("global_name").toString(), global.get("global_value").toString());
					}
				} catch(Exception e){
					log.error(e.getMessage());
				}
			}
		
		
			try {
				String activityDesc = "Task " + task_title + " was updated";
				fnInsertActivity("update", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}
		
			errorCode="SUCCESS";
			result.put("id",id);
			response.put("result",result);
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

    //from logic.TDM
	private static Object fnGetNumberOfMatchingEntities(String whereStmt, String sourceEnvName, Long beID) throws Exception {
		String sourceEnv = !Util.isEmpty(sourceEnvName) ? sourceEnvName : "_dev";
		String getEntitiesSql = generateListOfMatchingEntitiesQuery(beID, whereStmt, sourceEnv);
		Db tdmDB = db("TDM");
		Db.Rows rows = tdmDB.fetch("SELECT COUNT(entity_id) FROM (" + getEntitiesSql + " ) AS final_count");
		return  wrapWebServiceResults("SUCCESS", null, rows.firstValue());
	}
	//end from logic.TDM

	@desc("Calculates the number of entities that matches the selected task's parameters. The calculation is based on the task's Business Entity (BE), source environment, and the Where statement (populated in the request Body) reflecting the selected parameters.\r\n" +
			"\r\n" +
			"Use the ANY command when checking a value of a parameter since the TDM LU parameter tables contain an array of values on each parameter.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\"where\":\"(( '2' = ANY(\\\"BILLING.NO_OF_OPEN_INVOICES\\\") ) AND ( '3' = ANY(\\\"BILLING.SUBSCRIBER_TYPE\\\") ))\"}")
	@webService(path = "businessentity/{beId}/sourceEnv/{src_env_name}/analysiscount", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\"errorCode\":\"SUCCESS\",\"message\":null,\"result\":834}")
	public static Object wsGetAnalysiscountForBusinessEntity(@param(required=true) Long beId, @param(required=true) String src_env_name, String where) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		try {
			Object result = fnGetNumberOfMatchingEntities(where, src_env_name, beId); //TDM
			response.put("result",((Map<String,Object>)result).get("result"));
			response.put("errorCode","SUCCESS");
		} catch (Exception e){
			response.put("errorCode","FAIL");
			response.put("message", e.getMessage());
		}
		return response;
	}


	@desc("Adds Logical Units to the task.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\r\n" +
			"  \"logicalUnits\": [\r\n" +
			"    {\r\n" +
			"      \"lu_id\": \"27\",\r\n" +
			"      \"lu_name\": \"lu1\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_id\": \"8\",\r\n" +
			"      \"lu_name\": \"lu2\"\r\n" +
			"    }\r\n" +
			"  ]\r\n" +
			"}")
	@webService(path = "task/{taskId}/taskname/{taskName}/logicalUnits", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateLogicalUnitsFortask(@param(required=true) Long taskId, @param(required=true) String taskName, List<Map<String,Object>> logicalUnits) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			fnPostTaskLogicalUnits(taskId,logicalUnits);
			try {
				String activityDesc = "LogicalUnits of task " + taskName + " was updated";
				fnInsertActivity("update", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Adds Post Execution Processes to the task.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\r\n" +
			"  \"postexecutionprocesses\": [\r\n" +
			"    {\r\n" +
			"      \"process_id\": 1,\r\n" +
			"      \"process_name\": \"processName\",\r\n" +
			"      \"execution_order\": 2\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"process_id\": 2,\r\n" +
			"      \"process_name\": \"processName2\",\r\n" +
			"      \"execution_order\": 3\r\n" +
			"    }\r\n" +
			"  ]\r\n" +
			"}")
	@webService(path = "task/{taskId}/taskname/{taskName}/postexecutionprocesses", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"postexecutionprocesses\": [\r\n" +
			"    {\r\n" +
			"      \"process_id\": 1,\r\n" +
			"      \"process_name\": \"processName\",\r\n" +
			"      \"execution_order\": 2\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"process_id\": 2,\r\n" +
			"      \"process_name\": \"processName2\",\r\n" +
			"      \"execution_order\": 3\r\n" +
			"    }\r\n" +
			"  ]\r\n" +
			"}")
	public static Object wsCreatePostExecutionProcessesFortask(@param(required=true) Long taskId, @param(required=true) String taskName, List<Map<String,Object>> postexecutionprocesses) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			fnAddTaskPostExecutionProcess(postexecutionprocesses,taskId);
			try {
				String activityDesc = "Post Execution Processes of task " + taskName + " was updated";
				fnInsertActivity("update", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the task's Post Execution Process.")
	@webService(path = "task/{taskId}/postexecutionprocess", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"process_id\": 1,\r\n" +
			"      \"process_name\": \"processName\",\r\n" +
			"      \"task_id\": 145,\r\n" +
			"      \"execution_order\": 2\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"process_id\": 2,\r\n" +
			"      \"process_name\": \"processName2\",\r\n" +
			"      \"task_id\": 145,\r\n" +
			"      \"execution_order\": 3\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTaskPostExecutionProcesses(@param(required=true) Long taskId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String sql = "SELECT * FROM \"" + schema + "\".TASKS_POST_EXE_PROCESS  WHERE task_id =" + taskId;
			Db.Rows rows = db("TDM").fetch(sql);
			List<Map<String,Object>> result=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				result.add(rowMap);
			}
			response.put("result",result);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the task's Logical Units.")
	@webService(path = "task/{taskId}/logicalunits", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"lu_name\": \"luName\",\r\n" +
			"      \"lu_id\": 27,\r\n" +
			"      \"task_id\": 145\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_name\": \"luName2\",\r\n" +
			"      \"lu_id\": 8,\r\n" +
			"      \"task_id\": 145\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTaskLogicalUnits(@param(required=true) Long taskId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String sql = "SELECT * FROM \"" + schema + "\".tasks_logical_units WHERE task_id =" + taskId;
			Db.Rows rows = db("TDM").fetch(sql);
		
			List<Map<String,Object>> result=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				result.add(rowMap);
			}
			response.put("result",result);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets Globals list of a given task.")
	@webService(path = "task/{taskId}/globals", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"global_name\": \"globalName1\",\r\n" +
			"      \"task_id\": 145,\r\n" +
			"      \"global_value\": \"globalValue1\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"global_name\": \"globalName2\",\r\n" +
			"      \"task_id\": 145,\r\n" +
			"      \"global_value\": \"globalValue2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTaskGlobals(@param(required=true) String taskId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String sql = "SELECT * FROM \"" + schema + "\".task_globals WHERE task_globals.task_id = " + taskId;
			Db.Rows rows = db("TDM").fetch(sql);
		
			List<Map<String,Object>> result=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				result.add(rowMap);
			}
			response.put("result",result);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the list of available Reference tables for a task based on the task's Logical Units (LUs). Task type can be either LOAD or EXTRACT.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\"task_type\":\"LOAD\",\"logicalUnits\":[\"Customer\",\"Orders\"]}")
	@webService(path = "task/getReferenceTaskTable", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\"result\":[{\"ref_table_name\":\"CUSTOMER_TYPE\",\"lu_name\":\"Customer\",\"interface_name\":\"CRM_DB\",\"schema_name\":\"public\"}],\"errorCode\":\"SUCCESS\",\"message\":null}")
	public static Object wsGetReferenceTaskTable(List<String> logicalUnits, String task_type, Boolean version_ind) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			/*String luArray="[";
			for(String lu:logicalUnits) luArray+="'"+lu+"',";
			if(luArray.charAt(luArray.length()-1)=='\'')luArray=luArray.substring(0,luArray.length()-1);
			luArray+="]";
			 */
		
			if (task_type.equals("EXTRACT")){
				//Object resultList = com.k2view.cdbms.usercode.lu.k2_ws.TDM.Logic.wsGetRefTablesByLu(luArray);
		
				List<Object> refTablesList = new ArrayList<Object>();
				Map<String,Map<String, String>> trnRefListValues = getTranslationsData("trnRefList");
				List<String> luNamesList = logicalUnits;
		
				Set<String> keys = trnRefListValues.keySet();
				String luName;
				String prevLuName="";
				for(String trnLuKey:keys){
					luName = trnLuKey.substring(0, trnLuKey.indexOf("@") );
					if(luNamesList.contains(luName)) {
						Map<String,Object> refInfo = new HashMap<>();
						refInfo.put("logical_unit_name", luName);
						Set<String> internalKeys = trnRefListValues.get(trnLuKey).keySet();
						String paramValue;
						for(String paramKey:internalKeys) {
							paramValue = trnRefListValues.get(trnLuKey).get(paramKey);
							refInfo.put(paramKey, paramValue);
						}
						refTablesList.add(refInfo);
					}
				}
		
				List<Object> ans=new ArrayList<>();
				for(Object ref:refTablesList){
					((Map<String,Object>)ref).put("reference_table_name", ((Map<String,String>)ref).get("reference_table_name").trim());
					ans.add(ref);
				}
		
				response.put("result", ans);
		
			}
			else if (task_type.equals("LOAD") && version_ind != null && version_ind){
				List<Map<String,Object>> resultList = fnGetRefTableForLoadWithVersion(logicalUnits);
				response.put("result", resultList);
			}
			else{
				Db.Rows rows = fnGetRefTableForLoadWithoutVersion(logicalUnits);
				List<Map<String,Object>> result=new ArrayList<>();
				List<String> columnNames = rows.getColumnNames();
				for (Db.Row row : rows) {
					ResultSet resultSet = row.resultSet();
					Map<String, Object> rowMap = new HashMap<>();
					for (String columnName : columnNames) {
						rowMap.put(columnName, resultSet.getObject(columnName));
					}
					result.add(rowMap);
				}
				response.put("result",result);
			}
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

/*
	@desc("Returns all the Logical Units connected to the given SRC/TAGRET env")
	@webService(path = "sourceenvid/{srcEnvId}/targetendid/{targetEnvId}/logicalUnits", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"environment_product_id\": 3,\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"product_vendor\": null,\r\n" +
			"      \"environment_id\": 4,\r\n" +
			"      \"lu_parent_id\": null,\r\n" +
			"      \"product_status\": \"Active\",\r\n" +
			"      \"lu_dc_name\": null,\r\n" +
			"      \"lu_description\": description\",\r\n" +
			"      \"lu_is_ref\": null,\r\n" +
			"      \"execution_plan_name\": \"epLuName\",\r\n" +
			"      \"last_updated_by\": \"k2view\",\r\n" +
			"      \"lu_parent_name\": null,\r\n" +
			"      \"product_id\": 1,\r\n" +
			"      \"product_description\": null,\r\n" +
			"      \"last_updated_date\": \"2020-12-07 08:14:48.336\",\r\n" +
			"      \"product_version\": \"1\",\r\n" +
			"      \"product_last_updated_date\": \"2021-03-18 17:23:16.622\",\r\n" +
			"      \"product_created_by\": \"k2view\",\r\n" +
			"      \"creation_date\": \"2020-12-07 08:14:48.336\",\r\n" +
			"      \"product_name\": \"PROD\",\r\n" +
			"      \"created_by\": \"k2view\",\r\n" +
			"      \"last_executed_lu\": false,\r\n" +
			"      \"product_versions\": \"1\",\r\n" +
			"      \"product_last_updated_by\": \"K2View\",\r\n" +
			"      \"data_center_name\": \"DC1\",\r\n" +
			"      \"lu_name\": \"luName\",\r\n" +
			"      \"lu_id\": 1,\r\n" +
			"      \"product_creation_date\": \"2020-10-01 08:26:42.899\",\r\n" +
			"      \"status\": \"Active\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetLogicalUnitsForSourceAndTargetEnv(@param(required=true) Long srcEnvId, @param(required=true) Long targetEnvId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			List<Map<String,Object>> result = fnGetLogicalUnitsForSourceAndTargetEnv(targetEnvId,srcEnvId);
			response.put("result",result);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

 */


	@desc("Gets a list of available versions within a given time interval when creating load Data Flux tasks.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\r\n" +
			"  \"entitiesList\": \"1,7,48\",\r\n" +
			"  \"be_id\": 3,\r\n" +
			"  \"source_env_name\": \"ENV1\",\r\n" +
			"  \"fromDate\": \"01.01.2021\",\r\n" +
			"  \"toDate\": \"01.03.2021\",\r\n" +
			"  \"lu_list\": [\r\n" +
			"    {\r\n" +
			"      \"lu_name\":\"PATIENT_LU\",\r\n" +
			"      \"lu_name\":\"PATIENT_VISITS\"\r\n" +
			"    }\r\n" +
			"  ]\r\n" +
			"}")
	@webService(path = "tasks/versionsForLoad", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": [\n    {\n      \"number_of_extracted_entities\": 500,\n      \"version_datetime\": \"2021-05-06 09:58:55.017\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"fabric_execution_id\": \"ee39f7f0-3478-4ab3-a93e-80686d99f599\",\n      \"task_execution_id\": 292,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\",\n      \"root_indicator\": \"Y\"\n    },\n    {\n      \"number_of_extracted_entities\": 500,\n      \"version_datetime\": \"2021-05-06 10:51:06.451\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"fabric_execution_id\": \"a1dac0ee-7a8a-4a32-aa76-9f11a6da8b48\",\n      \"task_execution_id\": 311,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\",\n      \"root_indicator\": \"Y\"\n    },\n    {\n      \"number_of_extracted_entities\": 500,\n      \"version_datetime\": \"2021-05-06 11:32:47.599\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"fabric_execution_id\": \"72d7a444-b732-4505-9d7a-ada8852764a9\",\n      \"task_execution_id\": 332,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\",\n      \"root_indicator\": \"Y\"\n    },\n    {\n      \"number_of_extracted_entities\": 1,\n      \"version_datetime\": \"2021-05-05 11:55:54.937\",\n      \"version_name\": \"ex\",\n      \"version_type\": \"Selected Entities\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"fabric_execution_id\": \"02396fa1-b13f-4312-a762-3a92bfea28f0\",\n      \"task_execution_id\": 279,\n      \"task_id\": 193,\n      \"task_last_updated_by\": \"K2View\",\n      \"root_indicator\": \"Y\"\n    },\n    {\n      \"number_of_extracted_entities\": 2,\n      \"version_datetime\": \"2021-05-06 11:22:51.654\",\n      \"version_name\": \"v1\",\n      \"version_type\": \"Selected Entities\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"fabric_execution_id\": \"567501da-ea55-4fee-ba7c-5359fc733219\",\n      \"task_execution_id\": 331,\n      \"task_id\": 206,\n      \"task_last_updated_by\": \"K2View\",\n      \"root_indicator\": \"Y\"\n    }\n  ],\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
	public static Object wsGetVersionsForLoad(String entitiesList, Long be_id, String source_env_name, String fromDate, String toDate, List<Map<String,Object>> lu_list) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			Db.Rows rows = fnGetVersionsForLoad(entitiesList, be_id, source_env_name, fromDate, toDate, lu_list);
			List<Map<String,Object>> result=new ArrayList<>();
		
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				result.add(rowMap);
			}
		
			response.put("result",result);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Returns the list of all executions of the input task. Returns one summary record for each execution.")
	@webService(path = "task/{taskId}/summary", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"environment_id\": 1,\r\n" +
			"      \"tot_num_of_succeeded_post_executions\": null,\r\n" +
			"      \"task_execution_id\": 1,\r\n" +
			"      \"task_id\": 1,\r\n" +
			"      \"source_environment_id\": 1,\r\n" +
			"      \"version_datetime\": \"2020-10-01 08:29:29.170\",\r\n" +
			"      \"execution_status\": \"failed\",\r\n" +
			"      \"source_env_name\": \"ENV1\",\r\n" +
			"      \"tot_num_of_processed_root_entities\": 0,\r\n" +
			"      \"fabric_environment_name\": null,\r\n" +
			"      \"environment_name\": \"SRC\",\r\n" +
			"      \"tot_num_of_failed_ref_tables\": 0,\r\n" +
			"      \"start_execution_time\": \"1970-01-01 00:00:00.000\",\r\n" +
			"      \"tot_num_of_processed_post_executions\": null,\r\n" +
			"      \"creation_date\": \"2020-10-01 08:29:29.173\",\r\n" +
			"      \"tot_num_of_copied_root_entities\": 0,\r\n" +
			"      \"be_name\": \"BE\",\r\n" +
			"      \"tot_num_of_copied_ref_tables\": 0,\r\n" +
			"      \"update_date\": \"2020-12-30 08:40:17.576\",\r\n" +
			"      \"tot_num_of_failed_post_executions\": null,\r\n" +
			"      \"version_expiration_date\": null,\r\n" +
			"      \"end_execution_time\": \"1970-01-01 00:00:00.000\",\r\n" +
			"      \"tot_num_of_processed_ref_tables\": 0,\r\n" +
			"      \"task_type\": \"EXTRACT\",\r\n" +
			"      \"tot_num_of_failed_root_entities\": 0,\r\n" +
			"      \"task_executed_by\": null\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"environment_id\": 1,\r\n" +
			"      \"tot_num_of_succeeded_post_executions\": null,\r\n" +
			"      \"task_execution_id\": 3,\r\n" +
			"      \"task_id\": 1,\r\n" +
			"      \"source_environment_id\": 1,\r\n" +
			"      \"version_datetime\": \"2020-10-12 20:06:25.487\",\r\n" +
			"      \"execution_status\": \"completed\",\r\n" +
			"      \"source_env_name\": \"ENV1\",\r\n" +
			"      \"tot_num_of_processed_root_entities\": 5,\r\n" +
			"      \"fabric_environment_name\": null,\r\n" +
			"      \"environment_name\": \"SRC\",\r\n" +
			"      \"tot_num_of_failed_ref_tables\": 0,\r\n" +
			"      \"start_execution_time\": \"2020-10-12 20:06:29.000\",\r\n" +
			"      \"tot_num_of_processed_post_executions\": null,\r\n" +
			"      \"creation_date\": \"2020-10-12 20:06:25.539\",\r\n" +
			"      \"tot_num_of_copied_root_entities\": 5,\r\n" +
			"      \"be_name\": \"BE\",\r\n" +
			"      \"tot_num_of_copied_ref_tables\": 0,\r\n" +
			"      \"update_date\": \"2020-10-12 20:06:46.098\",\r\n" +
			"      \"tot_num_of_failed_post_executions\": null,\r\n" +
			"      \"version_expiration_date\": null,\r\n" +
			"      \"end_execution_time\": \"2020-10-12 20:06:31.000\",\r\n" +
			"      \"tot_num_of_processed_ref_tables\": 0,\r\n" +
			"      \"task_type\": \"EXTRACT\",\r\n" +
			"      \"tot_num_of_failed_root_entities\": 0,\r\n" +
			"      \"task_executed_by\": null\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetSummaryTaskHistory(@param(required=true) Long taskId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String query = "select summary.* , envs.environment_name, be.be_name from task_execution_summary as summary" +
					" inner join environments as envs on summary.environment_id = envs.environment_id " +
					"left join business_entities as be on (summary.be_id = be.be_id) where task_id = " + taskId;
			Db.Rows rows = db("TDM").fetch(query);
		
			List<Map<String,Object>> result=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				result.add(rowMap);
			}
		
			response.put("result",result);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets task execution details:\r\n" +
			"\r\n" +
			"> List of copied and Failed Entities.\r\n" +
			"\r\n" +
			"> Task's Logical Units hierarchy tree.\r\n" +
			"\r\n" +
			"> List of copied and failed reference tables.\r\n" +
			"\r\n" +
			"Examples of a request body:\r\n" +
			"\r\n" +
			"> Get the execution details of the root Logical Unit: \r\n" +
			"{taskExecutionId: \"69\"}\r\n" +
			"\r\n" +
			"> Get the execution details of a selected Logical Unit:\r\n" +
			"{taskExecutionId: \"69\", lu_name: \"PATIENT_VISITS\"}\r\n" +
			"\r\n" +
			"> Gets the execution details of a selected Logical Unit and entity ID:\r\n" +
			"{taskExecutionId: \"69\", targetId: \"400\", lu_name: \"PATIENT_VISITS\"}")
	@webService(path = "taskStats", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "1. Task Execution Level (Root Logical Unit)- \r\n" +
			"\r\n" +
			"{\"result\":{\"luTree\":[{\"isRoot\":true,\"test\":true,\"hasChildren\":true,\"collapsed\":true,\"lu_name\":\"PATIENT_LU\",\"task_execution_id\":69,\"count\":1,\"lu_id\":1,\"test1\":true,\"lu_status\":\"completed\",\"selected\":true,\"status\":\"completed\"}],\"data\":{\"Copied entities per execution\":{\"entitiesList\":[{\"sourceId\":\"1\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"1\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"2\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"2\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"3\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"3\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"4\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"4\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"5\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"5\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"6\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"6\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"6\",\"rootTargetId\":\"6\"},{\"sourceId\":\"7\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"7\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"8\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"8\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"}],\"NoOfEntities\":\"8\"},\"Failed entities per execution\":{\"entitiesList\":[],\"NoOfEntities\":\"0\"},\"Copied Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Failed Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Roots Status\":{\"PATIENT_LU\":\"completed\"}}},\"errorCode\":\"SUCCESS\",\"message\":null}\r\n" +
			"\r\n" +
			"2. Logical Unit Level - \r\n" +
			"\r\n" +
			"{\"result\":{\"data\":{\"Copied entities per execution\":{\"entitiesList\":[{\"sourceId\":\"24900\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"24900\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"24901\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"24901\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"24902\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"24902\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"24903\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"24903\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"24913\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24913\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24914\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24914\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24915\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24915\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24916\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24916\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24917\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24917\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24918\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24918\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24919\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24919\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24920\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24920\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24921\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24921\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24925\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24925\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24926\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24926\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24927\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24927\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24928\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24928\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24929\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24929\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24934\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24934\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24935\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24935\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24936\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24936\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24937\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24937\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24938\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24938\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24939\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24939\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24940\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24940\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24959\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"6\",\"targetId\":\"24959\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"6\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"6\",\"rootTargetId\":\"6\"},{\"sourceId\":\"24960\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"6\",\"targetId\":\"24960\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"6\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"6\",\"rootTargetId\":\"6\"},{\"sourceId\":\"24963\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24963\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24964\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24964\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24965\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24965\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24967\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24967\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24969\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24969\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24971\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24971\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24973\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24973\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24974\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24974\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"25072\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25072\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25085\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25085\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25089\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25089\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25092\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25092\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25094\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25094\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25112\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25112\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25114\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25114\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25115\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25115\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25116\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25116\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25120\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25120\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25122\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25122\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25134\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25134\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"400\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"400\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"}],\"NoOfEntities\":\"48\"},\"Failed entities per execution\":{\"entitiesList\":[],\"NoOfEntities\":\"0\"},\"Copied Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Failed Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Roots Status\":{\"PATIENT_LU\":\"completed\"}}},\"errorCode\":\"SUCCESS\",\"message\":null} \r\n" +
			"\r\n" +
			"3. Logical Unit and Entity ID level -\r\n" +
			"\r\n" +
			"{\"result\":{\"data\":{\"Copied entities per execution\":{\"entitiesList\":[{\"sourceId\":\"400\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"400\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"}],\"NoOfEntities\":\"1\"},\"Failed entities per execution\":{\"entitiesList\":[],\"NoOfEntities\":\"0\"},\"Copied Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Failed Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Roots Status\":{\"PATIENT_LU\":\"completed\"}}},\"errorCode\":\"SUCCESS\",\"message\":null}")
	public static Object wsGetTaskStats(Long targetId, Long parentTargetId, String taskExecutionId, String lu_name, Long entityId, String type) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		if (targetId != null || parentTargetId != null) {
			try {
				Map<String, Map> statsData = (Map<String, Map>)((Map<String, Object>) fnGetTDMTaskExecutionStats(taskExecutionId.toString(),
								lu_name, targetId != null ? targetId.toString() : null, "ENTITY",parentTargetId != null ? parentTargetId.toString() : null, 100, "true")).get("result");
				response.put("errorCode", "SUCCESS");
				response.put("message", null);
				HashMap<String, Object> data = new HashMap<>();
				data.put("data", statsData);
				response.put("result", data);
				return response;
			} catch (Exception e) {
				response.put("errorCode", "FAIL");
				response.put("message", e.getMessage());
				return response;
			}
		}
		
		try {
			if (lu_name == null) {
				List<Map<String, Object>> tree = fnGetRootLUs(taskExecutionId.toString());
				if (tree == null || tree.size() == 0) {
					response.put("errorCode", "SUCCESS");
					response.put("message", null);
					response.put("result", new ArrayList<>());
					return response;
				}
		
				tree.get(0).put("selected", true);
				Map<String, Object> taskStatsAns = new HashMap<>();
		
				for (Map<String, Object> root_lu : tree) {
					Map<String, Map> statsData = (Map<String, Map>)((Map<String, Object>) fnGetTDMTaskExecutionStats(taskExecutionId.toString(), root_lu.get("lu_name").toString(),
									null, "ENTITY", null, 100, "true")).get("result");
					Map<String,Object> dataObj=new HashMap<>();
					dataObj.put("data",statsData);
					taskStatsAns.put(root_lu.get("lu_name").toString(), dataObj);
				}
		
				for (String key : taskStatsAns.keySet()) {
					Map<String,Object> statsObj = (Map<String,Object>)taskStatsAns.get(key);
					if (statsObj == null) {
						continue;
					}
					tree.get(0).put("test1",true);
					//statsObj{data:Map<String,Map>}
					Map<String, Map> stats_Data = (Map<String, Map>) statsObj.get("data");
					Map<String, Object> statsDataFailedEntities = null;
					if(stats_Data!=null) statsDataFailedEntities = (Map<String, Object>)stats_Data.get("Failed entities per execution");
					if (stats_Data != null && statsDataFailedEntities != null
							&& Long.parseLong(statsDataFailedEntities.get("NoOfEntities").toString()) > 0){
								tree = fnUpdateFailedLUsInTree(tree, statsDataFailedEntities);
					} else {
						for (Map<String, Object> node : tree) {
							if (node.get("count")!=null && Long.parseLong(node.get("count").toString()) > 0) {
								node.put("hasChildren", true);
								node.put("collapsed", true);
							}
							node.put("isRoot", true);
						}
					}
		
					for (Map<String, Object> node : tree) {
						Map<String, Map> statsData = (Map<String, Map>) statsObj.get("data");
						Map<String, String> statsDateRootsStatus = (Map<String, String>) statsData.get("Roots Status");
						node.put("status", statsDateRootsStatus != null? statsDateRootsStatus.get(node.get("lu_name")) : null);
						node.put("test",true);
					}
				}
		
				Map<String, Object> responseObject = new HashMap<>();
		
				Map<String, Object> lu = (Map<String, Object>) taskStatsAns.get(tree.get(0).get("lu_name"));
				responseObject.put("data", lu.get("data"));
				responseObject.put("luTree", tree);
		
				response.put("result", responseObject);
				response.put("errorCode", "SUCCESS");
				response.put("message", null);
				return response;
			} else {
				Map<String, Map> data = (Map<String, Map>)((Map<String, Object>) fnGetTDMTaskExecutionStats(taskExecutionId.toString(), lu_name,
								entityId!=null?entityId.toString():null, "ENTITY", null, 100, "true")).get("result");
				Map<String,Object> dataObj = new HashMap<>();
				dataObj.put("data",data);
				if (type!=null) {
					HashMap<String,Object> returnedData = new HashMap<>();
					returnedData.put(type, dataObj.get("data")!=null?((Map<String, Map>)dataObj.get("data")).get(type):null);
					dataObj.put("data",returnedData);
					response.put("result", dataObj);
					response.put("errorCode", "SUCCESS");
					response.put("message", null);
					return response;
				}
				response.put("result", dataObj);
				response.put("errorCode", "SUCCESS");
				response.put("message", null);
				return response;
			}
		
		} catch (Exception e) {
			response.put("errorCode", "FAIL");
			response.put("message", e.getMessage());
			return response;
		}
	}



	@desc("Returns the list children LUs of a parent LU in a task execution. This API is invoked when navigating the Logical Units Hierarchy  tab of a selected task execution.")
	@webService(path = "luChildren", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"collapsed\": true,\r\n" +
			"      \"lu_name\": \"PATIENT_VISITS\",\r\n" +
			"      \"task_execution_id\": 161,\r\n" +
			"      \"count\": 0,\r\n" +
			"      \"lu_id\": 12,\r\n" +
			"      \"lu_status\": \"completed\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}\r\n" +
			"Response headers")
	public static Object wsGetLUChildren(Long taskExecutionId, String lu_name) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String query = "select t.task_execution_id, l.lu_id, lu_name, " +
					"(select count(*) from task_Execution_list t where parent_lu_id = l.lu_id and " +
					"t.task_execution_id = " + taskExecutionId + "), " +
					"case when (num_of_failed_entities > 0 or num_of_failed_ref_tables > 0) " +
					"then 'failed' else 'completed' end lu_status from task_Execution_list t, " +
					"(select lu_id, lu_name, lu_parent_name from product_logical_units) l " +
					"where t.task_execution_id =" + taskExecutionId + " and " +
					"l.lu_parent_name = '" + lu_name + "' and t.lu_id = l.lu_id ";
			Db.Rows rows = db("TDM").fetch(query);
		
			//if(true) return rows;
		
			List<Map<String,Object>> result=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				result.add(rowMap);
			}
		
			for (Map<String,Object> node:result) {
				if (node.get("count") !=null && Integer.parseInt(node.get("count").toString()) > 0){
					node.put("hasChildren", true);
				}
				node.put("collapsed", true);
			}
		
			response.put("result",result);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Returns the list of all executed Logical Units and Post Execution Processes of the input task execution id.")
	@webService(path = "task/{taskExeId}/history", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_id\": 2,\r\n" +
			"      \"selected_version_task_name\": null,\r\n" +
			"      \"selected_ref_version_task_name\": null,\r\n" +
			"      \"num_of_copied_entities\": 0,\r\n" +
			"      \"fabric_execution_id\": \"e51b4541-a24b-4f0d-a22c-9294ef7e55d1\",\r\n" +
			"      \"be_last_updated_date\": \"2021-04-19 07:38:03.456\",\r\n" +
			"      \"execution_plan_name\": \"epPATIENT_LU\",\r\n" +
			"      \"product_id\": 1,\r\n" +
			"      \"load_entity\": true,\r\n" +
			"      \"selected_version_task_exe_id\": null,\r\n" +
			"      \"task_created_by\": \"K2View\",\r\n" +
			"      \"product_description\": \"null\",\r\n" +
			"      \"scheduling_end_date\": null,\r\n" +
			"      \"environment_point_of_contact_phone1\": null,\r\n" +
			"      \"product_created_by\": \"K2View\",\r\n" +
			"      \"environment_status\": \"Active\",\r\n" +
			"      \"be_status\": \"Active\",\r\n" +
			"      \"creation_date\": \"2021-04-19 08:09:40.071\",\r\n" +
			"      \"selected_version_datetime\": null,\r\n" +
			"      \"last_executed_lu\": false,\r\n" +
			"      \"task_last_updated_by\": \"K2View\",\r\n" +
			"      \"selected_ref_version_task_exe_id\": null,\r\n" +
			"      \"task_execution_status\": \"Active\",\r\n" +
			"      \"product_versions\": \"1.0,2.0\",\r\n" +
			"      \"product_last_updated_by\": \"K2View\",\r\n" +
			"      \"replace_sequences\": false,\r\n" +
			"      \"end_execution_time\": \"2021-04-19 08:09:48.000\",\r\n" +
			"      \"environment_point_of_contact_last_name\": null,\r\n" +
			"      \"updated_by\": null,\r\n" +
			"      \"clean_redis\": false,\r\n" +
			"      \"environment_point_of_contact_phone2\": null,\r\n" +
			"      \"environment_last_updated_by\": \"K2View\",\r\n" +
			"      \"product_status\": \"Active\",\r\n" +
			"      \"task_id\": 1,\r\n" +
			"      \"be_created_by\": \"K2View\",\r\n" +
			"      \"environment_description\": null,\r\n" +
			"      \"version_datetime\": \"2021-04-19 08:09:40.071\",\r\n" +
			"      \"selected_ref_version_datetime\": null,\r\n" +
			"      \"source_env_name\": \"ENV1\",\r\n" +
			"      \"task_title\": \"Task1\",\r\n" +
			"      \"allow_write\": true,\r\n" +
			"      \"task_status\": \"Inactive\",\r\n" +
			"      \"environment_last_updated_date\": \"2021-05-09 06:11:51.263\",\r\n" +
			"      \"number_of_entities_to_copy\": 1,\r\n" +
			"      \"environment_point_of_contact_first_name\": null,\r\n" +
			"      \"product_creation_date\": \"2021-04-18 09:32:14.981\",\r\n" +
			"      \"task_type\": \"LOAD\",\r\n" +
			"      \"environment_creation_date\": \"2021-04-18 09:32:50.416\",\r\n" +
			"      \"task_executed_by\": \"K2View\",\r\n" +
			"      \"task_last_updated_date\": \"2021-04-18 09:34:13.320\",\r\n" +
			"      \"num_of_failed_ref_tables\": 0,\r\n" +
			"      \"environment_id\": 1,\r\n" +
			"      \"selection_method\": \"R\",\r\n" +
			"      \"lu_parent_id\": null,\r\n" +
			"      \"refresh_reference_data\": false,\r\n" +
			"      \"task_execution_id\": 1,\r\n" +
			"      \"lu_dc_name\": null,\r\n" +
			"      \"refcount\": 0,\r\n" +
			"      \"lu_is_ref\": null,\r\n" +
			"      \"lu_parent_name\": null,\r\n" +
			"      \"process_name\": null,\r\n" +
			"      \"be_last_updated_by\": \"K2View\",\r\n" +
			"      \"retention_period_type\": \"Days\",\r\n" +
			"      \"start_execution_time\": \"1970-01-01 00:00:00.000\",\r\n" +
			"      \"selection_param_value\": null,\r\n" +
			"      \"product_last_updated_date\": \"2021-04-18 14:49:32.536\",\r\n" +
			"      \"product_name\": \"PROD\",\r\n" +
			"      \"num_of_processed_ref_tables\": 0,\r\n" +
			"      \"execution_order\": null,\r\n" +
			"      \"sync_mode\": null,\r\n" +
			"      \"version_expiration_date\": null,\r\n" +
			"      \"entity_exclusion_list\": null,\r\n" +
			"      \"environment_point_of_contact_email\": null,\r\n" +
			"      \"lu_name\": \"PATIENT_LU\",\r\n" +
			"      \"lu_id\": 1,\r\n" +
			"      \"be_description\": \"\",\r\n" +
			"      \"parameters\": null,\r\n" +
			"      \"environment_expiration_date\": null,\r\n" +
			"      \"process_id\": 0,\r\n" +
			"      \"product_vendor\": \"null\",\r\n" +
			"      \"environment_created_by\": \"K2View\",\r\n" +
			"      \"be_creation_date\": \"2021-04-18 09:31:50.712\",\r\n" +
			"      \"allow_read\": true,\r\n" +
			"      \"source_environment_id\": 1,\r\n" +
			"      \"num_of_failed_entities\": 0,\r\n" +
			"      \"scheduler\": \"immediate\",\r\n" +
			"      \"parent_lu_id\": null,\r\n" +
			"      \"lu_description\": null,\r\n" +
			"      \"execution_status\": \"failed\",\r\n" +
			"      \"fabric_environment_name\": null,\r\n" +
			"      \"environment_name\": \"ENV1\",\r\n" +
			"      \"delete_before_load\": false,\r\n" +
			"      \"product_version\": \"1.0\",\r\n" +
			"      \"retention_period_value\": 5,\r\n" +
			"      \"synced_to_fabric\": true,\r\n" +
			"      \"be_name\": \"BE\",\r\n" +
			"      \"version_ind\": false,\r\n" +
			"      \"task_creation_date\": \"2021-04-18 09:34:13.320\",\r\n" +
			"      \"task_globals\": false,\r\n" +
			"      \"num_of_copied_ref_tables\": 0,\r\n" +
			"      \"data_center_name\": \"DC1\",\r\n" +
			"      \"num_of_processed_entities\": 0\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTaskHistory(@param(required=true) Long taskExeId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String query = "SELECT * , " +
					"( SELECT COUNT(*) FROM task_ref_tables WHERE task_ref_tables.task_id = task_execution_list.task_id " +
					"and task_ref_tables.lu_name in (select lu_name from tasks_logical_units " +
					"where  tasks_logical_units.lu_id = task_execution_list.lu_id)) AS refcount " +
					"FROM \"" + schema + "\".task_execution_list " +
					"LEFT JOIN \"" + schema + "\".products " +
					"ON (\"" + schema + "\".task_execution_list.product_id = \"" + schema + "\".products.product_id) " +
					"INNER JOIN \"" + schema + "\".tasks " +
					"ON (\"" + schema + "\".task_execution_list.task_id = \"" + schema + "\".tasks.task_id) " +
					"INNER JOIN \"" + schema + "\".environments " +
					"ON (\"" + schema + "\".task_execution_list.environment_id = \"" + schema + "\".environments.environment_id) " +
					"LEFT JOIN \"" + schema + "\".business_entities " +
					"ON (\"" + schema + "\".task_execution_list.be_id = \"" + schema + "\".business_entities.be_id) " +
					"LEFT JOIN \"" + schema + "\".product_logical_units " +
					"ON (\"" + schema + "\".task_execution_list.lu_id = \"" + schema + "\".product_logical_units.lu_id) " +
					"LEFT JOIN \"" + schema + "\".TASKS_POST_EXE_PROCESS " +
					"ON (\"" + schema + "\".task_execution_list.process_id = \"" + schema + "\".TASKS_POST_EXE_PROCESS.process_id AND \"" + schema + "\".task_execution_list.task_id = \"" + schema + "\".TASKS_POST_EXE_PROCESS.task_id) " +
					"WHERE task_execution_list.task_execution_id = " + taskExeId;
			Db.Rows rows = db("TDM").fetch(query);
		
			List<Map<String,Object>> result=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				result.add(rowMap);
			}
		
			response.put("result",result);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

    //from logic.TDM
	private static Object fnExecutionSummaryReport(String i_taskExecutionId, String i_luName) throws Exception {
		fabric().execute("get TDM.?", i_taskExecutionId);

		String taskType = "" + fabric().fetch("select task_type from tasks limit 1").firstValue();

		Object response;

		if ("LOAD".equalsIgnoreCase(taskType)) {
			if ("ALL".equalsIgnoreCase(i_luName)) {
				//log.info("Creating report for Load Task");
				response = graphit("LoadSummaryReport.graphit");
			} else {
				Map<String, Object> temp = new HashMap<>();
				temp.put("i_luName",i_luName);
				response = graphit("LoadSummaryReportPerLu.graphit", temp);
			}

		} else {
			//log.info("Creating report for Extract Task");
			response = graphit("ExtractSummaryReport.graphit");
		}

		return wrapWebServiceResults("SUCCESS", null, response);
	}
	//end from logic.TDM

	@desc("Gets task summary report.")
	@webService(path = "taskSummaryReport/{executionId}/luName/{luName}", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsTaskSummaryReport(@param(required=true) String executionId, @param(description="Will be populated by 'ALL' to get one unified summary report to all Logical Units of the task execution. Populate this parameter by the Logical Unit name to get a report of a given Logical Unit.", required=true) String luName) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		
		try {
			Object data = fnExecutionSummaryReport(executionId,luName!=null?luName:"ALL");
			response.put("result",((Map<String,Object>)data).get("result"));
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Gets a summary or detailed information of the input batch id (LU execution). This API gets the run mode and a list of batch IDs or only one batch id as input values.\r\n" +
			"The run mode can have the following values:\r\n" +
			"\r\n" +
			" > 'D': detailed execution. Returns detailed information of all entities and their execution status.\r\n" +
			"\r\n" +
			" > 'S': summary information of the execution\r\n" +
			"\r\n" +
			" > 'H': get the batch command.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\"migrateIds\" : [\"387a3b82-18e2-4d45-9482-16dc6ad15385\",\"387a3b82-18e2-4d45-9482-16dc6ad15386\"], \"runModes\" : [\"D\", \"H\", \"S\"]}")
	@webService(path = "migrateStatusWs", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"S\": {\r\n" +
			"        \"columnsNames\": [\r\n" +
			"          \"Status\",\r\n" +
			"          \"Ent./sec (avg.)\",\r\n" +
			"          \"Ent./sec (pace)\",\r\n" +
			"          \"Start time (UTC)\",\r\n" +
			"          \"Duration\",\r\n" +
			"          \"End time (UTC)\",\r\n" +
			"          \"Name\",\r\n" +
			"          \"Succeeded\",\r\n" +
			"          \"Failed\",\r\n" +
			"          \"Added\",\r\n" +
			"          \"Updated\",\r\n" +
			"          \"Unchanged\",\r\n" +
			"          \"Total\",\r\n" +
			"          \"Level\",\r\n" +
			"          \"Remaining dur.\",\r\n" +
			"          \"Remaining\",\r\n" +
			"          \"% Completed\"\r\n" +
			"        ],\r\n" +
			"        \"results\": [\r\n" +
			"          {\r\n" +
			"            \"columns\": {\r\n" +
			"              \"Status\": \"\",\r\n" +
			"              \"Ent./sec (avg.)\": \"7.84\",\r\n" +
			"              \"Added\": 0,\r\n" +
			"              \"Ent./sec (pace)\": \"7.84\",\r\n" +
			"              \"Updated\": 2,\r\n" +
			"              \"Failed\": \"0\",\r\n" +
			"              \"Duration\": \"00:05:05\",\r\n" +
			"              \"Start time (UTC)\": \"2021-05-08 06:46:22\",\r\n" +
			"              \"End time (UTC)\": \"2021-05-08 06:51:27\",\r\n" +
			"              \"End time\": \"2021-05-08 06:51:27\",\r\n" +
			"              \"Name\": \"76b2a141-4f0a-453b-8999-f2bdd66a9b8d\",\r\n" +
			"              \"Succeeded\": \"2\",\r\n" +
			"              \"Total\": \"--\",\r\n" +
			"              \"Level\": \"Node\",\r\n" +
			"              \"Remaining dur.\": \"00:00:00\",\r\n" +
			"              \"Remaining\": \"0\",\r\n" +
			"              \"Start time\": \"2021-05-08 06:46:22\",\r\n" +
			"              \"Unchanged\": 0,\r\n" +
			"              \"% Completed\": \"100\"\r\n" +
			"            }\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"columns\": {\r\n" +
			"              \"Status\": \"\",\r\n" +
			"              \"Ent./sec (avg.)\": \"7.84\",\r\n" +
			"              \"Added\": 0,\r\n" +
			"              \"Ent./sec (pace)\": \"7.84\",\r\n" +
			"              \"Updated\": 2393,\r\n" +
			"              \"Failed\": \"0\",\r\n" +
			"              \"Duration\": \"00:05:05\",\r\n" +
			"              \"Start time (UTC)\": \"2021-05-08 06:46:22\",\r\n" +
			"              \"End time (UTC)\": \"2021-05-08 06:51:27\",\r\n" +
			"              \"End time\": \"2021-05-08 06:51:27\",\r\n" +
			"              \"Name\": \"DC1\",\r\n" +
			"              \"Succeeded\": \"2393\",\r\n" +
			"              \"Total\": \"--\",\r\n" +
			"              \"Level\": \"DC\",\r\n" +
			"              \"Remaining dur.\": \"00:00:00\",\r\n" +
			"              \"Remaining\": \"0\",\r\n" +
			"              \"Start time\": \"2021-05-08 06:46:22\",\r\n" +
			"              \"Unchanged\": 0,\r\n" +
			"              \"% Completed\": \"100\"\r\n" +
			"            }\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"columns\": {\r\n" +
			"              \"Status\": \"DONE\",\r\n" +
			"              \"Ent./sec (avg.)\": \"7.84\",\r\n" +
			"              \"Added\": 0,\r\n" +
			"              \"Ent./sec (pace)\": \"7.84\",\r\n" +
			"              \"Updated\": 2393,\r\n" +
			"              \"Failed\": \"0\",\r\n" +
			"              \"Duration\": \"00:05:05\",\r\n" +
			"              \"Start time (UTC)\": \"2021-05-08 06:46:22\",\r\n" +
			"              \"End time (UTC)\": \"2021-05-08 06:51:27\",\r\n" +
			"              \"End time\": \"2021-05-08 06:51:27\",\r\n" +
			"              \"Name\": \"--\",\r\n" +
			"              \"Succeeded\": \"2393\",\r\n" +
			"              \"Total\": \"2393\",\r\n" +
			"              \"Level\": \"Cluster\",\r\n" +
			"              \"Remaining dur.\": \"00:00:00\",\r\n" +
			"              \"Remaining\": \"0\",\r\n" +
			"              \"Start time\": \"2021-05-08 06:46:22\",\r\n" +
			"              \"Unchanged\": 0,\r\n" +
			"              \"% Completed\": \"100\"\r\n" +
			"            }\r\n" +
			"          }\r\n" +
			"        ]\r\n" +
			"      },\r\n" +
			"      \"D\": [\r\n" +
			"        {\r\n" +
			"          \"Status\": \"COMPLETED\",\r\n" +
			"          \"Error\": \"\",\r\n" +
			"          \"Entity ID\": \"ENV1_26982\",\r\n" +
			"          \"Results\": \"{\\\"Added\\\":0,\\\"Updated\\\":1,\\\"Unchanged\\\":0}\",\r\n" +
			"          \"Node id\": \"76b2a141-4f0a-453b-8999-f2bdd66a9b8d\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"Status\": \"COMPLETED\",\r\n" +
			"          \"Error\": \"\",\r\n" +
			"          \"Entity ID\": \"ENV1_26191\",\r\n" +
			"          \"Results\": \"{\\\"Added\\\":0,\\\"Updated\\\":1,\\\"Unchanged\\\":0}\",\r\n" +
			"          \"Node id\": \"76b2a141-4f0a-453b-8999-f2bdd66a9b8d\"\r\n" +
			"        }\r\n" +
			"],\r\n" +
			"      \"H\": {\r\n" +
			"        \"Migration Command\": \"batch PATIENT_VISITS from TDM using ('SELECT ''''||rel.source_env||''_''||rel.lu_type2_eid||'''' child_entity_id FROM task_execution_entities t, tdm_lu_type_relation_eid rel where t.task_execution_id = ''33'' and t.execution_status = ''completed'' and t.lu_name = ''PATIENT_LU'' and t.lu_name = rel.lu_type_1 and rel.lu_type_2 = ''PATIENT_VISITS'' and rel.version_name = '''' and t.source_env = rel.source_env and t.iid = rel.lu_type1_eid') FABRIC_COMMAND=\\\"sync_instance PATIENT_VISITS.?\\\" WITH ASYNC=true\"\r\n" +
			"      }\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsMigrateStatusWs(Object migrateIds, List<String> runModes) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
		
			Boolean single = false;
		
			if (migrateIds instanceof String){
				List<String> list=new ArrayList<>();
				list.add((String)migrateIds);
				migrateIds = list;
				single = true;
			}
		
		
			Object returnedResults=null;
			List<HashMap<String,Object>> results=new ArrayList<>();
			for (int i = 0; i < ((List<String>)migrateIds).size(); i++) {
				results.add(fnMigrateStatusWs(((List<String>)migrateIds).get(i), runModes));
			}
		
			if (single && results!=null && results.size() == 1) {
				returnedResults = results.get(0);
			}
			else returnedResults=results;
		
			response.put("result",returnedResults);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	static HashMap<String,Object> fnMigrateStatusWs(String migrateId,List<String> runModes) throws Exception{
		HashMap<String,Object> result = new HashMap<>();
		for(String runMode:runModes) {
			Object data = fnGetBatchStats(migrateId, runMode);
			List<HashMap<String,Object>> results=new ArrayList<>();
			HashMap<String,Object> newData=new HashMap<>();
			if (runMode.equals("S")){
				String[] columnName = new String[]{"Status", "Ent./sec (avg.)", "Ent./sec (pace)","Start time (UTC)" , "Duration", "End time (UTC)",
						"Name", "Succeeded","Failed", "Added", "Updated","Unchanged", "Total", "Level", "Remaining dur.", "Remaining", "% Completed"};
				for(HashMap<String,Object> row:(List<HashMap>)((HashMap<String,Object>)data).get("result")){
					row.put("Start time (UTC)",row.get("Start time"));
					row.put("End time (UTC)",row.get("End time"));
					HashMap<String,Object> map=new HashMap<>();
					map.put("columns", row);
					results.add(map);
				}
				newData.put("results", results);
				newData.put("columnsNames", columnName);
				result.put(runMode,newData);
			}
			/*
			else if (runMode.equals("D")){
				String[] columnName = new String[]{"Entity ID", "Error", "Node id", "Results", "Status"};
				for(HashMap<String,Object> row: data){
					JSONObject _results = new JSONObject(row.get("Results").toString());
					if (_results!=null && _results.getBoolean("Added")){
						row.put("Results", "Added");
					}
					else if (_results!=null && _results.getBoolean("Updated")){
						row.put("Results", "Updated");
					}
					else if (_results!=null && _results.getBoolean("Unchanged")){
						row.put("Results", "Unchanged");
					}
					HashMap<String,Object> map=new HashMap();
					map.put("columns", row);
					results.add(map);
				}
				newData.put("results", results);
				newData.put("columnsNames", columnName);
				result.put(runMode,newData);
			}
			 */
			else {
				result.put(runMode,((HashMap<String,Object>)data).get("result"));
			}
		}
		return result;
	}

	private static Object fnGetBatchStats(String i_batchId, String i_runMode) throws Exception {
		return wrapWebServiceResults("SUCCESS", null, fnBatchStats(i_batchId, i_runMode));
	}

    //from logic.TDM
	private static Object fnGetTaskExecSeqVal(String task_execution_id) throws Exception {
		//Sereen - fix : tdm_seq_mapping PG table is deleted so we fetch the data from tdm_seq_mapping fabric table
		//DBExecute(DB_FABRIC, "set sync off", null);
		//DBExecute(DB_FABRIC, "get TDM." + task_execution_id, null);
		ludb().execute( "get TDM." + task_execution_id);
		String sql = "SELECT entity_target_id , lu_type, source_env, table_name, column_name, source_id, target_id, is_instance_id FROM tdm_seq_mapping";
		Db.Rows rows = db(DB_FABRIC).fetch(sql);
		return rows;
	}
	//end from logic.TDM
/*
	@desc("Gets Sequence Report")
	@webService(path = "sequencereport/{task_execution_id}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "[\r\n" +
			"  {\r\n" +
			"    \"ENTITY_TARGET_ID\": \"529\",\r\n" +
			"    \"LU_TYPE\": \"PATIENT_LU\",\r\n" +
			"    \"SOURCE_ENV\": \"ENV1\",\r\n" +
			"    \"TABLE_NAME\": \"ALLERGIES\",\r\n" +
			"    \"COLUMN_NAME\": \"ALLERGY_ID\",\r\n" +
			"    \"SOURCE_ID\": \"25545\",\r\n" +
			"    \"TARGET_ID\": \"47366\",\r\n" +
			"    \"IS_INSTANCE_ID\": \"N\"\r\n" +
			"  },\r\n" +
			"  {\r\n" +
			"    \"ENTITY_TARGET_ID\": \"529\",\r\n" +
			"    \"LU_TYPE\": \"PATIENT_LU\",\r\n" +
			"    \"SOURCE_ENV\": \"ENV1\",\r\n" +
			"    \"TABLE_NAME\": \"ALLERGIES\",\r\n" +
			"    \"COLUMN_NAME\": \"ALLERGY_ID\",\r\n" +
			"    \"SOURCE_ID\": \"25547\",\r\n" +
			"    \"TARGET_ID\": \"47367\",\r\n" +
			"    \"IS_INSTANCE_ID\": \"N\"\r\n" +
			"  },\r\n" +
			"  {\r\n" +
			"    \"ENTITY_TARGET_ID\": \"529\",\r\n" +
			"    \"LU_TYPE\": \"PATIENT_LU\",\r\n" +
			"    \"SOURCE_ENV\": \"ENV1\",\r\n" +
			"    \"TABLE_NAME\": \"ALLERGIES\",\r\n" +
			"    \"COLUMN_NAME\": \"ALLERGY_ID\",\r\n" +
			"    \"SOURCE_ID\": \"25548\",\r\n" +
			"    \"TARGET_ID\": \"47369\",\r\n" +
			"    \"IS_INSTANCE_ID\": \"N\"\r\n" +
			"  }\r\n" +
			"]")
	public static Object wsGetSequenceReport(@param(required=true) String task_execution_id) throws Exception {
		return fnGetTaskExecSeqVal(task_execution_id);
	}

 */

   //from logic.TDM
	private static Object fnExtractRefStats(String i_taskExecutionId, String i_runMode) throws Exception {
		Object rs =null;

		if (i_runMode.equals("S")) {

			//log.info("call to fnGetReferenceSummaryData");
			Map <String, Object> refSummaryStatsBuf = fnGetReferenceSummaryData(i_taskExecutionId);

			// Convert the dates to strings
			refSummaryStatsBuf.forEach((key, value) -> {
				String minStartDate ="";
				String maxEndDate="";
				Map <String, Object> refSummaryStats = (HashMap) value;

				if (refSummaryStats.get("minStartExecutionDate")!=null)
					minStartDate = refSummaryStats.get("minStartExecutionDate").toString();

				if (refSummaryStats.get("maxEndExecutionDate")!= null)
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
	//end from logic.TDM

	@desc("Gets a summary or detailed information of the reference tables execution of a given task execution id.\r\n" +
			"\r\n" +
			"The type is the run mode of the API and can have the following values:\r\n" +
			" \r\n" +
			"> 'D': detailed execution. Returning a detailed information of all reference tables their execution status.\r\n" +
			"\r\n" +
			" > 'S': summary information of the execution")
	@webService(path = "extractrefstats", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "Example 1 - Summary Information: \r\n" +
			"{\r\n" +
			"  \"result\": {\r\n" +
			"    \"PATIENT_LU\": {\r\n" +
			"      \"minStartExecutionDate\": \"Sun May 09 09:08:42 UTC 2021\",\r\n" +
			"      \"maxEndExecutionDate\": \"Sun May 09 09:08:42 UTC 2021\",\r\n" +
			"      \"totNumOfTablesToProcess\": 3,\r\n" +
			"      \"numOfProcessedRefTables\": 3,\r\n" +
			"      \"numOfCopiedRefTables\": 0,\r\n" +
			"      \"numOfFailedRefTables\": 3,\r\n" +
			"      \"numOfProcessingRefTables\": 0,\r\n" +
			"      \"numberOfNotStartedRefTables\": 0\r\n" +
			"    }\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}\r\n" +
			"Example 2 - Detailed Information:\r\n" +
			"{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"start_time\": \"2021-05-09 09:08:42.622\",\r\n" +
			"      \"error_msg\": \"java.lang.NullPointerException\",\r\n" +
			"      \"ref_table_name\": \"REF_GIBRISH\",\r\n" +
			"      \"number_of_processed_records\": null,\r\n" +
			"      \"lu_name\": \"PATIENT_LU\",\r\n" +
			"      \"execution_status\": \"failed\",\r\n" +
			"      \"end_time\": \"2021-05-09 09:08:42.622\",\r\n" +
			"      \"estimated_remaining_duration\": null,\r\n" +
			"      \"number_of_records_to_process\": 0\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"start_time\": \"2021-05-09 09:08:42.613\",\r\n" +
			"      \"error_msg\": \"java.lang.NullPointerException\",\r\n" +
			"      \"ref_table_name\": \"PATIENT_REF\",\r\n" +
			"      \"number_of_processed_records\": null,\r\n" +
			"      \"lu_name\": \"PATIENT_LU\",\r\n" +
			"      \"execution_status\": \"failed\",\r\n" +
			"      \"end_time\": \"2021-05-09 09:08:42.613\",\r\n" +
			"      \"estimated_remaining_duration\": null,\r\n" +
			"      \"number_of_records_to_process\": 0\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"start_time\": \"2021-05-09 09:08:42.617\",\r\n" +
			"      \"error_msg\": \"java.lang.NullPointerException\",\r\n" +
			"      \"ref_table_name\": \"REF_COMPLEX\",\r\n" +
			"      \"number_of_processed_records\": null,\r\n" +
			"      \"lu_name\": \"PATIENT_LU\",\r\n" +
			"      \"execution_status\": \"failed\",\r\n" +
			"      \"end_time\": \"2021-05-09 09:08:42.617\",\r\n" +
			"      \"estimated_remaining_duration\": null,\r\n" +
			"      \"number_of_records_to_process\": 0\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsExtractReferenceStats(String taskExecutionId, String type) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			Object data = fnExtractRefStats(taskExecutionId, type);
			response.put("result",((Map<String,Object>)data).get("result"));
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	//from TDM.logic
	@desc("Gets the hierarchy of a given entity and LU name within the task execution")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": {\n    \"PATIENT_LU\": {\n      \"luName\": \"PATIENT_LU\",\n      \"targetId\": \"1\",\n      \"sourceId\": \"1\",\n      \"entityStatus\": \"completed\",\n      \"parentLuName\": \"\",\n      \"parentTargetId\": \"\",\n      \"children\": [\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"24900\",\n          \"sourceId\": \"24900\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        },\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"24901\",\n          \"sourceId\": \"24901\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        },\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"24902\",\n          \"sourceId\": \"24902\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        },\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"24903\",\n          \"sourceId\": \"24903\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        },\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"400\",\n          \"sourceId\": \"400\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        }\n      ],\n      \"luStatus\": \"completed\"\n    }\n  },\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
	public static Object wsGetTaskExeStatsForEntity(String taskExecutionId, String luName, String targetId) throws Exception {
		String sqlGetEntityData = "select lu_name luName, target_entity_id targetId, entity_id sourceId, " +
				"execution_status luStatus from TDM.task_Execution_link_entities  " +
				"where lu_name <> ? and target_entity_id = ? and entity_id = ?";
		
		String sqlGetParent = "select parent_lu_name, target_parent_id from TDM.task_Execution_link_entities " +
				"where lu_name= ? and target_entity_id = ? and parent_lu_name <> ''";
		
		Map <String, Object> mainOutput = new HashMap<>();
		Map <String, Object> childHierarchyDetails = new HashMap<>();
		Map <String, Object> parentHierarchyDetails = new HashMap<>();
		
		Db.Row entityDetails = null;
		Boolean countChildren = false;
		
		fabric().execute( "get TDM." + taskExecutionId);
		
		//Get the Hierarchy starting from the given entity and below
		childHierarchyDetails = fnGetChildHierarchy(luName, targetId);
		
		String parentLuName = "";
		String parentTargetId = "";
		
		// Get the parent of the given LU, to see if there is a reason to get the ancestors or not
		Db.Row parentRec = fabric().fetch(sqlGetParent, luName, targetId).firstRow();
		
		if (!parentRec.isEmpty()) {
			//log.info("There is a parent: " + parentRec.cell(0));
			parentLuName = "" + parentRec.cell(0);
			parentTargetId = "" + parentRec.cell(1);
		}
		//If the the input entity has parents get the hierarchy above it
		if (parentLuName != null && !"".equals(parentLuName)) {
			//log.info("wsGetTaskExeStatsForEntity - parent Rec: Lu Name: " + parentLuName + ", Parent target ID: " + parentTargetId);
			//Starting for the parent as the details of the input entity is already included in the children part
			//Sending the chilren hierarchy in order to add it to the ancestors as child hierarchy
			parentHierarchyDetails = fnGetParentHierarchy(parentLuName, parentTargetId, childHierarchyDetails);
		} else {// Given inputs are of a root entity
			//log.info("The given LU is a root");
			parentHierarchyDetails =  childHierarchyDetails;
		}
		
		String rootLUName = "" + parentHierarchyDetails.get("luName");
		String rootTargetID = "" + parentHierarchyDetails.get("targetId");
		String rootSourceID = "" + parentHierarchyDetails.get("sourceId");
		//log.info ("wsGetTaskExeStatsForEntity - rootLUName: " + rootLUName + ", rootTargetID: " + rootTargetID + ", rootSourceID: " + rootSourceID);
		
		mainOutput.put(rootLUName, parentHierarchyDetails);
		//If there are other root entities with the same root entity ID get them,
		//they will be added to output as standalone (even if they have their own hierarchy)
		Db.Rows otherRootRecs = fabric().fetch(sqlGetEntityData, rootLUName, rootTargetID, rootSourceID);
		for (Db.Row rootRec : otherRootRecs) {
			Map <String, Object> rootDetails = new HashMap<>();
			String currRootLuName = "" + rootRec.cell(0);
			rootDetails.put("luName", currRootLuName);
			rootDetails.put("targetId", "" + rootRec.cell(1));
		
			//Get instance ID from entity id
			Object[] splitId = fnSplitUID("" + rootRec.cell(2));
			String instanceId = "" + splitId[0];
			rootDetails.put("sourceId", "" + instanceId);
		
			rootDetails.put("entityStatus", "" + rootRec.cell(3));
		
			mainOutput.put(currRootLuName, rootDetails);
		}
		if (otherRootRecs != null) {
			otherRootRecs.close();
		}
		return wrapWebServiceResults("SUCCESS", null, mainOutput);
	}

	private static Object fnResumeTaskExecution(Long task_execution_id) throws Exception {
		Boolean success_ind = true;
		Db.Rows batchIdList = null;
		try {
			//log.info("fnResumeTaskExecution - Starting");
			//TDM 6.0 - Get the list of migration IDs based on task execution ID, instead of getting one migrate_id as input
			batchIdList = db("TDM").fetch("select fabric_execution_id, execution_status, selection_method, l.task_type from task_execution_list l, tasks t " +
					"where task_execution_id = ? and l.task_id = t.task_id and selection_method <> 'REF'" +
					"and fabric_execution_id is not null", task_execution_id);

			db("TDM").execute("UPDATE task_execution_list SET execution_status='running' where fabric_execution_id is not null " +
							"and lower(execution_status) = 'stopped' and task_execution_id = ?",
					task_execution_id);

			// TDM 7, set the status in execution summary to running
			db("TDM").execute("UPDATE task_execution_summary SET execution_status='running' where task_execution_id = ? and execution_status = 'stopped'",
					task_execution_id);
			db("TDM").execute("UPDATE task_execution_list SET execution_status='pending' where fabric_execution_id is null and task_execution_id = ? " +
							"and lower(execution_status) = 'stopped' and task_id in (select task_id from tasks where lower(selection_method) <>'ref')",
					task_execution_id);

			// TDM 5.1- add a reference handling- update the status of the reference tables to 'resume'.
			// The resume of the jobs for the tables will be handled by the new fabric listener user job for the reference copy.

			db("TDM").execute("UPDATE task_execution_list l SET execution_status='pending' where fabric_execution_id is null and task_execution_id = ? " +
							"and (task_execution_id, lu_id) in (select task_execution_id, lu_id from task_ref_exe_stats r,  tasks_logical_units u, task_ref_tables s " +
							"where l.task_execution_id = r.task_execution_id and l.lu_id = u.lu_id " +
							"and l.task_id = u.task_id and u.lu_name = s.lu_name and s.task_ref_table_id = r.task_ref_table_id and s.task_id = l.task_id " +
							"and lower(r.execution_status) = 'stopped')",
					task_execution_id);
			db("TDM").execute("UPDATE task_ref_exe_stats set execution_status= 'resume' where task_execution_id = ? and lower(execution_status) = 'stopped'", task_execution_id);

			// TDM 5.1- cancel the migrate only if the input migration id is not null
			//TDM 6.0 - Loop over the list of migrate IDs
			for (Db.Row batchInfo : batchIdList)
			{
				fabric().execute("delete instance TDM.?",  task_execution_id);
				db("TDM").execute("UPDATE task_execution_list SET synced_to_fabric = FALSE WHERE task_execution_id = ?", task_execution_id);
				fabric().execute("batch_retry '" + batchInfo.get("fabric_execution_id") +"'");
				// TDM 7.1 Fix, resume execution of reference tables.
				//log.info("fnResumeTaskExecution - Resume Reference");
				String taskType = ("" + batchInfo.get("task_type")).toLowerCase();
				SharedLogic.fnTdmCopyReference(String.valueOf(task_execution_id), taskType);

			}
		}
		catch (Exception e){
			success_ind = false;
			log.error("wsResumeTaskExecution: " + e);

		} finally {
			if (batchIdList != null) {
				batchIdList.close();
			}
		}

		return wrapWebServiceResults((success_ind ? "SUCCESS" : "FAIL"), null, success_ind);
	}


	@desc("Resumes the stopped task execution")
	@webService(path = "resumeMigratWS", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsResumeMigratWS(Long taskExecutionId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			Object data = fnResumeTaskExecution(taskExecutionId);
			response.put("result", ((Map<String,Object>)data).get("result"));
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	private static Object fnStopTaskExecution(Long task_execution_id) throws Exception {
		Db.Rows batchIdList = null;
		//log.info("fnStopTaskExecution - Starting");
		try {
			//TDM 6.0 - Get the list of migration IDs based on task execution ID, instead of getting one migrate_id as input
			batchIdList = db("TDM").fetch("select fabric_execution_id, execution_status, l.lu_id, lu_name, task_type " +
					"from task_execution_list l, tasks_logical_units u where task_execution_id = ? " +
					"and fabric_execution_id is not null and UPPER(execution_status) IN " +
					"('RUNNING','EXECUTING','STARTED','PENDING','PAUSED','STARTEXECUTIONREQUESTED') " +
					"and l.task_id = u.task_id and l.lu_id = u.lu_id" , task_execution_id);

			db("TDM").execute("UPDATE task_execution_list SET execution_status='stopped' where task_execution_id = ? and execution_status not in ('completed', 'failed')",
					task_execution_id);
			// TDM 5.1- add a reference handling- update the status of the reference tables to 'stopped'.
			// The cancellation of the jobs for the tables will be handled by the new fabric listener user job for the reference copy.

			db("TDM").execute("UPDATE task_ref_exe_stats set execution_status='stopped', number_of_processed_records = 0 where task_execution_id = ? " +
					"and execution_status not in ('completed', 'failed')", task_execution_id);

			// TDM 7, set the execution summary to stopped also
			db("TDM").execute("UPDATE task_execution_summary SET execution_status='stopped' where task_execution_id = ? and execution_status not in ('completed', 'failed')",
					task_execution_id);

			// TDM 5.1- cancel the migrate only if the input migration id is not null
			//TDM 6.0 - Loop over the list of migrate IDs
			for (Db.Row batchInfo : batchIdList)
			{
				String fabricExecID = "" + batchInfo.get("fabric_execution_id");
				String taskType = ("" + batchInfo.get("task_type")).toLowerCase();
				Long luID = (Long) batchInfo.get("lu_id");
				String luName = "" + batchInfo.get("lu_name");
				String taskExecutionID = "" + task_execution_id;
				ludb().execute("cancel batch '" + fabricExecID +"'");
				// TDM 7.1 Fix, stop execution of reference tables.
				//log.info("fnStopTaskExecution - Stopping the reference Handling for task_execution_id: " + task_execution_id + ", task_type: " + taskType);
				SharedLogic.fnTdmCopyReference(String.valueOf(task_execution_id),taskType);

				if (luID > 0 && ("extract".equals(taskType))) {
					//log.info("wsStopTaskExecution - Updating task_execution_entities");
					fnTdmUpdateTaskExecutionEntities(taskExecutionID, luID, luName);
				}
			}
			return wrapWebServiceResults("SUCCESS", null, null);
		}
		catch (Exception e){
			log.error("wsStopTaskExecution", e);
			return wrapWebServiceResults("FAIL", null, null);

		} finally {
			if(batchIdList != null) {
				batchIdList.close();
			}
		}
	}
	
	@desc("Stops the task execution of the input task execution id.")
	@webService(path = "cancelMigratWS", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsCancelMigratWS(Long taskExecutionId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			Object data = fnStopTaskExecution(taskExecutionId);
			response.put("result", ((Map<String,Object>)data).get("result"));
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Starts an execution of a given task. The force parameter indicates if the execution should ignore a failure of the task's environment connections validation. If the 'force' parameter is set to 'true', then the execution ignores the validation failure and executes the task. If the 'force' parameter is set to 'false' and the environment validation fails, the execution is not initiated.")
	@webService(path = "task/{taskId}/forced/{forced}/startTask", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": {\n    \"taskExecutionId\": 48\n  },\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
	public static Object wsStartTask(@param(required=true) Long taskId, @param(description="true or false", required=true) Boolean forced) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		
		try {
			Long count=(Long)fnIsTaskRunning(taskId);
			if(count>0) throw new Exception("Task already running");
			fnIsTaskActive(taskId);
			fnTestTaskInterfaces(taskId,forced);
		
			List<Map<String,Object>> taskExecutions = fnGetActiveTaskForActivation(taskId);
			if (taskExecutions == null || taskExecutions.size() == 0) {
				throw new Exception("Failed to execute Task");
			}
		
			Long taskExecutionId = (Long)fnGetNextTaskExecution(taskId);
				if ((taskExecutions.get(0).get("selection_method")!=null&&(Long)taskExecutions.get(0).get("refcount")!=null)&&taskExecutions.get(0).get("selection_method").toString().equals("REF") ||
						(Long)taskExecutions.get(0).get("refcount") > 0) {
					fnSaveRefExeTablestoTask((Long)taskExecutions.get(0).get("task_id"), taskExecutionId);
				}
		
			fnStartTaskExecutions(taskExecutions, taskExecutionId);
			fnCreateSummaryRecord(taskExecutions.get(0), taskExecutionId);
		
			/*
			try {
				String activityDesc = "Execution list of task " + taskName + " was updated";
				fnInsertActivity("update", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}
			 */
		
			Map<String,Object> map=new HashMap<>();
			map.put("taskExecutionId",taskExecutionId);
			response.put("result",map);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Holds (pause) the execution of the input task")
	@webService(path = "task/{taskId}/holdTask", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsHoldTask(@param(required=true) Long taskId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String sql="UPDATE \"" + schema + "\".tasks SET task_execution_status = \'onHold\' WHERE \"" + schema + "\".tasks.task_id = " + taskId + "RETURNING \"" + schema + "\".tasks.task_title";
			Db.Row row = db("TDM").fetch(sql).firstRow();
			Object task_name = row.cell(0);
			try {
				String activityDesc = "task # " + task_name + " was Holded";
				fnInsertActivity("update", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}
			response.put("result", new HashMap<>());
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	//from TDM.logic
	private static Map<String,Object> fnCheckMigrateStatusForEntitiesList(String entities_list, String task_execution_id, String lu_list) throws Exception {
		Map <String, Object> entity_mig_status = new LinkedHashMap<String, Object>();
		String [] entities_list_arr = entities_list.split(",");
		String [] lu_list_arr = lu_list.split(",");
		String entities_list_for_qry = "";
		String lu_list_for_qry = "";

		//create a string with added single quotes to each entity in the entities list + preliminary mark every entity in the list as exists,
		//since the entity was already validated against the root LU in the GUI
		for (int i=0; i< entities_list_arr.length; i++) {
			entity_mig_status.put(entities_list_arr[i].trim(),"true");
			entities_list_for_qry += "'" + entities_list_arr[i].trim() + "',";
		}
		// remove last ,
		entities_list_for_qry = entities_list_for_qry.substring(0,entities_list_for_qry.length()-1);

		for (int i=0; i< lu_list_arr.length; i++) {
			lu_list_for_qry += "'" + lu_list_arr[i].trim() + "',";
		}
		// remove last ,
		lu_list_for_qry = lu_list_for_qry.substring(0,lu_list_for_qry.length()-1);

		fabric().execute("GET TDM.?", task_execution_id);

		String getStatusesSql = "select distinct be_root_entity_id from task_execution_link_entities " +
				"where be_root_entity_id in (" + entities_list_for_qry + ") and lu_name in (" + lu_list_for_qry + ") and execution_status <> 'completed'";

		//log.info("wsCheckMigrateStatusForEntitiesList - Query: " + getStatusesSql);

		Db.Rows getStatuses = fabric().fetch(getStatusesSql);
		//Only entities that failed on any of the LUs will be returned, therefore for all the returned entities set the status to false
		for (Db.Row entityStatus : getStatuses) {
			entity_mig_status.put("" + entityStatus.cell(0), "false");
		}

		return wrapWebServiceResults("SUCCESS", null, entity_mig_status);
	}
	//end from TDM.logic


	@desc("This API is invoked when creating a Data Flux load task to validate the selected version. It validates the entity list of the Data Flux load task and checks if all entities have been successfully migrated into Fabric by the selected version. Failed entities are marked with 'false' and successfully migrated entities are marked with 'true' indication. If some entities are marked with 'false', an error message is given to the user to update the load task and remove the failed entities from the entity list.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\r\n" +
			"  \"entitlesList\": \"1,2,7\",\r\n" +
			"  \"taskExecutionId\": \"38\",\r\n" +
			"  \"luList\": \"Customer, Billing, Orders\"\r\n" +
			"}")
	@webService(path = "checkMigrateStatusForEntitiesList", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object CheckMigrateStatusForEntities(String entitlesList, String taskExecutionId, String luList) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			Object data = fnCheckMigrateStatusForEntitiesList(entitlesList, taskExecutionId, luList).get("result");
			response.put("result",data);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}



	@desc("Activates a paused task (the task is set on-hold)")
	@webService(path = "task/{taskId}/activateTask", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\"result\":{},\"errorCode\":\"SUCCESS\",\"message\":null}")
	public static Object wsActivateTask(@param(required=true) Long taskId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String sql = "UPDATE \"" + schema + "\".tasks SET task_execution_status = \'Active\' WHERE \"" + schema + "\".tasks.task_id = " + taskId + "RETURNING \"" + schema + "\".tasks.task_title";
			Db.Row row = db("TDM").fetch(sql).firstRow();
			Object taskTitle= row.cell(0);
		
			try {
				String activityDesc = "task # " + taskTitle + " was activated";
				fnInsertActivity("update", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}
		
			response.put("result",new HashMap<>());
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
		
		
	}


	@desc("Gets available versions of the selected reference tables when creating Data Flux load tasks.\r\n" +
			"Example of request body: \r\n" +
			"{\"source_env_name\":\"ENV1\",\"fromDate\":\"2021-04-19T21:00:00.005Z\",\"toDate\":\"2021-05-20T20:59:59.005Z\",\"refList\":[\"MEDICATION_REFERENCE\",\"PATIENT_REF\"]}")
	@webService(path = "task/getVersionReferenceTaskTable", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": [\n    {\n      \"version_datetime\": \"2021-05-06 10:21:49.913\",\n      \"version_name\": \"v1\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"3a05d7fd-cf20-4805-a4fe-b550110a48b0\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"task_execution_id\": 296,\n      \"task_id\": 206,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 10:21:49.915\",\n      \"version_name\": \"v1\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"0d940278-ee1c-4e3b-b06b-de63c5741a3c\",\n      \"lu_name\": \"PATIENT_VISITS\",\n      \"task_execution_id\": 296,\n      \"task_id\": 206,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 10:17:49.182\",\n      \"version_name\": \"v1\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"6dda444d-6411-495d-b071-d81852b5f3e4\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"task_execution_id\": 294,\n      \"task_id\": 206,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 09:58:55.017\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"bfca5f37-31c8-45eb-a64f-be5b5160f93c\",\n      \"lu_name\": \"PATIENT_VISITS\",\n      \"task_execution_id\": 292,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 09:58:55.017\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"ee39f7f0-3478-4ab3-a93e-80686d99f599\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"task_execution_id\": 292,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 10:51:06.451\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"2035621e-0a06-4f30-8fc7-1f0400067b7e\",\n      \"lu_name\": \"PATIENT_VISITS\",\n      \"task_execution_id\": 311,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\"\n    }\n  ],\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
	public static Object wsGetVersioningReferenceTaskTable(List<String> refList, String source_env_name, String fromDate, String toDate) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			List<Map<String,Object>> referenceData = fnGetVersionForLoadRef(refList, source_env_name, fromDate, toDate);
			response.put("result",referenceData);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the list of reference table included in a given task.")
	@webService(path = "task/refsTable/{task_id}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"task_ref_table_id\": 1,\r\n" +
			"      \"ref_table_name\": \"Ref2\",\r\n" +
			"      \"lu_name\": \"RefLU\",\r\n" +
			"      \"interface_name\": \"RefInterface\",\r\n" +
			"      \"task_id\": 28,\r\n" +
			"      \"schema_name\": \"RefSchema\",\r\n" +
			"      \"update_date\": \"2021-04-20 11:06:42.926\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"task_ref_table_id\": 2,\r\n" +
			"      \"ref_table_name\": \"RefT2\",\r\n" +
			"      \"lu_name\": \"RefLU2\",\r\n" +
			"      \"interface_name\": \"RefInterface2\",\r\n" +
			"      \"task_id\": 28,\r\n" +
			"      \"schema_name\": \"RefSchema2\",\r\n" +
			"      \"update_date\": \"2021-04-20 11:06:42.937\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object getTaskReferenceTable(@param(required=true) Long task_id) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String sql = "SELECT * FROM \"" + schema + "\".task_ref_tables where task_id = " + task_id;
			Db.Rows rows = db("TDM").fetch(sql);
		
			List<Map<String,Object>> referenceTableData=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				referenceTableData.add(rowMap);
			}
		
			response.put("result",referenceTableData);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}



	static Boolean fnTestTaskInterfaces(Long task_id, Boolean forced) throws Exception {
		if (task_id !=null && forced == true) return true;
		fnTestEnvTargetInterfaces(task_id, forced);
		fnTestEnvSourceInterfaces(task_id);
		return true;
	}

	static Boolean fnTestEnvTargetInterfaces(Long taskId,Boolean forced) throws Exception {
		List<Map<String, Object>> taskData;
		try {
			taskData = fnGetTaskEnvData(taskId, false);
		} catch(Exception e){
			throw new Exception("Failed to get Task data for target env");
		}
		if (taskData.get(0).get("task_type").equals("EXTRACT")) return true;

		try {
			return fnTestInterfacesForEnvProduct(taskData.get(0).get("fabric_environment_name")!=null?taskData.get(0).get("fabric_environment_name").toString():null);
		} catch (Exception e){
			if(e.getMessage().indexOf("interfaceFailed;")==0){
				throw new Exception("The test connection of "+ e.getMessage().substring(16) + " failed. Please check the connection details of target environment " + taskData.get(0).get("environment_name"));
			}
			else throw new Exception("Failed to test target env interfaces");
		}
	}

	static Boolean fnTestInterfacesForEnvProduct(String source) throws Exception{
		Map<String, String> data;
		try {
			data = (Map<String, String>) ((Map<String,Object>) fnTestConnectionForEnv(source)).get("result");
		} catch (Exception e){
			throw new Exception("Failed to get interfaces for env from fabric");
		}

		List<String> errorInterfaces=new ArrayList<>();
		for (Map.Entry<String,String> entry : data.entrySet()) {
			if (data.get(entry.getKey()).equals("false"))
				errorInterfaces.add(entry.getKey());
		}

		if (errorInterfaces.size() == 0){
		  	return true;
		}
		else throw new Exception("interfaceFailed;" + errorInterfaces.toString());
	}


	static List<Map<String,Object>> fnGetTaskEnvData(Long task_id,Boolean source) throws Exception {
		String sql = "SELECT environments.fabric_environment_name, * FROM \"" + schema + "\".tasks " +
				"INNER JOIN \"" + schema + "\".tasks_logical_units " +
				"ON (\"" + schema + "\".tasks.task_id = \"" + schema + "\".tasks_logical_units.task_id) " +
				"INNER JOIN \"" + schema + "\".product_logical_units " +
				"ON (\"" + schema + "\".product_logical_units.lu_id = \"" + schema + "\".tasks_logical_units.lu_id ) " +
				"INNER JOIN \"" + schema + "\".environments " +
				(source ?
						"ON (\"" + schema + "\".environments.environment_id = \"" + schema + "\".tasks.source_environment_id ) " :
						"ON (\"" + schema + "\".environments.environment_id = \"" + schema + "\".tasks.environment_id ) "  ) +
				"INNER JOIN \"" + schema + "\".environment_products " +
				"ON (\"" + schema + "\".environment_products.status = \'Active\' " +
				"AND \"" + schema + "\".environment_products.product_id = \"" + schema + "\".product_logical_units.product_id " +
				(source ?
						"AND (\"" + schema + "\".environment_products.environment_id = \"" + schema + "\".tasks.source_environment_id )) " :
						"AND (\"" + schema + "\".environment_products.environment_id = \"" + schema + "\".tasks.environment_id )) "  ) +
				"WHERE tasks.task_id = " + task_id;
		Db.Rows rows = db("TDM").fetch(sql);

		List<Map<String,Object>> result=new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		return result;
	}

	static Boolean fnTestEnvSourceInterfaces(Long task_id) throws Exception {
		List<Map<String, Object>> taskData=null;
		try {
			taskData = fnGetTaskEnvData(task_id, true);
		} catch (Exception e){
			throw new Exception("Failed to get task source env data");
		}

		if (taskData!=null) {
			for (int i = 0; i < taskData.size(); i++) {
				try {
					fnTestInterfacesForEnvProduct(taskData.get(i).get("fabric_environment_name").toString());
				} catch (Exception e){
					if(e.getMessage().indexOf("interfaceFailed;")==0) {
						String errMessage = "The test connection of " + e.getMessage().substring(16) + " failed. Please check the connection details of source environment " + taskData.get(0).get("environment_name");
						//if (taskData.get("task_type").toString().equals("EXTRACT"))  throw new Exception(errMessage);
						try {
							return fnTestTaskEnvGlobals(task_id, (Long)taskData.get(0).get("environment_id"));
						} catch (Exception exc){
							throw new Exception(errMessage);
						}
					}
				}
			}
		}
		return true;
	}

	static Boolean fnTestTaskEnvGlobals(Long task_id,Long environment_id) throws Exception{
		Map<String, Object> data=null;
		try {
			data = fnGetTaskEnvGlobals(task_id, environment_id);
		}catch(Exception e){
			throw new Exception("Failed to get globals for task");
		}
		if(data==null) throw new Exception("tdm_set_sync_off doesn't exist in task/env globals");

		Map<String,Object> tdmSetGlobalTask=null;
		for(Map<String,Object> global:(List<Map<String,Object>>)data.get("globals")){
			if(global.get("global_name")!=null&& ((String)global.get("global_name")).equals("tdm_set_sync_off")){
				tdmSetGlobalTask=global;
				break;
			}
		}

		if (tdmSetGlobalTask!=null){
			if (tdmSetGlobalTask.get("global_value")!=null&&((String)tdmSetGlobalTask.get("global_value")).equals("true")){
				return true;
			}
			else{
				throw new Exception("tdm_set_sync_off is not true in task globals");
			}
		}


		Map<String,Object> tdmSetGlobalEnv=null;
		for(Map<String,Object> global:(List<Map<String,Object>>)data.get("env_globals")){
			if(global.get("global_name")!=null&& ((String)global.get("global_name")).equals("tdm_set_sync_off")){
				tdmSetGlobalEnv=global;
				break;
			}
		}

		if (tdmSetGlobalEnv!=null){
			if (tdmSetGlobalEnv.get("global_value")!=null&&((String)tdmSetGlobalEnv.get("global_value")).equals("true")){
				return true;
			}
			else{
				throw new Exception("tdm_set_sync_off is not true in env globals");
			}
		}

		throw new Exception("tdm_set_sync_off doesn't exist in task/env globals");
	}

	static Map<String,Object> fnGetTaskEnvGlobals(Long task_id, Long env_id) throws Exception{
		Map<String,Object> ans=new HashMap<>();

		String query = "SELECT * FROM \"" + schema + "\".task_globals " +
				"WHERE task_globals.task_id = " + task_id;
		Db.Rows rows=db("TDM").fetch(query);

		List<Map<String,Object>> result=new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}

		for(Map<String,Object> global:result){
			global.put("global_name", ((String)global.get("global_name")).toLowerCase());
			global.put("global_value", ((String)global.get("global_value")).toLowerCase());
		}

		ans.put("globals",result);

		query = "SELECT * FROM \"" + schema + "\".tdm_env_globals " +
				"WHERE tdm_env_globals.environment_id = " + env_id;

		Db.Rows envGlobalsrows = db("TDM").fetch(query);

		List<Map<String,Object>> envResult=new ArrayList<>();
		columnNames = envGlobalsrows.getColumnNames();
		for (Db.Row row : envGlobalsrows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			envResult.add(rowMap);
		}

		for(Map<String,Object> global:envResult){
			global.put("global_name", ((String)global.get("global_name")).toLowerCase());
			global.put("global_value", ((String)global.get("global_value")).toLowerCase());
		}

		ans.put("env_globals",envResult);
		return ans;
	}

	static Object fnIsTaskRunning(Long taskId) throws Exception {
		String sql = "SELECT count(*) FROM \"" + schema + "\".task_execution_list " +
				"WHERE task_id = " + taskId + " AND " +
				"(lower(execution_status) <> \'failed\' AND lower(execution_status) <> \'completed\' AND " +
		"lower(execution_status) <> \'stopped\' AND lower(execution_status) <> \'killed\')";
		Db.Rows rows = db("TDM").fetch(sql);
		return rows.firstRow().cell(0);
	}

	static Boolean fnIsTaskActive(Long taskId) throws Exception {
		String query = "SELECT * FROM \"" + schema + "\".tasks " +
				"WHERE task_id = " +taskId + " AND " +
				"task_status = \'Active\'";
		Db.Rows rows = db("TDM").fetch(query);
		if (!rows.firstRow().isEmpty()) {
			return true;
		} else {
			throw new Exception("This task was changed and is currently inactive. Please refresh the page first to execute the task.");
		}
	}

	static List<Map<String,Object>> fnGetActiveTaskForActivation(Long taskId) throws Exception{
		String clientQuery = "SELECT *, " +
				"( SELECT COUNT(*) FROM task_ref_tables WHERE task_ref_tables.task_id = tasks.task_id ) AS refcount "+
				"FROM \"" + schema + "\".tasks " +
				"INNER JOIN \"" + schema + "\".tasks_logical_units " +
				"ON (\"" + schema + "\".tasks.task_id = \"" + schema + "\".tasks_logical_units.task_id) " +
				"INNER JOIN \"" + schema + "\".product_logical_units " +
				"ON (\"" + schema + "\".product_logical_units.lu_id = \"" + schema + "\".tasks_logical_units.lu_id ) " +
				"INNER JOIN \"" + schema + "\".environment_products " +
				"ON (\"" + schema + "\".environment_products.status = \'Active\' " +
				"AND \"" + schema + "\".environment_products.product_id = \"" + schema + "\".product_logical_units.product_id " +
				"AND (\"" + schema + "\".environment_products.environment_id = \"" + schema + "\".tasks.environment_id " +
				"OR (\"" + schema + "\".tasks.environment_id IS NULL " +
				"AND \"" + schema + "\".environment_products.environment_id = \"" + schema + "\".tasks.source_environment_id ))) " +
				"WHERE \"" + schema + "\".tasks.task_id = " + taskId;
		log.error(clientQuery);
		Db.Rows rows = db("TDM").fetch(clientQuery);

		List<Map<String,Object>> executions=new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> execution = new HashMap<>();
			for (String columnName : columnNames) {
				execution.put(columnName, resultSet.getObject(columnName));
			}
			execution.put("process_id", 0);
			executions.add(execution);
		}

		try{
			List<Map<String,Object>> data=fnGetTaskPostExecutionProcesses(taskId);
			Map<String,Object> execution = new HashMap(executions.get(0));
			execution.put("lu_id", 0);
			execution.put("product_id", 0);
			execution.put("product_version", 0);
			for(Map<String,Object> item:data) {
				Map<String, Object> newItem = new HashMap<>(execution);
				newItem.put("process_id", item.get("process_id"));
				newItem.put("process_name", item.get("process_name"));
				newItem.put("execution_order", item.get("execution_order"));
				executions.add(newItem);
			}
		} catch(Exception e){
			log.error(e.getMessage());
		}

		return executions;
	}

	static List<Map<String,Object>> fnGetTaskPostExecutionProcesses(Long taskId) throws Exception {
		String query = "SELECT * FROM \"" + schema + "\".TASKS_POST_EXE_PROCESS  WHERE task_id =" + taskId;
		Db.Rows rows = db("TDM").fetch(query);

		List<Map<String,Object>> result=new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		return result;
	}

	static Object fnGetNextTaskExecution(Long taskId) throws Exception {
		String query = "SELECT nextval('tasks_task_execution_id_seq')";
		Db.Rows rows = db("TDM").fetch(query);
		List<String> columnNames = rows.getColumnNames();
		Db.Row row=rows.firstRow();
		if (!row.isEmpty()) {
			return row.cell(0);
		}
		return null;
	}

	static void fnSaveRefExeTablestoTask(Long task_id,Long taskExecutionId) throws Exception{
		List<Map<String,Object>> refs = (List<Map<String,Object>>) fnGetTaskReferenceTable(task_id);
		List<Map<String,Object>>  refData = refs;
		if (refData.size() == 0){
			return;
		}
		for(Map<String,Object> ref:refData) {
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());

			String query = "INSERT INTO \"" + schema + "\".task_ref_exe_stats (task_id,task_execution_id,task_ref_table_id, ref_table_name, update_date, execution_status) " +
					"VALUES (?, ?, ?, ?, ?, ?)";
			db("TDM").execute(query,
					task_id,
					taskExecutionId,
					ref.get("task_ref_table_id"),
					ref.get("ref_table_name"),
					now,
					"pending");
		}

	}

	static Object fnGetTaskReferenceTable(Long task_id) throws Exception{
			String query = "SELECT * FROM \"" + schema + "\".task_ref_tables where task_id = " + task_id;
			Db.Rows rows = db("TDM").fetch(query);
			List<Map<String,Object>> result=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				result.add(rowMap);
			}
			return result;
	}

	static void fnStartTaskExecutions(List<Map<String,Object>> taskExecutions,Long taskExecutionId) throws Exception{
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());
			for(Map<String,Object> entry:taskExecutions){
				String query = "INSERT INTO \"" + schema + "\".task_execution_list " +
						"(task_id, task_execution_id, creation_date, be_id, environment_id, product_id, product_version, lu_id, " +
						"data_center_name ,execution_status,last_executed_lu,parent_lu_id,source_env_name, task_executed_by, task_type, version_datetime, source_environment_id, process_id) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
				db("TDM").execute(query,
						entry.get("task_id"),
						taskExecutionId,
						now,
						entry.get("be_id"),
						entry.get("environment_id"),
						entry.get("product_id"),
						entry.get("product_version"),
						entry.get("lu_id"),
						entry.get("data_center_name"),
						"Pending",
						entry.get("last_executed_lu"),
						entry.get("lu_parent_id"),
						entry.get("source_env_name"),
						username,
						entry.get("task_type"),
						now,
						entry.get("source_environment_id"),
						entry.get("process_id"));
			}
	}

	static void fnCreateSummaryRecord(Map<String,Object> taskExecution, Long taskExecutionId) throws Exception{
			Map<String,Object> entry = taskExecution;
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());

			String query = "INSERT INTO \"" + schema + "\".task_execution_summary " +
					"(task_execution_id, task_id , task_type, creation_date, be_id, environment_id, execution_status, start_execution_time, end_execution_time," +
					" tot_num_of_processed_root_entities, tot_num_of_copied_root_entities, tot_num_of_failed_root_entities, tot_num_of_processed_ref_tables, tot_num_of_copied_ref_tables," +
					" tot_num_of_failed_ref_tables, source_env_name, source_environment_id, fabric_environment_name, task_executed_by, version_datetime, version_expiration_date, update_date) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			db("TDM").execute(query,
					taskExecutionId,
					entry.get("task_id"),
					entry.get("task_type"),
					now,
					entry.get("be_id"),
					entry.get("environment_id"),
					"Pending",
					entry.get("start_execution_time"),
					entry.get("end_execution_time"),
					entry.get("tot_num_of_processed_root_entities"),
					entry.get("tot_num_of_copied_root_entities"),
					entry.get("tot_num_of_failed_root_entities"),
					entry.get("tot_num_of_processed_ref_tables"),
					entry.get("tot_num_of_copied_ref_tables"),
					entry.get("tot_num_of_failed_ref_tables"),
					entry.get("source_env_name"),
					entry.get("source_environment_id"),
					entry.get("fabric_environment_name"),
					entry.get("task_executed_by"),
					entry.get("version_datetime"),
					entry.get("version_expiration_date"),
					entry.get("update_date"));
		}

	static void fnInsertActivity(String action,String entity,String description) throws Exception{
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
		String userId=username;
		String sql= "INSERT INTO \"" + schema + "\".activities " +
				"(date, action, entity, user_id, username, description) " +
				"VALUES (?, ?, ?, ?, ?, ?)";
		db("TDM").execute(sql,now,action,entity,userId,username,description);
	}

	static void fnAddTaskPostExecutionProcess(List<Map<String,Object>> postExecutionProcesses,Long taskId) throws Exception{
		String sql = "DELETE FROM \"" + schema + "\".TASKS_POST_EXE_PROCESS WHERE \"" + schema + "\".TASKS_POST_EXE_PROCESS.task_id = " + taskId;
		db("TDM").execute(sql);
		if(postExecutionProcesses==null) return;
		for(Map<String,Object> postExecutionProcess:postExecutionProcesses) {
			db("TDM").execute("INSERT INTO \"" + schema + "\".TASKS_POST_EXE_PROCESS (task_id, process_id,process_name, execution_order) VALUES (?,?,?,?)",
					taskId,
					postExecutionProcess.get("process_id"),
					postExecutionProcess.get("process_name"),
					postExecutionProcess.get("execution_order"));
		}
	}

	static void fnPostTaskLogicalUnits(Long taskId,List<Map<String,Object>> logicalUnits) throws Exception{
		String sql="DELETE FROM \"" + schema + "\".tasks_logical_units WHERE \"" + schema + "\".tasks_logical_units.task_id = " + taskId;
		db("TDM").execute(sql);
		if(logicalUnits!=null) {
			for (Map<String, Object> logicalUnit : logicalUnits) {
				db("TDM").execute("INSERT INTO \"" + schema + "\".tasks_logical_units (task_id, lu_id,lu_name) VALUES ( ?, ?, ?)",
						taskId, logicalUnit.get("lu_id"), logicalUnit.get("lu_name"));
			}
		}
	}

	static void fnSaveRefTablestoTask(Long taskId, List<Map<String,Object>> refList){
		try{
			for(Map<String,Object> ref:refList){
				String sql="INSERT INTO \"" + schema + "\".task_ref_tables (task_id, ref_table_name,lu_name, schema_name, interface_name, update_date) VALUES (?, ?, ?, ?, ?, ?)";
				db("TDM").execute(sql,
						taskId, ref.get("reference_table_name")!=null?ref.get("reference_table_name"):ref.get("ref_table_name"),
						ref.get("logical_unit_name")!=null?ref.get("logical_unit_name"):ref.get("lu_name"),
						ref.get("schema_name"),
						ref.get("interface_name"),
						DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
								.withZone(ZoneOffset.UTC)
								.format(Instant.now()));
			}
		} catch(Exception e){
			log.error(e.getMessage());
		}
	}
	static Db.Rows fnGetRefTableForLoadWithoutVersion(List<String> logicalUnits) throws Exception{
		String lus="";
		for(String lu:logicalUnits) lus+="'"+lu+"',";
		lus=lus.substring(0,lus.length()-1);
		String sql= "Select distinct ref.lu_name, ref.ref_table_name, ref.schema_name, ref.interface_name " +
				"from TASK_REF_TABLES ref, TASK_REF_EXE_STATS exe, task_execution_list l " +
				"Where ref.task_ref_table_id  = exe.task_ref_table_id " +
				"And ref.lu_name in (" + lus +  ") " +
				"And exe.execution_status = \'completed\' " +
				"and exe.task_execution_id = l.task_execution_id " +
				"and lower(l.execution_status) = \'completed\' " +
				"and l.version_expiration_date is null;";
		Db.Rows rows = db("TDM").fetch(sql);
		return rows;
	}

	static List<Map<String,Object>> fnGetRefTableForLoadWithVersion(List<String> logicalUnits) throws Exception{
		String lus="";
		for(String lu:logicalUnits) lus+="'"+lu+"',";
		lus=lus.substring(0,lus.length()-1);
		String sql= "Select distinct ref.lu_name, ref.ref_table_name, ref.schema_name, ref.interface_name , exe.execution_status , exe.task_execution_id "+
				"from task_ref_tables ref, task_ref_exe_stats exe , task_execution_list l " +
				"Where ref.ref_table_name  = exe.ref_table_name " +
				"And ref.lu_name in (" + lus + ") " +
				"And exe.execution_status = \'completed\' " +
				"and exe.task_execution_id = l.task_execution_id " +
				"and lower(l.execution_status) = \'completed\' " +
				"and l.version_expiration_date > CURRENT_TIMESTAMP AT TIME ZONE \'UTC\'";
		Db.Rows rows = db("TDM").fetch(sql);

		List<Map<String,Object>> result=new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}

		for(Map<String,Object> row:result){
			String id = row.get("lu_name") + "_" + row.get("ref_table_name") + "_" +
					row.get("schema_name") + "_" + row.get("interface_name");
			row.put("tId", id);
		}


		for(int i=0;i<result.size();i++) {
			for(int j=i+1;j<result.size();j++) {
				if(result.get(i).get("tId").toString().equals(result.get(j).get("tId").toString())){
					result.remove(j); j--;
				}
			}
		}



		/*
		List<String> resutWithoutDuplicates = result.stream()
				.distinct()
				.collect(Collectors.toList());
		 */
		return result;
	}


	static List<Map<String,Object>> fnGetVersionForLoadRef(List<String> refList,String source_env_name,String fromDate,String toDate) throws Exception{

		String query = "SELECT t.task_title as version_name, t.task_id, t.task_last_updated_by, l.task_execution_id, l.fabric_execution_id,  " +
				"CASE when t.selection_method= \'ALL\' then  \'ALL\' " +
				"when t.selection_method= \'REF\' then \'REF\' " +
				"else \'Selected Entities\' END version_Type, " +
				"l.version_datetime, lu.lu_name " +
				"FROM tasks t, task_execution_list l, tasks_logical_units lu, " +
				"(select  array_agg(lower(e.ref_table_name)) ref_list, array_agg(distinct lower(t.lu_name))  lu_list, task_execution_id " +
				"from task_ref_exe_stats e, task_ref_tables t where e.task_ref_table_id = t.task_ref_table_id and e.execution_status = \'completed\' " +
				"group by task_execution_id) ref " +
				"where lower(t.task_Type) = \'extract\'  " +
				"and t.task_id = l.task_id " +
				"and t.source_env_name = \'"+  source_env_name + "\' " +
				"and lower(l.execution_status) = \'completed\' " +
				"and l.version_datetime >= \'" + fromDate +  "\' " +
				"and l.version_datetime <= \'" + toDate +  "\' " +
				"and l.version_expiration_date > CURRENT_TIMESTAMP AT TIME ZONE \'UTC\' " +
				"and l.task_execution_id = ref.task_execution_id " +
				"and l.lu_id = lu.lu_id and l.task_id = lu.task_id ";

		for(String ref:refList){
			query = query + "and lower(\'" + ref.toLowerCase() + "\') = ANY(ref_list) ";
		}
		Db.Rows rows = db("TDM").fetch(query);

		List<Map<String,Object>> result=new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		return result;
	}


	static List<Map<String, Object>> fnGetLogicalUnitsForSourceAndTargetEnv(Long targetEnvId,Long srcEnvId) throws Exception {

		List<Map<String, Object>> result = new ArrayList<>();
		String query = "SELECT * FROM \"" + schema + "\".product_logical_units " +
				"INNER JOIN \"" + schema + "\".products " +
				"ON (\"" + schema + "\".product_logical_units.product_id = \"" + schema + "\".products.product_id AND \"" + schema + "\".products.product_status = \'Active\') " +
				"INNER JOIN \"" + schema + "\".environment_products " +
				"ON (\"" + schema + "\".product_logical_units.product_id = \"" + schema + "\".environment_products.product_id AND \"" + schema + "\".environment_products.status = \'Active\') " +
				"WHERE environment_id = " + srcEnvId + " OR environment_id = " + targetEnvId;
		Db.Rows rows = db("TDM").fetch(query);

		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}


		for (int i = 0; i < result.size(); i++) {
			Map<String, Object> row = result.get(i);
			searchEqual: for (int j = i + 1; j < result.size(); j++) {
				Map<String, Object> otherRow = result.get(j);
				if(otherRow.get("lu_name").equals(row.get("lu_name"))){
					for (String columnName : columnNames) {
						if (row.get(columnName)==null && otherRow.get(columnName)==null) continue;
						if (row.get(columnName)==null || otherRow.get(columnName)==null) continue searchEqual;
						if (!"lu_name".equals(columnName) && (!otherRow.get(columnName).toString().equals(row.get(columnName).toString())))
							continue searchEqual;
					}
					result.remove(j); j--;
				}
			}
		}



		return result;
	}

		static Db.Rows fnGetVersionsForLoad(String entitiesList, Long be_id, String source_env_name, String fromDate,String toDate,List<Map<String,Object>> lu_list) throws Exception{
		String clientQuery = "";
		String logicalUnitList="";
		for(Map<String,Object> lu:lu_list){
			logicalUnitList+=("'" + lu.get("lu_name") + "',");
		}
		if (logicalUnitList != "") logicalUnitList = logicalUnitList.substring(0, logicalUnitList.length() - 1);

		String logicalUnitListEqual = "";
		for(Map<String,Object> lu:lu_list){
			logicalUnitListEqual = logicalUnitListEqual + " and lower('" + lu.get("lu_name") + "') = any(lu_list) ";
		}

		clientQuery = "with lu_list as (select l.task_execution_id, array_agg(lower(lu.lu_name)) lu_list " +
				"from task_execution_list l, product_logical_units lu where l.lu_id = lu.lu_id and lower(l.execution_status) = 'completed'  " +
				"group by task_execution_id) " +
				"select distinct t1.task_title version_name, t1.task_id, t1.task_last_updated_by, " +
				"CASE when t1.selection_method='ALL' then  'ALL' else  'Selected Entities' END version_Type, " +
				"l1.num_of_copied_entities number_of_extracted_entities, l1.version_datetime , l1.task_execution_id, tlu.lu_name, l1.fabric_execution_id, " +
				"CASE when plu.lu_parent_id is null then 'Y' else 'N' END root_indicator " +
				"from task_execution_list l1, tasks t1, tasks_logical_units tlu, product_logical_units plu where  " +
				"(t1.task_title, t1.task_id, l1.version_datetime, l1.task_execution_id, l1.lu_id) in  " +
				"(SELECT distinct t.task_title as version_name, t.task_id, l.version_datetime , l.task_execution_id, l.lu_id FROM tasks t, task_execution_list l, lu_list  " +
				" where lower(t.task_Type) = 'extract' and t.task_id = l.task_id "+
				"and t.source_env_name = '" + source_env_name +  "' " +
				"and lower(l.execution_status) = 'completed' " +
				"and l.version_datetime::date >= '" + fromDate +  "' " +
				"and l.version_datetime::date <= '" + toDate +  "' " +
				"and l.version_expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC' "+
				"and l.lu_id in (select lu_id from tasks_logical_units u " +
				"where l.task_id = u.task_id and u.lu_name in " +
				"(" + logicalUnitList + "))" +
				"and l.task_execution_id = lu_list.task_execution_id" +
				logicalUnitListEqual + ")" +
				"and t1.task_id = l1.task_id and lower(l1.execution_status) = 'completed' " +
				"and l1.task_id = tlu.task_id and l1.lu_id = tlu.lu_id " +
				"and l1.lu_id = plu.lu_id " +
				"and (plu.lu_parent_id is not null or l1.num_of_copied_entities > 0) ";

		if (entitiesList != null) {
			if (entitiesList.length() > 0) {
				String[] numberOfEntitiesArray = entitiesList.split(",");
				clientQuery = clientQuery +
						"and ((t1.selection_method='ALL' or " + be_id + " != plu.be_id) or (";
				for (int i = 0; i < numberOfEntitiesArray.length; i++) {
					clientQuery = clientQuery + " ('" + numberOfEntitiesArray[i] + "' = ANY(string_to_array(selection_param_value, ',')))";
					if (i < numberOfEntitiesArray.length - 1) {
						clientQuery = clientQuery + " and ";
					}
				}
				clientQuery += "))";
			}
		}
		clientQuery = clientQuery + "order by task_id, task_execution_id;";
		Db.Rows rows=db("TDM").fetch(clientQuery);
		return rows;
	}


	static List<Map<String,Object>> fnGetRootLUs(String taskExecutionId) throws Exception{
		String sql = "select t.task_execution_id, lu_name ,l.lu_id, " +
				"(select count(*) from task_Execution_list t " +
				"where parent_lu_id = l.lu_id and t.task_execution_id = \'" + taskExecutionId + "\')," +
				"case when (num_of_failed_entities>0 or num_of_failed_ref_tables> 0) " +
				"then \'failed\' else \'completed\' end lu_status from task_Execution_list t, " +
				"(select lu_id, lu_name from product_logical_units) l " +
				"where t.task_execution_id =\'" + taskExecutionId + "\' and " +
				"t.parent_lu_id is null and t.lu_id = l.lu_id";
		Db.Rows rows = db("TDM").fetch(sql);

		List<Map<String,Object>> result = new ArrayList<>();
		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			result.add(rowMap);
		}
		return result;
	}

	static List<Map<String,Object>> fnUpdateFailedLUsInTree(List<Map<String,Object>> rootLUs, Map<String,Object> failedEntities){
		List<Map<String,Object>> tree = new ArrayList<>();

		Object entitiesList =  failedEntities.get("entitiesList");
		if(failedEntities!=null&&((List)entitiesList).size()>0) {
			for (Map<String, Object> entity : (List<Map<String, Object>>) entitiesList) {
				List<Object> fullPathError = (List<Object>) entity.get("Full Error Path");
				tree = fnBuildTreeFromFullPathError(fullPathError, tree);
			}
		}

		Map<String,Object> found = null;
		for(Map<String,Object> rootLU:rootLUs){
			for(Map<String,Object> e:tree){
				if(e.get("lu_name").toString().equals(rootLU.get("lu_name").toString()))
					found=e; break;
			}

			if (found!=null) {
				found.put("isRoot", true);
				found.put("count", rootLU.get("count"));
				found.put("errorInPath", true);
			}
			else {
				rootLU.put("isRoot",true);
				tree.add(rootLU);
			}
		}

		for(Map<String,Object> node:tree){
			fnTreeIterate(node);
		}
		return tree;
	}

	static void fnTreeIterate(Map<String,Object> current){
		List<Map<String,Object>> currentChildren=(List<Map<String,Object>>) current.get("children");
		if (currentChildren==null || currentChildren.size() == 0){
			current.put("collapsed", true);
			if (Long.parseLong(current.get("count").toString()) > 0) {
				current.put("hasChildren", true);
			}
			else{
				current.put("hasChildren", false);
			}
			return;
		}
		for (int i = 0, len = currentChildren.size(); i < len; i++) {
			fnTreeIterate(currentChildren.get(i));
		}
	}

	static List<Map<String,Object>> fnBuildTreeFromFullPathError(List<Object> list,List<Map<String,Object>> roots){
		Map<String,Integer> map = new HashMap<>();
		Map<String,Object> node;

		for (int i = 0; i < list.size(); i += 1) {
			map.put(((Map<String, Object>)list.get(i)).get("luName").toString(), i); // initialize the map
			((Map<String, Object>)list.get(i)).put("children", fnGetNodeChildren(roots,list,i)); // initialize the children
		}

		for (int i = 0; i < list.size(); i += 1) {
			node = ((Map<String, Object>)list.get(i));

			if (node.get("parentLuName") != null  && !node.get("parentLuName").toString().equals("")) {
				// if you have dangling branches check that map[node.parentId] exists
				List<Map<String,Object>> children = (List<Map<String,Object>>)((Map<String, Object>)list.get(map.get(node.get("parentLuName")))).get("children");

				Map<String,Object> found=null;
				for(Map<String,Object> e:children){
					if(e.get("lu_name").toString().equals(node.get("luName"))){
						found=e;
						break;
					}
				}

				if (found==null){
					HashMap<String,Object> nodeMap=new HashMap<>();
					nodeMap.put("lu_name",node.get("luName"));
					nodeMap.put("children",node.get("children")!=null? node.get("children") : new ArrayList<>());
					nodeMap.put("collapsed",node.get("false"));
					nodeMap.put("hasChildren",node.get("true"));
					//Map<String,Object> parentLu = list.get(map.get(node.get("parentLuName")));
					//List<HashMap<String,Object>> parentLuChildren = (List<HashMap<String,Object>>) parentLu.get("children");
					children.add(nodeMap);
				}
			} else {
				Map<String,Object> found=null;
				for(Map<String,Object> e:roots){
					if(e.get("lu_name").toString().equals(node.get("luName"))){
						found=e;
						break;
					}
				}
				if (found==null){
					HashMap<String,Object> nodeMap=new HashMap<>();
					nodeMap.put("lu_name",node.get("luName"));
					nodeMap.put("children",node.get("children")!=null? node.get("children") : new ArrayList<>());
					nodeMap.put("collapsed",node.get("false"));
					nodeMap.put("hasChildren",node.get("true"));
					roots.add(nodeMap);
				}
			}
		}

		node = roots.get(0);
		while (node!=null) {
			List<Map<String,Object>> nodeChildren = (List<Map<String,Object>>)node.get("children");
			if (nodeChildren.size() > 0 && list.size() > 1){
				node = nodeChildren.get(0);
			}
			else{
				node.put("lu_status", "failed");
				node = null;
			}
		}
		return roots;
	}

	static List<Map<String,Object>> fnGetNodeChildren(List<Map<String,Object>> roots,List<Object> list,int index){
		List<Map<String,Object>> treeNode = roots;
		for (int i = 0;i <= index; i++){
			Map<String,Object> treeNodeMap=null;
			for(Map<String,Object> e:treeNode){
				if(e.get("lu_name").toString().equals(((Map<String,Object>)list.get(i)).get("luName"))) treeNodeMap = e;
			}
			if (treeNodeMap==null) return new ArrayList<>();
			treeNode = treeNodeMap.get("children")!=null?(List<Map<String,Object>>)treeNodeMap.get("children"):new ArrayList<>();
		}
		return treeNode!=null? treeNode:new ArrayList<>();
	}



	// from TDM.logic.java


	private static Object fnTestConnectionForEnv(String env) throws Exception {
		Log log = Log.a(com.k2view.cdbms.usercode.lu.k2_ws.TDM_Tasks.Logic.class);
		
		if(Util.isEmpty(env)){
			env = "_dev";
		}
		fabric().execute("set environment='" + env + "';");
		
		Map<String,String> connResMap= new HashMap<>();
		Db.Rows dbSources = fabric().fetch("list DB_SOURCES");
		
		for (Db.Row source : dbSources) {
			Boolean active = Boolean.valueOf("" + source.cell(1));
			String iface = "" + source.cell(0);
			if(active){
		
				String connRes = "" + fabric().fetch( "test_connection DbInterface=?", iface).firstRow().cell(2);
				connResMap.put(iface, connRes);
			}else{
				connResMap.put(iface, ", this interface is not active");
			}
		
		}
		if (dbSources != null) dbSources.close();
		
		return wrapWebServiceResults("SUCCESS", null, connResMap);
	}


	@desc("Returns the statistics of the given task execution id and given LU name and other parameters")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	private static Object fnGetTDMTaskExecutionStats(String taskExecutionId, String luName, String luEntityId, String luIdType, String luParentId, Integer entitiesArraySize, String displayErrorPath) throws Exception {
		//Tali- 4-Dec-18- fix the queries- select distinct the combination of be_root_entity_id + TARGET_ROOT_ENTITY_ID when selecting the number of copied/failed entities. This is required to support the clone of one entity X times by a task

		//log.info("wsGetTDMTaskExecutionStats - Inputs:");
		//log.info("taskExecutionId: <" + taskExecutionId + ">, luName: <" + luName + ">, luEntityId: <" + luEntityId + ">, luIdType: <" +
		//		luIdType + ">, luParentId: <" + luParentId + ">, entitiesArraySize: <" + entitiesArraySize + ">, displayErrorPath: <" + displayErrorPath + ">");
		boolean isRootLu = true;
		Map <String, Map> Map_Outer = new LinkedHashMap<>();

		List <Object> copiedEntitiesList =  new ArrayList<>();
		List <Object> failedEntitiesList =  new ArrayList<>();
		List <Object> copiedRefEntitiesList =  new ArrayList<>();
		List <Object> failedRefEntitiesList =  new ArrayList<>();

		Map <String, Object> mapInnerCopiedBuf = new HashMap <>();
		Map <String, Object> mapInnerFailedBuf = new HashMap <>();
		Map <String, Object> mapInnerCopiedRefBuf = new HashMap <>();
		Map <String, Object> mapInnerFailedRefBuf = new HashMap <>();
		Map <String, String> mapRootsStatus = new HashMap <>();

		String sqlSelect = "select ENTITY_ID as sourceId, TARGET_ENTITY_ID as targetId, BE_ROOT_ENTITY_ID as rootSourceId, " +
				"TARGET_ROOT_ENTITY_ID as rootTargetId, PARENT_LU_NAME as parentLuName, PARENT_ENTITY_ID as parentSourceId, " +
				"TARGET_PARENT_ID as parentTargetId, " +
				"Case when EXECUTION_STATUS ='completed' then 'Copied' else 'Failed' end as copyEntityStatus, " +
				"Case when ROOT_ENTITY_STATUS <> 'completed' then 'Failed' else 'Copied' end as copyHierarchyStatus, " +
				"LU_NAME as luName " +
				"from TDM.TASK_EXECUTION_LINK_ENTITIES t1 where ";

		String sqlSelectOrder = " order by TARGET_ENTITY_ID";
		String sqlSelectCnt = "select count(1) from TDM.TASK_EXECUTION_LINK_ENTITIES t1 where ";

		fabric().execute("get TDM.?", taskExecutionId);
		String entity_id_alone ="";
		if (entitiesArraySize == null) entitiesArraySize = 100;
		//Check if the given LU is a root LU
		String sqlCheckParent = "SELECT PARENT_LU_NAME FROM TDM.TASK_EXECUTION_LINK_ENTITIES WHERE LU_NAME = ? LIMIT 1";
		String parentLuName = "" + fabric().fetch(sqlCheckParent,luName).firstValue();
		if(!("".equals(parentLuName))) {
			isRootLu = false;
		}
		//log.info("wsGetTDMTaskExecutionStats - isRootLu: " + isRootLu + ", luIdType: " + luIdType);
		//***** Entities lists *****//
		//

		String sqlCompEntities = "";
		Db.Rows compEntitiesBuf = null;
		String sqlCompCount = "";
		String compEntitiesCnt = "";

		String sqlFailedEntities = "";
		Db.Rows failedEntitiesBuf = null;
		String sqlFailedCount = "";
		String failedEntitiesCnt = "";

		List<String> luEntityIdList = new ArrayList<>();

		// TDM 6.0 - Check if the parent LU name is given or not
		// Get Completed entities

		if ("ENTITY".equals(luIdType)) {//If looking only for reference fo directly to reference section
			if (isRootLu) { //If root LU

				//log.info("wsGetTDMTaskExecutionStats - Handling Root Entity");

				//lu Entity ID of the root is not given then set the query to get all entities of the given root LU
				if ( (luEntityId == null || luEntityId.isEmpty())) {

					//log.info("wsGetTDMTaskExecutionStats - luEntityId of the root is not provided, retrieving it");

					sqlCompEntities = sqlSelect +
							"lu_name = ? and root_entity_status = 'completed' " +
							sqlSelectOrder + " limit ?";

					compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, entitiesArraySize);
					//log.info("wsGetTDMTaskExecutionStats - After getting the copied root entities");
					sqlCompCount = sqlSelectCnt +
							"lu_name = ? and root_entity_status = 'completed'";

					compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName).firstValue();
					//log.info("wsGetTDMTaskExecutionStats - After getting the count of copied root entities");

					sqlFailedEntities = sqlSelect +
							"lu_name = ? and root_entity_status <> 'completed' " +
							sqlSelectOrder + " limit ?";

					failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, entitiesArraySize);
					//log.info("wsGetTDMTaskExecutionStats - After getting the failed root entities");
					sqlFailedCount = sqlSelectCnt +
							"lu_name = ? and root_entity_status <> 'completed'";

					failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName).firstValue();
					//log.info("wsGetTDMTaskExecutionStats - After getting the count of failed root entities");
				} else {// luEntityId is given

					sqlCompEntities = sqlSelect +
							"lu_name = ? and root_entity_status = 'completed' and target_entity_id = ? " +
							sqlSelectOrder + " limit ?";

					compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, luEntityId, entitiesArraySize);

					sqlCompCount = sqlSelectCnt +
							"lu_name = ? and root_entity_status = 'completed' and target_entity_id = ?";

					compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luEntityId).firstValue();

					sqlFailedEntities = sqlSelect +
							"lu_name = ? and target_entity_id = ? and root_entity_status <> 'completed' " +
							sqlSelectOrder + " limit ?";

					failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luEntityId, entitiesArraySize);

					sqlFailedCount = sqlSelectCnt +
							"lu_name = ? and target_entity_id = ? and root_entity_status <> 'completed' ";

					failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luEntityId).firstValue();
				}
			} else {//if not a root LU
				//log.info("The LU is not a root LU");
				if ( !(luEntityId == null || luEntityId.isEmpty()) ) {//If entity ID is given and looking for entities and not reference
					//log.info("luEntityId provided: " + luEntityId);
					sqlCompEntities = sqlSelect +
							"LU_NAME = ? and target_entity_id = ? and execution_status = 'completed' and root_entity_status = 'completed' " +
							sqlSelectOrder;

					compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, luEntityId);

					sqlCompCount = sqlSelectCnt +
							"LU_NAME = ? and target_entity_id = ? and execution_status = 'completed' and root_entity_status = 'completed' ";

					compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luEntityId).firstValue();

					sqlFailedEntities = sqlSelect +
							"LU_NAME = ? and target_entity_id = ? and (ifNull(execution_status, 'failed') <> 'completed' " +
							"or ifNull(root_entity_status, 'failed') <> 'completed') " +
							sqlSelectOrder;

					failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luEntityId);

					sqlFailedCount = sqlSelectCnt +
							"LU_NAME = ? and target_entity_id = ? and (ifNull(execution_status, 'failed') <> 'completed' " +
							"or ifNull(root_entity_status, 'failed') <> 'completed') ";

					failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luEntityId).firstValue();

				} else {//lu Entity ID is not given
					//log.info("luEntityId is not provided");
					if(luParentId == null || luParentId.isEmpty()) {// if parent LU ID is not given
						//log.info("luParentId is not provided");
						sqlCompEntities = sqlSelect +
								"LU_NAME = ? and execution_status = 'completed' and root_entity_status = 'completed' " +
								sqlSelectOrder + " limit ?";
						//log.info("wsGetTDMTaskExecutionStats - sqlCompEntities for non Root without ID: " + sqlCompEntities);
						compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, entitiesArraySize);

						sqlCompCount = sqlSelectCnt +
								"LU_NAME = ? and execution_status = 'completed' and root_entity_status = 'completed'";

						compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName).firstValue();

						sqlFailedEntities = sqlSelect +
								"LU_NAME = ? and (ifNull(execution_status, 'failed') <> 'completed' or root_entity_status <> 'completed') " +
								sqlSelectOrder + " limit ?";

						failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, entitiesArraySize);

						sqlFailedCount = sqlSelectCnt +
								"LU_NAME = ? and (ifNull(execution_status, 'failed') <> 'completed' or root_entity_status <> 'completed')";

						failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName).firstValue();

					} else {// Parent LU ID is given
						//log.info("luParentId: " + luParentId);
						sqlCompEntities = sqlSelect +
								"LU_NAME = ? and target_parent_id = ? and execution_status = 'completed' and root_entity_status = 'completed' " +
								sqlSelectOrder + " limit ? ";

						compEntitiesBuf = fabric().fetch(sqlCompEntities, luName,luParentId, entitiesArraySize);

						sqlCompCount = sqlSelectCnt +
								"LU_NAME = ? and target_parent_id = ? and execution_status = 'completed' and root_entity_status = 'completed' ";

						compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luParentId).firstValue();

						sqlFailedEntities = sqlSelect +
								"LU_NAME = ? and target_parent_id = ? and (ifNull(execution_status, 'failed') <> 'completed' or " +
								"root_entity_status <> 'completed') " +
								sqlSelectOrder + " limit ?";

						failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luParentId, entitiesArraySize);

						sqlFailedCount = sqlSelectCnt +
								"LU_NAME = ? and target_parent_id = ? and (ifNull(execution_status, 'failed') <> 'completed' or " +
								"root_entity_status <> 'completed')";

						failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luParentId).firstValue();

					}
				}
			}

			String prevTargetID = "";
			//log.info("wsGetTDMTaskExecutionStats - Looping over copied entities");
			for (Db.Row copiedEnt : compEntitiesBuf) {
				Map <String, Object> mapInnerCopiedEnt = new HashMap <>();

				mapInnerCopiedEnt.put("luName", copiedEnt.cell(9));

				//Get instance ID from entity id
				Object[] splitId = fnSplitUID("" +  copiedEnt.cell(0));
				String instanceId = "" + splitId[0];
				mapInnerCopiedEnt.put("sourceId", instanceId);

				String targetID = "" + copiedEnt.cell(1);

				mapInnerCopiedEnt.put("targetId", targetID);
				mapInnerCopiedEnt.put("rootSourceId", copiedEnt.cell(2));
				mapInnerCopiedEnt.put("rootTargetId", copiedEnt.cell(3));
				mapInnerCopiedEnt.put("parentLuName", copiedEnt.cell(4));
				mapInnerCopiedEnt.put("parentSourceId", copiedEnt.cell(5));
				mapInnerCopiedEnt.put("parentTargetId", copiedEnt.cell(6));
				mapInnerCopiedEnt.put("copyEntityStatus", copiedEnt.cell(7));
				mapInnerCopiedEnt.put("copyHierarchyStatus", copiedEnt.cell(8));

				if (!prevTargetID.equals(targetID)) {
					prevTargetID = targetID;
				}
				copiedEntitiesList.add(mapInnerCopiedEnt);
			}
			if (compEntitiesBuf != null) {
				compEntitiesBuf.close();
			}
			mapInnerCopiedBuf.put("NoOfEntities",compEntitiesCnt);
			mapInnerCopiedBuf.put("entitiesList", copiedEntitiesList);

			prevTargetID = "";

			//log.info("wsGetTDMTaskExecutionStats - looping over failed entities");
			for (Db.Row failedEnt : failedEntitiesBuf) {
				Map <String, Object> mapInnerFailedEnt = new HashMap <>();

				mapInnerFailedEnt.put("luName", failedEnt.cell(9));

				//Get instance ID from entity id
				Object[] splitId = fnSplitUID("" +  failedEnt.cell(0));
				String instanceId = "" + splitId[0];
				mapInnerFailedEnt.put("sourceId", instanceId);

				String targetID = "" + failedEnt.cell(1);
				String copyEntityStatus = "" + failedEnt.cell(7);

				mapInnerFailedEnt.put("targetId", targetID);
				mapInnerFailedEnt.put("rootSourceId", failedEnt.cell(2));
				mapInnerFailedEnt.put("rootTargetId", failedEnt.cell(3));
				mapInnerFailedEnt.put("parentLuName", failedEnt.cell(4));
				mapInnerFailedEnt.put("parentSourceId", failedEnt.cell(5));
				mapInnerFailedEnt.put("parentTargetId", failedEnt.cell(6));
				mapInnerFailedEnt.put("copyEntityStatus", copyEntityStatus);
				mapInnerFailedEnt.put("copyHierarchyStatus", failedEnt.cell(8));
				//log.info ("Failed - luName: " + failedEnt.cell(9) + ", rootSourceId: " + failedEnt.cell(2));
				// TDM 6.1.1 - 20-may-20, add the error msg that casued the failure
				String errorMsgSql = "select error_message from task_exe_error_detailed where " +
						"task_execution_id = ? and lu_name = ? and target_entity_id = ?  ORDER BY ERROR_CATEGORY LIMIT 5";
				Db.Rows errorMsgs = fabric().fetch(errorMsgSql, taskExecutionId, failedEnt.cell(9), targetID);
				List<String> entityErrMsgs  = new ArrayList<>();
				for (Db.Row errorMsg : errorMsgs) {
					entityErrMsgs.add("" + errorMsg.cell(0));
				}
				mapInnerFailedEnt.put("errorMsg", entityErrMsgs);
				if (!prevTargetID.equals(targetID)) {
					prevTargetID = targetID;
				}

				if ("true".equals(displayErrorPath)) {
					LinkedList <Object> failedErrPathList =  new LinkedList<>();
					//Get the failures error path
					if (!("Failed".equals(copyEntityStatus))) {

						String sqlFirst = "select LU_NAME, TARGET_ENTITY_ID, EXECUTION_STATUS, PARENT_LU_NAME, TARGET_PARENT_ID, " +
								"1 as row_number FROM TASK_EXECUTION_LINK_ENTITIES where PARENT_LU_NAME = ? and TARGET_PARENT_ID = ? ";

						String sqlSecond = "select a.LU_NAME, a.TARGET_ENTITY_ID, a.EXECUTION_STATUS, a.PARENT_LU_NAME, a.TARGET_PARENT_ID," +
								"row_number+1 FROM TASK_EXECUTION_LINK_ENTITIES a INNER JOIN relations b ON " +
								"b.lu_name = a.parent_lu_name and b.target_entity_id = a.TARGET_PARENT_ID ";

						String sqlRecursiveGetChildren = "WITH RECURSIVE relations AS (" + sqlFirst + " UNION " + sqlSecond +
								") select LU_NAME, TARGET_ENTITY_ID, EXECUTION_STATUS, PARENT_LU_NAME, TARGET_PARENT_ID, row_number " +
								" from relations order by row_number DESC";

						//log.info("wsGetTDMTaskExecutionStats - Calling RECURSIVE sql for parent lu: " + failedEnt.cell(9) + ", and target ID: " + targetID);
						Db.Rows childrenBuf = fabric().fetch(sqlRecursiveGetChildren, failedEnt.cell(9), targetID);

						Boolean errorLevelFound = false;
						int rowNum = 0;
						String lookupParentLuName = "";
						String lookupParentID = "";

						for (Db.Row childRec : childrenBuf) {
							Map <String, Object> mapInnerErrorPath = new HashMap <>();
							String childluName = "" + childRec.cell(0);
							String childEnityID = "" + childRec.cell(1);
							String childStatus = "" + childRec.cell(2);
							String childParenLuName = "" + childRec.cell(3);
							String childParentID = "" + childRec.cell(4);
							String entityStatus = "";

							int currRowNum = Integer.parseInt("" + childRec.cell(5));
							//log.info("wsGetTDMTaskExecutionStats - Failed - luName: " + childluName + ", childTargetId: " + childEnityID +
							//	", parentLuName: " + childParenLuName + ", childParentID: " + childParentID + ", childStatus: " + childStatus);

							if(!errorLevelFound && "completed".equals(childStatus)) {
								//The input is coming from bottom up, therefore looking for the lowest LU that failed, and until it is found continue to the next one
								continue;
							}

							//This point will be reached for the first time when the lowest erred lu is found
							if (!errorLevelFound) {
								//log.info("wsGetTDMTaskExecutionStats - Failed Child Found");
								//log.info("wsGetTDMTaskExecutionStats - setting error level to true");
								errorLevelFound = true;
								if ("completed".equals(childStatus)) {
									entityStatus = "Copied";
								} else {
									entityStatus = "Failed";
								}
								mapInnerErrorPath.put("luName", childluName);
								mapInnerErrorPath.put("entityStatus", entityStatus);
								mapInnerErrorPath.put("parentLuName", childParenLuName);
								mapInnerErrorPath.put("luStatus", "Failed");

								failedErrPathList.addFirst(mapInnerErrorPath);

								//log.info("wsGetTDMTaskExecutionStats - Adding to Error Path: luName: " + childluName + ", ParentLuName: " + childParenLuName);
								lookupParentLuName = "" + childParenLuName;
								lookupParentID = "" + childParentID;

								//log.info("wsGetTDMTaskExecutionStats - Looking for Parent: ID: " + lookupParentID + ", Parent LU Name: " + lookupParentLuName);
								continue;
							}

							// This point will be reached only if the first failed record was found
							// From this point we are looking for the ancenstor LUs of the failed record
							//log.info("wsGetTDMTaskExecutionStats - Comparing - lookupParentLuName: " + lookupParentLuName + " with childluName: " + childluName +
							//	" and lookupParentID: " + lookupParentID + " with childEnityID: " + childEnityID);
							if (lookupParentLuName.equals(childluName) && lookupParentID.equals(childEnityID)) {
								//log.info("wsGetTDMTaskExecutionStats - Parent Record Found - luName: " + childluName + ", TargetId: " + childEnityID);
								if ("completed".equals(childStatus)) {
									entityStatus = "Copied";
								} else {
									entityStatus = "Failed";
								}
								mapInnerErrorPath.put("luName", childluName);
								mapInnerErrorPath.put("entityStatus", entityStatus);
								mapInnerErrorPath.put("parentLuName", childParenLuName);
								mapInnerErrorPath.put("luStatus", "Failed");

								failedErrPathList.addFirst(mapInnerErrorPath);

								lookupParentLuName = "" + childParenLuName;
								lookupParentID = "" + childParentID;
								continue;
							}
						}
						if (childrenBuf != null) {
							childrenBuf.close();
						}
					}
					Map <String, Object> mapInputLu = new HashMap <>();
					mapInputLu.put("luName", failedEnt.cell(9));
					mapInputLu.put("entityStatus", copyEntityStatus);
					mapInputLu.put("parentLuName", failedEnt.cell(4));
					mapInputLu.put("luStatus", "Failed");


					failedErrPathList.addFirst(mapInputLu);
					//log.info("Adding luName: " + failedEnt.cell(9) + ", entityStatus: " + copyEntityStatus + " to erro path");

					mapInnerFailedEnt.put("Full Error Path", failedErrPathList);
				}

				failedEntitiesList.add(mapInnerFailedEnt);
			}
			if(failedEntitiesBuf != null) {
				failedEntitiesBuf.close();
			}

			//log.info("wsGetTDMTaskExecutionStats - finished failed entities");
			mapInnerFailedBuf.put("NoOfEntities", failedEntitiesCnt);
			mapInnerFailedBuf.put("entitiesList", failedEntitiesList);

			Map_Outer.put("Copied entities per execution", mapInnerCopiedBuf);
			Map_Outer.put("Failed entities per execution", mapInnerFailedBuf);
		}

		//
		//Get Reference Info

		String sqlCopiedRefTabBuf = "";
		String sqlFailedRefRabBuf = "";
		Db.Rows copiedRefTabBuf  = null;
		Db.Rows failedRefTabBuf = null;

		//log.info("wsGetTDMTaskExecutionStats - start handling reference");
		if (!(luEntityId == null || luEntityId.isEmpty()) && "REFERENCE".equals(luIdType) ) {
			sqlCopiedRefTabBuf = "select distinct entity_id from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' " +
					"and entity_id = ? and t.Execution_Status = 'completed' and Lu_Name = ?";
			copiedRefTabBuf = fabric().fetch(sqlCopiedRefTabBuf, luEntityId, luName);

			sqlFailedRefRabBuf = "select distinct entity_id from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' " +
					"and entity_id = ? and ifNull(t.Execution_Status, 'failed') <> 'completed' and Lu_Name = ?";
			failedRefTabBuf = fabric().fetch(sqlCopiedRefTabBuf, luEntityId, luName);
			//log.info("wsGetTDMTaskExecutionStats - got failed reference");
		} else {
			sqlCopiedRefTabBuf = "select distinct entity_id from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' " +
					"and t.Execution_Status = 'completed' and Lu_Name = ?";
			copiedRefTabBuf = fabric().fetch(sqlCopiedRefTabBuf, luName);

			sqlFailedRefRabBuf = "select distinct entity_id from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' " +
					"and ifNull(t.Execution_Status, 'failed') <> 'completed' and Lu_Name = ?";
			failedRefTabBuf = fabric().fetch(sqlFailedRefRabBuf, luName);
		}
		//log.info("wsGetTDMTaskExecutionStats - Got reference Data");
		int totalCount = 0;
		for (Db.Row copiedRefEnt : copiedRefTabBuf) {
			Map <String, Object> mapInnerCopiedRefEnt = new HashMap <>();

			totalCount++;

			mapInnerCopiedRefEnt.put("RerernceTableName", copiedRefEnt.cell(0));
			copiedRefEntitiesList.add(mapInnerCopiedRefEnt);
		}
		if (copiedRefTabBuf != null) {
			copiedRefTabBuf.close();
		}

		mapInnerCopiedRefBuf.put("NoOfEntities", totalCount);
		mapInnerCopiedRefBuf.put("entitiesList", copiedRefEntitiesList);

		totalCount = 0;
		for (Db.Row failedRefEnt : failedRefTabBuf) {
			Map <String, Object> mapInnerFailedRefEnt = new HashMap <>();

			totalCount++;
			String reTableName = "" + failedRefEnt.cell(0);
			mapInnerFailedRefEnt.put("RerernceTableName", reTableName);

			// TDM 6.1.1 - 20-may-20, add the error msg that casued the failure
			String errorMsgSql = "select error_message from task_exe_error_detailed where " +
					"task_execution_id = ? and lu_name = ? and target_entity_id = ? LIMIT 5";
			Db.Rows errorMsgs = fabric().fetch(errorMsgSql, taskExecutionId, luName, reTableName);
			List<String> entityErrMsgs  = new ArrayList<>();
			for (Db.Row errorMsg : errorMsgs) {
				entityErrMsgs.add("" + errorMsg.cell(0));
			}
			mapInnerFailedRefEnt.put("errorMsg", entityErrMsgs);

			failedRefEntitiesList.add(mapInnerFailedRefEnt);
		}
		if (failedRefTabBuf != null) {
			failedRefTabBuf.close();
		}
		mapInnerFailedRefBuf.put("NoOfEntities", totalCount);
		mapInnerFailedRefBuf.put("entitiesList", failedRefEntitiesList);

		Map_Outer.put("Copied Reference per execution", mapInnerCopiedRefBuf);
		Map_Outer.put("Failed Reference per execution", mapInnerFailedRefBuf);

		//Get the status of all the hierachies of the task, for each hierarchy mark if it has failures or not,
		// and if it has failures, where they on the root level or not
		String rootsStatusSql = "select ROOT_LU_NAME, max(root_status) from ( " +
				"select distinct root_lu_name, case when root_entity_status = 'completed' and EXECUTION_STATUS = 'completed' then 1 " +
				"when root_entity_status = 'failed' and  EXECUTION_STATUS = 'completed' then 2 " +
				"else 3 end as  root_status from task_execution_link_entities where parent_lu_name = '') group by root_lu_name";

		Db.Rows rootsStatuses = fabric().fetch(rootsStatusSql);

		for (Db.Row rootStatus : rootsStatuses) {
			String rootName = "" + rootStatus.cell(0);
			int rootStatusInd =  (int) rootStatus.cell(1);
			String rootSts = "";
			switch (rootStatusInd) {
				case 1 :
					rootSts = "completed";
					break;
				case 2 :
					rootSts = "child failed";
					break;
				default:
					rootSts = "root failed";
					break;
			}

			mapRootsStatus.put(rootName, rootSts);
		}

		Map_Outer.put("Roots Status", mapRootsStatus);
		if (rootsStatuses != null) {
			rootsStatuses.close();
		}

		return wrapWebServiceResults("SUCCESS", null, Map_Outer);
	}

	//end from TDM.LOGIC

	//from TDMTasks.Logic
	@desc("Gets a user name and permission group and returns the list of active tasks that the user can run.")
	@webService(path = "wsGetUserTasks/{userName}/{userType}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.XML, example = "<HashMap>\n  <result>\n    <task_last_updated_date>1615278319339</task_last_updated_date>\n    <be_id>1</be_id>\n    <selected_version_task_name/>\n    <environment_id>1</environment_id>\n    <selection_method>L</selection_method>\n    <selected_ref_version_task_name/>\n    <refresh_reference_data/>\n    <task_id>21</task_id>\n    <source_environment_id>1</source_environment_id>\n    <scheduler>immediate</scheduler>\n    <selected_ref_version_datetime/>\n    <source_env_name>ENV1</source_env_name>\n    <load_entity>false</load_entity>\n    <task_title>tester</task_title>\n    <selected_version_task_exe_id/>\n    <task_created_by>k2vtester01</task_created_by>\n    <fabric_environment_name/>\n    <scheduling_end_date/>\n    <delete_before_load>false</delete_before_load>\n    <retention_period_type/>\n    <task_status>Active</task_status>\n    <selection_param_value>1,2,3,4,5,6,7,8,9,10</selection_param_value>\n    <retention_period_value/>\n    <selected_version_datetime/>\n    <task_last_updated_by>k2vtester01</task_last_updated_by>\n    <selected_ref_version_task_exe_id/>\n    <task_execution_status>Active</task_execution_status>\n    <version_ind>false</version_ind>\n    <sync_mode/>\n    <number_of_entities_to_copy>10</number_of_entities_to_copy>\n    <task_creation_date>1615278319339</task_creation_date>\n    <task_globals>false</task_globals>\n    <replace_sequences/>\n    <entity_exclusion_list/>\n    <task_type>EXTRACT</task_type>\n    <parameters/>\n  </result>\n  <errorCode>SUCCESS</errorCode>\n  <message/>\n</HashMap>")
	@resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": [\n    {\n      \"task_last_updated_date\": \"2021-03-09 08:25:19.339\",\n      \"be_id\": 1,\n      \"selected_version_task_name\": null,\n      \"environment_id\": 1,\n      \"selection_method\": \"L\",\n      \"selected_ref_version_task_name\": null,\n      \"refresh_reference_data\": null,\n      \"task_id\": 21,\n      \"source_environment_id\": 1,\n      \"scheduler\": \"immediate\",\n      \"selected_ref_version_datetime\": null,\n      \"source_env_name\": \"ENV1\",\n      \"load_entity\": false,\n      \"task_title\": \"tester\",\n      \"selected_version_task_exe_id\": null,\n      \"task_created_by\": \"k2vtester01\",\n      \"fabric_environment_name\": null,\n      \"scheduling_end_date\": null,\n      \"delete_before_load\": false,\n      \"retention_period_type\": null,\n      \"task_status\": \"Active\",\n      \"selection_param_value\": \"1,2,3,4,5,6,7,8,9,10\",\n      \"retention_period_value\": null,\n      \"selected_version_datetime\": null,\n      \"task_last_updated_by\": \"k2vtester01\",\n      \"selected_ref_version_task_exe_id\": null,\n      \"task_execution_status\": \"Active\",\n      \"version_ind\": false,\n      \"sync_mode\": null,\n      \"number_of_entities_to_copy\": 10,\n      \"task_creation_date\": \"2021-03-09 08:25:19.339\",\n      \"task_globals\": false,\n      \"replace_sequences\": null,\n      \"entity_exclusion_list\": null,\n      \"task_type\": \"EXTRACT\",\n      \"parameters\": null\n    }\n  ],\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
	public static Object wsGetUserTasks(@param(required=true) String userName, @param(description="admin, owner or tester", required=true) String userType) throws Exception {
		String adminQuery = "select * from tasks where task_status = 'Active' and task_execution_status = 'Active'";
		String envOwnerQuery =
				//Query 1- get the extract tasks where the user is the owner of the source env
				"select t.* from tasks t, environment_owners o " +
						"where lower(task_type) = 'extract' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.source_environment_id = o.environment_id " +
						"and o.user_name = ? " +
						"UNION " +
						//Query 2- get the tasks, created by the user
						"select t.* from tasks t where lower(task_type) = 'extract' " +
						"and lower(task_status) = 'active' and lower(task_execution_status) = 'active' " +
						"and t.task_Created_by = ? " +
						"UNION " +
						//Query 3 - get the task created by another tester which belongs to the same role of the user
						"select t.* from tasks t, environment_roles r, environment_role_users u " +
						"where lower(task_type) = 'extract' and lower(task_status) = 'active' "+
						"and lower(task_execution_status) = 'active' and t.task_Created_by <> ? " +
						"and not exists (select 1 from environment_owners o1 where o1.environment_id  = t.source_environment_id and o1.user_name = t.task_Created_by) " +
						"and t.source_environment_id = r.environment_id and r.role_id = u.role_id " +
						"and lower(r.role_status) = 'active' and (t.task_Created_by = u.username or lower(u.username) = 'all') " +
						"and exists (select 1 from " +
						"(Select env_list.environment_id, u.role_id " +
						"From environment_roles r, environment_role_users u, " +
						"(Select r1.environment_id, 'user' As assignment_type " +
						"From environment_roles r1, environment_role_users u1 " +
						"Where r1.role_id = u1.role_id And u1.username = ? And " +
						"lower(r1.role_status) = 'active' " +
						"UNION " +
						"Select r2.environment_id, 'all' As assignment_Type " +
						"From environment_roles r2, environment_role_users u2 " +
						"Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
						"And lower(r2.role_status) = 'active' And Not exists " +
						"(select 1 from environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
						"Where r.environment_id = env_list.environment_id " +
						"And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
						"And ((env_list.assignment_type = 'all' And " +
						"lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
						"where user_roles.role_id = r.role_id)" +
						"UNION " +
						//Query 4- get the load tasks where the user is the owner of the target env
						"select t.* from tasks t, environment_owners o " +
						"where lower(task_type) = 'load' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.environment_id = o.environment_id " +
						"and o.user_name = ? " +
						"UNION " +
						//Query 5 - get the tasks, created by the user
						"select t.* from tasks t where lower(task_type) = 'load' " +
						"and lower(task_status) = 'active' and lower(task_execution_status) = 'active' " +
						"and t.task_Created_by = ? " +
						"UNION " +
						//Query 6 - get the task created by another user which belongs to the same role of the user on the target env
						"select t.* from tasks t, environment_roles r, environment_role_users u " +
						"where lower(task_type) = 'load' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.task_Created_by <> ? " +
						"and not exists (select 1 from environment_owners o1 where o1.environment_id  = t.environment_id and o1.user_name = t.task_Created_by) " +
						"and t.environment_id = r.environment_id and r.role_id = u.role_id " +
						"and lower(r.role_status) = 'active' and (t.task_Created_by = u.username or lower(u.username) = 'all') " +
						"and exists (select 1 from " +
						"(Select env_list.environment_id, u.role_id " +
						"From environment_roles r, environment_role_users u, " +
						"(Select r1.environment_id, 'user' As assignment_type " +
						"From environment_roles r1, environment_role_users u1 " +
						"Where r1.role_id = u1.role_id And u1.username = ? And " +
						"lower(r1.role_status) = 'active' " +
						"UNION " +
						"Select r2.environment_id, 'all' As assignment_Type " +
						"From environment_roles r2, environment_role_users u2 " +
						"Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
						"And lower(r2.role_status) = 'active' And Not exists " +
						"(select 1 from environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
						"Where r.environment_id = env_list.environment_id " +
						"And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
						"And ((env_list.assignment_type = 'all' And " +
						"lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
						"where user_roles.role_id = r.role_id)";
		
		String testerQuery =
				//Query 1 - get the tasks, created by the user
				"select t.* from tasks t where lower(task_type) = 'extract' " +
						"and lower(task_status) = 'active' and lower(task_execution_status) = 'active' " +
						"and t.task_Created_by = ? " +
						"UNION " +
						//Query 2 - get the task created by another user which belongs to the same role of the user
						"select t.* from tasks t, environment_roles r, environment_role_users u " +
						"where lower(task_type) = 'extract' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.task_Created_by <> ? " +
						"and not exists (select 1 from environment_owners o1 where o1.environment_id  = t.source_environment_id and o1.user_name = t.task_Created_by) " +
						"and t.source_environment_id = r.environment_id " +
						"and r.role_id = u.role_id and lower(r.role_status) = 'active' " +
						"and (t.task_Created_by = u.username or lower(u.username) = 'all') " +
						"and exists (select 1 from " +
						"(Select env_list.environment_id, u.role_id " +
						"From environment_roles r, environment_role_users u, " +
						"(Select r1.environment_id, 'user' As assignment_type " +
						"From environment_roles r1, environment_role_users u1 " +
						"Where r1.role_id = u1.role_id And u1.username = ? And " +
						"lower(r1.role_status) = 'active' " +
						"UNION " +
						"Select r2.environment_id, 'all' As assignment_Type " +
						"From environment_roles r2, environment_role_users u2 " +
						"Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
						"And lower(r2.role_status) = 'active' And Not exists " +
						"(select 1 from environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
						"Where r.environment_id = env_list.environment_id " +
						"And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
						"And ((env_list.assignment_type = 'all' And " +
						"lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
						"where user_roles.role_id = r.role_id) " +
						"UNION " +
						//Query 3 - get the tasks, created by the user
						"select t.* from tasks t " +
						"where lower(task_type) = 'load' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.task_Created_by = ? " +
						"UNION " +
						//Query 4 - get the task created by another user which belongs to the same role of the user on the target env
						"select t.* from tasks t, environment_roles r, environment_role_users u " +
						"where lower(task_type) = 'load' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.task_Created_by <> ? " +
						"and not exists (select 1 from environment_owners o1 where o1.environment_id  = t.environment_id and o1.user_name = t.task_Created_by) " +
						"and t.environment_id = r.environment_id and r.role_id = u.role_id and lower(r.role_status) = 'active' " +
						"and (t.task_Created_by = u.username or lower(u.username) = 'all') " +
						"and exists (select 1 from " +
						"(Select env_list.environment_id, u.role_id " +
						"From environment_roles r, environment_role_users u, " +
						"(Select r1.environment_id, 'user' As assignment_type " +
						"From environment_roles r1, environment_role_users u1 " +
						"Where r1.role_id = u1.role_id And u1.username = ? And " +
						"lower(r1.role_status) = 'active' " +
						"UNION " +
						"Select r2.environment_id, 'all' As assignment_Type " +
						"From environment_roles r2, environment_role_users u2 " +
						"Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
						"And lower(r2.role_status) = 'active' And Not exists " +
						"(select 1 from environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
						"Where r.environment_id = env_list.environment_id " +
						"And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
						"And ((env_list.assignment_type = 'all' And " +
						"lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
						"where user_roles.role_id = r.role_id)";
		
		
		Object taskList = null;
		
		switch (userType.toLowerCase()) {
			case "admin" :
				taskList = db("TDM").fetch(adminQuery);
				break;
			case "owner" :
				String[] ownerParams = new String[12];
				Arrays.fill(ownerParams, userName);
				taskList = db("TDM").fetch(envOwnerQuery, ownerParams);
				break;
			case "tester" :
				String[] testerParams = new String[10];
				Arrays.fill(testerParams, userName);
				taskList = db("TDM").fetch(testerQuery, testerParams);
				break;
			default:
				log.error("wsGetUserTasks - Wrong User Type, supported types: admin, owner, tester");
				return wrapWebServiceResults("FAIL", "Wrong User Type - " + userType + ", supported types: admin, owner, tester", null);
		}
		
		
		// convert iterable to serializable object
		if (taskList instanceof Db.Rows) {
			ArrayList<Map> rows = new ArrayList<>();
			((Db.Rows) taskList).forEach(row -> {
				HashMap copy = new HashMap();
				copy.putAll(row);
				rows.add(copy);
			});
			taskList = rows;
		}
		
		return wrapWebServiceResults("SUCCESS", null,taskList);
	}


	@desc("Returns the details of the current/last execution of the given task_id. If the task is pending, it will return only its status, else it will return the statistics of the entities it is handling/handled.")
	@webService(path = "wsTaskMonitor/{taskID}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.XML, example = "<HashMap>\n  <result>\n    <Task ID>20</Task ID>\n    <Task Details>\n      <Fabric Batch ID>4d78fbe8-be2f-4928-8d66-9bb45b7cd8dc</Fabric Batch ID>\n      <Task Statistics>\n        <Status>\n        </Status>\n        <Ent./sec (avg.)>3.4</Ent./sec (avg.)>\n        <Added>5</Added>\n        <Ent./sec (pace)>3.4</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:01</Duration>\n        <End time>2021-03-08 11:31:23</End time>\n        <Name>26090035-d2a2-4489-abf7-6805d3baa3d8</Name>\n        <Succeeded>5</Succeeded>\n        <Total>--</Total>\n        <Level>Node</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:21</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Statistics>\n        <Status>\n        </Status>\n        <Ent./sec (avg.)>3.4</Ent./sec (avg.)>\n        <Added>5</Added>\n        <Ent./sec (pace)>3.4</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:01</Duration>\n        <End time>2021-03-08 11:31:23</End time>\n        <Name>DC1</Name>\n        <Succeeded>5</Succeeded>\n        <Total>--</Total>\n        <Level>DC</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:21</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Statistics>\n        <Status>DONE</Status>\n        <Ent./sec (avg.)>3.4</Ent./sec (avg.)>\n        <Added>5</Added>\n        <Ent./sec (pace)>3.4</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:01</Duration>\n        <End time>2021-03-08 11:31:23</End time>\n        <Name>--</Name>\n        <Succeeded>5</Succeeded>\n        <Total>5</Total>\n        <Level>Cluster</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:21</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Status>completed</Task Status>\n      <LU Name>PATIENT_LU</LU Name>\n    </Task Details>\n    <Task Details>\n      <Fabric Batch ID>ec79ecc6-ab01-43f6-9233-40163ce6df54</Fabric Batch ID>\n      <Task Statistics>\n        <Status>\n        </Status>\n        <Ent./sec (avg.)>7.13</Ent./sec (avg.)>\n        <Added>31</Added>\n        <Ent./sec (pace)>7.13</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:04</Duration>\n        <End time>2021-03-08 11:31:48</End time>\n        <Name>26090035-d2a2-4489-abf7-6805d3baa3d8</Name>\n        <Succeeded>31</Succeeded>\n        <Total>--</Total>\n        <Level>Node</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:43</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Statistics>\n        <Status>\n        </Status>\n        <Ent./sec (avg.)>7.13</Ent./sec (avg.)>\n        <Added>31</Added>\n        <Ent./sec (pace)>7.13</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:04</Duration>\n        <End time>2021-03-08 11:31:48</End time>\n        <Name>DC1</Name>\n        <Succeeded>31</Succeeded>\n        <Total>--</Total>\n        <Level>DC</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:43</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Statistics>\n        <Status>DONE</Status>\n        <Ent./sec (avg.)>7.13</Ent./sec (avg.)>\n        <Added>31</Added>\n        <Ent./sec (pace)>7.13</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:04</Duration>\n        <End time>2021-03-08 11:31:48</End time>\n        <Name>--</Name>\n        <Succeeded>31</Succeeded>\n        <Total>31</Total>\n        <Level>Cluster</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:43</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Status>completed</Task Status>\n      <LU Name>PATIENT_VISITS</LU Name>\n    </Task Details>\n    <Task Details>\n      <Task Status>Pending</Task Status>\n      <Process Name>LoggerFlow1</Process Name>\n    </Task Details>\n    <Task Details>\n      <Fabric Batch ID>c5da699f-357a-4298-9402-2a55ee9f5d57</Fabric Batch ID>\n      <Task Statistics>\n        <Status>\n        </Status>\n        <Ent./sec (avg.)>6.76</Ent./sec (avg.)>\n        <Added>15</Added>\n        <Ent./sec (pace)>6.76</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:02</Duration>\n        <End time>2021-03-08 11:31:57</End time>\n        <Name>26090035-d2a2-4489-abf7-6805d3baa3d8</Name>\n        <Succeeded>15</Succeeded>\n        <Total>--</Total>\n        <Level>Node</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:54</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Statistics>\n        <Status>\n        </Status>\n        <Ent./sec (avg.)>6.76</Ent./sec (avg.)>\n        <Added>15</Added>\n        <Ent./sec (pace)>6.76</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:02</Duration>\n        <End time>2021-03-08 11:31:57</End time>\n        <Name>DC1</Name>\n        <Succeeded>15</Succeeded>\n        <Total>--</Total>\n        <Level>DC</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:54</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Statistics>\n        <Status>DONE</Status>\n        <Ent./sec (avg.)>6.76</Ent./sec (avg.)>\n        <Added>15</Added>\n        <Ent./sec (pace)>6.76</Ent./sec (pace)>\n        <Updated>0</Updated>\n        <Failed>0</Failed>\n        <Duration>00:00:02</Duration>\n        <End time>2021-03-08 11:31:57</End time>\n        <Name>--</Name>\n        <Succeeded>15</Succeeded>\n        <Total>15</Total>\n        <Level>Cluster</Level>\n        <Remaining dur.>00:00:00</Remaining dur.>\n        <Remaining>0</Remaining>\n        <Start time>2021-03-08 11:31:54</Start time>\n        <Unchanged>0</Unchanged>\n        <% Completed>100</% Completed>\n      </Task Statistics>\n      <Task Status>running</Task Status>\n      <LU Name>VISIT_LAB_RESULTS</LU Name>\n    </Task Details>\n    <Task Name>Ext + Ver + Hier</Task Name>\n    <Task Execution ID>61</Task Execution ID>\n  </result>\n  <errorCode>SUCCESS</errorCode>\n  <message/>\n</HashMap>")
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"Task ID\": \"20\",\r\n" +
			"    \"Task Details\": [\r\n" +
			"      {\r\n" +
			"        \"Fabric Batch ID\": \"4d78fbe8-be2f-4928-8d66-9bb45b7cd8dc\",\r\n" +
			"        \"Task Statistics\": [\r\n" +
			"          {\r\n" +
			"            \"Status\": \"\",\r\n" +
			"            \"Ent./sec (avg.)\": \"3.4\",\r\n" +
			"            \"Added\": 5,\r\n" +
			"            \"Ent./sec (pace)\": \"3.4\",\r\n" +
			"            \"Updated\": 0,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:01\",\r\n" +
			"            \"End time\": \"2021-03-08 11:31:23\",\r\n" +
			"            \"Name\": \"26090035-d2a2-4489-abf7-6805d3baa3d8\",\r\n" +
			"            \"Succeeded\": \"5\",\r\n" +
			"            \"Total\": \"--\",\r\n" +
			"            \"Level\": \"Node\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-03-08 11:31:21\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"Status\": \"\",\r\n" +
			"            \"Ent./sec (avg.)\": \"3.4\",\r\n" +
			"            \"Added\": 5,\r\n" +
			"            \"Ent./sec (pace)\": \"3.4\",\r\n" +
			"            \"Updated\": 0,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:01\",\r\n" +
			"            \"End time\": \"2021-03-08 11:31:23\",\r\n" +
			"            \"Name\": \"DC1\",\r\n" +
			"            \"Succeeded\": \"5\",\r\n" +
			"            \"Total\": \"--\",\r\n" +
			"            \"Level\": \"DC\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-03-08 11:31:21\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"Status\": \"DONE\",\r\n" +
			"            \"Ent./sec (avg.)\": \"3.4\",\r\n" +
			"            \"Added\": 5,\r\n" +
			"            \"Ent./sec (pace)\": \"3.4\",\r\n" +
			"            \"Updated\": 0,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:01\",\r\n" +
			"            \"End time\": \"2021-03-08 11:31:23\",\r\n" +
			"            \"Name\": \"--\",\r\n" +
			"            \"Succeeded\": \"5\",\r\n" +
			"            \"Total\": \"5\",\r\n" +
			"            \"Level\": \"Cluster\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-03-08 11:31:21\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          }\r\n" +
			"        ],\r\n" +
			"        \"Task Status\": \"completed\",\r\n" +
			"        \"LU Name\": \"PATIENT_LU\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"Task Status\": \"Pending\",\r\n" +
			"        \"Process Name\": \"LoggerFlow1\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"Task Status\": \"Pending\",\r\n" +
			"        \"LU Name\": \"VISIT_LAB_RESULTS\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"Fabric Batch ID\": \"ec79ecc6-ab01-43f6-9233-40163ce6df54\",\r\n" +
			"        \"Task Statistics\": [\r\n" +
			"          {\r\n" +
			"            \"Status\": \"\",\r\n" +
			"            \"Ent./sec (avg.)\": \"7.13\",\r\n" +
			"            \"Added\": 31,\r\n" +
			"            \"Ent./sec (pace)\": \"7.13\",\r\n" +
			"            \"Updated\": 0,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:04\",\r\n" +
			"            \"End time\": \"2021-03-08 11:31:48\",\r\n" +
			"            \"Name\": \"26090035-d2a2-4489-abf7-6805d3baa3d8\",\r\n" +
			"            \"Succeeded\": \"31\",\r\n" +
			"            \"Total\": \"--\",\r\n" +
			"            \"Level\": \"Node\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-03-08 11:31:43\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"Status\": \"\",\r\n" +
			"            \"Ent./sec (avg.)\": \"7.13\",\r\n" +
			"            \"Added\": 31,\r\n" +
			"            \"Ent./sec (pace)\": \"7.13\",\r\n" +
			"            \"Updated\": 0,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:04\",\r\n" +
			"            \"End time\": \"2021-03-08 11:31:48\",\r\n" +
			"            \"Name\": \"DC1\",\r\n" +
			"            \"Succeeded\": \"31\",\r\n" +
			"            \"Total\": \"--\",\r\n" +
			"            \"Level\": \"DC\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-03-08 11:31:43\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"Status\": \"DONE\",\r\n" +
			"            \"Ent./sec (avg.)\": \"7.13\",\r\n" +
			"            \"Added\": 31,\r\n" +
			"            \"Ent./sec (pace)\": \"7.13\",\r\n" +
			"            \"Updated\": 0,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:04\",\r\n" +
			"            \"End time\": \"2021-03-08 11:31:48\",\r\n" +
			"            \"Name\": \"--\",\r\n" +
			"            \"Succeeded\": \"31\",\r\n" +
			"            \"Total\": \"31\",\r\n" +
			"            \"Level\": \"Cluster\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-03-08 11:31:43\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          }\r\n" +
			"        ],\r\n" +
			"        \"Task Status\": \"running\",\r\n" +
			"        \"LU Name\": \"PATIENT_VISITS\"\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"Task Name\": \"Ext + Ver + Hier\",\r\n" +
			"    \"Task Execution ID\": 61\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsTaskMonitor(@param(required=true) String taskID) throws Exception {
		String getExecIDsQuery = "SELECT task_execution_id, execution_status, fabric_execution_id, " +
				"lu_name as name, task_title, 'LU' as type " +
				"FROM TASK_EXECUTION_LIST L, TASKS_LOGICAL_UNITS U, TASKS T WHERE " +
				"t.task_id = l.task_id AND u.task_id = l.task_id AND u.lu_id = l.lu_id AND T.task_id = ? " +
				"AND l.task_execution_id = (SELECT MAX(TASK_EXECUTION_ID) " +
				"FROM TASK_EXECUTION_LIST L2 WHERE TASK_ID = ?) and process_id = 0 " +
				"UNION " +
				"SELECT task_execution_id, execution_status, fabric_execution_id, " +
				"process_name as name, task_title, 'Process' as type " +
				"FROM TASK_EXECUTION_LIST L, TASKS_POST_EXE_PROCESS P, TASKS T WHERE " +
				"t.task_id = l.task_id AND p.task_id = l.task_id AND p.process_id = l.process_id AND T.task_id = ? " +
				"AND l.task_execution_id = (SELECT MAX(TASK_EXECUTION_ID) " +
				"FROM TASK_EXECUTION_LIST L2 WHERE TASK_ID = ?) AND lu_id = 0";

		Db.Rows execIDsList = db("TDM").fetch(getExecIDsQuery, taskID, taskID, taskID, taskID);

		HashMap <String, Object> taskInfo = new HashMap<>();
		List <Object> taskList = new ArrayList<>();

		taskInfo.put("Task ID", taskID);
		boolean firstRecInd = true;

		for (Db.Row execRec : execIDsList) {
			if (firstRecInd) {
				firstRecInd = false;
				taskInfo.put("Task Name", execRec.get("task_title"));
				taskInfo.put("Task Execution ID", execRec.get("task_execution_id"));
			}
			HashMap<String, Object> taskLUInfo = new HashMap<>();
			String execStatus = "" + execRec.get("execution_status");
			if ("LU".equals(execRec.get("type"))) {
				taskLUInfo.put("LU Name", execRec.get("name"));
			} else {
				taskLUInfo.put("Process Name", execRec.get("name"));
			}
			taskLUInfo.put("Task Status", execStatus);

			if (!"pending".equalsIgnoreCase(execStatus)) {
				taskLUInfo.put("Fabric Batch ID", execRec.get("fabric_execution_id"));
				taskLUInfo.put("Task Statistics", fnBatchStats("" + execRec.get("fabric_execution_id"), "S"));
			}

			taskList.add(taskLUInfo);
		}

		taskInfo.put("Task Details", taskList);
		return wrapWebServiceResults("SUCCESS", null,taskInfo);
	}


	@desc("Returns active task ID by its name.")
	@webService(path = "wsGetTaskId/{taskName}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.XML, example = "<HashMap>\r\n" +
			"\t<result>5</result>\r\n" +
			"\t<errorCode>SUCCESS</errorCode>\r\n" +
			"\t<message/>\r\n" +
			"</HashMap>")
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"\t\"result\": 5,\r\n" +
			"\t\"errorCode\": \"SUCCESS\",\r\n" +
			"\t\"message\": null\r\n" +
			"}")
	public static Object wsGetTaskId(@param(required=true) String taskName) throws Exception {
		Object response = db("TDM").
				fetch("select public.tasks.task_id from public.tasks where public.tasks.task_title= ? and public.tasks.task_status='Active'", taskName).firstValue();
		if (response == null) {
			return wrapWebServiceResults("FAIL", "No active task found for task name '" + taskName + "'.", response);
		} else {
			return wrapWebServiceResults("SUCCESS", null, response);
		}
	}
	//end from TDMTasks.logic

	@desc("Updates Task Globals.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"\r\n" +
			"{\r\n" +
			"  \"globals\": [\r\n" +
			"    {\r\n" +
			"      \"global_name\": \"MASK_FLAG\",\r\n" +
			"      \"global_value\": \"0\"\r\n" +
			"    }\r\n" +
			"  ]\r\n" +
			"}")
	@webService(path = "task/{taskId}/globals", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object UpdateTaskGlobals(@param(required=true) Long taskId, List<Map<String,Object>> globals) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			if (globals!=null && globals.size() != 0 ){
				for (Map<String,Object> global:globals){
					String sql = "UPDATE \"" + schema + "\".task_globals SET " +
							"global_value=(?) " +
							"WHERE task_id = " + taskId + " AND global_name = \'" + global.get("global_name") + "\'";
					db("TDM").execute(sql,global.get("global_value"));
				}
			}
			response.put("result",new HashMap<>());
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Deletes Task")
	@webService(path = "task/{taskId}/taskname/{taskName}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsDeleteTask(@param(required=true) Long taskId, @param(required=true) String taskName) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());

		try {
			String sql = "UPDATE \"" + schema + "\".tasks SET " +
					"task_status=(?), task_execution_status=(?), " +
					"task_last_updated_date=(?), " +
					"task_last_updated_by=(?) " +
					"WHERE task_id = " + taskId;
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			db("TDM").execute(sql,"Inactive", "Inactive", now, username);
			try {
				String activityDesc = "Task " + taskName + " was deleted";
				fnInsertActivity("delete", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}

			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

}


