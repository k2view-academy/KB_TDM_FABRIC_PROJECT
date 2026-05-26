/////////////////////////////////////////////////////////////////////////
// Shared Globals
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;

public class SharedGlobals {

    @desc("Maximum values of combo box input object")
	@category("TDM")
	public static String COMBO_MAX_COUNT = "100";

    @desc("The TTL for masking cache")
	@category("TDM")
	public static String MASKING_CACHE_TTL = "2592000";

    @desc("The maximum number of entities to be returned to be displayed in list of entities")
	@category("TDM")
	public static final String MAX_NUMBER_OF_ENTITIES_IN_LIST = "100";

    @desc("Each Instance can have a TTL, this global holds the type of the TTL and it can have one of the following values:Minutes, Hours, Days, Weeks, or Years")
	@category("TDM")
	public static String TDM_LU_RETENTION_PERIOD_TYPE = "Days";

    @desc("The value of the TTL based on the type defined in TDM_LU_RETENTION_PERIOD_TYPE. Populate this global with zero or empty value to avoid setting a TTL on the TDM LUIs.")
	@category("TDM")
	public static String TDM_LU_RETENTION_PERIOD_VALUE = "10";

    @desc("The max number of retrieved entities from TDM_RESERVED_ENTITIES, if set to zero then no limit")
	@category("TDM")
	public static String GET_RESERVED_ENTITIES_LIMIT = "0";

    @category("TDM_DEBUG")
	public static String enable_masking = "true";

    @category("TDM_DEBUG")
	public static String enable_sequences = "false";

    @category("TDM")
	public static String TDM_REF_UPD_SIZE = "1000";

    @category("TDM")
	public static final String TDMDB_SCHEMA = "public";

    @category("TDM")
	public static String TDM_SUMMARY_REPORT_LIMIT = "10000";

    @category("TDM")
	public static final String TDM_DELETE_TABLES_PREFIX = "TAR_";

    @category("TDM")
	public static final String TDM_BATCH_LIMIT = "-1";

    @category("TDM")
	public static String TDM_SEQ_REPORT = "true";

    @desc("Indicates whether to run add TDM statitics")
	@category("TDM")
	public static final String TDM_POPULATE_JMX_STATS = "false";

    @desc("The interface to be used for storing the cache of Masking")
	@category("TDM")
	public static String SEQ_CACHE_INTERFACE = "POSTGRESQL_ADMIN";

    @desc("The separator to be used for parsing the values of Parameters")
	@category("TDM")
	public static final String TDM_PARAMETERS_SEPARATOR = "<#>";

    @category("TDM_DEMO")
	public static String DEVELOPMENT_PRODUCT_VERSION = "DEV";

    @category("TDM_DEMO")
	public static String PRODUCTION_PRODUCT_VERSION = "PROD";

    @category("AI")
	public static final String AI_DB_INTERFACE = "AI_DB";

    @category("AI")
	public static final String AI_ENVIRONMENT = "AI";

    @category("RULE_BASED")
	public static String SYNTHETIC_ENVIRONMENT = "Synthetic";

    @category("TDM")
	public static final String CREATE_AI_K2SYSTEM_DB = "false";

    @category("TDM")
	public static String TDM_SUPPRESS_TEST_CONNECTION = "false";

    @desc("Indicates whether to include the full parent-child entity hierarchy in the TDM LU during task execution. Set to false to exclude the hierarchy and prevent duplicate records when child entities have multiple parent relationships.")
	@category("TDM")
	public static final String POP_FULL_LU_HIERARCHY_IN_TDM_LU = "true";

    @desc("This Global relates to the param coupling mode of business parameters and indicates whether to update the LU schema exported to the TDM database during task execution. It can be configured at either the TDM environment or task level.")
	@category("TDM")
	public static String UPDATE_MDB_EXPORTED_SCHEMA = "false";

    @desc("Indicates whether to replace sequences (IDs) in Fabric when synchronizing an LU instance. Default is false. Set to true to replace sequences during LU instance synchronization.")
	@category("TDM")
	public static String REPLACE_SEQ_BY_LUI_SYNC = "false";

    @desc("Sets the default sequence handling behavior of TDM. Set this Global to true to enable catalog-based mode, or false to use non-catalog mode. This Global can be applied to an LU to control its behavior.")
	@category("TDM")
	public static String TDM_USING_CATALOG_SEQUENCES = "false";

    @desc("This Global relates to the param coupling mode of business parameters and indicates whether to create the FKs in the LU schema exported to the TDM database.")
	@category("TDM")
	public static String CREATE_PHYSICAL_FK_IN_MDB_EXPORT_SCHEMA = "true";

    @desc ("When All - All tables are reported to the stats table, When Diff - Only Tables with Differences are reported, When None - No tables are reported.")
	@category("TDM")
	public static String STATISTICS_REPORT_FLAG = "ALL";

    @desc("Used to suppress File System Interfaces in Table Level")
	@category("TDM")
	public static final String SUPPRESS_TABLE_LEVEL_SUPPRESS_FILE_SYSTEMS = "true";


}
