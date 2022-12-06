/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ref.Masking;

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
import com.k2view.cdbms.usercode.lu.k2_ref.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.k2_ref.Globals.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import com.k2view.fabric.events.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	@desc("Retrieves the list of sample first names from the trnSAMPLE_FIRST_NAME")
	@type(RootFunction)
	@out(name = "FIRST_NAME", type = String.class, desc = "")
	public static void getSampleFirstNames() throws Exception {
		// Loop through the translation and yield its content
		
		getTranslationsData("trnSAMPLE_FIRST_NAME").entrySet().stream().forEach(m -> {
			try {
				yield(new Object[] {
					m.getValue().get("FIRST_NAME")
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}


	@desc("Retrieves the list of sample last names from the trnSAMPLE_LAST_NAME")
	@type(RootFunction)
	@out(name = "LAST_NAME", type = String.class, desc = "")
	public static void getSampleLastNames() throws Exception {
		// Loop through the translation and yield its content
		
		getTranslationsData("trnSAMPLE_LAST_NAME").entrySet().stream().forEach(m -> {
			try {
				yield(new Object[] {
					m.getValue().get("LAST_NAME")
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}




	@desc("Retrieves the list of sample company names from the  trnSAMPLE_COMPANY_NAME")
	@type(RootFunction)
	@out(name = "COMPANY_NAME", type = String.class, desc = "")
	@out(name = "DESCRIPTION", type = String.class, desc = "")
	@out(name = "TAGLINE", type = String.class, desc = "")
	@out(name = "COMPANY_EMAIL", type = String.class, desc = "")
	@out(name = "EIN", type = String.class, desc = "")
	public static void getSampleCompanyNames() throws Exception {
		// Loop through the translation and yield its content
		
		getTranslationsData("trnSAMPLE_COMPANY_NAME").entrySet().stream().forEach(m -> {
			try {
				yield(new Object[] {
					m.getValue().get("COMPANY_NAME"),
					m.getValue().get("DESCRIPTION"),
					m.getValue().get("TAGLINE"),
					m.getValue().get("COMPANY_EMAIL"),
					m.getValue().get("EIN")
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}


	@desc("Retrieves the list of sample street names from the trnSAMPLE_STREET_NAME")
	@type(RootFunction)
	@out(name = "STREET_NAME", type = void.class, desc = "")
	public static void getSampleStreetNames() throws Exception {
		// Loop through the translation and yield its content
		
		getTranslationsData("trnSAMPLE_STREET_NAME").entrySet().stream().forEach(m -> {
			try {
				yield(new Object[] {
					m.getValue().get("STREET_NAME")
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}


	@desc("Retrieves the list of sample job titles from the trnSAMPLE_JOB_TITLE")
	@type(RootFunction)
	@out(name = "JOB_TITLE", type = String.class, desc = "")
	public static void getSampleJobTitles() throws Exception {
		// Loop through the translation and yield its content
		
		getTranslationsData("trnSAMPLE_JOB_TITLE").entrySet().stream().forEach(m -> {
			try {
				yield(new Object[] {
					m.getValue().get("JOB_TITLE")
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}


	@desc("Retrieves the list of Zip Codes")
	@type(RootFunction)
	@out(name = "zip", type = String.class, desc = "")
	@out(name = "city", type = String.class, desc = "")
	@out(name = "state", type = String.class, desc = "")
	@out(name = "latitude", type = String.class, desc = "")
	@out(name = "longitude", type = String.class, desc = "")
	public static void getSamplePostalCodes() throws Exception {
		// Loop through the translation and yield its content
		
		getTranslationsData("trnSAMPLE_POSTAL_CODES").entrySet().stream().forEach(m -> {
			try {
				yield(new Object[] {
					m.getKey(),
					m.getValue().get("city"),
					m.getValue().get("state"),
					m.getValue().get("latitude"),
					m.getValue().get("longitude")
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}


	@desc("Retrieves the list of Languages")
	@type(RootFunction)
	@out(name = "language_id", type = String.class, desc = "")
	@out(name = "language", type = String.class, desc = "")
	@out(name = "region", type = String.class, desc = "")
	public static void getSampleLanguages() throws Exception {
		// Loop through the translation and yield its content
		
		getTranslationsData("trnSAMPLE_LANGUAGE").entrySet().stream().forEach(m -> {
			try {
				yield(new Object[] {
					m.getKey(),
					m.getValue().get("language"),
					m.getValue().get("region")
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}



	
	
	
	
}
