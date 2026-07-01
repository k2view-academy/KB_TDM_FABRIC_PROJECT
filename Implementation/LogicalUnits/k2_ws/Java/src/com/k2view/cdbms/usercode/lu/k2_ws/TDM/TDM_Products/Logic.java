/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_Products;

import com.k2view.cdbms.interfaces.FabricInterface;
import com.k2view.cdbms.lut.InterfacesManager;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_Environments.EnvironmentUtils;
import com.k2view.fabric.api.endpoint.Endpoint.*;

import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnIsOwner;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getAllSuppressedInterfaces;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserPermissionGroup;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnIsOwner;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;

@SuppressWarnings({"DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {
	public static final String TDM = "TDM";
	final static String schema = TDMDB_SCHEMA;
	final static String admin_pg_access_denied_msg = "Access Denied. Please login with administrator privileges and try again";

	@desc("Gets all TDM System (products), Active and Inactive, to populate Systems window")
	@webService(path = "products", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
			  "result": [
			    {
			      "product_id": 1,
			      "product_name": "PROD",
			      "product_description": null,
			      "product_vendor": null,
			      "product_versions": "1.0,2.0",
			      "related_interfaces": ["BILLING_DB", "CRM_DB"],
			      "product_status": "Active",
			      "product_created_by": "K2View",
			      "product_creation_date": "2021-04-18 09:32:14.981",
			      "product_last_updated_date": "2021-04-18 14:49:32.536",
			      "product_last_updated_by": "K2View"
			    }
			  ],
			  "errorCode": "SUCCESS",
			  "message": null
			}
			""")
	public static Object wsGetProducts() throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;

		try {			
			String sql = """
					SELECT * FROM %s.products
					""".formatted(schema);

			Db.Rows rows = db(TDM).fetch(sql);
			List<Map<String, Object>> result = new ArrayList<>();

			try {
				for (Db.Row row : rows) {
					Map<String, Object> product = new HashMap<>();
					product.put("product_id", Integer.parseInt(row.get("product_id").toString()));
					product.put("product_name", row.get("product_name"));
					product.put("product_description", row.get("product_description"));
					product.put("product_vendor", row.get("product_vendor"));
					product.put("product_versions", row.get("product_versions"));					
					product.put("related_interfaces", row.get("related_interfaces"));
					product.put("product_created_by", row.get("product_created_by"));
					product.put("product_creation_date", row.get("product_creation_date"));
					product.put("product_last_updated_date", row.get("product_last_updated_date"));
					product.put("product_last_updated_by", row.get("product_last_updated_by"));
					product.put("product_status", row.get("product_status"));

					result.add(product);
				}
			} finally {
				if (rows != null)
					rows.close();
			}

			response.put("result", result);
			errorCode = "SUCCESS";

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("""
			Gets a System (product) by a product id.
			""")
	@webService(path = "product/{prodId}", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
			  "result": {
			    "product_id": 1,
			    "product_name": "PROD",
			    "product_description": null,
			    "product_vendor": null,
			    "product_versions": "1.0,2.0",
			    "related_interfaces": ["BILLING_DB", "CRM_DB"],
			    "product_status": "Active",
			    "product_created_by": "K2View",
			    "product_creation_date": "2021-04-18 09:32:14.981",
			    "product_last_updated_date": "2021-04-18 14:49:32.536",
			    "product_last_updated_by": "K2View"
			  },
			  "errorCode": "SUCCESS",
			  "message": null
			}
			""")
	public static Object wsGetProduct(@param(required = true) Long prodId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;

		try {			
			String sql = """
					SELECT * FROM %s.products
					WHERE product_id = (?)
					""".formatted(schema);

			Db.Row row = db(TDM).fetch(sql, prodId).firstRow();

			if (!row.isEmpty()) {
				Map<String, Object> product = new HashMap<>();
				product.put("product_id", Integer.parseInt(row.get("product_id").toString()));
				product.put("product_name", row.get("product_name"));
				product.put("product_description", row.get("product_description"));
				product.put("product_vendor", row.get("product_vendor"));
				product.put("product_versions", row.get("product_versions"));				
				product.put("related_interfaces", row.get("related_interfaces"));
				product.put("product_created_by", row.get("product_created_by"));
				product.put("product_creation_date", row.get("product_creation_date"));
				product.put("product_last_updated_date", row.get("product_last_updated_date"));
				product.put("product_last_updated_by", row.get("product_last_updated_by"));
				product.put("product_status", row.get("product_status"));

				response.put("result", product);
			}
			errorCode = "SUCCESS";
		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("""
			Creates a TDM System (product).

			Notes:
			> The product_name and product_versions parameters are mandatory.
			> At least one version must be set for a product. Multiple versions can be separated by a comma.
			  Example: "1.5,1.0,2.0".
			> Each Active product gets a unique product name.
			> related_interfaces is an optional list of external systems.
			""")
	@webService(path = "product", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
			  "result": {
			    "id": 16
			  },
			  "errorCode": "SUCCESS",
			  "message": null
			}
			""")
	public static Object wsPostProduct(String product_name, String product_description, String product_vendor,
			String product_versions, List<String> related_interfaces) throws Exception {
		String permissionGroup = fnGetUserPermissionGroup("");
		if (!"admin".equals(permissionGroup))
			return wrapWebServiceResults("FAILED", admin_pg_access_denied_msg, null);

		if (product_name == null || product_versions == null) {
			return wrapWebServiceResults("FAILED", "product_name and product_versions are mandatory fields.", null);
		}

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());

		try {			
			String sql = """
					INSERT INTO %s.products (
					    product_name,
					    product_description,
					    product_vendor,
					    product_versions,
					    product_created_by,
					    product_creation_date,
					    product_last_updated_date,
					    product_last_updated_by,
					    product_status,
					    related_interfaces
					)
					VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					RETURNING product_id
					""".formatted(schema);

			String username = sessionUser().name();
			
			Object interfacesArray = (related_interfaces != null)
					? related_interfaces.toArray(new String[0])
					: new String[0];

			Db.Row row = db(TDM).fetch(sql,
					product_name,
					product_description,
					product_vendor,
					product_versions,
					username,
					now,
					now,
					username,
					"Active",
					interfacesArray).firstRow();

			int prodId = Integer.parseInt(row.get("product_id").toString());

			String activityDesc = "System " + product_name + " was created";
			try {
				fnInsertActivity("create", "Systems", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}

			HashMap<String, Object> result = new HashMap<>();
			result.put("id", prodId);
			response.put("result", result);
			errorCode = "SUCCESS";

		} catch (Exception e) {
			message = e.getMessage();
			errorCode = "FAILED";
			log.error(message);
		}

		response.put("message", message);
		response.put("errorCode", errorCode);
		return response;
	}


	@desc("""
			Updates the System's (product) description, vendor, versions, and related interfaces.
			The versions are separated by a comma.

			Example request body:
			{
			  "product_name": "PROD",
			  "product_description": "Detailed description",
			  "product_vendor": "VendorName",
			  "product_versions": "1.0,2.0",
			  "related_interfaces": ["BILLING_DB", "CRM_SYSTEM"]
			}
			""")
	@webService(path = "product/{prodId}", verb = {
			MethodType.PUT }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdateProduct(@param(required = true) Long prodId, String product_name,
			String product_description, String product_vendor, String product_versions, List<String> related_interfaces)
			throws Exception {
		String permissionGroup = fnGetUserPermissionGroup("");
		if (!"admin".equals(permissionGroup))
			return wrapWebServiceResults("FAILED", admin_pg_access_denied_msg, null);
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		try {
			String sql = "UPDATE " + schema + ".products SET " +
					"product_name=(?)," +
					"product_description=(?)," +
					"product_vendor=(?)," +
					"product_versions=(?), " +
					"product_last_updated_date=(?)," +
					"product_last_updated_by=(?), " +
					"related_interfaces=(?) " +
					"WHERE product_id = ?";
			String username = sessionUser().name();
			Object interfacesArray = (related_interfaces != null) ? related_interfaces.toArray(new String[0])
					: new String[0];
			db(TDM).execute(sql, product_name, product_description, product_vendor, product_versions, now, username,
					interfacesArray, prodId);

			String activityDesc = "System " + product_name + " was updated";
			try {
				fnInsertActivity("update", "Systems", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}

			errorCode = "SUCCESS";

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets the list of Logical Units related to a given System (product).")
	@webService(path = "product/{prodId}/logicalunits", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"lu_parent_id\": 12,\r\n" +
			"      \"be_status\": \"Active\",\r\n" +
			"      \"be_creation_date\": \"date\",\r\n" +
			"      \"be_last_updated_date\": \"date\",\r\n" +
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
			String sql = "SELECT * FROM " + schema + ".product_logical_units p " +
			"INNER JOIN " + schema + ".business_entities b ON (p.be_id = b.be_id) " +
					"WHERE p.product_id = " + prodId;
			Db.Rows rows = db(TDM).fetch(sql);
		
			List<Map<String,Object>> productLogicalUnits=new ArrayList<>();
			Map<String,Object> productLogicalUnit;
		
			for(Db.Row row:rows) {
				productLogicalUnit=new HashMap<>();
		
				//product_logical_units
				productLogicalUnit.put("lu_name", row.get("lu_name"));
				productLogicalUnit.put("lu_description", row.get("lu_description"));
				productLogicalUnit.put("be_id", Long.parseLong(row.get("be_id").toString()));
				productLogicalUnit.put("lu_parent_id",row.get("lu_parent_id")!=null? Long.parseLong(row.get("lu_parent_id").toString()):null);
				productLogicalUnit.put("lu_id", Long.parseLong(row.get("lu_id").toString()));
				productLogicalUnit.put("product_name", row.get("product_name"));
				productLogicalUnit.put("lu_parent_name", row.get("lu_parent_name"));
				productLogicalUnit.put("product_id", Long.parseLong(row.get("product_id").toString()));
				//business_entities
				productLogicalUnit.put("be_name", row.get("be_name"));
				productLogicalUnit.put("be_description", row.get("be_description"));
				productLogicalUnit.put("be_id", Long.parseLong(row.get("be_id").toString()));
				productLogicalUnit.put("be_created_by", row.get("be_created_by"));
				productLogicalUnit.put("be_creation_date", row.get("be_creation_date"));
				productLogicalUnit.put("be_last_updated_date", row.get("be_last_updated_date"));
				productLogicalUnit.put("be_last_updated_by", row.get("be_last_updated_by"));
				productLogicalUnit.put("be_status", row.get("be_status"));
				productLogicalUnits.add(productLogicalUnit);
			}
			errorCode= "SUCCESS";
			response.put("result", productLogicalUnits);
			if (rows != null) {
				rows.close();
			}
		}
		catch(Exception e){
			errorCode= "FAILED";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Gets a list of Logical Units (LUs) that are not attached to any TDM System (product) and available to be attached to the given TDM system. This API is called when attaching a combination of a Business Entity (BE) and an LU to a given TDM system.")
	@webService(path = "logicalunitswithoutproduct", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"be_id\": 1,\r\n" +
			"      \"lu_parent_id\": null,\r\n" +
			"      \"be_status\": \"Active\",\r\n" +
			"      \"be_creation_date\": \"date\",\r\n" +
			"      \"be_last_updated_date\": \"date\",\r\n" +
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
			String sql = "SELECT * FROM " + schema + ".product_logical_units p " +
			"INNER JOIN " + schema + ".business_entities b ON (p.be_id = b.be_id) " +
					"WHERE product_id = -1 AND be_status = 'Active'";
			Db.Rows rows = db(TDM).fetch(sql);
			List<Map<String,Object>> productLogicalUnits=new ArrayList<>();
			Map<String,Object> productLogicalUnit;
		
			for(Db.Row row:rows) {
				//product_logical_units
				productLogicalUnit=new HashMap<>();
				productLogicalUnit.put("lu_name", row.get("lu_name"));
				productLogicalUnit.put("lu_description", row.get("lu_description"));
				productLogicalUnit.put("be_id", Long.parseLong(row.get("be_id").toString()));
				productLogicalUnit.put("lu_parent_id",row.get("lu_parent_id")!=null? Long.parseLong(row.get("lu_parent_id").toString()):null);
				productLogicalUnit.put("lu_id", Long.parseLong(row.get("lu_id").toString()));
				productLogicalUnit.put("product_name", row.get("product_name"));
				productLogicalUnit.put("lu_parent_name", row.get("lu_parent_name"));
				productLogicalUnit.put("product_id", Long.parseLong(row.get("product_id").toString()));
		
				//business_entities
				productLogicalUnit.put("be_name", row.get("be_name"));
				productLogicalUnit.put("be_description", row.get("be_description"));
				productLogicalUnit.put("be_id", Long.parseLong(row.get("be_id").toString()));
				productLogicalUnit.put("be_created_by", row.get("be_created_by"));
				productLogicalUnit.put("be_creation_date", row.get("be_creation_date"));
				productLogicalUnit.put("be_last_updated_date", row.get("be_last_updated_date"));
				productLogicalUnit.put("be_last_updated_by", row.get("be_last_updated_by"));
				productLogicalUnit.put("be_status", row.get("be_status"));
				productLogicalUnits.add(productLogicalUnit);
			}
			errorCode= "SUCCESS";
			response.put("result", productLogicalUnits);
			if (rows != null) {
				rows.close();
			}
		}
		catch(Exception e){
			errorCode= "FAILED";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Deletes a System (product)")
	@webService(path = "product/{prodId}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDeleteProduct(@param(required=true) Long prodId) throws Exception {
		String permissionGroup = fnGetUserPermissionGroup("");
		if (!"admin".equals(permissionGroup)) return wrapWebServiceResults("FAILED",admin_pg_access_denied_msg,null);
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
		
		try {
			String username = sessionUser().name();
			fnUpdateProductDate(prodId,username);
		} catch(Exception e){
			log.error(e.getMessage());
		}
		
		try {
			String sql= "UPDATE " + schema + ".products  SET " +
					"product_status=(?) " +
					"WHERE product_id = " + prodId + " RETURNING product_name";
			Db.Row row = db(TDM).fetch(sql,"Inactive").firstRow();
			String prodName = row.get("product_name").toString();
		
			{
				String updateProductLogicalUnits = "UPDATE " + schema + ".product_logical_units " +
						"SET product_id=(?), product_name=(?) " +
						"WHERE product_id = " + prodId;
				db(TDM).execute(updateProductLogicalUnits,-1,"");
			}
		
			{
				String updateEnvironmentProducts = "UPDATE " + schema + ".environment_products " +
						"SET status=(?) " +
						"WHERE product_id = " + prodId ;
				db(TDM).execute(updateEnvironmentProducts,"Inactive");
			}
		
			try {
				String activityDesc = "System " + prodName + " was deleted";
				fnInsertActivity("delete", "Systems", activityDesc);
			}
			catch(Exception e){
				log.error(e.getMessage());
			}
		
			errorCode="SUCCESS";
		} catch(Exception e){
			errorCode="FAILED";
			message=e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message",message);
		return response;
	}


	@desc("Gets the list of Active environments with the input System (product).")
	@webService(path = "product/{productId}/envcount", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
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
			String sql = "SELECT * FROM " + schema + ".environment_products p " +
			"INNER JOIN " + schema + ".environments e " +
					"ON (e.environment_id = p.environment_id AND e.environment_status = \'Active\' )" +
					"WHERE p.product_id = " +  productId +
					" AND p.status = 'Active'";
			Db.Rows rows = db(TDM).fetch(sql);
		
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
				env.put("allow_write",resultSet.getBoolean("allow_write"));
				env.put("allow_read",resultSet.getBoolean("allow_read"));
				env.put("sync_mode",resultSet.getString("sync_mode"));
				result.add(env);
			}
			errorCode= "SUCCESS";
			response.put("result", result);
			if (rows != null) {
				rows.close();
			}
		}
		catch(Exception e){
			errorCode= "FAILED";
			message= e.getMessage();
			log.error(message);
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Gets active Systems that have at least one LU or at least one related Interface in the product metadata.")
	@webService(path = "product/{envId}/loadAvailableSystems", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = {
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
				{
			"result": [
			  {
				"product_versions": "1",
				"product_id": 1,
				"related_interfaces": "{BILLING_DB,CRM_DB}",
				"lus": 2,
				"product_name": "CRM"
			  },
			  {
				"product_versions": "PROD",
				"product_id": 2,
				"related_interfaces": "{}",
				"lus": 2,
				"product_name": "Billing"
			  },
			  {
				"product_versions": "TEST",
				"product_id": 3,
				"related_interfaces": "{BILLING_DB,CRM_DB}",
				"lus": 0,
				"product_name": "CRM2"
			  }
			],
			"errorCode": "SUCCESS",
			"message": null
			 }
				""")

	public static Object wsLoadAvailableSystems(@param(required = true) Long envId) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();
		String errorCode = "SUCCESS";
		String message = null;

		String checkEnvSql = "SELECT 1 FROM " + schema + ".environments WHERE environment_id = ?";
		Db.Row envRow = db(TDM).fetch(checkEnvSql, envId).firstRow();

		if (envRow.isEmpty()) {
			return wrapWebServiceResults("FAILED", "Environment ID " + envId + " not found.", null);
		}

		try {
			String sql = "SELECT p.product_id, p.product_versions, p.product_name, p.related_interfaces, " +
					"COUNT(lu.lu_id) as lu_count " +
					"FROM " + schema + ".products p " +
					"LEFT JOIN " + schema + ".product_logical_units lu ON p.product_id = lu.product_id " +
					"WHERE p.product_status = 'Active' " +
					"GROUP BY p.product_id, p.product_versions, p.product_name, p.related_interfaces";

			Db.Rows rows = db(TDM).fetch(sql);

			for (Db.Row row : rows) {
				int luCount = Integer.parseInt(row.get("lu_count").toString());

				// Logic to check if the related_interfaces field has data
				Object interfacesObj = row.get("related_interfaces");
				String interfacesStr = (interfacesObj != null) ? interfacesObj.toString().trim() : "";
				// Checks if it's not null, not empty, and not just an empty JSON array "[]"
				boolean hasInterfaces = !interfacesStr.isEmpty() && !interfacesStr.equals("{}");

				// Filter: Return only if it has LUs OR has Interfaces
				if (luCount > 0 || hasInterfaces) {
					Map<String, Object> product = new HashMap<>();
					product.put("product_id", Integer.parseInt(row.get("product_id").toString()));
					product.put("product_name", row.get("product_name"));

					// Versioning logic based on envId
					if (envId != null && envId == -1) {
						product.put("product_versions", "Synthetic");
					} else if (envId != null && envId == -2) {
						product.put("product_versions", "AI");
					} else {
						product.put("product_versions", row.get("product_versions"));
					}

					product.put("lus", luCount);
					product.put("related_interfaces", interfacesStr);

					result.add(product);
				}
			}

			if (rows != null)
				rows.close();

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error("Error in wsLoadAvailableSystems: " + message);
		}

		response.put("result", result);
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	static void fnUpdateProductDate(long prodId,String username) throws Exception{
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());

		String sql = "UPDATE " + schema + ".products SET " +
				"product_last_updated_date=(?)," +
				"product_last_updated_by=(?) " +
				"WHERE product_id = " + prodId;
		db(TDM).execute(sql,now,username);
	}

	static void fnInsertActivity(String action,String entity, String description) throws Exception{
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());
		String username = sessionUser().name();
		String userId = username;
		String sql= "INSERT INTO " + schema + ".activities " +
				"(date, action, entity, user_id, username, description) " +
				"VALUES (?, ?, ?, ?, ?, ?)";
		db(TDM).execute(sql,now,action,entity,userId,username,description);
	}
	
	@webService(path = "product/{envId}/{productId}/DisableEnvironmentProduct", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	public static Object wsDisableEnvironmentProduct(@param(required=true) Long productId,Long envId,Long envProdcutID,String envName) throws Exception {
		String permissionGroup = fnGetUserPermissionGroup("");
		if(permissionGroup==null) return wrapWebServiceResults("FAILED", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAILED", "You have a Tester permission group and therefore are not allowed to disable environment products.", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) 	
					return wrapWebServiceResults("FAILED", "You are not the owner of this environment and therefore are not allowed to disable its products.", null);
			}
		}		
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "UPDATE " + schema + ".environment_products SET " +
					"enable_product=? WHERE environment_product_id = ? ";
			db(TDM).execute(sql, "false",envProdcutID);

			String activityDesc = "'Environment System " + envProdcutID + " was disbaled in environment " + envName ;

			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		
			errorCode = "SUCCESS";
		
		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}
	
	@webService(path = "product/{envId}/{productId}/EnableEnvironmentProduct", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	public static Object wsEnableEnvironmentProduct(@param(required=true) Long productId,Long envId,Long envProdcutID,String envName) throws Exception {
		String permissionGroup = fnGetUserPermissionGroup("");
		if(permissionGroup==null) return wrapWebServiceResults("FAILED", "Can't find a permission group for the user", null);
		if (!"admin".equals(permissionGroup)) {
			if ("tester".equals(permissionGroup)) {
				return wrapWebServiceResults("FAILED", "You have a Tester permission group and therefore are not allowed to disable environment products.", null);
			} else if("owner".equals(permissionGroup)){
				if(!fnIsOwner(envId.toString())) 	
					return wrapWebServiceResults("FAILED", "You are not the owner of this environment and therefore are not allowed to disable its products.", null);
			}
		}
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			String sql = "UPDATE " + schema + ".environment_products SET " +
					"enable_product=? WHERE environment_product_id = ?";
			db(TDM).execute(sql, "true",envProdcutID);

			String activityDesc = "'Environment System " + envProdcutID + " was enabled in environment " + envName ;

			try {
				fnInsertActivity("update", "Environments", activityDesc);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		
			errorCode = "SUCCESS";
		
		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Gets all active interfaces that are not suppressed in the configuration table")
	@webService(path = "interfaces", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
						{
			  "result": [
			    "TAR_CRM_DB",
			    "TAR_ORDERS_DB",
			    "ORDERS_DB",
			    "BILLING_DB",
			    "TAR_COLLECTION_DB",
			    "COLLECTION_DB",
			    "TAR_BILLING_DB",
			    "CRM_DB"
			  ],
			  "errorCode": "SUCCESS",
			  "message": null
			}
						""")
	public static Object wsGetActiveInterfaces() throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String errorCode = "";
		String message = null;

		try {
			Set<FabricInterface> interfaces = InterfacesManager.getInstance().getAllInterfaces();
			List<String> result = new ArrayList<>();
			Set<String> suppressedInterfaces = getAllSuppressedInterfaces();
			for (FabricInterface interfaceRec : interfaces) {
					String interfaceName = interfaceRec.getName();
				if (interfaceRec.getActiveMode() && !suppressedInterfaces.contains(interfaceName)) {
					result.add(interfaceName);
				}
			}
			response.put("result", result);
			errorCode = "SUCCESS";

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

}
