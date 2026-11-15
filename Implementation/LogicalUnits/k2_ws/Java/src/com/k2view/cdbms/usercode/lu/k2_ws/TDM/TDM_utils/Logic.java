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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getTDMDBSchema;

import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetRetentionPeriod;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserPermissionGroup;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnIsAdminOrOwner;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getGlobalMaxWorkersLimit;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.PermissionErrorMsg;



@SuppressWarnings({"DefaultAnnotationParam", "rawtypes", "unchecked"})
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
			String sql = "select * from " + getTDMDBSchema() + ".tdm_general_parameters where tdm_general_parameters.param_name = 'tdm_gui_params'";
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
		Object tdmVersion =  db(TDM).fetch("select param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'TDM_VERSION'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, tdmVersion);
	}
	public static Object wsGetTDMFooterText() throws Exception {
		Object tdmVersion =  db(TDM).fetch("select param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'FOOTER_TEXT'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, tdmVersion);
	}

    public static Object wsGetParamsAutoWidth() throws Exception {
		Object paramWidth =  db(TDM).fetch("select param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'ENABLE_PARAMETERS_AUTO_WIDTH'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, paramWidth);
	}

	public static Object wsCheckIfParamsCoupling() throws Exception {
		Object isCoupling =  db(TDM).fetch("select param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'PARAMS_COUPLING'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, isCoupling);
	}

    public static Object wsGetParamsLUName() throws Exception {
		Object paramLuName =  db(TDM).fetch("select param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'ADD_LU_NAME_TO_PARAM_NAME'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, paramLuName);
	}
	
	public static Object wsGetTaskLuEditForTesters() throws Exception {
		Object editIndicator =  db(TDM).fetch("select param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'ENABLE_TASK_LU_EDITING_FOR_TESTERS'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, editIndicator);
	}
	public static Object wsGetTablesAPIMode() throws Exception {
		Object splitIndicator =  db(TDM).fetch("select param_value from " + getTDMDBSchema() + ".tdm_general_parameters where param_name = 'TABLES_USE_SPLIT_API'").firstValue();
		
		return wrapWebServiceResults("SUCCESS", null, splitIndicator);
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

	@desc("Returns the retention and reservation period options the current user may select in the task window, based on the user's permission group and the tdm_gui_params in tdm_general_parameters.\r\n" +
			"Retention period:\r\n" +
			"- versionInd false - 'Do Not Delete', 'Do Not Retain' and the retentionPeriodTypes, default retentionDefaultPeriod.\r\n" +
			"- versionInd true - same options, 'Do Not Delete' only if allow_doNotDelete is true in versioningRetentionPeriod (admin/owner) or versioningRetentionPeriodForTesters (tester), which is also the default.\r\n" +
			"Reservation period: the reservationPeriodTypes, default reservationDefaultPeriod.\r\n" +
			"For testers the options are limited by the max retention/reservation period for testers, returned as maxRetentionPeriod/maxReservationPeriod (null for admin/owner).\r\n" +
			"envId is optional. When populated, the user is handled as admin/owner only if he is an admin or an owner of this environment (same check as the task save/execution validation). " +
			"Otherwise the user's permission group is used.")
	@webService(path = "retentionPeriodOptions", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\n" +
			"    \"result\": {\n" +
			"        \"isAdminOrOwner\": false,\n" +
			"        \"retentionPeriodTypes\": [\n" +
			"            {\"name\": \"Do Not Retain\", \"units\": 0, \"label\": \"Do not retain\", \"value\": \"Do Not Retain\"},\n" +
			"            {\"name\": \"Minutes\", \"units\": 6.9444444E-4, \"label\": \"Minutes\", \"value\": 6.9444444E-4},\n" +
			"            {\"name\": \"Hours\", \"units\": 0.04166666666, \"label\": \"Hours\", \"value\": 0.04166666666},\n" +
			"            {\"name\": \"Days\", \"units\": 1, \"label\": \"Days\", \"value\": 1},\n" +
			"            {\"name\": \"Weeks\", \"units\": 7, \"label\": \"Weeks\", \"value\": 7}\n" +
			"        ],\n" +
			"        \"retentionDefaultPeriod\": {\"units\": \"Days\", \"value\": 5, \"allow_doNotDelete\": false},\n" +
			"        \"maxRetentionPeriod\": {\"units\": \"Days\", \"value\": 90},\n" +
			"        \"reservationPeriodTypes\": [\n" +
			"            {\"name\": \"Minutes\", \"units\": 6.9444444E-4, \"label\": \"Minutes\", \"value\": 6.9444444E-4},\n" +
			"            {\"name\": \"Hours\", \"units\": 0.04166666666, \"label\": \"Hours\", \"value\": 0.04166666666},\n" +
			"            {\"name\": \"Days\", \"units\": 1, \"label\": \"Days\", \"value\": 1}\n" +
			"        ],\n" +
			"        \"reservationDefaultPeriod\": {\"units\": \"Days\", \"value\": 5},\n" +
			"        \"maxReservationPeriod\": {\"units\": \"Days\", \"value\": 5}\n" +
			"    },\n" +
			"    \"errorCode\": \"SUCCESS\",\n" +
			"    \"message\": \"\"\n" +
			"}\n")
	public static Object wsGetRetentionPeriodOptions(Boolean versionInd, String envId) throws Exception {
		try {
			String permissionGroup = fnGetUserPermissionGroup("");
			if (permissionGroup == null) {
				return wrapWebServiceResults("FAILED", PermissionErrorMsg.PERMISSION_GROUP_NOT_FOUND.getMsg(), null);
			}
			boolean adminOrOwner;
			if ("admin".equalsIgnoreCase(permissionGroup)) {
				adminOrOwner = true;
			} else if (envId != null && !envId.trim().isEmpty()) {
				adminOrOwner = fnIsAdminOrOwner(envId.trim(), sessionUser().name());
			} else {
				adminOrOwner = "owner".equalsIgnoreCase(permissionGroup);
			}

			Map<String, Object> definitions = fnGetRetentionPeriod();

			// The max periods apply to testers only
			Map<String, Object> maxRetentionPeriod = adminOrOwner ? null : (Map<String, Object>) definitions.get("maxRetentionPeriodForTesters");
			Map<String, Object> maxReservationPeriod = adminOrOwner ? null : (Map<String, Object>) definitions.get("maxReservationPeriodForTesters");

			// Retention period
			Object retentionDefaultPeriod;
			boolean allowDoNotDelete;
			if (Boolean.TRUE.equals(versionInd)) {
				Map<String, Object> versionPeriod = (Map<String, Object>) definitions.get(adminOrOwner ? "versioningRetentionPeriod" : "versioningRetentionPeriodForTesters");
				retentionDefaultPeriod = versionPeriod;
				allowDoNotDelete = versionPeriod != null && "true".equalsIgnoreCase(String.valueOf(versionPeriod.get("allow_doNotDelete")));
			} else {
				retentionDefaultPeriod = definitions.get("retentionDefaultPeriod");
				allowDoNotDelete = true;
			}
			List<Map<String, Object>> retentionOptions = new ArrayList<>();
			if (allowDoNotDelete) {
				retentionOptions.add(periodOption(DO_NOT_DELETE, -1, "Do not delete", DO_NOT_DELETE));
			}
			retentionOptions.add(periodOption(DO_NOT_RETAIN, 0, "Do not retain", DO_NOT_RETAIN));
			retentionOptions.addAll(periodOptions(definitions.get("retentionPeriodTypes"), maxRetentionPeriod));

			Map<String, Object> result = new LinkedHashMap<>();
			result.put("isAdminOrOwner", adminOrOwner);
			result.put("retentionPeriodTypes", retentionOptions);
			result.put("retentionDefaultPeriod", retentionDefaultPeriod);
			result.put("maxRetentionPeriod", maxRetentionPeriod);
			result.put("reservationPeriodTypes", periodOptions(definitions.get("reservationPeriodTypes"), maxReservationPeriod));
			result.put("reservationDefaultPeriod", definitions.get("reservationDefaultPeriod"));
			result.put("maxReservationPeriod", maxReservationPeriod);
			return wrapWebServiceResults("SUCCESS", "", result);
		} catch (Throwable t) {
			return wrapWebServiceResults("FAILED", t.getMessage(), null);
		}
	}

	private static final String DO_NOT_DELETE = "Do Not Delete";
	private static final String DO_NOT_RETAIN = "Do Not Retain";

	// Converts the period types of tdm_gui_params to select options, limited by the max period.
	// The max period is always in Days, and the units of each period type is its number of days.
	private static List<Map<String, Object>> periodOptions(Object configuredTypes, Map<String, Object> maxPeriod) {
		double maxValue = maxPeriod != null && maxPeriod.get("value") != null ?
				Double.parseDouble(String.valueOf(maxPeriod.get("value"))) : Double.MAX_VALUE;
		List<Map<String, Object>> options = new ArrayList<>();
		if (!(configuredTypes instanceof List)) return options;
		for (Object type : (List<?>) configuredTypes) {
			if (!(type instanceof Map)) continue;
			Map<String, Object> typeMap = (Map<String, Object>) type;
			String name = String.valueOf(typeMap.get("name"));
			Object units = typeMap.get("units");
			// 'Do Not Delete' and 'Do Not Retain' are added by the caller according to the user's permissions
			if (DO_NOT_DELETE.equalsIgnoreCase(name) || DO_NOT_RETAIN.equalsIgnoreCase(name)) continue;
			if (!(units instanceof Number) || ((Number) units).doubleValue() > maxValue) continue;
			options.add(periodOption(name, (Number) units, name, units));
		}
		return options;
	}

	private static Map<String, Object> periodOption(String name, Number units, String label, Object value) {
		Map<String, Object> option = new LinkedHashMap<>();
		option.put("name", name);
		option.put("units", units);
		option.put("label", label);
		option.put("value", value);
		return option;
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

	@desc("This API provides Max Workers Per Node from config with TDM override logic")
	@webService(path = "getMaxWorkersPerNode", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = false)
	@resultMetaData(mediaType = Produce.JSON, example = """
									{
			  "result": 5,
			  "errorCode": "SUCCESS",
			  "message": ""
			}
									""")
	public static Object wsGetMaxWorkersPerNode() throws Exception {
		try {
			// 1. Get the base limit from the new helper function
			int globalLimit = getGlobalMaxWorkersLimit();
			int finalValue = globalLimit;

			// 2. Load the override from tdm_general_parameters
			String sql = "SELECT param_value FROM " + getTDMDBSchema() +
					".tdm_general_parameters WHERE param_name = 'MAX_NO_OF_WORKERS_FOR_EXECUTION'";

			Object tdmValueObj = db(TDM).fetch(sql).firstValue();

			// 3. Apply TDM Logic
			if (tdmValueObj != null && !tdmValueObj.toString().trim().isEmpty()) {
				int tdmValue = Integer.parseInt(tdmValueObj.toString());
				// Only override if TDM value is lower than the Global limit
				if (tdmValue > 0 && tdmValue < globalLimit) {
					finalValue = tdmValue;
				}
			}

			return wrapWebServiceResults("SUCCESS", "", finalValue);

		} catch (NumberFormatException e) {
			return wrapWebServiceResults("FAILED", "Configuration contains a non-numeric value", null);
		} catch (Throwable t) {
			return wrapWebServiceResults("FAILED", t.getMessage(), null);
		}
	}

	@desc("This API provides the Global Max Workers limit from configuration, ignoring TDM overrides")
	@webService(path = "getGlobalMaxWorkersLimit", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = false)
	@resultMetaData(mediaType = Produce.JSON, example = """
			                       {
			  "result": 8,
			  "errorCode": "SUCCESS",
			  "message": ""
			}
			                        """)
	public static Object wsGetGlobalMaxWorkersLimit() throws Exception {
		try {			
			int globalLimit = getGlobalMaxWorkersLimit();

			return wrapWebServiceResults("SUCCESS", "", globalLimit);

		} catch (Throwable t) {
			// Standard error handling for unexpected configuration issues
			return wrapWebServiceResults("FAILED", t.getMessage(), null);
		}
	}

}
