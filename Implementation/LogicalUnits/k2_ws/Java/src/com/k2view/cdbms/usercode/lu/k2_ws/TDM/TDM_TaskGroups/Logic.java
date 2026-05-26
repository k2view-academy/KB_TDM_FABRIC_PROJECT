package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_TaskGroups;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.existAnotherMapping;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getAdminTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.getFavoriteThenByKeyComparator;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.taskGroupExist;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserPermissionGroup;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_TasksManagment.Logic.wsMoveTasksToGroups;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.param;
import com.k2view.fabric.api.endpoint.Endpoint.resultMetaData;
import com.k2view.fabric.api.endpoint.Endpoint.webService;

public class Logic extends WebServiceUserCode {

	private static final String favorite_item_type = "Group";
	private static final String noPermissionError = "You don’t have permissions";
	private static final String TDM = "TDM";

	private static boolean isPermittedUser(String username, long task_group_id) {
		String permissionGroup = fnGetUserPermissionGroup("");
		String created_by;
		try {
			created_by = "" + db(TDM).fetch(
					"SELECT created_by FROM " + TDMDB_SCHEMA + ".task_groups WHERE task_group_id=?",
					task_group_id).firstValue();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ("admin".equals(permissionGroup) || username.equals(created_by)) && !"system".equals(created_by);
	}

	@desc("""
			Creates a new Task Group.
			The following parameters are mandatory:
			task_group_name, task_group_desc
			Example of a request body:
			{"task_group_name":"zivtest","task_group_desc":"zivtest description"}
						""")
	@webService(path = "taskgroup", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
					{
					  "result": {
					    "task_group_id": 5
					  },
					  "errorCode": "SUCCESS",
					  "message": null
					}
			""")
	public static Object wsCreateTaskGroup(
			@param(description = "Task group name.", required = true) String task_group_name,
			@param(description = "Task group description.", required = true) String task_group_desc)
			throws Exception {
		Map<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		try {
			HashMap<String, Object> result = new HashMap<>();
			String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());
			String username = sessionUser().name();
			String insertSQL = "INSERT INTO " + TDMDB_SCHEMA + ".task_groups" +
					"(task_group_name, task_group_desc, creation_date, created_by) " +
					"VALUES (?, ?, ?, ?) RETURNING task_group_id";
			Db.Row row = db(TDM).fetch(insertSQL, task_group_name, task_group_desc, now, username)
					.firstRow();
			long task_group_id = Long.parseLong(row.get("task_group_id").toString());
			result.put("task_group_id", task_group_id);

			// handlePermissions(task_group_id, permitted_user, now, username);

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
			Update Task group.
			The following parameters are mandatory:
			task_group_id, task_group_name, task_group_desc
			Example of a request body:
			{"task_group_name": "zivtestupdate", "task_group_desc": "zivtestupdate des"}
						""")
	@webService(path = "taskgroup/{task_group_id}", verb = {
			MethodType.PUT }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"errorCode": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsUpdateTaskGroup(
			@param(description = "Task group id.", required = true) long task_group_id,
			@param(description = "Task group name.", required = true) String task_group_name,
			@param(description = "Task group description.", required = true) String task_group_desc)
			throws Exception {
		Map<String, Object> response = new HashMap<>();
		String message = null;
		String errorCode = "";
		String username = sessionUser().name();

		if (!isPermittedUser(username, task_group_id)) {
			return wrapWebServiceResults("FAILED", noPermissionError, null);
		}
		if (!taskGroupExist(task_group_id)) {
			return wrapWebServiceResults("FAILED", "task group id does not exist", null);
		}

		try {

			String updateSQL = "UPDATE " + TDMDB_SCHEMA + ".task_groups SET " +
					"task_group_name = ?, task_group_desc = ?" + " WHERE task_group_id = ?";
			db(TDM).execute(updateSQL, task_group_name, task_group_desc, task_group_id);

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

	@desc("Gets ALL task groups for the connected user")
	@webService(path = "taskgroup", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
						{
			  "allTaskGroups": [
			    {
			      "task_group_id": 6,
			      "task_group_name": "sivangr3",
			      "task_group_desc": "sivan group3 favorite",
			      "created_by": "sivan",
			      "isPermittedUser": true,
			      "favorite": true,
			      "has_task_created_by_the_user": false
			    },
			    {
			      "task_group_id": 1,
			      "task_group_name": "General",
			      "task_group_desc": "General",
			      "created_by": "system",
			      "isPermittedUser": false,
			      "favorite": true,
			      "has_task_created_by_the_user": true
			    },
			    {
			      "task_group_id": 4,
			      "task_group_name": "tali1",
			      "task_group_desc": "tali1desc1",
			      "created_by": "tali",
			      "isPermittedUser": true,
			      "favorite": false,
			      "has_task_created_by_the_user": false
			    },
			    {
			      "task_group_id": 5,
			      "task_group_name": "sivangr1",
			      "task_group_desc": "sivan group1",
			      "created_by": "sivan",
			      "isPermittedUser": true,
			      "favorite": false,
			      "has_task_created_by_the_user": false
			    },
			    {
			      "task_group_id": 3,
			      "task_group_name": "zivtest1",
			      "task_group_desc": "zivtest desc1",
			      "created_by": "ziv.genat@k2view.com",
			      "isPermittedUser": true,
			      "favorite": false,
			      "has_task_created_by_the_user": false
			    },
			    {
			      "task_group_id": 2,
			      "task_group_name": "zivtest",
			      "task_group_desc": "zivtest desc",
			      "created_by": "ziv.genat@k2view.com",
			      "isPermittedUser": true,
			      "favorite": false,
			      "has_task_created_by_the_user": false
			    }
			  ],
			  "myTaskGroups": [
			    {
			      "task_group_id": 1,
			      "task_group_name": "General",
			      "task_group_desc": "General",
			      "created_by": "system",
			      "isPermittedUser": false,
			      "favorite": true,
			      "has_task_created_by_the_user": true
			    },
			    {
			      "task_group_id": 3,
			      "task_group_name": "zivtest1",
			      "task_group_desc": "zivtest desc1",
			      "created_by": "ziv.genat@k2view.com",
			      "isPermittedUser": true,
			      "favorite": false,
			      "has_task_created_by_the_user": false
			    },
			    {
			      "task_group_id": 2,
			      "task_group_name": "zivtest",
			      "task_group_desc": "zivtest desc",
			      "created_by": "ziv.genat@k2view.com",
			      "isPermittedUser": true,
			      "favorite": false,
			      "has_task_created_by_the_user": false
			    }
			  ],
			  "favoritesTaskGroups": [
			    {
			      "task_group_id": 1,
			      "task_group_name": "General",
			      "task_group_desc": "General",
			      "created_by": "system",
			      "isPermittedUser": false,
			      "favorite": true,
			      "has_task_created_by_the_user": true
			    },
			    {
			      "task_group_id": 6,
			      "task_group_name": "sivangr3",
			      "task_group_desc": "sivan group3 favorite",
			      "created_by": "sivan",
			      "isPermittedUser": true,
			      "favorite": true,
			      "has_task_created_by_the_user": false
			    }
			  ],
			  "errorCode": "SUCCESS",
			  "message": null
			}
						""")
	public static Object wsGetTaskGroups() throws Exception {
		Map<String, Object> response = new LinkedHashMap<>();
		String errorCode = "";
		String message = null;
		String userName = sessionUser().name();

		try {
			String sql = "SELECT tg.* , CASE WHEN tuf.favorite_item_id IS NULL THEN FALSE ELSE TRUE END AS favorite, "
					+ "EXISTS ( SELECT 1 FROM " + TDMDB_SCHEMA + ".tasks t INNER JOIN " + TDMDB_SCHEMA
					+ ".task_group_mapping tgm ON t.task_id = tgm.task_id"
					+ " WHERE tgm.task_group_id = tg.task_group_id AND split_part(t.task_created_by, '##', 1) = ? ) AS has_task_created_by_the_user"
					+ " FROM " + TDMDB_SCHEMA + ".task_groups tg "
					+ "LEFT JOIN " + TDMDB_SCHEMA
					+ ".task_user_favorites tuf ON (tg.task_group_id = tuf.favorite_item_id AND tuf.favorite_item_type ='"
					+ favorite_item_type + "'" + "AND tuf.user_id = ?)";

			Db.Rows rows = db(TDM).fetch(sql, userName, userName);

			Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
			List<Map<String, Object>> allTaskGroups = new ArrayList<>();
			List<Map<String, Object>> myTaskGroups = new ArrayList<>();
			List<Map<String, Object>> favoritesTaskGroups = new ArrayList<>();

			for (Db.Row row : rows) {
				Map<String, Object> taskGroup = new LinkedHashMap<>();
				long task_group_id = Long.parseLong(row.get("task_group_id").toString());
				taskGroup.put("task_group_id", task_group_id);
				taskGroup.put("task_group_name", row.get("task_group_name"));
				taskGroup.put("task_group_desc", row.get("task_group_desc"));
				taskGroup.put("created_by", row.get("created_by"));
				taskGroup.put("isPermittedUser", isPermittedUser(sessionUser().name(), task_group_id));
				taskGroup.put("favorite", row.get("favorite"));
				taskGroup.put("has_task_created_by_the_user", row.get("has_task_created_by_the_user"));

				allTaskGroups.add(taskGroup);

				if ((boolean) taskGroup.get("favorite")) {
					favoritesTaskGroups.add(taskGroup);
				}
				if (taskGroup.get("created_by").equals(userName)
						|| (boolean) taskGroup.get("has_task_created_by_the_user")) {
					myTaskGroups.add(taskGroup);
				}
			}

			allTaskGroups.sort(getFavoriteThenByKeyComparator("task_group_name"));
			myTaskGroups.sort(getFavoriteThenByKeyComparator("task_group_name"));
			favoritesTaskGroups.sort(getFavoriteThenByKeyComparator("task_group_name"));

			result.put("allTaskGroups", allTaskGroups);
			result.put("myTaskGroups", myTaskGroups);
			result.put("favoritesTaskGroups", favoritesTaskGroups);

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
			Delete task group
			Example of a request body:
			{
				"task_group_id": 4
			}
						""")
	@webService(path = "taskgroup", verb = {
			MethodType.DELETE }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"status": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsDeleteTaskGroup(
			@param(description = "Task group id", required = true) long task_group_id)
			throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String status = "";
		String username = sessionUser().name();

		if (!isPermittedUser(username, task_group_id)) {
			return wrapWebServiceResults("FAILED", noPermissionError, null);
		}

		try {
			String deleteFavoritesSQL = "DELETE FROM " + TDMDB_SCHEMA + ".task_user_favorites" +
					" WHERE favorite_item_type = ? AND favorite_item_id = ?";

			db(TDM).execute(deleteFavoritesSQL, favorite_item_type, task_group_id);

			handleTasksGroupMapping(task_group_id);
			String deleteSQL = "DELETE FROM " + TDMDB_SCHEMA + ".task_groups" +
					" WHERE task_group_id = ?";

			db(TDM).execute(deleteSQL, task_group_id);

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

	private static void handleTasksGroupMapping(long task_group_id) throws Exception {
		Set<Map<String, Object>> result = getAdminTasks(task_group_id);
		List<Long> taskIdsToGeneral = new ArrayList<>();

		for (Map<String, Object> taskTemplate : result) {
			long taskID = ((Number) taskTemplate.get("task_id")).longValue();

			if (!existAnotherMapping(taskID, task_group_id)) {
				taskIdsToGeneral.add(taskID);
			}
		}

		if (!taskIdsToGeneral.isEmpty()) {
			wsMoveTasksToGroups(taskIdsToGeneral, task_group_id, Arrays.asList(1L), false);
		}

		String deleteMappingSQL = "DELETE FROM " + TDMDB_SCHEMA + ".task_group_mapping WHERE task_group_id = ?";
		db(TDM).execute(deleteMappingSQL, task_group_id);
	}

	@desc("""
			Mark task group as favorite
			Example of a request body:
			{
				"task_group_id": 4
			}
						""")
	@webService(path = "markFavorite", verb = {
			MethodType.POST }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"status": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsMarkFavorite(
			@param(description = "Task group id", required = true) long task_group_id)
			throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String status = "";

		if (!taskGroupExist(task_group_id)) {
			return wrapWebServiceResults("FAILED", "task group id does not exist", null);
		}

		try {
			String insertSQL = "INSERT INTO " + TDMDB_SCHEMA + ".task_user_favorites" +
					"(user_id, favorite_item_type, favorite_item_id) " +
					"VALUES (?, ?, ?)";
			String username = sessionUser().name();
			db(TDM).execute(insertSQL, username, favorite_item_type, task_group_id);

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
			Unmark task group as favorite
			Example of a request body:
			{
				"task_group_id": 4
			}
						""")
	@webService(path = "unMarkFavorite", verb = {
			MethodType.DELETE }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
			{
				"status": "SUCCESS",
				"message": null
			}
				""")
	public static Object wsUnMarkFavorite(
			@param(description = "Task group id", required = true) long task_group_id)
			throws Exception {
		HashMap<String, Object> response = new HashMap<>();
		String message = null;
		String status = "";

		try {
			String sql = "DELETE FROM " + TDMDB_SCHEMA + ".task_user_favorites" +
					" WHERE user_id= ? AND favorite_item_type = ? AND favorite_item_id = ?";
			String username = sessionUser().name();
			db(TDM).execute(sql, username, favorite_item_type, task_group_id);

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

}
