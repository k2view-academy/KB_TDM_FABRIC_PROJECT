/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM_utils;

import com.k2view.cdbms.FabricEncryption.FabricEncryption;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.resultMetaData;
import com.k2view.fabric.api.endpoint.Endpoint.webService;
import com.k2view.fabric.common.Json;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.*;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.wrapWebServiceResults;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends WebServiceUserCode {

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
			return TdmSharedUtils.wrapWebServiceResults("SUCCESS", "", result);
		} catch (Throwable t) {
			return TdmSharedUtils.wrapWebServiceResults("FAIL", t.getMessage(), null);
		}
	}


	@desc("Get the version of the TDM")
	@webService(path = "tdmVersion", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.XML, example = "<HashMap>\r\n" +
			"  <result>7.3.0</result>\r\n" +
			"  <errorCode>SUCCESS</errorCode>\r\n" +
			"  <message/>\r\n" +
			"</HashMap>")
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": \"7.3.0\",\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetTDMVersion() throws Exception {
		Object tdmVersion =  db("TDM").fetch("select param_value from tdm_general_parameters where param_name = 'TDM_VERSION'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, tdmVersion);
	}

}
