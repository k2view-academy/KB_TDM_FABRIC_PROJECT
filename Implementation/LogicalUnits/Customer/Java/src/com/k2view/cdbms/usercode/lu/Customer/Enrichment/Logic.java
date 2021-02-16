/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Customer.Enrichment;

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
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	public static void fnEnrichmentTest1() throws Exception {
		log.info ("Hello");
	}


	public static void fnEnrichmentTest2() throws Exception {
		log.info("World");

	}


	@desc("adds +1 to phone string when necessary")
	public static void PhoneFormat() throws Exception {
		String SQLNumber="SELECT ASSOCIATED_LINE, CONTRACT_DESCRIPTION FROM CONTRACT";
		String interCode="+1 ";
		log.info("PhoneFormat fonction is running");
		Db.Rows rows = fabric().fetch(SQLNumber);
		for (Db.Row row:rows){ 
			String SQLFormattedNumber="";
			String formattedNumber="";
			String cellValue=""+row.get("ASSOCIATED_LINE");
			String cellValueContDesc=""+row.get("CONTRACT_DESCRIPTION");
			
			if ((cellValue.matches("(.*)+1(.*)") == false)&& (cellValueContDesc.matches("(.*)5G(.*)")))
			{
				log.info(cellValue);
				
				formattedNumber = interCode + cellValue;
				SQLFormattedNumber="UPDATE CONTRACT SET ASSOCIATED_LINE  = ? where  ASSOCIATED_LINE = ?";
				fabric().execute(SQLFormattedNumber,formattedNumber,cellValue);
		// ending the if statement		
			}
		
		}
	}


	@desc("cleaning cases notes and closing cases for customers with no contracts")
	public static void CaseCleaning() throws Exception {
		String Contracts="SELECT COUNT (*) FROM CONTRACT";
		String SQLCASENote="SELECT CASE_ID, NOTE_TEXT, NOTE_DATE FROM CASE_NOTE";
		String SQLCASES="SELECT CASE_ID, CASE_TYPE, STATUS FROM CASES";
		ArrayList<String> open_cases = new ArrayList<String>();
		ArrayList<String> billing_cases = new ArrayList<String>();
		ArrayList<String> network_cases = new ArrayList<String>();
		
		log.info("Case Cleaning fonction is running");
		
		String newBillingNote ="insolvent customer due to alien assimilation";
		String newNetworkNote ="customer has been assimilated into a phone and is no longer network compatible";
		String statusClose="CLOSED";
		
		Db.Rows rowsC = ludb().fetch(SQLCASES);
		for (Db.Row row:rowsC){
			String cellStatus=""+row.get("STATUS");
			String cellCaseID=""+row.get("CASE_ID");
			String cellCaseType=""+row.get("CASE_Type");
			if (cellStatus.matches("Open"))
			{
			open_cases.add(cellStatus);
			String SQLCaseStatus="UPDATE CASES SET STATUS = ? where STATUS = ?";
			ludb().execute(SQLCaseStatus,statusClose,cellStatus);
			}
			if (cellCaseType.matches("Billing Issue"))
			{
			billing_cases.add(cellCaseID);
			}
			if (cellCaseType.matches("Network Issue"))
			{
			network_cases.add(cellCaseID);
			}	
			
		}
			
		Db.Rows rowsN = ludb().fetch(SQLCASENote);
		for (Db.Row row:rowsN){
			
			String cellNoteText=""+row.get("NOTE_TEXT");
			String cellCaseID=""+row.get("CASE_ID");
			log.info(cellCaseID);
			boolean ans1 = billing_cases.contains(cellCaseID);
			boolean ans2 = network_cases.contains(cellCaseID);
			if (ans1){
				String SQLBillingNote="UPDATE CASE_NOTE SET NOTE_TEXT = ? where  CASE_ID = ?";
				ludb().execute(SQLBillingNote,newBillingNote,cellCaseID);	
			}
			else if (ans2){
				String SQLNetworkNote="UPDATE CASE_NOTE SET NOTE_TEXT = ? where  CASE_ID = ?";
				ludb().execute(SQLNetworkNote,newNetworkNote,cellCaseID);	
			}	
			
		}
	}


	@desc("Exercice Globals - clean up invoices older than global named OLDINVOICES")
	public static void fnCleanUpInvoices() throws Exception {
		log.info("Invoice Cleaning fonction is running");
		
		String SQLINVOICES="SELECT * FROM INVOICE";
		String SQLInvoicesDelete="DELETE FROM INVOICE WHERE ISSUED_DATE = ?";
		Db.Rows rows = ludb().fetch(SQLINVOICES);
		
		for (Db.Row row:rows){
			String cellCaseDate=""+row.get("ISSUED_DATE");
			String[] date = cellCaseDate.split("\\s+");
			
			if(date[0].compareTo(OLDINVOICES) < 0) {
		    log.info("invoice date is earlier than 2015/12/31");
			fabric().execute(SQLInvoicesDelete,cellCaseDate);
		    }
		}
	}


	@desc("using global instead of hardcoded")
	public static void PhoneFormat_Global() throws Exception {
		String SQLNumber="SELECT ASSOCIATED_LINE, CONTRACT_DESCRIPTION FROM CONTRACT";
		String SQLFormattedNumber44="UPDATE CONTRACT SET ASSOCIATED_LINE  = ? where  ASSOCIATED_LINE = ?";
		log.info("PhoneFormat fonction is running");
		Db.Rows rows = fabric().fetch(SQLNumber);
		for (Db.Row row:rows){ 
			String SQLFormattedNumber="";
			String formattedNumber="";
			String cellValue=""+row.get("ASSOCIATED_LINE");
			String cellValueContDesc=""+row.get("CONTRACT_DESCRIPTION");
			
			if ((cellValue.matches("(.*)+(.*)") == false))
			{
				//log.info(cellValue);
				formattedNumber = INTERCODE_UK + cellValue;
				fabric().execute(SQLFormattedNumber44,formattedNumber,cellValue);
		// ending the if statement		
			}
		
		}
	}

	
	
	
	
}
