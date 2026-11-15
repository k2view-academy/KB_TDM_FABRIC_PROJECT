package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_TasksManagment;

import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getTDMDBSchema;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.existAnotherMapping;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.fnAssignTaskToTaskGroups;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.fnGetPermissionsForTask;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getAdminTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getFavoriteThenByDateComparator;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getOwnerTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getTesterTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.isAllowedToCreate;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.taskGroupExist;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.INTERNAL_ONLY_OVERRIDE_KEYS;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.reduceGenerateDataParams;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserEnvs;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetSessionUserName;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserPermissionGroup;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserRoles;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.Db.Rows;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.DisplayTaskType;
import com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.ProcessedData;
import com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.SelectionMethodFilter;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.OverrideParamKey;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.param;
import com.k2view.fabric.api.endpoint.Endpoint.resultMetaData;
import com.k2view.fabric.api.endpoint.Endpoint.webService;
import com.k2view.fabric.common.Json;

@SuppressWarnings({ "DefaultAnnotationParam", "unchecked" })
public class Logic extends WebServiceUserCode {

	private static final String favorite_item_type = "Task";
	private static final String noPermissionError = "You don’t have permissions";
	private static final String TDM = "TDM";

	private static String baseQuery() {
		return """
				SELECT
				         t.task_id,
				         tgm.task_group_id,
				         t.task_title,
				         tg.task_group_name,
				         CASE
				             WHEN tuf.favorite_item_id IS NULL THEN FALSE
				             ELSE TRUE
				         END AS favorite,
				         CASE
				             WHEN t.task_Type = 'EXTRACT' THEN 'Extract'
				             WHEN t.task_Type = 'LOAD' AND t.selection_method IN ('GENERATE', 'AI_GENERATED', 'GENERATE_SUBSET') THEN 'Synthetic generation'
				             WHEN t.task_Type = 'LOAD' AND (t.sync_mode = 'ON' OR t.sync_mode = 'FORCE') THEN 'Extract and load'
				             WHEN t.task_Type = 'LOAD' AND (t.sync_mode IS NULL OR t.sync_mode = 'OFF') THEN 'Load'
				             WHEN t.task_Type = 'DELETE' THEN 'Delete'
				             WHEN t.task_Type = 'RESERVE' THEN 'Reserve'
				             WHEN t.task_Type IN ('GENERATE', 'AI-GENERATE', 'TRAINING') THEN 'Synthetic generation'
				             ELSE t.task_Type
				         END AS task_type_derived,
				         CASE
				             WHEN t.in_place_masking_ind = true THEN 'In-place masking'
				             WHEN UPPER(t.task_type) = 'EXTRACT' THEN 'Extract'
				             WHEN UPPER(t.task_type) = 'LOAD' AND t.selection_method = 'GENERATE' THEN 'Rule-based generate & load'
				             WHEN UPPER(t.task_type) = 'LOAD' AND t.selection_method = 'AI_GENERATED' THEN 'AI-based generate & load'
				             WHEN UPPER(t.task_type) = 'LOAD' AND t.selection_method = 'GENERATE_SUBSET' AND t.source_environment_id = -2 THEN 'Load AI-based generated entities'
				             WHEN UPPER(t.task_type) = 'LOAD' AND t.selection_method = 'GENERATE_SUBSET' THEN 'Load rule-based generated entities'
				             WHEN UPPER(t.task_type) = 'LOAD' AND (t.sync_mode = 'ON' OR t.sync_mode = 'FORCE') THEN 'Extract & Load'
				             WHEN UPPER(t.task_type) = 'LOAD' AND t.reserve_ind = true AND t.delete_before_load = false THEN 'Load & Reserve'
				             WHEN UPPER(t.task_type) = 'LOAD' AND t.reserve_ind = true AND t.delete_before_load = true THEN 'Delete & Load & Reserve'
				             WHEN UPPER(t.task_type) = 'LOAD' AND t.reserve_ind = false AND t.delete_before_load = true THEN 'Delete & Load'
				             WHEN UPPER(t.task_type) = 'LOAD' THEN 'Load'
				             WHEN UPPER(t.task_type) = 'DELETE' THEN 'Delete'
				             WHEN UPPER(t.task_type) = 'RESERVE' THEN 'Reserve'
				             WHEN UPPER(t.task_type) = 'TRAINING' THEN 'AI training'
				             WHEN UPPER(t.task_type) = 'GENERATE' THEN 'Rule-based SDG'
				             WHEN UPPER(t.task_type) = 'AI_GENERATED' THEN 'AI-based SDG'
				             ELSE UPPER(t.task_type)
				         END AS task_type_filter,
				         t.task_Type AS task_type_raw,
				         t.task_last_updated_date,
				         t.environment_id,
				         t.source_environment_id,
				         (t.task_override_fields -> 'source_environment' ->> 'is_editable')::boolean AS source_env_editable,
				         (t.task_override_fields -> 'target_environment' ->> 'is_editable')::boolean AS target_env_editable,
				         (t.task_override_fields -> 'selection_method' ->> 'is_editable')::boolean AS selection_method_editable
				     FROM
				         """
				+ getTDMDBSchema() + """
						.task_group_mapping tgm
						    INNER JOIN
						        """ + getTDMDBSchema() + """
						.tasks t ON tgm.task_id = t.task_id
						    INNER JOIN
						        """ + getTDMDBSchema() + """
						.task_groups tg ON tgm.task_group_id = tg.task_group_id
						    LEFT JOIN
						        """ + getTDMDBSchema() + """
						.task_user_favorites tuf ON (
						            t.task_id = tuf.favorite_item_id
						            AND tuf.favorite_item_type = 'Task'
						            AND tuf.user_id = ?
						        )
						    WHERE 1=1""";
	}

	@desc("""
			Delete task from task group
			in case there is no other mapping move it to General group
						""")
	@webService(path = "deleteTaskFromTaskGroup", verb = {
			MethodType.DELETE }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"status": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsDeleteTaskFromGroup(
			@param(description = "Task id", required = true) long task_id,
			@param(description = "Task group id", required = true) long task_group_id)
			throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String status = "";
		String username = sessionUser().name();
		boolean moveToGeneral = false;
		String deleteSQL = "DELETE FROM " + getTDMDBSchema() + ".task_group_mapping" +
				" WHERE task_id = ? AND task_group_id = ?";

		if (!isAllowedToCreate(username)) {
			return wrapWebServiceResults("FAILED", noPermissionError, null);
		}

		if (!existAnotherMapping(task_id, task_group_id)) {
			moveToGeneral = true;
		}

		try {
			if (moveToGeneral) {
				wsMoveTasksToGroups(Arrays.asList(task_id), task_group_id, Arrays.asList(1L), false);

			} else {
				db(TDM).execute(deleteSQL, task_id, task_group_id);
			}
			status = "SUCCESS";

		} catch (Exception e) {
			status = "FAILED";
			message = e.getMessage();
			log.error(message);
		}
		response.put("status", status);
		response.put("message", message);
		return response;

	}
	@desc("Returns the list of task groups that the given task does NOT belong to.")
	@webService(path = "taskAvailableGroups/{task_id}", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
			"result": [
				{
				"task_group_id": 3,
				"task_group_name": "zivtest1"
				},
				{
				"task_group_id": 4,
				"task_group_name": "tali1"
				}
			],
			"errorCode": "SUCCESS",
			"message": null
			}
			""")
	public static Object wsGetAvailableTaskGroups(@param(description = "Task ID", required = true) long task_id) throws Exception {
		Map<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		try {
			String sql = "SELECT tg.task_group_id, tg.task_group_name " +
					"FROM " + getTDMDBSchema() + ".task_groups tg " +
					"WHERE NOT EXISTS (" +
					"    SELECT 1 FROM " + getTDMDBSchema() + ".task_group_mapping tgm " +
					"    WHERE tgm.task_group_id = tg.task_group_id AND tgm.task_id = ?" +
					")";

			List<Map<String, Object>> result = new ArrayList<>();
			try (Db.Rows rows = db(TDM).fetch(sql, task_id)) {
				for (Db.Row row : rows) {
					Map<String, Object> group = new LinkedHashMap<>();
					group.put("task_group_id", Long.parseLong(row.get("task_group_id").toString()));
					group.put("task_group_name", row.get("task_group_name"));
					result.add(group);
				}
			}

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

	@desc("""
			Move tasks from task group A to task group B and C
				Example of a request body:
				{
					"taskIds":[1,2],
					"fromTaskGroup": 1,
					"toTaskGroup": [2,3]
				  }
							""")
	@webService(path = "moveTasksToGroups", verb = {
			MethodType.PUT }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """

			""")
	public static Object wsMoveTasksToGroups(
			@param(description = "Task ids.", required = true) List<Long> taskIds,
			@param(description = "From task group id.", required = true) long fromTaskGroup,
			@param(description = "To task group ids.", required = true) List<Long> toTaskGroups,
			@param(description = "Keep current group.") boolean keepCurrentGroup)
			throws Exception {
		Map<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String username = sessionUser().name();

		for (long taskID : taskIds) {
			if (!isAllowedToCreate(username)) {
				return wrapWebServiceResults("FAILED", noPermissionError, null);
			}

			try {
				fnAssignTaskToTaskGroups(taskID, toTaskGroups, false);

				if (!keepCurrentGroup) {
					wsDeleteTaskFromGroup(taskID, fromTaskGroup);
				}

				errorCode = "SUCCESS";

			} catch (Exception e) {
				errorCode = "FAILED";
				message = e.getMessage();
				log.error(message);
				break;
			}

		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Marks a task as a favourite for the current user.")
	@webService(path = "markTaskFavorite", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"status": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsMarkTaskFavorite(
			@param(description = "Task id", required = true) long task_id)
			throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String status = "";
		String insertSQL = "INSERT INTO " + getTDMDBSchema() + ".task_user_favorites" +
				"(user_id, favorite_item_type, favorite_item_id) " +
				"VALUES (?, ?, ?)";
		String username = sessionUser().name();

		try {
			db(TDM).execute(insertSQL, username, favorite_item_type, task_id);
			status = "SUCCESS";

		} catch (Exception e) {
			status = "FAILED";
			message = e.getMessage();
			log.error(message);
		}
		response.put("status", status);
		response.put("message", message);
		return response;
	}

	@desc("""
			Unmark task as Favorite
			Example of a request body:
			{
				"task_id": 4
			}
						""")
	@webService(path = "unMarkTaskFavorite", verb = {
			MethodType.DELETE }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"status": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsUnMarkTaskFavorite(
			@param(description = "Task id", required = true) long task_id)
			throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String status = "";
		String sql = "DELETE FROM " + getTDMDBSchema() + ".task_user_favorites" +
				" WHERE user_id= ? AND favorite_item_type = ? AND favorite_item_id = ?";
		String username = sessionUser().name();

		try {
			db(TDM).execute(sql, username, favorite_item_type, task_id);
			status = "SUCCESS";

		} catch (Exception e) {
			status = "FAILED";
			message = e.getMessage();
			log.error(message);
		}
		response.put("status", status);
		response.put("message", message);
		return response;
	}

	@desc("""
			Search tasks across all task groups, filtered by any combination of the parameters below.
			All parameters are optional. Only tasks the calling user is permitted to see (based on their
			permission group: admin / owner / tester) are returned.
			Results are grouped by task group name; within each group tasks are sorted by favourite first,
			then by last updated date descending (most recently updated first).

			Parameters
			----------
			text             Free-text search. Matches against task title, task description,
			                 task group name, and task group description (case-insensitive).
			                 If the text is numeric, tasks whose task_id starts with it are also
			                 returned (e.g. 12 -> 12, 121, 123, 1200).

			taskTypes        Filter by task type. Multiple values are treated as a logical OR (any match included).
			                 Valid values:
			                   IN_PLACE_MASKING          - In-place masking task
			                   EXTRACT                   - Extract only
			                   EXTRACT_AND_LOAD          - Load with sync mode ON or FORCE
			                   LOAD_AND_RESERVE          - Load with reserve, no delete-before-load
			                   DELETE_AND_LOAD_AND_RESERVE - Load with reserve and delete-before-load
			                   DELETE_AND_LOAD           - Load with delete-before-load, no reserve
			                   LOAD                      - Plain load
			                   DELETE                    - Delete only
			                   RESERVE                   - Reserve only
			                   AI_TRAINING               - AI training task
			                   RULE_BASED_SDG            - Rule-based synthetic data generation
			                   AI_BASED_SDG              - AI-based synthetic data generation
			                   RULE_BASED_GENERATE_AND_LOAD          - Rule-based generate & load
			                   AI_BASED_GENERATE_AND_LOAD            - AI-based generate & load
			                   LOAD_RULE_BASED_GENERATED_ENTITIES    - Load rule-based generated entities
			                   LOAD_AI_BASED_GENERATED_ENTITIES      - Load AI-based generated entities

			sourceEnvironmentIds  Filter by source environment ID. Multiple values = OR (any match included).

			targetEnvironmentIds  Filter by target environment ID. Multiple values = OR (any match included).

			beIds            Filter by business entity ID. Multiple values = OR (any match included).

			selectionMethods Filter by entity selection method. Ignored when dataType = "tables".
			                 Multiple values are treated as a logical OR (any match included).
			                 Valid values:
			                   PREDEFINED_ENTITY_LIST    - Selection method ALL
			                   CUSTOM_LOGIC              - Custom logic (C)
			                   ENTITY_LIST               - Explicit entity list (L)
			                   BUSINESS_PARAMETERS       - Business parameters (P / PR)
			                   LOAD_PRE_GENERATED_SUBSET - Load a pre-generated subset
			                   RANDOM_LIST               - Random entity selection (R)
			                   SYNTHETIC_GENERATION      - Synthetic data generation

			creator          Filter by the username of the task creator (partial match, case-insensitive).

			isScheduled      true  = return only scheduled tasks (scheduler != IMMEDIATE)
			                 false = return only immediate tasks
			                 null  = return both

			dataType         Filter by the type of data the task processes.
			                 Valid values:
			                   entities - Entity-based tasks (excludes table-level tasks and tasks
			                              that also process reference tables)
			                   tables   - Table-level tasks only
			                   both     - Entity tasks that also include reference tables

			taskIds          Filter by task ID. Multiple values = OR (any match included).

			Example request body:
			{
			  "text": "GENERAL",
			  "taskTypes": ["EXTRACT", "LOAD"],
			  "sourceEnvironmentIds": [1],
			  "targetEnvironmentIds": [2],
			  "beIds": [1],
			  "selectionMethods": ["ENTITY_LIST", "RANDOM_LIST"],
			  "creator": "ZIV",
			  "isScheduled": false,
			  "dataType": "entities",
			  "taskIds": [9]
			}
				""")
	@webService(path = "search", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = {
					Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
						{
			  "result": {
			    "General": [
			      {
			        "task_last_updated_date": "2026-04-26 10:23:27.461",
			        "display_task_type": "Extract",
			        "task_title": "extract only",
			        "task_id": 9,
			        "can_edit_task": true,
			        "can_create_task": true,
			        "hold_task": false,
			        "favorite": false
			      }
			    ],
			    "Product predefined tasks": [
			      {
			        "task_last_updated_date": "2026-04-21 07:08:17.780096",
			        "display_task_type": "Extract",
			        "task_title": "Extract entities",
			        "task_id": 1,
			        "can_edit_task": true,
			        "can_create_task": true,
			        "hold_task": false,
			        "favorite": false
			      }
			    ],
			    "zivtest2": [
			      {
			        "task_last_updated_date": "2026-04-26 10:23:27.461",
			        "display_task_type": "Extract",
			        "task_title": "extract only",
			        "task_id": 9,
			        "can_edit_task": true,
			        "can_create_task": true,
			        "hold_task": false,
			        "favorite": false
			      }
			    ]
			  },
			  "errorCode": "SUCCESS",
			  "message": null
			}
							""")

	public static Object wsSearch(String text, DisplayTaskType[] taskTypes,
			Long[] sourceEnvironmentIds, Long[] targetEnvironmentIds,
			Long[] beIds, SelectionMethodFilter[] selectionMethods,
			String creator, Boolean isScheduled, ProcessedData dataType, Long[] taskIds)
			throws Exception {
		Map<String, Object> response = new LinkedHashMap<>();
		String errorCode = "";
		String message = null;
		String sql = buildSearchQuery(text, taskTypes, sourceEnvironmentIds, targetEnvironmentIds,
				beIds, selectionMethods, creator, isScheduled, dataType, taskIds);
		Object[] params = createParams(text, taskTypes, sourceEnvironmentIds, targetEnvironmentIds,
				beIds, selectionMethods, creator, taskIds);
		try {
			// Pass 1: collect search candidates.
			// A task may be mapped to more than one group, so the join can return the same
			// task_id on multiple rows. We keep task info once per task_id but record every
			// (taskId, groupId) assignment so multi-group tasks appear under each group.
			Map<Long, Map<String, Object>> taskIdToInfo = new LinkedHashMap<>();
			List<long[]> taskGroupAssignments = new ArrayList<>(); // [taskId, groupId] per row
			Map<Long, String> groupIdToName = new HashMap<>();
			Set<Long> uniqueGroupIds = new HashSet<>();
			String userName = fnGetSessionUserName();

			for (Db.Row row : db(TDM).fetch(sql, params)) {
				long taskId = Long.parseLong(row.get("task_id").toString());
				long groupId = Long.parseLong(row.get("task_group_id").toString());

				uniqueGroupIds.add(groupId);
				groupIdToName.put(groupId, row.get("task_group_name").toString());
				taskGroupAssignments.add(new long[] { taskId, groupId });

				taskIdToInfo.computeIfAbsent(taskId, id -> {
					Map<String, Object> taskInfo = new HashMap<>();
					taskInfo.put("task_id", id);
					taskInfo.put("task_title", row.get("task_title"));
					taskInfo.put("favorite", row.get("favorite"));
					taskInfo.put("task_last_updated_date", row.get("task_last_updated_date"));
					taskInfo.put("display_task_type", row.get("task_type_derived"));
					taskInfo.put("environment_id", row.get("environment_id"));
					taskInfo.put("source_environment_id", row.get("source_environment_id"));
					taskInfo.put("source_env_editable", row.get("source_env_editable"));
					taskInfo.put("target_env_editable", row.get("target_env_editable"));
					taskInfo.put("selection_method_editable", row.get("selection_method_editable"));
					return taskInfo;
				});
			}

			// Permission resolution: call the same functions as wsGetTasksPerTaskGroup,
			// once per unique group, and store the full task data per group so we can
			// enrich search results with hold_task, can_edit_task, can_create_task.
			String permissionGroup = fnGetUserPermissionGroup(userName);

			// For testers, resolve which environments they have access to and whether
			// they have random-selection permission — used in the override gates below.
			Set<Long> userTargetEnvIds = null;
			Set<Long> userSourceEnvIds = null;
			boolean testerHasRandomPerm = true; // unrestricted for admin / owner
			if ("tester".equalsIgnoreCase(permissionGroup)) {
				List<Map<String, Object>> allUserEnvsTypes = fnGetUserEnvs(userName);
				userTargetEnvIds = new HashSet<>();
				userSourceEnvIds = new HashSet<>();
				for (Map<String, Object> envType : allUserEnvsTypes) {					
					List<Map<String, Object>> srcEnvs = (List<Map<String, Object>>) envType.get("source environments");					
					List<Map<String, Object>> tgtEnvs = (List<Map<String, Object>>) envType.get("target environments");
					if (srcEnvs != null)
						for (Map<String, Object> e : srcEnvs)
							userSourceEnvIds.add(Long.parseLong(String.valueOf(e.get("environment_id"))));
					if (tgtEnvs != null)
						for (Map<String, Object> e : tgtEnvs)
							userTargetEnvIds.add(Long.parseLong(String.valueOf(e.get("environment_id"))));
				}
				testerHasRandomPerm = checkTesterHasRandomPerm(allUserEnvsTypes);
			}

			Map<Long, Map<Long, Map<String, Object>>> permittedByGroup = new HashMap<>();
			for (Long groupId : uniqueGroupIds) {
				Set<Map<String, Object>> groupTasks;
				if ("admin".equalsIgnoreCase(permissionGroup)) {
					groupTasks = getAdminTasks(groupId);
				} else if ("owner".equalsIgnoreCase(permissionGroup)) {
					groupTasks = getOwnerTasks(groupId);
				} else {
					groupTasks = getTesterTasks(groupId);
				}
				Map<Long, Map<String, Object>> taskMap = new HashMap<>();
				for (Map<String, Object> t : groupTasks) {
					taskMap.put(((Number) t.get("task_id")).longValue(), t);
				}
				permittedByGroup.put(groupId, taskMap);
			}

			// Pass 2: for each (taskId, groupId) assignment, include the task in that
			// group's list only if the user is permitted to see it in that group.
			// Enrich the search-result task info with hold_task, can_edit_task,
			// can_create_task from the permission-check data (computed once per task).
			Map<String, List<Map<String, Object>>> taskGroupToTasks = new LinkedHashMap<>();
			Set<String> seen = new HashSet<>();
			Set<Long> enriched = new HashSet<>();
			for (long[] pair : taskGroupAssignments) {
				long taskId = pair[0];
				long groupId = pair[1];
				String pairKey = taskId + ":" + groupId;
				if (!seen.add(pairKey))
					continue;
				Map<Long, Map<String, Object>> taskMapForGroup = permittedByGroup.get(groupId);
				if (taskMapForGroup != null && taskMapForGroup.containsKey(taskId)) {
					Map<String, Object> taskInfo = taskIdToInfo.get(taskId);

					// Override permission gates: tasks pulled in via is_editable=true need
					// additional checks that the user can actually use the searched parameter.

					// Target env gate: task is here due to override, not a direct env match →
					// tester must have access to at least one of the searched target envs.
					if (userTargetEnvIds != null && targetEnvironmentIds != null && targetEnvironmentIds.length > 0) {
						if (Boolean.TRUE.equals(taskInfo.get("target_env_editable"))) {
							Object actualId = taskInfo.get("environment_id");
							Set<Long> searchedTargetSet = new HashSet<>(Arrays.asList(targetEnvironmentIds));
							boolean directMatch = actualId != null
									&& searchedTargetSet.contains(((Number) actualId).longValue());
							if (!directMatch
									&& Arrays.stream(targetEnvironmentIds).noneMatch(userTargetEnvIds::contains))
								continue;
						}
					}

					// Source env gate: same pattern for source environments.
					if (userSourceEnvIds != null && sourceEnvironmentIds != null && sourceEnvironmentIds.length > 0) {
						if (Boolean.TRUE.equals(taskInfo.get("source_env_editable"))) {
							Object actualId = taskInfo.get("source_environment_id");
							Set<Long> searchedSourceSet = new HashSet<>(Arrays.asList(sourceEnvironmentIds));
							boolean directMatch = actualId != null
									&& searchedSourceSet.contains(((Number) actualId).longValue());
							if (!directMatch
									&& Arrays.stream(sourceEnvironmentIds).noneMatch(userSourceEnvIds::contains))
								continue;
						}
					}

					// Random selection method gate: overridable tasks that appear in a
					// RANDOM_LIST search require the tester to have random-selection permission.
					if (!testerHasRandomPerm && selectionMethods != null) {
						boolean searchedRandom = Arrays.stream(selectionMethods)
								.anyMatch(m -> m == SelectionMethodFilter.RANDOM_LIST);
						if (searchedRandom && Boolean.TRUE.equals(taskInfo.get("selection_method_editable")))
							continue;
					}
					if (enriched.add(taskId)) {
						Map<String, Object> groupTaskInfo = taskMapForGroup.get(taskId);
						taskInfo.put("hold_task", groupTaskInfo.get("hold_task"));
						taskInfo.put("can_edit_task", groupTaskInfo.get("can_edit_task"));
						taskInfo.put("can_create_task", groupTaskInfo.get("can_create_task"));
					}
					String groupName = groupIdToName.get(groupId);
					taskGroupToTasks.computeIfAbsent(groupName, k -> new ArrayList<>())
							.add(taskInfo);
				}
			}

			// Sort the tasks within each group by task_last_updated_date
			for (List<Map<String, Object>> taskList : taskGroupToTasks.values()) {
				taskList.sort(getFavoriteThenByDateComparator("task_last_updated_date"));
			}

			errorCode = "SUCCESS";
			response.put("result", taskGroupToTasks);

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	private static Object[] createParams(String text, DisplayTaskType[] taskTypes,
			Long[] sourceEnvironmentIds, Long[] targetEnvironmentIds,
			Long[] beIds, SelectionMethodFilter[] selectionMethods, String creator, Long[] taskIds) {
		List<Object> params = new LinkedList<>();
		params.add(fnGetSessionUserName()); // favorites LEFT JOIN

		if (text != null && !text.isEmpty()) {
			params.addAll(Collections.nCopies(4, text));
			if (isNumericText(text)) {
				params.add(text.trim()); // task_id prefix match
			}
		}
		if (sourceEnvironmentIds != null && sourceEnvironmentIds.length > 0) {
			params.addAll(Arrays.asList(sourceEnvironmentIds));
		}
		if (targetEnvironmentIds != null && targetEnvironmentIds.length > 0) {
			params.addAll(Arrays.asList(targetEnvironmentIds));
		}
		if (beIds != null && beIds.length > 0) {
			params.addAll(Arrays.asList(beIds));
		}
		if (selectionMethods != null && selectionMethods.length > 0) {
			for (SelectionMethodFilter m : selectionMethods)
				params.add(m.getDisplayValue());
		}
		if (creator != null && !creator.isEmpty()) {
			params.add("%" + creator + "%");
		}
		if (taskIds != null && taskIds.length > 0) {
			params.addAll(Arrays.asList(taskIds));
		}
		// Task type params last: they bind to the outer subquery WHERE, after all inner
		// query params
		if (taskTypes != null && taskTypes.length > 0) {
			for (DisplayTaskType t : taskTypes)
				params.add(t.getDisplayValue());
		}
		return params.toArray();
	}

	private static boolean isNumericText(String text) {
		return text != null && text.trim().matches("\\d+");
	}

	private static String buildSearchQuery(String text, DisplayTaskType[] taskTypes,
			Long[] sourceEnvironmentIds, Long[] targetEnvironmentIds,
			Long[] beIds, SelectionMethodFilter[] selectionMethods,
			String creator, Boolean isScheduled, ProcessedData dataType, Long[] taskIds) {
		StringBuilder sqlBuilder = new StringBuilder(baseQuery());

		if (text != null && !text.isEmpty()) {
			sqlBuilder.append("""
						AND (t.task_title ILIKE '%%' || ? || '%%' OR
							 t.task_description ILIKE '%%' || ? || '%%' OR
							 tg.task_group_name ILIKE '%%' || ? || '%%' OR
							 tg.task_group_desc ILIKE '%%' || ? || '%%'
					""");
			// Numeric text also matches task IDs starting with it (12 -> 12, 121, 1200...)
			if (isNumericText(text)) {
				sqlBuilder.append(" OR CAST(t.task_id AS TEXT) LIKE ? || '%'");
			}
			sqlBuilder.append(")\n");
		}
		// task_type_filter applied via subquery wrapper below — not inlined here
		if (sourceEnvironmentIds != null && sourceEnvironmentIds.length > 0) {
			sqlBuilder.append(
					" AND (t.source_environment_id IN (" + generatePlaceholders(sourceEnvironmentIds.length) + ")" +
							" OR (t.task_override_fields -> 'source_environment' ->> 'is_editable')::boolean = true)");
		}
		if (targetEnvironmentIds != null && targetEnvironmentIds.length > 0) {
			sqlBuilder.append(
					" AND (t.environment_id IN (" + generatePlaceholders(targetEnvironmentIds.length) + ")" +
							" OR (t.task_override_fields -> 'target_environment' ->> 'is_editable')::boolean = true)");
		}
		if (beIds != null && beIds.length > 0) {
			sqlBuilder.append(
					" AND (t.be_id IN (" + generatePlaceholders(beIds.length) + ")" +
							" OR (t.task_override_fields -> 'business_entity' ->> 'is_editable')::boolean = true)");
		}
		if (selectionMethods != null && selectionMethods.length > 0) {
			// Editable tasks can only be changed to the 4 standard methods;
			// LOAD_PRE_GENERATED_SUBSET,
			// SYNTHETIC_GENERATION and BUSINESS_PARAMETERS require exact-match only.
			boolean anyEditableTarget = Arrays.stream(selectionMethods)
					.anyMatch(m -> m == SelectionMethodFilter.PREDEFINED_ENTITY_LIST
							|| m == SelectionMethodFilter.CUSTOM_LOGIC
							|| m == SelectionMethodFilter.ENTITY_LIST
							|| m == SelectionMethodFilter.RANDOM_LIST);
			sqlBuilder.append(
					" AND ((CASE" +
							" WHEN t.selection_method = 'ALL' THEN 'Predefined entity list'" +
							" WHEN t.selection_method = 'C' THEN 'Custom logic'" +
							" WHEN t.selection_method = 'L' THEN 'Entity list'" +
							" WHEN t.selection_method IN ('P', 'PR') THEN 'Business parameters'" +
							" WHEN t.selection_method = 'GENERATE_SUBSET' THEN 'Load pre-generated subset'" +
							" WHEN t.selection_method = 'R' THEN 'Random list'" +
							" WHEN t.selection_method = 'GENERATE' THEN 'Synthetic generation'" +
							" ELSE t.selection_method END) IN (" + generatePlaceholders(selectionMethods.length) + ")" +
							(anyEditableTarget
									? " OR (t.task_override_fields -> 'selection_method' ->> 'is_editable')::boolean = true"
									: "")
							+
							")");
		}

		if (creator != null && !creator.isEmpty()) {
			sqlBuilder.append(" AND t.task_created_by ILIKE ?");
		}
		if (taskIds != null && taskIds.length > 0) {
			sqlBuilder.append(" AND t.task_id IN (" + generatePlaceholders(taskIds.length) + ")");
		}

		if (isScheduled != null) {
			if (isScheduled) {
				sqlBuilder.append(" AND UPPER(t.scheduler) != 'IMMEDIATE'");
			} else {
				sqlBuilder.append(" AND UPPER(t.scheduler) = 'IMMEDIATE'");
			}
		}
		if (dataType != null) {
			if (dataType == ProcessedData.entities) {
				sqlBuilder.append(" AND t.selection_method != 'TABLES' AND NOT EXISTS (" + existRefTablesQuery() + ")");
			} else if (dataType == ProcessedData.tables) {
				sqlBuilder.append(" AND t.selection_method = 'TABLES'");
			}

			// ProcessedData.both
			else {
				sqlBuilder.append(" AND t.selection_method != 'TABLES' AND EXISTS (" + existRefTablesQuery() + ")");
			}

		}

		if (taskTypes != null && taskTypes.length > 0) {
			return "SELECT * FROM (" + sqlBuilder + ") sub"
					+ " WHERE sub.task_type_filter IN (" + generatePlaceholders(taskTypes.length) + ")"
					+ " ORDER BY sub.task_group_name, sub.task_title";
		}

		sqlBuilder.append("""
					ORDER BY
						tg.task_group_name, t.task_title
				""");

		return sqlBuilder.toString();
	}

	private static String existRefTablesQuery() {
		return "select 1 from " + getTDMDBSchema() + ".task_ref_tables trt where trt.task_id = t.task_id";
	}

	private static String generatePlaceholders(int count) {
		return String.join(",", Collections.nCopies(count, "?"));
	}

	private static boolean checkTesterHasRandomPerm(List<Map<String, Object>> allUserEnvsTypes) throws Exception {
		String sql = "SELECT allowed_random_entity_selection FROM " + getTDMDBSchema() +
				".environment_roles WHERE environment_id = ? AND role_id = ? AND role_status = 'Active' LIMIT 1";
		Set<String> checkedPairs = new HashSet<>();
		for (Map<String, Object> envType : allUserEnvsTypes) {
			for (String key : new String[] { "source environments", "target environments" }) {				
				List<Map<String, Object>> envs = (List<Map<String, Object>>) envType.get(key);
				if (envs == null)
					continue;
				for (Map<String, Object> env : envs) {
					String envId = String.valueOf(env.get("environment_id"));
					String roleId = String.valueOf(env.get("role_id"));
					if (!checkedPairs.add(envId + ":" + roleId))
						continue;
					try (Db.Rows rows = db(TDM).fetch(sql, envId, roleId)) {
						for (Db.Row row : rows) {
							Object val = row.get("allowed_random_entity_selection");
							if (val != null && Boolean.parseBoolean(val.toString()))
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	@desc("Returns grouped tasks available for the user based on their permission group (admin, owner, tester).")
	@webService(path = "getTasksPerTaskGroup", verb = { MethodType.GET }, version = "1", produce = {
			Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
			  "result": [
			    {
			      "task_id": 1,
			      "task_title": "Extract entities",
			      "display_task_type": "Extract",
			      "task_last_updated_date": "2026-04-21 07:08:17.780096",
			      "can_edit_task": true,
			      "can_create_task": true,
			      "hold_task": false,
			      "favorite": false
			    }
			  ],
			  "errorCode": "SUCCESS",
			  "message": null
			}
			""")
	public static Object wsGetTasksPerTaskGroup(
			@param(description = "Task group ID", required = true) long task_group_id)
			throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		String permissionGroup = fnGetUserPermissionGroup("");
		try {
			Set<Map<String, Object>> taskList = new HashSet<>();
			if ("admin".equalsIgnoreCase(permissionGroup)) {
				taskList = getAdminTasks(task_group_id);
			} else if ("owner".equalsIgnoreCase(permissionGroup)) {
				taskList = getOwnerTasks(task_group_id);
			} else {
				taskList = getTesterTasks(task_group_id);
			}
			result.addAll(taskList);
			result.sort(getFavoriteThenByDateComparator("task_last_updated_date"));
			return wrapWebServiceResults("SUCCESS", null, result);
		} catch (Exception e) {
			log.error(e);
			return wrapWebServiceResults("FAILED", e.getMessage(), null);
		}
	}

	@desc("""
			Checks if the user is allowed to create tasks.
			""")
	@webService(path = "taskCreationPermission/canCreateTasks", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"errorCode": "SUCCESS",
				"message": null,
				"result": {
					"can_create_tasks": true
				}
			}
			""")
	public static Object wsCanCreateTasks(
			@param(description = "User name", required = true) String userName)
			throws Exception {

		HashMap<String, Object> response = new HashMap<>();
		HashMap<String, Object> result = new HashMap<>();

		String errorCode;
		String message = null;

		if (userName == null || userName.trim().isEmpty() || "Unknown User".equalsIgnoreCase(userName)) {
			userName = sessionUser().name();
		}
		try {
			result.put("can_create_tasks", isAllowedToCreate(userName));

			errorCode = "SUCCESS";

		} catch (Exception e) {
			errorCode = "FAILED";
			message = "Error checking task creation permission: " + e.getMessage();
			log.error(message, e);
		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		response.put("result", result);

		return response;
	}

	@desc("""
			Creates a draft override record for a task on behalf of the AI agent.
			Allocates a task_execution_id from the standard sequence without scheduling a real job.
			The draft can later be shown via GET /task/execution/{draftId} and promoted to a real
			execution via POST /task/{taskId}/runDraft/{draftId}.
			Multiple drafts per task_id are supported.
			""")
	@webService(path = "task/{taskId}/aiDraftOverride", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"result": {
					"draftId": 42,
					"taskId": 5
				},
				"errorCode": "SUCCESS",
				"message": null
			}
			""")
	public static Object wsCreateAiDraftOverride(
			@param(description = "Task ID", required = true) long taskId,
			@param(description = "Override parameters. Keys must be valid OverrideParamKey enum names (e.g. SOURCE_ENVIRONMENT_NAME, ENTITY_LIST).", required = true) Map<String, Object> overrideParameters,
			@param(description = "Optional. The aifusion session that produced this draft, recorded as its provenance. Survives onto the real execution, because starting a draft reuses this row.") String agentSessionId)
			throws Exception {

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		try {
			String taskCheckSql = "SELECT task_id FROM " + getTDMDBSchema() +
					".tasks WHERE task_id = ? AND task_status = 'Active'";
			Object exists = db(TDM).fetch(taskCheckSql, taskId).firstValue();
			if (exists == null) {
				response.put("errorCode", "FAILED");
				response.put("message", "Task " + taskId + " not found or is not active.");
				return response;
			}

			Long draftId = (Long) db(TDM)
					.fetch("SELECT nextval('" + getTDMDBSchema() + ".tasks_task_execution_id_seq')")
					.firstValue();
			if (draftId == null)
				throw new Exception("Failed to allocate draft execution ID.");

			Map<String, Object> validatedParams = new HashMap<>();
			if (overrideParameters != null) {
				for (Map.Entry<String, Object> entry : overrideParameters.entrySet()) {
					OverrideParamKey key;
					try {
						key = OverrideParamKey.valueOf(entry.getKey().toUpperCase());
					} catch (IllegalArgumentException e) {
						throw new Exception("Unknown override parameter key: " + entry.getKey());
					}
					if (INTERNAL_ONLY_OVERRIDE_KEYS.contains(key))
						throw new Exception("Override parameter '" + entry.getKey() + "' is system-internal and cannot be set externally.");
					Object rawValue = entry.getValue();
					if (key == OverrideParamKey.GENERATE_DATA_PARAMS && rawValue instanceof Map) {
						validatedParams.put(key.name(), reduceGenerateDataParams((Map<String, Object>) rawValue));
					} else {
						validatedParams.put(key.name(), rawValue);
					}
				}
			}

			String paramsJson = Json.get().toJson(validatedParams);
			String insertSql = "INSERT INTO " + getTDMDBSchema() +
					".task_execution_override_attrs (task_id, task_execution_id, override_parameters, created_by_ai, draft, agent_session_id) "
					+
					"VALUES (?, ?, ?::json, true, true, ?)";
			db(TDM).execute(insertSql, taskId, draftId, paramsJson, agentSessionId);

			HashMap<String, Object> result = new HashMap<>();
			result.put("draftId", draftId);
			result.put("taskId", taskId);
			response.put("result", result);
			errorCode = "SUCCESS";

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
			Updates the override parameters of a task execution's override attrs record.
			Only allowed while the record is still a draft (draft = true); once a draft has been
			run/promoted (draft = false) its override parameters can no longer be updated via this API.
			""")
	@webService(path = "task/{taskId}/execution/{taskExecutionId}/overrideAttrs", verb = {
			MethodType.PUT }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"result": {
					"taskId": 5,
					"taskExecutionId": 42
				},
				"errorCode": "SUCCESS",
				"message": null
			}
			""")
	public static Object wsUpdateTaskExecutionOverrideAttrs(
			@param(description = "Task ID", required = true) long taskId,
			@param(description = "Task execution ID", required = true) long taskExecutionId,
			@param(description = "Override parameters. Keys must be valid OverrideParamKey enum names (e.g. SOURCE_ENVIRONMENT_NAME, ENTITY_LIST).", required = true) Map<String, Object> overrideParameters)
			throws Exception {

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		try {
			Map<String, Object> validatedParams = new HashMap<>();
			if (overrideParameters != null) {
				for (Map.Entry<String, Object> entry : overrideParameters.entrySet()) {
					OverrideParamKey key;
					try {
						key = OverrideParamKey.valueOf(entry.getKey().toUpperCase());
					} catch (IllegalArgumentException e) {
						throw new Exception("Unknown override parameter key: " + entry.getKey());
					}
					if (INTERNAL_ONLY_OVERRIDE_KEYS.contains(key))
						throw new Exception("Override parameter '" + entry.getKey() + "' is system-internal and cannot be set externally.");
					Object rawValue = entry.getValue();
					if (key == OverrideParamKey.GENERATE_DATA_PARAMS && rawValue instanceof Map) {
						validatedParams.put(key.name(), reduceGenerateDataParams((Map<String, Object>) rawValue));
					} else {
						validatedParams.put(key.name(), rawValue);
					}
				}
			}

			String paramsJson = Json.get().toJson(validatedParams);
			String updateSql = "UPDATE " + getTDMDBSchema() +
					".task_execution_override_attrs SET override_parameters = ?::json " +
					"WHERE task_execution_id = ? AND task_id = ? AND draft = true RETURNING task_id";
			Object updatedTaskId = db(TDM).fetch(updateSql, paramsJson, taskExecutionId, taskId).firstValue();

			if (updatedTaskId == null) {
				String draftCheckSql = "SELECT draft FROM " + getTDMDBSchema() +
						".task_execution_override_attrs WHERE task_execution_id = ? AND task_id = ?";
				Object draft = db(TDM).fetch(draftCheckSql, taskExecutionId, taskId).firstValue();
				if (draft == null) {
					response.put("errorCode", "FAILED");
					response.put("message", "No override attrs found for task " + taskId + " and execution " + taskExecutionId + ".");
					return response;
				} else {
					response.put("errorCode", "FAILED");
					response.put("message", "Cannot update override parameters for task " + taskId + " and execution " + taskExecutionId + " because it is not a draft.");
					return response;
				}
			}

			HashMap<String, Object> result = new HashMap<>();
			result.put("taskId", taskId);
			result.put("taskExecutionId", taskExecutionId);
			response.put("result", result);
			errorCode = "SUCCESS";

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Returns the list of available selection methods for tasks.")
	@webService(path = "selectionMethods", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = {
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
			  "result": [
			    {
			      "label": "Predefined entity list",
			      "value": "PREDEFINED_ENTITY_LIST"
			    },
			    {
			      "label": "Custom logic",
			      "value": "CUSTOM_LOGIC"
			    },
			    {
			      "label": "Entity list",
			      "value": "ENTITY_LIST"
			    },
			    {
			      "label": "Business parameters",
			      "value": "BUSINESS_PARAMETERS"
			    },
			    {
			      "label": "Load pre-generated subset",
			      "value": "LOAD_PRE_GENERATED_SUBSET"
			    },
			    {
			      "label": "Random list",
			      "value": "RANDOM_LIST"
			    },
			    {
			      "label": "Synthetic generation",
			      "value": "SYNTHETIC_GENERATION"
			    }
			  ],
			  "errorCode": "SUCCESS",
			  "message": null
			}
						""")
	public static Object wsGetSelectionMethods() throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> methods = Arrays.stream(SelectionMethodFilter.values())
					.map(f -> {
						Map<String, Object> m = new HashMap<>();
						m.put("label", f.getDisplayValue());
						m.put("value", f.name());
						return m;
					})
					.collect(Collectors.toList());

			response.put("result", methods);
			response.put("errorCode", "SUCCESS");
			response.put("message", null);
		} catch (Exception e) {
			response.put("result", null);
			response.put("errorCode", "FAILED");
			response.put("message", e.getMessage());
			log.error("Error fetching selection methods: " + e.getMessage());
		}
		return response;
	}

	@desc("Returns the list of available task type filter options.")
	@webService(path = "taskTypeFilter", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = {
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
			  "result": [
			    { "label": "In-place masking",                   "value": "IN_PLACE_MASKING" },
			    { "label": "Extract",                             "value": "EXTRACT" },
			    { "label": "Extract & Load",                     "value": "EXTRACT_AND_LOAD" },
			    { "label": "Load & Reserve",                     "value": "LOAD_AND_RESERVE" },
			    { "label": "Delete & Load & Reserve",            "value": "DELETE_AND_LOAD_AND_RESERVE" },
			    { "label": "Delete & Load",                      "value": "DELETE_AND_LOAD" },
			    { "label": "Load",                               "value": "LOAD" },
			    { "label": "Delete",                             "value": "DELETE" },
			    { "label": "Reserve",                            "value": "RESERVE" },
			    { "label": "AI training",                        "value": "AI_TRAINING" },
			    { "label": "Rule-based SDG",                     "value": "RULE_BASED_SDG" },
			    { "label": "AI-based SDG",                       "value": "AI_BASED_SDG" },
			    { "label": "Rule-based generate & load",         "value": "RULE_BASED_GENERATE_AND_LOAD" },
			    { "label": "AI-based generate & load",           "value": "AI_BASED_GENERATE_AND_LOAD" },
			    { "label": "Load rule-based generated entities", "value": "LOAD_RULE_BASED_GENERATED_ENTITIES" },
			    { "label": "Load AI-based generated entities",   "value": "LOAD_AI_BASED_GENERATED_ENTITIES" }
			  ],
			  "errorCode": "SUCCESS",
			  "message": null
			}
			""")
	public static Object wsGetTaskTypeFilter() throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		try {
			List<Map<String, Object>> types = Arrays.stream(DisplayTaskType.values())
					.map(t -> {
						Map<String, Object> m = new HashMap<>();
						m.put("label", t.getDisplayValue());
						m.put("value", t.name());
						return m;
					})
					.collect(Collectors.toList());

			response.put("result", types);
			response.put("errorCode", "SUCCESS");
			response.put("message", null);
		} catch (Exception e) {
			response.put("result", null);
			response.put("errorCode", "FAILED");
			response.put("message", e.getMessage());
			log.error("Error fetching task type filter options: " + e.getMessage());
		}
		return response;
	}

	@desc("""
			Returns what a tester can and cannot do for a specific task configuration.
			The result is calculated based on source environment, target environment,
			task type, sync mode, reserve indication, and requested number of entities.
			""")
	@webService(path = "task/fnGetPermissionsForTask", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON }, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
			    "result": {
			        "user_name": "tester1",
			        "source_environment_id": "1",
			        "target_environment_id": "2",
			        "task_type": "LOAD",
			        "sync_mode": "OFF",
			        "reserve_ind": false,
			        "task_permissions": {
			            "can_read": false,
			            "can_write": true,
			            "can_reserve": true,
			            "allowed_entities_to_read": 0,
			            "allowed_entities_to_write": 500,
			            "allowed_entities_to_reserve": 100,
			            "max_entities_per_task": 500,
			            "can_request_unlimited_entities": false,
			            "can_run_reference_tasks": true,
			            "can_use_random_selection": true,
			            "can_request_fresh_data": true,
			            "can_schedule_task": false,
			            "can_use_clone": false,
			            "can_use_entity_versioning": true,
			            "can_delete_before_load": false,
			            "can_replace_sequences": true
			        }
			    },
			    "errorCode": "SUCCESS",
			    "message": null
			}
			""")
	public static Object wsGetPermissionsForTask(
			@param(description = "Source environment ID. Required only when the task type and sync mode require a source environment.", required = false) String sourceEnvId,
			@param(description = "Target environment ID. Required only when the task type requires a target environment.", required = false) String targetEnvId,
			@param(description = "User name", required = false) String userName,
			@param(description = "Task type, for example EXTRACT, LOAD, DELETE, RESERVE, TRAINING, GENERATE, AI_GENERATED.", required = false) String taskType,
			@param(description = "Sync mode, for example OFF or FORCE.", required = false) String syncMode,
			@param(description = "Reserve indication", required = false) Boolean reserveInd)
			throws Exception {

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		try {
			if (userName == null || userName.trim().isEmpty()) {
				userName = sessionUser().name();
			}

			if (taskType == null || taskType.trim().isEmpty()) {
				taskType = "Extract";
			}

			if (syncMode == null || syncMode.trim().isEmpty()) {
				syncMode = "OFF";
			}

			boolean reserveIndValue = reserveInd != null && reserveInd;

			Object result = fnGetPermissionsForTask(sourceEnvId,
					targetEnvId, userName, taskType, syncMode, reserveIndValue);

			return result;

		} catch (Exception e) {
			errorCode = "FAILED";
			message = e.getMessage();
			log.error(message);
		}

		response.put("errorCode", errorCode);
		response.put("message", message);
		response.put("result", null);
		return response;
	}

}
