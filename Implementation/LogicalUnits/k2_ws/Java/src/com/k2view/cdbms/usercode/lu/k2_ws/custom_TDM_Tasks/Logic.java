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

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends WebServiceUserCode {

	@desc("Gets regular active tasks (task_status and task_execution_status columns are active) for a user based on the user's permission group (admin, owner, or tester) and based on the user's TDM environment roles:\r\n" +
			"\r\n" +
			"Admin Users:\r\n" +
			"- Get all active tasks.\r\n" +
			"\r\n" +
			"Owner Users:\r\n" +
			"- Get all active extract tasks if the user is the owner of at least one source environment.\r\n" +
			"- Get all active load tasks if the user is the owner of at least one source environment and one target environment.\r\n" +
			"- Get all active extract tasks that do not require special permissions if the user has at least one Read TDM Environment role.\r\n" +
			"- Get all active extract tasks that require special permissions of the user has at least one Read TDM Environment role with these permissions.\r\n" +
			"- Get all active load tasks that do not require special permissions if the user has at least one Read TDM Environment role and one Write TDM Environment role.\r\n" +
			"- Get all active load tasks that require special permissions of the user has at least one Read TDM Environment role, and one Write TDM Environment role with these permissions.\r\n" +
			"\r\n" +
			"Tester Users:   \r\n" +
			"- Get all active extract tasks that do not require special permissions if the user has at least one Read TDM Environment role.\r\n" +
			"- Get all active extract tasks that require special permissions of the user has at least one Read TDM Environment role with these permissions.\r\n" +
			"- Get all active load tasks that do not require special permissions if the user has at least one Read TDM Environment role and one Write TDM Environment role.\r\n" +
			"- Get all active load tasks that require special permissions of the user has at least one Read TDM environment role, and one Write TDM Environment role with these permissions.")
	@webService(path = "regularTasksByUser", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
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
		List<Map<String, Object>> result = new ArrayList<>();
		try {
			String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
			if ("admin".equals(permissionGroup)){
				String sql = "select task_id, task_title from tasks where lower(task_status) = 'active' and lower(task_execution_status) = 'active'";
				Db.Rows rows = db("TDM").fetch(sql);
				List<String> columnNames = rows.getColumnNames();
				for (Db.Row row : rows) {
					ResultSet resultSet = row.resultSet();
					Map<String, Object> rowMap = new HashMap<>();
					for (String columnName : columnNames) {
						rowMap.put(columnName, resultSet.getObject(columnName));
					}
					result.add(rowMap);
				}
			}
			else if ("tester".equals(permissionGroup)){
				List<Map<String, Object>> envsList = (List<Map<String, Object>>) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Environments.Logic.wsGetListOfEnvsByUser()).get("result");
				Map<String,Object> roleIdsByEnvType=new HashMap<>();
				for (Map<String, Object> envsGroup : envsList) {
					Map.Entry<String, Object> entry = envsGroup.entrySet().iterator().next();
					List<Map<String, Object>> groupByType = (List<Map<String, Object>>) entry.getValue();
					roleIdsByEnvType.put(entry.getKey(),new ArrayList<String>());
					List<String> roleIds = (ArrayList<String>)roleIdsByEnvType.get(entry.getKey());
					for (Map<String, Object> env : groupByType) {
						if (!"owner".equals(env.get("role_id").toString())
								&& !"admin".equals(env.get("role_id").toString())) {
							roleIds.add(env.get("role_id").toString());
						}
					}
				}

				List<String> srcRoleIds = (ArrayList<String>)roleIdsByEnvType.get("source environments");
				List<String> tarRoleIds = (ArrayList<String>)roleIdsByEnvType.get("target environments");

				if(!srcRoleIds.isEmpty()){
					// Get all extract tasks that do not require special permissions except read permissions
					String sql = "select array_Agg(src_role_id) as src_role_list, task_id, task_title\n" +
							"from\n" +
							"(\n" +
							"SELECT distinct src.role_id as src_role_id, t.task_id, t.task_title\n" +
							"FROM public.tasks t, environment_roles src\n" +
							"where\n" +
							"src.role_id in ("+String.join(",",srcRoleIds)+")\n" +
							"and src.allow_read = true\n" +
							"and lower(t.task_type) = 'extract'\n" +
							"and t.version_ind = false\n" +
							"and lower(task_status) = 'active' and lower(task_execution_status) = 'active'\n" +
							"and t.refresh_reference_data is not true\n" +
							"and lower(COALESCE(t.sync_mode, 'on')) <> 'force' and t.selection_method <> 'ALL'" ;
					// Get all extract tasks with special permissions
					sql += "UNION\n" +
							"SELECT distinct src.role_id as src_role_id, t.task_id, t.task_title\n" +
							"FROM public.tasks t, environment_roles src\n" +
							"where\n" +
							"src.role_id in ("+String.join(",",srcRoleIds)+")\n" +
							"and src.allow_read = true\n" +
							"and lower(t.task_type) = 'extract'\n" +
							"and t.version_ind = false\n" +
							"and lower(task_status) = 'active' and lower(task_execution_status) = 'active'\n" +
							"and (t.refresh_reference_data is not true or src.allowed_refresh_reference_data = true)\n" +
							"and (lower(COALESCE(t.sync_mode, 'on')) <> 'force' or src.allowed_request_of_Fresh_data = true) and t.selection_method <> 'ALL'\n" +
							") extTaskList\n" +
							"group by task_id, task_title" ;
					Db.Rows rows = db("TDM").fetch(sql);
					List<String> columnNames = rows.getColumnNames();
					for (Db.Row row : rows) {
						ResultSet resultSet = row.resultSet();
						Map<String, Object> rowMap = new HashMap<>();
						for (String columnName : columnNames) {
							rowMap.put(columnName, resultSet.getObject(columnName));
						}
						result.add(rowMap);
					}
				}

				if(srcRoleIds.size()>0&&tarRoleIds.size()>0) {
					// Get all load regular tasks and their available roles. The user needs to be assigned at least to on of the source TDM environment role IDs
					String sql = "select array_Agg(src_role_id) as src_role_list, array_Agg(tar_role_id) as tar_role_list, task_id, task_title\n" +
							"from\n" +
							"(";
					// Get the load regular tasks the do not require special permisions except read and write permissions
					sql += "SELECT distinct src.role_id src_role_id, tar.role_id tar_role_id , t.task_id, t.task_title\n" +
							"FROM public.tasks t, environment_roles src, environment_roles tar\n" +
							"where src.role_id in ("+String.join(",",srcRoleIds)+") \n" +
							"and tar.role_id in ("+String.join(",",tarRoleIds)+") \n" +
							"and src.allow_read = true\n" +
							"and lower(t.task_type) = 'load'\n" +
							"and tar.allow_write = true\n" +
							"and t.version_ind = false\n" +
							"and lower(task_status) = 'active' and lower(task_execution_status) = 'active'\n" +
							"and t.selection_method not in ('S', 'R')\n" +
							"and t.refresh_reference_data is not true\n" +
							"and t.delete_before_load <> true\n" +
							"and t.replace_sequences <> true\n" +
							"and lower(COALESCE(t.sync_mode, 'on')) <> 'force' and t.selection_method <> 'ALL'\n" +
							"UNION ";
					// Get all load tasks with special permissions
					sql += "SELECT distinct src.role_id src_role_id, tar.role_id tar_role_id , t.task_id, t.task_title\n" +
							"FROM public.tasks t, environment_roles src, environment_roles tar\n" +
							"where src.role_id in ("+String.join(",",srcRoleIds)+")\n" +
							"and tar.role_id in ("+String.join(",",tarRoleIds)+") \n" +
							"and src.allow_read = true\n" +
							"and lower(t.task_type) = 'load'\n" +
							"and tar.allow_write = true\n" +
							"and t.version_ind = false\n" +
							"and lower(task_status) = 'active' and lower(task_execution_status) = 'active'\n" +
							"and (t.selection_method not in ('S', 'R') or (t.selection_method = 'S' and tar.allowed_creation_of_synthetic_data = true) or (t.selection_method = 'R' and tar.allowed_random_entity_selection = true))\n" +
							"and (t.refresh_reference_data is not true or tar.allowed_refresh_reference_data = true )\n" +
							"and (t.delete_before_load is not true or tar.allowed_delete_before_load = true)\n" +
							"and (t.replace_sequences is not true or tar.allowed_replace_sequences = true)\n" +
							"and (lower(COALESCE(t.sync_mode, 'on')) <> 'force' or src.allowed_request_of_Fresh_data = true) and t.selection_method <> 'ALL'\n" +
							") loadTaskList\n" +
							"group by task_id, task_title";
					Db.Rows rows = db("TDM").fetch(sql);

					List<String> columnNames = rows.getColumnNames();
					for (Db.Row row : rows) {
						ResultSet resultSet = row.resultSet();
						Map<String, Object> rowMap = new HashMap<>();
						for (String columnName : columnNames) {
							rowMap.put(columnName, resultSet.getObject(columnName));
						}
						result.add(rowMap);
					}
				}
			} else if ("owner".equals(permissionGroup)){
				List<Map<String, Object>> envsList = (List<Map<String, Object>>) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Environments.Logic.wsGetListOfEnvsByUser()).get("result");
				Map<String,Object> roleIdsByEnvTypeAsOwner=new HashMap<>();
				Map<String,Object> roleIdsByEnvTypeAsTester=new HashMap<>();
				for (Map<String, Object> envsGroup : envsList) {
					Map.Entry<String, Object> entry = envsGroup.entrySet().iterator().next();
					List<Map<String, Object>> groupBytype = (List<Map<String, Object>>) entry.getValue();
					roleIdsByEnvTypeAsOwner.put(entry.getKey(),new ArrayList<String>());
					roleIdsByEnvTypeAsTester.put(entry.getKey(),new ArrayList<String>());
					List<String> roleIdsAsOwner = (ArrayList<String>)roleIdsByEnvTypeAsOwner.get(entry.getKey());
					List<String> roleIdsAsTester = (ArrayList<String>)roleIdsByEnvTypeAsTester.get(entry.getKey());
					for (Map<String, Object> env : groupBytype) {
						if ("owner".equals(env.get("role_id").toString())) {
							roleIdsAsOwner.add(env.get("role_id").toString());
						}
						else if ("user".equals(env.get("assignment_type").toString())) {
							roleIdsAsTester.add(env.get("role_id").toString());
						}
					}
				}

				List<String> srcRoleIdsAsOwner = (ArrayList<String>)roleIdsByEnvTypeAsOwner.get("source environments");
				List<String> tarRoleIdsAsOwner = (ArrayList<String>)roleIdsByEnvTypeAsOwner.get("target environments");

				List srcRoleIdsAsTester = (ArrayList<String>)roleIdsByEnvTypeAsTester.get("source environments");
				List tarRoleIdsAsTester = (ArrayList<String>)roleIdsByEnvTypeAsTester.get("target environments");

				/*if(true) return "srcRoleIdsAsTester: " + String.join(",",srcRoleIdsAsTester) +
						" tarRoleIdsAsTester: " + String.join(",",tarRoleIdsAsTester) +
						" srcRoleIdsAsOwner: " + String.join(",",srcRoleIdsAsOwner) +
						" tarRoleIdsAsOwner: " + String.join(",",tarRoleIdsAsOwner) ;
				 */

				//"-- Owner- Extract tasks
				String sql = "select task_id, task_title\n" +
						"from\n" +
						"(\n" +
						// Get all extract tasks where the user is the owner of at least one source environment\n" +
						"select  t.task_id, t.task_title\n" +
						"from tasks t\n" +
						"where lower(task_type) = 'extract' and lower(task_status) = 'active'\n" +
						"and lower(task_execution_status) = 'active'\n" +
						"and 1 = "+ (srcRoleIdsAsOwner.size()>0?"1 ":"0 "); //1 if the user is the owner of at least one source env. Else- it will be zero
				if(srcRoleIdsAsTester.size()>0)
					sql+="UNION\n" +
							// Get all extract tasks that do not require special permissions except read permissions
							"SELECT t.task_id, t.task_title\n" +
							"FROM public.tasks t, environment_roles src\n" +
							"where\n" +
							//check the list of the user's TDM environment roles of the source environments
							"src.role_id in ("+String.join(",",srcRoleIdsAsTester)+ ") " +
							"and src.allow_read = true\n" +
							"and lower(t.task_type) = 'extract'\n" +
							"and t.version_ind = false\n" +
							"and lower(task_status) = 'active' and lower(task_execution_status) = 'active'\n" +
							"and t.refresh_reference_data is not true\n" +
							"and lower(COALESCE(t.sync_mode, 'on')) <> 'force' and t.selection_method <> 'ALL'\n";
				sql+=")extTaskList\n" +
						"group by task_id, task_title";

				Db.Rows rows = db("TDM").fetch(sql);
				List<String> columnNames = rows.getColumnNames();
				for (Db.Row row : rows) {
					ResultSet resultSet = row.resultSet();
					Map<String, Object> rowMap = new HashMap<>();
					for (String columnName : columnNames) {
						rowMap.put(columnName, resultSet.getObject(columnName));
					}
					result.add(rowMap);
				}


				//-- Owner- Load tasks-
				// Get all load tasks where the user is the owner of at least one source and one target environments\n" +
				sql = "Select loadTaskList.task_id,\n" +
						"loadTaskList.task_title\n" +
						"From ("+
						"select distinct t.task_id, t.task_title\n" +
						"from tasks t\n" +
						"where lower(task_type) = 'load' and lower(task_status) = 'active'\n" +
						"and lower(task_execution_status) = 'active'\n" +
						"and 1 = "+(srcRoleIdsAsOwner.size()>0?"1 ":"0 ")+ //will be populated by 1 if the user is the owner of at least one source env. Else- it will be zero\n" +
						"and 1 = "+(tarRoleIdsAsOwner.size()>0?"1 ":"0 ")+ //will be populated by 1 if the user is the owner of at least one target env. Else- it will be zero\n" +
						"UNION\n" +
						// Get all load tasks where the user is the owner of at least one target env\n" +
						"select distinct t.task_id, t.task_title\n" +
						"FROM tasks t\n" +
						"where\n" +
						"(1= " +(srcRoleIdsAsOwner.size()>0?"1 ":"0 ") + //the user is an owner of a source env
						(srcRoleIdsAsTester.size()>0?"or exists (select 1 from environment_roles src where src.role_id in ("+String.join(",",srcRoleIdsAsTester)+") " +
								// check the list of the user's TDM environment roles of the source environments
								"and src.allow_read = true) ":" ") +")\n" +
						"and lower(task_type) = 'load' and lower(task_status) = 'active'\n" +
						"and lower(task_execution_status) = 'active'\n" +
						"and 1 = "+(tarRoleIdsAsOwner.size()>0?"1 ":"0 ");
				//will be populated by 1 if the user is the owner of at least one target env. Else- it will be zero\n" +
				if(tarRoleIdsAsTester.size()>0) sql+="UNION\n" +
						// Get the load regular tasks the do not require special permisions except read and write permissions
						"SELECT distinct task_id, t.task_title\n" +
						"From public.tasks t, environment_roles tar\n" +
						"where\n" +
						"(1=" +(srcRoleIdsAsOwner.size()>0?"1 ":"0 ") +
						//the user is an owner of a source env
						(srcRoleIdsAsTester.size()>0?"or exists (select 1 from environment_roles src where src.role_id in ("+String.join(",",srcRoleIdsAsTester)+") " +
								// check the list of the user's TDM environment roles of the source environments
								"and src.allow_read = true)\n" :"") + ")\n"+
						"and tar.role_id in ("+String.join(",",tarRoleIdsAsTester)+") " +
						// check the list of the user's TDM environment roles of the target environments
						"and lower(t.task_type) = 'load'\n" +
						"and tar.allow_write = true\n" +
						"and t.version_ind = false\n" +
						"and lower(task_status) = 'active' and lower(task_execution_status) = 'active'\n" +
						"and t.selection_method not in ('S', 'R')\n" +
						"and t.refresh_reference_data is not true\n" +
						"and t.delete_before_load is not true\n" +
						"and t.replace_sequences is not true\n" +
						"and lower(COALESCE(t.sync_mode, 'on')) <> 'force' and t.selection_method <> 'ALL'\n" +
						"UNION\n" +
						// Get all load tasks with special permissions
						"SELECT distinct t.task_id, t.task_title\n" +
						"FROM public.tasks t, environment_roles tar\n" +
						"where\n" +
						"(1="+(srcRoleIdsAsOwner.size()>0?"1 ":"0 ") +
						// the user is an owner of a source env
						(srcRoleIdsAsTester.size()>0?
								"or exists (select 1 from environment_roles src where src.role_id in" +
										" ("+String.join(",",srcRoleIdsAsTester)+") " +
										// check the list of the user's TDM environment roles of the source environments
										"and src.allow_read = true)" :"") + ")\n" +
						"and tar.role_id in ("+String.join(",",tarRoleIdsAsTester)+") " +
						//-- check the list of the user's TDM environment roles of the target environments
						"and lower(t.task_type) = 'load'\n" +
						"and tar.allow_write = true\n" +
						"and t.version_ind = false\n" +
						"and lower(task_status) = 'active' and lower(task_execution_status) = 'active'\n" +
						"and (t.selection_method not in ('S', 'R') or (t.selection_method = 'S' and tar.allowed_creation_of_synthetic_data = true) or (t.selection_method = 'R' and tar.allowed_random_entity_selection = true))\n" +
						"and (t.refresh_reference_data is not true or tar.allowed_refresh_reference_data = true )\n" +
						"and (t.delete_before_load is not true or tar.allowed_delete_before_load = true)\n" +
						"and (t.replace_sequences is not true or tar.allowed_replace_sequences = true)\n" +
						"and (lower(COALESCE(t.sync_mode, 'on')) <> 'force' and t.selection_method <> 'ALL' " +
						(srcRoleIdsAsTester.size()>0?"or exists (select 1 from environment_roles src where src.role_id in ("+String.join(",",srcRoleIdsAsTester)+") " +
								// check the list of the user's TDM environment roles of the source environments
								"and src.allow_read = true and src.allowed_request_of_Fresh_data = true)":"")+")\n";
				sql+=") loadTaskList\n" +
						"group by task_id, task_title";

				rows = db("TDM").fetch(sql);

				columnNames = rows.getColumnNames();
				for (Db.Row row : rows) {
					ResultSet resultSet = row.resultSet();
					Map<String, Object> rowMap = new HashMap<>();
					for (String columnName : columnNames) {
						rowMap.put(columnName, resultSet.getObject(columnName));
					}
					result.add(rowMap);
				}

			}


			List<Map<String, Object>> returnedResult = new ArrayList<>();
			for(Map<String, Object> row:result){
				Map<String, Object> Data=new HashMap<>();
				Data.put("task_title",row.get("task_title"));
				Data.put("task_id",row.get("task_id"));
				returnedResult.add(Data);
			}

			return SharedLogic.wrapWebServiceResults("SUCCESS",null,returnedResult);
		}catch(Exception e){
			return SharedLogic.wrapWebServiceResults("FAIL",e.getMessage(),null);
		}
	}



}
