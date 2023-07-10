/////////////////////////////////////////////////////////////////////////
// Project Shared Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.TdmGenerateData;
import java.util.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import java.util.concurrent.ThreadLocalRandom;
import com.k2view.cdbms.usercode.lu.TDM.Globals;
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
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;

@SuppressWarnings({"DefaultAnnotationParam"})
public class SharedLogic {

	@out(name = "result", type = Integer.class, desc = "")
	public static Integer fnSSNGenerator() throws Exception {
		long timeSeed = System.nanoTime(); // to get the current date time value should be unique
		double randSeed = Math.random() * 1000; // random number generation
		long midSeed = (long) (timeSeed * randSeed); // mixing up the time and random num
		String s = midSeed + "";
		String subStr = s.substring(0, 9); //formatting as ssn is 9 digits
		int result = Integer.parseInt(subStr);// integer value
		return result ;
	}

	@out(name = "result", type = int.class, desc = "")
	public static int fnRandomInstancesNumGenerator(int max, int min) throws Exception {
		int result = ThreadLocalRandom.current().nextInt(min, max + 1);
		return result;
	}

	@out(name = "result", type = int.class, desc = "")
	public static int fnRandomGlobalNumGenerator() throws Exception {
		int min = Integer.parseInt(Globals.INSTANCES_RANDOM_MIN);
		int max= Integer.parseInt(Globals.INSTANCES_RANDOM_MAX);
		int result = ThreadLocalRandom.current().nextInt(min, max + 1);
		return result;
	}

}

