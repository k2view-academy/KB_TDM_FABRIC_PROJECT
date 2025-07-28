package com.k2view.cdbms.usercode.common.TDM.CoreGlobals;

import com.k2view.cdbms.shared.utils.UserCodeDescribe.category;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;

public class SharedGlobals {

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
    public static String TDM_TARGET_PRODUCT_VERSION = "0";

    @desc("Source product version to override by task execution process")
    @category("TDM")
    public static String TDM_SOURCE_PRODUCT_VERSION = "0";

    @category("TDM")
    public static String TDM_REPLACE_SEQUENCES = "false";

    @category("TDM")
    public static String TDM_TASK_EXE_ID = "0";

    @category("TDM")
    public static String TDM_SOURCE_ENVIRONMENT_NAME = "";

    @category("TDM")
    public static String TDM_TAR_ENV_NAME = "";

    @category("TDM")
    public static String TDM_CLONING_DATA = "false";

    @category("TDM")
    public static String TDM_TASK_ID = "0";

    @desc("Indicator to mark the task as dataflux or not")
    @category("TDM")
    public static String TDM_DATAFLUX_TASK = "false";

    @category("TDM")
    public static String clone_id = "0";

    @category("TDM")
    public static String LOAD_MASKING_FLAG = "false";

    @category("TDM")
    public static String TDM_VERSION_TASK_EXECUTION_ID = "0";

    @category("TDM")
    public static String TDM_DELETE_ONLY_TASK = "false";

    @category("TDM_DEBUG")
    public static String USER_NAME = "admin";

    @category("TDM_DEBUG")
    public static String USER_FABRIC_ROLES = "admin";

    @category("TDM_DEBUG")
    public static String TDM_RESERVE_IND = "false";

    @category("TDM_DEBUG")
    public static String RESERVE_RETENTION_PERIOD_TYPE = "Days";

    @category("TDM_DEBUG")
    public static String RESERVE_RETENTION_PERIOD_VALUE = "10";

    @category("TDM_DEBUG")
    public static String BE_ID = "0";

    @category("TDM_DEBUG")
    public static String TASK_TYPE = "EXTRACT";

}
