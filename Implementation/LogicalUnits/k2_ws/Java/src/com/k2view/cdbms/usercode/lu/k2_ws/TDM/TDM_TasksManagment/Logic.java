package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_TasksManagment;

import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.existAnotherMapping;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getAdminTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getFavoriteThenByDateComparator;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getOwnerTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getTesterTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.isAllowedToCreate;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.isTaskCreator;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.taskGroupExist;
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
				         t.task_last_updated_date
				     FROM
				         """
				+ TDMDB_SCHEMA + """
						.task_group_mapping tgm
						    INNER JOIN
						        """ + TDMDB_SCHEMA + """
						.tasks t ON tgm.task_id = t.task_id
						    INNER JOIN
						        """ + TDMDB_SCHEMA + """
						.task_groups tg ON tgm.task_group_id = tg.task_group_id
						    LEFT JOIN
						        """ + TDMDB_SCHEMA + """
						.task_user_favorites tuf ON (
						            t.task_id = tuf.favorite_item_id
						            AND tuf.favorite_item_type = 'Task'
						            AND tuf.user_id = ?
						        )
						    WHERE 1=1""";
	}

	@desc("Gets relevant Tasks for assign, if the user is admin get all otherwise get the created by user tasks"
			+ " also checking for not exist mapping for the task group id")
	@webService(path = "getTasksForAssign", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"result": [
				  {
					"task_id": 5,
					"task_title": "delete"
				  },
				  {
					"task_id": 8,
					"task_title": "RESERVE ONLY"
				  },
				  {
					"task_id": 4,
					"task_title": "tables"
				  }
				],
				"errorCode": "SUCCESS",
				"message": null
			  }
				""")

	public static Object wsGetTasksForAssign(long task_group_id) throws Exception {
		Map<String, Object> response = new LinkedHashMap<>();
		String errorCode = "";
		String message = null;

		String permissionGroup = fnGetUserPermissionGroup("");

		StringBuilder sql = new StringBuilder("""
				SELECT t.task_id, t.task_title
				FROM
				 """ + TDMDB_SCHEMA + """
				.tasks t
				 WHERE NOT EXISTS (
				     SELECT 1
				     FROM
				  """ + TDMDB_SCHEMA + """
							.task_group_mapping tgm
				        WHERE tgm.task_id = t.task_id
				        AND tgm.task_group_id = ?
				    )
				    AND t.task_status = 'Active'
				""");

		if (!"admin".equalsIgnoreCase(permissionGroup)) {
			sql.append(" AND split_part(t.task_created_by, '##', 1) = ?");
		}

		try {
			List<Object> params = new ArrayList<>();
			params.add(task_group_id);

			if (!"admin".equalsIgnoreCase(permissionGroup)) {
				params.add(sessionUser().name());
			}

			Db.Rows rows = db(TDM).fetch(sql.toString(), params.toArray());

			List<Map<String, Object>> result = new ArrayList<>();
			for (Db.Row row : rows) {
				Map<String, Object> taskInfo = new LinkedHashMap<>();
				long taskID = Long.parseLong(row.get("task_id").toString());
				taskInfo.put("task_id", taskID);
				taskInfo.put("task_title", row.get("task_title"));
				result.add(taskInfo);
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
			Assign task to a task groups.
			The following parameters are mandatory:
			taskID, task_group_id
			Example of a request body:
			{"taskID": 2,"task_group_id": [1]}
						""")
	@webService(path = "assignTaskToTaskGroups", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """

			""")
	public static Object wsAssignTaskToTaskGroups(
			@param(description = "Task id.", required = true) long taskID,
			@param(description = "Task group ids.", required = true) List<Long> taskGroupIds,
			@param(description = "reAssign, if true first delete the current mapping for the task (relevant for update task).", required = true) boolean reAssign)
			throws Exception {
		Map<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		if (reAssign) {
			String sql = "DELETE FROM " + TDMDB_SCHEMA + ".task_group_mapping WHERE task_id = " + taskID;
			db(TDM).execute(sql);
		}

		String username = sessionUser().name();
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());

		String insertSQL = "INSERT INTO " + TDMDB_SCHEMA + ".task_group_mapping" +
				"(task_id, task_group_id, creation_date, created_by) " +
				"VALUES (?, ?, ?, ?)";
		try {
			for (long task_group_id : taskGroupIds) {
				if (mappingAlreadyExist(task_group_id, taskID)) {
					continue;
				}
				if (!taskGroupExist(task_group_id)) {
					return wrapWebServiceResults("FAILED", "task group id does not exist", null);
				}
				db(TDM).execute(insertSQL, taskID, task_group_id, now, username);
			}

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
		String deleteSQL = "DELETE FROM " + TDMDB_SCHEMA + ".task_group_mapping" +
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
	/// TODO: remove after testing

	// @desc("Gets ALL Tasks for the task group")
	// @webService(path = "getTasksPerTaskGroup", verb = {
	// MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false,
	// produce = { Produce.XML,
	// Produce.JSON })
	// @resultMetaData(mediaType = Produce.JSON, example = """
	// {
	// "result": [
	// {
	// "task_id": 1,
	// "task_title": "dsf",
	// "isPermittedUser": true,
	// "favorite": true
	// }
	// ],
	// "errorCode": "SUCCESS",
	// "message": null
	// }
	// """)

	// public static Object wsGetTasksPerTaskGroup(long task_group_id, boolean
	// createdByUser) throws Exception {
	// Map<String, Object> response = new LinkedHashMap<>();
	// String errorCode = "";
	// String message = null;

	// StringBuilder sql = new StringBuilder(
	// baseQuery()
	// + " AND tgm.task_id = t.task_id AND tgm.task_group_id = ?");

	// if (createdByUser) {
	// sql.append(" AND split_part(t.task_created_by, '##', 1) = '" +
	// sessionUser().name() + "'");
	// }

	// try {
	// Db.Rows rows = db(TDM).fetch(sql.toString(), sessionUser().name(),
	// task_group_id);

	// List<Map<String, Object>> result = new ArrayList<>();
	// for (Db.Row row : rows) {
	// Map<String, Object> taskTemplate = new LinkedHashMap<>();
	// long taskID = Long.parseLong(row.get("task_id").toString());
	// taskTemplate.put("task_id", taskID);
	// taskTemplate.put("task_title", row.get("task_title"));
	// taskTemplate.put("isPermittedUser", isAllowedToCreate(sessionUser().name(),
	// taskID));
	// taskTemplate.put("isPermittedUserForTask",
	// isTaskCreator(sessionUser().name(), taskID));
	// taskTemplate.put("favorite", row.get("favorite"));
	// taskTemplate.put("display_task_type", row.get("task_type_derived"));
	// result.add(taskTemplate);
	// }

	// result.sort(getFavoriteThenByKeyComparator("task_title"));

	// errorCode = "SUCCESS";
	// response.put("result", result);
	// if (rows != null) {
	// rows.close();
	// }
	// } catch (Exception e) {
	// errorCode = "FAILED";
	// message = e.getMessage();
	// log.error(message);
	// }

	// response.put("errorCode", errorCode);
	// response.put("message", message);
	// return response;
	// }

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
				wsAssignTaskToTaskGroups(taskID, toTaskGroups, false);

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

	private static boolean mappingAlreadyExist(long toTaskGroup, long taskID) throws SQLException {
		String sql = "SELECT 1 "
				+ "FROM " + TDMDB_SCHEMA + ".task_group_mapping tgm "
				+ "WHERE " + " tgm.task_group_id = ? AND tgm.task_id = ?";

		Db.Row row = db(TDM).fetch(sql, toTaskGroup, taskID).firstRow();
		return !row.isEmpty();
	}

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
		String insertSQL = "INSERT INTO " + TDMDB_SCHEMA + ".task_user_favorites" +
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
		String sql = "DELETE FROM " + TDMDB_SCHEMA + ".task_user_favorites" +
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
			  "dataType": "entities"
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
			String creator, Boolean isScheduled, ProcessedData dataType)
			throws Exception {
		Map<String, Object> response = new LinkedHashMap<>();
		String errorCode = "";
		String message = null;
		String sql = buildSearchQuery(text, taskTypes, sourceEnvironmentIds, targetEnvironmentIds,
				beIds, selectionMethods, creator, isScheduled, dataType);
		Object[] params = createParams(text, taskTypes, sourceEnvironmentIds, targetEnvironmentIds,
				beIds, selectionMethods, creator);
		try {
			// Pass 1: collect search candidates.
			// A task may be mapped to more than one group, so the join can return the same
			// task_id on multiple rows. We keep task info once per task_id but record every
			// (taskId, groupId) assignment so multi-group tasks appear under each group.
			Map<Long, Map<String, Object>> taskIdToInfo = new LinkedHashMap<>();
			List<long[]> taskGroupAssignments = new ArrayList<>(); // [taskId, groupId] per row
			Map<Long, String> groupIdToName = new HashMap<>();
			Set<Long> uniqueGroupIds = new HashSet<>();
			String userName = sessionUser().name();

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
					return taskInfo;
				});
			}

			// Permission resolution: call the same functions as wsGetTasksPerTaskGroup,
			// once per unique group, and store the full task data per group so we can
			// enrich search results with hold_task, can_edit_task, can_create_task.
			String permissionGroup = fnGetUserPermissionGroup(userName);
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
			Long[] beIds, SelectionMethodFilter[] selectionMethods, String creator) {
		List<Object> params = new LinkedList<>();
		params.add(sessionUser().name()); // favorites LEFT JOIN

		if (text != null && !text.isEmpty()) {
			params.addAll(Collections.nCopies(4, text));
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
		// Task type params last: they bind to the outer subquery WHERE, after all inner query params
		if (taskTypes != null && taskTypes.length > 0) {
			for (DisplayTaskType t : taskTypes)
				params.add(t.getDisplayValue());
		}
		return params.toArray();
	}

	private static String buildSearchQuery(String text, DisplayTaskType[] taskTypes,
			Long[] sourceEnvironmentIds, Long[] targetEnvironmentIds,
			Long[] beIds, SelectionMethodFilter[] selectionMethods,
			String creator, Boolean isScheduled, ProcessedData dataType) {
		StringBuilder sqlBuilder = new StringBuilder(baseQuery());

		if (text != null && !text.isEmpty()) {
			sqlBuilder.append("""
						AND (t.task_title ILIKE '%%' || ? || '%%' OR
							 t.task_description ILIKE '%%' || ? || '%%' OR
							 tg.task_group_name ILIKE '%%' || ? || '%%' OR
							 tg.task_group_desc ILIKE '%%' || ? || '%%')
					""");
		}
		// task_type_filter applied via subquery wrapper below — not inlined here
		if (sourceEnvironmentIds != null && sourceEnvironmentIds.length > 0) {
			sqlBuilder.append(" AND t.source_environment_id IN (")
					.append(generatePlaceholders(sourceEnvironmentIds.length)).append(")");
		}
		if (targetEnvironmentIds != null &&
				targetEnvironmentIds.length > 0) {
			sqlBuilder.append(" AND t.environment_id IN (")
					.append(generatePlaceholders(targetEnvironmentIds.length)).append(")");
		}
		if (beIds != null && beIds.length > 0) {
			sqlBuilder.append(" AND t.be_id IN (")
					.append(generatePlaceholders(beIds.length)).append(")");
		}
		if (selectionMethods != null && selectionMethods.length > 0) {
			sqlBuilder.append(
					" AND (CASE" +
							" WHEN t.selection_method = 'ALL' THEN 'Predefined entity list'" +
							" WHEN t.selection_method = 'C' THEN 'Custom logic'" +
							" WHEN t.selection_method = 'L' THEN 'Entity list'" +
							" WHEN t.selection_method IN ('P', 'PR') THEN 'Business parameters'" +
							" WHEN t.selection_method = 'GENERATE_SUBSET' THEN 'Load pre-generated subset'" +
							" WHEN t.selection_method = 'R' THEN 'Random list'" +
							" WHEN t.selection_method = 'GENERATE' THEN 'Synthetic generation'" +
							" ELSE t.selection_method END) IN (" + generatePlaceholders(selectionMethods.length) + ")");
		}

		if (creator != null && !creator.isEmpty()) {
			sqlBuilder.append(" AND t.task_created_by ILIKE ?");
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
		return "select 1 from " + TDMDB_SCHEMA + ".task_ref_tables trt where trt.task_id = t.task_id";
	}

	private static String generatePlaceholders(int count) {
		return String.join(",", Collections.nCopies(count, "?"));
	}

	@webService(path = "getTasksPerTaskGroup", verb = { MethodType.GET }, version = "1", produce = {
			Produce.JSON }, elevatedPermission = true)
	@desc("Returns grouped tasks available for the user based on their permission group (admin, owner, tester).")
	public static Object wsGetTasksPerTaskGroup(long task_group_id) throws Exception {
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
			return wrapWebServiceResults("FAILED", e.getMessage(), null);
		}
	}

	@desc("""
			insert a new entries for task_exe_permissions
			permissions can be populated by Me,My user group,ALL, fabric role, or user ID
			""")
	@webService(path = "taskExecutionPermission/insertTaskExecution", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"errorCode": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsInsertTaskExecution(
			@param(description = "Task id", required = true) long task_id,
			@param(description = "List of permissions. Each item can be a User ID, Fabric Role, or one of the special keywords: ALL, ME, or MY_GROUP.", required = true) List<Map<String, String>> permissions)
			throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		String insertSQL = "INSERT INTO " + TDMDB_SCHEMA + ".task_exe_permissions" +
				"(task_id, permitted_user, user_type, creation_date, created_by) " +
				"VALUES (?, ?, ?, ?, ?)";

		String username = sessionUser().name();
		String roles = fnGetUserRoles(username);
		String[] currentUserRoles = roles != null ? roles.split(TDM_PARAMETERS_SEPARATOR) : new String[0];
		String createdBy = new StringBuilder().append(username).append("##").append(roles).toString();
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
				.format(Instant.now());

		try {
			List<Map<String, String>> validPermissions = new ArrayList<>();
			if (permissions != null) {
				for (Map<String, String> perm : permissions) {
					if (perm == null)
						continue;
					String type = perm.getOrDefault("type", "").trim().toUpperCase();
					String value = perm.getOrDefault("value", "").trim();
					if (!type.isEmpty() && !value.isEmpty()) {
						validPermissions.add(Map.of("type", type, "value", value));
					}
				}
			}

			boolean hasAll = validPermissions.stream()
					.anyMatch(p -> "ID".equals(p.get("type")) && "ALL".equalsIgnoreCase(p.get("value")));

			if (hasAll) {
				db(TDM).execute(insertSQL, task_id, "ALL", "ID", now, createdBy);
			} else {
				Set<String> inserted = new HashSet<>();
				for (Map<String, String> perm : validPermissions) {
					String type = perm.get("type");
					String value = perm.get("value");

					if ("ID".equals(type)) {
						String resolvedValue = "ME".equalsIgnoreCase(value) ? username : value;
						if (inserted.add(resolvedValue)) {
							db(TDM).execute(insertSQL, task_id, resolvedValue, "ID", now, createdBy);
						}
					} else if ("GROUP".equals(type)) {
						if ("MY_GROUP".equalsIgnoreCase(value)) {
							for (String role : currentUserRoles) {
								if (inserted.add(role)) {
									db(TDM).execute(insertSQL, task_id, role, "GROUP", now, createdBy);
								}
							}
						} else {
							if (inserted.add(value)) {
								db(TDM).execute(insertSQL, task_id, value, "GROUP", now, createdBy);
							}
						}
					}
				}
			}

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
			update task_exe_permissions by first delete and then
				insert a new entries for task_exe_permissions
				permissions can be populated by me,My user group,ALL, fabric role, or user ID
				""")
	@webService(path = "taskExecutionPermission/updateTaskExecution", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"errorCode": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsUpdateTaskExecution(
			@param(description = "Task id", required = true) long task_id,
			@param(description = "List of permissions. Each item can be a User ID, Fabric Role, or one of the special keywords: ALL, ME, or MY_GROUP.", required = true) List<Map<String, String>> permissions)
			throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		String deleteSQL = "DELETE FROM " + TDMDB_SCHEMA + ".task_exe_permissions WHERE task_id = ?";

		try {
			// Delete existing permissions for the task
			db(TDM).execute(deleteSQL, task_id);

			wsInsertTaskExecution(task_id, permissions);

			errorCode = "SUCCESS";
			message = "Task execution permission updated successfully.";

		} catch (Exception e) {
			errorCode = "FAILED";
			message = "Error updating task execution permission: " + e.getMessage();
			log.error(message);
		}
		response.put("errorCode", errorCode);
		response.put("message", message);
		return response;
	}

	@desc("Gets execution permission for task id")
	@webService(path = "taskExecutionPermission/getTaskExecution", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"message": null,
				"permittedUsers": [
				  {
					"permission_type": "Me (creator)",
					"permitted_user": "ziv.genat@k2view.com"
				  },
				  {
					"permission_type": "My user group",
					"permitted_user": "k2view_k2v_user"
				  }
				],
				"status": "SUCCESS"
			  }
				""")
	public static Map<String, Object> wsLoadTaskExecution(@param(required = true) long task_id) throws Exception {
		Map<String, Object> response = new HashMap<>();
		String message = null;
		String status = "SUCCESS";
		List<Map<String, String>> permittedUsers = new ArrayList<>(); // List of Map

		String selectSQL = "SELECT permitted_user FROM " + TDMDB_SCHEMA + ".task_exe_permissions WHERE task_id = ?";

		try {
			Rows results = db(TDM).fetch(selectSQL, task_id);
			if (results != null) {
				for (Map<String, Object> row : results) {
					String permittedUser = (String) row.get("permitted_user");
					String permissionType = determinePermissionType(permittedUser); // Determine the type
					Map<String, String> userMap = new HashMap<>();
					userMap.put("permitted_user", permittedUser);
					userMap.put("permission_type", permissionType);
					permittedUsers.add(userMap);
				}
			}

		} catch (Exception e) {
			status = "FAILED";
			message = "An unexpected error occurred: " + e.getMessage();
			log.error(message);
		}

		response.put("status", status);
		response.put("message", message);
		response.put("permittedUsers", permittedUsers);
		return response;
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

	private static String determinePermissionType(String permittedUser) {
		if ("ALL".equalsIgnoreCase(permittedUser)) {
			return "All users";
		} else if (sessionUser().name().equals(permittedUser)) {
			return "Me (creator)";
		} else if (sessionUser().roles().contains(permittedUser)) {
			return "My user group";
		} else {
			return "Users/user groups";
		}
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
			@param(description = "Override parameters. Keys must be valid OverrideParamKey enum names (e.g. SOURCE_ENVIRONMENT_NAME, ENTITY_LIST).", required = true) Map<String, Object> overrideParameters)
			throws Exception {

		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";

		try {
			String taskCheckSql = "SELECT task_id FROM " + TDMDB_SCHEMA +
					".tasks WHERE task_id = ? AND task_status = 'Active'";
			Object exists = db(TDM).fetch(taskCheckSql, taskId).firstValue();
			if (exists == null) {
				response.put("errorCode", "FAILED");
				response.put("message", "Task " + taskId + " not found or is not active.");
				return response;
			}

			Long draftId = (Long) db(TDM)
					.fetch("SELECT nextval('" + TDMDB_SCHEMA + ".tasks_task_execution_id_seq')")
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
					validatedParams.put(key.name(), entry.getValue());
				}
			}

			String paramsJson = Json.get().toJson(validatedParams);
			String insertSql = "INSERT INTO " + TDMDB_SCHEMA +
					".task_execution_override_attrs (task_id, task_execution_id, override_parameters, created_by_ai, draft) "
					+
					"VALUES (?, ?, ?::json, true, true)";
			db(TDM).execute(insertSql, taskId, draftId, paramsJson);

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

}
