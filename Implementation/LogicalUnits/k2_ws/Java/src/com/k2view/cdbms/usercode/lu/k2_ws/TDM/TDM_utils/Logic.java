/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_utils;

//import com.k2view.cdbms.FabricEncryption.FabricEncryption;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.GlobalProperties;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.resultMetaData;
import com.k2view.fabric.api.endpoint.Endpoint.webService;
import com.k2view.fabric.common.Json;

import java.util.HashMap;
import java.util.Map;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;

import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetRetentionPeriod;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;



@SuppressWarnings({"DefaultAnnotationParam", "rawtypes"})
public class Logic extends WebServiceUserCode {
	
	public static final String TDM = "TDM";
	
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
			Db.Row row = db(TDM).fetch("SELECT  current_setting(\'TIMEZONE\')").firstRow();
			errorCode="SUCCESS";
			response.put("result",row);
		} catch(Exception e){
			message=e.getMessage();
			log.error(message);
			errorCode="FAILED";
		}
		response.put("errorCode",errorCode);
		response.put("message", message);
		return response;
	}

	//from logic.TDM
//	@desc("Gets a decrypted password.")
//	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
//	public static String wsDecryptPwd(String passTodDcrypt) throws Exception {
//		//log.info("TEST-SHAI-Input pwd: "+ passTodDcrypt);
//		String decryptPwd = FabricEncryption.decrypt(passTodDcrypt);
//		
//		//log.info("TEST-SHAI-after calling Fabric: "+ decryptPwd);
//		
//		return decryptPwd;
//		//FabricEncryption.decrypt(passTodDcrypt);
//	}


	//end tdm

	@desc("This API provides configuration for TDM GUI")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"maxRetentionPeriod\": 90,\r\n" +
			"    \"retentionDefaultPeriod\": {\r\n" +
			"      \"unit\": \"Do Not Delete\",\r\n" +
			"      \"value\": 0\r\n" +
			"    },\r\n" +
			"    \"maxReservationPeriod\": 90,\r\n" +
			"    \"reservationDefaultPeriod\": {\r\n" +
			"      \"unit\": \"Days\",\r\n" +
			"      \"value\": 5\r\n" +
			"    },\r\n" +
			"    \"versioningRetentionPeriod\": {\r\n" +
			"      \"unit\": \"Days\",\r\n" +
			"      \"value\": 5,\r\n" +
			"      \"allow_doNotDelete\": true\r\n" +
			"    },\r\n" +
			"    \"retentionPeriodForTesters\": {\r\n" +
			"      \"unit\": \"Days\",\r\n" +
			"      \"value\": 5,\r\n" +
			"      \"allow_doNotDelete\": false\r\n" +
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
			"      },\r\n" +
			"      {\r\n" +
			"        \"name\": \"Do Not Delete\",\r\n" +
			"        \"units\": -1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"name\": \"Do Not Retain\",\r\n" +
			"        \"units\": 0\r\n" +
			"      }\r\n" +
			"    ],\r\n" +
			"    \"enable_reserve_by_params\": false\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": \"\"\r\n" +
			"}")
	public static Object getTdmGuiParams() throws Exception {
		try {
			String sql = "select * from " + TDMDB_SCHEMA + ".tdm_general_parameters where tdm_general_parameters.param_name = 'tdm_gui_params'";
			Object params = db(TDM).fetch(sql).firstRow().get("param_value");
			Map result = Json.get().fromJson((String) params, Map.class);
			return wrapWebServiceResults("SUCCESS", "", result);
		} catch (Throwable t) {
			return wrapWebServiceResults("FAILED", t.getMessage(), null);
		}
	}


	@desc("Get the version of the TDM")
	@webService(path = "tdmVersion", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
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
		Object tdmVersion =  db(TDM).fetch("select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'TDM_VERSION'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, tdmVersion);
	}
	public static Object wsGetTDMFooterText() throws Exception {
		Object tdmVersion =  db(TDM).fetch("select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'FOOTER_TEXT'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, tdmVersion);
	}

    public static Object wsGetParamsAutoWidth() throws Exception {
		Object paramWidth =  db(TDM).fetch("select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'ENABLE_PARAMETERS_AUTO_WIDTH'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, paramWidth);
	}

	public static Object wsCheckIfParamsCoupling() throws Exception {
		Object isCoupling =  db(TDM).fetch("select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'PARAMS_COUPLING'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, isCoupling);
	}

    public static Object wsGetParamsLUName() throws Exception {
		Object paramLuName =  db(TDM).fetch("select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'ADD_LU_NAME_TO_PARAM_NAME'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, paramLuName);
	}
	
	public static Object wsGetTaskLuEditForTesters() throws Exception {
		Object editIndicator =  db(TDM).fetch("select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'ENABLE_TASK_LU_EDITING_FOR_TESTERS'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, editIndicator);
	}

	@desc("This API provides Retention Period Info for TDM GUI")
	@webService(path = "retentionperiodinfo", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example ="{\n" +
			"    \"result\": {\n" +
			"        \"reservationDefaultPeriod\": {\n" +
			"            \"unit\": \"Days\",\n" +
			"            \"value\": 5\n" +
			"        },\n" +
			"        \"retentionDefaultPeriod\": {\n" +
			"            \"unit\": \"Do Not Delete\",\n" +
			"            \"value\": -1\n" +
			"        },\n" +
			"        \"retentionPeriodTypes\": [\n" +
			"            {\n" +
			"                \"name\": \"Minutes\",\n" +
			"                \"units\": 6.9444444E-4\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Hours\",\n" +
			"                \"units\": 0.04166666666\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Days\",\n" +
			"                \"units\": 1\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Weeks\",\n" +
			"                \"units\": 7\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Years\",\n" +
			"                \"units\": 365\n" +
			"            }\n" +
			"        ],\n" +
			"       \"reservationPeriodTypes\": [\n" +
			"            {\n" +
			"                \"name\": \"Minutes\",\n" +
			"                \"units\": 6.9444444E-4\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Hours\",\n" +
			"                \"units\": 0.04166666666\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Days\",\n" +
			"                \"units\": 1\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Weeks\",\n" +
			"                \"units\": 7\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"Years\",\n" +
			"                \"units\": 365\n" +
			"            }\n" +
			"        ],\n" +
			"        \"versioningRetentionPeriod\": {\n" +
			"            \"unit\": \"Days\",\n" +
			"            \"value\": 5,\n" +
			"            \"allow_doNotDelete\": true\n" +
			"        },\n" +
			"        \"versioningRetentionPeriodForTesters\": {\n" +
			"            \"unit\": \"Days\",\n" +
			"            \"value\": 5,\n" +
			"            \"allow_doNotDelete\": false\n" +
			"        },\n" +
			"        \"maxRetentionPeriodForTesters\": {\n" +
			"            \"units\": \"Days\",\n" +
			"            \"value\": 90\n" +
			"        },\n" +
			"        \"maxReservationPeriodForTesters\": {\n" +
			"            \"units\": \"Days\",\n" +
			"            \"value\": 10\n" +
			"        }\n" +
			"    },\n" +
			"    \"errorCode\": \"SUCCESS\",\n" +
			"    \"message\": \"\"\n" +
			"}\n")
	public static Object wsGetRetentionPeriodInfo() throws Exception {
		Map<String, Object> map;
				try{
		map = fnGetRetentionPeriod();
		return wrapWebServiceResults("SUCCESS", "", map);
		} catch (Throwable t) {
		return wrapWebServiceResults("FAILED", t.getMessage(), null);
				}  
	}

	@webService(path = "getDateTimeFormat", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = false)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"TimeFormat\": \"HH:mm:ss\",\r\n" +
			"  \"DateFormat\": \"yyyy-MM-dd\",\r\n" +
			"  \"DateTimeFormat\": \"yyyy-MM-dd HH:mm:ss.SSS\"\r\n" +
			"}")
	public static Object wsGetDateTimeFormat() throws Exception {
		Map<String, Object> formats = new HashMap<>();
		GlobalProperties gp = GlobalProperties.getInstance();
		
		formats.put("DateTimeFormat", gp.getDateTimeFormat());
		formats.put("DateFormat", gp.getDateFormat());
		formats.put("TimeFormat", gp.getTimeFormat());
		return formats;
	}

}
