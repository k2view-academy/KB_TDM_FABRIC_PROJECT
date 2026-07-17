package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_Tasks;

import static com.cronutils.model.CronType.QUARTZ;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.AI_ENVIRONMENT;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.MAX_NUMBER_OF_ENTITIES_IN_LIST;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.SYNTHETIC_ENVIRONMENT;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetReferenceDetailedData;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetReferenceSummaryData;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnSplitUID;
import static com.k2view.cdbms.usercode.common.TDM.TDMRef.SharedLogic.fnGetTableCountIndicator;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.createTaskGEnerateParams;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.createTaskGlobals;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnAddAIExecutionProcess;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnAddTaskExecutionProcess;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnCheckMigrateStatusForEntitiesList;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnExecutionSummaryReport;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetNumberOfMatchingEntities;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetRefTableForLoadWithVersion;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetRootLUs;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetTaskExeStatsForEntityFromTDMDB;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetTaskExeStatsForEntityFromTDMLU;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetTaskExecutionProcesses;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetVersionForLoadRef;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetVersionsForLoad;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnInsertActivity;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnMigrateStatusWs;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnPostTaskLogicalUnits;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnResumeTaskExecution;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnSaveRefTablestoTask;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnStopTaskExecution;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnUpdateFailedLUsInTree;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnValidateBELogicalUnits;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.canUserPerformTaskOperation;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getEnvironmentIdByName;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.requiresSourceCheck;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.requiresTargetCheck;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.sourceEnvSystemsDisabled;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.targetEnvSystemsDisabled;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnGetExecutionProcessParams;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnGetFlowParams;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateExtractNoRetention;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateOverrideSyncMode;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateProductForTask;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateSourceEnvForTask;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateTargetEnvForTask;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateVersionExecIdNotExpired;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.validateEnvTypeForUser;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.validateEnvironmentsAndBusinessEntity;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.validateMaxWorkers;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.validateParams;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.INTERNAL_ONLY_OVERRIDE_KEYS;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.checkWsResponse;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnBatchStatistics;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetLogicalUnitsByEnvironmentAndBusinessentity;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetTaskExecOverrideAttrs;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetTaskExecutionMode;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserEnvs;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserPermissionGroup;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getInterfaceConfig;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getLuTablesList;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_TasksManagment.Logic.wsAssignTaskToTaskGroups;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_TasksManagment.Logic.wsInsertTaskExecution;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_TasksManagment.Logic.wsUpdateTaskExecution;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_utils.Logic.wsGetMaxWorkersPerNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.OverrideParamKey;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.StartTask;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.param;
import com.k2view.fabric.api.endpoint.Endpoint.resultMetaData;
import com.k2view.fabric.api.endpoint.Endpoint.webService;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.ParamConvertor;
import com.k2view.fabric.common.mtable.MTable;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked", "rawtypes"})
public class Logic extends WebServiceUserCode {
    public static String schema = TDMDB_SCHEMA;
    public static final String DB_FABRIC = "fabric";
    public static final String TDM = "TDM";
    public static final String TABLES = "TABLES";
    public static final String TABLE_LEVEL_LU = "TDM_TableLevel";

    @desc("Gets the list of available Business Entities that can be selected in the task based on the related Products of the task's environment. The API checks the source environment of Extract tasks and the target environment of Load tasks.")
    @webService(path = "environment/{envId}/businessEntitiesForEnvProducts", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"be_id\": 1,\r\n" + "      \"be_name\": \"BE\"\r\n" + "    },\r\n" + "    {\r\n" + "      \"be_id\": 2,\r\n" + "      \"be_name\": \"BE2\"\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetBusinessEntitiesForEnvProducts(@param(required = true) Long envId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        List<HashMap<String, Object>> result = new ArrayList<>();
        String message = null;
        String errorCode = "";
        try {
            String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".product_logical_units lu " + "INNER JOIN " + TDMDB_SCHEMA + ".products p " + "ON (lu.product_id = p.product_id AND p.product_status = \'Active\') " + "INNER JOIN " + TDMDB_SCHEMA + ".environment_products ep " + "ON (lu.product_id = ep.product_id AND ep.status = \'Active\') " + "INNER JOIN " + TDMDB_SCHEMA + ".business_entities be " + "ON (be.be_id = lu.be_id) " + "WHERE environment_id = " + envId + " AND be_status = \'Active\'";
            Db.Rows rows = db(TDM).fetch(sql);


            HashMap<String, Object> be;
            for (Db.Row row : rows) {
                ResultSet resultSet = row.resultSet();
                be = new HashMap<>();
                be.put("be_id", resultSet.getInt("be_id"));
                be.put("be_name", resultSet.getString("be_name"));
                result.add(be);
            }

            HashMap<String, Object> be2;
            for (int i = 0; i < result.size(); i++) {
                be = result.get(i);
                for (int j = i + 1; j < result.size(); j++) {
                    be2 = result.get(j);
                    if (be.get("be_id").toString().equals(be2.get("be_id").toString()) && be.get("be_name").toString().equals(be2.get("be_name").toString())) {
                        result.remove(j);
                        j--;
                    }
                }
            }

            errorCode = "SUCCESS";
            response.put("result", result);
            if (rows != null) {
                rows.close();
            }
        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

	@desc("""
            This is the main API to get the task details. This API gets the list of all TDM tasks or a list of given task IDs if the input task_ids parameter is populated. The input task_ids is an optional parameter that can be populated to return the data of a given list of tasks. The ID(s) of the required task(s), will be supplied in this parameter separated by comma. For example, task_ids=4 or task_ids=3,2,6.

            If task_ids parameter is not populated, the data of all tasks will be returned by the API.
            """)
    @webService(path = "tasks", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                {
              "result": [
                {
                  "task_last_updated_date": "2026-04-12 08:39:48.457",
                  "task_description": "zivtest",
                  "filterout_reserved": "NA",
                  "be_id": 1,
                  "reserve_retention_period_type": "Days",
                  "statistics_report_flag": null,
                  "environment_id": 1,
                  "selection_method": "L",
                  "refresh_reference_data": false,
                  "tester": null,
                  "be_last_updated_date": "2026-03-31 11:34:44.720512",
                  "owners": [],
                  "refcount": 0,
                  "num_of_entities": 3,
                  "selected_subset_task_exe_id": 0,
                  "tester_type": null,
                  "reserve_note": null,
                  "enable_sequence_report": false,
                  "load_entity": false,
                  "selected_version_task_exe_id": 0,
                  "task_created_by": "ziv.genat@k2view.com",
                  "be_last_updated_by": "admin",
                  "scheduling_end_date": null,
                  "retention_period_type": "Do Not Delete",
                  "environment_point_of_contact_phone1": null,
                  "processnames": null,
                  "testers": [],
                  "selection_param_value": "1,2,3",
                  "environment_status": "Active",
                  "be_status": "Active",
                  "task_last_updated_by": "ziv.genat@k2view.com",
                  "selected_ref_version_task_exe_id": 0,
                  "task_execution_status": "Active",
                  "mask_sensitive_data": true,
                  "sync_mode": "ON",
                  "enable_execution": true,
                  "task_groups": [
                    {
                      "task_group_id": 1,
                      "task_group_name": "General"
                    },
                    {
                      "task_group_id": 3,
                      "task_group_name": "ziv1"
                    },
                    {
                      "task_group_id": 4,
                      "task_group_name": "ziv2"
                    }
                  ],
                  "execution_mode": "INHERITED",
                  "replace_sequences": false,
                  "environment_point_of_contact_last_name": null,
                  "environment_point_of_contact_email": null,
                  "be_description": "Normal Hierarchy",
                  "reserve_retention_period_value": "5",
                  "parameters": null,
                  "environment_expiration_date": null,
                  "environment_point_of_contact_phone2": null,
                  "environment_created_by": "admin",
                  "clone_ind": false,
                  "roles": [],
                  "environment_last_updated_by": "admin",
                  "be_creation_date": "2026-03-31 11:34:44.720512",
                  "task_id": 14,
                  "be_created_by": "admin",
                  "custom_logic_lu_name": null,
                  "source_environment_id": 1,
                  "role_id_orig": 0,
                  "scheduler": "immediate",
                  "environment_description": "This is the Source environment.",
                  "source_env_name": "Production",
                  "reserve_ind": false,
                  "task_title": "zivtest",
                  "environment_name": "Production",
                  "delete_before_load": false,
                  "allow_write": false,
                  "owner": null,
                  "in_place_masking_ind": false,
                  "task_status": "Active",
                  "retention_period_value": "-1",
                  "executioncount": 0,
                  "environment_last_updated_date": "2026-03-31 11:34:44.720512",
                  "be_name": "Customer",
                  "version_ind": false,
                  "task_creation_date": "2026-04-12 08:39:48.457",
                  "task_globals": false,
                  "environment_point_of_contact_first_name": null,
                  "task_type": "EXTRACT",
                  "environment_creation_date": "2026-03-31 11:34:44.720512",
                  "owner_type": null,
                  "creatorRoles": [
                    "k2view_k2v_user"
                  ]
                }
              ],
              "errorCode": "SUCCESS",
              "message": null
            }
                """)
    public static Object wsGetTasks(@param(description="list of task IDs separated by a comma") String task_ids,
                                    @param(description = "Specifies the task status to retrieve. Options: 'Active' to get only active tasks, 'Inactive' for only inactive tasks, or 'Both' to retrieve all tasks regardless of status.")
                                    String mode) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        final String TASK_CREATED_BY_SEPARATOR = "##";
        final String ROLE_SEPARATOR = TDM_PARAMETERS_SEPARATOR;

        Db.Rows result = fnGetTasks(task_ids,mode);
        
        String q = "SELECT * FROM " + TDMDB_SCHEMA + ".ENVIRONMENT_ROLES";
        Db.Rows rolesResult = db(TDM).fetch(q);
        
        List<Map<String, Object>> envsRoles = new ArrayList<>();
        for (Db.Row role : rolesResult) {
            ResultSet roleResultSet = role.resultSet();
            HashMap<String, Object> map = new HashMap<>();
            map.put("environment_id", roleResultSet.getInt("environment_id"));
            map.put("role_id", roleResultSet.getInt("role_id"));
            map.put("allowed_test_conn_failure", roleResultSet.getBoolean("allowed_test_conn_failure"));
    
            envsRoles.add(map);
        }
    
        //modified newRow will be added to newResult list
        List<Map<String, Object>> newResult = new ArrayList<>();
    
        Integer prevTaskId = 0;
        HashMap<String, Object> prevRow = new HashMap<>();
        String callerUserId = sessionUser().name();
        String callerFabricRoles = String.join(ROLE_SEPARATOR, sessionUser().roles());
        Map<String,List<String>> rolesPerUserEnvs = new HashMap<>();
    
        for (Db.Row row : result) {
            HashMap<String, Object> newRow = new HashMap<>();
            String userId = null;
            
            ResultSet resultSet = row.resultSet();
            String userRoles = "";
            Long targetEnvId = resultSet.getLong("environment_id");
            Long sourceEnvId = resultSet.getLong("source_environment_id");
            if (resultSet.getString("task_created_by") != null) {
                List<String> creatorFabricRoles = new ArrayList<>();
                String taskCreatedBy = resultSet.getString("task_created_by");
                String taskKey = taskCreatedBy + TASK_CREATED_BY_SEPARATOR + sourceEnvId + 
                    TASK_CREATED_BY_SEPARATOR + targetEnvId;
                
                // Split the string using TASK_CREATED_BY_SEPARATOR and not split function
                int separatorIndex = taskCreatedBy.indexOf(TASK_CREATED_BY_SEPARATOR);
                if (separatorIndex != -1) {
                    userId = taskCreatedBy.substring(0, separatorIndex);  // Get userId before the separator
                    userRoles = taskCreatedBy.substring(separatorIndex + TASK_CREATED_BY_SEPARATOR.length());
                    creatorFabricRoles = new ArrayList<String>(Arrays.asList(userRoles.split(ROLE_SEPARATOR)));  // Split userRoles using ROLE_SEPARATOR
                } else {
                    userId = taskCreatedBy; // If separator is not found, use the entire string as userId
                }

                List<String> roleList = new ArrayList<>();
                if (!rolesPerUserEnvs.containsKey(taskKey)) {

                    Set<String> creatorEnvRoles = fnGetUserEnvRoles(targetEnvId, userId, userRoles);
                    Set<String> callerEnvRoles = creatorEnvRoles;
                    if (!callerUserId.equals(userId) || !callerFabricRoles.equals(userRoles)) {
                        callerEnvRoles = fnGetUserEnvRoles(targetEnvId, callerUserId, callerFabricRoles);
                    }

                    creatorEnvRoles.retainAll(callerEnvRoles);

                    Set<String> creatorSourceEnvRoles = null;
                    if (resultSet.getLong("source_environment_id") != resultSet.getLong("environment_id")) {
                        creatorSourceEnvRoles = fnGetUserEnvRoles(resultSet.getLong("source_environment_id"), userId, userRoles);
                        Set<String> callerSourceEnvRoles = creatorSourceEnvRoles;
                        if (!callerUserId.equals(userId) || !callerFabricRoles.equals(userRoles)) {
                            callerSourceEnvRoles =  fnGetUserEnvRoles(resultSet.getLong("source_environment_id"), callerUserId, callerFabricRoles);
                        }
                        creatorSourceEnvRoles.retainAll(callerSourceEnvRoles);
                    } else {
                        creatorSourceEnvRoles = callerEnvRoles;
                    }

                    if(creatorEnvRoles.size() > 0 && creatorSourceEnvRoles.size() > 0) {
                        for(String role :sessionUser().roles()){
                            if(!("Everybody".equalsIgnoreCase(role)) && !creatorFabricRoles.contains(role)){
                                creatorFabricRoles.add(role);
                            }
                        }
                    }

                    rolesPerUserEnvs.put(taskKey, creatorFabricRoles);
                } else {
                    creatorFabricRoles = rolesPerUserEnvs.get(taskKey);
                }

                if (!creatorFabricRoles.isEmpty()) {
                    newRow.put("creatorRoles", creatorFabricRoles);
                }

            }
            newRow.put("task_id", resultSet.getInt("task_id"));
            newRow.put("task_title", resultSet.getString("task_title"));
            newRow.put("task_status", resultSet.getString("task_status"));
            newRow.put("task_execution_status", resultSet.getString("task_execution_status"));
            newRow.put("num_of_entities", resultSet.getInt("num_of_entities"));
            newRow.put("environment_id", resultSet.getObject("environment_id"));
            newRow.put("be_id", row.get("be_id"));
            newRow.put("selection_method", resultSet.getString("selection_method"));
            newRow.put("selection_param_value", resultSet.getString("selection_param_value"));
            newRow.put("custom_logic_lu_name", resultSet.getString("custom_logic_lu_name"));
            newRow.put("parameters", resultSet.getString("parameters"));
            newRow.put("refresh_reference_data", resultSet.getBoolean("refresh_reference_data"));
            newRow.put("delete_before_load", resultSet.getBoolean("delete_before_load"));
            newRow.put("replace_sequences", resultSet.getBoolean("replace_sequences"));
            newRow.put("scheduler", resultSet.getString("scheduler"));
            newRow.put("task_created_by", userId);
            newRow.put("task_creation_date", resultSet.getString("task_creation_date"));
            newRow.put("task_last_updated_date", resultSet.getString("task_last_updated_date"));
            newRow.put("task_last_updated_by", resultSet.getString("task_last_updated_by"));
            newRow.put("source_env_name", resultSet.getString("source_env_name"));
            newRow.put("source_environment_id", resultSet.getObject("source_environment_id"));
            newRow.put("load_entity", resultSet.getBoolean("load_entity"));
            newRow.put("task_type", resultSet.getString("task_type"));
            newRow.put("version_ind", resultSet.getBoolean("version_ind"));
            newRow.put("retention_period_type", resultSet.getString("retention_period_type"));
            newRow.put("retention_period_value", resultSet.getString("retention_period_value"));
            newRow.put("selected_version_task_exe_id", resultSet.getInt("selected_version_task_exe_id"));
            newRow.put("selected_subset_task_exe_id", resultSet.getInt("selected_subset_task_exe_id"));
            newRow.put("scheduling_end_date", resultSet.getString("scheduling_end_date"));
            newRow.put("selected_ref_version_task_exe_id", resultSet.getInt("selected_ref_version_task_exe_id"));
            newRow.put("task_globals", resultSet.getBoolean("task_globals"));
            newRow.put("task_description", resultSet.getString("task_description"));
            newRow.put("sync_mode", resultSet.getString("sync_mode"));
            newRow.put("environment_name", resultSet.getString("environment_name"));
            newRow.put("environment_description", resultSet.getString("environment_description"));
            newRow.put("environment_expiration_date", resultSet.getString("environment_expiration_date"));
            newRow.put("environment_point_of_contact_first_name", resultSet.getString("environment_point_of_contact_first_name"));
            newRow.put("environment_point_of_contact_last_name", resultSet.getString("environment_point_of_contact_last_name"));
            newRow.put("environment_point_of_contact_phone1", resultSet.getString("environment_point_of_contact_phone1"));
            newRow.put("environment_point_of_contact_phone2", resultSet.getString("environment_point_of_contact_phone2"));
            newRow.put("environment_point_of_contact_email", resultSet.getString("environment_point_of_contact_email"));
            newRow.put("environment_created_by", resultSet.getString("environment_created_by"));
            newRow.put("environment_creation_date", resultSet.getString("environment_creation_date"));
            newRow.put("environment_last_updated_date", resultSet.getString("environment_last_updated_date"));
            newRow.put("environment_last_updated_by", resultSet.getString("environment_last_updated_by"));
            newRow.put("environment_status", resultSet.getString("environment_status"));
            newRow.put("allow_write", resultSet.getBoolean("allow_write"));
            newRow.put("be_name", resultSet.getString("be_name"));
            newRow.put("be_description", resultSet.getString("be_description"));
            newRow.put("be_created_by", resultSet.getString("be_created_by"));
            newRow.put("be_creation_date", resultSet.getString("be_creation_date"));
            newRow.put("be_last_updated_date", resultSet.getString("be_last_updated_date"));
            newRow.put("be_last_updated_by", resultSet.getString("be_last_updated_by"));
            newRow.put("be_status", resultSet.getString("be_status"));
            newRow.put("owner", resultSet.getString("owner"));
            newRow.put("owner_type", resultSet.getString("owner_type"));
            newRow.put("tester", resultSet.getString("tester"));
            newRow.put("tester_type", resultSet.getString("tester_type"));
            newRow.put("role_id_orig", resultSet.getInt("role_id_orig"));
            int executionCount = resultSet.getObject("executioncount") == null ? 0 : 1;
            newRow.put("executioncount", executionCount);
            int refcount = resultSet.getObject("refcount") == null ? 0 : 1;
            newRow.put("refcount", refcount);
            newRow.put("processnames", resultSet.getString("processnames"));
            newRow.put("reserve_ind", resultSet.getBoolean("reserve_ind"));
            newRow.put("reserve_retention_period_type", resultSet.getString("reserve_retention_period_type"));
            newRow.put("reserve_retention_period_value", resultSet.getString("reserve_retention_period_value"));
            newRow.put("reserve_note", resultSet.getString("reserve_note"));
            newRow.put("filterout_reserved", resultSet.getString("filterout_reserved"));
            newRow.put("mask_sensitive_data", resultSet.getBoolean("mask_sensitive_data"));
            newRow.put("clone_ind", resultSet.getBoolean("clone_ind"));
            newRow.put("execution_mode", resultSet.getString("execution_mode"));
            newRow.put("enable_execution", resultSet.getBoolean("enable_execution"));
            newRow.put("in_place_masking_ind", resultSet.getBoolean("in_place_masking_ind"));
            newRow.put("enable_sequence_report", resultSet.getBoolean("enable_sequence_report"));
            newRow.put("statistics_report_flag", resultSet.getString("statistics_report_flag"));
            newRow.put("task_override_fields", resultSet.getString("task_override_fields"));

            Map<String, Object> task = null;

            if("CLONE".equalsIgnoreCase(resultSet.getString("selection_method"))){
                newRow.put("clone_ind", true);
            }
            if (prevTaskId == resultSet.getInt("task_id")) {
                task = prevRow;
            } else {
                prevTaskId = resultSet.getInt("task_id");
                prevRow = newRow;
            }
    
            List<Map<String, Object>> roleArr = new ArrayList<>();
            //for (Db.Row role : rolesResult) {
            for (Map<String, Object> envsRole : envsRoles) {
                HashMap<String, Object> roleMap = new HashMap<>();
                String envId = (envsRole.get("environment_id")).toString();
                String taskEnvId = "" + resultSet.getInt("environment_id");
                if (envId.equals(taskEnvId)) {
                    roleMap.put("role_id", Integer.valueOf(envsRole.get("role_id").toString()));
                    roleMap.put("allowed_test_conn_failure", Boolean.valueOf(envsRole.get("allowed_test_conn_failure").toString()));
                    roleArr.add(roleMap);
                }
            }
            //check for evalaution indicator
            Long task_id = resultSet.getLong("task_id");
            if(task_id != 0L && sourceEnvId == -2){
                String sql = "Select Count(1) as count from " + TDMDB_SCHEMA + ".tasks_exe_process where task_id = " +
                                            task_id + " AND process_id = -3"  +
                                            " AND process_name = 'Evaluating Data Subset'";
                Object cnt = db(TDM).fetch(sql).firstValue();
                Long evaluation_ind = Long.valueOf("" + cnt);
                newRow.put("evaluation_ind", evaluation_ind == 1);
            }
                
            if (task != null) {
                List<Map<String, Object>> owners = (List<Map<String, Object>>) task.get("owners");
                Map<String, Object> owner = null;
                //if (!owners.contains(resultSet.getString("owner"))) {
                for (Map<String, Object> _owner : owners) {
                    if (_owner.get("owner").toString().equals(resultSet.getString("owner"))) {
                        owner = _owner;
                    }
                }
    
                // Add owner type
                if (owner == null) {
                    HashMap<String, Object> ownerMap = new HashMap<>();
                    if (resultSet.getString("owner") != null) {
                        ownerMap.put("owner", resultSet.getString("owner"));
                        ownerMap.put("owner_type", resultSet.getString("owner_type"));
                        owners.add(ownerMap);
                    }
                }
    
                List<Map<String, Object>> testers = (List<Map<String, Object>>) task.get("testers");
                Map<String, Object> tester = null;
                for (Map<String, Object> _tester : testers) {
                    if (_tester.get("tester").toString().equals(resultSet.getString("tester"))) {
                        tester = _tester;
                    }
                }
    
                if (tester != null) {  //add role_id_orig to role_id list
                    List<String> roleId = (List<String>) tester.get("role_id");
                    if (!roleId.contains(resultSet.getString("role_id_orig"))) {
                        roleId.add(resultSet.getString("role_id_orig"));
                    }
                } else { //add new tester to task testers
                    HashMap<String, Object> testerMap = new HashMap<>();
                    if (resultSet.getString("tester") != null) {
                        testerMap.put("tester", resultSet.getString("tester"));
                        testerMap.put("tester_type", resultSet.getString("tester_type"));
    
                        List<String> roleIdList = new ArrayList<>();
                        roleIdList.add(resultSet.getString("role_id_orig"));
                        testerMap.put("role_id", roleIdList);
                        testers.add(testerMap);
                    }
                }
    
            } else {
                List<Map<String, Object>> owners = new ArrayList<>();
                newRow.put("owners", owners);
                if (resultSet.getString("owner") != null) {
                    HashMap<String, Object> ownerMap = new HashMap<>();
                    ownerMap.put("owner", resultSet.getString("owner"));
                    ownerMap.put("owner_type", resultSet.getString("owner_type"));
                    owners.add(ownerMap);
                }
    
                List<Map<String, Object>> testers = new ArrayList<>();
                newRow.put("testers", testers);
                if (resultSet.getString("tester") != null) {
                    HashMap<String, Object> testerMap = new HashMap<>();
                    testerMap.put("tester", resultSet.getString("tester"));
                    testerMap.put("tester_type", resultSet.getString("tester_type"));
                    List<String> roleIdList = new ArrayList<>();
                    roleIdList.add(resultSet.getString("role_id_orig"));
                    testerMap.put("role_id", roleIdList);
                    testers.add(testerMap);
                }
    
                List<List<Map<String, Object>>> roles = new ArrayList<>();
    
                if (roleArr != null && roleArr.size() > 0) {
                    roles.add(roleArr);
                }
                newRow.put("roles", roles);
                newResult.add(newRow);
            }
            {
                String source_env_id = resultSet.getString("source_environment_id");
                String source_env_name = resultSet.getString("source_env_name");
                String target_env_id = resultSet.getString("environment_id");
                String target_env_name = resultSet.getString("environment_name");
                String sync_mode = resultSet.getString("sync_mode");
                String task_type = resultSet.getString("task_type");
                String beId = row.get("be_id") != null ? row.get("be_id").toString() : null;
                if (sourceEnvSystemsDisabled(source_env_id, source_env_name, task_type, sync_mode, task_id, beId)) {
                    String inactive_source_products = fnValidateProductForTask(source_env_id, source_env_name,
                            task_type, sync_mode, "SOURCE", task_id);
                    if (!"".equalsIgnoreCase(inactive_source_products)) {
                        newRow.put("inactive_source_products", inactive_source_products);
                    }
                }
                if (target_env_id != null && !target_env_id.equals(source_env_id) &&
                        targetEnvSystemsDisabled(target_env_id, target_env_name, task_type, sync_mode, task_id, beId)) {
                    String inactive_target_products = fnValidateProductForTask(target_env_id, target_env_name,
                            task_type, sync_mode, "TARGET", task_id);
                    if (!"".equalsIgnoreCase(inactive_target_products)) {
                        newRow.put("inactive_target_products", inactive_target_products);
                    }
                }
            }
        }
        if (result != null) {
            result.close();
        }
        if (rolesResult != null) {
            rolesResult.close();
        }

        // Fetch task group mappings for all tasks in the result
        if (!newResult.isEmpty()) {
            StringBuilder taskIdsBuilder = new StringBuilder();
            for (Map<String, Object> taskRow : newResult) {
                if (taskIdsBuilder.length() > 0) taskIdsBuilder.append(",");
                taskIdsBuilder.append(taskRow.get("task_id"));
            }
            String groupSql = "SELECT tgm.task_id, tgm.task_group_id, tg.task_group_name FROM " + TDMDB_SCHEMA +
                ".task_group_mapping tgm JOIN " + TDMDB_SCHEMA +
                ".task_groups tg ON tgm.task_group_id = tg.task_group_id" +
                " WHERE tgm.task_id IN (" + taskIdsBuilder + ")";
            Map<Integer, List<Map<String, Object>>> groupsByTaskId = new HashMap<>();
            try (Db.Rows groupRows = db(TDM).fetch(groupSql)) {
                for (Db.Row groupRow : groupRows) {
                    int tId = Integer.parseInt(groupRow.get("task_id").toString());
                    groupsByTaskId.computeIfAbsent(tId, k -> new ArrayList<>());
                    Map<String, Object> groupMap = new LinkedHashMap<>();
                    groupMap.put("task_group_id", Long.parseLong(groupRow.get("task_group_id").toString()));
                    groupMap.put("task_group_name", groupRow.get("task_group_name"));
                    groupsByTaskId.get(tId).add(groupMap);
                }
            }
            // Fetch execution permissions for all tasks
            for (Map<String, Object> taskRow : newResult) {
                int tId = (Integer) taskRow.get("task_id");
                taskRow.put("task_groups", groupsByTaskId.getOrDefault(tId, new ArrayList<>()));
            }
            String permissionsSql = "SELECT task_id, permitted_user, user_type FROM " + TDMDB_SCHEMA +
            ".task_exe_permissions WHERE task_id IN (" + taskIdsBuilder + ")";
            Map<Integer, List<Map<String, Object>>> permissionsByTaskId = new HashMap<>();
            try (Db.Rows permRows = db(TDM).fetch(permissionsSql)) {
                for (Db.Row permRow : permRows) {
                    int tId = Integer.parseInt(permRow.get("task_id").toString());
                    permissionsByTaskId.computeIfAbsent(tId, k -> new ArrayList<>());
                
                    String permittedUser = permRow.get("permitted_user").toString();
                    String userType      = permRow.get("user_type").toString();
                
                    Map<String, Object> permMap = new LinkedHashMap<>();
                    permMap.put("type", userType);
                
                    if ("ID".equals(userType) && callerUserId.equalsIgnoreCase(permittedUser)) {
                        permMap.put("value", "ME");
                    } else if ("GROUP".equals(userType) && callerFabricRoles.contains(permittedUser)) {
                        permMap.put("value", "MY_GROUP");
                    } else {
                        permMap.put("value", permittedUser);
                    }
                
                    permissionsByTaskId.get(tId).add(permMap);
                }
            }
            for (Map<String, Object> taskRow : newResult) {
                int tId = (Integer) taskRow.get("task_id");
                taskRow.put("permissions", permissionsByTaskId.getOrDefault(tId, new ArrayList<>()));
                Long taskID = Long.valueOf(taskRow.get("task_id").toString());
                Map<String, Object> notesResponse = (Map<String, Object>) wsGetTaskNotes(taskID);
                taskRow.put("notes", notesResponse.get("result"));
            }

        }
       
        errorCode = "SUCCESS";
        response.put("result", newResult);
    
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static Set<String> fnGetUserEnvRoles(Long envId, String userId, String fabricRoles) throws Exception{
        
        String sql = "SELECT u.role_id FROM " + schema + ".environment_role_users u INNER JOIN " + schema + ".environment_roles r " +
					"ON (u.role_id = r.role_id AND r.role_status = 'Active') " +
					"WHERE u.environment_id = ? " + 
					"AND  u.user_id = ? AND u.user_type = 'ID' " +
                    " UNION SELECT u.role_id FROM " + schema + ".environment_role_users u INNER JOIN " + schema + ".environment_roles r " +
					"ON (u.role_id = r.role_id AND r.role_status = 'Active') " +
					"WHERE u.environment_id = ? " + 
					"AND  u.user_id = ANY(string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "')) AND u.user_type = 'GROUP' ";
        
        
        Db.Rows envUserRoles = db(TDM).fetch(sql, envId, userId, envId, fabricRoles);
        Set<String> result = new HashSet<>();
        for (Db.Row row : envUserRoles) {
            result.add(row.get("role_id").toString());
        }


		if (envUserRoles != null) {
			envUserRoles.close();
		}
        return result;
        
    }

    @desc("Gets Running Tasks IDs")
    @webService(path = "runningTasks", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    \"82\",\r\n" + "    \"83\",\r\n" + "    \"68\",\r\n" + "    \"72\",\r\n" + "    \"69\",\r\n" + "    \"67\",\r\n" + "    \"87\",\r\n" + "    \"114\"\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetRunningTasks() throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            String sql = "select DISTINCT task_id from " + TDMDB_SCHEMA + ".task_execution_summary " + "where  (lower(execution_status) <> 'failed' AND lower(execution_status) <> 'completed' " + "AND lower(execution_status) <> 'stopped' AND lower(execution_status) <> 'killed')";
            Db.Rows rows = db(TDM).fetch(sql);
            List<String> result = new ArrayList<>();
            for (Db.Row row : rows) {
                result.add(row.get("task_id").toString());
            }
            errorCode = "SUCCESS";
            response.put("result", result);
            if (rows != null) {
                rows.close();
            }

        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("Gets the Logical Units list of the task's Business Entity (BE) and the task's environment products. The user can select part or all of the LUs in the list in the task. For extract task, the source environment ID is sent to the API, and for load task the target environment ID is sent to the API.")
    @webService(path = "businessentity/{beId}/environment/{envId}/logicalunits", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                    {
              "result": [
                {
                  "env_max_number_of_workers": null,
                  "lu_parent_name": null,
                  "lu_parent_id": null,
                  "lu_name": "Customer",
                  "lu_id": 1,
                  "env_affinity": "",
                  "product_name": "CRM"
                },
                {
                  "env_max_number_of_workers": null,
                  "lu_parent_name": "Customer",
                  "lu_parent_id": 1,
                  "lu_name": "Billing",
                  "lu_id": 2,
                  "env_affinity": "",
                  "product_name": "Billing"
                }
              ],
              "errorCode": "SUCCESS",
              "message": null
            }
                    """)
    public static Object wsGetLogicalUnitsByEnvironmentAndBusinessentity(@param(required = true) Long beId,
            @param(required = true) Long envId) throws Exception {

        return fnGetLogicalUnitsByEnvironmentAndBusinessentity(beId, envId);
    }

    /**
     * Performs mandatory validation checks on environments, BE, sync mode,
     * entity count, and logical unit constraints before starting the transaction.
     *
     * @return An error message string, or an empty string ("") if all validations
     *         pass.
     */
    private static String validateBeforeSave(
            Long be_id, Long environment_id, Long source_environment_id,
            String task_override_fields, List<Map<String, Object>> preExecutionProcesses,
            List<Map<String, Object>> postExecutionProcesses, String task_type,
            String source_env_name, String env_name, String sync_mode, String selection_method,
            Integer num_of_entities, List<Map<String, Object>> logicalUnits, String params, String custom_logic_lu_name,
            String selection_param_value, String retention_period_type, List<Map<String, Object>> tableList,
            Boolean version_ind, Integer selected_version_task_exe_id) throws Exception {

        String msg;

        // 0. Validate environment types for user
        String currentUser = sessionUser().name();
        boolean sourceRequired = requiresSourceCheck(task_type, sync_mode, String.valueOf(source_environment_id));
		boolean targetRequired = requiresTargetCheck(task_type,String.valueOf(environment_id));
        if (sourceRequired) {
            msg = validateEnvTypeForUser(source_environment_id, source_env_name, "SOURCE", currentUser);
            if (!"".equals(msg)) return msg;
        }
        if (targetRequired) {
            msg = validateEnvTypeForUser(environment_id, env_name, "TARGET", currentUser);
            if (!"".equals(msg)) return msg;
        }

        // 1. Validate Environments and Business Entity
        msg = validateEnvironmentsAndBusinessEntity(
                be_id, environment_id, source_environment_id, task_override_fields,
                preExecutionProcesses, postExecutionProcesses, task_type);
        if (!"".equalsIgnoreCase(msg)) {
            return msg;
        }

        // 2. Validate Override Sync Mode
        msg = fnValidateOverrideSyncMode(source_environment_id, source_env_name, sync_mode);
        if (!"".equalsIgnoreCase(msg)) {
            return msg;
        }

        // 3. Validate table level extract with no retention
        msg = fnValidateExtractNoRetention(task_type, selection_method, retention_period_type);
        if (!"".equalsIgnoreCase(msg)) {
            return msg;
        }

        // 3a. Validate selected version fetch data policy is not supported for table-level extract tasks
        if ("EXTRACT".equalsIgnoreCase(task_type) && TABLES.equals(selection_method)
                && Boolean.TRUE.equals(version_ind) && "OFF".equalsIgnoreCase(sync_mode)) {
            return "Selected version fetch data policy is not supported for table-level extract tasks.";
        }

        // 4. Validate mandatory num_of_entities for specific LOAD tasks
        if ("LOAD".equals(task_type) && !"ALL".equals(selection_method) && !TABLES.equals(selection_method)
                && num_of_entities == null) {
            // Return a specific message that can be checked for throwing the
            // IllegalArgumentException
            return "In case the task_type is \"LOAD\" and the selection_method is not 'ALL' or 'TABLES' the parameter 'num_of_entities' is mandatory.";
        }

        // 5. Validate Logical Unit count for AI-based tasks
        if (("TRAINING".equalsIgnoreCase(task_type) || "AI_GENERATED".equalsIgnoreCase(selection_method) ||
                ("GENERATE_SUBSET".equalsIgnoreCase(selection_method) && ("AI".equalsIgnoreCase(source_env_name))))
                && (logicalUnits.size() > 1)) {
            return "AI-based training or generation tasks can run on one LU only";
        }

        if ("TRAINING".equalsIgnoreCase(task_type) && "TABLES".equalsIgnoreCase(selection_method)) {
            return "AI-based training is not supported for Table-Level tasks";
        }

        // 6. Validate BE is required when reference tables are selected
        if (be_id == null && tableList != null && !tableList.isEmpty()) {
            return "Business Entity (BE) must be selected when reference tables are included in the task.";
        }

        // 7. Validate no duplicate tables in tableList
        if (tableList != null && !tableList.isEmpty()) {
            Set<String> seen = new HashSet<>();
            for (Map<String, Object> table : tableList) {
                String key = table.get("interface_name") + "|" +
                             table.get("schema_name")    + "|" +
                             table.get("reference_table_name");
                if (!seen.add(key)) {
                    return "Duplicate table detected in the table list: interface=\"" +
                           table.get("interface_name") + "\", schema=\"" +
                           table.get("schema_name")    + "\", table=\"" +
                           table.get("reference_table_name") + "\".";
                }
            }
        }

        if (params != null && !params.isEmpty()) {
            msg = validateParams(selection_method, params, custom_logic_lu_name, selection_param_value);
            if (!"".equalsIgnoreCase(msg)) {
                return msg;
            }
        }

        // 8. Validate version selection when data_version_name is locked at runtime,
        // ignore on table level
        if (Boolean.TRUE.equals(version_ind) && "OFF".equalsIgnoreCase(sync_mode) && !TABLES.equals(selection_method)) {
            boolean hasOverrideConfig = task_override_fields != null
                    && !task_override_fields.isBlank()
                    && !task_override_fields.equals("{}");
            if (hasOverrideConfig) {
                JsonObject overrideJson = JsonParser.parseString(task_override_fields).getAsJsonObject();
                JsonObject dataVersionObj = overrideJson.has("data_version_name")
                        ? overrideJson.getAsJsonObject("data_version_name")
                        : null;
                if (dataVersionObj != null) {
                    boolean isDataVersionEditable = dataVersionObj.get("is_editable").getAsBoolean();
                    if (!isDataVersionEditable &&
                            (selected_version_task_exe_id == null || selected_version_task_exe_id == 0)) {
                        return "Data version selection is mandatory.";
                    }
                }
            }
        }

        // 9. Validate selected data version has not expired
        if (selected_version_task_exe_id != null && selected_version_task_exe_id > 0) {
            msg = fnValidateVersionExecIdNotExpired(selected_version_task_exe_id.longValue());
            if (!"".equals(msg)) return msg;
        }

        return "";
    }

    @desc("""
            Create a task in one transaction. Rollback the transaction in case of a failure.

            The following input parameters are mandatory:
            - be_id: populated by the ID of the task's Business Entity (BE)

            - environment_id:
              - Extract task: populated by the source environment id.
              - Load task: populated by the target environment id.

            - source_environment_id: source environment ID.

            - selection_method: populated by the following values:
              - Extract task: 'L' (entity list), 'TABLES' (reference only task), or 'ALL' (all entities).
              - Load task: 'L' (entity list), 'ALL', 'P' (Parameters), 'PR' (Parameters with random selection), or 'R' (Random).

            - task_title: task name

            - task_type: populated by 'EXTRACT' or 'LOAD'

            - taskGroupIds: the task group ids for mapping.

            - logicalUnits: populated by the list of the task's logical units (LUs).

            Example of a request body:

            {
                "be_id": 3,
                "environment_id": 1,
                "source_environment_id": 1,
                "scheduler": "immediate",
                "delete_before_load": true,
                "refresh_reference_data": true,
                "num_of_entities": 0,
                "selection_method": "R",
                "selection_param_value": null,
                "task_title": "taskTitle",
                "parameters": null,
                "replace_sequences": true,
                "source_env_name": "SRC",
                "env_name": "TAR",
                "load_entity": true,
                "task_type": "LOAD",
                "taskGroupIds": [1],
                "scheduling_end_date": "2021-02-04 14:20:59.454",
                "version_ind": true,
                "retention_period_type": "Days",
                "retention_period_value": 5,
                "selected_version_task_exe_id": 0,
                "task_globals": true,
                "selected_ref_version_task_exe_id": 0,
                "sync_mode": null,
                "evaluation_ind": false,
                "in_place_masking_ind": false,
                "statistics_report_flag": "FAILED",
                "enable_sequence_report": false,
                "mask_sensitive_data": false,
                "task_description": "My task description",
                "clone_ind": false,
                "execution_mode": null,
                "custom_logic_lu_name": null,
                "selected_subset_task_exe_id": null,
                "filterout_reserved": "false",
                "reserve_ind": false,
                "reserve_retention_period_type": "Days",
                "reserve_retention_period_value": 5,
                "reserve_note": null,
                "generateParams": null,
                "task_override_fields": "{\\\\"business_entity\\\\":{\\\\"field_connector\\\\":\\\\"be_name\\\\",\\\\"is_editable\\":false},\\\\"source_environment\\\\":{\\\\"field_connector\\\\":\\\\"source_env_name\\\\",\\\\"is_editable\\":false},\\\\"task_globals\\\\":{\\\\"field_connector\\\\":\\\\"task_globals\\\\",\\\\"is_editable\\":false},\\\\"target_environment\\\\":{\\\\"field_connector\\\\":\\\\"environment_name\\\\",\\\\"is_editable\\":false},\\\\"reservation_period\\\\":{\\\\"field_connector\\\\":\\\\"reservation_period\\\\",\\\\"is_editable\\":false},\\\\"retention_period\\\\":{\\\\"field_connector\\\\":\\\\"retention_period\\\\",\\\\"is_editable\\":false},\\\\"data_version_name\\\\":{\\\\"field_connector\\\\":\\\\"data_version_name\\\\",\\\\"is_editable\\":false},\\\\"selection_method\\\\":{\\\\"field_connector\\\\":\\\\"selection_method\\\\",\\\\"max_entities\\\\":{\\\\"is_editable\\":false},\\\\"is_editable\\":false,\\\\"entity_list\\\\":{\\\\"is_editable\\":false},\\\\"custom_logic\\\\":{\\\\"is_editable\\":false,\\\\"can_add_params\\\\":{\\\\"is_editable\\":false}},\\\\"random\\\\":{\\\\"is_editable\\":false},\\\\"business_parameters\\\\":{\\\\"is_editable\\":false}}}",
                "tableList": [{
                        "reference_table_name": "DEVICES_TABLE",
                        "lu_name": "Customer",
                        "schema_name": "public",
                        "interface_name": "CRM_DB",
                        "table_filter": null,
                        "gui_filter": null,
                        "filter_type": null,
                        "count_ind": false,
                        "source_affinity": null,
                        "target_affinity": null,
                        "source_max_no_of_workers": null,
                        "target_max_no_of_workers": null
                    }
                ],
                "globals": [{
                        "lu_name": "ALL",
                        "global_name": "MASK_FLAG",
                        "global_value": "false",
                        "is_editable": true
                    }, {
                        "lu_name": "Customer",
                        "global_name": "CUST_TYPE",
                        "global_value": "BUS",
                        "is_editable": false
                    }
                ],
                "reference": "ref",
                "preExecutionProcesses": [{
                        "process_id": 1,
                        "process_name": "preProcessName",
                        "lu_name": "luName1",
                        "execution_order": 1,
                        "is_editable": true,
                        "parameters": {
                            "inputs": [{"name": "param1", "value": "value1"}],
                            "is_editable": true
                        }
                    }
                ],
                "postExecutionProcesses": [{
                        "process_id": 2,
                        "process_name": "postProcessName",
                        "lu_name": "luName2",
                        "execution_order": 2,
                        "is_editable": true,
                        "parameters": {
                            "inputs": [{"name": "param1", "value": "value1"}],
                            "is_editable": true
                        }
                    }
                ],
                "permissions": [{"type": "ID", "value": "tester1"}, {"type": "GROUP", "value": "qa-team"}],
                "notes": [{"note_title": "Note 1", "note_description": "Some note", "note_date": "2025-01-01 00:00:00"}],
                "logicalUnits": [{
                        "lu_id": 7,
                        "lu_name": "Customer",
                        "source_affinity": "DC1",
                        "target_affinity": "DC2",
                        "source_max_no_of_workers": 4,
                        "target_max_no_of_workers": 4,
                        "override_fields": "{\\\\"source_affinity\\\\":true,\\\\"target_affinity\\\\":true,\\\\"max_no_of_workers\\\\":true}"
                    }, {
                        "lu_id": 9,
                        "lu_name": "Billing",
                        "source_affinity": null,
                        "target_affinity": null,
                        "source_max_no_of_workers": null,
                        "target_max_no_of_workers": null,
                        "override_fields": "{\\\\"source_affinity\\\\":false,\\\\"target_affinity\\\\":false,\\\\"max_no_of_workers\\\\":false}"
                    }
                ]
            }
            """)
    @webService(path = "task", verb = {MethodType.POST}, version = "2", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": {\r\n" + "    \"id\": 145\r\n" + "  },\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsCreateTaskV2(Long be_id, String env_name, Long environment_id,
            Long source_environment_id, String scheduler, Boolean delete_before_load, Integer num_of_entities,
            String selection_method, String selection_param_value, @param(required = true) String task_title,
            String parameters, Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name,
            Boolean load_entity, @param(required = true) String task_type, String scheduling_end_date,
            Boolean version_ind, String retention_period_type, Integer retention_period_value,
            Integer selected_version_task_exe_id, Boolean task_globals, Integer selected_ref_version_task_exe_id,
            String sync_mode, List<Map<String, Object>> tableList, List<Map<String, Object>> globals, String reference,
            List<Map<String, Object>> preExecutionProcesses, List<Map<String, Object>> postExecutionProcesses,
            @param(required = true) List<Map<String, Object>> logicalUnits, Boolean reserve_ind,
            String reserve_retention_period_type, Integer reserve_retention_period_value, String reserve_note,
            String filterout_reserved, HashMap<String, Object> generateParams, Boolean mask_sensitive_data,
            String task_description, String custom_logic_lu_name, Long selected_subset_task_exe_id, Boolean clone_ind,
            String execution_mode, boolean evaluation_ind, boolean in_place_masking_ind,
            List<Long> taskGroupIds, String task_override_fields,
            Boolean enable_sequence_report, String statistics_report_flag, List<Map<String, String>> permissions,
            List<Map<String, Object>> notes) throws Exception {

        Long taskId;

        boolean hasOverrideConfig = task_override_fields != null
                && !task_override_fields.isBlank()
                && !task_override_fields.equals("{}");

        if (in_place_masking_ind) {
            if (source_environment_id != null) {
                environment_id = source_environment_id;
                env_name = source_env_name;
            }
            if (hasOverrideConfig) {
                JsonObject jsonObject = JsonParser.parseString(task_override_fields).getAsJsonObject();
                boolean isSourceEnvEditable = jsonObject.getAsJsonObject("source_environment").get("is_editable")
                        .getAsBoolean();
                jsonObject.getAsJsonObject("target_environment").addProperty("is_editable", isSourceEnvEditable);
                task_override_fields = jsonObject.toString();
            }
        }

        if (clone_ind == null) {
            clone_ind = false;
        }
        if (reserve_ind == null) {
            reserve_ind = false;
        }
        if (mask_sensitive_data == null) {
            mask_sensitive_data = false;
        }
        if (delete_before_load == null) {
            delete_before_load = false;
        }
        if (sync_mode == null) {
            sync_mode = "OFF";
        }
        if (taskGroupIds == null || taskGroupIds.isEmpty()) {
            taskGroupIds = Arrays.asList(1L);
        }
        if (task_override_fields == null) {
            task_override_fields = "{}";
        }
        if (permissions == null) {
            permissions = Arrays.asList(Map.of("type", "ID", "value", "ALL"));
        }
        if (enable_sequence_report == null) {
            enable_sequence_report = true;
        }
        if (statistics_report_flag == null) {
            statistics_report_flag = "ALL";
        }

        if ("TRAINING".equalsIgnoreCase(task_type)
                && (SYNTHETIC_ENVIRONMENT.equals(source_env_name) || AI_ENVIRONMENT.equals(source_env_name))
                && "OFF".equalsIgnoreCase(sync_mode)) {
            selection_method = "GENERATE_SUBSET";
        } else if ("TRAINING".equalsIgnoreCase(task_type) && SYNTHETIC_ENVIRONMENT.equals(source_env_name)) {
            selection_method = "GENERATE";
        }

        if(source_environment_id != null && environment_id != null && source_environment_id.equals(environment_id)
            && "load".equalsIgnoreCase(task_type) && (Boolean) delete_before_load && (Boolean) version_ind == false) {
                return wrapWebServiceResults("FAILED",
                    "For Task with Delete, the source and target Environments cannot be the same",
                    null);
        }

        String msg = validateBeforeSave(
                be_id, environment_id, source_environment_id, task_override_fields,
                preExecutionProcesses, postExecutionProcesses, task_type,
                source_env_name, env_name, sync_mode, selection_method,
                num_of_entities, logicalUnits, parameters, custom_logic_lu_name, selection_param_value,
                retention_period_type, tableList,
                version_ind, selected_version_task_exe_id);

        if (!"".equalsIgnoreCase(msg)) {
            return wrapWebServiceResults("FAILED", msg, null);
        }

        if (tableList != null && !tableList.isEmpty() && !"OFF".equalsIgnoreCase(sync_mode)) {
            refresh_reference_data = true;
        }
        
        String userName = sessionUser().name();

        boolean canCreateTask = canUserPerformTaskOperation(
                userName,
                source_environment_id == null ? null : String.valueOf(source_environment_id),
                environment_id == null ? null : String.valueOf(environment_id),
                task_type,
                sync_mode,
                reserve_ind != null && reserve_ind,
                delete_before_load != null && delete_before_load,
                replace_sequences != null && replace_sequences,
                clone_ind != null && clone_ind,
                refresh_reference_data != null && refresh_reference_data,
                version_ind != null && version_ind,
                scheduler,
                selection_method,
                num_of_entities == null ? -1 : num_of_entities.longValue(),
                "CREATE",true,true,null 
        );

        if (!canCreateTask) {
            return wrapWebServiceResults(
                    "FAILED",
                    "User " + userName + " is not allowed to create this task based on the assigned permissions.",
                    null
            );
        }
        handlePreAndPostExecution(task_type, selection_method, evaluation_ind, preExecutionProcesses,
                postExecutionProcesses);

        db(TDM).beginTransaction();
        //try {
            Map<String, Object> result = (Map<String, Object>) wsCreateTaskV1(be_id, environment_id, source_environment_id, 
                    scheduler, delete_before_load, num_of_entities, selection_method, selection_param_value, task_title,
                    parameters, refresh_reference_data, replace_sequences, source_env_name, load_entity, task_type,
                    scheduling_end_date, version_ind, retention_period_type, retention_period_value,
                    selected_version_task_exe_id, task_globals, selected_ref_version_task_exe_id, sync_mode, tableList, globals,
                    reference, reserve_ind, reserve_retention_period_type, reserve_retention_period_value, reserve_note, 
                    filterout_reserved, generateParams, mask_sensitive_data, task_description, custom_logic_lu_name,
                    selected_subset_task_exe_id, clone_ind, execution_mode, in_place_masking_ind, task_override_fields, enable_sequence_report, statistics_report_flag, env_name);
            if (!checkWsResponse(result)) {
                db(TDM).rollback();
                return wrapWebServiceResults("FAILED", result.get("message"), null);
            } else {
                taskId = (Long) ((Map<String, Object>) result.get("result")).get("id");
            }
			result = (Map<String, Object>) wsCreatePreExecutionProcessesFortask(taskId, task_title, preExecutionProcesses);
            if (!checkWsResponse(result)) {
                db(TDM).rollback();
                return wrapWebServiceResults("FAILED", "Can't create pre execution processes for the task: " + result.get("message"), null);
            }
			result = (Map<String, Object>) wsCreatePostExecutionProcessesFortask(taskId, task_title, postExecutionProcesses);
			if (!checkWsResponse(result)) {
                db(TDM).rollback();
                return wrapWebServiceResults("FAILED", "Can't create post execution processes for the task: " + result.get("message"), null);
            }
            
            if (be_id != null && !"TABLES".equalsIgnoreCase(selection_method)) {
                Boolean validLus = fnValidateBELogicalUnits(be_id, logicalUnits);
                if (validLus) {

                    result = (Map<String, Object>) wsCreateLogicalUnitsFortask(taskId, logicalUnits);

                    if (!checkWsResponse(result)) {
                        db(TDM).rollback();
                        return wrapWebServiceResults("FAILED", result.get("message"), null);
                    }

                } else {
                    db(TDM).rollback();
                    return wrapWebServiceResults("FAILED",
                            "Logical Unit validation failed. Ensure that all selected Logical Units have their respective parents selected.",
                            null);
                }
            }
            if (source_environment_id != null) {
                // check for any disabled systems of source environment
                String inactive_source_products = fnValidateProductForTask(String.valueOf(source_environment_id),
                        source_env_name, task_type, sync_mode, "SOURCE", taskId);
                if (!"".equalsIgnoreCase(inactive_source_products)) {
                    db(TDM).rollback();
                    return wrapWebServiceResults("FAILED",
                            "The task cannot be created. The following systems are currently disabled in "
                                    + source_env_name + ": " + inactive_source_products,
                            null);
                }
            }

            if (environment_id != null) {
                // check for any disabled systems of target environment
                String inactive_target_products = fnValidateProductForTask(String.valueOf(environment_id),
                        env_name, task_type, sync_mode, "TARGET", taskId);
                if (!"".equalsIgnoreCase(inactive_target_products)) {
                    db(TDM).rollback();
                    return wrapWebServiceResults("FAILED",
                            "The task cannot be created. The following systems are currently disabled in "
                                    + env_name + ": " + inactive_target_products,
                            null);
                }
            }
            
            result = (Map<String, Object>) wsAssignTaskToTaskGroups(taskId, taskGroupIds, false);
            if (!checkWsResponse(result)) {
                db(TDM).rollback();
                return wrapWebServiceResults("FAILED", "Can't assign the task to group: " + result.get("message"),
                        null);
            }
            result = (Map<String, Object>) wsInsertTaskExecution(taskId, permissions);
            if (!checkWsResponse(result)) {
                db(TDM).rollback();
                return wrapWebServiceResults("FAILED", "Can't add the task execution permissions: " + result.get("message"),
                        null);
            }

            if (notes != null && !notes.isEmpty()) {
                result = (Map<String, Object>) wsCreateTaskNotes(taskId, notes);
                if (!checkWsResponse(result)) {
                    db(TDM).rollback();
                    return wrapWebServiceResults("FAILED", "Can't create notes for the task: " + result.get("message"),
                            null);
                }
            }
            
        //} catch (Exception e) {
        //    db(TDM).rollback();
       //     return wrapWebServiceResults("FAILED", e.getMessage(), null);
        //}

        db(TDM).commit();

        //Map<String, Object> result = new HashMap();
        result.put("id", taskId);
        return wrapWebServiceResults("SUCCESS", null, result);
    }

    @desc("Creates Task.\r\n" + "\r\n" + "Example of a request body:\r\n" + "\r\n" + "{\r\n" + "   \"filterout_reserved\":false,\r\n" + "   \"operationMode\":\"insert_entity_without_delete\",\r\n" + "   \"task_type\":\"LOAD\",\r\n" + "   \"task_title\":\"testapi2\",\r\n" + "   \"extractSelected\":false,\r\n" + "   \"load_entity\":true,\r\n" + "   \"delete_before_load\":false,\r\n" + "   \"reserve_ind\":true,\r\n" + "   \"be_id\":4,\r\n" + "   \"source_environment_id\":1,\r\n" + "   \"environment_id\":2,\r\n" + "   \"source_env_name\":\"SRC\",\r\n" + "   \"environment_name\":\"TAR\",\r\n" + "   \"allLogicalUnits\":[\r\n" + "      {\r\n" + "         \"lu_parent_name\":null,\r\n" + "         \"lu_name\":\"Customer\",\r\n" + "         \"lu_id\":7,\r\n" + "         \"product_name\":\"CRM\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Billing\",\r\n" + "         \"lu_id\":9,\r\n" + "         \"product_name\":\"FINANCE\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Collection\",\r\n" + "         \"lu_id\":10,\r\n" + "         \"product_name\":\"FINANCE\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Orders\",\r\n" + "         \"lu_id\":8,\r\n" + "         \"product_name\":\"ORDERS\"\r\n" + "      }\r\n" + "   ],\r\n" + "   \"selectedLogicalUnits\":[\r\n" + "      {\r\n" + "         \"lu_parent_name\":null,\r\n" + "         \"lu_name\":\"Customer\",\r\n" + "         \"lu_id\":7,\r\n" + "         \"product_name\":\"CRM\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Billing\",\r\n" + "         \"lu_id\":9,\r\n" + "         \"product_name\":\"FINANCE\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Collection\",\r\n" + "         \"lu_id\":10,\r\n" + "         \"product_name\":\"FINANCE\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Orders\",\r\n" + "         \"lu_id\":8,\r\n" + "         \"product_name\":\"ORDERS\"\r\n" + "      }\r\n" + "   ],\r\n" + "   \"missingRootLU\":[\r\n" + "      \r\n" + "   ],\r\n" + "   \"num_of_entities\":2,\r\n" + "   \"syncModeRadio\":null,\r\n" + "   \"sync_mode\":null,\r\n" + "   \"reserve_retention_period_value\":5,\r\n" + "   \"reserve_retention_period_type\":\"Days\",\r\n" + "   \"allPostExecutionProcess\":[\r\n" + "      {\r\n" + "         \"process_id\":1,\r\n" + "         \"be_id\":4,\r\n" + "         \"process_name\":\"PostExecFlow\",\r\n" + "         \"process_description\":null,\r\n" + "         \"execution_order\":1\r\n" + "      }\r\n" + "   ],\r\n" + "   \"postExecutionProcesses\":[\r\n" + "      1\r\n" + "   ],\r\n" + "   \"reference\":\"both\",\r\n" + "   \"versionsForLoad\":[\r\n" + "      \r\n" + "   ],\r\n" + "   \"selection_method\":\"PR\",\r\n" + "   \"selection_param_value\":\"(( 'Bronze' = ANY(\\\"BILLING.VIP_STATUS\\\") ))\",\r\n" + "   \"parameters\":\"{\\\"group\\\":{\\\"rules\\\":[{\\\"condition\\\":\\\"=\\\",\\\"field\\\":\\\"BILLING.VIP_STATUS\\\",\\\"data\\\":\\\"Bronze\\\",\\\"operator\\\":\\\"AND\\\",\\\"type\\\":\\\"text\\\",\\\"comboIndicator\\\":\\\"true\\\",\\\"validValues\\\":[\\\"Bronze\\\",\\\"Gold\\\",\\\"Platinum\\\",\\\"Silver\\\"],\\\"disableThird\\\":false}]}}\",\r\n" + "   \"refList\":[\r\n" + "      {\r\n" + "         \"ref_table_name\":\"DEVICESTABLE2017\",\r\n" + "         \"lu_name\":\"Customer\",\r\n" + "         \"interface_name\":\"CRM_DB\",\r\n" + "         \"schema_name\":\"public\",\r\n" + "         \"logical_unit_name\":\"Customer\",\r\n" + "         \"reference_table_name\":\"DEVICESTABLE2017\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"ref_table_name\":\"devicestable2017\",\r\n" + "         \"lu_name\":\"Customer\",\r\n" + "         \"interface_name\":\"CRM_DB\",\r\n" + "         \"schema_name\":\"public\",\r\n" + "         \"logical_unit_name\":\"Customer\",\r\n" + "         \"reference_table_name\":\"devicestable2017\",\r\n" + "         \"selected\":true\r\n" + "      }\r\n" + "   ],\r\n" + "   \"refresh_reference_data\":false,\r\n" + "   \"selected_version_task_exe_id\":null,\r\n" + "   \"selected_ref_version_task_exe_id\":null,\r\n" + "   \"scheduler\":\"immediate\"\r\n" + "}")
    @webService(path = "task", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": {\r\n" + "    \"id\": 145\r\n" + "  },\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsCreateTaskV1(Long be_id, Long environment_id,
            Long source_environment_id, String scheduler, Boolean delete_before_load, Integer num_of_entities,
            String selection_method, String selection_param_value, @param(required = true) String task_title,
            String parameters, Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name,
            Boolean load_entity, @param(required = true) String task_type, String scheduling_end_date,
            Boolean version_ind, String retention_period_type, Integer retention_period_value,
            Integer selected_version_task_exe_id, Boolean task_globals, Integer selected_ref_version_task_exe_id,
            String sync_mode, List<Map<String, Object>> tableList, List<Map<String, Object>> globals,
            String reference, Boolean reserve_ind, String reserve_retention_period_type,
            Integer reserve_retention_period_value, String reserve_note, String filterout_reserved,
            HashMap<String, Object> generateParams, Boolean mask_sensitive_data, String task_description,
            String custom_logic_lu_name, Long selected_subset_task_exe_id,Boolean clone_ind, 
            String execution_mode, boolean in_place_masking_ind, String editable_params, Boolean enable_sequence_report, String statistics_report_flag, String env_name) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        Map<String, Object> result = new HashMap<>();

        if (TABLES.equals(selection_method) && (tableList == null || tableList.isEmpty())) {
            response.put("errorCode", "FAILED");
            response.put("message", "Cannot create Tables Only Task without any table selected.");
            return response;
        }
        selected_ref_version_task_exe_id = selected_ref_version_task_exe_id != null ? selected_ref_version_task_exe_id : 0;
        selected_version_task_exe_id = selected_version_task_exe_id != null ? selected_version_task_exe_id : 0;
        selected_subset_task_exe_id = selected_subset_task_exe_id != null ? selected_subset_task_exe_id : 0;

        if (reference != null && reference.equals("refernceOnly")) {
            if (tableList != null) {
                if (tableList.size() > 0) {
                    selection_method = TABLES;
                    delete_before_load = false;
                    load_entity = false;
                }
            }
            num_of_entities = 0;
        } else {
            if (num_of_entities == null || num_of_entities == 0) {
                if (selection_param_value != null && !"".equals(selection_param_value)) {
                    num_of_entities = (selection_param_value.split(",")).length;
                }
            }
        }

    
        String sql = "INSERT INTO " + TDMDB_SCHEMA + ".tasks (be_id, environment_id, env_name, scheduler, delete_before_load," +
                "num_of_entities,selection_method,selection_param_value, custom_logic_lu_name, task_execution_status, " +
                "task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, task_status, task_title, parameters, refresh_reference_data,replace_sequences, " +
                "source_environment_id, source_env_name, load_entity, task_type, scheduling_end_date, version_ind, retention_period_type, retention_period_value, " +
                "selected_version_task_exe_id,selected_subset_task_exe_id,task_globals, selected_ref_version_task_exe_id, sync_mode, reserve_ind, " +
                "reserve_retention_period_type, reserve_retention_period_value, reserve_note, filterout_reserved,task_description,clone_ind, " + 
                "execution_mode, in_place_masking_ind, permission_group, fabric_roles, task_override_fields, enable_sequence_report, statistics_report_flag) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING task_id";
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC).format(Instant.now());
        String username = sessionUser().name();
        Set<String> tmpRoles = new HashSet<>();
        for(String role :sessionUser().roles()){
            if(!("Everybody".equalsIgnoreCase(role))){
                tmpRoles.add(role);}
        }
        String fabricRoles = String.join(TDM_PARAMETERS_SEPARATOR, tmpRoles);
        String createdBy = username + "##" + String.join(TDM_PARAMETERS_SEPARATOR, tmpRoles);
        String permessionGroup = fnGetUserPermissionGroup(username);
        if ("".equalsIgnoreCase(retention_period_type + "") || "null".equalsIgnoreCase(retention_period_type + "")) {
            retention_period_type = "Do Not Delete";
        }

        if ("Do Not Delete".equalsIgnoreCase(retention_period_type)) {
            retention_period_value = -1;
        }
        
        if (sync_mode == null) {
            sync_mode = "OFF";
        }
        
        Long taskId = null;
        try {
            Db.Row row = db(TDM).fetch(sql, be_id, environment_id, env_name, scheduler, ((delete_before_load != null) ? delete_before_load : false), num_of_entities, selection_method,
                    selection_param_value, custom_logic_lu_name, "Active", createdBy, now, now, username, "Active", task_title, parameters, refresh_reference_data, replace_sequences, source_environment_id,
                    ((source_env_name != null) ? source_env_name : ""), ((load_entity != null) ? load_entity : false), task_type, scheduling_end_date,
                    (version_ind != null ? version_ind == true : false), retention_period_type, retention_period_value, selected_version_task_exe_id,selected_subset_task_exe_id,
                    task_globals, selected_ref_version_task_exe_id, sync_mode, reserve_ind, reserve_retention_period_type, reserve_retention_period_value,
                    reserve_note, filterout_reserved, task_description, clone_ind, execution_mode, in_place_masking_ind, permessionGroup, fabricRoles, editable_params,
                    enable_sequence_report, statistics_report_flag).firstRow();

            taskId = Long.parseLong(row.get("task_id").toString());
        } catch (SQLException e) {
            message = e.getMessage();
            if (message.contains("duplicate key value violates unique constraint")) {
                message = "Another task already uses this name. Click the Task Name field to enter a unique name.";
            }
            e.printStackTrace();
            errorCode = "FAILED";
            response.put("errorCode", errorCode);
            response.put("message", message);
            return response;
        }
            

            if (tableList != null) {
                if (tableList.size() > 0) {
                    String msg = validateMaxWorkers(tableList);
                    if (!"".equalsIgnoreCase(msg)) {
                        return wrapWebServiceResults("FAILED", msg, null);
                    }
                    fnSaveRefTablestoTask(taskId, tableList, in_place_masking_ind);
                }
            }

            if (globals != null) {
                if (globals.size() > 0 && task_globals != null && task_globals) {

                    try {
                        for (Map<String, Object> global : globals) {
                            boolean isEditable = Boolean.TRUE.equals(global.get("is_editable"));
                            createTaskGlobals(taskId, global.get("lu_name").toString(), global.get("global_name").toString(), global.get("global_value").toString(), isEditable);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }

            // TDM 8.0 - In case of Synthetic task, add the input parameters of the population flows to the TDMDB tdm_generate_task_field_mappings
            //log.info("wsCreateTaskV1 - params size: " + DataManParams.size());
            if (source_environment_id != null && source_environment_id == -1 && generateParams != null) {
                try {
                    createTaskGEnerateParams(taskId, generateParams);
                } catch (Exception e) {
                    message = e.getMessage();
                    errorCode = "FAILED";
                    response.put("errorCode", errorCode);
                    response.put("message", message);
                    return response;
                }
            }

            String activityDesc = "Task " + task_title + " was created";
            fnInsertActivity("create", "Tasks", activityDesc);

            result.put("id", taskId);
            errorCode = "SUCCESS";
            response.put("result", result);

        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("""
            Updates an existing Task identified by {taskId}. This operation creates a new version of the task and sets the status of the previous version to Inactive only if the system configuration parameter 'CREATE_NEW_TASK_VERSION_ON_UPDATE' is set to true in the TDM_GENERAL_PARAMETERS table. If the parameter is false, the existing task record is updated directly.

            Example of a request body:
            {
              "copy": false,
              "task_status": "complete",
              "be_id": 3,
              "environment_id": 1,
              "source_environment_id": 1,
              "scheduler": "immediate",
              "delete_before_load": true,
              "refresh_reference_data": true,
              "num_of_entities": 0,
              "selection_method": "R",
              "selection_param_value": null,
              "task_title": "taskTitle",
              "parameters": null,
              "replace_sequences": true,
              "source_env_name": "SRC",
              "env_name": "TAR",
              "load_entity": true,
              "task_type": "LOAD",
              "taskGroupIds": [1],
              "scheduling_end_date": "2021-02-04 14:20:59.454",
              "version_ind": true,
              "retention_period_type": "Days",
              "retention_period_value": 0,
              "selected_version_task_exe_id": 0,
              "task_globals": true,
              "selected_ref_version_task_exe_id": 0,
              "sync_mode": null,
              "evaluation_ind": false,
              "in_place_masking_ind": false,
              "statistics_report_flag": "FAILED",
              "enable_sequence_report": false,
              "mask_sensitive_data": false,
              "task_description": "My task description",
              "clone_ind": false,
              "execution_mode": null,
              "custom_logic_lu_name": null,
              "selected_subset_task_exe_id": null,
              "filterout_reserved": "false",
              "reserve_ind": false,
              "reserve_retention_period_type": "Days",
              "reserve_retention_period_value": 5,
              "reserve_note": null,
              "generateParams": null,
              "task_override_fields": "{\\\\"business_entity\\\\":{\\\\"field_connector\\\\":\\\\"be_name\\\\",\\\\"is_editable\\":false},\\\\"source_environment\\\\":{\\\\"field_connector\\\\":\\\\"source_env_name\\\\",\\\\"is_editable\\":false},\\\\"task_globals\\\\":{\\\\"field_connector\\\\":\\\\"task_globals\\\\",\\\\"is_editable\\":false},\\\\"target_environment\\\\":{\\\\"field_connector\\\\":\\\\"environment_name\\\\",\\\\"is_editable\\":false},\\\\"reservation_period\\\\":{\\\\"field_connector\\\\":\\\\"reservation_period\\\\",\\\\"is_editable\\":false},\\\\"retention_period\\\\":{\\\\"field_connector\\\\":\\\\"retention_period\\\\",\\\\"is_editable\\":false},\\\\"data_version_name\\\\":{\\\\"field_connector\\\\":\\\\"data_version_name\\\\",\\\\"is_editable\\":false},\\\\"selection_method\\\\":{\\\\"field_connector\\\\":\\\\"selection_method\\\\",\\\\"max_entities\\\\":{\\\\"is_editable\\":false},\\\\"is_editable\\":false,\\\\"entity_list\\\\":{\\\\"is_editable\\":false},\\\\"custom_logic\\\\":{\\\\"is_editable\\":false,\\\\"can_add_params\\\\":{\\\\"is_editable\\":false}},\\\\"random\\\\":{\\\\"is_editable\\":false},\\\\"business_parameters\\\\":{\\\\"is_editable\\":false}}}",
              "tableList": [
                {
                  "reference_table_name": "DEVICES_TABLE",
                  "lu_name": "Customer",
                  "schema_name": "public",
                  "interface_name": "CRM_DB",
                  "table_filter": null,
                  "gui_filter": null,
                  "filter_type": null,
                  "count_ind": false,
                  "source_affinity": null,
                  "target_affinity": null,
                  "source_max_no_of_workers": null,
                  "target_max_no_of_workers": null
                }
              ],
              "globals": [
                {
                  "lu_name": "ALL",
                  "global_name": "MASK_FLAG",
                  "global_value": "false",
                  "is_editable": true
                },
                {
                  "lu_name": "Customer",
                  "global_name": "CUST_TYPE",
                  "global_value": "BUS",
                  "is_editable": false
                }
              ],
              "reference": "ref",
              "task_created_by": "test",
              "preExecutionProcesses": [{
                  "process_id": 1,
                  "process_name": "preProcessName",
                  "lu_name": "luName1",
                  "execution_order": 1,
                  "is_editable": true,
                  "parameters": {
                    "inputs": [{"name": "param1", "value": "value1"}],
                    "is_editable": true
                  }
                }
              ],
              "postExecutionProcesses": [{
                  "process_id": 2,
                  "process_name": "postProcessName",
                  "lu_name": "luName2",
                  "execution_order": 2,
                  "is_editable": true,
                  "parameters": {
                    "inputs": [{"name": "param1", "value": "value1"}],
                    "is_editable": true
                  }
                }
              ],
              "permissions": [{"type": "ID", "value": "tester1"}, {"type": "GROUP", "value": "qa-team"}],
              "notes": [{"note_title": "Note 1", "note_description": "Some note", "note_date": "2025-01-01 00:00:00"}],
              "logicalUnits": [{
                  "lu_id": 7,
                  "lu_name": "Customer",
                  "source_affinity": "DC1",
                  "target_affinity": "DC2",
                  "source_max_no_of_workers": 4,
                  "target_max_no_of_workers": 4,
                  "override_fields": "{\\\\"source_affinity\\\\":true,\\\\"target_affinity\\\\":true,\\\\"max_no_of_workers\\\\":true}"
                }, {
                  "lu_id": 9,
                  "lu_name": "Billing",
                  "source_affinity": null,
                  "target_affinity": null,
                  "source_max_no_of_workers": null,
                  "target_max_no_of_workers": null,
                  "override_fields": "{\\\\"source_affinity\\\\":false,\\\\"target_affinity\\\\":false,\\\\"max_no_of_workers\\\\":false}"
                }
              ]
            }
            """)
    @webService(path = "task/{taskId}", verb = {MethodType.PUT}, version = "2", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": {\r\n" + "    \"id\": 146\r\n" + "  },\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsUpdateTaskV2(@param(required = true) Long taskId, Boolean copy, String task_status,
            @param(required = false) Long be_id, String env_name, Long environment_id, Long source_environment_id,
            String scheduler,
            Boolean delete_before_load, Integer num_of_entities, String selection_method,
            String selection_param_value, @param(required = true) String task_title, String parameters,
            Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, Boolean load_entity,
            @param(required = true) String task_type, String scheduling_end_date, Boolean version_ind,
            String retention_period_type, Integer retention_period_value, Integer selected_version_task_exe_id,
            Boolean task_globals, Integer selected_ref_version_task_exe_id, String sync_mode,
            List<Map<String, Object>> tableList, List<Map<String, Object>> globals, String reference,
            String task_created_by, List<Map<String, Object>> preExecutionProcesses,
            List<Map<String, Object>> postExecutionProcesses, List<Map<String, Object>> logicalUnits,
            Boolean reserve_ind, String reserve_retention_period_type, Integer reserve_retention_period_value,
            String reserve_note, String filterout_reserved, HashMap<String, Object> generateParams,
            Boolean mask_sensitive_data, String task_description, String custom_logic_lu_name,
            Long selected_subset_task_exe_id, Boolean clone_ind, String execution_mode, boolean evaluation_ind,
            boolean in_place_masking_ind,
            List<Long> taskGroupIds, String task_override_fields,
            Boolean enable_sequence_report, String statistics_report_flag, List<Map<String, String>> permissions,
            List<Map<String, Object>> notes) throws Exception {

        Long finalTaskId;
        Map<String, Object> result = new HashMap<>();

        boolean hasOverrideConfig = task_override_fields != null
                && !task_override_fields.isBlank()
                && !task_override_fields.equals("{}");

        if (in_place_masking_ind) {
            if (source_environment_id != null) {
                environment_id = source_environment_id;
                env_name = source_env_name;
            }
            if (hasOverrideConfig) {
                JsonObject jsonObject = JsonParser.parseString(task_override_fields).getAsJsonObject();
                boolean isSourceEnvEditable = jsonObject.getAsJsonObject("source_environment").get("is_editable")
                        .getAsBoolean();
                jsonObject.getAsJsonObject("target_environment").addProperty("is_editable", isSourceEnvEditable);
                task_override_fields = jsonObject.toString();
            }
        }

        if(source_environment_id != null && environment_id != null && source_environment_id.equals(environment_id)
            && "load".equalsIgnoreCase(task_type) && (Boolean) delete_before_load && (Boolean) version_ind == false) {
                return wrapWebServiceResults("FAILED",
                    "For Task with Delete, the source and target Environments cannot be the same",
                    null);
        }
        
        if (clone_ind == null) {
            clone_ind = false;
        }
        if (reserve_ind == null) {
            reserve_ind = false;
        }
        if (mask_sensitive_data == null) {
            mask_sensitive_data = false;
        }
        if (delete_before_load == null) {
            delete_before_load = false;
        }
        if (sync_mode == null) {
            sync_mode = "OFF";
        }
        if (taskGroupIds == null || taskGroupIds.isEmpty()) {
            taskGroupIds = Arrays.asList(1L);
        }
        if (task_override_fields == null) {
            task_override_fields = "{}";
        }
        if (permissions == null) {
            permissions = Arrays.asList(Map.of("type", "ID", "value", "ALL"));
        }
        if (enable_sequence_report == null) {
            enable_sequence_report = true;
        }
        if (statistics_report_flag == null) {
            statistics_report_flag = "ALL";
        }
        if ("TRAINING".equalsIgnoreCase(task_type)
                && (SYNTHETIC_ENVIRONMENT.equals(source_env_name) || AI_ENVIRONMENT.equals(source_env_name))
                && "OFF".equalsIgnoreCase(sync_mode)) {
            selection_method = "GENERATE_SUBSET";
        } else if ("TRAINING".equalsIgnoreCase(task_type) && SYNTHETIC_ENVIRONMENT.equals(source_env_name)) {
            selection_method = "GENERATE";
        }

        String msg = validateBeforeSave(
                be_id, environment_id, source_environment_id, task_override_fields,
                preExecutionProcesses, postExecutionProcesses, task_type,
                source_env_name, env_name, sync_mode, selection_method,
                num_of_entities, logicalUnits, parameters, custom_logic_lu_name, selection_param_value,
                retention_period_type, tableList,
                version_ind, selected_version_task_exe_id);

        if (!"".equalsIgnoreCase(msg)) {
            return wrapWebServiceResults("FAILED", msg, null);
        }
        
        if (tableList != null && !tableList.isEmpty() && !"OFF".equalsIgnoreCase(sync_mode)) {
            refresh_reference_data = true;
        }
        
        String userName = sessionUser().name();

        boolean canUpdateTask = canUserPerformTaskOperation(
                userName,
                source_environment_id == null ? null : String.valueOf(source_environment_id),
                environment_id == null ? null : String.valueOf(environment_id),
                task_type,
                sync_mode,
                reserve_ind != null && reserve_ind,
                delete_before_load != null && delete_before_load,
                replace_sequences != null && replace_sequences,
                clone_ind != null && clone_ind,
                refresh_reference_data != null && refresh_reference_data,
                version_ind != null && version_ind,
                scheduler,
                selection_method,
                num_of_entities == null ? 0L : num_of_entities.longValue(),
                "UPDATE",true,true,null 
        );

        if (!canUpdateTask) {
            return wrapWebServiceResults(
                    "FAILED",
                    "User " + userName + " is not allowed to update this task based on the assigned permissions.",
                    null
            );
        }
        handlePreAndPostExecution(task_type, selection_method, evaluation_ind, preExecutionProcesses, postExecutionProcesses);

        boolean create_new_task_version_on_update = false;
        try {
            String paramValue = db(TDM)
                    .fetch("SELECT PARAM_VALUE FROM " + TDMDB_SCHEMA
                            + ".TDM_GENERAL_PARAMETERS WHERE PARAM_NAME = 'CREATE_NEW_TASK_VERSION_ON_UPDATE'")
                    .firstValue()
                    .toString();

            // Only parse the value if the database call succeeded
            create_new_task_version_on_update = Boolean.parseBoolean(paramValue);

        } catch (Exception e) {
            log.error(
                    "Error reading configuration parameter 'CREATE_NEW_TASK_VERSION_ON_UPDATE'. Using default value: false. Error: "
                            + e.getMessage());
        }
        db(TDM).beginTransaction();
        // try {
        if (create_new_task_version_on_update) {
            result = (Map<String, Object>) wsUpdateTaskV1(taskId, copy, task_status, be_id,
                    environment_id, source_environment_id, scheduler, delete_before_load, num_of_entities,
                    selection_method, selection_param_value, task_title, parameters, refresh_reference_data,
                    replace_sequences, source_env_name, load_entity, task_type, scheduling_end_date, version_ind,
                    retention_period_type, retention_period_value, selected_version_task_exe_id, task_globals,
                    selected_ref_version_task_exe_id, sync_mode, tableList, globals, reference, task_created_by,
                    reserve_ind, reserve_retention_period_type, reserve_retention_period_value,
                    reserve_note, filterout_reserved, generateParams, mask_sensitive_data, task_description,
                    custom_logic_lu_name, selected_subset_task_exe_id, clone_ind, execution_mode, in_place_masking_ind,
                    task_override_fields, enable_sequence_report, statistics_report_flag, env_name);

            finalTaskId = (Long) ((Map<String, Object>) result.get("result")).get("id");

        } else {
            result = (Map<String, Object>) wsUpdateTaskRecord(taskId, be_id, environment_id,
                    source_environment_id, scheduler, delete_before_load, num_of_entities, selection_method,
                    selection_param_value, task_title, parameters, refresh_reference_data, replace_sequences,
                    source_env_name, load_entity, task_type, scheduling_end_date, version_ind,
                    retention_period_type, retention_period_value, selected_version_task_exe_id, task_globals,
                    selected_ref_version_task_exe_id, sync_mode, tableList, globals, reference,
                    reserve_ind, reserve_retention_period_type, reserve_retention_period_value,
                    reserve_note, filterout_reserved, generateParams, mask_sensitive_data, task_description,
                    custom_logic_lu_name, selected_subset_task_exe_id, clone_ind, execution_mode,
                    in_place_masking_ind, task_override_fields, enable_sequence_report, statistics_report_flag, env_name);

            finalTaskId = taskId;

            if ("TABLES".equalsIgnoreCase(selection_method)) {
                try {
                    db(TDM).execute("DELETE FROM " + TDMDB_SCHEMA + ".tasks_logical_units WHERE task_id = ?",
                            finalTaskId);
                } catch (Exception e) {
                    db(TDM).rollback();
                    return wrapWebServiceResults("FAILED",
                            "Failed to clear old Logical Units for Table-level task: " + e.getMessage(), null);
                }
            }
        }
        if (!checkWsResponse(result)) {
            db(TDM).rollback();
            return wrapWebServiceResults("FAILED", result.get("message"), null);
        }
        result = (Map<String, Object>) wsCreatePreExecutionProcessesFortask(finalTaskId, task_title,
                preExecutionProcesses);
        if (!checkWsResponse(result)) {
            db(TDM).rollback();
            return wrapWebServiceResults("FAILED", result.get("message"), null);
        }
        result = (Map<String, Object>) wsCreatePostExecutionProcessesFortask(finalTaskId, task_title,
                postExecutionProcesses);
        if (!checkWsResponse(result)) {
            db(TDM).rollback();
            return wrapWebServiceResults("FAILED", result.get("message"), null);
        }
        if (be_id != null && !"TABLES".equalsIgnoreCase(selection_method)) {
            Boolean validLus = fnValidateBELogicalUnits(be_id, logicalUnits);
            if (validLus) {
                result = (Map<String, Object>) wsCreateLogicalUnitsFortask(finalTaskId, logicalUnits);
                if (!checkWsResponse(result)) {
                    db(TDM).rollback();
                    return wrapWebServiceResults("FAILED", result.get("message"), null);
                }
            } else {
                db(TDM).rollback();
                return wrapWebServiceResults("FAILED",
                        "Logical Unit validation failed. Ensure that all selected Logical Units have their respective parents selected.",
                        null);
            }
        }
        // TDM 9.3 check for any disabled systems of the tasks logical units in the
        // source environment
        String inactive_source_products = fnValidateProductForTask(String.valueOf(source_environment_id),
                source_env_name, task_type, sync_mode, "SOURCE", finalTaskId);
        if (!"".equalsIgnoreCase(inactive_source_products)) {
            db(TDM).rollback();
            return wrapWebServiceResults("FAILED",
                    "The task cannot be updated. The following systems are currently disabled in " + source_env_name
                            + ": " + inactive_source_products,
                    null);
        }
        // TDM 9.3 check for any disabled systems of the tasks logical units in the
        // target environment
        String inactive_target_products = fnValidateProductForTask(String.valueOf(environment_id), env_name,
                task_type, sync_mode, "TARGET", finalTaskId);
        if (!"".equalsIgnoreCase(inactive_target_products)) {
            db(TDM).rollback();
            return wrapWebServiceResults("FAILED",
                    "The task cannot be updated. The following systems are currently disabled in " + env_name
                            + ": " + inactive_target_products,
                    null);
        }

        result = (Map<String, Object>) wsAssignTaskToTaskGroups(finalTaskId, taskGroupIds, true);
        if (!checkWsResponse(result)) {
            db(TDM).rollback();
            return wrapWebServiceResults("FAILED", "Can't assign the task to group: " + result.get("message"),
                    null);
        }
        
        result = (Map<String, Object>) wsUpdateTaskExecution(finalTaskId, permissions);
        if (!checkWsResponse(result)) {
            db(TDM).rollback();
            return wrapWebServiceResults("FAILED", "Can't update the task execution permissions: " + result.get("message"),
                    null);
        }

        if (notes != null) {
            result = (Map<String, Object>) wsUpdateTaskNotes(finalTaskId, notes);
            if (!checkWsResponse(result)) {
                db(TDM).rollback();
                return wrapWebServiceResults("FAILED", "Can't update notes for the task: " + result.get("message"), null);
            }
        }

        db(TDM).commit();

        result.put("id", finalTaskId);
        return wrapWebServiceResults("SUCCESS", null, result);
    }

    @desc("Updates Task. The task update creates a new version of the task and set the status of the previous task version to Inactive.\r\n" + "\r\n" + "Example of a request body:\r\n" + "\r\n" + "{\r\n" + "   \"task_last_updated_date\":\"2022-12-19 12:16:48.257\",\r\n" + "   \"filterout_reserved\":false,\r\n" + "   \"be_id\":4,\r\n" + "   \"reserve_retention_period_type\":\"Days\",\r\n" + "   \"environment_id\":2,\r\n" + "   \"selection_method\":\"PR\",\r\n" + "   \"refresh_reference_data\":false,\r\n" + "   \"tester\":\"tester2\",\r\n" + "   \"be_last_updated_date\":\"2022-12-19 12:12:39.838\",\r\n" + "   \"owners\":[\r\n" + "      \r\n" + "   ],\r\n" + "   \"refcount\":1,\r\n" + "   \"num_of_entities\":2,\r\n" + "   \"tester_type\":\"ID\",\r\n" + "   \"reserve_note\":null,\r\n" + "   \"load_entity\":true,\r\n" + "   \"selected_version_task_exe_id\":null,\r\n" + "   \"task_created_by\":\"admin\",\r\n" + "   \"be_last_updated_by\":\"admin\",\r\n" + "   \"scheduling_end_date\":null,\r\n" + "   \"environment_point_of_contact_phone1\":null,\r\n" + "   \"processnames\":\"PostExecFlow\",\r\n" + "   \"testers\":[\r\n" + "      {\r\n" + "         \"tester_type\":\"ID\",\r\n" + "         \"role_id\":[\r\n" + "            \"4\"\r\n" + "         ],\r\n" + "         \"tester\":\"tester2\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"tester_type\":\"ID\",\r\n" + "         \"role_id\":[\r\n" + "            \"3\"\r\n" + "         ],\r\n" + "         \"tester\":\"tester1\"\r\n" + "      }\r\n" + "   ],\r\n" + "   \"selection_param_value\":\"(( 'Bronze' = ANY(\\\"BILLING.VIP_STATUS\\\") ))\",\r\n" + "   \"environment_status\":\"Active\",\r\n" + "   \"be_status\":\"Active\",\r\n" + "   \"task_last_updated_by\":\"admin\",\r\n" + "   \"selected_ref_version_task_exe_id\":null,\r\n" + "   \"task_execution_status\":\"Active\",\r\n" + "   \"sync_mode\":null,\r\n" + "   \"replace_sequences\":false,\r\n" + "   \"environment_point_of_contact_last_name\":null,\r\n" + "   \"environment_point_of_contact_email\":null,\r\n" + "   \"be_description\":\"\",\r\n" + "   \"reserve_retention_period_value\":5,\r\n" + "   \"parameters\":\"{\\\"group\\\":{\\\"rules\\\":[{\\\"condition\\\":\\\"=\\\",\\\"field\\\":\\\"BILLING.VIP_STATUS\\\",\\\"data\\\":\\\"Bronze\\\",\\\"operator\\\":\\\"AND\\\",\\\"type\\\":\\\"text\\\",\\\"comboIndicator\\\":\\\"true\\\",\\\"validValues\\\":[\\\"Bronze\\\",\\\"Gold\\\",\\\"Platinum\\\",\\\"Silver\\\"],\\\"disableThird\\\":false}]}}\",\r\n" + "   \"environment_expiration_date\":null,\r\n" + "   \"environment_point_of_contact_phone2\":null,\r\n" + "   \"environment_created_by\":\"admin\",\r\n" + "   \"roles\":[\r\n" + "      [\r\n" + "         {\r\n" + "            \"role_id\":4,\r\n" + "            \"allowed_test_conn_failure\":false\r\n" + "         },\r\n" + "         {\r\n" + "            \"role_id\":3,\r\n" + "            \"allowed_test_conn_failure\":false\r\n" + "         }\r\n" + "      ]\r\n" + "   ],\r\n" + "   \"environment_last_updated_by\":\"admin\",\r\n" + "   \"be_creation_date\":\"2022-10-19 18:42:40.301\",\r\n" + "   \"task_id\":87,\r\n" + "   \"be_created_by\":\"admin\",\r\n" + "   \"source_environment_id\":1,\r\n" + "   \"role_id_orig\":4,\r\n" + "   \"scheduler\":\"immediate\",\r\n" + "   \"environment_description\":null,\r\n" + "   \"source_env_name\":\"SRC\",\r\n" + "   \"reserve_ind\":true,\r\n" + "   \"task_title\":\"testapi2\",\r\n" + "   \"environment_name\":\"TAR\",\r\n" + "   \"delete_before_load\":false,\r\n" + "   \"allow_write\":true,\r\n" + "   \"owner\":null,\r\n" + "   \"task_status\":\"Active\",\r\n" + "   \"executioncount\":0,\r\n" + "   \"environment_last_updated_date\":\"2022-12-06 10:18:49.378\",\r\n" + "   \"be_name\":\"Customer\",\r\n" + "   \"version_ind\":false,\r\n" + "  \"task_globals\":false,\r\n" + "   \"environment_point_of_contact_first_name\":null,\r\n" + "   \"task_type\":\"LOAD\",\r\n" + "   \"environment_creation_date\":\"2022-09-21 13:43:25.13\",\r\n" + "   \"owner_type\":null,\r\n" + "   \"creatorRoles\":[\r\n" + "      \"admin\"\r\n" + "   ],\r\n" + "   \"selection_method2\":\"Parameters with Random Entity Selection\",\r\n" + "   \"task_type2\":\"LOAD\",\r\n" + "   \"operation_mode\":\"Load entity\",\r\n" + "   \"data_type\":\"Entities and Reference\",\r\n" + "   \"disabled\":false,\r\n" + "   \"onHold\":false,\r\n" + "   \"reference\":\"both\",\r\n" + "   \"operationMode\":\"insert_entity_without_delete\",\r\n" + "   \"extractSelected\":true,\r\n" + "   \"postExecutionProcesses\":[\r\n" + "      1\r\n" + "   ],\r\n" + "   \"refList\":[\r\n" + "      {\r\n" + "         \"ref_table_name\":\"DEVICESTABLE2017\",\r\n" + "         \"lu_name\":\"Customer\",\r\n" + "         \"interface_name\":\"CRM_DB\",\r\n" + "         \"schema_name\":\"public\",\r\n" + "         \"logical_unit_name\":\"Customer\",\r\n" + "         \"reference_table_name\":\"DEVICESTABLE2017\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"ref_table_name\":\"devicestable2017\",\r\n" + "         \"lu_name\":\"Customer\",\r\n" + "         \"interface_name\":\"CRM_DB\",\r\n" + "         \"schema_name\":\"public\",\r\n" + "         \"logical_unit_name\":\"Customer\",\r\n" + "         \"reference_table_name\":\"devicestable2017\",\r\n" + "         \"selected\":true\r\n" + "      }\r\n" + "   ],\r\n" + "   \"globals\":[\r\n" + "      \r\n" + "   ],\r\n" + "   \"selectedLogicalUnits\":[\r\n" + "      {\r\n" + "         \"lu_name\":\"Customer\",\r\n" + "         \"lu_id\":7,\r\n" + "         \"task_id\":87\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_name\":\"Billing\",\r\n" + "         \"lu_id\":9,\r\n" + "         \"task_id\":87\r\n" + "      {\r\n" + "         \"lu_name\":\"Collection\",\r\n" + "         \"lu_id\":10,\r\n" + "         \"task_id\":87\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_name\":\"Orders\",\r\n" + "         \"lu_id\":8,\r\n" + "         \"task_id\":87\r\n" + "      }\r\n" + "   ],\r\n" + "   \"allLogicalUnits\":[\r\n" + "      {\r\n" + "         \"lu_parent_name\":null,\r\n" + "         \"lu_name\":\"Customer\",\r\n" + "         \"lu_id\":7,\r\n" + "         \"product_name\":\"CRM\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Billing\",\r\n" + "         \"lu_id\":9,\r\n" + "         \"product_name\":\"FINANCE\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Collection\",\r\n" + "         \"lu_id\":10,\r\n" + "         \"product_name\":\"FINANCE\"\r\n" + "      },\r\n" + "      {\r\n" + "         \"lu_parent_name\":\"Customer\",\r\n" + "         \"lu_name\":\"Orders\",\r\n" + "         \"lu_id\":8,\r\n" + "         \"product_name\":\"ORDERS\"\r\n" + "      }\r\n" + "   ],\r\n" + "   \"missingRootLU\":[\r\n" + "      \r\n" + "   ],\r\n" + "   \"syncModeRadio\":null,\r\n" + "   \"allPostExecutionProcess\":[\r\n" + "      {\r\n" + "         \"process_id\":1,\r\n" + "         \"be_id\":4,\r\n" + "         \"process_name\":\"PostExecFlow\",\r\n" + "         \"process_description\":null,\r\n" + "         \"execution_order\":1\r\n" + "      }\r\n" + "   ],\r\n" + "   \"versionsForLoad\":[\r\n" + "      \r\n" + "   ]\r\n" + "}=-}")
    @webService(path = "task/{taskId}", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": {\r\n" + "    \"id\": 146\r\n" + "  },\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsUpdateTaskV1(@param(required = true) Long taskId, Boolean copy, String task_status, 
            @param(required = true) Long be_id, Long environment_id, Long source_environment_id, String scheduler,
            Boolean delete_before_load, Integer num_of_entities, String selection_method,
            String selection_param_value, @param(required = true) String task_title, String parameters,
            Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, Boolean load_entity,
            @param(required = true) String task_type, String scheduling_end_date, Boolean version_ind,
            String retention_period_type, Integer retention_period_value, Integer selected_version_task_exe_id,
            Boolean task_globals, Integer selected_ref_version_task_exe_id, String sync_mode,
            List<Map<String, Object>> tableList, List<Map<String, Object>> globals, String reference,
            String task_created_by, Boolean reserve_ind, String reserve_retention_period_type,
            Integer reserve_retention_period_value, String reserve_note, String filterout_reserved,
            HashMap<String, Object> generateParams, Boolean mask_sensitive_data, String task_description,
            String custom_logic_lu_name, Long selected_subset_task_exe_id,Boolean clone_ind, 
            String execution_mode , boolean in_place_masking_ind, String task_override_fields, Boolean enable_sequence_report, String statistics_report_flag, String env_name) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        String message = null;
        String errorCode = "";
        //try {
            db(TDM).execute("UPDATE " + TDMDB_SCHEMA + ".tasks SET " + "task_status=(?) WHERE task_id = " + taskId, copy != null && copy ? "Active" : "Inactive");

            if (TABLES.equals(selection_method) && (tableList == null || tableList.isEmpty())) {
                response.put("errorCode", "FAILED");
                response.put("message", "Cannot create Tables Only Task without any table selected.");
                return response;
            }
            
            selected_ref_version_task_exe_id = selected_ref_version_task_exe_id != null ? selected_ref_version_task_exe_id : 0;
            selected_version_task_exe_id = selected_version_task_exe_id != null ? selected_version_task_exe_id : 0;
            selected_subset_task_exe_id = selected_subset_task_exe_id != null ? selected_subset_task_exe_id : 0;

            if (reference != null && reference.equals("refernceOnly")) {
                if (tableList != null && tableList.size() > 0) {
                    selection_method = TABLES;
                    delete_before_load = false;
                    load_entity = false;
                }
                num_of_entities = 0;
            } else {
                if ("L".equals(selection_method) && selection_param_value != null && !"".equals(selection_param_value) && !clone_ind) {
                    int entitiesCnt = (selection_param_value.split(",")).length;
                    if (num_of_entities == null || num_of_entities == 0 || num_of_entities != entitiesCnt) {
                        num_of_entities = (selection_param_value.split(",")).length;
                    }
                }
            }

            if ("RESERVE".equalsIgnoreCase(task_type) || "DELETE".equalsIgnoreCase(task_type)) {
                source_environment_id = environment_id;
                source_env_name = env_name;
            }
            if ("EXTRACT".equalsIgnoreCase(task_type) || "GENERATE".equalsIgnoreCase(task_type)) {
                environment_id = source_environment_id;
                env_name = source_env_name;
            }
            
            String sql = "INSERT INTO " + TDMDB_SCHEMA + ".tasks (be_id, environment_id, env_name, scheduler, delete_before_load," +
                    "num_of_entities, selection_method,selection_param_value,custom_logic_lu_name, task_execution_status," +
                    "task_created_by, task_creation_date, task_last_updated_date, task_last_updated_by, task_status, " +
                    "task_title, parameters,refresh_reference_data, replace_sequences, source_environment_id, source_env_name, load_entity, task_type, " +
                    "scheduling_end_date, version_ind, retention_period_type, retention_period_value, " +
                    "selected_version_task_exe_id, selected_subset_task_exe_id, task_globals, " +
                    "selected_ref_version_task_exe_id, sync_mode, reserve_ind, " +
                    "reserve_retention_period_type, reserve_retention_period_value, reserve_note, filterout_reserved," +
                    "task_description, clone_ind, execution_mode,in_place_masking_ind, permission_group, fabric_roles, " +
                    "task_override_fields, enable_sequence_report, statistics_report_flag) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "RETURNING task_id";
            String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC).format(Instant.now());
            String username = sessionUser().name();
            Set<String> tmpRoles = new HashSet<>();
            for(String role :sessionUser().roles()){
                if(!("Everybody".equalsIgnoreCase(role))){
                    tmpRoles.add(role);}
            }
            
            String fabricRoles = String.join(TDM_PARAMETERS_SEPARATOR, tmpRoles);
            String createdBy = username + "##" + String.join(TDM_PARAMETERS_SEPARATOR, tmpRoles);
            String permessionGroup = fnGetUserPermissionGroup(username);
                        
            task_created_by = task_created_by == null ? createdBy : task_created_by;
            Db.Row row = db(TDM).fetch(sql, be_id, environment_id != null ? environment_id : source_environment_id,
                    scheduler, ((delete_before_load != null) ? delete_before_load : false), num_of_entities,
                    selection_method, selection_param_value, custom_logic_lu_name, "Active",
                    copy != null && copy ? createdBy : task_created_by, now,
                    now,
                    username, "Active", task_title, parameters, refresh_reference_data, replace_sequences,
                    source_environment_id, source_env_name, ((load_entity != null) ? load_entity : false),
                    task_type, scheduling_end_date, version_ind, retention_period_type, retention_period_value,
                    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals,
                    selected_ref_version_task_exe_id, sync_mode, reserve_ind, reserve_retention_period_type,
                    reserve_retention_period_value, reserve_note, filterout_reserved,
                    task_description, clone_ind, execution_mode, in_place_masking_ind, permessionGroup, fabricRoles,
                    task_override_fields, enable_sequence_report, statistics_report_flag, env_name).firstRow();

            Long id = Long.parseLong(row.get("task_id").toString());

            if (tableList != null) {
                if (tableList.size() > 0) {
                    String msg = validateMaxWorkers(tableList);
                    if (!"".equalsIgnoreCase(msg)) {
                        return wrapWebServiceResults("FAILED", msg, null);
                    }
                    fnSaveRefTablestoTask(id, tableList, in_place_masking_ind);
                }
            }


            if (globals != null && globals.size() > 0 && task_globals) {
                try {
                    for (Map<String, Object> global : globals) {
                        boolean isEditable = Boolean.TRUE.equals(global.get("is_editable"));
                        createTaskGlobals(id, global.get("lu_name").toString(), global.get("global_name").toString(), global.get("global_value").toString(), isEditable);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
            }


            //try {
                // TDM 8.0 - In case of Synthetic task, load the input parameters of the population flows into the TDMDB tdm_generate_task_field_mappings
                //log.info("wsUpdateTaskV1 - params size: " + DataManParams.size());
                if (source_environment_id != null && source_environment_id == -1) {
                    if (generateParams == null) {
                        String luList = "" + db(TDM).fetch("select STRING_AGG(lu_name, ',') from tasks_logical_units where task_id = ?", taskId).firstValue();
                        generateParams = (HashMap<String, Object>) ((Map<String, Object>) wsGetDMPopParams(luList,
                                taskId, null)).get("result");
                    }
                    if (generateParams != null) {
                        try {
                            createTaskGEnerateParams(id, generateParams);
                        } catch (Exception e) {
                            message = e.getMessage();
                            errorCode = "FAILED";
                            response.put("errorCode", errorCode);
                            response.put("message", message);
                            return response;
                        }
                    }

                }

                String activityDesc = "Task " + task_title + " was updated";
                fnInsertActivity("update", "Tasks", activityDesc);
           // } catch (Exception e) {
           //     e.printStackTrace();
           //     log.error(e.getMessage());
           // }

            errorCode = "SUCCESS";
            result.put("id", id);
            response.put("result", result);
      //  } catch (Exception e) {
      //      message = e.getMessage();
      //      log.error(message);
      //      errorCode = "FAILED";
     //   }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @webService(path = "task/{taskId}/direct", verb = {
            MethodType.PUT }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
            {
              "result": {
                "id": 87
              },
              "errorCode": "SUCCESS",
              "message": null
            }
            """)
    public static Object wsUpdateTaskRecord(@param(required = true) Long taskId,
            @param(required = true) Long be_id, Long environment_id, Long source_environment_id, String scheduler,
            Boolean delete_before_load, Integer num_of_entities, String selection_method,
            String selection_param_value, @param(required = true) String task_title, String parameters,
            Boolean refresh_reference_data, Boolean replace_sequences, String source_env_name, Boolean load_entity,
            @param(required = true) String task_type, String scheduling_end_date, Boolean version_ind,
            String retention_period_type, Integer retention_period_value, Integer selected_version_task_exe_id,
            Boolean task_globals, Integer selected_ref_version_task_exe_id, String sync_mode,
            List<Map<String, Object>> tableList, List<Map<String, Object>> globals, String reference,
            Boolean reserve_ind,
            String reserve_retention_period_type,
            Integer reserve_retention_period_value, String reserve_note, String filterout_reserved,
            HashMap<String, Object> generateParams, Boolean mask_sensitive_data, String task_description,
            String custom_logic_lu_name, Long selected_subset_task_exe_id, Boolean clone_ind,
            String execution_mode,boolean in_place_masking_ind, String editable_params,
            Boolean enable_sequence_report, String statistics_report_flag, String env_name) throws Exception {

        HashMap<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        String message = null;
        String errorCode = "";

        try {
            if (TABLES.equals(selection_method) && (tableList == null || tableList.isEmpty())) {
                response.put("errorCode", "FAILED");
                response.put("message", "Cannot update Tables Only Task without any table selected.");
                return response;
            }

            selected_ref_version_task_exe_id = selected_ref_version_task_exe_id != null
                    ? selected_ref_version_task_exe_id
                    : 0;
            selected_version_task_exe_id = selected_version_task_exe_id != null ? selected_version_task_exe_id : 0;
            selected_subset_task_exe_id = selected_subset_task_exe_id != null ? selected_subset_task_exe_id : 0;

            if (reference != null && reference.equals("refernceOnly")) {
                if (tableList != null && tableList.size() > 0) {
                    selection_method = TABLES;
                    delete_before_load = false;
                    load_entity = false;
                }
                num_of_entities = 0;
            } else {
                if ("L".equals(selection_method) && selection_param_value != null && !"".equals(selection_param_value)
                        && !clone_ind) {
                    int entitiesCnt = (selection_param_value.split(",")).length;
                    if (num_of_entities == null || num_of_entities == 0 || num_of_entities != entitiesCnt) {
                        num_of_entities = (selection_param_value.split(",")).length;
                    }
                }
            }

            if ("RESERVE".equalsIgnoreCase(task_type) || "DELETE".equalsIgnoreCase(task_type)) {
                source_environment_id = environment_id;
                source_env_name = env_name;
            }

            String username = sessionUser().name();
            Set<String> tmpRoles = new HashSet<>();
            for (String role : sessionUser().roles()) {
                if (!("Everybody".equalsIgnoreCase(role))) {
                    tmpRoles.add(role);
                }
            }
            String updatedBy = username + "##" + String.join(TDM_PARAMETERS_SEPARATOR, tmpRoles);

            // SQL UPDATE statement to modify the existing task record
            String sql = """
                    UPDATE %s.tasks SET
                    be_id = ?, environment_id = ?, env_name = ?,  scheduler = ?, delete_before_load = ?,
                    num_of_entities = ?, selection_method = ?, selection_param_value = ?, custom_logic_lu_name = ?,
                    task_last_updated_date = ?, task_last_updated_by = ?, task_status = ?,
                    task_title = ?, parameters = ?, refresh_reference_data = ?, replace_sequences = ?,
                    source_environment_id = ?, source_env_name = ?, load_entity = ?, task_type = ?,
                    scheduling_end_date = ?, version_ind = ?, retention_period_type = ?, retention_period_value = ?,
                    selected_version_task_exe_id = ?, selected_subset_task_exe_id = ?, task_globals = ?,
                    selected_ref_version_task_exe_id = ?, sync_mode = ?, reserve_ind = ?,
                    reserve_retention_period_type = ?, reserve_retention_period_value = ?, reserve_note = ?,
                    filterout_reserved = ?, task_description = ?, clone_ind = ?,
                    execution_mode = ? , in_place_masking_ind = ?, task_override_fields = ?, enable_sequence_report = ? , statistics_report_flag = ?
                    WHERE task_id = ?
                    """.formatted(TDMDB_SCHEMA);

            db(TDM).execute(sql,
                    be_id, environment_id != null ? environment_id : source_environment_id, env_name, scheduler,
                    ((delete_before_load != null) ? delete_before_load : false), num_of_entities,
                    selection_method, selection_param_value, custom_logic_lu_name,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
                            .format(Instant.now()),
                    updatedBy, "Active", task_title, parameters, refresh_reference_data, replace_sequences,
                    source_environment_id, source_env_name, ((load_entity != null) ? load_entity : false),
                    task_type, scheduling_end_date, version_ind, retention_period_type, retention_period_value,
                    selected_version_task_exe_id, selected_subset_task_exe_id, task_globals,
                    selected_ref_version_task_exe_id, sync_mode, reserve_ind, reserve_retention_period_type,
                    reserve_retention_period_value, reserve_note, filterout_reserved,
                    task_description, clone_ind, execution_mode, in_place_masking_ind, editable_params,
                    enable_sequence_report, statistics_report_flag, 
                    taskId); // Use taskId in the WHERE clause

            db(TDM).execute("DELETE FROM " + TDMDB_SCHEMA + ".task_ref_tables WHERE task_id = ?", taskId);
            if (tableList != null && !tableList.isEmpty()) {
                String msg = validateMaxWorkers(tableList);
                if (!"".equalsIgnoreCase(msg))
                {
                    return wrapWebServiceResults("FAILED", msg, null);
                }
                fnSaveRefTablestoTask(taskId, tableList, in_place_masking_ind);
            }

            db(TDM).execute("DELETE FROM " + TDMDB_SCHEMA + ".task_globals WHERE task_id = ?", taskId);
            if (globals != null && !globals.isEmpty() && task_globals) {
                for (Map<String, Object> global : globals) {
                    boolean isEditable = Boolean.TRUE.equals(global.get("is_editable"));
                    createTaskGlobals(taskId, global.get("lu_name").toString(), global.get("global_name").toString(),
                            global.get("global_value").toString(), isEditable);
                }
            }

            db(TDM).execute("DELETE FROM " + TDMDB_SCHEMA + ".tdm_generate_task_field_mappings WHERE task_id = ?",
                    taskId);
            if (source_environment_id != null && source_environment_id == -1) {
                if (generateParams == null) {
                    String luList = "" + db(TDM)
                            .fetch("select STRING_AGG(lu_name, ',') from tasks_logical_units where task_id = ?", taskId)
                            .firstValue();
                    generateParams = (HashMap<String, Object>) ((Map<String, Object>) wsGetDMPopParams(luList, taskId,
                            null))
                            .get("result");
                }
                if (generateParams != null) {
                    createTaskGEnerateParams(taskId, generateParams);
                }
            }

            String activityDesc = "Task " + task_title + " (ID: " + taskId + ") was directly updated.";
            fnInsertActivity("update", "Tasks", activityDesc);

            errorCode = "SUCCESS";
            result.put("id", taskId); // Return the ID of the updated task
            response.put("result", result);
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static void handlePreAndPostExecution(String taskType, String selectionMethod, boolean evaluationInd,
            List<Map<String, Object>> preExecutionProcesses,
            List<Map<String, Object>> postExecutionProcesses) {

        // Handle the specific TRAINING task logic.
        if ("TRAINING".equalsIgnoreCase(taskType)) {
            postExecutionProcesses = fnAddAIExecutionProcess(postExecutionProcesses, "Training Data Subset",
                    "training_ai", -1, "Exporting Data Subset", "export_entities", -2);
        }

        // Combine the logic for "Load" and "AI_GENERATED" tasks that use "AI_GENERATED"
        // selection.
        if ("AI_GENERATED".equalsIgnoreCase(selectionMethod)) {
            // All AI_GENERATED selection tasks start with this process.
            preExecutionProcesses = fnAddAIExecutionProcess(preExecutionProcesses, "Generating Data Subset",
                    "generation_ai", -2, "", "", 0);

            // Add the "Importing Data Subset" process only for "Load" tasks.
            if ("Load".equalsIgnoreCase(taskType)) {
                preExecutionProcesses = fnAddAIExecutionProcess(preExecutionProcesses, "Importing Data Subset",
                        "import_entities", -1, "", "", 0);
            }

            // Add evaluation process if the flag is true for both task types.
            if (evaluationInd) {
                postExecutionProcesses = fnAddAIExecutionProcess(postExecutionProcesses, "Evaluating Data Subset",
                        "evalaution_ai", -3, "", "", 0);
            }
        }
    }

    @desc("Calculates the number of entities that matches the selected task's parameters. The calculation is based on the task's Business Entity (BE), source environment, and the Where statement (populated in the request Body) reflecting the selected parameters.\r\n" + "\r\n" + "Use the ANY command when checking a value of a parameter since the TDM LU parameter tables contain an array of values on each parameter.\r\n" + "\r\n" + "Example of a request body:\r\n" + "{\r\n" + "  \"tar_env_name\": \"TAR\",\r\n" + "  \"where\": \"(( 'Alise' = ANY(\\\"CUSTOMER.FIRST_NAME\\\") ))\",\r\n" + "  \"filterout_reserved\": OTHERS\r\n" + "}")
    @webService(path = "businessentity/{beId}/sourceEnv/{src_env_name}/analysiscount", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\"errorCode\":\"SUCCESS\",\"message\":null,\"result\":834}")
    public static Object wsGetAnalysiscountForBusinessEntity(@param(required = true) Long beId, @param(required = true) String src_env_name, String tar_env_name, String where, String queryJson, String filterout_reserved) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        try {
            Object result = fnGetNumberOfMatchingEntities(where, queryJson, src_env_name, tar_env_name, beId, filterout_reserved,true); //TDM
            response.put("result", ((Map<String, Object>) result).get("result"));
            response.put("errorCode", "SUCCESS");
        } catch (Exception e) {
            response.put("errorCode", "FAILED");
            response.put("message", e.getMessage());
            log.error("wsGetAnalysiscountForBusinessEntity - " + e.getMessage());
        }
        return response;
    }


    @desc("""
            Adds Logical Units to the task.

            Example of a request body:
            {
              "logicalUnits": [
                {
                  "lu_id": "27",
                  "lu_name": "lu1",
                  "source_max_no_of_workers": 5,
                  "target_max_no_of_workers": 5,
                  "source_affinity": "DC1",
                  "target_affinity": "DC2",
                  "override_fields": {"max_no_of_workers": true, "source_affinity": true, "target_affinity": false}
                }
              ]
            }
            """)
    @webService(path = "task/{taskId}/logicalUnits", verb = {
            MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON })
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n"
            + "  \"message\": null\r\n" + "}")
    public static Object wsCreateLogicalUnitsFortask(@param(required = true) Long taskId,
            List<Map<String, Object>> logicalUnits) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            fnPostTaskLogicalUnits(taskId, logicalUnits);
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


	@desc("Adds Post Execution Processes to the task.\r\n" + "\r\n" + "Example of a request body:\r\n" + "{\r\n" + "  \"postexecutionprocesses\": [\r\n" + "    {\r\n" + "      \"process_id\": 1,\r\n" + "      \"process_name\": \"processName\",\r\n" + "      \"execution_order\": 2\r\n" + "    },\r\n" + "    {\r\n" + "      \"process_id\": 2,\r\n" + "      \"process_name\": \"processName2\",\r\n" + "      \"execution_order\": 3\r\n" + " \"process_type\": \"post\",\r\n" + "    }\r\n" + "  ]\r\n" + "}")
	@webService(path = "task/{taskId}/taskname/{taskName}/postexecutionprocesses", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"postexecutionprocesses\": [\r\n" + "    {\r\n" + "      \"process_id\": 1,\r\n" + "      \"process_name\": \"processName\",\r\n" + "      \"execution_order\": 2\r\n" + "    },\r\n" + "    {\r\n" + "      \"process_id\": 2,\r\n" + "      \"process_name\": \"processName2\",\r\n" + "      \"execution_order\": 3\r\n" + " \"process_type\": \"post\",\r\n" + "    }\r\n" + "  ]\r\n" + "}")
	public static Object wsCreatePostExecutionProcessesFortask(@param(required = true) Long taskId, @param(required = true) String taskName, List<Map<String, Object>> postexecutionprocesses) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String processType = "post";
		try {
			fnAddTaskExecutionProcess(postexecutionprocesses, taskId,processType);			
			errorCode = "SUCCESS";
		} catch (Exception e) {
			message = e.getMessage();
			log.error(message);
			errorCode = "FAILED";
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}
	@desc("Adds Pre Execution Processes to the task.\r\n" + "\r\n" + "Example of a request body:\r\n" + "{\r\n" + "  \"preexecutionprocesses\": [\r\n" + "    {\r\n" + "      \"process_id\": 1,\r\n" + "      \"process_name\": \"processName\",\r\n" + "      \"execution_order\": 2\r\n" + "    },\r\n" + "    {\r\n" + "      \"process_id\": 2,\r\n" + "      \"process_name\": \"processName2\",\r\n" + "      \"execution_order\": 3\r\n" + " \"process_type\": \"pre\",\r\n" + " }\r\n" + "  ]\r\n" + "}")
	@webService(path = "task/{taskId}/taskname/{taskName}/preexecutionprocesses", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"postpreexecutionprocesses\": [\r\n" + "    {\r\n" + "      \"process_id\": 1,\r\n" + "      \"process_name\": \"processName\",\r\n" + "      \"execution_order\": 2\r\n" + "    },\r\n" + "    {\r\n" + "      \"process_id\": 2,\r\n" + "      \"process_name\": \"processName2\",\r\n" + "      \"execution_order\": 3\r\n" + " \"process_type\": \"pre\",\r\n" + "    }\r\n" + "  ]\r\n" + "}")
	public static Object wsCreatePreExecutionProcessesFortask(@param(required = true) Long taskId, @param(required = true) String taskName, List<Map<String, Object>> preexecutionprocesses) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String processType = "pre";
		try {
			fnAddTaskExecutionProcess(preexecutionprocesses, taskId,processType);			
			errorCode = "SUCCESS";
		} catch (Exception e) {
			message = e.getMessage();
			log.error(message);
			errorCode = "FAILED";
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Gets the task's Post Execution Process.")
	@webService(path = "task/{taskId}/postexecutionprocess", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"process_id\": 1,\r\n" + "      \"process_name\": \"processName\",\r\n" + "      \"task_id\": 145,\r\n" + "      \"execution_order\": 2\r\n" + "    },\r\n" + "    {\r\n" + "      \"process_id\": 2,\r\n" + "      \"process_name\": \"processName2\",\r\n" + "      \"task_id\": 145,\r\n" + "      \"execution_order\": 3\r\n" + "  \"process_type\": \"post\",\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
	public static Object wsGetTaskPostExecutionProcesses(@param(required = true) Long taskId) throws Exception {
		String processType = "post";
		return fnGetTaskExecutionProcesses(taskId,processType);
	}

	@desc("Gets the task's PreExecution Process.")
	@webService(path = "task/{taskId}/preexecutionprocess", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"process_id\": 1,\r\n" + "      \"process_name\": \"processName\",\r\n" + "      \"task_id\": 145,\r\n" + "      \"execution_order\": 2\r\n" + "    },\r\n" + "    {\r\n" + "      \"process_id\": 2,\r\n" + "      \"process_name\": \"processName2\",\r\n" + "      \"task_id\": 145,\r\n" + "      \"execution_order\": 3\r\n" + "  \"process_type\": \"pre\",\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
	public static Object wsGetTaskPreExecutionProcesses(@param(required = true) Long taskId) throws Exception {
		String processType = "pre";
		return fnGetTaskExecutionProcesses(taskId,processType);

	}

    @desc("This API gets the task's Logical Units. Note that the Business Entity (BE) ID and name are returned by /tasks API in be_id and be_name output attributes.")
    @webService(path = "task/{taskId}/logicalunits", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"lu_name\": \"Collection\",\r\n" + "      \"lu_id\": 23,\r\n" + "      \"task_id\": 291\r\n" + "    },\r\n" + "    {\r\n" + "      \"lu_name\": \"Customer\",\r\n" + "      \"lu_id\": 20,\r\n" + "      \"task_id\": 291\r\n" + "    },\r\n" + "    {\r\n" + "      \"lu_name\": \"Billing\",\r\n" + "      \"lu_id\": 22,\r\n" + "      \"task_id\": 291\r\n" + "    },\r\n" + "    {\r\n" + "      \"lu_name\": \"Orders\",\r\n" + "      \"lu_id\": 21,\r\n" + "      \"task_id\": 291\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetTaskLogicalUnits(@param(required = true) Long taskId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".tasks_logical_units WHERE task_id =" + taskId;
            Db.Rows rows = db(TDM).fetch(sql);

            List<Map<String, Object>> result = new ArrayList<>();
            List<String> columnNames = rows.getColumnNames();
            for (Db.Row row : rows) {
                ResultSet resultSet = row.resultSet();
                Map<String, Object> rowMap = new HashMap<>();
                for (String columnName : columnNames) {
                    rowMap.put(columnName, resultSet.getObject(columnName));
                }
                result.add(rowMap);
            }
            response.put("result", result);
            errorCode = "SUCCESS";
            if (rows != null) {
                rows.close();
            }
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("This API gets the task's Globals if they exist. Note that task_globals attribute of /tasks API indicates if the task has globals. This attribute is populated by true if the task has Globals.")
    @webService(path = "task/{taskId}/globals", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"global_name\": \"MASK_FLAG\",\r\n" + "      \"lu_name\": \"ALL\",\r\n" + "      \"task_id\": 57,\r\n" + "      \"global_value\": \"false\"\r\n" + "    },\r\n" + "    {\r\n" + "      \"global_name\": \"MAIL_ADDRESS\",\r\n" + "      \"lu_name\": \"Customer\",\r\n" + "      \"task_id\": 57,\r\n" + "      \"global_value\": \"tali@gmail.com\"\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetTaskGlobals(@param(required = true) String taskId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".task_globals WHERE task_globals.task_id = " + taskId;
            Db.Rows rows = db(TDM).fetch(sql);

            List<Map<String, Object>> result = new ArrayList<>();

            for (Db.Row row : rows) {
                ResultSet resultSet = row.resultSet();
                HashMap<String, Object> global = new HashMap<>();
                String luName = "ALL";
                String globalName = "" + resultSet.getString("global_name");
                if (globalName.contains(".")) {
                    String[] arr = globalName.split("\\.");
                    luName = arr[0];
                    globalName = arr[1];
                }
                global.put("task_id", resultSet.getLong("task_id"));
                global.put("lu_name", luName);
                global.put("global_name", globalName);
                global.put("global_value", resultSet.getString("global_value"));
                global.put("is_editable", resultSet.getBoolean("is_editable"));
                result.add(global);
            }
            response.put("result", result);
            errorCode = "SUCCESS";
            if (rows != null) {
                rows.close();
            }
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("Gets the list of available Reference tables for a task based on the task's Logical Units (LUs). Task type can be either LOAD or EXTRACT.\r\n" + "\r\n" + "Example of a request body:\r\n" + "{\"task_type\":\"LOAD\",\"logicalUnits\":[\"Customer\",\"Orders\"]}")
    @webService(path = "task/getReferenceTaskTable", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\"result\":[{\"ref_table_name\":\"CUSTOMER_TYPE\",\"lu_name\":\"Customer\",\"interface_name\":\"CRM_DB\",\"schema_name\":\"public\"}],\"errorCode\":\"SUCCESS\",\"message\":null}")
    public static Object wsGetReferenceTaskTable(String beName, String task_type, Boolean version_ind) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        List<String> logicalUnits = new ArrayList<>();

        String message = null;
        String errorCode = "";
        try {

            if (beName != null && !beName.isEmpty()) {
                Db.Rows luRows = fabric().fetch("SELECT pl.lu_name FROM " + TDMDB_SCHEMA + ".BUSINESS_ENTITIES be, " + TDMDB_SCHEMA + ".PRODUCT_LOGICAL_UNITS pl " + "WHERE be.be_name = ? AND be.be_status = 'Active' AND be.be_id = pl.be_id");

                for (Db.Row row : luRows) {
                    logicalUnits.add(row.get("lu_name").toString());
                }
            }
            if (task_type.equals("EXTRACT") || (task_type.equals("LOAD") && (version_ind == null || !version_ind))) {
                String luName;
                String prevLuName = "";
                List<Object> refTablesList = new ArrayList<Object>();

                String broadwayCommand = "broadway TDM.refListLookup;";
                Db.Rows rows = fabric().fetch(broadwayCommand);
                for (Db.Row row : rows) {
                    Iterable<? extends Map<?, ?>> maps = ParamConvertor.toIterableOf(row.get("result"), ParamConvertor::toMap);
                    for (Map<?, ?> map : maps) {
                        luName = "" + map.get("lu_name");
                        if (logicalUnits.contains(luName)) {
                            Map<String, Object> refInfo = new HashMap<>();
                            refInfo.put("logical_unit_name", luName);
                            Set fields = map.keySet();
                            for (Object f : fields) {
                                if ("schema_name".equalsIgnoreCase(f.toString())) {
                                     String schemaName = "" + map.get("schema_name");
                                     if (schemaName.startsWith("@")) {
                                        String globalName = schemaName.replaceAll("@", "");
                                        schemaName = getGlobal(globalName, luName);
                                    }
                                    refInfo.put("schema_name", schemaName);
                                } else {
                                    refInfo.put((String) f, map.get(f));
                                }
                            }
                            refTablesList.add(refInfo);
                        }
                    }

                }
                List<Object> ans = new ArrayList<>();
                for (Object ref : refTablesList) {
                    ((Map<String, Object>) ref).put("reference_table_name", ((Map<String, String>) ref).get("reference_table_name").trim());
                    ans.add(ref);
                }

                response.put("result", ans);
                if (rows != null) {
                    rows.close();
                }
            } else if (task_type.equals("LOAD") && version_ind != null && version_ind) {
                List<Map<String, Object>> resultList = fnGetRefTableForLoadWithVersion(logicalUnits);
                response.put("result", resultList);
            }
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


	@desc("Get the list of the available data versions that match the input parameters. The user can select one of the data versions for the Data Versioning load task and reload the entities in the selected versions to the target environment. The API filters our expired data versions. For example, a data version that is created with a retention period of 5 days, will no longer be able to be retrieved after 6 days have passed.\r\n" +
			"\r\n" +
			"Note that the from date and to date inputs must be populated with \"MM.DD.YYYY\" or \"MM-DD-YYYYY\" date formats. \r\n" +
			"\r\n" +
			"Request BODY examples:\r\n" +
			"\r\n" +
			"{\r\n" +
			"\t\"fromDate\":\"2022-02-18\",\r\n" +
			"\t\"toDate\":\"2022-03-21\",\r\n" +
			"\t\"entitiesList\":\"\",\r\n" +
			"\t\"lu_list\":[\r\n" +
			"\t\t{\"lu_name\":\"Customer\"},\r\n" +
			"\t\t{\"lu_name\":\"Billing\"},\r\n" +
			"\t\t{\"lu_name\":\"Orders\"},\r\n" +
			"\t\t{\"lu_name\":\"Collection\"}],\r\n" +
			"\t\"source_env_name\":\"TAR\",\r\n" +
			"\t\"target_env_name\":\"TAR\",\"be_id\":1\r\n" +
			"}\r\n" +
			"\r\n" +
			"\r\n" +
			"{\r\n" +
			"\t\"fromDate\":\"2022-02-18\",\r\n" +
			"\t\"toDate\":\"2022-03-21\",\r\n" +
			"\t\"entitiesList\":\"1, 2\",\r\n" +
			"\t\"lu_list\":[\r\n" +
			"\t\t{\"lu_name\":\"Customer\"},\r\n" +
			"\t\t{\"lu_name\":\"Billing\"},\r\n" +
			"\t\t{\"lu_name\":\"Orders\"},\r\n" +
			"\t\t{\"lu_name\":\"Collection\"}],\r\n" +
			"\t\"source_env_name\":\"TAR\",\r\n" +
			"\t\"target_env_name\":\"TAR\",\"be_id\":1\r\n" +
			"}")
            
	@webService(path = "tasks/versionsForLoad", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": {\r\n" +
			"    \"EntityReservationValidations\": {},\r\n" +
			"    \"ListOfVersions\": [\r\n" +
			"      {\r\n" +
			"        \"task_id\": 59,\r\n" +
			"        \"task_execution_id\": 208,\r\n" +
			"        \"task_last_updated_by\": \"harry\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 104,\r\n" +
			"        \"lu_name\": \"Collection\",\r\n" +
			"        \"fabric_execution_id\": \"41079b2d-55ab-4ed7-9f74-e18941d5296e\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 104,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 59,\r\n" +
			"        \"task_execution_id\": 208,\r\n" +
			"        \"task_last_updated_by\": \"harry\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 221,\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"fabric_execution_id\": \"eb4f3157-3c79-4138-a719-97840f8e3d0e\",\r\n" +
			"        \"root_indicator\": \"Y\",\r\n" +
			"        \"num_of_succeeded_entities\": 221,\r\n" +
			"        \"num_of_failed_entities\": 279,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 59,\r\n" +
			"        \"task_execution_id\": 208,\r\n" +
			"        \"task_last_updated_by\": \"harry\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 748,\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"fabric_execution_id\": \"30622d98-d983-40eb-884b-c496642aa7c2\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 748,\r\n" +
			"        \"num_of_failed_entities\": 27,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 59,\r\n" +
			"        \"task_execution_id\": 208,\r\n" +
			"        \"task_last_updated_by\": \"harry\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 1048,\r\n" +
			"        \"lu_name\": \"Orders\",\r\n" +
			"        \"fabric_execution_id\": \"2da473d7-c795-443b-9b90-610c958a19b2\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 1048,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 1\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 209,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 112,\r\n" +
			"        \"lu_name\": \"Collection\",\r\n" +
			"        \"fabric_execution_id\": \"8b6aef81-f632-48c7-8db6-2423cfb1a23f\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 112,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 2\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 209,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 253,\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"fabric_execution_id\": \"ef84e092-9683-4f5a-b494-9d06b8aee711\",\r\n" +
			"        \"root_indicator\": \"Y\",\r\n" +
			"        \"num_of_succeeded_entities\": 253,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 2\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 209,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 861,\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"fabric_execution_id\": \"665d6de7-caca-4d52-9e6a-aca57ef28994\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 861,\r\n" +
			"        \"num_of_failed_entities\": 27,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 2\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 209,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 1109,\r\n" +
			"        \"lu_name\": \"Orders\",\r\n" +
			"        \"fabric_execution_id\": \"4790ccfa-37d5-48f8-8b15-e1fc7a041ed6\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 1109,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 2\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 210,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 104,\r\n" +
			"        \"lu_name\": \"Collection\",\r\n" +
			"        \"fabric_execution_id\": \"0f0c893c-182c-4bb8-938d-0529064b21c2\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 104,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 3\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 210,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 221,\r\n" +
			"        \"lu_name\": \"Customer\",\r\n" +
			"        \"fabric_execution_id\": \"63644180-f674-4d21-a0b1-05677dc39fb5\",\r\n" +
			"        \"root_indicator\": \"Y\",\r\n" +
			"        \"num_of_succeeded_entities\": 221,\r\n" +
			"        \"num_of_failed_entities\": 279,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 3\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 210,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 748,\r\n" +
			"        \"lu_name\": \"Billing\",\r\n" +
			"        \"fabric_execution_id\": \"e18117d0-c599-4e11-ab60-2cc153c2cbd6\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 748,\r\n" +
			"        \"num_of_failed_entities\": 27,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 3\r\n" +
			"      },\r\n" +
			"      {\r\n" +
			"        \"task_id\": 60,\r\n" +
			"        \"task_execution_id\": 210,\r\n" +
			"        \"task_last_updated_by\": \"admin\",\r\n" +
			"        \"version_type\": \"Selected Entities\",\r\n" +
			"        \"number_of_extracted_entities\": 1048,\r\n" +
			"        \"lu_name\": \"Orders\",\r\n" +
			"        \"fabric_execution_id\": \"8c27aeb7-ccd8-4b7a-bb2a-d2cfddcc90a3\",\r\n" +
			"        \"root_indicator\": \"N\",\r\n" +
			"        \"num_of_succeeded_entities\": 1048,\r\n" +
			"        \"num_of_failed_entities\": 0,\r\n" +
			"        \"execution_note\": null,\r\n" +
			"        \"version_no\": 3\r\n" +
			"      }\r\n" +
			"    ]\r\n" +
			"  },\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetVersionsForLoad(String entitiesList, Long be_id, String source_env_name, String fromDate, String toDate, List<String> lu_list, String target_env_name,String filterout_reserved) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
		    entitiesList = entitiesList.replaceAll("\\s+", "");
		    Map<String, Object> versions = fnGetVersionsForLoad(entitiesList, be_id, source_env_name, fromDate, toDate, lu_list, target_env_name,filterout_reserved);
		
		    Map<String, Object> validations = (Map<String, Object>) versions.get("EntityReservationValidations");
		    message = (String) validations.get("message");
		    response.put("result", versions);
		
		    errorCode = "SUCCESS";
		} catch (Exception e) {
		    message = e.getMessage();
		    e.printStackTrace();
		    log.error(message);
		    errorCode = "FAILED";
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}


    @desc("Returns the list of executions of the input task based on the input filtering parameters. Returns one summary record for each execution.")
    @webService(path = "task/{taskId}/summary", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"be_id\": 1,\r\n" + "      \"environment_id\": 1,\r\n" + "      \"tot_num_of_succeeded_post_executions\": null,\r\n" + "      \"task_execution_id\": 1,\r\n" + "      \"task_id\": 1,\r\n" + "      \"source_environment_id\": 1,\r\n" + "      \"version_datetime\": \"2020-10-01 08:29:29.170\",\r\n" + "      \"execution_status\": \"failed\",\r\n" + "      \"source_env_name\": \"ENV1\",\r\n" + "      \"tot_num_of_processed_root_entities\": 0,\r\n" + "      \"environment_name\": \"SRC\",\r\n" + "      \"tot_num_of_failed_ref_tables\": 0,\r\n" + "      \"start_execution_time\": \"1970-01-01 00:00:00.000\",\r\n" + "      \"tot_num_of_processed_post_executions\": null,\r\n" + "      \"creation_date\": \"2020-10-01 08:29:29.173\",\r\n" + "      \"tot_num_of_copied_root_entities\": 0,\r\n" + "      \"be_name\": \"BE\",\r\n" + "      \"tot_num_of_copied_ref_tables\": 0,\r\n" + "      \"update_date\": \"2020-12-30 08:40:17.576\",\r\n" + "      \"tot_num_of_failed_post_executions\": null,\r\n" + "      \"expiration_date\": null,\r\n" + "      \"end_execution_time\": \"1970-01-01 00:00:00.000\",\r\n" + "      \"tot_num_of_processed_ref_tables\": 0,\r\n" + "      \"task_type\": \"EXTRACT\",\r\n" + "      \"tot_num_of_failed_root_entities\": 0,\r\n" + "      \"task_executed_by\": \"admin\"\r\n" + "    },\r\n" + "    {\r\n" + "      \"be_id\": 1,\r\n" + "      \"environment_id\": 1,\r\n" + "      \"tot_num_of_succeeded_post_executions\": null,\r\n" + "      \"task_execution_id\": 3,\r\n" + "      \"task_id\": 1,\r\n" + "      \"source_environment_id\": 1,\r\n" + "      \"version_datetime\": \"2020-10-12 20:06:25.487\",\r\n" + "      \"execution_status\": \"completed\",\r\n" + "      \"source_env_name\": \"ENV1\",\r\n" + "      \"tot_num_of_processed_root_entities\": 5,\r\n" + "      \"environment_name\": \"SRC\",\r\n" + "      \"tot_num_of_failed_ref_tables\": 0,\r\n" + "      \"start_execution_time\": \"2020-10-12 20:06:29.000\",\r\n" + "      \"tot_num_of_processed_post_executions\": null,\r\n" + "      \"creation_date\": \"2020-10-12 20:06:25.539\",\r\n" + "      \"tot_num_of_copied_root_entities\": 5,\r\n" + "      \"be_name\": \"BE\",\r\n" + "      \"tot_num_of_copied_ref_tables\": 0,\r\n" + "      \"update_date\": \"2020-10-12 20:06:46.098\",\r\n" + "      \"tot_num_of_failed_post_executions\": null,\r\n" + "      \"expiration_date\": null,\r\n" + "      \"end_execution_time\": \"2020-10-12 20:06:31.000\",\r\n" + "      \"tot_num_of_processed_ref_tables\": 0,\r\n" + "      \"task_type\": \"EXTRACT\",\r\n" + "      \"tot_num_of_failed_root_entities\": 0,\r\n" + "      \"task_executed_by\": \"admin\"\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetSummaryTaskHistory(@param(required = true) Long taskId, @param(description = "Limits the number of task executions returned by the API. For example, if this parameter is populated by 5, the API only returns the last five task executions.") Long numberOfExecutions, @param(description = "Returns the task executions of the input user if populated. Else, returns the task executions of all users.") String userId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            String query = "SELECT DISTINCT summary.*, envs.environment_name, be.be_name, list.execution_note " + "FROM " + TDMDB_SCHEMA + ".task_execution_summary AS summary " + "INNER JOIN " + TDMDB_SCHEMA + ".task_execution_list AS list ON summary.task_execution_id = list.task_execution_id and list.parent_lu_id is null " + "INNER JOIN " + TDMDB_SCHEMA + ".environments AS envs ON summary.environment_id = envs.environment_id " + "LEFT JOIN " + TDMDB_SCHEMA + ".business_entities AS be ON (summary.be_id = be.be_id) " + "WHERE summary.task_id = " + taskId + (userId != null ? " AND summary.task_executed_by = '" + userId + "'" : "") + (numberOfExecutions != null ? " ORDER BY end_execution_time DESC LIMIT " + numberOfExecutions + "" : "");

            Db.Rows rows = db(TDM).fetch(query);

            List<Map<String, Object>> result = new ArrayList<>();
            List<String> columnNames = rows.getColumnNames();

            for (Db.Row row : rows) {

                ResultSet resultSet = row.resultSet();
                Map<String, Object> rowMap = new HashMap<>();

                for (String columnName : columnNames) {
                    rowMap.put(columnName, resultSet.getObject(columnName));
                }
                result.add(rowMap);
            }

            response.put("result", result);
            errorCode = "SUCCESS";
            rows.close();

        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("Gets task execution details:\r\n" + "\r\n" + "> List of copied and Failed Entities.\r\n" + "\r\n" + "> Task's Logical Units hierarchy tree.\r\n" + "\r\n" + "> List of copied and failed reference tables.\r\n" + "\r\n" + "Examples of a request body:\r\n" + "\r\n" + "> Get the execution details of the root Logical Unit: \r\n" + "{taskExecutionId: \"69\"}\r\n" + "\r\n" + "> Get the execution details of a selected Logical Unit:\r\n" + "{taskExecutionId: \"69\", lu_name: \"PATIENT_VISITS\"}\r\n" + "\r\n" + "> Gets the execution details of a selected Logical Unit and entity ID:\r\n" + "{taskExecutionId: \"69\", targetId: \"400\", lu_name: \"PATIENT_VISITS\"}")
    @webService(path = "taskStats", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "1. Task Execution Level (Root Logical Unit)- \r\n" + "\r\n" + "{\"result\":{\"luTree\":[{\"isRoot\":true,\"test\":true,\"hasChildren\":true,\"collapsed\":true,\"lu_name\":\"PATIENT_LU\",\"task_execution_id\":69,\"count\":1,\"lu_id\":1,\"test1\":true,\"lu_status\":\"completed\",\"selected\":true,\"status\":\"completed\"}],\"data\":{\"Copied entities per execution\":{\"entitiesList\":[{\"sourceId\":\"1\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"1\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"2\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"2\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"3\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"3\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"4\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"4\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"5\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"5\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"6\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"6\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"6\",\"rootTargetId\":\"6\"},{\"sourceId\":\"7\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"7\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"8\",\"parentLuName\":\"\",\"parentTargetId\":\"\",\"targetId\":\"8\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_LU\",\"parentSourceId\":\"\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"}],\"NoOfEntities\":\"8\"},\"Failed entities per execution\":{\"entitiesList\":[],\"NoOfEntities\":\"0\"},\"Copied Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Failed Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Roots Status\":{\"PATIENT_LU\":\"completed\"}}},\"errorCode\":\"SUCCESS\",\"message\":null}\r\n" + "\r\n" + "2. Logical Unit Level - \r\n" + "\r\n" + "{\"result\":{\"data\":{\"Copied entities per execution\":{\"entitiesList\":[{\"sourceId\":\"24900\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"24900\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"24901\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"24901\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"24902\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"24902\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"24903\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"24903\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"},{\"sourceId\":\"24913\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24913\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24914\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24914\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24915\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24915\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24916\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24916\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24917\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24917\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24918\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24918\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24919\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24919\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24920\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24920\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24921\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"2\",\"targetId\":\"24921\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"2\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"2\",\"rootTargetId\":\"2\"},{\"sourceId\":\"24925\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24925\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24926\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24926\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24927\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24927\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24928\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24928\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24929\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"4\",\"targetId\":\"24929\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"4\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"4\",\"rootTargetId\":\"4\"},{\"sourceId\":\"24934\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24934\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24935\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24935\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24936\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24936\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24937\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24937\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24938\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24938\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24939\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24939\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24940\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"7\",\"targetId\":\"24940\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"7\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"7\",\"rootTargetId\":\"7\"},{\"sourceId\":\"24959\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"6\",\"targetId\":\"24959\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"6\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"6\",\"rootTargetId\":\"6\"},{\"sourceId\":\"24960\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"6\",\"targetId\":\"24960\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"6\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"6\",\"rootTargetId\":\"6\"},{\"sourceId\":\"24963\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24963\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24964\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24964\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24965\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24965\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24967\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24967\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24969\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24969\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24971\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24971\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24973\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24973\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"24974\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"8\",\"targetId\":\"24974\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"8\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"8\",\"rootTargetId\":\"8\"},{\"sourceId\":\"25072\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25072\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25085\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25085\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25089\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25089\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25092\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25092\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25094\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"5\",\"targetId\":\"25094\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"5\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"5\",\"rootTargetId\":\"5\"},{\"sourceId\":\"25112\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25112\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25114\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25114\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25115\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25115\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25116\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25116\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25120\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25120\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25122\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25122\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"25134\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"3\",\"targetId\":\"25134\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"3\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"3\",\"rootTargetId\":\"3\"},{\"sourceId\":\"400\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"400\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"}],\"NoOfEntities\":\"48\"},\"Failed entities per execution\":{\"entitiesList\":[],\"NoOfEntities\":\"0\"},\"Copied Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Failed Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Roots Status\":{\"PATIENT_LU\":\"completed\"}}},\"errorCode\":\"SUCCESS\",\"message\":null} \r\n" + "\r\n" + "3. Logical Unit and Entity ID level -\r\n" + "\r\n" + "{\"result\":{\"data\":{\"Copied entities per execution\":{\"entitiesList\":[{\"sourceId\":\"400\",\"parentLuName\":\"PATIENT_LU\",\"parentTargetId\":\"1\",\"targetId\":\"400\",\"copyEntityStatus\":\"Copied\",\"luName\":\"PATIENT_VISITS\",\"parentSourceId\":\"1\",\"copyHierarchyStatus\":\"Copied\",\"rootSourceId\":\"1\",\"rootTargetId\":\"1\"}],\"NoOfEntities\":\"1\"},\"Failed entities per execution\":{\"entitiesList\":[],\"NoOfEntities\":\"0\"},\"Copied Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Failed Reference per execution\":{\"entitiesList\":[],\"NoOfEntities\":0},\"Roots Status\":{\"PATIENT_LU\":\"completed\"}}},\"errorCode\":\"SUCCESS\",\"message\":null}")
    public static Object wsGetTaskStats(String targetId, String parentTargetId, String taskExecutionId, String lu_name, String entityId, String type) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        // TDM 7.3 - Get Max number of entries to be returned from Global
        Integer maxEntitiesSize = Integer.parseInt(MAX_NUMBER_OF_ENTITIES_IN_LIST);
        String selMtdSql = "select selection_method from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".task_execution_list l " +
            "where l.task_execution_id = ? and l.task_id = t.task_id limit 1";
        
        Boolean tableLevelInd = false;
        String selectionMethod = "" + db(TDM).fetch(selMtdSql, taskExecutionId).firstValue();
        if (TABLES.equals(selectionMethod)) {
            lu_name = TABLE_LEVEL_LU;
            tableLevelInd = true;
        }
        if (targetId != null || parentTargetId != null) {

            try {
                Map<String, Map> statsData = (Map<String, Map>) ((Map<String, Object>) fnGetTDMTaskExecutionStats(taskExecutionId.toString(), lu_name, targetId != null ? targetId : null, "ENTITY", parentTargetId != null ? parentTargetId : null, maxEntitiesSize, "true")).get("result");
                response.put("errorCode", "SUCCESS");
                response.put("message", null);
                HashMap<String, Object> data = new HashMap<>();
                data.put("data", statsData);
                data.put("tableLevelInd", tableLevelInd);
                response.put("result", data);
                return response;
            } catch (Exception e) {
                response.put("errorCode", "FAILED");
                response.put("message", e.getMessage());
                return response;
            }
        }

        if (lu_name == null) {
            List<Map<String, Object>> tree = fnGetRootLUs(taskExecutionId.toString());
            if (tree == null || tree.size() == 0) {
                response.put("errorCode", "SUCCESS");
                response.put("message", null);
                response.put("result", new ArrayList<>());
                return response;
            }

            tree.get(0).put("selected", true);
            Map<String, Object> taskStatsAns = new HashMap<>();

            for (Map<String, Object> root_lu : tree) {
                Map<String, Map> statsData = (Map<String, Map>) ((Map<String, Object>) fnGetTDMTaskExecutionStats(taskExecutionId.toString(), root_lu.get("lu_name").toString(), null, "ENTITY", null, maxEntitiesSize, "true")).get("result");
                Map<String, Object> dataObj = new HashMap<>();
                dataObj.put("data", statsData);
                taskStatsAns.put(root_lu.get("lu_name").toString(), dataObj);
            }
            //iterate through root lus
            for (String key : taskStatsAns.keySet()) {
                Map<String, Object> statsObj = (Map<String, Object>) taskStatsAns.get(key);
                if (statsObj == null) {
                    continue;
                }
                //tree.get(0).put("test1",true);
                //statsObj{data:Map<String,Map>}
                Map<String, Map> stats_Data = (Map<String, Map>) statsObj.get("data");
                Map<String, Object> statsDataFailedEntities = null;
                if (stats_Data != null)
                    statsDataFailedEntities = (Map<String, Object>) stats_Data.get("Failed entities per execution");
                if (stats_Data != null && statsDataFailedEntities != null && Long.parseLong(statsDataFailedEntities.get("NoOfEntities").toString()) > 0) {
                    tree = fnUpdateFailedLUsInTree(tree, statsDataFailedEntities);
                } else {
                    for (Map<String, Object> node : tree) {
                        if (node.get("count") != null && Long.parseLong(node.get("count").toString()) > 0) {
                            node.put("hasChildren", true);
                            node.put("collapsed", true);
                        }
                        node.put("isRoot", true);
                    }
                }

                for (Map<String, Object> node : tree) {
                    Map<String, Map> statsData = (Map<String, Map>) statsObj.get("data");
                    Map<String, String> statsDateRootsStatus = (Map<String, String>) statsData.get("Roots Status");
                    node.put("status", statsDateRootsStatus != null ? statsDateRootsStatus.get(node.get("lu_name")) : null);
                    //node.put("test",true);
                }
            }

            Map<String, Object> responseObject = new HashMap<>();

            Map<String, Object> lu = (Map<String, Object>) taskStatsAns.get(tree.get(0).get("lu_name"));
            responseObject.put("data", lu.get("data"));
            responseObject.put("luTree", tree);

            responseObject.put("tableLevelInd", tableLevelInd);
            response.put("result", responseObject);
            response.put("errorCode", "SUCCESS");
            response.put("message", null);
            return response;
        } else {
            String luIdType = "ENTITY";
            if (TABLE_LEVEL_LU.equals(lu_name)) {
                luIdType = "REFERENCE";
            }
            Map<String, Map> data = (Map<String, Map>) ((Map<String, Object>) fnGetTDMTaskExecutionStats(taskExecutionId.toString(), lu_name, entityId != null ? entityId : null, luIdType, null, maxEntitiesSize, "true")).get("result");
            Map<String, Object> dataObj = new HashMap<>();
            dataObj.put("data", data);
            if (type != null) {
                HashMap<String, Object> returnedData = new HashMap<>();
                returnedData.put(type, dataObj.get("data") != null ? ((Map<String, Map>) dataObj.get("data")).get(type) : null);
                dataObj.put("data", returnedData);
                dataObj.put("tableLevelInd", tableLevelInd);
                response.put("result", dataObj);
                response.put("errorCode", "SUCCESS");
                response.put("message", null);
                return response;
            }
            dataObj.put("tableLevelInd", tableLevelInd);
            response.put("result", dataObj);
            response.put("errorCode", "SUCCESS");
            response.put("message", null);
            return response;
        }
    }


    @desc("Returns the list children LUs of a parent LU in a task execution. This API is invoked when navigating the Logical Units Hierarchy  tab of a selected task execution.")
    @webService(path = "luChildren", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"collapsed\": true,\r\n" + "      \"lu_name\": \"PATIENT_VISITS\",\r\n" + "      \"task_execution_id\": 161,\r\n" + "      \"count\": 0,\r\n" + "      \"lu_id\": 12,\r\n" + "      \"lu_status\": \"completed\"\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}\r\n" + "Response headers")
    public static Object wsGetLUChildren(Long taskExecutionId, String lu_name) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            String query = "select t.task_execution_id, l.lu_id, lu_name, " + "(select count(*) from " + TDMDB_SCHEMA + ".task_Execution_list t where parent_lu_id = l.lu_id and " + "t.task_execution_id = " + taskExecutionId + "), " + "case when (num_of_failed_entities > 0 or num_of_failed_ref_tables > 0) " + "then 'failed' else 'completed' end lu_status from " + TDMDB_SCHEMA + ".task_Execution_list t, " + "(select lu_id, lu_name, lu_parent_name from " + TDMDB_SCHEMA + ".product_logical_units) l " + "where t.task_execution_id =" + taskExecutionId + " and " + "l.lu_parent_name = '" + lu_name + "' and t.lu_id = l.lu_id ";
            Db.Rows rows = db(TDM).fetch(query);

            //if(true) return rows;

            List<Map<String, Object>> result = new ArrayList<>();
            List<String> columnNames = rows.getColumnNames();
            for (Db.Row row : rows) {
                ResultSet resultSet = row.resultSet();
                Map<String, Object> rowMap = new HashMap<>();
                for (String columnName : columnNames) {
                    rowMap.put(columnName, resultSet.getObject(columnName));
                }
                result.add(rowMap);
            }

            for (Map<String, Object> node : result) {
                if (node.get("count") != null && Integer.parseInt(node.get("count").toString()) > 0) {
                    node.put("hasChildren", true);
                }
                node.put("collapsed", true);
            }

            response.put("result", result);
            errorCode = "SUCCESS";
            if (rows != null) {
                rows.close();
            }
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("Returns the list of all executed Logical Units and Post Execution Processes of the input task execution id.")
    @webService(path = "task/{taskExeId}/history", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
            {
              "summary": {
                "source_environment_name": "Production",
                "target_environment_name": "Production",
                "be_name": "Customer",
                "task_executed_by": "admin",
                "task_type_label": "Extract",
                "start_time": "2026-05-26 09:00:07.04126",
                "end_time": "2026-05-26 09:00:40.055341",
                "expiration_date": null,
                "execution_note": null,
                "task_title": "Extract entities"
              },
              "result": [
                {
                  "task_execution_id": 312,
                  "execution_status": "completed",
                  "execution_mode": "HORIZONTAL",
                  "fabric_execution_id": "Extract entities_8bdc486e-5fa3-41e",
                  "num_of_entities": 5,
                  "num_of_copied_entities": 5,
                  "num_of_failed_entities": 0,
                  "num_of_processed_entities": 5,
                  "num_of_copied_ref_tables": null,
                  "num_of_failed_ref_tables": null,
                  "num_of_processed_ref_tables": null,
                  "refcount": 0,
                  "start_execution_time": "2026-05-26 09:00:07.04126",
                  "end_execution_time": "2026-05-26 09:00:40.055341",
                  "source_env_name": "Production",
                  "environment_name": "Production",
                  "selection_method": "ALL",
                  "creation_date": "2026-05-26 08:59:57.103",
                  "expiration_date": null,
                  "sync_mode": "ON",
                  "process_name": null,
                  "lu_name": "Customer",
                  "lu_id": 1,
                  "lu_parent_id": null,
                  "lu_parent_name": null,
                  "product_name": "CRM",
                  "product_description": "CRM Application",
                  "process_id": 0,
                  "process_type": null,
                  "execution_note": null
                },
                {
                  "task_execution_id": 312,
                  "execution_status": "completed",
                  "execution_mode": "HORIZONTAL",
                  "fabric_execution_id": null,
                  "num_of_entities": 17,
                  "num_of_copied_entities": 17,
                  "num_of_failed_entities": 0,
                  "num_of_processed_entities": 17,
                  "num_of_copied_ref_tables": null,
                  "num_of_failed_ref_tables": null,
                  "num_of_processed_ref_tables": null,
                  "refcount": 0,
                  "start_execution_time": "2026-05-26 09:00:12.218769",
                  "end_execution_time": "2026-05-26 09:00:40.055341",
                  "source_env_name": "Production",
                  "environment_name": "Production",
                  "selection_method": "ALL",
                  "creation_date": "2026-05-26 08:59:57.103",
                  "expiration_date": null,
                  "sync_mode": "ON",
                  "process_name": null,
                  "lu_name": "Billing",
                  "lu_id": 2,
                  "lu_parent_id": 1,
                  "lu_parent_name": "Customer",
                  "product_name": "Billing",
                  "product_description": "Billing Application",
                  "process_id": 0,
                  "process_type": null,
                  "execution_note": null
                }
              ],
              "errorCode": "SUCCESS",
              "message": null
            }
            """)
    public static Object wsGetTaskHistory(@param(required = true) Long taskExeId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "SUCCESS";

        String summaryQuery = "SELECT be.be_name, s.environment_id as target_environment_id , s.source_environment_id, " +
                              "s.env_name as target_environment_name, s.source_env_name as source_environment_name, s.task_executed_by, s.task_type_label, " +
                              "s.start_execution_time AS final_start_time, " +
                              "s.end_execution_time AS final_end_time, " +
                              "s.expiration_date " +
                              "FROM " + TDMDB_SCHEMA + ".task_execution_summary s " +
                              "LEFT JOIN " + TDMDB_SCHEMA + ".business_entities be " + "ON (s.be_id = be.be_id)  " +
                              "WHERE task_execution_id = ?";

        Map<String, Object> summaryResult = new HashMap<>();
        try (Db.Rows summaryRows = db(TDM).fetch(summaryQuery, taskExeId)) {
            for (Db.Row summaryRow : summaryRows) {
                ResultSet summaryRs = summaryRow.resultSet();
                summaryResult.put("source_environment_name", summaryRs.getObject("source_environment_name"));
                summaryResult.put("target_environment_name", summaryRs.getObject("target_environment_name"));
                summaryResult.put("be_name", summaryRs.getObject("be_name"));
                summaryResult.put("task_executed_by", summaryRs.getObject("task_executed_by"));
                summaryResult.put("task_type_label", summaryRs.getObject("task_type_label"));
                summaryResult.put("start_time", summaryRs.getObject("final_start_time"));
                summaryResult.put("end_time", summaryRs.getObject("final_end_time"));
                break;
            }
        }

        String query = "SELECT " +
                        "el.task_execution_id, el.task_id, el.lu_id, COALESCE(lu.lu_name, plu.lu_name) AS lu_name, " +
                        "el.parent_lu_id, COALESCE(parent_lu.lu_name, parent_plu.lu_name) AS parent_lu_name, el.be_id, el.process_id, " +
                        "el.execution_status, t.execution_mode, el.fabric_execution_id, " +
                        "el.num_of_copied_entities, el.num_of_failed_entities, el.num_of_processed_entities, " +
                        "el.num_of_copied_ref_tables, el.num_of_failed_ref_tables, el.num_of_processed_ref_tables, " +
                        "el.start_execution_time, el.end_execution_time, " +
                        "el.source_env_name, el.env_name AS environment_name, " +
                        "el.execution_note, " +
                        "t.num_of_entities, t.task_type, t.version_ind, t.task_title, t.clone_ind, " +
                        "t.selection_method, t.task_creation_date, el.expiration_date, t.sync_mode, " +
                        "p.product_name, p.product_description, " +
                        "pep.process_name, pep.process_type, " +
                        "CASE " +
                        "WHEN el.process_id > 0 THEN NULL " +
                        "WHEN el.parent_lu_id IS NOT NULL THEN el.num_of_processed_entities " +
                        "ELSE t.num_of_entities " +
                        "END AS fixed_num_of_entities, " +
                        "( SELECT COUNT(*) FROM " + TDMDB_SCHEMA
                            + ".task_ref_tables rt WHERE rt.task_id = el.task_id " +
                            "AND rt.lu_name IN (SELECT lu_name FROM " + TDMDB_SCHEMA + ".tasks_logical_units tlu " +
                            "WHERE tlu.lu_id = el.lu_id)) AS refcount " +
                            "FROM " + TDMDB_SCHEMA + ".task_execution_list el " +
                            "LEFT JOIN " + TDMDB_SCHEMA + ".products p ON (el.product_id = p.product_id) " +
                            "INNER JOIN " + TDMDB_SCHEMA + ".tasks t ON (el.task_id = t.task_id) " +
                            "LEFT JOIN " + TDMDB_SCHEMA + ".tasks_logical_units lu ON (lu.lu_id = el.lu_id AND lu.task_id = el.task_id) " +
                            "LEFT JOIN " + TDMDB_SCHEMA + ".tasks_logical_units parent_lu ON (parent_lu.lu_id = el.parent_lu_id AND parent_lu.task_id = el.task_id) " +
                            "LEFT JOIN " + TDMDB_SCHEMA + ".product_logical_units plu ON (plu.lu_id = el.lu_id) " +
                            "LEFT JOIN " + TDMDB_SCHEMA + ".product_logical_units parent_plu ON (parent_plu.lu_id = el.parent_lu_id) " +
                            "LEFT JOIN " + TDMDB_SCHEMA
                            + ".tasks_exe_process pep ON (el.process_id = pep.process_id AND el.task_id = pep.task_id) " +
                            "WHERE el.task_execution_id = ? ";

        List<Map<String, Object>> result = new ArrayList<>();

        try (Db.Rows rows = db(TDM).fetch(query, taskExeId)) {
            Boolean mainBatchStarted = true;
            boolean summaryPostFilled = false;

            for (Db.Row row : rows) {
                ResultSet resultSet = row.resultSet();
                Map<String, Object> rowMap = new HashMap<>();

                String status = resultSet.getString("execution_status");
                if ("STARTEXECUTIONREQUESTED".equalsIgnoreCase(status)) {
                    status = "running";
                    mainBatchStarted = false;
                }
                rowMap.put("execution_status", status.toLowerCase());
                rowMap.put("task_execution_id", resultSet.getObject("task_execution_id"));
                rowMap.put("task_id", resultSet.getObject("task_id"));
                rowMap.put("lu_id", resultSet.getObject("lu_id"));
                rowMap.put("lu_name", resultSet.getObject("lu_name"));
                rowMap.put("parent_lu_id", resultSet.getObject("parent_lu_id"));
                rowMap.put("parent_lu_name", resultSet.getObject("parent_lu_name"));
                rowMap.put("be_id", resultSet.getObject("be_id"));
                rowMap.put("process_id", resultSet.getObject("process_id"));
                rowMap.put("execution_mode", resultSet.getObject("execution_mode"));
                rowMap.put("fabric_execution_id", resultSet.getObject("fabric_execution_id"));
                rowMap.put("num_of_copied_entities", resultSet.getObject("num_of_copied_entities"));
                rowMap.put("num_of_failed_entities", resultSet.getObject("num_of_failed_entities"));
                rowMap.put("num_of_processed_entities", resultSet.getObject("num_of_processed_entities"));
                rowMap.put("num_of_copied_ref_tables", resultSet.getObject("num_of_copied_ref_tables"));
                rowMap.put("num_of_failed_ref_tables", resultSet.getObject("num_of_failed_ref_tables"));
                rowMap.put("num_of_processed_ref_tables", resultSet.getObject("num_of_processed_ref_tables"));
                rowMap.put("start_execution_time", resultSet.getObject("start_execution_time"));
                rowMap.put("end_execution_time", resultSet.getObject("end_execution_time"));
                rowMap.put("source_env_name", resultSet.getObject("source_env_name"));
                rowMap.put("environment_name", resultSet.getObject("environment_name"));
                rowMap.put("execution_note", resultSet.getObject("execution_note"));
                rowMap.put("task_type", resultSet.getObject("task_type"));
                rowMap.put("version_ind", resultSet.getObject("version_ind"));
                rowMap.put("task_title", resultSet.getObject("task_title"));
                rowMap.put("clone_ind", resultSet.getObject("clone_ind"));
                rowMap.put("selection_method", resultSet.getObject("selection_method"));
                rowMap.put("creation_date", resultSet.getObject("task_creation_date"));
                rowMap.put("expiration_date", resultSet.getObject("expiration_date"));
                rowMap.put("sync_mode", resultSet.getObject("sync_mode"));
                rowMap.put("product_name", resultSet.getObject("product_name"));
                rowMap.put("product_description", resultSet.getObject("product_description"));
                rowMap.put("process_name", resultSet.getObject("process_name"));
                rowMap.put("process_type", resultSet.getObject("process_type"));
                rowMap.put("num_of_entities", resultSet.getObject("fixed_num_of_entities"));
                rowMap.put("refcount", resultSet.getObject("refcount"));
                if (!summaryPostFilled) {
                    if (!Boolean.TRUE.equals(rowMap.get("version_ind"))) {
                        summaryResult.put("expiration_date", null);
                    }
                    summaryResult.put("execution_note", rowMap.get("execution_note"));
                    summaryResult.put("task_title", rowMap.get("task_title"));
                    summaryPostFilled = true;
                }
                if (!mainBatchStarted) {
                    rowMap.put("fabric_execution_id", null);
                }

                boolean cloneInd = Boolean.parseBoolean(row.get("clone_ind").toString());
                String executionMode = fnGetTaskExecutionMode(row.get("execution_mode").toString(),
                        row.get("task_type").toString(),
                        Long.parseLong(row.get("be_id").toString()),
                        cloneInd);
               if ("VERTICAL".equalsIgnoreCase(executionMode) && row.get("parent_lu_id") != null) {
                    rowMap.put("fabric_execution_id", null);
                }
                rowMap.put("execution_mode", executionMode);
                Map<String, Object> taskOverrideAttrs = fnGetTaskExecOverrideAttrs((Long) row.get("task_id"),
                        (Long) row.get("task_execution_id"));
                Object overrideValue = new Object();
                String attrName = "";
                boolean hasEntityList = taskOverrideAttrs.containsKey(OverrideParamKey.ENTITY_LIST.name());
                for (String attr : taskOverrideAttrs.keySet()) {

                    if ("task_globals".equalsIgnoreCase(attr)) {
                        continue;
                    }
                    try {
                        OverrideParamKey key = OverrideParamKey.valueOf(attr);

                        overrideValue = taskOverrideAttrs.get(attr);
                        boolean isParentLu = row.get("parent_lu_id") == null;
                        attrName = attr.toLowerCase();
                        switch (key) {
                            case OverrideParamKey.SELECTION_METHOD:
                                rowMap.put(attrName, overrideValue);
                                break;
                            case OverrideParamKey.ENTITY_LIST:
                                rowMap.put("selection_param_value", overrideValue);
                                if (!cloneInd) {
                                    int numberOfEntities = String.valueOf(overrideValue).split(",", -1).length;
                                    rowMap.put("num_of_entities", numberOfEntities);
                                }
                                break;
                            case OverrideParamKey.CUSTOM_LOGIC_FLOW:
                                rowMap.put("selection_param_value", overrideValue);
                                break;
                            case OverrideParamKey.CUSTOM_LOGIC_LU_NAME:
                                rowMap.put("custom_logic_lu_name", overrideValue);
                                break;
                            case OverrideParamKey.BP_QUERY:
                                rowMap.put("selection_param_value", overrideValue);
                                break;
                            case OverrideParamKey.PARAMETERS:
                                rowMap.put("parameters", overrideValue);
                                break;
                            case OverrideParamKey.NO_OF_ENTITIES:
                                if ((!hasEntityList || cloneInd) && isParentLu) {
                                    rowMap.put("num_of_entities", overrideValue);
                                }
                                break;
                            case OverrideParamKey.SELECTED_VERSION_TASK_EXE_ID:
                                rowMap.put(attrName, overrideValue);
                                break;
                            case OverrideParamKey.SOURCE_ENVIRONMENT_NAME:
                                rowMap.put(attrName, overrideValue);
                                break;
                            case OverrideParamKey.TARGET_ENVIRONMENT_NAME:
                                rowMap.put(attrName, overrideValue);
                                break;
                            case OverrideParamKey.DATAFLUX_RETENTION_PARAMS:
                                Map rentionPeriodInfo = (Map) overrideValue;
                                rowMap.put("retention_period_type", "" + rentionPeriodInfo.get("units"));
                                rowMap.put("retention_period_value", "" + rentionPeriodInfo.get("value"));
                                break;
                            case OverrideParamKey.RESERVE_IND:
                                rowMap.put("reserve_ind", overrideValue);
                                break;
                            case OverrideParamKey.RESERVE_RETENTION_PARAMS:
                                Map reserveRentionPeriodInfo = (Map) overrideValue;
                                rowMap.put("reserve_retention_period_type",
                                        "" + reserveRentionPeriodInfo.get("units"));
                                rowMap.put("reserve_retention_period_value",
                                        "" + reserveRentionPeriodInfo.get("value"));
                                break;
                            case OverrideParamKey.IMPLICIT_OVERRIDE_LOGICAL_UNITS:
                                break;
                            default:
                                rowMap.put(attrName, overrideValue);
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid override attribute name: " + attr, e);
                    }
                }

                Object fabricExecId = rowMap.get("fabric_execution_id");
                if ("running".equals(rowMap.get("execution_status")) && fabricExecId != null
                        && !fabricExecId.toString().isEmpty()) {
                    try {
                        List<Map<String, Object>> batchSummary = (List<Map<String, Object>>) fnBatchStatistics(
                                fabricExecId.toString(), "S");
                        if (batchSummary != null) {
                            for (Map<String, Object> levelRow : batchSummary) {
                                if ("Cluster".equals(levelRow.get("Level"))) {
                                    String rawBatchStatus = levelRow.get("Status") != null
                                            ? levelRow.get("Status").toString()
                                            : "";
                                    String tdmStatus = null;
                                    if ("DONE".equals(rawBatchStatus)) {
                                        Long total = levelRow.get("Total") != null 
                                            ? (Long)levelRow.get("Total") : 0;
                                        Long failed = levelRow.get("Failed") != null
                                            ? (Long)levelRow.get("Failed") : 0;
                                        if (total > 0 && total == failed) {
                                            tdmStatus = "failed";
                                        } else {
                                            tdmStatus = "completed";
                                        }
                                    } else if ("CANCELLED".equals(rawBatchStatus)) {
                                        tdmStatus = "stopped";
                                    }
                                    if (tdmStatus != null) {
                                        rowMap.put("execution_status", tdmStatus);
                                        Object endTime = levelRow.get("End time");
                                        if (endTime != null && !endTime.toString().isEmpty()
                                                && !"null".equalsIgnoreCase(endTime.toString())) {
                                            rowMap.put("end_execution_time", endTime);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("wsGetTaskHistory: failed to fetch live batch status for " + fabricExecId + ": "
                                + e.getMessage());
                    }
                }

                Map<String, Object> filteredRow = new HashMap<>();
                filteredRow.put("task_execution_id", rowMap.get("task_execution_id"));
                filteredRow.put("execution_status", rowMap.get("execution_status"));
                filteredRow.put("execution_mode", rowMap.get("execution_mode"));
                filteredRow.put("fabric_execution_id", rowMap.get("fabric_execution_id"));
                filteredRow.put("num_of_entities", rowMap.get("num_of_entities"));
                filteredRow.put("num_of_copied_entities", rowMap.get("num_of_copied_entities"));
                filteredRow.put("num_of_failed_entities", rowMap.get("num_of_failed_entities"));
                filteredRow.put("num_of_processed_entities", rowMap.get("num_of_processed_entities"));
                filteredRow.put("num_of_copied_ref_tables", rowMap.get("num_of_copied_ref_tables"));
                filteredRow.put("num_of_failed_ref_tables", rowMap.get("num_of_failed_ref_tables"));
                filteredRow.put("num_of_processed_ref_tables", rowMap.get("num_of_processed_ref_tables"));
                filteredRow.put("refcount", rowMap.get("refcount"));
                filteredRow.put("start_execution_time", rowMap.get("start_execution_time"));
                filteredRow.put("end_execution_time", rowMap.get("end_execution_time"));
                filteredRow.put("source_env_name", rowMap.get("source_env_name"));
                filteredRow.put("environment_name", rowMap.get("environment_name"));
                filteredRow.put("selection_method", rowMap.get("selection_method"));
                filteredRow.put("creation_date", rowMap.get("creation_date"));
                filteredRow.put("expiration_date", rowMap.get("expiration_date"));
                filteredRow.put("sync_mode", rowMap.get("sync_mode"));
                filteredRow.put("process_name", rowMap.get("process_name"));
                Object processId = rowMap.get("process_id");
                filteredRow.put("lu_name", processId != null && ((Number) processId).longValue() < 0 ? null : rowMap.get("lu_name"));
                filteredRow.put("lu_id", rowMap.get("lu_id"));
                filteredRow.put("lu_parent_id", rowMap.get("parent_lu_id"));
                filteredRow.put("lu_parent_name", rowMap.get("parent_lu_name"));
                filteredRow.put("product_name", rowMap.get("product_name"));
                filteredRow.put("product_description", rowMap.get("product_description"));
                filteredRow.put("process_id", rowMap.get("process_id"));
                filteredRow.put("process_type", rowMap.get("process_type"));
                filteredRow.put("execution_note", rowMap.get("execution_note"));
                result.add(filteredRow);
            }
        }

        response.put("summary", summaryResult);
        response.put("result", result);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Gets the task execution summary report.")
    @webService(path = "taskSummaryReport/{executionId}/luName/{luName}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "Populate the LU Name with ALL:\r\n" + "==========================\r\n" + "\r\n" + "{\r\n" + "  \"result\": {\r\n" + "    \"General Info\": [\r\n" + "      {\r\n" + "        \"task_name\": \"createTaskByTester\",\r\n" + "        \"task_id\": 294,\r\n" + "        \"task_execution_id\": \"490\",\r\n" + "        \"created_by\": \"tali\",\r\n" + "        \"executed_by\": \"admin\",\r\n" + "        \"start_execution\": \"2021-06-16 16:24:21.0\",\r\n" + "        \"end_execution\": \"2021-06-16 16:24:32.0\",\r\n" + "        \"execution_status\": \"completed\",\r\n" + "        \"source_env\": \"SRC\",\r\n" + "        \"target_env\": \"TAR\",\r\n" + "        \"be_name\": \"CUSTOMER\",\r\n" + "        \"task_type\": \"LOAD\",\r\n" + "        \"selection_method\": \"Randon Selection\",\r\n" + "        \"task_sync_mode\": null,\r\n" + "        \"env_sync_mode\": \"ON\",\r\n" + "        \"operation_mode\": \"Delete and load entity\",\r\n" + "        \"replace_sequences\": \"false\",\r\n" + "        \"version_ind\": \"false\",\r\n" + "        \"scheduling_parameters\": \"immediate\",\r\n" + "        \"schedule_expiration_date\": null,\r\n" + "        \"override_parameters\": \"{\\\"TASK_GLOBALS\\\":{\\\"MASKING_FLAG\\\":\\\"0\\\", \\\"GLOBAL2\\\":\\\"aaaaaa\\\"},\\\"ENTITY_LIST\\\":\\\"1\\\"}\",\r\n" + "        \"reserve_ind\": \"true\",\r\n" + "        \"reserve_retention_period_type\": \"Days\",\r\n" + "        \"reserve_retention_period_value\": null\r\n" + "      }\r\n" + "    ],\r\n" + "    \"Source Environment\": {\r\n" + "      \"Environment Name\": [\r\n" + "        {\r\n" + "          \"name\": \"SRC\"\r\n" + "        }\r\n" + "      ],\r\n" + "      \"Source Environment Products\": [\r\n" + "        {\r\n" + "          \"product_name\": \"BILLING\",\r\n" + "          \"source_product_version\": \"PROD\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"COLLECTION\",\r\n" + "          \"source_product_version\": \"1\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"CRM\",\r\n" + "          \"source_product_version\": \"1\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"ORDERING\",\r\n" + "          \"source_product_version\": \"1\"\r\n" + "        }\r\n" + "      ]\r\n" + "    },\r\n" + "    \"Target Environment\": {\r\n" + "      \"Environment Name\": [\r\n" + "        {\r\n" + "          \"name\": \"TAR\"\r\n" + "        }\r\n" + "      ],\r\n" + "      \"Target Environment Products\": [\r\n" + "        {\r\n" + "          \"product_name\": \"BILLING\",\r\n" + "          \"target_product_version\": \"PROD\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"COLLECTION\",\r\n" + "          \"target_product_version\": \"1\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"CRM\",\r\n" + "          \"target_product_version\": \"1.5\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"ORDERING\",\r\n" + "          \"target_product_version\": \"1\"\r\n" + "        }\r\n" + "      ]\r\n" + "    },\r\n" + "    \"Task Execution Summary\": [\r\n" + "      {\r\n" + "        \"lu_name\": \"Customer\",\r\n" + "        \"fabric_execution_id\": \"aced32c8-7d2c-43cd-a7ba-f363733f59e7\",\r\n" + "        \"parent_lu_name\": \"null\",\r\n" + "        \"data_center_name\": \"null\",\r\n" + "        \"start_execution_time\": \"2021-06-16 16:24:21.0\",\r\n" + "        \"end_execution_time\": \"2021-06-16 16:24:22.0\",\r\n" + "        \"num_of_processed_entities\": 5,\r\n" + "        \"num_of_copied_entities\": 5,\r\n" + "        \"num_of_failed_entities\": 0,\r\n" + "        \"num_of_processed_ref_tables\": 0,\r\n" + "        \"num_of_copied_ref_tables\": 0,\r\n" + "        \"num_of_failed_ref_tables\": 0\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Collection\",\r\n" + "        \"fabric_execution_id\": \"4f4e3609-1b9b-418b-b059-ceb38b4e93eb\",\r\n" + "        \"parent_lu_name\": \"Customer\",\r\n" + "        \"data_center_name\": \"null\",\r\n" + "        \"start_execution_time\": \"2021-06-16 16:24:31.0\",\r\n" + "        \"end_execution_time\": \"2021-06-16 16:24:32.0\",\r\n" + "        \"num_of_processed_entities\": 3,\r\n" + "        \"num_of_copied_entities\": 3,\r\n" + "        \"num_of_failed_entities\": 0,\r\n" + "        \"num_of_processed_ref_tables\": 0,\r\n" + "        \"num_of_copied_ref_tables\": 0,\r\n" + "        \"num_of_failed_ref_tables\": 0\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"fabric_execution_id\": \"8868047e-0aa4-4793-a2f2-d41f8a24a94b\",\r\n" + "        \"parent_lu_name\": \"Customer\",\r\n" + "        \"data_center_name\": \"null\",\r\n" + "        \"start_execution_time\": \"2021-06-16 16:24:31.0\",\r\n" + "        \"end_execution_time\": \"2021-06-16 16:24:32.0\",\r\n" + "        \"num_of_processed_entities\": 17,\r\n" + "        \"num_of_copied_entities\": 17,\r\n" + "        \"num_of_failed_entities\": 0,\r\n" + "        \"num_of_processed_ref_tables\": 0,\r\n" + "        \"num_of_copied_ref_tables\": 0,\r\n" + "        \"num_of_failed_ref_tables\": 0\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Orders\",\r\n" + "        \"fabric_execution_id\": \"d423dcfc-e175-4aed-b31f-0ca55e59f095\",\r\n" + "        \"parent_lu_name\": \"Customer\",\r\n" + "        \"data_center_name\": \"null\",\r\n" + "        \"start_execution_time\": \"2021-06-16 16:24:31.0\",\r\n" + "        \"end_execution_time\": \"2021-06-16 16:24:32.0\",\r\n" + "        \"num_of_processed_entities\": 27,\r\n" + "        \"num_of_copied_entities\": 27,\r\n" + "        \"num_of_failed_entities\": 0,\r\n" + "        \"num_of_processed_ref_tables\": 0,\r\n" + "        \"num_of_copied_ref_tables\": 0,\r\n" + "        \"num_of_failed_ref_tables\": 0\r\n" + "      }\r\n" + "    ],\r\n" + "    \"List of Root Entities\": {\r\n" + "      \"Number of Copied Entities\": [\r\n" + "        {\r\n" + "          \"number_of_copied_root_entities\": 5\r\n" + "        }\r\n" + "      ],\r\n" + "      \"List of Copied Entities\": [\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_36\",\r\n" + "          \"target_id\": \"36\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_532\",\r\n" + "          \"target_id\": \"532\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_577\",\r\n" + "          \"target_id\": \"577\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_627\",\r\n" + "          \"target_id\": \"627\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_794\",\r\n" + "          \"target_id\": \"794\"\r\n" + "        }\r\n" + "      ],\r\n" + "      \"Number of Failed Entities\": [\r\n" + "        {\r\n" + "          \"number_of_failed_root_entities\": 0\r\n" + "        }\r\n" + "      ],\r\n" + "      \"List of Failed Entities\": []\r\n" + "    },\r\n" + "    \"List of Reference Tables\": {\r\n" + "      \"Number of Copied Reference Tables\": [\r\n" + "        {\r\n" + "          \"count\": 0\r\n" + "        }\r\n" + "      ],\r\n" + "      \"List of Copied Reference Tables\": [],\r\n" + "      \"Number of Failed Reference Tables\": [\r\n" + "        {\r\n" + "          \"count\": 0\r\n" + "        }\r\n" + "      ],\r\n" + "      \"List of Failed Reference Tables\": []\r\n" + "    },\r\n" + "    \"Error Summary\": [],\r\n" + "    \"Error Details\": [],\r\n" + "    \"Statistics Report\": [\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"table_name\": \"BALANCE\",\r\n" + "        \"target_count\": \"172\",\r\n" + "        \"source_count\": \"172\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"table_name\": \"INVOICE\",\r\n" + "        \"target_count\": \"107\",\r\n" + "        \"source_count\": \"107\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"table_name\": \"OFFER\",\r\n" + "        \"target_count\": \"9\",\r\n" + "        \"source_count\": \"9\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"table_name\": \"SUBSCRIBER\",\r\n" + "        \"target_count\": \"17\",\r\n" + "        \"source_count\": \"17\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Collection\",\r\n" + "        \"table_name\": \"COLLECTION\",\r\n" + "        \"target_count\": \"3\",\r\n" + "        \"source_count\": \"3\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Customer\",\r\n" + "        \"table_name\": \"ACTIVITY\",\r\n" + "        \"target_count\": \"26\",\r\n" + "        \"source_count\": \"26\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Customer\",\r\n" + "        \"table_name\": \"ADDRESS\",\r\n" + "        \"target_count\": \"5\",\r\n" + "        \"source_count\": \"5\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Customer\",\r\n" + "        \"table_name\": \"CASE_NOTE\",\r\n" + "        \"target_count\": \"35\",\r\n" + "        \"source_count\": \"35\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Customer\",\r\n" + "        \"table_name\": \"CASES\",\r\n" + "        \"target_count\": \"16\",\r\n" + "        \"source_count\": \"16\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Customer\",\r\n" + "        \"table_name\": \"CONTRACT\",\r\n" + "        \"target_count\": \"17\",\r\n" + "        \"source_count\": \"17\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Customer\",\r\n" + "        \"table_name\": \"CUSTOMER\",\r\n" + "        \"target_count\": \"5\",\r\n" + "        \"source_count\": \"5\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Orders\",\r\n" + "        \"table_name\": \"ORDERS\",\r\n" + "        \"target_count\": \"27\",\r\n" + "        \"source_count\": \"27\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      }\r\n" + "    ],\r\n" + "    \"Replace Sequence Summary Report\": []\r\n" + "  },\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": \"\"\r\n" + "}\r\n" + "\r\n" + "Populate the LU Name with a Logical Unit:\r\n" + "==================================\r\n" + "\r\n" + "{\r\n" + "  \"result\": {\r\n" + "    \"General Info\": [\r\n" + "      {\r\n" + "        \"task_name\": \"createTaskByTester\",\r\n" + "        \"task_id\": 294,\r\n" + "        \"task_execution_id\": \"490\",\r\n" + "        \"created_by\": \"tali\",\r\n" + "        \"executed_by\": \"admin\",\r\n" + "        \"start_execution\": \"2021-06-16 16:24:21.0\",\r\n" + "        \"end_execution\": \"2021-06-16 16:24:32.0\",\r\n" + "        \"execution_status\": \"completed\",\r\n" + "        \"source_env\": \"SRC\",\r\n" + "        \"target_env\": \"TAR\",\r\n" + "        \"be_name\": \"CUSTOMER\",\r\n" + "        \"task_type\": \"LOAD\",\r\n" + "        \"selection_method\": \"Randon Selection\",\r\n" + "       \t\"task_sync_mode\": null,\r\n" + "        \"env_sync_mode\": \"ON\",\r\n" + "        \"operation_mode\": \"Delete and load entity\",\r\n" + "        \"replace_sequences\": \"false\",\r\n" + "        \"version_ind\": \"false\",\r\n" + "        \"scheduling_parameters\": \"immediate\",\r\n" + "        \"schedule_expiration_date\": null,\r\n" + "        \"override_parameters\": null,\r\n" + "        \"reserve_ind\": \"false\",\r\n" + "        \"reserve_retention_period_type\": null,\r\n" + "        \"reserve_retention_period_value\": null,\r\n" + "        \"fabric_execution_id\": \"c30a97f2-fc3e-4b66-9986-aa0c6108456a\"\r\n" + "      }\r\n" + "    ],\r\n" + "    \"Source Environment\": {\r\n" + "      \"Environment Name\": [\r\n" + "        {\r\n" + "          \"name\": \"SRC\"\r\n" + "        }\r\n" + "      ],\r\n" + "      \"Source Environment Products\": [\r\n" + "        {\r\n" + "          \"product_name\": \"BILLING\",\r\n" + "          \"source_product_version\": \"PROD\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"COLLECTION\",\r\n" + "          \"source_product_version\": \"1\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"CRM\",\r\n" + "          \"source_product_version\": \"1\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"ORDERING\",\r\n" + "          \"source_product_version\": \"1\"\r\n" + "        }\r\n" + "      ]\r\n" + "    },\r\n" + "    \"Target Environment\": {\r\n" + "      \"Environment Name\": [\r\n" + "        {\r\n" + "          \"name\": \"TAR\"\r\n" + "        }\r\n" + "      ],\r\n" + "      \"Target Environment Products\": [\r\n" + "        {\r\n" + "          \"product_name\": \"BILLING\",\r\n" + "          \"target_product_version\": \"PROD\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"COLLECTION\",\r\n" + "          \"target_product_version\": \"1\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"CRM\",\r\n" + "          \"target_product_version\": \"1.5\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"product_name\": \"ORDERING\",\r\n" + "          \"target_product_version\": \"1\"\r\n" + "        }\r\n" + "      ]\r\n" + "    },\r\n" + "    \"Task Execution Summary\": [\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"fabric_execution_id\": \"8868047e-0aa4-4793-a2f2-d41f8a24a94b\",\r\n" + "        \"parent_lu_name\": \"Customer\",\r\n" + "        \"data_center_name\": \"null\",\r\n" + "        \"start_execution_time\": \"2021-06-16 16:24:31.0\",\r\n" + "        \"end_execution_time\": \"2021-06-16 16:24:32.0\",\r\n" + "        \"num_of_processed_entities\": 17,\r\n" + "        \"num_of_copied_entities\": 17,\r\n" + "        \"num_of_failed_entities\": 0,\r\n" + "        \"num_of_processed_ref_tables\": 0,\r\n" + "        \"num_of_copied_ref_tables\": 0,\r\n" + "        \"num_of_failed_ref_tables\": 0\r\n" + "      }\r\n" + "    ],\r\n" + "    \"List of Root Entities\": {\r\n" + "      \"Number of Copied Entities\": [\r\n" + "        {\r\n" + "          \"number_of_copied_root_entities\": 17\r\n" + "        }\r\n" + "      ],\r\n" + "      \"List of Copied Entities\": [\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_102\",\r\n" + "          \"target_id\": \"102\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_103\",\r\n" + "          \"target_id\": \"103\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_104\",\r\n" + "          \"target_id\": \"104\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_105\",\r\n" + "          \"target_id\": \"105\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_106\",\r\n" + "          \"target_id\": \"106\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1324\",\r\n" + "          \"target_id\": \"1324\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1325\",\r\n" + "          \"target_id\": \"1325\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1326\",\r\n" + "          \"target_id\": \"1326\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1429\",\r\n" + "          \"target_id\": \"1429\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1430\",\r\n" + "          \"target_id\": \"1430\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1431\",\r\n" + "          \"target_id\": \"1431\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1432\",\r\n" + "          \"target_id\": \"1432\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1537\",\r\n" + "          \"target_id\": \"1537\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1538\",\r\n" + "          \"target_id\": \"1538\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1965\",\r\n" + "          \"target_id\": \"1965\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1966\",\r\n" + "          \"target_id\": \"1966\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"source_id\": \"SRC_1967\",\r\n" + "          \"target_id\": \"1967\"\r\n" + "        }\r\n" + "      ],\r\n" + "      \"Number of Failed Entities\": [\r\n" + "        {\r\n" + "          \"number_of_failed_root_entities\": 0\r\n" + "        }\r\n" + "      ],\r\n" + "      \"List of Failed Entities\": []\r\n" + "    },\r\n" + "    \"List of Reference Tables\": {\r\n" + "      \"Number of Copied Reference Tables\": [\r\n" + "        {\r\n" + "          \"count\": 0\r\n" + "        }\r\n" + "      ],\r\n" + "      \"List of Copied Reference Tables\": [],\r\n" + "      \"Number of Failed Reference Tables\": [\r\n" + "        {\r\n" + "          \"count\": 0\r\n" + "        }\r\n" + "      ],\r\n" + "      \"List of Failed Reference Tables\": []\r\n" + "    },\r\n" + "    \"Error Summary\": [],\r\n" + "    \"Error Details\": [],\r\n" + "    \"Statistics Report\": [\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"table_name\": \"BALANCE\",\r\n" + "        \"target_count\": \"172\",\r\n" + "        \"source_count\": \"172\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"table_name\": \"INVOICE\",\r\n" + "        \"target_count\": \"107\",\r\n" + "        \"source_count\": \"107\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"table_name\": \"OFFER\",\r\n" + "        \"target_count\": \"9\",\r\n" + "        \"source_count\": \"9\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"lu_name\": \"Billing\",\r\n" + "        \"table_name\": \"SUBSCRIBER\",\r\n" + "        \"target_count\": \"17\",\r\n" + "        \"source_count\": \"17\",\r\n" + "        \"diff\": \"0\",\r\n" + "        \"results\": \"OK\"\r\n" + "      }\r\n" + "    ],\r\n" + "    \"Replace Sequence Summary Report\": []\r\n" + "  },\r\n" + "  \"message\": \"\"\r\n" + "}")
    public static Object wsTaskSummaryReport(@param(description = "Task execution ID", required = true) String executionId, @param(description = "Will be populated by 'ALL' to get one unified summary report to all Logical Units of the task execution. Populate this parameter by the Logical Unit name to get a report of a given Logical Unit.", required = true) String luName) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";

        try {
            //log.info("wsTaskSummaryReport - luName: " + luName);
            Object data = fnExecutionSummaryReport(executionId, luName != null && !"null".equalsIgnoreCase(luName) ? luName : "ALL");            
            response.put("result", ((Map<String, Object>) data).get("result"));
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Gets a summary or detailed information of the input batch id (LU execution). This API gets the run mode and a list of batch IDs or only one batch id as input values.\r\n" + "The run mode can have the following values:\r\n" + "\r\n" + " > 'D': detailed execution. Returns detailed information of all entities and their execution status.\r\n" + "\r\n" + " > 'S': summary information of the execution\r\n" + "\r\n" + " > 'H': get the batch command.\r\n" + "\r\n" + "Example of a request body:\r\n" + "{\"migrateIds\" : [\"387a3b82-18e2-4d45-9482-16dc6ad15385\",\"387a3b82-18e2-4d45-9482-16dc6ad15386\"], \"runModes\" : [\"D\", \"H\", \"S\"]}")
    @webService(path = "migrateStatusWs", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"S\": {\r\n" + "        \"columnsNames\": [\r\n" + "          \"Status\",\r\n" + "          \"Ent./sec (avg.)\",\r\n" + "          \"Ent./sec (pace)\",\r\n" + "          \"Start time (UTC)\",\r\n" + "          \"Duration\",\r\n" + "          \"End time (UTC)\",\r\n" + "          \"Name\",\r\n" + "          \"Succeeded\",\r\n" + "          \"Failed\",\r\n" + "          \"Added\",\r\n" + "          \"Updated\",\r\n" + "          \"Unchanged\",\r\n" + "          \"Total\",\r\n" + "          \"Level\",\r\n" + "          \"Remaining dur.\",\r\n" + "          \"Remaining\",\r\n" + "          \"% Completed\"\r\n" + "        ],\r\n" + "        \"results\": [\r\n" + "          {\r\n" + "            \"columns\": {\r\n" + "              \"Status\": \"\",\r\n" + "              \"Ent./sec (avg.)\": \"7.84\",\r\n" + "              \"Added\": 0,\r\n" + "              \"Ent./sec (pace)\": \"7.84\",\r\n" + "              \"Updated\": 2,\r\n" + "              \"Failed\": \"0\",\r\n" + "              \"Duration\": \"00:05:05\",\r\n" + "              \"Start time (UTC)\": \"2021-05-08 06:46:22\",\r\n" + "              \"End time (UTC)\": \"2021-05-08 06:51:27\",\r\n" + "              \"End time\": \"2021-05-08 06:51:27\",\r\n" + "              \"Name\": \"76b2a141-4f0a-453b-8999-f2bdd66a9b8d\",\r\n" + "              \"Succeeded\": \"2\",\r\n" + "              \"Total\": \"--\",\r\n" + "              \"Level\": \"Node\",\r\n" + "              \"Remaining dur.\": \"00:00:00\",\r\n" + "              \"Remaining\": \"0\",\r\n" + "              \"Start time\": \"2021-05-08 06:46:22\",\r\n" + "              \"Unchanged\": 0,\r\n" + "              \"% Completed\": \"100\"\r\n" + "            }\r\n" + "          },\r\n" + "          {\r\n" + "            \"columns\": {\r\n" + "              \"Status\": \"\",\r\n" + "              \"Ent./sec (avg.)\": \"7.84\",\r\n" + "              \"Added\": 0,\r\n" + "              \"Ent./sec (pace)\": \"7.84\",\r\n" + "              \"Updated\": 2393,\r\n" + "              \"Failed\": \"0\",\r\n" + "              \"Duration\": \"00:05:05\",\r\n" + "              \"Start time (UTC)\": \"2021-05-08 06:46:22\",\r\n" + "              \"End time (UTC)\": \"2021-05-08 06:51:27\",\r\n" + "              \"End time\": \"2021-05-08 06:51:27\",\r\n" + "              \"Name\": \"DC1\",\r\n" + "              \"Succeeded\": \"2393\",\r\n" + "              \"Total\": \"--\",\r\n" + "              \"Level\": \"DC\",\r\n" + "              \"Remaining dur.\": \"00:00:00\",\r\n" + "              \"Remaining\": \"0\",\r\n" + "              \"Start time\": \"2021-05-08 06:46:22\",\r\n" + "              \"Unchanged\": 0,\r\n" + "              \"% Completed\": \"100\"\r\n" + "            }\r\n" + "          },\r\n" + "          {\r\n" + "            \"columns\": {\r\n" + "              \"Status\": \"DONE\",\r\n" + "              \"Ent./sec (avg.)\": \"7.84\",\r\n" + "              \"Added\": 0,\r\n" + "              \"Ent./sec (pace)\": \"7.84\",\r\n" + "              \"Updated\": 2393,\r\n" + "              \"Failed\": \"0\",\r\n" + "              \"Duration\": \"00:05:05\",\r\n" + "              \"Start time (UTC)\": \"2021-05-08 06:46:22\",\r\n" + "              \"End time (UTC)\": \"2021-05-08 06:51:27\",\r\n" + "              \"End time\": \"2021-05-08 06:51:27\",\r\n" + "              \"Name\": \"--\",\r\n" + "              \"Succeeded\": \"2393\",\r\n" + "              \"Total\": \"2393\",\r\n" + "              \"Level\": \"Cluster\",\r\n" + "              \"Remaining dur.\": \"00:00:00\",\r\n" + "              \"Remaining\": \"0\",\r\n" + "              \"Start time\": \"2021-05-08 06:46:22\",\r\n" + "              \"Unchanged\": 0,\r\n" + "              \"% Completed\": \"100\"\r\n" + "            }\r\n" + "          }\r\n" + "        ]\r\n" + "      },\r\n" + "      \"D\": [\r\n" + "        {\r\n" + "          \"Status\": \"COMPLETED\",\r\n" + "          \"Error\": \"\",\r\n" + "          \"Entity ID\": \"ENV1_26982\",\r\n" + "          \"Results\": \"{\\\"Added\\\":0,\\\"Updated\\\":1,\\\"Unchanged\\\":0}\",\r\n" + "          \"Node id\": \"76b2a141-4f0a-453b-8999-f2bdd66a9b8d\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"Status\": \"COMPLETED\",\r\n" + "          \"Error\": \"\",\r\n" + "          \"Entity ID\": \"ENV1_26191\",\r\n" + "          \"Results\": \"{\\\"Added\\\":0,\\\"Updated\\\":1,\\\"Unchanged\\\":0}\",\r\n" + "          \"Node id\": \"76b2a141-4f0a-453b-8999-f2bdd66a9b8d\"\r\n" + "        }\r\n" + "],\r\n" + "      \"H\": {\r\n" + "        \"Migration Command\": \"batch PATIENT_VISITS from TDM using ('SELECT ''''||rel.source_env||''_''||rel.lu_type2_eid||'''' child_entity_id FROM task_execution_entities t, tdm_lu_type_relation_eid rel where t.task_execution_id = ''33'' and t.execution_status = ''completed'' and t.lu_name = ''PATIENT_LU'' and t.lu_name = rel.lu_type_1 and rel.lu_type_2 = ''PATIENT_VISITS'' and rel.version_name = '''' and t.source_env = rel.source_env and t.iid = rel.lu_type1_eid') FABRIC_COMMAND=\\\"sync_instance PATIENT_VISITS.?\\\" WITH ASYNC=true\"\r\n" + "      }\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsMigrateStatusWs(Object migrateIds, List<String> runModes) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {

            Boolean single = false;

            if (migrateIds instanceof String) {
                List<String> list = new ArrayList<>();
                list.add((String) migrateIds);
                migrateIds = list;
                single = true;
            }


            Object returnedResults = null;
            List<HashMap<String, Object>> results = new ArrayList<>();
            for (int i = 0; i < ((List<String>) migrateIds).size(); i++) {
                String batchID = ((List<String>) migrateIds).get(i);
                //log.info("wsMigrateStatusWs - batchID: " + batchID);
                if (batchID != null && !"".equals(batchID)) {
                    results.add(fnMigrateStatusWs(batchID, runModes));
                }
            }

            if (single && results != null && results.size() == 1) {
                returnedResults = results.get(0);
            } else returnedResults = results;

            response.put("result", returnedResults);
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Gets a summary or detailed information of the reference tables execution of a given task execution id.\r\n" + "\r\n" + "The type is the run mode of the API and can have the following values:\r\n" + " \r\n" + "> 'D': detailed execution. Returning a detailed information of all reference tables their execution status.\r\n" + "\r\n" + " > 'S': summary information of the execution")
    @webService(path = "extractrefstats", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    @resultMetaData(mediaType = Produce.JSON, example = "Example 1 - Summary Information: \r\n" + "{\r\n" + "  \"result\": {\r\n" + "    \"PATIENT_LU\": {\r\n" + "      \"minStartExecutionDate\": \"Sun May 09 09:08:42 UTC 2021\",\r\n" + "      \"maxEndExecutionDate\": \"Sun May 09 09:08:42 UTC 2021\",\r\n" + "      \"totNumOfTablesToProcess\": 3,\r\n" + "      \"numOfProcessedRefTables\": 3,\r\n" + "      \"numOfCopiedRefTables\": 0,\r\n" + "      \"numOfFailedRefTables\": 3,\r\n" + "      \"numOfProcessingRefTables\": 0,\r\n" + "      \"numberOfNotStartedRefTables\": 0\r\n" + "    }\r\n" + "  },\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}\r\n" + "Example 2 - Detailed Information:\r\n" + "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"start_time\": \"2021-05-09 09:08:42.622\",\r\n" + "      \"error_msg\": \"java.lang.NullPointerException\",\r\n" + "      \"ref_table_name\": \"REF_GIBRISH\",\r\n" + "      \"number_of_processed_records\": null,\r\n" + "      \"lu_name\": \"PATIENT_LU\",\r\n" + "      \"execution_status\": \"failed\",\r\n" + "      \"end_time\": \"2021-05-09 09:08:42.622\",\r\n" + "      \"estimated_remaining_duration\": null,\r\n" + "      \"number_of_records_to_process\": 0\r\n" + "    },\r\n" + "    {\r\n" + "      \"start_time\": \"2021-05-09 09:08:42.613\",\r\n" + "      \"error_msg\": \"java.lang.NullPointerException\",\r\n" + "      \"ref_table_name\": \"PATIENT_REF\",\r\n" + "      \"number_of_processed_records\": null,\r\n" + "      \"lu_name\": \"PATIENT_LU\",\r\n" + "      \"execution_status\": \"failed\",\r\n" + "      \"end_time\": \"2021-05-09 09:08:42.613\",\r\n" + "      \"estimated_remaining_duration\": null,\r\n" + "      \"number_of_records_to_process\": 0\r\n" + "    },\r\n" + "    {\r\n" + "      \"start_time\": \"2021-05-09 09:08:42.617\",\r\n" + "      \"error_msg\": \"java.lang.NullPointerException\",\r\n" + "      \"ref_table_name\": \"REF_COMPLEX\",\r\n" + "      \"number_of_processed_records\": null,\r\n" + "      \"lu_name\": \"PATIENT_LU\",\r\n" + "      \"execution_status\": \"failed\",\r\n" + "      \"end_time\": \"2021-05-09 09:08:42.617\",\r\n" + "      \"estimated_remaining_duration\": null,\r\n" + "      \"number_of_records_to_process\": 0\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsExtractReferenceStats(String taskExecutionId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";

        try {
            Object data = fnGetReferenceDetailedData(taskExecutionId);
            response.put("result", data);
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            log.error(message);
            errorCode = "FAILED";
        }

        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    //from TDM.logic
    @desc("Gets the hierarchy of a given entity and LU name within the task execution")
    @webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": {\n    \"PATIENT_LU\": {\n      \"luName\": \"PATIENT_LU\",\n      \"targetId\": \"1\",\n      \"sourceId\": \"1\",\n      \"entityStatus\": \"completed\",\n      \"parentLuName\": \"\",\n      \"parentTargetId\": \"\",\n      \"children\": [\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"24900\",\n          \"sourceId\": \"24900\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        },\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"24901\",\n          \"sourceId\": \"24901\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        },\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"24902\",\n          \"sourceId\": \"24902\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        },\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"24903\",\n          \"sourceId\": \"24903\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        },\n        {\n          \"luName\": \"PATIENT_VISITS\",\n          \"targetId\": \"400\",\n          \"sourceId\": \"400\",\n          \"entityStatus\": \"completed\",\n          \"parentLuName\": \"PATIENT_LU\",\n          \"parentTargetId\": \"1\",\n          \"luStatus\": \"completed\"\n        }\n      ],\n      \"luStatus\": \"completed\"\n    }\n  },\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
    public static Object wsGetTaskExeStatsForEntity(String taskExecutionId, String luName, String targetId) throws Exception {
         boolean isFullHierarchyEnabled = "true".equalsIgnoreCase(fabric().fetch("Set POP_FULL_LU_HIERARCHY_IN_TDM_LU").firstValue().toString());
        Map<String, Object> mainOutput = new HashMap<>();
        if(isFullHierarchyEnabled){
            fabric().execute("GET TDM.?",taskExecutionId);
            mainOutput = fnGetTaskExeStatsForEntityFromTDMLU(taskExecutionId, luName, targetId);
        }else{
            mainOutput = fnGetTaskExeStatsForEntityFromTDMDB(taskExecutionId, luName, targetId);
        }
        return wrapWebServiceResults("SUCCESS", null, mainOutput);
    }

    @desc("Resumes the stopped task execution")
    @webService(path = "resumeMigratWS", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    public static Object wsResumeMigratWS(Long taskExecutionId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        Object message = null;
        Object errorCode = "";
        try {
            Object data = fnResumeTaskExecution(taskExecutionId);
            Map<String, Object> resultMap = (Map<String, Object>) data;
            response.put("result", resultMap.get("result"));
            message = resultMap.get("message");
            errorCode = resultMap.get("errorCode");
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
            log.error(e.getMessage());
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Stops the task execution of the input task execution id.")
    @webService(path = "cancelMigratWS", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    public static Object wsCancelMigratWS(Long taskExecutionId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        Object message = null;
        Object errorCode = "";
        try {
            Object data = fnStopTaskExecution(taskExecutionId);
            Map<String, Object> resultMap = (Map<String, Object>) data;
            response.put("result", resultMap.get("result"));
            message = resultMap.get("message");
            errorCode = resultMap.get("errorCode");
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
            log.error(e.getMessage());

        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Starts an execution of a TDM task. \r\n" +
            "A task execution can override the following parameters:\r\n" +
            "\r\n" +
            "- entitieslist: populated by a list of entities separated by a comma. Note that the entity list can only contain one entity ID when executing a task with an Entity Clone selection method.\r\n" +
            "\r\n" +
            "- sourceEnvironmentName: source environment name.\r\n" +
            "\r\n" +
            "- targetEnvironmentName: target environment name.\r\n" +
            "\r\n" +
            "- taskGlobals: list of Global variables and their values.\r\n" +
            "\r\n" +
            "- numberOfEntities: populated by a number to change the number of entities processed by the task. This parameter is only relevant when the entitylist override parameter is not set.\r\n" +
            "\r\n" +
            "- dataVersionExecId: populated with the task execution id of the selected data version. The parameter can be set on Data Versioning load tasks.\r\n" +
            "\r\n" +
            "- dataVersionRetentionPeriod:  populated with the retention period of the extracted data version. This parameter contains the unit (Hours, Days, Weeks…) and the value.\r\n" +
            "\r\n" +
            "- reserveInd: populated with true or false. Set to true if the task execution needs to reserve the entities on the target environment.\r\n" +
            "\r\n" +
            "- reserveRetention: populated with the reservation period of the task's entities. This parameter contains the unit (Hours, Days, Weeks.) and the value.\r\n" +
            "\r\n" +
            "- executionNote: Free text. Add a note to the execution.\r\n" +
            "\r\n" +
            "\r\n" +
            "The task execution is validated whether the execution parameters are overridden or taken from the task itself:\r\n" +
            "\r\n" +
            "- Do not enable an execution if the TDM task execution processes are down. \r\n" +
            "\r\n" +
            "- Test the connection details of the source and target environments of the task execution if the forced parameter is false.\r\n" +
            "\r\n" +
            "- Do not enable an execution if another execution with the same execution parameters is already running on the task.\r\n" +
            "\r\n" +
            "- Validate the task's BE and LUs with the TDM products of the task execution's source and target environment.\r\n" +
            "- Verify that the user is permitted to execute the task on the task execution's source and target environment. For example, the user cannot run a Load task with a sequence replacement on environment X if the user does not have permissions to run such a task on this environment.\r\n" +
            "- Validate a Data Versioning extract task: verify that the given retention period do not exceed the maximum retention allowed for the user.\r\n" +
            "\r\n" +
            "Entity Reservation Validations:\r\n" +
            "===========================\r\n" +
            "- Validate the number of reserved entities: if the task reserves the entities whether the reserve_ind is set to true in the task itself or in the overridden parameters, accumulate the number of entities in the task to the total number of reserved entities for the user on the target environment. If the total number of reserved entities exceeds the user's permissions on the environment, return an error. For example, if the user is allowed to reserved up to 70 entities in ST1 and there are 50 entities that are already reserved for the user in ST1, the user can reserve up to 20 additional entities in ST1.\r\n" +
            "- Validate the retention period to verify that the number of days does not exceed the maximum number of days allowed for the tester.\r\n" +
            "\r\n" +
            "If at least one of the validations fail, the API does not start the task. Instead it returns a FAILED status and populates the list of validation errors in the results.\r\n" +
            "\r\n" +
            "Below is the list of the validation codes, returned by the API:\r\n" +
            "\r\n" +
            "- BEandLUs\r\n" +
            "\r\n" +
            "- Reference\r\n" +
            "\r\n" +
            "- selectionMethod\r\n" +
            "\r\n" +
            "- Versioning \r\n" +
            "\r\n" +
            "- ReplaceSequence\r\n" +
            "\r\n" +
            "- DeleteBeforeLoad\r\n" +
            "\r\n" +
            "- syncMode\r\n" +
            "\r\n" +
            "- totalNumberOfReservedEntities\r\n" +
            "\r\n" +
            "- versioningRetentionPeriod\r\n" +
            "\r\n" +
            "- reserveRetentionPeriod\r\n" +
            "\r\n" +
            "\r\n" +
            "If the validations pass successfully, start the task execution.\r\n" +
            "\r\n" +
            "Note that if a Global's value is a JSON, it is required to add backslash to the values. See example.\r\n" +
            "\r\n" +
            "Request body example:\r\n" +
            "\r\n" +
            "{\r\n" +
            "\t\"entitieslist\": \"1,2,4,9,8,11\",\r\n" +
            "\t\"sourceEnvironmentName\": \"SRC\",\r\n" +
            "\t\"targetEnvironmentName\": \"TAR\",\r\n" +
            "\t\"taskGlobals\": {\r\n" +
            "\t\t\"CUST_TYPE\": \"BUS\",\r\n" +
            "\t\t\"Customer.SUB_TYPE\": \"XX\"\r\n" +
            "\t},\r\n" +
            "\t\"numberOfEntities\": 14,\r\n" +
            "\t\"dataVersionExecId\": 2,\r\n" +
            "\t\"dataVersionRetentionPeriod\": {\r\n" +
            "\t\t\"units\": \"Days\",\r\n" +
            "\t\t\"value\": \"10\"\r\n" +
            "\t},\r\n" +
            "\t\"reserveInd\": true,\r\n" +
            "\t\"reserveRetention\": {\r\n" +
            "\t\t\"units\": \"Days\",\r\n" +
            "\t\t\"value\": \"5\"\r\n" +
            "\t},\r\n" +
            "\t\"executionNote\": \"Example Task\"\r\n" +
            "}")
    @webService(path = "task/{taskId}/forced/{forced}/startTask", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "1. Succesful Execution:\r\n" +
            "\r\n" +
            "{\r\n" +
            "  \"result\": {\r\n" +
            "    \"taskExecutionId\": 43\r\n" +
            "  },\r\n" +
            "  \"errorCode\": \"SUCCESS\",\r\n" +
            "  \"message\": null\r\n" +
            "}\r\n" +
            "\r\n" +
            "2. Validation Failure:\r\n" +
            "\r\n" +
            "{\r\n" +
            "    \"result\":[{\"Number of entity\":\"The number of entities exceeds the number of entities in the write permission\",\"selectionMethod\":\"The User has no permissions to run the task's selection method on the task's target environment\"}],\r\n" +
            "    \"errorCode\":\"FAILED\",\r\n" +
            "    \"message\":\"validation failure\"\r\n" +
            "}\r\n" +
            "\r\n" +
            "{ \r\n" +
            "    \"result\": \r\n" +
            "    [{\"reference\": \"The user has no permissions to run tasks on Reference tables on source environment\", \r\n" +
            "      \"syncMode\": \"the user has no permissions to ask to always sync the data from the source.\"    } ], \r\n" +
            "    \"errorCode\": \"FAILED\",\r\n" +
            "    \"message\": \"validation failure\"\r\n" +
            "}")
    public static Object wsStartTaskV1(@param(required = true) Long taskId,
            @param(description = "true or false", required = true) Boolean forced,
            String entitieslist,
            String sourceEnvironmentName,
            String targetEnvironmentName,
            Map<String, String> taskGlobals,
            Integer numberOfEntities,
            Long dataVersionExecId,
            Map<String, String> dataVersionRetentionPeriod,
            Boolean reserveInd,
            Map<String, String> reserveRetention,
            String executionNote) throws Exception {
        Map<OverrideParamKey, Object> overrideParameters = new HashMap<>();
        if (entitieslist != null)
            overrideParameters.put(OverrideParamKey.ENTITY_LIST, entitieslist);
        if (sourceEnvironmentName != null)
            overrideParameters.put(OverrideParamKey.SOURCE_ENVIRONMENT_NAME, sourceEnvironmentName);
        if (targetEnvironmentName != null)
            overrideParameters.put(OverrideParamKey.TARGET_ENVIRONMENT_NAME, targetEnvironmentName);
        if (taskGlobals != null)
            overrideParameters.put(OverrideParamKey.TASK_GLOBALS, taskGlobals);
        if (numberOfEntities != null)
            overrideParameters.put(OverrideParamKey.NO_OF_ENTITIES, numberOfEntities.longValue());
        if (dataVersionExecId != null)
            overrideParameters.put(OverrideParamKey.SELECTED_VERSION_TASK_EXE_ID, dataVersionExecId);
        if (dataVersionRetentionPeriod != null)
            overrideParameters.put(OverrideParamKey.DATAFLUX_RETENTION_PARAMS, dataVersionRetentionPeriod);
        if (reserveInd != null)
            overrideParameters.put(OverrideParamKey.RESERVE_IND, reserveInd);
        if (reserveRetention != null)
            overrideParameters.put(OverrideParamKey.RESERVE_RETENTION_PARAMS, reserveRetention);
        if (executionNote != null)
            overrideParameters.put(OverrideParamKey.EXECUTION_NOTE, executionNote);
        return StartTask.perform(taskId, forced, overrideParameters);
    }

    @desc("""
            Starts an execution of a TDM task.
            A task execution can override the following parameters:

            - BE_ID: populated with the Business Entity ID of the task.

            - ENTITY_LIST: a list of entities separated by a comma. Note that the entity list can only contain one entity ID when executing a task with an Entity Clone selection method.

            - SOURCE_ENVIRONMENT_NAME: source environment name.

            - TARGET_ENVIRONMENT_NAME: target environment name.

            - TASK_GLOBALS: a map of Global variable names to their override values.

            - NO_OF_ENTITIES: a number to change the number of entities processed by the task. This parameter is only relevant for tasks when the ENTITY_LIST override parameter is not set and the task selection is not Entity List.

            - SELECTED_VERSION_TASK_EXE_ID: the task execution id of the selected data version. The parameter can be set on Data Versioning load tasks.

            - DATAFLUX_RETENTION_PARAMS: the retention period of the extracted data version. This parameter contains the unit (Hours, Days, Weeks…) and the value.

            - RESERVE_IND: true or false. Set to true if the task execution needs to reserve the entities on the target environment.

            - RESERVE_RETENTION_PARAMS: the reservation period of the task's entities. This parameter contains the unit (Hours, Days, Weeks.) and the value.

            - EXECUTION_NOTE: Free text. Add a note to the execution.

            - SELECTION_METHOD: overrides the task's selection method. Only the following values are supported as overrides: 'L' (entity list — requires ENTITY_LIST), 'ALL' (predefined entity list), 'R' (random — requires NO_OF_ENTITIES), 'C' (custom logic — requires NO_OF_ENTITIES; CUSTOM_LOGIC_FLOW and CUSTOM_LOGIC_LU_NAME must be present either in the override or already stored in the task definition).

            - PARAMETERS: a JSON string that overrides the task's filter or input parameters. The structure varies by selection method: for Custom Logic ('C'), provide {"inputs": [{"name": "...", "value": "..."}]}; for Business Parameters ('P'/'PR'), provide the full parameter group JSON.

            - BP_QUERY: a SQL WHERE clause string that overrides the business parameter filter of the task. Applicable only when the selection method is 'P' (Parameters) or 'PR' (Parameters with Random selection).

            - GENERATE_DATA_PARAMS: a map of synthetic data generation parameter overrides. Each key is a parameter name; each value is an object with a "value" field. Applicable only when the selection method is 'GENERATE'.

            - CUSTOM_LOGIC_FLOW: overrides the flow name used for a Custom Logic selection method.

            - CUSTOM_LOGIC_LU_NAME: overrides the LU name used for a Custom Logic selection method.

            - LOGICAL_UNITS: a list of objects, one per LU, that override per-LU settings. Each entry must include lu_id and lu_name, and may include max_no_of_workers, source_affinity, and target_affinity.

            - TABLE_FILTERS: a list of objects that override the filter values of reference tables included in the task. Each entry must include ref_table_name and a fields array. Each fields item must include field and may include condition and value.

            - PRE_EXECUTION_PROCESSES_PARAMS: a list of objects that override the input parameters of pre-execution processes. Each entry must include process_id and a parameter_overrides array. Each parameter_overrides item must include name and value.

            - POST_EXECUTION_PROCESSES_PARAMS: same structure as PRE_EXECUTION_PROCESSES_PARAMS, applied to post-execution processes.

            The task execution is validated whether the execution parameters are overridden or taken from the task itself:

            - Do not enable an execution if the TDM task execution processes are down.

            - Test the connection details of the source and target environments of the task execution if the forced parameter is false.

            - Do not enable an execution if another execution with the same execution parameters is already running on the task.

            - Validate the task's BE and LUs with the TDM products of the task execution's source and target environment.

            - Verify that the user is permitted to execute the task on the task execution's source and target environment. For example, the user cannot run a Load task with a sequence replacement on environment X if the user does not have permissions to run such a task on this environment.

            - Validate a Data Versioning extract task: verify that the given retention period do not exceed the maximum retention allowed for the user.

            Entity Reservation Validations:
            ===========================
            - Validate the number of reserved entities: if the task reserves the entities whether the RESERVE_IND is set to true in the task itself or in the overridden parameters, accumulate the number of entities in the task to the total number of reserved entities for the user on the target environment. If the total number of reserved entities exceeds the user's permissions on the environment, return an error. For example, if the user is allowed to reserved up to 70 entities in ST1 and there are 50 entities that are already reserved for the user in ST1, the user can reserve up to 20 additional entities in ST1.

            - Validate the retention period to verify that the number of days does not exceed the maximum number of days allowed for the tester.

            If at least one of the validations fail, the API does not start the task. Instead it returns a FAILED status and populates the list of validation errors in the results.

            Below is the list of the validation codes, returned by the API:

            - BEandLUs

            - Reference

            - selectionMethod

            - Versioning

            - ReplaceSequence

            - DeleteBeforeLoad

            - syncMode

            - totalNumberOfReservedEntities

            - versioningRetentionPeriod

            - reserveRetentionPeriod

            If the validations pass successfully, start the task execution.

            Note that if a Global's value is a JSON, it is required to add backslash to the values. See example.

            Request body example:

            {
              "overrideParameters": {
                "BE_ID": "2",
                "ENTITY_LIST": "1,2,4,9,8,11",
                "SOURCE_ENVIRONMENT_NAME": "SRC",
                "TARGET_ENVIRONMENT_NAME": "TAR",
                "TASK_GLOBALS": {
                  "CUST_TYPE": "BUS",
                  "Customer.SUB_TYPE": "XX"
                },
                "NO_OF_ENTITIES": 14,
                "SELECTED_VERSION_TASK_EXE_ID": 2,
                "DATAFLUX_RETENTION_PARAMS": {
                  "units": "Days",
                  "value": "10"
                },
                "RESERVE_IND": true,
                "RESERVE_RETENTION_PARAMS": {
                  "units": "Days",
                  "value": "5"
                },
                "EXECUTION_NOTE": "Example Task",
                "LOGICAL_UNITS": [
                  {
                    "lu_id": 1,
                    "lu_name": "Customer",
                    "max_no_of_workers": 5,
                    "source_affinity": "DC1",
                    "target_affinity": "DC2"
                  }
                ],
                "TABLE_FILTERS": [
                  {
                    "ref_table_name": "RefT",
                    "interface_name": "RefInterface",
                    "schema_name": "RefSchema",
                    "fields": [
                      {"field": "col1", "condition": "=", "value": "val1"}
                    ]
                  }
                ],
                "PRE_EXECUTION_PROCESSES_PARAMS": [
                  {
                    "process_id": 1,
                    "parameter_overrides": [
                      {"name": "param1", "value": "v1"}
                    ]
                  }
                ],
                "POST_EXECUTION_PROCESSES_PARAMS": [
                  {
                    "process_id": 2,
                    "parameter_overrides": [
                      {"name": "param1", "value": "v1"}
                    ]
                  }
                ]
              }
            }
            """)
    @webService(path = "task/{taskId}/forced/{forced}/startTask", verb = {
            MethodType.POST }, version = "2", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
            1. Successful Execution:
            {
              "result": {
                "taskExecutionId": 43
              },
              "errorCode": "SUCCESS",
              "message": null
            }

            2. Validation Failure - override or permission error (error text in message):
            {
              "result": null,
              "errorCode": "FAILED",
              "message": "LOGICAL_UNITS[0] is missing required field 'lu_name'."
            }

            3. Validation Failure - entity/environment permission error (error map in result):
            {
              "result": {
                "Number of entity": "Exceeds write permission"
              },
              "errorCode": "FAILED",
              "message": "validation failure"
            }
            """)
    public static Object wsStartTaskV2(@param(required = true) Long taskId,
            @param(description = "true or false", required = true) Boolean forced,
            Map<OverrideParamKey, Object> overrideParameters,
            @param(description = "Optional draft ID from POST /task/{taskId}/aiDraftOverride. When provided, the draft record is reused as the execution record (draft=false) instead of allocating a new execution ID.") Long draftExecutionId)
            throws Exception {
        if (overrideParameters == null) {
            overrideParameters = new HashMap<>();
        }
        return StartTask.perform(taskId, forced, overrideParameters, draftExecutionId);
    }

    @desc("Holds (pause) the execution of the input task")
    @webService(path = "task/{taskId}/holdTask", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    public static Object wsHoldTask(@param(required = true) Long taskId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            String sql = "UPDATE " + TDMDB_SCHEMA + ".tasks SET task_execution_status = \'onHold\' WHERE tasks.task_id = " + taskId + " RETURNING tasks.task_title";
            Db.Row row = db(TDM).fetch(sql).firstRow();
            Object task_name = row.cell(0);
            try {
                String activityDesc = "task # " + task_name + " was Holded";
                fnInsertActivity("update", "Tasks", activityDesc);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            response.put("result", new HashMap<>());
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("This API is invoked when creating a Data Flux load task to validate the selected version. It validates the entity list of the Data Flux load task and checks if all entities have been successfully migrated into Fabric by the selected version. Failed entities are marked with 'false' and successfully migrated entities are marked with 'true' indication. If some entities are marked with 'false', an error message is given to the user to update the load task and remove the failed entities from the entity list.\r\n" + "\r\n" + "Example of a request body:\r\n" + "{\r\n" + "  \"entitlesList\": \"1,2,7\",\r\n" + "  \"taskExecutionId\": \"38\",\r\n" + "  \"luList\": \"Customer, Billing, Orders\"\r\n" + "}")
    @webService(path = "checkMigrateStatusForEntitiesList", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    public static Object CheckMigrateStatusForEntities(String entitlesList, String taskExecutionId, String luList) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            Object data = fnCheckMigrateStatusForEntitiesList(entitlesList, taskExecutionId, luList).get("result");
            response.put("result", data);
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("Activates a paused task (the task is set on-hold)")
    @webService(path = "task/{taskId}/activateTask", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\"result\":{},\"errorCode\":\"SUCCESS\",\"message\":null}")
    public static Object wsActivateTask(@param(required = true) Long taskId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            String sql = "UPDATE " + TDMDB_SCHEMA + ".tasks SET task_execution_status = \'Active\' WHERE tasks.task_id = " + taskId + " RETURNING tasks.task_title";
            Db.Row row = db(TDM).fetch(sql).firstRow();
            Object taskTitle = row.get("task_title");

            try {
                String activityDesc = "task # " + taskTitle + " was activated";
                fnInsertActivity("update", "Tasks", activityDesc);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            response.put("result", new HashMap<>());
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;

    }


    @desc("Gets available versions of the selected reference tables when creating Data Flux load tasks.\r\n" + "Example of request body: \r\n" + "{\"source_env_name\":\"ENV1\",\"fromDate\":\"2021-04-19T21:00:00.005Z\",\"toDate\":\"2021-05-20T20:59:59.005Z\",\"refList\":[\"MEDICATION_REFERENCE\",\"PATIENT_REF\"]}")
    @webService(path = "task/getVersionReferenceTaskTable", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    @resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": [\n    {\n      \"version_datetime\": \"2021-05-06 10:21:49.913\",\n      \"version_name\": \"v1\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"3a05d7fd-cf20-4805-a4fe-b550110a48b0\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"task_execution_id\": 296,\n      \"task_id\": 206,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 10:21:49.915\",\n      \"version_name\": \"v1\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"0d940278-ee1c-4e3b-b06b-de63c5741a3c\",\n      \"lu_name\": \"PATIENT_VISITS\",\n      \"task_execution_id\": 296,\n      \"task_id\": 206,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 10:17:49.182\",\n      \"version_name\": \"v1\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"6dda444d-6411-495d-b071-d81852b5f3e4\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"task_execution_id\": 294,\n      \"task_id\": 206,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 09:58:55.017\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"bfca5f37-31c8-45eb-a64f-be5b5160f93c\",\n      \"lu_name\": \"PATIENT_VISITS\",\n      \"task_execution_id\": 292,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 09:58:55.017\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"ee39f7f0-3478-4ab3-a93e-80686d99f599\",\n      \"lu_name\": \"PATIENT_LU\",\n      \"task_execution_id\": 292,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\"\n    },\n    {\n      \"version_datetime\": \"2021-05-06 10:51:06.451\",\n      \"version_name\": \"EXref123\",\n      \"version_type\": \"Selected Entities\",\n      \"fabric_execution_id\": \"2035621e-0a06-4f30-8fc7-1f0400067b7e\",\n      \"lu_name\": \"PATIENT_VISITS\",\n      \"task_execution_id\": 311,\n      \"task_id\": 173,\n      \"task_last_updated_by\": \"K2View\"\n    }\n  ],\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
    public static Object wsGetVersioningReferenceTaskTable(List<String> refList, String source_env_name, String fromDate, String toDate) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            List<Map<String, Object>> referenceData = fnGetVersionForLoadRef(refList, source_env_name, fromDate, toDate);
            response.put("result", referenceData);
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Gets the list of reference table included in a given task. Note that refcount attribute of /tasks API is populated by the number of Reference tables included in the task. If the refcount attribute is populated by zero, the task does not have Reference tables.")
    @webService(path = "task/refsTable/{task_id}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"task_ref_table_id\": 1,\r\n" + "      \"ref_table_name\": \"Ref2\",\r\n" + "      \"lu_name\": \"RefLU\",\r\n" + "      \"interface_name\": \"RefInterface\",\r\n" + "      \"task_id\": 28,\r\n" + "      \"schema_name\": \"RefSchema\",\r\n" + "      \"update_date\": \"2021-04-20 11:06:42.926\"\r\n" + "    },\r\n" + "    {\r\n" + "      \"task_ref_table_id\": 2,\r\n" + "      \"ref_table_name\": \"RefT2\",\r\n" + "      \"lu_name\": \"RefLU2\",\r\n" + "      \"interface_name\": \"RefInterface2\",\r\n" + "      \"task_id\": 28,\r\n" + "      \"schema_name\": \"RefSchema2\",\r\n" + "      \"update_date\": \"2021-04-20 11:06:42.937\"\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object getTaskReferenceTable(@param(required = true) Long task_id, @param String mode) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        try {
            if (mode == null) {
                String selMethod = "" + db(TDM).fetch(
                    "SELECT selection_method FROM " + TDMDB_SCHEMA + ".tasks WHERE task_id = " + task_id
                ).firstValue();
                if ("TABLES".equalsIgnoreCase(selMethod)) {
                    mode = "tables";
                }
            }
            String sql = "SELECT * FROM " + TDMDB_SCHEMA + ".task_ref_tables where task_id = " + task_id;
            Db.Rows rows = db(TDM).fetch(sql);
            boolean tablesMode = "tables".equalsIgnoreCase(mode);
            String sourceMtable = tablesMode ? "TableLevelDefinitions" : "RefList";
            List<Map<String, Object>> referenceTableData = new ArrayList<>();
            List<String> columnNames = rows.getColumnNames();
            for (Db.Row row : rows) {
                ResultSet resultSet = row.resultSet();
                Map<String, Object> rowMap = new HashMap<>();
                for (String columnName : columnNames) {
                    rowMap.put(columnName, resultSet.getObject(columnName));
                }
                String interface_name = "" + resultSet.getObject("interface_name");
                String schema_name = "" + resultSet.getObject("schema_name");
                String table_name = "" + resultSet.getObject("ref_table_name");
                String luName =  "" + resultSet.getObject("lu_name");
                rowMap.put("org_count_ind", fnGetTableCountIndicator(interface_name, schema_name, table_name,
                        "count_ind", sourceMtable, luName));
                referenceTableData.add(rowMap);
            }

            response.put("result", referenceTableData);
            errorCode = "SUCCESS";
            if (rows != null) {
                rows.close();
            }
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Returns the statistics of the given task execution id and given LU name and other parameters")
    @webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    private static Object fnGetTDMTaskExecutionStats(String taskExecutionId, String luName, String luEntityId, String luIdType, String luParentId, Integer entitiesArraySize, String displayErrorPath) throws Exception {
        //Tali- 4-Dec-18- fix the queries- select distinct the combination of be_root_entity_id + TARGET_ROOT_ENTITY_ID when selecting the number of copied/failed entities. This is required to support the clone of one entity X times by a task

        //log.info("wsGetTDMTaskExecutionStats - Inputs:");
        //log.info("taskExecutionId: <" + taskExecutionId + ">, luName: <" + luName + ">, luEntityId: <" + luEntityId + ">, luIdType: <" +
        //		luIdType + ">, luParentId: <" + luParentId + ">, entitiesArraySize: <" + entitiesArraySize + ">, displayErrorPath: <" + displayErrorPath + ">");
        boolean isRootLu = true;
        Map<String, Map> Map_Outer = new LinkedHashMap<>();

        List<Object> copiedEntitiesList = new ArrayList<>();
        List<Object> failedEntitiesList = new ArrayList<>();
        List<Object> copiedRefEntitiesList = new ArrayList<>();
        List<Object> failedRefEntitiesList = new ArrayList<>();

        Map<String, Object> mapInnerCopiedBuf = new HashMap<>();
        Map<String, Object> mapInnerFailedBuf = new HashMap<>();
        Map<String, Object> mapInnerCopiedRefBuf = new HashMap<>();
        Map<String, Object> mapInnerFailedRefBuf = new HashMap<>();
        Map<String, String> mapRootsStatus = new HashMap<>();

        String sqlSelect = "select ENTITY_ID as sourceId, TARGET_ENTITY_ID as targetId, BE_ROOT_ENTITY_ID as rootSourceId, " +
            "TARGET_ROOT_ENTITY_ID as rootTargetId, Case when PARENT_LU_NAME = '' then LU_NAME else PARENT_LU_NAME end as parentLuName, " +
            "Case when PARENT_ENTITY_ID = '' then BE_ROOT_ENTITY_ID else PARENT_ENTITY_ID end as parentSourceId, " +
            "Case when TARGET_PARENT_ID = '' then TARGET_ROOT_ENTITY_ID else TARGET_PARENT_ID end as parentTargetId, " +
            "Case when EXECUTION_STATUS ='completed' then 'Successful' else 'Failed' end as copyEntityStatus, " +
            "Case when ROOT_ENTITY_STATUS <> 'completed' then 'Failed' else 'Successful' end as copyHierarchyStatus, " +
            "LU_NAME as luName, EXECUTION_NOTE as entityExecutionNote, IID " + "from TDM.TASK_EXECUTION_LINK_ENTITIES t1 where id_type = '" + luIdType + "' and ";

        String sqlSelectOrder = " order by TARGET_ENTITY_ID";
        String sqlSelectCnt = "select count(1) from TDM.TASK_EXECUTION_LINK_ENTITIES t1 where id_type = '" + luIdType + "' and ";

        fabric().execute("get TDM.?", taskExecutionId);
        String entity_id_alone = "";
        if (entitiesArraySize == null) entitiesArraySize = 100;
        //Check if the given LU is a root LU
        String sqlCheckParent = "SELECT PARENT_LU_NAME FROM TDM.TASK_EXECUTION_LINK_ENTITIES WHERE LU_NAME = ? LIMIT 1";
        Object parentLuName = fabric().fetch(sqlCheckParent, luName).firstValue();
        if (parentLuName != null) {
            isRootLu = false;
        }
        //log.info("wsGetTDMTaskExecutionStats - isRootLu: " + isRootLu + ", luIdType: " + luIdType);
        //***** Entities lists *****//
        //

        String sqlCompEntities = "";
        Db.Rows compEntitiesBuf = null;
        String sqlCompCount = "";
        String compEntitiesCnt = "";

        String sqlFailedEntities = "";
        Db.Rows failedEntitiesBuf = null;
        String sqlFailedCount = "";
        String failedEntitiesCnt = "";

        List<String> luEntityIdList = new ArrayList<>();

        // TDM 6.0 - Check if the parent LU name is given or not
        // Get Completed entities

        if ("ENTITY".equals(luIdType)) {//If looking only for reference go directly to reference section
            if (isRootLu) { //If root LU

                //log.info("wsGetTDMTaskExecutionStats - Handling Root Entity");

                //lu Entity ID of the root is not given then set the query to get all entities of the given root LU
                if ((luEntityId == null || luEntityId.isEmpty())) {

                    //log.info("wsGetTDMTaskExecutionStats - luEntityId of the root is not provided, retrieving it");

                    sqlCompEntities = sqlSelect + "lu_name = ? and root_entity_status = 'completed' " + sqlSelectOrder + " limit ?";

                    compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, entitiesArraySize);
                    //log.info("wsGetTDMTaskExecutionStats - After getting the copied root entities");
                    sqlCompCount = sqlSelectCnt + "lu_name = ? and root_entity_status = 'completed'";

                    compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName).firstValue();
                    //log.info("wsGetTDMTaskExecutionStats - After getting the count of copied root entities");

                    sqlFailedEntities = sqlSelect + "lu_name = ? and root_entity_status <> 'completed' " + sqlSelectOrder + " limit ?";

                    failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, entitiesArraySize);
                    //log.info("wsGetTDMTaskExecutionStats - After getting the failed root entities");
                    sqlFailedCount = sqlSelectCnt + "lu_name = ? and root_entity_status <> 'completed'";

                    failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName).firstValue();
                    //log.info("wsGetTDMTaskExecutionStats - After getting the count of failed root entities");
                } else {// luEntityId is given

                    sqlCompEntities = sqlSelect + "lu_name = ? and root_entity_status = 'completed' and target_entity_id = ? " + sqlSelectOrder + " limit ?";

                    compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, luEntityId, entitiesArraySize);

                    sqlCompCount = sqlSelectCnt + "lu_name = ? and root_entity_status = 'completed' and target_entity_id = ?";

                    compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luEntityId).firstValue();

                    sqlFailedEntities = sqlSelect + "lu_name = ? and target_entity_id = ? and root_entity_status <> 'completed' " + sqlSelectOrder + " limit ?";

                    failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luEntityId, entitiesArraySize);

                    sqlFailedCount = sqlSelectCnt + "lu_name = ? and target_entity_id = ? and root_entity_status <> 'completed' ";

                    failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luEntityId).firstValue();
                }
            } else {//if not a root LU
                //log.info("The LU is not a root LU");
                if (!(luEntityId == null || luEntityId.isEmpty())) {//If entity ID is given and looking for entities and not reference
                    //log.info("luEntityId provided: " + luEntityId);
                    sqlCompEntities = sqlSelect + "LU_NAME = ? and target_entity_id = ? and execution_status = 'completed' and root_entity_status = 'completed' " + sqlSelectOrder;

                    compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, luEntityId);

                    sqlCompCount = sqlSelectCnt + "LU_NAME = ? and target_entity_id = ? and execution_status = 'completed' and root_entity_status = 'completed' ";

                    compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luEntityId).firstValue();

                    sqlFailedEntities = sqlSelect + "LU_NAME = ? and target_entity_id = ? and (ifNull(execution_status, 'failed') <> 'completed' " + "or ifNull(root_entity_status, 'failed') <> 'completed') " + sqlSelectOrder;

                    failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luEntityId);

                    sqlFailedCount = sqlSelectCnt + "LU_NAME = ? and target_entity_id = ? and (ifNull(execution_status, 'failed') <> 'completed' " + "or ifNull(root_entity_status, 'failed') <> 'completed') ";

                    failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luEntityId).firstValue();

                } else {//lu Entity ID is not given
                    //log.info("luEntityId is not provided");
                    if (luParentId == null || luParentId.isEmpty()) {// if parent LU ID is not given
                        //log.info("luParentId is not provided");
                        sqlCompEntities = sqlSelect + "LU_NAME = ? and execution_status = 'completed' and root_entity_status = 'completed' " + sqlSelectOrder + " limit ?";
                        //log.info("wsGetTDMTaskExecutionStats - sqlCompEntities for non Root without ID: " + sqlCompEntities);
                        compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, entitiesArraySize);

                        sqlCompCount = sqlSelectCnt + "LU_NAME = ? and execution_status = 'completed' and root_entity_status = 'completed'";

                        compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName).firstValue();

                        sqlFailedEntities = sqlSelect + "LU_NAME = ? and (ifNull(execution_status, 'failed') <> 'completed' or root_entity_status <> 'completed') " + sqlSelectOrder + " limit ?";

                        failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, entitiesArraySize);

                        sqlFailedCount = sqlSelectCnt + "LU_NAME = ? and (ifNull(execution_status, 'failed') <> 'completed' or root_entity_status <> 'completed')";

                        failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName).firstValue();

                    } else {// Parent LU ID is given
                        //log.info("luParentId: " + luParentId);
                        sqlCompEntities = sqlSelect + "LU_NAME = ? and target_parent_id = ? and execution_status = 'completed' and root_entity_status = 'completed' " + sqlSelectOrder + " limit ? ";

                        compEntitiesBuf = fabric().fetch(sqlCompEntities, luName, luParentId, entitiesArraySize);

                        sqlCompCount = sqlSelectCnt + "LU_NAME = ? and target_parent_id = ? and execution_status = 'completed' and root_entity_status = 'completed' ";

                        compEntitiesCnt = "" + fabric().fetch(sqlCompCount, luName, luParentId).firstValue();

                        sqlFailedEntities = sqlSelect + "LU_NAME = ? and target_parent_id = ? and (ifNull(execution_status, 'failed') <> 'completed' or " + "root_entity_status <> 'completed') " + sqlSelectOrder + " limit ?";

                        failedEntitiesBuf = fabric().fetch(sqlFailedEntities, luName, luParentId, entitiesArraySize);

                        sqlFailedCount = sqlSelectCnt + "LU_NAME = ? and target_parent_id = ? and (ifNull(execution_status, 'failed') <> 'completed' or " + "root_entity_status <> 'completed')";

                        failedEntitiesCnt = "" + fabric().fetch(sqlFailedCount, luName, luParentId).firstValue();

                    }
                }
            }

            String prevTargetID = "";
            //log.info("wsGetTDMTaskExecutionStats - Looping over copied entities");
            for (Db.Row copiedEnt : compEntitiesBuf) {
                Map<String, Object> mapInnerCopiedEnt = new HashMap<>();

                mapInnerCopiedEnt.put("luName", copiedEnt.get("luName"));

                //Get instance ID from entity id
                Object[] splitId = fnSplitUID("" + copiedEnt.get("sourceId"));
                String instanceId = "" + splitId[0];
                mapInnerCopiedEnt.put("entityId", copiedEnt.get("sourceId"));
                mapInnerCopiedEnt.put("sourceId", instanceId);

                String targetID = "" + copiedEnt.get("targetId");

                mapInnerCopiedEnt.put("targetId", targetID);
                mapInnerCopiedEnt.put("rootSourceId", copiedEnt.get("rootSourceId"));
                mapInnerCopiedEnt.put("rootTargetId", copiedEnt.get("rootTargetId"));
                mapInnerCopiedEnt.put("parentLuName", copiedEnt.get("parentLuName"));
                mapInnerCopiedEnt.put("parentSourceId", copiedEnt.get("parentSourceId"));
                mapInnerCopiedEnt.put("parentTargetId", copiedEnt.get("parentTargetId"));
                mapInnerCopiedEnt.put("copyEntityStatus", copiedEnt.get("copyEntityStatus"));
                mapInnerCopiedEnt.put("copyHierarchyStatus", copiedEnt.get("copyHierarchyStatus"));
                mapInnerCopiedEnt.put("entityExecutionNote", copiedEnt.get("entityExecutionNote"));
                mapInnerCopiedEnt.put("IID", copiedEnt.get("IID"));

                

                if (!prevTargetID.equals(targetID)) {
                    prevTargetID = targetID;
                }
                copiedEntitiesList.add(mapInnerCopiedEnt);
            }
            if (compEntitiesBuf != null) {
                compEntitiesBuf.close();
            }
            mapInnerCopiedBuf.put("NoOfEntities", compEntitiesCnt);
            mapInnerCopiedBuf.put("entitiesList", copiedEntitiesList);

            prevTargetID = "";

            //log.info("wsGetTDMTaskExecutionStats - looping over failed entities");
            for (Db.Row failedEnt : failedEntitiesBuf) {
                Map<String, Object> mapInnerFailedEnt = new HashMap<>();

                mapInnerFailedEnt.put("luName", failedEnt.get("luName"));

                //Get instance ID from entity id
                Object[] splitId = fnSplitUID("" + failedEnt.get("sourceId"));
                String instanceId = "" + splitId[0];
                mapInnerFailedEnt.put("entityId", failedEnt.get("sourceId"));
                mapInnerFailedEnt.put("sourceId", instanceId);

                String targetID = "" + failedEnt.get("targetId");
                String copyEntityStatus = "" + failedEnt.get("copyEntityStatus");

                mapInnerFailedEnt.put("targetId", targetID);
                mapInnerFailedEnt.put("rootSourceId", failedEnt.get("rootSourceId"));
                mapInnerFailedEnt.put("rootTargetId", failedEnt.get("rootTargetId"));
                mapInnerFailedEnt.put("parentLuName", failedEnt.get("parentLuName"));
                mapInnerFailedEnt.put("parentSourceId", failedEnt.get("parentSourceId"));
                mapInnerFailedEnt.put("parentTargetId", failedEnt.get("parentTargetId"));
                mapInnerFailedEnt.put("copyEntityStatus", copyEntityStatus);
                mapInnerFailedEnt.put("copyHierarchyStatus", failedEnt.get("copyHierarchyStatus"));
                mapInnerFailedEnt.put("entityExecutionNote", failedEnt.get("entityExecutionNote"));
                mapInnerFailedEnt.put("IID", failedEnt.get("IID"));

                //log.info ("Failed - luName: " + failedEnt.get("luName") + ", rootSourceId: " + failedEnt.get("rootSourceId"));
                // TDM 6.1.1 - 20-may-20, add the error msg that casued the failure
                String errorMsgSql = "select error_message from task_exe_error_detailed where " + "task_execution_id = ? and lu_name = ? and target_entity_id = ?  ORDER BY ERROR_CATEGORY LIMIT 5";
                Db.Rows errorMsgs = fabric().fetch(errorMsgSql, taskExecutionId, failedEnt.get("luName"), targetID);
                List<String> entityErrMsgs = new ArrayList<>();
                for (Db.Row errorMsg : errorMsgs) {
                    entityErrMsgs.add("" + errorMsg.get("error_message"));
                }
                mapInnerFailedEnt.put("errorMsg", entityErrMsgs);
                if (!prevTargetID.equals(targetID)) {
                    prevTargetID = targetID;
                }

                if ("true".equals(displayErrorPath)) {
                    LinkedList<Object> failedErrPathList = new LinkedList<>();
                    //Get the failures error path
                    if (!("Failed".equals(copyEntityStatus))) {

                        String sqlFirst = "select LU_NAME, TARGET_ENTITY_ID, EXECUTION_STATUS, PARENT_LU_NAME, TARGET_PARENT_ID, " + "1 as row_number FROM TASK_EXECUTION_LINK_ENTITIES where PARENT_LU_NAME = ? and TARGET_PARENT_ID = ? ";

                        String sqlSecond = "select a.LU_NAME, a.TARGET_ENTITY_ID, a.EXECUTION_STATUS, a.PARENT_LU_NAME, a.TARGET_PARENT_ID," + "row_number+1 FROM TASK_EXECUTION_LINK_ENTITIES a INNER JOIN relations b ON " + "b.lu_name = a.parent_lu_name and b.target_entity_id = a.TARGET_PARENT_ID ";

                        String sqlRecursiveGetChildren = "WITH RECURSIVE relations AS (" + sqlFirst + " UNION " + sqlSecond + ") select LU_NAME, TARGET_ENTITY_ID, EXECUTION_STATUS, PARENT_LU_NAME, TARGET_PARENT_ID, row_number " + " from relations order by row_number DESC";

                        //log.info("wsGetTDMTaskExecutionStats - Calling RECURSIVE sql for parent lu: " + failedEnt.get("parentLuName") + ", and target ID: " + targetID);
                        Db.Rows childrenBuf = fabric().fetch(sqlRecursiveGetChildren, failedEnt.get("parentLuName"), targetID);

                        Boolean errorLevelFound = false;
                        int rowNum = 0;
                        String lookupParentLuName = "";
                        String lookupParentID = "";

                        for (Db.Row childRec : childrenBuf) {
                            Map<String, Object> mapInnerErrorPath = new HashMap<>();
                            String childluName = "" + childRec.get("lu_name");
                            String childEnityID = "" + childRec.get("target_entity_id");
                            String childStatus = "" + childRec.get("execution_status");
                            String childParenLuName = "" + childRec.get("parent_lu_name");
                            String childParentID = "" + childRec.get("target_parent_id");
                            String entityStatus = "";

                            int currRowNum = Integer.parseInt("" + childRec.get("row_number"));
                            //log.info("wsGetTDMTaskExecutionStats - Failed - luName: " + childluName + ", childTargetId: " + childEnityID +
                            //	", parentLuName: " + childParenLuName + ", childParentID: " + childParentID + ", childStatus: " + childStatus);

                            if (!errorLevelFound && "completed".equals(childStatus)) {
                                //The input is coming from bottom up, therefore looking for the lowest LU that failed, and until it is found continue to the next one
                                continue;
                            }

                            //This point will be reached for the first time when the lowest erred lu is found
                            if (!errorLevelFound) {
                                //log.info("wsGetTDMTaskExecutionStats - Failed Child Found");
                                //log.info("wsGetTDMTaskExecutionStats - setting error level to true");
                                errorLevelFound = true;
                                if ("completed".equals(childStatus)) {
                                    entityStatus = "Copied";
                                } else {
                                    entityStatus = "Failed";
                                }
                                mapInnerErrorPath.put("luName", childluName);
                                mapInnerErrorPath.put("entityStatus", entityStatus);
                                mapInnerErrorPath.put("parentLuName", childParenLuName);
                                mapInnerErrorPath.put("luStatus", "Failed");

                                failedErrPathList.addFirst(mapInnerErrorPath);

                                //log.info("wsGetTDMTaskExecutionStats - Adding to Error Path: luName: " + childluName + ", ParentLuName: " + childParenLuName);
                                lookupParentLuName = "" + childParenLuName;
                                lookupParentID = "" + childParentID;

                                //log.info("wsGetTDMTaskExecutionStats - Looking for Parent: ID: " + lookupParentID + ", Parent LU Name: " + lookupParentLuName);
                                continue;
                            }

                            // This point will be reached only if the first failed record was found
                            // From this point we are looking for the ancenstor LUs of the failed record
                            //log.info("wsGetTDMTaskExecutionStats - Comparing - lookupParentLuName: " + lookupParentLuName + " with childluName: " + childluName +
                            //	" and lookupParentID: " + lookupParentID + " with childEnityID: " + childEnityID);
                            if (lookupParentLuName.equals(childluName) && lookupParentID.equals(childEnityID)) {
                                //log.info("wsGetTDMTaskExecutionStats - Parent Record Found - luName: " + childluName + ", TargetId: " + childEnityID);
                                if ("completed".equals(childStatus)) {
                                    entityStatus = "Copied";
                                } else {
                                    entityStatus = "Failed";
                                }
                                mapInnerErrorPath.put("luName", childluName);
                                mapInnerErrorPath.put("entityStatus", entityStatus);
                                mapInnerErrorPath.put("parentLuName", childParenLuName);
                                mapInnerErrorPath.put("luStatus", "Failed");

                                failedErrPathList.addFirst(mapInnerErrorPath);

                                lookupParentLuName = "" + childParenLuName;
                                lookupParentID = "" + childParentID;
                                continue;
                            }
                        }
                        if (childrenBuf != null) {
                            childrenBuf.close();
                        }
                    }
                    Map<String, Object> mapInputLu = new HashMap<>();
                    mapInputLu.put("luName", failedEnt.get("luName"));
                    mapInputLu.put("entityStatus", copyEntityStatus);
                    mapInputLu.put("parentLuName", failedEnt.get("parentLuName"));
                    mapInputLu.put("luStatus", "Failed");


                    failedErrPathList.addFirst(mapInputLu);
                    //log.info("Adding luName: " + failedEnt.get("luName") + ", entityStatus: " + copyEntityStatus + " to erro path");

                    mapInnerFailedEnt.put("Full Error Path", failedErrPathList);
                }

                failedEntitiesList.add(mapInnerFailedEnt);
                if (errorMsgs != null) {
                    errorMsgs.close();
                }
            }
            if (failedEntitiesBuf != null) {
                failedEntitiesBuf.close();
            }

            //log.info("wsGetTDMTaskExecutionStats - finished failed entities");
            mapInnerFailedBuf.put("NoOfEntities", failedEntitiesCnt);
            mapInnerFailedBuf.put("entitiesList", failedEntitiesList);

            Map_Outer.put("Copied entities per execution", mapInnerCopiedBuf);
            Map_Outer.put("Failed entities per execution", mapInnerFailedBuf);
        }

        //
        //Get Reference Info

        String sqlCopiedRefTabBuf = "";
        String sqlFailedRefRabBuf = "";
        Db.Rows copiedRefTabBuf = null;
        Db.Rows failedRefTabBuf = null;

        //log.info("wsGetTDMTaskExecutionStats - start handling reference");
        if (!(luEntityId == null || luEntityId.isEmpty()) && "REFERENCE".equals(luIdType)) {
            sqlCopiedRefTabBuf = "select distinct ref_table_name, " +
                "number_of_processed_records, number_of_failed_records, batch_id, execution_action " +
                "from TDM.task_ref_exe_stats t where ref_table_name = ? and t.Execution_Status = 'completed' " +
                "and execution_action <> 'Delete' and Lu_Name = ?";
            copiedRefTabBuf = fabric().fetch(sqlCopiedRefTabBuf, luEntityId, luName);

            sqlFailedRefRabBuf = "select distinct ref_table_name, " +
                "COALESCE(number_of_processed_records, 0) as number_of_processed_records, " +
                "COALESCE(number_of_failed_records,0) as number_of_failed_records, batch_id, error_msg, execution_action " +
                "from TDM.task_ref_exe_stats t where ref_table_name = ? and Lu_Name = ? and (" +
                "(execution_action = 'Delete' and COALESCE(t.Execution_Status, 'failed') = 'failed') " +
                "or (execution_action <> 'Delete' and COALESCE(t.Execution_Status, 'failed') = 'failed' and batch_id is not null " +
                "and not exists (" +
                "select 1 from TDM.task_ref_exe_stats d where d.ref_table_name = t.ref_table_name " +
                "and d.Lu_Name = t.Lu_Name and d.execution_action = 'Delete' " +
                "and COALESCE(d.Execution_Status, 'failed') = 'failed'))" +
                ")";
            failedRefTabBuf = fabric().fetch(sqlFailedRefRabBuf, luEntityId, luName);

            //log.info("wsGetTDMTaskExecutionStats - got failed reference");
        } else {
            sqlCopiedRefTabBuf = "select distinct ref_table_name, " +
                "number_of_processed_records, number_of_failed_records, batch_id, execution_action " +
                "from TDM.task_ref_exe_stats t where t.Execution_Status = 'completed' " +
                "and execution_action <> 'Delete' and Lu_Name = ?";
            copiedRefTabBuf = fabric().fetch(sqlCopiedRefTabBuf, luName);

            sqlFailedRefRabBuf = "select distinct ref_table_name, " +
                "COALESCE(number_of_processed_records, 0) as number_of_processed_records, " +
                "COALESCE(number_of_failed_records, 0) as number_of_failed_records, error_msg, batch_id, execution_action " +
                "from TDM.task_ref_exe_stats t where Lu_Name = ? and (" +
                "(execution_action = 'Delete' and COALESCE(t.Execution_Status, 'failed') = 'failed') " +
                "or (execution_action <> 'Delete' and COALESCE(t.Execution_Status, 'failed') = 'failed' and batch_id is not null " +
                "and not exists (" +
                "select 1 from TDM.task_ref_exe_stats d where d.ref_table_name = t.ref_table_name " +
                "and d.Lu_Name = t.Lu_Name and d.execution_action = 'Delete' " +
                "and COALESCE(d.Execution_Status, 'failed') = 'failed'))" +
                ")";
            failedRefTabBuf = fabric().fetch(sqlFailedRefRabBuf, luName);
        }
        //log.info("wsGetTDMTaskExecutionStats - Got reference Data");
        int totalCount = 0;
        for (Db.Row copiedRefEnt : copiedRefTabBuf) {
            Map<String, Object> mapInnerCopiedRefEnt = new HashMap<>();

            totalCount++;

            mapInnerCopiedRefEnt.put("RerernceTableName", copiedRefEnt.get("ref_table_name"));
            mapInnerCopiedRefEnt.put("RerernceTableCount", copiedRefEnt.get("number_of_processed_records"));
            mapInnerCopiedRefEnt.put("RerernceTableErrCount", copiedRefEnt.get("number_of_failed_records"));
            mapInnerCopiedRefEnt.put("BatchId", copiedRefEnt.get("batch_id"));
            copiedRefEntitiesList.add(mapInnerCopiedRefEnt);
        }
        if (copiedRefTabBuf != null) {
            copiedRefTabBuf.close();
        }

        mapInnerCopiedRefBuf.put("NoOfEntities", totalCount);
        mapInnerCopiedRefBuf.put("entitiesList", copiedRefEntitiesList);

        totalCount = 0;
        for (Db.Row failedRefEnt : failedRefTabBuf) {
            Map<String, Object> mapInnerFailedRefEnt = new HashMap<>();

            totalCount++;
            String reTableName = "" + failedRefEnt.get("ref_table_name");
            
            mapInnerFailedRefEnt.put("RerernceTableName", reTableName);
            mapInnerFailedRefEnt.put("RerernceTableCount", failedRefEnt.get("number_of_processed_records"));
            mapInnerFailedRefEnt.put("RerernceTableErrCount", failedRefEnt.get("number_of_failed_records"));
            mapInnerFailedRefEnt.put("BatchId", failedRefEnt.get("batch_id"));
            mapInnerFailedRefEnt.put("errorMsg", failedRefEnt.get("error_msg"));

            // TDM 6.1.1 - 20-may-20, add the error msg that casued the failure
          /*  String errorMsgSql = "select error_message from task_exe_error_detailed where " + "task_execution_id = ? and target_entity_id = ? LIMIT 5";
            Db.Rows errorMsgs = fabric().fetch(errorMsgSql, taskExecutionId, reTableName);
            List<String> entityErrMsgs = new ArrayList<>();
            for (Db.Row errorMsg : errorMsgs) {
                entityErrMsgs.add("" + errorMsg.get("error_message"));
            }
            mapInnerFailedRefEnt.put("errorMsg", entityErrMsgs);
            if (errorMsgs != null) {
                errorMsgs.close();
            }
            */
            failedRefEntitiesList.add(mapInnerFailedRefEnt);
            
        }
        if (failedRefTabBuf != null) {
            failedRefTabBuf.close();
        }
        mapInnerFailedRefBuf.put("NoOfEntities", totalCount);
        mapInnerFailedRefBuf.put("entitiesList", failedRefEntitiesList);

        Map_Outer.put("Copied Reference per execution", mapInnerCopiedRefBuf);
        Map_Outer.put("Failed Reference per execution", mapInnerFailedRefBuf);

        //Get the status of all the hierachies of the task, for each hierarchy mark if it has failures or not,
        // and if it has failures, where they on the root level or not
        String rootsStatusSql = "select ROOT_LU_NAME, max(root_status) as MAX_ROOT_STS from ( " + "select distinct root_lu_name, case when root_entity_status = 'completed' and EXECUTION_STATUS = 'completed' then 1 " + "when root_entity_status = 'failed' and  EXECUTION_STATUS = 'completed' then 2 " + "else 3 end as  root_status from task_execution_link_entities where parent_lu_name = '') group by root_lu_name";

        Db.Rows rootsStatuses = fabric().fetch(rootsStatusSql);

        for (Db.Row rootStatus : rootsStatuses) {
            String rootName = "" + rootStatus.get("root_lu_name");
            int rootStatusInd = (int) rootStatus.get("max_root_sts");
            String rootSts = "";
            switch (rootStatusInd) {
                case 1:
                    rootSts = "completed";
                    break;
                case 2:
                    rootSts = "child failed";
                    break;
                default:
                    rootSts = "root failed";
                    break;
            }

            mapRootsStatus.put(rootName, rootSts);
        }

        Map_Outer.put("Roots Status", mapRootsStatus);
        if (rootsStatuses != null) {
            rootsStatuses.close();
        }

        return wrapWebServiceResults("SUCCESS", null, Map_Outer);
    }

    //end from TDM.LOGIC

    //from TDMTasks.Logic
    @desc("Gets a user name and permission group and returns the list of active tasks that the user can run.")
    @webService(path = "wsGetUserTasks/{userName}/{userType}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    @resultMetaData(mediaType = Produce.XML, example = "<HashMap>\n  <result>\n    <task_last_updated_date>1615278319339</task_last_updated_date>\n    <be_id>1</be_id>\n    <environment_id>1</environment_id>\n    <selection_method>L</selection_method>\n   <refresh_reference_data/>\n    <task_id>21</task_id>\n    <source_environment_id>1</source_environment_id>\n    <scheduler>immediate</scheduler>\n    <selected_ref_version_datetime/>\n    <source_env_name>ENV1</source_env_name>\n    <load_entity>false</load_entity>\n    <task_title>tester</task_title>\n    <selected_version_task_exe_id/>\n    <task_created_by>k2vtester01</task_created_by>\n      <scheduling_end_date/>\n    <delete_before_load>false</delete_before_load>\n    <retention_period_type/>\n    <task_status>Active</task_status>\n    <selection_param_value>1,2,3,4,5,6,7,8,9,10</selection_param_value>\n    <retention_period_value/>\n    <selected_version_datetime/>\n    <task_last_updated_by>k2vtester01</task_last_updated_by>\n    <selected_ref_version_task_exe_id/>\n    <task_execution_status>Active</task_execution_status>\n    <version_ind>false</version_ind>\n    <sync_mode/>\n    <num_of_entities>10</num_of_entities>\n    <task_creation_date>1615278319339</task_creation_date>\n    <task_globals>false</task_globals>\n    <replace_sequences/>\n     <task_type>EXTRACT</task_type>\n    <parameters/>\n  </result>\n  <errorCode>SUCCESS</errorCode>\n  <message/>\n</HashMap>")
    @resultMetaData(mediaType = Produce.JSON, example = "{\n  \"result\": [\n    {\n      \"task_last_updated_date\": \"2021-03-09 08:25:19.339\",\n      \"be_id\": 1,\n     \"environment_id\": 1,\n      \"selection_method\": \"L\",\n   \"refresh_reference_data\": null,\n      \"task_id\": 21,\n      \"source_environment_id\": 1,\n      \"scheduler\": \"immediate\",\n      \"selected_ref_version_datetime\": null,\n      \"source_env_name\": \"ENV1\",\n      \"load_entity\": false,\n      \"task_title\": \"tester\",\n      \"selected_version_task_exe_id\": null,\n      \"task_created_by\": \"k2vtester01\",\n     \"scheduling_end_date\": null,\n      \"delete_before_load\": false,\n      \"retention_period_type\": null,\n      \"task_status\": \"Active\",\n      \"selection_param_value\": \"1,2,3,4,5,6,7,8,9,10\",\n      \"retention_period_value\": null,\n      \"selected_version_datetime\": null,\n      \"task_last_updated_by\": \"k2vtester01\",\n      \"selected_ref_version_task_exe_id\": null,\n      \"task_execution_status\": \"Active\",\n      \"version_ind\": false,\n      \"sync_mode\": null,\n      \"num_of_entities\": 10,\n      \"task_creation_date\": \"2021-03-09 08:25:19.339\",\n      \"task_globals\": false,\n      \"replace_sequences\": null,\n          \"task_type\": \"EXTRACT\",\n      \"parameters\": null\n    }\n  ],\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
    public static Object wsGetUserTasks(@param(required = true) String userName, @param(description = "admin, owner or tester", required = true) String userType) throws Exception {
        String adminQuery = "select * from " + TDMDB_SCHEMA + ".tasks where task_status = 'Active' and task_execution_status = 'Active'";
        String envOwnerQuery =
                //Query 1- get the extract tasks where the user is the owner of the source env
                "select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_owners o " + "where lower(task_type) = 'extract' " +
                    "and lower(task_status) = 'active' and lower(task_execution_status) = 'active' and t.source_environment_id = o.environment_id " +
                    "and o.user_name = ? " + "UNION " +
                        //Query 2- get the tasks, created by the user
                        "select t.* from " + TDMDB_SCHEMA + ".tasks t where lower(task_type) = 'extract' " + "and lower(task_status) = 'active' "+
                            "and lower(task_execution_status) = 'active' " + "and split_part(t.task_Created_by, '##', 1) = ? " + "UNION " +
                        //Query 3 - get the task created by another tester which belongs to the same role of the user
                        "select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
                            "where lower(task_type) = 'extract' and lower(task_status) = 'active' " +
                            "and lower(task_execution_status) = 'active' and split_part(t.task_Created_by, '##', 1) <> ? " +
                            "and not exists (select 1 from environment_owners o1 where o1.environment_id  = t.source_environment_id " +
                            "and o1.user_name = split_part(t.task_Created_by, '##', 1)) " + "and t.source_environment_id = r.environment_id and r.role_id = u.role_id " +
                            "and lower(r.role_status) = 'active' and (split_part(t.task_Created_by, '##', 1) = u.username or lower(u.username) = 'all') " +
                            "and exists (select 1 from " + "(Select env_list.environment_id, u.role_id " + "From " + TDMDB_SCHEMA + ".environment_roles r, " +
                            TDMDB_SCHEMA + ".environment_role_users u, " + "(Select r1.environment_id, 'user' As assignment_type " + "From " + TDMDB_SCHEMA +
                            ".environment_roles r1, " + TDMDB_SCHEMA + ".environment_role_users u1 " + "Where r1.role_id = u1.role_id And u1.username = ? And " +
                            "lower(r1.role_status) = 'active' " + "UNION " + "Select r2.environment_id, 'all' As assignment_Type " + "From " + TDMDB_SCHEMA +
                            ".environment_roles r2, " + TDMDB_SCHEMA + ".environment_role_users u2 " + "Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
                            "And lower(r2.role_status) = 'active' And Not exists " + "(select 1 from " + TDMDB_SCHEMA +
                            ".environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
                            "Where r.environment_id = env_list.environment_id " + "And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
                            "And ((env_list.assignment_type = 'all' And " + "lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
                            "where user_roles.role_id = r.role_id)" + "UNION " +
                        //Query 4- get the load tasks where the user is the owner of the target env
                        "select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_owners o " +
                            "where lower(task_type) = 'load' and lower(task_status) = 'active' " +
                            "and lower(task_execution_status) = 'active' and t.environment_id = o.environment_id " +
                            "and o.user_name = ? " + "UNION " +
                        //Query 5 - get the tasks, created by the user
                        "select t.* from " + TDMDB_SCHEMA + ".tasks t where lower(task_type) = 'load' " +
                            "and lower(task_status) = 'active' and lower(task_execution_status) = 'active' " +
                            "and split_part(t.task_Created_by, '##', 1) = ? " + "UNION " +
                        //Query 6 - get the task created by another user which belongs to the same role of the user on the target env
                        "select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_roles r, " +
                            TDMDB_SCHEMA + ".environment_role_users u " + "where lower(task_type) = 'load' and lower(task_status) = 'active' " +
                            "and lower(task_execution_status) = 'active' and split_part(t.task_Created_by, '##', 1) <> ? " + "and not exists (select 1 from " +
                            TDMDB_SCHEMA + ".environment_owners o1 where o1.environment_id  = t.environment_id and o1.user_name = split_part(t.task_Created_by, '##', 1)) " +
                            "and t.environment_id = r.environment_id and r.role_id = u.role_id " +
                            "and lower(r.role_status) = 'active' and (split_part(t.task_Created_by, '##', 1) = u.username or lower(u.username) = 'all') " +
                            "and exists (select 1 from " + "(Select env_list.environment_id, u.role_id " + "From " + TDMDB_SCHEMA + ".environment_roles r, " +
                            TDMDB_SCHEMA + ".environment_role_users u, " + "(Select r1.environment_id, 'user' As assignment_type " + "From " +
                            TDMDB_SCHEMA + ".environment_roles r1," + TDMDB_SCHEMA + ". environment_role_users u1 " +
                            "Where r1.role_id = u1.role_id And u1.username = ? And " + "lower(r1.role_status) = 'active' " + "UNION " +
                            "Select r2.environment_id, 'all' As assignment_Type " + "From " + TDMDB_SCHEMA + ".environment_roles r2, " +
                            TDMDB_SCHEMA + ".environment_role_users u2 " + "Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
                            "And lower(r2.role_status) = 'active' And Not exists " + "(select 1 from " + TDMDB_SCHEMA +
                            ".environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
                            "Where r.environment_id = env_list.environment_id " + "And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
                            "And ((env_list.assignment_type = 'all' And " + "lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
                            "where user_roles.role_id = r.role_id)";

        String testerQuery =
                //Query 1 - get the tasks, created by the user
                "select t.* from " + TDMDB_SCHEMA + ".tasks t where lower(task_type) = 'extract' " +
                "and lower(task_status) = 'active' and lower(task_execution_status) = 'active' " + "and split_part(t.task_Created_by, '##', 1) = ? " + "UNION " +
                        //Query 2 - get the task created by another user which belongs to the same role of the user
                        "select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_roles r, " +
                            TDMDB_SCHEMA + ".environment_role_users u " + "where lower(task_type) = 'extract' and lower(task_status) = 'active' " +
                            "and lower(task_execution_status) = 'active' and split_part(t.task_Created_by, '##', 1) <> ? " + "and not exists (select 1 from " +
                            TDMDB_SCHEMA + ".environment_owners o1 where o1.environment_id  = t.source_environment_id and o1.user_name = split_part(t.task_Created_by, '##', 1)) " +
                            "and t.source_environment_id = r.environment_id " + "and r.role_id = u.role_id and lower(r.role_status) = 'active' " +
                            "and (split_part(t.task_Created_by, '##', 1) = u.username or lower(u.username) = 'all') " + "and exists (select 1 from " +
                            "(Select env_list.environment_id, u.role_id " + "From " + TDMDB_SCHEMA + ".environment_roles r, " +
                            TDMDB_SCHEMA + ".environment_role_users u, " + "(Select r1.environment_id, 'user' As assignment_type " + "From " +
                            TDMDB_SCHEMA + ".environment_roles r1, " + TDMDB_SCHEMA + ".environment_role_users u1 " +
                            "Where r1.role_id = u1.role_id And u1.username = ? And " + "lower(r1.role_status) = 'active' " + "UNION " +
                            "Select r2.environment_id, 'all' As assignment_Type " + "From " + TDMDB_SCHEMA + ".environment_roles r2, " +
                            TDMDB_SCHEMA + ".environment_role_users u2 " + "Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
                            "And lower(r2.role_status) = 'active' And Not exists " + "(select 1 from " + TDMDB_SCHEMA +
                            ".environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
                            "Where r.environment_id = env_list.environment_id " + "And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
                            "And ((env_list.assignment_type = 'all' And " +
                            "lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " + "where user_roles.role_id = r.role_id) " +
                            "UNION " +
                        //Query 3 - get the tasks, created by the user
                        "select t.* from " + TDMDB_SCHEMA + ".tasks t " + "where lower(task_type) = 'load' and lower(task_status) = 'active' " +
                            "and lower(task_execution_status) = 'active' and split_part(t.task_Created_by, '##', 1) = ? " + "UNION " +
                        //Query 4 - get the task created by another user which belongs to the same role of the user on the target env
                        "select t.* from " + TDMDB_SCHEMA + ".tasks t, " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u " +
                            "where lower(task_type) = 'load' and lower(task_status) = 'active' " + "and lower(task_execution_status) = 'active' and split_part(t.task_Created_by, '##', 1) <> ? " +
                            "and not exists (select 1 from " + TDMDB_SCHEMA + ".environment_owners o1 where o1.environment_id  = t.environment_id " +
                            "and o1.user_name = split_part(t.task_Created_by, '##', 1)) " + "and t.environment_id = r.environment_id and r.role_id = u.role_id and lower(r.role_status) = 'active' " +
                            "and (split_part(t.task_Created_by, '##', 1) = u.username or lower(u.username) = 'all') " + "and exists (select 1 from " + "(Select env_list.environment_id, u.role_id " +
                            "From " + TDMDB_SCHEMA + ".environment_roles r, " + TDMDB_SCHEMA + ".environment_role_users u, " + "(Select r1.environment_id, 'user' As assignment_type " +
                            "From " + TDMDB_SCHEMA + ".environment_roles r1, " + TDMDB_SCHEMA + ".environment_role_users u1 " + "Where r1.role_id = u1.role_id And u1.username = ? And " +
                            "lower(r1.role_status) = 'active' " + "UNION " + "Select r2.environment_id, 'all' As assignment_Type " + "From " + TDMDB_SCHEMA + ".environment_roles r2, " +
                            TDMDB_SCHEMA + ".environment_role_users u2 " + "Where r2.role_id = u2.role_id And Lower(u2.username) = 'all' " +
                            "And lower(r2.role_status) = 'active' And Not exists " + "(select 1 from " + TDMDB_SCHEMA +
                            ".environment_role_users r3 where r3.environment_id = r2.environment_id and r3.username = ?)) env_list " +
                            "Where r.environment_id = env_list.environment_id " + "And r.role_id = u.role_id And lower(r.role_status) = 'active' " +
                            "And ((env_list.assignment_type = 'all' And " + "lower(u.username) = 'all') Or (env_list.assignment_type = 'user' And u.username = ?))) user_roles " +
                            "where user_roles.role_id = r.role_id)";


        Object taskList = null;

        switch (userType.toLowerCase()) {
            case "admin":
                taskList = db(TDM).fetch(adminQuery);
                break;
            case "owner":
                Object[] ownerParams = new Object[12];
                Arrays.fill(ownerParams, userName);
                taskList = db(TDM).fetch(envOwnerQuery, ownerParams);
                break;
            case "tester":
                Object[] testerParams = new Object[10];
                Arrays.fill(testerParams, userName);
                taskList = db(TDM).fetch(testerQuery, testerParams);
                break;
            default:
                log.error("wsGetUserTasks - Wrong User Type, supported types: admin, owner, tester");
                return wrapWebServiceResults("FAILED", "Wrong User Type - " + userType + ", supported types: admin, owner, tester", null);
        }


        // convert iterable to serializable object
        if (taskList instanceof Db.Rows) {
            ArrayList<Map> rows = new ArrayList<>();
            ((Db.Rows) taskList).forEach(row -> {
                HashMap copy = new HashMap();
                copy.putAll(row);
                rows.add(copy);
            });
            taskList = rows;
        }

        return wrapWebServiceResults("SUCCESS", null, taskList);
    }


    @desc("Returns the details of the input task execution ID if populated. \r\n" + "When the task execution id is empty, the API returns current/last execution of the given task id. If the latest task execution is pending, it will only return its status, else it will return the statistics of the entities handled by the task execution.")
    @webService(path = "wsTaskMonitor/{taskID}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.XML, example = "{\n  \"result\": {\n    \"Task ID\": \"10\",\n    \"Task Details\": [\n      {\n        \"Fabric Batch ID\": \"cc02e34d-2367-4a15-aae7-79dcb4c3e48e\",\n        \"Task Statistics\": [\n          {\n            \"Status\": \"\",\n            \"Ent./sec (avg.)\": \"2.9\",\n            \"Added\": 0,\n            \"Ent./sec (pace)\": \"2.9\",\n            \"Updated\": 3,\n            \"Failed\": \"0\",\n            \"Duration\": \"00:00:01\",\n            \"End time\": \"2021-06-17 12:23:42.464\",\n            \"Name\": \"b6b8f7b8-5eb8-4b27-9b26-c0a387f17ba8\",\n            \"Succeeded\": \"3\",\n            \"Total\": \"--\",\n            \"Level\": \"Node\",\n            \"Remaining dur.\": \"00:00:00\",\n            \"Remaining\": \"0\",\n            \"Start time\": \"2021-06-17 12:23:41.429\",\n            \"Unchanged\": 0,\n            \"% Completed\": \"100\"\n          },\n          {\n            \"Status\": \"\",\n            \"Ent./sec (avg.)\": \"2.9\",\n            \"Added\": 0,\n            \"Ent./sec (pace)\": \"2.9\",\n            \"Updated\": 3,\n            \"Failed\": \"0\",\n            \"Duration\": \"00:00:01\",\n            \"End time\": \"2021-06-17 12:23:42.464\",\n            \"Name\": \"DC1\",\n            \"Succeeded\": \"3\",\n            \"Total\": \"--\",\n            \"Level\": \"DC\",\n            \"Remaining dur.\": \"00:00:00\",\n            \"Remaining\": \"0\",\n            \"Start time\": \"2021-06-17 12:23:41.429\",\n            \"Unchanged\": 0,\n            \"% Completed\": \"100\"\n          },\n          {\n            \"Status\": \"DONE\",\n            \"Ent./sec (avg.)\": \"2.9\",\n            \"Added\": 0,\n            \"Ent./sec (pace)\": \"2.9\",\n            \"Updated\": 3,\n            \"Failed\": \"0\",\n            \"Duration\": \"00:00:01\",\n            \"End time\": \"2021-06-17 12:23:42.464\",\n            \"Name\": \"--\",\n            \"Succeeded\": \"3\",\n            \"Total\": \"3\",\n            \"Level\": \"Cluster\",\n            \"Remaining dur.\": \"00:00:00\",\n            \"Remaining\": \"0\",\n            \"Start time\": \"2021-06-17 12:23:41.429\",\n            \"Unchanged\": 0,\n            \"% Completed\": \"100\"\n          }\n        ],\n        \"Task Status\": \"completed\",\n        \"LU Name\": \"PATIENT_LU\"\n      },\n      {\n        \"Fabric Batch ID\": \"cef3e9fb-2f11-437f-8859-da535300930a\",\n        \"Task Statistics\": [\n          {\n            \"Status\": \"IN_PROGRESS\",\n            \"Ent./sec (avg.)\": \"0\",\n            \"Ent./sec (pace)\": \"0\",\n            \"Failed\": \"0\",\n            \"Duration\": \"00:00:01\",\n            \"End time\": \"-\",\n            \"Name\": \"--\",\n            \"Succeeded\": \"0\",\n            \"Total\": \"19\",\n            \"Level\": \"Cluster\",\n            \"Remaining dur.\": \"00:00:01\",\n            \"Remaining\": \"19\",\n            \"Start time\": \"2021-06-17 12:24:02.523\",\n            \"% Completed\": \"0\"\n          }\n        ],\n        \"Task Status\": \"running\",\n        \"LU Name\": \"PATIENT_VISITS\"\n      }\n    ],\n    \"Task Name\": \"Extract2\",\n    \"Task Execution ID\": 50,\n    \"Task Reference Statistics\": {\n      \"PATIENT_LU\": {\n        \"minStartExecutionDate\": \"Thu Jun 17 12:23:41 UTC 2021\",\n        \"maxEndExecutionDate\": \"Thu Jun 17 12:23:42 UTC 2021\",\n        \"totNumOfTablesToProcess\": 2,\n        \"numOfProcessedRefTables\": 2,\n        \"numOfCopiedRefTables\": 2,\n        \"numOfFailedRefTables\": 0,\n        \"numOfProcessingRefTables\": 0,\n        \"numberOfNotStartedRefTables\": 0\n      },\n      \"PATIENT_VISITS\": {\n        \"minStartExecutionDate\": \"Thu Jun 17 12:23:41 UTC 2021\",\n        \"maxEndExecutionDate\": \"Thu Jun 17 12:23:52 UTC 2021\",\n        \"totNumOfTablesToProcess\": 1,\n        \"numOfProcessedRefTables\": 1,\n        \"numOfCopiedRefTables\": 1,\n        \"numOfFailedRefTables\": 0,\n        \"numOfProcessingRefTables\": 0,\n        \"numberOfNotStartedRefTables\": 0\n      }\n    }\n  },\n  \"errorCode\": \"SUCCESS\",\n  \"message\": null\n}")
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": {\r\n" + "    \"Task ID\": \"300\",\r\n" + "    \"Task Details\": [\r\n" + "      {\r\n" + "        \"Task Status\": \"Pending\",\r\n" + "        \"LU Name\": \"Billing\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"Task Status\": \"Pending\",\r\n" + "        \"LU Name\": \"Collection\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"Task Status\": \"Pending\",\r\n" + "        \"LU Name\": \"Orders\"\r\n" + "      },\r\n" + "      {\r\n" + "        \"Fabric Batch ID\": \"c03d06e2-3766-428c-8465-79e6487b2887\",\r\n" + "        \"Task Statistics\": [\r\n" + "          {\r\n" + "            \"Status\": \"\",\r\n" + "            \"Ent./sec (avg.)\": \"242.01\",\r\n" + "            \"Added\": 0,\r\n" + "            \"Ent./sec (pace)\": \"242.01\",\r\n" + "            \"Updated\": 1000,\r\n" + "            \"Failed\": \"0\",\r\n" + "            \"Duration\": \"00:00:04\",\r\n" + "            \"End time\": \"2021-06-20 09:08:19.487\",\r\n" + "            \"Name\": \"2373be01-f751-47ee-926b-c6e9312aab6e\",\r\n" + "            \"Succeeded\": \"1000\",\r\n" + "            \"Total\": \"--\",\r\n" + "            \"Level\": \"Node\",\r\n" + "            \"Remaining dur.\": \"00:00:00\",\r\n" + "            \"Remaining\": \"0\",\r\n" + "            \"Start time\": \"2021-06-20 09:08:15.355\",\r\n" + "            \"Unchanged\": 0,\r\n" + "            \"% Completed\": \"100\"\r\n" + "          },\r\n" + "          {\r\n" + "            \"Status\": \"\",\r\n" + "            \"Ent./sec (avg.)\": \"242.01\",\r\n" + "            \"Added\": 0,\r\n" + "            \"Ent./sec (pace)\": \"242.01\",\r\n" + "            \"Updated\": 1000,\r\n" + "            \"Failed\": \"0\",\r\n" + "            \"Duration\": \"00:00:04\",\r\n" + "            \"End time\": \"2021-06-20 09:08:19.487\",\r\n" + "            \"Name\": \"DC1\",\r\n" + "            \"Succeeded\": \"1000\",\r\n" + "            \"Total\": \"--\",\r\n" + "            \"Level\": \"DC\",\r\n" + "            \"Remaining dur.\": \"00:00:00\",\r\n" + "            \"Remaining\": \"0\",\r\n" + "            \"Start time\": \"2021-06-20 09:08:15.355\",\r\n" + "            \"Unchanged\": 0,\r\n" + "            \"% Completed\": \"100\"\r\n" + "          },\r\n" + "          {\r\n" + "            \"Status\": \"DONE\",\r\n" + "            \"Ent./sec (avg.)\": \"242.01\",\r\n" + "            \"Added\": 0,\r\n" + "            \"Ent./sec (pace)\": \"242.01\",\r\n" + "            \"Updated\": 1000,\r\n" + "            \"Failed\": \"0\",\r\n" + "            \"Duration\": \"00:00:04\",\r\n" + "            \"End time\": \"2021-06-20 09:08:19.487\",\r\n" + "            \"Name\": \"--\",\r\n" + "            \"Succeeded\": \"1000\",\r\n" + "            \"Total\": \"1000\",\r\n" + "            \"Level\": \"Cluster\",\r\n" + "            \"Remaining dur.\": \"00:00:00\",\r\n" + "            \"Remaining\": \"0\",\r\n" + "            \"Start time\": \"2021-06-20 09:08:15.355\",\r\n" + "            \"Unchanged\": 0,\r\n" + "            \"% Completed\": \"100\"\r\n" + "          }\r\n" + "        ],\r\n" + "        \"Task Status\": \"running\",\r\n" + "        \"LU Name\": \"Customer\"\r\n" + "      }\r\n" + "    ],\r\n" + "    \"Task Name\": \"testRefAndEntities\",\r\n" + "    \"Task Execution ID\": 499,\r\n" + "    \"Task Reference Statistics\": {\r\n" + "      \"Customer\": {\r\n" + "        \"minStartExecutionDate\": \"Sun Jun 20 09:08:15 UTC 2021\",\r\n" + "        \"maxEndExecutionDate\": \"Sun Jun 20 09:08:20 UTC 2021\",\r\n" + "        \"totNumOfTablesToProcess\": 1,\r\n" + "        \"numOfProcessedRefTables\": 1,\r\n" + "        \"numOfCopiedRefTables\": 1,\r\n" + "        \"numOfFailedRefTables\": 0,\r\n" + "        \"numOfProcessingRefTables\": 0,\r\n" + "        \"numberOfNotStartedRefTables\": 0\r\n" + "      }\r\n" + "    }\r\n" + "  },\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsTaskMonitor(@param(required = true) String taskID, @param(description = "Task execution id. This parameter enables monitoring a given task execution.") String executionID) throws Exception {
        String executionIdWhere = executionID == null ? "(SELECT MAX(TASK_EXECUTION_ID) FROM TASK_EXECUTION_LIST L2 WHERE TASK_ID = " + taskID + ")" : executionID;
        String getExecIDsQuery = "SELECT task_execution_id, execution_status, fabric_execution_id, lu_name as name, task_title, 'LU' as type " + 
            "FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_LIST L, " + TDMDB_SCHEMA + ".TASKS_LOGICAL_UNITS U, " + TDMDB_SCHEMA + ".TASKS T " + 
            "WHERE t.task_id = l.task_id AND u.task_id = l.task_id AND u.lu_id = l.lu_id AND T.task_id = " + taskID + 
            " AND l.task_execution_id = " + executionIdWhere + " and process_id = 0 " + 
            "UNION SELECT task_execution_id, execution_status, fabric_execution_id, process_name as name, task_title, " + 
            "CASE WHEN lower(process_type) = 'pre' THEN 'Pre Process' ELSE 'Post Process' END as type " + 
            "FROM " + TDMDB_SCHEMA + ".TASK_EXECUTION_LIST L, " + TDMDB_SCHEMA + ".TASKS_EXE_PROCESS P, " + TDMDB_SCHEMA + ".TASKS T " +
            "WHERE t.task_id = l.task_id AND p.task_id = l.task_id AND p.process_id = l.process_id AND T.task_id = " + taskID + 
            " AND l.task_execution_id = " + executionIdWhere + " AND lu_id = 0";

        Db.Rows execIDsList = db(TDM).fetch(getExecIDsQuery);

        HashMap<String, Object> taskInfo = new HashMap<>();
        List<Object> taskList = new ArrayList<>();

        taskInfo.put("Task ID", taskID);
        boolean firstRecInd = true;
        String taskExecutionId = "";

        for (Db.Row execRec : execIDsList) {
            if (firstRecInd) {
                firstRecInd = false;
                taskInfo.put("Task Name", execRec.get("task_title"));
                taskInfo.put("Task Execution ID", execRec.get("task_execution_id"));
            }
            HashMap<String, Object> taskLUInfo = new HashMap<>();
            String execStatus = "" + execRec.get("execution_status");
            if ("LU".equals(execRec.get("type"))) {
                taskLUInfo.put("LU Name", execRec.get("name"));
                taskExecutionId = "" + execRec.get("task_execution_id");
            } else {
                taskLUInfo.put("Process Name", execRec.get("name"));
            }
            taskLUInfo.put("Task Status", execStatus);
            String fabricExecutionId = "" + execRec.get("fabric_execution_id");
            if (!"pending".equalsIgnoreCase(execStatus) && "LU".equals(execRec.get("type")) && !"".equals(fabricExecutionId) && !"null".equals(fabricExecutionId)) {
                taskLUInfo.put("Fabric Batch ID", execRec.get("fabric_execution_id"));
                taskLUInfo.put("Task Statistics", fnBatchStatistics(fabricExecutionId, "S"));
            }

            taskList.add(taskLUInfo);
        }

        taskInfo.put("Task Details", taskList);
        if (!"".equals(taskExecutionId)) {
            Map<String, Object> refSummaryStatsBuf = fnGetReferenceSummaryData(taskExecutionId);
            // Convert the dates to strings
            refSummaryStatsBuf.forEach((key, value) -> {
                String minStartDate = "";
                String maxEndDate = "";
                Map<String, Object> refSummaryStats = (HashMap) value;

                if (refSummaryStats.get("minStartExecutionDate") != null)
                    minStartDate = refSummaryStats.get("minStartExecutionDate").toString();

                if (refSummaryStats.get("maxEndExecutionDate") != null)
                    maxEndDate = refSummaryStats.get("maxEndExecutionDate").toString();

                refSummaryStats.put("minStartExecutionDate", minStartDate);
                refSummaryStats.put("maxEndExecutionDate", maxEndDate);

                //log.info("after calling fnGetReferenceSummaryData");
            });

            taskInfo.put("Task Reference Statistics", refSummaryStatsBuf);
        }


        if (execIDsList != null) {
            execIDsList.close();
        }
        return wrapWebServiceResults("SUCCESS", null, taskInfo);
    }


    @desc("Returns active task ID by its name.")
    @webService(path = "wsGetTaskId/{taskName}", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.XML, example = "<HashMap>\r\n" + "\t<result>5</result>\r\n" + "\t<errorCode>SUCCESS</errorCode>\r\n" + "\t<message/>\r\n" + "</HashMap>")
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "\t\"result\": 5,\r\n" + "\t\"errorCode\": \"SUCCESS\",\r\n" + "\t\"message\": null\r\n" + "}")
    public static Object wsGetTaskId(@param(required = true) String taskName) throws Exception {
        Object response = db(TDM).fetch("select t.task_id from " + TDMDB_SCHEMA + ".tasks t where t.task_title= ? and t.task_status='Active'", taskName).firstValue();
        if (response == null) {
            return wrapWebServiceResults("FAILED", "No active task found for task name '" + taskName + "'.", response);
        } else {
            return wrapWebServiceResults("SUCCESS", null, response);
        }
    }
    //end from TDMTasks.logic

    @desc("Updates Task Globals.\r\n" + "\r\n" + "Example of a request body:\r\n" + "\r\n" + "{\r\n" + "  \"globals\": [\r\n" + "    {\r\n" + "      \"global_name\": \"MASK_FLAG\",\r\n" + "      \"lu_name\": \"Customer\",\r\n" + "      \"global_value\": \"false\"\r\n" + "    }\r\n" + "  ]\r\n" + "}")
    @webService(path = "task/{taskId}/globals", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    public static Object UpdateTaskGlobals(@param(required = true) Long taskId, List<Map<String, Object>> globals) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        String sql = "UPDATE " + TDMDB_SCHEMA + ".task_globals SET " + "global_value=(?) " + "WHERE task_id = ? AND global_name = ?";
        try {
            if (globals != null && globals.size() != 0) {
                for (Map<String, Object> global : globals) {
                    String globalName = "" + global.get("global_name");
                    String globalValue = "" + global.get("global_value");
                    String luName = "" + global.get("lu_name");
                    if (luName != null && !"".equals(luName) && !"ALL".equals(luName) && !"null".equals(luName)) {
                        globalName = luName + "." + globalName;
                    }
                    db(TDM).execute(sql, globalValue, taskId, globalName);
                }
            }
            response.put("result", new HashMap<>());
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("Deletes Task")
    @webService(path = "task/{taskId}/taskname/{taskName}", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    public static Object wsDeleteTask(@param(required = true) Long taskId, @param(required = true) String taskName) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC).format(Instant.now());

        try {
            String sql = "UPDATE " + TDMDB_SCHEMA + ".tasks SET " + "task_status=(?), task_execution_status=(?), " + "task_last_updated_date=(?), " + "task_last_updated_by=(?) " + "WHERE task_id = " + taskId;
            String username = sessionUser().name();
            db(TDM).execute(sql, "Inactive", "Inactive", now, username);
            String fav = "DELETE FROM " + TDMDB_SCHEMA + ".task_user_favorites WHERE favorite_item_id = ? AND favorite_item_type = 'Task' ";
            db(TDM).execute(fav, taskId);
            try {
                String activityDesc = "Task " + taskName + " was deleted";
                fnInsertActivity("delete", "Tasks", activityDesc);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Get the list of environments that are aligned with the input filtering parameters, according to the task type and attributes, as follows: \r\n" + "- If the task type is Extract , then validate and return the list of available source environments.\r\n" + "- If the task type is Load, then validate and return both - source and target environments.\r\n" + "- If the task type is Reserve (reserve only task), then validate and return the list of available target environments.\r\n" + "- If the task is a delete only task, then validate and return the list of available target environments.")
    @webService(path = "getEnvironmentsForTaskAttr", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"source environments\": [\r\n" + "        {\r\n" + "          \"environment_id\": \"4\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"ENV6\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"environment_id\": \"1\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"ENV1\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"environment_id\": \"6\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"ENV3\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"environment_id\": \"3\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"ENV4\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"environment_id\": \"9\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"Env9\"\r\n" + "        }\r\n" + "      ]\r\n" + "    },\r\n" + "    {\r\n" + "      \"target environments\": [\r\n" + "        {\r\n" + "          \"environment_id\": \"2\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"ENV2\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"environment_id\": \"5\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"ENV5\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"environment_id\": \"6\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"ENV3\"\r\n" + "        },\r\n" + "        {\r\n" + "          \"environment_id\": \"9\",\r\n" + "          \"role_id\": \"admin\",\r\n" + "          \"environment_name\": \"Env9\"\r\n" + "        }\r\n" + "      ]\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetEnvironmentsByTaskFilteringParams(@param(description = "Populated by the task's be_id (Business Entity ID). For example: 1.", required = true) String be, @param(description = "Populated by the task's Logical Units (LU IDs), separated by comma. Example : 1,5,6,7", required = true) String lus, Integer refcount,
     @param(description = "Can be populated by the following values: \"L\" (entity list), \"R\" (random selection), \"C\" (custom logic), \"PR\" (parameters with a random selection), \"P\" (parameters), \"ALL\" (all entities), or \"TABLES\" (tables only task)") String selection_method, 
     @param(description = "Can be populated by 'OFF', 'FORCE', or can be empty") String sync_mode, Boolean version_ind, Boolean replace_sequences, 
     Boolean delete_before_load, @param(description = "Populated by \"extract\", \"load\", or \"reserve\"", required = true) String task_type, 
     Boolean reserve_ind, Boolean clone_ind) throws Exception {
        // variables declaration
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        String env_id;
        String role_id;
        String env_name;

        List<Map<String, Object>> finalSourceEnvs = new ArrayList<>();
        List<Map<String, Object>> finalTargetEnvs = new ArrayList<>();

        lus = lus.replaceAll("\\s+", "");
        String[] lus_arr = lus.split(",");
        ArrayList<String> lus_list = new ArrayList<String>();
        for (String str : lus_arr)
            lus_list.add(str);
        Map<String, Object> be_lus = new HashMap<String, Object>();
        be_lus.put("be_id", be);
        be_lus.put("LU List", lus_list);

        try {

            List<Map<String, Object>> allUserEnvsTypes = fnGetUserEnvs("");
            int i = 0;
            for (Map<String, Object> envType : allUserEnvsTypes) {
                List<Map<String, Object>> allSourceEnvs = (List<Map<String, Object>>) (envType.get("source environments"));
                List<Map<String, Object>> allTargetEnvs = (List<Map<String, Object>>) (envType.get("target environments"));

                if (allSourceEnvs != null && !"reserve".equalsIgnoreCase(task_type)) {
                    // loop over user source envs
                    for (Map<String, Object> sourceEnvMap : allSourceEnvs) {

                        env_id = "" + sourceEnvMap.get("environment_id");
                        role_id = "" + sourceEnvMap.get("role_id");
                        env_name = "" + sourceEnvMap.get("environment_name");

                        //check if source env satisfies all relevant cases
                        if (fnValidateSourceEnvForTask(be_lus, refcount, selection_method, sync_mode, version_ind, task_type, sourceEnvMap,null,0L).isEmpty()) {
                            Map<String, Object> envData = new HashMap<>();
                            envData.put("environment_id", env_id);
                            envData.put("environment_name", env_name);
                            envData.put("role_id", role_id);
                            finalSourceEnvs.add(envData);

                        }
                    }
                }

                // loop over user target envs only if it is a load task, otherwsie target envs are not relevant
                if (("load".equalsIgnoreCase(task_type) || "reserve".equalsIgnoreCase(task_type)) && allTargetEnvs != null) {
                    for (Map<String, Object> targetEnvMap : allTargetEnvs) {
                        env_id = "" + targetEnvMap.get("environment_id");
                        role_id = "" + targetEnvMap.get("role_id");
                        env_name = "" + targetEnvMap.get("environment_name");
                        int noOfEntities = -1;
                        //check if target env satisfies all relevant cases
                        if (fnValidateTargetEnvForTask(be_lus, refcount, selection_method, version_ind, replace_sequences, delete_before_load, task_type, reserve_ind, noOfEntities, targetEnvMap, clone_ind,sync_mode,null,0L).isEmpty()) {
                            Map<String, Object> envData = new HashMap<>();
                            envData.put("environment_id", env_id);
                            envData.put("environment_name", env_name);
                            envData.put("role_id", role_id);
                            finalTargetEnvs.add(envData);

                        }
                    }
                }
            }

            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> sourceEnvsMap = new HashMap<>();
            sourceEnvsMap.put("source environments", finalSourceEnvs);
            result.add(sourceEnvsMap);
            Map<String, Object> targetEnvsMap = new HashMap<>();
            targetEnvsMap.put("target environments", finalTargetEnvs);
            result.add(targetEnvsMap);

            response.put("result", result);
            errorCode = "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            errorCode = "FAILED";
        }


        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("Checks if the list of requested entities are copied successfully in the selected data version across all the input LU names in the task'sBusiness Entity hierarchy (the versionsForLoad API only checks the status of root LUI). For example, if the customer's order is not extracted succesfuly to Fabric, the validation on the related customer returns it as a failed entity. \r\n" + " \r\n" + "Example of a request body:\r\n" + "{\r\n" + "  \"entitiesList\": \"1,2\",\r\n" + "  \"taskExecId\": \"2\",\r\n" + "  \"lu_list\": [\r\n" + "  {\r\n" + "   \"lu_name\": \"Billing\"\r\n" + "  },\r\n" + "  {\r\n" + "    \"lu_name\": \"Customer\"\r\n" + "  },\r\n" + "  {\r\n" + "    \"lu_name\": \"Orders\"\r\n" + "  }\r\n" + "]\r\n" + "}")
    @webService(path = "tasks/validateVersionForLoad", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsDataVersionValidation(@param(required = true) String entitiesList, @param(required = true) String taskExecId, @param(required = true) List<Map<String, Object>> lu_list) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "SUCCESS";
        List<HashMap<String, Object>> result = new ArrayList<>();

        String logicalUnitList = "";
        for (Map<String, Object> lu : lu_list) {
            logicalUnitList += ("'" + lu.get("lu_name") + "',");
        }

        if (logicalUnitList != "") logicalUnitList = logicalUnitList.substring(0, logicalUnitList.length() - 1);

        entitiesList = entitiesList.replaceAll("\\s+", "");
        String[] entitiesArray = entitiesList.split(",");
        String entitiesListForQuery = "";
        for (String entityID : entitiesArray) {
            entitiesListForQuery += "'" + entityID + "',";
        }

        if (entitiesListForQuery != "")
            entitiesListForQuery = entitiesListForQuery.substring(0, entitiesListForQuery.length() - 1);

        fabric().fetch("get TDM.?", taskExecId);

        String sqlQuery = "SELECT be_root_entity_id, lu_name, iid FROM task_execution_link_entities " + "WHERE lu_name IN " + "(" + logicalUnitList + ")" + " AND be_root_entity_id IN " + "(" + entitiesListForQuery + ") " + "AND lower(execution_status) != 'completed'";

        //log.info("sqlQuery: " + sqlQuery);
        Db.Rows failedEntities = fabric().fetch(sqlQuery);

        Set<String> setOFFailedEntities = new HashSet<>();
        String listOfFailed = "";
        for (Db.Row row : failedEntities) {
            HashMap<String, Object> failedEntity = new HashMap<>();
            failedEntity.put("root_entity_id", row.get("be_root_entity_id"));
            failedEntity.put("lu_name", row.get("lu_name"));
            failedEntity.put("lu_entity_id", row.get("iid"));
            result.add(failedEntity);
            setOFFailedEntities.add("" + row.get("be_root_entity_id"));
        }

        for (String failedEntityID : setOFFailedEntities) {
            if ("".equals(listOfFailed)) {
                listOfFailed = failedEntityID;
            } else {
                listOfFailed += ", " + failedEntityID;
            }
        }
        if (result != null && result.size() > 0) {
            errorCode = "FAILED";
            message = listOfFailed;
        }

        if (failedEntities != null) {
            failedEntities.close();
        }
        response.put("result", result);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }


    @desc("Get the list of custom logic flows to be used to select the list of entities for give Business Entity and Environment.")
    @webService(path = "getcustomlogicflows", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"luName\": \"Customer\",\r\n" + "      \"flowName\": \"test\",\r\n" + "      \"Description\": \"test 1\"\r\n" + "    },\r\n" + "    {\r\n" + "      \"luName\": \"Customer\",\r\n" + "      \"flowName\": \"test2\",\r\n" + "      \"Description\": \"test 2\"\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetCustomLogicFlows(String beName, String envName) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "SUCCESS";
        List<HashMap<String, Object>> result = new ArrayList<>();

        //luList = luList.replaceAll("\\s+", "");
        //String[] lus = luList.split(",");

        String luListSql = "SELECT lu_name FROM " + TDMDB_SCHEMA + ".BUSINESS_ENTITIES b, " + TDMDB_SCHEMA + ".PRODUCT_LOGICAL_UNITS lu, " + TDMDB_SCHEMA + ".ENVIRONMENTS e, " + TDMDB_SCHEMA + ".ENVIRONMENT_PRODUCTS ep " + "WHERE b.be_name = ? and b.be_status = 'Active' AND b.be_id = lu.be_id AND lu.lu_parent_id is null " + "AND e.environment_name = ? AND e.environment_status = 'Active' AND e.environment_id = ep.environment_id " + "AND ep.product_id = lu.product_id AND ep.status = 'Active' ";

        Db.Rows luList = db(TDM).fetch(luListSql, beName, envName);
        for (Db.Row row : luList) {
            String luName = row.get("lu_name").toString();
            List<HashMap<String, Object>> luResult = (List<HashMap<String, Object>>) (fabric().fetch("broadway " + luName + ".GetCustomLogicFlows LU_NAME='" + luName + "'").firstRow()).get("value");
            if (luResult != null) {
                result.addAll(luResult);
            }
        }

        //Add flows that can run for all LUs
        List<HashMap<String, Object>> luResult = (List<HashMap<String, Object>>) (fabric().fetch("broadway TDM.GetCustomLogicFlows LU_NAME=''").firstRow()).get("value");
        if (luResult != null) {
            result.addAll(luResult);
        }

        response.put("result", result);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Get the list of parameters of the given custom Flow")
    @webService(path = "getCustomLogicParams", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"editor\": {\r\n" + "        \"schema\": {\r\n" + "          \"type\": \"date\"\r\n" + "        },\r\n" + "        \"name\": \"param1\",\r\n" + "        \"context\": {\r\n" + "          \"level\": {\r\n" + "            \"const\": \"error\"\r\n" + "          },\r\n" + "          \"message\": {\r\n" + "            \"const\": \"${param1}, ${param2}\"\r\n" + "          },\r\n" + "          \"param1\": {\r\n" + "            \"self\": \"param1\"\r\n" + "          },\r\n" + "          \"param2\": {\r\n" + "            \"external\": \"param2\"\r\n" + "          }\r\n" + "        },\r\n" + "        \"id\": \"com.k2view.default\"\r\n" + "      },\r\n" + "      \"default\": null,\r\n" + "      \"description\": \"\",\r\n" + "      \"type\": \"date\",\r\n" + "      \"mandatory\": false\r\n" + "    },\r\n" + "    {\r\n" + "      \"editor\": {\r\n" + "        \"schema\": {\r\n" + "          \"type\": \"integer\"\r\n" + "        },\r\n" + "        \"name\": \"param2\",\r\n" + "        \"context\": {\r\n" + "          \"level\": {\r\n" + "            \"const\": \"error\"\r\n" + "          },\r\n" + "          \"message\": {\r\n" + "            \"const\": \"${param1}, ${param2}\"\r\n" + "          },\r\n" + "          \"param1\": {\r\n" + "            \"external\": \"param1\"\r\n" + "          },\r\n" + "          \"param2\": {\r\n" + "            \"self\": \"param2\"\r\n" + "          }\r\n" + "        },\r\n" + "        \"id\": \"com.k2view.default\"\r\n" + "      },\r\n" + "      \"default\": null,\r\n" + "      \"description\": \"\",\r\n" + "      \"type\": \"integer\",\r\n" + "      \"mandatory\": false\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetCustomLogicParam(String luName, String flowName) throws Exception {
        try {
            List<Map<String, Object>> result = fnGetFlowParams(luName, flowName);
            return wrapWebServiceResults("SUCCESS", null, result);
        } catch (Exception e) {
            return wrapWebServiceResults("FAILED", e.getMessage(), null);
        }
    }

    public enum PARAMS_TO_IGNORE {
        PARENT_ROW,
        PARENT_ROWS,
        TABLE,
        TEMPLATE_ROW,
        TOTAL,
        LU_NAME,
        NUM_OF_ENTITIES,
        COUNT;
       
        private static final Set<String> IGNORE_PARAMS =
                Arrays.stream(values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

        public static boolean containsIgnoreCase(String name) {
            return name != null && IGNORE_PARAMS.stream()
                .anyMatch(n -> n.equalsIgnoreCase(name));
        }
    }

   @desc("Get the list of parameters of the given custom Flow.\r\n" + "It gets a list of LU Names separated by comma, and optional task id. Task Id will be sent in case of updating an existing task, to get the values already set in that task.\r\nOptional taskExecutionId: when provided, pre-populates parameter values from the override params used in that specific execution.")
    @webService(path = "getDMPopParams", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "\t\"result\": {\r\n" + "\t\t\"activity_note\": {\r\n" + "\t\t\t\"editor\": {\r\n" + "\t\t\t\t\"name\": \"activity_note\",\r\n" + "\t\t\t\t\"schema\": {\r\n" + "\t\t\t\t\t\"type\": \"string\"\r\n" + "\t\t\t\t},\r\n" + "\t\t\t\t\"context\": {\r\n" + "\t\t\t\t\t\"activity_note\": {\r\n" + "\t\t\t\t\t\t\"self\": \"value\"\r\n" + "\t\t\t\t\t},\r\n" + "\t\t\t\t\t\"value\": {\r\n" + "\t\t\t\t\t\t\"const\": \"My Note\"\r\n" + "\t\t\t\t\t}\r\n" + "\t\t\t\t},\r\n" + "\t\t\t\t\"syncOutput\": true,\r\n" + "\t\t\t\t\"id\": \"com.k2view.default\"\r\n" + "\t\t\t},\r\n" + "\t\t\t\"default\": \"My Note\",\r\n" + "\t\t\t\"description\": \"\",\r\n" + "\t\t\t\"type\": \"string\",\r\n" + "\t\t\t\"mandatory\": false\r\n" + "\t\t},\r\n" + "\t\t\"billing_balance_number_of_records\": {\r\n" + "\t\t\t\"editor\": {\r\n" + "\t\t\t\t\"schema\": {\r\n" + "\t\t\t\t\t\"type\": \"integer\"\r\n" + "\t\t\t\t},\r\n" + "\t\t\t\t\"name\": \"billing_balance_number_of_records\",\r\n" + "\t\t\t\t\"context\": {\r\n" + "\t\t\t\t\t\"billing_balance_number_of_records\": {\r\n" + "\t\t\t\t\t\t\"self\": \"distribution\"\r\n" + "\t\t\t\t\t},\r\n" + "\t\t\t\t\t\"distribution\": {\r\n" + "\t\t\t\t\t\t\"const\": {\r\n" + "\t\t\t\t\t\t\t\"distribution\": \"uniform\",\r\n" + "\t\t\t\t\t\t\t\"round\": true,\r\n" + "\t\t\t\t\t\t\t\"type\": \"integer\",\r\n" + "\t\t\t\t\t\t\t\"minimum\": 1,\r\n" + "\t\t\t\t\t\t\t\"maximum\": 3\r\n" + "\t\t\t\t\t\t}\r\n" + "\t\t\t\t\t}\r\n" + "\t\t\t\t},\r\n" + "\t\t\t\t\"id\": \"com.k2view.distribution\"\r\n" + "\t\t\t},\r\n" + "\t\t\t\"default\": {\r\n" + "\t\t\t\t\"distribution\": \"uniform\",\r\n" + "\t\t\t\t\"round\": true,\r\n" + "\t\t\t\t\"type\": \"integer\",\r\n" + "\t\t\t\t\"minimum\": 1,\r\n" + "\t\t\t\t\"maximum\": 3\r\n" + "\t\t\t},\r\n" + "\t\t\t\"description\": \"Distribution Of Records Of table balance\",\r\n" + "\t\t\t\"type\": \"any\",\r\n" + "\t\t\t\"mandatory\": \"false\"\r\n" + "\t\t},\r\n" + "\t\t\"state\": {\r\n" + "\t\t\t\"editor\": {\r\n" + "\t\t\t\t\"name\": \"state\",\r\n" + "\t\t\t\t\"schema\": {\r\n" + "\t\t\t\t\t\"type\": \"string\"\r\n" + "\t\t\t\t},\r\n" + "\t\t\t\t\"context\": {\r\n" + "\t\t\t\t\t\"mtableRandomRow\": {\r\n" + "\t\t\t\t\t\t\"const\": true\r\n" + "\t\t\t\t\t},\r\n" + "\t\t\t\t\t\"mtable\": {\r\n" + "\t\t\t\t\t\t\"const\": \"addresses\"\r\n" + "\t\t\t\t\t},\r\n" + "\t\t\t\t\t\"city\": {\r\n" + "\t\t\t\t\t\t\"external\": \"city\"\r\n" + "\t\t\t\t\t},\r\n" + "\t\t\t\t\t\"state\": {\r\n" + "\t\t\t\t\t\t\"self\": \"state\"\r\n" + "\t\t\t\t\t},\r\n" + "\t\t\t\t\t\"mtableKey\": {\r\n" + "\t\t\t\t\t\t\"const\": {}\r\n" + "\t\t\t\t\t},\r\n" + "\t\t\t\t\t\"mtableCaseSensitive\": {\r\n" + "\t\t\t\t\t\t\"const\": true\r\n" + "\t\t\t\t\t}\r\n" + "\t\t\t\t},\r\n" + "\t\t\t\t\"id\": \"com.k2view.mTableKey\"\r\n" + "\t\t\t},\r\n" + "\t\t\t\"default\": null,\r\n" + "\t\t\t\"description\": \"\",\r\n" + "\t\t\t\"type\": \"string\",\r\n" + "\t\t\t\"mandatory\": false\r\n" + "\t\t}\r\n" + "\t},\r\n" + "\t\"errorCode\": \"SUCCESS\",\r\n" + "\t\"message\": null\r\n" + "}")
    public static Object wsGetDMPopParams(String luList, Long taskId, Long taskExecutionId) throws Exception {
        LinkedHashMap<String, HashMap<String, Object>> result = new LinkedHashMap<>();
        SortedMap<String, HashMap<String, Object>> flowsParams = new TreeMap<>();

        //try {
            luList = luList.replaceAll("\\s+", "");
            String[] lus = luList.split(",");

            for (String luName : lus) {
                String rootTableName = getGlobal("ROOT_TABLE_NAME", luName);
                Db.Rows flows = null;
                
                try {
                    flows = fabric().fetch("list BF lu_name = " + luName + " tag = 'Generate Data'");
                } catch (Exception e) {
                    continue;
                }

                for (Db.Row flow : flows) {
                    String flowName = flow.get("Flow").toString();
                    //log.info("wsGetDMPopParams - Flow Name: " + flowName);
                    Db.Rows rows = fabric().fetch("list BF lu_name = '" + luName + "' flow='" + flowName + "'");
                    for (Db.Row row : rows) {
                        String name = "" + row.get("name");
                        //log.info("wsGetDMPopParams - Parameter Name: " + name + " param: " + row.get("param"));
                        if (!PARAMS_TO_IGNORE.containsIgnoreCase(name) && "input".equalsIgnoreCase("" + row.get("param"))) {
                            //log.info("Adding Param: " + name);
                            HashMap<String, Object> map = new HashMap<>();
                            HashMap<Object, Object> editorMap = new HashMap<>();
                            Map<String, Object[]> editor = Json.get().fromJson(ParamConvertor.toString(row.get("editor")));
                            Map<?, ?> context = Json.get().fromJson(ParamConvertor.toString(row.get("context")));
                            Object[] interfaces = new Object[0];
                            if ("com.k2view.interface".equalsIgnoreCase(String.valueOf(editor.get("id")))) {
                                String broadwayCommand = "Broadway TDM.ListEditorInterfaces editor=" + row.get("editor");
                                Db.Rows interfaceNames = fabric().fetch(broadwayCommand);
                                for (Db.Row Name : interfaceNames) {
                                    interfaces = ParamConvertor.toArray(Name.get("array"));

                                }
                                editor.put("interfaces", interfaces);
                            }
                            if (editor.isEmpty()) {
                                editorMap.put("id", "com.k2view.default");
                            } else {
                                editorMap.putAll(editor);
                            }

                            Object constMap = null;
                            Object schema = null;
                            editorMap.put("name", name);
                            if ("com.k2view.distribution".equals(editorMap.get("id"))) {
                                if (Json.get().fromJson(ParamConvertor.toString(context.get("distribution"))) != null) {
                                    Map<?, ?> distMap = Json.get().fromJson(ParamConvertor.toString(context.get("distribution")));

                                    constMap = Json.get().fromJson(ParamConvertor.toString(distMap.get("const")));
                                    if (constMap != null) {
                                        if (constMap instanceof Map<?,?>) {
                                            schema = Json.get().fromJson(ParamConvertor.toString(((Map<?,?>)constMap).get("type")));
                                        }
                                    }
                                    if (schema != null) {
                                        String schemaJson = "{type=" + schema.toString() + "}";
                                        editorMap.put("schema", Json.get().fromJson(schemaJson));
                                    } else {
                                        editorMap.put("schema", Json.get().fromJson((String) row.get("schema")));
                                    }
                                    
                                } else {
                                    editorMap.put("schema", Json.get().fromJson((String) row.get("schema")));
                                }
                            } else {
                                editorMap.put("schema", Json.get().fromJson((String) row.get("schema")));
                            }
                            editorMap.put("mandatory", row.get("mandatory"));
                            editorMap.put("context", context);
                            map.put("editor", editorMap);
                            map.put("type", row.get("type"));
                            map.put("mandatory", row.get("mandatory"));
                            map.put("default", row.get("default"));

                            String description = ("" + row.get("remark")).replaceAll("/n", "\n");
                            map.put("description", description);
                            map.put("order", 99999999);

                            flowsParams.put(name, map);
                        }
                    }
                    if (rows != null) {
                        rows.close();
                    }
                }

                // Add the distribution of each table in the LU
                List<String> tablesList = getLuTablesList(luName);
                for (String tableName : tablesList) {
                    Map<String, Object> checkmap = new HashMap<>();
                    checkmap.put("lu_name", luName);
                    checkmap.put("table_name", tableName);
                    List<Map<String, Object>> ignoreGenTablelist = MtableLookup("IgnoreGenerateTableDistList", checkmap, MTable.Feature.caseInsensitive);
                    if (!rootTableName.equalsIgnoreCase(tableName) && ignoreGenTablelist.size() == 0) {
                        HashMap<String, Object> map = new HashMap<>();
                        HashMap<Object, Object> editorMap = new HashMap<>();
                        String distParamName = luName.toLowerCase() + "_" + tableName.toLowerCase() + "_number_of_records";
                        String minDist = "" + db(TDM).fetch("SELECT PARAM_VALUE FROM " + TDMDB_SCHEMA + ".TDM_GENERAL_PARAMETERS " + "WHERE PARAM_NAME = 'TABLE_DEFAULT_DISTRIBUTION_MIN'").firstValue();
                        String maxDist = "" + db(TDM).fetch("SELECT PARAM_VALUE FROM " + TDMDB_SCHEMA + ".TDM_GENERAL_PARAMETERS " + "WHERE PARAM_NAME = 'TABLE_DEFAULT_DISTRIBUTION_MAX'").firstValue();
                        String contextString = "{" + distParamName + "={self=distribution},distribution={const={distribution=uniform,round=true,type=integer,minimum=" + minDist + ",maximum=" + maxDist + "}}}";
                        Map<?, ?> distDefault = Json.get().fromJson("{distribution=uniform,round=true,type=integer,minimum=" + minDist + ",maximum=" + maxDist + "}");
                        Map<?, ?> context = Json.get().fromJson(contextString);
                        editorMap.put("id", "com.k2view.distribution");
                        editorMap.put("name", distParamName);
                        editorMap.put("schema", Json.get().fromJson("{type=integer}"));
                        editorMap.put("context", context);
                        editorMap.put("mandatory", false);
                        map.put("editor", editorMap);
                        map.put("type", "any");
                        map.put("mandatory", false);
                        map.put("default", distDefault);
                        map.put("description", "Distribution Of Records Of table " + tableName);
                        flowsParams.put(distParamName, map);
                        map.put("order", 99999999);
                    }
                }
            }

            if (taskExecutionId != null && taskExecutionId > 0) {
                String execSql = "SELECT es.task_id, ovr.override_parameters::text AS override_parameters_json " +
                    "FROM " + TDMDB_SCHEMA + ".task_execution_summary es " +
                    "LEFT JOIN " + TDMDB_SCHEMA + ".task_execution_override_attrs ovr " +
                    "    ON es.task_execution_id = ovr.task_execution_id AND es.task_id = ovr.task_id " +
                    "WHERE es.task_execution_id = ?";
                Db.Row execRow = db(TDM).fetch(execSql, taskExecutionId).firstRow();
                if (execRow != null && !execRow.isEmpty()) {
                    long execTaskId = Long.parseLong(execRow.get("task_id").toString());
                    String luName = getLuType().luName;
                    String selectSql = "broadway " + luName + ".GetGenerateParamsByTaskId task_id=?, RESULT_STRUCTURE=ROW";
                    Db.Row taskParams = fabric().fetch(selectSql, execTaskId).firstRow();
                    Map<String, Object> taskParamsMap = (HashMap) taskParams.get("value");
                    Map<String, Object> execGenParams = null;
                    String overrideParamsJson = (String) execRow.get("override_parameters_json");
                    if (overrideParamsJson != null && !overrideParamsJson.equals("null")) {
                        Map<String, Object> overrideParamsMap = Json.get().fromJson(overrideParamsJson);
                        Object genDataObj = overrideParamsMap.get("GENERATE_DATA_PARAMS");
                        if (genDataObj instanceof Map) {
                            execGenParams = (Map<String, Object>) genDataObj;
                        }
                    }
                    final Map<String, Object> finalExecGenParams = execGenParams;
                    if (taskParamsMap != null && !taskParamsMap.isEmpty()) {
                        for (String paramName : taskParamsMap.keySet()) {
                            HashMap<String, Object> paramMap = flowsParams.get(paramName);
                            if (paramMap != null) {
                                Long order = (Long) ((Map<String, Object>) taskParamsMap.get(paramName)).get("order");
                                Object newValue = (finalExecGenParams != null && finalExecGenParams.containsKey(paramName))
                                    ? finalExecGenParams.get(paramName)
                                    : ((Map<String, Object>) taskParamsMap.get(paramName)).get("value");
                                paramMap.put("value", newValue);
                                paramMap.put("order", order);
                                Object isEditable = ((Map<String, Object>) taskParamsMap.get(paramName)).get("is_editable");
                                if (isEditable != null) {
                                    paramMap.put("is_editable", isEditable);
                                }
                                flowsParams.remove(paramName);
                                result.put(paramName, paramMap);
                            }
                        }
                    } else if (execGenParams != null) {
                        int order = 1;
                        for (String paramName : execGenParams.keySet()) {
                            HashMap<String, Object> paramMap = flowsParams.get(paramName);
                            if (paramMap != null) {
                                paramMap.put("value", execGenParams.get(paramName));
                                paramMap.put("order", (long) order++);
                                flowsParams.remove(paramName);
                                result.put(paramName, paramMap);
                            }
                        }
                    }
                }
            }
            //log.info("wsGetDMPopParams - taskId: " + taskId);
            if (taskId != null && taskId > 0) {
                String luName = getLuType().luName;
                String selectSql = "broadway " + luName + ".GetGenerateParamsByTaskId task_id=?" + ", RESULT_STRUCTURE=ROW";
                //log.info("wsGetDMPopParams - sql: " + selectSql);
                Db.Row taskParams = fabric().fetch(selectSql, taskId).firstRow();
                Map<String, Object> taskParamsMap = (HashMap) taskParams.get("value");
                if (taskParamsMap != null && !taskParamsMap.isEmpty()) {
                    for (String paramName : taskParamsMap.keySet()) {
                        //log.info("wsGetDMPopParams - paramName: " + paramName);
                        HashMap<String, Object> paramMap = flowsParams.get(paramName);
                        if (paramMap != null) {
                            Object newValue = ((Map<String, Object>) taskParamsMap.get(paramName)).get("value");
                            Long order = (Long) ((Map<String, Object>) taskParamsMap.get(paramName)).get("order");
                            //log.info("wsGetDMPopParams - paramValue: " + taskParamsMap.get(paramName));
                            paramMap.put("value", newValue);
                            paramMap.put("order", order);
                            Object isEditable = ((Map<String, Object>) taskParamsMap.get(paramName)).get("is_editable");
                            if (isEditable != null) {
                                paramMap.put("is_editable", isEditable);
                            }
                            flowsParams.remove(paramName);
                            result.put(paramName, paramMap);
                        }
                    }
                }
            }
            result.putAll(flowsParams);
        /*} catch (Exception e) {
            //log.error
            e.printStackTrace();
            log.error(e.getMessage());
            return wrapWebServiceResults("FAILED", e.getMessage(), null);
        }*/

        return wrapWebServiceResults("SUCCESS", null, result);
    }

    @desc("Get the list of parameters of the given Post/Pre Execution Flow")
    @webService(path = "getExecutionProcessParams", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = "{\r\n" + "  \"result\": [\r\n" + "    {\r\n" + "      \"editor\": {\r\n" + "        \"schema\": {\r\n" + "          \"type\": \"date\"\r\n" + "        },\r\n" + "        \"name\": \"param1\",\r\n" + "        \"context\": {\r\n" + "          \"level\": {\r\n" + "            \"const\": \"error\"\r\n" + "          },\r\n" + "          \"message\": {\r\n" + "            \"const\": \"${param1}, ${param2}\"\r\n" + "          },\r\n" + "          \"param1\": {\r\n" + "            \"self\": \"param1\"\r\n" + "          },\r\n" + "          \"param2\": {\r\n" + "            \"external\": \"param2\"\r\n" + "          }\r\n" + "        },\r\n" + "        \"id\": \"com.k2view.default\"\r\n" + "      },\r\n" + "      \"default\": null,\r\n" + "      \"description\": \"\",\r\n" + "      \"type\": \"date\",\r\n" + "      \"mandatory\": false\r\n" + "    },\r\n" + "    {\r\n" + "      \"editor\": {\r\n" + "        \"schema\": {\r\n" + "          \"type\": \"integer\"\r\n" + "        },\r\n" + "        \"name\": \"param2\",\r\n" + "        \"context\": {\r\n" + "          \"level\": {\r\n" + "            \"const\": \"error\"\r\n" + "          },\r\n" + "          \"message\": {\r\n" + "            \"const\": \"${param1}, ${param2}\"\r\n" + "          },\r\n" + "          \"param1\": {\r\n" + "            \"external\": \"param1\"\r\n" + "          },\r\n" + "          \"param2\": {\r\n" + "            \"self\": \"param2\"\r\n" + "          }\r\n" + "        },\r\n" + "        \"id\": \"com.k2view.default\"\r\n" + "      },\r\n" + "      \"default\": null,\r\n" + "      \"description\": \"\",\r\n" + "      \"type\": \"integer\",\r\n" + "      \"mandatory\": false\r\n" + "    }\r\n" + "  ],\r\n" + "  \"errorCode\": \"SUCCESS\",\r\n" + "  \"message\": null\r\n" + "}")
    public static Object wsGetExecutionProcessParam(String processType, List<Map<String, String>> processesList) throws Exception {
        try {
            List<HashMap<String, Object>> result = fnGetExecutionProcessParams(processType, processesList);
            return wrapWebServiceResults("SUCCESS", null, result);
        } catch (Exception e) {
            return wrapWebServiceResults("FAILED", e.getMessage(), null);
        }
    }

	@desc("Get the list of available training tasks for AI generate")
	@webService(path = "tasks/getTrainingModels", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	public static Object wsGetTrainingModels(String fromDate, String toDate,String be_name, String lu_name) throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String trainingTaskQry = "";
		
		try {
		    String AI_Interface = (String) fabric().fetch("set AI_DB_INTERFACE").firstValue();
            String k2systemSchema = "k2system";
            // Object clusterId = fabric().fetch("clusterid").firstValue();
            // if (clusterId != null && !"".equals(clusterId)) {
            //     k2systemSchema = k2systemSchema + "_" + clusterId;
            // }
		    trainingTaskQry = "select id as task_execution_id ,creation_time from " + k2systemSchema + ".task_executions where status='DONE' and task_type='TRAINING'";
		
		    if (fromDate != null && !"".equals(fromDate) && toDate != null && !"".equals(toDate)) {
		        trainingTaskQry += " and creation_time::date >= '" + fromDate + "' and creation_time::date <= '" + toDate + "'";
		    }
            trainingTaskQry += "order by creation_time desc ;";
		    Db.Rows rows = db(AI_Interface).fetch(trainingTaskQry);
		    List<Map<String, Object>> rowsList = new ArrayList<>();
		    for (Db.Row row : rows) {
		        String task_exe_id = "" + row.get("task_execution_id") ;
		        String query = "SELECT DISTINCT  " +
						"    s.task_executed_by,  " +
						"    t.task_title,  " +
						"    t.task_id,  " +
						"    l.task_execution_id, " +
						"    l.execution_note,  " +
						"    l.start_execution_time,  " +
                        "    l.creation_date,  " +
						"    l.num_of_copied_entities AS num_of_entities " +
						"FROM  " +
						TDMDB_SCHEMA +".tasks t " +
						"INNER JOIN  " +
						TDMDB_SCHEMA +".task_execution_summary s ON s.task_id = t.task_id " +
						"INNER JOIN  " +
						TDMDB_SCHEMA +".task_execution_list l ON s.task_execution_id = l.task_execution_id " +
						"INNER JOIN  " +
						TDMDB_SCHEMA +".tasks_logical_units u ON u.task_id = t.task_id " +
						"WHERE  " +
						"    t.task_type = 'TRAINING' " +
                        "    AND t.task_status = 'Active' " +
						"    AND u.lu_name = '" + lu_name + "'" +
                        "    AND l.task_execution_id = '" + task_exe_id + "'" +
						"    AND l.execution_status <> 'failed' " +
						"    AND l.process_id IN (-2, -1, 0) " +
						"    AND (SELECT COUNT(DISTINCT process_id)  " +
						"         FROM " + TDMDB_SCHEMA + ".task_execution_list  " +
						"         WHERE task_execution_id = l.task_execution_id  " +
						"           AND execution_status = 'completed'  " +
						"           AND process_id IN (-2, -1, 0)) = 3 " +
						"    AND l.process_id = -1 " +
						"    AND s.execution_status <> 'failed' " +
						"    AND t.be_id = ( " +
						"        SELECT DISTINCT  " +
						"            p.be_id  " +
						"        FROM  " +
						TDMDB_SCHEMA +".product_logical_units p  " +
						"        WHERE  " +
						"            p.lu_name = '" + lu_name + "'" +
						"            AND p.be_id IN ( " +
						"                SELECT  " +
						"                    b.be_id  " +
						"                FROM  " +
						TDMDB_SCHEMA +".business_entities b  " +
						"                WHERE  " +
						"                    b.be_name = '" + be_name + "'" +
						"            ))";
                query += " ORDER BY l.creation_date DESC ";
                Db.Rows taskDetailsRows = db("TDM").fetch(query);
		        List<String> columnNames = taskDetailsRows.getColumnNames();
		
		        for (Db.Row taskRow : taskDetailsRows) {
		            Map<String, Object> rowMap = new HashMap<>();
		            rowMap.put("task_execution_id", row.get("task_execution_id"));
		
		            ResultSet resultSet = taskRow.resultSet();
		            for (String columnName : columnNames) {
		                rowMap.put(columnName, resultSet.getObject(columnName));
		            }
		
		            rowsList.add(rowMap);
		        }
		    }
            if (rows != null) {
                rows.close();
            }
		    response.put("result", rowsList);
		    errorCode = "SUCCESS";
		} catch (Exception e) {
		    message = e.getMessage();
		    e.printStackTrace();
		    log.error("Can't get the list of training models: " +message);
		    errorCode = "FAILED";
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Get the list of available training tasks for AI generate")
	@webService(path = "tasks/getGenerationModels", verb = {MethodType.GET, MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	public static Object wsGetGenerationModels(String fromDate, String toDate, String envName, Long beID, List<String> selectedLogicalUnits) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";

        if (selectedLogicalUnits == null || selectedLogicalUnits.isEmpty()) {
            response.put("result", new ArrayList<>());
            response.put("errorCode", "SUCCESS");
            response.put("message", null);
            return response;
        }

        try {
            List<String> matchedLuNames = new ArrayList<>();
            List<String> matchedLuNamesLower = new ArrayList<>();
            String luQuery = "SELECT lu_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE be_id= " + beID;
            try (Db.Rows lus = db(TDM).fetch(luQuery)) {
                for (Db.Row lu : lus) {
                    String luName = (String) lu.get("lu_name");
                    if (selectedLogicalUnits.contains(luName)) {
                        matchedLuNames.add(luName);
                        matchedLuNamesLower.add(luName.toLowerCase());
                    }
                }
            }

            if (matchedLuNames.isEmpty()) {
                response.put("result", new ArrayList<>());
                response.put("errorCode", "SUCCESS");
                response.put("message", null);
                return response;
            }

            String logicalUnitList = "'" + String.join("','", matchedLuNames) + "'";
            String luSubsetCondition = " AND ARRAY['" + String.join("','", matchedLuNamesLower) + "'] <@ lu_list";

            String generationDatesCond = "";
            if (fromDate != null && !"".equals(fromDate) && toDate != null && !"".equals(toDate)) {
                generationDatesCond = "AND l.creation_date::date >= '" + fromDate + "' AND l.creation_date::date <= '" + toDate + "' ";
            }

            String taskTypeCondition = " = 'AI_GENERATED'";
            String processIdCondition = " AND (SELECT COUNT(DISTINCT process_id) FROM " + TDMDB_SCHEMA + ".task_execution_list p1 WHERE p1.task_execution_id = l1.task_execution_id" +
                                        " AND execution_status = 'completed' AND process_id IN (-2, 0)) = 2 AND l1.process_id = -2";
            String processIdConditionSimple = " AND (SELECT COUNT(DISTINCT process_id) FROM " + TDMDB_SCHEMA + ".task_execution_list p WHERE p.task_execution_id = l.task_execution_id" +
                                              " AND execution_status = 'completed' AND process_id IN (-2, 0)) = 2 AND l.process_id = -2";
            String unionQuery = "";

            if (SYNTHETIC_ENVIRONMENT.equalsIgnoreCase(envName)) {
                taskTypeCondition = " = 'GENERATE' OR (t.task_Type = 'LOAD' AND t.selection_method = 'GENERATE') ";
                processIdCondition = "";
                processIdConditionSimple = "";
            } else {
                unionQuery = buildAiLoadUnionQuery(logicalUnitList, luSubsetCondition, generationDatesCond);
            }

            String sql = buildGenerationModelsSql(envName, logicalUnitList, luSubsetCondition, generationDatesCond,
                taskTypeCondition, processIdCondition, processIdConditionSimple, unionQuery);

            List<Map<String, Object>> rowsList = new ArrayList<>();
            try (Db.Rows rows = db(TDM).fetch(sql)) {
                List<String> columnNames = rows.getColumnNames();
                for (Db.Row row : rows) {
                    ResultSet resultSet = row.resultSet();
                    Map<String, Object> rowMap = new HashMap<>();
                    for (String columnName : columnNames) {
                        rowMap.put(columnName, resultSet.getObject(columnName));
                    }
                    rowsList.add(rowMap);
                }
            }

            response.put("result", rowsList);
            errorCode = "SUCCESS";
        } catch (Exception e) {
            message = e.getMessage();
            e.printStackTrace();
            log.error("Can't get the list of generation models: " + message);
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static String buildAiLoadUnionQuery(String logicalUnitList, String luSubsetCondition, String generationDatesCond) {
        return "UNION " +
            "SELECT DISTINCT " +
            "    t2.task_title, " +
            "    t2.task_id, " +
            "    tlu2.lu_name, " +
            "    l2.task_execution_id, " +
            "    l2.start_execution_time, " +
            "    t2.task_last_updated_by AS task_executed_by, " +
            "    l2.num_of_processed_entities AS number_of_entities, " +
            "    l2.execution_note, " +
            "    l2.creation_date, " +
            "    l2.fabric_execution_id, " +
            "    CASE WHEN plu2.lu_parent_id IS NULL THEN 'Y' ELSE 'N' END root_indicator, " +
            "    l2.num_of_copied_entities AS num_of_succeeded_entities, " +
            "    l2.num_of_failed_entities, " +
            "    plu2.lu_parent_name, " +
            "    ROW_NUMBER() OVER (PARTITION BY t2.task_title, l2.lu_id ORDER BY l2.task_execution_id) AS row_number " +
            "FROM " +
            TDMDB_SCHEMA + ".task_execution_list l2 " +
            "JOIN " + TDMDB_SCHEMA + ".tasks t2 ON t2.task_id = l2.task_id " +
            "JOIN " + TDMDB_SCHEMA + ".tasks_logical_units tlu2 ON l2.task_id = tlu2.task_id AND l2.lu_id = tlu2.lu_id " +
            "JOIN " + TDMDB_SCHEMA + ".product_logical_units plu2 ON l2.lu_id = plu2.lu_id " +
            "WHERE " +
            "    (t2.task_title, t2.task_id, l2.creation_date, l2.task_execution_id, l2.lu_id) IN " +
            "    ( " +
            "        SELECT DISTINCT t.task_title, t.task_id, l.creation_date, l.task_execution_id, l.lu_id " +
            "        FROM " + TDMDB_SCHEMA + ".tasks t " +
            "        JOIN " + TDMDB_SCHEMA + ".task_execution_list l ON t.task_id = l.task_id " +
            "        JOIN lu_list ON l2.task_execution_id = lu_list.task_execution_id " +
            "        WHERE t.task_Type = 'LOAD' " +
            "            AND t.selection_method = 'AI_GENERATED' " +
            "            AND t.task_id = l.task_id " +
            "            AND lower(l.execution_status) = 'completed' " + generationDatesCond +
            "            AND l.lu_id IN (" +
            "                SELECT lu_id FROM " + TDMDB_SCHEMA + ".tasks_logical_units u " +
            "                WHERE l.task_id = u.task_id AND u.lu_name IN (" + logicalUnitList + "))" +
            "            AND l.task_execution_id = lu_list.task_execution_id" + luSubsetCondition +
            "            AND t2.task_id = l2.task_id " +
            "            AND lower(l2.execution_status) = 'completed' " +
            "            AND l2.task_id = tlu2.task_id " +
            "            AND l2.lu_id = tlu2.lu_id " +
            "            AND l2.lu_id = plu2.lu_id " +
            "            AND (plu2.lu_parent_id IS NOT NULL OR l2.num_of_copied_entities > 0) " +
            "    ) " +
            "    AND (SELECT COUNT(DISTINCT process_id) " +
            "         FROM " + TDMDB_SCHEMA + ".task_execution_list p2 " +
            "         WHERE p2.task_execution_id = l2.task_execution_id " +
            "             AND execution_status = 'completed' AND process_id IN (-2, -1)) = 2 " +
            "    AND l2.process_id = -1 ";
    }

    private static String buildGenerationModelsSql(
            String envName,
            String logicalUnitList,
            String luSubsetCondition,
            String generationDatesCond,
            String taskTypeCondition,
            String processIdCondition,
            String processIdConditionSimple,
            String unionQuery) {

        String sql = "WITH lu_list AS (" +
            "    SELECT l.task_execution_id, array_agg(lower(lu.lu_name)) lu_list" +
            "    FROM " + TDMDB_SCHEMA + ".task_execution_list l" +
            "    JOIN " + TDMDB_SCHEMA + ".product_logical_units lu ON l.lu_id = lu.lu_id" +
            "    WHERE l.lu_id = lu.lu_id AND lower(l.execution_status) = 'completed'" +
            "    GROUP BY task_execution_id" +
            ") " +
            "(SELECT DISTINCT" +
            "    t1.task_title," +
            "    t1.task_id," +
            "    tlu.lu_name," +
            "    l1.task_execution_id," +
            "    l1.start_execution_time," +
            "    t1.task_last_updated_by AS task_executed_by," +
            "    l1.num_of_processed_entities AS number_of_entities," +
            "    l1.execution_note," +
            "    l1.creation_date," +
            "    l1.fabric_execution_id," +
            "    CASE WHEN plu.lu_parent_id IS NULL THEN 'Y' ELSE 'N' END root_indicator," +
            "    l1.num_of_copied_entities AS num_of_succeeded_entities," +
            "    l1.num_of_failed_entities," +
            "    plu.lu_parent_name," +
            "    ROW_NUMBER() OVER (PARTITION BY t1.task_title, l1.lu_id ORDER BY l1.task_execution_id) AS row_number" +
            " FROM " + TDMDB_SCHEMA + ".task_execution_list l1" +
            " JOIN " + TDMDB_SCHEMA + ".tasks t1 ON t1.task_id = l1.task_id" +
            " JOIN " + TDMDB_SCHEMA + ".tasks_logical_units tlu ON l1.task_id = tlu.task_id AND l1.lu_id = tlu.lu_id" +
            " JOIN " + TDMDB_SCHEMA + ".product_logical_units plu ON l1.lu_id = plu.lu_id" +
            " WHERE (t1.task_title, t1.task_id, l1.creation_date, l1.task_execution_id, l1.lu_id) IN (" +
            "    SELECT DISTINCT t.task_title, t.task_id, l.creation_date, l.task_execution_id, l.lu_id" +
            "    FROM " + TDMDB_SCHEMA + ".tasks t" +
            "    JOIN " + TDMDB_SCHEMA + ".task_execution_list l ON t.task_id = l.task_id" +
            "    JOIN lu_list ON l1.task_execution_id = lu_list.task_execution_id" +
            "    WHERE (t.task_Type" + taskTypeCondition + ")" +
            "        AND t.task_id = l.task_id" +
            "        AND t.source_env_name = '" + envName + "'" +
            "        AND lower(l.execution_status) = 'completed' " + generationDatesCond +
            "        AND l.lu_id IN (" +
            "            SELECT lu_id FROM " + TDMDB_SCHEMA + ".tasks_logical_units u" +
            "            WHERE l.task_id = u.task_id AND u.lu_name IN (" + logicalUnitList + "))" +
            "        AND l.task_execution_id = lu_list.task_execution_id" + luSubsetCondition +
            "        AND t1.task_id = l1.task_id" +
            "        AND lower(l1.execution_status) = 'completed'" +
            "        AND l1.task_id = tlu.task_id" +
            "        AND l1.lu_id = tlu.lu_id" +
            "        AND l1.lu_id = plu.lu_id" +
            "        AND (plu.lu_parent_id IS NOT NULL OR l1.num_of_copied_entities > 0))" +
            processIdCondition + " ORDER BY l1.creation_date ASC)";

        if (AI_ENVIRONMENT.equalsIgnoreCase(envName)) {
            sql += " " + unionQuery;
        }

        // Also include template tasks (no BE/LUs defined) that match the same task type and env
        sql += " UNION " +
            "SELECT DISTINCT" +
            "    t.task_title," +
            "    t.task_id," +
            "    plu.lu_name," +
            "    l.task_execution_id," +
            "    l.start_execution_time," +
            "    t.task_last_updated_by AS task_executed_by," +
            "    l.num_of_processed_entities AS number_of_entities," +
            "    l.execution_note," +
            "    l.creation_date," +
            "    l.fabric_execution_id," +
            "    CASE WHEN plu.lu_parent_id IS NULL THEN 'Y' ELSE 'N' END root_indicator," +
            "    l.num_of_copied_entities AS num_of_succeeded_entities," +
            "    l.num_of_failed_entities," +
            "    plu.lu_parent_name," +
            "    ROW_NUMBER() OVER (PARTITION BY t.task_title, l.lu_id ORDER BY l.task_execution_id) AS row_number" +
            " FROM " + TDMDB_SCHEMA + ".task_execution_list l" +
            " JOIN " + TDMDB_SCHEMA + ".tasks t ON t.task_id = l.task_id" +
            " LEFT JOIN " + TDMDB_SCHEMA + ".product_logical_units plu ON l.lu_id = plu.lu_id" +
            " WHERE (t.task_Type" + taskTypeCondition + ")" +
            "    AND t.source_env_name = '" + envName + "'" +
            "    AND lower(l.execution_status) = 'completed' " + generationDatesCond +
            "    AND NOT EXISTS (SELECT 1 FROM " + TDMDB_SCHEMA + ".tasks_logical_units tlu WHERE tlu.task_id = t.task_id)" +
            "    AND plu.lu_name IN (" + logicalUnitList + ") " +
            processIdConditionSimple;

        return sql;
    }
    
    @desc("Check If AI Task Interfaces Are Active")
    @webService(path = "tasks/checkAIInstallation", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
    public static Object wsCheckAIInstallation(@param(required = true) String taskType) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";
        String broadwayCommand = "";
        String warning = "Run a training task to create a training model before generating AI-based entities. Set the source env and the entity subset for the training process. Then select the AI Training option in the Target component.";
        try {
            if("AIGeneration".equalsIgnoreCase(taskType)){
                broadwayCommand = "broadway TDM.AIActiveSourceInterfacesCheck";
                message = (String) fabric().fetch(broadwayCommand).firstValue();
                if(warning.equalsIgnoreCase(message)){
                    errorCode = "WARNING";
                }else{
                    errorCode = "FAILED";
                }
            }else{
                broadwayCommand = "broadway TDM.AIActiveTargetInterfacesCheck";
                message = (String) fabric().fetch(broadwayCommand).firstValue();
                if(message != null){
                    errorCode = "WARNING";
                }
            }
            if(message == null|| "".equalsIgnoreCase(message)){
                errorCode = "SUCCESS";
            }
        } catch (Exception e) {
            message = e.getMessage();
            errorCode = "FAILED";
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    
    @desc("Returns task details by task id.")
    @webService(path = "task/{taskId}", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                                      {
              "result": {
                "task_id": 13,
                "task_title": "extract and load max num",
                "task_description": "extract and load max num",
                "task_override_fields": {
                  "type": "jsonb",
                  "value": "{\"business_entity\": {\"is_editable\": false, \"field_connector\": \"be_name\"}, \"selection_method\": {\"random\": {\"is_editable\": false}, \"entity_list\": {\"is_editable\": false}, \"is_editable\": false, \"custom_logic\": {\"is_editable\": false, \"can_add_params\": {\"is_editable\": false}}, \"max_entities\": {\"is_editable\": false}, \"field_connector\": \"selection_method\", \"business_parameters\": {\"is_editable\": false}}, \"reservation_period\": {\"is_editable\": false, \"field_connector\": \"reservation_period\"}, \"source_environment\": {\"is_editable\": true, \"field_connector\": \"source_env_name\"}, \"target_environment\": {\"is_editable\": true, \"field_connector\": \"environment_name\"}}"
                },
                "task_type": "Extract and load entities",
                "be_id": 1,
                "be_name": "Customer",
                "selection_method": "Entity list",
                "selection_param_value": "1,2,3,4",
                "parameters": null,
                "num_of_entities": 4,
                "reserve_retention_period_type": "Days",
                "reserve_retention_period_value": 5,
                "source_environment_id": 1,
                "source_env_name": "Production",
                "target_env_id": 2,
                "environment_name": "UAT",
                "base_prompt_text": "Copy data by <be_name> from <source_env_name> to <environment_name>.\nSubset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.",
                "prompt_text": "Copy data by <be_name> from <source_env_name> to <environment_name>.\nSubset by <selection_method> using: <selection_param_value>.",
                "prompt_text_with_params_replacment": "Copy data by Customer from Production to UAT.\nSubset by Entity list using: 1,2,3,4.",
                "notes": [],
                "logical_units_data": [
                  {
                    "lu_id": 2,
                    "lu_name": "Billing",
                    "source_affinity": "",
                    "target_affinity": "",
                    "max_no_of_workers": 5
                  },
                  {
                    "lu_id": 1,
                    "lu_name": "Customer",
                    "source_affinity": "",
                    "target_affinity": "",
                    "max_no_of_workers": 6
                  }
                ]
              },
              "errorCode": "SUCCESS",
              "message": null
            }
                                        """)
    public static Object wsGetTaskDetails(@param(required = true) Long taskId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";

        String taskSql = "SELECT " +
                "    t.task_title, " +
                "    t.task_description, " +
                "    t.task_override_fields, " +
                "    t.task_Type, " +
                "    t.be_id, " +
                "    t.source_environment_id, " +
                "    t.source_env_name, " +
                "    t.sync_mode, " +
                "    t.clone_ind, " +
                "    t.reserve_ind, " +
                "    t.reserve_retention_period_type, " +
                "    t.reserve_retention_period_value, " +
                "    t.selection_method, " +
                "    t.selection_param_value, " +
                "    t.parameters, " +
                "    t.num_of_entities, " +
                "    t.environment_id AS target_env_id, " +
                "    be.be_name AS be_name, " +
                "    te.environment_name AS target_env_name, " +
                "    t.version_ind, " +
                "    t.selected_version_task_exe_id, " +
                "    t.selected_subset_task_exe_id, " +
                "    t.retention_period_type, " +
                "    t.retention_period_value, " +
                "    t.filterout_reserved, " +
                "    t.in_place_masking_ind " +
                "FROM " + TDMDB_SCHEMA + ".tasks t " +
                "LEFT JOIN " + TDMDB_SCHEMA + ".business_entities be ON t.be_id = be.be_id " +
                "LEFT JOIN " + TDMDB_SCHEMA + ".environments te ON t.environment_id = te.environment_id " +
                "WHERE " +
                "    t.task_id = ? AND t.task_status = 'Active'";

        try {

            Db.Row row = db(TDM).fetch(taskSql, taskId).firstRow();

            if (row.isEmpty()) {
                errorCode = "FAILED";
                message = "Task not found or is inactive.";
                response.put("errorCode", errorCode);
                response.put("message", message);
                return response;
            }

            // Check if task_id has records in task_ref_tables
            String refTableCheckSql = "SELECT 1 FROM " + TDMDB_SCHEMA + ".task_ref_tables WHERE task_id = ?";
            Db.Row refTableRow = db(TDM).fetch(refTableCheckSql, taskId).firstRow();
            boolean hasRefTables = (refTableRow != null && !refTableRow.isEmpty());

            String taskTypeDerived = deriveTaskType(row, hasRefTables);

            Map<String, Object> taskDetails = new LinkedHashMap<>();
            taskDetails.put("task_id", taskId);
            taskDetails.put("task_title", row.get("task_title"));
            taskDetails.put("task_description", row.get("task_description"));
            taskDetails.put("task_override_fields", row.get("task_override_fields"));
            taskDetails.put("task_type", taskTypeDerived);
            taskDetails.put("db_task_type", row.get("task_Type"));
            taskDetails.put("be_id", row.get("be_id"));
            taskDetails.put("be_name", row.get("be_name"));
            taskDetails.put("sync_mode", row.get("sync_mode"));

            String selectionMethod = (String) row.get("selection_method");
            String displayedSelectionMethod = selectionMethod;
            if ("L".equalsIgnoreCase(selectionMethod)) {
                displayedSelectionMethod = "Entity list";
            }
            if ("ALL".equalsIgnoreCase(selectionMethod)) {
                displayedSelectionMethod = "Predefined entity list";
            }
            if ("C".equalsIgnoreCase(selectionMethod)) {
                displayedSelectionMethod = "Custom logic";
            }
            if ("P".equalsIgnoreCase(selectionMethod)) {
                displayedSelectionMethod = "Business parameters";
            }
            if ("PR".equalsIgnoreCase(selectionMethod)) {
                displayedSelectionMethod = "Business parameters with random";
            }
            if ("R".equalsIgnoreCase(selectionMethod)) {
                displayedSelectionMethod = "Random";
            }
            taskDetails.put("selection_method", displayedSelectionMethod);
            taskDetails.put("selection_param_value", row.get("selection_param_value"));
            taskDetails.put("parameters", row.get("parameters"));
            Long num_of_entities = (Long) row.get("num_of_entities");
            taskDetails.put("num_of_entities", row.get("num_of_entities"));
            taskDetails.put("reserve_retention_period_type", row.get("reserve_retention_period_type"));
            taskDetails.put("reserve_retention_period_value", row.get("reserve_retention_period_value"));
            taskDetails.put("filterout_reserved", row.get("filterout_reserved"));
            taskDetails.put("retention_period_type", row.get("retention_period_type"));
            taskDetails.put("retention_period_value", row.get("retention_period_value"));

            boolean isSourceDisabled = row.get("source_environment_id") != null && row.get("source_env_name") != null &&
                    sourceEnvSystemsDisabled(String.valueOf(row.get("source_environment_id")),
                            String.valueOf(row.get("source_env_name")), String.valueOf(row.get("task_Type")),
                            String.valueOf(row.get("sync_mode")), taskId, String.valueOf(row.get("be_id")));

            boolean isTargetDisabled = row.get("target_env_id") != null && row.get("target_env_name") != null &&
                    targetEnvSystemsDisabled(String.valueOf(row.get("target_env_id")),
                            String.valueOf(row.get("target_env_name")),
                            String.valueOf(row.get("task_Type")), String.valueOf(row.get("sync_mode")), taskId,
                            String.valueOf(row.get("be_id")));

            
            if (isSourceDisabled) {
                taskDetails.put("source_environment_id", null);
                taskDetails.put("source_env_name", null);
            } else {
                taskDetails.put("source_environment_id", row.get("source_environment_id"));
                taskDetails.put("source_env_name", row.get("source_env_name"));
            }

            if (isTargetDisabled) {
                taskDetails.put("target_env_id", null);
                taskDetails.put("environment_name", null);
            } else {
                taskDetails.put("target_env_id", row.get("target_env_id"));
                taskDetails.put("environment_name", row.get("target_env_name"));

            }

            taskDetails.put("selected_version_task_exe_id", row.get("selected_version_task_exe_id"));
            taskDetails.put("selected_subset_task_exe_id", row.get("selected_subset_task_exe_id"));

            if ("Load data version entities".equals(taskTypeDerived)) {
                Object versionExeIdObj = row.get("selected_version_task_exe_id");
                long selectedVersionExeId = (versionExeIdObj != null) ? Long.parseLong(versionExeIdObj.toString()) : 0L;
                if (selectedVersionExeId > 0) {
                    String versionInfoSql =
                        "SELECT t.task_title AS data_version_name, tes.creation_date AS version_creation_date " +
                        "FROM " + TDMDB_SCHEMA + ".task_execution_summary tes " +
                        "JOIN " + TDMDB_SCHEMA + ".tasks t ON tes.task_id = t.task_id " +
                        "WHERE tes.task_execution_id = ?";
                    Db.Row versionRow = db(TDM).fetch(versionInfoSql, selectedVersionExeId).firstRow();
                    if (versionRow != null && !versionRow.isEmpty()) {
                        taskDetails.put("data_version_name", versionRow.get("data_version_name"));
                        Object cd = versionRow.get("version_creation_date");
                        taskDetails.put("version_creation_date",
                            cd != null ? new java.text.SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(cd) : "");
                    } else {
                        taskDetails.put("data_version_name", "");
                        taskDetails.put("version_creation_date", "");
                    }
                } else {
                    taskDetails.put("data_version_name", "");
                    taskDetails.put("version_creation_date", "");
                }
            }

            if ("Load execution generation entities (AI/rule based)".equals(taskTypeDerived)) {
                Object subsetExeIdObj = row.get("selected_subset_task_exe_id");
                if (subsetExeIdObj != null) {
                    long selectedSubsetExeId = Long.parseLong(subsetExeIdObj.toString());
                    if (selectedSubsetExeId > 0) {
                        Long beId = Long.parseLong(row.get("be_id").toString());
                        String sourceEnvName = (String) row.get("source_env_name");
                        List<String> luNames = new ArrayList<>();
                        String luQuery = "SELECT lu_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE be_id = " + beId;
                        db(TDM).fetch(luQuery).forEach(luRow -> luNames.add((String) luRow.get("lu_name")));
                        Map<String, Object> genResponse = (Map<String, Object>) wsGetGenerationModels(null, null, sourceEnvName, beId, luNames);
                        if ("SUCCESS".equals(genResponse.get("errorCode"))) {
                            List<Map<String, Object>> genList = (List<Map<String, Object>>) genResponse.get("result");
                            for (Map<String, Object> genModel : genList) {
                                Object exeId = genModel.get("task_execution_id");
                                if (exeId != null && Long.parseLong(exeId.toString()) == selectedSubsetExeId) {
                                    taskDetails.put("generation_title", genModel.get("task_title"));
                                    taskDetails.put("generation_creation_date", genModel.get("creation_date"));
                                    taskDetails.put("generation_number_of_entities", genModel.get("number_of_entities"));
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            String prompt = basePromptText(taskTypeDerived);

            if (Boolean.TRUE.equals(row.get("in_place_masking_ind"))) {
                String inPlaceMaskingPrompt = basePromptText("In-place masking");
                if (!inPlaceMaskingPrompt.isEmpty()) {
                    prompt = inPlaceMaskingPrompt;
                }
            }

            boolean versionInd = Boolean.TRUE.equals(row.get("version_ind"));
            Object beIdValForSnapshot = row.get("be_id");
            boolean isTablesOnlyTask = beIdValForSnapshot != null && Long.parseLong(beIdValForSnapshot.toString()) == -1L;
            if (versionInd && taskTypeDerived.startsWith("Extract") && !isTablesOnlyTask) {
                String retentionType = (String) row.get("retention_period_type");
                if (retentionType == null || "Do Not Delete".equalsIgnoreCase(retentionType)) {
                    prompt += "\nSave the extracted data into a data snapshot, retained indefinitely.";
                } else {
                    prompt += "\nSave the extracted data into a data snapshot, retained for <retention_period_value> <retention_period_type>.";
                }
            }
            taskDetails.put("base_prompt_text", prompt);

            if (taskTypeDerived.startsWith("Rule-based Generate")
                    && shouldRemoveGenParamsSentence(taskId, row.get("task_override_fields"))) {
                prompt = prompt.replaceAll("\nThe entities are generated based on the following parameters:\\s*", "").trim();
            }

            String finalPrompt = createFinalPrompt(prompt, displayedSelectionMethod, num_of_entities);

            taskDetails.put("prompt_text", finalPrompt);
            String finalPromptWithParams = replacePromptParameters(finalPrompt, taskDetails);
            taskDetails.put("prompt_text_with_params_replacment", finalPromptWithParams);

            Map<String, Object> notesResponse = (Map<String, Object>) wsGetTaskNotes(taskId);
            taskDetails.put("notes", notesResponse.get("result"));

            errorCode = "SUCCESS";
            response.put("result", taskDetails);

        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }

        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Returns task details for a specific execution, with overridden parameters from task_execution_override_attrs applied.")
    @webService(path = "task/execution/{taskExecutionId}", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                {
              "result": {
                "task_id": 2,
                "task_title": "Extract and Load entities",
                "task_description": null,
                "task_override_fields": null,
                "task_type": "Extract and load entities",
                "be_id": 2,
                "be_name": "Subscriber",
                "source_environment_id": 1,
                "source_env_name": "Production",
                "target_env_id": 2,
                "environment_name": "UAT",
                "selection_method": "Business parameters",
                "selection_param_value": null,
                "parameters": null,
                "num_of_entities": 5,
                "reserve_retention_period_type": null,
                "reserve_retention_period_value": null,
                "base_prompt_text": null,
                "prompt_text": null,
                "prompt_text_with_params_replacment": null
              },
              "errorCode": "SUCCESS",
              "message": null
            }
                """)
    public static Object wsGetTaskDetailsByExecution(@param(required = true) long taskExecutionId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";

        String execSql = "SELECT " +
                "    es.task_id, " +
                "    ovr.override_parameters::text AS override_parameters_json " +
                "FROM " + TDMDB_SCHEMA + ".task_execution_summary es " +
                "LEFT JOIN " + TDMDB_SCHEMA + ".task_execution_override_attrs ovr " +
                "    ON es.task_execution_id = ovr.task_execution_id AND es.task_id = ovr.task_id " +
                "WHERE es.task_execution_id = ?";

        try {
            Db.Row row = db(TDM).fetch(execSql, taskExecutionId).firstRow();
            if (row == null || row.isEmpty()) {
                // Fall back to AI draft records: task_execution_override_attrs already has task_id,
                // so no join with task_execution_summary is needed for draft display.
                String draftSql = "SELECT task_id, override_parameters::text AS override_parameters_json " +
                        "FROM " + TDMDB_SCHEMA + ".task_execution_override_attrs " +
                        "WHERE task_execution_id = ? AND draft = true AND created_by_ai = true";
                row = db(TDM).fetch(draftSql, taskExecutionId).firstRow();
            if (row == null || row.isEmpty()) {
                errorCode = "FAILED";
                message = "Task execution not found.";
                response.put("errorCode", errorCode);
                response.put("message", message);
                return response;
                }
            }

            long taskId = Long.parseLong(row.get("task_id").toString());

            // Parse stored override_parameters JSON into Map<OverrideParamKey, Object>
            Map<OverrideParamKey, Object> overrideParameters = new HashMap<>();
            String overrideParamsJson = (String) row.get("override_parameters_json");
            if (overrideParamsJson != null && !overrideParamsJson.equals("null")) {
                Map<String, Object> storedParams = Json.get().fromJson(overrideParamsJson);
                for (Map.Entry<String, Object> entry : storedParams.entrySet()) {
                    try {
                        OverrideParamKey key = OverrideParamKey.valueOf(entry.getKey());
                        if (INTERNAL_ONLY_OVERRIDE_KEYS.contains(key))
                            continue;
                        Object value = entry.getValue();
                        if (value == null)
                            continue;
                        if (key == OverrideParamKey.GENERATE_DATA_PARAMS && value instanceof Map) {
                            // Preserve as-is: values can be complex objects (maps/lists), so
                            // toString() would produce Java format instead of JSON
                            overrideParameters.put(key, value);
                        } else if (value instanceof Map) {
                            // Nested objects (RESERVE_RETENTION_PARAMS, DATAFLUX_RETENTION_PARAMS) —
                            // convert to Map<String, String>
                            Map<String, String> stringMap = new HashMap<>();
                            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                                stringMap.put(e.getKey().toString(),
                                        e.getValue() != null ? e.getValue().toString() : null);
                            }
                            overrideParameters.put(key, stringMap);
                        } else if (key == OverrideParamKey.NO_OF_ENTITIES) {
                            overrideParameters.put(key, Long.parseLong(value.toString()));
                        } else if (key == OverrideParamKey.RESERVE_IND) {
                            overrideParameters.put(key, Boolean.parseBoolean(value.toString()));
                        } else if (value instanceof List) {
                            // PRE/POST_EXECUTION_PROCESSES_PARAMS, LOGICAL_UNITS, TABLE_FILTERS —
                            // already a proper List after JSON parsing; preserve as-is
                            overrideParameters.put(key, value);
                        } else {
                            overrideParameters.put(key, value.toString());
                        }
                    } catch (IllegalArgumentException ignored) {
                        // unknown key — skip
                    }
                }
            }

            return wsGetTaskDetailsWithOverrides(taskId, overrideParameters);

        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }

        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Returns task details by task id with optional runtime override parameters applied (same keys as StartTask).")
    @webService(path = "task/{taskId}/previewTask", verb = {
            MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                            {
                "result": {
                    "task_id": 1,
                    "task_title": "Extract entities",
                    "task_description": "Extract entities",
                    "task_override_fields": {
                        "type": "jsonb",
                        "value": "{\"business_entity\": {\"is_editable\": true, \"field_connector\": \"be_name\"}, \"selection_method\": {\"random\": {\"is_editable\": true}, \"entity_list\": {\"is_editable\": true}, \"is_editable\": true, \"custom_logic\": {\"is_editable\": true, \"can_add_params\": {\"is_editable\": true}}, \"max_entities\": {\"is_editable\": true}, \"field_connector\": \"selection_method\", \"business_parameters\": {\"is_editable\": true}}, \"reservation_period\": {\"is_editable\": false, \"field_connector\": \"reservation_period\"}, \"source_environment\": {\"is_editable\": true, \"field_connector\": \"source_env_name\"}, \"target_environment\": {\"is_editable\": false, \"field_connector\": \"environment_name\"}}"
                    },
                    "task_type": "Extract entities",
                    "be_id": 1,
                    "be_name": "Customer",
                    "selection_method": "Entity list",
                    "selection_param_value": "1,10,2,3,4,5,6,7,8,9",
                    "parameters": null,
                    "num_of_entities": 0,
                    "reserve_retention_period_type": "Days",
                    "reserve_retention_period_value": 5,
                    "source_environment_id": 1,
                    "source_env_name": "Production",
                    "target_env_id": null,
                    "environment_name": null,
                    "base_prompt_text": "Extract data by <be_name> from <source_env_name>. \nSubset by <selection_method> using: <selection_param_value>. Process up to <num_of_entities> matching entities.",
                    "prompt_text": "Extract data by <be_name> from <source_env_name>. \nSubset by <selection_method> using: <selection_param_value>.",
                    "prompt_text_with_params_replacment": "Extract data by Customer from Production. \nSubset by Entity list using: 1,10,2,3,4,5,6,7,8,9.",
                    "notes": [],
                    "override_params": {
                        "ENTITY_LIST": "1,10,2,3,4,5,6,7,8,9",
                        "BE_ID": "1",
                        "SOURCE_ENVIRONMENT_NAME": "Production",
                        "SELECTION_METHOD": "L"
                    }
                },
                "errorCode": "SUCCESS",
                "message": null
            }
                            """)
    public static Object wsGetTaskDetailsWithOverrides(@param(required = true) long taskId,
            Map<OverrideParamKey, Object> overrideParameters) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";

        String taskSql = "SELECT " +
                "    t.task_title, " +
                "    t.task_description, " +
                "    t.task_override_fields, " +
                "    t.task_Type, " +
                "    t.be_id, " +
                "    t.source_environment_id, " +
                "    t.source_env_name, " +
                "    t.sync_mode, " +
                "    t.clone_ind, " +
                "    t.reserve_ind, " +
                "    t.reserve_retention_period_type, " +
                "    t.reserve_retention_period_value, " +
                "    t.selection_method, " +
                "    t.selection_param_value, " +
                "    t.parameters, " +
                "    t.num_of_entities, " +
                "    t.environment_id AS target_env_id, " +
                "    be.be_name AS be_name, " +
                "    te.environment_name AS target_env_name, " +
                "    t.version_ind, " +
                "    t.selected_version_task_exe_id, " +
                "    t.selected_subset_task_exe_id, " +
                "    t.retention_period_type, " +
                "    t.retention_period_value, " +
                "    t.filterout_reserved, " +
                "    t.in_place_masking_ind " +
                "FROM " + TDMDB_SCHEMA + ".tasks t " +
                "LEFT JOIN " + TDMDB_SCHEMA + ".business_entities be ON t.be_id = be.be_id " +
                "LEFT JOIN " + TDMDB_SCHEMA + ".environments te ON t.environment_id = te.environment_id " +
                "WHERE " +
                "    t.task_id = ?";

        try {
            if (overrideParameters == null) {
                overrideParameters = new HashMap<>();
            }

            Db.Row row = db(TDM).fetch(taskSql, taskId).firstRow();

            if (row.isEmpty()) {
                errorCode = "FAILED";
                message = "Task not found.";
                response.put("errorCode", errorCode);
                response.put("message", message);
                return response;
            }

            // Build effective values map: start with DB row values, then apply caller
            // overrides on top
            Map<String, Object> effective = new LinkedHashMap<>(row);

            if (overrideParameters.containsKey(OverrideParamKey.SOURCE_ENVIRONMENT_NAME)) {
                effective.put("source_env_name", overrideParameters.get(OverrideParamKey.SOURCE_ENVIRONMENT_NAME));
            }
            if (overrideParameters.containsKey(OverrideParamKey.TARGET_ENVIRONMENT_NAME)) {
                effective.put("target_env_name", overrideParameters.get(OverrideParamKey.TARGET_ENVIRONMENT_NAME));
            }
            if (overrideParameters.containsKey(OverrideParamKey.SELECTION_METHOD)) {
                effective.put("selection_method", overrideParameters.get(OverrideParamKey.SELECTION_METHOD));
            }
            if (overrideParameters.containsKey(OverrideParamKey.NO_OF_ENTITIES)) {
                Object numEntities = overrideParameters.get(OverrideParamKey.NO_OF_ENTITIES);
                effective.put("num_of_entities", numEntities != null ? Long.parseLong(numEntities.toString()) : null);
            }
            if (overrideParameters.containsKey(OverrideParamKey.PARAMETERS)) {
                effective.put("parameters", overrideParameters.get(OverrideParamKey.PARAMETERS));
            }
            if (overrideParameters.containsKey(OverrideParamKey.BP_QUERY)) {
                effective.put("selection_param_value", overrideParameters.get(OverrideParamKey.BP_QUERY));
            }
            if (overrideParameters.containsKey(OverrideParamKey.RESERVE_IND)) {
                effective.put("reserve_ind", overrideParameters.get(OverrideParamKey.RESERVE_IND));
            }
            if (overrideParameters.containsKey(OverrideParamKey.BE_ID)) {
                String overrideBeIdStr = (String) overrideParameters.get(OverrideParamKey.BE_ID);
                if (overrideBeIdStr != null && !"null".equals(overrideBeIdStr)) {
                    long overrideBeId = Long.parseLong(overrideBeIdStr);
                    effective.put("be_id", overrideBeId);
                    String beNameSql = "SELECT be_name FROM " + TDMDB_SCHEMA + ".business_entities WHERE be_id = ?";
                    Db.Row beRow = db(TDM).fetch(beNameSql, overrideBeId).firstRow();
                    effective.put("be_name", (beRow != null && !beRow.isEmpty()) ? beRow.get("be_name") : null);
                }
            }
            if (overrideParameters.containsKey(OverrideParamKey.RESERVE_RETENTION_PARAMS)) {
                Map<String, String> retentionParams = (Map<String, String>) overrideParameters
                        .get(OverrideParamKey.RESERVE_RETENTION_PARAMS);
                if (retentionParams != null) {
                    effective.put("reserve_retention_period_type", retentionParams.get("units"));
                    String retVal = retentionParams.get("value");
                    effective.put("reserve_retention_period_value", retVal != null ? Long.parseLong(retVal) : null);
                }
            }
            if (overrideParameters.containsKey(OverrideParamKey.SELECTED_VERSION_TASK_EXE_ID)) {
                effective.put("selected_version_task_exe_id",
                        overrideParameters.get(OverrideParamKey.SELECTED_VERSION_TASK_EXE_ID));
            }

            // selection_param_value: resolve based on effective selection method
            String effectiveSelectionMethod = (String) effective.get("selection_method");
            if ("L".equalsIgnoreCase(effectiveSelectionMethod)
                    && overrideParameters.containsKey(OverrideParamKey.ENTITY_LIST)) {
                effective.put("selection_param_value", overrideParameters.get(OverrideParamKey.ENTITY_LIST));
            } else if ("C".equalsIgnoreCase(effectiveSelectionMethod)
                    && overrideParameters.containsKey(OverrideParamKey.CUSTOM_LOGIC_FLOW)) {
                effective.put("selection_param_value", overrideParameters.get(OverrideParamKey.CUSTOM_LOGIC_FLOW));
            }

            // Check if task has ref tables (used for task type derivation)
            String refTableCheckSql = "SELECT 1 FROM " + TDMDB_SCHEMA + ".task_ref_tables WHERE task_id = ?";
            Db.Row refTableRow = db(TDM).fetch(refTableCheckSql, taskId).firstRow();
            boolean hasRefTables = (refTableRow != null && !refTableRow.isEmpty());

            String taskTypeDerived = deriveTaskType(effective, hasRefTables);

            Map<String, Object> taskDetails = new LinkedHashMap<>();
            taskDetails.put("task_id", taskId);
            taskDetails.put("task_title", effective.get("task_title"));
            taskDetails.put("task_description", effective.get("task_description"));
            taskDetails.put("task_override_fields", effective.get("task_override_fields"));
            taskDetails.put("task_type", taskTypeDerived);
            taskDetails.put("db_task_type", effective.get("task_type"));
            taskDetails.put("be_id", effective.get("be_id"));
            taskDetails.put("be_name", effective.get("be_name"));
            taskDetails.put("sync_mode", row.get("sync_mode"));

            String selectionMethod = (String) effective.get("selection_method");
            String displayedSelectionMethod = selectionMethod;
            if ("L".equalsIgnoreCase(selectionMethod))
                displayedSelectionMethod = "Entity list";
            if ("ALL".equalsIgnoreCase(selectionMethod))
                displayedSelectionMethod = "Predefined entity list";
            if ("C".equalsIgnoreCase(selectionMethod))
                displayedSelectionMethod = "Custom logic";
            if ("P".equalsIgnoreCase(selectionMethod))
                displayedSelectionMethod = "Business parameters";
            if ("PR".equalsIgnoreCase(selectionMethod))
                displayedSelectionMethod = "Business parameters with random";
            if ("R".equalsIgnoreCase(selectionMethod))
                displayedSelectionMethod = "Random";

            taskDetails.put("selection_method", displayedSelectionMethod);
            taskDetails.put("selection_param_value", effective.get("selection_param_value"));
            taskDetails.put("parameters", effective.get("parameters"));
            Long numOfEntities = effective.get("num_of_entities") != null
                    ? Long.parseLong(effective.get("num_of_entities").toString())
                    : null;
            taskDetails.put("num_of_entities", numOfEntities);
            taskDetails.put("reserve_retention_period_type", effective.get("reserve_retention_period_type"));
            taskDetails.put("reserve_retention_period_value", effective.get("reserve_retention_period_value"));
            taskDetails.put("filterout_reserved", effective.get("filterout_reserved"));
            taskDetails.put("retention_period_type", effective.get("retention_period_type"));
            taskDetails.put("retention_period_value", effective.get("retention_period_value"));
            
            boolean isSourceDisabled = effective.get("source_environment_id") != null && effective.get("source_env_name") != null &&
                                        sourceEnvSystemsDisabled(String.valueOf(effective.get("source_environment_id")),
	                                    String.valueOf(effective.get("source_env_name")),String.valueOf(effective.get("task_Type")),
	                                    String.valueOf(effective.get("sync_mode")),taskId,String.valueOf(effective.get("be_id")));
           
            boolean isTargetDisabled = effective.get("target_env_id") != null && effective.get("target_env_name") != null &&
                                       targetEnvSystemsDisabled(String.valueOf(effective.get("target_env_id")),String.valueOf(effective.get("target_env_name")),
	                                   String.valueOf(effective.get("task_Type")),String.valueOf(effective.get("sync_mode")),taskId,
	                                   String.valueOf(effective.get("be_id")));    

            if (isSourceDisabled) {
                taskDetails.put("source_environment_id", null);
                taskDetails.put("source_env_name", null);
            } else {
                String sourceEnvName = (String) effective.get("source_env_name");
                taskDetails.put("source_environment_id", getEnvironmentIdByName(sourceEnvName));
                taskDetails.put("source_env_name", sourceEnvName);
            }

            if (isTargetDisabled) {
                taskDetails.put("target_env_id", null);
                taskDetails.put("environment_name", null);
            } else {
                String targetEnvName = (String) effective.get("target_env_name");
                taskDetails.put("target_env_id", getEnvironmentIdByName(targetEnvName));
                taskDetails.put("environment_name", targetEnvName);
            }
            

            taskDetails.put("selected_version_task_exe_id", effective.get("selected_version_task_exe_id"));
            taskDetails.put("selected_subset_task_exe_id", effective.get("selected_subset_task_exe_id"));

            if ("Load data version entities".equals(taskTypeDerived)) {
                Object versionExeIdObj = effective.get("selected_version_task_exe_id");
                long selectedVersionExeId = (versionExeIdObj != null) ? Long.parseLong(versionExeIdObj.toString()) : 0L;
                if (selectedVersionExeId > 0) {
                    String versionInfoSql =
                        "SELECT t.task_title AS data_version_name, tes.creation_date AS version_creation_date " +
                        "FROM " + TDMDB_SCHEMA + ".task_execution_summary tes " +
                        "JOIN " + TDMDB_SCHEMA + ".tasks t ON tes.task_id = t.task_id " +
                        "WHERE tes.task_execution_id = ?";
                    Db.Row versionRow = db(TDM).fetch(versionInfoSql, selectedVersionExeId).firstRow();
                    if (versionRow != null && !versionRow.isEmpty()) {
                        taskDetails.put("data_version_name", versionRow.get("data_version_name"));
                        Object cd = versionRow.get("version_creation_date");
                        taskDetails.put("version_creation_date",
                            cd != null ? new java.text.SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(cd) : "");
                    } else {
                        taskDetails.put("data_version_name", "");
                        taskDetails.put("version_creation_date", "");
                    }
                } else {
                    taskDetails.put("data_version_name", "");
                    taskDetails.put("version_creation_date", "");
                }
            }

            if ("Load execution generation entities (AI/rule based)".equals(taskTypeDerived)) {
                Object subsetExeIdObj = effective.get("selected_subset_task_exe_id");
                if (subsetExeIdObj != null) {
                    long selectedSubsetExeId = Long.parseLong(subsetExeIdObj.toString());
                    if (selectedSubsetExeId > 0) {
                        Long beId = Long.parseLong(effective.get("be_id").toString());
                        String sourceEnvName = (String) effective.get("source_env_name");
                        List<String> luNames = new ArrayList<>();
                        String luQuery = "SELECT lu_name FROM " + TDMDB_SCHEMA + ".product_logical_units WHERE be_id = " + beId;
                        db(TDM).fetch(luQuery).forEach(luRow -> luNames.add((String) luRow.get("lu_name")));
                        Map<String, Object> genResponse = (Map<String, Object>) wsGetGenerationModels(null, null, sourceEnvName, beId, luNames);
                        if ("SUCCESS".equals(genResponse.get("errorCode"))) {
                            List<Map<String, Object>> genList = (List<Map<String, Object>>) genResponse.get("result");
                            for (Map<String, Object> genModel : genList) {
                                Object exeId = genModel.get("task_execution_id");
                                if (exeId != null && Long.parseLong(exeId.toString()) == selectedSubsetExeId) {
                                    taskDetails.put("generation_title", genModel.get("task_title"));
                                    taskDetails.put("generation_creation_date", genModel.get("creation_date"));
                                    taskDetails.put("generation_number_of_entities", genModel.get("number_of_entities"));
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            String prompt = basePromptText(taskTypeDerived);

            if (Boolean.TRUE.equals(effective.get("in_place_masking_ind"))) {
                String inPlaceMaskingPrompt = basePromptText("In-place masking");
                if (!inPlaceMaskingPrompt.isEmpty()) {
                    prompt = inPlaceMaskingPrompt;
                }
            }

            boolean versionInd = Boolean.TRUE.equals(effective.get("version_ind"));
            Object beIdValForSnapshot = effective.get("be_id");
            boolean isTablesOnlyTask = beIdValForSnapshot != null && Long.parseLong(beIdValForSnapshot.toString()) == -1L;
            if (versionInd && taskTypeDerived.startsWith("Extract") && !isTablesOnlyTask) {
                String retentionType = (String) effective.get("retention_period_type");
                if (retentionType == null || "Do Not Delete".equalsIgnoreCase(retentionType)) {
                    prompt += "\nSave the extracted data into a data snapshot, retained indefinitely.";
                } else {
                    prompt += "\nSave the extracted data into a data snapshot, retained for <retention_period_value> <retention_period_type>.";
                }
            }
            taskDetails.put("base_prompt_text", prompt);

            if (taskTypeDerived.startsWith("Rule-based Generate")
                    && shouldRemoveGenParamsSentence(taskId, effective.get("task_override_fields"))) {
                prompt = prompt.replaceAll("\nThe entities are generated based on the following parameters:\\s*", "").trim();
            }

            String finalPrompt = createFinalPrompt(prompt, displayedSelectionMethod, numOfEntities);

            taskDetails.put("prompt_text", finalPrompt);
            String finalPromptWithParams = replacePromptParameters(finalPrompt, taskDetails);
            taskDetails.put("prompt_text_with_params_replacment", finalPromptWithParams);

            Map<String, Object> notesResponse = (Map<String, Object>) wsGetTaskNotes(taskId);
            taskDetails.put("notes", notesResponse.get("result"));
            taskDetails.put("override_params", overrideParameters);

            errorCode = "SUCCESS";
            response.put("result", taskDetails);

        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }

        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Returns per-LU affinity and max workers for the given environments. " +
            "When a side's env ID matches what is saved on the task, saved tasks_logical_units " +
            "overrides are preferred (COALESCE). When the env changed for a side, that side returns " +
            "pure environment defaults so the UI shows fresh values for the new env.")
    @webService(path = "task/{taskId}/lu-affinity-workers", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = {
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                            {
              "result": {
                "logical_units_data": [
                  {
                    "lu_id": 2,
                    "lu_name": "Billing",
                    "source_affinity": "",
                    "target_affinity": "",
                    "max_no_of_workers": null
                  },
                  {
                    "lu_id": 1,
                    "lu_name": "Customer",
                    "source_affinity": "",
                    "target_affinity": "",
                    "max_no_of_workers": 2
                  }
                ]
              },
              "errorCode": "SUCCESS",
              "message": null
            }
                            """)
    public static Object wsGetLuAffinityAndWorkersByEnv(
            @param(required = true) Long taskId,
            @param(required = false) Long srcEnvId,
            @param(required = false) Long tgtEnvId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "";

        try {
            // Compare caller's env IDs against the task's saved envs per side.
            // A side whose env is unchanged should use saved tlu overrides (COALESCE);
            // a side whose env changed should use pure env defaults.
            Long taskSrcEnvId = null;
            Long taskTgtEnvId = null;
            Long taskBeId = null;
            String taskType = null;
            Db.Row taskRow = db(TDM).fetch(
                    "SELECT source_environment_id, environment_id, task_type, be_id FROM " + TDMDB_SCHEMA
                            + ".tasks WHERE task_id = ?",
                    taskId).firstRow();
            if (taskRow != null) {
                Object raw = taskRow.get("source_environment_id");
                taskSrcEnvId = raw != null ? ((Number) raw).longValue() : null;
                raw = taskRow.get("environment_id");
                taskTgtEnvId = raw != null ? ((Number) raw).longValue() : null;
                raw = taskRow.get("be_id");
                taskBeId = raw != null ? ((Number) raw).longValue() : null;
                taskType = (String) taskRow.get("task_type");
            }
            boolean isSourceTask = "EXTRACT".equalsIgnoreCase(taskType)
                    || "GENERATE".equalsIgnoreCase(taskType)
                    || "TRAINING".equalsIgnoreCase(taskType)
                    || "AI_GENERATED".equalsIgnoreCase(taskType);
            boolean srcUnchanged = Objects.equals(srcEnvId, taskSrcEnvId);
            boolean tgtUnchanged = Objects.equals(tgtEnvId, taskTgtEnvId);

            // Only INNER JOIN a side when its env ID is explicitly provided and differs from the
            // saved task env; a null env ID means "not specified" so we use LEFT JOIN.
            boolean srcEnvChanged = srcEnvId != null && !srcUnchanged;
            boolean tgtEnvChanged = tgtEnvId != null && !tgtUnchanged;
            boolean anyEnvChanged = srcEnvChanged || tgtEnvChanged;
            String srcJoinType = srcEnvChanged ? "INNER" : "LEFT";
            String tgtJoinType = tgtEnvChanged ? "INNER" : "LEFT";

            List<Map<String, Object>> luData = new ArrayList<>();
            
            Map<String, Object> maxWorkersWsResult = (Map<String, Object>) wsGetMaxWorkersPerNode();
            Object effectiveMaxWorkers = maxWorkersWsResult.get("result");

            if (anyEnvChanged && taskBeId != null) {
                // Env changed: start from the BE's full LU set so that LUs new to the env are
                // included; LEFT JOIN tasks_logical_units to get any saved task overrides.
                String luSql = "SELECT plu.lu_id, plu.lu_name, " +
                        "tlu.source_affinity          AS tlu_src_affinity, " +
                        "tlu.target_affinity          AS tlu_tgt_affinity, " +
                        "tlu.source_max_no_of_workers AS tlu_src_workers, " +
                        "tlu.target_max_no_of_workers AS tlu_tgt_workers, " +
                        "ep_src.data_center_name      AS ep_src_affinity, " +
                        "ep_tgt.data_center_name      AS ep_tgt_affinity, " +
                        "ep_src.max_number_of_workers AS ep_src_workers, " +
                        "ep_tgt.max_number_of_workers AS ep_tgt_workers, " +
                        "tlu.override_fields " +
                        "FROM " + TDMDB_SCHEMA + ".product_logical_units plu " +
                        srcJoinType + " JOIN " + TDMDB_SCHEMA + ".environment_products ep_src " +
                        "    ON ep_src.product_id = plu.product_id AND ep_src.environment_id = ? AND ep_src.status = 'Active' " +
                        tgtJoinType + " JOIN " + TDMDB_SCHEMA + ".environment_products ep_tgt " +
                        "    ON ep_tgt.product_id = plu.product_id AND ep_tgt.environment_id = ? AND ep_tgt.status = 'Active' " +
                        "LEFT JOIN " + TDMDB_SCHEMA + ".tasks_logical_units tlu " +
                        "    ON tlu.lu_id = plu.lu_id AND tlu.task_id = ? " +
                        "WHERE plu.be_id = ? ORDER BY plu.lu_name";
                for (Db.Row luRow : db(TDM).fetch(luSql, srcEnvId, tgtEnvId, taskId, taskBeId)) {
                    Object srcAffinity = srcUnchanged
                            ? coalesce(luRow.get("tlu_src_affinity"), luRow.get("ep_src_affinity"))
                            : luRow.get("ep_src_affinity");
                    Object tgtAffinity = tgtUnchanged
                            ? coalesce(luRow.get("tlu_tgt_affinity"), luRow.get("ep_tgt_affinity"))
                            : luRow.get("ep_tgt_affinity");
                    Object srcWorkers = srcUnchanged
                            ? coalesce(luRow.get("tlu_src_workers"), luRow.get("ep_src_workers"))
                            : luRow.get("ep_src_workers");
                    Object tgtWorkers = tgtUnchanged
                            ? coalesce(luRow.get("tlu_tgt_workers"), luRow.get("ep_tgt_workers"))
                            : luRow.get("ep_tgt_workers");
                    // LUs not in tasks_logical_units (new to this env) get all-false locked flags.
                    Object overrideFields = luRow.get("override_fields");
                    if (overrideFields == null) {
                        overrideFields = "{\"source_affinity\": false, \"target_affinity\": false, \"max_no_of_workers\": false}";
                    }
                    Object maxWorkers = isSourceTask ? srcWorkers : tgtWorkers;
                    if (maxWorkers == null) maxWorkers = effectiveMaxWorkers;
                    Map<String, Object> luEntry = new LinkedHashMap<>();
                    luEntry.put("lu_id", luRow.get("lu_id"));
                    luEntry.put("lu_name", luRow.get("lu_name"));
                    luEntry.put("source_affinity", srcAffinity);
                    luEntry.put("target_affinity", isSourceTask ? null : tgtAffinity);
                    luEntry.put("max_no_of_workers", maxWorkers);
                    luEntry.put("override_fields", overrideFields);
                    luData.add(luEntry);
                }
            } else {
                // Env unchanged: start from tasks_logical_units (task-scoped LU list).
                String luSql = "SELECT tlu.lu_id, plu.lu_name, " +
                        "tlu.source_affinity          AS tlu_src_affinity, " +
                        "tlu.target_affinity          AS tlu_tgt_affinity, " +
                        "tlu.source_max_no_of_workers AS tlu_src_workers, " +
                        "tlu.target_max_no_of_workers AS tlu_tgt_workers, " +
                        "ep_src.data_center_name      AS ep_src_affinity, " +
                        "ep_tgt.data_center_name      AS ep_tgt_affinity, " +
                        "ep_src.max_number_of_workers AS ep_src_workers, " +
                        "ep_tgt.max_number_of_workers AS ep_tgt_workers, " +
                        "tlu.override_fields " +
                        "FROM " + TDMDB_SCHEMA + ".tasks_logical_units tlu " +
                        "JOIN " + TDMDB_SCHEMA + ".product_logical_units plu ON plu.lu_id = tlu.lu_id " +
                        srcJoinType + " JOIN " + TDMDB_SCHEMA + ".environment_products ep_src " +
                        "    ON ep_src.product_id = plu.product_id AND ep_src.environment_id = ? AND ep_src.status = 'Active' " +
                        tgtJoinType + " JOIN " + TDMDB_SCHEMA + ".environment_products ep_tgt " +
                        "    ON ep_tgt.product_id = plu.product_id AND ep_tgt.environment_id = ? AND ep_tgt.status = 'Active' " +
                        "WHERE tlu.task_id = ? ORDER BY plu.lu_name";
                for (Db.Row luRow : db(TDM).fetch(luSql, srcEnvId, tgtEnvId, taskId)) {
                    Object srcAffinity = srcUnchanged
                            ? coalesce(luRow.get("tlu_src_affinity"), luRow.get("ep_src_affinity"))
                            : luRow.get("ep_src_affinity");
                    Object tgtAffinity = tgtUnchanged
                            ? coalesce(luRow.get("tlu_tgt_affinity"), luRow.get("ep_tgt_affinity"))
                            : luRow.get("ep_tgt_affinity");
                    Object srcWorkers = srcUnchanged
                            ? coalesce(luRow.get("tlu_src_workers"), luRow.get("ep_src_workers"))
                            : luRow.get("ep_src_workers");
                    Object tgtWorkers = tgtUnchanged
                            ? coalesce(luRow.get("tlu_tgt_workers"), luRow.get("ep_tgt_workers"))
                            : luRow.get("ep_tgt_workers");
                    Object maxWorkers = isSourceTask ? srcWorkers : tgtWorkers;
                    if (maxWorkers == null) maxWorkers = effectiveMaxWorkers;
                    Map<String, Object> luEntry = new LinkedHashMap<>();
                    luEntry.put("lu_id", luRow.get("lu_id"));
                    luEntry.put("lu_name", luRow.get("lu_name"));
                    luEntry.put("source_affinity", srcAffinity);
                    luEntry.put("target_affinity", isSourceTask ? null : tgtAffinity);
                    luEntry.put("max_no_of_workers", maxWorkers);
                    luEntry.put("override_fields", luRow.get("override_fields"));
                    luData.add(luEntry);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("logical_units_data", luData);
            errorCode = "SUCCESS";
            response.put("result", result);

        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }

        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static Object coalesce(Object... values) {
        for (Object v : values) {
            if (v != null) return v;
        }
        return null;
    }

    private static String createFinalPrompt(String prompt, String displayedSelectionMethod, Long numOfEntities) {
        if (prompt == null) {
            return prompt;
        }
        String replacedPrompt = prompt;

        boolean isEntityListOrCustomLogic = "Entity list".equalsIgnoreCase(displayedSelectionMethod)
                || "Custom logic".equalsIgnoreCase(displayedSelectionMethod);

        boolean isEntityListOrPredefined = "Entity list".equalsIgnoreCase(displayedSelectionMethod)
                || "Predefined entity list".equalsIgnoreCase(displayedSelectionMethod);

        if (!isEntityListOrCustomLogic) {
            // Remove the "using: <Selection method parameter’s>" part
            replacedPrompt = replacedPrompt.replaceAll("using:\\s*<selection_param_value>\\s*", "").trim();
        }
        // Handle "Entity list/Predefined entity list" selection method to remove the
        // Process up to from the prompt
        if (isEntityListOrPredefined) {
            // More precise removal using regex, specifically looking for the angle brackets
            replacedPrompt = replacedPrompt.replaceAll(
                    "(?i)Process\\s+up\\s+to\\s+<num_of_entities>\\s+matching\\s+entities\\s*\\.?\\s*", "").trim();
        }

        if (numOfEntities != null && numOfEntities == -1) {
            replacedPrompt = replacedPrompt.replaceAll("(?i)Process\\s+up\\s+to\\s+<num_of_entities>",
                    "Process <num_of_entities>");
        }

        return replacedPrompt.trim();
    }

    @desc("Returns the prompt text based on the selection method, selection method options: Entity list,Custom logic,Random")
    @webService(path = "getFinalPrompt", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
            {
              "result": "Copy <source_env_name> data from the Test Data Store by <be_name> to <environment_name>.\\nSubset by <selection_method> . Limit number of entities to <num_of_entities>",
              "errorCode": "SUCCESS",
              "message": null
            }
                        """)
    public static Object getFinalPrompt(String prompt, String displayedSelectionMethod, Long numOfEntities) {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "SUCCESS";
        String finalPrompt = createFinalPrompt(prompt, displayedSelectionMethod, numOfEntities);
        response.put("result", finalPrompt);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static String deriveTaskType(Map<String, Object> taskRow, boolean hasRefTables) {
        String taskType = (String) taskRow.get("task_type");
        Object beIdObject = taskRow.get("be_id");
        Long beId = null;
        if (beIdObject != null) {
            beId = Long.parseLong(beIdObject.toString());
        }
        String syncMode = (String) taskRow.get("sync_mode");
        boolean reserveInd = (boolean) taskRow.get("reserve_ind");
        boolean cloneInd = (boolean) taskRow.get("clone_ind");
        String sourceEnvName = (String) taskRow.get("source_env_name");
        String selectionMethod = (String) taskRow.get("selection_method");

        if (cloneInd) {
            return "Clone entities";
        }
        // Handle specific task types first
        if ("DELETE".equals(taskType)) {
            return "Delete entities";
        }
        if ("RESERVE".equals(taskType)) {
            return "Reserve entities";
        }

        if ("TRAINING".equals(taskType)) {
            return "Training";
        }

        // Handle specific beId cases
        if (beId != null && beId == -1) {
            if ("EXTRACT".equals(taskType)) {
                return "Extract tables";
            }
            if ("LOAD".equals(taskType)) {
                return "OFF".equals(syncMode) ? "Load tables" : "Extract and load tables";
            }
        }

        // Handle LOAD tasks with specific selection methods first (more specific
        // conditions)
        if ("LOAD".equals(taskType) && "GENERATE".equals(selectionMethod)) {
            if (reserveInd) {
                return "Rule-based Generate load and reserve entities";
            }
            return "Rule-based Generate and load entities";
        }

        if ("LOAD".equals(taskType) && "AI_GENERATED".equals(selectionMethod)) {
            if (reserveInd) {
                return "AI-based Generate load and reserve entities";
            }
            return "AI-based Generate and load entities";
        }

        // Handle GENERATE/AI_GENERATED task types
        if ("GENERATE".equals(taskType)
                && (selectionMethod == null || !"AI_GENERATED".equalsIgnoreCase(selectionMethod))) {
            return "Rule-based Generate entities";
        }
        if ("AI_GENERATED".equals(taskType)
                || (selectionMethod != null && "AI_GENERATED".equalsIgnoreCase(selectionMethod))) {
            return "AI-based Generate entities";
        }

        // Handle LOAD and EXTRACT based on other parameters
        boolean isEntityTask = (beId == null || beId > 0);
        String scopeSuffix = "";
        if (isEntityTask) {
            scopeSuffix = hasRefTables ? " entities + tables" : " entities";
        }

        if ("EXTRACT".equals(taskType) && isEntityTask) {
            return "Extract" + scopeSuffix;
        }

        if ("LOAD".equals(taskType) && isEntityTask) {
            if (sourceEnvName != null && (sourceEnvName.equalsIgnoreCase(SYNTHETIC_ENVIRONMENT)
                    || sourceEnvName.equalsIgnoreCase(AI_ENVIRONMENT))) {
                if (reserveInd) {
                    return "Load and reserve generated entities (AI/rule based)";
                }
                return "GENERATE_SUBSET".equalsIgnoreCase(selectionMethod)
                        ? "Load execution generation entities (AI/rule based)"
                        : "Load generated entities (AI/rule based)";
            }

            if ("OFF".equals(syncMode)) {
                boolean versionInd = taskRow.get("version_ind") != null && (boolean) taskRow.get("version_ind");
                if (versionInd) {
                    return "Load data version entities";
                }
                return reserveInd ? "Load and reserve" + scopeSuffix : "Load" + scopeSuffix;
            } else { // syncMode is not "OFF"
                return reserveInd ? "Extract, load, and reserve" + scopeSuffix : "Extract and load" + scopeSuffix;
            }
        }

        return "Unknown Task Type";
    }

    private static String basePromptText(String taskType) throws SQLException {
        String result = "";
        String sql = "SELECT prompt_text " +
                "FROM " + TDMDB_SCHEMA + ".task_execution_prompt_text " +
                "WHERE task_type = ?";
        Db.Row row = db(TDM).fetch(sql, taskType).firstRow();
        if (row != null) {
            result = (String) row.get("prompt_text");
        }
        return result;
    }

    private static boolean shouldRemoveGenParamsSentence(Long taskId, Object taskOverrideFieldsRaw) throws SQLException {
        String taskOverrideFieldsJson = (taskOverrideFieldsRaw != null) ? String.valueOf(taskOverrideFieldsRaw) : null;
        if (taskOverrideFieldsJson != null && !taskOverrideFieldsJson.isEmpty() && !"null".equals(taskOverrideFieldsJson)) {
            try {
                JsonObject overrideJson = JsonParser.parseString(taskOverrideFieldsJson).getAsJsonObject();
                if (overrideJson.has("selection_method")) {
                    JsonObject selMethod = overrideJson.getAsJsonObject("selection_method");
                    if (selMethod.has("generate_data_params")) {
                        JsonObject genDataParams = selMethod.getAsJsonObject("generate_data_params");
                        if (genDataParams.has("can_add_params")) {
                            JsonObject canAddParamsObj = genDataParams.getAsJsonObject("can_add_params");
                            if (canAddParamsObj.has("is_editable") && canAddParamsObj.get("is_editable").getAsBoolean()) {
                                return false;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        String countSql = "SELECT COUNT(*) AS cnt FROM " + TDMDB_SCHEMA + ".tdm_generate_task_field_mappings WHERE task_id = ?";
        Db.Row countRow = db(TDM).fetch(countSql, taskId).firstRow();
        long cnt = countRow != null ? Long.parseLong(countRow.get("cnt").toString()) : 0;
        return cnt == 0;
    }

    private static String replacePromptParameters(String finalPrompt, Map<String, Object> taskDetails) {
        String replacedPrompt = finalPrompt;

        replacedPrompt = replacedPrompt.replace("<be_name>", String.valueOf(taskDetails.get("be_name")));
        replacedPrompt = replacedPrompt.replace("<source_env_name>",
                String.valueOf(taskDetails.get("source_env_name")));
        replacedPrompt = replacedPrompt.replace("<environment_name>",
                String.valueOf(taskDetails.get("environment_name")));
        replacedPrompt = replacedPrompt.replace("<selection_method>",
                String.valueOf(taskDetails.get("selection_method")));
        if (replacedPrompt.contains("<scope>")) {
            if ("Predefined entity list".equalsIgnoreCase(String.valueOf(taskDetails.get("selection_method")))) {
                replacedPrompt = replacedPrompt.replace("<scope>", "all entities");
            } else {
                replacedPrompt = replacedPrompt.replace("<scope>", "entity IDs (<selection_param_value>)");
            }
        }
        replacedPrompt = replacedPrompt.replace("<selection_param_value>",
                String.valueOf(taskDetails.get("selection_param_value")));
        replacedPrompt = replacedPrompt.replace("<num_of_entities>",
                String.valueOf(taskDetails.get("num_of_entities")));
        replacedPrompt = replacedPrompt.replace("<reserve_retention_period_value>",
                String.valueOf(taskDetails.get("reserve_retention_period_value")));
        replacedPrompt = replacedPrompt.replace("<reserve_retention_period_type>",
                String.valueOf(taskDetails.get("reserve_retention_period_type")).toLowerCase());
        replacedPrompt = replacedPrompt.replace("<retention_period_value>",
                String.valueOf(taskDetails.getOrDefault("retention_period_value", "")));
        replacedPrompt = replacedPrompt.replace("<retention_period_type>",
                String.valueOf(taskDetails.getOrDefault("retention_period_type", "")).toLowerCase());
        replacedPrompt = replacedPrompt.replace("<data_version_name>",
                String.valueOf(taskDetails.getOrDefault("data_version_name", "")));
        replacedPrompt = replacedPrompt.replace("<version_creation_date>",
                String.valueOf(taskDetails.getOrDefault("version_creation_date", "")));
        replacedPrompt = replacedPrompt.replace("<generation_title>",
                String.valueOf(taskDetails.getOrDefault("generation_title", "")));
        replacedPrompt = replacedPrompt.replace("<generation_creation_date>",
                String.valueOf(taskDetails.getOrDefault("generation_creation_date", "")));
        replacedPrompt = replacedPrompt.replace("<generation_number_of_entities>",
                String.valueOf(taskDetails.getOrDefault("generation_number_of_entities", "")));

        return replacedPrompt;
    }

    @desc("Returns executions based on scope: History (completed/failed/stopped/killed), Running (active), Future (scheduled tasks not yet executed), or All. Date range is required for History and All scopes.")
    @webService(path = "tasks/executions", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                        {
              "result": [
                {
                  "task_id": 2,
                  "task_title": "Extract and Load entities",
                  "execution_status": "completed",
                  "start_execution_time": "2026-03-31 13:56:01.683532",
                  "end_execution_time": "2026-03-31 13:56:25.0",
                  "environment_name": "UAT",
                  "be_name": "Subscriber",
                  "actions": "Extract and load",
                  "scope": "history"
                },
                {
                  "task_id": 5,
                  "task_title": "Clone entities",
                  "execution_status": "running",
                  "start_execution_time": "2026-04-09 10:00:00.0",
                  "environment_name": "UAT",
                  "be_name": "Customer",
                  "actions": "Extract and load",
                  "scope": "running"
                },
                {
                  "task_id": 7,
                  "task_title": "Nightly Extract",
                  "task_type": "EXTRACT",
                  "scheduler": "0 0 * * *",
                  "scheduling_end_date": null,
                  "environment_name": "Production",
                  "be_name": "Customer",
                  "actions": "Extract",
                  "scope": "future"
                }
              ],
              "errorCode": "SUCCESS",
              "message": null
            }
            """)
    public static Object wsGetExecutions(
            @param(required = true, description = "Scope of executions to return. Allowed values: All, In-process, Future, History") String scope,
            @param(required = false, description = "Required for History and All scopes. Date format: ISO 8601") String fromDate,
            @param(required = false, description = "Required for History and All scopes. Date format: ISO 8601") String toDate,
            @param(required = false, description = "Filter by specific Task ID") Long taskId)
            throws Exception {

        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "FAILED";
        String permissionGroup = fnGetUserPermissionGroup("");
        String userName = sessionUser().name();
        String fabricRoles = String.join(TDM_PARAMETERS_SEPARATOR , sessionUser().roles());

        // 1. Validate scope — comma-separated combination of: History, In-process,
        // Future, All
        if (scope == null || scope.trim().isEmpty()) {
            message = "scope is required. Allowed values (combinable via comma): History, In-process, Future, All.";
            log.error(message);
            response.put("errorCode", errorCode);
            response.put("message", message);
            return response;
        }

        Set<String> scopeTokens = new HashSet<>();
        for (String token : scope.split(",")) {
            String t = token.trim().toUpperCase();
            if (t.equals("ALL")) {
                scopeTokens.add("HISTORY");
                scopeTokens.add("IN-PROCESS");
                scopeTokens.add("FUTURE");
            } else if (t.equals("HISTORY") || t.equals("IN-PROCESS") || t.equals("FUTURE")) {
                scopeTokens.add(t);
            } else {
                message = "Invalid scope value: '" + token.trim()
                        + "'. Allowed values: History, In-process, Future, All.";
                log.error(message);
                response.put("errorCode", errorCode);
                response.put("message", message);
                return response;
            }
        }

        boolean includeHistory = scopeTokens.contains("HISTORY");
        boolean includeInProcess = scopeTokens.contains("IN-PROCESS");
        boolean includeFuture = scopeTokens.contains("FUTURE");
        // Apply date range to in-process only when history is also requested (dates are
        // provided)
        boolean applyDateRangeToInProcess = includeHistory && includeInProcess;

        // 2. Validate dates (required for History and All)
        OffsetDateTime fromDateTime = null;
        OffsetDateTime toDateTime = null;

        if (includeHistory) {
            if (fromDate == null || fromDate.trim().isEmpty() || toDate == null || toDate.trim().isEmpty()) {
                message = "fromDate and toDate are required for scope: " + scope + ".";
                log.error(message);
                response.put("errorCode", errorCode);
                response.put("message", message);
                return response;
            }
            try {
                fromDateTime = parseAndValidateDate(fromDate, "fromDate");
                toDateTime = parseAndValidateDate(toDate, "toDate");
            } catch (Exception e) {
                return handleDateException(response, e.getMessage());
            }
            long maxRangeInMonths = getMaxDateRange();
            if (!isDateRangeValid(fromDateTime, toDateTime, maxRangeInMonths)) {
                message = "Date range between fromDate and toDate must be at most " + maxRangeInMonths + " months.";
                log.error(message);
                response.put("errorCode", "FAILED");
                response.put("message", message);
                return response;
            }
        }

        // 3. Computed column expressions for execution-based scopes (History +
        // In-process)
        // COALESCE helpers: prefer override value from task_execution_override_attrs
        // over task defaults
        String effSelMethod = "COALESCE(ovr.override_parameters->>'SELECTION_METHOD', t.selection_method)";
        String effCustomLogicFlow = "COALESCE(ovr.override_parameters->>'CUSTOM_LOGIC_FLOW', t.selection_param_value)";
        String effReserveInd = "COALESCE((ovr.override_parameters->>'RESERVE_IND')::boolean, t.reserve_ind)";

        String execSelectionMethodCaseExpr = "CASE "
                + "   WHEN " + effSelMethod + " = 'ALL' AND summary.be_id >= 0 THEN 'Predefined entity list' "
                + "   WHEN " + effSelMethod + " = 'TABLES' THEN 'Tables' "
                + "   WHEN " + effSelMethod + " = 'C' THEN 'Custom logic' "
                + "   WHEN " + effSelMethod + " = 'L' THEN 'Entity list' "
                + "   WHEN " + effSelMethod + " IN ('P', 'PR') THEN 'Business parameters' "
                + "   WHEN " + effSelMethod + " = 'GENERATE_SUBSET' THEN 'Load pre-generated subset' "
                + "   WHEN " + effSelMethod + " = 'R' THEN 'Random list' "
                + "   WHEN " + effSelMethod + " = 'GENERATE' THEN 'Synthetic generation' "
                + "   ELSE " + effSelMethod + " "
                + "END AS selection_method_label ";

        // Base query for execution-based scopes (History + In-process)
        String executionBaseQuery = "SELECT DISTINCT summary.*, "
                + "t.task_title AS task_name, "
                + "summary.env_name as environment_name, "
                + "be.be_name, "
                + "list.execution_note, "
                + effSelMethod + " AS selection_method, "
                + "CASE WHEN " + effSelMethod + " = 'C' THEN " + effCustomLogicFlow
                + " ELSE NULL END AS selection_method_param, "
                + "CASE "
                + "   WHEN summary.be_id < 0 THEN 'Tables' "
                + "   WHEN summary.be_id >= 0 AND summary.tot_num_of_processed_ref_tables > 0 THEN 'Entities & Tables' "
                + "   ELSE 'Entities' "
                + "END AS processed_data, "
                + "CASE "
                + "   WHEN UPPER(summary.execution_status) = 'RUNNING' THEN summary.start_execution_time::text "
                + "   WHEN UPPER(summary.execution_status) = 'PENDING' THEN summary.creation_date::text "
                + "   ELSE summary.end_execution_time::text "
                + "END AS task_status_date, "
                + "CASE "
                + "   WHEN summary.end_execution_time IS NOT NULL AND summary.start_execution_time IS NOT NULL "
                + "       THEN TO_CHAR(summary.end_execution_time - summary.start_execution_time, 'HH24:MI:SS') "
                + "   WHEN summary.start_execution_time IS NOT NULL "
                + "       THEN TO_CHAR(NOW() - summary.start_execution_time, 'HH24:MI:SS') "
                + "   ELSE NULL "
                + "END AS duration, "
                + "summary.task_type_label, "
                + execSelectionMethodCaseExpr
                + "FROM " + TDMDB_SCHEMA + ".task_execution_summary AS summary "
                + "LEFT JOIN " + TDMDB_SCHEMA + ".task_execution_list AS list "
                + "ON summary.task_execution_id = list.task_execution_id AND list.parent_lu_id IS NULL "
                + "LEFT JOIN " + TDMDB_SCHEMA + ".business_entities AS be "
                + "ON (summary.be_id = be.be_id) "
                + "INNER JOIN " + TDMDB_SCHEMA + ".tasks AS t ON summary.task_id = t.task_id "
                + "LEFT JOIN " + TDMDB_SCHEMA + ".task_execution_override_attrs AS ovr "
                + "ON summary.task_execution_id = ovr.task_execution_id AND summary.task_id = ovr.task_id ";

        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 4. In-process (pending + running) — returned first as highest priority
            if (includeInProcess) {
                StringBuilder inProcessQuery = new StringBuilder(executionBaseQuery);
                List<Object> inProcessParams = new ArrayList<>();

                if ("tester".equalsIgnoreCase(permissionGroup)) {
                    inProcessQuery.append(" WHERE summary.task_executed_by = ? ");
                    inProcessParams.add(userName);
                } else if ("owner".equalsIgnoreCase(permissionGroup)) {
                    inProcessQuery.append(" WHERE (summary.task_executed_by = ? OR ");
                    inProcessQuery.append(ownerExecutionVisibilityCondition(
                            "summary",
                            "t.sync_mode",
                            effSelMethod,
                            "summary.environment_id",
                            "summary.source_environment_id"
                    ));
                    inProcessQuery.append(") ");

                    inProcessParams.add(userName);

                    addOwnerEnvParams(inProcessParams, userName, fabricRoles);

                    
                } else {
                    inProcessQuery.append(" WHERE 1=1 ");
                }

                inProcessQuery.append(" AND UPPER(summary.execution_status) IN ('PENDING', 'RUNNING', 'WAITING')");

                if (taskId != null) {
                    inProcessQuery.append(" AND summary.task_id = ?");
                    inProcessParams.add(taskId);
                }

                if (applyDateRangeToInProcess) {
                    inProcessQuery.append(" AND COALESCE(summary.start_execution_time, summary.creation_date) >= ?");
                    inProcessParams.add(Timestamp.from(fromDateTime.toInstant()));
                    inProcessQuery.append(" AND COALESCE(summary.start_execution_time, summary.creation_date) <= ?");
                    inProcessParams.add(Timestamp.from(toDateTime.toInstant()));
                }

                inProcessQuery.append(" ORDER BY summary.task_execution_id DESC");

                try (Db.Rows rows = db(TDM).fetch(inProcessQuery.toString(), inProcessParams.toArray())) {
                    for (Map<String, Object> row : getResultList(rows)) {
                        row.put("execution_status", "In-process");
                        row.put("scope", "in-process");
                        result.add(row);
                    }
                }
            }

            // 5. History
            if (includeHistory) {
                StringBuilder historyQuery = new StringBuilder(executionBaseQuery);
                List<Object> historyParams = new ArrayList<>();

                if ("tester".equalsIgnoreCase(permissionGroup)) {
                    historyQuery.append(" WHERE summary.task_executed_by = ? ");
                    historyParams.add(userName);
                } else if ("owner".equalsIgnoreCase(permissionGroup)) {
                    historyQuery.append(" WHERE (summary.task_executed_by = ? OR ");
                    historyQuery.append(ownerExecutionVisibilityCondition(
                            "summary",
                            "t.sync_mode",
                            effSelMethod,
                            "summary.environment_id",
                            "summary.source_environment_id"
                    ));
                    historyQuery.append(") ");

                    historyParams.add(userName);

                    addOwnerEnvParams(historyParams, userName, fabricRoles);


                } else {
                    historyQuery.append(" WHERE 1=1 ");
                }

                historyQuery.append(" AND UPPER(summary.execution_status) NOT IN ('PENDING', 'RUNNING', 'WAITING')");

                if (taskId != null) {
                    historyQuery.append(" AND summary.task_id = ?");
                    historyParams.add(taskId);
                }

                historyQuery.append(" AND COALESCE(summary.start_execution_time, summary.creation_date) >= ?");
                historyParams.add(Timestamp.from(fromDateTime.toInstant()));
                historyQuery.append(" AND COALESCE(summary.start_execution_time, summary.creation_date) <= ?");
                historyParams.add(Timestamp.from(toDateTime.toInstant()));
                historyQuery.append(" ORDER BY summary.task_execution_id DESC");

                try (Db.Rows rows = db(TDM).fetch(historyQuery.toString(), historyParams.toArray())) {
                    for (Map<String, Object> row : getResultList(rows)) {
                        row.put("scope", "history");
                        result.add(row);
                    }
                }
            }

            // 6. Future (active scheduled tasks from the tasks table)
            if (includeFuture) {
                StringBuilder futureQuery = new StringBuilder(
                        "SELECT DISTINCT t.task_id, "
                                + "t.task_title AS task_name, "
                                + "t.task_type, "
                                + "t.scheduler, "
                                + "t.scheduling_end_date, "
                                + "t.be_id, t.environment_id, t.env_name, t.source_env_name, t.source_environment_id, "
                                + "t.task_created_by, t.task_creation_date, t.task_last_updated_date, "
                                + "t.selection_method, "
                                + "CASE WHEN t.selection_method = 'C' THEN t.selection_param_value ELSE NULL END AS selection_method_param, "
                                + "t.env_name as environment_name, be.be_name, "
                                + "NULL::text AS execution_note, "
                                + "NULL::text AS task_executed_by, "
                                + "NULL::text AS duration, "
                                + "CASE "
                                + "   WHEN t.be_id < 0 THEN 'Tables' "
                                + "   WHEN t.be_id >= 0 AND t.refresh_reference_data = true THEN 'Entities & Tables' "
                                + "   ELSE 'Entities' "
                                + "END AS processed_data, "
                                + "CASE "
                                + "   WHEN t.in_place_masking_ind = true THEN 'In-place masking' "
                                + "   WHEN t.task_type = 'EXTRACT' THEN 'Extract' "
                                + "   WHEN t.task_type = 'LOAD' AND t.sync_mode <> 'OFF' AND t.delete_before_load = true THEN 'Extract & Delete & Load' "
                                + "   WHEN t.task_type = 'LOAD' AND t.source_environment_id < 0 AND t.selection_method NOT IN ('GENERATE', 'AI_GENERATED') THEN 'Load SDG entities' "
                                + "   WHEN t.task_type = 'LOAD' AND t.selection_method = 'GENERATE' THEN 'Rule-based SDG & Load' "
                                + "   WHEN t.task_type = 'LOAD' AND t.sync_mode <> 'OFF' THEN 'Extract & Load' "
                                + "   WHEN t.task_type = 'LOAD' AND t.reserve_ind = true AND t.delete_before_load = false THEN 'Load & Reserve' "
                                + "   WHEN t.task_type = 'LOAD' AND t.reserve_ind = true AND t.delete_before_load = true THEN 'Load & Reserve & Delete' "
                                + "   WHEN t.task_type = 'LOAD' AND t.reserve_ind = false AND t.delete_before_load = true THEN 'Load & Delete' "
                                + "   WHEN t.task_type = 'LOAD' THEN 'Load' "
                                + "   WHEN t.task_type = 'DELETE' THEN 'Delete only' "
                                + "   WHEN t.task_type = 'RESERVE' THEN 'Reserve only' "
                                + "   WHEN t.task_type = 'TRAINING' THEN 'AI training' "
                                + "   WHEN t.task_type = 'GENERATE' THEN 'Rule-based generation' "
                                + "   WHEN t.task_type = 'AI_GENERATED' THEN 'AI-based generation' "
                                + "   ELSE t.task_type "
                                + "END AS task_type_label, "
                                + "CASE "
                                + "   WHEN t.selection_method = 'ALL' AND t.be_id >= 0 THEN 'Predefined entity list' "
                                + "   WHEN t.selection_method = 'TABLES' THEN 'Tables' "
                                + "   WHEN t.selection_method = 'C' THEN 'Custom logic' "
                                + "   WHEN t.selection_method = 'L' THEN 'Entity list' "
                                + "   WHEN t.selection_method IN ('P', 'PR') THEN 'Business parameters' "
                                + "   WHEN t.selection_method = 'GENERATE_SUBSET' THEN 'Load pre-generated subset' "
                                + "   WHEN t.selection_method = 'R' THEN 'Random list' "
                                + "   WHEN t.selection_method = 'GENERATE' THEN 'Synthetic generation' "
                                + "   ELSE t.selection_method "
                                + "END AS selection_method_label "
                                + "FROM " + TDMDB_SCHEMA + ".tasks AS t "
                                + "LEFT JOIN " + TDMDB_SCHEMA + ".business_entities AS be ON t.be_id = be.be_id "
                                + "WHERE t.task_status = 'Active' AND t.task_execution_status = 'Active' "
                                + "  AND t.scheduler <> 'immediate' "
                                + "  AND (t.scheduling_end_date IS NULL OR t.scheduling_end_date > NOW())");

                List<Object> futureParams = new ArrayList<>();

                if ("tester".equalsIgnoreCase(permissionGroup)) {
                    futureQuery.append(" AND t.task_created_by = ?");
                    futureParams.add(userName);
                } else if ("owner".equalsIgnoreCase(permissionGroup)) {
                    futureQuery.append(" AND (t.task_created_by = ? OR ");
                    futureQuery.append(ownerExecutionVisibilityCondition(
                            "t",
                            "t.sync_mode",
                            "t.selection_method",
                            "t.environment_id",
                            "t.source_environment_id"
                    ));
                    futureQuery.append(") ");

                    futureParams.add(userName);
                    addOwnerEnvParams(futureParams, userName, fabricRoles);
                }

                if (taskId != null) {
                    futureQuery.append(" AND t.task_id = ?");
                    futureParams.add(taskId);
                }

                try (Db.Rows rows = db(TDM).fetch(futureQuery.toString(), futureParams.toArray())) {
                    List<String> columnNames = rows.getColumnNames();
                    CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
                    ZonedDateTime now = ZonedDateTime.now();
                    for (Db.Row row : rows) {
                        Map<String, Object> rowMap = new HashMap<>();
                        for (String col : columnNames) {
                            rowMap.put(col, row.get(col));
                        }
                        String scheduler = (String) rowMap.get("scheduler");
                        if (scheduler != null && !scheduler.equalsIgnoreCase("immediate")) {
                            try {
                                ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse(scheduler));
                                Optional<ZonedDateTime> nextExec = executionTime.nextExecution(now);
                                if (nextExec.isPresent()) {
                                    rowMap.put("task_status_date",
                                            nextExec.get().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                }
                            } catch (Exception e) {
                                log.warn("Failed to compute next run date from scheduler: " + scheduler, e);
                            }
                        }
                        rowMap.put("scope", "future");
                        result.add(rowMap);
                    }
                }
            }

            errorCode = "SUCCESS";
        } catch (Exception e) {
            return handleGeneralException(response, e.getMessage());
        }

        response.put("result", result);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }
    
    private static String ownerExecutionVisibilityCondition(String taskAlias, String syncModeCol,
        String selectionMethodExpr, String ownerEnvTargetCol, String ownerEnvSourceCol) {

        String ownerEnvSql = "SELECT environment_id FROM " + TDMDB_SCHEMA
                + ".environment_owners WHERE user_name = ? OR user_id = ANY(string_to_array(?, '"
                + TDM_PARAMETERS_SEPARATOR + "'))";

       return "("
            //source only tasks
            + " (" + taskAlias + ".task_type = 'EXTRACT' "
            + "   AND " + syncModeCol + " IN ('ON', 'FORCE') "
            + "   AND " + ownerEnvSourceCol + " IN (" + ownerEnvSql + ")) "
            // target only tasks DELETE AND RESERVE 
            + " OR (" + taskAlias + ".task_type IN ('DELETE', 'RESERVE') "
            + "   AND " + ownerEnvTargetCol + " IN (" + ownerEnvSql + ")) "
            // target only tasks LOAD WITH SYNC OFF
            + " OR (" + taskAlias + ".task_type = 'LOAD' "
            + "   AND " + syncModeCol + " = 'OFF' "
            + "   AND " + ownerEnvTargetCol + " IN (" + ownerEnvSql + ")) "
            // BOTH 
            + " OR (" + taskAlias + ".task_type = 'LOAD' "
            + "   AND " + syncModeCol + " <> 'OFF' "
            + "   AND " + selectionMethodExpr + " NOT IN ('GENERATE', 'AI_GENERATED') "
            + "   AND " + ownerEnvSourceCol + " IN (" + ownerEnvSql + ") "
            + "   AND " + ownerEnvTargetCol + " IN (" + ownerEnvSql + ")) "
            // AI training OFF -> AI env owner only
            + " OR (" + taskAlias + ".task_type = 'TRAINING' "
            + "   AND " + syncModeCol + " = 'OFF' "
            + "   AND " + ownerEnvSourceCol + " = -2 "
            + "   AND " + ownerEnvSourceCol + " IN (" + ownerEnvSql + ")) "
            // Training with sync ON/FORCE -> must own both
            + " OR (" + taskAlias + ".task_type = 'TRAINING' "
            + "   AND " + syncModeCol + " <> 'OFF' "
            + "   AND " + ownerEnvSourceCol + " IN (" + ownerEnvSql + ") "
            + "   AND " + ownerEnvTargetCol + " IN (" + ownerEnvSql + ")) "
            // AI SGD - RULE BASED tasks with load
            + " OR (" + taskAlias + ".task_type = 'LOAD' "
            + "   AND " + ownerEnvSourceCol + " IN (-1, -2) "
            + "   AND " + selectionMethodExpr + " IN ('GENERATE', 'AI_GENERATED') "
            + "   AND " + ownerEnvSourceCol + " IN (" + ownerEnvSql + ") "
            + "   AND " + ownerEnvTargetCol + " IN (" + ownerEnvSql + ")) "
            // AI SGD - RULE BASED tasks without LAOD
            + " OR (" + taskAlias + ".task_type IN ('GENERATE', 'AI_GENERATED') "
            + "   AND " + ownerEnvSourceCol + " IN (-1, -2) "
            + "   AND " + ownerEnvSourceCol + " IN (" + ownerEnvSql + ")) "
            + ")";
    }
    
    private static void addOwnerEnvParams(List<Object> params, String userName, String fabricRoles) {
        for (int i = 0; i < 11; i++) {
            params.add(userName);
            params.add(fabricRoles);
        }
    }
    
    private static OffsetDateTime parseAndValidateDate(String date, String fieldName) throws Exception {
        if (date != null && !date.isEmpty()) {
            try {
                return OffsetDateTime.parse(date).withOffsetSameInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                String message = "Invalid " + fieldName + " format (ISO 8601): " + date + ".  " + e.getMessage();
                log.warn(message);
                throw new Exception(message);
            }
        }
        return null;
    }

    private static Object handleDateException(HashMap<String, Object> response, String message) {
        String errorCode = "FAILED";
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static long getMaxDateRange() {
        long maxRangeInMonths = 12;
        String sql = "SELECT param_value from " + TDMDB_SCHEMA
                + ".tdm_general_parameters where param_name = 'MAX_DATE_RANGE_IN_MONTHS'";
        try {
            Object param = db(TDM).fetch(sql).firstValue();
            if (param != null) {
                try {
                    maxRangeInMonths = Long.valueOf(String.valueOf(param));
                } catch (NumberFormatException e) {
                    String message = "Invalid value for MAX_DATE_RANGE_IN_MONTHS in tdm_general_parameters: " + param
                            + ".  Using default value of 12.";
                    log.warn(message);
                }
            }
        } catch (SQLException e) {
            String message = "Error fetching MAX_DATE_RANGE_IN_MONTHS from database: " + e.getMessage();
            log.error(message);
        }
        return maxRangeInMonths;
    }

    private static boolean isDateRangeValid(OffsetDateTime fromDateTime, OffsetDateTime toDateTime, long maxRange) {
        long diffInMonths = ChronoUnit.MONTHS.between(fromDateTime, toDateTime);
        return Math.abs(diffInMonths) <= maxRange;
    }

    private static Object handleGeneralException(HashMap<String, Object> response, String message) {
        String errorCode = "FAILED";
        log.error(message);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    private static List<Map<String, Object>> getResultList(Db.Rows rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> columnNames = rows.getColumnNames();

        for (Db.Row row : rows) {
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                Object columnValue = row.get(columnName);

                // Check if the current column is 'override_parameters' and its value is a
                // String
                if ("override_parameters".equals(columnName) && columnValue instanceof String) {
                    String jsonString = (String) columnValue;
                    if (!jsonString.isEmpty()) { // Only try to parse non-empty strings
                        try {
                            Map<String, Object> parsedJson = Json.get().fromJson(jsonString);
                            rowMap.put(columnName, parsedJson);
                        } catch (Exception e) {
                            log.error("Failed to parse JSON for column '" + columnName
                                    + "' for task_execution_id: " + row.get("task_execution_id") + ". JSON String: '"
                                    + jsonString + "'. Error: " + e.getMessage());
                            rowMap.put(columnName, null);
                        }
                    } else {
                        rowMap.put(columnName, null);
                    }
                } else {
                    // For all other columns or if 'override_parameters' is null/not a String
                    rowMap.put(columnName, columnValue);
                }
            }
            result.add(rowMap);
        }
        return result;
    }

    @desc("""
            Updates the execution note for a specific entity associated with a task execution.
            This API updates the note in both the TDM DB and Fabric DB to ensure data consistency.
            Example of a request body:
            {
              "execution_note": "ZivTest update",
              "lu_name": "Customer",
              "entity_id": "Production_4",              
              "root_entity_id": "4",
              "root_target_entity_id": "201013",
              "iid": "4",
              "target_entity_id": "201013"
            }""")
    @webService(path = "task/{task_execution_id}/entityNote", verb = {
            MethodType.PUT }, version = "1", isRaw = false, isCustomPayload = false, produce = {
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
            {
              "result": {
                "message": "Execution note updated successfully."
              },
              "errorCode": "SUCCESS",
              "message": null
            }""")

    public static Object wsUpdateTaskExecutionNote(
            @param(required = true) Long task_execution_id,
            @param(required = true) String execution_note,
            @param(required = true) String lu_name,
            @param(required = true) String entity_id,           
            @param(required = true) String root_entity_id,
            @param(required = true) String root_target_entity_id,
            @param(required = true) String iid,
            @param(required = true) String target_entity_id) throws Exception {

        HashMap<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        String message = null;
        String errorCode = "SUCCESS";

        final String UPDATE_TDM_DB_SQL = String.format(
                """
                        UPDATE %s.task_execution_entities
                        SET execution_note = ?
                        WHERE task_execution_id = ? AND lu_name = ? AND entity_id = ? AND target_entity_id = ? AND root_entity_id = ? AND root_target_entity_id = ?
                        """,
                TDMDB_SCHEMA);

        final String UPDATE_FABRIC_DB_SQL = """
                UPDATE TDM.TASK_EXECUTION_LINK_ENTITIES
                SET EXECUTION_NOTE = ?
                WHERE TASK_EXECUTION_ID = ? AND IID = ? AND LU_NAME = ? AND TARGET_ENTITY_ID = ? AND BE_ROOT_ENTITY_ID = ? AND TARGET_ROOT_ENTITY_ID = ?
                """;

        try {

            db(TDM).beginTransaction();
            fabric().beginTransaction();
            // Update TDM DB
            db(TDM).execute(
                    UPDATE_TDM_DB_SQL,
                    execution_note,
                    task_execution_id,
                    lu_name,
                    entity_id,
                    target_entity_id,
                    root_entity_id,
                    root_target_entity_id);

            fabric().execute("get TDM.?", task_execution_id);

            // Update Fabric DB
            fabric().execute(
                    UPDATE_FABRIC_DB_SQL,
                    execution_note,
                    task_execution_id,
                    iid,
                    lu_name,
                    target_entity_id,
                    root_entity_id,
                    root_target_entity_id);

            db(TDM).commit();
            fabric().commit();

            message = "Execution note updated successfully.";
            result.put("message", message);
            response.put("result", result);

        } catch (Exception e) {
            db(TDM).rollback();
            fabric().rollback();
            message = e.getMessage();
            errorCode = "FAILED";
        }

        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Finds interface configuration for a given environment. The envId is provided in the path and the interfaceName must be provided as a required query parameter. Returns nulls if the interface is not found.")
    @webService(path = "environment/{envId}/interfaceConfig", verb = { MethodType.GET }, version = "1", produce = {
            Produce.JSON })
    @resultMetaData(mediaType = Produce.JSON, example = """
                {
              "result": {
                "max_number_of_workers": 6,
                "affinity": "LOCAL_DC"
              },
              "errorCode": "SUCCESS",
              "message": null
            }
                """)
    public static Object wsGetInterfaceConfig(@param(required = true) Long envId,
            @param(required = true) String interfaceName) throws Exception {
        return getInterfaceConfig(envId, interfaceName);

    }

    @desc("Returns the enable_sequence_report and statistics_report_flag default values")
    @webService(path = "reportsIndicators", verb = { MethodType.GET }, version = "1", produce = {
            Produce.JSON })
    public static Object wsGetReportsDefaultIndicators() throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        String message = null;
        String errorCode = "SUCCESS";

        result.put("enable_sequence_report", getGlobal("TDM_SEQ_REPORT", "TDM"));
        result.put("statistics_report_flag", getGlobal("STATISTICS_REPORT_FLAG", "TDM"));

        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("result", result);
        return response;

    }

     @desc("Returns all notes for a given task.")
    @webService(path = "task/{taskId}/notes", verb = {
            MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = {
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
                {
              "result": [
                {
                  "note_id": 1,
                  "note_title": "Initial Inspection",
                  "task_id": 9,
                  "note_date": "2026-04-16 10:00:00.0",
                  "note_description": "The site was inspected and cleared for work."
                },
                {
                  "note_id": 2,
                  "note_title": "Follow-up required",
                  "task_id": 9,
                  "note_date": "2026-04-16 07:00:55.056",
                  "note_description": "Need to check the wiring again tomorrow."
                }
              ],
              "errorCode": "SUCCESS",
              "message": null
            }
                       """)
    public static Object wsGetTaskNotes(@param(required = true) Long taskId) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        String message = null;
        String errorCode = "SUCCESS";
        try {
            String sql = "SELECT note_id, task_id, note_title, note_description, note_date " +
                    "FROM " + TDMDB_SCHEMA + ".task_notes WHERE task_id = ? ORDER BY note_date DESC";
            try (Db.Rows rows = db(TDM).fetch(sql, taskId)) {
                for (Db.Row row : rows) {
                    Map<String, Object> note = new HashMap<>();
                    note.put("note_id", row.get("note_id"));
                    note.put("task_id", row.get("task_id"));
                    note.put("note_title", row.get("note_title"));
                    note.put("note_description", row.get("note_description"));
                    note.put("note_date", row.get("note_date"));
                    result.add(note);
                }
            }
            response.put("result", result);
        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Creates new notes for a given task.")
    @webService(path = "task/{taskId}/notes", verb = {
            MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = {
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
            {
              "result": [1, 2, 3],
              "errorCode": "SUCCESS",
              "message": null
            }""")
    public static Object wsCreateTaskNotes(
            @param(required = true) Long taskId,
            @param(required = true) List<Map<String, Object>> notes) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "SUCCESS";
        try {
            String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
                    .format(Instant.now());
            String sql = "INSERT INTO " + TDMDB_SCHEMA
                    + ".task_notes (task_id, note_title, note_description, note_date) " +
                    "VALUES (?, ?, ?, ?) RETURNING note_id";
            List<Object> insertedIds = new ArrayList<>();
            for (Map<String, Object> note : notes) {
                String note_title = (String) note.get("note_title");
                String note_description = (String) note.get("note_description");
                String note_date = (String) note.get("note_date");
                String resolvedDate = (note_date != null && !note_date.isEmpty()) ? note_date : now;
                Db.Row row = db(TDM).fetch(sql, taskId, note_title, note_description, resolvedDate).firstRow();
                insertedIds.add(row.get("note_id"));
            }
            response.put("result", insertedIds);
        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }

    @desc("Replaces all notes for a given task by deleting existing ones and inserting the provided list.")
    @webService(path = "task/{taskId}/notes", verb = {
            MethodType.PUT }, version = "1", isRaw = false, isCustomPayload = false, produce = {
                    Produce.JSON }, elevatedPermission = true)
    @resultMetaData(mediaType = Produce.JSON, example = """
            {
              "result": [1, 2, 3],
              "errorCode": "SUCCESS",
              "message": null
            }""")
    public static Object wsUpdateTaskNotes(
            @param(required = true) Long taskId,
            @param(required = true) List<Map<String, Object>> notes) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        String message = null;
        String errorCode = "SUCCESS";
        try {
            db(TDM).execute("DELETE FROM " + TDMDB_SCHEMA + ".task_notes WHERE task_id = ?", taskId);
            String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
                    .format(Instant.now());
            String sql = "INSERT INTO " + TDMDB_SCHEMA
                    + ".task_notes (task_id, note_title, note_description, note_date) " +
                    "VALUES (?, ?, ?, ?) RETURNING note_id";
            List<Object> insertedIds = new ArrayList<>();
            for (Map<String, Object> note : notes) {
                String note_title = (String) note.get("note_title");
                String note_description = (String) note.get("note_description");
                String note_date = (String) note.get("note_date");
                String resolvedDate = (note_date != null && !note_date.isEmpty()) ? note_date : now;
                Db.Row row = db(TDM).fetch(sql, taskId, note_title, note_description, resolvedDate).firstRow();
                insertedIds.add(row.get("note_id"));
            }
            response.put("result", insertedIds);
        } catch (Exception e) {
            errorCode = "FAILED";
            message = e.getMessage();
            log.error(message);
        }
        response.put("errorCode", errorCode);
        response.put("message", message);
        return response;
    }
    
}
