/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.tdmProcessExecution;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.TDM.*;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;
import com.k2view.fabric.events.*;
import com.k2view.fabric.fabricdb.datachange.TableDataChange;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnCreateUpdateLUParams;
import static com.k2view.cdbms.usercode.lu.TDM.Globals.*;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.TASK_PROPERTIES.*;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.updatedAIFailedStatus;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.TdmExecuteTask.updatedFailedStatus;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnUpdateAIProcess;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.isParamsCoupling;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends UserCode {
    public static final String TDM = "TDM";

    @type(UserJob)
	public static void tdmProcessExecution(Long taskExecutionID, String processType, String sessionGlobals, String numOfEntities, String subsetID, String luID) throws Exception {
        //log.info("tdmProcessExecution Starting");
        String executionId = "";

        String executionsSql = "Select t.process_id , t.process_name, t.execution_order, t.process_type, t.parameters from " +
            TDMDB_SCHEMA + ".tasks_exe_process t, " + TDMDB_SCHEMA + ".task_execution_list l " +
            "where l.task_execution_id = ? and l.process_id = t.process_id and upper(l.execution_status) = 'PENDING' " +
            "and l.task_id = t.task_id and t.process_type = ? " +
            "order by t.execution_order";
        
        //log.info("tdmProcessExecution - sessionGlobals: " + sessionGlobals);

        //Map<?,?> globalsInput = Json.get().fromJson(sessionGlobals, Map.class);

        sessionGlobals = sessionGlobals.substring(1, sessionGlobals.length()-1); //remove curly brackets
        String[] keyValuePairs= sessionGlobals.split(",(?![^\\{]*\\})"); //split the string to creat key-value pairs according to the last , after the = 
        Map<String,String> globalsInput = new HashMap<>();               
        //log.info("keyValuePairs: " + keyValuePairs.toString());
        for(String pair : keyValuePairs)                        //iterate over the pairs
        {
           // log.info("pair: " + pair);
            String[] entry = pair.split("=");                 //split the pairs to get key and value 
            //log.info("entry key: " + entry[0]);
            String value = "";
            if (entry.length > 1) {
                value = entry[1];
            }
            //log.info("entry value: " + value);
            globalsInput.put(entry[0].trim(), value);          //add them to the hashmap and trim whitespaces
        }
    
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
        Db.Rows rows = db(TDM).fetch(executionsSql, taskExecutionID, processType);
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            String processName = Util.rte(() -> resultSet.getString("process_name"));
            Integer executionOrder = Util.rte(() -> resultSet.getInt("execution_order"));
            String processID = Util.rte(() -> resultSet.getString("process_id"));
            String flowParams = Util.rte(() -> resultSet.getString("parameters"));
            String luName = null;

            Map<String, Object> flowParamJson=null;

            if (flowParams != null && !"".equals(flowParams)) {
                flowParams = flowParams.replaceAll("\\\\n","").replaceAll("\\\\t","");
		        //log.info("flowParams after replace: " + flowParams);
		        if( flowParams!=null  && !("null".equalsIgnoreCase(flowParams))){
                    flowParamJson = Json.get().fromJson(flowParams, Map.class);
                }
            }

            if (flowParamJson != null && !(flowParamJson.isEmpty())) {
                List<Map <String, Object>> flowParamList = (List<Map <String, Object>>)flowParamJson.get("inputs");
                for (Map <String, Object> flowParamMap : flowParamList) {
                    Object paramValue = flowParamMap.get("value");
                    if("".equals(paramValue)){
                        paramValue=null;
                    }
                   
                    /*try {

                        String paramValueJson = Json.get().toJson(paramValue.toString());
                        paramValue = paramValueJson.substring( 1, paramValueJson.length() - 1 );
                        
                        
                    } catch (JSONException  e) {

                    }*/
                    //String valueStr = fabric().fetch("broadway TDM.CheckIfJsonAndRetun paramValue=?", paramValue).firstValue().toString();
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
                        Map<String, String> executionInfo = executeTrainingJob(String.valueOf(taskExecutionID), processName, processID);
                        AIprocessExecution(executionInfo, taskExecutionID, processID,luID);
                    }else {
                        sql = "SELECT execution_status FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? and process_id=? AND Lower(execution_status) in (?,?) ";
                        String status = db(TDM).fetch(sql,taskExecutionID, 0, "failed","stopped").firstValue().toString();
                        Util.rte(() -> db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET execution_status=?,num_of_processed_entities = ?, " +
                                                       "num_of_copied_entities = ?, num_of_failed_entities = ? ,fabric_execution_id=?, " +
                                                       "start_execution_time = COALESCE(start_execution_time, (now() at time zone 'utc')) " +
                                                       "WHERE task_execution_id=? and process_id=?", status , null, null, null, null, taskExecutionID, processID));
                    }
                } else if ("Generating Data Subset".equalsIgnoreCase(processName) || "Importing Data Subset".equalsIgnoreCase(processName)) {
                    Map<String, String> executionInfo = executeGenerationJob(String.valueOf(taskExecutionID), processName, processID,numOfEntities,subsetID);
                    AIprocessExecution(executionInfo, taskExecutionID, processID,luID);
                }
                //log.info("************* set task execution list to running for process id " + processID + " *************");
                else {
                    log.info("************* set task execution list to running for process id " + processID + " *************");
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
                    String broadwayCommand = "broadway " + luName + "." + processName + " iid=?," + fabricCommandParams;
                    String batch = "BATCH " + luName + ".('" + taskExecutionID + "_" + processID + "')" + " fabric_command=? with async=true";
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
	
    private static void waitUntilPrevProcessDone(Long taskExecutionID, Integer executionOrder, String processType) throws Exception {
        long count = -1;
        //log.info("waitUntilPrevProcessDone Starting with - taskExecutionID: " + taskExecutionID +", executionOrder: " + executionOrder);
        String EXECUTIONS_COUNT =  "select count(*) from " + TDMDB_SCHEMA + ".tasks_exe_process tt inner join " + TDMDB_SCHEMA + ".task_execution_list ll on tt.task_id=ll.task_id " +
            "where ll.task_execution_id =? and ll.process_id = tt.process_id and (? = -100 or tt.execution_order < ?) and tt.process_type = ? " +
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
                String query = "UPDATE " + TDMDB_SCHEMA + ".task_execution_list SET fabric_execution_id=? ";
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

    private static Map<String, String> executeTrainingJob(String taskExecutionID,String processName, String processID) throws Exception {
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
        String query = "Select l.data_center_name,u.lu_name,l.lu_id FROM " + TDMDB_SCHEMA + ".task_execution_list l Join " + TDMDB_SCHEMA + ".tasks_logical_units u on l.lu_id=u.lu_id and l.task_id=u.task_id And l.process_id=0 " +
                "WHERE task_execution_id= ?";
        Db.Rows taskExecutionList = db(TDM).fetch(query, taskExecutionID);
        String dcName = "";
        String luID = "";
        String luName = "";
        for (Db.Row row : taskExecutionList) {
            dcName = "" + row.get("data_center_name");
            luName = "" + row.get("lu_name");
            luID = "" + row.get("lu_id");
        }

        if ("Exporting Data Subset".equalsIgnoreCase(processName)) {
            executionStatus = executeExportingSubset(taskExecutionID,luName,dcName,luID);
        }else{
            executionStatus = executeTrainingSubset(taskExecutionID,luName,dcName,luID);
        }
       return executionStatus;
    }

    private static Map<String, String> executeExportingSubset(String taskExecutionID,String luName,String dcName,String luID) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        String broadwayCommand = "broadway TDM.ExportDataSubset " + "luName = '" + luName + "'" +
                ", dcName='" + dcName + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", LuID='" + luID +"'" ;
        //log.info("TRAINING >>>> "+broadwayCommand);
        Db.Rows rows = fabric().fetch(broadwayCommand);
        String batchID = null;
        for(Db.Row row:rows){
           batchID="" + row.get("batchID");
        }
        Map<String,String> entities = fnUpdateAIProcess(taskExecutionID,"0");
        ExecutionInfo.put("total", "" + entities.get("total"));
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;

    }

    private static Map<String, String> executeTrainingSubset(String taskExecutionID,String luName,String dcName,String luID) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        String batchCommand = "BATCH " +luName+ ".(" + luName + "_" + taskExecutionID + ") FABRIC_COMMAND=? WITH ASYNC='true'";
        String broadwayCommand = "broadway TDM.TrainingDataSubset " + "luName = '" + luName + "'" +
                ", dcName='" + dcName + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", LuID='" + luID +"', iid=?" ;
        //log.info("TRAINING >>>> "+broadwayCommand);
        String batchID= (String) fabric().fetch(batchCommand, broadwayCommand).firstValue();
        Map<String,String> entities = fnUpdateAIProcess(taskExecutionID,"-2");
        ExecutionInfo.put("total", "" + entities.get("total"));
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;

    }

    private static Map<String, String> executeGenerationJob(String taskExecutionID,String processName, String processID,String numOfEntities, String subsetID) throws Exception {
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
        String query = "Select u.lu_name,l.lu_id FROM " + TDMDB_SCHEMA + ".task_execution_list l Join " + TDMDB_SCHEMA + ".tasks_logical_units u on l.lu_id=u.lu_id and l.task_id=u.task_id And l.process_id=0 " +
                "WHERE task_execution_id= ?";
        Db.Rows taskExecutionList = db(TDM).fetch(query, taskExecutionID);
        String dcName = "";
        String luID = "";
        String luName = "";
        String trainingExecutionID = "";
        for (Db.Row row : taskExecutionList) {
            luName = "" + row.get("lu_name");
            luID = "" + row.get("lu_id");
        }
        if (!"Importing Data Subset".equalsIgnoreCase(processName)) {
            executionStatus = executeGenerationSubset( taskExecutionID, luName, luID, numOfEntities, subsetID);
        }else{
            executionStatus = executeImportingSubset(taskExecutionID,luName,dcName,luID);
        }
    return executionStatus;
    }

    private static Map<String, String> executeImportingSubset(String taskExecutionID,String luName,String dcName,String luID) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        String sql = "SELECT be_id FROM " + TDMDB_SCHEMA + ".task_execution_list WHERE task_execution_id= ? AND lu_id= ? ";
        String beID=db(TDM).fetch(sql, taskExecutionID,luID).firstValue().toString();
        Boolean paramCoupling = isParamsCoupling();
        String broadwayCommand = "broadway TDM.ImportDataSubset " + "luName = '" + luName + "'" +
                ", dcName='" + dcName + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", loadIndicator='" + true + "'" +
                ", beID='" + beID + "'" +
                ", LuID='" + luID +"' ,isParamCoupling = " + paramCoupling ;

        //Check if param table exists and create it, and if it exists, check if its structure is correct
        //TDM 9.1 only check if its not paramsCoupling 
        if(!paramCoupling){
            fnCreateUpdateLUParams(luName);
        }  
        //log.info("TRAINING >>>> "+broadwayCommand);
        Db.Rows rows = fabric().fetch(broadwayCommand);
        String batchID = null;
        for(Db.Row row:rows){
        batchID="" + row.get("batchID");
        }
        Map<String,String> entities = fnUpdateAIProcess(taskExecutionID,"-1");
        ExecutionInfo.put("total", "" + entities.get("total"));
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;
    }

    private static Map<String, String> executeGenerationSubset(String taskExecutionID,String luName,String luID,String numOfEntities,String trainingExecutionID) throws Exception {
        Map<String, String> ExecutionInfo = new LinkedHashMap<>();
        String batchCommand = "BATCH " +luName+ ".(" + luName + "_" + taskExecutionID + ") FABRIC_COMMAND=? WITH ASYNC='true'";
        String broadwayCommand = "broadway TDM.GenerationDataSubset " + "LuID = '" + luID + "'" +
                ", numOfEntities='" + numOfEntities + "'" +
                ", luName='" + luName + "'" +
                ", taskExecutionID='" + taskExecutionID + "'" +
                ", trainingTaskId='" + trainingExecutionID +"', iid=?" ;
        //log.info("GENERATION >>>> "+broadwayCommand);
        String batchID= (String) fabric().fetch(batchCommand, broadwayCommand).firstValue();
        
        ExecutionInfo.put("fabric_execution_id", batchID);
        return ExecutionInfo;
    }
    
}
