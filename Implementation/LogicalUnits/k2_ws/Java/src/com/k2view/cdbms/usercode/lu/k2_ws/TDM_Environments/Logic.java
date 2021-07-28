/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM_Environments;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.FabricEncryption.FabricEncryption;
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
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import org.apache.jasper.tagplugins.jstl.core.Catch;
//import sun.misc.Cache;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getFabricResponse;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.wrapWebServiceResults;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends WebServiceUserCode {

	static final String schema = "public";
	static final String admin = "admin";
	final static String admin_pg_access_denied_msg = "Access Denied. Please login with administrator privileges and try again";
	static final String adi_only = "false";

	static String ldapUrlString = "ldap://62.90.46.136:10389";
	static String ldapAdminDN = "uid=tdmldap,ou=users,ou=system";
	static String ldapAdminPassword = "Q1w2e3r4t5";
	static String ldapOwnersDN = "ou=k2venvownerg,ou=system";
	static String ldapTestersDN = "ou=k2vtestg,ou=system";
	static String ldapOwnersGroupName = "k2venvownerg";
	static String ldapTestersGroupName = "k2vtestg";
	static String ldapBaseCN = "DC=training,DC=k2view,DC=com";

	@desc("Gets Environments")
	@webService(path = "environments", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [ \r\n" +
			"    {\r\n" +
			"      \"allow_write\": \"true\",\r\n" +
			"      \"environment_point_of_contact_phone1\": \"phone1\",\r\n" +
			"      \"environment_id\": 11,\r\n" +
			"      \"environment_created_by\": \"K2View\",\r\n" +
			"      \"user_name\": \"owner1\",\r\n" +
			"      \"environment_last_updated_date\": \"2021-03-25 12:04:01.633\",\r\n" +
			"      \"environment_last_updated_by\": \"K2View\",\r\n" +
			"      \"environment_status\": \"Active\",\r\n" +
			"      \"allow_read\": \"true\",\r\n" +
			"      \"owners\": [\r\n" +
			"        \"owner1\",\r\n" +
			"        \"owner2\"\r\n" +
			"      ],\r\n" +
			"      \"environment_type\": \"Both\",\r\n" +
			"      \"sync_mode\": \"on\",\r\n" +
			"      \"environment_description\": \"envDesc\",\r\n" +
			"      \"environment_point_of_contact_first_name\": \"firstName\",\r\n" +
			"      \"environment_point_of_contact_last_name\": \"lastName\",\r\n" +
			"      \"environment_point_of_contact_email\": \"email\",\r\n" +
			"      \"environment_creation_date\": \"2021-03-25 12:04:01.633\",\r\n" +
			"      \"fabric_environment_name\": \"fabricEnvName\",\r\n" +
			"      \"environment_point_of_contact_phone2\": \"phone2\",\r\n" +
			"      \"environment_name\": \"envName\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironments() throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;
		
		try {
			String sql = "SELECT \"" + schema + "\".environments.environment_id, " +
					"\"" + schema + "\".environments.environment_name, \"" + schema + "\".environments.environment_description, " +
					"\"" + schema + "\".environments.environment_point_of_contact_first_name, \"" + schema + "\".environments.environment_point_of_contact_last_name, " +
					"\"" + schema + "\".environments.environment_point_of_contact_phone1, \"" + schema + "\".environments.environment_point_of_contact_phone2, " +
					"\"" + schema + "\".environments.environment_point_of_contact_email, \"" + schema + "\".environments.environment_created_by, " +
					"\"" + schema + "\".environments.environment_creation_date, \"" + schema + "\".environments.environment_last_updated_date, " +
					"\"" + schema + "\".environments.environment_last_updated_by, \"" + schema + "\".environments.environment_status, " +
					"\"" + schema + "\".environments.allow_write, \"" + schema + "\".environments.fabric_environment_name, " +
					"\"" + schema + "\".environments.allow_read, " +
					"\"" + schema + "\".environments.sync_mode, " +
					"\"" + schema + "\".environment_owners.user_name " +
					" FROM \"" + schema + "\".environments " +
					"LEFT JOIN \"" + schema + "\".environment_owners ON ( " +
					"\"" + schema + "\".environments.environment_id = \"" + schema + "\".environment_owners.environment_id )";
			Db.Rows rows = db("TDM").fetch(sql);

			List<Map<String, Object>> result = new ArrayList<>();
			Map<String, Object> environment;
			for (Db.Row row : rows) {
				environment = new HashMap<>();
				environment.put("environment_id", Integer.parseInt(row.cell(0).toString()));
				environment.put("environment_name",  row.cell(1));
				environment.put("environment_description",  row.cell(2));
				environment.put("environment_point_of_contact_first_name", row.cell(3));
				environment.put("environment_point_of_contact_last_name", row.cell(4));
				environment.put("environment_point_of_contact_phone1", row.cell(5));
				environment.put("environment_point_of_contact_phone2", row.cell(6));
				environment.put("environment_point_of_contact_email", row.cell(7));
				environment.put("environment_created_by", row.cell(8));
				environment.put("environment_creation_date", row.cell(9));
				environment.put("environment_last_updated_date", row.cell(10));
				environment.put("environment_last_updated_by", row.cell(11));
				environment.put("environment_status", row.cell(12));
				environment.put("allow_write", row.cell(13)!=null?Boolean.parseBoolean(row.cell(13).toString()):null);
				environment.put("fabric_environment_name", row.cell(14));
				environment.put("allow_read", row.cell(15)!=null?Boolean.parseBoolean(row.cell(15).toString()):null);
				environment.put("sync_mode", row.cell(16));
				environment.put("user_name", row.cell(17));
				result.add(environment);
			}
		
		
			for (int i = 0; i < result.size(); i++) {
				environment = result.get(i);
				Map<String, Object> otherEnvironment;
				List<String> owners = new ArrayList<>();
				if(environment.get("user_name")!=null) owners.add(environment.get("user_name").toString());
				for (int j = i + 1; j < result.size(); j++) {
					otherEnvironment = result.get(j);
					if (otherEnvironment.get("environment_id").toString().equals(environment.get("environment_id").toString())) {
						if(otherEnvironment.get("user_name")!=null) owners.add(otherEnvironment.get("user_name").toString());
						result.remove(j); j--;
					}
				}
				environment.put("owners", owners);
		
				String envType = "None";
				if(environment.get("allow_write")!=null&&environment.get("allow_read")!=null) {
					if (!Boolean.parseBoolean(environment.get("allow_write").toString()) && Boolean.parseBoolean(environment.get("allow_read").toString())) {
						envType = "Source";
					} else if (Boolean.parseBoolean(environment.get("allow_write").toString()) && !Boolean.parseBoolean(environment.get("allow_read").toString())) {
						envType = "Target";
					} else if (Boolean.parseBoolean(environment.get("allow_write").toString()) && Boolean.parseBoolean(environment.get("allow_read").toString())) {
						envType = "Both";
					}
				}
				environment.put("environment_type", envType);
			}

			errorCode = "SUCCESS";
			response.put("result", result);
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Modifies Environment's general properties.\r\n" +
			"The following parameters are mandatory:\r\n" +
			"emvId, environment_name, fabric_environment_name, allow_write, and allow_read.\r\n" +
			"\r\n" +
			"Notes:\r\n" +
			"\r\n" +
			"> Both parameters - environment_name amd fabric environment_name - are populated by one of the Fabric's environments.\r\n" +
			"\r\n" +
			">  The sync_mode parameter can be populated by one of the following values: \"OFF\", \"FORCE\" or null.\r\n" +
			"\r\n" +
			">  The allow_write parameter is set to true if the environment can be used as a target environment. Else, set this parameter to false.\r\n" +
			"\r\n" +
			"> The allow_read parameter is set to true if the environment can be used as a source environment. Else, set this parameter to false.\r\n" +
			"\r\n" +
			"\r\n" +
			"Example of the owners:\r\n" +
			"\"owners\":[{\"uid\":\"king123\",\"user_id\":\"king123\",\"displayName\":\"king123\",\"username\":\"king123\"},{\"uid\":\"owner1\",\"user_id\":\"owner1\",\"displayName\":\"owner1\",\"username\":\"owner1\"}]")
	@webService(path = "environment/{envId}", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsModifyEnvironment(@param(description="Environment name. The environment name must also be defined in Fabric's environments.", required=true) Long envId, @param(description="Optional parameter", required=true) String environment_name, @param(description="Optional parameter") String environment_description, @param(description="Optional parameter") String environment_point_of_contact_first_name, @param(description="Optional parameter") String environment_point_of_contact_last_name, @param(description="Optional parameter") String environment_point_of_contact_phone1, @param(description="Optional parameter") String environment_point_of_contact_phone2, @param(description="Optional parameter") String environment_point_of_contact_email, @param(description="Identical to the environment name", required=true) String fabric_environment_name, @param(description="Will be populated by true if the environment can be used as a target environment. Else- will be false.", required=true) Boolean allow_write, @param(description="Will be populated by true when the environment can be used as a source environment. Else- will be false.", required=true) Boolean allow_read, @param(description="Can be populated by eaither of the following values: \"OFF\" or \"FORCE\"") String sync_mode, @param(description="List of owners attached to the environment") List<Map<String,String>> owners) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		try {
			String sql = "UPDATE \"" + schema + "\".environments SET " +
					"environment_name=(?)," +
					"environment_description=(?)," +
					"environment_point_of_contact_first_name=(?)," +
					"environment_point_of_contact_last_name=(?)," +
					"environment_point_of_contact_phone1=(?)," +
					"environment_point_of_contact_phone2=(?)," +
					"environment_point_of_contact_email=(?)," +
					"environment_last_updated_date=(?)," +
					"environment_last_updated_by=(?), " +
					"fabric_environment_name=(?) ," +
					"allow_write=(?) ," +
					"allow_read=(?) ," +
					"sync_mode=(?) " +
					"WHERE environment_id = " + envId;
			db("TDM").execute(sql,
					environment_name, environment_description, environment_point_of_contact_first_name,
					environment_point_of_contact_last_name, environment_point_of_contact_phone1, environment_point_of_contact_phone2,
					environment_point_of_contact_email, now,
					(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value"), fabric_environment_name,
					allow_write, (allow_read != null ? allow_read : false), (sync_mode != null ? sync_mode : "ON"));
		
			String activityDesc = "Environment " + environment_name + " was updated";
			try {
				fnInsertActivity("update", "Environment", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			try {
				if (allow_read != true) {
					fnUpdateEnvironmentRolesPermissions(envId, "allow_read", false);
				}
				if (allow_write != true) {
					fnUpdateEnvironmentRolesPermissions(envId, "allow_write", false);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}


			if ("admin".equals(permissionGroup)) {
				String updateEnvironmentOwners = "DELETE FROM \"" + schema + "\".environment_owners WHERE environment_id = (?)";
				db("TDM").execute(updateEnvironmentOwners, envId);

				String userNameSubQuery = "";
				String ownersTemp = "";

				for (Map<String, String> owner : owners) {
					if (ownersTemp != "")
						ownersTemp += ",'" + (owner.get("username") != null ? owner.get("username") : owner.get("user_name")) + "'";
					else
						ownersTemp += "'" + (owner.get("username") != null ? owner.get("username") : owner.get("user_name")) + "'";
				}

				if (ownersTemp.length() > 0) {
					userNameSubQuery = " AND username IN (" + ownersTemp + ")";
					db("TDM").execute("DELETE FROM \"" + schema + "\".environment_role_users WHERE environment_id = (?)" + userNameSubQuery, envId);
				}

				for (Map<String, String> owner : owners) {
					db("TDM").execute("INSERT INTO \"" + schema + "\".environment_owners (environment_id, user_id, user_name) VALUES (?, ?, ?)",
							envId, owner.get("user_id"), owner.get("username") != null ? owner.get("username") : owner.get("user_name"));
				}
			} else if (owners!=null&&!owners.isEmpty()){
				message="The sent owners list was ignored, you need administrative permissions to add or remove owners.";
			}

			errorCode = "SUCCESS";
		
		
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Deletes Environment")
	@webService(path = "environment/{envId}/envname/{envName}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDeleteEnvironment(@param(required=true) Long envId, @param(required=true) String envName) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if (!"admin".equals(permissionGroup)) return wrapWebServiceResults("FAIL",admin_pg_access_denied_msg,null);
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "UPDATE \"" + schema + "\".environments SET " +
					"environment_status=(?) " + "WHERE environment_id = " + envId;
			db("TDM").execute(sql, "Inactive");
		
			{
				String updateTasks = "UPDATE \"" + schema + "\".tasks " +
						"SET task_status = \'Inactive\'" +
						"WHERE environment_id = " + envId;
				db("TDM").execute(updateTasks);
			}
			{
				String updateEnvironmentProducts = "UPDATE \"" + schema + "\".environment_products " +
						"SET status = \'Inactive\'" +
						"WHERE environment_id = \'" + envId + "\'";
				db("TDM").execute(updateEnvironmentProducts);
			}
			{
				String updateEnvironmentRoles = "UPDATE \"" + schema + "\".environment_roles " +
						"SET role_status = \'Inactive\'" +
						"WHERE environment_id = \'" + envId + "\'";
				db("TDM").execute(updateEnvironmentRoles);
			}
		
			String activityDesc = "'Environment " + envName + " was deleted";
			try {
				fnInsertActivity("delete", "Environment", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		
			errorCode = "SUCCESS";
		
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the environment's Products  regardless of their status (Active and Inactive products)")
	@webService(path = "environment/{envId}/products", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"environment_product_id\": 3,\r\n" +
			"      \"product_version\": \"1\",\r\n" +
			"      \"environment_id\": 4,\r\n" +
			"      \"product_vendor\": \"vendor\",\r\n" +
			"      \"product_last_updated_date\": \"2021-03-18 17:23:16.622\",\r\n" +
			"      \"product_created_by\": \"k2view\",\r\n" +
			"      \"product_status\": \"Active\",\r\n" +
			"      \"creation_date\": \"2020-12-07 08:14:48.336\",\r\n" +
			"      \"created_by\": \"k2view\",\r\n" +
			"      \"product_name\": \"PROD\",\r\n" +
			"      \"product_versions\": \"1\",\r\n" +
			"      \"product_last_updated_by\": \"K2View\",\r\n" +
			"      \"last_updated_by\": \"k2view\",\r\n" +
			"      \"data_center_name\": \"DC1\",\r\n" +
			"      \"product_id\": 1,\r\n" +
			"      \"product_id1\": 1,\r\n" +
			"      \"product_creation_date\": \"2020-10-01 08:26:42.899\",\r\n" +
			"      \"last_updated_date\": \"2020-12-07 08:14:48.336\",\r\n" +
			"      \"product_description\": \"description\",\r\n" +
			"      \"status\": \"Active\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetProductsforEnvironment(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		try {
			//getEnv
			String getEnvironmentSql = "SELECT * FROM \"" + schema + "\".environments WHERE environment_id = " + envId;
			Db.Row env = db("TDM").fetch(getEnvironmentSql).firstRow();
		
			if(env.isEmpty()){ //envId doesn't exist
				response.put("errorCode", "SUCCESS");
				response.put("resutl", null);
				return response;
			}
		
			//getEnvProduct
			String sql = "SELECT * , " +
					"( SELECT COUNT(*) FROM tasks " +
					"INNER JOIN \"" + schema + "\".tasks_logical_units " +
					"ON (\"" + schema + "\".tasks_logical_units.task_id = \"" + schema + "\".tasks.task_id) " +
					"INNER JOIN \"" + schema + "\".product_logical_units " +
					"ON (\"" + schema + "\".product_logical_units.lu_id = \"" + schema + "\".tasks_logical_units.lu_id) " +
					"WHERE product_logical_units.product_id = products.product_id AND tasks.task_status = \'Active\' ) AS taskcount " +
					"FROM \"" + schema + "\".environment_products " +
					"INNER JOIN \"" + schema + "\".products " +
					"ON (\"" + schema + "\".environment_products.product_id = \"" + schema + "\".products.product_id) " +
					"WHERE \"" + schema + "\".environment_products.environment_id = " + envId;
			Db.Rows rows = db("TDM").fetch(sql);
		
			//updateEnvProductInterfaces
			List<HashMap<String, Object>> products = new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				HashMap<String, Object> productData = new HashMap<>();
				ResultSet resultset = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					productData.put(columnName, resultset.getObject(columnName));
				}
				String interfaceName;
				try{
					interfaceName=resultset.getString("interface_name");
				} catch (Exception e){
					interfaceName=null;
				}
		
				if (interfaceName != null) {
					HashMap<String, Object> envProductInterface = new HashMap<>();
					envProductInterface.put("interface_name", resultset.getString("interface_name"));
					envProductInterface.put("interface_type", resultset.getString("interface_type"));
					envProductInterface.put("db_host", resultset.getString("db_host"));
					try {
						envProductInterface.put("db_port", Integer.parseInt(resultset.getString("db_port")));
					} catch (Exception e){
						envProductInterface.put("db_port", null);
					}
					envProductInterface.put("db_user", resultset.getString("db_user"));
					envProductInterface.put("db_password", resultset.getString("db_password"));
					envProductInterface.put("db_schema", resultset.getString("db_schema"));
					envProductInterface.put("db_connection_string", resultset.getString("db_connection_string"));
					envProductInterface.put("env_product_interface_id", resultset.getString("env_product_interface_id"));
					productData.put("interface", envProductInterface);
				}
				products.add(productData);
			}
		
		
		
			for (int i = 0; i < products.size(); i++) {
				Map<String, Object> productData = products.get(i);
				List<Map<String, Object>> interfaces = new ArrayList<>();
				interfaces.add((HashMap<String, Object>)productData.get("interface"));
				for (int j = i + 1; j < products.size(); j++) {
					Map<String, Object> productData2 = products.get(j);
					if (productData.get("environment_product_id").toString().equals(productData.get("environment_product_id").toString())) {
						if (productData2.get("interface_name") != null) {
							interfaces.add((HashMap<String, Object>) productData2.get("interface"));
						}
						products.remove(j); j--;
					}
				}
				productData.put("interfaces", interfaces);
			}
		
			//end updateEnvProductInterfaces
		
			sql = "SELECT * FROM environment_products " +
					"INNER JOIN \"" + schema + "\".products " +
					"ON (\"" + schema + "\".environment_products.product_id = \"" + schema + "\".products.product_id) " +
					"WHERE environment_id = " + envId;
		
			Db.Rows otherProducts = db("TDM").fetch(sql);
		
			List<HashMap<String, Object>> otherProductsList = new ArrayList<>();
			otherProducts: for (Db.Row row : otherProducts) {
				for (Map<String, Object> productData : products) {
					if (productData.get("environment_product_id").toString().equals(row.cell(0).toString()))
						continue otherProducts;
				}
				HashMap<String, Object> productData = new HashMap<>();
				productData.put("environment_product_id", row.cell(0));
				productData.put("environment_id", row.cell(1));
				productData.put("product_id", row.cell(2));
				productData.put("product_version", row.cell(3));
				productData.put("created_by", row.cell(4));
				productData.put("creation_date", row.cell(5));
				productData.put("last_updated_date", row.cell(6));
				productData.put("last_updated_by", row.cell(7));
				productData.put("status", row.cell(8));
				productData.put("data_center_name", row.cell(9));
				productData.put("product_name", row.cell(10));
				productData.put("product_description", row.cell(11));
				productData.put("product_vendor", row.cell(12));
				productData.put("product_versions", row.cell(13));
				productData.put("product_id1", row.cell(14));
				productData.put("product_created_by", row.cell(15));
				productData.put("product_creation_date", row.cell(16));
				productData.put("product_last_updated_date", row.cell(17));
				productData.put("product_last_updated_by", row.cell(18));
				productData.put("product_status", row.cell(19));
				otherProductsList.add(productData);
			}
			products.addAll(otherProductsList);
			response.put("result", products);
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Creates an Environment Exclusion List.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{exclusion_list: \"1,4,65,89\", be_id: 9, requested_by: \"k2vtester02\"}")
	@webService(path = "environment/{envId}/exclusionLists", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 9\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateEnvironmentExclusionList(@param(required=true) Long envId, Long be_id, @param(description="Populated by the list of entities separtated by a comma") String exclusion_list, String requested_by) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}
		
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			HashMap<String, Object> result = new HashMap<>();
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			String sql = "INSERT INTO \"" + schema + "\".tdm_be_env_exclusion_list " +
					"(be_id, environment_id, exclusion_list, requested_by, update_date, created_by, updated_by, creation_date) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING be_env_exclusion_list_id";
			Db.Row row = db("TDM").fetch(sql,be_id, envId, exclusion_list, requested_by, now, username,
					username, now).firstRow();
			result.put("id", Long.parseLong(row.cell(0).toString()));
			errorCode = "SUCCESS";
			response.put("result", result);
			String activityDesc = "Exclusion list of environment " + envId + " was added";
			try {
				fnInsertActivity("update", "Environment", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets All Environment Exclusion Lists")
	@webService(path = "environment/{envId}/exclusionLists", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "Response body\r\n" +
			"Download\r\n" +
			"{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_env_exclusion_list_id\": 10,\r\n" +
			"      \"be_id\": 15,\r\n" +
			"      \"requested_by\": \"test2903\",\r\n" +
			"      \"environment_id\": 11,\r\n" +
			"      \"exclusion_list\": \"1,9,35,91\",\r\n" +
			"      \"updated_by\": \"K2View\",\r\n" +
			"      \"creation_date\": \"2021-03-29 07:55:06.839\",\r\n" +
			"      \"created_by\": \"K2View\",\r\n" +
			"      \"be_name\": \"beName\",\r\n" +
			"      \"update_date\": \"2021-03-29 07:55:06.839\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetAllEnvironmentExclusionLists(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			List<Map<String, Object>> result = new ArrayList<>();
			String sql = "SELECT el.be_id, el.environment_id, el.exclusion_list, el.requested_by, el.update_date," +
					" el.created_by, el.updated_by, el.be_env_exclusion_list_id, el.creation_date, be.be_name " +
					"FROM public.tdm_be_env_exclusion_list el " +
					"INNER JOIN business_entities be ON el.be_id = be.be_id WHERE el.environment_id = " + envId;
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
			/*
			HashMap<String, Object> El;
			for (Db.Row row : rows) {
				El = new HashMap<>();
				El.put("be_id", Integer.parseInt(row.cell(0).toString()));
				El.put("environment_id", Integer.parseInt(row.cell(1).toString()));
				El.put("exclusion_list", row.cell(2));
				El.put("requested_by", row.cell(3));
				El.put("update_date", row.cell(4));
				El.put("created_by", row.cell(5));
				El.put("updated_by", row.cell(6));
				El.put("be_env_exclusion_list_id", Integer.parseInt(row.cell(7).toString()));
				El.put("creation_date", row.cell(8));
				El.put("be_name", row.cell(9));
				result.add(El);
			}
			 */
			errorCode = "SUCCESS";
			response.put("result", result);
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Adds a Global to the Environment")
	@webService(path = "environment/{envId}/envname/{envName}/global", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateEnvironmentGlobal(@param(required=true) Long envId, @param(required=true) String envName, String global_name, String global_value) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		try {
			String sql = "INSERT INTO \"" + schema + "\".tdm_env_globals (environment_id, global_name, global_value, update_date, updated_by) VALUES (?, ?, ?, ?, ?)";
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			db("TDM").execute(sql, envId, global_name, global_value, now, username);
		
			String activityDesc = "'Global " + global_name + " was added to environment " + envName;
			try {
				fnInsertActivity("update", "Environment", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		
			errorCode = "SUCCESS";
		} catch (Exception e) {
			message = e.getMessage();
			log.error(message);
			errorCode = "FAIL";
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Creates an Environment Role")
	@webService(path = "environment/{envId}/envname/{envName}/role", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 10\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateEnvironmentRole(@param(required=true) Long envId, @param(required=true) String envName, String role_name, String role_description, Boolean allowed_delete_before_load, Boolean allowed_creation_of_synthetic_data, Boolean allowed_random_entity_selection, Boolean allowed_request_of_fresh_data, Boolean allowed_task_scheduling, Integer allowed_number_of_entities_to_copy, Boolean allowed_refresh_reference_data, Boolean allowed_replace_sequences, Integer allowed_number_of_entities_to_read, Boolean allow_read, Boolean allow_write, Boolean allowed_entity_versioning, Boolean allowed_test_conn_failure) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		HashMap<String, Object> result = new HashMap<>();
		String message = null;
		String errorCode = "";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		try {
			String sql = "INSERT INTO \"" + schema + "\".environment_roles (environment_id, role_name," +
					"role_description, allowed_delete_before_load, allowed_creation_of_synthetic_data, " +
					"allowed_random_entity_selection, allowed_request_of_fresh_data, allowed_task_scheduling, " +
					"allowed_number_of_entities_to_copy, allowed_refresh_reference_data, role_created_by," +
					" role_creation_date, role_last_updated_date," +
					"role_expiration_date,role_last_updated_by,role_status,allowed_replace_sequences," +
					" allowed_number_of_entities_to_read, allow_read, allow_write, allowed_entity_versioning, allowed_test_conn_failure) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING role_id";
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			Db.Row row = db("TDM").fetch(sql,
					envId,
					role_name,
					role_description,
					allowed_delete_before_load,
					allowed_creation_of_synthetic_data,
					allowed_random_entity_selection,
					allowed_request_of_fresh_data,
					allowed_task_scheduling,
					allowed_number_of_entities_to_copy,
					allowed_refresh_reference_data,
					username,
					now,
					now,
					null,
					username,
					"Active",
					allowed_replace_sequences,
					allowed_number_of_entities_to_read,
					allow_read != null ? allow_read : false,
					allow_write,
					allowed_entity_versioning,
					allowed_test_conn_failure).firstRow();
		
			Long roleId = Long.parseLong(row.cell(0).toString());
			result.put("id", roleId);
			response.put("result", result);
		
			String activityDesc = "Role " + role_name + " was added to environment " + envName;
			try {
				fnInsertActivity("update", "Environment", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		
			errorCode = "SUCCESS";
		} catch (Exception e) {
			message = e.getMessage();
			log.error(message);
			errorCode = "FAIL";
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Creates a new Environment.\r\n" +
			"The following parameters are mandatory:\r\n" +
			"environment_name, fabric_environment_name, allow_write, and allow_read.\r\n" +
			"\r\n" +
			"Notes:\r\n" +
			"\r\n" +
			"> Both parameters - environment_name and fabric environment_name - are populated by one of the Fabric's environments.\r\n" +
			"\r\n" +
			">  The sync_mode parameter can be populated by one of the following values: \"OFF\", \"FORCE\", or null.\r\n" +
			"\r\n" +
			"> The allow_write parameter is set to true if the environment can be used as a target environment. Else, set this parameter to false.\r\n" +
			"\r\n" +
			"> The allow_read parameter is set to true if the environment can be used as a source environment. Else, set this parameter to false.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\"allow_write\":true,\"environment_name\":\"ENV6\",\"environment_description\":\"test\",\"owners\":[{\"uid\":\"king123\",\"user_id\":\"king123\",\"displayName\":\"king123\",\"username\":\"king123\"}],\"fabric_environment_name\":\"ENV6\"}")
	@webService(path = "environment", verb = {MethodType.POST}, version = "", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 10\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateEnvironment(@param(description="Environment name. The environment name must also be defined in Fabric's environments.", required=true) String environment_name,
											 @param(description="Optional parameter") String environment_description,
											 @param(description="Optional parameter") String environment_point_of_contact_fist_name,
											 @param(description="Optional parameter") String environment_point_of_contact_last_name,
											 @param(description="Optional parameter") String environment_point_of_contact_phone1,
											 @param(description="Optional parameter") String environment_point_of_contact_phone2,
											 @param(description="Optional parameter") String environment_point_of_contact_email,
											 @param(description="Identical to the environment name", required=true) String fabric_environment_name,
											 @param(description="Will be populated by true if the environment can be used as a target environment", required=true) Boolean allow_write,
											 @param(description="Will be populated by true when the environment can be used as a source environment", required=true) Boolean allow_read,
											 @param(description="Can be populated by one of the following values: \"OFF\" or \"FORCE\"") String sync_mode,
											 @param(description="List of owners attached to the environment") List<Map<String,String>> owners) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if (!"admin".equals(permissionGroup)) return wrapWebServiceResults("FAIL",admin_pg_access_denied_msg,null);
		HashMap<String, Object> response = new HashMap<>();
		HashMap<String, Object> result = new HashMap<>();
		String message = null;
		String errorCode = "";
			try {
				String sql = "INSERT INTO \"" + schema + "\".environments (environment_name, environment_description, " +
						"environment_point_of_contact_first_name, environment_point_of_contact_last_name, " +
						"environment_point_of_contact_phone1, environment_point_of_contact_phone2, environment_point_of_contact_email, environment_created_by," +
						"environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, fabric_environment_name, allow_write, allow_read, sync_mode) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) RETURNING environment_id";
				String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
						.withZone(ZoneOffset.UTC)
						.format(Instant.now());
				String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
				Db.Row row = db("TDM").fetch(sql, environment_name,
						environment_description,
						environment_point_of_contact_fist_name,
						environment_point_of_contact_last_name,
						environment_point_of_contact_phone1,
						environment_point_of_contact_phone2,
						environment_point_of_contact_email,
						username,
						now,
						now,
						username,
						"Active",
						fabric_environment_name,
						allow_write,
						allow_read != null ? allow_read : false,
						sync_mode != null ? sync_mode : "ON"
				).firstRow();
				long environmentId = Long.parseLong(row.cell(0).toString());
				result.put("id", environmentId);
				try {
					for (Map owner : owners) {
						db("TDM").execute("INSERT INTO \"" + schema + "\".environment_owners (environment_id, user_id, user_name) VALUES (?, ?, ?)",
								environmentId,
								owner.get("user_id"),
								owner.get("username") != null ? owner.get("username") : owner.get("user_name"));
					}
				} catch (Exception e) {
					log.error(e.getMessage());
					result.put("Warning", "true");
				}
				String activityDesc = "Environment " + environment_name + " was created";
				try {
					fnInsertActivity("delete", "Environment", activityDesc);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				errorCode = "SUCCESS";
				response.put("result", result);
			} catch (Exception e) {
				errorCode = "FAIL";
				message = e.getMessage();
				log.error(message);
			}

		
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the number of TDM tasks of the target Environment. If an Environment is connected to TDM tasks, a warning messahe is given to the user if the user deletes the Environment.")
	@webService(path = "environment/{envId}/taskCount", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": 30,\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentTaskCount(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "SELECT COUNT (environment_id) FROM \"" + schema + "\".tasks " +
					"WHERE \"" + schema + "\".tasks.environment_id = " + envId;
			Db.Row row = db("TDM").fetch(sql).firstRow();
			if (!row.isEmpty()) {
				response.put("result", row.cell(0));
				//response.put("result", !row.cell(0).toString().equals("0"));
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the owners list of a given environment")
	@webService(path = "environment/{envId}/owners", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"user_id\": \"3\",\r\n" +
			"      \"user_name\": \"owner1\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"user_id\": \"4\",\r\n" +
			"      \"user_name\": \"owner2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentOwners(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		List<HashMap<String, Object>> result = new ArrayList<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "SELECT user_id, user_name " +
					"FROM \"" + schema + "\".environment_owners WHERE environment_id = " + envId;
			Db.Rows rows = db("TDM").fetch(sql);
			errorCode = "SUCCESS";
			HashMap<String, Object> owner;
			for (Db.Row row : rows) {
				owner = new HashMap<>();
				owner.put("user_id", row.cell(0));
				owner.put("user_name", row.cell(1));
				result.add(owner);
			}
			response.put("result", result);
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets all TDM roles of a given environment ")
	@webService(path = "environment/{envId}/roles", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"role_last_updated_by\": \"K2View\",\r\n" +
			"      \"maximum_number_of_entities_data_ownership\": null,\r\n" +
			"      \"allowed_delete_before_load\": \"t\",\r\n" +
			"      \"environment_id\": 11,\r\n" +
			"      \"allowed_number_of_entities_to_read\": 1,\r\n" +
			"      \"allowed_replace_sequences\": \"t\",\r\n" +
			"      \"maximum_number_of_days_data_ownership\": null,\r\n" +
			"      \"allowed_random_entity_selection\": \"t\",\r\n" +
			"      \"role_created_by\": \"K2View\",\r\n" +
			"      \"allow_read\": \"t\",\r\n" +
			"      \"role_description\": \"roleDescription\",\r\n" +
			"      \"allowed_task_scheduling\": \"t\",\r\n" +
			"      \"allowed_request_of_fresh_data\": \"t\",\r\n" +
			"      \"role_id\": 10,\r\n" +
			"      \"allowed_entity_versioning\": \"t\",\r\n" +
			"      \"role_last_updated_date\": \"2021-03-28 16:16:37.678\",\r\n" +
			"      \"allowed_number_of_entities_to_copy\": 1,\r\n" +
			"      \"role_expiration_date\": \"2021-03-25 12:04:01.633\",\r\n" +
			"      \"allow_write\": \"t\",\r\n" +
			"      \"allowed_creation_of_synthetic_data\": \"t\",\r\n" +
			"      \"allowed_refresh_reference_data\": \"t\",\r\n" +
			"      \"role_creation_date\": \"2021-03-28 16:16:37.678\",\r\n" +
			"      \"role_name\": \"roleName\",\r\n" +
			"      \"role_status\": \"Active\",\r\n" +
			"      \"allowed_test_conn_failure\": \"t\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentRoles(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		List<HashMap<String, Object>> result = new ArrayList<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "SELECT * FROM \"" + schema + "\".environment_roles " +
					"WHERE environment_id = " + envId;
			Db.Rows rows = db("TDM").fetch(sql);
			HashMap<String, Object> role;
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				role = new HashMap<>();
				role.put("environment_id", resultSet.getLong("environment_id"));
				role.put("role_name", resultSet.getString("role_name"));
				role.put("role_description", resultSet.getString("role_description"));
				role.put("allowed_delete_before_load", resultSet.getBoolean("allowed_delete_before_load"));
				role.put("allowed_creation_of_synthetic_data", resultSet.getBoolean("allowed_creation_of_synthetic_data"));
				role.put("allowed_random_entity_selection", resultSet.getBoolean("allowed_random_entity_selection"));
				role.put("allowed_request_of_fresh_data", resultSet.getBoolean("allowed_request_of_fresh_data"));
				role.put("allowed_task_scheduling", resultSet.getBoolean("allowed_task_scheduling"));
				role.put("allowed_number_of_entities_to_copy", resultSet.getInt("allowed_number_of_entities_to_copy"));
				role.put("role_id", resultSet.getLong("role_id"));
				role.put("role_created_by", resultSet.getString("role_created_by"));
				role.put("role_creation_date", resultSet.getString("role_creation_date"));
				role.put("role_last_updated_date", resultSet.getString("role_last_updated_date"));
				role.put("role_expiration_date", resultSet.getString("role_expiration_date"));
				role.put("role_last_updated_by", resultSet.getString("role_last_updated_by"));
				role.put("role_status", resultSet.getString("role_status"));
				role.put("allowed_refresh_reference_data", resultSet.getBoolean("allowed_refresh_reference_data"));
				role.put("allowed_replace_sequences", resultSet.getBoolean("allowed_replace_sequences"));
				role.put("allow_read", resultSet.getBoolean("allow_read"));
				role.put("allow_write", resultSet.getBoolean("allow_write"));
				role.put("allowed_number_of_entities_to_read", resultSet.getInt("allowed_number_of_entities_to_read"));
				role.put("allowed_entity_versioning", resultSet.getBoolean("allowed_entity_versioning"));
				role.put("allowed_test_conn_failure", resultSet.getBoolean("allowed_test_conn_failure"));
				result.add(role);
			}
			errorCode = "SUCCESS";
			response.put("result", result);
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets Environment Globals")
	@webService(path = "environment/{envId}/globals", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"global_name\": \"globalName1\",\r\n" +
			"      \"environment_id\": 10,\r\n" +
			"      \"updated_by\": \"K2View\",\r\n" +
			"      \"global_value\": \"globalValue1\",\r\n" +
			"      \"update_date\": \"2021-03-25 11:30:48.609\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"global_name\": \"globalName2\",\r\n" +
			"      \"environment_id\": 10,\r\n" +
			"      \"updated_by\": \"K2View\",\r\n" +
			"      \"global_value\": \"globalValue2\",\r\n" +
			"      \"update_date\": \"2021-03-25 11:40:49.668\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentGlobals(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		List<HashMap<String, Object>> result = new ArrayList<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "SELECT * FROM \"" + schema + "\".tdm_env_globals " +
					"WHERE environment_id = " + envId;
			Db.Rows rows = db("TDM").fetch(sql);
			HashMap<String, Object> global;
		
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				global = new HashMap<>();
				global.put("environment_id", resultSet.getLong("environment_id"));
				global.put("global_name", resultSet.getString("global_name"));
				global.put("global_value", resultSet.getString("global_value"));
				global.put("update_date", resultSet.getString("update_date"));
				global.put("updated_by", resultSet.getString("updated_by"));
				result.add(global);
			}
			response.put("result", result);
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Modifies the environment's role setting except  the Testers setting. Adding or removing testers to the environment's role is handled by a separate API: /environment/{envId}/envname/{envName}/role/{roleId}/rolename/{roleName}/users")
	@webService(path = "environment/{envId}/envname/{envName}/role/{roleId}", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsModifyEnvironmentRole(@param(required=true) Long envId, @param(required=true) String envName, @param(required=true) Long roleId, String role_name, String role_description, Boolean allowed_delete_before_load, Boolean allowed_creation_of_synthetic_data,
												 Boolean allowed_random_entity_selection, Boolean allowed_task_scheduling, Integer allowed_number_of_entities_to_copy, Boolean allowed_replace_sequences, Boolean allowed_refresh_reference_data, Integer allowed_number_of_entities_to_read, Boolean allow_read, Boolean allow_write, Boolean allowed_entity_versioning,
												 Boolean allowed_test_conn_failure,Boolean allowed_request_of_fresh_data) throws Exception {
				String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
				if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
				if (!"admin".equals(permissionGroup)) {
					if ("tester".equals(permissionGroup)) {
						return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
					} else if("owner".equals(permissionGroup)){
						if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
					}
				}
		
		fnUpdateEnvironmentDate(envId);
		HashMap<String, Object> response = new HashMap<>();
		List<HashMap<String, Object>> result = new ArrayList<>();
		String message = null;
		String errorCode = "";
		
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		
		try {
			String sql = "UPDATE \"" + schema + "\".environment_roles SET " +
					"role_name=(?)," +
					"role_description=(?)," +
					"allowed_delete_before_load=(?)," +
					"allowed_creation_of_synthetic_data=(?)," +
					"allowed_random_entity_selection=(?)," +
					"allowed_task_scheduling=(?)," +
					"allowed_number_of_entities_to_copy=(?)," +
					"allowed_replace_sequences=(?)," +
					"allowed_refresh_reference_data=(?)," +
					"role_last_updated_date=(?)," +
					"role_last_updated_by=(?)," +
					"allowed_number_of_entities_to_read=(?)," +
					"allow_read=(?)," +
					"allow_write=(?)," +
					"allowed_entity_versioning=(?)," +
					"allowed_test_conn_failure=(?)," +
					"allowed_request_of_fresh_data=(?) " +
					"WHERE environment_id = " + envId + " AND role_id = \'" + roleId + "\'";
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			db("TDM").execute(sql,
					role_name,
					role_description,
					allowed_delete_before_load,
					allowed_creation_of_synthetic_data,
					allowed_random_entity_selection,
					allowed_task_scheduling,
					allowed_number_of_entities_to_copy,
					allowed_replace_sequences,
					allowed_refresh_reference_data,
					now,
					username,
					allowed_number_of_entities_to_read,
					allow_read != null ? allow_read : false,
					allow_write,
					allowed_entity_versioning,
					allowed_test_conn_failure,
					allowed_request_of_fresh_data);
			String activityDesc = "Role " + role_name + " of environment " + envName + " was updated";
			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Deletes Environment Role")
	@webService(path = "environment/{envId}/envname/{envName}/role/{roleId}/rolename/{roleName}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDeleteEnvironmentRole(@param(required=true) Long envId, @param(required=true) String envName, @param(required=true) Long roleId, @param(required=true) String roleName) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		fnUpdateEnvironmentDate(envId);
		HashMap<String, Object> response = new HashMap<>();
		List<HashMap<String, Object>> result = new ArrayList<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "UPDATE \"" + schema + "\".environment_roles SET " +
					"role_status=(?),role_expiration_date=(?) " +
					"WHERE environment_id = " + envId + " AND role_id = \'" + roleId + "\'";
			db("TDM").execute(sql,
					"Inactive",DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
							.withZone(ZoneOffset.UTC)
							.format(Instant.now()));

			// cleanup environment_role_users table
			db("TDM").execute("DELETE from \"" + schema + "\".environment_role_users WHERE environment_id=" + envId + " AND role_id=" + roleId);

			String activityDesc = "Role " + roleName + " of environment " + envName + " was deleted";
			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets Environment Testers attached to a given TDM Environment role")
	@webService(path = "environment/{envId}/role/{roleId}/users", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "[\r\n" +
			"  {\r\n" +
			"    \"environment_id\": 11,\r\n" +
			"    \"role_id\": 10,\r\n" +
			"    \"username\": \"username1\",\r\n" +
			"    \"user_id\": \"1\"\r\n" +
			"  },\r\n" +
			"  {\r\n" +
			"    \"environment_id\": 11,\r\n" +
			"    \"role_id\": 10,\r\n" +
			"    \"username\": \"username2\",\r\n" +
			"    \"user_id\": \"2\"\r\n" +
			"  }\r\n" +
			"]")
	public static Object wsGetEnvironmentTestersByRole(@param(required=true) Long envId, @param(required=true) Long roleId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();
		String message = null;
		String errorCode = "";
		
		try {
			String sql = "SELECT * FROM \"" + schema + "\".environment_role_users " +
					"WHERE environment_id = " + envId + " AND role_id = " + roleId;
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
		
			response.put("result",result);
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Updates the Testers setting on an TDM Environment role: add or delete testers. Note that this API needs to get the update testers list in the input BODY.\r\n" +
			"\r\n" +
			"Example of a request body:\r\n" +
			"{\n  \"users\": [\n    {\n      \"user_id \": \"-1\",\n      \"username\": \"ALL\"\n    },\n    {\n      \"user_id\": \"tester1\",\n      \"username\": \"tester1\"\n    }\n  ]\n}")
	@webService(path = "environment/{envId}/envname/{envName}/role/{roleId}/rolename/{roleName}/users", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateEnvironmentRoleTesters(@param(required=true) Long envId, @param(required=true) String envName, @param(required=true) Long roleId, @param(required=true) String roleName, List<Map<String,Object>> users) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		try {
			String sql = "DELETE FROM \"" + schema + "\".environment_role_users WHERE environment_id = " + envId + " AND role_id = " + roleId;
			db("TDM").execute(sql);
		
			for (Map<String, Object> user : users) {
				sql = "INSERT INTO \"" + schema + "\".environment_role_users (environment_id, role_id, user_id, username) VALUES (?, ?, ?, ?)";
				db("TDM").execute(sql,
						envId, roleId,
						user.get("user_id").toString().toLowerCase(), user.get("username").toString().toLowerCase());
			}
		
			String activityDesc = "Testers of role " + roleName + " of environment " + envName + " were updated";
			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Adds a Product to the TDM Environment.")
	@webService(path = "environment/{envId}/envname/{envName}/product", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsCreateProductForEnvironment(@param(required=true) Long envId, @param(required=true) String envName, @param(description="A unique identifier of the product in Products TDM DB table") long product_id, @param(description="Optional parameter") String data_center_name, @param(description="Populated by one of the product's versions as populated in Products TDM DB table") String product_version) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		fnUpdateEnvironmentDate(envId);
		
		try {
			Long env_product_id = fnAddProcutToEnvironment(envId, product_id, data_center_name, product_version);
			//fnAddProductInterfacesEnvironment(envId, product_id, env_product_id, interfaces);
		
			String activityDesc = "Products of environment " + envName + " were updated";
			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Updates a Product in the environment. Update the Product's version or the Product's data center name.")
	@webService(path = "environment/{envId}/envname/{envName}/product", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdateProductForEnvironment(@param(required=true) Long envId, @param(required=true) String envName, @param(description="The unique identifier of the product in environment_products TDM DB table") Long environment_product_id, String data_center_name, @param(description="Populated by one of the product's versions") String product_version, @param(description="A unique identifier of the product in products TDM DB table") long product_id) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		fnUpdateEnvironmentDate(envId);
		fnUpdateProductToEnvironment(environment_product_id, data_center_name, product_version);
		//fnUpdateProductInterfacesEnvironment(envId,product_id,environment_product_id,interfaces);
		
		try {
			String activityDesc = "Products of environment " + envName + " were updated";
			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Deletes Product from environment. This API also deletes that tasks that use the deleted Product.")
	@webService(path = "environment/{envId}/envname/{envName}/product/{prodId}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDeleteProductFromEnvironment(@param(required=true) Long envId, @param(required=true) String envName, @param(required=true) Long prodId) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		fnUpdateEnvironmentDate(envId);
		
		try {
			{
				String queryString = "UPDATE \"" + schema + "\".environment_products SET " +
						"status=(?) " +
						"WHERE environment_id = " + envId + " AND product_id = " + prodId;
				db("TDM").execute(queryString, "Inactive");
			}
			{
				String queryString = "UPDATE \"public\".tasks SET task_status = \'Inactive\' " +
						"FROM ( SELECT \"public\".tasks_logical_units.task_id " +
						"FROM \"public\".tasks_logical_units " +
						"INNER JOIN \"public\".product_logical_units " +
						"ON (\"public\".product_logical_units.lu_id = \"public\".tasks_logical_units.lu_id) " +
						"WHERE \"public\".product_logical_units.product_id = " + prodId + " ) AS sq  " +
						"WHERE \"public\".tasks.task_id = sq.task_id AND \"public\".tasks.task_status = \'Active\' ";
				db("TDM").execute(queryString);
			}
		
			String activityDesc = "Products of environment " + envName + " were updated";
			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Gets the TDM Environment roles of a given environment for the logged in user.")
	@webService(path = "environment/{envId}/userRole", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "Example 1;\r\n" +
			"{\r\n" +
			"  \"result\": {\r\n" +
			"    \"minRead\": -1,\r\n" +
			"    \"userRole\": null,\r\n" +
			"    \"minWrite\": -1\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}\r\n" +
			"Example 2:\r\n" +
			"{\r\n" +
			"  \"result\": {\r\n" +
			"    \"minRead\": -1,\r\n" +
			"    \"userRole\": {\r\n" +
			"      \"role_last_updated_by\": \"K2View\",\r\n" +
			"      \"allowed_delete_before_load\": false,\r\n" +
			"      \"environment_id\": 1,\r\n" +
			"      \"allowed_number_of_entities_to_read\": 3,\r\n" +
			"      \"allowed_replace_sequences\": false,\r\n" +
			"      \"allowed_random_entity_selection\": false,\r\n" +
			"      \"role_created_by\": \"K2View\",\r\n" +
			"      \"allow_read\": true,\r\n" +
			"      \"role_description\": \"\",\r\n" +
			"      \"allowed_task_scheduling\": false,\r\n" +
			"      \"role_id\": 5,\r\n" +
			"      \"allowed_request_of_fresh_data\": false,\r\n" +
			"      \"allowed_entity_versioning\": false,\r\n" +
			"      \"role_last_updated_date\": \"2021-05-24 10:36:33.328\",\r\n" +
			"      \"allowed_number_of_entities_to_copy\": 3,\r\n" +
			"      \"role_expiration_date\": null,\r\n" +
			"      \"allow_write\": true,\r\n" +
			"      \"allowed_creation_of_synthetic_data\": false,\r\n" +
			"      \"allowed_refresh_reference_data\": false,\r\n" +
			"      \"role_creation_date\": \"2021-05-24 10:36:33.328\",\r\n" +
			"      \"role_name\": \"roleN\",\r\n" +
			"      \"user_id\": \"admin\",\r\n" +
			"      \"role_status\": \"Active\",\r\n" +
			"      \"allowed_test_conn_failure\": false,\r\n" +
			"      \"username\": \"admin\"\r\n" +
			"    },\r\n" +
			"    \"minWrite\": -1\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetRoleForUserInEnv(@param(required=true) Long envId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		String user_id = (String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
		
		try {
			String sql= "SELECT * FROM \"" + schema + "\".environment_role_users INNER JOIN \"" + schema + "\".environment_roles" +
					" ON (\"" + schema + "\".environment_role_users.role_id = \"" + schema + "\".environment_roles.role_id" +
					" AND \"" + schema + "\".environment_roles.role_status = \'Active\') " +
					"WHERE \"" + schema + "\".environment_role_users.environment_id = " + envId +
					" AND ( \"" + schema + "\".environment_role_users.user_id = \'" + user_id + "\'" +
					" OR \"" + schema + "\".environment_role_users.user_id = \'-1\' )";
			Db.Rows rows = db("TDM").fetch(sql);
		
			List<Map<String,Object>> results=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				results.add(rowMap);
			}
		
		
			Map<String,Object> userRole = null;
			List<Map<String,Object>> testerRoles=new ArrayList<>();
		
			for(Map<String,Object> role:results){
				if(user_id.equals(role.get("user_id").toString())) testerRoles.add(role);
			}
		
			if (testerRoles.size() > 0) {
				userRole = testerRoles.get(0);
			}
			int minRead = fnGetMinRoleByType(testerRoles, "allowed_number_of_entities_to_read", "allow_read");
			int minWrite = fnGetMinRoleByType(testerRoles, "allowed_number_of_entities_to_copy", "allow_write");

			List<Map<String,Object>> allRoles=new ArrayList<>();
			if (minRead == -1 || minWrite == -1) {
				if (userRole!=null) {
					userRole = results.get(0);
				}

				for(Map<String,Object> role:results){
					if("-1".equals(role.get("user_id").toString())) allRoles.add(role);
				}
		
				if (minRead == -1) {
					minRead = fnGetMinRoleByType(allRoles, "allowed_number_of_entities_to_read", "allow_read");
				}
				if (minWrite == -1) {
					minWrite = fnGetMinRoleByType(allRoles, "allowed_number_of_entities_to_copy", "allow_write");
				}
			}
		
			Map<String,Object> returnedData=new HashMap<>();
			returnedData.put("minRead",minRead);
			returnedData.put("minWrite",minWrite);
			returnedData.put("userRole",userRole);

			if(userRole == null && allRoles.size() > 0 )
			{
				returnedData.put("userRole",allRoles);
			}
			else {
				returnedData.put("userRole", userRole);
			}
		
			response.put("result",returnedData);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	private static int fnGetMinRoleByType(List<Map<String,Object>> roles, String type, String isAllowed) {
		int min = -1;
		for (int i = 0; i < roles.size(); i++) {
			if ("true".equals(roles.get(i).get(isAllowed).toString())) {
				if (min == -1) {
					min = Integer.parseInt(roles.get(i).get(type).toString());
				} else if (min > Integer.parseInt(roles.get(i).get(type).toString())) {
					min = Integer.parseInt(roles.get(i).get(type).toString());
				}
			}
		}
		return min;
	}


	@desc("Get details for environments in which the user is associated with a role or the user is the owner.")
	@webService(path = "environmentsbyuser", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"role_last_updated_by\": \"K2View\",\r\n" +
			"      \"allowed_delete_before_load\": true,\r\n" +
			"      \"environment_id\": 1,\r\n" +
			"      \"allowed_number_of_entities_to_read\": 1,\r\n" +
			"      \"environment_created_by\": \"K2View\",\r\n" +
			"      \"allowed_replace_sequences\": false,\r\n" +
			"      \"environment_last_updated_by\": \"K2View\",\r\n" +
			"      \"allowed_random_entity_selection\": false,\r\n" +
			"      \"role_created_by\": \"K2View\",\r\n" +
			"      \"allow_read\": true,\r\n" +
			"      \"role_description\": \"\",\r\n" +
			"      \"environment_description\": null,\r\n" +
			"      \"allowed_task_scheduling\": false,\r\n" +
			"      \"role_id\": 2,\r\n" +
			"      \"allowed_request_of_fresh_data\": false,\r\n" +
			"      \"allowed_entity_versioning\": false,\r\n" +
			"      \"role_last_updated_date\": \"2021-04-25 08:17:09.010\",\r\n" +
			"      \"fabric_environment_name\": \"ENV1\",\r\n" +
			"      \"allowed_number_of_entities_to_copy\": 1,\r\n" +
			"      \"environment_name\": \"ENV1\",\r\n" +
			"      \"allow_write\": true,\r\n" +
			"      \"role_expiration_date\": null,\r\n" +
			"      \"environment_point_of_contact_phone1\": null,\r\n" +
			"      \"environment_last_updated_date\": \"2021-05-12 09:16:32.632\",\r\n" +
			"      \"environment_status\": \"Active\",\r\n" +
			"      \"allowed_creation_of_synthetic_data\": false,\r\n" +
			"      \"allowed_refresh_reference_data\": false,\r\n" +
			"      \"role_creation_date\": \"2021-04-19 09:15:54.684\",\r\n" +
			"      \"sync_mode\": \"ON\",\r\n" +
			"      \"role_name\": \"role2\",\r\n" +
			"      \"environment_point_of_contact_first_name\": null,\r\n" +
			"      \"user_id\": \"-1\",\r\n" +
			"      \"environment_point_of_contact_last_name\": null,\r\n" +
			"      \"environment_point_of_contact_email\": null,\r\n" +
			"      \"role_status\": \"Active\",\r\n" +
			"      \"allowed_test_conn_failure\": true,\r\n" +
			"      \"environment_creation_date\": \"2021-04-18 09:32:50.416\",\r\n" +
			"      \"environment_expiration_date\": null,\r\n" +
			"      \"environment_point_of_contact_phone2\": null,\r\n" +
			"      \"username\": \"all\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentsByUser() throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		String userId = sessionUser().name();
		
		try {
			response.put("result",fnGetEnvsByuser(userId));
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the list of Global variables defined in the Fabric project except the TDM product Globals. This API is invoked when the user adds a Global to the TDM Environment to override its value in this environment. The parameter \"lus\" should contain LU names divided by comma, it is optional, in case it is not null the API will return defined by the param LUs only.")
	@webService(path = "environment/getAllGlobals", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_VISITS.REDIS_MACHINE_PORT\",\r\n" +
			"      \"globalValue\": \"6379\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_VISITS.REDIS_MACHINE_IP\",\r\n" +
			"      \"globalValue\": \"10.21.3.4\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_LU.REDIS_MACHINE_PORT\",\r\n" +
			"      \"globalValue\": \"6379\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_LU.CLONE_CLEANUP_RETENTION_PERIOD_VALUE\",\r\n" +
			"      \"globalValue\": \"3\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_LU.MASK_FLAG\",\r\n" +
			"      \"globalValue\": \"0\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_LU.CLONE_CLEANUP_RETENTION_PERIOD_TYPE\",\r\n" +
			"      \"globalValue\": \"Minutes\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_LU.TDM_SOURCE_PRODUCT_VERSION\",\r\n" +
			"      \"globalValue\": \"false\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_LU.TDM_TARGET_PRODUCT_VERSION\",\r\n" +
			"      \"globalValue\": \"false\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_LU.TDM_SYNTHETIC_DATA\",\r\n" +
			"      \"globalValue\": \"false\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"globalName\": \"PATIENT_LU.REDIS_MACHINE_IP\",\r\n" +
			"      \"globalValue\": \"10.21.3.4\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetAllFabricGlobals(@param(required=false) String lus) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		try {
			//from wsGetAllGlobals
			Map<String, Map<String, Object>> globalsPerLu = new HashMap<>();
			Map<String, Map<String, Object>> globalsShared = new HashMap<>();
		
			((List) getFabricResponse("set")).forEach(var -> {
				String[] keyParts = ((String) ((Map) var).get("key")).split("\\.");
				if (keyParts.length == 3 && "Global".equals(keyParts[0])) {
					if (!"TDM_DELETE_BEFORE_LOAD".equals(keyParts[2]) &&
						!"TDM_INSERT_TO_TARGET".equals(keyParts[2]) &&
						!"TDM_SOURCE_PRODUCT_VERSION".equals(keyParts[2]) &&
						!"TDM_TARGET_PRODUCT_VERSION".equals(keyParts[2]) &&
						!"TDM_SYNC_SOURCE_DATA".equals(keyParts[2]) &&
						!"ROOT_TABLE_NAME".equals(keyParts[2]) &&
						!"ROOT_COLUMN_NAME".equals(keyParts[2]) &&
						!"TDM_REPLACE_SEQUENCES".equals(keyParts[2]) &&
						!"TDM_TASK_EXE_ID".equals(keyParts[2]) &&
						!"TDM_SOURCE_ENVIRONMENT_NAME".equals(keyParts[2]) &&
						!"TDM_TAR_ENV_NAME".equals(keyParts[2]) &&
						!"TDM_TASK_ID".equals(keyParts[2]) &&
						!"TDM_VERSION_DATETIME".equals(keyParts[2]) &&
						!"TDM_VERSION_NAME".equals(keyParts[2]) &&
						!"ORACLE8_DB_TYPE".equals(keyParts[2]) &&
						!"COMBO_MAX_COUNT".equals(keyParts[2]) &&
						!"TDM_SYNTHETIC_DATA".equals(keyParts[2])
					) 
					{
						if ("k2_ws".equals(keyParts[1])) {
							// TDM 7.1 - Add the globals of k2_ws as they are the Shared globals, to allow user to add globals at shared level to impact all LUs
							Map<String, Object> sharedGlobals = globalsShared.computeIfAbsent(keyParts[1], k -> new HashMap<>());
							sharedGlobals.put(keyParts[2], ((Map<String, Object>) var).get("value"));
						} else if (!"k2_ref".equals(keyParts[1]) &&  !"TDM".equals(keyParts[1])) {
							Map<String, Object> luGlobals = globalsPerLu.computeIfAbsent(keyParts[1], k -> new HashMap<>());
							luGlobals.put(keyParts[2], ((Map<String, Object>) var).get("value"));
						}
					}
				}
			});
			//end WsGetAllGlobals
		
		    List<Map<String,Object>> globals = new ArrayList<>();
		
			// TDM 7.1 - Add the globals of k2_ws as they are the Shared globals, to allow user to add globals at shared level to impact all LUs
			for (Map.Entry<String, Map<String, Object>> entry : globalsShared.entrySet()) {
				Map<String,Object> value = entry.getValue();
				for (String varName : value.keySet()) {
					HashMap<String,Object> global=new HashMap<>();
					global.put("globalName",  varName);
					global.put("globalValue", value.get(varName));
					globals.add(global);
				}
			}

			List luNames = null;
			if (lus != null && !lus.isEmpty()) {
				luNames = new ArrayList();
				List finalLuNames = luNames;
				Arrays.stream(lus.split(",")).forEach(lu -> {
					finalLuNames.add(lu.trim());
				});
			}
			for (Map.Entry<String, Map<String, Object>> entry : globalsPerLu.entrySet()) {
				String luName = entry.getKey();
				if (luNames != null && !luNames.contains(luName)) {
					continue;
				}
				Map<String,Object> value = entry.getValue();
				for (String varName : value.keySet()) {
					HashMap<String,Object> global=new HashMap<>();
					global.put("globalName", luName + "." + varName);
					global.put("globalValue", value.get(varName));
					globals.add(global);
				}
			}
			errorCode = "SUCCESS";
			response.put("result",globals);
			//return globalsPerLu;
		
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Updates Environment Global")
	@webService(path = "environment/{envId}/envname/{envName}/global", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdateEnvironmentGlobal(@param(required=true) String envName, @param(required=true) Long envId, String environment_id, String global_name, String global_value) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		fnUpdateEnvironmentDate(envId);
		
		try {
			String updateQuery= "UPDATE \"" + schema + "\".tdm_env_globals SET " +
					"environment_id=(?)," +
					"global_name=CAST(? AS VARCHAR)," +
					"global_value=(?)," +
					"update_date=(?)," +
					"updated_by=(?) " +
					"WHERE environment_id=(?) AND global_name=CAST(? AS VARCHAR)";
		
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			db("TDM").execute(updateQuery, environment_id, global_name, global_value, now, username, environment_id,global_name);
		
			String activityDesc = "Globals of environment " + envName + " were updated";
			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Deletes Environment Global")
	@webService(path = "environment/{envId}/envname/{envName}/global/{globalName}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDeleteEnvironmentGlobal(@param(required=true) Long envId, @param(required=true) String envName, @param(required=true) String globalName) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		fnUpdateEnvironmentDate(envId);
		
		try {
			String sql= "DELETE FROM \"" + schema + "\".tdm_env_globals" + " WHERE environment_id = " + envId + " AND global_name = \'" + globalName +"\'";
			db("TDM").execute(sql);
		
			String activityDesc = "Globals of environment " + envName + " were updated";
			try {
				fnInsertActivity("global", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Checks if an exclusion list already exists for the user, environment and business entity. If yes- a new list cannot be opened. Insrtead, the existing list can be updated.")
	@webService(path = "environment/{envId}/validateRequestedBy", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_env_exclusion_list_id\": 10,\r\n" +
			"      \"be_id\": 3,\r\n" +
			"      \"requested_by\": \"test2903\",\r\n" +
			"      \"environment_id\": 11,\r\n" +
			"      \"exclusion_list\": \"12,13,22,91\",\r\n" +
			"      \"updated_by\": \"K2View\",\r\n" +
			"      \"creation_date\": \"2021-03-29 07:55:06.839\",\r\n" +
			"      \"created_by\": \"K2View\",\r\n" +
			"      \"update_date\": \"2021-03-29 08:25:51.168\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetValidateRequestBy(@param(required=true) Long envId, Long be_id, String requested_by) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();
		String message = null;
		String errorCode = "";
		
		try {
			String sql= "SELECT * FROM   \"" + schema + "\".tdm_be_env_exclusion_list " +
					"WHERE environment_id = " + envId + " AND be_id = " + be_id +
					"AND requested_by = " + "\'" + requested_by + "\'";
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
		
			/*
			result.put("be_env_exclusion_list_id",Integer.parseInt(row.cell(0).toString()));
			result.put("be_id",Integer.parseInt(row.cell(1).toString()));
			result.put("environment_id",Integer.parseInt(row.cell(2).toString()));
			result.put("exclusion_list",row.cell(3));
			result.put("requested_by",row.cell(4));
			result.put("update_date",row.cell(5));
			result.put("created_by",row.cell(5));
			result.put("updated_by",row.cell(7));
			result.put("creation_date",row.cell(8));
			 */
			response.put("result", result);
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Adds an Exclusion List to the TDM environment.The Exclusion List is being validated. If at least of the the entites is already included in another Exclusion List of the Environment and the Business Entity, the API returns this list and the user is asked to change ther entities in the Exclusion List.")
	@webService(path = "environment/{envId}/validateList", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"unnest\": \"32\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"unnest\": \"34\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"unnest\": \"35\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"unnest\": \"73\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsAddValidateList(@param(required=true) Long envId, String exclusion_list, Long be_id) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();
		String message = null;
		String errorCode = "";
		
		try {
			String sql = "select unnest(string_to_array(replace('" + exclusion_list + "', ' ', ''), ',')) " +
					"INTERSECT select unnest(string_to_array(replace(string_agg(exclusion_list, ','), ' ', ''), ',')) " +
					"from TDM_BE_ENV_EXCLUSION_LIST where be_id=" + be_id + " and environment_id=" + envId;
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
			errorCode = "SUCCESS";
			response.put("result",result);
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Validate the Exclusion List when it is updated, If at least of the the entites is already included in another Exclusion List of the Environment and the Business Entity, the API returns this list and the user is asked to change ther entities in the Exclusion List.")
	@webService(path = "environment/{envId}/validateListBeforeUpdate", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"unnest\": \"77\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"unnest\": \"83\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsValidateListBeforeUpdate(@param(required=true) Long envId, String exclusion_list, Long be_id, Long be_env_exclusion_list_id) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		try {
			String sql= "select unnest(string_to_array(replace('" + exclusion_list + "', ' ', ''), ',')) " +
					"INTERSECT select unnest(string_to_array(replace(string_agg(exclusion_list, ','), ' ', ''), ',')) " +
					"from TDM_BE_ENV_EXCLUSION_LIST where be_id=" + be_id + " and environment_id=" +  envId
					+ "and be_env_exclusion_list_id != " +  be_env_exclusion_list_id;
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
			errorCode = "SUCCESS";
			response.put("result",result);
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Updates the Environment Exclusion List")
	@webService(path = "environment/{envId}/exclusionLists/{elId}", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdateEnvironmentExclusionList(@param(required=true) Long envId, @param(description="The unique identifier of the exclusion list in tdm_be_env_exclusion_list TDM DB table", required=true) Long elId, Long be_id, @param(description="A list of entities separated by a comma") String exclusion_list, @param(description="The user identifier of the user who requsted for the exclusion list") String requested_by) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		fnUpdateEnvironmentDate(envId);
		
		try {
			String sql= "UPDATE tdm_be_env_exclusion_list SET be_id=(?), exclusion_list=(?), requested_by=(?), update_date=(?), updated_by=(?) " +
					"WHERE be_env_exclusion_list_id = " + elId;
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			db("TDM").execute(sql,be_id, exclusion_list,
					requested_by, now, username);
			String activityDesc = "Exclusion list " + elId + " of environment " + envId + " was updated.";
			try {
				fnInsertActivity("Update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Deletes an Environment Exclusion List.")
	@webService(path = "environment/{envId}/exclusionLists/{elId}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDeleteEnvironmentExclusionList(@param(required=true) Long envId, @param(required=true) Long elId) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if(permissionGroup==null) return wrapWebServiceResults("FAIL", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAIL", "You have a Tester permission group and therefore cannot update the environment", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) return wrapWebServiceResults("FAIL", "You are not the owner of the environment and therefore is not allowed to update the environment", null);
			}
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		fnUpdateEnvironmentDate(envId);
		
		try {
			String sql = "DELETE FROM \"" + schema + "\".tdm_be_env_exclusion_list WHERE be_env_exclusion_list_id = " + elId;
			db("TDM").execute(sql);
			String activityDesc = "Exclusion list " + elId + " of environment " + envId + " was deleted.";
			try {
				fnInsertActivity("Update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets all testers not attached to the input TDM environment")
	@webService(path = "environment/{envId}/envTesters", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"uid\": \"tester1\",\r\n" +
			"      \"user_id\": \"tester1\",\r\n" +
			"      \"displayName\": \"tester1\",\r\n" +
			"      \"username\": \"tester1\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentTesters(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		try {
			List<Map<String,Object>> testers = new ArrayList<>();
			List<String> users = (List<String>) ((Map<String,Object>)com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUsersByPermissionGroup("tester")).get("result");
			for(String user:users){
				Map<String,Object> tester=new HashMap<>();
				tester.put("user_id", user);
				tester.put("username", user);
				tester.put("uid", user);
				tester.put("displayName", user);
				testers.add(tester);
			}
		
			String sql = "SELECT * FROM \"" + schema + "\".environment_role_users " +
					"INNER JOIN \"" + schema + "\".environment_roles " +
					"ON (\"" + schema + "\".environment_role_users.role_id = \"" + schema + "\".environment_roles.role_id) " +
					"WHERE \"" + schema + "\".environment_roles.role_status = \'Active\'";
			Db.Rows rows = db("TDM").fetch(sql);
			List<Map<String,Object>> testersRows=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				testersRows.add(rowMap);
			}
		
			boolean found;
			List<Map<String,Object>> freeTesters=new ArrayList<>();
			for(Map<String,Object> tester:testers) {
				found=false;
				for(Map<String,Object> testerRow:testersRows) {
					if(testerRow.get("user_id")!=null && tester.get("uid")!=null
							&& testerRow.get("user_id").toString().equals(tester.get("uid").toString())
							&& testerRow.get("environment_id")!=null
							&& testerRow.get("environment_id").toString().equals(envId.toString())) {
						found = true; break;
					}
				}
				if(!found) freeTesters.add(tester);
			}
		
			for(Map<String,Object> tester:freeTesters){
				tester.put("user_id", tester.get("uid"));
				tester.put("username", tester.get("displayName"));
			}
		
			response.put("result",freeTesters);
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets Environment Testers")
	@webService(path = "environment/{envId}/testers", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"role_last_updated_by\": \"k2view\",\r\n" +
			"      \"maximum_number_of_entities_data_ownership\": null,\r\n" +
			"      \"allowed_delete_before_load\": false,\r\n" +
			"      \"environment_id\": 6,\r\n" +
			"      \"allowed_number_of_entities_to_read\": 1,\r\n" +
			"      \"allowed_replace_sequences\": false,\r\n" +
			"      \"maximum_number_of_days_data_ownership\": null,\r\n" +
			"      \"allowed_random_entity_selection\": true,\r\n" +
			"      \"role_created_by\": \"k2view\",\r\n" +
			"      \"allow_read\": true,\r\n" +
			"      \"role_description\": \"roleDescription\",\r\n" +
			"      \"allowed_task_scheduling\": false,\r\n" +
			"      \"role_id\": 9,\r\n" +
			"      \"allowed_request_of_fresh_data\": true,\r\n" +
			"      \"allowed_entity_versioning\": false,\r\n" +
			"      \"role_last_updated_date\": \"2021-03-24 20:29:50.846\",\r\n" +
			"      \"allowed_number_of_entities_to_copy\": 1,\r\n" +
			"      \"role_expiration_date\": null,\r\n" +
			"      \"allow_write\": true,\r\n" +
			"      \"allowed_creation_of_synthetic_data\": false,\r\n" +
			"      \"allowed_refresh_reference_data\": false,\r\n" +
			"      \"role_creation_date\": \"2021-03-24 20:29:50.846\",\r\n" +
			"      \"role_name\": \"roleName\",\r\n" +
			"      \"user_id\": null,\r\n" +
			"      \"role_status\": \"Active\",\r\n" +
			"      \"allowed_test_conn_failure\": false,\r\n" +
			"      \"username\": null\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"uid\": \"tester01\",\r\n" +
			"      \"user_id\": \"tester01\",\r\n" +
			"      \"displayName\": \"tester01\",\r\n" +
			"      \"username\": \"tester01\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"uid\": \"envadmin01\",\r\n" +
			"      \"user_id\": \"envadmin01\",\r\n" +
			"      \"displayName\": \"envadmin01\",\r\n" +
			"      \"username\": \"envadmin01\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvTesters(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		
		try {
			List<Map<String,Object>> testers = new ArrayList<>(); // fnGetTesters();
			List<String> users = (List<String>) ((Map<String,Object>)com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUsersByPermissionGroup("tester")).get("result");
			for(String user:users){
				Map<String,Object> tester=new HashMap<>();
				tester.put("user_id", user);
				tester.put("username", user);
				tester.put("uid", user);
				tester.put("displayName", user);
				testers.add(tester);
			}

			try {
				//List<Map<String,Object>> owners = fnGetEnvOwners();
				List<String> owners = (List<String>) ((Map<String,Object>)com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUsersByPermissionGroup("owner")).get("result");
				for(String user:owners){
					Map<String,Object> owner=new HashMap<>();
					owner.put("user_id", user);
					owner.put("username", user);
					owner.put("uid", user);
					owner.put("displayName", user);
					testers.add(owner);
				}
			} catch(Exception e){
				log.error("failed to get owners");
			}

			for(int i=0;i<testers.size();i++){
				Map<String,Object> tester=testers.get(i);
				for(int j=i+1;j<testers.size();j++){
					Map<String,Object> _tester=testers.get(j);
					if(_tester.get("uid").toString().equals(tester.get("uid").toString())){
						testers.remove(_tester); j--;
					}
				}
			}

			Map<String,Object> t=new HashMap<>();
			t.put("displayName","ALL");
			t.put("uid","-1");
			t.put("user_id","-1");
			t.put("username","ALL");
			testers.add(0,t);

			String sql = "SELECT * FROM \"" + schema + "\".environment_role_users " +
					"INNER JOIN \"" + schema + "\".environment_roles " +
					"ON (\"" + schema + "\".environment_role_users.role_id = \"" + schema + "\".environment_roles.role_id) " +
					"WHERE \"" + schema + "\".environment_roles.role_status = \'Active\'";
			Db.Rows rows = db("TDM").fetch(sql);
		
			List<Map<String,Object>> testersRows=new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> rowMap = new HashMap<>();
				for (String columnName : columnNames) {
					rowMap.put(columnName, resultSet.getObject(columnName));
				}
				testersRows.add(rowMap);
			}

			boolean found;
			List<Map<String,Object>> freeTesters=new ArrayList<>();
			for(Map<String,Object> tester:testers) {
				found=false;
				for(Map<String,Object> testerRow:testersRows) {
					if(testerRow.get("user_id")!=null
							&& testerRow.get("user_id").toString().equals(tester.get("uid").toString())
							&& testerRow.get("environment_id")!=null
							&& testerRow.get("environment_id").toString().equals(envId.toString())){
						found=true;break;
					}
				}
				if(!found) freeTesters.add(tester);
			}
		
			for(Map<String,Object> tester:freeTesters){
				tester.put("user_id", tester.get("uid"));
				tester.put("username", tester.get("displayName"));
			}

			response.put("result",freeTesters);
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets Environment by ID")
	@webService(path = "environment/{envId}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"allow_write\": true,\r\n" +
			"      \"environment_point_of_contact_phone1\": null,\r\n" +
			"      \"environment_id\": 1,\r\n" +
			"      \"environment_created_by\": \"K2View\",\r\n" +
			"      \"environment_last_updated_date\": \"2021-04-25 13:06:35.781\",\r\n" +
			"      \"environment_last_updated_by\": \"K2View\",\r\n" +
			"      \"environment_status\": \"Active\",\r\n" +
			"      \"allow_read\": true,\r\n" +
			"      \"sync_mode\": \"ON\",\r\n" +
			"      \"environment_description\": null,\r\n" +
			"      \"environment_point_of_contact_first_name\": null,\r\n" +
			"      \"environment_point_of_contact_last_name\": null,\r\n" +
			"      \"environment_point_of_contact_email\": null,\r\n" +
			"      \"environment_creation_date\": \"2021-04-18 09:32:50.416\",\r\n" +
			"      \"fabric_environment_name\": \"ENV1\",\r\n" +
			"      \"environment_expiration_date\": null,\r\n" +
			"      \"environment_point_of_contact_phone2\": null,\r\n" +
			"      \"environment_name\": \"ENV1\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object GetEnvironmentById(@param(required=true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;
		
		
		try {
			String sql = "SELECT * FROM \"" + schema + "\".environments WHERE environment_id = " + envId;
			Db.Rows rows = db("TDM").fetch(sql);
			errorCode = "SUCCESS";
			List<Map<String, Object>> result = new ArrayList<>();
			List<String> columnNames = rows.getColumnNames();
			for (Db.Row row : rows) {
				ResultSet resultSet = row.resultSet();
				Map<String, Object> environment = new HashMap<>();
				for (String columnName : columnNames) {
					environment.put(columnName, resultSet.getObject(columnName));
				}
				result.add(environment);
			}
			/*
			for (Db.Row row : rows) {
				environment = new HashMap<String, Object>();
				environment.put("environment_id", Integer.parseInt(row.cell(0).toString()));
				environment.put("environment_name", "" + row.cell(1));
				environment.put("environment_description", "" + row.cell(2));
				environment.put("environment_point_of_contact_first_name", "" + row.cell(3));
				environment.put("environment_point_of_contact_last_name", "" + row.cell(4));
				environment.put("environment_point_of_contact_phone1", "" + row.cell(5));
				environment.put("environment_point_of_contact_phone2", "" + row.cell(6));
				environment.put("environment_point_of_contact_email", "" + row.cell(7));
				environment.put("environment_created_by", "" + row.cell(8));
				environment.put("environment_creation_date", "" + row.cell(9));
				environment.put("environment_last_updated_date", "" + row.cell(10));
				environment.put("environment_last_updated_by", "" + row.cell(11));
				environment.put("environment_status", "" + row.cell(12));
				environment.put("allow_write", row.cell(13)!=null?Boolean.parseBoolean(row.cell(13).toString()):null);
				environment.put("fabric_environment_name", "" + row.cell(14));
				environment.put("allow_read", row.cell(15)!=null?Boolean.parseBoolean(row.cell(15).toString()):null);
				environment.put("sync_mode", "" + row.cell(16));
				environment.put("user_name", "" + row.cell(17));
				result.add(environment);
			}
			 */
		
			response.put("result", result);
		
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Gets a summary information and statistics on the tasks executed over the last month on a given environment:\r\n" +
			"> Processed Entities, the number of entities processed by tasks executed on the environment.\r\n" +
			"> The number of active tasks on the environment per task execution status which can be either Active or On hold.\r\n" +
			"> The number of testers that are attached to the environment.\r\n" +
			"> The number of tasks executed on an environment over last month per execution status.")
	@webService(path = "environment/{envId}/summary/{interval}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"numberOfTesters\": {\r\n" +
			"      \"numberoftesters\": 4\r\n" +
			"    },\r\n" +
			"    \"processedEntities\": {\r\n" +
			"      \"sum\": 31896\r\n" +
			"    },\r\n" +
			"    \"taskExecutionStatus\": {\r\n" +
			"      \"running\": 0,\r\n" +
			"      \"paused\": 0,\r\n" +
			"      \"stopped\": 0,\r\n" +
			"      \"pending\": 0,\r\n" +
			"      \"failed\": 23,\r\n" +
			"      \"completed\": 15\r\n" +
			"    },\r\n" +
			"    \"numberOfALLTesters\": {\r\n" +
			"      \"value\": 2\r\n" +
			"    },\r\n" +
			"    \"Environment\": 1,\r\n" +
			"    \"tasks\": {\r\n" +
			"      \"active\": 19,\r\n" +
			"      \"onhold\": 3\r\n" +
			"    },\r\n" +
			"    \"Interval\": \"Year\"\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentSummary(@param(required=true) Long envId, @param(description="Day, Week, Month ,3Month or Year", required=true) String interval) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;
		
		Map<String,Object> data=new HashMap<>();
		data.put("Environment", envId);
		data.put("Interval", interval);
		try {
			{
				String numberOfTestersQuery = "SELECT count(*) as numberOfTesters FROM  \"" + schema + "\".environment_role_users  " +
						"INNER JOIN \"" + schema + "\".environment_roles  ON (\"" + schema + "\".environment_role_users.role_id = \"" + schema + "\".environment_roles.role_id)" +
						" WHERE \"" + schema + "\".environment_roles.role_status = \'Active\' and \"" + schema + "\".environment_roles.environment_id = " + envId;
				String index = "numberOfTesters";
				Db.Row row = db("TDM").fetch(numberOfTestersQuery).firstRow();
				data.put(index, row);
			}
			{
				String numberOfALLTestersQuery = "SELECT count(*) as value FROM  \"" + schema + "\".environment_role_users  " +
						"INNER JOIN \"" + schema + "\".environment_roles  ON (\"" + schema + "\".environment_role_users.role_id = \"" + schema + "\".environment_roles.role_id)" +
						" WHERE \"" + schema + "\".environment_roles.role_status = \'Active\' and " +
						"\"" + schema + "\".environment_roles.environment_id = " + envId + " AND environment_role_users.user_id = \'-1\'";
				String index = "numberOfALLTesters";
				Db.Row row = db("TDM").fetch(numberOfALLTestersQuery).firstRow();
				data.put(index, row);
			}
			{
				String tasksQuery = "SELECT COUNT(case when \"" + schema + "\".tasks.task_execution_status = \'Active\' then 1 end) as active ," +
						"COUNT(case when \"" + schema + "\".tasks.task_execution_status = \'onHold\' then 1 end  )  as onHold" +
						" FROM \"" + schema + "\".tasks " +
						" where \"" + schema + "\".tasks.environment_id = " + envId + " AND \"" + schema + "\".tasks.task_status = \'Active\'";
				String index = "tasks";
				Db.Row row = db("TDM").fetch(tasksQuery).firstRow();
				data.put(index, row);
			}
			{
		
				String taskExecutionStatusQuery = "SELECT \"" + schema + "\".task_execution_list.task_execution_id,\"" + schema + "\".task_execution_list.execution_status FROM \"" + schema + "\".task_execution_list " +
						" WHERE \"" + schema + "\".task_execution_list.environment_id=" + envId + " AND  (  select now() - interval \'" + fnGetInterval(interval) + "\')  <= \"" + schema + "\".task_execution_list.creation_date";
				String index = "taskExecutionStatus";
				Db.Rows rows = db("TDM").fetch(taskExecutionStatusQuery);
		
				List<Map<String, Object>> rowsList = new ArrayList<>();
				List<String> columnNames = rows.getColumnNames();
				for (Db.Row row : rows) {
					ResultSet resultSet = row.resultSet();
					Map<String, Object> rowMap = new HashMap<>();
					for (String columnName : columnNames) {
						rowMap.put(columnName, resultSet.getObject(columnName));
					}
					rowsList.add(rowMap);
				}
		
				Map<String, List<Map<String, Object>>> groups = new HashMap<>();
				List<Map<String, Object>> groupById;
		
				for (int i = 0; i < rowsList.size(); i++) {
					Map<String, Object> row = rowsList.get(i);
					groupById = new ArrayList<>();
					groupById.add(row);
		
					for (int j = i + 1; j < rowsList.size(); j++) {
						Map<String, Object> otherRow = rowsList.get(j);
						if (row.get("task_execution_id").toString().equals(otherRow.get("task_execution_id").toString())) {
							groupById.add(otherRow);
							rowsList.remove(j); j--;
						}
					}
		
					groups.put(row.get("task_execution_id").toString(), groupById);
				}
				data.put(index, fnExtractExecutionStatus(groups));
		
			}
			{
				String processedEntitiesQuery = "Select sum(num_of_processed_entities) from " +
						"(select distinct COALESCE(\"" + schema + "\".task_execution_list.num_of_processed_entities,0) num_of_processed_entities , \"" +
						schema + "\".task_execution_list.task_execution_id from \"" + schema + "\".task_execution_list , \"" + schema + "\".product_logical_units p " +
						"Where p.lu_id = \"" + schema + "\".task_execution_list.lu_id And \"" + schema + "\".task_execution_list.environment_id = " + envId + " " +
						"And  \"" + schema + "\".task_execution_list.creation_date >= (  select now() - interval \'" + fnGetInterval(interval) + "\') ) t";
				String index = "processedEntities";
				Db.Row row = db("TDM").fetch(processedEntitiesQuery).firstRow();
				data.put(index, row);
			}
			errorCode="SUCCESS";
			response.put("result",data);
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	//from TDM.logic
	@desc("Returns all the environments deployed to Fabric to set the environment name setting in the TDM GUI for a given environment.")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    \"ENV1\",\r\n" +
			"    \"ENV2\"\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetAllEnvs() throws Exception {
		try {
			Set<String> envList = InterfacesManager.getInstance().getAllEnvironments();
			envList.remove("_dev");
			return wrapWebServiceResults("SUCCESS", null, envList);
		} catch (Exception e) {
			return wrapWebServiceResults("FAIL", e.getMessage(), null);
		}
		
	}
	//end from TDM.logic


	@desc("Gets the list of the user's TDM active environments and the role ID of each environment.\r\n" +
			"\r\n" +
			"- If the user's permission group is Admin:\r\n" +
			"   - Select all active environments and populate the role_id by \"admin\".\r\n" +
			"\r\n" +
			"\r\n" +
			"- If the user's permission group is Owner, select the following environments:  \r\n" +
			"   - Select all active environments where the user is set as environment owner. Populate the role_id by \"owner\".\r\n" +
			"\r\n" +
			"   - Select all active environments where the user is attached to by a TDM environment role. Populate the role_id by the TDM environment's role_id.  \r\n" +
			"   \r\n" +
			"- If the user's permission group is \"Tester\":\r\n" +
			"  - Select all active environments where the user is attached to by a TDM environment role. Populate the role_id by the TDM Environment's role. \r\n" +
			"\r\n" +
			"Notes:\r\n" +
			"-  A TDM Environment's role can be attached to selected users or to all TDM users. A user can be attached to a TDM Environment role by their user id.\r\n" +
			"- A user cannot be attached to multiple TDM environment role on a given environment. Therefore, if a  user is attached to a TDM Environment role by their user id, the 'ALL' role is not applicable for this user.")
	@webService(path = "listOfEnvsByUser", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"source environments\": [\r\n" +
			"\t\t{\r\n" +
			"          \"environment_id\": 3,\r\n" +
			"          \"role_id\": \"owner\",\r\n" +
			"          \"assignment_type\": \"owner\",\r\n" +
			"          \"environment_name\": \"testEnv\"\r\n" +
			"        },\r\n" +
			"        {\r\n" +
			"          \"environment_id\": 1,\r\n" +
			"          \"role_id\": 5,\r\n" +
			"          \"assignment_type\": \"user\",\r\n" +
			"          \"environment_name\": \"ENV1\"\r\n" +
			"        }\r\n" +
			"\t  ]\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"target environments\": [\r\n" +
			"        {\r\n" +
			"          \"environment_id\": 4,\r\n" +
			"          \"role_id\": 6,\r\n" +
			"          \"assignment_type\": \"user\",\r\n" +
			"          \"environment_name\": \"ENV3\"\r\n" +
			"        }\r\n" +
			"      ]\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetListOfEnvsByUser() throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;
		try{
			List<Map<String, Object>> rowsList = new ArrayList<>();
			String userId = "" + fabric().fetch("set username").firstValue();
		
			//Check the permission group of the user.
			//If the permission group is Admin => select all the active environments
			String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
			if (admin.equalsIgnoreCase(permissionGroup)){
				String allEnvs = "Select env.environment_id,env.environment_name,\n" +
						"  Case When env.allow_read = True And env.allow_write = True Then 'BOTH'\n" +
						"    When env.allow_write = True Then 'TARGET' Else 'SOURCE'\n" +
						"  End As environment_type,\n" +
						"  'admin' As role_id,\n" +
						"  'admin' As assignment_type\n" +
						"From environments env\n" +
						"Where env.environment_status = 'Active'";
				Db.Rows rows= db("TDM").fetch(allEnvs);
				List<String> columnNames = rows.getColumnNames();
				for (Db.Row row : rows) {
					ResultSet resultSet = row.resultSet();
					Map<String, Object> rowMap = new HashMap<>();
					for (String columnName : columnNames) {
						rowMap.put(columnName, resultSet.getObject(columnName));
					}
					rowsList.add(rowMap);
				}
		
			} else {
				rowsList.addAll(fnGetEnvsByuser(userId));
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
		
			errorCode="SUCCESS";
			response.put("result",result);
		
		} catch (Exception e) {
			errorCode = "FAIL";
			message = e.getMessage();
			log.error(message);
		}
		
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	static List<Map<String,Object>> fnGetEnvsByuser(String userId) throws Exception{
		List<Map<String, Object>> rowsList = new ArrayList<>();

		//get the environments where the user is the owner
		String query1 = "select *, " +
				"CASE when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, 'owner' as role_id, 'owner' as assignment_type " +
				"from environments env, environment_owners o " +
				"where env.environment_id = o.environment_id " +
				"and o.user_id = (?) " +
				"and env.environment_status = 'Active'";
		Db.Rows rows = db("TDM").fetch(query1,userId);

		List<String> columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			rowsList.add(rowMap);
		}

		String envIds="(";
		if(!rowsList.isEmpty()){
			for(Map<String,Object> row:rowsList) envIds+=row.get("environment_id")+",";
			envIds=envIds.substring(0,envIds.length()-1);}
		envIds+=")";

		//get the environments where the user is assigned to a role by their username
		String query2 = "select *, " +
				"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
				"from environments env, environment_roles r, environment_role_users u " +
				"where env.environment_id = r.environment_id " +
				"and lower(r.role_status) = 'active' " +
				"and r.role_id = u.role_id " +
				"and u.user_id = (?) " +
				"and env.environment_status = 'Active'";
		// remove the list of environments returned by query 1;
		query2+="()".equals(envIds)?"": "and env.environment_id not in " + envIds;
		rows = db("TDM").fetch(query2,userId);

		columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			rowsList.add(rowMap);
		}

		envIds="(";
		if(!rowsList.isEmpty()) {
			for(Map<String,Object> row:rowsList) envIds+=row.get("environment_id")+",";
			envIds=envIds.substring(0,envIds.length()-1);
		}
		envIds+=")";

		//get the environments where the user is assigned to a role by 'ALL' assignment
		String query3 = "select *, " +
				"CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type " +
				", r.role_id, 'all' as assignment_type " +
				"from environments env, environment_roles r, environment_role_users u " +
				"where env.environment_id = r.environment_id " +
				"and lower(r.role_status) = 'active' " +
				"and r.role_id = u.role_id " +
				"and lower(u.username) = 'all' " +
				"and env.environment_status = 'Active'";
		// remove the list of environments returned by queries 1+2;
		query3+="()".equals(envIds)?"": "and env.environment_id not in " + envIds;
		rows = db("TDM").fetch(query3);

		columnNames = rows.getColumnNames();
		for (Db.Row row : rows) {
			ResultSet resultSet = row.resultSet();
			Map<String, Object> rowMap = new HashMap<>();
			for (String columnName : columnNames) {
				rowMap.put(columnName, resultSet.getObject(columnName));
			}
			rowsList.add(rowMap);
		}
		return rowsList;
	}


	static  String fnGetInterval(String interval){
		if ("Day".equals(interval)){
			return "1 day";
		}
		else if ("Week".equals(interval)){
			return "1 week";
		}
		else if ("Month".equals(interval)){
			return "1 month";
		}
		else if ("3Month".equals(interval)){
			return "3 month";
		}
		else if ("Year".equals("interval")){
			return "1 year";
		}
		else {
			return "1 month";
		}
	}

	//checks if the registered user is an owner of the given environment
	static Boolean fnIsOwner(String envId) throws Exception{
		List<Map<String, Object>> envsList = (List<Map<String, Object>>) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Environments.Logic.wsGetListOfEnvsByUser()).get("result");
		boolean found=false;
		for (Map<String, Object> envsGroup : envsList) {
			Map.Entry<String, Object> entry = envsGroup.entrySet().iterator().next();
			List<Map<String, Object>> groupByType = (List<Map<String, Object>>) entry.getValue();
			for (Map<String, Object> env : groupByType) {
				if ("owner".equals(env.get("role_id").toString())) {
					if(env.get("environment_id").toString().equals(envId)){
						found=true; break;
					}
				}
			}
		}
		return found;
	}

	static Object fnExtractExecutionStatus(Map<String,List<Map<String,Object>>> executionsStatusGroup){
		Map<String,Integer> executionsStatus = new HashMap<>();
		executionsStatus.put("failed", 0);
		executionsStatus.put("pending", 0);
		executionsStatus.put("paused", 0);
		executionsStatus.put("stopped", 0);
		executionsStatus.put("running",0);
		executionsStatus.put("completed", 0);

		out: for (Map.Entry<String, List<Map<String,Object>>> entry : executionsStatusGroup.entrySet()) {
			List<Map<String,Object>> group = entry.getValue();
			for (Map<String,Object> execution:group) {
				if("FAILED".equals(execution.get("execution_status").toString().toUpperCase())) {
					executionsStatus.put("failed", executionsStatus.get("failed") + 1);
					continue out;
				}
			}
			for (Map<String,Object> execution:group) {
				if("PENDING".equals(execution.get("execution_status").toString().toUpperCase())) {
					executionsStatus.put("pending", executionsStatus.get("pending") + 1);
					continue out;
				}
			}
			for (Map<String,Object> execution:group) {
				if("PAUSED".equals(execution.get("execution_status").toString().toUpperCase())) {
					executionsStatus.put("paused", executionsStatus.get("paused") + 1);
					continue out;
				}
			}
			for (Map<String,Object> execution:group) {
				if("stopped".equals(execution.get("execution_status").toString().toUpperCase())) {
					executionsStatus.put("stopped", executionsStatus.get("stopped") + 1);
					continue out;
				}
			}

			Boolean runningFound = false;
			for (Map<String,Object> execution:group){
				if (execution.get("execution_status")==null) continue;
				if ("RUNNING".equals(execution.get("execution_status").toString().toUpperCase()) || "EXECUTING".equals(execution.get("execution_status").toString().toUpperCase()) ||
						"STARTED".equals(execution.get("execution_status").toString().toUpperCase()) || "STARTEXECUTIONREQUESTED".equals(execution.get("execution_status").toString())){
					executionsStatus.put("running", executionsStatus.get("running") + 1);
					runningFound = true;
					break;
				}
			}
			if (runningFound==true){
				continue out;
			}

			executionsStatus.put("completed",executionsStatus.get("completed") + 1);
		}

		return executionsStatus;
	}

	static List<Map<String,Object>> fnGetTesters() throws Exception{
		List<Map<String,Object>> result=new ArrayList<>();
		String url = ldapUrlString;
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ldapAdminDN);
		env.put(Context.SECURITY_CREDENTIALS, ldapAdminPassword);

		DirContext ctx = new InitialDirContext(env);
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String attrList[] = {"uid","displayName"};
		constraints.setReturningAttributes(attrList);

		NamingEnumeration answer = ctx.search(ldapTestersDN,"(objectClass=*)",constraints);
		Map<String,Object> entry;
		while (answer.hasMore()) {
			entry=new HashMap<>();
			SearchResult searchResult =(SearchResult)answer.next();
			Attributes attrs = searchResult.getAttributes();
			if (attrs == null) continue;
			NamingEnumeration attrsEnum = attrs.getAll();
			if(!attrsEnum.hasMoreElements()) continue;
			while (attrsEnum.hasMoreElements()) {
				Attribute attr =(Attribute)attrsEnum.next();
				String id = attr.getID();
				entry.put(id , attr.get());
			}
			result.add(entry);
		}

		ctx.close();

		return result;
	}

	static List<Map<String,Object>> fnGetEnvOwners () throws Exception {
		List<Map<String,Object>> result=new ArrayList<>();
		String url = ldapUrlString;
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ldapAdminDN);
		env.put(Context.SECURITY_CREDENTIALS, ldapAdminPassword);

		DirContext ctx = new InitialDirContext(env);
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String attrList[] = {"uid","displayName"};
		constraints.setReturningAttributes(attrList);

		NamingEnumeration answer = ctx.search(ldapOwnersDN,"(objectClass=*)",constraints);
		Map<String,Object> entry;
		while (answer.hasMore()) {
			entry=new HashMap<>();
			SearchResult searchResult =(SearchResult)answer.next();
			Attributes attrs = searchResult.getAttributes();
			if (attrs == null) continue;
			NamingEnumeration attrsEnum = attrs.getAll();
			if(!attrsEnum.hasMoreElements()) continue;
			while (attrsEnum.hasMoreElements()) {
				Attribute attr =(Attribute)attrsEnum.next();
				String id = attr.getID();
				entry.put(id , attr.get());
			}
			result.add(entry);
		}

		ctx.close();

		return result;
	}

	static void fnUpdateEnvironmentDate(Long envId) {
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		try {
			String sql= "UPDATE \"" + schema + "\".environments SET " +
					"environment_last_updated_date=(?),"+
					"environment_last_updated_by=(?) "+
					"WHERE environment_id = " + envId;
			String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			db("TDM").execute(sql,now,username);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	static Long fnAddProcutToEnvironment(long envId, Long product_id, String data_center_name, String product_version) throws Exception {
		String sql = "INSERT INTO \"" + schema + "\".environment_products " +
				"(environment_id, product_id, data_center_name, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING environment_product_id";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
		Db.Row row = db("TDM").fetch(sql,
				envId,
				product_id,
				data_center_name,
				product_version,
				username,
				now,
				now,
				username,
				"Active").firstRow();
		return Long.parseLong(row.cell(0).toString());
	}

	 static void fnAddProductInterfacesEnvironment(Long envId, Long product_id, Long env_product_id, List<Map<String, Object>> interfaces) throws Exception {
		if(interfaces==null) return;

		for (Map<String, Object> _interface : interfaces) {
			//k2viewAPIs.getDataFromAPI("envEncryptDBConnPwd",[{"key" : "password", "value" : interface.db_password"}]
			String decryptPwd = FabricEncryption.decrypt(_interface.get("db_password").toString());
			//replace regular text password with encrypted password
			_interface.put("db_password", decryptPwd);

			String sql = "INSERT INTO \"" + schema + "\".environment_product_interfaces " +
					"(environment_id, product_id, db_host, db_port, db_user, db_password, db_schema, db_connection_string,env_product_interface_id,interface_name,interface_type,env_product_interface_status) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
			db("TDM").execute(sql,
					envId,
					product_id,
					_interface.get("db_host"),
					_interface.get("db_port"),
					_interface.get("db_user"),
					_interface.get("db_password"),
					_interface.get("db_schema"),
					_interface.get("db_connection_string"),
					env_product_id,
					_interface.get("interface_name"),
					_interface.get("interface_type"),
					"Active");
		}
	}

	 static void fnUpdateProductToEnvironment(Long environment_product_id, String data_center_name, String product_version) throws Exception {
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		String sql = "UPDATE \"" + schema + "\".environment_products SET " +
				"data_center_name=(?)," +
				"product_version=(?)," +
				"last_updated_date=(?)," +
				"last_updated_by=(?) " +
				"WHERE environment_product_id = " + environment_product_id;
		 String username=(String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
		db("TDM").execute(sql, data_center_name, product_version, now, username);
	}

	 static void fnUpdateProductInterfacesEnvironment(Long envId, long product_id, Long env_product_id, List<Map<String, Object>> interfaces) throws Exception {
		if(interfaces==null) return;
		for (Map<String, Object> _interface : interfaces) {
			if (_interface.get("update").toString() == "false" || _interface.get("passwordDecrypt").toString()!="false"){
				String decryptPwd = FabricEncryption.decrypt(_interface.get("db_password").toString());
				//replace regular text password with encrypted password
				_interface.put("db_password", decryptPwd);
			}
								//check: env_product_interface_status
			if (_interface.get("newInterface").toString() == "true" && _interface.get("env_product_interface_status").toString() == "null") {
				String sql = "INSERT INTO \"" + schema + "\".environment_product_interfaces " +
						"(environment_id, product_id, db_host, db_port, db_user, db_password, db_schema, db_connection_string,env_product_interface_id,interface_name,interface_type,env_product_interface_status) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
				db("TDM").execute(sql, envId, product_id, _interface.get("db_host"),
						_interface.get("db_port"), _interface.get("db_user"),
						_interface.get("db_password"), _interface.get("db_schema"),
						_interface.get("db_connection_string"), env_product_id,
						_interface.get("interface_name"), _interface.get("interface_type"), "Active");
			} else if (_interface.get("deleted").toString() == "true") {
				String sql = "DELETE FROM \"" + schema + "\".environment_product_interfaces WHERE ( env_product_interface_id = (?) AND interface_name = (?))";
				db("TDM").execute(sql, _interface.get("env_product_interface_id"), _interface.get("interface_name"));

			} else {
				String sql = "UPDATE \"" + schema + "\".environment_product_interfaces SET " +
						"db_host=(?)," +
						"db_port=(?)," +
						"db_user=(?)," +
						"db_password=(?)," +
						"db_schema=(?)," +
						"db_connection_string=(?) " +
						"WHERE env_product_interface_id = " + _interface.get("env_product_interface_id") + " AND interface_name = " + "\"" + _interface.get("interface_name") + "\"";
				db("TDM").execute(sql,
						_interface.get("db_host"), _interface.get("db_port"),
						_interface.get("db_user"), _interface.get("db_password"),
						_interface.get("db_schema"), _interface.get("db_connection_string"));
			}
		}
	}


	static void fnUpdateEnvironmentRolesPermissions(Long environment_id, String type, boolean value){
		try {
			String sql = "UPDATE \"" + schema + "\".environment_roles SET " +
					"" + type + "=(?)" +
					"WHERE environment_id = " + environment_id;
			db("TDM").execute(sql, value);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	static void fnInsertActivity(String action,String entity,String description) throws Exception{
		String userId = (String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
		String username = userId;
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		String sql= "INSERT INTO \"" + schema + "\".activities " +
				"(date, action, entity, user_id, username, description) " +
				"VALUES (?, ?, ?, ?, ?, ?)";
		db("TDM").execute(sql,now,action,entity,userId,username,description);
	}

}
