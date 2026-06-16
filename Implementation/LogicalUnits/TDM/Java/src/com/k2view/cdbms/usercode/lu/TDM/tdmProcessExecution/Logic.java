/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.tdmProcessExecution;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.UserJob;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnCreateUpdateLUParams;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnUpdateAIProcess;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.isParamsCoupling;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.findMaxNumOfWorkers;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetTaskExecOverrideAttrs;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getAffinityString;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.updatedAIFailedStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.type;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.OverrideParamKey;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends UserCode {
    public static final String TDM = "TDM";

    @type(UserJob)
	public static void tdmProcessExecution(Long taskExecutionID, String processType, String sessionGlobals, String numOfEntities, String subsetID, String luID, String taskTitle, String sourceMaxWorkers, String targetMaxWorkers) throws Exception {
        //log.info("tdmProcessExecution Starting");
        String executionId = "";

        String executionsSql = "Select t.process_id , t.process_name, t.execution_order, t.process_type, t.parameters, l.be_id, l.task_id from " +
            TDMDB_SCHEMA + ".tasks_exe_process t, " + TDMDB_SCHEMA + ".task_execution_list l " +
            "where l.task_execution_id = ? and l.process_id = t.process_id and upper(l.execution_status) = 'PENDING' " +
            "and l.task_id = t.task_id and t.process_type = ? and t.status='Active' " +
            "order by t.execution_order";

        // log.info("tdmProcessExecution - sessionGlobals: " + sessionGlobals);

        Map<?, ?> globalsInput = Json.get().fromJson(sessionGlobals, Map.class);

        if (globalsInput != null && !(globalsInput.isEmpty())) {
            globalsInput.forEach((key, value) -> {
                String query = "set " + key + "='" + value + "'";
                try {
                    fabric().execute(query);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }

        String fabricCommandParams = " taskExecutionID=" + taskExecutionID;
        Map<Long, List<Map<String, Object>>> processParamOverrides = new HashMap<>();
        Long resolvedTaskId = null;
        Db.Rows rows = db(TDM).fetch(executionsSql, taskExecutionID, processType);
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            String processName = Util.rte(() -> resultSet.getString("process_name"));
            Integer executionOrder = Util.rte(() -> resultSet.getInt("execution_order"));
            String processID = Util.rte(() -> resultSet.getString("process_id"));
            String flowParams = Util.rte(() -> resultSet.getString("parameters"));
            Integer beId = Util.rte(() -> resultSet.getInt("be_id"));
            String luName = null;

            if (resolvedTaskId == null) {
                resolvedTaskId = Util.rte(() -> resultSet.getLong("task_id"));
                String overrideKey = "pre".equalsIgnoreCase(processType)
                        ? OverrideParamKey.PRE_EXECUTION_PROCESSES_PARAMS.name()
                        : OverrideParamKey.POST_EXECUTION_PROCESSES_PARAMS.name();
                Map<String, Object> overrideAttrs = fnGetTaskExecOverrideAttrs(resolvedTaskId, taskExecutionID);
                if (overrideAttrs != null) {
                    List<Map<String, Object>> procList =
                            (List<Map<String, Object>>) overrideAttrs.get(overrideKey);
                    if (procList != null) {
                        for (Map<String, Object> entry : procList) {
                            Object pid = entry.get("process_id");
                            if (pid != null)
                                processParamOverrides.put(((Number) pid).longValue(),
                                        (List<Map<String, Object>>) entry.get("parameter_overrides"));
                        }
                    }
                }
            }

            Map<String, Object> flowParamJson=null;

            if (flowParams != null && !"".equals(flowParams)) {
                flowParams = flowParams.replaceAll("\\\\n","").replaceAll("\\\\t","");
		        //log.info("flowParams after replace: " + flowParams);
		        if( flowParams!=null  && !("null".equalsIgnoreCase(flowParams))){
                    flowParamJson = Json.get().fromJson(flowParams, Map.class);
                }
            }

            List<Map<String, Object>> overrideDelta =
                    processParamOverrides.get(Long.parseLong(processID));
            if (overrideDelta != null && flowParamJson != null) {
                Map<String, Object> overrideByName = new HashMap<>();
                for (Map<String, Object> ov : overrideDelta) {
                    Object n = ov.get("name");
                    if (n != null) overrideByName.put(n.toString(), ov.get("value"));
                }
                List<Map<String, Object>> inputs =
                        (List<Map<String, Object>>) flowParamJson.get("inputs");
                if (inputs != null) {
                    for (Map<String, Object> input : inputs) {
                        Object n = input.get("name");
                        if (n != null && overrideByName.containsKey(n.toString()))
                            input.put("value", overrideByName.get(n.toString()));
                    }
                }
            }

            if (flowParamJson != null && !(flowParamJson.isEmpty())) {
                List<Map <String, Object>> flowParamList = (List<Map <String, Object>>)flowParamJson.get("inputs");
                for (Map <String, Object> flowParamMap : flowParamList) {
                    Object paramValue = flowParamMap.get("value");
                    if("".equals(paramValue)){
                        paramValue=null;
                    }
                    
                    fabricCommandParams += ", " + flowParamMap.get("name") + "=\"" + paramValue + "\"";
                }
            }

            Map<String, Object> ProcessInputs = new HashMap<>();
            ProcessInputs.put("Process_name", processName);
            ProcessInputs.put("Process_type", processType);
            
            try {
                //log.info ("tdmProcessExecution - runExecution - process Name: " + processName + ", process Type: " + processType);
                waitUntilPrevProcessDone(taskExecutionID, executionOrder, processType);
                if ("Training Data Subset".equalsIgnoreCase(processName) || "Exporting Data Subset".equalsIgnoreCase(processName)) {
                    String sql = "SELECT COUNT(*) FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
                    Long count = Long.valueOf(db(TDM).fetch(sql,taskExecutionID, 0, "failed","stopped").firstValue().toString());
                    // only if extarct worked then run training AI 
                    if (count == 0) {
                        Map<String, String> executionInfo = executeTrainingJob(String.valueOf(taskExecutionID),
                                processName, processID, taskTitle, targetMaxWorkers, sourceMaxWorkers);
                        AIprocessExecution(executionInfo, taskExecutionID, processID, luID);
                    } else {
                        sql = "SELECT execution_status FROM " + TDMDB_SCHEMA
                                + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
                        String status = db(TDM).fetch(sql, taskExecutionID, 0, "failed", "stopped").firstValue()
                                .toString();
                        Util.rte(() -> db(TDM).execute("UPDATE " + TDMDB_SCHEMA
                                + ".task_execution_list SET execution_status=?,num_of_processed_entities = ?, " +
                                "num_of_copied_entities = ?, num_of_failed_entities = ? ,fabric_execution_id=?, " +
                                "start_execution_time = COALESCE(start_execution_time, (now() at time zone 'utc')) " +
                                "WHERE task_execution_id=? and process_id=?", status, null, null, null, null,
                                taskExecutionID, processID));
                    }
                } else if ("Generating Data Subset".equalsIgnoreCase(processName) || "Importing Data Subset".equalsIgnoreCase(processName)) {
                    Map<String, String> executionInfo = executeGenerationJob(String.valueOf(taskExecutionID), processName, processID,numOfEntities,subsetID,taskTitle, sourceMaxWorkers);
                    AIprocessExecution(executionInfo, taskExecutionID, processID,luID);
                } else if ("Evaluating Data Subset".contentEquals(processName)){
                    Map<String, String> executionInfo = executeEvaluationJob(String.valueOf(taskExecutionID), processName, processID, numOfEntities, taskTitle, targetMaxWorkers);
                    AIprocessExecution(executionInfo, taskExecutionID, processID,luID);
                }else {
                    log.info("************* set task execution list to running for process id " + processID + " *************");
                    if (beId != -1) {
                        List<Map<String, Object>> ProcessList = MtableLookup("PostAndPreExecutionProcess", ProcessInputs, MTable.Feature.caseInsensitive);
                        for (Map<String, Object> t : ProcessList) {
                            Object luNameObj = t.get("Lu_name");
                            if (luNameObj != null) {
                                luName = luNameObj.toString();
                            }
                        }

                        if ((luName == null || luName.isEmpty()) && ProcessList.size() > 0) {
                            luName = "TDM";
                        }
                    } else {
                        luName = "TDM_TableLevel";
                    }
                    
                    String maxNumOfWorkers = findMinWorkers(sourceMaxWorkers, targetMaxWorkers);
                    String broadwayCommand = "broadway " + luName + "." + processName + " iid=?," + fabricCommandParams;
                    String batch = "BATCH " + luName + ".('" + taskExecutionID + "_" + processID + "')" + " fabric_command=? with async=true" + " BATCH_ID_PREFIX ='" + taskTitle +"'" + maxNumOfWorkers;
                    //log.info("broadwayCommand - " + broadwayCommand);
                    //log.info("Starting batch command for post execution: " + batch);
                    executionId =  (String) fabric().fetch(batch, broadwayCommand).firstValue();
                    String finalExecutionId = executionId;
                    Util.rte(() -> db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=?,num_of_processed_entities = ?, " +
                            "num_of_copied_entities = ?, num_of_failed_entities = ? ,fabric_execution_id=?, " +
                            "start_execution_time = COALESCE(start_execution_time, (now() at time zone 'utc')) " +
                            "WHERE task_execution_id=? and process_id=?", "running", null, null, null, finalExecutionId, taskExecutionID, processID));
                }
            } catch (Exception e) {
                String finalExecutionId1 = executionId;
                Util.rte(() -> db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=?,num_of_processed_entities = ?, " +
                        "num_of_copied_entities = ?, num_of_failed_entities = ? ,fabric_execution_id=?, " +
                        "start_execution_time = COALESCE(start_execution_time, (now() at time zone 'utc')) " +
                        "WHERE task_execution_id=? and process_id=?", "failed", null, null, null, finalExecutionId1, taskExecutionID, processID));
                log.error("Process " + processName + "failed due to " + e.getMessage());
                fabric().execute("stopjob USER_JOB name='TDM.tdmProcessExecution'");
                throw new RuntimeException(e);
            }
        };
    }
	
    public static String findMinWorkers(String sourceMaxWorkers, String targetMaxWorkers) {
        
        String source = findMaxNumOfWorkers(sourceMaxWorkers);
        String target = findMaxNumOfWorkers(targetMaxWorkers);
        
        int count1 = extractWorkerCount(source);
        int count2 = extractWorkerCount(target);

        if (count1 > 0 && count2 > 0) {
            // Both are positive, return the smaller one's formatted string
            return (count1 <= count2) ? source : target;
            
        } else if (count1 > 0) {
            // Only count1 is positive (count2 is 0 or invalid)
            return source;
            
        } else if (count2 > 0) {
            // Only count2 is positive (count1 is 0 or invalid)
            return target;
            
        } else {
            // Both are 0 or invalid - return an empty string to signify no explicit limit.
            return "";
        }
    }

    private static int extractWorkerCount(String paramString) {
        // Example paramString: " MAX_WORKERS_PER_NODE=10"
        
        // Return 0 if the string is empty or invalid. 
        // This is a safe default, assuming 0 workers means no explicit limit/default.
        if (paramString == null || paramString.trim().isEmpty()) {
            return 0;
        }

        // Regex to find the number after the '=' sign.
        Pattern pattern = Pattern.compile("=(\\d+)");
        Matcher matcher = pattern.matcher(paramString);

        if (matcher.find()) {
            try {
                // Group 1 is the number captured by (\\d+)
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                // Should not happen if regex is correct, but safe to handle.
                log.error("Error parsing extracted worker count: " + matcher.group(1));
            }
        }
        
        // If the pattern is not found, or parsing failed, return 0.
        return 0;
    }

    private static void waitUntilPrevProcessDone(Long taskExecutionID, Integer executionOrder, String processType) throws Exception {
        long count = -1;
        //log.info("waitUntilPrevProcessDone Starting with - taskExecutionID: " + taskExecutionID +", executionOrder: " + executionOrder);
        String EXECUTIONS_COUNT =  "select count(*) from " + TDMDB_SCHEMA + ".tasks_exe_process tt inner join " + TDMDB_SCHEMA + ".task_execution_list ll on tt.task_id=ll.task_id " +
            "where ll.task_execution_id =? and ll.process_id = tt.process_id and (? = -100 or tt.execution_order < ?) and tt.process_type = ? and tt.status='Active' " +
            "and ll.execution_status NOT IN ('stopped','completed','failed','killed');";
        //log.info("waitUntilPrevProcessDone - EXECUTIONS_COUNT: " + EXECUTIONS_COUNT);
        while (count != 0) {
            count = (long)  db(TDM).fetch(EXECUTIONS_COUNT, taskExecutionID, executionOrder, executionOrder, processType).firstValue();
            //log.info("**************** exec count = " +  count + "********************");
            Util.sleep(1000);
        }
    }
	
    private static void AIprocessExecution(Map<String, String> executionInfo, Long taskExecutionID, String processID, String luID) throws SQLException {
        try{      
            String total = executionInfo.get("total");
            String executionId = executionInfo.get("fabric_execution_id");
            if (Util.isEmpty(executionId) || "null".equalsIgnoreCase(executionId) || executionId == null) {
                String status = executionInfo.get("status");
                updatedAIFailedStatus(status,taskExecutionID, Long.parseLong(luID));
                log.error("AI Execution failed for task execution: " + taskExecutionID + ", LU ID: " +luID);
                return;
            } else {
                String query = "UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET fabric_execution_id=? ,num_of_copied_ref_tables=0 , num_of_failed_ref_tables=0 ,num_of_processed_ref_tables=0 ";
                query += (total != null && !"null".equalsIgnoreCase(total)) ? ",num_of_processed_entities= ? " : "";
                query += "WHERE task_execution_id=? and process_id = ?";

                if (total != null && !"null".equalsIgnoreCase(total)) {
                    db(TDM).execute(query, executionId, total, taskExecutionID, processID);    
                }else {
                    db(TDM).execute(query, executionId, taskExecutionID, processID); 
                }
   
            }
        }catch(SQLException e){
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> executeTrainingJob(String taskExecutionID,String processName, String processID, String taskTitle, String targetMaxWorkers, String sourceMaxWorkers) throws Exception {
        Map<String, String> executionStatus = new LinkedHashMap<>();
        // Check if the status of the task with the specified process_id is not failed
        String sql = "SELECT COUNT(*) FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
        Long count = Long.valueOf(db(TDM).fetch(sql,taskExecutionID, -2, "failed","stopped").firstValue().toString());
        if (count == 0) {
            // If the status is not failed, execute the update query
            sql = "UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=? WHERE task_execution_id=? AND process_id=?";
            db(TDM).execute(sql, "running", taskExecutionID, processID);
        }else{
            sql = "SELECT execution_status FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
            String status = db(TDM).fetch(sql,taskExecutionID, -2, "failed","stopped").firstValue().toString();
            executionStatus.put("total", "0");
            executionStatus.put("fabric_execution_id", null);
            executionStatus.put("status", status);
            return executionStatus;
        }
        String batchID = "";
        String query = "Select l.source_affinity,l.target_affinity,u.lu_name,l.lu_id FROM " + TDMDB_SCHEMA + ".task_execution_list l Join " + TDMDB_SCHEMA + ".tasks_logical_units u on l.lu_id=u.lu_id and l.task_id=u.task_id And l.process_id=0 " +
                "WHERE task_execution_id= ?";
        Db.Rows taskExecutionList = db(TDM).fetch(query, taskExecutionID);
        String sourceAffinity = "";
        String targetAffinity = "";
        String luID = "";
        String luName = "";
        for (Db.Row row : taskExecutionList) {
            luName = "" + row.get("lu_name");
            luID = "" + row.get("lu_id");
            sourceAffinity = "" + row.get("source_affinity");
            targetAffinity = "" + row.get("target_affinity");
        }

        if ("Exporting Data Subset".equalsIgnoreCase(processName)) {
            executionStatus = executeExportingSubset(taskExecutionID, luName, sourceAffinity, luID, taskTitle, sourceMaxWorkers);
        } else {
            executionStatus = executeTrainingSubset(taskExecutionID, luName, targetAffinity, luID, taskTitle, targetMaxWorkers);
        }
        return executionStatus;
    }

    private static Map<String, String> executeExportingSubset(String taskExecutionID, String luName, String sourceAffinity,
            String luID, String taskTitle, String sourceMaxWorkers) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        String maxNumOfWorkers = findMaxNumOfWorkers(sourceMaxWorkers);
        String broadwayCommand = "broadway TDM.ExportDataSubset " + "luName = '" + luName + "'" +
                ", dcName='" + sourceAffinity + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", LuID='" + luID + "'" + " , taskTitle = '" + taskTitle + "'"
                + " , maxNumOfWorkers = '" + maxNumOfWorkers + "'";
        // log.info("TRAINING >>>> "+broadwayCommand);
        Db.Rows rows = fabric().fetch(broadwayCommand);
        String batchID = null;
        for (Db.Row row : rows) {
            batchID = "" + row.get("batchID");
        }
        Map<String, String> entities = fnUpdateAIProcess(taskExecutionID, "0");
        ExecutionInfo.put("total", "" + entities.get("total"));
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;

    }

    private static Map<String, String> executeTrainingSubset(String taskExecutionID, String luName, String targetAffinity,
            String luID, String taskTitle, String targetMaxWorkers) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>(); 
        String affinity = getAffinityString(targetAffinity);       
        String maxNumOfWorkers = findMaxNumOfWorkers(targetMaxWorkers);        
        String batchCommand = "BATCH " + luName + ".(" + luName + "_" + taskExecutionID
                + ") FABRIC_COMMAND=? WITH " + affinity + " ASYNC='true'" + " BATCH_ID_PREFIX ='" + taskTitle + "'" + maxNumOfWorkers;
        String broadwayCommand = "broadway TDM.TrainingDataSubset " + "luName = '" + luName + "'" +
                ", dcName='" + targetAffinity + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", LuID='" + luID + "', iid=?";
        // log.info("TRAINING >>>> "+broadwayCommand);
        String batchID = (String) fabric().fetch(batchCommand, broadwayCommand).firstValue();
        Map<String, String> entities = fnUpdateAIProcess(taskExecutionID, "-2");
        ExecutionInfo.put("total", "" + entities.get("total"));
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;

    }

    private static Map<String, String> executeGenerationJob(String taskExecutionID,String processName, String processID,String numOfEntities, String subsetID, String taskTitle, String sourceMaxWorkers) throws Exception {
        Map<String, String> executionStatus = new LinkedHashMap<>();
        // Check if the status of the task with the specified process_id is not failed
        String sql = "SELECT COUNT(*) FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
        Long count = Long.valueOf(db(TDM).fetch(sql,taskExecutionID, -2, "failed","stopped").firstValue().toString());
        if (count == 0) {
            // If the status is not failed, execute the update query
            sql = "UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=? WHERE task_execution_id=? AND process_id=?";
            db(TDM).execute(sql, "running", taskExecutionID, processID);
        }else{
            sql = "SELECT execution_status FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
            String status = db(TDM).fetch(sql,taskExecutionID, -2, "failed","stopped").firstValue().toString();
            executionStatus.put("total", "0");
            executionStatus.put("fabric_execution_id", null);
            executionStatus.put("status", status);
            return executionStatus;
        }
        String batchID = "";
        String query = "Select u.lu_name,l.lu_id,l.source_affinity FROM " + TDMDB_SCHEMA + ".task_execution_list l Join " + TDMDB_SCHEMA + ".tasks_logical_units u on l.lu_id=u.lu_id and l.task_id=u.task_id And l.process_id=0 " +
                "WHERE task_execution_id= ?";
        Db.Rows taskExecutionList = db(TDM).fetch(query, taskExecutionID);        
        String luID = "";
        String luName = "";
        String sourceAffinity = "";
        String trainingExecutionID = "";
        for (Db.Row row : taskExecutionList) {
            luName = "" + row.get("lu_name");
            luID = "" + row.get("lu_id");
            sourceAffinity =  "" + row.get("source_affinity");
        }
        if (!"Importing Data Subset".equalsIgnoreCase(processName)) {
            executionStatus = executeGenerationSubset(taskExecutionID, luName, luID, numOfEntities, subsetID,
                    taskTitle, sourceMaxWorkers, sourceAffinity);
        } else {
            executionStatus = executeImportingSubset(taskExecutionID, luName, sourceAffinity, luID, taskTitle, sourceMaxWorkers);
        }
    return executionStatus;
    }

    private static Map<String, String> executeImportingSubset(String taskExecutionID, String luName, String sourceAffinity,
            String luID, String taskTitle, String sourceMaxWorkers) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        String sql = "SELECT be_id FROM " + TDMDB_SCHEMA
                + ".task_execution_list WHERE task_execution_id= ? AND lu_id= ? ";
        String beID = db(TDM).fetch(sql, taskExecutionID, luID).firstValue().toString();
        Boolean paramCoupling = isParamsCoupling();
        String maxNumOfWorkers = findMaxNumOfWorkers(sourceMaxWorkers);       
        String broadwayCommand = "broadway TDM.ImportDataSubset " + "luName = '" + luName + "'" +
                ", dcName='" + sourceAffinity + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", loadIndicator='" + true + "'" +
                ", beID='" + beID + "'" +
                ", LuID='" + luID + "' ,isParamCoupling = " + paramCoupling + " , taskTitle = '" + taskTitle + "'"
                + " , maxNumOfWorkers = '" + maxNumOfWorkers + "'";

        // Check if param table exists and create it, and if it exists, check if its
        // structure is correct
        // TDM 9.1 only check if its not paramsCoupling
        if (!paramCoupling) {
            fnCreateUpdateLUParams(luName);
        }
        // log.info("TRAINING >>>> "+broadwayCommand);
        Db.Rows rows = fabric().fetch(broadwayCommand);
        String batchID = null;
        for (Db.Row row : rows) {
            batchID = "" + row.get("batchID");
        }
        Map<String, String> entities = fnUpdateAIProcess(taskExecutionID, "-1");
        ExecutionInfo.put("total", "" + entities.get("total"));
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;
    }

    private static Map<String, String> executeGenerationSubset(String taskExecutionID, String luName, String luID,
            String numOfEntities, String trainingExecutionID, String taskTitle, String sourceMaxWorkers, String sourceAffinity) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();   
        String affinity = getAffinityString(sourceAffinity);     
        String maxNumOfWorkers = findMaxNumOfWorkers(sourceMaxWorkers);
        String batchCommand = "BATCH " + luName + ".(" + luName + "_" + taskExecutionID
                + ") FABRIC_COMMAND=? WITH " + affinity + " ASYNC='true'" + " BATCH_ID_PREFIX ='" + taskTitle + "'" + maxNumOfWorkers;
        String broadwayCommand = "broadway TDM.GenerationDataSubset " + "LuID = '" + luID + "'" +
                ", numOfEntities='" + numOfEntities + "'" +
                ", luName='" + luName + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", trainingTaskId='" + trainingExecutionID + "', iid=?";
        // log.info("GENERATION >>>> "+broadwayCommand);
        String batchID = (String) fabric().fetch(batchCommand, broadwayCommand).firstValue();

        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;
    }

    private static Map<String, String> executeEvaluationJob(String taskExecutionID, String processName, String processID,String numOfEntities, String taskTitle, String targetMaxWorkers) throws Exception{ 
            Map<String, String> executionStatus = new LinkedHashMap<>();
            // Check if the status of the task with the specified process_id is not failed
            String sql = "SELECT COUNT(*) FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
            Long count = Long.valueOf(db(TDM).fetch(sql,taskExecutionID, 0, "failed","stopped").firstValue().toString());
            if (count == 0) {
                // If the status is not failed, execute the update query
                sql = "UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=? WHERE task_execution_id=? AND process_id=?";
                db(TDM).execute(sql, "running", taskExecutionID, processID);
            }else{
                sql = "SELECT execution_status FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
                String status = db(TDM).fetch(sql,taskExecutionID, -2, "failed","stopped").firstValue().toString();
                executionStatus.put("total", "0");
                executionStatus.put("fabric_execution_id", null);
                executionStatus.put("status", status);
                return executionStatus;
            }
            String query = "Select u.lu_name,l.lu_id,l.target_affinity FROM " + TDMDB_SCHEMA + ".task_execution_list l Join " + TDMDB_SCHEMA + ".tasks_logical_units u on l.lu_id=u.lu_id and l.task_id=u.task_id And l.process_id=0 " +
                    "WHERE task_execution_id= ?";
            Db.Rows taskExecutionList = db(TDM).fetch(query, taskExecutionID);
            String luName = "";
            String targetAffinity = "";
            for (Db.Row row : taskExecutionList) {
                luName = "" + row.get("lu_name");
                targetAffinity = "" + row.get("target_affinity");
            }
                        
            String broadwayCommand = "broadway " + luName + ".EvaluationDataSubset taskExecutionID =" + taskExecutionID + " , iid=?";           
            String affinity = getAffinityString(targetAffinity);
            String maxNumOfWorkers = findMaxNumOfWorkers(targetMaxWorkers);
            String batch = "BATCH " + luName + ".(" + luName + "_" + taskExecutionID + ")" + " fabric_command=? with " + affinity + " async=true" + " BATCH_ID_PREFIX ='" + taskTitle + "'" + maxNumOfWorkers;
            //log.info("EVALUATION >>>> "+ broadwayCommand);

            String batchID =  (String) fabric().fetch(batch, broadwayCommand).firstValue();
            executionStatus.put("total", numOfEntities);
            executionStatus.put("fabric_execution_id", batchID);
            return executionStatus;
            }
}
