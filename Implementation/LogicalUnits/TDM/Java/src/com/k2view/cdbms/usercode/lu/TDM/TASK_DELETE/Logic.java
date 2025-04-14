/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TASK_DELETE;

import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;



@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {
	public static final String TDM = "TDM";

	@desc("14-Mar-19- add a check if the has a debug mode. Do not delete from source for debug mode.")
	public static void fnDelTaskFromSource() throws Exception {
		// Clean TDM execution tables from TDMDB after the data is populated in TDM LU
		// For TEST mode- you can comment this delete
		String instID = getInstanceID();
        //db(TDM).execute("Delete from " + TDMDB_SCHEMA + ".task_execution_entities where task_execution_id = ?", instID);
        //db(TDM).execute("Delete from " + TDMDB_SCHEMA + ".tdm_seq_mapping where task_execution_id = ?", instID);
        //db(TDM).execute("Delete from " + TDMDB_SCHEMA + ".task_execution_entities where task_execution_id = ?", instID);
        //db(TDM).execute("Delete from " + TDMDB_SCHEMA + ".tdm_seq_mapping where task_execution_id = ?", instID);
        //db(TDM).execute("Delete from " + TDMDB_SCHEMA + ".TASK_EXE_STATS_DETAILED where task_execution_id = ?", instID);
        //db(TDM).execute("Delete from " + TDMDB_SCHEMA + ".TASK_EXE_ERROR_DETAILED where task_execution_id = ?", instID);
				
	}


	@desc("25-Sep-19- new function- update the TDM DB - task_execution_list set synced_to_fabric = TRUE. Originally this was done by a parser.")
	public static void fnUpdateTaskSyncStatus() throws Exception {
			String sql = "update " + TDMDB_SCHEMA + ".task_execution_list set synced_to_fabric = TRUE where task_execution_id = ? ";
			db(TDM).execute(sql, ludb().fetch("SELECT IID('TDM')").firstValue());
		
	}
}
