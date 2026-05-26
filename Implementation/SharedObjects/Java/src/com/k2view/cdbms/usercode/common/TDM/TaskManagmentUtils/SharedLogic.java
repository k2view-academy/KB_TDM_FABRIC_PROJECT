package com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils;

import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.shared.user.UserCode.isFirstSync;
import static com.k2view.cdbms.shared.user.UserCode.log;
import static com.k2view.cdbms.shared.user.UserCode.sessionUser;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserEnvs;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserPermissionGroup;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserRoles;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnIsOwner;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.k2view.cdbms.shared.Db;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;

@SuppressWarnings("unchecked")
public class SharedLogic {

	private static final String TDM = "TDM";

	public enum ProcessedData {
		entities, tables, both
	}

	public enum DisplayTaskType {
		IN_PLACE_MASKING("In-place masking"),
		EXTRACT("Extract"),
		EXTRACT_AND_LOAD("Extract & Load"),
		LOAD_AND_RESERVE("Load & Reserve"),
		DELETE_AND_LOAD_AND_RESERVE("Delete & Load & Reserve"),
		DELETE_AND_LOAD("Delete & Load"),
		LOAD("Load"),
		DELETE("Delete"),
		RESERVE("Reserve"),
		AI_TRAINING("AI training"),
		RULE_BASED_SDG("Rule-based SDG"),
		AI_BASED_SDG("AI-based SDG"),
		RULE_BASED_GENERATE_AND_LOAD("Rule-based generate & load"),
		AI_BASED_GENERATE_AND_LOAD("AI-based generate & load"),
		LOAD_RULE_BASED_GENERATED_ENTITIES("Load rule-based generated entities"),
		LOAD_AI_BASED_GENERATED_ENTITIES("Load AI-based generated entities");

		private final String displayValue;
		DisplayTaskType(String displayValue) { this.displayValue = displayValue; }
		public String getDisplayValue() { return displayValue; }
	}

	public enum SelectionMethodFilter {
		PREDEFINED_ENTITY_LIST("Predefined entity list"),
		CUSTOM_LOGIC("Custom logic"),
		ENTITY_LIST("Entity list"),
		BUSINESS_PARAMETERS("Business parameters"),
		LOAD_PRE_GENERATED_SUBSET("Load pre-generated subset"),
		RANDOM_LIST("Random list"),
		SYNTHETIC_GENERATION("Synthetic generation");

		private final String displayValue;
		SelectionMethodFilter(String displayValue) { this.displayValue = displayValue; }
		public String getDisplayValue() { return displayValue; }
	}

	private static String getDisplayedTaskType(String taskType, String syncMode, String selectionMethod) {
		selectionMethod = selectionMethod.trim().toUpperCase();
		if(selectionMethod.contains("GENERATE")){
			return "Synthetic generation";
		}
		switch (taskType.toUpperCase()) {
			case "LOAD":
				return "OFF".equalsIgnoreCase(syncMode) ? "Load" : "Extract and load";
			case "EXTRACT":
				return "Extract";
			case "DELETE":
				return "Delete";
			case "RESERVE":
				return "Reserve";
			default:
				return "Synthetic generation";
		}
	}

	private static Map<String, Object> buildTaskInfo(Long taskId, String taskTitle, String taskType, String selectionMethod, String syncMode,
			Object holdTask, boolean editSourceEnv, boolean editTargetEnv, Object favorite, boolean isPermittedUser, String userName, String task_last_updated_date) {

		Map<String, Object> taskInfo = new HashMap<>();
		taskInfo.put("task_id", taskId);
		taskInfo.put("task_title", taskTitle);
		taskInfo.put("task_last_updated_date", task_last_updated_date);
		//taskInfo.put("edit_source_env", editSourceEnv);
		//taskInfo.put("edit_target_env", editTargetEnv);
		taskInfo.put("hold_task", holdTask);
		taskInfo.put("display_task_type", getDisplayedTaskType(taskType, syncMode,selectionMethod));
		taskInfo.put("favorite", favorite);
		taskInfo.put("can_edit_task", isTaskCreator(userName,taskId));
		taskInfo.put("can_create_task", isAllowedToCreate(userName));
		//taskInfo.put("is_permitted_for_execution", isPermittedUserForExecution(taskId));
		return taskInfo;
	}

	public static boolean existAnotherMapping(long taskID, long task_group_id) throws SQLException {
		// check if there is mapping to a different group
		String sql = "SELECT 1 "
				+ "FROM " + TDMDB_SCHEMA + ".task_group_mapping tgm "
				+ "WHERE " + " tgm.task_group_id <> ? AND tgm.task_id = ?";

		Db.Row row = db(TDM).fetch(sql, task_group_id, taskID).firstRow();
		return !row.isEmpty();
	}

	public static boolean taskGroupCreatedByuser(long task_group_id, String username) throws SQLException {
		String sql = "SELECT 1 "
				+ "FROM " + TDMDB_SCHEMA + ".task_groups tg "
				+ "WHERE " + " tg.task_group_id = ? AND tg.created_by = ?";

		Db.Row row = db(TDM).fetch(sql, task_group_id, username).firstRow();
		return !row.isEmpty();
	}

	public static boolean taskGroupExist(long task_group_id) throws SQLException {
		String sql = "SELECT 1 "
				+ "FROM " + TDMDB_SCHEMA + ".task_groups tg "
				+ "WHERE " + " tg." + "task_group_id = ?";

		Db.Row row = db(TDM).fetch(sql, task_group_id).firstRow();
		return !row.isEmpty();
	}

	public static Comparator<Map<String, Object>> getFavoriteThenByKeyComparator(String secondarySortKey) {
		return Comparator
				.comparing((Map<String, Object> tg) -> (boolean) tg.get("favorite"), Comparator.reverseOrder())
				.thenComparing(tg -> (String) tg.get(secondarySortKey), String.CASE_INSENSITIVE_ORDER);
	}
	
	public static Comparator<Map<String, Object>> getFavoriteThenByDateComparator(String secondarySortKey) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendPattern("yyyy-MM-dd HH:mm:ss")
				.optionalStart()
				.appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
				.optionalEnd()
				.toFormatter();

		return Comparator
				.comparing((Map<String, Object> task) ->
						Boolean.TRUE.equals(task.get("favorite")), Comparator.reverseOrder())
				.thenComparing(task -> {
					Object val = task.get(secondarySortKey);

					if (val == null) {
						return null;
					}

					if (val instanceof java.sql.Timestamp) {
						return ((java.sql.Timestamp) val).toLocalDateTime();
					}

					return LocalDateTime.parse(val.toString(), formatter);
				}, Comparator.nullsLast(Comparator.reverseOrder()));
	}

	/**
	 * Permession Handling
	 **/

	

	private enum EnvType {
		SOURCE,
		TARGET
	}

	/**
	 * Alternative environment matching the task and user permissions.
	 **/
	private static class AlternativeEnv {
		String environmentId;
		String environmentName;

		AlternativeEnv(String environmentId, String environmentName) {
			this.environmentId = environmentId;
			this.environmentName = environmentName;
		}
	}

	/**
	 * Holds the evaluation result of a task's availability for execution,
	 * based on environment status, editability, and user permissions.
	 */
	private static class TaskAvailabilityDecision {
		boolean sourceEnvDisabled;
		boolean targetEnvDisabled;

		boolean sourceEnvEditable;
		boolean targetEnvEditable;
		boolean beEditable;

		boolean alternativeSourceEnvExists;
		boolean alternativeTargetEnvExists;

		boolean holdTask;

		boolean removeSourceDefaultEnv;
		boolean removeTargetDefaultEnv;

		AlternativeEnv alternativeSourceEnv;
		AlternativeEnv alternativeTargetEnv;
	}

	/**
	 * Represents the permission set of a tester for a specific environment.
	 */
	private static class TesterPermissions {

		boolean allowedDeleteBeforeLoad;
		boolean allowedCreationOfSyntheticData;
		boolean allowedRandomEntitySelection;
		boolean allowedRequestOfFreshData;
		boolean allowedTaskScheduling;

		long allowedNumberOfEntitiesToCopy;
		long allowedNumberOfEntitiesToRead;
		long allowedNumberOfReservedEntities;

		boolean allowedRefreshReferenceData;
		boolean allowedReplaceSequences;

		boolean allowRead;
		boolean allowWrite;

		boolean allowedEntityVersioning;
	}

	/**
	 * try to remove the join of the favorite to only add it when retriving the
	 * tasks of the current user
	 * pluse add the group id in the conditions and task status is active
	 */
	private static StringBuilder baseQuery() {
		StringBuilder sql = new StringBuilder(600);
		sql.append("SELECT t.task_id, t.task_title, t.task_type, t.selection_method, t.sync_mode, t.task_execution_status,t.task_last_updated_date, ")
				.append("t.task_override_fields, t.be_id, t.source_env_name, t.source_environment_id, t.reserve_ind, ")
				.append("t.environment_id AS target_environment_id, t.num_of_entities, t.env_name AS target_env_name, ")
				.append("CASE WHEN tuf.favorite_item_id IS NULL THEN FALSE ELSE TRUE END AS favorite ")
				.append("FROM ")
				.append(TDMDB_SCHEMA)
				.append(".tasks t ")
				.append("INNER JOIN ")
				.append(TDMDB_SCHEMA)
				.append(".task_group_mapping tgm ON tgm.task_id = t.task_id ")
				.append("LEFT JOIN ")
				.append(TDMDB_SCHEMA)
				.append(".task_user_favorites tuf ON tuf.favorite_item_id = t.task_id ")
				.append("AND tuf.favorite_item_type = 'Task' ")
				.append("AND tuf.user_id = ? ")
				.append("WHERE lower(t.task_status) = 'active' ")
				.append("AND tgm.task_group_id = ? ");
		return sql;
	}

	private static StringBuilder createdByUser() {
		StringBuilder sql = baseQuery();
		sql.append("AND split_part(t.task_created_by, '##', 1) = ? ");
		return sql;
	}

	private static StringBuilder notCreatedByUser() {
		StringBuilder sql = baseQuery();
		sql.append("AND split_part(t.task_created_by, '##', 1) <> ? ");
		return sql;
	}

	public static boolean sourceEnvSystemsDisabled(String sourceEnvId, String sourceEnvName, String taskType, String syncMode, Long taskId, String beId) throws Exception {
		if ( !"OFF".equalsIgnoreCase(syncMode) && !"RESERVE".equalsIgnoreCase(taskType)) {
			return fnAreAllRootLuSystemsDisabled(sourceEnvId, sourceEnvName, taskId, beId) || fnAreAllLuSystemsDisabled(sourceEnvId, sourceEnvName, taskId, beId);
		}
		return false;

	}

	public static boolean targetEnvSystemsDisabled(String targetEnvId, String targetEnvName, String taskType, String syncMode, Long taskId, String beId) throws Exception {
		if("LOAD".equalsIgnoreCase(taskType) || "DELETE".equalsIgnoreCase(taskType)) {
			return fnAreAllRootLuSystemsDisabled(targetEnvId, targetEnvName, taskId, beId) || fnAreAllLuSystemsDisabled(targetEnvId, targetEnvName, taskId, beId);
			}
			return false;
	}

	private static boolean isSourceEnvEditable(String overrideParams) {
		return isEditable(overrideParams, "source_environment");
	}

	private static boolean isTargetEnvEditable(String overrideParams) {
		return isEditable(overrideParams, "target_environment");
	}

	private static boolean isBeEditable(String overrideParams) {
		return isEditable(overrideParams, "business_entity");
	}

	private static boolean isMaxEntitiesEditable(String overrideParams, Long numOfEntites) {
		if (numOfEntites == null || numOfEntites < 0) {
			return false;
		}

		return isNestedEditable(overrideParams, "selection_method", "max_entities");
	}
	
	private static boolean isNestedEditable(String overrideParams, String parentName, String childName) {
		try {
			Map<String, Object> map = parseOverrideParams(overrideParams);
			if (map == null || !map.containsKey(parentName)) {
				return false;
			}

			Map<String, Object> parentMap = (Map<String, Object>) map.get(parentName);
			if (parentMap == null || !parentMap.containsKey(childName)) {
				return false;
			}

			Map<String, Object> childMap = (Map<String, Object>) parentMap.get(childName);
			if (childMap == null || !childMap.containsKey("is_editable")) {
				return false;
			}

			Object editable = childMap.get("is_editable");
			return editable != null && Boolean.parseBoolean(editable.toString());

		} catch (Exception e) {
			return false;
		}
	}
	
	private static boolean isEditable(String overrideParams, String objectName) {
		try {
			Map<String, Object> map = parseOverrideParams(overrideParams);
			if (map == null || !map.containsKey(objectName)) {
				return false;
			}
			Map<String, Object> objectMap = (Map<String, Object>) map.get(objectName);
			if (objectMap == null || !objectMap.containsKey("is_editable")) {
				return false;
			}
			Object editable = objectMap.get("is_editable");
			return editable != null && Boolean.parseBoolean(editable.toString());

		} catch (Exception e) {
			return false;
		}
	}
	
	private static Map<String, Object> parseOverrideParams(String overrideParams) {
		if (Util.isEmpty(overrideParams)) {
			return null;
		}

		try {
			Map<String, Object> map = Json.get().fromJson(overrideParams, Map.class);
			while (map != null) {
				if (map.containsKey("business_entity")
					|| map.containsKey("source_environment")
					|| map.containsKey("target_environment")
					|| map.containsKey("selection_method")) {
					return map;
				}
				Object value = map.get("value");
				if (value == null || !(value instanceof String)) {
					return map;
				}
				map = Json.get().fromJson((String) value, Map.class);
			}

		} catch (Exception e) {
			log.error("Failed to parse override params: " + e.getMessage());
		}
		return null;
	}
	
	private static Set<Long> getCandidateEnvironments(long taskId, long currentEnvId) {
		Set<Long> environmentIds = new HashSet<>();

		StringBuilder sql = new StringBuilder(1000);
		sql.append("WITH task_products AS ( ")
				.append("SELECT DISTINCT plu.product_id ")
				.append("FROM ").append(TDMDB_SCHEMA).append(".tasks_logical_units tlu ")
				.append("JOIN ").append(TDMDB_SCHEMA).append(".product_logical_units plu ON plu.lu_id = tlu.lu_id ")
				.append("JOIN ").append(TDMDB_SCHEMA)
				.append(".environment_products ep ON ep.product_id = plu.product_id ")
				.append("AND ep.environment_id = ? AND ep.status = 'Active' ")
				.append("WHERE tlu.task_id = ? ) ")
				.append("SELECT e.environment_id ")
				.append("FROM ").append(TDMDB_SCHEMA).append(".environments e ")
				.append("WHERE e.environment_status = 'Active' ")
				.append("AND e.environment_id NOT IN (-1,-2,?) ")
				.append("AND EXISTS (SELECT 1 FROM task_products) ")
				.append("AND NOT EXISTS ( ")
				.append("SELECT 1 FROM task_products tp ")
				.append("WHERE NOT EXISTS ( ")
				.append("SELECT 1 FROM ").append(TDMDB_SCHEMA).append(".environment_products ep ")
				.append("WHERE ep.environment_id = e.environment_id ")
				.append("AND ep.product_id = tp.product_id ")
				.append("AND ep.status = 'Active' AND ep.enable_product = true ))");

		try {
			for (Db.Row row : db(TDM).fetch(sql.toString(), currentEnvId, taskId, currentEnvId)) {
				environmentIds.add(Long.valueOf(row.get("environment_id").toString()));
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch candidate environments for taskId="
					+ taskId + ", currentEnvId=" + currentEnvId, e);
		}
		return environmentIds;
	}

	// if env is Null select systems based on BE alone
	private static Set<Long> getCandidateEnvironmentsByTask(long taskId) {
		Set<Long> environmentIds = new HashSet<>();

		StringBuilder sql = new StringBuilder(800);
		sql.append("WITH task_products AS ( ")
				.append("SELECT DISTINCT plu.product_id ")
				.append("FROM ").append(TDMDB_SCHEMA).append(".tasks_logical_units tlu ")
				.append("JOIN ").append(TDMDB_SCHEMA).append(".product_logical_units plu ON plu.lu_id = tlu.lu_id ")
				.append("WHERE tlu.task_id = ? ) ")
				.append("SELECT e.environment_id ")
				.append("FROM ").append(TDMDB_SCHEMA).append(".environments e ")
				.append("WHERE e.environment_status = 'Active' ")
				.append("AND e.environment_id NOT IN (-1,-2) ")
				.append("AND EXISTS (SELECT 1 FROM task_products) ")
				.append("AND NOT EXISTS ( ")
				.append("SELECT 1 FROM task_products tp ")
				.append("WHERE NOT EXISTS ( ")
				.append("SELECT 1 FROM ").append(TDMDB_SCHEMA).append(".environment_products ep ")
				.append("WHERE ep.environment_id = e.environment_id ")
				.append("AND ep.product_id = tp.product_id ")
				.append("AND ep.status = 'Active' AND ep.enable_product = true ))");

		try {
			for (Db.Row row : db(TDM).fetch(sql.toString(), taskId)) {
				environmentIds.add(Long.valueOf(row.get("environment_id").toString()));
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch candidate environments for taskId=" + taskId, e);
		}

		return environmentIds;
	}

	private static boolean hasEnoughEntities(long requestedEntities, long allowedEntities) {
		if (requestedEntities == -1) {
			return allowedEntities == -1;
		}
		if (allowedEntities == -1) {
			return true;
		}
		return allowedEntities >= requestedEntities;
	}

	private static long maxEntitiesLimit(long first, long second) {
		if (first == -1 || second == -1) {
			return -1;
		}
		return Math.max(first, second);
	}

	private static boolean hasEnv(String envId) {
		return envId != null && !envId.trim().isEmpty();
	}

	private static boolean requiresSourceCheck(String taskType, String syncMode) {
		String type = taskType == null ? "" : taskType.trim().toUpperCase();

		if ("EXTRACT".equals(type) || "GENERATE".equals(type) || "AI_GENERATED".equals(type)) {
			return true;
		}

		if ("LOAD".equals(type) || "TRAINING".equals(type)) {
			return !"OFF".equalsIgnoreCase(syncMode);
		}

		return false;
	}

	private static boolean requiresTargetCheck(String taskType) {
		String type = taskType == null ? "" : taskType.trim().toUpperCase();
		return "LOAD".equals(type) || "DELETE".equals(type) || "RESERVE".equals(type) || "TRAINING".equals(type);
	}

	private static TesterPermissions getTesterPermissionsForUserEnv(String userName, String envId, EnvType side,
			List<Map<String, Object>> allUserEnvsTypes) throws Exception {
		String key = side == EnvType.SOURCE ? "source environments" : "target environments";
		for (Map<String, Object> envType : allUserEnvsTypes) {
			List<Map<String, Object>> envs = (List<Map<String, Object>>) envType.get(key);
			if (envs == null) {
				continue;
			}
			for (Map<String, Object> envMap : envs) {
				if (Objects.equals(String.valueOf(envMap.get("environment_id")), envId)) {
					String roleId = String.valueOf(envMap.get("role_id"));
					return getTesterPermissions(envId, roleId);
				}
			}
		}

		return null;
	}

	private static boolean canTesterRunTaskEntities(String sourceEnvId, String targetEnvId, String taskType,
			long taskId, long requestedEntities, long allowedToRead, long allowedToWrite, long allowedToReserve,
			String syncMode, boolean reserveInd) throws Exception {

		String normalizedTaskType = taskType == null ? "" : taskType.trim().toUpperCase();
		boolean syncOff = "OFF".equalsIgnoreCase(syncMode);

		switch (normalizedTaskType) {
			case "EXTRACT":
			case "GENERATE":
			case "AI_GENERATED":
				return hasEnv(sourceEnvId) && hasEnoughEntities(requestedEntities, allowedToRead);

			case "DELETE":
				return hasEnv(targetEnvId) && hasEnoughEntities(requestedEntities, allowedToWrite);

			case "RESERVE":
				return hasEnv(targetEnvId) && hasEnoughEntities(requestedEntities, allowedToReserve);

			case "LOAD":
				if (syncOff) {
					if (reserveInd) {
						long targetLimit = maxEntitiesLimit(allowedToWrite, allowedToReserve);
						return hasEnv(targetEnvId) && hasEnoughEntities(requestedEntities, targetLimit);
					}
					return hasEnv(targetEnvId) && hasEnoughEntities(requestedEntities, allowedToWrite);
				}

				if (reserveInd) {
					long targetLimit = maxEntitiesLimit(allowedToWrite, allowedToReserve);
					return hasEnv(sourceEnvId) && hasEnv(targetEnvId)
							&& hasEnoughEntities(requestedEntities, allowedToRead)
							&& hasEnoughEntities(requestedEntities, targetLimit);
				}

				return hasEnv(sourceEnvId) && hasEnv(targetEnvId)
						&& hasEnoughEntities(requestedEntities, allowedToRead)
						&& hasEnoughEntities(requestedEntities, allowedToWrite);

			case "TRAINING":
				if (syncOff) {
					return hasEnv(targetEnvId) && hasEnoughEntities(requestedEntities, allowedToWrite);
				}
				return hasEnv(sourceEnvId) && hasEnv(targetEnvId)
						&& hasEnoughEntities(requestedEntities, allowedToRead)
						&& hasEnoughEntities(requestedEntities, allowedToWrite);

			default:
				return false;
		}
	}

	/**
	 * Finds a valid alternative environment for a task based on user permissions
	 * and system compatibility, excluding the current environment.
	 */
	private static AlternativeEnv findAlternativeEnv(String userName, String permissionGroup, EnvType side,
		String currentEnvId, String oppositeEnvId, String taskType, String syncMode, Long taskId,
		long numOfEntities, boolean reserveInd, boolean maxEntitiesEditable, boolean beEditable, String beId,
		List<Map<String, Object>> userEnvs) throws Exception {

		if (isAiOrSdgTask(taskType,currentEnvId,oppositeEnvId)) {
			return null;
		}

		Set<Long> candidateEnvIds = Collections.emptySet();
		if (!beEditable) {
			Set<Long> resolved = hasEnv(currentEnvId)
					? getCandidateEnvironments(taskId, Long.parseLong(currentEnvId))
					: getCandidateEnvironmentsByTask(taskId);
			if (resolved != null) {
				candidateEnvIds = resolved;
			}
		}

		boolean isTesterWithFixedEntities = "tester".equalsIgnoreCase(permissionGroup) && !maxEntitiesEditable;

		TesterPermissions oppositePerms = null;
		if (isTesterWithFixedEntities) {
			EnvType oppositeType = (side == EnvType.SOURCE) ? EnvType.TARGET : EnvType.SOURCE;
			oppositePerms = getTesterPermissionsForUserEnv(userName, oppositeEnvId, oppositeType, userEnvs);
		}

		String key = (side == EnvType.SOURCE) ? "source environments" : "target environments";

		for (Map<String, Object> envType : userEnvs) {
			List<Map<String, Object>> envs = (List<Map<String, Object>>) envType.get(key);
			if (envs == null) {
				continue;
			}

			for (Map<String, Object> envMap : envs) {
				String envId = String.valueOf(envMap.get("environment_id"));
				String envName = String.valueOf(envMap.get("environment_name"));

				if(Long.valueOf(envId) < 0 ){
					continue;
				}
				
				if (Objects.equals(envId, currentEnvId)) {
					continue;
				}

				if (!beEditable && !candidateEnvIds.contains(Long.valueOf(envId))) {
					continue;
				}

				boolean envDisabled = (side == EnvType.SOURCE)
						? sourceEnvSystemsDisabled(envId, envName, taskType, syncMode, taskId, beId)
						: targetEnvSystemsDisabled(envId, envName, taskType, syncMode, taskId, beId);
				if (envDisabled) {
					continue;
				}

				if (isTesterWithFixedEntities) {
					String roleId = String.valueOf(envMap.get("role_id"));
					TesterPermissions candidatePerms = getTesterPermissions(envId, roleId);

					String candidateSourceEnvId, candidateTargetEnvId;
					long allowedToRead, allowedToWrite, allowedToReserve;

					if (side == EnvType.SOURCE) {
						candidateSourceEnvId = envId;
						candidateTargetEnvId = oppositeEnvId;
						allowedToRead = candidatePerms.allowedNumberOfEntitiesToRead;
						allowedToWrite = (oppositePerms != null) ? oppositePerms.allowedNumberOfEntitiesToCopy : 0;
						allowedToReserve = (oppositePerms != null) ? oppositePerms.allowedNumberOfReservedEntities : 0;
					} else {
						candidateSourceEnvId = oppositeEnvId;
						candidateTargetEnvId = envId;
						allowedToRead = (oppositePerms != null) ? oppositePerms.allowedNumberOfEntitiesToRead : 0;
						allowedToWrite = candidatePerms.allowedNumberOfEntitiesToCopy;
						allowedToReserve = candidatePerms.allowedNumberOfReservedEntities;
					}

					if (!canTesterRunTaskEntities(candidateSourceEnvId, candidateTargetEnvId,
							taskType, taskId, numOfEntities,
							allowedToRead, allowedToWrite, allowedToReserve,
							syncMode, reserveInd)) {
						continue;
					}
				}

				return new AlternativeEnv(envId, envName);
			}
		}

		return null;
	}

	private static TesterPermissions getTesterPermissions(String envId, String roleId) {
		StringBuilder sql = new StringBuilder(400);
		sql.append("SELECT allowed_delete_before_load, allowed_creation_of_synthetic_data, ")
				.append("allowed_random_entity_selection, allowed_request_of_fresh_data, ")
				.append("allowed_task_scheduling, allowed_number_of_entities_to_copy, ")
				.append("allowed_refresh_reference_data, allowed_replace_sequences, ")
				.append("allow_read, allow_write, allowed_number_of_entities_to_read, ")
				.append("allowed_entity_versioning, ")
				.append("allowed_number_of_reserved_entities ")
				.append("FROM ").append(TDMDB_SCHEMA).append(".environment_roles ")
				.append("WHERE environment_id = ? AND role_id = ? AND role_status = ?");

		TesterPermissions permissions = new TesterPermissions();

		try (Db.Rows rows = db(TDM).fetch(sql.toString(), envId, roleId, "Active")) {
			for (Db.Row row : rows) {
				permissions.allowedDeleteBeforeLoad = Boolean
						.parseBoolean(row.get("allowed_delete_before_load").toString());
				permissions.allowedCreationOfSyntheticData = Boolean
						.parseBoolean(row.get("allowed_creation_of_synthetic_data").toString());
				permissions.allowedRandomEntitySelection = Boolean
						.parseBoolean(row.get("allowed_random_entity_selection").toString());
				permissions.allowedRequestOfFreshData = Boolean
						.parseBoolean(row.get("allowed_request_of_fresh_data").toString());
				permissions.allowedTaskScheduling = Boolean.parseBoolean(row.get("allowed_task_scheduling").toString());

				permissions.allowedNumberOfEntitiesToCopy = Long
						.parseLong(row.get("allowed_number_of_entities_to_copy").toString());
				permissions.allowedNumberOfEntitiesToRead = Long
						.parseLong(row.get("allowed_number_of_entities_to_read").toString());
				permissions.allowedNumberOfReservedEntities = Long
						.parseLong(row.get("allowed_number_of_reserved_entities").toString());

				permissions.allowedRefreshReferenceData = Boolean
						.parseBoolean(row.get("allowed_refresh_reference_data").toString());
				permissions.allowedReplaceSequences = Boolean
						.parseBoolean(row.get("allowed_replace_sequences").toString());

				permissions.allowRead = Boolean.parseBoolean(row.get("allow_read").toString());
				permissions.allowWrite = Boolean.parseBoolean(row.get("allow_write").toString());

				permissions.allowedEntityVersioning = Boolean
						.parseBoolean(row.get("allowed_entity_versioning").toString());
				break;
			}

		} catch (Exception e) {
			throw new RuntimeException("Error checking tester permissions for role " + roleId, e);
		}

		return permissions;
	}

	private static String buildTaskTypes(boolean allowSynthetic, String envId, boolean isTarget, boolean allowDelete,
			boolean allowReserve) {
		Set<String> typeConditions = new LinkedHashSet<>();
		if (isTarget) {
			typeConditions.add("'load'");
			if (allowDelete) {
				typeConditions.add("'delete'");
			}
			if (allowReserve) {
				typeConditions.add("'reserve'");
			}
		} else {
			typeConditions.add("'extract'");

		}

		if (allowSynthetic || "-1".equalsIgnoreCase(envId)) {
			typeConditions.add("'generate'");
		}

		if (allowSynthetic || "-2".equalsIgnoreCase(envId)) {
			typeConditions.add("'training'");
			typeConditions.add("'ai_generated'");
		}

		return String.join(", ", typeConditions);
	}

	/**
	 * Determines if a task can be executed based on environment status,
	 * editability, and availability of alternative environments.
	 *
	 * Marks task as hold if required environments are disabled and cannot be
	 * replaced.
	 */
	private static TaskAvailabilityDecision evaluateTaskAvailability(String userName, String permissionGroup,
			Long taskId, String taskType, String syncMode, String overrideParams,String beID, String sourceEnvId,
			String sourceEnvName, String targetEnvId, String targetEnvName, String taskExecutionStatus,
			long numOfEntities, boolean reserveInd, List<Map<String, Object>> userEnvs) throws Exception {

		TaskAvailabilityDecision decision = new TaskAvailabilityDecision();

		decision.sourceEnvDisabled = requiresSourceCheck(taskType, syncMode)
				&& sourceEnvSystemsDisabled(sourceEnvId, sourceEnvName, taskType, syncMode, taskId, beID);
		decision.targetEnvDisabled = requiresTargetCheck(taskType)
				&& targetEnvSystemsDisabled(targetEnvId, targetEnvName, taskType, syncMode, taskId, beID);

		decision.sourceEnvEditable = isSourceEnvEditable(overrideParams);
		decision.targetEnvEditable = isTargetEnvEditable(overrideParams);
		decision.beEditable = isBeEditable(overrideParams);

		boolean maxEntitiesEditable = isMaxEntitiesEditable(overrideParams, numOfEntities);
		boolean beNull = false ; 
		boolean beBlocked = false;
		
		if (beID == null || "null".equalsIgnoreCase(beID))	beNull =true ;
		if (beID == null && !decision.beEditable)	beBlocked=true;
			
		if ("tester".equalsIgnoreCase(permissionGroup) && !maxEntitiesEditable) {
			TesterPermissions sourcePerms = getTesterPermissionsForUserEnv(userName, sourceEnvId, EnvType.SOURCE,
					userEnvs);
			TesterPermissions targetPerms = getTesterPermissionsForUserEnv(userName, targetEnvId, EnvType.TARGET,
					userEnvs);

			boolean canRun = canTesterRunTaskEntities(
					sourceEnvId, targetEnvId, taskType, taskId, numOfEntities,
					sourcePerms != null ? sourcePerms.allowedNumberOfEntitiesToRead : 0,
					targetPerms != null ? targetPerms.allowedNumberOfEntitiesToCopy : 0,
					targetPerms != null ? targetPerms.allowedNumberOfReservedEntities : 0,
					syncMode, reserveInd);

			if (!canRun) {
				if (requiresSourceCheck(taskType, syncMode)) {
					decision.sourceEnvDisabled = true;
				}
				if (requiresTargetCheck(taskType)) {
					decision.targetEnvDisabled = true;
				}
			}
		}

		if (decision.sourceEnvDisabled && decision.sourceEnvEditable) {
			decision.alternativeSourceEnv = findAlternativeEnv(userName, permissionGroup, EnvType.SOURCE, sourceEnvId,
					targetEnvId,
					taskType, syncMode, taskId, numOfEntities, reserveInd, maxEntitiesEditable, decision.beEditable, beID,
					userEnvs);
			decision.alternativeSourceEnvExists = decision.alternativeSourceEnv != null;
		}
		
		if (decision.targetEnvDisabled && decision.targetEnvEditable) {
			decision.alternativeTargetEnv = findAlternativeEnv(userName, permissionGroup, EnvType.TARGET, targetEnvId,
					sourceEnvId,
					taskType, syncMode, taskId, numOfEntities, reserveInd, maxEntitiesEditable, decision.beEditable, beID,
					userEnvs);
			decision.alternativeTargetEnvExists = decision.alternativeTargetEnv != null;
		}

		boolean pausedTask = !"active".equalsIgnoreCase(taskExecutionStatus);

		boolean sourceBlocked = decision.sourceEnvDisabled && !decision.sourceEnvEditable;
		boolean targetBlocked = decision.targetEnvDisabled && !decision.targetEnvEditable;
		if(beNull && sourceBlocked || beNull && targetBlocked) beBlocked=true;

		boolean noSourceReplacement = decision.sourceEnvDisabled && decision.sourceEnvEditable
				&& !decision.alternativeSourceEnvExists;
		boolean noTargetReplacement = decision.targetEnvDisabled && decision.targetEnvEditable
				&& !decision.alternativeTargetEnvExists;
				
		if(beNull && noSourceReplacement || beNull && noTargetReplacement) beBlocked=true;
		decision.holdTask = pausedTask || sourceBlocked || noSourceReplacement || targetBlocked
				|| noTargetReplacement || beBlocked;

		decision.removeSourceDefaultEnv = decision.sourceEnvDisabled && decision.sourceEnvEditable;
		decision.removeTargetDefaultEnv = decision.targetEnvDisabled && decision.targetEnvEditable;

		return decision;
	}

	/**
	 * Retrieves all tasks available for an Admin user within a specific task group.
	 *
	 * Admin users have full visibility over tasks, regardless of environment
	 * ownership.
	 * This method:
	 * - Fetches all tasks mapped to the given task group.
	 * - Determines whether each task is on hold based on admin rules.
	 * - Evaluates whether source/target environments are editable.
	 * - Marks tasks as favorite if applicable.
	 *
	 * @param groupId the task group identifier
	 * @return a set of task metadata maps containing:
	 *         task_id, task_name, hold_task, task_type_icon,
	 *         is_favorite, edit_source_env, edit_target_env
	 * @throws Exception if database access or processing fails
	 */
	public static Set<Map<String, Object>> getAdminTasks(Long groupId) throws Exception {
		Set<Map<String, Object>> taskList = new HashSet<>();
		String userName = sessionUser().name();
		List<Map<String, Object>> userEnvs = fnGetUserEnvs(userName);

		StringBuilder sql = createdByUser();

		taskList.addAll(
				fetchTasksForQuery(sql.toString(), new Object[] { userName, groupId, userName }, "admin", userEnvs));

		sql = notCreatedByUser();

		taskList.addAll(
				fetchTasksForQuery(sql.toString(), new Object[] { userName, groupId, userName }, "admin", userEnvs));

		return taskList;
	}

	/**
	 * Retrieves all tasks available for an Environment Owner within a specific task
	 * group.
	 *
	 * The method aggregates tasks based on ownership and permissions:
	 * - Tasks created by the user.
	 * - Tasks on environments owned by the user (source/target).
	 * - Tester-accessible tasks on non-owned environments (based on role
	 * permissions).
	 * - Tasks where source/target environments are editable by the user.
	 * 
	 * @param groupId the task group identifier
	 * @return a set of task metadata maps
	 * @throws Exception if database access or processing fails
	 */
	public static Set<Map<String, Object>> getOwnerTasks(Long groupId) throws Exception {
		Set<Map<String, Object>> taskList = new HashSet<>();
		String userName = sessionUser().name();
		List<Map<String, Object>> userEnvs = fnGetUserEnvs(userName);

		taskList.addAll(fetchTasksForQuery(createdByUser().toString(), new Object[] { userName, groupId, userName },
				"owner", userEnvs));

		for (Map<String, Object> envType : userEnvs) {
			List<Map<String, Object>> allSourceEnvs = (List<Map<String, Object>>) envType.get("source environments");
			List<Map<String, Object>> allTargetEnvs = (List<Map<String, Object>>) envType.get("target environments");

			if (allSourceEnvs != null) {
				for (Map<String, Object> sourceEnvMap : allSourceEnvs) {
					String envId = String.valueOf(sourceEnvMap.get("environment_id"));
					String roleId = String.valueOf(sourceEnvMap.get("role_id"));

					if (fnIsOwner(envId)) {
						StringBuilder sourceOwnerQuery = notCreatedByUser();
						sourceOwnerQuery.append(" AND lower(t.task_type) IN ('extract', 'training', 'generate') ");
						sourceOwnerQuery.append(" AND t.source_environment_id = ? ");

						taskList.addAll(fetchTasksForQuery(sourceOwnerQuery.toString(),
								new Object[] { userName, groupId, userName, envId }, "owner", userEnvs));
					} else { // if user env is returned but he is not the owner means he is a tester in the
								// env
						String sourceTesterQuery = getTesterSourceTasks(envId, roleId);
						log.debug("Owner source tester query: {}", sourceTesterQuery);
						taskList.addAll(fetchTasksForQuery(sourceTesterQuery,
								new Object[] { userName, groupId, userName, envId }, "tester", userEnvs));
					}
				}

				StringBuilder editableSourceBEQuery = notCreatedByUser();
				editableSourceBEQuery.append(" AND (t.task_override_fields -> 'source_environment' ->> 'is_editable')::boolean = true")
				.append(" AND (t.task_override_fields -> 'business_entity' ->> 'is_editable')::boolean = true");

				taskList.addAll(fetchTasksForQuery(editableSourceBEQuery.toString(),
						new Object[] { userName, groupId, userName }, "owner", userEnvs));

				StringBuilder editableSourceQuery = notCreatedByUser();
				editableSourceQuery.append(" AND (t.task_override_fields -> 'source_environment' ->> 'is_editable')::boolean = true")
				.append(" AND (t.task_override_fields -> 'business_entity'    ->> 'is_editable')::boolean = false");

				taskList.addAll(fetchTasksForQuery(editableSourceQuery.toString(),
						new Object[] { userName, groupId, userName }, "owner", userEnvs));
			}

			if (allTargetEnvs != null) {
				for (Map<String, Object> targetEnvMap : allTargetEnvs) {
					String envId = String.valueOf(targetEnvMap.get("environment_id"));
					String roleId = String.valueOf(targetEnvMap.get("role_id"));

					if (fnIsOwner(envId)) {
						StringBuilder targetOwnerQuery = notCreatedByUser();
						targetOwnerQuery
								.append(" AND lower(t.task_type) IN ('load', 'reserve', 'delete', 'ai_generated') ");
						targetOwnerQuery.append(" AND t.environment_id = ? ");

						taskList.addAll(fetchTasksForQuery(targetOwnerQuery.toString(),
								new Object[] { userName, groupId, userName, envId }, "owner", userEnvs));
					} else { // if user env is returned but he is not the owner means he is a tester in the
								// env
						String targetTesterQuery = getTesterTargetTasks(envId, roleId);
						log.debug("Owner target tester query: {}", targetTesterQuery);
						taskList.addAll(fetchTasksForQuery(targetTesterQuery,
								new Object[] { userName, groupId, userName, envId }, "tester", userEnvs));
					}
				}

				StringBuilder editableTargetBEQuery = notCreatedByUser();
				editableTargetBEQuery.append(" AND (t.task_override_fields -> 'target_environment' ->> 'is_editable')::boolean = true")
				.append(" AND (t.task_override_fields -> 'business_entity'    ->> 'is_editable')::boolean = true");

				taskList.addAll(fetchTasksForQuery(editableTargetBEQuery.toString(),
						new Object[] { userName, groupId, userName }, "owner", userEnvs));

				StringBuilder editableTargetQuery = notCreatedByUser();
				editableTargetQuery.append(" AND (t.task_override_fields -> 'target_environment' ->> 'is_editable')::boolean = true")
				.append(" AND (t.task_override_fields -> 'business_entity' ->> 'is_editable')::boolean = false");

				taskList.addAll(fetchTasksForQuery(editableTargetQuery.toString(),
						new Object[] { userName, groupId, userName }, "owner", userEnvs));
			}
		}

		return taskList;
	}

	/**
	 * Retrieves all tasks available for a Tester user within a specific task group.
	 *
	 * This method:
	 * - Fetches tasks created by the user.
	 * - Retrieves tasks based on the user’s assigned source and target
	 * environments.
	 * - Applies tester permission rules for source and target tasks.
	 * - Evaluates task availability (disabled environments, editability, etc.)
	 *
	 * @param groupId the task group identifier
	 * @return a set of task metadata maps accessible to the tester
	 * @throws Exception if database access or processing fails
	 */
	public static Set<Map<String, Object>> getTesterTasks(Long groupId) throws Exception {
		Set<Map<String, Object>> taskList = new HashSet<>();
		String userName = sessionUser().name();
		// Case 1: Get the tasks created by user.
		StringBuilder createdByUserQuery = createdByUser();
		List<Map<String, Object>> allUserEnvsTypes = fnGetUserEnvs(userName);

		taskList.addAll(fetchTasksForQuery(createdByUserQuery.toString(), new Object[] { userName, groupId, userName },
				"tester", allUserEnvsTypes));

		for (Map<String, Object> envType : allUserEnvsTypes) {
			List<Map<String, Object>> allSourceEnvs = (List<Map<String, Object>>) envType.get("source environments");
			List<Map<String, Object>> allTargetEnvs = (List<Map<String, Object>>) envType.get("target environments");
			// Case 2: Get tasks where the user has permissions for the source environment.
			if (allSourceEnvs != null) {
				for (Map<String, Object> sourceEnvMap : allSourceEnvs) {
					String envId = String.valueOf(sourceEnvMap.get("environment_id"));
					String roleId = String.valueOf(sourceEnvMap.get("role_id"));
					String queryBuilderSource = getTesterSourceTasks(envId, roleId);
					log.debug("Tester source query: {}", queryBuilderSource);
					taskList.addAll(fetchTasksForQuery(queryBuilderSource,
							new Object[] { userName, groupId, userName, envId }, "tester", allUserEnvsTypes));
				}
			}
			// Case 3: Get tasks where the user has permissions for the target environment.
			if (allTargetEnvs != null) {
				for (Map<String, Object> targetEnvMap : allTargetEnvs) {
					String envId = String.valueOf(targetEnvMap.get("environment_id"));
					String roleId = String.valueOf(targetEnvMap.get("role_id"));
					String queryBuilderTarget = getTesterTargetTasks(envId, roleId);
					log.debug("Tester target query: {}", queryBuilderTarget);
					taskList.addAll(fetchTasksForQuery(queryBuilderTarget,
							new Object[] { userName, groupId, userName, envId }, "tester", allUserEnvsTypes));
				}
			}
		}

		return taskList;
	}

	/**
	 * Executes a task query and enriches each task with availability and
	 * environment logic.
	 *
	 * This method:
	 * - Executes the given SQL query with parameters
	 * - Iterates over all returned task rows
	 * - Evaluates each task's availability based on:
	 * - Disabled environments
	 * - Editability (source/target/BE)
	 * - Alternative environment availability
	 * - Builds a response map per task including:
	 * - Execution status (hold / executable)
	 * - Environment replacement indicators
	 * - Alternative environments (if applicable)
	 *
	 * @param query       the SQL query to execute
	 * @param queryParams parameters for the SQL query
	 * @return list of task metadata maps enriched with availability logic
	 * @throws Exception if database access or evaluation fails
	 */
	public static List<Map<String, Object>> fetchTasksForQuery(String query, Object[] queryParams,
		String permessionGroup, List<Map<String, Object>> userEnvs) throws Exception {
		// List<Map<String, Object>> result = new ArrayList<>();
		List<Map<String, Object>> tasks = new ArrayList<>();
		String userName = sessionUser().name();
		try {
			for (Db.Row row : db(TDM).fetch(query, queryParams)) {
				Long taskId = ((Number) row.get("task_id")).longValue();

				String taskTitle = String.valueOf(row.get("task_title"));
				String taskExecutionStatus = String.valueOf(row.get("task_execution_status"));
				String taskType = String.valueOf(row.get("task_type"));
				String syncMode = String.valueOf(row.get("sync_mode"));
				String overrideParams = String.valueOf(row.get("task_override_fields"));
				Long numOfEntities = row.get("num_of_entities") == null ? 0L
						: ((Number) row.get("num_of_entities")).longValue();
				boolean reserveInd = row.get("reserve_ind") != null
						&& Boolean.parseBoolean(String.valueOf(row.get("reserve_ind")));

				String sourceEnvName = String.valueOf(row.get("source_env_name"));
				String sourceEnvId = String.valueOf(row.get("source_environment_id"));
				String targetEnvName = String.valueOf(row.get("target_env_name"));
				String targetEnvId = String.valueOf(row.get("target_environment_id"));
				String beId = String.valueOf(row.get("be_id"));
				String updatedDate = String.valueOf(row.get("task_last_updated_date"));
				String selectionMethod = String.valueOf(row.get("selection_method"));

				boolean isPermittedUser = isPermittedUserForExecution(taskId,permessionGroup,sourceEnvId,targetEnvId);
				if (!isPermittedUser) {
						continue;
				}
				TaskAvailabilityDecision availability = evaluateTaskAvailability(userName, permessionGroup, taskId,
						taskType,
						syncMode, overrideParams, beId, sourceEnvId, sourceEnvName, targetEnvId, targetEnvName,
						taskExecutionStatus, numOfEntities, reserveInd, userEnvs);

				Map<String, Object> taskInfo = buildTaskInfo(taskId, taskTitle, taskType, selectionMethod, syncMode,
						availability.holdTask,
						availability.sourceEnvEditable, availability.targetEnvEditable, row.get("favorite"),
						isPermittedUser, userName,updatedDate);

				Map<String,Object> taskDebug = new HashMap<String,Object>();
				taskDebug.put("source_env_disabled", availability.sourceEnvDisabled);
				taskDebug.put("target_env_disabled", availability.targetEnvDisabled);
				taskDebug.put("be_editable", availability.beEditable);

				if (availability.removeSourceDefaultEnv) {
					taskDebug.put("remove_source_default_env", true);
					if (availability.alternativeSourceEnv != null) {
						taskDebug.put("alternative_source_environment_id",
								availability.alternativeSourceEnv.environmentId);
						taskDebug.put("alternative_source_environment_name",
								availability.alternativeSourceEnv.environmentName);
					} else {
						taskDebug.put("alternative_source_environment_id", null);
						taskDebug.put("alternative_source_environment_name", null);
					}
				} else {
					taskDebug.put("remove_source_default_env", false);
				}

				if (availability.removeTargetDefaultEnv) {
					taskDebug.put("remove_target_default_env", true);
					if (availability.alternativeTargetEnv != null) {
						taskDebug.put("alternative_target_environment_id",
								availability.alternativeTargetEnv.environmentId);
								taskDebug.put("alternative_target_environment_name",
								availability.alternativeTargetEnv.environmentName);
					} else {
						taskDebug.put("alternative_target_environment_id", null);
						taskDebug.put("alternative_target_environment_name", null);
					}
				} else {
					taskDebug.put("remove_target_default_env", false);
				}

				tasks.add(taskInfo);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error fetching tasks for user: " + userName, e);
		}
		return tasks;

	}

	/**
	 * Retrieves all tasks available for a Tester within a specific task group.
	 *
	 * The method aggregates tasks based on the tester's permissions and assigned
	 * environments:
	 * - Tasks created by the user.
	 * - Source tasks available through the user's read permissions.
	 * - Target tasks available through the user's read/write permissions.
	 *
	 * @param groupId the task group identifier
	 * @return a set of task metadata maps
	 * @throws Exception if database access or processing fails
	 */
	public static String getTesterSourceTasks(String envId, String roleId) throws Exception {
		StringBuilder queryBuilderSource = notCreatedByUser();
		queryBuilderSource.append(" AND (t.source_environment_id = ? ")
        .append("OR (t.source_environment_id IS NULL ")
        .append("AND (t.task_override_fields -> 'source_environment' ->> 'is_editable')::Boolean = true)) ");
		try {
			TesterPermissions permissions = getTesterPermissions(envId, roleId);

			boolean allowRead = permissions.allowRead;
			boolean allowFreshData = permissions.allowedRequestOfFreshData;
			boolean allowSynthetic = permissions.allowedCreationOfSyntheticData;
			boolean allowVersioning = permissions.allowedEntityVersioning;
			boolean allowScheduling = permissions.allowedTaskScheduling;
			boolean allowRefresh = permissions.allowedRefreshReferenceData;
			boolean allowRandom = permissions.allowedRandomEntitySelection;

			queryBuilderSource.append(" AND lower(t.task_type) IN (")
					.append(buildTaskTypes(allowSynthetic, envId, false, false, false))
					.append(") ");

			if (allowRead) {
				queryBuilderSource.append(" AND t.selection_method <> 'ALL' ");

				if (!allowFreshData) {
					queryBuilderSource.append(" AND t.sync_mode <> 'FORCE' ");
				}

				if (!allowVersioning) {
					queryBuilderSource.append(" AND t.version_ind = false ");
				}

				if (!allowScheduling) {
					queryBuilderSource.append(" AND lower(t.scheduler) = 'immediate' ");
				}

				if (!allowRandom) {
					queryBuilderSource.append(" AND t.selection_method NOT IN ('R', 'PR') ");
				}

				if (!allowRefresh) {
					queryBuilderSource.append(" AND (t.selection_method <> 'TABLES' AND NOT EXISTS (SELECT 1 FROM ")
							.append(TDMDB_SCHEMA)
							.append(".task_ref_tables trt WHERE trt.task_id = t.task_id)) ");
				}
			} else {
				queryBuilderSource.append(" AND 1 = 0 ");
			}

		} catch (Exception e) {
			throw new RuntimeException("Error checking for testers source tasks", e);
		}

		return queryBuilderSource.toString();
	}

	public static String getTesterTargetTasks(String envId, String roleId) throws Exception {
		StringBuilder queryBuilderTarget = notCreatedByUser();
		queryBuilderTarget.append(" AND (t.environment_id = ? ")
        .append("OR (t.environment_id IS NULL ")
        .append("AND (t.task_override_fields -> 'target_environment' ->> 'is_editable')::Boolean = true)) ");
		try {
			TesterPermissions permissions = getTesterPermissions(envId, roleId);

			boolean allowWrite = permissions.allowWrite;
			boolean allowReplace = permissions.allowedReplaceSequences;
			boolean allowFreshData = permissions.allowedRequestOfFreshData;
			boolean allowVersioning = permissions.allowedEntityVersioning;
			boolean allowScheduling = permissions.allowedTaskScheduling;
			boolean allowRefresh = permissions.allowedRefreshReferenceData;
			boolean allowRandom = permissions.allowedRandomEntitySelection;
			boolean allowReserve = permissions.allowedNumberOfReservedEntities != 0;
			boolean allowDelete = permissions.allowedDeleteBeforeLoad;

			if (allowWrite) {
				queryBuilderTarget.append(" AND t.selection_method <> 'ALL' ");

				if (!allowFreshData) {
					queryBuilderTarget.append(" AND t.sync_mode <> 'FORCE' ");
				}

				if (!allowVersioning) {
					queryBuilderTarget.append(" AND t.version_ind = false ");
				}

				if (!allowScheduling) {
					queryBuilderTarget.append(" AND lower(t.scheduler) = 'immediate' ");
				}

				if (!allowRandom) {
					queryBuilderTarget.append(" AND t.selection_method NOT IN ('R', 'PR') ");
				}

				if (!allowReplace) {
					queryBuilderTarget.append(" AND t.replace_sequences = false ");
				}

				if (!allowRefresh) {
					queryBuilderTarget.append(" AND (t.selection_method <> 'TABLES' AND NOT EXISTS (SELECT 1 FROM ")
							.append(TDMDB_SCHEMA)
							.append(".task_ref_tables trt WHERE trt.task_id = t.task_id)) ");
				}

				if (!allowReserve) {
					queryBuilderTarget.append(" AND t.reserve_ind = false ");
				}

				if (!allowDelete) {
					queryBuilderTarget.append(" AND t.delete_before_load = false ");
				}

				queryBuilderTarget.append(" AND lower(t.task_type) IN (")
						.append(buildTaskTypes(permissions.allowedCreationOfSyntheticData, envId, true, allowDelete,
								allowReserve))
						.append(") ");
			} else {
				queryBuilderTarget.append(" AND 1 = 0 ");
			}

		} catch (Exception e) {
			throw new RuntimeException("Error checking for testers target tasks", e);
		}

		return queryBuilderTarget.toString();
	}

	public static boolean isPermittedUserForExecution(long taskId, String permessionGroup, String sourceEnvId, String targetEnvId) {
		String currentUser = sessionUser().name();
		String roles = fnGetUserRoles(currentUser);
		String[] currentUserRoles = roles != null
				? roles.split(TDM_PARAMETERS_SEPARATOR)
				: new String[0];
		boolean isAdmin = permessionGroup.equalsIgnoreCase("admin");
		if(isAdmin)	return true;
		if(permessionGroup.equalsIgnoreCase("owner")){
			try {
				if(fnIsOwner(sourceEnvId) || fnIsOwner(targetEnvId)){
					return true;
				}
			} catch (Exception e) {
				throw new RuntimeException("Error checking owner permission for taskId=" + taskId, e);
			}
		}
		StringBuilder sql = new StringBuilder(200);
		sql.append("SELECT permitted_user ").append("FROM ").append(TDMDB_SCHEMA).append(".task_exe_permissions ")
				.append("WHERE task_id = ?");
		try {
			for (Db.Row row : db(TDM).fetch(sql.toString(), taskId)) {
				Object val = row.get("permitted_user");
				if (val == null) continue;
				String permittedUser = val.toString();
				if ("ALL".equalsIgnoreCase(permittedUser)) {
					return true;
				}
				if (permittedUser.equalsIgnoreCase(currentUser)) {
					return true;
				}
				for (String role : currentUserRoles) {
					if (permittedUser.equalsIgnoreCase(role)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error checking permitted users for taskId=" + taskId, e);
		}

		return false;
	}

	public static boolean isPermittedUserForStopResume(long taskExeId) {
		String currentUser = sessionUser().name();
		String permessionGroup = fnGetUserPermissionGroup(currentUser);
		boolean isAdmin = permessionGroup.equalsIgnoreCase("admin");
		if (isAdmin)
			return true;
		StringBuilder sql = new StringBuilder(200);
		sql.append("SELECT task_executed_by ").append("FROM ").append(TDMDB_SCHEMA)
				.append(".task_execution_list where task_execution_id=?");
		try {
			for (Db.Row row : db(TDM).fetch(sql.toString(), taskExeId)) {
				String executed_by = row.get("task_executed_by").toString();
				if (executed_by.equalsIgnoreCase(currentUser))
					return true;
				return false;
			}
		} catch (Exception e) {
			throw new RuntimeException("Error checking permitted users for execution =" + taskExeId, e);
		}

		return false;
	}
	
	public static boolean isAllowedToCreate(String username) {
		String permissionGroup = fnGetUserPermissionGroup(username);
		String roles = fnGetUserRoles(username);
		String created_by;
		try {
			created_by = "" + db(TDM).fetch(
					"SELECT can_create_tasks FROM " + TDMDB_SCHEMA + ".permission_groups_mapping WHERE fabric_role = ANY (string_to_array(?,?)) and permission_group = ? ",
					roles,TDM_PARAMETERS_SEPARATOR,permissionGroup).firstValue();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return "admin".equals(permissionGroup) || "true".equalsIgnoreCase(created_by);
	}

	public static boolean isTaskCreator(String username, long taskID) {
		String permissionGroup = fnGetUserPermissionGroup("");
		String created_by;
		try {
			created_by = "" + db(TDM).fetch(
					"SELECT split_part(task_created_by, '##', 1) FROM " + TDMDB_SCHEMA + ".tasks WHERE task_id=?",
					taskID).firstValue();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return "admin".equals(permissionGroup) || username.equals(created_by);
	}
	
	public static Object getEnvironmentIdByName(Object name) throws Exception {
		Object id = db(TDM)
				.fetch("select environment_id from " + TDMDB_SCHEMA
						+ ".environments where environment_name=(?) and environment_status = 'Active'", name)
				.firstValue();
		return id;
	}

	private static boolean isAiOrSdgTask(String taskType, String currentEnvId, String oppositeEnvId) {
		if (taskType == null) {
			return false;
		}
		String normalizedTaskType = taskType.trim().toUpperCase();
		return normalizedTaskType.contains("TRAINING")
				|| normalizedTaskType.contains("GENERATE")|| Long.valueOf(currentEnvId) <0 || Long.valueOf(oppositeEnvId) <0 ;
	}

	private static boolean fnAreSystemsDisabled(String envId, String envName, Long taskId, String beId, boolean rootOnly) throws Exception {
		if (Util.isEmpty(envName) || "null".equalsIgnoreCase(envName) || taskId == null) {
			return false;
		}
		if (rootOnly && (Util.isEmpty(beId) || "null".equalsIgnoreCase(beId))) {
			return false;
		}
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append("COUNT(DISTINCT pu.product_id) AS total_systems, ");
		query.append("COUNT(DISTINCT CASE WHEN ep.enable_product = false THEN pu.product_id END) AS disabled_systems ");
		query.append("FROM ").append(TDMDB_SCHEMA).append(".product_logical_units pu ");
		query.append("JOIN ").append(TDMDB_SCHEMA).append(".tasks_logical_units tu ");
		query.append("ON tu.lu_id = pu.lu_id ");
		if (!Util.isEmpty(beId) && !"null".equalsIgnoreCase(beId)){
			query.append("AND pu.be_id = ? ");
		}
		query.append("JOIN ").append(TDMDB_SCHEMA).append(".environments e ");
		query.append("ON e.environment_id = ? ");
		query.append("AND e.environment_name = ? ");
		query.append("AND e.environment_status = ? ");
		query.append("JOIN ").append(TDMDB_SCHEMA).append(".environment_products ep ");
		query.append("ON ep.environment_id = e.environment_id ");
		query.append("AND ep.product_id = pu.product_id ");
		query.append("AND ep.status = ? ");
		query.append("WHERE tu.task_id = ? ");
		query.append("AND pu.product_id IS NOT NULL ");
		if (rootOnly) {
			query.append("AND pu.lu_parent_id IS NULL ");
		}
		if (!Util.isEmpty(beId) && !"null".equalsIgnoreCase(beId)) {
			return areAllSystemsDisabled(query.toString(), Long.valueOf(beId), envId, envName, "Active", "Active", taskId);
		}
		return areAllSystemsDisabled(query.toString(), envId, envName, "Active", "Active", taskId);
	}
    
    private static boolean areAllSystemsDisabled(String query, Object... params) throws Exception {
        try {
            Db.Row row = db(TDM).fetch(query, params).firstRow();

            if (row == null) {
                return false;
            }

            long totalSystems = Long.parseLong(row.get("total_systems").toString());
            long disabledSystems = Long.parseLong(row.get("disabled_systems").toString());

            return totalSystems > 0 && totalSystems == disabledSystems;

        } catch (Exception e) {
            log.error("Error in areAllSystemsDisabled: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

	private static boolean fnAreAllLuSystemsDisabled(String envId, String envName, Long taskId, String beId) throws Exception {
		return fnAreSystemsDisabled(envId, envName, taskId, beId, false);
	}

	private static boolean fnAreAllRootLuSystemsDisabled(String envId, String envName, Long taskId, String beId) throws Exception {
		return fnAreSystemsDisabled(envId, envName, taskId, beId, true);
	}
}
