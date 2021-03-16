/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDMTasks;

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
import com.k2view.cdbms.usercode.lu.k2_ws.*;
import com.k2view.fabric.api.endpoint.Endpoint.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {

	private static Map<String, Object> wrapWebServiceResults(String errorCode, String message, Object result) {
		HashMap<String, Object> response = new HashMap<>();
		response.put("errorCode", errorCode);
		response.put("message", message);
		response.put("result", result);
		return response;
	}
	
	@desc("This function will get a user name and type and return the list of active tasks that the luser can run")
	@webService(path = "wsGetUserTasks/{userName}/{userType}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.XML, example = "<HashMap>\n  <result>\n    <task_last_updated_date>1615278319339</task_last_updated_date>\n    <be_id>1</be_id>\n    <selected_version_task_name/>\n    <environment_id>1</environment_id>\n    <selection_method>L</selection_method>\n    <selected_ref_version_task_name/>\n    <refresh_reference_data/>\n    <task_id>21</task_id>\n    <source_environment_id>1</source_environment_id>\n    <scheduler>immediate</scheduler>\n    <selected_ref_version_datetime/>\n    <source_env_name>ENV1</source_env_name>\n    <load_entity>false</load_entity>\n    <task_title>tester</task_title>\n    <selected_version_task_exe_id/>\n    <task_created_by>k2vtester01</task_created_by>\n    <fabric_environment_name/>\n    <scheduling_end_date/>\n    <delete_before_load>false</delete_before_load>\n    <retention_period_type/>\n    <task_status>Active</task_status>\n    <selection_param_value>1,2,3,4,5,6,7,8,9,10</selection_param_value>\n    <retention_period_value/>\n    <selected_version_datetime/>\n    <task_last_updated_by>k2vtester01</task_last_updated_by>\n    <selected_ref_version_task_exe_id/>\n    <task_execution_status>Active</task_execution_status>\n    <version_ind>false</version_ind>\n    <sync_mode/>\n    <number_of_entities_to_copy>10</number_of_entities_to_copy>\n    <task_creation_date>1615278319339</task_creation_date>\n    <task_globals>false</task_globals>\n    <replace_sequences/>\n    <entity_exclusion_list/>\n    <task_type>EXTRACT</task_type>\n    <parameters/>\n  </result>\n  <errorCode>SUCCESS</errorCode>\n  <message/>\n</HashMap>")
	@resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": [\n    {\n      \"task_last_updated_date\": \"2021-03-09 08:25:19.339\",\n      \"be_id\": 1,\n      \"selected_version_task_name\": null,\n      \"environment_id\": 1,\n      \"selection_method\": \"L\",\n      \"selected_ref_version_task_name\": null,\n      \"refresh_reference_data\": null,\n      \"task_id\": 21,\n      \"source_environment_id\": 1,\n      \"scheduler\": \"immediate\",\n      \"selected_ref_version_datetime\": null,\n      \"source_env_name\": \"ENV1\",\n      \"load_entity\": false,\n      \"task_title\": \"tester\",\n      \"selected_version_task_exe_id\": null,\n      \"task_created_by\": \"k2vtester01\",\n      \"fabric_environment_name\": null,\n      \"scheduling_end_date\": null,\n      \"delete_before_load\": false,\n      \"retention_period_type\": null,\n      \"task_status\": \"Active\",\n      \"selection_param_value\": \"1,2,3,4,5,6,7,8,9,10\",\n      \"retention_period_value\": null,\n      \"selected_version_datetime\": null,\n      \"task_last_updated_by\": \"k2vtester01\",\n      \"selected_ref_version_task_exe_id\": null,\n      \"task_execution_status\": \"Active\",\n      \"version_ind\": false,\n      \"sync_mode\": null,\n      \"number_of_entities_to_copy\": 10,\n      \"task_creation_date\": \"2021-03-09 08:25:19.339\",\n      \"task_globals\": false,\n      \"replace_sequences\": null,\n      \"entity_exclusion_list\": null,\n      \"task_type\": \"EXTRACT\",\n      \"parameters\": null\n    }\n  ],\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
	public static Object wsGetUserTasks(@param(required=true) String userName, @param(required=true) String userType) throws Exception {
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


	@desc("This function returns the details of the current/last execution of the given task_id. If the task is pending, it will return only its status, else it will return the statistics of the entities it is handling/handled.")
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


	@desc("This web service returns active task ID by its name.")
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
				fetch("select public.tasks.task_id from public.tasks where public.tasks.task_title= ? and public.tasks.task_execution_status='Active'", taskName).firstValue();
		if (response == null) {
			return wrapWebServiceResults("FAIL", "No active task found for task name '" + taskName + "'.", response);
		} else {
			return wrapWebServiceResults("SUCCESS", null, response);
		}
	}
}
