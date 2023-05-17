package com.k2view.cdbms.usercode.lu.TDM.TDM;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.usercode.common.TDM.SharedLogic;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.lang.reflect.Type;

import static com.k2view.cdbms.shared.user.UserCode.*;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetIIdSeparatorsFromTDM;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.setGlobals;
import static com.k2view.cdbms.usercode.common.TdmSharedUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getRetention;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.TASK_PROPERTIES.*;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.TASK_TYPES.*;
import static com.k2view.cdbms.usercode.lu.TDM.TDM_Extract.Logic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDMRef.SharedLogic.fnTdmReference;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class TdmExecuteTask {

    public static final String TDM = "TDM";
    public static final Log log = Log.a(TdmExecuteTask.class);
    public static final String TDMDB = "TDM";
    public static final String CASSANDRA = "DB_CASSANDRA";
    public static final String POST_EXECUTIONS = "Select t.process_id , t.process_name, t.execution_order from " + 
			TDMDB_SCHEMA + ".tasks_post_exe_process t, " + TDMDB_SCHEMA + ".task_execution_list l " +
            "where l.task_execution_id = ? and l.process_id = t.process_id and upper(l.execution_status) = 'PENDING' and l.task_id = t.task_id " +
            "order by t.execution_order;";
    public static final String FABRIC = "fabric";
    public static final String POST_EXECUTIONS_COUNT = "select count(*) from " + TDMDB_SCHEMA + ".tasks_post_exe_process tt, " + TDMDB_SCHEMA + ".task_execution_list ll " +
			"where ll.task_execution_id =? and ll.process_id = tt.process_id and tt.execution_order < ? and ll.execution_status NOT IN ('stopped','completed','failed','killed');";
    public static Map<String, String> entityInclusions = new HashMap<>();
	public static String separator = "";

    enum TASK_TYPES {
	    GENERATE(() -> Util.rte(() -> new String(getLuType().loadResource("TDM/fnTdmExecuteTask/query_get_extract_globals.sql"))),
				(taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties)}),
        EXTRACT(() -> Util.rte(() -> new String(getLuType().loadResource("TDM/fnTdmExecuteTask/query_get_extract_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties)}),
        LOAD(() -> Util.rte(() -> new String(getLuType().loadResource("TDM/fnTdmExecuteTask/query_get_load_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties)}),
		RESERVE(() -> Util.rte(() -> new String(getLuType().loadResource("TDM/fnTdmExecuteTask/query_get_load_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties)});

        Supplier<String> query;
        Function<Map<String, Object>, Object[]> params;

        TASK_TYPES(Supplier<String> query, Function<Map<String, Object>, Object[]> params){
            this.query = query;
            this.params = params;
        }

        public String query(){
            return this.query.get();
        }

        public Object[] params(Map<String, Object> args){
            return this.params.apply(args);
        }
    }

    public static void fnTdmExecuteTask() throws Exception {
        log.info("----------------- Starting tdmExecuteTask -------------------");
        String query = new String(getLuType().loadResource("TDM/fnTdmExecuteTask/query_get_tasks.sql"));

        Db tdmDB = db(TDM);
	
		String iidSeparator = "" + tdmDB.fetch("Select param_value from " + TDMDB_SCHEMA + ".tdm_general_parameters where param_name = 'iid_separator'").firstValue();
        //separator = !Util.isEmpty(iidSeparator) ? iidSeparator : "_";
		if ( !Util.isEmpty(iidSeparator) && !"null".equals(iidSeparator) ) {
			separator = iidSeparator;
		} else {
			separator = "_";
		}
		//log.info("tdmExecuteTask - separator: " + separator);
        tdmDB.fetch(query).forEach(row -> {

            // Get task properties
            Map<String, Object> taskProperties = getTaskProperties(row);

            Long taskExecutionID = (Long) taskProperties.get("task_execution_id");
			Long luID = (Long) LU_ID.get(taskProperties);

            // Check for child LU- if the parent LU execution failed- do not execute the child LU. Instead- update the execution_status of the child LU by the status of the parent LU and continue to the next LU
            String parentLUStatus = PARENT_LU_STATUS.get(taskProperties);
            if(isChildLU(taskProperties) && !parentLUStatus.toUpperCase().equals("COMPLETED")) {
                String startTime = "" + Util.rte(() -> db(TDM).fetch("select current_timestamp at time zone 'utc' ").firstValue());
                updateTaskExecutionStatus(parentLUStatus, taskExecutionID,luID, null, "0", "0", "0", startTime);
                return;
            }
			
			String startTime = "" + Util.rte(() -> db(TDM).fetch("select current_timestamp at time zone 'utc' ").firstValue());
            //updateTaskExecutionStatus("running", taskExecutionID,luID, "", "0", "0", "0", null);
            // Update task execution summary
            updateTaskExecutionSummary(taskExecutionID, "running");

			String selectionMethod = SELECTION_METHOD.get(taskProperties);
            String taskType = TASK_TYPE.get(taskProperties).toString().toLowerCase();
			// TDM 7.3 - The sync mode should be set at beginning based on task/env
			setSyncMode(taskProperties);

			if((long)LU_ID.get(taskProperties)  > 0){
            	switch (taskType) {
                	case "extract":
                    	log.info("----------------- extract task -------------------");
						String versionExpDate = null;
						if (!selectionMethod.equalsIgnoreCase("ref")) {

	        	            Map<String, String> migrationStatus = migrateEntitiesForTdmExtract(taskExecutionID, taskProperties);
    	        	        String fabricExecutionId = migrationStatus != null ? migrationStatus.get("fabric_execution_id") : null;
						
        	            	if (!Util.isEmpty(fabricExecutionId)) {
								versionExpDate = migrationStatus.get("version_expiration_date");
                	        	updateLuExecutionStatus(taskExecutionID, luID, "running", fabricExecutionId, versionExpDate);
								try {
									//log.info("TdmExecuteTask - Calling fnTdmReference");
                                    fnTdmReference(String.valueOf(taskExecutionID), taskType);
								} catch (Exception e) {
									log.error("TdmExecuteTask - Update extract task status to failed");
		                        	updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
    		                    	updateTaskExecutionSummary(taskExecutionID, "failed");
								}
            		        } else {
                		        // rollback LU and task status
                    		    updateLuExecutionStatus(taskExecutionID, luID, "failed", fabricExecutionId, versionExpDate);
								updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
	                        	updateTaskExecutionSummary(taskExecutionID, "failed");
    	                	}
						} else {
							try {
								//log.info("TdmExecuteTask - Calling fnTdmReference");
                                String batchId = fnTdmReference(String.valueOf(taskExecutionID), taskType);
								updateTaskExecutionBatchID(taskExecutionID, luID, batchId);
							} catch (Exception e) {
								log.error("TdmExecuteTask - Update extract task status to failed");
								updateLuExecutionStatus(taskExecutionID, luID, "failed", null, versionExpDate);
								updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
								updateTaskExecutionSummary(taskExecutionID, "failed");
							}
						}
        	            break;
                	case "load":
                    	log.info("----------------- load task -------------------");
	                    try {
							if (!selectionMethod.equalsIgnoreCase("ref")) {
        	        	        String executionId = executeLoadBatch(taskProperties);
            	        	    if (!executionId.isEmpty()) {
                	        	    updateTaskExecutionStatus("running", taskExecutionID, luID, executionId, "0", "0", "0", null);
									//log.info("TdmExecuteTask - Calling fnTdmReference");
                                    fnTdmReference(String.valueOf(taskExecutionID), taskType);
		                        } else {
    		                        // rollback LU and task status
        		                    updatedFailedStatus(taskExecutionID, luID);
            		                log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
                		        }
							} else {
								//log.info("TdmExecuteTask - Calling fnTdmReference");
                                String batchId = fnTdmReference(String.valueOf(taskExecutionID), taskType);
								updateTaskExecutionBatchID(taskExecutionID, luID, batchId);
							}
        	            } catch (Exception e) {
             	       	// rollback LU and task status
                 	       updatedFailedStatus(taskExecutionID, luID);
							updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
                        	log.error("Execution failed for task execution: " + taskExecutionID + " due to " + e.getMessage(), e);
                    	}
	                    break;
					// TDM 7.4 - Support new task type revese
					case "reserve":
						log.info("----------------- reserve task -------------------");
						try {
							// run broadway flow
							String executionId = executeReserveBatch(taskProperties);
							if (!executionId.isEmpty()) {
								// In case of reserve only one LU (root) will be executed, any other LU will be ignored
								if ("NA".equals(executionId)) {
              					  updateTaskExecutionStatus("completed", taskExecutionID,luID, null, "0", "0", "0", startTime);
					                return;
								}
								updateTaskExecutionStatus("running", taskExecutionID, luID, executionId, "0", "0", "0", null);
							} else {
								// rollback LU and task status
								updatedFailedStatus(taskExecutionID, luID);
								log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
							}
        	            } catch (Exception e) {
             	       	// rollback LU and task status
							updatedFailedStatus(taskExecutionID, luID);
							updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
							log.error("Execution failed for task execution: " + taskExecutionID + " due to " + e.getMessage(), e);
                    	}
						break;
	                 // TDM 8.0 - Support new task type generate synthetic entites
					case "generate":
						log.info("----------------- generate synthetic task -------------------");
						try {
							String executionId = executeGenerateBatch(taskProperties);
							if (!executionId.isEmpty()) {
								updateTaskExecutionStatus("running", taskExecutionID, luID, executionId, "0", "0", "0", null);
							} else {
								// rollback LU and task status
								updatedFailedStatus(taskExecutionID, luID);
								log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
							}
							
						} catch (Exception e) {
							// rollback LU and task status
							updatedFailedStatus(taskExecutionID, luID);
							updateLuRefExeFailedStatus(taskExecutionID, LU_NAME.get(taskProperties), "failed");
							log.error("Execution failed for task execution: " + taskExecutionID + " due to " + e.getMessage(), e);
						}
						break;
    	            default:
        	            log.error("Unknown task type '" + taskType + "'");
            	}
			} else {
		
                long luCount = (long) Util.rte(() -> db(TDM).fetch("SELECT COUNT(lu_id) FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? AND lu_id > 0 AND execution_status NOT IN ('stopped','completed','failed','killed');", taskExecutionID).firstValue());
                if(luCount == 0) {
                    //log.info("************* lu count = 0 starting post executions *************");
					// TDM 7.4 - 19.01.22 - Set globals at task level instead of instance level
                    //TASK_TYPES type = TASK_TYPES.valueOf(TASK_TYPE.get(taskProperties).toString().toUpperCase());
                    //Map<String,Object> globals = getGlobals(type.query(), taskProperties, new HashMap<>(), type.params(taskProperties));
					setGlobalsForTask(taskType, taskProperties);

                    try {
                        //String fabricCommandParams = " globals=" + globals + ", taskExecutionID=" + taskExecutionID; //todo: iid=?
						String fabricCommandParams = " taskExecutionID=" + taskExecutionID; //todo: iid=?
                        db(TDM).fetch(POST_EXECUTIONS, taskExecutionID).forEach(res -> runPostExecution(taskExecutionID, fabricCommandParams, res));
                    } catch (SQLException e) {
                        log.error("Execution of post failed for task execution" + taskExecutionID);
                        e.printStackTrace();
                    }
                }

			}
        });
    }
	
	private static String executeGenerateBatch(Map<String, Object> taskProperties) throws Exception {

		setGlobalsForTask("generate", taskProperties);
		String luName = LU_NAME.get(taskProperties);
		String entityInclusionOverride = "";

		entityInclusionOverride = getEntityInclusion(taskProperties);
		String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
		String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";
		String batchCommand = "BATCH " + luName + " FROM " + CASSANDRA + " USING(?) fabric_command=? with " + affinity + " async=true";
		String broadwayCommand = "broadway " + luName + ".TDMGenerateOrchestrator " + "iid=?, luName=" + luName 
            +", syncMode=FORCE";
        //log.info("batchCommand: " + batchCommand + " ,broadwayCommand: " + broadwayCommand);
		return (String) fabric().fetch(batchCommand, entityInclusionOverride, broadwayCommand).firstValue();
	}
	
    private static void runPostExecution(Long taskExecutionID, String fabricCommandParams, Db.Row res) {
        ResultSet resultSet = res.resultSet();
        Integer executionOrder = Util.rte(() -> resultSet.getInt("execution_order"));
        waitUntilPrevProcessDone(taskExecutionID, executionOrder);

        String processID = Util.rte(() -> resultSet.getString("process_id"));
        //log.info("************* set task execution list to running for process id " + processID + " *************");

        Map<String, Map<String, String>> trnPostProcessList = getTranslationsData("trnPostProcessList");
        String processName = Util.rte(() -> resultSet.getString("process_name"));
        String luName = trnPostProcessList.get(processName).get("lu_name"); //todo ????

		if (Util.isEmpty(luName)) { 
			luName = "TDM";
		}
        String broadwayCommand = "broadway " + luName + "." + processName +  " iid=?," + fabricCommandParams;
        String batch = "BATCH " + luName + ".('" + taskExecutionID + "_" + processID + "')" + " fabric_command='" + broadwayCommand + "' with async=true";
        //log.info("Starting batch command for post execution: " + batch);
        String executionId = Util.rte(() -> (String) db(FABRIC).fetch(batch).firstValue());
        Util.rte(()-> db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=?, fabric_execution_id=?, start_execution_time = (now() at time zone 'utc') WHERE task_execution_id=? and process_id=?", "running", executionId, taskExecutionID, processID));

    }
	
    private static void waitUntilPrevProcessDone(Long taskExecutionID, Integer executionOrder) {
        long count = -1;
        while(count != 0){
            count = (long) Util.rte(() ->db(TDM).fetch(POST_EXECUTIONS_COUNT, taskExecutionID, executionOrder).firstValue());
            //log.info("**************** post exec count = " +  count + "********************");
            Util.sleep(1000);
        }
    }

    private static void updatedFailedStatus(Long taskExecutionID, Long luID) {
        Timestamp endTime = (Timestamp) Util.rte(() -> db(TDM).fetch("select current_timestamp at time zone 'utc' ").firstValue());
        updateTaskExecutionStatus("failed", taskExecutionID, luID, null, "0", "0", "0", endTime);
        updateTaskExecutionSummary(taskExecutionID, "failed");
    }

    private static boolean isChildLU(Map<String, Object> taskProperties) {
        Long parentID = PARENT_LU_ID.get(taskProperties);
        return parentID != null && parentID > 0;
    }

    private static String executeLoadBatch(Map<String, Object> taskProperties) throws Exception {
        //log.info("----------------- preparing for load execution -------------------");
		 
		// TDM 7.4 - 19.01.22 - Set globals at task level instead of instance level
		//String srcSyncData = getSrcSyncDataVal(taskProperties);
		//Map<String,Object> globals = getGlobals(LOAD.query(), taskProperties, Util.map("TDM_SYNC_SOURCE_DATA", srcSyncData), LOAD.params(taskProperties));
		setGlobalsForTask("load", taskProperties);
	
        String selectionMethod = SELECTION_METHOD.get(taskProperties);
        String luName = LU_NAME.get(taskProperties);
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String entityInclusionOverride = "";
        
        //Calculate retention date + set TTL
		String syncMode = getSyncModeForLoad(taskProperties);
		String retentionPeriodType = RETENTION_PERIOD_TYPE.get(taskProperties);
		Long unixTime = System.currentTimeMillis();
		Long unixTime_plus_retention;
		String version_exp_date = null;
		String timeStamp = null;
		String versionDateTime=VERSION_DATETIME.get(taskProperties);
        float retentionPeriodValue = Float.parseFloat(RETENTION_PERIOD_VALUE.get(taskProperties));

		if (retentionPeriodType != null && !retentionPeriodType.isEmpty() && retentionPeriodValue >= 0) {
			Integer retention_in_seconds = getRetention(retentionPeriodType, retentionPeriodValue);
			if (versionDateTime != null && !versionDateTime.isEmpty()) {
				timeStamp = versionDateTime;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date timeStampDate = sdf.parse(timeStamp);
				long millis = timeStampDate.getTime();
				unixTime_plus_retention = (millis / 1000L + retention_in_seconds) * 1000;
			} else {
				timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime);
				unixTime_plus_retention = (unixTime / 1000L + retention_in_seconds) * 1000;
			}
			version_exp_date = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTime_plus_retention);
			//Set TTL
            if(-1 != retention_in_seconds) {
				ludb().execute("SET INSTANCE_TTL = " + retention_in_seconds);
			}
		}
		Boolean reserveInd = true;
		//log.info("executeFabricBatch - luName: " + luName + ", isChild: " + isChildLU(taskProperties));
        // check the selection method only for root LUs. Build only once the root selection method per task execution
        if(!isChildLU(taskProperties)){
            String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
			// TDM 7.4 - In case of reservation, the reservation will be done in one LU only
            //entityInclusionOverride = Util.isEmpty(entityInclusion) ? getEntityInclusion(taskProperties) : entityInclusion; //the task execution has several root LUs, and if the entity inclusion was already populated for the previous root LU it will be reused
			if (Util.isEmpty(entityInclusion)) {
				entityInclusionOverride = getEntityInclusion(taskProperties);
			} else { //the task execution has several root LUs, and if the entity inclusion was already populated for the previous root LU it will be reused
				entityInclusionOverride = entityInclusion;
				reserveInd = false;
				
			}
        }else{// the parent id is populated- handle the child luID
            entityInclusionOverride = getEntityInclusionForChildLU(taskProperties, luName);
			// TDM 7.4 - Reservation will be done only at Root LU
			reserveInd = false;
        }
	
        //log.info(" entity inclusion: " + entityInclusionOverride);
		// TDM 7.4 - For Custom Logic the source DB is Cassandra
        String entityInclusionInterface = "";
		if ((selectionMethod.equals("C") || selectionMethod.equalsIgnoreCase("SYNTHETIC") || selectionMethod.equalsIgnoreCase("GENERATE")) && !isChildLU(taskProperties)) {
			entityInclusionInterface = CASSANDRA;
		} else {
			entityInclusionInterface = TDMDB;
		}
	
		// TDM 7.3 - If the selection method is cloning data , then the sync of entities will be executed before calling the broadway to make sure each orignal instance is sync at most once
		if ("CLONE".equalsIgnoreCase(selectionMethod)) {
			//log.info("executeFabricBatch - Handling Cloning Entity");
			syncInstanceForCloning(entityInclusionOverride, taskProperties);
			syncMode = "off";
		}

        String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
        String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";
	
		//TDM 7.4 - For Custom Logic the source DB is Cassandra
		// In case of entity list, the batch command will be different as it gets a entity list and not an SQL statement
		//String batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
		String batchCommand = "";
		if("L".equalsIgnoreCase(selectionMethod) && !isChildLU(taskProperties)){
			batchCommand = "BATCH " + luName + ".(?) fabric_command=? with " + affinity + " async=true";
		} else {
			batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
		}

        String broadwayCommand = "broadway " + luName + ".TDMOrchestrator " + "iid=?, luName=" + luName +", syncMode=\"" + syncMode + "\"" + ", reserveInd=" + reserveInd;
           
        //log.info("Starting batch command: " + batchCommand);
        //log.info("Starting broadway command: " + broadwayCommand);
	
        return (String) fabric().fetch(batchCommand, entityInclusionOverride, broadwayCommand).firstValue();
    }

	private static String executeReserveBatch(Map<String, Object> taskProperties) throws Exception {
		//log.info("----------------- preparing for reserve execution -------------------");

		// TDM 7.4 - 19.01.22 - Set globals at task level instead of instance level

		setGlobalsForTask("reserve", taskProperties);

		String selectionMethod = SELECTION_METHOD.get(taskProperties);
		String luName = LU_NAME.get(taskProperties);
		String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
		String entityInclusionOverride = "";
		//log.info("executeFabricBatch - luName: " + luName + ", isChild: " + isChildLU(taskProperties));
		// check the selection method only for root LUs. Build only once the root selection method per task execution
		
		// TDM 7.4 - For Custom Logic the source DB is Cassandra
        String entityInclusionInterface = "";
		if (selectionMethod.equals("C")) {
			entityInclusionInterface = CASSANDRA;
		} else {
			entityInclusionInterface = TDMDB;
		}
		
		//TDM 7.4 - In case of Reserve only on Root LU will be processed, and all child LUs will be ignored
		if(!isChildLU(taskProperties)){
			String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
			
			if (Util.isEmpty(entityInclusion)) {
				entityInclusionOverride = getEntityInclusion(taskProperties);
			} else {
				// No need to run the LU
				return "NA";
			}
		}else{// the parent id is populated- handle the child luID
			// No need to run the LU
			return "NA";
		}
		
		//log.info(" entity inclusion: " + entityInclusionOverride);

		String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
		String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";

		// In case of entity list, the batch command will be different as it gets a entity list and not an SQL statement
		//String batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
		String batchCommand = "";
		if("L".equalsIgnoreCase(selectionMethod) && !isChildLU(taskProperties)){
			batchCommand = "BATCH " + luName + ".(?) fabric_command=? with " + affinity + " async=true";
		} else {
			batchCommand = "BATCH " + luName + " FROM " + entityInclusionInterface + " USING(?) fabric_command=? with " + affinity + " async=true";
		}

		String broadwayCommand = "broadway " + luName + ".TDMReserveOrchestrator " + "iid=?, luName=" + luName;// +

		//log.info("Starting batch command: " + batchCommand);
		//log.info("Starting broadway command: " + broadwayCommand);
		//log.info("entityInclusionOverride: " + entityInclusionOverride);

		return (String) fabric().fetch(batchCommand, entityInclusionOverride, broadwayCommand).firstValue();
	}
    private static String getEntityInclusionForChildLU(Map<String, Object> taskProperties, String luName) throws Exception {
		//log.info("getEntityInclusionForChildLU - handling Child LU: " + luName);
        String parentLU = "" + db(TDM).fetch("SELECT lu_parent_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE lu_id=?", (Object)LU_ID.get(taskProperties)).firstValue();
        String entityIdSelectChildID = "t.source_env" + getEntityIDSelect("rel.lu_type2_eid");
        //String entityIdSelectParID = "t.source_env" + getEntityIDSelect("rel.lu_type1_eid");
        String versionClause;
        String selectedVersionTaskName = SELECTED_VERSION_TASK_NAME.get(taskProperties);
        String selectedVersionDateTime = SELECTED_VERSION_DATETIME.get(taskProperties);
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String entityInclusion;
		String selectionMethod = ((String)SELECTION_METHOD.get(taskProperties)).toUpperCase();
		//log.info("getEntityInclusionForChildLU - selectedVersionTaskName: <" + selectedVersionTaskName + ">, selectedVersionDateTime: <" + selectedVersionDateTime + ">, separator: <" + separator + ">");
        if(!Util.isEmpty(selectedVersionTaskName) && !Util.isEmpty(selectedVersionDateTime)){
            entityIdSelectChildID += "||'" + separator + "'||'" + selectedVersionTaskName + "'||'" + separator + "'||'" + selectedVersionDateTime + "'";
            //entityIdSelectParID += "||'" + separator + "'||'" + selectedVersionTaskName + "'||'" + separator + "'||'" + selectedVersionDateTime + "'";
            versionClause = " and rel.version_name = '" + selectedVersionTaskName + "' and to_char(rel.version_datetime,'yyyyMMddhh24miss') = '" + selectedVersionDateTime + "'";
        }else{
            versionClause = " and rel.version_name ='''' ";
        }

        // TDM 8.0 - Adding the root entity ID to the params to be used in the masking/sequences actors
        entityIdSelectChildID = entityIdSelectChildID + "||'#params#{\"root_entity_id\" : '||t.root_entity_id";
		
		if ("CLONE".equals(selectionMethod)) {
			
			entityIdSelectChildID = entityIdSelectChildID + "||', \"clone_id\" : '||generate_series(1, " + NUM_OF_ENTITIES.get(taskProperties) + " )||'}'";
		} else {
            entityIdSelectChildID = entityIdSelectChildID + "||'}'";
        }
		
		if (isDeleteOnlyMode(taskProperties)){
			// TDM 7.2 use iid instead of target_entity_id
			entityInclusion = "select distinct " + entityIdSelectChildID + " as child_entity_id FROM " + TDMDB_SCHEMA + ".task_execution_entities t, " + TDMDB_SCHEMA + ".tdm_lu_type_rel_tar_eid rel " +
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and rel.target_env = '" + TARGET_ENVIRONMENT_NAME.get(taskProperties) + "' and t.lu_name = rel.lu_type_1 " +
						" and t.iid = rel.lu_type1_eid and rel.lu_type_2= '" + luName + "'"; 
		} else if (isDeleteAndLoad(taskProperties)) {
			// TDM 7.2 use iid instead of target_entity_id
			entityInclusion = "SELECT distinct " + entityIdSelectChildID + " child_entity_id FROM " + TDMDB_SCHEMA + ".task_execution_entities t, " + TDMDB_SCHEMA + ".tdm_lu_type_relation_eid rel " +
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and rel.source_env = t.source_env " +
						" and t.lu_name = rel.lu_type_1 and t.iid = rel.lu_type1_eid " +
						" and rel.lu_type_2= '" + luName + "'" +  versionClause +
					// In case of delete from target, there could be entries added to the target environment after the TDM load.
					" UNION SELECT distinct " + entityIdSelectChildID + " child_entity_id FROM " + TDMDB_SCHEMA + ".task_execution_entities t, " + TDMDB_SCHEMA + ".tdm_lu_type_rel_tar_eid rel " +
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and rel.target_env = '" + TARGET_ENVIRONMENT_NAME.get(taskProperties) + 
						"' and t.lu_name = rel.lu_type_1 and t.iid = rel.lu_type1_eid and rel.lu_type_2= '" + luName + "'"; 
		} else {// Case of insert to target only
			// TDM 7.2 use iid instead of target_entity_id
			entityInclusion = "SELECT distinct " + entityIdSelectChildID + " child_entity_id FROM " + TDMDB_SCHEMA + ".task_execution_entities t, " + TDMDB_SCHEMA + ".tdm_lu_type_relation_eid rel " + 
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and rel.source_env = t.source_env " +
						" and t.lu_name = rel.lu_type_1 and t.iid = rel.lu_type1_eid " +
						" and rel.lu_type_2= '" + luName + "'" +
						 versionClause;
		}
		
		//log.info("getEntityInclusionForChildLU - entityInclusion: " + entityInclusion);
        return entityInclusion;
    }

    private static boolean isDeleteOnlyMode(Map<String,Object> taskProperties) {
        String insetToTarget = LOAD_ENTITY.get(taskProperties);
        String deleteBeforeLoad = DELETE_BEFORE_LOAD.get(taskProperties);
        return !Util.isEmpty(deleteBeforeLoad) && deleteBeforeLoad.equals("true") &&  (Util.isEmpty(insetToTarget) || !Util.isEmpty(insetToTarget) && insetToTarget.equals("false"));
    }
	
	private static boolean isDeleteAndLoad(Map<String,Object> taskProperties) {
        String insetToTarget = LOAD_ENTITY.get(taskProperties);
        String deleteBeforeLoad = DELETE_BEFORE_LOAD.get(taskProperties);
        return !Util.isEmpty(deleteBeforeLoad) && deleteBeforeLoad.equals("true") &&  !Util.isEmpty(insetToTarget) && insetToTarget.equals("true");
    }

    private static String getEntityInclusion(Map<String, Object> taskProperties) throws Exception {
        String entitiesList = SELECTION_PARAM_VALUE.get(taskProperties);
        //log.info("creating entity inclusion for root lu with entitiesList: " + entitiesList);
        String listOfMatchingEntities;
        String env = isDeleteOnlyMode(taskProperties) ? TARGET_ENVIRONMENT_NAME.get(taskProperties) : SOURCE_ENVIRONMENT_NAME.get(taskProperties);
        String selectionMethod = ("" + SELECTION_METHOD.get(taskProperties)).toUpperCase();
		String replaceSequences = "" + REPLACE_SEQUENCES.get(taskProperties);
		String taskType = "" + TASK_TYPE.get(taskProperties);
		String entityExclusionListWhere = "";
		//TDM 7.6 - The entity list will be checked against reserved entities only if requested in the task
		String filterOutReserved = "" + FILTEROUT_RESERVED.get(taskProperties);
		// Reservation is not relevant in case of replace sequence.
		// And in case of entity list the reservation will be checked by the batch process to fail the entity like any other failure
		if ("true".equals(filterOutReserved) && !"L".equals(selectionMethod) && !"CLONE".equals(selectionMethod) && !"true".equals(replaceSequences) && !"extract".equalsIgnoreCase(taskType)) {
			entityExclusionListWhere = getReserveCondition(taskProperties);
		}
        String entityInclusion = "";
		//log.info("getEntityInclusion - separator: " + separator);
        switch(selectionMethod){
            case "L": // In case the task lists the entities to run
               String versionParams = VERSION_IND.get(taskProperties).equals("true") ? 
					separator + SELECTED_VERSION_TASK_NAME.get(taskProperties) + separator + SELECTED_VERSION_DATETIME.get(taskProperties) : "";
					//entitiesList = entitiesList.replaceAll("\\s+","");
                String[] entitiesListArray = !Util.isEmpty(entitiesList) ? entitiesList.split(",") : new String[]{};
				
				for (String entityID : entitiesListArray) {
					// TDM 7.6 - Add separtors to the entity ID if they are in use
					entityID = addSeparators(entityID.trim());
					entityInclusion +=  "'" + env + separator + entityID + versionParams + "',";
				}
	
				entityInclusion = entityInclusion.substring(0, entityInclusion.length() - 1);
				//log.info("getEntityInclusion: entityInclusion For L: " + entityInclusion);
                break;
            case "R": // In case the task requests a random list of entities
				if ("".equals(entityExclusionListWhere)) {
                	entityExclusionListWhere = " WHERE source_environment='" + SOURCE_ENVIRONMENT_NAME.get(taskProperties) + "' ";
				} else {
					entityExclusionListWhere += " AND source_environment='" + SOURCE_ENVIRONMENT_NAME.get(taskProperties) + "' ";
				}
                String luParamsTable = TDMDB_SCHEMA + "." + ((String)LU_NAME.get(taskProperties)).toLowerCase() + "_params";
                entityInclusion = "SELECT '" + env + "'" + getEntityIDSelect("entity_id") + " FROM " + luParamsTable + entityExclusionListWhere + " ORDER BY md5(entity_id || '" + CREATION_DATE.get(taskProperties) + "') LIMIT " + NUM_OF_ENTITIES.get(taskProperties);
                break;
            case "CLONE": // In case the task requests to Clone entities
				entityInclusion = "SELECT '" + env + separator + addSeparators(entitiesList) + "#params#{\"clone_id\" : '||generate_series(1, " + NUM_OF_ENTITIES.get(taskProperties) + " )||'}' as entity_id ";
                break;
            case "P": // In case the task has criteria based on parameters
                listOfMatchingEntities = generateListOfMatchingEntitiesQuery(BE_ID.get(taskProperties), entitiesList, SOURCE_ENVIRONMENT_NAME.get(taskProperties));
                entityInclusion = "SELECT entity_id FROM (SELECT '" + env + "'" + getEntityIDSelect("entity_id") + " as entity_id FROM (" + listOfMatchingEntities + ")  AS ALIAS0) AS ALIAS1 " + entityExclusionListWhere + " LIMIT " + NUM_OF_ENTITIES.get(taskProperties);
                break;
            case "PR": // In case the task has criteria based on parameters and random list
                listOfMatchingEntities = generateListOfMatchingEntitiesQuery(BE_ID.get(taskProperties), entitiesList, SOURCE_ENVIRONMENT_NAME.get(taskProperties));
                entityInclusion = "SELECT entity_id FROM ( SELECT '" + env + "'" + getEntityIDSelect("entity_id") + " as entity_id FROM (" + listOfMatchingEntities + ")  AS ALIAS0) AS ALIAS1 " + entityExclusionListWhere + "  ORDER BY md5(entity_id || '" + CREATION_DATE.get(taskProperties) + "') LIMIT " + NUM_OF_ENTITIES.get(taskProperties);
                break;
            case "ALL":
                if(VERSION_IND.get(taskProperties).equals("true") && taskType.equalsIgnoreCase("load")){
					// TDM 7.4 - Make the entities reserved by other tasks are not loaded
					entityExclusionListWhere.replace("WHERE", "AND");
					// The entity list should be taken from TDMDB and it should consider the status of the entities, only entities extracted successfully should be loaded
                    //Db.Rows rows = db(TDM).fetch("SELECT fabric_execution_id FROM TASK_EXECUTION_LIST WHERE task_execution_id=? and lu_id=?", SELECTED_VERSION_TASK_EXE_ID.get(taskProperties), LU_ID.get(taskProperties));
                    //entityInclusion = "SELECT entityid FROM k2batchprocess.batchprocess_entities_info WHERE bid = '" + rows.firstValue() + "' ALLOW FILTERING";
					
					entityInclusion = "SELECT entity_id FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_ENTITIES " + ("".equals(entityExclusionListWhere) ? "WHERE " : entityExclusionListWhere) + " AND " + "task_execution_id='" +  SELECTED_VERSION_TASK_EXE_ID.get(taskProperties) +
										"' and lu_name='" + LU_NAME.get(taskProperties) + "' and lower(execution_status) = 'completed' and id_type = 'ENTITY' ";
                }
                break;
			// TDM 7.4 - New selection method - Custom Logic
			case "C" :
				// TDM 7.5 - Set the environment before calling the Flow to make sure that the records are restored from the right DB.
				fabric().execute("set environment " + env);
				
				// TDM 7.6 - Get the Lu Name of the Custom Logic flow from the SELECTION_PARAM_VALUE
				//String luName = LU_NAME.get(taskProperties);
				String[] params = ("" + SELECTION_PARAM_VALUE.get(taskProperties)).split("#");
				String luName = params[0];
				String customLogicFlow = params[1];
	
				// TDM 7.5.1 - If the entity List table does not exists create it
				String createEntityListTab = "broadway " + luName + ".createLuExternalEntityListTable luName = " + luName + ", RESULT_STRUCTURE=ROW";
				fabric().execute(createEntityListTab);
				String broadwayCommand = geCLBroadwayCmd(luName, customLogicFlow, taskProperties);
				//Db.Row entityListTableRec = fabric().fetch(broadwayCommand).firstRow();
				//log.info("Custom Logic broadwayCommand: " + broadwayCommand);
				String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
				String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";
				String batchCommand = "BATCH " + luName + ".("+ luName + "_" + TASK_EXECUTION_ID.get(taskProperties) + ") fabric_command=? with " + affinity + " async=true";
				//log.info("Custom Logic batchCommand: " + batchCommand);
				String batchId = "" + fabric().fetch(batchCommand, broadwayCommand).firstValue();
				db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list set execution_status = 'STARTEXECUTIONREQUESTED', fabric_execution_id = ? " + 
					"WHERE task_execution_id=? and lu_id = ?", batchId, TASK_EXECUTION_ID.get(taskProperties), LU_ID.get(taskProperties));

				String waitForBatch = "broadway " + luName + ".WaitForCustomLogicFlow luName = " + luName + ", batchId = '" + batchId + "', RESULT_STRUCTURE=ROW";
				//log.info("Custom Logic waitForBatch: " + waitForBatch);
				Db.Row entityListTableRec = fabric().fetch(waitForBatch).firstRow();
				String entityListTable = "" + entityListTableRec.get("value");
				entityInclusion = "select tdm_eid from " + entityListTable + " where task_execution_id = '" +  TASK_EXECUTION_ID.get(taskProperties) + "'";
				break;
	
	        // TDM 8.0 - New selection method - Generate Synthetic
			case "GENERATE":
				// In case the task requests to generate synthetic entities
				luName = LU_NAME.get(taskProperties);
				broadwayCommand = "broadway " + luName + ".GenerateIIDs " + "iid=? , lu_name = " + luName + 
					", task_execution_id=" + TASK_EXECUTION_ID.get(taskProperties) + 
                    ", num_of_entities=" + Math.toIntExact(NUM_OF_ENTITIES.get(taskProperties));
				dcName = DATA_CENTER_NAME.get(taskProperties).toString();
				affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";
				batchCommand = "BATCH " + luName + ".("+ luName + "_" + TASK_EXECUTION_ID.get(taskProperties) + ") fabric_command=? with " + affinity + " async=true";
				//log.info("Custom Logic broadwayCommand: " + broadwayCommand);
				batchId = "" + fabric().fetch(batchCommand, broadwayCommand).firstValue();
				db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list set execution_status = 'STARTEXECUTIONREQUESTED', fabric_execution_id = ? " +
						"WHERE task_execution_id=? and lu_id = ?", batchId, TASK_EXECUTION_ID.get(taskProperties), LU_ID.get(taskProperties));

				waitForBatch = "broadway " + luName + ".WaitForGenerateFlow luName = " + luName + ", batchId = '" + batchId + "', RESULT_STRUCTURE=ROW";
				//log.info("Custom Logic waitForBatch: " + waitForBatch);
				entityListTableRec = fabric().fetch(waitForBatch).firstRow();
				entityListTable = "" + entityListTableRec.get("value");
                //log.info("entityListTable: " + entityListTable);
				entityInclusion = "select tdm_eid from " + entityListTable + " where task_execution_id = '" +  TASK_EXECUTION_ID.get(taskProperties) + "'";
				break;
            default: // This column is populated automatically by the application and should not include any other options
                break;
        }
        entityInclusions.put("" + TASK_EXECUTION_ID.get(taskProperties), entityInclusion);
		//log.info("getEntityInclusion - entityInclusion: " + entityInclusion);
        return entityInclusion;
    }

	private static String geCLBroadwayCmd(String luName, String clFlowName, Map<String, Object> taskProperties) {
		
		//String luName = LU_NAME.get(taskProperties);
		Long entitiesLimit = 0L;
		if(NUM_OF_ENTITIES.get(taskProperties) instanceof Long) {
			entitiesLimit = NUM_OF_ENTITIES.get(taskProperties);
		} else {
			entitiesLimit = Long.valueOf(NUM_OF_ENTITIES.get(taskProperties));
		}
		//String clFlowName = SELECTION_PARAM_VALUE.get(taskProperties);
		String clFlowParams = PARAMETERS.get(taskProperties);
		
		clFlowParams = clFlowParams.replaceAll("\\\\n","").replaceAll("\\\\t","");
		//log.info("clFlowParams after replace: " + clFlowParams);
		// Replace gson with K2view Json
		//Gson gson = new Gson();
		//Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>(){}.getType();
		//Map<String, List<Map <String, Object>>> clFlowParamJson = gson.fromJson(clFlowParams, mapType);
		Map<String, List<Map <String, Object>>> clFlowParamJson = Json.get().fromJson(clFlowParams);
		
		String fabricCommandParams = " LU_NAME='" + luName + "', NUM_OF_ENTITIES=" + entitiesLimit;
		if (!(clFlowParamJson.isEmpty())) {
			List<Map <String, Object>> clFlowParamList = clFlowParamJson.get("inputs");
			for (Map <String, Object> clFlowParamMap : clFlowParamList) {
				String paramValue = "" + clFlowParamMap.get("value");
				fabricCommandParams += ", " + clFlowParamMap.get("name") + "=\"" + paramValue + "\"";
				//fabricCommandParams += ", " + clFlowParamMap.get("name") + "=" + paramValue;
			}
		}

		return "broadway " + luName + "."  +  clFlowName +  " iid=?," + fabricCommandParams;

      }

    private static String getExclusion(Map<String, Object> taskProperties) throws SQLException {
        String entityExclusionLIst = ENTITY_EXCLUSION_LIST.get(taskProperties);
		String env = isDeleteOnlyMode(taskProperties) ? TARGET_ENVIRONMENT_NAME.get(taskProperties) : SOURCE_ENVIRONMENT_NAME.get(taskProperties);
        String entityExclusionListWhere = !Util.isEmpty(entityExclusionLIst) ? "cast (entity_id as text) NOT IN (" + Arrays.stream(entityExclusionLIst.split(","))
                .map(entityID -> buildEntityExclusion(env, SELECTION_METHOD.get(taskProperties), VERSION_IND.get(taskProperties), SELECTED_VERSION_TASK_NAME.get(taskProperties), SELECTED_VERSION_DATETIME.get(taskProperties), entityID))
                .map(eID -> "'" + eID + "'").collect(Collectors.joining(",")) + ")" : "1 = 1";

        String eIDsToExcludeBasedOnEnvAndBE = (String) db(TDM).fetch("select replace(string_agg(exclusion_list, ','), ' ', '') from " + TDMDB_SCHEMA + ".TDM_BE_ENV_EXCLUSION_LIST where be_id=? and environment_id=?;", BE_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties)).firstValue();
        if(!Util.isEmpty(eIDsToExcludeBasedOnEnvAndBE)){
            entityExclusionListWhere += " AND cast (entity_id as text) NOT IN (" + Arrays.stream(eIDsToExcludeBasedOnEnvAndBE.split(","))
                    .map(entityID -> buildEntityExclusion(env, SELECTION_METHOD.get(taskProperties), VERSION_IND.get(taskProperties), SELECTED_VERSION_TASK_NAME.get(taskProperties), SELECTED_VERSION_DATETIME.get(taskProperties), entityID))
                    .map(eID -> "'" + eID + "'").collect(Collectors.joining(",")) + ")";
        }
        return entityExclusionListWhere;
    }


    private static String getReserveCondition(Map<String, Object> taskProperties) throws SQLException {
	String env = isDeleteOnlyMode(taskProperties) ? TARGET_ENVIRONMENT_NAME.get(taskProperties) : SOURCE_ENVIRONMENT_NAME.get(taskProperties);
	String envID = "" + ENVIRONMENT_ID.get(taskProperties);
	String beID = "" + BE_ID.get(taskProperties);
	String userID = "" + TASK_EXECUTED_BY.get(taskProperties);
	String rersevedExclusionListWhere = Util.rte(() -> " WHERE cast (entity_id as text) NOT IN (SELECT " + buildEntityReserved(env, taskProperties) + " FROM " +
				TDMDB_SCHEMA + ".tdm_reserved_entities WHERE env_id = " + envID + " and be_id = " + beID + " and reserve_owner != '" + userID + 
				"' and (end_datetime is null or end_datetime > CURRENT_TIMESTAMP))");
	
	return rersevedExclusionListWhere;
		
	}
    private static Map<String,Object> getGlobals(String globalsQuery, Map<String, Object> taskProperties, Map<String, Object> args, Object... params) {
        String selectionMethod = SELECTION_METHOD.get(taskProperties);

        Map<String,Object> globals = new HashMap<>();
		
		// TDM 7.4 - Support Reserved Entities
		String userName = TASK_EXECUTED_BY.get(taskProperties);
		String userRoles = USER_ROLES.get(taskProperties);
		//log.info("ExecuteTask - userName: " + userName + ", userRoles: " + userRoles);
		String permissionGroup = getPermissionGroupByRoles(userRoles);
		globals.put("USER_NAME", userName);
		globals.put("USER_FABRIC_ROLES", String.join(",", "{" + userRoles + "}"));
		globals.put("USER_PERMISSION_GROUP",permissionGroup);
		globals.put("TDM_RESERVE_IND", RESERVE_IND.get(taskProperties));
		globals.put("RESERVE_RETENTION_PERIOD_TYPE", RESERVE_RETENTION_PERIOD_TYPE.get(taskProperties));
		globals.put("RESERVE_RETENTION_PERIOD_VALUE", RESERVE_RETENTION_PERIOD_VALUE.get(taskProperties));
		globals.put("RESERVE_NOTE", RESERVE_NOTE.get(taskProperties));
		globals.put("BE_ID", "" + BE_ID.get(taskProperties));
		globals.put("LU_ID", "" + LU_ID.get(taskProperties));
		
		globals.put("TDM_TARGET_PRODUCT_VERSION", TDM_TARGET_PRODUCT_VERSION.get(taskProperties));
        globals.put("TDM_SOURCE_PRODUCT_VERSION", TDM_SOURCE_PRODUCT_VERSION.get(taskProperties));        
        globals.put("TDM_SOURCE_ENVIRONMENT_NAME", SOURCE_ENVIRONMENT_NAME.get(taskProperties));
        globals.put("TDM_TAR_ENV_NAME", TARGET_ENVIRONMENT_NAME.get(taskProperties));
        globals.put("TDM_TASK_ID",  "" + TASK_ID.get(taskProperties));
        globals.put("TDM_TASK_EXE_ID", "" + TASK_EXECUTION_ID.get(taskProperties));
		globals.put("execution_id", "" + TASK_EXECUTION_ID.get(taskProperties));
		globals.put("clone_id", "NO_CLONE_ID");
        globals.put("TDM_REPLACE_SEQUENCES", selectionMethod.equals("CLONE") ? "true" : REPLACE_SEQUENCES.get(taskProperties));
		globals.put("enable_sequences", selectionMethod.equals("CLONE") ? "true" : REPLACE_SEQUENCES.get(taskProperties));
		globals.put("TASK_TYPE", TASK_TYPE.get(taskProperties).toString().toUpperCase());
        Object minDist = Util.rte(() -> db(TDM).fetch("SELECT param_value FROM " + TDMDB_SCHEMA + 
            ".tdm_general_parameters WHERE PARAM_NAME = 'TABLE_DEFAULT_DISTRIBUTION_MIN'").firstValue());

        if (minDist != null) {
            globals.put("TABLE_DEFAULT_DISTRIBUTION_MIN", minDist);
        } else {
            globals.put("TABLE_DEFAULT_DISTRIBUTION_MIN", "1");
        }
        Object maxDist = Util.rte(() -> db(TDM).fetch("SELECT param_value FROM " + TDMDB_SCHEMA + 
        ".tdm_general_parameters WHERE PARAM_NAME = 'TABLE_DEFAULT_DISTRIBUTION_MAX'").firstValue());

    if (minDist != null) {
        globals.put("TABLE_DEFAULT_DISTRIBUTION_MAX", maxDist);
    } else {
        globals.put("TABLE_DEFAULT_DISTRIBUTION_MAX", "3");
    }
        globals.putAll(args);

        Util.rte(() -> db(TDM).fetch(globalsQuery, params).forEach(res ->Util.rte(() -> globals.put(res.resultSet().getString("global_name"), res.resultSet().getString("global_value")))));
		// Replace gson with K2view Json
        //Gson gson = new Gson();
		
		//TDM 7.2 - Get task execution override globals and add them to the task's globals.
		Map <String, Object> taskOverrideAttrs = fnGetTaskExecOverrideAttrs(TASK_ID.get(taskProperties), TASK_EXECUTION_ID.get(taskProperties));
		String overrideGlobalsStr = "" + taskOverrideAttrs.get("TASK_GLOBALS");
		//log.info("TdmExecuteTask - overrideGlobalsStr : " + overrideGlobalsStr);
	
		if (!"".equals(overrideGlobalsStr) && !"null".equals(overrideGlobalsStr)) {
			//Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
			//Map <String, Object> overrideGlobals = gson.fromJson(overrideGlobalsStr, mapType);
			Map <String, Object> overrideGlobals = Json.get().fromJson(overrideGlobalsStr);
			globals.putAll(overrideGlobals);
		}
		
		if (selectionMethod.equals("CLONE")) {
			globals.keySet().removeIf(key -> key.contains("LOAD_MASKING_FLAG"));
			globals.put("LOAD_MASKING_FLAG", "true");
		}

        globals.put("TDM_CLONING_DATA", getCloningData(selectionMethod));
		//log.info("getGlobals - VERSION_IND: <" + VERSION_IND.get(taskProperties) + ">");
		
		if (VERSION_IND.get(taskProperties).equals("true")) {
	        globals.put("TDM_VERSION_NAME", SELECTED_VERSION_TASK_NAME.get(taskProperties));
    	    globals.put("TDM_VERSION_DATETIME", SELECTED_VERSION_DATETIME.get(taskProperties));
			
            globals.keySet().removeIf(key -> key.contains("MASKING_FLAG"));
    	    globals.put("LOAD_MASKING_FLAG", "false");
			globals.put("EXTRACT_MASKING_FLAG", "false");

			globals.put("TDM_REPLACE_SEQUENCES", "false");
			
			globals.put("enable_sequences", "false");			

			//TDM 7.3 - Add global to mark dataflux tasks
			globals.put("TDM_DATAFLUX_TASK", "true");
			if ("load".equalsIgnoreCase("" + TASK_TYPE.get(taskProperties))) {
				globals.put("TDM_DELETE_BEFORE_LOAD", "true");
			}

		} else {
			globals.put("TDM_VERSION_NAME", "");
    	    globals.put("TDM_VERSION_DATETIME", "19700101000000" );
			//TDM 7.3 - Add global to mark dataflux tasks
			globals.put("TDM_DATAFLUX_TASK", "false");
		}
			
		if (globals.containsKey("TDM_REPLACE_SEQUENCES")) {
			if ("false".equalsIgnoreCase("" + globals.get("TDM_REPLACE_SEQUENCES"))) {
				globals.put("enable_sequences", "false");
			} else {
				globals.put("enable_sequences", "true");
			}
		} else {
			globals.put("enable_sequences", "false");
		}

		if (getGlobal("SYNTHETIC_ENVIRONMENT").equalsIgnoreCase("" + globals.get("TDM_SOURCE_ENVIRONMENT_NAME"))) {
            globals.put("enable_sequences", "true");
            globals.put("TDM_REPLACE_SEQUENCES", "true");
        }
	
		if ("Synthetic".equalsIgnoreCase(SELECTION_METHOD.get(taskProperties)) || "Generate".equalsIgnoreCase(SELECTION_METHOD.get(taskProperties))) {
			globals.put("ROWS_GENERATOR", "true");
		}
        
		return globals;
    }
	
	private static void setGlobalsForTask(String taskType, Map<String, Object> taskProperties) {
		
		Map<String,Object> globals = new HashMap<>();
		Map<String, Object> additionalGlobals = new HashMap<>();
		switch (taskType) {
			case "load" :
			case "reserve":
				
				additionalGlobals.put("TDM_SYNC_SOURCE_DATA", getSrcSyncDataVal(taskProperties));
                additionalGlobals.put("TDM_DELETE_BEFORE_LOAD", DELETE_BEFORE_LOAD.get(taskProperties));
				additionalGlobals.put("SELECTION_METHOD", SELECTION_METHOD.get(taskProperties));
                additionalGlobals.put("TDM_INSERT_TO_TARGET", LOAD_ENTITY.get(taskProperties));
				additionalGlobals.put("TDM_DELETE_ONLY_TASK", isDeleteOnlyMode(taskProperties));
				additionalGlobals.put("TARGET_ENVIRONMENT_ID", "" + ENVIRONMENT_ID.get(taskProperties));
				additionalGlobals.put("CHILD_LU_IND", isChildLU(taskProperties));
				additionalGlobals.put("FILTEROUT_RESERVED", FILTEROUT_RESERVED.get(taskProperties));
			
				// TDM 7.4 - Get MAX_RESERVATION_DAYS_FOR_TESTER
				String maxReserveTester = "";
                String executed_by=TASK_EXECUTED_BY.get(taskProperties);
				String task_id = TASK_ID.get(taskProperties).toString();
				String created_by;
                try {
                    created_by = "" + db(TDM).fetch("SELECT task_created_by FROM " + TDMDB_SCHEMA + ".tasks WHERE task_id=?" ,task_id).firstValue();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
				boolean adminOrOwner = false;
				if("TDM.tdmTaskScheduler".equalsIgnoreCase(executed_by)){
					adminOrOwner = Util.rte(() -> fnIsAdminOrOwner("" + ENVIRONMENT_ID.get(taskProperties), created_by));
				}
				else{
					adminOrOwner = Util.rte(() -> fnIsAdminOrOwner("" + ENVIRONMENT_ID.get(taskProperties), executed_by));
				}
				if (!adminOrOwner) {
					Map<String, Object> retentionInfo = fnGetRetentionPeriod();
					maxReserveTester = String.valueOf(retentionInfo.get("maxRetentionPeriodForTester"));
				}
				//log.info("MAX_RESERVATION_DAYS_FOR_TESTER: " +  maxReserveTester);
				additionalGlobals.put("MAX_RESERVATION_DAYS_FOR_TESTER", maxReserveTester);
			
                //String syntheticData = getSyntheticData(selectionMethod);
				globals = getGlobals(LOAD.query(), taskProperties, additionalGlobals, LOAD.params(taskProperties));
				break;
			
			case "extract" :
            case "generate" :
				additionalGlobals.put("TDM_SYNC_SOURCE_DATA", getSrcSyncDataVal(taskProperties));
				additionalGlobals.put("TDM_DELETE_BEFORE_LOAD", "false");
				if (VERSION_IND.get(taskProperties).equals("true")) {
					taskProperties.put("selected_version_task_name", TASK_TITLE.get(taskProperties));
					taskProperties.put("selected_version_datetime", VERSION_DATETIME.get(taskProperties));
				} else {
					taskProperties.put("selected_version_task_name", "");
					taskProperties.put("selected_version_datetime", "19700101000000");
				}
				globals = getGlobals(EXTRACT.query(), taskProperties, additionalGlobals, EXTRACT.params(taskProperties));
				break;
			default:
				TASK_TYPES type = TASK_TYPES.valueOf(TASK_TYPE.get(taskProperties).toString().toUpperCase());
				globals = getGlobals(type.query(), taskProperties, new HashMap<>(), type.params(taskProperties));
				break;
		}
		
		// Replace gson with K2view Json
		//Gson gson = new Gson();
		//String globalsJson = gson.toJson(globals, new TypeToken<HashMap>(){}.getType());
		String globalsJson = Json.get().toJson(globals);
		//log.info("globalsJson: " + globalsJson);
		Util.rte(() -> setGlobals(globalsJson));
	}
	private static String getSrcSyncDataVal(Map<String, Object> taskProperties) {
		String syncSrcData = "true";
		//log.info("SYNC_MODE: " + SYNC_MODE.get(taskProperties) + ", TASK_TYPE: " + TASK_TYPE.get(taskProperties) + ", VERSION_IND: " + VERSION_IND.get(taskProperties) + ", LOAD_ENTITY: " + LOAD_ENTITY.get(taskProperties));
		if("off".equalsIgnoreCase(SYNC_MODE.get(taskProperties))) {
			syncSrcData = "false";
		} else {
			if ("load".equalsIgnoreCase(TASK_TYPE.get(taskProperties)) &&
				("true".equalsIgnoreCase(VERSION_IND.get(taskProperties)) || 
				 "false".equalsIgnoreCase(LOAD_ENTITY.get(taskProperties)) ) ) {
				syncSrcData = "false";
			}
		}
		//log.info("getSrcSyncDataVal - syncSrcData: " + syncSrcData);
		return syncSrcData;
	}
	
	private static void setSyncMode(Map<String, Object> taskProperties) {
		String syncMode = SYNC_MODE.get(taskProperties);
		if(Util.isEmpty(syncMode)){
			String sourceEnv = SOURCE_ENVIRONMENT_NAME.get(taskProperties);
			syncMode = "" + Util.rte(() -> db(TDM).fetch("SELECT sync_mode FROM " + TDMDB_SCHEMA + ".environments where environment_status = 'Active' and environment_name=?", sourceEnv).firstValue());
			//log.info("TdmExecuteTask - syncMode Environments: <" + syncMode + ">, for Env: " + sourceEnv);
			taskProperties.put("sync_mode", syncMode);
			//log.info("setSyncMode - SYNC_MODE: " + SYNC_MODE.get(taskProperties));
		}
	}
	private static String getSyncModeForLoad(Map<String, Object> taskProperties) {
		String syncMode = SYNC_MODE.get(taskProperties);
		// In case of Load and sync mode is set to OFF and the deleteBeforeLoad is set to TRUE or it is dataflux load task (therefore requires delete before load), 
		// then the sync mode should be set to ON, to allow population of delete tables.
	
		if ("off".equalsIgnoreCase(syncMode) && "load".equalsIgnoreCase(TASK_TYPE.get(taskProperties)) &&
			("true".equalsIgnoreCase(DELETE_BEFORE_LOAD.get(taskProperties)) ||  "true".equalsIgnoreCase(VERSION_IND.get(taskProperties)))) {
			syncMode = "ON";
		}
		
		//In case of sync mode is force and the task is dataflux load, the sync mode must be set to ON, 
		// to prevent resyncing the data of the LU but to allow populating the Delete tables.
		if ("force".equalsIgnoreCase(syncMode) && "load".equalsIgnoreCase(TASK_TYPE.get(taskProperties)) &&
			"true".equalsIgnoreCase(VERSION_IND.get(taskProperties)) ) {
			syncMode = "ON";
		}
		
		taskProperties.put("sync_mode", syncMode);
		return syncMode;
	}
	
	private static void syncInstanceForCloning(String entityInclusion, Map<String, Object> taskProperties) throws SQLException{
		String luName = LU_NAME.get(taskProperties);
		String taskExeId = "" + TASK_EXECUTION_ID.get(taskProperties);
		Set<String> entityList = new HashSet<>();
	
		String getSrcDCSQL  = "select p.data_center_name from " + TDMDB_SCHEMA + ".environment_products p, " + TDMDB_SCHEMA + ".task_execution_list l, " + TDMDB_SCHEMA + ".tasks_logical_units u " + 
							"where l.task_execution_id=? and l.task_id = u.task_id and l.lu_id = u.lu_id and u.lu_name = ? " + 
							"and l.source_environment_id= p.environment_id and l.product_id = p.product_id ";

		String srcDC = "" + db(TDMDB).fetch(getSrcDCSQL, taskExeId, luName).firstValue();

		//log.info("syncInstanceForCloning - entityInclusion: " + entityInclusion);
		//log.info("syncInstanceForCloning - sync:" + SYNC_MODE.get(taskProperties));

		fabric().execute("set sync " + SYNC_MODE.get(taskProperties));
		//27-01-22 - The env should be set to the source env of the task before syncing the entity from source
		fabric().execute("set environment " + SOURCE_ENVIRONMENT_NAME.get(taskProperties));

		//log.info("syncInstanceForCloning - srcDC: " + srcDC + ", luName: " + luName + ", taskExeId: " + taskExeId);

		String getCmd = "get " + luName + ".? WITH PARALLEL=false STOP_ON_ERROR=true";

		if (srcDC != null && !Util.isEmpty(srcDC) && !srcDC.equals("null")) {
			 getCmd = "get " + luName + ".? @'" + srcDC + "' WITH PARALLEL=false STOP_ON_ERROR=true";
		}
		//log.info("syncInstanceForCloning - getCmd: " + getCmd);
		
		// The query for batch may include '''' for the batch command to process the query, we need to replace it with ''
		String cleanedEntityInclusion = entityInclusion.replaceAll("''''", "''");
		
		//log.info("syncInstanceForCloning - cleanedEntityInclusion: " + cleanedEntityInclusion);
		
		Db.Rows rows = db(TDMDB).fetch(cleanedEntityInclusion);
		
		for (Db.Row row : rows) {
			//log.info("syncInstanceForCloning - entity_id: " + row.cell(0));
			String intsanceID = "" + row.cell(0);
			//Object[] splitCloneId = intsanceID.split("#params#");
			
			Db.Rows instanceSplitted = fabric().fetch("broadway " + luName + ".SplitIIDAndCloneNumber iid='" + intsanceID + "', RESULT_STRUCTURE=ROW");

			for (Db.Row instanceField : instanceSplitted) {
				if ("UID".equals(instanceField.get("column"))) {
					intsanceID = "" + instanceField.get("value");
				}
				
				if ("cloneNo".equals(instanceField.get("column"))) {
					fabric().execute("set clone_id " + instanceField.get("value"));
				}
                
                // TDM 8.0 - Since the iid is sent wihout the params, we need to set the root_iid before hand
                if ("rootEntityId".equals(instanceField.get("column"))) {
					fabric().execute("set root_iid " + instanceField.get("value"));
				}

			}
			
			if (!entityList.contains(intsanceID)) {
				//log.info("syncInstanceForCloning - intsanceID: " + intsanceID);
				entityList.add(intsanceID);
				fabric().execute(getCmd, intsanceID);
			}
		}
		
		taskProperties.put("sync_mode", "off");
		if (rows != null) {
			rows.close();
		}
	}
	
    private static String getGeneratedcData(String selectionMethod) {
        return selectionMethod.equals("GEN") ? "true" : "false";
    }
    private static String getCloningData(String selectionMethod) {
        return selectionMethod.equals("CLONE") ? "true" : "false";
    }
    private static String buildEntityExclusion(String environment, String selectionMethod, String versionInd, String selectedVersionTaskName, String selectedVersionDateTime, String entityID) {
		
        if (!selectionMethod.toUpperCase().equals("R")) {
            String ent = Util.rte(() -> addSeparators(entityID.trim()));
			 String exclusionList;
            if (versionInd.equals("true")) {
                exclusionList = environment + separator + ent + separator + selectedVersionTaskName.trim() + separator + selectedVersionDateTime.trim();
            } else {
                exclusionList = environment + separator + ent;
            }
            return exclusionList;
        } else {
            return entityID.trim();
        }
    }

    private static String buildEntityReserved(String environment, Map<String, Object> taskProperties) throws Exception {
		String selectionMethod = ("" + SELECTION_METHOD.get(taskProperties)).toUpperCase();
		String versionInd = "" + VERSION_IND.get(taskProperties);
		String selectedVersionTaskName =  "" + SELECTED_VERSION_TASK_NAME.get(taskProperties);
		String selectedVersionDateTime = "" + SELECTED_VERSION_DATETIME.get(taskProperties);
		String fieldName = "entity_id";
		
		
        if (!"R".equals(selectionMethod)) {
			Object[] separators = fnGetIIdSeparatorsFromTDM();
	        String open = (String) separators[0];
    	    String close = (String) separators[1];
			
			String reservedEntitySelect = "'" + environment + "'||'" + separator + "'||" + (!Util.isEmpty(open) ? "'" + open + "'||" + fieldName :  fieldName);
			reservedEntitySelect = !Util.isEmpty(close) ? reservedEntitySelect + "||'" + close + "'" : reservedEntitySelect;
            if (versionInd.equals("true")) {
                reservedEntitySelect += "||'" +  separator + selectedVersionTaskName.trim() + separator + selectedVersionDateTime.trim() + "'";
            }
			//log.info("reservedEntitySelect: " + reservedEntitySelect);
            return reservedEntitySelect;
        } else {
            return fieldName.trim();
        }
    }
	
    private static String getEntityIDSelect(String name) throws Exception {
        Object[] separators = fnGetIIdSeparatorsFromTDM();
        String open = (String) separators[0];
        String close = (String) separators[1];
		//log.info("getEntityIDSelect - separator: " + separator);
        String entityIdSelect = "||'" + separator + "'||" + (!Util.isEmpty(open) ? "'" + open + "'||" + name :  name);
        entityIdSelect = !Util.isEmpty(close) ? entityIdSelect + "||'" + close + "'" : entityIdSelect;
        return entityIdSelect;
    }

    private static String addSeparators(String entityID) throws Exception {
        Object[] separators = fnGetIIdSeparatorsFromTDM();
        String open = (String) separators[0];
        String close = (String) separators[1];
        entityID = !Util.isEmpty(open) ? open + entityID : entityID;
        return  !Util.isEmpty(close) ? entityID + close : entityID;
    }

    private static Map<String, Object> getTaskProperties(Db.Row row) {
        Map<String, Object> taskProperties = new HashMap<>();

        try {
            // get task props
            taskProperties.putAll(row);
            Long taskId = (Long) row.get("task_id");
            taskProperties.putAll(Objects.requireNonNull(getTaskProperties(taskId)));
            taskProperties.put("task_id", taskId);
            taskProperties.put("be_id", row.get("be_id"));
            taskProperties.put("environment_id", row.get("environment_id"));
            taskProperties.put("creation_date", row.get("creation_date"));
            // get LU properties
            Db.Row luProperties = getLuProperties((Long) taskProperties.get("lu_id"));
            taskProperties.put("lu_name", luProperties.get("lu_name"));
            taskProperties.put("parent_lu_name", getLuName((Long) taskProperties.get("parent_lu_id")));
        } catch (Exception e) {
            log.error("Can't get task properties for task_execution_id=" + row.get("task_execution_id"), e);
        }
	
		//TDM 7.2 - Get task execution override attributes and use them to override the task's attributes
		Map <String, Object> taskOverrideAttrs = fnGetTaskExecOverrideAttrs((Long) row.get("task_id"), (Long) row.get("task_execution_id"));
		String overrideValue = "";
		try {
			for (String attrName : taskOverrideAttrs.keySet()) {
				boolean entityListFlag = false;
				
				if (!"task_globals".equalsIgnoreCase(attrName)) {
					overrideValue = "" + taskOverrideAttrs.get(attrName);
					//log.info("getTaskProperties - attrName: " + attrName + ", overrideValue: " + overrideValue);
					attrName = attrName.toLowerCase();
					switch (attrName) {
						case "selection_method":
							taskProperties.put(attrName, overrideValue);
							break;
						case "entity_list" :
							taskProperties.put("selection_param_value", overrideValue);
							int numberOfEntities = overrideValue.split(",", -1).length;
							taskProperties.put("num_of_entities", numberOfEntities);
							entityListFlag = true;
							break;
						case "no_of_entities" : 
							if (!entityListFlag) {
								taskProperties.put("num_of_entities", overrideValue);
							}
							break;
						case "source_environment_name":
							taskProperties.put(attrName, overrideValue);
							String srcEnvId = "" + db(TDM).fetch("select environment_id from " + TDMDB_SCHEMA + ".environments where environment_name = ? and lower(environment_status) = 'active'", overrideValue).firstValue();
							taskProperties.put("source_environment_id", srcEnvId);
							
							break;
						case "target_environment_name":
							taskProperties.put(attrName, overrideValue);
							String tarEnvId = "" + db(TDM).fetch("select environment_id from " + TDMDB_SCHEMA + ".environments where environment_name = ? and lower(environment_status) = 'active'", overrideValue).firstValue();
							taskProperties.put("environment_id", tarEnvId);
							break;
						// TDM 7.4 - 16-Jan-22 - Add support for overriding DataFlux parameters
						case "selected_version_task_exe_id":
							taskProperties.put(attrName, overrideValue);
							break;
						case "selected_version_task_name":
							taskProperties.put(attrName, overrideValue);
							break;
						case "selected_version_datetime":
							taskProperties.put(attrName, overrideValue);
							break;
						case "dataflux_retention_params":
							Map rentionPeriodInfo = Json.get().fromJson((String) overrideValue, Map.class);
							taskProperties.put("retention_period_type", "" + rentionPeriodInfo.get("unit"));
							taskProperties.put("retention_period_value", "" + rentionPeriodInfo.get("value"));
							break;
						case "reserve_ind":
							taskProperties.put("reserve_ind", overrideValue);
							break;
						case "reserve_retention_params":
							Map reserveRentionPeriodInfo = Json.get().fromJson((String) overrideValue, Map.class);
							taskProperties.put("reserve_retention_period_type", "" + reserveRentionPeriodInfo.get("unit"));
							taskProperties.put("reserve_retention_period_value", "" + reserveRentionPeriodInfo.get("value"));
							break;
						// TDM 7.4 - End of Change
						default:
							taskProperties.put(attrName, overrideValue);
							break;
					}
				}
			}
		} catch (SQLException e) {
            log.error("Can't Get Environment ID of Environment Name: " + overrideValue, e);
        }

        return taskProperties;
    }

    private static Db.Row getTaskProperties(Long taskId) {
        try {
            String query = new String(getLuType().loadResource("TDM/fnTdmExecuteTask/query_get_tasks_properties.sql"));
            return db(TDM).fetch(query, taskId).firstRow();
        } catch (Exception e) {
            log.error("Can't get properties for task_id=" + taskId, e);
            return null;
        }
    }

    private static Map<String, String> migrateEntitiesForTdmExtract(Long taskExecutionId, Map<String, Object> taskProperties) {
        try {
			 // TDM 7.4 - 19.01.22 - Set globals at task level instead of instance level
			setGlobalsForTask("extract", taskProperties);
            String syncMode = SYNC_MODE.get(taskProperties);
            String sourceEnv = SOURCE_ENVIRONMENT_NAME.get(taskProperties);
            
			//log.info("migrateEntitiesForTdmExtract - luName: " + LU_NAME.get(taskProperties) + ", isChild: " + isChildLU(taskProperties));
			String entitiesList = "";
			if (!isChildLU(taskProperties)) {
				entitiesList = getEntityInclusion(taskProperties);
				//log.info("migrateEntitiesForTdmExtract - entitiesList: " + entitiesList);
			}
	
            float retentionPeriodValue = Float.parseFloat(RETENTION_PERIOD_VALUE.get(taskProperties));
            return fnMExtractEntities(
                    LU_NAME.get(taskProperties),
                    //LU_DC_NAME.get(taskProperties),
					DATA_CENTER_NAME.get(taskProperties),
                    sourceEnv,
                    TASK_TITLE.get(taskProperties),
                    VERSION_IND.get(taskProperties),
                    entitiesList,
                    RETENTION_PERIOD_TYPE.get(taskProperties),
                    retentionPeriodValue,
                    //globals,
                    String.valueOf(taskExecutionId),
                    PARENT_LU_NAME.get(taskProperties),
                    VERSION_DATETIME.get(taskProperties),
                    syncMode,
					SELECTION_METHOD.get(taskProperties),
					LU_ID.get(taskProperties));
        } catch (Exception e) {
            log.error("Can't run migration for task_execution_id=" + taskExecutionId, e);
            return null;
        }
    }

    private static String getLuName(Long luId) throws SQLException {
        return (String) getLuProperties(luId).get("lu_name");
    }

    private static Db.Row getLuProperties(Long luId) throws SQLException {
        return db(TDM).fetch("SELECT lu_name, lu_dc_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE lu_id = ?", luId).firstRow();
    }

    private static void updateLuExecutionStatus(Long taskExecutionId, Long luID, String status, String fabricExecutionId, String versionExpDate) {
        try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=?, fabric_execution_id=?," + 
					"start_execution_time = (case when start_execution_time is null then current_timestamp at time zone 'utc' else start_execution_time end)," +
					"version_expiration_date=TO_TIMESTAMP(?, 'YYYYMMDDHH24MISS')" +
					" WHERE task_execution_id=? AND lu_id=?"
                    , status, fabricExecutionId, versionExpDate, taskExecutionId, luID);
        } catch (SQLException e) {
            log.error("Can't update status in task_execution_list for task_execution_id=" + taskExecutionId, e);
        }
    }

    private static void updateLuRefExeFailedStatus(Long taskExecutionId, String luName, String status) {
		try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_ref_exe_stats s SET execution_status=?" +
					" WHERE task_execution_id=? and task_ref_table_id in (SELECT task_ref_table_id FROM " + TDMDB_SCHEMA + ".task_ref_tables t " +
					" WHERE s.task_id = t.task_id AND t.lu_name=?)"
                    , status, taskExecutionId, luName);
        } catch (SQLException e) {
            log.error("Can't update status in task_ref_exe_stats for task_execution_id=" + taskExecutionId, e);
        }
	}

    private static void updateTaskExecutionStatus(String status, Long taskExecutionID, Long luID, Object... params) {
        try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
                            "execution_status=?, " +
                            "fabric_execution_id=?, " +
                            "start_execution_time = (case when start_execution_time is null then current_timestamp at time zone 'utc' else start_execution_time end),\n" +
                            "num_of_processed_entities = ?," +
                            "num_of_copied_entities = ?," +
                            "num_of_failed_entities = ?," +
                            "end_execution_time = ? " +
                            "WHERE task_execution_id=? and lu_id = ?"
                    , status, params[0], params[1], params[2], params[3], params[4], taskExecutionID, luID);
        } catch (SQLException e) {
            log.error("Can't update status in task_execution_list table for task_execution_id=" + taskExecutionID + ", lu_id: " + luID, e);
        }
    }

	private static void updateTaskExecutionBatchID(Long taskExecutionID, Long luID, String batchID) {
        try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET " +
                            "fabric_execution_id=? " +
                            "WHERE task_execution_id=? and lu_id = ?"
                    , batchID, taskExecutionID, luID);
        } catch (SQLException e) {
            log.error("Can't update batch ID in task_execution_list table for task_execution_id=" + taskExecutionID + ", lu_id: " + luID, e);
        }
    }
    private static void updateTaskExecutionSummary(Long taskExecutionId, String status) {
        try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_summary SET execution_status=? WHERE task_execution_id = ?", status, taskExecutionId);
        } catch (SQLException e) {
            log.error("Can't update status in task summary table for task_execution_id=" + taskExecutionId, e);
        }
    }

	//TDM 7.4 - This function gets the fabric roles of the given user
	private static String getFabricRolesByUser(String userName) {
		List<String> roles=new ArrayList<>();
		Util.rte(() -> fabric().fetch("list users;").
		forEach(r -> {
			if(userName.equals(r.get("user"))) {
				roles.addAll(Arrays.asList(((String) r.get("roles")).split(",")));
			}
		}));
		
		return  String.join(",", roles);
	}
	
	//TDM 7.4 - This function gets the highest permission group of roles of the user
	private static String getPermissionGroupByRoles(String roles) {
		Map<String, Integer> PERMISSION_GROUPS = new HashMap() {{
			put("admin", 3);
			put("owner", 2);
			put("tester", 1);
		}};

		Integer[] weight = {0};
		String sql = "select permission_group from " + TDMDB_SCHEMA + ".permission_groups_mapping where fabric_role = ANY (string_to_array(?, ','))";
		Util.rte(() -> db(TDM).fetch(sql, roles).forEach(row -> {
			Integer nextWeight = PERMISSION_GROUPS.get(row.get("permission_group"));
			if (nextWeight != null && nextWeight > weight[0]) {
				weight[0] = nextWeight;
			}
		}));

		if (weight[0] == 0) {
			log.error("Can't find permission group for the user");
			return "";
		} else {
			String permissionGroup = null;
			for (Map.Entry<String, Integer> e : PERMISSION_GROUPS.entrySet()) {
				if (e.getValue().equals(weight[0])) {
					permissionGroup = e.getKey();
					break;
				}
			}

			return permissionGroup;
		}
	}


    public enum TASK_PROPERTIES {
        TASK_ID(null),
        TASK_EXECUTION_ID(""),
        TASK_TYPE(""),
        TASK_TITLE(""),
        DATA_CENTER_NAME(""),
        LU_NAME(""),
        LU_ID(""),
        //LU_DC_NAME(""),
        BE_ID(null),
        PARENT_LU_ID(null),
        PARENT_LU_NAME(""),
        PARENT_LU_STATUS(""),
        VERSION_DATETIME(""),
        SELECTION_PARAM_VALUE(""), //EntitiesLIst,
        ENTITY_EXCLUSION_LIST(""),
        ENVIRONMENT_ID(null),
        SOURCE_ENVIRONMENT_ID(null),
        SOURCE_ENVIRONMENT_NAME(""),
        TARGET_ENVIRONMENT_NAME(""),
        LOAD_ENTITY(""),
        REPLACE_SEQUENCES("false"),
        REFRESH_REFERENCE_DATA("false"),
        SYNC_MODE(""),
        CREATION_DATE(""),
        NUM_OF_ENTITIES(null),
        TDM_SOURCE_PRODUCT_VERSION(""),
        TDM_TARGET_PRODUCT_VERSION(""),
        SELECTION_METHOD(""),
        DELETE_BEFORE_LOAD("false"),
        SELECTED_VERSION_TASK_NAME(""),
        SELECTED_VERSION_DATETIME(""),
        SELECTED_VERSION_TASK_EXE_ID(null),
        RETENTION_PERIOD_TYPE(""),
        RETENTION_PERIOD_VALUE("0"),
		VERSION_IND("false"),
	    RESERVE_IND("false"),
		RESERVE_RETENTION_PERIOD_TYPE(""),
		RESERVE_RETENTION_PERIOD_VALUE("0"),
		TASK_EXECUTED_BY(""),
		USER_ROLES(""),
		PARAMETERS(""),
		RESERVE_NOTE(""),
		FILTEROUT_RESERVED("true");
        private Object def;

        TASK_PROPERTIES(Object def) {
            this.def = def;
        }

        public String getName() {
            return this.name().toLowerCase();
        }

//        @SuppressWarnings("unused")
        public <T> T get(Map<String, Object> args) {
            if (args == null) {
                return (T) this.def;
            } else {
                Object value = args.get(this.getName());
                return (T) (value == null ? this.def : value);
            }
        }
    }
}
