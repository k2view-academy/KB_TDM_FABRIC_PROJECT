/////////////////////////////////////////////////////////////////////////
// LU Globals
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM;

import com.k2view.cdbms.usercode.common.TDM.SharedGlobals;
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

	@category("TDM")
	public static String TDM_DEPLOY_ENVIRONMENTS = "true";

    @desc("This flag is used to enble running sync in Studio")
	@category("TDM")
	public static String TDM_DEBUG_MODE = "true";

    @category("TDM_UPGRADE")
    public static final String TARGET_TDM_VERSION = "9.3.1";
}
