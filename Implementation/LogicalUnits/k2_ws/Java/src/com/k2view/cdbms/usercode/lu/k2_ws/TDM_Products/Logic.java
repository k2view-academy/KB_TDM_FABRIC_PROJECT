/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM_Products;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import java.lang.Long;

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
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getFabricResponse;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.wrapWebServiceResults;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends WebServiceUserCode {
	final static String schema="public";
	final static String admin_pg_access_denied_msg = "Access Denied. Please login with administrator privileges and try again";

	@desc("Gets all TDM Products (Active and Inactive) to populate Products window")
	@webService(path = "products", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"product_versions\": \"1.0,2.0\",\r\n" +
			"      \"product_last_updated_by\": \"K2View\",\r\n" +
			"      \"product_vendor\": null,\r\n" +
			"      \"product_last_updated_date\": \"2021-04-18 14:49:32.536\",\r\n" +
			"      \"product_id\": 1,\r\n" +
			"      \"product_created_by\": \"K2View\",\r\n" +
			"      \"product_status\": \"Active\",\r\n" +
			"      \"product_creation_date\": \"2021-04-18 09:32:14.981\",\r\n" +
			"      \"product_description\": null,\r\n" +
			"      \"product_name\": \"PROD\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetProducts() throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String errorCode="";
		String message=null;
		
		try{
			String sql = "SELECT * FROM \"" + schema + "\".products";
			Db.Rows rows = db("TDM").fetch(sql);
			List<Map<String,Object>> result=new ArrayList<>();
			Map<String,Object> product;
			for(Db.Row row:rows) {
				product=new HashMap<>();
				product.put("product_name", row.cell(0));
				product.put("product_description", row.cell(1));
				product.put("product_vendor", row.cell(2));
				product.put("product_versions", row.cell(3));
				product.put("product_id",Integer.parseInt(row.cell(4).toString()));
				product.put("product_created_by", row.cell(5));
				product.put("product_creation_date", row.cell(6));
				product.put("product_last_updated_date", row.cell(7));
				product.put("product_last_updated_by", row.cell(8));
				product.put("product_status", row.cell(9));
				result.add(product);
			}
			response.put("result", result);
			errorCode= "SUCCESS";
		
		}
		catch(Exception e){
			errorCode= "FAIL";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets a Product by a product id.")
	@webService(path = "product/{prodId}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"product_versions\": \"1.0,2.0\",\r\n" +
			"    \"product_last_updated_by\": \"K2View\",\r\n" +
			"    \"product_vendor\": null,\r\n" +
			"    \"product_last_updated_date\": \"2021-04-18 14:49:32.536\",\r\n" +
			"    \"product_id\": 1,\r\n" +
			"    \"product_created_by\": \"K2View\",\r\n" +
			"    \"product_status\": \"Active\",\r\n" +
			"    \"product_creation_date\": \"2021-04-18 09:32:14.981\",\r\n" +
			"    \"product_description\": null,\r\n" +
			"    \"product_name\": \"PROD\"\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetProduct(@param(required=true) Long prodId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String errorCode="";
		String message=null;
		
		try{
			String sql = "SELECT * FROM \"" + schema + "\".products " +
					"WHERE \"" + schema + "\".products.product_id = " + prodId;
			Db.Row row = db("TDM").fetch(sql).firstRow();
			Map<String,Object> product;
			if(!row.isEmpty()) {
				product=new HashMap<>();
				product.put("product_name", row.cell(0));
				product.put("product_description", row.cell(1));
				product.put("product_vendor", row.cell(2));
				product.put("product_versions", row.cell(3));
				product.put("product_id",Integer.parseInt(row.cell(4).toString()));
				product.put("product_created_by", row.cell(5));
				product.put("product_creation_date", row.cell(6));
				product.put("product_last_updated_date", row.cell(7));
				product.put("product_last_updated_by", row.cell(8));
				product.put("product_status", row.cell(9));
				response.put("result", product);
			}
			errorCode= "SUCCESS";
		}
		catch(Exception e){
			errorCode= "FAIL";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Creates a TDM Product.\r\n" +
			"\r\n" +
			"Notes:\r\n" +
			"\r\n" +
			"> The product_name and product_versions parameters are mandatory.\r\n" +
			"\r\n" +
			"> At least one version must be set for a product. Multiple product versions can also be set, seperaed by a comma. For example: \"1.5,1.0,2.0\".\r\n" +
			"\r\n" +
			"> Each Active product gets a unique product name.")
	@webService(path = "product", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"id\": 16\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsPostProduct(String product_name, String product_description, String product_vendor, String product_versions) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if (!"admin".equals(permissionGroup)) return wrapWebServiceResults("FAIL",admin_pg_access_denied_msg,null);
		if(product_name==null||product_versions==null) return wrapWebServiceResults("FAIL","product_name and product_versions are mandatory fields.",null);
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		
		try {
			String sql= "INSERT INTO \"" + schema + "\".products " +
					"(product_name, product_description, product_vendor, product_versions, product_created_by, " +
					"product_creation_date, product_last_updated_date, product_last_updated_by, product_status) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING product_id";
			String username = (String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			Db.Row row = db("TDM").fetch(sql,product_name, product_description, product_vendor, product_versions, username, now, now,
					username, "Active").firstRow();
			int prodId = Integer.parseInt(row.cell(0).toString());
		
			String activityDesc = "Product " + product_name + " was created";
			try {
				fnInsertActivity("create", "Product", activityDesc);
			}
			catch(Exception e){
				log.error(e.getMessage());
			}
			HashMap<String,Object> result=new HashMap<>();
			result.put("id",prodId);
			response.put("result",result);
			errorCode="SUCCESS";
		
		}catch(Exception e){
			message=e.getMessage();
			errorCode= "FAIL";
			log.error(message);
		}
		response.put("message",message);
		response.put("errorCode",errorCode);
		return response;
	}


	@desc("Updates the Product's description, vendor, or versions. Product versions are separated by a comma.\r\n" +
			"Example request body:\r\n" +
			"{\r\n" +
			"  \"product_name\": \"PROD\",\r\n" +
			"  \"product_description\": \"desc\",\r\n" +
			"  \"product_vendor\": \"verndor\",\r\n" +
			"  \"product_versions\": \"1.0,2.0\"\r\n" +
			"}")
	@webService(path = "product/{prodId}", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdateProduct(@param(required=true) Long prodId, String product_name, String product_description, String product_vendor, String product_versions) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if (!"admin".equals(permissionGroup)) return wrapWebServiceResults("FAIL",admin_pg_access_denied_msg,null);
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		try {
			String sql = "UPDATE \"" + schema + "\".products SET " +
			"product_name=(?)," +
					"product_description=(?)," +
					"product_vendor=(?)," +
					"product_versions=(?), " +
					"product_last_updated_date=(?)," +
					"product_last_updated_by=(?) " +
					"WHERE product_id = " + prodId;
			String username = (String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			db("TDM").execute(sql, product_name, product_description, product_vendor, product_versions, now, username);
		
			String activityDesc = "Product " + product_name + " was updated";
			try {
				fnInsertActivity("update", "Product", activityDesc);
			}
			catch(Exception e){
				log.error(e.getMessage());
			}
		
			errorCode="SUCCESS";
		
		}catch(Exception e){
			errorCode="FAIL";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("Gets the list of Logical Units related to a given Product.")
	@webService(path = "product/{prodId}/logicalunits", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"lu_parent_id\": 12,\r\n" +
			"      \"be_status\": \"Active\",\r\n" +
			"      \"be_creation_date\": \"date\",\r\n" +
			"      \"be_last_updated_date\": \"date\",\r\n" +
			"      \"lu_dc_name\": \"DC1\",\r\n" +
			"      \"be_created_by\": \"k2view\",\r\n" +
			"      \"product_name\": \"PROD\",\r\n" +
			"      \"be_name\": \"BE\",\r\n" +
			"      \"lu_description\": \"null\",\r\n" +
			"      \"lu_parent_name\": \"parentName\",\r\n" +
			"      \"lu_name\": \"luName\",\r\n" +
			"      \"product_id\": 1,\r\n" +
			"      \"lu_id\": 16,\r\n" +
			"      \"be_description\": \"beDesc\",\r\n" +
			"      \"be_last_updated_by\": \"K2View\"\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetProductLogicalUnits(@param(required=true) Long prodId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String errorCode="";
		String message=null;
		
		try{
			String sql = "SELECT * FROM \"" + schema + "\".product_logical_units " +
			"INNER JOIN \"" + schema + "\".business_entities ON (\"" + schema + "\".product_logical_units.be_id = \"" + schema + "\".business_entities.be_id) " +
					"WHERE product_logical_units.product_id = " + prodId;
			Db.Rows rows = db("TDM").fetch(sql);
		
			List<Map<String,Object>> productLogicalUnits=new ArrayList<>();
			Map<String,Object> productLogicalUnit;
		
			for(Db.Row row:rows) {
				productLogicalUnit=new HashMap<>();
				//product_logical_units
				productLogicalUnit.put("lu_name", row.cell(0));
				productLogicalUnit.put("lu_description", row.cell(1));
				productLogicalUnit.put("be_id", Long.parseLong(row.cell(2).toString()));
				productLogicalUnit.put("lu_parent_id",row.cell(3)!=null? Long.parseLong(row.cell(3).toString()):null);
				productLogicalUnit.put("lu_id", Long.parseLong(row.cell(5).toString()));
				productLogicalUnit.put("product_name", row.cell(6));
				productLogicalUnit.put("lu_parent_name", row.cell(7));
				productLogicalUnit.put("product_id", Long.parseLong(row.cell(8).toString()));
				// Remove ADI Fields
				//productLogicalUnit.put("execution_plan_name", row.cell(9));
				//productLogicalUnit.put("last_executed_lu",row.cell(10)!=null?Boolean.parseBoolean(row.cell(10).toString()):null);
				//productLogicalUnit.put("lu_is_ref",row.cell(4)!=null?Boolean.parseBoolean(row.cell(4).toString()):null);
				productLogicalUnit.put("lu_dc_name", row.cell(11));
				//business_entities
				productLogicalUnit.put("be_name", row.cell(12));
				productLogicalUnit.put("be_description", row.cell(13));
				productLogicalUnit.put("be_id", Long.parseLong(row.cell(14).toString()));
				productLogicalUnit.put("be_created_by", row.cell(15));
				productLogicalUnit.put("be_creation_date", row.cell(16));
				productLogicalUnit.put("be_last_updated_date", row.cell(17));
				productLogicalUnit.put("be_last_updated_by", row.cell(18));
				productLogicalUnit.put("be_status", row.cell(19));
				productLogicalUnits.add(productLogicalUnit);
			}
			errorCode= "SUCCESS";
			response.put("result", productLogicalUnits);
		}
		catch(Exception e){
			errorCode= "FAIL";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets a list of Logical Units (LUs) that are not attached to any TDM Product and therefore can be attached to the given TDM product. This API is called when attaching a combination of a Business Entity (BE) and an LU to a given TDM product.")
	@webService(path = "logicalunitswithoutproduct", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"lu_parent_id\": null,\r\n" +
			"      \"be_status\": \"Active\",\r\n" +
			"      \"be_creation_date\": \"date\",\r\n" +
			"      \"be_last_updated_date\": \"date\",\r\n" +
			"      \"lu_dc_name\": \"dcName\",\r\n" +
			"      \"be_created_by\": \"k2view\",\r\n" +
			"      \"product_name\": \"null\",\r\n" +
			"      \"be_name\": \"BE\",\r\n" +
			"      \"lu_description\": \"description\",\r\n" +
			"      \"lu_parent_name\": \"null\",\r\n" +
			"      \"lu_name\": \"luName\",\r\n" +
			"      \"product_id\": -1,\r\n" +
			"      \"lu_id\": 25,\r\n" +
			"      \"be_description\": \"null\",\r\n" +
			"      \"be_last_updated_by\": \"K2View\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetLogicalUnitsWithoutProduct() throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String errorCode="";
		String message=null;
		
		try{
			String sql = "SELECT * FROM \"" + schema + "\".product_logical_units " +
			"INNER JOIN \"" + schema + "\".business_entities ON (\"" + schema + "\".product_logical_units.be_id = \"" + schema + "\".business_entities.be_id) " +
					"WHERE product_id = -1 AND be_status = 'Active'";
			Db.Rows rows = db("TDM").fetch(sql);
			List<Map<String,Object>> productLogicalUnits=new ArrayList<>();
			Map<String,Object> productLogicalUnit;
		
			for(Db.Row row:rows) {
				//product_logical_units
				productLogicalUnit=new HashMap<>();
				productLogicalUnit.put("lu_name", row.cell(0));
				productLogicalUnit.put("lu_description", row.cell(1));
				productLogicalUnit.put("be_id", Long.parseLong(row.cell(2).toString()));
				productLogicalUnit.put("lu_parent_id",row.cell(3)!=null? Long.parseLong(row.cell(3).toString()):null);
				productLogicalUnit.put("lu_id", Long.parseLong(row.cell(5).toString()));
				productLogicalUnit.put("product_name", row.cell(6));
				productLogicalUnit.put("lu_parent_name", row.cell(7));
				productLogicalUnit.put("product_id", Long.parseLong(row.cell(8).toString()));
				// Remove ADI fields
				//productLogicalUnit.put("execution_plan_name", row.cell(9));
				//productLogicalUnit.put("last_executed_lu",row.cell(10)!=null?Boolean.parseBoolean(row.cell(10).toString()):null);
				//productLogicalUnit.put("lu_is_ref",row.cell(4)!=null? Boolean.parseBoolean(row.cell(4).toString()):null);
				productLogicalUnit.put("lu_dc_name", row.cell(11));
				//business_entities
				productLogicalUnit.put("be_name", row.cell(12));
				productLogicalUnit.put("be_description", row.cell(13));
				productLogicalUnit.put("be_id", Long.parseLong(row.cell(14).toString()));
				productLogicalUnit.put("be_created_by", row.cell(15));
				productLogicalUnit.put("be_creation_date", row.cell(16));
				productLogicalUnit.put("be_last_updated_date", row.cell(17));
				productLogicalUnit.put("be_last_updated_by", row.cell(18));
				productLogicalUnit.put("be_status", row.cell(19));
				productLogicalUnits.add(productLogicalUnit);
			}
			errorCode= "SUCCESS";
			response.put("result", productLogicalUnits);
		}
		catch(Exception e){
			errorCode= "FAIL";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Deletes a Product")
	@webService(path = "product/{prodId}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDeleteProduct(@param(required=true) Long prodId) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if (!"admin".equals(permissionGroup)) return wrapWebServiceResults("FAIL",admin_pg_access_denied_msg,null);
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		
		try {
			String username = (String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
			fnUpdateProductDate(prodId,username);
		} catch(Exception e){
			log.error(e.getMessage());
		}
		
		try {
			String sql= "UPDATE \"" + schema + "\".products  SET " +
					"product_status=(?) " +
					"WHERE product_id = " + prodId + " RETURNING product_name";
			Db.Row row = db("TDM").fetch(sql,"Inactive").firstRow();
			String prodName = row.cell(0).toString();
		
			{
				String updateProductLogicalUnits = "UPDATE \"" + schema + "\".product_logical_units " +
						"SET product_id=(?), product_name=(?) " +
						"WHERE product_id = " + prodId;
				db("TDM").execute(updateProductLogicalUnits,-1,"");
			}
		
			{
				String updateEnvironmentProducts = "UPDATE \"" + schema + "\".environment_products " +
						"SET status=(?) " +
						"WHERE product_id = " + prodId ;
				db("TDM").execute(updateEnvironmentProducts,"Inactive");
			}
		
			/*
			{
				// relation environment_product_interfaces no longer exist
				String updateEnvironmentProductInterfaces = "UPDATE \"" + schema + "\".environment_product_interfaces SET " +
					"env_product_interface_status=(?) " +
					"WHERE product_id = " + prodId;
				db("TDM").execute(updateEnvironmentProductInterfaces,"Inactive");

			}
		
			{
				String updateTasks = "WITH src AS ( " +
				"UPDATE \"" + schema + "\".tasks_products " +
						"SET task_product_status = (?) " +
						"WHERE \"" + schema + "\".tasks_products.product_id = " + prodId + " " +
						"RETURNING \"" + schema + "\".tasks_products.task_id ) " +
						" " +
						"UPDATE \"" + schema + "\".tasks SET task_status = (?) " +
						"FROM ( SELECT \"" + schema + "\".tasks_products.task_id FROM \"" + schema + "\".tasks_products " +
						"WHERE \"" + schema + "\".tasks_products.task_product_status = \'Active\' " +
						"GROUP BY \"" + schema + "\".tasks_products.task_id " +
						"HAVING COUNT(*) = 1 " +
						"INTERSECT " +
						"SELECT \"" + schema + "\".tasks_products.task_id " +
						"FROM \"" + schema + "\".tasks_products " +
						"WHERE \"" + schema + "\".tasks_products.product_id = " + prodId + " ) AS sq " +
						"WHERE \"" + schema + "\".tasks.task_id = sq.task_id AND \"" + schema + "\".tasks.task_status = \'Active\'";
				db("TDM").execute(updateTasks,"Inactive","Inactive");
			}
			 */
		
			try {
				String activityDesc = "Product " + prodName + " was deleted";
				fnInsertActivity("delete", "Data Center", activityDesc);
			}
			catch(Exception e){
				log.error(e.getMessage());
			}
		
			errorCode="SUCCESS";
		} catch(Exception e){
			errorCode="FAIL";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("Gets the list of Active environments with the input Product.")
	@webService(path = "product/{productId}/envcount", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"environment_product_id\": 3,\r\n" +
			"      \"environment_id\": 4,\r\n" +
			"      \"environment_created_by\": \"k2view\",\r\n" +
			"      \"environment_last_updated_by\": \"k2view\",\r\n" +
			"      \"allow_read\": \"t\",\r\n" +
			"      \"environment_description\": \"envDescription\",\r\n" +
			"      \"last_updated_by\": \"k2view\",\r\n" +
			"      \"product_id\": 1,\r\n" +
			"      \"last_updated_date\": \"date\",\r\n" +
			"      \"fabric_environment_name\": \"ENV2\",\r\n" +
			"      \"environment_name\": \"envName\",\r\n" +
			"      \"allow_write\": \"t\",\r\n" +
			"      \"environment_point_of_contact_phone1\": null,\r\n" +
			"      \"product_version\": \"1\",\r\n" +
			"      \"environment_last_updated_date\": \"date\",\r\n" +
			"      \"environment_status\": \"Active\",\r\n" +
			"      \"creation_date\": \"date\",\r\n" +
			"      \"created_by\": \"k2view\",\r\n" +
			"      \"sync_mode\": \"OFF\",\r\n" +
			"      \"environment_point_of_contact_first_name\": null,\r\n" +
			"      \"data_center_name\": \"DC1\",\r\n" +
			"      \"environment_point_of_contact_last_name\": null,\r\n" +
			"      \"environment_point_of_contact_email\": null,\r\n" +
			"      \"environment_creation_date\": \"date\",\r\n" +
			"      \"environment_expiration_date\": null,\r\n" +
			"      \"environment_point_of_contact_phone2\": null,\r\n" +
			"      \"status\": \"Active\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetEnvironmentCountForProduct(@param(description="A unique identifier of the product.", required=true) Long productId) throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String errorCode="";
		String message=null;
		
		try{
			String sql = "SELECT * FROM \"" + schema + "\".environment_products " +
			"INNER JOIN \"" + schema + "\".environments " +
					"ON (\"" + schema + "\".environments.environment_id = \"" + schema + "\".environment_products.environment_id AND \"" + schema + "\".environments.environment_status = \'Active\' )" +
					"WHERE \"" + schema + "\".environment_products.product_id = " +  productId +
					" AND \"" + schema + "\".environment_products.status = 'Active'";
			Db.Rows rows = db("TDM").fetch(sql);
		
			HashMap<String,Object> env;
			List<HashMap<String,Object>> result=new ArrayList<>();
			for(Db.Row row:rows){
				ResultSet resultSet=row.resultSet();
				env=new HashMap<>();
				env.put("environment_product_id",resultSet.getInt("environment_product_id"));
				env.put("environment_id",resultSet.getInt("environment_id"));
				env.put("product_id",resultSet.getInt("product_id"));
				env.put("product_version",resultSet.getString("product_version"));
				env.put("created_by",resultSet.getString("created_by"));
				env.put("creation_date",resultSet.getString("creation_date"));
				env.put("last_updated_date",resultSet.getString("last_updated_date"));
				env.put("last_updated_by",resultSet.getString("last_updated_by"));
				env.put("status",resultSet.getString("status"));
				env.put("data_center_name",resultSet.getString("data_center_name"));
				env.put("environment_name",resultSet.getString("environment_name"));
				env.put("environment_description",resultSet.getString("environment_description"));
				env.put("environment_expiration_date",resultSet.getString("environment_expiration_date"));
				env.put("environment_point_of_contact_first_name",resultSet.getString("environment_point_of_contact_first_name"));
				env.put("environment_point_of_contact_last_name",resultSet.getString("environment_point_of_contact_last_name"));
				env.put("environment_point_of_contact_phone1",resultSet.getString("environment_point_of_contact_phone1"));
				env.put("environment_point_of_contact_phone2",resultSet.getString("environment_point_of_contact_phone2"));
				env.put("environment_point_of_contact_email",resultSet.getString("environment_point_of_contact_email"));
				env.put("environment_created_by",resultSet.getString("environment_created_by"));
				env.put("environment_creation_date",resultSet.getString("environment_creation_date"));
				env.put("environment_last_updated_date",resultSet.getString("environment_last_updated_date"));
				env.put("environment_last_updated_by",resultSet.getString("environment_last_updated_by"));
				env.put("environment_status",resultSet.getString("environment_status"));
				env.put("fabric_environment_name",resultSet.getString("fabric_environment_name"));
				env.put("allow_write",resultSet.getBoolean("allow_write"));
				env.put("allow_read",resultSet.getBoolean("allow_read"));
				env.put("sync_mode",resultSet.getString("sync_mode"));
				result.add(env);
			}
			errorCode= "SUCCESS";
			response.put("result", result);
		}
		catch(Exception e){
			errorCode= "FAIL";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets active Products With at least one LU. This API is called when adding a product to a TDM environment.")
	@webService(path = "productsWithLUs", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"product_versions\": \"1\",\r\n" +
			"      \"product_id\": 15,\r\n" +
			"      \"lus\": 3,\r\n" +
			"      \"product_name\": \"PROD\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"product_versions\": \"1\",\r\n" +
			"      \"product_id\": 16,\r\n" +
			"      \"lus\": 4,\r\n" +
			"      \"product_name\": \"PROD2\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsProductsWithLUs() throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String errorCode="";
		String message=null;
		
		try{
			String sql="SELECT \"" + schema + "\".products.product_id,\"" + schema + "\".products.product_versions,\"" + schema + "\".products.product_name,COUNT(\"" + schema + "\".product_logical_units.lu_id) as lus " +
					"FROM \"" + schema + "\".products " +
					"LEFT JOIN \"" + schema + "\".product_logical_units ON (\"" + schema + "\".product_logical_units.product_id = \"" + schema + "\".products.product_id) " +
					"WHERE \"" + schema + "\".products.product_status = 'Active' " +
					"GROUP BY \"" + schema + "\".products.product_id ";
			Db.Rows rows= db("TDM").fetch(sql);
		
			List<Map<String,Object>> result=new ArrayList<>();
			Map<String,Object> product;
			for(Db.Row row:rows) {
				product=new HashMap<>();
				product.put("product_id", Integer.parseInt(row.cell(0).toString()));
				product.put("product_versions", row.cell(1));
				product.put("product_name",row.cell(2));
				product.put("lus", Integer.parseInt(row.cell(3).toString()));
				result.add(product);
			}
			response.put("result", result);
			errorCode= "SUCCESS";
		
		}
		catch(Exception e){
			errorCode= "FAIL";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	static void fnUpdateProductDate(long prodId,String username) throws Exception{
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());

		String sql = "UPDATE \"" + schema + "\".products SET " +
				"product_last_updated_date=(?)," +
				"product_last_updated_by=(?) " +
				"WHERE product_id = " + prodId;
		db("TDM").execute(sql,now,username);
	}

	static void fnInsertActivity(String action,String entity, String description) throws Exception{
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		String username = (String)((Map)((List) getFabricResponse("set username")).get(0)).get("value");
		String userId = username;
		String sql= "INSERT INTO \"" + schema + "\".activities " +
				"(date, action, entity, user_id, username, description) " +
				"VALUES (?, ?, ?, ?, ?, ?)";
		db("TDM").execute(sql,now,action,entity,userId,username,description);
	}

}
