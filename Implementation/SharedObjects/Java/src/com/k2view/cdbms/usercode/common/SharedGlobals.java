/////////////////////////////////////////////////////////////////////////
// Shared Globals
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common;

import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;

public class SharedGlobals {


	@desc("The global is used by the test connection of Oracle 8- the dbType parameter must be set to the database type name, defined for the Oracle8 DB")
	public static String ORACLE8_DB_TYPE = "Oracle8";


	@desc("Maximum values of combo box input object")
	public static String COMBO_MAX_COUNT = "49";


	@desc("Indicator to delete the instance to target DB")
	public static String TDM_DELETE_BEFORE_LOAD = "false";
	@desc("Indicator to insert the instance to target DB")
	public static String TDM_INSERT_TO_TARGET = "true";

	public static String TDM_SYNC_SOURCE_DATA = "true";


	@desc("Target product version to override by task execution process")
	public static String TDM_TARGET_PRODUCT_VERSION = "1";
	@desc("Source product version to override by task execution process")
	public static String TDM_SOURCE_PRODUCT_VERSION = "1";

	public static String TDM_REPLACE_SEQUENCES = "false";

	public static String TDM_TASK_EXE_ID = "0";

	public static String MASK_FLAG = "0";
	public static String TDM_SOURCE_ENVIRONMENT_NAME = "SRC";
	public static String TDM_TAR_ENV_NAME = "TAR";
	public static String TDM_SYNTHETIC_DATA = "false";
	public static String TDM_TASK_ID = "0";
	public static String TDM_VERSION_DATETIME = "19700101000000";
	public static String TDM_VERSION_NAME = "";

	@desc("Prefix of delete tables")
	public static String TDM_DEL_TABLE_PREFIX = "TAR";
	@desc("Invoices cleaning")
	public static final String OLDINVOICES = "2015-12-31";
	@desc("changing international code")
	public static String INTERCODE_UK = "+44";
	@desc("threshold sync")
	public static String CASES_THRESHOLD = "30000";
	@desc("globals threshold")
	public static final String RUN_POP = "20000";


}
