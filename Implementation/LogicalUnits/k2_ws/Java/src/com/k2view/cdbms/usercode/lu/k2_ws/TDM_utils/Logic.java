/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM_utils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.naming.*;
import javax.naming.directory.*;
import javax.validation.constraints.Null;

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
import com.k2view.fabric.common.Json;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.wrapWebServiceResults;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends WebServiceUserCode {

	static String ldapUrlString = "ldap://62.90.46.136:10389";
	static String ldapAdminDN = "uid=tdmldap,ou=users,ou=system";
	static String ldapAdminPassword = "Q1w2e3r4t5";
	static String ldapOwnersDN = "ou=k2venvownerg,ou=system";
	static String ldapTestersDN = "ou=k2vtestg,ou=system";
	static String ldapOwnersGroupName = "k2venvownerg";
	static String ldapTestersGroupName = "k2vtestg";
	static String ldapBaseCN = "DC=training,DC=k2view,DC=com";


	@desc("Get DB TimeZone")
	@webService(path = "dbtimezone", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"current_setting\": \"UTC\"\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetDBTimeZone() throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String errorCode="";
		String message=null;
		try{
			Db.Row row = db("TDM").fetch("SELECT  current_setting(\'TIMEZONE\')").firstRow();
			errorCode="SUCCESS";
			response.put("result",row);
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}


	@desc("Get owners")
	@webService(path = "owners", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"uid\": \"id\",\r\n" +
			"      \"user_id\": \"userId\",\r\n" +
			"      \"displayName\": \"displayName\",\r\n" +
			"      \"username\": \"username\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"uid\": \"id\",\r\n" +
			"      \"user_id\": \"userId\",\r\n" +
			"      \"displayName\": \"displayName\",\r\n" +
			"      \"username\": \"username\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetOwners() throws Exception {
		HashMap<String,Object> response=new HashMap<>();
		String errorCode="";
		String message=null;
		try{
			List<Map<String,Object>> envOwners =  fnGetEnvOwners();
		
			for(Map<String,Object> envOwner:envOwners) {
				envOwner.put("user_id", envOwner.get("uid"));
				envOwner.put("username", envOwner.get("displayName"));
			}
			response.put("result",envOwners);
			errorCode="SUCCESS";
		} catch(Exception e){
			message=e.getMessage();
			errorCode="FAIL";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
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

	//from logic.TDM
	@desc("Gets a decrypted password.")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	public static String wsDecryptPwd(String passTodDcrypt) throws Exception {
		//log.info("TEST-SHAI-Input pwd: "+ passTodDcrypt);
		String decryptPwd = FabricEncryption.decrypt(passTodDcrypt);
		
		//log.info("TEST-SHAI-after calling Fabric: "+ decryptPwd);
		
		return decryptPwd;
		//FabricEncryption.decrypt(passTodDcrypt);
	}


	//end tdm

	@desc("This API provides configuration for TDM GUI")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"maxRetentionPeriod\": 90,\r\n" +
			"    \"defaultPeriod\": {\r\n" +
			"      \"unit\": \"Days\",\r\n" +
			"      \"value\": 5\r\n" +
			"    },\r\n" +
			"    \"permissionGroups\": [\r\n" +
			"      \"admin\",\r\n" +
			"      \"owner\",\r\n" +
			"      \"tester\"\r\n" +
			"    ],\r\n" +
			"    \"availableOptions\": [\r\n" +
			"      {\r\n" +
			"        \"name\": \"Minutes\",\r\n" +
			"        \"units\": 0.00069444444\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"name\": \"Hours\",\r\n" +
			"        \"units\": 0.04166666666\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"name\": \"Days\",\r\n" +
			"        \"units\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"name\": \"Weeks\",\r\n" +
			"        \"units\": 7\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"name\": \"Years\",\r\n" +
			"        \"units\": 365\r\n" +
			"      }\r\n" +
			"    ]\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": \"\"\r\n" +
			"}")
	public static Object getTdmGuiParams() throws Exception {
		try {
			String sql = "select * from \"public\".tdm_general_parameters where tdm_general_parameters.param_name = 'tdm_gui_params'";
			Object params = db("TDM").fetch(sql).firstRow().get("param_value");
			Map result = Json.get().fromJson((String) params, Map.class);
			return wrapWebServiceResults("SUCCESS", "", result);
		} catch (Throwable t) {
			return wrapWebServiceResults("FAIL", t.getMessage(), null);
		}
	}

}
