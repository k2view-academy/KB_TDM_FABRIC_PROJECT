/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Customer.Decision;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	@desc("runs if a change happened in CASE table, brings all Notes for modified CaseID")
	@type(DecisionFunction)
	@out(name = "syncInd", type = Boolean.class, desc = "")
	public static Boolean CasesUpdateMonitor() throws Exception {
		// this function will decide to synchronize an LUI if the number of cases is higher than an arbitrary hardcoded threshold
		log.info("running decision function");
		
		int CRMCases_threshold=25000; //latest known number of cases in CRM_DB.CASES
		Boolean syncInd = false;
		
		String count = db("CRM_DB").fetch("SELECT count(*) FROM CASES").firstValue().toString();
		//puts the number of rows in CASES DB into variable count
		
		log.info(count);
		int cnt=Integer.parseInt(count);
		
		if (cnt >= CRMCases_threshold){
		log.info("new case in !!");
		syncInd = true;	
		log.info("true");
		}
		
		else{
		syncInd = false;
		log.info("false");
		}
		return syncInd;
	}


	@desc("run the sync depending on the number of cases in CRM CASES from Globals : CRMCASES_THRESHOLD")
	@type(DecisionFunction)
	@out(name = "syncInd", type = Boolean.class, desc = "")
	public static Boolean CaeUpdateMonitorGlobal() throws Exception {
		// this function will decide to synchronize an LUI if the number of cases is higher than an arbitrary hardcoded threshold
		
		Boolean syncInd = false;
		
		String count = db("CRM_DB").fetch("SELECT count(*) FROM CASES").firstValue().toString();
		//puts the number of rows in CASES DB into variable count
		int cnt=Integer.parseInt(count);
		
		log.info(RUN_POP);
		if (cnt > Integer.parseInt(RUN_POP)){
		syncInd = true;	
		}
		else{
		syncInd = false;
		}
		
		return syncInd;
	}

	
	
	
	
}
