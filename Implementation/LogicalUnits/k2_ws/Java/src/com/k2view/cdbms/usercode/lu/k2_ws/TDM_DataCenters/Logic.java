/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM_DataCenters;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {

	@desc("Gets the list of the Data Centers defined in the Fabric cluster.")
	@webService(path = "dataCenters", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"notes\": \"local node\",\r\n" +
			"      \"effective_ip\": \"127.0.0.1\",\r\n" +
			"      \"logical_ids\": \"\",\r\n" +
			"      \"node_id\": \"fabric_debug\",\r\n" +
			"      \"dc\": \"DC1\",\r\n" +
			"      \"status\": \"ALIVE\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetDataCenters() throws Exception {
		return wrapWebServiceResults("SUCCESS", null, getFabricResponse("clusterstatus;"));
				/*
		HashMap<String,Object> response=new HashMap<>();
		String message=null;
		String errorCode="";
				try{
		String sql = "SELECT * FROM \"" + schema + "\".data_centers";
		Db.Rows rows = db("TDM").fetch(sql);
		errorCode= "SUCCESS";
		List<Map<String,Object>> result=new ArrayList<>();
		Map<String,Object> dataCenter;
		for(Db.Row row:rows) {
		dataCenter=new HashMap<String,Object>();
		dataCenter.put("data_center_id",Integer.parseInt(row.cell(0).toString()));
		dataCenter.put("data_center_name", row.cell(1));
		dataCenter.put("data_center_description", row.cell(2));
		dataCenter.put("data_center_created_by", row.cell(3));
		dataCenter.put("data_center_etl_ip_address", row.cell(4));
		dataCenter.put("data_center_creation_date", row.cell(5));
		dataCenter.put("data_center_expiration_date", row.cell(6));
		dataCenter.put("data_center_last_updated_date", row.cell(7));
		dataCenter.put("data_center_last_updated_by", row.cell(8));
		dataCenter.put("data_center_status", row.cell(9));
		result.add(dataCenter);
		response.put("result", result);
					}
		
		
				}
		catch(Exception e){
		message= e.getMessage();
		log.error(message);
		errorCode="FAIL";
				}
		response.put("errorCode","FAIL");
		response.put("message", message);
		return response;
		
					 */
	}

}
