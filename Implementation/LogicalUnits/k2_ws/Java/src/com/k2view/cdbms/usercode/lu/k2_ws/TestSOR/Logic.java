/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TestSOR;

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

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {


	@desc("Test Insert of INV_SUMMARY table")
	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static String wsTestInsertTable(String UID, String LU_NAME) throws Exception {
		//-----------------------------------------------------------------//
		//  Example of how to update an LU table outside of SYNC process   //
		//-----------------------------------------------------------------//
		
		String getCommand = "get " + LU_NAME + ".'" + UID + "'";
		Db ci = db("fabric");
		String status = "SUCCESS";
		
		try
		{
			//log.info("*-*-* GET command: "+ getCommand);
			ci.execute(getCommand);         
			
			String sql = "SELECT COUNT(*) NO_OF_ACT FROM ACTIVITY";	
			//log.info("*-*-* Run SQL: " + sql);
			Db.Rows rows = ludb(LU_NAME, UID).fetch(sql);
		
			for (Db.Row row:rows){
				ci.beginTransaction();
				Object [] params = new Object[]{UID, 99, 0, "NA", row.cell(0) + " total num of activities for the customer"};
				//log.info("*-*-* Insert records into ACT_CASE_NOTE" );
				ci.execute("INSERT INTO ACT_CASE_NOTE (CUSTOMER_ID, ACTIVITY_ID, CASE_ID, CASE_NOTE_ID, CASE_NOTE_TEXT) VALUES (?, ?, ?, ?, ?)", params);
				ci.commit();
			}		
		}catch (Exception e){
			//log.info("*-*-* Insert Failed");
			log.info("*-*-* Insert failed: " + e.getMessage());
			ci.rollback();
			status = "FAILURE";
			
		}
		
		return status;
	}

	
	

	
}
