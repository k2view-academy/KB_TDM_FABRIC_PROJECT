/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDM;

import com.k2view.cdbms.lut.DbInterface;
import com.k2view.cdbms.lut.LUType;
import com.k2view.cdbms.lut.LudbColumn;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.Utils;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.out;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.type;
import com.k2view.cdbms.usercode.lu.TDM.TdmTaskScheduler;
import com.k2view.cdbms.utils.K2TimestampWithTimeZone;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.ParamConvertor;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.UserJob;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnUpdateAIProcess;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.*;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;


@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class Logic extends UserCode {

	public static final String PENDING = "pending";
	public static final String TABLES = "TABLES";
    public static final String TABLE_LEVEL_LU = "TDM_TableLevel";
	public static final String TASKS = TDMDB_SCHEMA + ".TASKS";
	public static final String TDM = "TDM";
	public static final String DB_FABRIC = "fabric";
	public static final String TASK_REF_TABLES = TDMDB_SCHEMA + ".TASK_REF_TABLES";
	public static final String PRODUCT_LOGICAL_UNITS = TDMDB_SCHEMA + ".product_logical_units";
	public static final String TASK_REF_EXE_STATS = TDMDB_SCHEMA + ".TASK_REF_EXE_STATS";
	public static final String TASKS_LOGICAL_UNITS = TDMDB_SCHEMA + ".tasks_logical_units";
	public static final String RUNNING = "running";
	public static final String WAITING = "waiting";
	public static final String STOPPED = "stopped";
	public static final String RESUME = "resume";
	public static final String TASK_EXECUTION_LIST = TDMDB_SCHEMA + ".task_execution_list";
    public static final String FAILED = "failed";
    public static final String COMPLETED = "completed";
	public static final String PAUSED = "paused";
    public static final Long maxNumOfValues = Optional.ofNullable(getGlobal("COMBO_MAX_COUNT", "TDM")).map(Long::parseLong).orElse(0L); 

    public static final String insertDistintValuesSql = "INSERT INTO " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES " +
		        "(SOURCE_ENVIRONMENT, LU_NAME, FIELD_NAME, NUMBER_OF_VALUES, FIELD_VALUES, IS_NUMERIC, MIN_VALUE, MAX_VALUE, FIELD_TYPE ) " +
		        "VALUES (?, ?, ? ,?, string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "'), ?, ?, ?,?)";
		    
    public static final String updateDistintValuesSql = "UPDATE " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES SET " +
		    "NUMBER_OF_VALUES = ?, FIELD_VALUES = string_to_array(?, '" +  TDM_PARAMETERS_SEPARATOR + "'), IS_NUMERIC = ?, MIN_VALUE = ?, MAX_VALUE = ?, FIELD_TYPE=? " +
		    "WHERE source_environment = ? AND lu_name = ? AND field_name = ?";

    public static final String getDistinctValuesSql = "SELECT source_environment, lu_name, field_name, number_of_values, " +
    "array_to_string(field_values, '" + TDM_PARAMETERS_SEPARATOR + "') as field_values, is_numeric, min_value, max_value , field_type " +
    "from " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES WHERE source_environment = ? and lu_name = ? and (number_of_values <= ? or is_numeric = true)";

	@out(name = "instanceId", type = String.class, desc = "")
	@out(name = "envName", type = String.class, desc = "")
	public static Object fnGetSplittedID(String entityID, String idType, String envID) throws Exception {
		Object[] res = {entityID, envID};

		if ("ENTITY".equals(idType)) {
			res = fnSplitUID(entityID);
			return res;
		} 

		return res;
	}

	@desc("check whether task of EXTRACT type has finished its migrate.\r\n" +
			"if yes - update TASK_EXECUTION_LIST with the status and migration information.\r\n" +
			"It will also clean redis for load tasks.")
	@type(UserJob)
	public static void fnCheckMigrateAndUpdateTDMDB() throws Exception {
		// TDM 5.1- fix the query- check the task_type instead of the fabric_execution_id, since a reference only task does not have the fabric_execution_id (= migrate id)
		String selectFromTaskExecutionListSql = "Select tel.fabric_execution_id, tel.task_id, tel.lu_id, " +
                "tlu.lu_name, tel.task_type, tel.process_id, tpost.process_name, tpost.process_type, " + 
                "tel.task_execution_id, tel.parent_lu_id, t.execution_mode, tel.be_id, t.clone_ind " +
                "from " + TDMDB_SCHEMA + ".task_execution_list tel " +
                "left join " + TDMDB_SCHEMA + ".tasks t on tel.task_id = t.task_id " +
				"left join " + TDMDB_SCHEMA + ".tasks_logical_units tlu on tel.task_id = tlu.task_id And tel.lu_id = tlu.lu_id " +
				"left join " + TDMDB_SCHEMA + ".tasks_exe_process tpost On tel.task_id = tpost.task_id " +
				"and tel.process_id = tpost.process_id Where Lower(tel.execution_status) = 'running'";
		
		Db.Rows taskExecutionList = null;
		Db.Rows rows = null;
		Db.Rows refRunningLus = null;
		
		try {
			//log.info("fnCheckMigrateAndUpdateTDMDB- get running extract tasks: " + selectFromTaskExecutionListSql);
			taskExecutionList = db(TDM).fetch(selectFromTaskExecutionListSql);
		
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
				String updateTaskExecutionListSql = "UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status = ?, num_of_processed_entities = ?, " +
					"num_of_copied_entities = ?, num_of_failed_entities = ?, end_execution_time = ?, " +
					"num_of_processed_ref_tables = ?, num_of_copied_ref_tables = ?, num_of_failed_ref_tables = ? " +
					"WHERE task_id = ? AND task_execution_id = ? and lu_id = ? and process_id = ?";
				String status = COMPLETED;
				
				Long luID = 0L;
				Long parentLuID = 0L;
				Long processID = 0L;
				String total="0";
                String failed="0";
                String copied="0";
				String luName = "";
				String taskType = "";
                String processType = "";

                //TDM 9.1 - Params Coupling
                Boolean paramsCoupling = isParamsCoupling();
                
				//TDM 9.2 - Vertical Execution
                Boolean verticalExecution = false;
                
				try {
					// TDM 5.1- add a check if row[0] is null (will be null for reference only tasks)
					String batchID = null;
					
					if (row.get("fabric_execution_id") != null && !("null".equalsIgnoreCase("" + row.get("fabric_execution_id"))) && !Util.isEmpty("" + row.get("fabric_execution_id"))) {
                            batchID = "" + row.get("fabric_execution_id");
                        }
	
					taskID = "" + row.get("task_id");
					taskExecutionID = "" + row.get("task_execution_id");
					
					// TDM 6.0 - Since the extract tasks can include multi LUs, each extract task execution will have multi entries;
					// entry per LU, therefore we need to check the data of each LU seperately
					luID = (Long) row.get("lu_id");
					processID = (Long) row.get("process_id");
                    processType = "" + row.get("process_type");
					luName = "" + row.get("lu_name");
					// TDM 7.0 - Since the this job also handles Load tasks, get the task_type
					taskType = "" + row.get("task_type");
					//log.info("Procssing LU_NAME: " + luName + ", Migrate ID: " + batchID);
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
					if (row.get("parent_lu_id") != null) {
						parentLuID = (Long)row.get("parent_lu_id");
					}

                    // TDM 9.2 - Vertical Execution
                    String executionMode = fnGetTaskExecutionMode(row.get("execution_mode").toString(), taskType, 
                        Long.parseLong(row.get("be_id").toString()), Boolean.parseBoolean(row.get("clone_ind").toString()));
                    verticalExecution = "VERTICAL".equalsIgnoreCase(executionMode) ? true : false;
					// TDM 5.1- Tali- Fix- get the selection_method from TASKS. If this selection_method is REF- do not call the migrate_summary command, 
					//but check the reference status instead
		
					//String selectionMethod = "" + db(TDM).fetch("Select selection_method from " + TASKS + " where task_id=?", taskID).firstValue();
                    Db.Row taskRec = db(TDM).fetch("Select selection_method, source_env_name from " + TASKS + " where task_id=?", taskID).firstRow();
                    String selectionMethod = taskRec.get("selection_method").toString();
                    String srcEnv = taskRec.get("source_env_name").toString();

					Integer totNoOfRefTables = 0;
					// TDM 5.1- add the update of the reference tables fields
					//log.info("selectionMethod: " + selectionMethod);
					if (selectionMethod != null && selectionMethod.equals(TABLES) && (processID == 0 || processID==null)) {
						//log.info("fnCheckMigrateAndUpdateTDMDB- handle reference only task");
						Map<String, Object> refSummaryStatsBuf = fnGetReferenceSummaryData(taskExecutionID);
						//log.info("Getting refSummaryStats for luName: " + luName);
						HashMap <String, Object> refSummaryStats = (HashMap <String, Object>)refSummaryStatsBuf.get(TABLE_LEVEL_LU);
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
									//log.info("num_of_copied_ref_tables: " + num_of_copied_ref_tables);
									if (num_of_copied_ref_tables == 0) {
										status = FAILED;
                                    }
		
									// TDM 5.1- change the start and end date parameters and add the parameters for the reference tables
									//log.info("Updating task status to: " + status);
									db(TDM).execute(updateTaskExecutionListSql, status, 0, 0, 0, //refMinDate.toString(), -- No need to update the start time
										refMaxDate.toString(), num_of_processed_ref_tables, num_of_copied_ref_tables, 
										num_of_failed_ref_tables, taskID, taskExecutionID, luID, processID);
								}
							} // if(totNoOfRefTables > 0)
						}//if (refSummaryStats != null)
					}// if(selectionMethod != null && selectionMethod.equals(TABLES))
					else // the task contains entities (but can still have reference tables in addition to the entities)
					{
					
						Map<String, Object> batchStats = null;
						if (batchID != null && !"".equals(batchID)) {
							batchStats = fnBatchStats(batchID);
						}
						//log.info("fnCheckMigrateAndUpdateTDMDB - Returned output from fnRunBatchSummary: " + batchStats);
						
						if (batchStats != null) // response from migrate_summary command (which is run in wsMigrateStats ) should be returned with all levels (Node,DC,Cluster). if its returned without level DC - skip the update command and wait to next time the user job will be executed
						{
							status = getBatchStatus(batchStats.get("Status"));
		
							//log.info("fnCheckMigrateAndUpdateTDMDB- migration status for task execution id " + taskExecutionID + " is: " + status);
							// In case there are reference table to be handled, if the refernece job has finished
							String sqlrefSts = "select distinct lu_name from " + TDMDB_SCHEMA + ".task_ref_exe_stats es, " + TDMDB_SCHEMA + ".task_ref_tables rt where " +
								"lower(execution_status) in ('waiting', 'running', 'pending') and " +
								"task_Execution_id = ? and es.task_id = rt.task_id and es.task_ref_table_id = rt.task_ref_table_id " +
								"group by lu_name";
							
							refRunningLus = db(TDM).fetch(sqlrefSts, taskExecutionID);
							
							Boolean refStillRunning = false;
							for (Db.Row refRunningLu : refRunningLus) {
								if (luName.equals(refRunningLu.get("lu_name"))) {
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
							
										
								// TDM 7.5 - if the task was paused or cancelled not from TDMGUI, fail the task
								if (PAUSED.equals(status) || STOPPED.equals(status)) {						
									log.error("The task execution ID: " + taskExecutionID + ", of LU: " + luName + "was " + status + " from outside TDM, failing it");
									status = FAILED;
								}
								total = "" + batchStats.get("Total");
								failed = "" + batchStats.get("Failed");
								
								// TDM 7.5 - if the total is zero for a root LU, then set the status of the task to failed
								if (Long.parseLong(total) == 0 && parentLuID == 0) {
									status = FAILED;
									log.error("No Instances were handled by Task");
									String insertSql = "insert into " + TDMDB_SCHEMA + ".TASK_EXE_ERROR_DETAILED (TASK_EXECUTION_ID,LU_NAME,ENTITY_ID,IID,TARGET_ENTITY_ID, " +
										"ERROR_CATEGORY, ERROR_MESSAGE) " +
										"VALUES (?, ?, ?, ?, ?, ?, ?)";
									db(TDM).execute(insertSql, taskExecutionID, luName, " ", " ", " ", "No Instances were handled by Task", "No Instances");
								}
								//If all the entities failed, then set the status of the task to failed
								if (failed.equals(total) && Long.parseLong(total) > 0) {
									status = FAILED;
								}
								
								//log.info("fnCheckMigrateAndUpdateTDMDB - total: " + total + ", failed: " + failed);
								copied = "" + batchStats.get("Succeeded");//String.valueOf(Integer.parseInt(total) - Integer.parseInt(failed));
		
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

								//log.info("fnCheckMigrateAndUpdateTDMDB - finished updating TASK_EXECUTION_ENTITIES");
								if(processID != null && processID != 0){
									num_of_processed_ref_tables=null;
									num_of_copied_ref_tables=null;
									num_of_failed_ref_tables=null;
									if(processID>0){
										total=null;
										copied =null ;
										failed =null;
									}
								}
                                if("completed".equalsIgnoreCase(status) && processID==-2 && "pre".equalsIgnoreCase(processType)){
									Map num = fnUpdateAIProcess(taskExecutionID,"-2");
									total= "" + num.get("total");
									copied= "" + num.get("copied");
									failed= "" + num.get("failed");

								}
								// TDM 5.1- change the start and end date parameters and add the parameters for the reference tables
								//log.info("Updating task for luName: " + luName + " to status: " + status + " with:");
								//log.info("numOfProcessedRefTables: " + num_of_processed_ref_tables);
								//log.info("numOfCopiedRefTables: " + num_of_copied_ref_tables + ", numOfFailedRefTables: " + num_of_failed_ref_tables);
								if("completed".equalsIgnoreCase(status) && processID==-1 && "post".equalsIgnoreCase(processType)){
									Map num = fnUpdateAIProcess(taskExecutionID,"-2");
									total= "" + num.get("total");
									copied= "" + num.get("copied");
									failed= "" + num.get("failed");

								}
								//log.info("verticalExecution: " + verticalExecution);
                                // TDM 9.2 - in case of vertical execution, the statistics of the child LUs will be taken from task_execution_entities.
                                if (verticalExecution && parentLuID != 0) {
                                    //log.info("Handling Vertical for luName: " + luName);
                                    String childStatsSql = "SELECT count(*)  as total, " + 
                                            "coalesce(sum(case when execution_status = 'completed' then 1 else 0 end), 0) as copied, " +
                                            "coalesce(sum(case when execution_status = 'failed' then 1 else 0 end), 0) as failed " +
                                            "FROM " + TDMDB_SCHEMA + ".task_execution_entities " +
                                            "WHERE task_execution_id = ? AND lu_name = ? AND id_type = 'ENTITY'";

                                    Db.Row childStats = db(TDM).fetch(childStatsSql, taskExecutionID, luName).firstRow();
                                    total = "" + childStats.get("total");
                                    copied= "" + childStats.get("copied");
									failed= "" + childStats.get("failed");
                                    //log.info("Vertical results: total: " + total + ", copied: " + copied);

                                }


								db(TDM).execute(updateTaskExecutionListSql, new Object[]{status, total, copied, failed, //taskStartDate.toString(),  -- no need to set the start time
										taskEndDate.toString(), num_of_processed_ref_tables, num_of_copied_ref_tables, num_of_failed_ref_tables,
										taskID, taskExecutionID, luID, processID});

								// TDM 8.1 Update TDMDB table TDM_PARAMS_DISTINCT_VALUES
                                // TDM 9.1 Params Coupling 
								if (luID != 0 && processID == 0 && "completed".equalsIgnoreCase(status)) {
                                    if(paramsCoupling) {
                                        fnUpdateParamCouplingDistinctValues(srcEnv, luName, taskExecutionID);
                                    }else{
                                        fnUpdateParamDistinctValues(srcEnv, luName, taskExecutionID);
                                    }
								}
                                
							} // end of if status is not running
						}// end if ( (batchStats).contains("\"Level\" : \"Cluster\"") )
					} // end of else (if the selection method is not 'REF')
				} catch (Exception e){
					log.error("Task Failed");
					log.error(e.getMessage(),e);
					status = FAILED;
					db(TDM).execute(updateTaskExecutionListSql, new Object[]{status, total, copied, failed, //taskStartDate.toString(), -- No need to set the start time
							taskEndDate.toString(), num_of_processed_ref_tables, num_of_copied_ref_tables, num_of_failed_ref_tables, taskID, taskExecutionID, luID, processID});
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
		String sqlCompletedTasks= "select distinct task_execution_id, upper(out.task_type) as task_type, selection_method, sync_mode, execution_status from " + TDMDB_SCHEMA + ".task_execution_list out, " + TDMDB_SCHEMA + ".tasks t1 where  " + 
		"out.task_id = t1.task_id and out.synced_to_fabric = FALSE "+ 
		"and not exists (select 1 from " + TDMDB_SCHEMA + ".task_execution_list tbl, " + TDMDB_SCHEMA + ".tasks t where tbl.task_execution_id = out.task_execution_id " +  
		"and t.task_id = tbl.task_id " +
		"and tbl.execution_status not in ('completed','failed','killed') )";
		
		String taskExecutionId= "";
		String taskType="";
		String selectionMethod = "";
		String syncMode = "";
        String executionStatus = "";
		
		try
		{
			rows = db(TDM).fetch(sqlCompletedTasks);
		
			for (Db.Row row:rows){
				// TALi- 25-Sep-19- TDM 5.5- add the clean of redis for completed load tasks
				if(row.get("task_execution_id") != null)
				{
					taskExecutionId = "" + row.get("task_execution_id");
					taskType = "" + row.get("task_type");
					selectionMethod = "" + row.get("selection_method");
					syncMode = "" + row.get("sync_mode");
                    executionStatus = "" + row.get("execution_status");
					
					//log.info("fnCheckMigrateAndUpdateTDMDB - Loading task: " + taskExecutionId + " to TDM");
					int count = 0;
					int retries = 5;
					while(!Thread.currentThread().isInterrupted()) {
						try
						{
                            // TDM 9.0 Calling function to update task execution summary based on TDMDB and not part of the sync
							fnUpdateTaskSummaryTableBasedOnTDMDB(taskExecutionId);
                            // Get the task into the TDM LU
							fabric().execute("get TDM." + taskExecutionId);
							db(TDM).execute("update " + TDMDB_SCHEMA + ".task_execution_list set synced_to_fabric=TRUE where task_execution_id = ?", taskExecutionId );
		                           // TDM 8.1 - in case of Custom Logic, drop the entity list table if exists
		                           if ("C".equalsIgnoreCase(selectionMethod) && !"stopped".equalsIgnoreCase(executionStatus)) {
		                              String dropSql = "DROP TABLE IF EXISTS " + TDMDB_SCHEMA + ".entity_list_" + taskExecutionId;
		                              db(TDM).execute(dropSql);
		                          }
							break;
						}catch(Exception e){
							if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
								throw e;
							}
							if (++count >= retries) {
								db(TDM).execute("update " + TDMDB_SCHEMA + ".task_execution_list set synced_to_fabric=TRUE where task_execution_id = ?", taskExecutionId );
								log.error("Failed to get task execution id: " + taskExecutionId + " into TDM LU. Updating the synced_to_fabric indicator of TDM DB. Error message: " + e.getMessage(),e);
								retries = 0;
								break;
							} else {
								log.warn("Failed to update to get the task execution id: " + taskExecutionId + " into TDM LU.  this is retry number: " + count);
								Thread.sleep(5000);
							}
						}
		
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

	@out(name = "result", type = Object.class, desc = "")
    public static Object typeCheck(Object val) throws Exception {

        try {

            if (val instanceof Utils.NullType || val == null) {
                return null;
            }

            // Check first for the commonly used types to save all other 'instanceof'.
            if (isCommonlyUsedType(val)) {
                Class cls = val.getClass();
                //log.info("val instanceof " + cls.getName());
                if (val instanceof java.math.BigDecimal){
                    return  ((BigDecimal) val).doubleValue();
                }else{
                    return val;
                }
            }

            if (val instanceof K2TimestampWithTimeZone) {
                //log.info("val " + val + "instanceof K2TimestampWithTimeZone ");
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Timestamp) {
                //return ((Date) val).getTime();
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Date) {
                return ((Date) val).toString();
            }

            if (val instanceof java.sql.Time) {
                return ((Date) val).toString();
            }

            if (val instanceof java.util.Date) {
                //log.info("val " + val + " instanceof java.util.Date");
                return ((Date) val).toString();
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

    @desc("This function updates TASK_EXECUTION_SUMMARY table for ended tasks")
	public static void fnUpdateTaskSummaryTableBasedOnTDMDB(String taskExecId) throws Exception {

		String executionStatus = "completed";
		String startExecTime = "";
		String endExecTime = "";
		Long totProcessedRootEnt = 0L;
		Long totCopiedRootEnt = 0L;
		Long totFailedRootEnt = 0L;
		Long totProcessedRefTabs = 0L;
		Long totCopiedRefTabs = 0L;
		Long totFailedRefTabs = 0L;
		String updateDate = "";
        String versionExeID = "";
        String subsetExeID="";
		String expDate = "";

		// SQL queries on TDM DB

		String getTotProcessedRootIdsSQL = "select num_of_processed_entities from " + TDMDB_SCHEMA + ".task_execution_list where task_Execution_id = ? " +
                "and parent_lu_id is null and lu_id <> 0 limit 1";

		//String getTotCopiedRootIdsSQL = "select count(*) from (select distinct root_entity_id from " + TDMDB_SCHEMA + ".task_execution_entities t1 where task_Execution_id = ? and id_type = 'ENTITY' " +
		//		" and execution_status = 'completed' and not exists (select 1 from " + TDMDB_SCHEMA + ".task_execution_entities t2 " +
		//		" where t2.task_execution_id = t1.task_execution_id and t2.id_type = 'ENTITY' and t2.root_entity_id = t1.root_entity_id and t2.execution_status <> 'completed')) entList";

		String getTotFailedRootIdsSQL = "select count(distinct root_entity_id) from " + TDMDB_SCHEMA + ".task_execution_entities where task_Execution_id = ? " +
				"and execution_status <> 'completed'";

        String getTableStaticsSQL = "select sum(num_of_processed_ref_tables) as num_of_processed_ref_tables, " +
                "sum(num_of_copied_ref_tables) as num_of_copied_ref_tables, sum(num_of_failed_ref_tables) as num_of_failed_ref_tables " +
                "from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id = ?";
		
        //String getTotProcessedTablesSQL = "select count(*) from (select distinct root_entity_id from " + TDMDB_SCHEMA + ".task_execution_entities where task_Execution_id = ? and id_type = 'REFERENCE') entList";

		//String getTotCopiedTablesSQL = "select count(*) from (select distinct root_entity_id from " + TDMDB_SCHEMA + ".task_execution_entities where task_Execution_id = ? and id_type = 'REFERENCE' and execution_status = 'completed' ) entList";

		//String getTotFailedTablesSQL = "select count(*) from (select distinct root_entity_id from " + TDMDB_SCHEMA + ".task_execution_entities where task_Execution_id = ? and id_type = 'REFERENCE' and execution_status <> 'completed' ) entList";

		String getTotProcessedPostExeProcesses = "select count(*) from " + TDMDB_SCHEMA + ".task_execution_list l inner join " + TDMDB_SCHEMA + ".tasks_exe_process p on l.task_id=p.task_id and l.process_id=p.process_id where l.task_execution_id = ? and l.process_id != 0 and p.process_type ='post' ";

		String getTotSucceededPostExeProcesses = "select count(*) from " + TDMDB_SCHEMA + ".task_execution_list l inner join " + TDMDB_SCHEMA + ".tasks_exe_process p on l.task_id=p.task_id and l.process_id=p.process_id where l.task_execution_id = ? and l.process_id != 0 and p.process_type ='post' and l.execution_status ='completed' ";

		String getTotFailedPostExeProcesses = "select count(*) from " + TDMDB_SCHEMA + ".task_execution_list l inner join " + TDMDB_SCHEMA + ".tasks_exe_process p on l.task_id=p.task_id and l.process_id=p.process_id where l.task_execution_id = ? and l.process_id != 0 and p.process_type ='post' and l.execution_status <> 'completed' ";
        
        String getTotProcessedPreExeProcesses = "select count(*) from " + TDMDB_SCHEMA + ".task_execution_list l inner join " + TDMDB_SCHEMA + ".tasks_exe_process p on l.task_id=p.task_id and l.process_id=p.process_id where l.task_execution_id = ? and l.process_id != 0 and p.process_type ='pre' ";

		String getTotSucceededPreExeProcesses = "select count(*) from " + TDMDB_SCHEMA + ".task_execution_list l inner join " + TDMDB_SCHEMA + ".tasks_exe_process p on l.task_id=p.task_id and l.process_id=p.process_id where l.task_execution_id = ? and l.process_id != 0 and p.process_type ='pre' and l.execution_status ='completed' ";

		String getTotFailedPreExeProcesses = "select count(*) from " + TDMDB_SCHEMA + ".task_execution_list l inner join " + TDMDB_SCHEMA + ".tasks_exe_process p on l.task_id=p.task_id and l.process_id=p.process_id where l.task_execution_id = ? and l.process_id != 0 and p.process_type ='pre' and l.execution_status <> 'completed' ";

		String getRootTaskStatus = "select execution_status from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id = ? and parent_lu_id is null and lu_id <> 0";

		// Get data from the TDM DB

		totProcessedRootEnt = Long.valueOf(db(TDM).fetch(getTotProcessedRootIdsSQL, taskExecId).firstValue().toString());

		//totCopiedRootEnt = Long.valueOf(db(TDM).fetch(getTotCopiedRootIdsSQL, taskExecId).firstValue().toString());

		totFailedRootEnt  = Long.valueOf(db(TDM).fetch(getTotFailedRootIdsSQL, taskExecId).firstValue().toString());
        totCopiedRootEnt = totProcessedRootEnt - totFailedRootEnt;

        Db.Row tablesStatics = db(TDM).fetch(getTableStaticsSQL, taskExecId).firstRow();

		totProcessedRefTabs = Long.valueOf(tablesStatics.get("num_of_processed_ref_tables").toString());

		totCopiedRefTabs = Long.valueOf(tablesStatics.get("num_of_copied_ref_tables").toString());

		totFailedRefTabs =Long.valueOf(tablesStatics.get("num_of_failed_ref_tables").toString());

		Long totNumOfProcessedPostExecutions =  Long.valueOf(db(TDM).fetch(getTotProcessedPostExeProcesses, taskExecId).firstValue().toString());
		Long totNumOfSucceededPostExecutions =  Long.valueOf(db(TDM).fetch(getTotSucceededPostExeProcesses, taskExecId).firstValue().toString());
		Long totNumOfFailedPostExecutions =  Long.valueOf(db(TDM).fetch(getTotFailedPostExeProcesses, taskExecId).firstValue().toString());
        Long totNumOfProcessedPreExecutions =  Long.valueOf(db(TDM).fetch(getTotProcessedPreExeProcesses, taskExecId).firstValue().toString());
		Long totNumOfSucceededPreExecutions =  Long.valueOf(db(TDM).fetch(getTotSucceededPreExeProcesses, taskExecId).firstValue().toString());
		Long totNumOfFailedPreExecutions =  Long.valueOf(db(TDM).fetch(getTotFailedPreExeProcesses, taskExecId).firstValue().toString());

		String rootTaskStatus = "" + db(TDM).fetch(getRootTaskStatus, taskExecId).firstValue();

		if ((totCopiedRootEnt == 0 && totCopiedRefTabs == 0) || ("failed".equalsIgnoreCase(rootTaskStatus)) ) {
			executionStatus = "failed";
		}

		Long numOfStoppedLUs =  Long.valueOf(db(TDM).fetch("select count(*) from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id = ? and execution_status = 'stopped'",taskExecId).firstValue().toString());

		if (numOfStoppedLUs > 0) {
			executionStatus = "stopped";
		}

		startExecTime = "" + db(TDM).fetch("select min(start_execution_time) from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id = ? ",taskExecId).firstValue();
		endExecTime = "" + db(TDM).fetch("select max(end_execution_time) from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id = ? ",taskExecId).firstValue();

        versionExeID = "" + db(TDM).fetch("select version_task_execution_id from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id = ? ",taskExecId).firstValue();
        subsetExeID = "" + db(TDM).fetch("select subset_task_execution_id from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id = ? ",taskExecId).firstValue();
		expDate = "" + db(TDM).fetch("select max(expiration_date) from " + TDMDB_SCHEMA + ".task_execution_list where task_execution_id = ? ",taskExecId).firstValue();
		updateDate = Instant.now().toString();
		//log.info("Setting Dates - start_execution_time: " + startExecTime + ". end_execution_time: " + endExecTime +
		//		 ", version_datetime: <" + versionDateTime + ">, version_expiration_date: <" + versionExpDate + ">, update_date: " + updateDate);

		String sqlUpdateTaskSummaryTable = "update " + TDMDB_SCHEMA + ".task_execution_summary set execution_status = ?, " +
				"tot_num_of_processed_root_entities = ?, tot_num_of_copied_root_entities = ?, tot_num_of_failed_root_entities = ?, " +
				"tot_num_of_processed_ref_tables = ?, tot_num_of_copied_ref_tables = ?, tot_num_of_failed_ref_tables = ?, " +
				"tot_num_of_processed_post_executions = ?, tot_num_of_succeeded_post_executions = ?, tot_num_of_failed_post_executions = ? ," +	
                "tot_num_of_processed_pre_executions = ?, tot_num_of_succeeded_pre_executions = ?, tot_num_of_failed_pre_executions = ?," +
                "subset_task_execution_id = ?,update_date = ?";

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
        paramList.add(totNumOfProcessedPreExecutions);
		paramList.add(totNumOfSucceededPreExecutions);
		paramList.add(totNumOfFailedPreExecutions);
        paramList.add(subsetExeID);
		paramList.add(updateDate);

		if (!"null".equals(startExecTime) && !"".equals(startExecTime)) {
			sqlUpdateTaskSummaryTable += ", start_execution_time = ?";
			paramList.add(startExecTime);
		}
		if (!"null".equals(endExecTime) && !"".equals(endExecTime)) {
			sqlUpdateTaskSummaryTable += ", end_execution_time = ?";
			paramList.add(endExecTime);
		}
		if (!"0".equals(versionExeID) && !"null".equalsIgnoreCase(versionExeID)) {
			sqlUpdateTaskSummaryTable += ", version_task_execution_id = ?";
			paramList.add(versionExeID);
		}
		if (!"null".equals(expDate) && !"".equals(expDate)) {
			sqlUpdateTaskSummaryTable += ", expiration_date = ?";
			paramList.add(expDate);
        }
		sqlUpdateTaskSummaryTable += " where task_execution_id = ?";
		paramList.add(taskExecId);
		Object[] params = paramList.toArray();

		db(TDM).execute(sqlUpdateTaskSummaryTable,params);
        
	}

	@desc("This function runs the Fabric command migrate_summary and returns its output")
	@out(name = "migrateSummaryOutput", type = Map.class, desc = "")
	public static Map<String,Object> fnBatchStats(String i_migrateID) throws Exception {
		int retries = 0;
		Map<String, Object> response = null;
		
		
		while (response == null && retries < 4) {
			try {
				List<Map<String, Object>> stats = (List<Map<String, Object>>) fnBatchStatistics(i_migrateID, "S");
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
		String insertSql = "insert into " + TDMDB_SCHEMA + ".TASK_EXE_ERROR_DETAILED (TASK_EXECUTION_ID,LU_NAME,ENTITY_ID,IID,TARGET_ENTITY_ID, " +
			"ERROR_CATEGORY, ERROR_MESSAGE) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?)";
		
		String getErrorlistSql = "";

        Db.Rows errorList = fabric().fetch("batch_details '" + i_migrateId + "' STATUS = 'FAILED' LIMIT = " + TDM_BATCH_LIMIT);
		for (Db.Row errorRec : errorList) {
			String entityId = "" + errorRec.get("Entity ID");
			String erroMsg = "" + errorRec.get("Error");
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


	@type(UserJob)
	public static void tdmTaskScheduler() throws Exception {
		TdmTaskScheduler.fnTdmTaskScheduler();
	}


	@desc("Function to call reject instance with given message")
	public static void fnRejectInstance(String msg) throws Exception {
        String taskExecId = "" + fabric().fetch("SELECT iid(?)", TDM).firstValue();
		fnUpdateTaskSummaryTableBasedOnTDMDB(taskExecId);
		rejectInstance(msg);
	}

	static String getBatchStatus(Object originalStatus) {
		if (originalStatus == null) {
			return null;
		}
		if (originalStatus.equals("WAITING_FOR_JOB")) {
			return WAITING;
		} else if (originalStatus.equals("IN_PROGRESS")) {
			return RUNNING;
		} else if(originalStatus.equals("DONE")) {
			return COMPLETED;
		} else if (originalStatus.equals("CANCELLED")) {
			return STOPPED;
		} else if (originalStatus.equals("PAUSED")) {
			return PAUSED;
		}

		return originalStatus.toString().toLowerCase();
	}
	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,String> getCommandForAll(String luName, String taskExecutionId, String sourceEnvName, String versionInd, String separator, String openSeparator, String closeSeparator,String versionExeID,String dcName, Long luId, String sessionGlobals) throws Exception {
		String modified_sql = "";
		String batchCommand = "";
		String interface_name = null;
		String sql = null;
		String externalTableFlow = null;
		// TDM 8.1 using Mtables
		Map<String, String> batchStrings = new HashMap<>();
		Map<String, Object> migrateListInputs = new HashMap<>();
		migrateListInputs.put("lu_name",luName);
		migrateListInputs.put("source_env_name",sourceEnvName);
		List<Map<String, Object>> migrateList = MtableLookup("MigrateList", migrateListInputs, MTable.Feature.caseInsensitive);
		for(Map<String, Object> t : migrateList){
			interface_name = "" + t.get("interface_name");
			sql = "" + t.get("ig_sql");
			externalTableFlow =  "" + t.get("external_table_flow");
		
		}
		// TDM 5.1- If no translation record was found for the combination of lu name + source env- get the translation with null value of source env as input
		if (interface_name == null) {
			migrateListInputs.put("source_env_name",null);
			migrateList = MtableLookup("MigrateList", migrateListInputs,MTable.Feature.caseInsensitive);
			for(Map<String, Object> t : migrateList){
				interface_name = "" + t.get("interface_name");
				sql = "" + t.get("ig_sql");
				externalTableFlow =  "" + t.get("external_table_flow");
		
			}
		}
		if(migrateList.size()>1){
			log.warn("More than one Row matches the Mtable lookup, last row is picked by default");
		}
		
		if ((interface_name == null || "null".equalsIgnoreCase(interface_name))
				&& (sql == null || "null".equalsIgnoreCase(sql))
				&& (externalTableFlow == null || "null".equalsIgnoreCase(externalTableFlow))) {
		
			throw new RuntimeException("No entry found for LU_NAME: " + luName + " in Mtable MigrateList");
		}
		
		interface_name = ("null".equalsIgnoreCase(interface_name)) ? "" : interface_name;
		sql = ("null".equalsIgnoreCase(sql)) ? "" : sql.replaceAll("\n", " ");
		externalTableFlow = ("null".equalsIgnoreCase(externalTableFlow)) ? "" : externalTableFlow;
		
		if (externalTableFlow.isEmpty() || "null".equalsIgnoreCase(externalTableFlow)) {
			if (interface_name.isEmpty() || "null".equalsIgnoreCase(interface_name)) {
				throw new RuntimeException("No Interface found to run query : " + sql + " in Mtable MigrateList");
			}
            String splitSQL[] = sql.toLowerCase().split("\\s+");
			String qry_entity_col = "";
			for (int i = 0; i < splitSQL.length; i++) {
				if (splitSQL[i].equals("from")) {
					qry_entity_col = splitSQL[i - 1].replaceAll("\\s+", "");
					break;
				}
			}
		
			// get original SQL statement "select" including the next SQL command like "distinct"
            String select = sql.toLowerCase().substring(0, sql.toLowerCase().indexOf(qry_entity_col));
			String sql_part2 = sql.substring(sql.toLowerCase().indexOf(" from ")).replace("'", "''");
		
			//Using trnMigrateListQueryFormats to support DBs that don't accept || as concatenation operator
		
			String interface_type = null ;
			DbInterface dbObj = com.k2view.cdbms.lut.InterfacesManager.getInstance().getTypedInterface(interface_name, sourceEnvName);
			if(dbObj!=null) {
				interface_type = dbObj.jdbcDriver;
			}
			Map<String, Object> migrateListQueryFormatsInput = new HashMap<>();
			migrateListQueryFormatsInput.put("interface_type", interface_type);
			migrateListQueryFormatsInput.put("version_ind", versionInd);
			List<Map<String, Object>> migrateListQueryFormats = MtableLookup("MigrateListQueryFormats", migrateListQueryFormatsInput, MTable.Feature.caseInsensitive);
			String query_format = null;
            if (migrateListQueryFormats != null) {
			    for (Map<String, Object> t : migrateListQueryFormats) {
				    query_format = "" + t.get("query_format");
    		    }
            }
			if (!(query_format == null || query_format.isEmpty() || "null".equalsIgnoreCase(query_format))){
				// TDM 5.1- add the handle of configurable separator for special formats- the separator may need to be added to the trnMigrateListQueryFormats
                String sql_part1 = sql.toLowerCase().substring(0, sql.toLowerCase().indexOf(qry_entity_col));
		
				if (!openSeparator.equals("") && !closeSeparator.equals("")) // if the open and close separators for the entity id are populated
				{
					StringBuffer sqlStr = new StringBuffer(query_format);
					// Get the substring between source env and entity id
		
					String formatSeparator = query_format.substring(query_format.indexOf("<source_env_name>") + "<source_env_named>".length(), query_format.indexOf("<entity_id>"));
					formatSeparator = formatSeparator.replaceFirst("'" + separator + "'", "");
					String insertOpenStr = "'" + openSeparator + "'" + formatSeparator;
					String insertCloseStr = formatSeparator + "'" + closeSeparator + "'";
					sqlStr.insert(sqlStr.indexOf("<entity_id>"), insertOpenStr);
					sqlStr.insert(sqlStr.indexOf("<entity_id>") + "<entity_id>".length(), insertCloseStr);
					sql_part1 = select + " " + sqlStr.toString();
				}
                sql_part1 = query_format;
									sql_part1 = sql_part1.replace("<source_env_name>", "'" + sourceEnvName + "'");
					sql_part1 = sql_part1.replace("<entity_id>", qry_entity_col);
					if (versionInd.equals("true")) {
                    String taskId = "0".equalsIgnoreCase(versionExeID) ? taskExecutionId : versionExeID;
						sql_part1 = sql_part1.replace("<task_execution_id>", "'" + taskId + "'");
					}
                // Escape single quotes and build the final query
					modified_sql = select + " " + sql_part1.replace("'", "''") + sql_part2;
				
			}
			//No query format --> modify query by using || concatenation operator
			else {
				// TDM 5.1- concatenate the open and close separators to the qry_entity_col variables
		
				if (!openSeparator.equals(""))
					qry_entity_col = "''" + openSeparator + "''||" + qry_entity_col;
		
				if (!closeSeparator.equals(""))
					qry_entity_col = qry_entity_col + "||''" + closeSeparator + "''";
		
				if (versionInd.equals("true")) { //Modify entities to be in the format of <source_env>_<entity_id>_<task_name>_<timestamp>
					if("0".equalsIgnoreCase(versionExeID)) {//Modify entities to be in the format of <source_env>_<entity_id>_<task_name>_<timestamp>
						modified_sql = select + " ''" + sourceEnvName + separator + "''||" + qry_entity_col + "||''" + separator + taskExecutionId + "''" + sql_part2;
					}else {
						modified_sql = select + " ''" + sourceEnvName + separator + "''||" + qry_entity_col + "||''" + separator + versionExeID + "''" + sql_part2;
					}				} else { ////Modify entities to be in the format of <source_env>_<entity_id>
					modified_sql = select + " ''" + sourceEnvName + separator + "''||" + qry_entity_col + sql_part2;
				}
			}
			batchStrings.put("mode","query");
		
		} else { //External Flow was supplied to create the entity list table.
		
			//TDM 8.1 replace function getCommandForAllCL with getCustomLogicBatch. 
			//Sending the number of entities as negative to suppress the limitation as it is set in the extrenal flow
			//And sending the parameters as empty string, as we do not support parameters for this type of custom logic
			//modified_sql = getCommandForAllCL(luName, externalTableFlow, taskExecutionId, luId, dcName);
			Long numberOfEntities = -1L;
			Map<String,String> BFCmdAndInterface = getCustomLogicBatch(luName, externalTableFlow, taskExecutionId, luId, 
					dcName, numberOfEntities, "", sessionGlobals, false);
			modified_sql = BFCmdAndInterface.get("batchQuery");
			interface_name = BFCmdAndInterface.get("batchInterface");
			batchStrings.put("mode","external_flow");
		}
		
		if (dcName != null && !dcName.isEmpty()) {
			batchCommand = "batch " + luName + " from " + interface_name + " using (?) FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH AFFINITY='" + dcName + "' ASYNC=true";
		} else {// input DC is empty
			batchCommand = "batch " + luName + " from " + interface_name + " using (?) FABRIC_COMMAND=\"sync_instance " + luName + ".?\" WITH ASYNC=true";
		}
		
		batchStrings.put("batchCommand", batchCommand);
		batchStrings.put("usingClause", modified_sql);
		batchStrings.put("interface",interface_name);
		return batchStrings;
	}

	@out(name = "result", type = Map.class, desc = "")
	public static Map<String,String> getCustomLogicBatch(String luName, String customLogicFlow, String taskExecutionId, Long luId, 
        String dcName, Long entitiesLimit, String flowParams, String sessionGlobals, Boolean cloneInd) throws Exception {
		String batchQuery = "";
		String batchInterface = TDM;
		Map<String, String> result = new HashMap<>();
        Map<String, List<Map <String, Object>>> clFlowParamJson=null;
		
		// TDM 8.1 - Check if the flow is direct flow that does not require Table
		Boolean directFlow = Boolean.parseBoolean((String) fabric().fetch("broadway " + luName + ".CheckIfCustomFlowIsDirect LU_NAME = '" + luName +
				"', FLOW_NAME = '" + customLogicFlow + "' RESULT_STRUCTURE=COLUMN").firstValue());
		
		flowParams = flowParams.replaceAll("\\\\n","").replaceAll("\\\\t","");
		//log.info("flowParams after replace: " + flowParams);
		if( flowParams!=null  && !("null".equalsIgnoreCase(flowParams))){
            clFlowParamJson = Json.get().fromJson(flowParams);

        }
		
		String fabricCommandParams = " LU_NAME='" + luName + "', SESSION_GLOBALS='" + sessionGlobals + "'";
		
        if (cloneInd) {
            fabricCommandParams = fabricCommandParams + ", NUM_OF_ENTITIES=1";

        } else 	if (entitiesLimit > 0) {
			fabricCommandParams = fabricCommandParams + ", NUM_OF_ENTITIES=" + entitiesLimit;
		}
		
		if (clFlowParamJson != null && !(clFlowParamJson.isEmpty())) {
			List<Map <String, Object>> clFlowParamList = clFlowParamJson.get("inputs");
			for (Map <String, Object> clFlowParamMap : clFlowParamList) {
				Object paramValue = clFlowParamMap.get("value");
				if("".equals(paramValue)){
					paramValue=null;
				}
				fabricCommandParams += ", " + clFlowParamMap.get("name") + "=\"" + paramValue + "\"";
			}
		}
		
		if (directFlow) {
			//log.info("DIRECT FLOW");
            batchQuery = "broadway " + luName + "."  +  customLogicFlow + fabricCommandParams;
            //log.info("batchQuery: " + batchQuery);
            if (cloneInd) {
                batchQuery += ", RESULT_STRUCTURE=CURSOR";
                Object iid = fabric().fetch(batchQuery).firstValue();
                //log.info("batchQuery result: " + iid);
                batchQuery = "SELECT '" + iid.toString() + 
                    "#params#{\"clone_id\" : '||generate_series(1, " + entitiesLimit + " )||'}' as entity_id ";
                batchInterface = TDM;
            } else {
			    batchQuery = "broadway " + luName + "."  +  customLogicFlow + fabricCommandParams;
			    //log.info("Setting interface to Fabric");
			    batchInterface = DB_FABRIC;
            }
		} else {
			// TDM 7.5.1 - If the entity List table does not exists create it
			String createEntityListTab = "broadway " + luName + ".createLuExternalEntityListTable taskExecutionId = " + taskExecutionId;
			//log.info("createEntityListTab: " + createEntityListTab);
			fabric().execute(createEntityListTab);
			
			String affinity = !Util.isEmpty(dcName) ? "affinity='" + dcName + "'" : "";
			String batchCommand = "BATCH " + luName + ".(CL_"+ luName + "_" + taskExecutionId + ") fabric_command=? with " + affinity + " async=true";
			//log.info("Custom Logic batchCommand: " + batchCommand);
			
			String broadwayCommand = "broadway " + luName + "."  +  customLogicFlow +  " iid=?," + fabricCommandParams;
			//log.info("Custom Logic broadwayCommand: " + broadwayCommand);
			String batchId = "" + fabric().fetch(batchCommand, broadwayCommand).firstValue();
			db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list set execution_status = 'STARTEXECUTIONREQUESTED', fabric_execution_id = ? " +
					"WHERE task_execution_id=? and lu_id = ? and process_id=0 ", batchId, taskExecutionId, luId);
			
			String waitForBatch = "broadway " + luName + ".WaitForCustomLogicFlow taskExecutionId = " + taskExecutionId + ", batchId = '" + batchId + "', RESULT_STRUCTURE=ROW";
			//log.info("Custom Logic waitForBatch: " + waitForBatch);
			Db.Row entityListTableRec = fabric().fetch(waitForBatch).firstRow();
			String entityListTable = "" + entityListTableRec.get("value");

            if (cloneInd) {
                batchQuery = "select tdm_eid||'#params#{\"clone_id\" : '||generate_series(1, " + entitiesLimit + " )||'}' as entity_id from " + entityListTable;
            } else {
			    batchQuery = "select tdm_eid from " + entityListTable;
            }
		}
		
		result.put("batchQuery", batchQuery);
		result.put("batchInterface", batchInterface);
		return result;
	}

    public static Map<String,String> getEntityListByBF(String luName, String broadwayCommand, String taskExecutionId,Long luId, 
    String dcName, Long entitiesLimit,  Boolean cloneInd) throws Exception {
        Map<String, String> result = new HashMap<>();
        String createEntityListTab = "broadway " + luName + ".createLuExternalEntityListTable taskExecutionId = " + taskExecutionId;
			//log.info("createEntityListTab: " + createEntityListTab);
			fabric().execute(createEntityListTab);
			
			String affinity = !Util.isEmpty(dcName) ? "affinity='" + dcName + "'" : "";
			String batchCommand = "BATCH " + luName + ".(CL_"+ luName + "_" + taskExecutionId + ") fabric_command=? with " + affinity + " async=true";
			//log.info("Custom Logic batchCommand: " + batchCommand);
		
			//log.info("Custom Logic broadwayCommand: " + broadwayCommand);
			String batchId = "" + fabric().fetch(batchCommand, broadwayCommand).firstValue();
			db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list set execution_status = 'STARTEXECUTIONREQUESTED', fabric_execution_id = ? " +
					"WHERE task_execution_id=? and lu_id = ? and process_id=0 ", batchId, taskExecutionId, luId);
			
			String waitForBatch = "broadway " + luName + ".WaitForCustomLogicFlow taskExecutionId = " + taskExecutionId + ", batchId = '" + batchId + "', RESULT_STRUCTURE=ROW";
			//log.info("Custom Logic waitForBatch: " + waitForBatch);
			Db.Row entityListTableRec = fabric().fetch(waitForBatch).firstRow();
			String entityListTable = "" + entityListTableRec.get("value");

            String batchQuery = "";
            if (cloneInd) {
                batchQuery = "select tdm_eid||'#params#{\"clone_id\" : '||generate_series(1, " + entitiesLimit + " )||'}' as entity_id from " + entityListTable;
            } else {
			    batchQuery = "select tdm_eid from " + entityListTable;
            }

            result.put("batchQuery", batchQuery);
		    result.put("batchInterface", TDM);
		return result;

    }

    public static String addSeparators(String entityID) throws Exception {
        Object[] separators = fnGetIIdSeparatorsFromTDM();
        String open = (String) separators[0];
        String close = (String) separators[1];
        entityID = !Util.isEmpty(open) ? open + entityID : entityID;
        return !Util.isEmpty(close) ? entityID + close : entityID;
    }

    public static String fnGetParamType(String luName, String col) throws Exception{
        Map<String, Object> mapListInputs = new HashMap<>();
		mapListInputs.put("lu_name",luName);
		mapListInputs.put("param_name",col);
        String table_name = "";
        String column_name = "";
        String column_type = "";
        LUType luType = null;
		try {
			List<Map<String, Object>> mapList;
			mapList = MtableLookup("LuParamsMapping", mapListInputs, MTable.Feature.caseInsensitive);
            for(Map<String,Object> map : mapList ) {
                table_name=map.get("lu_table").toString().trim();
                column_name=map.get("lu_table_field").toString().toLowerCase().trim();
            }
            List<HashMap<String, String>> tableData = new ArrayList<HashMap<String, String>>();
            if (luName == null || Util.isEmpty(luName)) {
                luType = getLuType();
            } else {
                luType = LUType.getTypeByName(luName);
            }
                
            // Get columns for the specified table
            HashMap<String, LudbColumn> originalColumns = new HashMap<>(luType.ludbObjects.get(table_name).getLudbObjectColumns());

            // Create a new map with lower case keys to handle issue if mtable returns param all caps 
            HashMap<String, LudbColumn> columns = new HashMap<>();
            for (Map.Entry<String, LudbColumn> entry : originalColumns.entrySet()) {
                columns.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        
            if (column_name != null && !Util.isEmpty(column_name)) {
                // If column name is found , find and return its type else retun empty string
                if (columns.containsKey(column_name)) {
                    LudbColumn columnData = columns.get(column_name);
                    column_type =  columnData.columnType;
                }
            }
        }catch(Exception e){
            log.error(e.getMessage());
            throw new RuntimeException("Failed to get " + luName + " ,column type for column " + column_name + " in table " + table_name );
        }
        return column_type;

    }

    public static Map<String, Map<String, Object>> fnGetParamDistinctValues(String srcEnv, String luName)throws Exception {
        Map<String, Map<String, Object>> disitnctValuesMap = new HashMap<>();
        try{
            Db.Rows distinctValues = db(TDM).fetch(getDistinctValuesSql, srcEnv, luName.toUpperCase(), maxNumOfValues);
            for (Db.Row row: distinctValues) {
                Map<String, Object> fieldData = new HashMap<>();
                fieldData.put("numberOfValues", Long.parseLong(row.get("number_of_values").toString()));
                fieldData.put("isNumeric", Boolean.parseBoolean(row.get("is_numeric").toString()));
                fieldData.put("minValue", row.get("min_value").toString());
                fieldData.put("maxValue", row.get("max_value").toString());
                String value = row.get("field_values").toString();
                value = value.replace("{", "");
                value = value.replace("}", "");
                HashSet<String> values = new HashSet<String>();
                if (!value.isEmpty() && !"".equals(value)) {
                    values = new HashSet<String>(Arrays.stream(value.split(TDM_PARAMETERS_SEPARATOR)).collect(Collectors.toSet()));
                }
                fieldData.put("fieldValues", values);
                fieldData.put("newField", false);
                fieldData.put("fieldType", row.get("field_type").toString());
                disitnctValuesMap.put(row.get("field_name").toString(), fieldData);
            }
            if (distinctValues != null) {
                distinctValues.close();
            }
        }catch(Exception e){
            log.error(e);
            throw new RuntimeException("Failed to get TDMDB table TDM_PARAMS_DISTINCT_VALUES");
        }
        return disitnctValuesMap ;
    }

    public static Map<String, Map<String, Object>> fnReturnDistinctMapValues(String query , String luName,Map<String, Map<String, Object>> disitnctValuesMap)throws Exception{
        Db.Rows tableRecords;
        try{
            tableRecords = db(TDM).fetch(query);
            List<String> columnNames = tableRecords.getColumnNames();
            for (Db.Row row : tableRecords) {
                for (String columnName : columnNames) {
                    if (row.get(columnName) != null) {
                        String value = row.get(columnName).toString();
                        value = value.replace("{", "");
                        value = value.replace("}", "");
                        String col = columnName.split("\\.")[1];
                        String columnType = "";
                        if(isParamsCoupling()){
                            columnType = fnGetParamType(luName, col);
                        }
                        HashSet<String> values = new HashSet<String>(Arrays.stream(value.split(TDM_PARAMETERS_SEPARATOR)).collect(Collectors.toSet()));
                        disitnctValuesMap = fnUpdateDistinctFieldData(columnName, columnType, disitnctValuesMap, values);
                    }
                }
            }
            if (tableRecords != null) {
                tableRecords.close();
            }
        }catch(Exception e){
            log.error(e);
            throw new RuntimeException(e.getMessage());
        }
        return disitnctValuesMap;
    }
    
    public static void fnInsertValuestoDistinctParamsTable(String srcEnv , String luName,Map<String, Map<String, Object>> disitnctValuesMap){
        try{
			
            for (String key : disitnctValuesMap.keySet()) {
                Map<String, Object> fieldinfo = disitnctValuesMap.get(key);
                Long numberOfValues = Long.parseLong(fieldinfo.get("numberOfValues").toString());
                Boolean isNumeric  = Boolean.parseBoolean(fieldinfo.get("isNumeric").toString());
                String minValue = fieldinfo.get("minValue").toString();
                String maxValue = fieldinfo.get("maxValue").toString();
                String fieldType = fieldinfo.get("fieldType").toString();
                HashSet<String> valuesSet = (HashSet<String>)fieldinfo.get("fieldValues");
                Boolean newField  = Boolean.parseBoolean(fieldinfo.get("newField").toString());
                String newFieldvalues= String.join(TDM_PARAMETERS_SEPARATOR, valuesSet);
                if (newField) {
                    db(TDM).execute(insertDistintValuesSql, srcEnv, luName.toUpperCase(), key,
                        numberOfValues, newFieldvalues, isNumeric, minValue, maxValue,fieldType);
                } else {
                    db(TDM).execute(updateDistintValuesSql, numberOfValues, newFieldvalues, isNumeric, 
                        minValue, maxValue,fieldType, srcEnv, luName.toUpperCase(), key);
                }
            }
        }catch(Exception e){
            log.error(e);
            throw new RuntimeException("Failed to insert/update TDM_PARAMS_DISTINCT_VALUE");
        }
    }

	public static void fnUpdateParamCouplingDistinctValues(String srcEnv, String luName, String taskExecId) throws Exception {
        //log.info("Starting fnUpdateParamCouplingDistinctValues - luName: " + luName);
        // TDM 9.1 adding params coupling mode fetching data based on MDB_Exported schema		
        String sql = "broadway " + luName + ".GetTablesAndFieldsNames sourceEnv = " + srcEnv + " ,luName = " +  luName + " RESULT_STRUCTURE=COLUMN";
        try{
            Map<String, Map<String, Object>> disitnctValuesMap=fnGetParamDistinctValues(srcEnv,luName);
            Db.Rows tableRows = fabric().fetch(sql);
            for(Db.Row tableRow : tableRows ){
                if (tableRow != null && !tableRow.isEmpty()) { 
                    String field = "";
                    String column = "";
                    Map<?, ?> maps = ParamConvertor.toMap(tableRow.get("map"));
                    for (Map.Entry<?, ?> entry : maps.entrySet()) {
                        String tableName = entry.getKey().toString();
                        LinkedHashSet<String> columnsSet = (LinkedHashSet<String>) entry.getValue();
                        String[] columnsArr = new String[columnsSet.size()];
                        int idx = 0;
                        for (String col : columnsSet) {
                            field = col.split(TDM_PARAMETERS_SEPARATOR)[0];
                            column = col.split(TDM_PARAMETERS_SEPARATOR)[1];
                            columnsArr[idx] = "string_agg(" + column + "::text, '" + TDM_PARAMETERS_SEPARATOR + "') as \"" + field + "\"";
                            idx++;
                        }
                        String newSelClause = String.join(",", columnsArr);
                        String query = "SELECT " + newSelClause + " FROM "  + luName.toLowerCase() + "." + tableName +
                            " p INNER JOIN " +luName.toLowerCase()+ ".fabric_tdm_root r ON p.__iid=r.__iid INNER JOIN " + TDMDB_SCHEMA + ".task_execution_entities t ON r.__iid=t.entity_id" +
                            " WHERE r.source_env=t.source_env AND r.task_execution_id=t.task_execution_id::TEXT" +
                            " AND t.task_execution_id = " + taskExecId +
                            " AND t.lu_name = '" + luName + "' AND r.source_env = '" + srcEnv + "' AND t.execution_status = 'completed'";

                        disitnctValuesMap = fnReturnDistinctMapValues(query,luName,disitnctValuesMap);
                    }
                }
            }
            fnInsertValuestoDistinctParamsTable(srcEnv,luName,disitnctValuesMap);
        }catch(Exception e){
            String insertSql = "insert into " + TDMDB_SCHEMA + ".TASK_EXE_ERROR_DETAILED (TASK_EXECUTION_ID,LU_NAME,ENTITY_ID,IID,TARGET_ENTITY_ID, " +
                                        "ERROR_CATEGORY, ERROR_MESSAGE) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            db(TDM).execute(insertSql, taskExecId, luName, " ", " ", " ", "fnUpdateParamCouplingDistinctValues failed", e.getMessage());
            log.error(e);
            throw new RuntimeException("Failed to update TDMDB table TDM_PARAMS_DISTINCT_VALUES for param coupling mode");
        }
    }
    
    public static void fnUpdateParamDistinctValues(String srcEnv, String luName, String taskExecId) throws Exception {
		//log.info("Starting fnUpdateParamDistinctValues - luName: " + luName);		
		String sql = "select table_name, '\"' || array_to_string(array_agg(column_name), '\",\"') || '\"' as columns " +
		        " FROM information_schema.columns where table_schema = '" + TDMDB_SCHEMA + "'" +
		        " and table_name = '" + luName.toLowerCase() + "_params' and column_name like '%.%'" +
		        " and column_name not in (select REPLACE(field_name, '\"','') from " + TDMDB_SCHEMA + ".tdm_params_distinct_values" + 
		        " where lu_name = ? and number_of_values > ? and is_numeric = false and source_environment=?)" +
		        " group by table_name" +
		        " order by table_name";
		// TDM 8.1 - Get the existing distinct values of the LU's Parameters
        try{
            Map<String, Map<String, Object>> disitnctValuesMap=fnGetParamDistinctValues(srcEnv,luName);    
            Db.Row tableRow = db(TDM).fetch(sql, luName.toUpperCase(), maxNumOfValues,srcEnv).firstRow();
            if (tableRow != null && !tableRow.isEmpty()) {
                String tableName = tableRow.get("table_name").toString();
                String[] columnsArr = tableRow.get("columns").toString().split(",");
                for (int idx = 0; idx < columnsArr.length; idx++) {
                    columnsArr[idx] = "array_to_string(" + columnsArr[idx] + ", '" + TDM_PARAMETERS_SEPARATOR + "') as " + columnsArr[idx];
                }
                String newSelClause = String.join(",", columnsArr);
                String query = "SELECT " + newSelClause + " FROM "  + TDMDB_SCHEMA + "." + tableName +
                    " p, "  + TDMDB_SCHEMA + ".task_execution_entities t WHERE p.root_lu_name = t.root_lu_name " +
                    "AND p.root_iid = t.root_entity_id and p.entity_id = t.iid AND t.task_execution_id = " + taskExecId +
                    " AND t.lu_name = '" + luName + "' AND p.source_environment = '" + srcEnv + "' AND t.execution_status = 'completed'";

                disitnctValuesMap = fnReturnDistinctMapValues(query,luName,disitnctValuesMap);
                fnInsertValuestoDistinctParamsTable(srcEnv,luName,disitnctValuesMap);
            }
            //log.info("Finished fnUpdateParamDistinctValues");	
        }catch(Exception e){
            String insertSql = "insert into " + TDMDB_SCHEMA + ".TASK_EXE_ERROR_DETAILED (TASK_EXECUTION_ID,LU_NAME,ENTITY_ID,IID,TARGET_ENTITY_ID, " +
            "ERROR_CATEGORY, ERROR_MESSAGE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
            db(TDM).execute(insertSql, taskExecId, luName, " ", " ", " ", "fnUpdateParamDistinctValues failed", e.getMessage());
            log.error(e);
            throw new RuntimeException("Failed to update TDMDB table TDM_PARAMS_DISTINCT_VALUES");
        }
    }
}
