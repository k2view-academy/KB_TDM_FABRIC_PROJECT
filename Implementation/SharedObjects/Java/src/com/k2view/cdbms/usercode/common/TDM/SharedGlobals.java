/////////////////////////////////////////////////////////////////////////
// Shared Globals
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;

public class SharedGlobals {

	@desc("Maximum values of combo box input object")
	@category("TDM")
	public static final String COMBO_MAX_COUNT = "100";

	@desc("The TTL for masking cache")
	@category("TDM")
	public static final String MASKING_CACHE_TTL = "2592000";

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

	@category("TDM")
	public static final String TDM_REF_UPD_SIZE = "1000";

	@category("TDM")
	public static final String TDMDB_SCHEMA = "k2tdm_${ClusterID}";

	@category("TDM")
	public static final String TDM_SUMMARY_REPORT_LIMIT = "10000";

	@category("TDM")
	public static final String TDM_DELETE_TABLES_PREFIX = "TAR_";

	@category("TDM")
	public static final String TDM_BATCH_LIMIT = "-1";

	@category("TDM")
	public static final String TDM_SEQ_REPORT = "true";

	@desc("Indicates whether to run add TDM statitics")
	@category("TDM")
	public static final String TDM_POPULATE_JMX_STATS = "false";

	@desc("The interface to be used for storing the cache of Masking")
	@category("TDM")
	public static final String SEQ_CACHE_INTERFACE = "POSTGRESQL_ADMIN";

	@desc("The separator to be used for parsing the values of Parameters")
	@category("TDM")
	public static final String TDM_PARAMETERS_SEPARATOR = "<#>";

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

	@category("TDM")
	public static final String POP_FULL_LU_HIERARCHY_IN_TDM_LU = "true";

	@category("TDM")
	public static String UPDATE_MDB_EXPORTED_SCHEMA = "false";

	@category("TDM")
	public static String REPLACE_SEQ_BY_LUI_SYNC = "false";

	@category("TDM")
	public static String TDM_USING_CATALOG_SEQUENCES = "false";

	@category("TDM")
	public static String CREATE_PHYSICAL_FK_IN_MDB_EXPORT_SCHEMA = "true";

}
