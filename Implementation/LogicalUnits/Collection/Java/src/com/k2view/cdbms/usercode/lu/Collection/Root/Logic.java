/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Collection.Root;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.Collection.*;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.Collection.Globals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	@type(RootFunction)
	@out(name = "customer_id", type = Double.class, desc = "")
	@out(name = "collection_id", type = Double.class, desc = "")
	@out(name = "last_update", type = String.class, desc = "")
	@out(name = "collection_status", type = String.class, desc = "")
	public static void fnPop_collection(String input) throws Exception {
		// Check the TDM_INSERT_TO_TARGET, TDM_DELETE_BEFORE_LOAD and TDM_SYNC_SOURCE_DATA.
		// Note: if both globals - TDM_SYNC_SOURCE_DATA is false and TDM_DELETE_BEFORE_LOAD - are false, the Init flow needs to set the sync mode to OFF
		
		String luName = getLuType().luName;
		String tdmInsertToTarget = "" +fabric().fetch("SET " + luName +".TDM_INSERT_TO_TARGET").firstValue();
		String tdmSyncSourceData = "" + fabric().fetch("SET " + luName +".TDM_SYNC_SOURCE_DATA").firstValue();
		
		// Check TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA. If the TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA are true, then select the data from the source and yield the results
		if(tdmInsertToTarget.equals("true") ) {
			if (tdmSyncSourceData.equals("false")) {
				// If this is the first sync (the instance is not in Fabric) - throw exception
				if (isFirstSync()) {
					throw new Exception("The instance does not exist in Fabric and sync from source is off");
				}
		
			} else // if the TDM_SYNC_SOURCE_DATA is true - select the data from the source and yield the results
			{
		
				// Indicates if any of the source root table has the instance id
				AtomicBoolean instanceExists = new AtomicBoolean(false);
		
				String sql = "SELECT customer_id, collection_id, last_update, collection_status FROM collection";
				db("COLLECTION_DB").fetch(sql).each(row->{
					instanceExists.set(true);
					yield(row.cells());
				});
		
				if(!instanceExists.get()) {
					throw new Exception("Instance " + getInstanceID() + " is not found in the Source");
				}
			}
		}
	}

	
	
	
	
}
