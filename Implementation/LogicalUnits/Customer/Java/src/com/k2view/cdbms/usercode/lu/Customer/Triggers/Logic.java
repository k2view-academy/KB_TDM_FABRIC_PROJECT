/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Customer.Triggers;

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
import com.k2view.fabric.fabricdb.datachange.DataChangeType;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.Customer.Globals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	@type(TriggerFunction)
	public static void fnLogDataChanges(TableDataChange tableDataChange) throws Exception {
		//---------------------------------------------------------------------------//
		// Example of a Trigger function - which is triggered from CUSTOMER table   //
		//---------------------------------------------------------------------------//
		
		String tbl=tableDataChange.getTable().toString();
		DataChangeType change=tableDataChange.getType();
		List newValuesList = tableDataChange.newValuesAsList();
		String newValues = newValuesList.toString();
		List oldValuesList = tableDataChange.oldValuesAsList();
		String oldValues = oldValuesList.toString();
		
		String entityId = ludb().fetch("SELECT IID('Customer')").firstValue().toString();
		
		//log.info("----entity ID is -----"+entityId);
		
		if( change == DataChangeType.INSERT) {
			Db ci = db("fabric");
			ci.beginTransaction();
			ci.execute("insert into DATA_CHANGES values (?,?,?,?)",entityId,tbl,oldValues,newValues);
			ci.commit();
		}
	}

	
	
	
	
}
