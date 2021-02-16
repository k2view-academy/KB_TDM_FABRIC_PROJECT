/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Billing.Root;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.Billing.*;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.Billing.Globals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	@type(RootFunction)
	@out(name = "PATIENT_ID", type = String.class, desc = "")
	@out(name = "SSN", type = String.class, desc = "")
	@out(name = "FIRST_NAME", type = String.class, desc = "")
	@out(name = "LAST_NAME", type = String.class, desc = "")
	@out(name = "ADDRESS", type = String.class, desc = "")
	@out(name = "CITY", type = String.class, desc = "")
	@out(name = "ZIP", type = String.class, desc = "")
	@out(name = "STATE", type = String.class, desc = "")
	@out(name = "COUNTRY", type = String.class, desc = "")
	@out(name = "DATE1", type = String.class, desc = "")
	@out(name = "ATTR_LIST", type = String.class, desc = "")
	@out(name = "PATIENT_CODE", type = String.class, desc = "")
	public static void fnPop_RootTable(String PATIENT_ID) throws Exception {
				// Check the TDM_INSERT_TO_TARGET, TDM_DELETE_BEFORE_LOAD and TDM_SYNC_SOURCE_DATA.
				// Note: if both globals - TDM_SYNC_SOURCE_DATA is false and TDM_DELETE_BEFORE_LOAD - are false, the Init flow needs to set the sync mode to OFF
				
				String luName = getLuType().luName;
				String tdmInsertToTarget = "" +fabric().fetch("SET " + luName +".TDM_INSERT_TO_TARGET").firstValue();
				String tdmSyncSourceData = "" + fabric().fetch("SET " + luName +".TDM_SYNC_SOURCE_DATA").firstValue();
				String rootTableName = "" + fabric().fetch("SET " + luName +".ROOT_TABLE_NAME").firstValue();
				String rootTableColumn = "" + fabric().fetch("SET " + luName +".ROOT_COLUMN_NAME").firstValue();
				
				// Check TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA. If the TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA are true, then select the data from the source and yield the results
				if(tdmInsertToTarget.equals("true") ) {
					if (tdmSyncSourceData.equals("false")) {
						// If this is the first sync (the instance is not in Fabric) - throw exception
						if (isFirstSync()) {
							throw new Exception("The instance does not exist in Fabric and sync from source is off");
						}
				
					} else // if the TDM_SYNC_SOURCE_DATA is true - select the data from the source and yield the results
					{
						// Note that sql needs to be edited to select from the main source table by the input id
						String sql = "SELECT * FROM " + rootTableName + " WHERE " + rootTableColumn + "=?";
						db("<SOURCE DB>").fetch(sql, PATIENT_ID).each(row -> {
							yield(row.cells());
						});
					}
				}
							
			
	}


	@type(RootFunction)
	@out(name = "SUBSCRIBER_ID", type = Double.class, desc = "")
	@out(name = "MSISDN", type = String.class, desc = "")
	@out(name = "IMSI", type = String.class, desc = "")
	@out(name = "SIM", type = String.class, desc = "")
	@out(name = "FIRST_NAME", type = String.class, desc = "")
	@out(name = "LAST_NAME", type = String.class, desc = "")
	@out(name = "SUBSCRIBER_TYPE", type = String.class, desc = "")
	@out(name = "VIP_STATUS", type = String.class, desc = "")
	public static void fnPop_SUBSCRIBER(String input) throws Exception {
		// Check the TDM_INSERT_TO_TARGET, TDM_DELETE_BEFORE_LOAD and TDM_SYNC_SOURCE_DATA.
		// Note: if both globals - TDM_SYNC_SOURCE_DATA is false and TDM_DELETE_BEFORE_LOAD - are false, the Init flow needs to set the sync mode to OFF
		
		String luName = getLuType().luName;
		String tdmInsertToTarget = "" +fabric().fetch("SET " + luName +".TDM_INSERT_TO_TARGET").firstValue();
		String tdmSyncSourceData = "" + fabric().fetch("SET " + luName +".TDM_SYNC_SOURCE_DATA").firstValue();
		String rootTableName = "" + fabric().fetch("SET " + luName +".ROOT_TABLE_NAME").firstValue();
		String rootTableColumn = "" + fabric().fetch("SET " + luName +".ROOT_COLUMN_NAME").firstValue();
		
		// Check TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA. If the TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA are true, then select the data from the source and yield the results
		if(tdmInsertToTarget.equals("true") ) {
			if (tdmSyncSourceData.equals("false")) {
				// If this is the first sync (the instance is not in Fabric) - throw exception
				if (isFirstSync()) {
					throw new Exception("The instance does not exist in Fabric and sync from source is off");
				}
		
			} else // if the TDM_SYNC_SOURCE_DATA is true - select the data from the source and yield the results
			{
		
				String sql = "SELECT SUBSCRIBER_ID, MSISDN, IMSI, SIM, FIRST_NAME, LAST_NAME, SUBSCRIBER_TYPE, VIP_STATUS FROM main.SUBSCRIBER where subscriber_id = ?";
				db("BILLING_DB").fetch(sql, input).each(row->{
					yield(row.cells());
				});
				
			}
		} 
	}

	
	
	
	
}
