/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDMDB_CleanUp;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import org.apache.commons.lang3.StringUtils;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.TDM.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.TDM.Globals.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import com.k2view.fabric.events.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	@desc("The function will be  called by user job to clean up the TDMDB tables\r\n" +
			" based on retention period given in TDMDB table tdm_general_parameters")
	@type(UserJob)
	public static void TDMDB_CleanUp() throws Exception {
		//The expectation that in TDM project, interface TDM is always defined.
		//Connection con = getConnection("TDM");
		
		String paramName = "";
		String paramValue = "";
		String retentionDate = "";
		String retentionPeriod = "";
		String activeInd = "";
		
		//Boolean delSts = null;
		
		
		//Set the SQL parameter
		String queryString = "SELECT * FROM tdm_general_parameters where UPPER(param_name) in ('CLEANUP_ACTIVE_IND', 'CLEANUP_RETENTION_PERIOD')";
		
		//Set the prepared statement object
		//PreparedStatement queryStatement = con.prepareStatement(queryString);
		
			
		//Execute the query
		//ResultSetWrapper rs = DBQueryPrepared (queryStatement,null);
		Db.Rows rows = db("TDM").fetch(queryString);
		
		//for (Object[] row : rs) {
		for (Db.Row row : rows) {
			paramName = "" + row.cell(0);
			paramValue = "" + row.cell(1);
			//log.info("param_name: " + paramName + ", param_value: " + paramValue);
			if (paramName.equalsIgnoreCase("CLEANUP_ACTIVE_IND")) {
				activeInd = paramValue;
			} else if (paramName.equalsIgnoreCase("CLEANUP_RETENTION_PERIOD")) {
				retentionPeriod = paramValue;
			}
		}
		 if (rows != null) {
			 rows.close();
		 }
		if (activeInd == null || retentionPeriod == null) {
			throw new Exception("2 and only 2 entries in tdm_general_parameters are expected for CleanUp process");
		}
		
		// Check if the Clean Up Functionality is activated
		if (!activeInd.equalsIgnoreCase("true")) {
			log.info("TDMDB_CleanUp - The TDMDB CleanUp is disabled, in order to enable it, change the active_ind in table tdm_general_parameters in TDM DB");
			return;
		}
		
		queryString = "SELECT date_trunc('day', NOW() - interval ' " + retentionPeriod + " MONTH')";
		
		//Set the prepared statement object
		/*queryStatement = con.prepareStatement(queryString);
			
		//Execute the query
		rs = DBQueryPrepared (queryStatement,null);
		
		for (Object[] row : rs) {
			retentionDate = (String)row[0];
		}
		if (rs != null) {
			rs.closeStmt();
		}*/
		retentionDate = "" + db("TDM").fetch(queryString).firstValue();
		
		//log.info("retentionDate = " + retentionDate);		
		//Loop on the translation table and run each delete statement
		Map<String,Map<String, String>> trnClnUpListValues = getTranslationsData("trnTDMCleanUp");
		
		for(String index: trnClnUpListValues.keySet()){
			Map<String, String> valMap = trnClnUpListValues.get(index);
			
			String active = valMap.get("CLEANUP_IND");
			//If the entry in translation table is inactive, then the statement should not run, and continue to the next one
			if(active.equalsIgnoreCase("false")){
				log.warn("TDMDB_CleanUp - Disabled table: " + 
								  valMap.get("TABLE_NAME") + ", will not be cleaned up");
				continue;
			}
			String statement = valMap.get("CLEANUP_STATEMENT");
				
			//count the number of ? in the statement, to add the retention date to all of them
			int  numOfParams = StringUtils.countMatches(statement, "?");
			Object[] queryParam = new Object[numOfParams];
			for (int i = 0; i < numOfParams; i++) {
				queryParam[i] = retentionDate;
			}
			//Set the prepared statement object
			//queryStatement = con.prepareStatement(statement);
					
			//Execute the query	
			try {
				//log.info("Running Statement: " + statement);
				//delSts = DBExecutePrepared(queryStatement,queryParam);
				db("TDM").execute(statement, queryParam);
			} 
			catch (Exception e) {
				
				log.error("TDMDB_Cleanup - Failed to clean Table: " + 
			              valMap.get("TABLE_NAME") + " with Retention Date: " + retentionDate, e);
				continue;
			}
		}
		
		//con.close();
	}
	
}
