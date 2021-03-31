/////////////////////////////////////////////////////////////////////////
// Project Shared Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.Project_Decision;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class SharedLogic {

	
	
	@type(DecisionFunction)
	@out(name = "decision", type = Boolean.class, desc = "")
	public static Boolean fnDecisionProductionProductVersion() throws Exception {
		String luName = getLuType().luName;
		String tdmSourceProdVersion = "" + ludb().fetch("SET " + luName + ".TDM_SOURCE_PRODUCT_VERSION").firstValue();
		
		log.info("fnDecisionProductionProductVersion - check TDM_SOURCE_PRODUCT_VERSION: " + tdmSourceProdVersion + ", DEVELOPMENT_PRODUCT_VERSION: " + PRODUCTION_PRODUCT_VERSION);
		Boolean decision = false;

		if(tdmSourceProdVersion.equals(PRODUCTION_PRODUCT_VERSION))
		{
			decision = true;
		}

		log.info("fnDecisionProductionProductVersion: " + decision.toString());

		return decision;

	}


	@type(DecisionFunction)
	@out(name = "decision", type = Boolean.class, desc = "")
	public static Boolean fnDecisionDevProductVersion() throws Exception {
		String luName = getLuType().luName;
		String tdmSourceProdVersion = "" + ludb().fetch("SET " + luName + ".TDM_SOURCE_PRODUCT_VERSION").firstValue();
		
		log.info("fnDecisionDevProductVersion - check TDM_SOURCE_PRODUCT_VERSION: " + tdmSourceProdVersion + ", DEVELOPMENT_PRODUCT_VERSION: " + DEVELOPMENT_PRODUCT_VERSION);
		
		
		Boolean decision = false; 
		if(tdmSourceProdVersion.equals(DEVELOPMENT_PRODUCT_VERSION))
		{
			decision = true;
		}
		
		log.info("fnDecisionDevProductVersion: " + decision.toString());
		
		return decision;
	}
	

}
