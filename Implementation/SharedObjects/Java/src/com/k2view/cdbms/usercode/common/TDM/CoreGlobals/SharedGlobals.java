package com.k2view.cdbms.usercode.common.TDM.CoreGlobals;

import com.k2view.cdbms.shared.utils.UserCodeDescribe.category;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;

public class SharedGlobals {

    @desc("Indicates if the task execution needs to delete the instance from target DB. Values are true or false.")
    @category("TDM")
    public static String TDM_DELETE_BEFORE_LOAD = "false";

    @desc("Indicates if the task execution needs to insert the instance to target DB. Values are true or false.")
    @category("TDM")
    public static String TDM_INSERT_TO_TARGET = "false";

    @desc("Indicates if to sync the LU source tables for the task's LUIs. Values are true or false. This Global is set to false when the task's policy of fetching data is 'Available [source environment name] data in the Test data store' or 'Selected snapshot (version)'. ")
    @category("TDM")
    public static String TDM_SYNC_SOURCE_DATA = "true";

    @desc("This Global is set by the task execution based on the target System version. This Global enables supporting different System versions in the source and target, e.g. extract from Production and load to a development environment.")
    @category("TDM")
    public static String TDM_TARGET_PRODUCT_VERSION = "0";

    @desc("This Global is set by the task execution based on the source System version. This Global enables supporting different System versions in the source and target, e.g. extract from Production and load to a development environment.")
    @category("TDM")
    public static String TDM_SOURCE_PRODUCT_VERSION = "0";

    @desc("Indicates if to replace the sequences (IDs) when loading the entities to the target. Values are true or false. ")
    @category("TDM")
    public static String TDM_REPLACE_SEQUENCES = "false";

    @desc("Populated by the task execution process with the current execution's task execution ID.")
    @category("TDM")
    public static String TDM_TASK_EXE_ID = "0";

    @desc("Populated by the task execution process with the source environment name")
    @category("TDM")
    public static String TDM_SOURCE_ENVIRONMENT_NAME = "";

    @desc("Populated by the task execution process with the target environment name")
    @category("TDM")
    public static String TDM_TAR_ENV_NAME = "";

    @desc("Indicates if the task clones an entity. Values are true or false")
    @category("TDM")
    public static String TDM_CLONING_DATA = "false";

    @desc("Populated by the task execution process with the current execution's task ID.")
    @category("TDM")
    public static String TDM_TASK_ID = "0";

    @desc("Indicates if the task creates a new data snapshot (version). Values are true or false.")
    @category("TDM")
    public static String TDM_DATAFLUX_TASK = "false";

    @desc("Indicates the number of the cloned replica for a clone task. For other tasks, it is set to zero.")
    @category("TDM")
    public static String clone_id = "0";

    @desc("Indicates if to mask the data by the load flows in addition to the LU population flows. The PII data is re-maksed by the load flows when runnin gclone tasks in order to get different values on each replica (clone).")
    @category("TDM")
    public static String LOAD_MASKING_FLAG = "false";

    @desc("This Global is populated with the task execution ID of the select data snapshot (version) when loading a data version to the target.")
    @category("TDM")
    public static String TDM_VERSION_TASK_EXECUTION_ID = "0";

    @desc("This Global is set to true for delete only tasks. Otherwise, it is set to false.")
    @category("TDM")
    public static String TDM_DELETE_ONLY_TASK = "false";

    @desc("This Global is set to enable running the TDM flows from thr Studio for debug purpose.")
    @category("TDM_DEBUG")
    public static String USER_NAME = "admin";

    @desc("This Global is set to enable running the TDM flows from thr Studio for debug purpose.")
    @category("TDM_DEBUG")
    public static String USER_FABRIC_ROLES = "admin";

    @desc("This Global is set to enable running the TDM flows from thr Studio for debug purpose.")
    @category("TDM_DEBUG")
    public static String TDM_RESERVE_IND = "false";

    @desc("This Global is set to enable running the TDM flows from thr Studio for debug purpose.")
    @category("TDM_DEBUG")
    public static String RESERVE_RETENTION_PERIOD_TYPE = "Days";

    @desc("This Global is set to enable running the TDM flows from thr Studio for debug purpose.")
    @category("TDM_DEBUG")
    public static String RESERVE_RETENTION_PERIOD_VALUE = "10";

    @desc("This Global is set to enable running the TDM flows from thr Studio for debug purpose.")
    @category("TDM_DEBUG")
    public static String BE_ID = "0";

    @desc("This Global is set to enable running the TDM flows from thr Studio for debug purpose.")
    @category("TDM_DEBUG")
    public static String TASK_TYPE = "EXTRACT";

}
