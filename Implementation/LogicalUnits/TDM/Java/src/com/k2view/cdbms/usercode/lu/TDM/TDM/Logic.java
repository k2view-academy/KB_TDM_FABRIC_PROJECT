/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDM;

import com.google.gson.annotations.SerializedName;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.Utils;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.type;
import com.k2view.cdbms.usercode.common.TDM.SharedLogic;
import com.k2view.cdbms.usercode.lu.TDM.TdmTaskScheduler;
import com.k2view.cdbms.utils.K2TimestampWithTimeZone;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import com.k2view.loader.Loader;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.RootFunction;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.UserJob;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import java.math.*;
import java.io.*;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {

	public static final String CASSANDRA_TEXT_TYPE = "TEXT";
	public static final String CASSANDRA_BIGINT_TYPE = "BIGINT";
	public static final String CASSANDRA_INT_TYPE = "INT";
	public static final String CASSANDRA_DOUBLE_TYPE = "DOUBLE";
	public static final String CASSANDRA_BLOB_TYPE = "BLOB";
	public static final String CASSANDRA_BOOL_TYPE = "BOOLEAN";
	public static final String COMMA_DEL = ",";
	public static final String PENDING = "pending";

	private static final int BATCH_SIZE = 10;
	public static final String REF = "REF";
	public static final String TASKS = "TASKS";
	public static final String TDM = "TDM";
	public static final String DBCASSANDRA = "DB_CASSANDRA";
	//Fabric 5.3 feature, USER_JOB runs on FABRIC DB - "fabric"
	// public static final String DB_FABRIC = "dbFabric";
	public static final String DB_FABRIC = "fabric";
	public static final String TASK_REF_TABLES = "TASK_REF_TABLES";
	public static final String PRODUCT_LOGICAL_UNITS = "product_logical_units";
	public static final String TASK_REF_EXE_STATS = "TASK_REF_EXE_STATS";
	public static final String TASKS_LOGICAL_UNITS = "tasks_logical_units";
	public static final String RUNNING = "running";
	public static final String WAITING = "waiting";
	public static final String STOPPED = "stopped";
	public static final String RESUME = "resume";
	public static final String TASK_EXECUTION_LIST = "task_execution_list";
    public static final String FAILED = "failed";
    public  static final String COMPLETED = "completed";
	public static final String TDM_COPY_REFERENCE = "fnTdmCopyReference";
	public static final String TDM_COPY_REF_TABLES_FOR_TDM = "tdmCopyRefTablesForTDM";
	
	@out(name = "instanceId", type = String.class, desc = "")
	@out(name = "envName", type = String.class, desc = "")
	public static Object fnGetSplittedID(String entityID, String idType, String envID) throws Exception {
		
		if ("ENTITY".equals(idType)) {
			Object[] res = fnSplitUID(entityID);
			return res;
		} 
		
		Object[] res = {entityID, envID};
		return res;
		
		
		
	}


	@desc("check whether task of EXTRACT type has finished its migrate.\r\n" +
			"if yes - update TASK_EXECUTION_LIST with the status and migration information.\r\n" +
			"It will also clean redis for load tasks.")
	@type(UserJob)
	public static void fnCheckMigrateAndUpdateTDMDB() throws Exception {
		// TDM 5.1- fix the query- check the task_type instead of the fabric_execution_id, since a reference only task does not have the fabric_execution_id (= migrate id)
		String selectFromTaskExecutionListSql = "Select tel.fabric_execution_id, tel.task_id, tel.lu_id, tlu.lu_name, task_type," +
				"tel.process_id, tpost.process_name, tel.task_execution_id from public.task_execution_list tel\n" +
				"left join public.tasks_logical_units tlu on tel.task_id = tlu.task_id And tel.lu_id = tlu.lu_id " +
				"left join public.tasks_post_exe_process tpost On tel.task_id = tpost.task_id " +
				"and tel.process_id = tpost.process_id Where Lower(tel.execution_status) = 'running'";
		//"Select fabric_execution_id, task_id, task_execution_id from task_execution_list where lower(execution_status) = 'running' and fabric_execution_id is not null";
		
		//ResultSetWrapper taskExecutionList = null;
		Db.Rows taskExecutionList = null;
		Db.Rows rows = null;
		Db.Rows refRunningLus = null;
		Boolean res = false;
		
		
		try {
			//log.info("fnCheckMigrateAndUpdateTDMDB- get running extract tasks");
			taskExecutionList = db("TDM").fetch(selectFromTaskExecutionListSql);
		
			for (Db.Row row : taskExecutionList) {
				//5.1 - Add Try - catch inside the loop to allow handling each iteration seprately
				// and allow continuation of the loop
				Date taskStartDate = new Date(0);
				Date taskEndDate = new Date();
				Integer num_of_processed_ref_tables = 0;
				Integer num_of_copied_ref_tables = 0;
				Integer num_of_failed_ref_tables = 0;
				Integer num_of_incomplete_ref_tables = 0;
				String taskID = "";
				String taskExecutionID = "";
				String updateTaskExecutionListSql = "UPDATE task_execution_list SET execution_status = ?, num_of_processed_entities = ?, " +
					"num_of_copied_entities = ?, num_of_failed_entities = ?, start_execution_time = ?, end_execution_time = ?, " +
					"num_of_processed_ref_tables = ?, num_of_copied_ref_tables = ?, num_of_failed_ref_tables = ? " +
					"WHERE task_id = ? AND task_execution_id = ? and ((lu_id > 0 and lu_id = ?) or (process_id > 0 and process_id = ?) )";
				String status = COMPLETED;
				
				Long luID = 0L;
				Long processID = 0L;
				String luName = "";
				String taskType = "";
				try {
					// TDM 5.1- add a check if row[0] is null (will be null for reference only tasks)
					String batchID = null;
					
					if (row.get("fabric_execution_id") != null)
						batchID = "" + row.get("fabric_execution_id");
		
					taskID = "" + row.get("task_id");;
					taskExecutionID = "" + row.get("task_execution_id");
					
					// TDM 6.0 - Since the extract tasks can include multi LUs, each extract task execution will have multi entries;
					// entry per LU, therefore we need to check the data of each LU seperately
					luID = (Long) row.get("lu_id");
					processID = (Long) row.get("process_id");
					luName = "" + row.get("lu_name");
					// TDM 7.0 - Since the this job also handles Load tasks, get the task_type
					taskType = "" + row.get("task_type");
					//log.info("Procssing LU_NAME: " + luName + ", Migrate ID: " + batchID);
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
					// TDM 5.1- Tali- Fix- get the selection_method from TASKS. If this selection_method is REF- do not call the migrate_summary command, 
					//but check the reference status instead
		
					String selectionMethod = "" + db("TDM").fetch("Select selection_method from " + TASKS + " where task_id=?", taskID).firstValue();
					
					Integer totNoOfRefTables = 0;
					// TDM 5.1- add the update of the reference tables fields
					//log.info("selectionMethod: " + selectionMethod);
					if (selectionMethod != null && selectionMethod.equals(REF)) {
						//log.info("fnCheckMigrateAndUpdateTDMDB- handle reference only task");
						Map<String, Object> refSummaryStatsBuf = fnGetReferenceSummaryData(taskExecutionID);
						//log.info("Getting refSummaryStats for luName: " + luName);
						HashMap <String, Object> refSummaryStats = (HashMap <String, Object>)refSummaryStatsBuf.get(luName);
						if (refSummaryStats != null) {
							totNoOfRefTables = (Integer) refSummaryStats.get("totNumOfTablesToProcess");
		
							//log.info("fnCheckMigrateAndUpdateTDMDB- totNoOfRefTables: " + totNoOfRefTables + " for task execution id: " + taskExecutionID);
							if (totNoOfRefTables > 0) // if the task has reference
							{
								num_of_incomplete_ref_tables = (Integer) refSummaryStats.get("numOfProcessingRefTables") + (Integer) refSummaryStats.get("numberOfNotStartedRefTables");
								//log.info("num of incomplete ref tables: " + num_of_incomplete_ref_tables);
		
								if (num_of_incomplete_ref_tables == 0) { // the processing of the reference tables is not completed yet
								
		
									// If the execution of the reference tables ended- get the summary information for the referenece tables
									Date refMinDate = new Date(0);
									Date refMaxDate = new Date(0);
									//log.info("minStartExecutionDate: " + refSummaryStats.get("minStartExecutionDate"));
									//log.info("maxEndExecutionDate: " + refSummaryStats.get("maxEndExecutionDate"));
									if (!"".equals(refSummaryStats.get("minStartExecutionDate"))) {
										refMinDate = (Date) refSummaryStats.get("minStartExecutionDate");
									}
									if (!"".equals(refSummaryStats.get("maxEndExecutionDate"))) {
										refMaxDate = (Date) refSummaryStats.get("maxEndExecutionDate");
									}
		
									num_of_processed_ref_tables = (Integer) refSummaryStats.get("numOfProcessedRefTables");
									num_of_copied_ref_tables = (Integer) refSummaryStats.get("numOfCopiedRefTables");
									num_of_failed_ref_tables = (Integer) refSummaryStats.get("numOfFailedRefTables");
		
									if (num_of_copied_ref_tables == 0)
										status = FAILED;
		
									// TDM 5.1- change the start and end date parameters and add the parameters for the reference tables
									//log.info("Updating task status to failed as no reference table was copied");
									db("TDM").execute(updateTaskExecutionListSql, status, 0, 0, 0, refMinDate.toString(), 
										refMaxDate.toString(), num_of_processed_ref_tables, num_of_copied_ref_tables, 
										num_of_failed_ref_tables, taskID, taskExecutionID, luID, processID);
								}
							} // if(totNoOfRefTables > 0)
						}//if (refSummaryStats != null)
						//Add reference tables to TASK_EXECUTION_ENTITIES
						//log.info("Calling fnTdmUpdateTaskExecutionEntities for reference only task");
						if(luID > 0){
							fnTdmUpdateTaskExecutionEntities(taskExecutionID, luID, luName);
						}else if(processID != null && processID > 0){
							String finalTaskExecutionID = taskExecutionID;
							Long finalProcessID = processID;
							Util.rte(()-> db(TDM).execute("UPDATE public.task_execution_list SET execution_status=?, start_execution_time = (now() at time zone 'utc') WHERE task_execution_id=? and process_id=?", "completed", finalTaskExecutionID, finalProcessID));
						}
					}// if(selectionMethod != null && selectionMethod.equals(REF))
					else // the task contains entities (but can still have reference tables in addition to the entities)
					{
					
						Map<String, Object> batchStats = fnBatchStats(batchID);
						//log.info("fnCheckMigrateAndUpdateTDMDB - Returned output from fnRunBatchSummary: " + batchStats);
						
						if (batchStats != null) // response from migrate_summary command (which is run in wsMigrateStats ) should be returned with all levels (Node,DC,Cluster). if its returned without level DC - skip the update command and wait to next time the user job will be executed
						{
							status = getBatchStatus(batchStats.get("Status"));
		
							//log.info("fnCheckMigrateAndUpdateTDMDB- migration status for task execution id " + taskExecutionID + " is: " + status);
							// In case there are reference table to be handled, if the refernece job has finished
							String sqlrefSts = "select distinct lu_name from task_ref_exe_stats es, task_ref_tables rt where " +
								"lower(execution_status) in ('waiting', 'running', 'pending') and " +
								"task_Execution_id = ? and es.task_id = rt.task_id and es.task_ref_table_id = rt.task_ref_table_id " +
								"group by lu_name";
							
							refRunningLus = db("TDM").fetch(sqlrefSts, taskExecutionID);
							
							Boolean refStillRunning = false;
							for (Db.Row refRunningLu : refRunningLus) {
								if (luName.equals(refRunningLu.cell(0))) {
									refStillRunning = true;
									break;
								}
							}
							
							//log.info("fnCheckMigrateAndUpdateTDMDB - luName: " + luName + ", refStillRunning: " + refStillRunning +
							//	", status: " + status);
							// Tali- fix defect- add the check that status is not "generate_iid_list"
							// Taha - check that all refernece tables were copied
							if (!status.equalsIgnoreCase(RUNNING) && !status.equalsIgnoreCase("generate_iid_list") 
									&& !status.equalsIgnoreCase("new") && !status.equalsIgnoreCase("WAITING_FOR_JOB")
									&& !refStillRunning) {
								String total = "" + batchStats.get("Total");
								String failed = "" + batchStats.get("Failed");
								
								//If all the entities failed, then set the status of the task to failed
								if (failed.equals(total) && Long.parseLong(total) > 0) {
									status = FAILED;
								}
								
								//log.info("fnCheckMigrateAndUpdateTDMDB - total: " + total + ", failed: " + failed);
								String copied = String.valueOf(Integer.parseInt(total) - Integer.parseInt(failed));
		
								String start_time = "" + batchStats.get("Start time");
								String end_time = "";
								if (!FAILED.equals(status)) {
										end_time = "" + batchStats.get("End time");
								}
		
								// TDM 5.1- add handle of reference tables
		
								//log.info("fnCheckMigrateAndUpdateTDMDB- start time of batch: " + start_time + " , end time of batch: " + end_time);
		
								taskStartDate = formatter.parse(start_time);
								if (!FAILED.equals(status)) {
									taskEndDate = formatter.parse(end_time);
								} 
		
								Map<String, Object> refSummaryStatsBuf = fnGetReferenceSummaryData(taskExecutionID);
								
								//for (Object map : refSummaryStatsBuf.values()) {
								//	HashMap <String, Object> refSummaryStats = (HashMap <String, Object>)map;
								num_of_processed_ref_tables = 0;
								num_of_copied_ref_tables = 0;
								num_of_failed_ref_tables = 0;
								num_of_incomplete_ref_tables = 0;
								
								//log.info("Getting refSummaryStats for luName: " + luName);
								HashMap <String, Object> refSummaryStats = (HashMap <String, Object>)refSummaryStatsBuf.get(luName);
								if (refSummaryStats != null) {
		
									totNoOfRefTables = (Integer) refSummaryStats.get("totNumOfTablesToProcess");
									
									//log.info("refSummaryStats - totNumOfTablesToProcess " + totNoOfRefTables + " for luName: " + luName);
		
									//log.info("fnCheckMigrateAndUpdateTDMDB- totNoOfRefTables: " + totNoOfRefTables + " for task execution id: " + taskExecutionID);
									if (totNoOfRefTables > 0) // if the task has reference
									{
										num_of_incomplete_ref_tables = (Integer) refSummaryStats.get("numOfProcessingRefTables") + (Integer) refSummaryStats.get("numberOfNotStartedRefTables");
		
										//log.info("num of incomplete ref tables: " + num_of_incomplete_ref_tables);
		
										if (num_of_incomplete_ref_tables > 0) // the processing of the reference tables is not completed yet
											continue;
		
										// If the execution of the reference tables ended- get the summary information for the refernece tables
		
										//log.info("fnCheckMigrateAndUpdateTDMDB - PARSING THE DATES OF REF TABLES");
										Date refMinDate = new Date(Long.MAX_VALUE);
										Date refMaxDate = new Date(Long.MIN_VALUE);
										if (!"".equals(refSummaryStats.get("minStartExecutionDate"))) {
											refMinDate = (Date) refSummaryStats.get("minStartExecutionDate");
										}
										if (!"".equals(refSummaryStats.get("maxEndExecutionDate"))) {
											refMaxDate = (Date) refSummaryStats.get("maxEndExecutionDate");
										}
		
										num_of_processed_ref_tables = (Integer) refSummaryStats.get("numOfProcessedRefTables");
										num_of_copied_ref_tables = (Integer) refSummaryStats.get("numOfCopiedRefTables");
										num_of_failed_ref_tables = (Integer) refSummaryStats.get("numOfFailedRefTables");
										//log.info("luName: " + luName + " - numOfProcessedRefTables: " + num_of_processed_ref_tables);
										//log.info("numOfCopiedRefTables: " + num_of_copied_ref_tables + ", numOfFailedRefTables: " + num_of_failed_ref_tables);
		
										// Check the refMinDate adn the refMaxDate against the start and end execution date of the migrate of entities
		
										if (taskStartDate.after(refMinDate))
											taskStartDate = refMinDate;
		
										if (taskEndDate.before(refMaxDate))
											taskEndDate = refMaxDate;
		
									} // end of if the task has reference tables
								}//if (refSummaryStats != null)
								
								// Taha - 16-Sep-19 - TDM5.6.0 - Add entries to TDM table TASK_EXECUTION_ENTITIES
								//log.info("fnCheckMigrateAndUpdateTDMDB - Calling fnTdmUpdateTaskExecutionEntities for LU_NAME: " + luName);
								//TDM 7.0, For Load tasks, the task_execution_entities table is populated by the BF, therefore handling only Extract tasks
								if ("extract".equalsIgnoreCase(taskType)) {
									fnTdmUpdateTaskExecutionEntities(taskExecutionID, luID, luName);
								}
								
								//TDM 6.1.1 - 20-may-20 - Insert the migrate errors to the TDM DB Error table
								//log.info("fnCheckMigrateAndUpdateTDMDB - Calling fnUpdateTaskErrorsDetails for LU_NAME: " + luName);
								//TDM 7.0, For Load tasks, the error details table is populated by the BF, therefore handling only Extract tasks
								if ("extract".equalsIgnoreCase(taskType) && !Util.isEmpty(luName)) {
									fnUpdateTaskErrorsDetails(taskExecutionID, luName, batchID);
								}
								//log.info("fnCheckMigrateAndUpdateTDMDB - finished updating TASK_EXECUTION_ENTITIES");
								
								// TDM 5.1- change the start and end date parameters and add the parameters for the reference tables
								//log.info("Updating task for luName: " + luName + " to status: " + status + " with:");
								//log.info("numOfProcessedRefTables: " + num_of_processed_ref_tables);
								//log.info("numOfCopiedRefTables: " + num_of_copied_ref_tables + ", numOfFailedRefTables: " + num_of_failed_ref_tables);
								
								
								db("TDM").execute(updateTaskExecutionListSql, new Object[]{status, total, copied, failed, taskStartDate.toString(), 
										taskEndDate.toString(), num_of_processed_ref_tables, num_of_copied_ref_tables, num_of_failed_ref_tables, 
										taskID, taskExecutionID, luID, processID});
								//log.info("fnCheckMigrateAndUpdateTDMDB - Updated the status");
							} // end of if status is not running
						}// end if if ( (batchStats).contains("\"Level\" : \"Cluster\"") )
					} // end of else (if the selection method is not 'REF')
				} catch (Exception e){
					log.error("Task Failed");
					log.error(e.getMessage(),e);
					status = FAILED;
					db("TDM").execute(updateTaskExecutionListSql, new Object[]{status, "0", "0", "0", taskStartDate.toString(), taskEndDate.toString(), num_of_processed_ref_tables, num_of_copied_ref_tables, num_of_failed_ref_tables, taskID, taskExecutionID, luID, processID});
					throw e;
				}
			} // end of for loop on the task_execution_list
		} finally {
			if (taskExecutionList != null){
				taskExecutionList.close();
			}
		}
		
		// Tali- 15-Oct-19- add the get into TDM for all completed tasks
		// In case of extract task with multi LUs, if it was stopped, the TDM LU will be updated only when all LUs are completed.
		String sqlCompletedTasks= "select distinct task_execution_id, upper(task_type) as task_type, clean_redis from task_execution_list out where  " + 
		"synced_to_fabric = FALSE "+ 
		"and not exists (select 1 from task_execution_list tbl, tasks t where tbl.task_execution_id = out.task_execution_id " +  
		"and t.task_id = tbl.task_id " +
		"and ( ( t.task_type = 'LOAD' and tbl.execution_status not in ('completed','failed','killed') )" +
			  "OR ( t.task_type = 'EXTRACT' and tbl.execution_status not in ('completed','failed','killed') ) ) )";
		
		String taskExecutionId= "";
		String taskType="";
		Boolean cleanRedis = false;
		
		try
		{
			rows = db("TDM").fetch(sqlCompletedTasks);
		
			for (Db.Row row:rows){
				// TALi- 25-Sep-19- TDM 5.5- add the clean of redis for completed load tasks
				if(row.cell(0) != null)
				{
					taskExecutionId = row.cell(0).toString();
					taskType = row.cell(1).toString();
					cleanRedis=Boolean.valueOf(row.cell(2).toString());
					//log.info("fnCheckMigrateAndUpdateTDMDB - Loading task: " + taskExecutionId + " to TDM");
					try
					{
						// Get the task into the TDM LU
						fabric().execute("get TDM." + taskExecutionId);
						db("TDM").execute("update task_execution_list set synced_to_fabric=TRUE where task_execution_id = ?", taskExecutionId );
					}catch(Exception e){
						log.error("Failed to get task execution id: " + taskExecutionId + " into TDM LU and update synced_to_fabric indicator of TDM DB. Error message: " + e.getMessage(),e);
					}
		
					if (taskType.equalsIgnoreCase("EXTRACT")) {
						// Refresh the LU Params Materialized View
						// For each LU Param view asssociated with this LU...
						db("TDM").fetch("" +
								"WITH business_entities AS ( " +
								"   SELECT DISTINCT 'lu_relations_' || be.be_name || '_' || tel.source_env_name as view_name  " +
								"   FROM            public.task_execution_list tel  " +
								"              JOIN public.tasks_logical_units tlu   ON (tel.task_id = tlu.task_id) " +
								"              JOIN public.product_logical_units plu ON (tlu.lu_name = plu.lu_name) " +
								"              JOIN public.business_entities be      ON (plu.be_id = be.be_id)  " +
								"   WHERE           tel.task_execution_id=? " +
								") " +
								"SELECT bes.view_name   " +
								"FROM   business_entities bes   " +
								"  JOIN pg_matviews ON (bes.view_name = pg_matviews.matviewname)",taskExecutionId).each(r-> {
		
							// Refresh the view
							db("TDM").execute(String.format("REFRESH MATERIALIZED VIEW public.\"%s\"",r.get("view_name")));
						});
					}
				}
			}
		}catch(Exception e)
		{
			log.error("Failed to clean redis for load task execution id: " + taskExecutionId + ". Error message: " + e.getMessage(),e);
			throw e;
			
		} finally {
			if (taskExecutionList != null) {
				taskExecutionList.close();
			}
			
			if (refRunningLus != null) {
				refRunningLus.close();
			}
			
			if (rows != null) {
				rows.close();
			}
		}
	}

	// This function will add entities of child LUs to TDM table TASK_EXECUTION_ENTITIES


	@type(UserJob)
	public static void tdmCopyRefTablesForTDM(String sourceEnv, String ttl, Boolean versionID, String taskExecID, String taskRefTableID, String luName) throws Exception {
		//log.info("tdmCopyRefTablesForTDM - task_ref_table_id: " + taskRefTableID + "task exection id: " + taskExecID);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		
		String errorCategory = "Rejected Reference Table Extract";
		
		//TDM 6.1.1 - sql to insert data into TDMDB error table
		String insertSql = "insert into TASK_EXE_ERROR_DETAILED (TASK_EXECUTION_ID,LU_NAME,ENTITY_ID,IID,TARGET_ENTITY_ID, ERROR_MESSAGE, ERROR_CATEGORY) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?)";
		
		
		//log.info("tdmCopyRefTablesForTDM - sourceEnv = " + sourceEnv);
		//DBExecute(DB_FABRIC, "set environment='" + sourceEnv + "'", null);
		fabric().execute("set environment='" + sourceEnv + "'");
		
		//log.info("tdmCopyRefTablesForTDM- input task_ref_table_id: " + taskRefTableID+ ", task execution id: " + taskExecID);
		
		if(StringUtils.isEmpty(taskRefTableID)){
			throw new RuntimeException("task_ref_table_id is empty");
		}
		
		Db.Row rs = db(TDM).fetch("Select ref_table_name,schema_name,interface_name from " + TASK_REF_TABLES + "  where task_ref_table_id = ? and lu_name = ?", 
									Integer.valueOf(taskRefTableID), luName).firstRow();
		
		String tableName = "";
		String schemaName = "";
		String iface = "";
		if (!rs.isEmpty()) {
			tableName = "" + rs.cell(0);
			schemaName = "" + rs.cell(1);
			iface = "" + rs.cell(2);
		}
		
		//log.info("tdmCopyRefTablesForTDM - tableName: " + tableName);
		//log.info("tdmCopyRefTablesForTDM - schemaName: " + schemaName);
		//log.info("tdmCopyRefTablesForTDM - iface: " + iface);
		
		Object countObj=null;
		String count = "0";
		String job_uid = taskExecID + "_" + taskRefTableID;
		
		try {
		
		//countObj = DBSelectValue(iface, "Select count(*) from " + schemaName + "." + tableName, new Object[]{});
			countObj = db(iface).fetch("Select count(*) from " + schemaName + "." + tableName).firstValue();
		//if(countObj!= null)	
			//log.info("tdmCopyRefTablesForTDM - number of records for table: " + schemaName+ "." + tableName + " and interface: " + iface + " is: " + countObj.toString());	
			
		} catch (SQLException e){
			log.error(e.getMessage(), e);
			
			db(TDM).execute("Update " + TASK_REF_EXE_STATS + " set job_uid = ?, execution_status = ?, " + 
					"number_of_records_to_process = ?, start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
					" where task_execution_id = ? and task_ref_table_id = ?; ",
				job_uid, FAILED, count, e.getMessage(), TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID);
			
			// TDM 6.1.1, 20-may-20, Add entry to TDM DB error table
			db(TDM).execute(insertSql, taskExecID, luName, tableName, tableName, tableName, e.getMessage(), errorCategory);
			
			return;
		}
		
		//log.info("tdmCopyRefTablesForTDM - No of records for table name: " + tableName + " is: " + count);
		count = countObj != null ? countObj.toString() : "0";
		
		if(Integer.valueOf(count) == 0){
			
			//log.info("tdmCopyRefTablesForTDM- update status to failure- empty source table: " + tableName);
		
			db(TDM).execute("Update " + TASK_REF_EXE_STATS + " set job_uid = ?, execution_status = ?, number_of_records_to_process = ?, " + 
					"start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
					" where task_execution_id = ? and task_ref_table_id = ?; ",
				job_uid, FAILED, count, " Source table is empty", TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID);
			
			// TDM 6.1.1, 20-may-20, Add entry to TDM DB error table
			db(TDM).execute(insertSql, taskExecID, luName, tableName, tableName, tableName, "Empty Source Table", errorCategory);
			
			//log.info("tdmCopyRefTablesForTDM- after update, exiting function");
			return;
		}else{
			//log.info("tdmCopyRefTablesForTDM - Updating status to Running");
			db(TDM).execute("update " + TASK_REF_EXE_STATS + " set job_uid= ?, execution_status = ?, " +
					"start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', number_of_records_to_process = ?, updated_by = ?" +
					"where task_execution_id = ? and task_ref_table_id = ?; ",
				job_uid, RUNNING, count, TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID);
		
		}
		
		try (Connection conn = getConnection(iface)) {
			Statement select = conn.createStatement();
		
			ResultSet refTableRS = select.executeQuery("Select * from " + schemaName + "." + tableName);
			ResultSetMetaData metaData = refTableRS.getMetaData();
		
		
			String table = "";
			//String clusterID = (String) DBSelectValue(DB_FABRIC, "clusterid", null);
			String clusterID = (String) fabric().fetch("clusterid").firstValue();
			if (clusterID == null || clusterID.isEmpty()) {
				table = "k2view_tdm." + tableName;
			} else {
				table = "k2view_tdm_" + clusterID + "." + tableName;
			}
		
			StringBuilder insertStmt = new StringBuilder("INSERT INTO ").append(table).append(" ( source_env_name, tdm_task_execution_id, rec_id, ");
			StringBuilder insertPrepare = new StringBuilder();
		
			insertPrepare.append("?,?,?");
		
			int colsCount = metaData.getColumnCount();
			if (colsCount > 0) {
				insertPrepare.append(COMMA_DEL);
			}
			for (int i = 1; i <= colsCount; i++) {
				String colName = metaData.getColumnName(i);
				insertStmt.append(colName);
				insertPrepare.append("?");
				if (i < colsCount) {
					insertStmt.append(COMMA_DEL);
					insertPrepare.append(COMMA_DEL);
				}
			}
		
			if (!ttl.isEmpty() && !ttl.equals("0")) {
				insertStmt.append(") VALUES (").append(insertPrepare).append(") USING TTL ").append(ttl);
			} else {
				insertStmt.append(") VALUES (").append(insertPrepare).append(")");
			}
		
			//log.info("tdmCopyRefTablesForTDM - insertStmt: " + insertStmt.toString());
		
			insertRefDataToCassandra(sourceEnv, taskExecID, versionID, taskRefTableID, tableName, refTableRS, insertStmt, colsCount);
		}catch (Exception e) {
			log.error(e.getMessage());
			db(TDM).execute( "update " + TASK_REF_EXE_STATS + " set job_uid= ?, execution_status = ?, " +
					"start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC',  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', " +
					"number_of_records_to_process = ?, updated_by = ? " +
					"where task_execution_id = ? and task_ref_table_id = ?; ",
				job_uid, FAILED, 0, TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID);
			
			// TDM 6.1.1, 20-may-20, Add entry to TDM DB error table
			db(TDM).execute(insertSql, taskExecID, luName, tableName, tableName, tableName, e.getMessage(), errorCategory);
		}
	}


	@desc("This user job validates for reference tables defined in \r\ntranslation table trnRefList, the following:\r\n1. The table exits in Cassandra DB, if not then create it\r\n2. If the table already exists then check if the structure \r\n  is up to date and if not drop and recreate the table.\r\nThe function will also expire entries related to each \r\nrebuilt table in task_execution_list")
	@type(UserJob)
	public static void fnValidateAndRebuildRefTables() throws Exception {
		Map<String,Map<String, String>> trnRefListValues = getTranslationsData("trnRefList");
		
		String reTableName;
		String schemaName;
		String interfaceName;
		
		for(String index: trnRefListValues.keySet()){
			Map<String, String> valMap = trnRefListValues.get(index);
		
			reTableName = valMap.get("reference_table_name");
			schemaName = valMap.get("schema_name");
			interfaceName = valMap.get("interface_name");
		
			//log.info("Calling createCassandraTable with: interfaceName: " + interfaceName +
			//		" schemaName: " + schemaName + " reTableName: " + reTableName);
			createCassandraTable(interfaceName, schemaName, reTableName);
		}
	}

	private static void insertRefDataToCassandra(String sourceEnv, String taskExecID, Boolean versionID, String taskRefTableID, String tableName, ResultSet refTableRS, StringBuilder insertStmt, int colsCount) throws SQLException, ExecutionException, InterruptedException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		int processedCounter = 0;
		AtomicInteger counter = new AtomicInteger(0);
		AtomicBoolean failed = new AtomicBoolean(false);
		Loader loader = buildLoader(counter, failed, tableName);

		int rec_id = 1;
		while (refTableRS.next()) {
			if (failed.get()) {
				//DBExecute("TDM", "update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?, updated_by = ? " +
				//		"where task_execution_id = ? and task_ref_table_id = ?; ", new Object[]{FAILED, " failed to insert " , TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID});
				db("TDM").execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?, updated_by = ? " +
					"where task_execution_id = ? and task_ref_table_id = ?; ", FAILED, " failed to insert " , TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID);

				throw new RuntimeException("failed to insert... ");
			}
			//log.info("colsCount: " + colsCount);

			Object[] row = new Object[colsCount + 3];
			row[0] = sourceEnv;
			row[1] = versionID ?  taskExecID : "ALL";
			row[2]= String.valueOf(rec_id++);

			for (int j = 1; j <= colsCount; j++) {
				//log.info("type: " + refTableRS.getMetaData().getColumnTypeName(j).toLowerCase());
				row[j+2] = typeCheck(refTableRS.getObject(j));
				//log.info("val: " + row[j]);
			}
			//log.info("TEST=- insert statement for loader: " + insertStmt.toString() + Arrays.toString(row));
            try {
                loader.submit(insertStmt.toString(), row);
                //Thread.sleep(5000);
            }
            catch(Exception e) {
				//DBExecute("TDM", "Update " + TASK_REF_EXE_STATS + " set execution_status = ?, end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?, number_of_records_to_process=?, number_of_processed_records=?, updated_by = ? " +
				//		"where task_execution_id = ? and task_ref_table_id = ?; ", new Object[]{FAILED, e.getMessage(), null, null, TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID});
				db("TDM").execute("Update " + TASK_REF_EXE_STATS + " set execution_status = ?, end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?, " +
						"number_of_records_to_process=?, number_of_processed_records=?, updated_by = ? " +
						"where task_execution_id = ? and task_ref_table_id = ?; ", 
						FAILED, e.getMessage(), null, null, TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID);

				throw new RuntimeException(e.getMessage());
            }
			processedCounter++;
			//DBExecute("TDM", "Update " + TASK_REF_EXE_STATS + " set number_of_processed_records = ?, updated_by = ?" +
			//		"where task_execution_id = ? and task_ref_table_id = ?; ", new Object[]{processedCounter, TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID});
			db("TDM").execute("Update " + TASK_REF_EXE_STATS + " set number_of_processed_records = ?, updated_by = ?" +
					"where task_execution_id = ? and task_ref_table_id = ?; ", processedCounter, TDM_COPY_REF_TABLES_FOR_TDM, taskExecID, taskRefTableID);
		}
		loader.join();
		loader.close();

		//DBExecute("TDM", "update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC' , updated_by = ?, error_msg = ?" +
		//		"where task_execution_id = ? and task_ref_table_id = ? and execution_status != ?; ", new Object[]{COMPLETED, TDM_COPY_REF_TABLES_FOR_TDM, null, taskExecID, taskRefTableID, FAILED});
		db("TDM").execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC' , updated_by = ?, error_msg = ?" +
				"where task_execution_id = ? and task_ref_table_id = ? and execution_status != ?; ", COMPLETED, TDM_COPY_REF_TABLES_FOR_TDM, null, taskExecID, taskRefTableID, FAILED);
	}

	private static boolean isCommonlyUsedType(Object val) {
		// Cover all cases of Integer, Decimal, Double, Float etc...
		if (val instanceof Number) {
			return true;
		}
		if (val instanceof String) {
			return true;
		}
		return false;
	}

	public static Object typeCheck(Object val) {
		try {

			if (val instanceof Utils.NullType || val == null) {
				return null;
			}

			// Check first for the commonly used types to save all other 'instanceof'.
			if (isCommonlyUsedType(val)) {
				Class cls = val.getClass();
				//log.info("val instanceof " + cls.getName());
				if (val instanceof java.math.BigDecimal){
					return  ((Date) val).getTime();
				}else{
					return val;
				}
			}

			if (val instanceof K2TimestampWithTimeZone) {
				//log.info("val " + val + "instanceof K2TimestampWithTimeZone ");
				return ((Date) val).getTime();
				/*return StringUtils.isBlank(TypeConversion.DATETIME_WITH_TIMEZONE_FORMAT) ? val.toString()
						:((K2TimestampWithTimeZone) val).formatWithTZ(TypeConversion.DATETIME_WITH_TIMEZONE_FORMAT);*/
			}

			if (val instanceof java.sql.Timestamp) {
				return ((Date) val).getTime();
			}

			if (val instanceof java.sql.Date) {
				return ((Date) val).getTime();
				/*return StringUtils.isBlank(DATE_FORMAT) ? val.toString()
						: new SimpleDateFormat(DATE_FORMAT).format((java.sql.Date) val);*/
			}

			if (val instanceof java.sql.Time) {
				return ((Date) val).getTime();
				/*return StringUtils.isBlank(TIME_FORMAT) ? val.toString()
						: new SimpleDateFormat(TIME_FORMAT).format((java.sql.Time) val);*/
			}

			if (val instanceof java.util.Date) {
				//log.info("val " + val + " instanceof java.util.Date");
				return ((Date) val).getTime();
				/*return StringUtils.isBlank(DATETIME_FORMAT) ? val.toString()
						: new SimpleDateFormat(DATETIME_FORMAT).format((java.util.Date) val);*/
			}

			if (val instanceof Blob) {
				//return ((Blob) val).getBytes(1, (int) ((Blob) val).length());
				return ((ByteBuffer)val).array();
			}

			if (val instanceof Clob) {
				return Utils.clobToString(((Clob) val));
			}

			if (val instanceof ByteBuffer){
				return ((ByteBuffer)val).array();
			}

			return val;
		} catch (Exception e) {
			log.warn(e);
			return null;
		}
	}


	@desc("This function will populate field ROOT_ENTITY_STATUS \r\n" +
			"in TASK_EXECUTION_LINK_ENTITIES table.")
	public static void fnEnrSetRootEntSts() throws Exception {
		// TDM 6.0  - 11-Sep-19 - New enrichment function to populate ROOT_ENTITY_STATUS in TASK_EXECUTION_LINK_ENTITIES
		String updateSql1 = "Update TASK_EXECUTION_LINK_ENTITIES set root_entity_status = 'failed' " +
			"where ROOT_LU_NAME||TARGET_ROOT_ENTITY_ID in " +
			"(select ROOT_LU_NAME||TARGET_ROOT_ENTITY_ID from TASK_EXECUTION_LINK_ENTITIES where Execution_Status <> 'completed')";
		/*"where Execution_Status = 'completed' and not exists " +
		"(select 1 from TDM.TASK_EXECUTION_LINK_ENTITIES t2 " +
		"where t2.TARGET_ROOT_ENTITY_ID = TASK_EXECUTION_LINK_ENTITIES.TARGET_ROOT_ENTITY_ID " +
		"and t2.ROOT_LU_NAME = TASK_EXECUTION_LINK_ENTITIES.ROOT_LU_NAME " +
		"and (t2.Execution_Status <> 'completed' or t2.Execution_Status is null))";
		*/
		String updateSql2 = "Update TASK_EXECUTION_LINK_ENTITIES set root_entity_status = 'completed' where root_entity_status is null";
		
		// Update status to 'failed' if all records, related to this root entity have a completed status
		ludb().execute(updateSql1);
		//Update remaining records to 'completed'
		ludb().execute(updateSql2);
	}


	@desc("This enrichment function updates TASK_EXECUTION_LIST table for ended tasks")
	public static void fnUpdateTaskSummaryTable() throws Exception {
		// TDM 6.0 - New Enrichment function
		String taskExecId = "" + fabric().fetch("SELECT iid(?)", TDM).firstValue();
			
		String taskId = "" + fabric().fetch("select distinct task_id from task_execution_list").firstValue();
		//log.info("fnUpdateTaskSummaryTable - taskID: " + taskId);
		String executionStatus = "completed";
		String startExecTime = "";
		String endExecTime = "";
		int totProcessedRootEnt = 0;
		int totCopiedRootEnt = 0;
		int totFailedRootEnt = 0;
		int totProcessedRefTabs = 0;
		int totCopiedRefTabs = 0;
		int totFailedRefTabs = 0;
		String versionDateTime = "";
		String versionExpDate = "";
		String updateDate = "";
		
		totProcessedRootEnt = (int)fabric().fetch("select count(*) from (Select distinct be_root_entity_id, TARGET_ROOT_ENTITY_ID from TDM.TASK_EXECUTION_LINK_ENTITIES)").firstValue();
		totCopiedRootEnt = (int)fabric().fetch("select count(*) from (Select distinct be_root_entity_id, TARGET_ROOT_ENTITY_ID " +
							"from TDM.TASK_EXECUTION_LINK_ENTITIES where root_entity_status = 'completed' " +
							"EXCEPT Select be_root_entity_id, TARGET_ROOT_ENTITY_ID from TDM.TASK_EXECUTION_LINK_ENTITIES " +
							"where root_entity_status <> 'completed')").firstValue();
		totFailedRootEnt  = (int)fabric().fetch("select count(*) from (Select distinct be_root_entity_id, TARGET_ROOT_ENTITY_ID from TDM.TASK_EXECUTION_LINK_ENTITIES  where root_entity_status <> 'completed')").firstValue();
		
		totProcessedRefTabs = (int)fabric().fetch("select count(distinct entity_id) from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE'").firstValue();
		totCopiedRefTabs = (int)fabric().fetch("select count(distinct entity_id) from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' and t.Execution_Status = 'completed' and not exists (select 1 from TDM.task_execution_entities t2 where t2.entity_id = t.entity_id  and ifNull(t2.Execution_Status, 'failed') <> 'completed')").firstValue();
		totFailedRefTabs = (int)fabric().fetch("select count(distinct entity_id) from TDM.task_execution_entities t where t.ID_Type = 'REFERENCE' and ifNull(t.Execution_Status, 'failed') <> 'completed'").firstValue();
		
		int totNumOfProcessedPostExecutions = (int) fabric().fetch("select count(*) from TDM.task_execution_list where task_execution_id = ? and process_id > 0", taskExecId).firstValue();
		int totNumOfSucceededPostExecutions = (int) fabric().fetch("select count(*) from TDM.task_execution_list where task_execution_id = ? and process_id > 0 and execution_status ='completed'", taskExecId).firstValue();
		int totNumOfFailedPostExecutions = (int) fabric().fetch("select count(*) from TDM.task_execution_list where task_execution_id = ? and process_id > 0 and execution_status !='completed'", taskExecId).firstValue();
		
		if (totCopiedRootEnt == 0 && totCopiedRefTabs == 0) {
			executionStatus = "failed";
		}
		
		//TDM 7 - in case the task was stopped the summary status should be updated to stopped
		int numOfStoppedLUs = (int)fabric().fetch("select count(*) from TDM.task_execution_list where execution_status = 'stopped'").firstValue();
		
		if (numOfStoppedLUs > 0) {
			executionStatus = "stopped";
		}
		
		startExecTime = "" + fabric().fetch("select min(start_execution_time) from task_execution_list").firstValue();
		endExecTime = "" + fabric().fetch("select max(end_execution_time) from task_execution_list").firstValue();
		
		versionDateTime = "" + fabric().fetch("select max(version_datetime) from task_execution_list").firstValue();
		versionExpDate = "" + fabric().fetch("select max(version_expiration_date) from task_execution_list").firstValue();
		
		updateDate = Instant.now().toString();
		//log.info("Setting Dates - start_execution_time: " + startExecTime + ". end_execution_time: " + endExecTime +
		//		 ", version_datetime: <" + versionDateTime + ">, version_expiration_date: <" + versionExpDate + ">, update_date: " + updateDate);
		
		String sqlUpdateTaskSummaryTable = "update task_execution_summary set execution_status = ?, " +
				"tot_num_of_processed_root_entities = ?, tot_num_of_copied_root_entities = ?, tot_num_of_failed_root_entities = ?, " +
				"tot_num_of_processed_ref_tables = ?, tot_num_of_copied_ref_tables = ?, tot_num_of_failed_ref_tables = ?, " +
				"tot_num_of_processed_post_executions = ?, tot_num_of_succeeded_post_executions = ?, tot_num_of_failed_post_executions = ?, update_date = ?";
		
		ArrayList<Object> paramList = new ArrayList<>();
		paramList.add(executionStatus);
		paramList.add(totProcessedRootEnt);
		paramList.add(totCopiedRootEnt);
		paramList.add(totFailedRootEnt);
		paramList.add(totProcessedRefTabs);
		paramList.add(totCopiedRefTabs);
		paramList.add(totFailedRefTabs);
		paramList.add(totNumOfProcessedPostExecutions);
		paramList.add(totNumOfSucceededPostExecutions);
		paramList.add(totNumOfFailedPostExecutions);
		paramList.add(updateDate);
		
		if (!"null".equals(startExecTime) && !"".equals(startExecTime)) {
			sqlUpdateTaskSummaryTable += ", start_execution_time = ?";
			paramList.add(startExecTime);
		}
		if (!"null".equals(endExecTime) && !"".equals(endExecTime)) {
			sqlUpdateTaskSummaryTable += ", end_execution_time = ?";
			paramList.add(endExecTime);
		}
		if (!"null".equals(versionDateTime) && !"".equals(versionDateTime)) {
			sqlUpdateTaskSummaryTable += ", version_datetime = ?";
			paramList.add(versionDateTime);
		}
		if (!"null".equals(versionExpDate) && !"".equals(versionExpDate)) {
			sqlUpdateTaskSummaryTable += ", version_expiration_date = ?";
			paramList.add(versionExpDate);
		}
		sqlUpdateTaskSummaryTable += " where task_execution_id = ?";
		paramList.add(taskExecId);
		Object[] params = paramList.toArray();
		
		db(TDM).execute(sqlUpdateTaskSummaryTable,params);
	}


	public static void tdmUpdateTaskExecutionEntities(String taskExecutionId, Long luId, String luName) throws Exception {
		// TALI- 5-May-20- add a select of selection_method  + fabric_Execution_uid columns. 
		//Remove the condition of fabric_execution_id is not null to support reference only task

		String taskExeListSql = "SELECT L.FABRIC_EXECUTION_ID, L.SOURCE_ENV_NAME, L.CREATION_DATE, L.START_EXECUTION_TIME, " +
						"L.END_EXECUTION_TIME, L.ENVIRONMENT_ID, T.VERSION_IND, T.TASK_TITLE, L.VERSION_DATETIME, T.SELECTION_METHOD, COALESCE(FABRIC_EXECUTION_ID, '') AS FABRIC_EXECUTION_ID " +
						"FROM TASK_EXECUTION_LIST L, TASKS T " +
						"WHERE TASK_EXECUTION_ID = ? AND LU_ID = ? AND L.TASK_ID = T.TASK_ID";
		
		String fabricExecID = "";
		String srcEnvName = "";
		String creationDate = "";
		String startExecDate = "";
		String endExecDate = "";
		String envID = "";
		String entityID = "";
		String targetEntityID = "";
		String execStatus = "";
		String idType = "ENTITY";
		String IID = "";
		String versionInd = "";
		String versionName = "";
		String verstionDateTime = "";
		// Add selectionMethod
		String selectionMethod = "";
		String fabricExecutionId="";
		
		final String UIDLIST = "UIDList";
		
		String insertSql = "INSERT INTO TASK_EXECUTION_ENTITIES(" +
				"TASK_EXECUTION_ID, LU_NAME, ENTITY_ID, TARGET_ENTITY_ID, ENV_ID, EXECUTION_STATUS, ID_TYPE, " +
				"FABRIC_EXECUTION_ID, IID, SOURCE_ENV";
		String insertBinding = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
		
		Db.Row taskData = db(TDM).fetch(taskExeListSql, taskExecutionId, luId).firstRow();
		
		
		//log.info("tdmUpdateTaskExecutionEntities: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_ID: " + LU_ID + ", LU_NAME: " + LU_NAME);
		if(!taskData.isEmpty()) {
			fabricExecID = "" + taskData.cell(0);
			srcEnvName = "" + taskData.cell(1);
			creationDate = "" + taskData.cell(2);
			startExecDate = "" + taskData.cell(3);
			endExecDate = "" + taskData.cell(4);
			envID = "" + taskData.cell(5);
			versionInd = "" + taskData.cell(6);
			versionName = "" + taskData.cell(7);
			verstionDateTime = "" + taskData.cell(8);
		
			// Add selection method and fabric_execution_id
			selectionMethod = "" + taskData.cell(9);
			fabricExecutionId = "" + taskData.cell(10);
			
			//log.info("creationDate: " + creationDate + ", startExecDate: " + startExecDate + ", endExecDate: " + endExecDate + ", SELECTION METHOD: " + selectionMethod);
			 
			if(!"null".equals(creationDate) && !"".equals(creationDate)) {
				insertSql += ", CREATION_DATE";
				creationDate = creationDate.substring(1);
				insertBinding += ", ?";
			} 
			
			if(!"null".equals(startExecDate) && !"".equals(startExecDate)) {
				insertSql += ", ENTITY_START_TIME";
				insertBinding += ", ?";
			}
			
			if(!"null".equals(endExecDate) && !"".equals(endExecDate)) {
				insertSql += ", ENTITY_END_TIME";
				insertBinding += ", ?";
			}
			
			if ("true".equals(versionInd)) {
				insertSql += ", VERSION_NAME,VERSION_DATETIME";
				insertBinding += ", ?, ?";
			}
			
			insertBinding += ")";
			insertSql += ") " + insertBinding;
			insertSql += " ON CONFLICT ON CONSTRAINT task_execution_entities_pkey Do update set execution_status = ?";
		
			// TALI- 5-May-20 - add a check of the sectionMethod. Do not get the list of IIDs for reference only task
		
			Map<String, Map> migrationList = new LinkedHashMap<String, Map>();
			//Map<String, Map> migrationList = (Map<String, Map>) fnGetIIDListForMigration(fabricExecID, null);
			if(!selectionMethod.equals("REF") && !fabricExecutionId.equals("")) {
				migrationList = (Map<String, Map>) fnGetIIDListForMigration(fabricExecID, null);
			}
			
			//log.info ("tdmUpdateTaskExecutionEntities - insertSql: " + insertSql);
			if (migrationList.containsKey("Copied entities per execution")) {
				LinkedHashMap<String, Object> m1 = (LinkedHashMap<String, Object>) migrationList.get("Copied entities per execution");
				
				if (m1.containsKey(UIDLIST)) {
					List<Object> copied_UID_list = (List<Object>) m1.get(UIDLIST);
					//log.info("Size of copied_UID_list: " + copied_UID_list.size());
					for (Object UID : copied_UID_list) {
						Map<Object, Object> innerCopiedUIDMap = (Map<java.lang.Object, java.lang.Object>) UID;
			
						for (Map.Entry<Object, Object> copiedUID : innerCopiedUIDMap.entrySet()) {
							targetEntityID = (String) copiedUID.getKey();
							IID = (String) copiedUID.getKey();
							entityID = (String) copiedUID.getValue();
							execStatus = COMPLETED;
							
							ArrayList<String> paramList = new ArrayList<>();
							
							paramList.add(taskExecutionId);
							paramList.add(luName);
							paramList.add(entityID);
							paramList.add(targetEntityID);
							paramList.add(envID);
							paramList.add(execStatus);
							paramList.add(idType);
							paramList.add(fabricExecID);
							paramList.add(IID);
							paramList.add(srcEnvName);
							
							//log.info("Inserting Copied: LU_NAME: " + LU_NAME + ", TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", entityID: " + entityID);
							//In postgres, timestamp fields cannot be set to empty string,
							//therefore date fields should be insterted only if they have value		
							//log.info("Inserting: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_NAME: " + LU_NAME + ", entityID: " + entityID);			
							if(!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
							if(!"null".equals(startExecDate) && !"".equals(startExecDate)) paramList.add(startExecDate);
							if(!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
							if ("true".equals(versionInd)) {
								paramList.add(versionName);
								paramList.add(verstionDateTime);
							}
							
							//Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
							//in that case only the status will be updated,such case can happen in case of cancel resume
							paramList.add(execStatus);
							
							Object[] params = paramList.toArray();
			
							//log.info ("insertSql - Copied Entities: " + insertSql);
							db(TDM).execute(insertSql, params);
						}
			
					}
				}
			}
			
			if (migrationList.containsKey("Failed entities per execution")) {
				LinkedHashMap<String, Object> m2 = (LinkedHashMap<String, Object>) migrationList.get("Failed entities per execution");
				if (m2.containsKey(UIDLIST)) {
					List<Object> failed_UID_list = (List<Object>) m2.get(UIDLIST);
					for (Object UID : failed_UID_list) {
						Map<Object, Object> innerFailedUIDMap = (Map<java.lang.Object, java.lang.Object>) UID;
			
						for (Map.Entry<Object, Object> failedUID : innerFailedUIDMap.entrySet()) {
							targetEntityID = (String) failedUID.getKey();
							IID = (String) failedUID.getKey();
							entityID = (String) failedUID.getValue();
							execStatus = FAILED;
							
							ArrayList<String> paramList = new ArrayList<>();
							
							paramList.add(taskExecutionId);
							paramList.add(luName);
							paramList.add(entityID);
							paramList.add(targetEntityID);
							paramList.add(envID);
							paramList.add(execStatus);
							paramList.add(idType);
							paramList.add(fabricExecID);
							paramList.add(IID);
							paramList.add(srcEnvName);
							
							//log.info("Inserting Failed: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_NAME: " + LU_NAME + ", entityID: " + entityID);
							if(!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
							if(!"null".equals(startExecDate) && !"".equals(startExecDate)) paramList.add(startExecDate);
							if(!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
							if ("true".equals(versionInd)) {
								paramList.add(versionName);
								paramList.add(verstionDateTime);
							}
							//Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
							//in that case only the status will be updated,such case can happen in case of cancel resume
							paramList.add(execStatus);
							
							Object[] params = paramList.toArray();
			
							//log.info ("insertSql - Failed Entities: " + insertSql);
							db(TDM).execute(insertSql, params);
						}
			
					}
				}
			}
			
			//Add reference Entities to TASK_EXECUTION_ENTITIES table
			String refListSql = "SELECT REF_TABLE_NAME, EXECUTION_STATUS FROM TASK_REF_EXE_STATS ES WHERE " +
					"TASK_EXECUTION_ID = ? AND TASK_REF_TABLE_ID IN (SELECT TASK_REF_TABLE_ID FROM TASK_REF_TABLES RT " +
						"WHERE RT.TASK_ID = ES.TASK_ID AND RT.TASK_REF_TABLE_ID = ES.TASK_REF_TABLE_ID AND RT.LU_NAME = ?)";
			
			idType = "REFERENCE";
			
			Db.Rows refList = db(TDM).fetch(refListSql, taskExecutionId, luName);
			
			for (Db.Row refTable : refList) {
				entityID = "" + refTable.cell(0);
				targetEntityID = entityID;
				execStatus = "" + refTable.cell(1);
				IID = entityID;
							
				ArrayList<String> paramList = new ArrayList<>();
							
				paramList.add(taskExecutionId);
				paramList.add(luName);
				paramList.add(entityID);
				paramList.add(targetEntityID);
				paramList.add(envID);
				paramList.add(execStatus);
				paramList.add(idType);
				paramList.add(fabricExecID);
				paramList.add(IID);
				paramList.add(srcEnvName);
				
				if(!"null".equals(creationDate) && !"".equals(creationDate)) paramList.add(creationDate);
				if(!"null".equals(startExecDate) && !"".equals(startExecDate)) paramList.add(startExecDate);
				if(!"null".equals(endExecDate) && !"".equals(endExecDate)) paramList.add(endExecDate);
				if ("true".equals(versionInd)) {
					paramList.add(versionName);
					paramList.add(verstionDateTime);
				}
				//Adding additional parameter for execution_status, in case the insert failed on primary key constraint,
				//in that case only the status will be updated,such case can happen in case of cancel resume
				paramList.add(execStatus);
				
				Object[] params = paramList.toArray();
				
				//log.info("Inserting Reference: TASK_EXECUTION_ID: " + TASK_EXECUTION_ID + ", LU_NAME: " + LU_NAME + ", entityID: " + entityID);
				//log.info ("insertSql - Reference Entity: " + insertSql);
				db(TDM).execute(insertSql, params);
			}
			
			if (refList != null) {
				refList.close();
			}
		}
	}


	@desc("This function runs the Fabric command migrate_summary and returns its output")
	@out(name = "migrateSummaryOutput", type = String.class, desc = "")
	public static Map<String, Object> fnBatchStats(String i_migrateID) throws Exception {
		int retries = 0;
		Map<String, Object> response = null;


		while (response == null && retries < 4) {
			try {
				List<Map<String, Object>> stats = (List<Map<String, Object>>) SharedLogic.fnBatchStats(i_migrateID, "S");
				for (Map<String, Object> tmp : stats) {
					if ("Cluster".equals(tmp.get("Level"))) {
						response = tmp;
						break;
					}
				}
				retries++;
			} catch (Exception e) {
				log.error("wsMigrateStats - Failed to get migrate info, with exception: " + e.getMessage());
				if (e.getMessage().toString().contains("Batch process is waiting to be taken by a job") && retries < 4) {
					response = null;
					retries++;
					Thread.sleep(1000);
				} else {
					log.error("wsMigrateStats - Check the command's afinity");
				    throw new Exception(e);
				}
			}
		}
			
		return response;
	}


	@desc("This function will load the errors of the migrate command to the error table task_exe_error_detailed")
	public static void fnUpdateTaskErrorsDetails(String i_taskExecId, String i_luName, String i_migrateId) throws Exception {
		//TDM 6.1.1 - New function to populate table task_exe_error_detailed with the errors from migrate command
		String insertSql = "insert into TASK_EXE_ERROR_DETAILED (TASK_EXECUTION_ID,LU_NAME,ENTITY_ID,IID,TARGET_ENTITY_ID, " +
			"ERROR_CATEGORY, ERROR_MESSAGE) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?)";
		
		String getErrorlistSql = "";
		
		// if the cluster ID is define, add it to the name of the keyspace.
		String clusterID = "" + fabric().fetch("clusterid").firstValue();
				
		if (clusterID == null || clusterID.isEmpty()) {
			getErrorlistSql="select entityid, error from k2batchprocess.batchprocess_entities_errors where bid=?";
		} else {
			getErrorlistSql="select entityid, error from k2batchprocess_" + clusterID + ".batchprocess_entities_errors where bid=?";
		}
				
		Db.Rows errorList = db("DB_CASSANDRA").fetch(getErrorlistSql, i_migrateId);
		
		for (Db.Row errorRec : errorList) {
			String entityId = "" + errorRec.cell(0);
			String erroMsg = "" + errorRec.cell(1);
			Object[] split_iid = fnSplitUID(entityId);
			String instanceId = "" + split_iid[0];
			
			db(TDM).execute(insertSql, i_taskExecId, i_luName, entityId, instanceId, instanceId, "Entity Failed", erroMsg);
		}
		
		
		if (errorList != null) {
				errorList.close();
			}
	}

	@desc("Execute TDM pending tasks and update the tasks status.")
	@type(UserJob)
	public static void tdmExecuteTask() throws Exception {
		TdmExecuteTask.fnTdmExecuteTask();
	}


	@desc("This function runs the Fabric command batch_summary and returns its output")
	@out(name = "fnRunBatchSummary", type = String.class, desc = "")
	public static String fnRunBatchSummary(String i_batchID) throws Exception {
		int retries = 0;
		StringBuilder outputString = new StringBuilder();
		String summaryOut = "";
				
		while ("".equals(summaryOut)) {
			try {
				Object batchSummary = getFabricResponse("batch_summary '" + i_batchID + "'");
				ArrayList batchSummaryList = (ArrayList) batchSummary;
				for (int i = 0; i < batchSummaryList.size(); i++) {
					outputString.append(batchSummaryList.get(i));
				}
				summaryOut = "" + outputString;
				//log.info("fnRunBatchSummary - summaryOut: " + summaryOut);
			} catch (Exception e) {
				log.error("fnRunBatchSummary - Failed to get migrate info, with exception: " + e.getMessage());
				if (e.getMessage().toString().contains("Batch process is waiting to be taken by a job") && retries < 4) {
					summaryOut = "";
					retries++;
					Thread.sleep(1000);
				} else {
					log.error("fnRunBatchSummary - Check the command's afinity");
				    throw new Exception(e);
				}
			}
		}
			
		return (summaryOut);
	}


	public static void fnTdmCopyReference(String taskExecutionID, String taskType) throws Exception {
		//log.info("-- START Reference JOB for Task Type: " + taskType + " Task Execution ID: " + taskExecutionID + "---");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		
		Db.Rows refTabLst = null;
		
		String refQuery ="Select ES.* from TASK_REF_EXE_STATS ES, TASKS T where execution_status  in ('"+WAITING+"', '"+PENDING+"', '"+RESUME+"', '"+STOPPED+"') " + 
			"and ES.TASK_ID = T.TASK_ID and lower(T.TASK_TYPE) = ? and ES.TASK_EXECUTION_ID = ? order by task_execution_id, task_id";
		
		refTabLst = db(TDM).fetch(refQuery, taskType, taskExecutionID);
		
		int refCount = 0;
		
		for (Db.Row row : refTabLst) {
			refCount++;
			String taskRefTableID = "" + row.get("task_ref_table_id");
			String refTableName = "" + row.get("ref_table_name");
			//log.info("fnTdmCopyReference - refTableName: " + refTableName);
				
			if (!StringUtils.isEmpty(taskRefTableID) && !StringUtils.isEmpty(taskExecutionID)) {
				
				String luName = "" + db(TDM).fetch("Select lu_name from " + TASK_REF_TABLES + " where task_ref_table_id = ?", taskRefTableID).firstValue();
		
				String taskID = "" + row.get("task_id");
		
				String execStatus = "" + row.get("execution_status");
		
				Db.Row taskParams = db(TDM).fetch("Select source_env_name, version_ind, retention_period_type, retention_period_value, " +
					"selection_method, selected_ref_version_task_name, selected_ref_version_datetime, selected_ref_version_task_exe_id from " +
					TASKS + " where task_id = ?", taskID).firstRow();
		
				String versionInd = "false";
				if(taskParams.get("version_ind") != null) {
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
			
				String affinity =  "";
				
				if (luDCName != null && !"".equals(luDCName) && !"null".equals(luDCName)) {
					affinity = " AFFINITY='" + luDCName + "'";
				}
				
				String uid = "" + row.get("job_uid");
				
				Object selectionMethod = "" + taskParams.get("selection_method");
				
				if(selectionMethod != null && selectionMethod.toString().equals(REF)){
					Long unixTime = System.currentTimeMillis() ;
					Long unixTime_plus_retention;
		
					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
					Integer retention_in_seconds = getRetention(retType, retVal);
		
					unixTime_plus_retention = ( unixTime/1000L + retention_in_seconds )*1000;
					String versionExpirationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(unixTime_plus_retention);
		
					Object[] param;
					if (versionInd.equals("true")) {
						param = new Object[]{RUNNING, timeStamp, versionExpirationDate, taskExecutionID, taskID, luID};
					} else {
						param = new Object[]{RUNNING, null, null, taskExecutionID, taskID, luID};
					}
					
					//log.info("fnTdmCopyReference - Updating task to running for - task_execution_id: " + taskExecutionID + 
					//		", task_id: " + taskID + ", lu_id: " + luID);
				
					db(TDM).execute("update " + TASK_EXECUTION_LIST + " set execution_status = ?, " +
									"version_datetime = ?, version_expiration_date = ? " +
									"where task_execution_id = ? and task_id = ? and lu_id = ?", param);
				}
				int count = 0;
				int retries = 5;
				
				String sourceEnv;
		
				sourceEnv = "" + taskParams.get("source_env_name");
				
				if (taskType.equalsIgnoreCase("extract")) {
				
					String args = " ARGS='{\"sourceEnv\":\"" + sourceEnv + "\",\"versionID\":\"" + versionInd +
									"\",\"ttl\":\"" + ttl + "\", \"taskExecID\":\"" + taskExecutionID +
									"\",\"taskRefTableID\":\"" + taskRefTableID + "\",\"luName\":\"" + luName + "\"}'";
						
					while(!Thread.currentThread().isInterrupted()) {
						try{
							switch (execStatus) {
								case WAITING:
								case PENDING:
									//log.info("inside waiting status: " + execStatus);
				
									fabric().execute("startjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + taskExecutionID + "_" + taskRefTableID + "' " + affinity + args + ";");
									break;
								case STOPPED:
									fabric().execute("stopjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "';");
									break;
								case RESUME:
									if(Util.isEmpty(uid)){
										uid = taskExecutionID + "_" + taskRefTableID;
										fabric().execute("startjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "' " + affinity + args + ";");
									}else{
										db(TDM).execute("update " + TASK_EXECUTION_LIST + " set execution_status = ? " +
											"where task_execution_id = ? and task_id = ? and lu_id = ?; ",
											RUNNING, taskExecutionID, taskID,luName);
										fabric().execute("resumejob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "'"  + ";");
										//log.info("------------------------------------- After resume case ------------------------------------");
									}
									break;
							}
							break;
						} catch (Exception e) {
							if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
								throw e;
							}
							if (++count >= retries) {
								db(TDM).execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
									"where task_execution_id = ? and task_ref_table_id = ?; ",
									FAILED, e.getMessage(), TDM_COPY_REFERENCE, taskExecutionID, taskRefTableID);
								fabric().execute("stopjob USER_JOB NAME='TDM.tdmCopyRefTablesForTDM' UID='" + uid + "';");
								retries = 0;
								break;
							} else {
								//log.info(e.getMessage() + "; this is retry number " + count);
								Thread.sleep(5000);
							}
						}
					}
				} else {// In case of Load Task
				//	while(!Thread.currentThread().isInterrupted()) {
						//log.info("fnTdmCopyReference - Handling Load Task");
					try{
						switch (execStatus) {
							case WAITING:
							case PENDING:
								String selectedRefVersionTaskName = "''";
								String selectedRefVersionDateTime = "19700101000000";
								String selectedRefVersionTaskExeId = "''";
								if (versionInd.equals("true")) {
									selectedRefVersionTaskName = "" + taskParams.get("selected_ref_version_task_name");
									selectedRefVersionDateTime = "" + taskParams.get("selected_ref_version_datetime");
									selectedRefVersionTaskExeId = "" + taskParams.get("selected_ref_version_task_exe_id");
								}
								String loadRefCmd = "BATCH " + luName + ".('" + taskExecutionID + "_" + refTableName + "') fabric_command=\"broadway " + luName + ".TDMReferenceLoader " +
		                            "iid=?, taskExecutionID="+ taskExecutionID + ", luName=" + luName + ", refTableName=" + refTableName + ", sourceEnvName=" + sourceEnv + 
									", selectedRefVersionTaskName='" + selectedRefVersionTaskName +"', selectedRefVersionDateTime=" +
									selectedRefVersionDateTime + ", selectedRefVersionTaskExeId=" + selectedRefVersionTaskExeId +"\" with async='true'";
									
								//log.info("fnTdmCopyReference - Running batch command: " + loadRefCmd);
								String batchID = Util.rte(() -> "" + fabric().fetch(loadRefCmd).firstValue());
								
								//log.info("fnTdmCopyReference - Updating batch ID to: " + batchID);
								db(TDM).execute("update " + TASK_REF_EXE_STATS + " set job_uid= ?, execution_status = ?, " +
									"start_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', updated_by = ?" +
									"where task_execution_id = ? and task_ref_table_id = ?; ",
									batchID, RUNNING, "TDMReferenceLoader", taskExecutionID, taskRefTableID);
								break;
							case STOPPED:
								fabric().execute("cancel batch '" + uid + "';");
								break;
							case RESUME:
								fabric().execute("batch_retry '" + uid + "'"  + ";");
								//log.info("------------------------------------- After resume case ------------------------------------");
								break;
						}
						//break;
					} catch (Exception e) {
						log.error("Reference Table handling had an exception: " + e);
						if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
							throw e;
						}
						//if (++count >= retries) {
						db(TDM).execute("update " + TASK_REF_EXE_STATS + " set execution_status = ?,  end_time = CURRENT_TIMESTAMP AT TIME ZONE 'UTC', error_msg = ?,  updated_by = ?" +
							"where task_execution_id = ? and task_ref_table_id = ?; ",
							FAILED, e.getMessage(), TDM_COPY_REFERENCE, taskExecutionID, taskRefTableID);
						//log.info("uid to cancel: " + uid);
						if (!"null".equals(uid)) {
							fabric().execute("cancel batch '" + uid + "';");
						}
							//	retries = 0;
							//break;
							//} else {
								//log.info(e.getMessage() + "; this is retry number " + count);
							//	Thread.sleep(5000);
							//}
					}
				}
				
			}
			//log.info("fnTdmCopyReference - Handling next Ref table #" + refCount);
		}
		//log.info("fnTdmCopyReference - After the loop on ref tables, refCount: " + refCount);
		//In case the task type was REF, but included LUs that do not have reference,or none of their reference were chosen, update the status to COMPLETE
		String lusWithoutRefSql = "select l.task_id, l.task_execution_id, l.lu_id from task_execution_list l, tasks t, tasks_logical_units lu " +
			"where l.task_id = t.task_id and t.selection_method = 'REF' and l.task_id = lu.task_id and l.lu_id = lu.lu_id and " +
			"lower(l.execution_status) in ('pending', 'stopped') and " +
			"not exists (select 1 from task_ref_tables r, task_ref_exe_stats s where r.task_id = lu.task_id and r.lu_name = lu.lu_name " +
			"and r.task_ref_table_id = s.task_ref_table_id and s.task_execution_id = l.task_execution_id " +
			"and lower(s.execution_status) not in ('completed', 'failed'))";
		
		String updateLuWithoutRefSql = "UPDATE task_execution_list SET execution_status = ?, num_of_processed_entities = 0, " +
					"start_execution_time = current_timestamp at time zone 'utc', " +
					"end_execution_time = current_timestamp at time zone 'utc', num_of_copied_entities = 0, num_of_failed_entities = 0, " +
					"num_of_processed_ref_tables = 0, num_of_copied_ref_tables = 0, num_of_failed_ref_tables = 0 " +
					"WHERE  task_id = ? AND task_execution_id = ? and lu_id = ?";
		
		Db.Rows lusWithoutRef = db(TDM).fetch(lusWithoutRefSql);
		
		for (Db.Row luWithoutRef : lusWithoutRef) {
			String taskId = "" + luWithoutRef.cell(0);
			String taskExecID = "" + luWithoutRef.cell(1);
			String luId = "" + luWithoutRef.cell(2);
			//log.info("tdmCopyReferenceListener - Setting status to completed for LU without reference to be handled. Task Execution ID: " + taskExecID + ", lu id: " + luId);
			db(TDM).execute(updateLuWithoutRefSql, COMPLETED, taskId, taskExecID,luId);
		}
		
		if (refTabLst != null) {
			refTabLst.close();
		}
		
		if (lusWithoutRef != null) {
			lusWithoutRef.close();
		}
	}


	@type(UserJob)
	public static void tdmTaskScheduler() throws Exception {
		TdmTaskScheduler.fnTdmTaskScheduler();
	}

	private static void createCassandraTable(String iface, String schemaName, String tableName) throws SQLException {

		try  {
			Connection conn = getConnection(iface);
			Connection cassandraConn = getConnection(DBCASSANDRA);
			Statement select = conn.createStatement();

			ResultSet refTableRS = select.executeQuery("Select * from " + schemaName + "." + tableName);

			ResultSetMetaData metaData = refTableRS.getMetaData();
			DatabaseMetaData dbmd = conn.getMetaData();
			
			// Tali- Fix- send the schema name as parameter to getPrimaryKeys
			ResultSet pk = dbmd.getPrimaryKeys(null, schemaName, tableName);
			//ResultSet pk = dbmd.getPrimaryKeys(null, null, tableName);
			
			Statement selectTableInfo = cassandraConn.createStatement();
			ResultSet tableInfoRes = null;

			StringBuilder pkList = new StringBuilder();
			while (pk.next()) {
				pkList.append(',').append((pk.getString("COLUMN_NAME")).toLowerCase());
			}
			//log.info("pkList: " + pkList);

			String table = "";
			//TDM 5.1, replacing dbFabtic with fabric as Frabic 5.3 expectations
			//String clusterID = (String) DBSelectValue(DB_FABRIC, "clusterid", null);
			String clusterID = "" + fabric().fetch("clusterid").firstValue();
			if (clusterID == null || clusterID.isEmpty()) {
				table = "k2view_tdm." + tableName.toLowerCase();
			} else {
				table = "k2view_tdm_" + clusterID + "." + tableName.toLowerCase();
			}


			if (clusterID == null || clusterID.isEmpty()) {
				tableInfoRes = selectTableInfo.executeQuery("SELECT * FROM system_schema.columns WHERE keyspace_name = 'k2view_tdm' AND table_name = '" + tableName.toLowerCase() + "';");
			} else {
				tableInfoRes = selectTableInfo.executeQuery("SELECT * FROM system_schema.columns WHERE keyspace_name = 'k2view_tdm_" + clusterID + "' AND table_name = '" + tableName.toLowerCase() + "';");
			}

			Set<String> originColumns = new HashSet<>();
			while (tableInfoRes.next()) {
				//log.info("Adding Original Column: " + tableInfoRes.getString("column_name"));
				originColumns.add(tableInfoRes.getString("column_name"));
			}

			Set<String> newColumns = new HashSet<>();

			// Fix- add rec_id field. This field will be added to the PK to avoid an override of the same record if the source table does not have its own PK fields
			StringBuilder createStmt = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table).append(" ( source_env_name text, tdm_task_execution_id text, rec_id text, ");

			//Add the control fields, otherwise the structure will always be different
			newColumns.add("source_env_name");
			newColumns.add("tdm_task_execution_id");
			newColumns.add("rec_id");

			int colsCount = metaData.getColumnCount();
			//log.info("colsCount: " + colsCount);
			//log.info("first Col Name: " + metaData.getColumnName(0));
			for (int i = 1; i <= colsCount; i++) {
				String colType;
				colType = convertToCassandraType(metaData.getColumnTypeName(i).toLowerCase());
				String colName = metaData.getColumnName(i);
				//log.info("colName: " + colName);
				newColumns.add(colName.toLowerCase());
				createStmt.append(colName.toLowerCase()).append(" ").append(colType);
				if (i < colsCount) {
					createStmt.append(',');
				}
			}

			createStmt.append(" , PRIMARY KEY (source_env_name, tdm_task_execution_id, rec_id ").append(pkList).append("));");

			StringBuilder updateStatement = new StringBuilder();
			updateStatement.append("update task_ref_exe_stats set execution_status = 'invalid', updated_by = 'createCassandraTable'");
			updateStatement.append(" where lower(ref_table_name) = lower('");
			updateStatement.append(tableName);
			updateStatement.append("') and task_execution_id in (select task_execution_id from task_execution_list");
			updateStatement.append(" where (version_expiration_date > CURRENT_TIMESTAMP AT TIME ZONE 'UTC' or version_expiration_date is null))");

			//log.info("createStmt: " + createStmt.toString());
			//log.info("updateStatement: " + updateStatement.toString() + ", size of existing table: " + originColumns.size() + ", size of new table: " + newColumns.size());

			//Compare the columns list only if the table already exists in DB
			if ((originColumns.size() > 0) && (!originColumns.equals(newColumns))) {
				//log.info("---- The table" + tableName.toLowerCase() + " exists, but the schema was changed ---");

				// Update the task_execution_list and expire the entries related to the rebuilt reference table
				//log.info("Updating the task_ref_exe_stats table");
				//DBExecute("TDM", updateStatement.toString(),null);
				db("TDM").execute("" + updateStatement);
				//log.info("Dropping the table");
				//DBExecute(DBCASSANDRA,"DROP TABLE " + table, null);
				db(DBCASSANDRA).execute("DROP TABLE " + table);
				//log.info("Creating the table after dropping");
				//DBExecute(DBCASSANDRA, createStmt.toString(), null);
				db(DBCASSANDRA).execute("" + createStmt);

			} else if (originColumns.size() == 0) {
				//log.info("Creating the table as it does not exist");
				//DBExecute(DBCASSANDRA, createStmt.toString(), null);
				db(DBCASSANDRA).execute("" + createStmt);
			}
			select.close();
			pk.close();
			selectTableInfo.close();
			tableInfoRes.close();
			refTableRS.close();
		} catch (Exception e) {
			log.error("Table: " + schemaName + "." + tableName + " at Interface: " + iface + " failed with Err Message: " + e.getMessage());
		}
	}

	private static String convertToCassandraType(String originType) throws SQLException {
		String colType;
		switch (originType) {
			case "varchar2":
			case "char":
			case "long":
			case "clob":
			case "nchar":
				colType = CASSANDRA_TEXT_TYPE;
				break;
			case "number":
			case "integer":
			case "int4":
				colType = CASSANDRA_INT_TYPE;
				break;
			case "decimal":
			case "binary_double":
			case "binary_float":
			case "float8":
				colType = CASSANDRA_DOUBLE_TYPE;
				break;
			case "date":
				colType = "timestamp";
				break;
			case "bool":
			case "boolean":
			case "bit": //sql server
				colType = CASSANDRA_BOOL_TYPE;
				break;
			default:
				colType = originType;
				break;
		}
		return colType;
	}

	private static Loader buildLoader(AtomicInteger counter, AtomicBoolean failed, String tableName) {
		return Loader.to("TDM").withOnFailure(t -> {
			failed.set(true);
			//log.error("String.format("Failed on table [%s] with error message", tableName"), t);
			System.out.println(String.format("Failed on table [%s] with error message", tableName, t.getMessage()));// DBExecute(save failure)
		})
				.withOnSuccess(() -> {
					int currentCount = counter.addAndGet(10);
					//DBexecute(update the source-update the number of processed records by the batch size)
				}).please();
	}

	private static String getBatchStatus(Object originalStatus) {
		if (originalStatus == null) {
			return null;
		}

		if (originalStatus.equals("IN_PROGRESS")) {
			return RUNNING;
		} else if(originalStatus.equals("DONE")) {
			return COMPLETED;
		} else if (originalStatus.equals("CANCELLED")) {
			return STOPPED;
		}

		return originalStatus.toString().toLowerCase();
	}

}
