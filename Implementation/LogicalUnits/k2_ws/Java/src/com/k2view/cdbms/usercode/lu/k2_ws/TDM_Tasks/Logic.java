/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM_Tasks;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.*;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.ParamConvertor;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.VirtualMap;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.k2view.cdbms.usercode.common.SharedGlobals.MAX_NUMBER_OF_ENTITIES_IN_LIST;
import static com.k2view.cdbms.usercode.common.SharedGlobals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetReferenceSummaryData;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnSplitUID;
import static com.k2view.cdbms.usercode.common.TaskExecutionUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TaskValidationsUtils.SharedLogic.fnValidateSourceEnvForTask;
import static com.k2view.cdbms.usercode.common.TaskValidationsUtils.SharedLogic.fnValidateTargetEnvForTask;
import static com.k2view.cdbms.usercode.common.TdmSharedUtils.SharedLogic.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class Logic extends WebServiceUserCode {
	public static String schema="public";
	public static final String DB_FABRIC = "fabric";
    public static final String TDM = "TDM";


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
String sql= "SELECT * FROM " + TDMDB_SCHEMA + ".product_logical_units lu " +
					"INNER JOIN " + TDMDB_SCHEMA + ".products p " +
					"ON (lu.product_id = p.product_id AND p.product_status = \'Active\') " +
					"INNER JOIN " + TDMDB_SCHEMA + ".environment_products ep " +
					"ON (lu.product_id = ep.product_id AND ep.status = \'Active\') " +
					"INNER JOIN " + TDMDB_SCHEMA + ".business_entities be " +
					"ON (be.be_id = lu.be_id) " +
					"WHERE environment_id = " + envId + " AND be_status = \'Active\'";
			Db.Rows rows= db(TDM).fetch(sql);
		
		
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
						result.remove(j); 
						j--;
					}
				}
			}
		
			errorCode="SUCCESS";
			response.put("result",result);
			if (rows != null) {
				rows.close();
			}		
		} catch(Exception e){
			errorCode="FAILED";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("This is the main API to get the task details. This API gets the list of all TDM tasks or a list of given task IDs if the input task_ids parameter is populated. The input task_ids is an optional parameter that can be populated to return the data of a given list of tasks. The ID(s) of the required task(s), will be supplied in this parameter separated by comma. For example, task_ids=4 or task_ids=3,2,6. \r\n" +
			"\r\n" +
			"If task_ids parameter is not populated, the data of all tasks will be returned by the API.")
	@webService(path = "tasks", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"task_last_updated_date\": \"2022-03-15 12:02:15.84\",\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"selected_version_task_name\": null,\r\n" +
			"      \"reserve_retention_period_type\": \"Days\",\r\n" +
			"      \"environment_id\": 2,\r\n" +
			"      \"selection_method\": \"C\",\r\n" +
			"      \"selected_ref_version_task_name\": null,\r\n" +
			"      \"refresh_reference_data\": false,\r\n" +
			"      \"tester\": \"tdmTesters2\",\r\n" +
			"      \"be_last_updated_date\": \"2021-11-07 11:24:45.078\",\r\n" +
			"      \"owners\": [\r\n" +
			"        {\r\n" +
			"          \"owner\": \"tali\",\r\n" +
			"          \"owner_type\": \"ID\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"refcount\": 0,\r\n" +
			"      \"num_of_entities\": 120,\r\n" +
			"      \"tester_type\": \"GROUP\",\r\n" +
			"      \"load_entity\": true,\r\n" +
			"      \"selected_version_task_exe_id\": 0,\r\n" +
			"      \"task_created_by\": \"tali\",\r\n" +
			"      \"be_last_updated_by\": \"admin\",\r\n" +
			"      \"scheduling_end_date\": null,\r\n" +
			"      \"retention_period_type\": null,\r\n" +
			"      \"environment_point_of_contact_phone1\": null,\r\n" +
			"      \"processnames\": null,\r\n" +
			"      \"testers\": [\r\n" +
			"        {\r\n" +
			"          \"tester_type\": \"GROUP\",\r\n" +
			"          \"role_id\": [\r\n" +
			"            \"6\"\r\n" +
			"          ],\r\n" +
			"          \"tester\": \"tdmTesters2\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"tester_type\": \"ID\",\r\n" +
			"          \"role_id\": [\r\n" +
			"            \"2\"\r\n" +
			"          ],\r\n" +
			"          \"tester\": \"taha\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"tester_type\": \"GROUP\",\r\n" +
			"          \"role_id\": [\r\n" +
			"            \"2\"\r\n" +
			"          ],\r\n" +
			"          \"tester\": \"tdmTesters1\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"selection_param_value\": \"get5GCustomers\",\r\n" +
			"      \"environment_status\": \"Active\",\r\n" +
			"      \"be_status\": \"Active\",\r\n" +
			"      \"selected_version_datetime\": null,\r\n" +
			"      \"task_last_updated_by\": \"admin\",\r\n" +
			"      \"selected_ref_version_task_exe_id\": 0,\r\n" +
			"      \"task_execution_status\": \"Active\",\r\n" +
			"      \"sync_mode\": null,\r\n" +
			"      \"replace_sequences\": false,\r\n" +
			"      \"entity_exclusion_list\": null,\r\n" +
			"      \"environment_point_of_contact_last_name\": null,\r\n" +
			"      \"environment_point_of_contact_email\": null,\r\n" +
			"      \"be_description\": \"\",\r\n" +
			"      \"reserve_retention_period_value\": null,\r\n" +
			"      \"parameters\": \"{\\\"group\\\":{\\\"rules\\\":[{\\\"condition\\\":\\\"=\\\",\\\"field\\\":\\\"BILLING.NO_OF_OPEN_INVOICES\\\",\\\"data\\\":\\\"2\\\",\\\"operator\\\":\\\"AND\\\",\\\"$$hashKey\\\":\\\"object:389\\\",\\\"type\\\":\\\"combo\\\",\\\"validValues\\\":[\\\"0\\\",\\\"1\\\",\\\"2\\\",\\\"3\\\",\\\"4\\\",\\\"5\\\",\\\"6\\\",\\\"7\\\"],\\\"disableThird\\\":false}]}}\",\r\n" +
			"      \"environment_expiration_date\": null,\r\n" +
			"      \"environment_point_of_contact_phone2\": null,\r\n" +
			"      \"environment_created_by\": \"admin\",\r\n" +
			"      \"roles\": [\r\n" +
			"        [\r\n" +
			"          {\r\n" +
			"            \"role_id\": 6,\r\n" +
			"            \"allowed_test_conn_failure\": true\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"role_id\": 2,\r\n" +
			"            \"allowed_test_conn_failure\": true\r\n" +
			"          }\r\n" +
			"        ]\r\n" +
			"      ],\r\n" +
			"      \"environment_last_updated_by\": \"admin\",\r\n" +
			"      \"be_creation_date\": \"2021-11-07 11:24:17.668\",\r\n" +
			"      \"task_id\": 48,\r\n" +
			"      \"be_created_by\": \"admin\",\r\n" +
			"      \"source_environment_id\": 1,\r\n" +
			"      \"role_id_orig\": 6,\r\n" +
			"      \"scheduler\": \"immediate\",\r\n" +
			"      \"environment_description\": null,\r\n" +
			"      \"selected_ref_version_datetime\": null,\r\n" +
			"      \"source_env_name\": \"SRC\",\r\n" +
			"      \"reserve_ind\": true,\r\n" +
			"      \"task_title\": \"testLoadAndReserve\",\r\n" +
			"      \"fabric_environment_name\": null,\r\n" +
			"      \"environment_name\": \"TAR\",\r\n" +
			"      \"delete_before_load\": false,\r\n" +
			"      \"allow_write\": true,\r\n" +
			"      \"owner\": \"tali\",\r\n" +
			"      \"task_status\": \"Active\",\r\n" +
			"      \"retention_period_value\": null,\r\n" +
			"      \"executioncount\": 0,\r\n" +
			"      \"environment_last_updated_date\": \"2022-03-09 15:00:45.845\",\r\n" +
			"      \"be_name\": \"Customer\",\r\n" +
			"      \"version_ind\": false,\r\n" +
			"      \"task_creation_date\": \"2022-03-15 09:35:02.208\",\r\n" +
			"      \"task_globals\": false,\r\n" +
			"      \"environment_point_of_contact_first_name\": null,\r\n" +
			"      \"task_type\": \"LOAD\",\r\n" +
			"      \"environment_creation_date\": \"2021-11-07 15:10:17.569\",\r\n" +
			"      \"owner_type\": \"ID\",\r\n" +
			"      \"creatorRoles\": [\r\n" +
			"        \"tdmOwner\"\r\n" +
			"      ]\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"task_last_updated_date\": \"2022-03-16 10:35:19.317\",\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"selected_version_task_name\": null,\r\n" +
			"      \"reserve_retention_period_type\": \"Days\",\r\n" +
			"      \"environment_id\": 2,\r\n" +
			"      \"selection_method\": \"C\",\r\n" +
			"      \"selected_ref_version_task_name\": null,\r\n" +
			"      \"refresh_reference_data\": false,\r\n" +
			"      \"tester\": \"tdmTesters2\",\r\n" +
			"      \"be_last_updated_date\": \"2021-11-07 11:24:45.078\",\r\n" +
			"      \"owners\": [\r\n" +
			"        {\r\n" +
			"          \"owner\": \"tali\",\r\n" +
			"          \"owner_type\": \"ID\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"refcount\": 0,\r\n" +
			"      \"num_of_entities\": 10,\r\n" +
			"      \"tester_type\": \"GROUP\",\r\n" +
			"      \"load_entity\": false,\r\n" +
			"      \"selected_version_task_exe_id\": 0,\r\n" +
			"      \"task_created_by\": \"admin\",\r\n" +
			"      \"be_last_updated_by\": \"admin\",\r\n" +
			"      \"scheduling_end_date\": null,\r\n" +
			"      \"retention_period_type\": null,\r\n" +
			"      \"environment_point_of_contact_phone1\": null,\r\n" +
			"      \"processnames\": null,\r\n" +
			"      \"testers\": [\r\n" +
			"        {\r\n" +
			"          \"tester_type\": \"GROUP\",\r\n" +
			"          \"role_id\": [\r\n" +
			"            \"6\"\r\n" +
			"          ],\r\n" +
			"          \"tester\": \"tdmTesters2\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"tester_type\": \"ID\",\r\n" +
			"          \"role_id\": [\r\n" +
			"            \"2\"\r\n" +
			"          ],\r\n" +
			"          \"tester\": \"taha\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"tester_type\": \"GROUP\",\r\n" +
			"          \"role_id\": [\r\n" +
			"            \"2\"\r\n" +
			"          ],\r\n" +
			"          \"tester\": \"tdmTesters1\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"selection_param_value\": \"get5GCustomers\",\r\n" +
			"      \"environment_status\": \"Active\",\r\n" +
			"      \"be_status\": \"Active\",\r\n" +
			"      \"selected_version_datetime\": null,\r\n" +
			"      \"task_last_updated_by\": \"admin\",\r\n" +
			"      \"selected_ref_version_task_exe_id\": 0,\r\n" +
			"      \"task_execution_status\": \"Active\",\r\n" +
			"      \"sync_mode\": null,\r\n" +
			"      \"replace_sequences\": false,\r\n" +
			"      \"entity_exclusion_list\": null,\r\n" +
			"      \"environment_point_of_contact_last_name\": null,\r\n" +
			"      \"environment_point_of_contact_email\": null,\r\n" +
			"      \"be_description\": \"\",\r\n" +
			"      \"reserve_retention_period_value\": \"5\",\r\n" +
			"      \"parameters\": null,\r\n" +
			"      \"environment_expiration_date\": null,\r\n" +
			"      \"environment_point_of_contact_phone2\": null,\r\n" +
			"      \"environment_created_by\": \"admin\",\r\n" +
			"      \"roles\": [],\r\n" +
			"      \"environment_last_updated_by\": \"admin\",\r\n" +
			"      \"be_creation_date\": \"2021-11-07 11:24:17.668\",\r\n" +
			"      \"task_id\": 50,\r\n" +
			"      \"be_created_by\": \"admin\",\r\n" +
			"      \"source_environment_id\": 2,\r\n" +
			"      \"role_id_orig\": 6,\r\n" +
			"      \"scheduler\": null,\r\n" +
			"      \"environment_description\": null,\r\n" +
			"      \"selected_ref_version_datetime\": null,\r\n" +
			"      \"source_env_name\": \"\",\r\n" +
			"      \"reserve_ind\": true,\r\n" +
			"      \"task_title\": \"ReserveEntities\",\r\n" +
			"      \"fabric_environment_name\": null,\r\n" +
			"      \"environment_name\": \"TAR\",\r\n" +
			"      \"delete_before_load\": false,\r\n" +
			"      \"allow_write\": true,\r\n" +
			"      \"owner\": \"tali\",\r\n" +
			"      \"task_status\": \"Active\",\r\n" +
			"      \"retention_period_value\": null,\r\n" +
			"      \"executioncount\": 0,\r\n" +
			"      \"environment_last_updated_date\": \"2022-03-09 15:00:45.845\",\r\n" +
			"      \"be_name\": \"Customer\",\r\n" +
			"      \"version_ind\": false,\r\n" +
			"      \"task_creation_date\": \"2022-03-16 10:35:19.317\",\r\n" +
			"      \"task_globals\": false,\r\n" +
			"      \"environment_point_of_contact_first_name\": null,\r\n" +
			"      \"task_type\": \"RESERVE\",\r\n" +
			"      \"environment_creation_date\": \"2021-11-07 15:10:17.569\",\r\n" +
			"      \"owner_type\": \"ID\",\r\n" +
			"      \"creatorRoles\": [\r\n" +
			"        \"admin\"\r\n" +
			"      ]\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTasks(@param(description="list of task IDs separated by a comma") String task_ids) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		
		try{
			Db.Rows result = fnGetTasks(task_ids);
		
			String q = "SELECT * FROM " + TDMDB_SCHEMA + ".ENVIRONMENT_ROLES";
			Db.Rows rolesResult = db(TDM).fetch(q);
			
			List<Map<String, Object>> envsRoles = new ArrayList<>();
			for (Db.Row role : rolesResult) {
				ResultSet roleResultSet = role.resultSet();
				HashMap<String, Object> map = new HashMap<>();
				map.put("environment_id", roleResultSet.getInt("environment_id"));
				map.put("role_id", roleResultSet.getInt("role_id"));
				map.put("allowed_test_conn_failure", roleResultSet.getBoolean("allowed_test_conn_failure"));
				
				envsRoles.add(map);
			}
		
			Map<String, List<String>> usersRoles = new HashMap<>();
			fabric().fetch("list users;").forEach(r -> {
				List<String> roles=new ArrayList<>();
				roles.addAll(Arrays.asList(((String) r.get("roles")).split(",")));
				usersRoles.put("" + r.get("user"), roles);
			});
		
						//modified newRow will be added to newResult list
			List<Map<String,Object>> newResult=new ArrayList<>();
		
			Integer prevTaskId = 0;
			HashMap<String, Object> prevRow = new HashMap<>();
		
			for (Db.Row row:result) {
				HashMap<String, Object> newRow = new HashMap<>();
						
				ResultSet resultSet = row.resultSet();
				
				String userId = resultSet.getString("task_created_by");
				List<String> creatorfabricRoles = usersRoles.get(userId);
				
				if(creatorfabricRoles != null && creatorfabricRoles.size() > 0) {
					newRow.put("creatorRoles", creatorfabricRoles);
				}
				newRow.put("task_id",resultSet.getInt("task_id"));
				newRow.put("task_title", resultSet.getString("task_title"));
				newRow.put("task_status", resultSet.getString("task_status"));
				newRow.put("task_execution_status", resultSet.getString("task_execution_status"));
				newRow.put("num_of_entities", resultSet.getInt("num_of_entities"));
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
				newRow.put("owner_type", resultSet.getString("owner_type"));
				newRow.put("tester", resultSet.getString("tester"));
				newRow.put("tester_type", resultSet.getString("tester_type"));
				newRow.put("role_id_orig", resultSet.getInt("role_id_orig"));
				newRow.put("executioncount", resultSet.getInt("executioncount"));
				newRow.put("refcount", resultSet.getInt("refcount"));
				newRow.put("processnames", resultSet.getString("processnames"));
				newRow.put("reserve_ind",resultSet.getBoolean("reserve_ind"));
				newRow.put("reserve_retention_period_type", resultSet.getString("reserve_retention_period_type"));
				newRow.put("reserve_retention_period_value", resultSet.getString("reserve_retention_period_value"));
				newRow.put("reserve_note", resultSet.getString("reserve_note"));
				newRow.put("filterout_reserved", resultSet.getBoolean("filterout_reserved"));
				newRow.put("mask_sensitive_data", resultSet.getBoolean("mask_sensitive_data"));
		              
				Map<String, Object> task = null;
				
				if (prevTaskId == resultSet.getInt("task_id")) {
					task = prevRow;
				} else {
					prevTaskId = resultSet.getInt("task_id");
					prevRow = newRow;
				}
		
				List<Map<String, Object>> roleArr = new ArrayList<>();
				//for (Db.Row role : rolesResult) {
				for (Map<String, Object> envsRole: envsRoles)
				{
					HashMap<String, Object> roleMap = new HashMap<>();
					String envId = (envsRole.get("environment_id")).toString();
					String taskEnvId = "" + resultSet.getInt("environment_id");
					if (envId.equals(taskEnvId)) {
						roleMap.put("role_id", Integer.valueOf(envsRole.get("role_id").toString()));
						roleMap.put("allowed_test_conn_failure", Boolean.valueOf(envsRole.get("allowed_test_conn_failure").toString()));
						roleArr.add(roleMap);
					}
				}
		
				if (task != null) {
					List<Map<String, Object>> owners = (List<Map<String, Object>>) task.get("owners");
					Map<String, Object> owner = null;
					//if (!owners.contains(resultSet.getString("owner"))) {
					for (Map<String, Object> _owner :owners) {
						if (_owner.get("owner").toString().equals(resultSet.getString("owner"))) {
							owner = _owner;
						}
					}
					
					// Add owner type
					if (owner == null) {
						HashMap<String, Object> ownerMap = new HashMap<>();
						if (resultSet.getString("owner") != null) {
							ownerMap.put("owner", resultSet.getString("owner"));
							ownerMap.put("owner_type", resultSet.getString("owner_type"));
							owners.add(ownerMap);
						}
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
						if (resultSet.getString("tester") != null) {
							testerMap.put("tester", resultSet.getString("tester"));
							testerMap.put("tester_type", resultSet.getString("tester_type"));
							
							List<String> roleIdList= new ArrayList<>();
							roleIdList.add(resultSet.getString("role_id_orig"));
							testerMap.put("role_id", roleIdList);
							testers.add(testerMap);
						}
					}
		
				} else {
					List<Map<String, Object>> owners = new ArrayList<>();
					newRow.put("owners", owners);
					if (resultSet.getString("owner") != null) {
						HashMap<String, Object> ownerMap = new HashMap<>();
						ownerMap.put("owner", resultSet.getString("owner"));
						ownerMap.put("owner_type", resultSet.getString("owner_type"));
						owners.add(ownerMap);
					}
		
					List<Map<String, Object>> testers = new ArrayList<>();
					newRow.put("testers", testers);
					if (resultSet.getString("tester") != null) {
						HashMap<String, Object> testerMap = new HashMap<>();
						testerMap.put("tester", resultSet.getString("tester"));
						testerMap.put("tester_type", resultSet.getString("tester_type"));
						List<String> roleIdList= new ArrayList<>();
						roleIdList.add(resultSet.getString("role_id_orig"));
						testerMap.put("role_id", roleIdList);
						testers.add(testerMap);
					}
		
					List<List<Map<String, Object>>> roles = new ArrayList<>();
					
					if (roleArr != null && roleArr.size() > 0) {
						roles.add(roleArr);
					}
					newRow.put("roles", roles);
					newResult.add(newRow);
				}
		
				}
			if (result != null) {
				result.close();
			}
			if(rolesResult != null) {
				rolesResult.close();
			}
			errorCode="SUCCESS";
			response.put("result",newResult);
		
		} catch(Exception e){
			errorCode="FAILED";
			e.printStackTrace();
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
			String sql= "select DISTINCT task_id from " + TDMDB_SCHEMA + ".task_execution_list " +
					"where  (lower(execution_status) <> 'failed' AND lower(execution_status) <> 'completed' " +
					"AND lower(execution_status) <> 'stopped' AND lower(execution_status) <> 'killed')";
			Db.Rows rows= db(TDM).fetch(sql);
			List<String> result = new ArrayList<>();
			for(Db.Row row:rows){
				result.add(row.get("task_id").toString());
			}
			errorCode="SUCCESS";
			response.put("result",result);
			if(rows != null) {
				rows.close();
			}
		
		} catch(Exception e){
			errorCode="FAILED";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("Gets the Logical Units list of the task's Business Entity (BE) and the task's environment products. The user can select part or all of the LUs in the list in the task. For extract task, the source environment ID is sent to the API, and for load task the target environment ID is sent to the API.")
	@webService(path = "businessentity/{beId}/environment/{envId}/logicalunits", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"lu_parent_name\": \"Customer\",\r\n" +
			"      \"lu_name\": \"Billing\",\r\n" +
			"      \"lu_id\": 3,\r\n" +
			"      \"product_name\": \"Customer\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_parent_name\": null,\r\n" +
			"      \"lu_name\": \"Customer\",\r\n" +
			"      \"lu_id\": 1,\r\n" +
			"      \"product_name\": \"Customer\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_parent_name\": \"Customer\",\r\n" +
			"      \"lu_name\": \"Orders\",\r\n" +
			"      \"lu_id\": 2,\r\n" +
			"      \"product_name\": \"Customer\"\r\n" +
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
			String sql= "SELECT * FROM " + TDMDB_SCHEMA + ".product_logical_units lu " +
					"INNER JOIN " + TDMDB_SCHEMA + ".products p " +
					"ON (lu.product_id = p.product_id) " +
					"INNER JOIN " + TDMDB_SCHEMA + ".environment_products ep " +
					"ON (lu.product_id = ep.product_id " +
					"AND ep.status = \'Active\') " +
					"WHERE be_id = " + beId + " AND environment_id = " + envId;
			Db.Rows rows= db(TDM).fetch(sql);
			List<HashMap<String,Object>> result = new ArrayList<>();
		
			HashMap<String,Object> lU;
			for(Db.Row row:rows){
				ResultSet resultSet=row.resultSet();
				lU=new HashMap<>();
				lU.put("lu_id",resultSet.getInt("lu_id"));
				lU.put("lu_parent_name",resultSet.getString("lu_parent_name"));
				lU.put("lu_name",resultSet.getString("lu_name"));
				lU.put("product_name", resultSet.getString("product_name"));
				result.add(lU);
			}
		
			errorCode="SUCCESS";
			response.put("result",result);
			if(rows != null) {
				rows.close();
			}
		
		} catch(Exception e){
			errorCode="FAILED";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}

	@desc("Create a task in one transaction. Rollback the transaction in case of a failure.\r\n" +
			"\r\n" +
			"The following input parameters are mandatory:\r\n" +
			"- be_id: populated by the ID of the task's Business Entity (BE)\r\n" +
			"\r\n" +
			"- environment_id: \r\n" +
			"  - Extract task: populated by the source environment id.\r\n" +
			"  - Load task: populated by the target environment id.\r\n" +
			"\r\n" +
			"- source_environment_id: source environment ID.\r\n" +
			"\r\n" +
			"- selection_method: populated by the following values:\r\n" +
			"  - Extract task: 'L' (entity list), 'REF' (reference only task),  or 'ALL' (all entities).\r\n" +
			"  - Load task: 'L' (entity list), 'ALL', 'P' (Paramerers), 'PR' (Parameters with random selection), 'CLONE' (Cloning), or 'R' (Random).\r\n" +
			"\r\n" +
			"- task_title: task name\r\n" +
			"\r\n" +
			"- task_type: populated by 'EXTRACT' or 'LOAD'\r\n" +
			"\r\n" +
			"- logicalUnits: populated by the list of the task's logical units (LUs).\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"\r\n" +
			"{\r\n" +
			"    \"be_id\": 3,\r\n" +
			"    \"environment_id\": 1,\r\n" +
			"    \"source_environment_id\": 1,\r\n" +
			"    \"scheduler\": \"immediate\",\r\n" +
			"    \"delete_before_load\": true,\r\n" +
			"    \"request_of_fresh_data\": true,\r\n" +
			"    \"num_of_entities\": 0,\r\n" +
			"    \"selection_method\": \"R\",\r\n" +
			"    \"selection_param_value\": null,\r\n" +
			"    \"entity_exclusion_list\": \"5,9,28\",\r\n" +
			"    \"task_title\": \"taskTitle\",\r\n" +
			"    \"parameters\": null,\r\n" +
			"    \"refresh_reference_data\": true,\r\n" +
			"    \"replace_sequences\": true,\r\n" +
			"    \"source_env_name\": \"env1\",\r\n" +
			"    \"load_entity\": true,\r\n" +
			"    \"task_type\": \"LOAD\",\r\n" +
			"    \"scheduling_end_date\": \"2021-02-04 14:20:59.454\",\r\n" +
			"    \"version_ind\": true,\r\n" +
			"    \"retention_period_type\": \"Days\",\r\n" +
			"    \"retention_period_value\": 5,\r\n" +
			"    \"selected_version_task_name\": \"TaskName\",\r\n" +
			"    \"selected_version_datetime\": \"2021-02-04\",\r\n" +
			"    \"selected_version_task_exe_id\": 0,\r\n" +
			"    \"task_globals\": true,\r\n" +
			"    \"selected_ref_version_task_exe_id\": 0,\r\n" +
			"    \"selected_ref_version_datetime\": \"2021-02-04\",\r\n" +
			"    \"selected_ref_version_task_name\": null,\r\n" +
			"    \"sync_mode\": null,\r\n" +
			"    \"selectAllEntites\": true,\r\n" +
			"    \"refList\": [{\r\n" +
			"            \"reference_table_name\": \"RefT\",\r\n" +
			"            \"logical_unit_name\": \"RefLU\",\r\n" +
			"            \"schema_name\": \"RefSchema\",\r\n" +
			"            \"interface_name\": \"RefInterface\"\r\n" +
			"        }, {\r\n" +
			"            \"reference_table_name\": \"RefT2\",\r\n" +
			"            \"logical_unit_name\": \"RefLU2\",\r\n" +
			"            \"schema_name\": \"RefSchema2\",\r\n" +
			"            \"interface_name\": \"RefInterface2\"\r\n" +
			"        }\r\n" +
			"    ],\r\n" +
			"    \"globals\": [{\r\n" +
			"            \"global_name\": \"globalName1\",\r\n" +
			"            \"global_value\": \"globalValue1\"\r\n" +
			"        }, {\r\n" +
			"            \"global_name\": \"globalName2\",\r\n" +
			"            \"global_value\": \"globalValue2\"\r\n" +
			"        }\r\n" +
			"    ],\r\n" +
			"    \"reference\": \"ref\",\r\n" +
			"    \"postExecutionProcesses\": [{\r\n" +
			"            \"process_id\": 1,\r\n" +
			"            \"process_name\": \"processName\",\r\n" +
			"            \"task_id\": 145,\r\n" +
			"            \"execution_order\": 2\r\n" +
			"        }, {\r\n" +
			"            \"process_id\": 2,\r\n" +
			"            \"process_name\": \"processName2\",\r\n" +
			"            \"task_id\": 145,\r\n" +
			"            \"execution_order\": 3\r\n" +
			"        }\r\n" +
			"    ],\r\n" +
			"    \"logicalUnits\": [{\r\n" +
			"            \"lu_parent_name\": \"parentName\",\r\n" +
			"            \"lu_name\": \"name\",\r\n" +
			"            \"lu_id\": 23\r\n" +
			"        }, {\r\n" +
			"            \"lu_parent_name\": \"PATIENT_LU\",\r\n" +
			"            \"lu_name\": \"PATIENT_VISITS\",\r\n" +
			"            \"lu_id\": 12\r\n" +
			"        }, {\r\n" +
			"            \"lu_parent_name\": \"PATIENT_VISITS\",\r\n" +
			"            \"lu_name\": \"VISIT_LAB_RESULTS\",\r\n" +
			"            \"lu_id\": 16\r\n" +
			"        }\r\n" +
			"    ]\r\n" +
			"}")
	@webService(path = "task", verb = {MethodType.POST}, version = "0", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 145\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateTaskV2(@param(required=true) Long be_id, Long environment_id, Long source_environment_id, 
                                        String scheduler, Boolean delete_before_load, Integer num_of_entities, String selection_method, 
                                        String selection_param_value, String entity_exclusion_list, @param(required=true) String task_title, 
                                        String parameters, Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, 
                                        Boolean load_entity, @param(required=true) String task_type, String scheduling_end_date, Boolean version_ind, 
                                        String retention_period_type, Integer retention_period_value, String selected_version_task_name, 
                                        String selected_version_datetime, Integer selected_version_task_exe_id, Boolean task_globals, 
                                        Integer selected_ref_version_task_exe_id, String selected_ref_version_datetime, 
                                        String selected_ref_version_task_name, String sync_mode, Boolean selectAllEntites, 
                                        List<Map<String,Object>> refList, List<Map<String,Object>> globals, 
                                        String reference, List<Map<String,Object>> postExecutionProcesses, 
                                        @param(required=true) List<Map<String,Object>> logicalUnits, 
                                        Boolean reserve_ind, String reserve_retention_period_type, Integer reserve_retention_period_value, 
                                        String reserve_note, Boolean filterout_reserved, HashMap<String,Object> generateParams, Boolean mask_sensitive_data) throws Exception {
		Long taskId;
		
		if ("LOAD".equals(task_type) && !"ALL".equals(selection_method) && !"REF".equals(selection_method) && num_of_entities == null) {
			throw new IllegalArgumentException("In case the task_type is \"LOAD\" and the selection_method is not 'ALL' or 'REF' the parameter 'num_of_entities' is mandatory.");
		}
		
		db(TDM).beginTransaction();
		try {
			Map<String, Object> result = (Map<String, Object>) wsCreateTaskV1(be_id, environment_id, source_environment_id, scheduler, 
				delete_before_load, num_of_entities, selection_method, selection_param_value, entity_exclusion_list, 
				task_title, parameters, refresh_reference_data, replace_sequences, source_env_name, load_entity, task_type, 
				scheduling_end_date, version_ind, retention_period_type, retention_period_value, selected_version_task_name,
				selected_version_datetime, selected_version_task_exe_id, task_globals, selected_ref_version_task_exe_id, 
				selected_ref_version_datetime, selected_ref_version_task_name, sync_mode, selectAllEntites, refList, globals, reference,
				reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, filterout_reserved, generateParams,mask_sensitive_data);
			if (!checkWsResponse(result)) {
				db(TDM).rollback();
				return wrapWebServiceResults("FAILED", result.get("message"), null);
			} else {
				taskId = (Long) ((Map<String, Object>) result.get("result")).get("id");
			}
		
			result = (Map<String, Object>) wsCreatePostExecutionProcessesFortask(taskId, task_title, postExecutionProcesses);
			if (!checkWsResponse(result)) {
				db(TDM).rollback();
				return wrapWebServiceResults("FAILED", "Can't create post execution processes for the task: " + result.get("message"), null);
			}
		
			result = (Map<String, Object>) wsCreateLogicalUnitsFortask(taskId, task_title, logicalUnits);
			if (!checkWsResponse(result)) {
				db(TDM).rollback();
				return wrapWebServiceResults("FAILED", "Can't create logical units for the task: " + result.get("message"), null);
			}
		
		} catch (Exception e) {
			db(TDM).rollback();
			return wrapWebServiceResults("FAILED", e.getMessage(), null);
		}
		
		db(TDM).commit();
		
		Map<String, Object> result = new HashMap();
		result.put("id", taskId);
		return wrapWebServiceResults("SUCCESS", null, result);
	}

	@desc("Creates Task.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"\r\n" +
			"{\r\n" +
			"   \"filterout_reserved\":false,\r\n" +
			"   \"operationMode\":\"insert_entity_without_delete\",\r\n" +
			"   \"task_type\":\"LOAD\",\r\n" +
			"   \"task_title\":\"testapi2\",\r\n" +
			"   \"extractSelected\":false,\r\n" +
			"   \"load_entity\":true,\r\n" +
			"   \"delete_before_load\":false,\r\n" +
			"   \"reserve_ind\":true,\r\n" +
			"   \"be_id\":4,\r\n" +
			"   \"source_environment_id\":1,\r\n" +
			"   \"environment_id\":2,\r\n" +
			"   \"source_env_name\":\"SRC\",\r\n" +
			"   \"environment_name\":\"TAR\",\r\n" +
			"   \"allLogicalUnits\":[\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":null,\r\n" +
			"         \"lu_name\":\"Customer\",\r\n" +
			"         \"lu_id\":7,\r\n" +
			"         \"product_name\":\"CRM\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Billing\",\r\n" +
			"         \"lu_id\":9,\r\n" +
			"         \"product_name\":\"FINANCE\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Collection\",\r\n" +
			"         \"lu_id\":10,\r\n" +
			"         \"product_name\":\"FINANCE\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Orders\",\r\n" +
			"         \"lu_id\":8,\r\n" +
			"         \"product_name\":\"ORDERS\"\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"selectedLogicalUnits\":[\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":null,\r\n" +
			"         \"lu_name\":\"Customer\",\r\n" +
			"         \"lu_id\":7,\r\n" +
			"         \"product_name\":\"CRM\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Billing\",\r\n" +
			"         \"lu_id\":9,\r\n" +
			"         \"product_name\":\"FINANCE\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Collection\",\r\n" +
			"         \"lu_id\":10,\r\n" +
			"         \"product_name\":\"FINANCE\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Orders\",\r\n" +
			"         \"lu_id\":8,\r\n" +
			"         \"product_name\":\"ORDERS\"\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"missingRootLU\":[\r\n" +
			"      \r\n" +
			"   ],\r\n" +
			"   \"num_of_entities\":2,\r\n" +
			"   \"syncModeRadio\":null,\r\n" +
			"   \"sync_mode\":null,\r\n" +
			"   \"reserve_retention_period_value\":5,\r\n" +
			"   \"reserve_retention_period_type\":\"Days\",\r\n" +
			"   \"allPostExecutionProcess\":[\r\n" +
			"      {\r\n" +
			"         \"process_id\":1,\r\n" +
			"         \"be_id\":4,\r\n" +
			"         \"process_name\":\"PostExecFlow\",\r\n" +
			"         \"process_description\":null,\r\n" +
			"         \"execution_order\":1\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"postExecutionProcesses\":[\r\n" +
			"      1\r\n" +
			"   ],\r\n" +
			"   \"reference\":\"both\",\r\n" +
			"   \"versionsForLoad\":[\r\n" +
			"      \r\n" +
			"   ],\r\n" +
			"   \"selection_method\":\"PR\",\r\n" +
			"   \"selection_param_value\":\"(( 'Bronze' = ANY(\\\"BILLING.VIP_STATUS\\\") ))\",\r\n" +
			"   \"parameters\":\"{\\\"group\\\":{\\\"rules\\\":[{\\\"condition\\\":\\\"=\\\",\\\"field\\\":\\\"BILLING.VIP_STATUS\\\",\\\"data\\\":\\\"Bronze\\\",\\\"operator\\\":\\\"AND\\\",\\\"type\\\":\\\"text\\\",\\\"comboIndicator\\\":\\\"true\\\",\\\"validValues\\\":[\\\"Bronze\\\",\\\"Gold\\\",\\\"Platinum\\\",\\\"Silver\\\"],\\\"disableThird\\\":false}]}}\",\r\n" +
			"   \"refList\":[\r\n" +
			"      {\r\n" +
			"         \"ref_table_name\":\"DEVICESTABLE2017\",\r\n" +
			"         \"lu_name\":\"Customer\",\r\n" +
			"         \"interface_name\":\"CRM_DB\",\r\n" +
			"         \"schema_name\":\"public\",\r\n" +
			"         \"logical_unit_name\":\"Customer\",\r\n" +
			"         \"reference_table_name\":\"DEVICESTABLE2017\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"ref_table_name\":\"devicestable2017\",\r\n" +
			"         \"lu_name\":\"Customer\",\r\n" +
			"         \"interface_name\":\"CRM_DB\",\r\n" +
			"         \"schema_name\":\"public\",\r\n" +
			"         \"logical_unit_name\":\"Customer\",\r\n" +
			"         \"reference_table_name\":\"devicestable2017\",\r\n" +
			"         \"selected\":true\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"refresh_reference_data\":false,\r\n" +
			"   \"selected_version_task_name\":null,\r\n" +
			"   \"selected_version_datetime\":null,\r\n" +
			"   \"selected_version_task_exe_id\":null,\r\n" +
			"   \"selected_ref_version_task_name\":null,\r\n" +
			"   \"selected_ref_version_datetime\":null,\r\n" +
			"   \"selected_ref_version_task_exe_id\":null,\r\n" +
			"   \"scheduler\":\"immediate\"\r\n" +
			"}")
	@webService(path = "task", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 145\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateTaskV1(@param(required=true) Long be_id, Long environment_id, Long source_environment_id, String scheduler, Boolean delete_before_load, Integer num_of_entities, String selection_method, String selection_param_value, String entity_exclusion_list, @param(required=true) String task_title, String parameters, Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, Boolean load_entity, @param(required=true) String task_type, String scheduling_end_date, Boolean version_ind, String retention_period_type, Integer retention_period_value, String selected_version_task_name, String selected_version_datetime, Integer selected_version_task_exe_id, Boolean task_globals, Integer selected_ref_version_task_exe_id, String selected_ref_version_datetime, String selected_ref_version_task_name, String sync_mode, Boolean selectAllEntites, List<Map<String,Object>> refList, List<Map<String,Object>> globals, String reference, Boolean reserve_ind, String reserve_retention_period_type, Integer reserve_retention_period_value, String reserve_note, Boolean filterout_reserved, HashMap<String,Object> DataManParams, Boolean mask_sensitive_data) throws Exception {
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
		/* Keep the entity list as is, the leading and trailing spaces will be trimmed when the entities are processed,
			we should keep internal spaces
		
		if (selection_param_value != null && !"".equals(selection_param_value) && "L".equals(selection_method)) {
			selection_param_value = selection_param_value.replaceAll("\\s+","");
		}
		*/
		
		if (reference!=null&&reference.equals("refernceOnly")) {
			if (refList!=null ) {
				if(refList.size() > 0){
					selection_method = "REF";
					delete_before_load = false;
					load_entity = false;
				}
			}
			num_of_entities = 0;
			selectAllEntites = false;
		} else {
			if (num_of_entities == null || num_of_entities == 0) {
				if (selection_param_value != null && !"".equals(selection_param_value)) {
					num_of_entities = (selection_param_value.split(",")).length;
				}
			}
		}
		
		if (selectAllEntites!=null&&selectAllEntites==true) {
			selection_method = "ALL";
		}
		
		try{
			String sql= "INSERT INTO " + TDMDB_SCHEMA + ".tasks (be_id, environment_id, scheduler, delete_before_load," +
					"num_of_entities,selection_method,selection_param_value,entity_exclusion_list, task_execution_status, " +
					"task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, task_status, task_title, parameters, refresh_reference_data,replace_sequences, " +
					"source_environment_id, source_env_name, load_entity, task_type, scheduling_end_date, version_ind, retention_period_type, retention_period_value, selected_version_task_name, " +
					"selected_version_datetime, selected_version_task_exe_id,task_globals, " +
					"selected_ref_version_task_exe_id, selected_ref_version_datetime, selected_ref_version_task_name, sync_mode, reserve_ind, " + 
					"reserve_retention_period_type, reserve_retention_period_value, reserve_note, filterout_reserved, mask_sensitive_data) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING task_id";
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());
			String username=sessionUser().name();
		          if("Do Not Delete".equalsIgnoreCase(retention_period_type)){
				retention_period_value = -1;
			}
			
			
			if (source_environment_id == null) {
				source_environment_id = environment_id;	
			}
			if (environment_id == null) {
				environment_id = source_environment_id;	
			}
			
			Db.Row row = db(TDM).fetch(sql,be_id, environment_id,
					scheduler, 
		                  ((delete_before_load != null) ? delete_before_load : false),
					num_of_entities, selection_method,
					selection_param_value, entity_exclusion_list, "Active",
					username, now, now, username, "Active", task_title,
					parameters, refresh_reference_data, replace_sequences, 
					source_environment_id,
					((source_env_name != null) ? source_env_name : ""), 
					((load_entity != null) ? load_entity : false), 
		                  task_type, scheduling_end_date,
					(version_ind!=null? version_ind == true:false), retention_period_type, retention_period_value,
					selected_version_task_name, selected_version_datetime,
					selected_version_task_exe_id, task_globals, selected_ref_version_task_exe_id,
					selected_ref_version_datetime, selected_ref_version_task_name, sync_mode,
					reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, filterout_reserved, mask_sensitive_data).firstRow();
			Long taskId=Long.parseLong(row.get("task_id").toString());
		
			if (refList!=null ) {
				if(refList.size() > 0){
					fnSaveRefTablestoTask(taskId, refList);
				}
			}
		
			if (globals != null ) {
				if(globals.size() > 0 && task_globals!=null && task_globals){
					
					try {
						/* TDM 7.4 - LU Name is sent separately in Globals input
						String insertGlobalSql = "INSERT INTO " + schema + ".task_globals (task_id, global_name,global_value) VALUES (?, ?,?)";
						for(Map<String,Object> global:globals){
							db(TDM).execute(insertGlobalSql,taskId, global.get("global_name").toString(), global.get("global_value").toString());
						}*/
						for(Map<String,Object> global:globals){
							createTaskGlobals(taskId, global.get("lu_name").toString(), global.get("global_name").toString(), global.get("global_value").toString());
						}
					} catch(Exception e){
						log.error(e.getMessage());
					}
				}
			}
		
		    // TDM 8.0 - In case of Synthetic task, add the input parameters of the population flows to the TDMDB tdm_generate_task_field_mappings
			//log.info("wsCreateTaskV1 - params size: " + DataManParams.size());
			if (source_environment_id == -1 && DataManParams != null) {
				createTaskGEnerateParams(taskId, DataManParams);
			}
		
			String activityDesc = "Task " + task_title + " was created";
			fnInsertActivity("create", "Tasks", activityDesc);
		
			result.put("id",taskId);
			errorCode="SUCCESS";
			response.put("result",result);
		
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
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
			"  \"num_of_entities\": 0,\r\n" +
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
			"  \"task_creation_date\": \"2021-02-04 14:20:59.454\",\r\n" +
			"    \"postExecutionProcesses\": [{\r\n" +
			"            \"process_id\": 1,\r\n" +
			"            \"process_name\": \"processName\",\r\n" +
			"            \"task_id\": 145,\r\n" +
			"            \"execution_order\": 2\r\n" +
			"        }, {\r\n" +
			"            \"process_id\": 2,\r\n" +
			"            \"process_name\": \"processName2\",\r\n" +
			"            \"task_id\": 145,\r\n" +
			"            \"execution_order\": 3\r\n" +
			"        }\r\n" +
			"    ],\r\n" +
			"    \"logicalUnits\": [{\r\n" +
			"            \"lu_parent_name\": \"parentName\",\r\n" +
			"            \"lu_name\": \"name\",\r\n" +
			"            \"lu_id\": 23\r\n" +
			"        }, {\r\n" +
			"            \"lu_parent_name\": \"PATIENT_LU\",\r\n" +
			"            \"lu_name\": \"PATIENT_VISITS\",\r\n" +
			"            \"lu_id\": 12\r\n" +
			"        }, {\r\n" +
			"            \"lu_parent_name\": \"PATIENT_VISITS\",\r\n" +
			"            \"lu_name\": \"VISIT_LAB_RESULTS\",\r\n" +
			"            \"lu_id\": 16\r\n" +
			"        }\r\n" +
			"    ]\r\n" +
			"}")
	@webService(path = "task/{taskId}", verb = {MethodType.PUT}, version = "0", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 146\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdateTaskV2(@param(required=true) Long taskId, Boolean copy, String task_status, @param(required=true) Long be_id, Long environment_id, 
                                        Long source_environment_id, String scheduler, Boolean delete_before_load, Integer num_of_entities, String selection_method, 
                                        String selection_param_value, String entity_exclusion_list, @param(required=true) String task_title, String parameters, 
                                        Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, Boolean load_entity, 
                                        @param(required=true) String task_type, String scheduling_end_date, Boolean version_ind, String retention_period_type, 
                                        Integer retention_period_value, String selected_version_task_name, String selected_version_datetime, Integer selected_version_task_exe_id, 
                                        Boolean task_globals, Integer selected_ref_version_task_exe_id, String selected_ref_version_datetime, String selected_ref_version_task_name, 
                                        String sync_mode, Boolean selectAllEntites, List<Map<String,Object>> refList, List<Map<String,Object>> globals, String reference, 
                                        String task_created_by, String task_creation_date, List<Map<String,Object>> postExecutionProcesses, List<Map<String,Object>> logicalUnits, 
                                        Boolean reserve_ind, String reserve_retention_period_type, Integer reserve_retention_period_value, String reserve_note, Boolean filterout_reserved, 
                                        HashMap<String,Object> generateParams, Boolean mask_sensitive_data) throws Exception {
		Long newTaskId = null;
		
		db(TDM).beginTransaction();
		try {
			Map<String, Object> result = (Map<String, Object>) wsUpdateTaskV1(taskId, copy, task_status, be_id, environment_id, 
				source_environment_id, scheduler, delete_before_load, num_of_entities, selection_method, selection_param_value, 
				entity_exclusion_list, task_title, parameters, refresh_reference_data, replace_sequences, source_env_name, load_entity, 
				task_type, scheduling_end_date, version_ind, retention_period_type, retention_period_value, selected_version_task_name, 
				selected_version_datetime, selected_version_task_exe_id, task_globals, selected_ref_version_task_exe_id, selected_ref_version_datetime, 
				selected_ref_version_task_name, sync_mode, selectAllEntites, refList, globals, reference, task_created_by, task_creation_date,
				reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, filterout_reserved, generateParams,mask_sensitive_data);
			if (!checkWsResponse(result)) {
				db(TDM).rollback();
				return wrapWebServiceResults("FAILED", result.get("message"), null);
			} else {
				taskId = (Long) ((Map<String, Object>) result.get("result")).get("id");
			}
		
			result = (Map<String, Object>) wsCreatePostExecutionProcessesFortask(taskId, task_title, postExecutionProcesses);
			if (!checkWsResponse(result)) {
				db(TDM).rollback();
				return wrapWebServiceResults("FAILED", result.get("message"), null);
			}
		
			result = (Map<String, Object>) wsCreateLogicalUnitsFortask(taskId, task_title, logicalUnits);
			if (!checkWsResponse(result)) {
				db(TDM).rollback();
				return wrapWebServiceResults("FAILED", result.get("message"), null);
			}
		
		} catch (Exception e) {
			db(TDM).rollback();
			return wrapWebServiceResults("FAILED", e.getMessage(), null);
		}
		
		db(TDM).commit();
		
		Map<String, Object> result = new HashMap();
		result.put("id", newTaskId);
		return wrapWebServiceResults("SUCCESS", null, result);
	}

	@desc("Updates Task. The task update creates a new version of the task and set the status of the previous task version to Inactive.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"\r\n" +
			"{\r\n" +
			"   \"task_last_updated_date\":\"2022-12-19 12:16:48.257\",\r\n" +
			"   \"filterout_reserved\":false,\r\n" +
			"   \"be_id\":4,\r\n" +
			"   \"selected_version_task_name\":null,\r\n" +
			"   \"reserve_retention_period_type\":\"Days\",\r\n" +
			"   \"environment_id\":2,\r\n" +
			"   \"selection_method\":\"PR\",\r\n" +
			"   \"selected_ref_version_task_name\":null,\r\n" +
			"   \"refresh_reference_data\":false,\r\n" +
			"   \"tester\":\"tester2\",\r\n" +
			"   \"be_last_updated_date\":\"2022-12-19 12:12:39.838\",\r\n" +
			"   \"owners\":[\r\n" +
			"      \r\n" +
			"   ],\r\n" +
			"   \"refcount\":1,\r\n" +
			"   \"num_of_entities\":2,\r\n" +
			"   \"tester_type\":\"ID\",\r\n" +
			"   \"reserve_note\":null,\r\n" +
			"   \"load_entity\":true,\r\n" +
			"   \"selected_version_task_exe_id\":null,\r\n" +
			"   \"task_created_by\":\"admin\",\r\n" +
			"   \"be_last_updated_by\":\"admin\",\r\n" +
			"   \"scheduling_end_date\":null,\r\n" +
			"   \"environment_point_of_contact_phone1\":null,\r\n" +
			"   \"processnames\":\"PostExecFlow\",\r\n" +
			"   \"testers\":[\r\n" +
			"      {\r\n" +
			"         \"tester_type\":\"ID\",\r\n" +
			"         \"role_id\":[\r\n" +
			"            \"4\"\r\n" +
			"         ],\r\n" +
			"         \"tester\":\"tester2\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"tester_type\":\"ID\",\r\n" +
			"         \"role_id\":[\r\n" +
			"            \"3\"\r\n" +
			"         ],\r\n" +
			"         \"tester\":\"tester1\"\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"selection_param_value\":\"(( 'Bronze' = ANY(\\\"BILLING.VIP_STATUS\\\") ))\",\r\n" +
			"   \"environment_status\":\"Active\",\r\n" +
			"   \"be_status\":\"Active\",\r\n" +
			"   \"selected_version_datetime\":null,\r\n" +
			"   \"task_last_updated_by\":\"admin\",\r\n" +
			"   \"selected_ref_version_task_exe_id\":null,\r\n" +
			"   \"task_execution_status\":\"Active\",\r\n" +
			"   \"sync_mode\":null,\r\n" +
			"   \"replace_sequences\":false,\r\n" +
			"   \"entity_exclusion_list\":null,\r\n" +
			"   \"environment_point_of_contact_last_name\":null,\r\n" +
			"   \"environment_point_of_contact_email\":null,\r\n" +
			"   \"be_description\":\"\",\r\n" +
			"   \"reserve_retention_period_value\":5,\r\n" +
			"   \"parameters\":\"{\\\"group\\\":{\\\"rules\\\":[{\\\"condition\\\":\\\"=\\\",\\\"field\\\":\\\"BILLING.VIP_STATUS\\\",\\\"data\\\":\\\"Bronze\\\",\\\"operator\\\":\\\"AND\\\",\\\"type\\\":\\\"text\\\",\\\"comboIndicator\\\":\\\"true\\\",\\\"validValues\\\":[\\\"Bronze\\\",\\\"Gold\\\",\\\"Platinum\\\",\\\"Silver\\\"],\\\"disableThird\\\":false}]}}\",\r\n" +
			"   \"environment_expiration_date\":null,\r\n" +
			"   \"environment_point_of_contact_phone2\":null,\r\n" +
			"   \"environment_created_by\":\"admin\",\r\n" +
			"   \"roles\":[\r\n" +
			"      [\r\n" +
			"         {\r\n" +
			"            \"role_id\":4,\r\n" +
			"            \"allowed_test_conn_failure\":false\r\n" +
			"         },\r\n" +
			"         {\r\n" +
			"            \"role_id\":3,\r\n" +
			"            \"allowed_test_conn_failure\":false\r\n" +
			"         }\r\n" +
			"      ]\r\n" +
			"   ],\r\n" +
			"   \"environment_last_updated_by\":\"admin\",\r\n" +
			"   \"be_creation_date\":\"2022-10-19 18:42:40.301\",\r\n" +
			"   \"task_id\":87,\r\n" +
			"   \"be_created_by\":\"admin\",\r\n" +
			"   \"source_environment_id\":1,\r\n" +
			"   \"role_id_orig\":4,\r\n" +
			"   \"scheduler\":\"immediate\",\r\n" +
			"   \"environment_description\":null,\r\n" +
			"   \"selected_ref_version_datetime\":null,\r\n" +
			"   \"source_env_name\":\"SRC\",\r\n" +
			"   \"reserve_ind\":true,\r\n" +
			"   \"task_title\":\"testapi2\",\r\n" +
			"   \"fabric_environment_name\":null,\r\n" +
			"   \"environment_name\":\"TAR\",\r\n" +
			"   \"delete_before_load\":false,\r\n" +
			"   \"allow_write\":true,\r\n" +
			"   \"owner\":null,\r\n" +
			"   \"task_status\":\"Active\",\r\n" +
			"   \"executioncount\":0,\r\n" +
			"   \"environment_last_updated_date\":\"2022-12-06 10:18:49.378\",\r\n" +
			"   \"be_name\":\"Customer\",\r\n" +
			"   \"version_ind\":false,\r\n" +
			"   \"task_creation_date\":\"2022-12-19 12:16:48.257\",\r\n" +
			"   \"task_globals\":false,\r\n" +
			"   \"environment_point_of_contact_first_name\":null,\r\n" +
			"   \"task_type\":\"LOAD\",\r\n" +
			"   \"environment_creation_date\":\"2022-09-21 13:43:25.13\",\r\n" +
			"   \"owner_type\":null,\r\n" +
			"   \"creatorRoles\":[\r\n" +
			"      \"admin\"\r\n" +
			"   ],\r\n" +
			"   \"selection_method2\":\"Parameters with Random Entity Selection\",\r\n" +
			"   \"task_type2\":\"LOAD\",\r\n" +
			"   \"operation_mode\":\"Load entity\",\r\n" +
			"   \"data_type\":\"Entities and Reference\",\r\n" +
			"   \"disabled\":false,\r\n" +
			"   \"onHold\":false,\r\n" +
			"   \"reference\":\"both\",\r\n" +
			"   \"operationMode\":\"insert_entity_without_delete\",\r\n" +
			"   \"extractSelected\":true,\r\n" +
			"   \"postExecutionProcesses\":[\r\n" +
			"      1\r\n" +
			"   ],\r\n" +
			"   \"refList\":[\r\n" +
			"      {\r\n" +
			"         \"ref_table_name\":\"DEVICESTABLE2017\",\r\n" +
			"         \"lu_name\":\"Customer\",\r\n" +
			"         \"interface_name\":\"CRM_DB\",\r\n" +
			"         \"schema_name\":\"public\",\r\n" +
			"         \"logical_unit_name\":\"Customer\",\r\n" +
			"         \"reference_table_name\":\"DEVICESTABLE2017\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"ref_table_name\":\"devicestable2017\",\r\n" +
			"         \"lu_name\":\"Customer\",\r\n" +
			"         \"interface_name\":\"CRM_DB\",\r\n" +
			"         \"schema_name\":\"public\",\r\n" +
			"         \"logical_unit_name\":\"Customer\",\r\n" +
			"         \"reference_table_name\":\"devicestable2017\",\r\n" +
			"         \"selected\":true\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"globals\":[\r\n" +
			"      \r\n" +
			"   ],\r\n" +
			"   \"selectedLogicalUnits\":[\r\n" +
			"      {\r\n" +
			"         \"lu_name\":\"Customer\",\r\n" +
			"         \"lu_id\":7,\r\n" +
			"         \"task_id\":87\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_name\":\"Billing\",\r\n" +
			"         \"lu_id\":9,\r\n" +
			"         \"task_id\":87\r\n" +
			"      {\r\n" +
			"         \"lu_name\":\"Collection\",\r\n" +
			"         \"lu_id\":10,\r\n" +
			"         \"task_id\":87\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_name\":\"Orders\",\r\n" +
			"         \"lu_id\":8,\r\n" +
			"         \"task_id\":87\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"allLogicalUnits\":[\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":null,\r\n" +
			"         \"lu_name\":\"Customer\",\r\n" +
			"         \"lu_id\":7,\r\n" +
			"         \"product_name\":\"CRM\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Billing\",\r\n" +
			"         \"lu_id\":9,\r\n" +
			"         \"product_name\":\"FINANCE\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Collection\",\r\n" +
			"         \"lu_id\":10,\r\n" +
			"         \"product_name\":\"FINANCE\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"         \"lu_parent_name\":\"Customer\",\r\n" +
			"         \"lu_name\":\"Orders\",\r\n" +
			"         \"lu_id\":8,\r\n" +
			"         \"product_name\":\"ORDERS\"\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"missingRootLU\":[\r\n" +
			"      \r\n" +
			"   ],\r\n" +
			"   \"syncModeRadio\":null,\r\n" +
			"   \"allPostExecutionProcess\":[\r\n" +
			"      {\r\n" +
			"         \"process_id\":1,\r\n" +
			"         \"be_id\":4,\r\n" +
			"         \"process_name\":\"PostExecFlow\",\r\n" +
			"         \"process_description\":null,\r\n" +
			"         \"execution_order\":1\r\n" +
			"      }\r\n" +
			"   ],\r\n" +
			"   \"versionsForLoad\":[\r\n" +
			"      \r\n" +
			"   ]\r\n" +
			"}=-}")
	@webService(path = "task/{taskId}", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 146\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdateTaskV1(@param(required=true) Long taskId, Boolean copy, String task_status, @param(required=true) Long be_id, Long environment_id, 
                                        Long source_environment_id, String scheduler, Boolean delete_before_load, Integer num_of_entities, String selection_method, 
                                        String selection_param_value, String entity_exclusion_list, @param(required=true) String task_title, String parameters, 
                                        Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, Boolean load_entity, 
                                        @param(required=true) String task_type, String scheduling_end_date, Boolean version_ind, String retention_period_type, 
                                        Integer retention_period_value, String selected_version_task_name, String selected_version_datetime, Integer selected_version_task_exe_id, 
                                        Boolean task_globals, Integer selected_ref_version_task_exe_id, String selected_ref_version_datetime, String selected_ref_version_task_name, 
                                        String sync_mode, Boolean selectAllEntites, List<Map<String,Object>> refList, List<Map<String,Object>> globals, String reference, 
                                        String task_created_by, String task_creation_date, Boolean reserve_ind, String reserve_retention_period_type, Integer reserve_retention_period_value, 
                                        String reserve_note, Boolean filterout_reserved, HashMap<String,Object> DataManParams, Boolean mask_sensitive_data) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		Map<String,Object> result=new HashMap<>();
		String message=null;
		String errorCode="";
		
		try {
			db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".tasks SET " +
							"task_status=(?) WHERE task_id = " + taskId, copy != null && copy ? task_status : "Inactive");
		
		/* Keep the entity list as is, the leading and trailing spaces will be trimmed when the entities are processed,
			we should keep internal spaces
		
			if (selection_param_value != null && !"".equals(selection_param_value) && "L".equals(selection_method)) {
				selection_param_value = selection_param_value.replaceAll("\\s+","");
			}
			*/
		
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
				num_of_entities = 0;
				selectAllEntites = false;
			} else {
				if ("L".equals(selection_method) && selection_param_value != null && !"".equals(selection_param_value)) {
					int entitiesCnt = (selection_param_value.split(",")).length;
					if (num_of_entities == null || num_of_entities == 0 || num_of_entities != entitiesCnt) {
						num_of_entities = (selection_param_value.split(",")).length;
					}
				}
			}
		
			if (selectAllEntites != null && selectAllEntites) {
				selection_method = "ALL";
			}
			String sql = "INSERT INTO " + TDMDB_SCHEMA + ".tasks (be_id, environment_id, scheduler, delete_before_load," +
					"num_of_entities, selection_method,selection_param_value,entity_exclusion_list, task_execution_status," +
					"task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, task_status, " +
					"task_title, parameters,refresh_reference_data, replace_sequences, source_environment_id, source_env_name, load_entity, task_type, " +
					"scheduling_end_date, version_ind, retention_period_type, retention_period_value, selected_version_task_name, " +
					"selected_version_datetime, selected_version_task_exe_id, task_globals, " +
					"selected_ref_version_task_exe_id, selected_ref_version_datetime, selected_ref_version_task_name, sync_mode, reserve_ind, " + 
					"reserve_retention_period_type, reserve_retention_period_value, reserve_note, filterout_reserved, mask_sensitive_data) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING task_id";
			String username=sessionUser().name();
		          if("Do Not Delete".equalsIgnoreCase(retention_period_type)){
				retention_period_value = -1;
			}
			Db.Row row = db(TDM).fetch(sql, be_id, environment_id!=null?environment_id:source_environment_id,
					scheduler,
					((delete_before_load != null) ? delete_before_load : false),
					num_of_entities,
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
					source_environment_id, source_env_name, 
		                  ((load_entity != null) ? load_entity : false),
		                  task_type, scheduling_end_date, version_ind, retention_period_type,
					retention_period_value, selected_version_task_name, selected_version_datetime,
					selected_version_task_exe_id, task_globals, selected_ref_version_task_exe_id,
					selected_ref_version_datetime, selected_ref_version_task_name, sync_mode,
					reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, filterout_reserved, mask_sensitive_data).firstRow();
		
			Long id = Long.parseLong(row.get("task_id").toString());
		
			if (refList!=null ) {
				if(refList.size() > 0){
					fnSaveRefTablestoTask(id, refList);
				}
			}
		
		
			if(globals != null && globals.size() > 0 && task_globals){
				try {
					/* TDM 7.4 - LU Name is sent separately in Globals input
					String insertGlobalSql = "INSERT INTO " + schema + ".task_globals (task_id, global_name,global_value) VALUES (?, ?,?)";
					for(Map<String,Object> global:globals){
						db(TDM).execute(insertGlobalSql,id, global.get("global_name").toString(), global.get("global_value").toString());
					}*/
					for(Map<String,Object> global:globals){
							createTaskGlobals(id, global.get("lu_name").toString(), global.get("global_name").toString(), global.get("global_value").toString());
					}
		
				} catch(Exception e){
		                  e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		
		
			try {
				// TDM 8.0 - In case of Synthetic task, load the input parameters of the population flows into the TDMDB tdm_generate_task_field_mappings
				//log.info("wsUpdateTaskV1 - params size: " + DataManParams.size());
				if (source_environment_id == -1) {
					if (DataManParams == null) {
						String luList = "" + db(TDM).fetch("select STRING_AGG(lu_name, ',') from tasks_logical_units where task_id = ?", 
							taskId).firstValue();
						DataManParams = (HashMap<String, Object>)((Map<String,Object>)wsGetDMPopParams(luList,taskId)).get("result");
					}
		                  if (DataManParams != null) {
					    createTaskGEnerateParams(id, DataManParams);
		                  }
		
				}
				
				String activityDesc = "Task " + task_title + " was updated";
				fnInsertActivity("update", "Tasks", activityDesc);
			} catch(Exception e){
		              e.printStackTrace();
				log.error(e.getMessage());
			}
		
			errorCode="SUCCESS";
			result.put("id",id);
			response.put("result",result);
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Calculates the number of entities that matches the selected task's parameters. The calculation is based on the task's Business Entity (BE), source environment, and the Where statement (populated in the request Body) reflecting the selected parameters.\r\n" +
			"\r\n" +
			"Use the ANY command when checking a value of a parameter since the TDM LU parameter tables contain an array of values on each parameter.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\r\n" +
			"  \"tar_env_name\": \"TAR\",\r\n" +
			"  \"where\": \"(( 'Alise' = ANY(\\\"CUSTOMER.FIRST_NAME\\\") ))\",\r\n" +
			"  \"filterout_reserved\": false\r\n" +
			"}")
	@webService(path = "businessentity/{beId}/sourceEnv/{src_env_name}/analysiscount", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\"errorCode\":\"SUCCESS\",\"message\":null,\"result\":834}")
	public static Object wsGetAnalysiscountForBusinessEntity(@param(required=true) Long beId, @param(required=true) String src_env_name, String tar_env_name, String where, Boolean filterout_reserved) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		try {
			Object result = fnGetNumberOfMatchingEntities(where, src_env_name, tar_env_name, beId, filterout_reserved); //TDM
			response.put("result",((Map<String,Object>)result).get("result"));
			response.put("errorCode","SUCCESS");
		} catch (Exception e){
			response.put("errorCode","FAILED");
			response.put("message", e.getMessage());
			log.error("wsGetAnalysiscountForBusinessEntity - " + e.getMessage());
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
			errorCode="FAILED";
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
			errorCode="FAILED";
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
			String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".TASKS_POST_EXE_PROCESS  WHERE task_id =" + taskId;
			Db.Rows rows = db(TDM).fetch(sql);
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
			if(rows != null) {
				rows.close();
			}
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("This API gets the task's Logical Units. Note that the Business Entity (BE) ID and name are returned by /tasks API in be_id and be_name output attributes.")
	@webService(path = "task/{taskId}/logicalunits", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"lu_name\": \"Collection\",\r\n" +
			"      \"lu_id\": 23,\r\n" +
			"      \"task_id\": 291\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_name\": \"Customer\",\r\n" +
			"      \"lu_id\": 20,\r\n" +
			"      \"task_id\": 291\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_name\": \"Billing\",\r\n" +
			"      \"lu_id\": 22,\r\n" +
			"      \"task_id\": 291\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"lu_name\": \"Orders\",\r\n" +
			"      \"lu_id\": 21,\r\n" +
			"      \"task_id\": 291\r\n" +
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
			String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".tasks_logical_units WHERE task_id =" + taskId;
			Db.Rows rows = db(TDM).fetch(sql);
		
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
			if(rows != null) {
				rows.close();
			}
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("This API gets the task's Globals if they exist. Note that task_globals attribute of /tasks API indicates if the task has globals. This attribute is populated by true if the task has Globals.")
	@webService(path = "task/{taskId}/globals", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"global_name\": \"MASK_FLAG\",\r\n" +
			"      \"lu_name\": \"ALL\",\r\n" +
			"      \"task_id\": 57,\r\n" +
			"      \"global_value\": \"false\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"global_name\": \"MAIL_ADDRESS\",\r\n" +
			"      \"lu_name\": \"Customer\",\r\n" +
			"      \"task_id\": 57,\r\n" +
			"      \"global_value\": \"tali@gmail.com\"\r\n" +
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
			String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".task_globals WHERE task_globals.task_id = " + taskId;
			Db.Rows rows = db(TDM).fetch(sql);
		
			List<Map<String,Object>> result=new ArrayList<>();

			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				HashMap<String, Object> global = new HashMap<>();
				String luName = "ALL";
				String globalName = "" + resultSet.getString("global_name");
				if (globalName.contains(".")) {
					String[] arr = globalName.split("\\.");
					luName = arr[0];
					globalName = arr[1];
				}
				global.put("task_id", resultSet.getLong("task_id"));
				global.put("lu_name", luName);
				global.put("global_name", globalName);
				global.put("global_value", resultSet.getString("global_value"));
				result.add(global);
			}
			response.put("result",result);
			errorCode="SUCCESS";
			if(rows != null) {
				rows.close();
			}
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the list of available Reference tables for a task based on the task's Logical Units (LUs). Task type can be either LOAD or EXTRACT.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\"task_type\":\"LOAD\",\"logicalUnits\":[\"Customer\",\"Orders\"]}")
	@webService(path = "task/getReferenceTaskTable", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\"result\":[{\"ref_table_name\":\"CUSTOMER_TYPE\",\"lu_name\":\"Customer\",\"interface_name\":\"CRM_DB\",\"schema_name\":\"public\"}],\"errorCode\":\"SUCCESS\",\"message\":null}")
	public static Object wsGetReferenceTaskTable(List<String> logicalUnits, String task_type, Boolean version_ind) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
		
			if (task_type.equals("EXTRACT") || (task_type.equals("LOAD") && (version_ind == null || !version_ind))){
				String luName;
				String prevLuName="";
				List<Object> refTablesList = new ArrayList<Object>();
				List<String> luNamesList = logicalUnits;
				String broadwayCommand = "broadway TDM.refListLookup;";
				Db.Rows rows = fabric().fetch(broadwayCommand);
				for(Db.Row row:rows){
					Iterable<? extends Map<?, ?>> maps = ParamConvertor.toIterableOf(row.get("result"), ParamConvertor::toMap);
					for (Map<?,?> map : maps) {
						luName =  "" + map.get("lu_name");
						if (luNamesList.contains(luName)) {
							Map<String, Object> refInfo = new HashMap<>();
							refInfo.put("logical_unit_name", luName);
							Set fields = map.keySet();
							for(Object f:fields){
								refInfo.put((String) f, map.get(f));
						}
						refTablesList.add(refInfo);
					}
				}
		
				}
						List<Object> ans=new ArrayList<>();
				for(Object ref:refTablesList){
					((Map<String,Object>)ref).put("reference_table_name", ((Map<String,String>)ref).get("reference_table_name").trim());
					ans.add(ref);
				}
		
				response.put("result", ans);
				if(rows != null) {
					rows.close();
				}
			}
			else if (task_type.equals("LOAD") && version_ind != null && version_ind){
				List<Map<String,Object>> resultList = fnGetRefTableForLoadWithVersion(logicalUnits);
				response.put("result", resultList);
			}
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Get the list of the available data versions that match the input parameters. The user can select one of the data versions for the Data Versioning load task and reload the entities in the selected versions to the target environment. The API filters our expired data versions. For example, a data version that is created with a retention period of 5 days, will no longer be able to be retrieved after 6 days have passed.\r\n" +
			"\r\n" +
			"Note that the from date and to date inputs must be populated with \"MM.DD.YYYY\" or \"MM-DD-YYYYY\" date formats. \r\n" +
			"\r\n" +
			"Request BODY examples:\r\n" +
			"\r\n" +
			"{\r\n" +
			"\t\"fromDate\":\"2022-02-18\",\r\n" +
			"\t\"toDate\":\"2022-03-21\",\r\n" +
			"\t\"entitiesList\":\"\",\r\n" +
			"\t\"lu_list\":[\r\n" +
			"\t\t{\"lu_name\":\"Customer\"},\r\n" +
			"\t\t{\"lu_name\":\"Billing\"},\r\n" +
			"\t\t{\"lu_name\":\"Orders\"},\r\n" +
			"\t\t{\"lu_name\":\"Collection\"}],\r\n" +
			"\t\"source_env_name\":\"TAR\",\r\n" +
			"\t\"target_env_name\":\"TAR\",\"be_id\":1\r\n" +
			"}\r\n" +
			"\r\n" +
			"\r\n" +
			"{\r\n" +
			"\t\"fromDate\":\"2022-02-18\",\r\n" +
			"\t\"toDate\":\"2022-03-21\",\r\n" +
			"\t\"entitiesList\":\"1, 2\",\r\n" +
			"\t\"lu_list\":[\r\n" +
			"\t\t{\"lu_name\":\"Customer\"},\r\n" +
			"\t\t{\"lu_name\":\"Billing\"},\r\n" +
			"\t\t{\"lu_name\":\"Orders\"},\r\n" +
			"\t\t{\"lu_name\":\"Collection\"}],\r\n" +
			"\t\"source_env_name\":\"TAR\",\r\n" +
			"\t\"target_env_name\":\"TAR\",\"be_id\":1\r\n" +
			"}")
	@webService(path = "tasks/versionsForLoad", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"EntityReservationValidations\": {},\r\n" +
			"    \"ListOfVersions\": [\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 59,\r\n" +
			"        \"task_execution_id\": 208,\r\n" +
			"        \"task_last_updated_by\": \"harry\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 104,\r\n" +
			"        \"version_datetime\": \"2022-03-20 11:25:31.229\",\r\n" +
			"        \"lu_name\": \"Collection\",\r\n" +
			"        \"fabric_execution_id\": \"41079b2d-55ab-4ed7-9f74-e18941d5296e\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 104,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 59,\r\n" +
			"        \"task_execution_id\": 208,\r\n" +
			"        \"task_last_updated_by\": \"harry\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 221,\r\n" +
			"        \"version_datetime\": \"2022-03-20 11:25:31.229\",\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"fabric_execution_id\": \"eb4f3157-3c79-4138-a719-97840f8e3d0e\",\r\n" +
			"        \"root_indicator\": \"Y\",\r\n" +
			"        \"num_of_succeeded_entities\": 221,\r\n" +
			"        \"num_of_failed_entities\": 279,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 59,\r\n" +
			"        \"task_execution_id\": 208,\r\n" +
			"        \"task_last_updated_by\": \"harry\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 748,\r\n" +
			"        \"version_datetime\": \"2022-03-20 11:25:31.229\",\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"fabric_execution_id\": \"30622d98-d983-40eb-884b-c496642aa7c2\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 748,\r\n" +
			"        \"num_of_failed_entities\": 27,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 59,\r\n" +
			"        \"task_execution_id\": 208,\r\n" +
			"        \"task_last_updated_by\": \"harry\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 1048,\r\n" +
			"        \"version_datetime\": \"2022-03-20 11:25:31.229\",\r\n" +
			"        \"lu_name\": \"Orders\",\r\n" +
			"        \"fabric_execution_id\": \"2da473d7-c795-443b-9b90-610c958a19b2\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 1048,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 209,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 112,\r\n" +
			"        \"version_datetime\": \"2022-03-20 13:25:31.230\",\r\n" +
			"        \"lu_name\": \"Collection\",\r\n" +
			"        \"fabric_execution_id\": \"8b6aef81-f632-48c7-8db6-2423cfb1a23f\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 112,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 2\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 209,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 253,\r\n" +
			"        \"version_datetime\": \"2022-03-20 13:25:31.230\",\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"fabric_execution_id\": \"ef84e092-9683-4f5a-b494-9d06b8aee711\",\r\n" +
			"        \"root_indicator\": \"Y\",\r\n" +
			"        \"num_of_succeeded_entities\": 253,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 2\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 209,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 861,\r\n" +
			"        \"version_datetime\": \"2022-03-20 13:25:31.230\",\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"fabric_execution_id\": \"665d6de7-caca-4d52-9e6a-aca57ef28994\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 861,\r\n" +
			"        \"num_of_failed_entities\": 27,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 2\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 209,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 1109,\r\n" +
			"        \"version_datetime\": \"2022-03-20 13:25:31.230\",\r\n" +
			"        \"lu_name\": \"Orders\",\r\n" +
			"        \"fabric_execution_id\": \"4790ccfa-37d5-48f8-8b15-e1fc7a041ed6\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 1109,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 2\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 210,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 104,\r\n" +
			"        \"version_datetime\": \"2022-03-20 13:34:05.077\",\r\n" +
			"        \"lu_name\": \"Collection\",\r\n" +
			"        \"fabric_execution_id\": \"0f0c893c-182c-4bb8-938d-0529064b21c2\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 104,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 3\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 210,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 221,\r\n" +
			"        \"version_datetime\": \"2022-03-20 13:34:05.077\",\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"fabric_execution_id\": \"63644180-f674-4d21-a0b1-05677dc39fb5\",\r\n" +
			"        \"root_indicator\": \"Y\",\r\n" +
			"        \"num_of_succeeded_entities\": 221,\r\n" +
			"        \"num_of_failed_entities\": 279,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 3\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 210,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 748,\r\n" +
			"        \"version_datetime\": \"2022-03-20 13:34:05.077\",\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"fabric_execution_id\": \"e18117d0-c599-4e11-ab60-2cc153c2cbd6\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 748,\r\n" +
			"        \"num_of_failed_entities\": 27,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 3\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"version_name\": \"createCustSnapshot\",\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 210,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 1048,\r\n" +
			"        \"version_datetime\": \"2022-03-20 13:34:05.077\",\r\n" +
			"        \"lu_name\": \"Orders\",\r\n" +
			"        \"fabric_execution_id\": \"8c27aeb7-ccd8-4b7a-bb2a-d2cfddcc90a3\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 1048,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 3\r\n" +
			"      }\r\n" +
			"    ]\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetVersionsForLoad(String entitiesList, Long be_id, String source_env_name, String fromDate, String toDate, List<Map<String,Object>> lu_list, String target_env_name) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			//TDM 7.4 - 26/01/2022 - In addition to the returned versions, the inner function and the API will return the resevation info of the requested entities.
			//Db.Rows rows = fnGetVersionsForLoad(entitiesList, be_id, source_env_name, fromDate, toDate, lu_list, target_env_name);
			entitiesList = entitiesList.replaceAll("\\s+","");
			Map<String, Object> versions = fnGetVersionsForLoad(entitiesList, be_id, source_env_name, fromDate, toDate, lu_list, target_env_name);

			Map<String, Object> validations = (Map<String, Object>)versions.get("EntityReservationValidations");
			message = (String) validations.get("message");
			response.put("result",versions);
			
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			e.printStackTrace();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Returns the list of executions of the input task based on the input filtering parameters. Returns one summary record for each execution.")
	@webService(path = "task/{taskId}/summary", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
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
			"      \"task_executed_by\": \"admin\"\r\n" +
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
			"      \"task_executed_by\": \"admin\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetSummaryTaskHistory(@param(required=true) Long taskId, @param(description="Limits the number of task executions returned by the API. For example, if this parameter is populated by 5, the API only returns the last five task executions.") Long numberOfExecutions, @param(description="Returns the task executions of the input user if populated. Else, returns the task executions of all users.") String userId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			String query = "SELECT DISTINCT summary.*, envs.environment_name, be.be_name, list.execution_note " +
			"FROM " + TDMDB_SCHEMA + ".task_execution_summary AS summary " +
			"INNER JOIN " + TDMDB_SCHEMA + ".task_execution_list AS list ON summary.task_execution_id = list.task_execution_id and list.parent_lu_id is null " +
			"INNER JOIN " + TDMDB_SCHEMA + ".environments AS envs ON summary.environment_id = envs.environment_id " +
			"LEFT JOIN " + TDMDB_SCHEMA + ".business_entities AS be ON (summary.be_id = be.be_id) " +
			"WHERE summary.task_id = " + taskId +
					(userId != null ? " AND summary.task_executed_by = '" + userId + "'" : "") +
					(numberOfExecutions != null ? " ORDER BY end_execution_time DESC LIMIT " + numberOfExecutions + "" : "");
		
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
		
			response.put("result", result);
			errorCode = "SUCCESS";
			rows.close();

		} catch (Exception e) {
			message = e.getMessage();
			log.error(message);
			errorCode = "FAILED";
		}
		response.put("errorCode", errorCode);
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
	@webService(path = "taskStats", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
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
	public static Object wsGetTaskStats(String targetId, String parentTargetId, String taskExecutionId, String lu_name, String entityId, String type) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		// TDM 7.3 - Get Max number of entries to be returned from Global
		Integer maxEntitiesSize = Integer.parseInt(MAX_NUMBER_OF_ENTITIES_IN_LIST);
		
		if (targetId != null || parentTargetId != null) {

			try {
				Map<String, Map> statsData = (Map<String, Map>)((Map<String, Object>) fnGetTDMTaskExecutionStats(taskExecutionId.toString(),
								lu_name, targetId != null ? targetId : null, "ENTITY",parentTargetId != null ? parentTargetId : null, maxEntitiesSize, "true")).get("result");
				response.put("errorCode", "SUCCESS");
				response.put("message", null);
				HashMap<String, Object> data = new HashMap<>();
				data.put("data", statsData);
				response.put("result", data);
				return response;
			} catch (Exception e) {
				response.put("errorCode", "FAILED");
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
									null, "ENTITY", null, maxEntitiesSize, "true")).get("result");
					Map<String,Object> dataObj=new HashMap<>();
					dataObj.put("data",statsData);
					taskStatsAns.put(root_lu.get("lu_name").toString(), dataObj);
				}
				//iterate through root lus
				for (String key : taskStatsAns.keySet()) {
					Map<String,Object> statsObj = (Map<String,Object>)taskStatsAns.get(key);
					if (statsObj == null) {
						continue;
					}
					//tree.get(0).put("test1",true);
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
						//node.put("test",true);
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
								entityId!=null?entityId:null, "ENTITY", null, maxEntitiesSize, "true")).get("result");
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
			response.put("errorCode", "FAILED");
			response.put("message", e.getMessage());
			return response;
		}
	}



	@desc("Returns the list children LUs of a parent LU in a task execution. This API is invoked when navigating the Logical Units Hierarchy  tab of a selected task execution.")
	@webService(path = "luChildren", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
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
		            "(select count(*) from " + TDMDB_SCHEMA + ".task_Execution_list t where parent_lu_id = l.lu_id and " +
		            "t.task_execution_id = " + taskExecutionId + "), " +
		            "case when (num_of_failed_entities > 0 or num_of_failed_ref_tables > 0) " +
		            "then 'failed' else 'completed' end lu_status from " + TDMDB_SCHEMA + ".task_Execution_list t, " +
		            "(select lu_id, lu_name, lu_parent_name from " + TDMDB_SCHEMA + ".product_logical_units) l " +
		            "where t.task_execution_id =" + taskExecutionId + " and " +
		            "l.lu_parent_name = '" + lu_name + "' and t.lu_id = l.lu_id ";
			Db.Rows rows = db(TDM).fetch(query);
		
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
			if(rows != null) {
				rows.close();
			}
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Returns the list of all executed Logical Units and Post Execution Processes of the input task execution id.")
	@webService(path = "task/{taskExeId}/history", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_id\": 2,\r\n" +
			"      \"selected_version_task_name\": null,\r\n" +
			"      \"selected_ref_version_task_name\": null,\r\n" +
			"      \"num_of_copied_entities\": 0,\r\n" +
			"      \"fabric_execution_id\": \"e51b4541-a24b-4f0d-a22c-9294ef7e55d1\",\r\n" +
			"      \"be_last_updated_date\": \"2021-04-19 07:38:03.456\",\r\n" +
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
			"      \"num_of_entities\": 1,\r\n" +
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
			"      \"refcount\": 0,\r\n" +
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
					"( SELECT COUNT(*) FROM " + TDMDB_SCHEMA + ".task_ref_tables rt WHERE rt.task_id = el.task_id " +
					"and rt.lu_name in (select lu_name from " + TDMDB_SCHEMA + ".tasks_logical_units lu " +
					"where  lu.lu_id = el.lu_id)) AS refcount " +
					"FROM " + TDMDB_SCHEMA + ".task_execution_list el " +
					"LEFT JOIN " + TDMDB_SCHEMA + ".products p " +
					"ON (el.product_id = p.product_id) " +
					"INNER JOIN " + TDMDB_SCHEMA + ".tasks t " +
					"ON (el.task_id = t.task_id) " +
					"INNER JOIN " + TDMDB_SCHEMA + ".environments e " +
					"ON (el.environment_id = e.environment_id) " +
					"LEFT JOIN " + TDMDB_SCHEMA + ".business_entities be " +
					"ON (el.be_id = be.be_id) " +
					"LEFT JOIN " + TDMDB_SCHEMA + ".product_logical_units plu " +
					"ON (el.lu_id = plu.lu_id) " +
					"LEFT JOIN " + TDMDB_SCHEMA + ".TASKS_POST_EXE_PROCESS pep " +
					"ON (el.process_id = pep.process_id AND el.task_id = pep.task_id) " +
					"WHERE el.task_execution_id = " + taskExeId;
			Db.Rows rows = db(TDM).fetch(query);
		
			List<Map<String,Object>> result=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					if ("task_executed_by".equalsIgnoreCase(columnName)) {
						String[] userData = resultSet.getObject(columnName).toString().split("##");
						rowMap.put(columnName, userData[0]);
					} else {
						rowMap.put(columnName, resultSet.getObject(columnName));
					}
				}
				Map <String, Object> taskOverrideAttrs = fnGetTaskExecOverrideAttrs(
						(Long) row.get("task_id"), (Long) row.get("task_execution_id"));
				String overrideValue = "";
				for (String attrName : taskOverrideAttrs.keySet()) {
					Boolean entityListFlag = false;
					
					if (!"task_globals".equalsIgnoreCase(attrName)) {
						overrideValue = "" + taskOverrideAttrs.get(attrName);
						//log.info("getTaskProperties - attrName: " + attrName + ", overrideValue: " + overrideValue);
						attrName = attrName.toLowerCase();
						switch (attrName) {
							case "selection_method":
								rowMap.put(attrName, overrideValue);
								break;
							case "entity_list" :
								rowMap.put("selection_param_value", overrideValue);
								int numberOfEntities = overrideValue.split(",", -1).length;
								rowMap.put("num_of_entities", numberOfEntities);
								entityListFlag = true;
								break;
							case "no_of_entities" : 
								if (!entityListFlag) {
									rowMap.put("num_of_entities", overrideValue);
								}
								break;
							case "source_environment_name":
								rowMap.put(attrName, overrideValue);
								String srcEnvId = "" + db(TDM).fetch("select environment_id from " + TDMDB_SCHEMA + ".environments " +
									"where environment_name = ? and lower(environment_status) = 'active'", overrideValue).firstValue();
								rowMap.put("source_environment_id", srcEnvId);
								
								break;
							case "target_environment_name":
								rowMap.put(attrName, overrideValue);
								String tarEnvId = "" + db(TDM).fetch("select environment_id from " + TDMDB_SCHEMA + ".environments " +
									"where environment_name = ? and lower(environment_status) = 'active'", overrideValue).firstValue();
								rowMap.put("environment_id", tarEnvId);
								break;
							// TDM 7.4 - 16-Jan-22 - Add support for overriding DataFlux parameters
							case "selected_version_task_exe_id":
								rowMap.put(attrName, overrideValue);
								break;
							case "dataflux_retention_params":
								Map rentionPeriodInfo = Json.get().fromJson((String) overrideValue, Map.class);
								rowMap.put("retention_period_type", "" + rentionPeriodInfo.get("unit"));
								rowMap.put("retention_period_value", "" + rentionPeriodInfo.get("value"));
								break;
							case "reserve_ind":
								rowMap.put("reserve_ind", overrideValue);
								break;
							case "reserve_retention_params":
								Map reserveRentionPeriodInfo = Json.get().fromJson((String) overrideValue, Map.class);
								rowMap.put("reserve_retention_period_type", "" + reserveRentionPeriodInfo.get("unit"));
								rowMap.put("reserve_retention_period_value", "" + reserveRentionPeriodInfo.get("value"));
								break;
							// TDM 7.4 - End of Change
							default:
								rowMap.put(attrName, overrideValue);
								break;
						}
					}
				}
		
								result.add(rowMap);
			}
		
		
			response.put("result",result);
			errorCode="SUCCESS";
			if(rows != null) {
				rows.close();
			}
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Gets the task execution summary report.")
	@webService(path = "taskSummaryReport/{executionId}/luName/{luName}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "Populate the LU Name with ALL:\r\n" +
			"==========================\r\n" +
			"\r\n" +
			"{\r\n" +
			"  \"result\": {\r\n" +
			"    \"General Info\": [\r\n" +
			"      {\r\n" +
			"        \"task_name\": \"createTaskByTester\",\r\n" +
			"        \"task_id\": 294,\r\n" +
			"        \"task_execution_id\": \"490\",\r\n" +
			"        \"created_by\": \"tali\",\r\n" +
			"        \"executed_by\": \"admin\",\r\n" +
			"        \"start_execution\": \"2021-06-16 16:24:21.0\",\r\n" +
			"        \"end_execution\": \"2021-06-16 16:24:32.0\",\r\n" +
			"        \"execution_status\": \"completed\",\r\n" +
			"        \"source_env\": \"SRC\",\r\n" +
			"        \"target_env\": \"TAR\",\r\n" +
			"        \"be_name\": \"CUSTOMER\",\r\n" +
			"        \"task_type\": \"LOAD\",\r\n" +
			"        \"selection_method\": \"Randon Selection\",\r\n" +
			"        \"task_sync_mode\": null,\r\n" +
			"        \"env_sync_mode\": \"ON\",\r\n" +
			"        \"operation_mode\": \"Delete and load entity\",\r\n" +
			"        \"replace_sequences\": \"false\",\r\n" +
			"        \"version_ind\": \"false\",\r\n" +
			"        \"selected_version_task_name\": null,\r\n" +
			"        \"selected_version_datetime\": null,\r\n" +
			"        \"selected_ref_version_task_name\": null,\r\n" +
			"        \"selected_ref_version_datetime\": null,\r\n" +
			"        \"scheduling_parameters\": \"immediate\",\r\n" +
			"        \"schedule_expiration_date\": null,\r\n" +
			"        \"override_parameters\": \"{\\\"TASK_GLOBALS\\\":{\\\"MASKING_FLAG\\\":\\\"0\\\", \\\"GLOBAL2\\\":\\\"aaaaaa\\\"},\\\"ENTITY_LIST\\\":\\\"1\\\"}\",\r\n" +
			"        \"reserve_ind\": \"true\",\r\n" +
			"        \"reserve_retention_period_type\": \"Days\",\r\n" +
			"        \"reserve_retention_period_value\": null\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"Source Environment\": {\r\n" +
			"      \"Environment Name\": [\r\n" +
			"        {\r\n" +
			"          \"name\": \"SRC\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"Source Environment Products\": [\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"BILLING\",\r\n" +
			"          \"source_product_version\": \"PROD\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"COLLECTION\",\r\n" +
			"          \"source_product_version\": \"1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"CRM\",\r\n" +
			"          \"source_product_version\": \"1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"ORDERING\",\r\n" +
			"          \"source_product_version\": \"1\"\r\n" +
			"        }\r\n" +
			"      ]\r\n" +
			"    },\r\n" +
			"    \"Target Environment\": {\r\n" +
			"      \"Environment Name\": [\r\n" +
			"        {\r\n" +
			"          \"name\": \"TAR\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"Target Environment Products\": [\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"BILLING\",\r\n" +
			"          \"target_product_version\": \"PROD\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"COLLECTION\",\r\n" +
			"          \"target_product_version\": \"1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"CRM\",\r\n" +
			"          \"target_product_version\": \"1.5\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"ORDERING\",\r\n" +
			"          \"target_product_version\": \"1\"\r\n" +
			"        }\r\n" +
			"      ]\r\n" +
			"    },\r\n" +
			"    \"Task Execution Summary\": [\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"fabric_execution_id\": \"aced32c8-7d2c-43cd-a7ba-f363733f59e7\",\r\n" +
			"        \"parent_lu_name\": \"null\",\r\n" +
			"        \"data_center_name\": \"null\",\r\n" +
			"        \"start_execution_time\": \"2021-06-16 16:24:21.0\",\r\n" +
			"        \"end_execution_time\": \"2021-06-16 16:24:22.0\",\r\n" +
			"        \"num_of_processed_entities\": 5,\r\n" +
			"        \"num_of_copied_entities\": 5,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"num_of_processed_ref_tables\": 0,\r\n" +
			"        \"num_of_copied_ref_tables\": 0,\r\n" +
			"        \"num_of_failed_ref_tables\": 0\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Collection\",\r\n" +
			"        \"fabric_execution_id\": \"4f4e3609-1b9b-418b-b059-ceb38b4e93eb\",\r\n" +
			"        \"parent_lu_name\": \"Customer\",\r\n" +
			"        \"data_center_name\": \"null\",\r\n" +
			"        \"start_execution_time\": \"2021-06-16 16:24:31.0\",\r\n" +
			"        \"end_execution_time\": \"2021-06-16 16:24:32.0\",\r\n" +
			"        \"num_of_processed_entities\": 3,\r\n" +
			"        \"num_of_copied_entities\": 3,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"num_of_processed_ref_tables\": 0,\r\n" +
			"        \"num_of_copied_ref_tables\": 0,\r\n" +
			"        \"num_of_failed_ref_tables\": 0\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"fabric_execution_id\": \"8868047e-0aa4-4793-a2f2-d41f8a24a94b\",\r\n" +
			"        \"parent_lu_name\": \"Customer\",\r\n" +
			"        \"data_center_name\": \"null\",\r\n" +
			"        \"start_execution_time\": \"2021-06-16 16:24:31.0\",\r\n" +
			"        \"end_execution_time\": \"2021-06-16 16:24:32.0\",\r\n" +
			"        \"num_of_processed_entities\": 17,\r\n" +
			"        \"num_of_copied_entities\": 17,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"num_of_processed_ref_tables\": 0,\r\n" +
			"        \"num_of_copied_ref_tables\": 0,\r\n" +
			"        \"num_of_failed_ref_tables\": 0\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Orders\",\r\n" +
			"        \"fabric_execution_id\": \"d423dcfc-e175-4aed-b31f-0ca55e59f095\",\r\n" +
			"        \"parent_lu_name\": \"Customer\",\r\n" +
			"        \"data_center_name\": \"null\",\r\n" +
			"        \"start_execution_time\": \"2021-06-16 16:24:31.0\",\r\n" +
			"        \"end_execution_time\": \"2021-06-16 16:24:32.0\",\r\n" +
			"        \"num_of_processed_entities\": 27,\r\n" +
			"        \"num_of_copied_entities\": 27,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"num_of_processed_ref_tables\": 0,\r\n" +
			"        \"num_of_copied_ref_tables\": 0,\r\n" +
			"        \"num_of_failed_ref_tables\": 0\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"List of Root Entities\": {\r\n" +
			"      \"Number of Copied Entities\": [\r\n" +
			"        {\r\n" +
			"          \"number_of_copied_root_entities\": 5\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"List of Copied Entities\": [\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_36\",\r\n" +
			"          \"target_id\": \"36\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_532\",\r\n" +
			"          \"target_id\": \"532\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_577\",\r\n" +
			"          \"target_id\": \"577\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_627\",\r\n" +
			"          \"target_id\": \"627\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_794\",\r\n" +
			"          \"target_id\": \"794\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"Number of Failed Entities\": [\r\n" +
			"        {\r\n" +
			"          \"number_of_failed_root_entities\": 0\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"List of Failed Entities\": []\r\n" +
			"    },\r\n" +
			"    \"List of Reference Tables\": {\r\n" +
			"      \"Number of Copied Reference Tables\": [\r\n" +
			"        {\r\n" +
			"          \"count\": 0\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"List of Copied Reference Tables\": [],\r\n" +
			"      \"Number of Failed Reference Tables\": [\r\n" +
			"        {\r\n" +
			"          \"count\": 0\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"List of Failed Reference Tables\": []\r\n" +
			"    },\r\n" +
			"    \"Error Summary\": [],\r\n" +
			"    \"Error Details\": [],\r\n" +
			"    \"Statistics Report\": [\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"table_name\": \"BALANCE\",\r\n" +
			"        \"target_count\": \"172\",\r\n" +
			"        \"source_count\": \"172\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"table_name\": \"INVOICE\",\r\n" +
			"        \"target_count\": \"107\",\r\n" +
			"        \"source_count\": \"107\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"table_name\": \"OFFER\",\r\n" +
			"        \"target_count\": \"9\",\r\n" +
			"        \"source_count\": \"9\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"table_name\": \"SUBSCRIBER\",\r\n" +
			"        \"target_count\": \"17\",\r\n" +
			"        \"source_count\": \"17\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Collection\",\r\n" +
			"        \"table_name\": \"COLLECTION\",\r\n" +
			"        \"target_count\": \"3\",\r\n" +
			"        \"source_count\": \"3\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"table_name\": \"ACTIVITY\",\r\n" +
			"        \"target_count\": \"26\",\r\n" +
			"        \"source_count\": \"26\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"table_name\": \"ADDRESS\",\r\n" +
			"        \"target_count\": \"5\",\r\n" +
			"        \"source_count\": \"5\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"table_name\": \"CASE_NOTE\",\r\n" +
			"        \"target_count\": \"35\",\r\n" +
			"        \"source_count\": \"35\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"table_name\": \"CASES\",\r\n" +
			"        \"target_count\": \"16\",\r\n" +
			"        \"source_count\": \"16\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"table_name\": \"CONTRACT\",\r\n" +
			"        \"target_count\": \"17\",\r\n" +
			"        \"source_count\": \"17\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"table_name\": \"CUSTOMER\",\r\n" +
			"        \"target_count\": \"5\",\r\n" +
			"        \"source_count\": \"5\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Orders\",\r\n" +
			"        \"table_name\": \"ORDERS\",\r\n" +
			"        \"target_count\": \"27\",\r\n" +
			"        \"source_count\": \"27\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"Replace Sequence Summary Report\": []\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": \"\"\r\n" +
			"}\r\n" +
			"\r\n" +
			"Populate the LU Name with a Logical Unit:\r\n" +
			"==================================\r\n" +
			"\r\n" +
			"{\r\n" +
			"  \"result\": {\r\n" +
			"    \"General Info\": [\r\n" +
			"      {\r\n" +
			"        \"task_name\": \"createTaskByTester\",\r\n" +
			"        \"task_id\": 294,\r\n" +
			"        \"task_execution_id\": \"490\",\r\n" +
			"        \"created_by\": \"tali\",\r\n" +
			"        \"executed_by\": \"admin\",\r\n" +
			"        \"start_execution\": \"2021-06-16 16:24:21.0\",\r\n" +
			"        \"end_execution\": \"2021-06-16 16:24:32.0\",\r\n" +
			"        \"execution_status\": \"completed\",\r\n" +
			"        \"source_env\": \"SRC\",\r\n" +
			"        \"target_env\": \"TAR\",\r\n" +
			"        \"be_name\": \"CUSTOMER\",\r\n" +
			"        \"task_type\": \"LOAD\",\r\n" +
			"        \"selection_method\": \"Randon Selection\",\r\n" +
			"       \t\"task_sync_mode\": null,\r\n" +
			"        \"env_sync_mode\": \"ON\",\r\n" +
			"        \"operation_mode\": \"Delete and load entity\",\r\n" +
			"        \"replace_sequences\": \"false\",\r\n" +
			"        \"version_ind\": \"false\",\r\n" +
			"        \"selected_version_task_name\": null,\r\n" +
			"        \"selected_version_datetime\": null,\r\n" +
			"        \"selected_ref_version_task_name\": null,\r\n" +
			"        \"selected_ref_version_datetime\": null,\r\n" +
			"        \"scheduling_parameters\": \"immediate\",\r\n" +
			"        \"schedule_expiration_date\": null,\r\n" +
			"        \"override_parameters\": null,\r\n" +
			"        \"reserve_ind\": \"false\",\r\n" +
			"        \"reserve_retention_period_type\": null,\r\n" +
			"        \"reserve_retention_period_value\": null,\r\n" +
			"        \"fabric_execution_id\": \"c30a97f2-fc3e-4b66-9986-aa0c6108456a\"\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"Source Environment\": {\r\n" +
			"      \"Environment Name\": [\r\n" +
			"        {\r\n" +
			"          \"name\": \"SRC\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"Source Environment Products\": [\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"BILLING\",\r\n" +
			"          \"source_product_version\": \"PROD\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"COLLECTION\",\r\n" +
			"          \"source_product_version\": \"1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"CRM\",\r\n" +
			"          \"source_product_version\": \"1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"ORDERING\",\r\n" +
			"          \"source_product_version\": \"1\"\r\n" +
			"        }\r\n" +
			"      ]\r\n" +
			"    },\r\n" +
			"    \"Target Environment\": {\r\n" +
			"      \"Environment Name\": [\r\n" +
			"        {\r\n" +
			"          \"name\": \"TAR\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"Target Environment Products\": [\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"BILLING\",\r\n" +
			"          \"target_product_version\": \"PROD\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"COLLECTION\",\r\n" +
			"          \"target_product_version\": \"1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"CRM\",\r\n" +
			"          \"target_product_version\": \"1.5\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"product_name\": \"ORDERING\",\r\n" +
			"          \"target_product_version\": \"1\"\r\n" +
			"        }\r\n" +
			"      ]\r\n" +
			"    },\r\n" +
			"    \"Task Execution Summary\": [\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"fabric_execution_id\": \"8868047e-0aa4-4793-a2f2-d41f8a24a94b\",\r\n" +
			"        \"parent_lu_name\": \"Customer\",\r\n" +
			"        \"data_center_name\": \"null\",\r\n" +
			"        \"start_execution_time\": \"2021-06-16 16:24:31.0\",\r\n" +
			"        \"end_execution_time\": \"2021-06-16 16:24:32.0\",\r\n" +
			"        \"num_of_processed_entities\": 17,\r\n" +
			"        \"num_of_copied_entities\": 17,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"num_of_processed_ref_tables\": 0,\r\n" +
			"        \"num_of_copied_ref_tables\": 0,\r\n" +
			"        \"num_of_failed_ref_tables\": 0\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"List of Root Entities\": {\r\n" +
			"      \"Number of Copied Entities\": [\r\n" +
			"        {\r\n" +
			"          \"number_of_copied_root_entities\": 17\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"List of Copied Entities\": [\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_102\",\r\n" +
			"          \"target_id\": \"102\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_103\",\r\n" +
			"          \"target_id\": \"103\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_104\",\r\n" +
			"          \"target_id\": \"104\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_105\",\r\n" +
			"          \"target_id\": \"105\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_106\",\r\n" +
			"          \"target_id\": \"106\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1324\",\r\n" +
			"          \"target_id\": \"1324\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1325\",\r\n" +
			"          \"target_id\": \"1325\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1326\",\r\n" +
			"          \"target_id\": \"1326\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1429\",\r\n" +
			"          \"target_id\": \"1429\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1430\",\r\n" +
			"          \"target_id\": \"1430\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1431\",\r\n" +
			"          \"target_id\": \"1431\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1432\",\r\n" +
			"          \"target_id\": \"1432\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1537\",\r\n" +
			"          \"target_id\": \"1537\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1538\",\r\n" +
			"          \"target_id\": \"1538\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1965\",\r\n" +
			"          \"target_id\": \"1965\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1966\",\r\n" +
			"          \"target_id\": \"1966\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"source_id\": \"SRC_1967\",\r\n" +
			"          \"target_id\": \"1967\"\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"Number of Failed Entities\": [\r\n" +
			"        {\r\n" +
			"          \"number_of_failed_root_entities\": 0\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"List of Failed Entities\": []\r\n" +
			"    },\r\n" +
			"    \"List of Reference Tables\": {\r\n" +
			"      \"Number of Copied Reference Tables\": [\r\n" +
			"        {\r\n" +
			"          \"count\": 0\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"List of Copied Reference Tables\": [],\r\n" +
			"      \"Number of Failed Reference Tables\": [\r\n" +
			"        {\r\n" +
			"          \"count\": 0\r\n" +
			"        }\r\n" +
			"      ],\r\n" +
			"      \"List of Failed Reference Tables\": []\r\n" +
			"    },\r\n" +
			"    \"Error Summary\": [],\r\n" +
			"    \"Error Details\": [],\r\n" +
			"    \"Statistics Report\": [\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"table_name\": \"BALANCE\",\r\n" +
			"        \"target_count\": \"172\",\r\n" +
			"        \"source_count\": \"172\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"table_name\": \"INVOICE\",\r\n" +
			"        \"target_count\": \"107\",\r\n" +
			"        \"source_count\": \"107\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"table_name\": \"OFFER\",\r\n" +
			"        \"target_count\": \"9\",\r\n" +
			"        \"source_count\": \"9\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"table_name\": \"SUBSCRIBER\",\r\n" +
			"        \"target_count\": \"17\",\r\n" +
			"        \"source_count\": \"17\",\r\n" +
			"        \"diff\": \"0\",\r\n" +
			"        \"results\": \"OK\"\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"Replace Sequence Summary Report\": []\r\n" +
			"  },\r\n" +
			"  \"message\": \"\"\r\n" +
			"}")
	public static Object wsTaskSummaryReport(@param(description="Task execution ID", required=true) String executionId, @param(description="Will be populated by 'ALL' to get one unified summary report to all Logical Units of the task execution. Populate this parameter by the Logical Unit name to get a report of a given Logical Unit.", required=true) String luName) throws Exception {
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
			errorCode="FAILED";
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
	@webService(path = "migrateStatusWs", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
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
				String batchID = ((List<String>)migrateIds).get(i);
				//log.info("wsMigrateStatusWs - batchID: " + batchID);
				if (batchID != null && !"".equals(batchID)) {
					results.add(fnMigrateStatusWs(batchID, runModes));
				}
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
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

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
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	//from TDM.logic
	@desc("Gets the hierarchy of a given entity and LU name within the task execution")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
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
			//log.info("There is a parent: " + parentRec.get("parent_lu_name"));
			parentLuName = "" + parentRec.get("parent_lu_name");
			parentTargetId = "" + parentRec.get("target_parent_id");
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
			String currRootLuName = "" + rootRec.get("luName");
			rootDetails.put("luName", currRootLuName);
			rootDetails.put("targetId", "" + rootRec.get("targetId"));
		
			//Get instance ID from entity id
			Object[] splitId = fnSplitUID("" + rootRec.get("sourceId"));
			String instanceId = "" + splitId[0];
			rootDetails.put("sourceId", "" + instanceId);
		
			rootDetails.put("entityStatus", "" + rootRec.get("luStatus"));
		
			mainOutput.put(currRootLuName, rootDetails);
		}
		if (otherRootRecs != null) {
			otherRootRecs.close();
		}
		return wrapWebServiceResults("SUCCESS", null, mainOutput);
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
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
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
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Starts an execution of a TDM task. \r\n" +
			"A task execution can override the following parameters:\r\n" +
			"\r\n" +
			"- entitieslist: populated by a list of entities separated by a comma. Note that the entity list can only contain one entity ID when executing a task with an Entity Clone selection method.\r\n" +
			"\r\n" +
			"- sourceEnvironmentName: source environment name.\r\n" +
			"\r\n" +
			"- targetEnvironmentName: target environment name.\r\n" +
			"\r\n" +
			"- taskGlobals: list of Global variables and their values.\r\n" +
			"\r\n" +
			"- numberOfEntities: populated by a number to change the number of entities processed by the task. This parameter is only relevant for Load tasks when the entitylist override parameter is not set.\r\n" +
			"\r\n" +
			"- dataVersionExecId: populated with the task execution id of the selected data version. The parameter can be set on Data Versioning load tasks.\r\n" +
			"\r\n" +
			"- dataVersionRetentionPeriod:  populated with the retention period of the extracted data version. This parameter contains the unit (Hours, Days, Weeks…) and the value.\r\n" +
			"\r\n" +
			"- reserveInd: populated with true or false. Set to true if the task execution needs to reserve the entities on the target environment.\r\n" +
			"\r\n" +
			"- reserveRetention: populated with the reservation period of the task's entities. This parameter contains the unit (Hours, Days, Weeks.) and the value.\r\n" +
			"\r\n" +
			"- executionNote: Free text. Add a note to the execution.\r\n" +
			"\r\n" +
			"\r\n" +
			"The task execution is validated whether the execution parameters are overridden or taken from the task itself:\r\n" +
			"\r\n" +
			"- Do not enable an execution if the TDM task execution processes are down. \r\n" +
			"\r\n" +
			"- Test the connection details of the source and target environments of the task execution if the forced parameter is false.\r\n" +
			"\r\n" +
			"- Do not enable an execution if another execution with the same execution parameters is already running on the task.\r\n" +
			"\r\n" +
			"- Validate the task's BE and LUs with the TDM products of the task execution's source and target environment.\r\n" +
			"- Verify that the user is permitted to execute the task on the task execution's source and target environment. For example, the user cannot run a Load task with a sequence replacement on environment X if the user does not have permissions to run such a task on this environment.\r\n" +
			"- Validate a Data Versioning extract task: verify that the given retention period do not exceed the maximum retention allowed for the user.\r\n" +
			"\r\n" +
			"Entity Reservation Validations:\r\n" +
			"===========================\r\n" +
			"- Validate the number of reserved entities: if the task reserves the entities whether the reserve_ind is set to true in the task itself or in the overridden parameters, accumulate the number of entities in the task to the total number of reserved entities for the user on the target environment. If the total number of reserved entities exceeds the user's permissions on the environment, return an error. For example, if the user is allowed to reserved up to 70 entities in ST1 and there are 50 entities that are already reserved for the user in ST1, the user can reserve up to 20 additional entities in ST1.\r\n" +
			"- Validate the retention period to verify that the number of days does not exceed the maximum number of days allowed for the tester.\r\n" +
			"\r\n" +
			"If at least one of the validations fail, the API does not start the task. Instead it returns a FAILED status and populates the list of validation errors in the results.\r\n" +
			"\r\n" +
			"Below is the list of the validation codes, returned by the API:\r\n" +
			"\r\n" +
			"- BEandLUs\r\n" +
			"\r\n" +
			"- Reference\r\n" +
			"\r\n" +
			"- selectionMethod\r\n" +
			"\r\n" +
			"- Versioning \r\n" +
			"\r\n" +
			"- ReplaceSequence\r\n" +
			"\r\n" +
			"- DeleteBeforeLoad\r\n" +
			"\r\n" +
			"- syncMode\r\n" +
			"\r\n" +
			"- totalNumberOfReservedEntities\r\n" +
			"\r\n" +
			"- versioningRetentionPeriod\r\n" +
			"\r\n" +
			"- reserveRetentionPeriod\r\n" +
			"\r\n" +
			"\r\n" +
			"If the validations pass successfully, start the task execution.\r\n" +
			"\r\n" +
			"Note that if a Global's value is a JSON, it is required to add backslash to the values. See example.\r\n" +
			"\r\n" +
			"Request body example:\r\n" +
			"\r\n" +
			"{\r\n" +
			"\t\"entitieslist\": \"1,2,4,9,8,11\",\r\n" +
			"\t\"sourceEnvironmentName\": \"SRC\",\r\n" +
			"\t\"targetEnvironmentName\": \"TAR\",\r\n" +
			"\t\"taskGlobals\": {\r\n" +
			"\t\t\"CUST_TYPE\": \"BUS\",\r\n" +
			"\t\t\"Customer.SUB_TYPE\": \"XX\"\r\n" +
			"\t},\r\n" +
			"\t\"numberOfEntities\": 14,\r\n" +
			"\t\"dataVersionExecId\": 2,\r\n" +
			"\t\"dataVersionRetentionPeriod\": {\r\n" +
			"\t\t\"units\": \"Days\",\r\n" +
			"\t\t\"value\": \"10\"\r\n" +
			"\t},\r\n" +
			"\t\"reserveInd\": true,\r\n" +
			"\t\"reserveRetention\": {\r\n" +
			"\t\t\"units\": \"Days\",\r\n" +
			"\t\t\"value\": \"5\"\r\n" +
			"\t},\r\n" +
			"\t\"executionNote\": \"Example Task\"\r\n" +
			"}")
	@webService(path = "task/{taskId}/forced/{forced}/startTask", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "1. Succesful Execution:\r\n" +
			"\r\n" +
			"{\r\n" +
			"  \"result\": {\r\n" +
			"    \"taskExecutionId\": 43\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}\r\n" +
			"\r\n" +
			"2. Validation Failure:\r\n" +
			"\r\n" +
			"{\r\n" +
			"    \"result\":[{\"Number of entity\":\"The number of entities exceeds the number of entities in the write permission\",\"selectionMethod\":\"The User has no permissions to run the task's selection method on the task's target environment\"}],\r\n" +
			"    \"errorCode\":\"FAIL\",\r\n" +
			"    \"message\":\"validation failure\"\r\n" +
			"}\r\n" +
			"\r\n" +
			"{ \r\n" +
			"    \"result\": \r\n" +
			"    [{\"reference\": \"The user has no permissions to run tasks on Reference tables on source environment\", \r\n" +
			"      \"syncMode\": \"the user has no permissions to ask to always sync the data from the source.\"    } ], \r\n" +
			"    \"errorCode\": \"FAIL\",\r\n" +
			"    \"message\": \"validation failure\"\r\n" +
			"}")
	public static Object wsStartTask(@param(required=true) Long taskId, @param(description="true or false", required=true) Boolean forced, String entitieslist, String sourceEnvironmentName, String targetEnvironmentName, Map<String,String> taskGlobals, @param(description="Only relevant for Load tasks") Integer numberOfEntities, Long dataVersionExecId, Map<String,String> dataVersionRetentionPeriod, Boolean reserveInd, Map<String,String> reserveRetention, String executionNote) throws Exception {
        return  fnStartTask(taskId, forced, entitieslist, sourceEnvironmentName, targetEnvironmentName, taskGlobals, numberOfEntities, dataVersionExecId, dataVersionRetentionPeriod, reserveInd, reserveRetention, executionNote);

	}

	@desc("Holds (pause) the execution of the input task")
	@webService(path = "task/{taskId}/holdTask", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	public static Object wsHoldTask(@param(required=true) Long taskId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String sql="UPDATE " + TDMDB_SCHEMA + ".tasks SET task_execution_status = \'onHold\' WHERE tasks.task_id = " + taskId + " RETURNING tasks.task_title";
			Db.Row row = db(TDM).fetch(sql).firstRow();
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
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

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
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}



	@desc("Activates a paused task (the task is set on-hold)")
	@webService(path = "task/{taskId}/activateTask", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\"result\":{},\"errorCode\":\"SUCCESS\",\"message\":null}")
	public static Object wsActivateTask(@param(required=true) Long taskId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		try {
			String sql = "UPDATE " + TDMDB_SCHEMA + ".tasks SET task_execution_status = \'Active\' WHERE tasks.task_id = " + taskId + " RETURNING tasks.task_title";
			Db.Row row = db(TDM).fetch(sql).firstRow();
			Object taskTitle= row.get("task_title");
		
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
			errorCode="FAILED";
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
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the list of reference table included in a given task. Note that refcount attribute of /tasks API is populated by the number of Reference tables included in the task. If the refcount attribute is populated by zero, the task does not have Reference tables.")
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
			String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".task_ref_tables where task_id = " + task_id;
			Db.Rows rows = db(TDM).fetch(sql);
		
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
			if(rows != null) {
				rows.close();
			}
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
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
				"TARGET_ROOT_ENTITY_ID as rootTargetId, Case when PARENT_LU_NAME = '' then LU_NAME else PARENT_LU_NAME end as parentLuName, " + 
				"Case when PARENT_ENTITY_ID = '' then BE_ROOT_ENTITY_ID else PARENT_ENTITY_ID end as parentSourceId, " +
				"Case when TARGET_PARENT_ID = '' then TARGET_ROOT_ENTITY_ID else TARGET_PARENT_ID end as parentTargetId, " +
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

				mapInnerCopiedEnt.put("luName", copiedEnt.get("luName"));

				//Get instance ID from entity id
				Object[] splitId = fnSplitUID("" +  copiedEnt.get("sourceId"));
				String instanceId = "" + splitId[0];
				mapInnerCopiedEnt.put("sourceId", instanceId);

				String targetID = "" + copiedEnt.get("targetId");

				mapInnerCopiedEnt.put("targetId", targetID);
				mapInnerCopiedEnt.put("rootSourceId", copiedEnt.get("rootSourceId"));
				mapInnerCopiedEnt.put("rootTargetId", copiedEnt.get("rootTargetId"));
				mapInnerCopiedEnt.put("parentLuName", copiedEnt.get("parentLuName"));
				mapInnerCopiedEnt.put("parentSourceId", copiedEnt.get("parentSourceId"));
				mapInnerCopiedEnt.put("parentTargetId", copiedEnt.get("parentTargetId"));
				mapInnerCopiedEnt.put("copyEntityStatus", copiedEnt.get("copyEntityStatus"));
				mapInnerCopiedEnt.put("copyHierarchyStatus", copiedEnt.get("copyHierarchyStatus"));

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

				mapInnerFailedEnt.put("luName", failedEnt.get("luName"));

				//Get instance ID from entity id
				Object[] splitId = fnSplitUID("" +  failedEnt.get("sourceId"));
				String instanceId = "" + splitId[0];
				mapInnerFailedEnt.put("sourceId", instanceId);

				String targetID = "" + failedEnt.get("targetId");
				String copyEntityStatus = "" + failedEnt.get("copyEntityStatus");

				mapInnerFailedEnt.put("targetId", targetID);
				mapInnerFailedEnt.put("rootSourceId", failedEnt.get("rootSourceId"));
				mapInnerFailedEnt.put("rootTargetId", failedEnt.get("rootTargetId"));
				mapInnerFailedEnt.put("parentLuName", failedEnt.get("parentLuName"));
				mapInnerFailedEnt.put("parentSourceId", failedEnt.get("parentSourceId"));
				mapInnerFailedEnt.put("parentTargetId", failedEnt.get("parentTargetId"));
				mapInnerFailedEnt.put("copyEntityStatus", copyEntityStatus);
				mapInnerFailedEnt.put("copyHierarchyStatus", failedEnt.get("copyHierarchyStatus"));
				//log.info ("Failed - luName: " + failedEnt.get("luName") + ", rootSourceId: " + failedEnt.get("rootSourceId"));
				// TDM 6.1.1 - 20-may-20, add the error msg that casued the failure
				String errorMsgSql = "select error_message from task_exe_error_detailed where " +
						"task_execution_id = ? and lu_name = ? and target_entity_id = ?  ORDER BY ERROR_CATEGORY LIMIT 5";
				Db.Rows errorMsgs = fabric().fetch(errorMsgSql, taskExecutionId, failedEnt.get("luName"), targetID);
				List<String> entityErrMsgs  = new ArrayList<>();
				for (Db.Row errorMsg : errorMsgs) {
					entityErrMsgs.add("" + errorMsg.get("error_message"));
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

						//log.info("wsGetTDMTaskExecutionStats - Calling RECURSIVE sql for parent lu: " + failedEnt.get("parentLuName") + ", and target ID: " + targetID);
						Db.Rows childrenBuf = fabric().fetch(sqlRecursiveGetChildren, failedEnt.get("parentLuName"), targetID);

						Boolean errorLevelFound = false;
						int rowNum = 0;
						String lookupParentLuName = "";
						String lookupParentID = "";

						for (Db.Row childRec : childrenBuf) {
							Map <String, Object> mapInnerErrorPath = new HashMap <>();
							String childluName = "" + childRec.get("lu_name");
							String childEnityID = "" + childRec.get("target_entity_id");
							String childStatus = "" + childRec.get("execution_status");
							String childParenLuName = "" + childRec.get("parent_lu_name");
							String childParentID = "" + childRec.get("target_parent_id");
							String entityStatus = "";

							int currRowNum = Integer.parseInt("" + childRec.get("row_number"));
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
					mapInputLu.put("luName", failedEnt.get("luName"));
					mapInputLu.put("entityStatus", copyEntityStatus);
					mapInputLu.put("parentLuName", failedEnt.get("parentLuName"));
					mapInputLu.put("luStatus", "Failed");


					failedErrPathList.addFirst(mapInputLu);
					//log.info("Adding luName: " + failedEnt.get("luName") + ", entityStatus: " + copyEntityStatus + " to erro path");

					mapInnerFailedEnt.put("Full Error Path", failedErrPathList);
				}

				failedEntitiesList.add(mapInnerFailedEnt);
				if(errorMsgs != null) {
				errorMsgs.close();
			}
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

			mapInnerCopiedRefEnt.put("RerernceTableName", copiedRefEnt.get("entity_id"));
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
			String reTableName = "" + failedRefEnt.get("entity_id");
			mapInnerFailedRefEnt.put("RerernceTableName", reTableName);

			// TDM 6.1.1 - 20-may-20, add the error msg that casued the failure
			String errorMsgSql = "select error_message from task_exe_error_detailed where " +
					"task_execution_id = ? and lu_name = ? and target_entity_id = ? LIMIT 5";
			Db.Rows errorMsgs = fabric().fetch(errorMsgSql, taskExecutionId, luName, reTableName);
			List<String> entityErrMsgs  = new ArrayList<>();
			for (Db.Row errorMsg : errorMsgs) {
				entityErrMsgs.add("" + errorMsg.get("error_message"));
			}
			mapInnerFailedRefEnt.put("errorMsg", entityErrMsgs);

			failedRefEntitiesList.add(mapInnerFailedRefEnt);
			if (errorMsgs != null) {
				errorMsgs.close();
			}
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
		String rootsStatusSql = "select ROOT_LU_NAME, max(root_status) as MAX_ROOT_STS from ( " +
				"select distinct root_lu_name, case when root_entity_status = 'completed' and EXECUTION_STATUS = 'completed' then 1 " +
				"when root_entity_status = 'failed' and  EXECUTION_STATUS = 'completed' then 2 " +
				"else 3 end as  root_status from task_execution_link_entities where parent_lu_name = '') group by root_lu_name";

		Db.Rows rootsStatuses = fabric().fetch(rootsStatusSql);

		for (Db.Row rootStatus : rootsStatuses) {
			String rootName = "" + rootStatus.get("root_lu_name");
			int rootStatusInd =  (int) rootStatus.get("max_root_sts");
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
	@resultMetaData(mediaType = Produce.XML, example = "<HashMap>\n  <result>\n    <task_last_updated_date>1615278319339</task_last_updated_date>\n    <be_id>1</be_id>\n    <selected_version_task_name/>\n    <environment_id>1</environment_id>\n    <selection_method>L</selection_method>\n    <selected_ref_version_task_name/>\n    <refresh_reference_data/>\n    <task_id>21</task_id>\n    <source_environment_id>1</source_environment_id>\n    <scheduler>immediate</scheduler>\n    <selected_ref_version_datetime/>\n    <source_env_name>ENV1</source_env_name>\n    <load_entity>false</load_entity>\n    <task_title>tester</task_title>\n    <selected_version_task_exe_id/>\n    <task_created_by>k2vtester01</task_created_by>\n    <fabric_environment_name/>\n    <scheduling_end_date/>\n    <delete_before_load>false</delete_before_load>\n    <retention_period_type/>\n    <task_status>Active</task_status>\n    <selection_param_value>1,2,3,4,5,6,7,8,9,10</selection_param_value>\n    <retention_period_value/>\n    <selected_version_datetime/>\n    <task_last_updated_by>k2vtester01</task_last_updated_by>\n    <selected_ref_version_task_exe_id/>\n    <task_execution_status>Active</task_execution_status>\n    <version_ind>false</version_ind>\n    <sync_mode/>\n    <num_of_entities>10</num_of_entities>\n    <task_creation_date>1615278319339</task_creation_date>\n    <task_globals>false</task_globals>\n    <replace_sequences/>\n    <entity_exclusion_list/>\n    <task_type>EXTRACT</task_type>\n    <parameters/>\n  </result>\n  <errorCode>SUCCESS</errorCode>\n  <message/>\n</HashMap>")
	@resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": [\n    {\n      \"task_last_updated_date\": \"2021-03-09 08:25:19.339\",\n      \"be_id\": 1,\n      \"selected_version_task_name\": null,\n      \"environment_id\": 1,\n      \"selection_method\": \"L\",\n      \"selected_ref_version_task_name\": null,\n      \"refresh_reference_data\": null,\n      \"task_id\": 21,\n      \"source_environment_id\": 1,\n      \"scheduler\": \"immediate\",\n      \"selected_ref_version_datetime\": null,\n      \"source_env_name\": \"ENV1\",\n      \"load_entity\": false,\n      \"task_title\": \"tester\",\n      \"selected_version_task_exe_id\": null,\n      \"task_created_by\": \"k2vtester01\",\n      \"fabric_environment_name\": null,\n      \"scheduling_end_date\": null,\n      \"delete_before_load\": false,\n      \"retention_period_type\": null,\n      \"task_status\": \"Active\",\n      \"selection_param_value\": \"1,2,3,4,5,6,7,8,9,10\",\n      \"retention_period_value\": null,\n      \"selected_version_datetime\": null,\n      \"task_last_updated_by\": \"k2vtester01\",\n      \"selected_ref_version_task_exe_id\": null,\n      \"task_execution_status\": \"Active\",\n      \"version_ind\": false,\n      \"sync_mode\": null,\n      \"num_of_entities\": 10,\n      \"task_creation_date\": \"2021-03-09 08:25:19.339\",\n      \"task_globals\": false,\n      \"replace_sequences\": null,\n      \"entity_exclusion_list\": null,\n      \"task_type\": \"EXTRACT\",\n      \"parameters\": null\n    }\n  ],\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
	public static Object wsGetUserTasks(@param(required=true) String userName, @param(description="admin, owner or tester", required=true) String userType) throws Exception {
		String adminQuery = "select * from " + TDMDB_SCHEMA + ".tasks where task_status = 'Active' and task_execution_status = 'Active'";
		String envOwnerQuery =
				//Query 1- get the extract tasks where the user is the owner of the source env
				"select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_owners o " +
						"where lower(task_type) = 'extract' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.source_environment_id = o.environment_id " +
						"and o.user_name = ? " +
						"UNION " +
						//Query 2- get the tasks, created by the user
						"select t.* from " + TDMDB_SCHEMA + ".tasks t where lower(task_type) = 'extract' " +
						"and lower(task_status) = 'active' and lower(task_execution_status) = 'active' " +
						"and t.task_Created_by = ? " +
						"UNION " +
						//Query 3 - get the task created by another tester which belongs to the same role of the user
						"select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
						"where lower(task_type) = 'extract' and lower(task_status) = 'active' "+
						"and lower(task_execution_status) = 'active' and t.task_Created_by <> ? " +
						"and not exists (select 1 from environment_owners o1 where o1.environment_id  = t.source_environment_id and o1.user_name = t.task_Created_by) " +
						"and t.source_environment_id = r.environment_id and r.role_id = u.role_id " +
						"and lower(r.role_status) = 'active' and (t.task_Created_by = u.username or lower(u.username) = 'all') " +
						"and exists (select 1 from " +
						"(Select env_list.environment_id, u.role_id " +
						"From " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u, " +
						"(Select r1.environment_id, 'user' As assignment_type " +
						"From " + TDMDB_SCHEMA + ".environment_roles r1, " + TDMDB_SCHEMA + ".environment_role_users u1 " +
						"Where r1.role_id = u1.role_id And u1.username = ? And " +
						"lower(r1.role_status) = 'active' " +
						"UNION " +
						"Select r2.environment_id, 'all' As assignment_Type " +
						"From " + TDMDB_SCHEMA + ".environment_roles r2, " + TDMDB_SCHEMA + ".environment_role_users u2 " +
						"Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
						"And lower(r2.role_status) = 'active' And Not exists " +
						"(select 1 from " + TDMDB_SCHEMA + ".environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
						"Where r.environment_id = env_list.environment_id " +
						"And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
						"And ((env_list.assignment_type = 'all' And " +
						"lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
						"where user_roles.role_id = r.role_id)" +
						"UNION " +
						//Query 4- get the load tasks where the user is the owner of the target env
						"select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_owners o " +
						"where lower(task_type) = 'load' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.environment_id = o.environment_id " +
						"and o.user_name = ? " +
						"UNION " +
						//Query 5 - get the tasks, created by the user
						"select t.* from " + TDMDB_SCHEMA + ".tasks t where lower(task_type) = 'load' " +
						"and lower(task_status) = 'active' and lower(task_execution_status) = 'active' " +
						"and t.task_Created_by = ? " +
						"UNION " +
						//Query 6 - get the task created by another user which belongs to the same role of the user on the target env
						"select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
						"where lower(task_type) = 'load' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.task_Created_by <> ? " +
						"and not exists (select 1 from " + TDMDB_SCHEMA + ".environment_owners o1 where o1.environment_id  = t.environment_id and o1.user_name = t.task_Created_by) " +
						"and t.environment_id = r.environment_id and r.role_id = u.role_id " +
						"and lower(r.role_status) = 'active' and (t.task_Created_by = u.username or lower(u.username) = 'all') " +
						"and exists (select 1 from " +
						"(Select env_list.environment_id, u.role_id " +
						"From " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u, " +
						"(Select r1.environment_id, 'user' As assignment_type " +
						"From " + TDMDB_SCHEMA + ".environment_roles r1," + TDMDB_SCHEMA + ". environment_role_users u1 " +
						"Where r1.role_id = u1.role_id And u1.username = ? And " +
						"lower(r1.role_status) = 'active' " +
						"UNION " +
						"Select r2.environment_id, 'all' As assignment_Type " +
						"From " + TDMDB_SCHEMA + ".environment_roles r2, " + TDMDB_SCHEMA + ".environment_role_users u2 " +
						"Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
						"And lower(r2.role_status) = 'active' And Not exists " +
						"(select 1 from " + TDMDB_SCHEMA + ".environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
						"Where r.environment_id = env_list.environment_id " +
						"And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
						"And ((env_list.assignment_type = 'all' And " +
						"lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
						"where user_roles.role_id = r.role_id)";
		
		String testerQuery =
				//Query 1 - get the tasks, created by the user
				"select t.* from " + TDMDB_SCHEMA + ".tasks t where lower(task_type) = 'extract' " +
						"and lower(task_status) = 'active' and lower(task_execution_status) = 'active' " +
						"and t.task_Created_by = ? " +
						"UNION " +
						//Query 2 - get the task created by another user which belongs to the same role of the user
						"select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
						"where lower(task_type) = 'extract' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.task_Created_by <> ? " +
						"and not exists (select 1 from " + TDMDB_SCHEMA + ".environment_owners o1 where o1.environment_id  = t.source_environment_id and o1.user_name = t.task_Created_by) " +
						"and t.source_environment_id = r.environment_id " +
						"and r.role_id = u.role_id and lower(r.role_status) = 'active' " +
						"and (t.task_Created_by = u.username or lower(u.username) = 'all') " +
						"and exists (select 1 from " +
						"(Select env_list.environment_id, u.role_id " +
						"From " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u, " +
						"(Select r1.environment_id, 'user' As assignment_type " +
						"From " + TDMDB_SCHEMA + ".environment_roles r1, " + TDMDB_SCHEMA + ".environment_role_users u1 " +
						"Where r1.role_id = u1.role_id And u1.username = ? And " +
						"lower(r1.role_status) = 'active' " +
						"UNION " +
						"Select r2.environment_id, 'all' As assignment_Type " +
						"From " + TDMDB_SCHEMA + ".environment_roles r2, " + TDMDB_SCHEMA + ".environment_role_users u2 " +
						"Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
						"And lower(r2.role_status) = 'active' And Not exists " +
						"(select 1 from " + TDMDB_SCHEMA + ".environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
						"Where r.environment_id = env_list.environment_id " +
						"And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
						"And ((env_list.assignment_type = 'all' And " +
						"lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
						"where user_roles.role_id = r.role_id) " +
						"UNION " +
						//Query 3 - get the tasks, created by the user
						"select t.* from " + TDMDB_SCHEMA + ".tasks t " +
						"where lower(task_type) = 'load' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.task_Created_by = ? " +
						"UNION " +
						//Query 4 - get the task created by another user which belongs to the same role of the user on the target env
						"select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
						"where lower(task_type) = 'load' and lower(task_status) = 'active' " +
						"and lower(task_execution_status) = 'active' and t.task_Created_by <> ? " +
						"and not exists (select 1 from " + TDMDB_SCHEMA + ".environment_owners o1 where o1.environment_id  = t.environment_id and o1.user_name = t.task_Created_by) " +
						"and t.environment_id = r.environment_id and r.role_id = u.role_id and lower(r.role_status) = 'active' " +
						"and (t.task_Created_by = u.username or lower(u.username) = 'all') " +
						"and exists (select 1 from " +
						"(Select env_list.environment_id, u.role_id " +
						"From " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u, " +
						"(Select r1.environment_id, 'user' As assignment_type " +
						"From " + TDMDB_SCHEMA + ".environment_roles r1, " + TDMDB_SCHEMA + ".environment_role_users u1 " +
						"Where r1.role_id = u1.role_id And u1.username = ? And " +
						"lower(r1.role_status) = 'active' " +
						"UNION " +
						"Select r2.environment_id, 'all' As assignment_Type " +
						"From " + TDMDB_SCHEMA + ".environment_roles r2, " + TDMDB_SCHEMA + ".environment_role_users u2 " +
						"Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
						"And lower(r2.role_status) = 'active' And Not exists " +
						"(select 1 from " + TDMDB_SCHEMA + ".environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
						"Where r.environment_id = env_list.environment_id " +
						"And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
						"And ((env_list.assignment_type = 'all' And " +
						"lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
						"where user_roles.role_id = r.role_id)";
		
		
		Object taskList = null;
		
		switch (userType.toLowerCase()) {
			case "admin" :
				taskList = db(TDM).fetch(adminQuery);
				break;
			case "owner" :
				Object[] ownerParams = new Object[12];
				Arrays.fill(ownerParams, userName);
				taskList = db(TDM).fetch(envOwnerQuery, ownerParams);
				break;
			case "tester" :
				Object[] testerParams = new Object[10];
				Arrays.fill(testerParams, userName);
				taskList = db(TDM).fetch(testerQuery, testerParams);
				break;
			default:
				log.error("wsGetUserTasks - Wrong User Type, supported types: admin, owner, tester");
				return wrapWebServiceResults("FAILED", "Wrong User Type - " + userType + ", supported types: admin, owner, tester", null);
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


	@desc("Returns the details of the input task execution ID if populated. \r\n" +
			"When the task execution id is empty, the API returns current/last execution of the given task id. If the latest task execution is pending, it will only return its status, else it will return the statistics of the entities handled by the task execution.")
	@webService(path = "wsTaskMonitor/{taskID}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.XML, example = "{\n  \"result\": {\n    \"Task ID\": \"10\",\n    \"Task Details\": [\n      {\n        \"Fabric Batch ID\": \"cc02e34d-2367-4a15-aae7-79dcb4c3e48e\",\n        \"Task Statistics\": [\n          {\n            \"Status\": \"\",\n            \"Ent./sec (avg.)\": \"2.9\",\n            \"Added\": 0,\n            \"Ent./sec (pace)\": \"2.9\",\n            \"Updated\": 3,\n            \"Failed\": \"0\",\n            \"Duration\": \"00:00:01\",\n            \"End time\": \"2021-06-17 12:23:42.464\",\n            \"Name\": \"b6b8f7b8-5eb8-4b27-9b26-c0a387f17ba8\",\n            \"Succeeded\": \"3\",\n            \"Total\": \"--\",\n            \"Level\": \"Node\",\n            \"Remaining dur.\": \"00:00:00\",\n            \"Remaining\": \"0\",\n            \"Start time\": \"2021-06-17 12:23:41.429\",\n            \"Unchanged\": 0,\n            \"% Completed\": \"100\"\n          },\n          {\n            \"Status\": \"\",\n            \"Ent./sec (avg.)\": \"2.9\",\n            \"Added\": 0,\n            \"Ent./sec (pace)\": \"2.9\",\n            \"Updated\": 3,\n            \"Failed\": \"0\",\n            \"Duration\": \"00:00:01\",\n            \"End time\": \"2021-06-17 12:23:42.464\",\n            \"Name\": \"DC1\",\n            \"Succeeded\": \"3\",\n            \"Total\": \"--\",\n            \"Level\": \"DC\",\n            \"Remaining dur.\": \"00:00:00\",\n            \"Remaining\": \"0\",\n            \"Start time\": \"2021-06-17 12:23:41.429\",\n            \"Unchanged\": 0,\n            \"% Completed\": \"100\"\n          },\n          {\n            \"Status\": \"DONE\",\n            \"Ent./sec (avg.)\": \"2.9\",\n            \"Added\": 0,\n            \"Ent./sec (pace)\": \"2.9\",\n            \"Updated\": 3,\n            \"Failed\": \"0\",\n            \"Duration\": \"00:00:01\",\n            \"End time\": \"2021-06-17 12:23:42.464\",\n            \"Name\": \"--\",\n            \"Succeeded\": \"3\",\n            \"Total\": \"3\",\n            \"Level\": \"Cluster\",\n            \"Remaining dur.\": \"00:00:00\",\n            \"Remaining\": \"0\",\n            \"Start time\": \"2021-06-17 12:23:41.429\",\n            \"Unchanged\": 0,\n            \"% Completed\": \"100\"\n          }\n        ],\n        \"Task Status\": \"completed\",\n        \"LU Name\": \"PATIENT_LU\"\n      },\n      {\n        \"Fabric Batch ID\": \"cef3e9fb-2f11-437f-8859-da535300930a\",\n        \"Task Statistics\": [\n          {\n            \"Status\": \"IN_PROGRESS\",\n            \"Ent./sec (avg.)\": \"0\",\n            \"Ent./sec (pace)\": \"0\",\n            \"Failed\": \"0\",\n            \"Duration\": \"00:00:01\",\n            \"End time\": \"-\",\n            \"Name\": \"--\",\n            \"Succeeded\": \"0\",\n            \"Total\": \"19\",\n            \"Level\": \"Cluster\",\n            \"Remaining dur.\": \"00:00:01\",\n            \"Remaining\": \"19\",\n            \"Start time\": \"2021-06-17 12:24:02.523\",\n            \"% Completed\": \"0\"\n          }\n        ],\n        \"Task Status\": \"running\",\n        \"LU Name\": \"PATIENT_VISITS\"\n      }\n    ],\n    \"Task Name\": \"Extract2\",\n    \"Task Execution ID\": 50,\n    \"Task Reference Statistics\": {\n      \"PATIENT_LU\": {\n        \"minStartExecutionDate\": \"Thu Jun 17 12:23:41 UTC 2021\",\n        \"maxEndExecutionDate\": \"Thu Jun 17 12:23:42 UTC 2021\",\n        \"totNumOfTablesToProcess\": 2,\n        \"numOfProcessedRefTables\": 2,\n        \"numOfCopiedRefTables\": 2,\n        \"numOfFailedRefTables\": 0,\n        \"numOfProcessingRefTables\": 0,\n        \"numberOfNotStartedRefTables\": 0\n      },\n      \"PATIENT_VISITS\": {\n        \"minStartExecutionDate\": \"Thu Jun 17 12:23:41 UTC 2021\",\n        \"maxEndExecutionDate\": \"Thu Jun 17 12:23:52 UTC 2021\",\n        \"totNumOfTablesToProcess\": 1,\n        \"numOfProcessedRefTables\": 1,\n        \"numOfCopiedRefTables\": 1,\n        \"numOfFailedRefTables\": 0,\n        \"numOfProcessingRefTables\": 0,\n        \"numberOfNotStartedRefTables\": 0\n      }\n    }\n  },\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"Task ID\": \"300\",\r\n" +
			"    \"Task Details\": [\r\n" +
			"      {\r\n" +
			"        \"Task Status\": \"Pending\",\r\n" +
			"        \"LU Name\": \"Billing\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"Task Status\": \"Pending\",\r\n" +
			"        \"LU Name\": \"Collection\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"Task Status\": \"Pending\",\r\n" +
			"        \"LU Name\": \"Orders\"\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"Fabric Batch ID\": \"c03d06e2-3766-428c-8465-79e6487b2887\",\r\n" +
			"        \"Task Statistics\": [\r\n" +
			"          {\r\n" +
			"            \"Status\": \"\",\r\n" +
			"            \"Ent./sec (avg.)\": \"242.01\",\r\n" +
			"            \"Added\": 0,\r\n" +
			"            \"Ent./sec (pace)\": \"242.01\",\r\n" +
			"            \"Updated\": 1000,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:04\",\r\n" +
			"            \"End time\": \"2021-06-20 09:08:19.487\",\r\n" +
			"            \"Name\": \"2373be01-f751-47ee-926b-c6e9312aab6e\",\r\n" +
			"            \"Succeeded\": \"1000\",\r\n" +
			"            \"Total\": \"--\",\r\n" +
			"            \"Level\": \"Node\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-06-20 09:08:15.355\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"Status\": \"\",\r\n" +
			"            \"Ent./sec (avg.)\": \"242.01\",\r\n" +
			"            \"Added\": 0,\r\n" +
			"            \"Ent./sec (pace)\": \"242.01\",\r\n" +
			"            \"Updated\": 1000,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:04\",\r\n" +
			"            \"End time\": \"2021-06-20 09:08:19.487\",\r\n" +
			"            \"Name\": \"DC1\",\r\n" +
			"            \"Succeeded\": \"1000\",\r\n" +
			"            \"Total\": \"--\",\r\n" +
			"            \"Level\": \"DC\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-06-20 09:08:15.355\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          },\r\n" +
			"          {\r\n" +
			"            \"Status\": \"DONE\",\r\n" +
			"            \"Ent./sec (avg.)\": \"242.01\",\r\n" +
			"            \"Added\": 0,\r\n" +
			"            \"Ent./sec (pace)\": \"242.01\",\r\n" +
			"            \"Updated\": 1000,\r\n" +
			"            \"Failed\": \"0\",\r\n" +
			"            \"Duration\": \"00:00:04\",\r\n" +
			"            \"End time\": \"2021-06-20 09:08:19.487\",\r\n" +
			"            \"Name\": \"--\",\r\n" +
			"            \"Succeeded\": \"1000\",\r\n" +
			"            \"Total\": \"1000\",\r\n" +
			"            \"Level\": \"Cluster\",\r\n" +
			"            \"Remaining dur.\": \"00:00:00\",\r\n" +
			"            \"Remaining\": \"0\",\r\n" +
			"            \"Start time\": \"2021-06-20 09:08:15.355\",\r\n" +
			"            \"Unchanged\": 0,\r\n" +
			"            \"% Completed\": \"100\"\r\n" +
			"          }\r\n" +
			"        ],\r\n" +
			"        \"Task Status\": \"running\",\r\n" +
			"        \"LU Name\": \"Customer\"\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"Task Name\": \"testRefAndEntities\",\r\n" +
			"    \"Task Execution ID\": 499,\r\n" +
			"    \"Task Reference Statistics\": {\r\n" +
			"      \"Customer\": {\r\n" +
			"        \"minStartExecutionDate\": \"Sun Jun 20 09:08:15 UTC 2021\",\r\n" +
			"        \"maxEndExecutionDate\": \"Sun Jun 20 09:08:20 UTC 2021\",\r\n" +
			"        \"totNumOfTablesToProcess\": 1,\r\n" +
			"        \"numOfProcessedRefTables\": 1,\r\n" +
			"        \"numOfCopiedRefTables\": 1,\r\n" +
			"        \"numOfFailedRefTables\": 0,\r\n" +
			"        \"numOfProcessingRefTables\": 0,\r\n" +
			"        \"numberOfNotStartedRefTables\": 0\r\n" +
			"      }\r\n" +
			"    }\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsTaskMonitor(@param(required=true) String taskID, @param(description="Task execution id. This parameter enables monitoring a given task execution.") String executionID) throws Exception {
		String executionIdWhere = executionID == null ? "(SELECT MAX(TASK_EXECUTION_ID) FROM TASK_EXECUTION_LIST L2 WHERE TASK_ID = " + taskID + ")" : executionID;
		String getExecIDsQuery = "SELECT task_execution_id, execution_status, fabric_execution_id, " +
				"lu_name as name, task_title, 'LU' as type " +
				"FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_LIST L, " + TDMDB_SCHEMA + ".TASKS_LOGICAL_UNITS U, " + TDMDB_SCHEMA + ".TASKS T WHERE " +
				"t.task_id = l.task_id AND u.task_id = l.task_id AND u.lu_id = l.lu_id AND T.task_id = " + taskID + " " +
				"AND l.task_execution_id = " + executionIdWhere + " and process_id = 0 " +
				"UNION " +
				"SELECT task_execution_id, execution_status, fabric_execution_id, " +
				"process_name as name, task_title, 'Process' as type " +
				"FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_LIST L, " + TDMDB_SCHEMA + ".TASKS_POST_EXE_PROCESS P, " + TDMDB_SCHEMA + ".TASKS T WHERE " +
				"t.task_id = l.task_id AND p.task_id = l.task_id AND p.process_id = l.process_id AND T.task_id = " + taskID + " " +
				"AND l.task_execution_id = " + executionIdWhere + " AND lu_id = 0";
		
		Db.Rows execIDsList = db(TDM).fetch(getExecIDsQuery);
		
		HashMap <String, Object> taskInfo = new HashMap<>();
		List <Object> taskList = new ArrayList<>();
		
		taskInfo.put("Task ID", taskID);
		boolean firstRecInd = true;
		String taskExecutionId = "";
		
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
				taskExecutionId = "" + execRec.get("task_execution_id");
			} else {
				taskLUInfo.put("Process Name", execRec.get("name"));
			}
			taskLUInfo.put("Task Status", execStatus);
			String fabricExecutionId = "" + execRec.get("fabric_execution_id");
			if (!"pending".equalsIgnoreCase(execStatus) && 
				"LU".equals(execRec.get("type")) &&  !"".equals(fabricExecutionId) &&  !"null".equals(fabricExecutionId)) {
				taskLUInfo.put("Fabric Batch ID", execRec.get("fabric_execution_id"));
				taskLUInfo.put("Task Statistics", fnBatchStatistics(fabricExecutionId, "S"));
			}
		
			taskList.add(taskLUInfo);
		}
		
		taskInfo.put("Task Details", taskList);
		if (!"".equals(taskExecutionId)) {
			Map <String, Object> refSummaryStatsBuf = fnGetReferenceSummaryData(taskExecutionId);
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
			
			taskInfo.put("Task Reference Statistics", refSummaryStatsBuf);
		}
		
		
		if(execIDsList != null) {
			execIDsList.close();
		}
		return wrapWebServiceResults("SUCCESS", null,taskInfo);
	}


	@desc("Returns active task ID by its name.")
	@webService(path = "wsGetTaskId/{taskName}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
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
		Object response = db(TDM).
				fetch("select t.task_id from " + TDMDB_SCHEMA + ".tasks t where t.task_title= ? and t.task_status='Active'", taskName).firstValue();
		if (response == null) {
			return wrapWebServiceResults("FAILED", "No active task found for task name '" + taskName + "'.", response);
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
			"      \"lu_name\": \"Customer\",\r\n" +
			"      \"global_value\": \"false\"\r\n" +
			"    }\r\n" +
			"  ]\r\n" +
			"}")
	@webService(path = "task/{taskId}/globals", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	public static Object UpdateTaskGlobals(@param(required=true) Long taskId, List<Map<String,Object>> globals) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		String sql = "UPDATE " + TDMDB_SCHEMA + ".task_globals SET " +
				"global_value=(?) " +
				"WHERE task_id = ? AND global_name = ?";
		try {
			if (globals!=null && globals.size() != 0 ){
				for (Map<String,Object> global:globals){
					String globalName = "" + global.get("global_name");
					String globalValue = "" + global.get("global_value");
					String luName = "" +  global.get("lu_name");
					if (luName != null && !"".equals(luName) && !"ALL".equals(luName) && !"null".equals(luName)) {
						globalName = luName + "." + globalName;
					}
					db(TDM).execute(sql, globalValue, taskId, globalName);
				}
			}
			response.put("result",new HashMap<>());
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAILED";
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
			String sql = "UPDATE " + TDMDB_SCHEMA + ".tasks SET " +
					"task_status=(?), task_execution_status=(?), " +
					"task_last_updated_date=(?), " +
					"task_last_updated_by=(?) " +
					"WHERE task_id = " + taskId;
			String username=sessionUser().name();
			db(TDM).execute(sql,"Inactive", "Inactive", now, username);
			try {
				String activityDesc = "Task " + taskName + " was deleted";
				fnInsertActivity("delete", "Tasks", activityDesc);
			} catch(Exception e){
				log.error(e.getMessage());
			}

			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Get the list of environments that are aligned with the input filtering parameters, according to the task type and attributes, as follows: \r\n" +
			"- If the task type is Extract , then validate and return the list of available source environments.\r\n" +
			"- If the task type is Load, then validate and return both - source and target environments.\r\n" +
			"- If the task type is Reserve (reserve only task), then validate and return the list of available target environments.\r\n" +
			"- If the task is a delete only task, then validate and return the list of available target environments.")
	@webService(path = "getEnvironmentsForTaskAttr", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"source environments\": [\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"4\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"ENV6\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"1\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"ENV1\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"6\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"ENV3\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"3\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"ENV4\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"9\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"Env9\"\r\n" +
			"        }\r\n" +
			"      ]\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"target environments\": [\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"2\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"ENV2\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"5\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"ENV5\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"6\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"ENV3\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"environment_id\": \"9\",\r\n" +
			"          \"role_id\": \"admin\",\r\n" +
			"          \"environment_name\": \"Env9\"\r\n" +
			"        }\r\n" +
			"      ]\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentsByTaskFilteringParams(@param(description="Populated by the task's be_id (Business Entity ID). For example: 1.", required=true) String be, @param(description="Populated by the task's Logical Units (LU IDs), separated by comma. Example : 1,5,6,7", required=true) String lus, Integer refcount, @param(description="Can be populated by the following values: \"L\" (entity list), \"R\" (random selection), \"S\" (entity cloning),  \"C\" (custom logic), \"PR\" (parameters with a random selection), \"P\" (parameters), \"ALL\" (all entities), or \"REF\" (reference only task)") String selection_method, @param(description="Can be populated by 'OFF', 'FORCE', or can be empty") String sync_mode, Boolean version_ind, Boolean replace_sequences, Boolean delete_before_load, @param(description="Populated by \"extract\", \"load\", or \"reserve\"", required=true) String task_type, Boolean reserve_ind) throws Exception {
		// variables declaration
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String env_id;
		String role_id;
		String env_name;
		
		List<Map<String, Object>> finalSourceEnvs = new ArrayList<>();
		List<Map<String, Object>> finalTargetEnvs = new ArrayList<>();
		
		lus = lus.replaceAll("\\s+","");
		String[] lus_arr=lus.split(",");
		ArrayList<String> lus_list = new ArrayList<String>();
		for(String str : lus_arr)
			lus_list.add(str);
		Map<String,Object> be_lus= new HashMap<String,Object>();
		be_lus.put("be_id",be);
		be_lus.put("LU List", lus_list);
		
		try {
		
			List<Map<String, Object>> allUserEnvsTypes = fnGetUserEnvs("");
			int i=0;
			for (Map<String, Object> envType : allUserEnvsTypes) {
				List<Map<String, Object>> allSourceEnvs = (List<Map<String, Object>>) (envType.get("source environments"));
				List<Map<String, Object>> allTargetEnvs = (List<Map<String, Object>>) (envType.get("target environments"));
		
				if(allSourceEnvs!=null && !"reserve".equalsIgnoreCase(task_type)) {
					// loop over user source envs
					for (Map<String, Object> sourceEnvMap : allSourceEnvs) {
		
						env_id = "" + sourceEnvMap.get("environment_id");
						role_id = "" + sourceEnvMap.get("role_id");
						env_name = "" + sourceEnvMap.get("environment_name");
		
						//check if source env satisfies all relevant cases
						if (fnValidateSourceEnvForTask(be_lus, refcount, selection_method, sync_mode, version_ind, task_type, sourceEnvMap).isEmpty()) {
							Map<String, Object> envData = new HashMap<>();
							envData.put("environment_id", env_id);
							envData.put("environment_name", env_name);
							envData.put("role_id", role_id);
							finalSourceEnvs.add(envData);
		
						}
					}
				}
		
				// loop over user target envs only if it is a load task, otherwsie target envs are not relevant
				if (("load".equalsIgnoreCase(task_type) || "reserve".equalsIgnoreCase(task_type)) && allTargetEnvs!=null) {
					for (Map<String, Object> targetEnvMap : allTargetEnvs) {
		
						env_id = "" + targetEnvMap.get("environment_id");
						role_id = "" + targetEnvMap.get("role_id");
						env_name = "" + targetEnvMap.get("environment_name");
						int noOfEntities = -1;
						//check if target env satisfies all relevant cases
						if (fnValidateTargetEnvForTask(be_lus, refcount, selection_method, version_ind, replace_sequences, delete_before_load, task_type, reserve_ind, noOfEntities, targetEnvMap).isEmpty()) {
							Map<String, Object> envData = new HashMap<>();
							envData.put("environment_id", env_id);
							envData.put("environment_name", env_name);
							envData.put("role_id", role_id);
							finalTargetEnvs.add(envData);
		
						}
					}
				}
			}
		
			List<Map<String, Object>> result = new ArrayList<>();
			Map<String, Object> sourceEnvsMap = new HashMap<>();
			sourceEnvsMap.put("source environments", finalSourceEnvs);
			result.add(sourceEnvsMap);
			Map<String, Object> targetEnvsMap = new HashMap<>();
			targetEnvsMap.put("target environments", finalTargetEnvs);
			result.add(targetEnvsMap);
		
			response.put("result", result);
			errorCode = "SUCCESS";
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
			errorCode = "FAILED";
		}
		
		
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Checks if the list of requested entities are copied successfully in the selected data version across all the input LU names in the task'sBusiness Entity hierarchy (the versionsForLoad API only checks the status of root LUI). For example, if the customer's order is not extracted succesfuly to Fabric, the validation on the related customer returns it as a failed entity. \r\n" +
			" \r\n" +
			"Example of a request body:\r\n" +
			"{\r\n" +
			"  \"entitiesList\": \"1,2\",\r\n" +
			"  \"taskExecId\": \"2\",\r\n" +
			"  \"lu_list\": [\r\n" +
			"  {\r\n" +
			"   \"lu_name\": \"Billing\"\r\n" +
			"  },\r\n" +
			"  {\r\n" +
			"    \"lu_name\": \"Customer\"\r\n" +
			"  },\r\n" +
			"  {\r\n" +
			"    \"lu_name\": \"Orders\"\r\n" +
			"  }\r\n" +
			"]\r\n" +
			"}")
	@webService(path = "tasks/validateVersionForLoad", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDataVersionValidation(@param(required=true) String entitiesList, @param(required=true) String taskExecId, @param(required=true) List<Map<String,Object>> lu_list) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="SUCCESS";
		List<HashMap<String, Object>> result = new ArrayList<>();
		
		String logicalUnitList = "";
		for (Map<String, Object> lu : lu_list) {
			logicalUnitList += ("'" + lu.get("lu_name") + "',");
		}
		
		if (logicalUnitList != "") logicalUnitList = logicalUnitList.substring(0, logicalUnitList.length() - 1);
		
		entitiesList = entitiesList.replaceAll("\\s+","");
		String[] entitiesArray = entitiesList.split(",");
		String entitiesListForQuery = "";
		for (String entityID : entitiesArray) {
			entitiesListForQuery += "'" + entityID + "',";
		}
		
		if (entitiesListForQuery != "") entitiesListForQuery = entitiesListForQuery.substring(0, entitiesListForQuery.length() - 1);
		
		fabric().fetch("get TDM.?", taskExecId);
		
		String sqlQuery = "SELECT be_root_entity_id, lu_name, iid FROM task_execution_link_entities " +
					"WHERE lu_name IN " +  "(" + logicalUnitList + ")" + " AND be_root_entity_id IN " + "(" + entitiesListForQuery + ") " +
					"AND lower(execution_status) != 'completed'" ;
		
		//log.info("sqlQuery: " + sqlQuery);
		Db.Rows failedEntities = fabric().fetch(sqlQuery);
		
		Set<String> setOFFailedEntities = new HashSet<>();
		String listOfFailed = "";
		for (Db.Row row : failedEntities) {
			HashMap<String, Object> failedEntity = new HashMap<>();
			failedEntity.put("root_entity_id", row.get("be_root_entity_id"));
			failedEntity.put("lu_name", row.get("lu_name"));
			failedEntity.put("lu_entity_id", row.get("iid"));
			result.add(failedEntity);
			setOFFailedEntities.add("" + row.get("be_root_entity_id"));
		}
		
		for (String failedEntityID : setOFFailedEntities) {
			if ("".equals(listOfFailed)) {
				listOfFailed = failedEntityID;
			} else {
				listOfFailed += ", " + failedEntityID;
			}
		}
		if(result != null && result.size() > 0) {
			errorCode="FAILED";
			message = listOfFailed;
		}
		
		if(failedEntities != null) {
			failedEntities.close();
		}		
		response.put("result", result);
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Get the list of custom logic flows to be used to select the list of entities for give root LU.\r\n" +
			"The input can be one root LU or more separated by comma.")
	@webService(path = "getcustomlogicflows", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"luName\": \"Customer\",\r\n" +
			"      \"flowName\": \"test\",\r\n" +
			"      \"Description\": \"test 1\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"luName\": \"Customer\",\r\n" +
			"      \"flowName\": \"test2\",\r\n" +
			"      \"Description\": \"test 2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetCustomLogicFlows(String luList) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "SUCCESS";
		 List<HashMap<String,Object>> result = new ArrayList<>();
		
		luList = luList.replaceAll("\\s+", "");
		String[] lus = luList.split(",");
		
		for (String luName : lus) {
			List<HashMap<String,Object>> luResult = (List<HashMap<String,Object>>)(fabric().fetch("broadway " + luName + ".GetCustomLogicFlows LU_NAME='" + luName + "'").firstRow()).get("value");
			if (luResult != null) {
				result.addAll(luResult);
			}
		}
		
		//Add flows that can run for all LUs
		List<HashMap<String,Object>> luResult = (List<HashMap<String,Object>>)(fabric().fetch("broadway TDM.GetCustomLogicFlows LU_NAME=''").firstRow()).get("value");
		if (luResult != null) {
				result.addAll(luResult);
		}
		
		response.put("result",result);
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Get the list of parameters of the given custom Flow")
	@webService(path = "getCustomLogicParams", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"editor\": {\r\n" +
			"        \"schema\": {\r\n" +
			"          \"type\": \"date\"\r\n" +
			"        },\r\n" +
			"        \"name\": \"param1\",\r\n" +
			"        \"context\": {\r\n" +
			"          \"level\": {\r\n" +
			"            \"const\": \"error\"\r\n" +
			"          },\r\n" +
			"          \"message\": {\r\n" +
			"            \"const\": \"${param1}, ${param2}\"\r\n" +
			"          },\r\n" +
			"          \"param1\": {\r\n" +
			"            \"self\": \"param1\"\r\n" +
			"          },\r\n" +
			"          \"param2\": {\r\n" +
			"            \"external\": \"param2\"\r\n" +
			"          }\r\n" +
			"        },\r\n" +
			"        \"id\": \"com.k2view.default\"\r\n" +
			"      },\r\n" +
			"      \"default\": null,\r\n" +
			"      \"description\": \"\",\r\n" +
			"      \"type\": \"date\",\r\n" +
			"      \"mandatory\": false\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"editor\": {\r\n" +
			"        \"schema\": {\r\n" +
			"          \"type\": \"integer\"\r\n" +
			"        },\r\n" +
			"        \"name\": \"param2\",\r\n" +
			"        \"context\": {\r\n" +
			"          \"level\": {\r\n" +
			"            \"const\": \"error\"\r\n" +
			"          },\r\n" +
			"          \"message\": {\r\n" +
			"            \"const\": \"${param1}, ${param2}\"\r\n" +
			"          },\r\n" +
			"          \"param1\": {\r\n" +
			"            \"external\": \"param1\"\r\n" +
			"          },\r\n" +
			"          \"param2\": {\r\n" +
			"            \"self\": \"param2\"\r\n" +
			"          }\r\n" +
			"        },\r\n" +
			"        \"id\": \"com.k2view.default\"\r\n" +
			"      },\r\n" +
			"      \"default\": null,\r\n" +
			"      \"description\": \"\",\r\n" +
			"      \"type\": \"integer\",\r\n" +
			"      \"mandatory\": false\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetCustomLogicParam(String luName, String flowName) throws Exception {
		List<HashMap<String,Object>> result = new ArrayList<>();
		if ("ALL".equalsIgnoreCase(luName) || luName == null || Util.isEmpty(luName)) {
			luName = "TDM";
		}
		try {
			Db.Rows rows = fabric().fetch("list BF lu_name = '" + luName + "' flow='" + flowName +"'");
			for (Db.Row row : rows) {
				if ( !"LU_NAME".equalsIgnoreCase("" + row.get("name")) 
					&& !"NUM_OF_ENTITIES".equalsIgnoreCase("" + row.get("name"))
					&& !"SESSION_GLOBALS".equalsIgnoreCase("" + row.get("name"))
					&& "input".equalsIgnoreCase("" + row.get("param")) ) {
					HashMap<String, Object> map = new HashMap<>();
					HashMap<Object, Object> editorMap = new HashMap<>();
					Map<String, Object[]> editor = Json.get().fromJson(ParamConvertor.toString(row.get("editor")));
					Map<?, ?> context = Json.get().fromJson(ParamConvertor.toString(row.get("context")));
					Object[] interfaces = new Object[0];
					if("com.k2view.interface".equalsIgnoreCase(String.valueOf(editor.get("id")))){
						String broadwayCommand = "Broadway TDM.ListEditorInterfaces editor=" + row.get("editor");
						Db.Rows interfaceNames = fabric().fetch(broadwayCommand);
						for(Db.Row name:interfaceNames){
							interfaces = ParamConvertor.toArray(name.get("array"));
		
						}
						editor.put("interfaces",interfaces);
					}
					if (editor.isEmpty()) {
						editorMap.put("id","com.k2view.default");
					} else {
						editorMap.putAll(editor);
					}
					editorMap.put("name", row.get("name"));
					editorMap.put("schema",Json.get().fromJson((String) row.get("schema")));
					editorMap.put("mandatory", row.get("mandatory"));
					editorMap.put("context", context);
					map.put("editor", editorMap);
					map.put("default", row.get("default"));
					map.put("type", row.get("type"));
					map.put("mandatory", row.get("mandatory"));
					String description = ("" + row.get("remark")).replaceAll("/n", "\n");
					map.put("description", description);
					result.add(map);
				}
			}
			if(rows != null) {
				rows.close();
			}
		} catch (Exception e) {
			return wrapWebServiceResults("FAILED", e.getMessage(), null);
		}
		
		return wrapWebServiceResults("SUCCESS", null, result);
	}
	@desc("Get the list of parameters of the given custom Flow.\r\n" +
			"It gets a list of LU Names separated by comma, and optional task id. Task Id will be sent in case of updating an existing task, to get the values already set in that task.")
	@webService(path = "getDMPopParams", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"\t\"result\": {\r\n" +
			"\t\t\"activity_note\": {\r\n" +
			"\t\t\t\"editor\": {\r\n" +
			"\t\t\t\t\"name\": \"activity_note\",\r\n" +
			"\t\t\t\t\"schema\": {\r\n" +
			"\t\t\t\t\t\"type\": \"string\"\r\n" +
			"\t\t\t\t},\r\n" +
			"\t\t\t\t\"context\": {\r\n" +
			"\t\t\t\t\t\"activity_note\": {\r\n" +
			"\t\t\t\t\t\t\"self\": \"value\"\r\n" +
			"\t\t\t\t\t},\r\n" +
			"\t\t\t\t\t\"value\": {\r\n" +
			"\t\t\t\t\t\t\"const\": \"My Note\"\r\n" +
			"\t\t\t\t\t}\r\n" +
			"\t\t\t\t},\r\n" +
			"\t\t\t\t\"syncOutput\": true,\r\n" +
			"\t\t\t\t\"id\": \"com.k2view.default\"\r\n" +
			"\t\t\t},\r\n" +
			"\t\t\t\"default\": \"My Note\",\r\n" +
			"\t\t\t\"description\": \"\",\r\n" +
			"\t\t\t\"type\": \"string\",\r\n" +
			"\t\t\t\"mandatory\": false\r\n" +
			"\t\t},\r\n" +
			"\t\t\"billing_balance_number_of_records\": {\r\n" +
			"\t\t\t\"editor\": {\r\n" +
			"\t\t\t\t\"schema\": {\r\n" +
			"\t\t\t\t\t\"type\": \"integer\"\r\n" +
			"\t\t\t\t},\r\n" +
			"\t\t\t\t\"name\": \"billing_balance_number_of_records\",\r\n" +
			"\t\t\t\t\"context\": {\r\n" +
			"\t\t\t\t\t\"billing_balance_number_of_records\": {\r\n" +
			"\t\t\t\t\t\t\"self\": \"distribution\"\r\n" +
			"\t\t\t\t\t},\r\n" +
			"\t\t\t\t\t\"distribution\": {\r\n" +
			"\t\t\t\t\t\t\"const\": {\r\n" +
			"\t\t\t\t\t\t\t\"distribution\": \"uniform\",\r\n" +
			"\t\t\t\t\t\t\t\"round\": true,\r\n" +
			"\t\t\t\t\t\t\t\"type\": \"integer\",\r\n" +
			"\t\t\t\t\t\t\t\"minimum\": 1,\r\n" +
			"\t\t\t\t\t\t\t\"maximum\": 3\r\n" +
			"\t\t\t\t\t\t}\r\n" +
			"\t\t\t\t\t}\r\n" +
			"\t\t\t\t},\r\n" +
			"\t\t\t\t\"id\": \"com.k2view.distribution\"\r\n" +
			"\t\t\t},\r\n" +
			"\t\t\t\"default\": {\r\n" +
			"\t\t\t\t\"distribution\": \"uniform\",\r\n" +
			"\t\t\t\t\"round\": true,\r\n" +
			"\t\t\t\t\"type\": \"integer\",\r\n" +
			"\t\t\t\t\"minimum\": 1,\r\n" +
			"\t\t\t\t\"maximum\": 3\r\n" +
			"\t\t\t},\r\n" +
			"\t\t\t\"description\": \"Distribution Of Records Of table balance\",\r\n" +
			"\t\t\t\"type\": \"any\",\r\n" +
			"\t\t\t\"mandatory\": \"false\"\r\n" +
			"\t\t},\r\n" +
			"\t\t\"state\": {\r\n" +
			"\t\t\t\"editor\": {\r\n" +
			"\t\t\t\t\"name\": \"state\",\r\n" +
			"\t\t\t\t\"schema\": {\r\n" +
			"\t\t\t\t\t\"type\": \"string\"\r\n" +
			"\t\t\t\t},\r\n" +
			"\t\t\t\t\"context\": {\r\n" +
			"\t\t\t\t\t\"mtableRandomRow\": {\r\n" +
			"\t\t\t\t\t\t\"const\": true\r\n" +
			"\t\t\t\t\t},\r\n" +
			"\t\t\t\t\t\"mtable\": {\r\n" +
			"\t\t\t\t\t\t\"const\": \"addresses\"\r\n" +
			"\t\t\t\t\t},\r\n" +
			"\t\t\t\t\t\"city\": {\r\n" +
			"\t\t\t\t\t\t\"external\": \"city\"\r\n" +
			"\t\t\t\t\t},\r\n" +
			"\t\t\t\t\t\"state\": {\r\n" +
			"\t\t\t\t\t\t\"self\": \"state\"\r\n" +
			"\t\t\t\t\t},\r\n" +
			"\t\t\t\t\t\"mtableKey\": {\r\n" +
			"\t\t\t\t\t\t\"const\": {}\r\n" +
			"\t\t\t\t\t},\r\n" +
			"\t\t\t\t\t\"mtableCaseSensitive\": {\r\n" +
			"\t\t\t\t\t\t\"const\": true\r\n" +
			"\t\t\t\t\t}\r\n" +
			"\t\t\t\t},\r\n" +
			"\t\t\t\t\"id\": \"com.k2view.mTableKey\"\r\n" +
			"\t\t\t},\r\n" +
			"\t\t\t\"default\": null,\r\n" +
			"\t\t\t\"description\": \"\",\r\n" +
			"\t\t\t\"type\": \"string\",\r\n" +
			"\t\t\t\"mandatory\": false\r\n" +
			"\t\t}\r\n" +
			"\t},\r\n" +
			"\t\"errorCode\": \"SUCCESS\",\r\n" +
			"\t\"message\": null\r\n" +
			"}")
	public static Object wsGetDMPopParams(String luList, Long taskId) throws Exception {
		LinkedHashMap<String, HashMap<String, Object>> result = new LinkedHashMap<>();
		SortedMap<String, HashMap<String, Object>> flowsParams = new TreeMap<>();
		
		try {
		        luList = luList.replaceAll("\\s+", "");
		        String[] lus = luList.split(",");
		
		        for (String luName : lus) {
		            String rootTableName = getGlobal("ROOT_TABLE_NAME", luName);
				    Db.Rows flows = fabric().fetch("list BF lu_name = " + luName + " tag = 'Generate Data'");
		
				for (Db.Row flow : flows) {
					String flowName = flow.get("Flow").toString();
					//log.info("wsGetDMPopParams - Flow Name: " + flowName);
					Db.Rows rows = fabric().fetch("list BF lu_name = '" + luName + "' flow='" + flowName + "'");
					for (Db.Row row : rows) {
						String name = "" + row.get("name");
						//log.info("wsGetDMPopParams - Parameter Name: " + name + " param: " + row.get("param"));
						if (!"LU_NAME".equalsIgnoreCase(name)
								&& !"NUM_OF_ENTITIES".equalsIgnoreCase(name)
								&& !"total".equalsIgnoreCase(name)
								&& !"count".equalsIgnoreCase(name)
								&& !"parent_row".equalsIgnoreCase(name)
								&& !"parent_rows".equalsIgnoreCase(name)
								&& !"table".equalsIgnoreCase(name)
								&& "input".equalsIgnoreCase("" + row.get("param"))) {
							//log.info("Adding Param: " + name);
							HashMap<String, Object> map = new HashMap<>();
							HashMap<Object, Object> editorMap = new HashMap<>();
							Map<String, Object[]> editor = Json.get().fromJson(ParamConvertor.toString(row.get("editor")));
		                    Map<?, ?> context = Json.get().fromJson(ParamConvertor.toString(row.get("context")));
							Object[] interfaces = new Object[0];
							if("com.k2view.interface".equalsIgnoreCase(String.valueOf(editor.get("id")))){
								String broadwayCommand = "Broadway TDM.ListEditorInterfaces editor=" + row.get("editor");
								Db.Rows interfaceNames = fabric().fetch(broadwayCommand);
								for(Db.Row Name:interfaceNames){
									interfaces = ParamConvertor.toArray(Name.get("array"));
		
								}
								editor.put("interfaces",interfaces);
							}
							if (editor.isEmpty()) {
		                              editorMap.put("id","com.k2view.default");
		                          } else {
		                              editorMap.putAll(editor);
		                          }
		
							editorMap.put("name", name);
		                          if ("com.k2view.distribution".equals(editorMap.get("id"))) {
		                              Map<?,?> distMap = Json.get().fromJson(ParamConvertor.toString(context.get("distribution")));
		                              Map<?,?> constMap = Json.get().fromJson(ParamConvertor.toString(distMap.get("const")));
		                              //Map<?,?> innerDist = Json.get().fromJson(ParamConvertor.toString(constMap.get("distribution")));
		
		                              Object schema =  Json.get().fromJson(ParamConvertor.toString(constMap.get("type")));
		
		                              String schemaJson = "{type=" + schema.toString() + "}";
		                              editorMap.put("schema", Json.get().fromJson(schemaJson));
		                          } else {
		                              editorMap.put("schema", Json.get().fromJson((String) row.get("schema")));
		                          }
							editorMap.put("mandatory", row.get("mandatory"));
		                    editorMap.put("context", context);
							map.put("editor", editorMap);
							map.put("type", row.get("type"));
							map.put("mandatory", row.get("mandatory"));
							map.put("default", row.get("default"));
		
							String description = ("" + row.get("remark")).replaceAll("/n", "\n");
							map.put("description", description);
							map.put("order", 99999999);
		                          
							flowsParams.put(name, map);
						}
					}
					if (rows != null) {
						rows.close();
					}
				}
		
		        // Add the distribution of each table in the LU
		        List<String> tablesList = getLuTablesList(luName);
		        for (String tableName : tablesList) {
		                  if (!rootTableName.equalsIgnoreCase(tableName)) {
		                      HashMap<String, Object> map = new HashMap<>();
		                      HashMap<Object, Object> editorMap = new HashMap<>();
		                String distParamName = luName.toLowerCase() + "_" + tableName.toLowerCase() + "_number_of_records";
						String minDist = "" + db(TDM).fetch("SELECT PARAM_VALUE FROM " + TDMDB_SCHEMA + ".TDM_GENERAL_PARAMETERS " +
							"WHERE PARAM_NAME = 'TABLE_DEFAULT_DISTRIBUTION_MIN'").firstValue();
						String maxDist = "" + db(TDM).fetch("SELECT PARAM_VALUE FROM " + TDMDB_SCHEMA + ".TDM_GENERAL_PARAMETERS " +
							"WHERE PARAM_NAME = 'TABLE_DEFAULT_DISTRIBUTION_MAX'").firstValue();
		                String contextString = "{" + distParamName + 
		                    "={self=distribution},distribution={const={distribution=uniform,round=true,type=integer,minimum=" + 
							minDist + ",maximum=" + maxDist + "}}}";
		                Map<?, ?> distDefault = Json.get().fromJson("{distribution=uniform,round=true,type=integer,minimum=" + 
							minDist + ",maximum=" + maxDist + "}");
		                Map<?, ?> context = Json.get().fromJson(contextString);
		                editorMap.put("id","com.k2view.distribution");
		                editorMap.put("name", distParamName);
					    editorMap.put("schema", Json.get().fromJson("{type=integer}"));
		                editorMap.put("context", context);
						editorMap.put("mandatory", false);
		                map.put("editor", editorMap);
				        map.put("type", "any");
				        map.put("mandatory", false);
				        map.put("default", distDefault);
				        map.put("description", "Distribution Of Records Of table " + tableName);
				        flowsParams.put(distParamName, map);
						map.put("order", 99999999);
		            }
		        }
			}
		          
		          //log.info("wsGetDMPopParams - taskId: " + taskId);
		    if (taskId != null && taskId > 0) {
		        String luName = getLuType().luName;
		        String selectSql = "broadway " + luName + ".GetGenerateParamsByTaskId task_id=?" + ", RESULT_STRUCTURE=ROW";
		        //log.info("wsGetDMPopParams - sql: " + selectSql);
		        Db.Row taskParams = fabric().fetch(selectSql, taskId).firstRow();
				Map<String, Object> taskParamsMap = (HashMap)taskParams.get("value");
				if (taskParamsMap != null && !taskParamsMap.isEmpty()) {
					for (String paramName : taskParamsMap.keySet()) {
						//log.info("wsGetDMPopParams - paramName: " + paramName);
						HashMap<String,Object> paramMap  = flowsParams.get(paramName);
						if (paramMap != null) {
							Object newValue = ((Map<String, Object>)taskParamsMap.get(paramName)).get("value");
							Long order = (Long)((Map<String, Object>)taskParamsMap.get(paramName)).get("order");
							//log.info("wsGetDMPopParams - paramValue: " + taskParamsMap.get(paramName));
							paramMap.put("value", newValue);
							paramMap.put("order", order);
							flowsParams.remove(paramName);
							result.put(paramName, paramMap);
						}   
					}
				}
			}
			result.putAll(flowsParams);
		 } catch (Exception e) {
		          //log.error
		          e.printStackTrace();
		          log.error(e.getMessage());
			return wrapWebServiceResults("FAILED", e.getMessage(), null);
		}
		
		return wrapWebServiceResults("SUCCESS", null, result);
	}
}
