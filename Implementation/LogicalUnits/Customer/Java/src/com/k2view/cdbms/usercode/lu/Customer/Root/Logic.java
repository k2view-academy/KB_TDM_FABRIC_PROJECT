/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Customer.Root;

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
import com.k2view.cdbms.usercode.lu.Customer.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.Customer.Globals.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import com.k2view.fabric.events.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {








	@type(RootFunction)
	@out(name = "ACTIVITY_ID", type = Long.class, desc = "")
	@out(name = "CASE_ID", type = Long.class, desc = "")
	@out(name = "CASE_DATE", type = String.class, desc = "")
	@out(name = "CASE_TYPE", type = String.class, desc = "")
	@out(name = "STATUS", type = String.class, desc = "")
	public static void fnPop_ADDRESS(String input) throws Exception {
		String sql = "SELECT ACTIVITY_ID, CASE_ID, CASE_DATE, CASE_TYPE, STATUS FROM CRM_DB.CASES";
		db("CRM_DB").fetch(sql).each(row->{
			yield(row.cells());
		});
	}


	@type(RootFunction)
	@out(name = "dummy_output", type = void.class, desc = "")
	public static void fnPop_tdm_lu_type_rel_tar_eid(String dummy_input) throws Exception {
		if(1 == 2)yield(new Object[] {null});
	}


	@type(RootFunction)
	@out(name = "CUSTOMER_ID", type = Double.class, desc = "")
	@out(name = "SSN", type = String.class, desc = "")
	@out(name = "FIRST_NAME", type = String.class, desc = "")
	@out(name = "LAST_NAME", type = String.class, desc = "")
	public static void fnPop_CUSTOMER(String input) throws Exception {
		// Check the TDM_INSERT_TO_TARGET, TDM_DELETE_BEFORE_LOAD and TDM_SYNC_SOURCE_DATA.
		// Note: if both globals - TDM_SYNC_SOURCE_DATA is false and TDM_DELETE_BEFORE_LOAD - are false, the Init flow needs to set the sync mode to OFF
		
		String luName = getLuType().luName;
		String tdmInsertToTarget = "" +fabric().fetch("SET " + luName +".TDM_INSERT_TO_TARGET").firstValue();
		String tdmSyncSourceData = "" + fabric().fetch("SET " + luName +".TDM_SYNC_SOURCE_DATA").firstValue();
		
		log.info("TEST1---tdmInsertToTarget: " + tdmInsertToTarget + ", tdmSyncSourceData: " + tdmSyncSourceData);
		
		// Check TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA. If the TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA are true, then select the data from the source and yield the results
		if(tdmInsertToTarget.equals("true") ) {
			if (tdmSyncSourceData.equals("false")) {
				// If this is the first sync (the instance is not in Fabric) - throw exception
				if (isFirstSync()) {
					throw new Exception("The instance does not exist in Fabric and sync from source is off");
				}
		
			} else // if the TDM_SYNC_SOURCE_DATA is true - select the data from the source and yield the results
			{
				log.info("TEST2---select data for entity ID: " + input);
				String sql = "SELECT CUSTOMER_ID, SSN, FIRST_NAME, LAST_NAME FROM main.CUSTOMER where customer_id = ?";
				db("CRM_DB").fetch(sql, input).each(row->{
					yield(row.cells());
				});
			}
		} 
	}

	
	
	
	
}
