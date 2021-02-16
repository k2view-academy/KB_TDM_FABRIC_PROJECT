package com.k2view.cdbms.usercode.lu.TDM.TDM;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.usercode.common.TDM.SharedLogic;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.k2view.cdbms.shared.user.UserCode.*;
				 
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetIIdSeparatorsFromTDM;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.generateListOfMatchingEntitiesQuery;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.TASK_PROPERTIES.*;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.TASK_TYPES.*;

public class TdmExecuteTask {

    public static final String TDM = "TDM";
    public static final Log log = Log.a(TdmExecuteTask.class);
    public static final String TDMDB = "TDM";
    public static final String CASSANDRA = "DB_CASSANDRA";
    public static final String POST_EXECUTIONS = "Select t.process_id , t.process_name, t.execution_order from tasks_post_exe_process t, task_execution_list l " +
            "where l.task_execution_id = ? and l.process_id = t.process_id and upper(l.execution_status) = 'PENDING' and l.task_id = t.task_id " +
            "order by t.execution_order;";
    public static final String FABRIC = "fabric";
    public static final String POST_EXECUTIONS_COUNT = "select count(*) from tasks_post_exe_process tt, task_execution_list ll where ll.task_execution_id =? and ll.process_id = tt.process_id and tt.execution_order < ? and ll.execution_status NOT IN ('stopped','completed','failed','killed');";
    public static Map<String, String> entityInclusions = new HashMap<>();

    enum TASK_TYPES {
        EXTRACT(() -> Util.rte(() -> new String(getLuType().loadResource("TDM/fnTdmExecuteTask/query_get_extract_globals.sql"))),
                (taskProperties) -> new Object[]{SOURCE_ENVIRONMENT_ID.get(taskProperties), TASK_ID.get(taskProperties), TASK_ID.get(taskProperties), SOURCE_ENVIRONMENT_ID.get(taskProperties)}),
        LOAD(() -> Util.rte(() -> new String(getLuType().loadResource("TDM/fnTdmExecuteTask/query_get_load_globals.sql"))),
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

        Db tdmDB = db("TDM");

        tdmDB.fetch(query).forEach(row -> {

            // Get task properties
            Map<String, Object> taskProperties = getTaskProperties(row);

            Long taskExecutionID = (Long) taskProperties.get("task_execution_id");
			Long luID = (Long) LU_ID.get(taskProperties);
            String globals = "";
            // Check for child LU- if the parent LU execution failed- do not execute the child LU. Instead- update the execution_status of the child LU by the status of the parent LU and continue to the next LU
            String parentLUStatus = PARENT_LU_STATUS.get(taskProperties);
            if(isChildLU(taskProperties) && !parentLUStatus.toUpperCase().equals("COMPLETED")) {
                String startTime = "" + Util.rte(() -> db(TDM).fetch("select current_timestamp at time zone 'utc' ").firstValue());
                updateTaskExecutionStatus(parentLUStatus, taskExecutionID,luID, null, "0", "0", "0", startTime);
                return;
            }

            // Update task execution summary
            updateTaskExecutionSummary(taskExecutionID, "running");

			String selectionMethod = SELECTION_METHOD.get(taskProperties);
            String taskType = TASK_TYPE.get(taskProperties).toString().toLowerCase();
													  
			if((long)LU_ID.get(taskProperties)  > 0){
            	switch (taskType) {
                	case "extract":
                    	log.info("----------------- extract task -------------------");
						String versionExpDate = null;
						if (!selectionMethod.equalsIgnoreCase("ref")) {
	                    	globals = getGlobals(EXTRACT.query(), taskProperties, Util.map("TDM_INSERT_TO_TARGET", "true", "TDM_DELETE_BEFORE_LOAD", "false"), EXTRACT.params(taskProperties));

    	                	//log.info("globals:" + globals);
	        	            Map<String, String> migrationStatus = migrateEntitiesForTdmExtract(taskExecutionID, taskProperties, globals);
    	        	        String fabricExecutionId = migrationStatus != null ? migrationStatus.get("fabric_execution_id") : null;
						
        	            	if (!Util.isEmpty(fabricExecutionId)) {
								versionExpDate = migrationStatus.get("version_expiration_date");
                	        	updateLuExecutionStatus(taskExecutionID, luID, "running", fabricExecutionId, versionExpDate);
								try {
									log.info("TdmExecuteTask - Calling fnTdmCopyReference");
									Logic.fnTdmCopyReference(String.valueOf(taskExecutionID), taskType);
								} catch (Exception e) {
									log.info("TdmExecuteTask - Update extract task status to failed");
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
								log.info("TdmExecuteTask - Calling fnTdmCopyReference");
								Logic.fnTdmCopyReference(String.valueOf(taskExecutionID), taskType);
							} catch (Exception e) {
								log.info("TdmExecuteTask - Update extract task status to failed");
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
	    	                    globals = getGlobals(LOAD.query(), taskProperties, new HashMap<>(), LOAD.params(taskProperties));
    	    	                // run broadway flow
												 
        	        	        String executionId = executeFabricBatch(taskProperties, globals);
            	        	    if (!executionId.isEmpty()) {
                	        	    updateTaskExecutionStatus("running", taskExecutionID, luID, executionId, "0", "0", "0", null);
									log.info("TdmExecuteTask - Calling fnTdmCopyReference");
									Logic.fnTdmCopyReference(String.valueOf(taskExecutionID), taskType);
		                        } else {
    		                        // rollback LU and task status
        		                    updatedFailedStatus(taskExecutionID, luID);
            		                log.error("Execution failed for task execution: " + taskExecutionID + ", LU ID: " + luID);
                		        }
							} else {
								log.info("TdmExecuteTask - Calling fnTdmCopyReference");
								Logic.fnTdmCopyReference(String.valueOf(taskExecutionID), taskType);
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
		
                long luCount = (long) Util.rte(() -> db(TDM).fetch("SELECT COUNT(lu_id) FROM task_execution_list WHERE task_execution_id= ? AND lu_id > 0 AND execution_status NOT IN ('stopped','completed','failed','killed');", taskExecutionID).firstValue());
                if(luCount == 0) {
                    log.info("************* lu count = 0 starting post executions *************");
                    TASK_TYPES type = TASK_TYPES.valueOf(TASK_TYPE.get(taskProperties).toString().toUpperCase());
                    globals = getGlobals(type.query(), taskProperties, new HashMap<>(), type.params(taskProperties));
                    try {
                        String fabricCommandParams = " globals=" + globals + ", taskExecutionID=" + taskExecutionID; //todo: iid=?
                        db(TDM).fetch(POST_EXECUTIONS, taskExecutionID).forEach(res -> runPostExecution(taskExecutionID, fabricCommandParams, res));
                    } catch (SQLException e) {
                        log.error("Execution of post failed for task execution" + taskExecutionID);
                        e.printStackTrace();
                    }
                }

			}
        });
    }
	
    private static void runPostExecution(Long taskExecutionID, String fabricCommandParams, Db.Row res) {
        ResultSet resultSet = res.resultSet();
        Integer executionOrder = Util.rte(() -> resultSet.getInt("execution_order"));
        waitUntilPrevProcessDone(taskExecutionID, executionOrder);

        String processID = Util.rte(() -> resultSet.getString("process_id"));
        log.info("************* set task execution list to running for process id " + processID + " *************");

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
        Util.rte(()-> db(TDM).execute("UPDATE public.task_execution_list SET execution_status=?, fabric_execution_id=?, start_execution_time = (now() at time zone 'utc') WHERE task_execution_id=? and process_id=?", "running", executionId, taskExecutionID, processID));

    }

    private static void waitUntilPrevProcessDone(Long taskExecutionID, Integer executionOrder) {
        long count = -1;
        while(count != 0){
            count = (long) Util.rte(() ->db(TDM).fetch(POST_EXECUTIONS_COUNT, taskExecutionID, executionOrder).firstValue());
            log.info("**************** post exec count = " +  count + "********************");
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

    @NotNull
    private static String executeFabricBatch(Map<String, Object> taskProperties, String globals) throws Exception {
        log.info("----------------- preparing for batch execution -------------------");
		 
        String selectionMethod = SELECTION_METHOD.get(taskProperties);
        String luName = LU_NAME.get(taskProperties);
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String entityInclusionOverride = "";
		//log.info("executeFabricBatch - luName: " + luName + ", isChild: " + isChildLU(taskProperties));
        // check the selection method only for root LUs. Build only once the root selection method per task execution
        if(!isChildLU(taskProperties)){
            String entityInclusion = entityInclusions.getOrDefault(taskExecutionID, "");
				  
	
            entityInclusionOverride = Util.isEmpty(entityInclusion) ? getEntityInclusion(taskProperties) : entityInclusion; //the task execution has several root LUs, and if the entity inclusion was already populated for the previous root LU it will be reused
        }else{// the parent id is populated- handle the child luID
            entityInclusionOverride = getEntityInclusionForChildLU(taskProperties, luName);
        }
        //log.info(" entity inclusion: " + entityInclusionOverride);
		// No need to use Cassandra, the entity list will always be taken from TDMDB
        //String entityInclusionInterface = selectionMethod.equals("ALL") ? CASSANDRA : TDMDB;
 		String syncMode = SYNC_MODE.get(taskProperties);
		//log.info("TdmExecuteTask - syncMode from Tasks: <" + syncMode +">");
         String sourceEnv = SOURCE_ENVIRONMENT_NAME.get(taskProperties);
            if(Util.isEmpty(syncMode)){
                syncMode = "" + Util.rte(() -> db(TDM).fetch("SELECT sync_mode FROM environments where environment_status = 'Active' and fabric_environment_name=?", sourceEnv).firstValue());
				//log.info("TdmExecuteTask - syncMode Environments: <" + syncMode + ">, for Env: " + sourceEnv);
            }

        String dcName = DATA_CENTER_NAME.get(taskProperties).toString();
        String affinity = !Util.isEmpty(dcName) ? "affinity='" + DATA_CENTER_NAME.get(taskProperties) + "'" : "";
		String batchCommand = "BATCH " + luName + " FROM " + TDMDB + " USING(?) fabric_command=? with " + affinity + " async=true";

        String broadwayCommand = "broadway " + luName + ".TDMOrchestrator " + "iid=?, luName=" + luName +
                ", sourceEnv=" + SOURCE_ENVIRONMENT_NAME.get(taskProperties) +
                ", targetEnv=" + TARGET_ENVIRONMENT_NAME.get(taskProperties) +
                ", syncMode=\"" + syncMode + "\"" +
                ", deleteBeforeLoad=" + DELETE_BEFORE_LOAD.get(taskProperties) +
                ", selectionMethod=\"" + selectionMethod + "\"" +
                ", globals=" + globals +
                ", insertToTarget=" + LOAD_ENTITY.get(taskProperties) +
                ", versionInd=" + VERSION_IND.get(taskProperties) +
                ", replaceSequences=" + REPLACE_SEQUENCES.get(taskProperties) +
                ", syntheticData=" + getSyntheticData(selectionMethod);

        //log.info("Starting batch command: " + batchCommand);
        //log.info("Starting broadway command: " + broadwayCommand);

        return (String) fabric().fetch(batchCommand, entityInclusionOverride, broadwayCommand).firstValue();
    }

    private static String getEntityInclusionForChildLU(Map<String, Object> taskProperties, String luName) throws Exception {
		log.info("getEntityInclusionForChildLU - handling Child LU: " + luName);
        String parentLU = "" + db(TDM).fetch("SELECT lu_parent_name FROM public.product_logical_units WHERE lu_id=?", (Object)LU_ID.get(taskProperties)).firstValue();
        String entityIdSelectChildID = "t.source_env" + getEntityIDSelect("rel.lu_type2_eid");
        String entityIdSelectParID = "t.source_env" + getEntityIDSelect("rel.lu_type1_eid");
        String versionClause;
        String selectedVersionTaskName = SELECTED_VERSION_TASK_NAME.get(taskProperties);
        String selectedVersionDateTime = SELECTED_VERSION_DATETIME.get(taskProperties);
        String taskExecutionID = "" + TASK_EXECUTION_ID.get(taskProperties);
        String entityInclusion;
		String selectionMethod = ((String)SELECTION_METHOD.get(taskProperties)).toUpperCase();
		log.info("getEntityInclusionForChildLU - selectedVersionTaskName: <" + selectedVersionTaskName + ">, selectedVersionDateTime: <" + selectedVersionDateTime + ">");
        if(!Util.isEmpty(selectedVersionTaskName) && !Util.isEmpty(selectedVersionDateTime)){
            entityIdSelectChildID += "||'_'||'" + selectedVersionTaskName + "'||'_'||'" + selectedVersionDateTime + "'";
            entityIdSelectParID += "||'_'||'" + selectedVersionTaskName + "'||'_'||'" + selectedVersionDateTime + "'";
            versionClause = " and rel.version_name = '" + selectedVersionTaskName + "' and to_char(rel.version_datetime,'yyyyMMddhh24miss') = '" + selectedVersionDateTime + "'";
        }else{
            versionClause = " and rel.version_name ='''' ";
        }
		
		if ("S".equals(selectionMethod)) {
			
			entityIdSelectChildID = "DISTINCT "+ entityIdSelectChildID + "||'#params#{\"clone_id\"='||generate_series(1, " + NUMBER_OF_ENTITIES_TO_COPY.get(taskProperties) + " )||'}'";
		}
		
		if (isDeleteOnlyMode(taskProperties)){
			entityInclusion = "select " + entityIdSelectChildID + " as child_entity_id from task_execution_entities t, tdm_lu_type_rel_tar_eid rel " +
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and rel.target_env = '" + TARGET_ENVIRONMENT_NAME.get(taskProperties) + "' and t.lu_name = rel.lu_type_1 " +
						" and t.target_entity_id = rel.lu_type1_eid and rel.lu_type_2= '" + luName + "'"; 
		} else if (isDeleteAndLoad(taskProperties)) {
			entityInclusion = "SELECT " + entityIdSelectChildID + " child_entity_id FROM task_execution_entities t, tdm_lu_type_relation_eid rel " +
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and t.lu_name = rel.lu_type_1 and rel.lu_type_2= '" + luName + 
						"' and t.entity_id =  " + entityIdSelectParID + versionClause +
					// In case of delete from target, there could be entries added to the target environment after the TDM load.
					" UNION SELECT " + entityIdSelectChildID + " child_entity_id FROM task_execution_entities t, tdm_lu_type_rel_tar_eid rel " +
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and rel.target_env = '" + TARGET_ENVIRONMENT_NAME.get(taskProperties) + "' and t.lu_name = rel.lu_type_1 " +
						" and t.target_entity_id = rel.lu_type1_eid and rel.lu_type_2= '" + luName + "'"; 
		} else {// Case of insert to target only
			entityInclusion = "SELECT " + entityIdSelectChildID + " child_entity_id FROM task_execution_entities t, tdm_lu_type_relation_eid rel " + 
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and t.lu_name = rel.lu_type_1 and rel.lu_type_2= '" + luName + 
						"' and t.entity_id =  " + entityIdSelectParID + versionClause;
		}
		
/*        if(!isDeleteOnlyMode(taskProperties)){
            entityInclusion = "SELECT " + entityIdSelectChildID + " child_entity_id FROM task_execution_entities t, tdm_lu_type_relation_eid rel where t.task_execution_id= '" + taskExecutionID + 
						"' and t.execution_status = 'completed' and t.lu_name = '" + parentLU + "' and t.lu_name = rel.lu_type_1 and rel.lu_type_2= '" + luName + 
						"' and t.entity_id =  " + entityIdSelectParID + versionClause;
        }else{
            entityInclusion = "select " + entityIdSelectChildID + " as child_entity_id from task_execution_entities t, tdm_lu_type_rel_tar_eid rel " +
						" where t.task_execution_id= '" + taskExecutionID + "' and t.execution_status = 'completed' " + 
						" and t.lu_name = '" + parentLU + "' and rel.target_env = '" + TARGET_ENVIRONMENT_NAME.get(taskProperties) + "' and t.lu_name = rel.lu_type_1 " +
						" and t.target_entity_id = rel.lu_type1_eid and rel.lu_type_2= '" + luName + "'"; 
        } */
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
        String entityExclusionListWhere = getExclusion(taskProperties);
        String listOfMatchingEntities;
        String env = isDeleteOnlyMode(taskProperties) ? TARGET_ENVIRONMENT_NAME.get(taskProperties) : SOURCE_ENVIRONMENT_NAME.get(taskProperties);
        String selectionMethod = ((String)SELECTION_METHOD.get(taskProperties)).toUpperCase();
        String entityInclusion = "";
        switch(selectionMethod){
            case "L": // In case the task lists the entities to run
                String[] entitiesListArray = !Util.isEmpty(entitiesList) ? entitiesList.split(",") : new String[]{};
                String iidSeparator = (String) db(TDM).fetch("Select param_value from tdm_general_parameters where param_name = 'iid_separator'").firstValue();
                String separator = !Util.isEmpty(iidSeparator) ? iidSeparator : "_"; //todo: check if needed
                entityInclusion = "SELECT entity_id from (" + Arrays.stream(entitiesListArray)
                        .map(String::trim)
                        .map(entityID -> Util.rte(() -> addSeparators(entityID)))
                        .map(entityID -> {
                            String versionParams = VERSION_IND.get(taskProperties).equals("true") ? separator + SELECTED_VERSION_TASK_NAME.get(taskProperties) + separator + SELECTED_VERSION_DATETIME.get(taskProperties) : "";
                            return "SELECT '" + env + separator + entityID + versionParams + "' AS entity_id";
                        }).collect(Collectors.joining(" union ")) + ") AS ALIAS1 WHERE " + entityExclusionListWhere + " LIMIT " + NUMBER_OF_ENTITIES_TO_COPY.get(taskProperties);

                break;
            case "R": // In case the task requests a random list of entities
                entityExclusionListWhere += " AND source_environment='" + SOURCE_ENVIRONMENT_NAME.get(taskProperties) + "' ";
                String luParamsTable = ((String)LU_NAME.get(taskProperties)).toLowerCase() + "_params";
                entityInclusion = "SELECT '" + env + "'" + getEntityIDSelect("entity_id") + " FROM " + luParamsTable + " WHERE " + entityExclusionListWhere + " ORDER BY md5(entity_id || '" + CREATION_DATE.get(taskProperties) + "') LIMIT " + NUMBER_OF_ENTITIES_TO_COPY.get(taskProperties);
                break;
            case "S": // In case the task requests to synthetically create entities
                    //entityInclusion = "SELECT '" + env + getEntityIDSelect("'" + entitiesList + "'") + " as entity_id from (select generate_series(1, " + NUMBER_OF_ENTITIES_TO_COPY.get(taskProperties) + " ) as serie) t";
                    entityInclusion = "SELECT '" + env + "_" + entitiesList + "#params#{\"clone_id\"='||generate_series(1, " + NUMBER_OF_ENTITIES_TO_COPY.get(taskProperties) + " )||'}' as entity_id ";
                //todo - check if should be: env + getEntityIDSelect("'" + entitiesList + "'")
                break;
            case "P": // In case the task has criteria based on parameters
                listOfMatchingEntities = generateListOfMatchingEntitiesQuery(BE_ID.get(taskProperties), entitiesList, SOURCE_ENVIRONMENT_NAME.get(taskProperties));
                entityInclusion = "SELECT entity_id FROM (SELECT '" + env + "'" + getEntityIDSelect("entity_id") + " as entity_id FROM (" + listOfMatchingEntities + ")  AS ALIAS0) AS ALIAS1 WHERE " + entityExclusionListWhere + " LIMIT " + NUMBER_OF_ENTITIES_TO_COPY.get(taskProperties);
                break;
            case "PR": // In case the task has criteria based on parameters and random list
                listOfMatchingEntities = generateListOfMatchingEntitiesQuery(BE_ID.get(taskProperties), entitiesList, SOURCE_ENVIRONMENT_NAME.get(taskProperties));
                entityInclusion = "SELECT entity_id FROM ( SELECT '" + env + "'" + getEntityIDSelect("entity_id") + " as entity_id FROM (" + listOfMatchingEntities + ")  AS ALIAS0) AS ALIAS1 WHERE " + entityExclusionListWhere + "  ORDER BY md5(entity_id || '" + CREATION_DATE.get(taskProperties) + "') LIMIT " + NUMBER_OF_ENTITIES_TO_COPY.get(taskProperties);
                break;
            case "ALL":
                if(VERSION_IND.get(taskProperties).equals("true")){
					// The entity list should be taken from TDMDB and it should consider the status of the entities, only entities extract successfully should be loaded
                    //Db.Rows rows = db(TDM).fetch("SELECT fabric_execution_id FROM TASK_EXECUTION_LIST WHERE task_execution_id=? and lu_id=?", SELECTED_VERSION_TASK_EXE_ID.get(taskProperties), LU_ID.get(taskProperties));
                    //entityInclusion = "SELECT entityid FROM k2batchprocess.batchprocess_entities_info WHERE bid = '" + rows.firstValue() + "' ALLOW FILTERING";
					
					entityInclusion = "SELECT entity_id FROM TASK_EXECUTION_ENTITIES WHERE task_execution_id='" +  SELECTED_VERSION_TASK_EXE_ID.get(taskProperties) +
										"' and lu_name='" + LU_NAME.get(taskProperties) + "' and lower(execution_status) = 'completed'";
                }
                break;
            default: // This column is populated automatically by the application and should not include any other options
                break;
        }
        entityInclusions.put(TASK_EXECUTION_ID.get(taskProperties), entityInclusion);
		//log.info("getEntityInclusion - entityInclusion: " + entityInclusion);
        return entityInclusion;
    }

    @NotNull
    private static String getExclusion(Map<String, Object> taskProperties) throws SQLException {
        String entityExclusionLIst = ENTITY_EXCLUSION_LIST.get(taskProperties);
        String entityExclusionListWhere = !Util.isEmpty(entityExclusionLIst) ? "cast (entity_id as text) NOT IN (" + Arrays.stream(entityExclusionLIst.split(","))
                .map(entityID -> buildEntityExclusion(SELECTION_METHOD.get(taskProperties), VERSION_IND.get(taskProperties), SELECTED_VERSION_TASK_NAME.get(taskProperties), SELECTED_VERSION_DATETIME.get(taskProperties), entityID))
                .map(eID -> "'" + eID + "'").collect(Collectors.joining(",")) + ")" : "1 = 1";

        String eIDsToExcludeBasedOnEnvAndBE = (String) db(TDM).fetch("select replace(string_agg(exclusion_list, ','), ' ', '') from TDM_BE_ENV_EXCLUSION_LIST where be_id=? and environment_id=?;", BE_ID.get(taskProperties), ENVIRONMENT_ID.get(taskProperties)).firstValue();
        if(!Util.isEmpty(eIDsToExcludeBasedOnEnvAndBE)){
            entityExclusionListWhere += " AND cast (entity_id as text) NOT IN (" + Arrays.stream(eIDsToExcludeBasedOnEnvAndBE.split(","))
                    .map(entityID -> buildEntityExclusion(SELECTION_METHOD.get(taskProperties), VERSION_IND.get(taskProperties), SELECTED_VERSION_TASK_NAME.get(taskProperties), SELECTED_VERSION_DATETIME.get(taskProperties), entityID))
                    .map(eID -> "'" + eID + "'").collect(Collectors.joining(",")) + ")";
        }
        return entityExclusionListWhere;
    }

    private static String getGlobals(String globalsQuery, Map<String, Object> taskProperties, Map<String, Object> args, Object... params) {
        String selectionMethod = SELECTION_METHOD.get(taskProperties);

        Map<String,Object> globals = new HashMap<>();

		globals.put("TDM_TARGET_PRODUCT_VERSION", TDM_TARGET_PRODUCT_VERSION.get(taskProperties));
        globals.put("TDM_SOURCE_PRODUCT_VERSION", TDM_SOURCE_PRODUCT_VERSION.get(taskProperties));        
        globals.put("TDM_SOURCE_ENVIRONMENT_NAME", SOURCE_ENVIRONMENT_NAME.get(taskProperties));
        globals.put("TDM_TAR_ENV_NAME", TARGET_ENVIRONMENT_NAME.get(taskProperties));
        globals.put("TDM_TASK_ID", TASK_ID.get(taskProperties));
        globals.put("TDM_TASK_EXE_ID", "" + TASK_EXECUTION_ID.get(taskProperties));
        globals.put("TDM_REPLACE_SEQUENCES", selectionMethod.equals("S") ? "true" : REPLACE_SEQUENCES.get(taskProperties));
        globals.put("MASK_FLAG", selectionMethod.equals("S") ? "1" : "");
        globals.put("TDM_SYNTHETIC_DATA", getSyntheticData(selectionMethod));
		log.info("getGlobals - VERSION_IND: <" + VERSION_IND.get(taskProperties) + ">");
		if (VERSION_IND.get(taskProperties).equals("true")) {
	        globals.put("TDM_VERSION_NAME", SELECTED_VERSION_TASK_NAME.get(taskProperties));
    	    globals.put("TDM_VERSION_DATETIME", SELECTED_VERSION_DATETIME.get(taskProperties));
    	    globals.put("MASK_FLAG", "0");
    	    globals.put("TDM_REPLACE_SEQUENCES", "false");		
		} else {
			globals.put("TDM_VERSION_NAME", "");
    	    globals.put("TDM_VERSION_DATETIME", "19700101000000" );
		}
        globals.put("NUMBER_OF_ENTITIES_PER_STAT_REPORT", "1"); //todo If the number of entities to migrate is small than the STAT_REPORT_THRESHOLD value

        globals.putAll(args);

        Util.rte(() -> db(TDM).fetch(globalsQuery, params).forEach(res ->Util.rte(() -> globals.put(res.resultSet().getString("global_name"), res.resultSet().getString("global_value")))));
        Gson gson = new Gson();
        return gson.toJson(globals, new TypeToken<HashMap>(){}.getType());
    }

    @NotNull
    private static String getSyntheticData(String selectionMethod) {
        return selectionMethod.equals("S") ? "true" : "false";
    }

    @NotNull
    private static String buildEntityExclusion(String selectionMethod, String versionInd, String selectedVersionTaskName, String selectedVersionDateTime, String entityID) {
        if (!selectionMethod.toUpperCase().equals("R")) {
            String ent = Util.rte(() -> addSeparators(entityID.trim()));
            return ent + (versionInd.equals("true") ? "_" + selectedVersionTaskName.trim() + "_" + selectedVersionDateTime.trim() : entityID.trim());
        } else {
            return entityID.trim();
        }
    }

    @NotNull
    private static String getEntityIDSelect(String name) throws Exception {
        Object[] separators = fnGetIIdSeparatorsFromTDM();
        String open = (String) separators[0];
        String close = (String) separators[1];
        String entityIdSelect = "||'_'||" + (!Util.isEmpty(open) ? "'" + open + "'||" + name :  name);
        entityIdSelect += !Util.isEmpty(close) ? entityIdSelect + "||'" + close + "'" : "";
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
            taskProperties.put("lu_dc_name", luProperties.get("lu_dc_name"));
            taskProperties.put("parent_lu_name", getLuName((Long) taskProperties.get("parent_lu_id")));
        } catch (Exception e) {
            log.error("Can't get task properties for task_execution_id=" + row.get("task_execution_id"), e);
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

    private static Map<String, String> migrateEntitiesForTdmExtract(Long taskExecutionId, Map<String, Object> taskProperties, String globals) {
        try {
            String syncMode = SYNC_MODE.get(taskProperties);
            String sourceEnv = SOURCE_ENVIRONMENT_NAME.get(taskProperties);
            if(Util.isEmpty(syncMode)){
                syncMode = "" + Util.rte(() -> db(TDM).fetch("SELECT sync_mode FROM environments where environment_status = 'Active' and fabric_environment_name=?", sourceEnv).firstValue());
            }
			//log.info("migrateEntitiesForTdmExtract - luName: " + LU_NAME.get(taskProperties) + ", isChild: " + isChildLU(taskProperties));
			String entitiesList = SELECTION_PARAM_VALUE.get(taskProperties);
			if (isChildLU(taskProperties)) {
				entitiesList = "";
			}
            float retentionPeriodValue = Float.parseFloat(RETENTION_PERIOD_VALUE.get(taskProperties));
            return SharedLogic.fnMigrateEntitiesForTdmExtract(
                    LU_NAME.get(taskProperties),
                    LU_DC_NAME.get(taskProperties),
                    sourceEnv,
                    TASK_TITLE.get(taskProperties),
                    VERSION_IND.get(taskProperties),
                    entitiesList,
                    RETENTION_PERIOD_TYPE.get(taskProperties),
                    retentionPeriodValue,
                    globals,
                    String.valueOf(taskExecutionId),
                    PARENT_LU_NAME.get(taskProperties),
                    VERSION_DATETIME.get(taskProperties),
                    syncMode);
        } catch (Exception e) {
            log.error("Can't run migration for task_execution_id=" + taskExecutionId, e);
            return null;
        }
    }

    private static String getLuName(Long luId) throws SQLException {
        return (String) getLuProperties(luId).get("lu_name");
    }

    private static Db.Row getLuProperties(Long luId) throws SQLException {
        return db(TDM).fetch("SELECT lu_name, lu_dc_name FROM product_logical_units WHERE lu_id = ?", luId).firstRow();
    }

    private static void updateLuExecutionStatus(Long taskExecutionId, Long luID, String status, String fabricExecutionId, String versionExpDate) {
        try {
            db(TDM).execute("UPDATE public.task_execution_list SET execution_status=?, fabric_execution_id=?,  version_expiration_date=TO_TIMESTAMP(?, 'YYYYMMDDHH24MISS')" +
					" WHERE task_execution_id=? AND lu_id=?"
                    , status, fabricExecutionId, versionExpDate, taskExecutionId, luID);
        } catch (SQLException e) {
            log.error("Can't update status in task_execution_list for task_execution_id=" + taskExecutionId, e);
        }
    }

    private static void updateLuRefExeFailedStatus(Long taskExecutionId, String luName, String status) {
		try {
            db(TDM).execute("UPDATE public.task_ref_exe_stats s SET execution_status=?" +
					" WHERE task_execution_id=? and task_ref_table_id in (SELECT task_ref_table_id FROM task_ref_tables t " +
					" WHERE s.task_id = t.task_id AND t.lu_name=?)"
                    , status, taskExecutionId, luName);
        } catch (SQLException e) {
            log.error("Can't update status in task_ref_exe_stats for task_execution_id=" + taskExecutionId, e);
        }
	}
    private static void updateTaskExecutionStatus(String status, Long taskExecutionID, Long luID, Object... params) {
        try {
            db(TDM).execute("UPDATE public.task_execution_list SET " +
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

    private static void updateTaskExecutionSummary(Long taskExecutionId, String status) {
        try {
            db(TDM).execute("UPDATE public.task_execution_summary SET execution_status=? WHERE task_execution_id = ?", status, taskExecutionId);
        } catch (SQLException e) {
            log.error("Can't update status in task summary table for task_execution_id=" + taskExecutionId, e);
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
        LU_DC_NAME(""),
        BE_ID(""),
        PARENT_LU_ID(null),
        PARENT_LU_NAME(""),
        PARENT_LU_STATUS(""),
        VERSION_DATETIME(""),
        SELECTION_PARAM_VALUE(""), //EntitiesLIst,
        ENTITY_EXCLUSION_LIST(""),
        ENVIRONMENT_ID(""),
        SOURCE_ENVIRONMENT_ID(""),
        SOURCE_ENVIRONMENT_NAME(""),
        TARGET_ENVIRONMENT_NAME(""),
        LOAD_ENTITY(""),
        REPLACE_SEQUENCES("false"),
        REFRESH_REFERENCE_DATA("false"),
        SYNC_MODE(""),
        CREATION_DATE(""),
        NUMBER_OF_ENTITIES_TO_COPY(""),
        TDM_SOURCE_PRODUCT_VERSION(""),
        TDM_TARGET_PRODUCT_VERSION(""),
        SELECTION_METHOD(""),
        DELETE_BEFORE_LOAD("false"),
        SELECTED_VERSION_TASK_NAME(""),
        SELECTED_VERSION_DATETIME(""),
        SELECTED_VERSION_TASK_EXE_ID(""),
        RETENTION_PERIOD_TYPE(""),
        RETENTION_PERIOD_VALUE("0"),
		VERSION_IND("false");
        private Object def;

        TASK_PROPERTIES(Object def) {
            this.def = def;
        }

        public String getName() {
            return this.name().toLowerCase();
        }

        @SuppressWarnings("unchecked")
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