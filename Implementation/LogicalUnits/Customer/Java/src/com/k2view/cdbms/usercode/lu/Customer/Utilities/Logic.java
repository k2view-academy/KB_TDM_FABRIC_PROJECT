/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Customer.Utilities;

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


	@desc("Accept input number and increase it by 10")
	@type(LudbFunction)
	@out(name = "o_id", type = String.class, desc = "")
	public static String fnCreateInstId(String i_id) throws Exception {
		//Validate that the input id is not null or empty
		if (i_id!=null && !i_id.isEmpty()){
		// Increase the input by 1000 and return
			return Integer.sum(Integer.valueOf(i_id),1000)+"";
			  
		   }
		return "0";
	}


	@desc("User Job to write into a file every 10 secs")
	@type(UserJob)
	public static void testJob(Integer test, Integer inSecTime) throws Exception {
		//writing into a file;
		
		while (test<5 && !isAborted()){
			test=test+1;
			sleep(10000);
			//inSecTime=test*10;
			try (FileWriter myWriter = new FileWriter("job_test4.txt", true)) {
				try {
					myWriter.write("Test Number: " + test + "::->" + 10 * test + " seconds have passed since ...");
					myWriter.close();
				} finally {
					myWriter.close();
				}
			}
		}
	}


	@out(name = "output", type = Object.class, desc = "")
	public static Object fnGetChildLinkTrn() throws Exception {
		return getTranslationsData("trnChildLink");
	}

	
	
	
	
}
