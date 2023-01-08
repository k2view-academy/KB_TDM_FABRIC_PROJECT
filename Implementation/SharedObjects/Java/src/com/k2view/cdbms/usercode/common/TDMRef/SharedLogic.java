/////////////////////////////////////////////////////////////////////////
// Project Shared Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.TDMRef;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.fabric.common.Util;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.SharedGlobals.TDM_TASK_EXE_ID;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getRetention;
import static com.k2view.cdbms.usercode.common.TdmSharedUtils.SharedLogic.createJsonArrayFromTableRecords;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class SharedLogic {

	private static final String TDM = "TDM";
    private static final String REF = "REF";
    private static final String TASKS = TDMDB_SCHEMA + ".tasks";
    private static final String ENVIRONMENTS = TDMDB_SCHEMA + ".environments";
    private static final String TASK_EXECUTION_LIST = TDMDB_SCHEMA + ".task_execution_list";
    private static final String TASK_REF_TABLES = TDMDB_SCHEMA + ".TASK_REF_TABLES";
    private static final String PRODUCT_LOGICAL_UNITS = TDMDB_SCHEMA + ".product_logical_units";
    private static final String TASK_REF_EXE_STATS = TDMDB_SCHEMA + ".TASK_REF_EXE_STATS";
    private static final String TASKS_LOGICAL_UNITS = TDMDB_SCHEMA + ".tasks_logical_units";
    private static final String TDM_REFERENCE = "fnTdmReference";
    private static final String PENDING = "pending";
    private static final String RUNNING = "running";
    private static final String WAITING = "waiting";
    private static final String STOPPED = "stopped";
    private static final String RESUME = "resume";
    private static final String FAILED = "failed";
    private static final String COMPLETED = "completed";

	
	@out(name = "batchID", type = String.class, desc = "")
	public static String fnTdmReference(String taskExecutionID, String taskType) throws Exception {
		//log.info("-- START Reference JOB for Task Type: " + taskType + " Task Execution ID: " + taskExecutionID + "---");
		fabric().execute("set TDM_TASK_EXE_ID = " + taskExecutionID);
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		//Date date = new Date();
		
		Db.Rows refTabLst = null;
		
		String refQuery = "Select ES.* from " + TASK_REF_EXE_STATS +" ES, " +  TASKS + " T where execution_status  in ('" + WAITING + "', '" + PENDING + "', '" + RESUME + "', '" + STOPPED + "') " +
		        "and ES.TASK_ID = T.TASK_ID and lower(T.TASK_TYPE) = ? and ES.TASK_EXECUTION_ID = ? order by task_execution_id, task_id";
		
		refTabLst = db(TDM).fetch(refQuery, taskType, taskExecutionID);
		
		int refCount = 0;
		String batchID = "";
		for (Db.Row row : refTabLst) {
		    refCount++;
		    String taskRefTableID = "" + row.get("task_ref_table_id");
		    String refTableName = "" + row.get("ref_table_name");
		    //log.info("fnTdmReference - refTableName: " + refTableName);
		
		    if (!StringUtils.isEmpty(taskRefTableID) && !StringUtils.isEmpty(taskExecutionID)) {
		
		        String luName = "" + db(TDM).fetch("Select lu_name from " + TASK_REF_TABLES + " where task_ref_table_id = ?", taskRefTableID).firstValue();
		
		        String taskID = "" + row.get("task_id");
		
		        String execStatus = "" + row.get("execution_status");
		
		        //log.info("fnTdmReference - execution_status: " + execStatus);
		
		        Db.Row taskParams = db(TDM).fetch("Select l.source_env_name, e.environment_name as target_env_name, t.version_ind, " +
		                        "t.retention_period_type, t.retention_period_value, t.selection_method, t.selected_ref_version_task_name, " +
		                        "t.selected_ref_version_datetime, t.selected_ref_version_task_exe_id " +
		                        "from " + TASKS + " t, " + TASK_EXECUTION_LIST + " l, " + ENVIRONMENTS + " e " +
		                        "where task_execution_id = ? and l.task_id = ? and t.task_id = l.task_id and l.environment_id = e.environment_id",
		                taskExecutionID, taskID).firstRow();
		
		        String versionInd = "false";
		        if (taskParams.get("version_ind") != null) {
		            versionInd = "" + taskParams.get("version_ind");
		        }
		        Object retPeriodType = taskParams.get("retention_period_type");
		        String retType = (retPeriodType != null) ? "" + retPeriodType : "";
		
		        Object retPeriodVal = taskParams.get("retention_period_value");
		        Float retVal = (retPeriodVal != null) ? Float.valueOf(retPeriodVal.toString()) : 0;
		        Integer ttl = getRetention(retType, retVal);
		
		        Db.Row luData = db(TDM).fetch("select p.lu_id, p.lu_dc_name from " + TASKS_LOGICAL_UNITS + " t, "
		                        + PRODUCT_LOGICAL_UNITS + " p where t.lu_name = ? and t.task_id = ? and t.lu_name = p.lu_name and t.lu_id = p.lu_id",
		                luName, taskID).firstRow();
		
		        String luID = "" + luData.get("lu_id");
		        String luDCName = "" + luData.get("lu_dc_name");
		
		        String affinity = "";
		
		        if (luDCName != null && !"".equals(luDCName) && !"null".equals(luDCName)) {
		            affinity = " AFFINITY='" + luDCName + "'";
		        }
		
		        String uid = "" + row.get("job_uid");
		
		        Object selectionMethod = "" + taskParams.get("selection_method");
		
		        if (selectionMethod != null && selectionMethod.toString().equals(REF)) {
		            Long unixTime = System.currentTimeMillis();
		            Long unixTime_plus_retention;
		
		            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
		            Integer retention_in_seconds = getRetention(retType, retVal);
		
		            unixTime_plus_retention = (unixTime / 1000L + retention_in_seconds) * 1000;
		            String versionExpirationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(unixTime_plus_retention);
		
					if (!STOPPED.equals(execStatus) && !RESUME.equals(execStatus)) {
			            Object[] param;
		    	        if (versionInd.equals("true")) {
		        	        param = new Object[]{RUNNING, timeStamp, versionExpirationDate, taskExecutionID, taskID, luID};
		            	} else {
		                	param = new Object[]{RUNNING, null, null, taskExecutionID, taskID, luID};
			            }
		
		    	        //log.info("fnTdmReference - Updating task to running for - task_execution_id: " + taskExecutionID +
		        	    //		", task_id: " + taskID + ", lu_id: " + luID);
		
		            	db(TDM).execute("update " + TASK_EXECUTION_LIST + " set execution_status = ?, " +
		                	    "version_datetime = ?, version_expiration_date = ? " +
		                    	"where task_execution_id = ? and task_id = ? and lu_id = ?", param);
					}
		        }
		
		        String sourceEnv = "" + taskParams.get("source_env_name");
		        String targetEnv = "" + taskParams.get("target_env_name");
		        //log.info("fnTdmReference - sourceEnv: " + sourceEnv + ", targetEnv: " + targetEnv);
		
		        try {
		            switch (execStatus) {
		                case WAITING:
		                case PENDING:
		                    //log.info("fnTdmReference -inside PENDING/WAITING status: " + execStatus);
		                    String updatedBy = "";
							String REFERENCE_LU = "TDM_Reference";
							if (taskType.equalsIgnoreCase("extract")) {
		                        updatedBy = "TDMReferenceExtractor";
								//
								String batchRefCmd = "BATCH " + REFERENCE_LU + ".('" + taskExecutionID + "_" + refTableName + "') " +
		                                "fabric_command=\"broadway " + REFERENCE_LU + ".TDMReferenceExtractor " +
		                                "iid=?, taskExecutionID=" + taskExecutionID + ", luName=" + luName + ", refTableName=" + refTableName +
		                                ", sourceEnvName=" + sourceEnv + ", taskRefTableID=" + taskRefTableID +
										"\" WITH " +  affinity + " ASYNC='true'";
			
		                        //String batchRefCmd = "BATCH " + REFERENCE_LU + ".(?) FABRIC_COMMAND=\"sync_instance " + REFERENCE_LU + ".?\" WITH " + affinity + " ASYNC='true'";
		
		                        //log.info("fnTdmReference - Running batch command: " + loadRefCmd);
		                        batchID = Util.rte(() -> "" + fabric().fetch(batchRefCmd).firstValue());
		
		                    } else {// In case of Load Task
		                        updatedBy = "TDMReferenceLoader";
		                        String selectedRefVersionTaskName = "''";
		                        String selectedRefVersionDateTime = "19700101000000";
		                        String selectedRefVersionTaskExeId = "''";
		                        if (versionInd.equals("true")) {
		                            selectedRefVersionTaskName = "" + taskParams.get("selected_ref_version_task_name");
		                            selectedRefVersionDateTime = "" + taskParams.get("selected_ref_version_datetime");
		                            selectedRefVersionTaskExeId = "" + taskParams.get("selected_ref_version_task_exe_id");
		                        }
		                        String batchRefCmd = "BATCH " + REFERENCE_LU + ".('" + taskExecutionID + "_" + refTableName + "') " +
		                                "fabric_command=\"broadway " + REFERENCE_LU + ".TDMReferenceLoader " +
		                                "iid=?, taskExecutionID=" + taskExecutionID + ", luName=" + luName + ", refTableName=" + refTableName +
		                                ", sourceEnvName=" + sourceEnv + ", targetEnvName=" + targetEnv +
		                                ", selectedRefVersionTaskName='" + selectedRefVersionTaskName + "', selectedRefVersionDateTime=" +
		                                selectedRefVersionDateTime + ", selectedRefVersionTaskExeId=" + selectedRefVersionTaskExeId + 
										"\" WITH " +  affinity + " ASYNC='true'";
		                        //log.info("fnTdmReference - Running batch command: " + loadRefCmd);
		                        batchID = Util.rte(() -> "" + fabric().fetch(batchRefCmd).firstValue());
		
		                    }
		
		                    //log.info("fnTdmReference - Updating batch ID to: " + batchID);
		                    db(TDM).execute("update " + TASK_REF_EXE_STATS + " set job_uid= ?, execution_status = ?, " +
		                                    "start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', updated_by = ?" +
		                                    "where task_execution_id = ? and task_ref_table_id = ?; ",
		                            batchID, RUNNING, "TDMReferenceLoader", taskExecutionID, taskRefTableID);
		                    break;
		                case STOPPED:
							//log.info("Stopping uid: " + uid);
		                    fabric().execute("batch_pause '" + uid + "';");
		                    break;
		                case RESUME:
							//log.info("Resuming uid: " + uid);
							db(TDM).execute("UPDATE " + TASK_REF_EXE_STATS + " set execution_status=? where task_execution_id = ? " +
		                    "and execution_status = 'resume'", RUNNING, taskExecutionID);
							fabric().execute("batch_retry '" + uid + "'" + ";");
		                    //log.info("------------------------------------- After resume case ------------------------------------");
		                    break;
		            }
		        } catch (Exception e) {
		            log.error("Reference Table: <" + refTableName + "> handling had an exception: " + e);
					e.printStackTrace();
		            if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException || e.getMessage().contains("already stopped or completed")) {
		                throw e;
		            }
		            db(TDM).execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
		                            "where task_execution_id = ? and task_ref_table_id = ?; ",
		                    FAILED, e.getMessage(), TDM_REFERENCE, taskExecutionID, taskRefTableID);
		            //log.info("uid to cancel: " + uid);
		            if (!"null".equals(uid)) {
		                fabric().execute("cancel batch '" + uid + "';");
		            }
		        }
		
		    }
		    //log.info("fnTdmReference - Handling next Ref table #" + refCount);
		}
		//log.info("fnTdmReference - After the loop on ref tables, refCount: " + refCount);
		//In case the task type was REF, but included LUs that do not have reference,or none of their reference were chosen, update the status to COMPLETE
		String lusWithoutRefSql = "select l.task_id, l.task_execution_id, l.lu_id " +
		"from " + TASK_EXECUTION_LIST + " l, " + TASKS + " t, " + TASKS_LOGICAL_UNITS + " lu " +
		        "where l.task_id = t.task_id and t.selection_method = 'REF' and l.task_id = lu.task_id and l.lu_id = lu.lu_id and " +
		        "lower(l.execution_status) in ('pending', 'stopped') and " +
		        "not exists (select 1 from " + TASK_REF_TABLES + " r " +
				"where r.task_id = lu.task_id and r.lu_name = lu.lu_name)" ;
		
		String updateLuWithoutRefSql = "UPDATE " + TASK_EXECUTION_LIST + " SET execution_status = ?, num_of_processed_entities = 0, " +
		        "start_execution_time = current_timestamp at time zone 'utc', " +
		        "end_execution_time = current_timestamp at time zone 'utc', num_of_copied_entities = 0, num_of_failed_entities = 0, " +
		        "num_of_processed_ref_tables = 0, num_of_copied_ref_tables = 0, num_of_failed_ref_tables = 0 " +
		        "WHERE  task_id = ? AND task_execution_id = ? and lu_id = ?";
		
		Db.Rows lusWithoutRef = db(TDM).fetch(lusWithoutRefSql);
		
		for (Db.Row luWithoutRef : lusWithoutRef) {
		    String taskId = "" + luWithoutRef.get("task_id");
		    String taskExecID = "" + luWithoutRef.get("task_execution_id");
		    String luId = "" + luWithoutRef.get("lu_id");
		    //log.info("tdmCopyReferenceListener - Setting status to completed for LU without reference to be handled. Task Execution ID: " + taskExecID + ", lu id: " + luId);
		    db(TDM).execute(updateLuWithoutRefSql, COMPLETED, taskId, taskExecID, luId);
		}
		
		if (refTabLst != null) {
		    refTabLst.close();
		}
		
		if (lusWithoutRef != null) {
		    lusWithoutRef.close();
		}
		
		return batchID;
	}
	
	@desc("Get the data of table as Json Array")
	@out(name = "tableRecords", type = JSONArray.class, desc = "")
	public static JSONArray fnGetTableInJson(String interfaceName, String schemaName, String tableName) throws Exception {
		JSONArray tableRecords = new JSONArray();
		try (Connection conn = getConnection(interfaceName)) {
		    Statement select = conn.createStatement();
		
		    ResultSet refTableRS = select.executeQuery("Select * from " + schemaName + "." + tableName);
		    int colsCount = refTableRS.getMetaData().getColumnCount();
		
		    tableRecords = createJsonArrayFromTableRecords(TDM_TASK_EXE_ID, tableName, refTableRS, colsCount);
		}
		return tableRecords;
	}

	

}
