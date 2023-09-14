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

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.TDMDB_SCHEMA;
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
        Timestamp startTime = (Timestamp) Util.rte(() -> db(TDM).fetch("select current_timestamp at time zone 'utc'").firstValue());
        fabric().execute("set TDM_TASK_EXE_ID = " + taskExecutionID);
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        //Date date = new Date();
        
        Db.Rows refTabLst = null;
        
        String refQuery = "Select ES.* from " + TASK_REF_EXE_STATS + " ES, " + TASKS + " T where execution_status  in ('" + WAITING + "', '" + PENDING + "', '" + RESUME + "', '" + STOPPED + "') " +
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
        
                Db.Row taskParams = db(TDM).fetch("Select l.source_env_name, l.data_center_name as dc_name, e.environment_name as target_env_name,e.environment_id as environment_id, t.version_ind, " +
                                "t.retention_period_type, t.retention_period_value, t.selection_method, t.selected_ref_version_task_name, " +
                                "t.selected_ref_version_datetime, t.selected_ref_version_task_exe_id " +
                                "from " + TASKS + " t, " + TASK_EXECUTION_LIST + " l, " + ENVIRONMENTS + " e " +
                                "where task_execution_id = ? and l.task_id = ? and t.task_id = l.task_id and l.environment_id = e.environment_id ",
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
        
                Db.Row luData = db(TDM).fetch("select p.lu_id from " + TASKS_LOGICAL_UNITS + " t, "
                                + PRODUCT_LOGICAL_UNITS + " p where t.lu_name = ? and t.task_id = ? and t.lu_name = p.lu_name and t.lu_id = p.lu_id ",
                        luName, taskID).firstRow();
        
                String luID = "" + luData.get("lu_id");

                String affinity = "";
                String luDCName = "" + taskParams.get("dc_name");
                if (luDCName != null && !"".equals(luDCName) && !"null".equals(luDCName)) {
                    affinity = " AFFINITY='" + luDCName + "'";
                }
        
                String uid = "" + row.get("job_uid");
        
                String selectionMethod = "" + taskParams.get("selection_method");
        
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
                String environment_id = "" + taskParams.get("environment_id");
                String selectedRefVersionTaskName = "";
                String selectedRefVersionDateTime = "19700101000000";
                String selectedRefVersionTaskExeId = "";
                if (versionInd.equals("true")) {
                    selectedRefVersionTaskName = "" + taskParams.get("selected_ref_version_task_name");
                    selectedRefVersionDateTime = "" + taskParams.get("selected_ref_version_datetime");
                    selectedRefVersionTaskExeId = "" + taskParams.get("selected_ref_version_task_exe_id");
                }
        
                try {
                    switch (execStatus) {
                        case WAITING:
                        case PENDING:
                            //log.info("fnTdmReference -inside PENDING/WAITING status: " + execStatus);
                            String updatedBy = "";
                            String REFERENCE_LU = "TDM_Reference";
                            if (taskType.equalsIgnoreCase("extract")) {
                                updatedBy = "TDMReferenceExtractor";
                                String batchRefCmd = "BATCH " + REFERENCE_LU + ".('" + taskExecutionID + "_" + refTableName + "') " +
                                        "fabric_command=\"broadway " + REFERENCE_LU + ".TDMReferenceExtractor " +
                                        "iid=?, taskExecutionID=" + taskExecutionID + ", luName=" + luName + ", refTableName=" + refTableName +
                                        ", sourceEnvName=" + sourceEnv + ", taskRefTableID=" + taskRefTableID +
                                        "\" WITH " + affinity + " ASYNC='true'";
        
                                //String batchRefCmd = "BATCH " + REFERENCE_LU + ".(?) FABRIC_COMMAND=\"sync_instance " + REFERENCE_LU + ".?\" WITH " + affinity + " ASYNC='true'";
        
                                //log.info("fnTdmReference - Running batch command: " + loadRefCmd);
                                batchID = Util.rte(() -> "" + fabric().fetch(batchRefCmd).firstValue());
        
                            } else {// In case of Load Task
                                updatedBy = "TDMReferenceLoader";
                                String batchRefCmd = "BATCH " + REFERENCE_LU + ".('" + taskExecutionID + "_" + refTableName + "') " +
                                        "fabric_command=\"broadway " + REFERENCE_LU + ".TDMReferenceLoader " +
                                        "iid=?, taskExecutionID=" + taskExecutionID + ", luName=" + luName + ", refTableName=" + refTableName +
                                        ", sourceEnvName=" + sourceEnv + ", targetEnvName=" + targetEnv +
                                        ", selectedRefVersionTaskName='" + selectedRefVersionTaskName + "', selectedRefVersionDateTime=" +
                                        selectedRefVersionDateTime + ", selectedRefVersionTaskExeId='" + selectedRefVersionTaskExeId +
                                        "' \" WITH " + affinity + " ASYNC='true'";
                                //log.info("fnTdmReference - Running batch command: " + loadRefCmd);
                                batchID = Util.rte(() -> "" + fabric().fetch(batchRefCmd).firstValue());
        
                            }
        
                            //log.info("fnTdmReference - Updating batch ID to: " + batchID);
                            db(TDM).execute("update " + TASK_REF_EXE_STATS + " set job_uid= ?, execution_status = ?, " +
                                            "start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', updated_by = ? " +
                                            "where task_execution_id = ? and task_ref_table_id = ? ",
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
                    Timestamp endTime = (Timestamp) Util.rte(() -> db(TDM).fetch("select current_timestamp at time zone 'utc'").firstValue());

                    db(TDM).execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ? " +
                                    "where task_execution_id = ? and task_ref_table_id = ? ",
                            FAILED, e.getMessage(), TDM_REFERENCE, taskExecutionID, taskRefTableID);

                    String sql = "update " + TDMDB_SCHEMA + ".task_execution_entities SET lu_name=?, entity_id=?, target_entity_id=?, creation_date=?, entity_end_time=?, entity_start_time=?, env_id=?, execution_status=?, id_type=?, fabric_execution_id=?, iid=?, source_env=?, version_name=?, version_datetime=TO_TIMESTAMP(?,'YYYY-MM-DD HH24:MI:SS'), fabric_get_time=?, total_processing_time=?, clone_no=? " +
							"WHERE task_execution_id = ?";
                    db(TDM).execute(sql, luName, refTableName, refTableName, startTime, endTime, startTime, environment_id, FAILED, "REFERENCE", batchID, refTableName, sourceEnv, selectedRefVersionTaskName, Timestamp.valueOf(selectedRefVersionDateTime), null, null, "NO_CLONE_ID",taskExecutionID);
                    db(TDM).execute("update " + TDMDB_SCHEMA + ".task_execution_entities set total_processing_time = EXTRACT(EPOCH FROM(entity_end_time-entity_start_time)) " +
                            "where task_execution_id = ? and entity_id = ? ", taskExecutionID, refTableName);

                    String errSql= "INSERT INTO " + TDMDB_SCHEMA + ".task_exe_error_detailed(task_execution_id, lu_name, entity_id, iid, target_entity_id, error_category, error_code, error_message, creation_date) " +
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" ;
                    db(TDM).execute(errSql, taskExecutionID, luName, refTableName, refTableName,refTableName,"Reference Failed","",e.getMessage(),startTime);

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
                "where r.task_id = lu.task_id and r.lu_name = lu.lu_name) ";

        String updateLuWithoutRefSql = "UPDATE " + TASK_EXECUTION_LIST + " SET execution_status = ?, num_of_processed_entities = 0, start_execution_time=?,  " +
                "end_execution_time = current_timestamp at time zone 'utc', num_of_copied_entities = 0, num_of_failed_entities = 0, " +
                "num_of_processed_ref_tables = 0, num_of_copied_ref_tables = 0, num_of_failed_ref_tables = 0 " +
                "WHERE  task_id = ? AND task_execution_id = ? and lu_id = ? ";

        Db.Rows lusWithoutRef = db(TDM).fetch(lusWithoutRefSql);

        for (Db.Row luWithoutRef : lusWithoutRef) {
            if(!luWithoutRef.isEmpty()){
                String taskId = "" + luWithoutRef.get("task_id");
                String taskExecID = "" + luWithoutRef.get("task_execution_id");
                String luId = "" + luWithoutRef.get("lu_id");
                //log.info("tdmCopyReferenceListener - Setting status to completed for LU without reference to be handled. Task Execution ID: " + taskExecID + ", lu id: " + luId);
                db(TDM).execute(updateLuWithoutRefSql, COMPLETED, startTime, taskId, taskExecID, luId);
            }
        }
        
        refTabLst.close();
        
        lusWithoutRef.close();
        
        return batchID;
    }

	@desc("Get the data of table as Json Array")
	@out(name = "tableRecords", type = JSONArray.class, desc = "")
	public static JSONArray fnGetTableInJson(String interfaceName, String schemaName, String tableName, int recordsCount,String TaskExeID) throws Exception {
		JSONArray tableRecords = new JSONArray();
		try (Connection conn = getConnection(interfaceName)) {
			DatabaseMetaData metaData = conn.getMetaData();
			String ConnType = metaData.getDatabaseProductName();
			Statement select = conn.createStatement();
			ResultSet refTableRS = select.executeQuery("Select * from " + schemaName + "." + tableName);
			int colsCount = refTableRS.getMetaData().getColumnCount();
			tableRecords = createJsonArrayFromTableRecords(TaskExeID, tableName, refTableRS, colsCount, recordsCount);
		}

		return tableRecords;
	}
	@out(name = "result", type = String.class, desc = "")
	public static String fnGetDBConnector(String interfaceName) throws Exception {
		String result;
		try (Connection conn = getConnection(interfaceName)) {
			DatabaseMetaData metaData = conn.getMetaData();
			result = metaData.getDatabaseProductName();
		
		}
		return result;
	}
	@out(name = "result", type = Collection.class, desc = "")
	public static Set<String> fnGetMapKeys(Map<String, Object> map) throws Exception {
		return map.keySet();
	}

	@out(name = "result", type = Collection.class, desc = "")
	public static Collection<Object> fnGetMapValues(Map<String,Object> map) throws Exception {
		return map.values();
	}
	@out(name = "result", type = java.sql.Timestamp.class, desc = "")
	public static java.sql.Timestamp fnToTimestamp(String columnValue) throws Exception {
		try {
			if (columnValue == null || columnValue.isEmpty()) {
				return null;
			}
			return java.sql.Timestamp.valueOf(columnValue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@out(name = "result", type = java.sql.Time.class, desc = "")
	public static java.sql.Time fnToTime(String columnValue) throws Exception {
		try {
			if (columnValue == null || columnValue.isEmpty()) {
				return null;
			}
			return java.sql.Time.valueOf(columnValue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	@out(name = "result", type = java.sql.Date.class, desc = "")
	public static java.sql.Date fnToDate(String columnValue) throws Exception {
		try {
			if (columnValue == null || columnValue.isEmpty()) {
				return null;
			}
			return java.sql.Date.valueOf(columnValue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@out(name = "result", type = Boolean.class, desc = "")
	public static Boolean fnCheckIfDate(String columnType) throws Exception {
		String cType = columnType.toLowerCase();
		return cType.contains("timestamp") || cType.contains("date");
	}
}
