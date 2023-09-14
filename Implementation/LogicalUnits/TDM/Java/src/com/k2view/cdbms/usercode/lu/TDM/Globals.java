/////////////////////////////////////////////////////////////////////////
// LU Globals
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM;

import com.k2view.cdbms.usercode.common.SharedGlobals;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;

public class Globals extends SharedGlobals {

	
	@category("TDM")
	public static String BUILD_TDMDB = "true";

	@category("GENERATE_DATA")
	public static String INSTANCES_RANDOM_MIN = "1";
	@category("GENERATE_DATA")
	public static String INSTANCES_RANDOM_MAX = "10";
	@category("GENERATE_DATA")
	public static String SYNTHETIC_INDICATOR = "false";
	@category("GENERATE_DATA")
	public static String SYNTHETIC_ENVIRONMENT = "Synthetic";

	@category("TDM")
	public static final String TDM_SUMMARY_REPORT_LIMIT = "10000";


	@category("TDM")
	public static String TDM_DEPLOY_ENVIRONMENTS = "true";


}
