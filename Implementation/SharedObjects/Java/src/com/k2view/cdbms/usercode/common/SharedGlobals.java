/////////////////////////////////////////////////////////////////////////
// Shared Globals
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common;

import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;

public class SharedGlobals {


	@desc("The global is used by the test connection of Oracle 8- the dbType parameter must be set to the database type name, defined for the Oracle8 DB")
	@category("TDM")
	public static String ORACLE8_DB_TYPE = "Oracle8";


	@desc("Maximum values of combo box input object")
	@category("TDM")
	public static String COMBO_MAX_COUNT = "49";


	@desc("Indicator to delete the instance to target DB")
	@category("TDM")
	public static String TDM_DELETE_BEFORE_LOAD = "false";
	@desc("Indicator to insert the instance to target DB")
	@category("TDM")
	public static String TDM_INSERT_TO_TARGET = "false";

	@category("TDM")
	public static String TDM_SYNC_SOURCE_DATA = "true";


	@desc("Target product version to override by task execution process")
	@category("TDM")
	public static String TDM_TARGET_PRODUCT_VERSION = "false";
	@desc("Source product version to override by task execution process")
	@category("TDM")
	public static String TDM_SOURCE_PRODUCT_VERSION = "false";

	@category("TDM")
	public static String TDM_REPLACE_SEQUENCES = "false";

	@category("TDM")
	public static String TDM_TASK_EXE_ID = "0";

	@category("TDM")
	public static String MASK_FLAG = "0";
	@category("TDM")
	public static String TDM_SOURCE_ENVIRONMENT_NAME = "ENV1";
	@category("TDM")
	public static String TDM_TAR_ENV_NAME = "ENV2";
	@category("TDM")
	public static String TDM_SYNTHETIC_DATA = "false";
	@category("TDM")
	public static String TDM_TASK_ID = "0";
	@category("TDM")
	public static String TDM_VERSION_DATETIME = "19700101000000";
	@category("TDM")
	public static String TDM_VERSION_NAME = "";

	@desc("In case of cloning, each clone can have a TTL, this global holds the type of the TTL and it can have one of the following values:Minutes, Days, Weeks, Months")
	@category("TDM")
	public static String CLONE_CLEANUP_RETENTION_PERIOD_TYPE = "Days";
	@desc("The value of the TTL based on the type defined in CLONE_CLEANUP_RETENTION_PERIOD_TYPE")
	@category("TDM")
	public static String CLONE_CLEANUP_RETENTION_PERIOD_VALUE = "1";

	@desc("Prefix of delete tables")
	@category("TDM")
	public static String TDM_DEL_TABLE_PREFIX = "TAR";
	public static String DEVELOPMENT_PRODUCT_VERSION = "DEV";
	public static String PRODUCTION_PRODUCT_VERSION = "PROD";


}
