package com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils;

import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.shared.user.UserCode.isFirstSync;
import static com.k2view.cdbms.shared.user.UserCode.log;
import static com.k2view.cdbms.shared.user.UserCode.sessionUser;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.enable_sequences;
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
import java.util.Arrays;
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

	private static final DateTimeFormatter TASK_DATE_FORMATTER = new DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd HH:mm:ss")
			.optionalStart()
			.appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
			.optionalEnd()
			.toFormatter();

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
			Object holdTask, boolean editSourceEnv, boolean editTargetEnv, Object favorite, boolean isPermittedUser,
			String task_last_updated_date, boolean canEdit, boolean canCreate) {

		Map<String, Object> taskInfo = new HashMap<>();
		taskInfo.put("task_id", taskId);
		taskInfo.put("task_title", taskTitle);
		taskInfo.put("task_last_updated_date", task_last_updated_date);
		taskInfo.put("hold_task", holdTask);
		taskInfo.put("display_task_type", getDisplayedTaskType(taskType, syncMode, selectionMethod));
		taskInfo.put("favorite", favorite);
		taskInfo.put("can_edit_task", canEdit);
		taskInfo.put("can_create_task", canCreate);
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
					return LocalDateTime.parse(val.toString(), TASK_DATE_FORMATTER);
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
		boolean allowedTestConnFailure;

	}

	/**
	 * try to remove the join of the favorite to only add it when retriving the
	 * tasks of the current user
	 * pluse add the group id in the conditions and task status is active
	 */
	private static StringBuilder baseQuery() {
		StringBuilder sql = new StringBuilder(600);
		sql.append("SELECT t.task_id, t.task_title, t.task_type, t.selection_method, t.sync_mode, t.task_execution_status,t.task_last_updated_date,t.version_ind, t.replace_sequences, ")
				.append("t.delete_before_load, t.clone_ind, t.task_override_fields, t.scheduler, t.refresh_reference_data, t.be_id, t.source_env_name, t.source_environment_id, t.reserve_ind, ")
				.append("t.environment_id AS target_environment_id, t.num_of_entities, t.env_name AS target_env_name, ")
				.append("split_part(t.task_created_by, '##', 1) AS task_creator, ")
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

	private static boolean sourceEnvSystemsDisabled(String sourceEnvId, String sourceEnvName, String taskType, String syncMode, Long taskId, String beId, Map<String, Boolean> cache) throws Exception {
		if (!"OFF".equalsIgnoreCase(syncMode) && !"RESERVE".equalsIgnoreCase(taskType)) {
			String rootKey = sourceEnvId + "|" + taskId + "|" + beId + "|root";
			Boolean rootResult = cache.get(rootKey);
			if (rootResult == null) {
				rootResult = fnAreAllRootLuSystemsDisabled(sourceEnvId, sourceEnvName, taskId, beId);
				cache.put(rootKey, rootResult);
			}
			if (rootResult) return true;
			String allKey = sourceEnvId + "|" + taskId + "|" + beId + "|all";
			Boolean allResult = cache.get(allKey);
			if (allResult == null) {
				allResult = fnAreAllLuSystemsDisabled(sourceEnvId, sourceEnvName, taskId, beId);
				cache.put(allKey, allResult);
			}
			return allResult;
		}
		return false;
	}

	private static boolean targetEnvSystemsDisabled(String targetEnvId, String targetEnvName, String taskType, String syncMode, Long taskId, String beId, Map<String, Boolean> cache) throws Exception {
		if ("LOAD".equalsIgnoreCase(taskType) || "DELETE".equalsIgnoreCase(taskType)) {
			String rootKey = targetEnvId + "|" + taskId + "|" + beId + "|root";
			Boolean rootResult = cache.get(rootKey);
			if (rootResult == null) {
				rootResult = fnAreAllRootLuSystemsDisabled(targetEnvId, targetEnvName, taskId, beId);
				cache.put(rootKey, rootResult);
			}
			if (rootResult) return true;
			String allKey = targetEnvId + "|" + taskId + "|" + beId + "|all";
			Boolean allResult = cache.get(allKey);
			if (allResult == null) {
				allResult = fnAreAllLuSystemsDisabled(targetEnvId, targetEnvName, taskId, beId);
				cache.put(allKey, allResult);
			}
			return allResult;
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

	private static boolean isMaxEntitiesEditable(String overrideParams) {
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
	
	private static Set<Long> getCandidateEnvironments(long taskId, Long currentEnvId) {
		StringBuilder sql = new StringBuilder(400);
		sql.append("SELECT e.environment_id ")
				.append("FROM ").append(TDMDB_SCHEMA).append(".environments e ")
				.append("WHERE e.environment_status = 'Active' ")
				.append("AND e.environment_id NOT IN (-1,-2")
				.append(currentEnvId != null ? ",?" : "")
				.append(") ")
				.append("AND EXISTS ( ")
				.append("SELECT 1 FROM ").append(TDMDB_SCHEMA).append(".tasks_logical_units tlu ")
				.append("JOIN ").append(TDMDB_SCHEMA).append(".product_logical_units plu ON plu.lu_id = tlu.lu_id ")
				.append("JOIN ").append(TDMDB_SCHEMA).append(".environment_products ep ")
				.append("ON ep.environment_id = e.environment_id AND ep.product_id = plu.product_id ")
				.append("WHERE tlu.task_id = ? ")
				.append("AND plu.lu_parent_id IS NULL ")
				.append("AND ep.enable_product = true ")
				.append("AND ep.status = 'Active') ");

		try {
			Object[] params = currentEnvId != null ? new Object[]{currentEnvId, taskId} : new Object[]{taskId};
			Set<Long> result = new HashSet<>();
			for (Db.Row row : db(TDM).fetch(sql.toString(), params)) {
				result.add(Long.valueOf(row.get("environment_id").toString()));
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch candidate environments for taskId=" + taskId
					+ (currentEnvId != null ? ", currentEnvId=" + currentEnvId : ""), e);
		}
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
		return envId != null && !envId.trim().isEmpty() && !"undefined".equalsIgnoreCase(envId);
	}

	public static boolean requiresSourceCheck(String taskType, String syncMode ,String envId) {
		String type = taskType == null ? "" : taskType.trim().toUpperCase();
		if(hasEnv(envId)){
			if("undefined".equalsIgnoreCase(taskType)) taskType = "EXTRACT" ;
			
			if ("EXTRACT".equals(type) || "GENERATE".equals(type) || "AI_GENERATED".equals(type)) {
				return true;
			}

			if ("LOAD".equals(type) || "TRAINING".equals(type)) {
				return !"OFF".equalsIgnoreCase(syncMode);
			}
		}
		return false;
		}

	public static boolean requiresTargetCheck(String taskType, String envId) {
		String type = taskType == null ? "" : taskType.trim().toUpperCase();
		if(!hasEnv(envId)) return false ;
		if("undefined".equalsIgnoreCase(taskType)) type = "LOAD" ;
		return "LOAD".equals(type) || "DELETE".equals(type) || "RESERVE".equals(type) || "TRAINING".equals(type);
	}

	private static TesterPermissions getTesterPermissionsForUserEnv(String userName, String envId, EnvType side,
			List<Map<String, Object>> allUserEnvsTypes, Map<String, TesterPermissions> cache) throws Exception {
		String key = side == EnvType.SOURCE ? "source environments" : "target environments";
		for (Map<String, Object> envType : allUserEnvsTypes) {
			List<Map<String, Object>> envs = (List<Map<String, Object>>) envType.get(key);
			if (envs == null) {
				continue;
			}
			for (Map<String, Object> envMap : envs) {
				if (Objects.equals(String.valueOf(envMap.get("environment_id")), envId)) {
					String roleId = String.valueOf(envMap.get("role_id"));
					String cacheKey = envId + "|" + roleId;
					TesterPermissions perms = cache.get(cacheKey);
					if (perms == null) {
						perms = getTesterPermissions(envId, roleId);
						if (perms != null) cache.put(cacheKey, perms);
					}
					return perms;
				}
			}
		}

		return null;
	}

	private static boolean isOwnerOfEnv(String envId, List<Map<String, Object>> userEnvs) {
		if (envId == null || userEnvs == null) return false;
		for (Map<String, Object> envType : userEnvs) {
			for (String key : Arrays.asList("source environments", "target environments")) {
				List<Map<String, Object>> envs = (List<Map<String, Object>>) envType.get(key);
				if (envs == null) continue;
				for (Map<String, Object> envMap : envs) {
					if ("owner".equalsIgnoreCase(String.valueOf(envMap.get("role_id")))
							&& String.valueOf(envMap.get("environment_id")).equals(envId)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean canTesterRunTaskEntities(String sourceEnvId, String targetEnvId, String taskType,
			long requestedEntities, long allowedToRead, long allowedToWrite, long allowedToReserve,
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

	private static boolean canTesterRunTaskEntitiesForSide(EnvType side, String sourceEnvId, String targetEnvId,
		 String taskType,long requestedEntities, long allowedToRead, long allowedToWrite, long allowedToReserve,
			String syncMode, boolean reserveInd) throws Exception {
		String normalizedTaskType = taskType == null ? "" : taskType.trim().toUpperCase();
		boolean syncOff = "OFF".equalsIgnoreCase(syncMode);

		if (side == EnvType.SOURCE) {
			if ("EXTRACT".equals(normalizedTaskType)
					|| "GENERATE".equals(normalizedTaskType)
					|| "AI_GENERATED".equals(normalizedTaskType)
					|| ("LOAD".equals(normalizedTaskType) && !syncOff)
					|| ("TRAINING".equals(normalizedTaskType) && !syncOff)) {
				return hasEnv(sourceEnvId)
						&& hasEnoughEntities(requestedEntities, allowedToRead);
			}

			return true;
		}

		if ("DELETE".equals(normalizedTaskType)) {
			return hasEnv(targetEnvId)
					&& hasEnoughEntities(requestedEntities, allowedToWrite);
		}

		if ("RESERVE".equals(normalizedTaskType)) {
			return hasEnv(targetEnvId)
					&& hasEnoughEntities(requestedEntities, allowedToReserve);
		}

		if ("LOAD".equals(normalizedTaskType) || "TRAINING".equals(normalizedTaskType)) {
			long targetLimit = reserveInd
					? maxEntitiesLimit(allowedToWrite, allowedToReserve)
					: allowedToWrite;

			return hasEnv(targetEnvId)
					&& hasEnoughEntities(requestedEntities, targetLimit);
		}

		return true;
	}

	/**
	 * Finds a valid alternative environment for a task based on user permissions
	 * and system compatibility, excluding the current environment.
	*/
	private static AlternativeEnv findAlternativeEnv(String userName, String permissionGroup, EnvType side,
		String currentEnvId, String oppositeEnvId, String taskType, String syncMode, Long taskId,
		long numOfEntities, boolean reserveInd, boolean maxEntitiesEditable, boolean selectionMethodEditable, boolean beEditable, String beId,
		List<Map<String, Object>> userEnvs, boolean deleteBeforeLoad, boolean replaceSequences, boolean cloneInd, boolean refreshReferenceData, boolean versionInd, String scheduler, String selectionMethod,
		Map<String, Boolean> envDisabledCache, Map<String, TesterPermissions> testerPermsCache) throws Exception {
	
		if (isAiOrSdgTask(taskType,currentEnvId,oppositeEnvId)) {
			return null;
		}
		boolean isTablesOnlyTask = "-1".equals(String.valueOf(beId));
		Set<Long> candidateEnvIds = Collections.emptySet();
		if (!beEditable && !isTablesOnlyTask) {
			Set<Long> resolved = getCandidateEnvironments(taskId,
					hasEnv(currentEnvId) ? Long.parseLong(currentEnvId) : null);
			if (resolved != null) {
				candidateEnvIds = resolved;
			}
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
	
				if (!beEditable && !isTablesOnlyTask && !candidateEnvIds.contains(Long.valueOf(envId))) {
					continue;
				}
	
				boolean envDisabled = (side == EnvType.SOURCE)
						? sourceEnvSystemsDisabled(envId, envName, taskType, syncMode, taskId, beId, envDisabledCache)
						: targetEnvSystemsDisabled(envId, envName, taskType, syncMode, taskId, beId, envDisabledCache);
				if (envDisabled) {
					continue;
				}

				String candidateSourceEnvId = side == EnvType.SOURCE ? envId : null;
				String candidateTargetEnvId = side == EnvType.TARGET ? envId : null;

				if ("tester".equalsIgnoreCase(permissionGroup)) {
					boolean canRunWithCandidate = canUserPerformTaskOperation(
							userName, candidateSourceEnvId, candidateTargetEnvId,
							taskType, syncMode, reserveInd, deleteBeforeLoad, replaceSequences,
							cloneInd, refreshReferenceData, versionInd, scheduler,
							selectionMethod, numOfEntities, "RUN",maxEntitiesEditable,selectionMethodEditable,userEnvs, permissionGroup, testerPermsCache);
					if (!canRunWithCandidate) {
						continue;
					}
				}

				return new AlternativeEnv(envId, envName);
			}
		}

		return null;
	}

	private static TesterPermissions getTesterPermissions(String envId, String roleId) {
		
		if ("owner".equalsIgnoreCase(roleId)) {
			TesterPermissions permissions = new TesterPermissions();

			permissions.allowedDeleteBeforeLoad = true;
			permissions.allowedCreationOfSyntheticData = true;
			permissions.allowedRandomEntitySelection = true;
			permissions.allowedRequestOfFreshData = true;
			permissions.allowedTaskScheduling = true;

			permissions.allowedNumberOfEntitiesToCopy = -1L;
			permissions.allowedNumberOfEntitiesToRead = -1L;
			permissions.allowedNumberOfReservedEntities = -1L;

			permissions.allowedRefreshReferenceData = true;
			permissions.allowedReplaceSequences = true;

			permissions.allowRead = true;
			permissions.allowWrite = true;

			permissions.allowedEntityVersioning = true;
			permissions.allowedTestConnFailure = true;

			return permissions;
		}
		
		StringBuilder sql = new StringBuilder(400);
		sql.append("SELECT allowed_delete_before_load, allowed_creation_of_synthetic_data, ")
				.append("allowed_random_entity_selection, allowed_request_of_fresh_data, ")
				.append("allowed_task_scheduling, allowed_number_of_entities_to_copy, ")
				.append("allowed_refresh_reference_data, allowed_replace_sequences, ")
				.append("allow_read, allow_write, allowed_number_of_entities_to_read, ")
				.append("allowed_entity_versioning, allowed_test_conn_failure, ")
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

				permissions.allowRead = Boolean
				.parseBoolean(row.get("allow_read").toString());
				permissions.allowWrite = Boolean
				.parseBoolean(row.get("allow_write").toString());

				permissions.allowedEntityVersioning = Boolean
				.parseBoolean(row.get("allowed_entity_versioning").toString());
				permissions.allowedTestConnFailure = Boolean
				.parseBoolean(row.get("allowed_test_conn_failure").toString());
				break;
			}

		} catch (Exception e) {
			throw new RuntimeException("Error checking tester permissions for role " + roleId, e);
		}

		return permissions;
	}

	private static String buildTaskTypes(String envId, boolean isTarget, boolean allowDelete,
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

		if ("-1".equalsIgnoreCase(envId)) {
			typeConditions.add("'generate'");
		}

		if ("-2".equalsIgnoreCase(envId)) {
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
			long numOfEntities, boolean reserveInd, List<Map<String, Object>> userEnvs, String selectionMethod, String scheduler,
			boolean versionInd, boolean refreshReferenceData, boolean cloneInd,boolean replaceSequences,boolean deleteBeforeLoad,
			Map<String, Boolean> envDisabledCache, Map<String, TesterPermissions> testerPermsCache) throws Exception {

		TaskAvailabilityDecision decision = new TaskAvailabilityDecision();
		
		boolean sourceRequired = requiresSourceCheck(taskType, syncMode, sourceEnvId);
		boolean targetRequired = requiresTargetCheck(taskType, targetEnvId);

		decision.sourceEnvDisabled = sourceRequired
					&& sourceEnvSystemsDisabled(sourceEnvId, sourceEnvName, taskType, syncMode, taskId, beID, envDisabledCache);

		decision.targetEnvDisabled = targetRequired
					&& targetEnvSystemsDisabled(targetEnvId, targetEnvName, taskType, syncMode, taskId, beID, envDisabledCache);


		decision.sourceEnvEditable = isSourceEnvEditable(overrideParams);
		decision.targetEnvEditable = isTargetEnvEditable(overrideParams);
		decision.beEditable = isBeEditable(overrideParams);

		boolean maxEntitiesEditable = isMaxEntitiesEditable(overrideParams);
		boolean randomEditable = isNestedEditable(overrideParams, "selection_method", "random") || isEditable(overrideParams, "selection_method");
		boolean beNull = false ; 
		boolean beBlocked = false;
		
		if (beID == null || "null".equalsIgnoreCase(beID))	beNull =true ;
		if (beID == null && !decision.beEditable)	beBlocked=true;
			
		if ("tester".equalsIgnoreCase(permissionGroup)) {

			boolean sourceCanRun = true;
			boolean targetCanRun = true;
			boolean forceSelectionMethod = !randomEditable ;
			boolean forceEntitesNum = !maxEntitiesEditable ;
			if (sourceRequired) {
				sourceCanRun = canUserPerformTaskOperation(
						userName,
						sourceEnvId,
						decision.targetEnvEditable ? null : targetEnvId,
						taskType, syncMode, reserveInd, deleteBeforeLoad, replaceSequences,
						cloneInd, refreshReferenceData, versionInd, scheduler,
						selectionMethod, numOfEntities, "RUN",
						forceEntitesNum, forceSelectionMethod, userEnvs, permissionGroup, testerPermsCache);
			}

			if (targetRequired) {
				targetCanRun = canUserPerformTaskOperation(
						userName,
						decision.sourceEnvEditable ? null : sourceEnvId,
						targetEnvId,
						taskType, syncMode, reserveInd, deleteBeforeLoad, replaceSequences,
						cloneInd, refreshReferenceData, versionInd, scheduler,
						selectionMethod, numOfEntities, "RUN",
						forceEntitesNum, forceSelectionMethod, userEnvs, permissionGroup, testerPermsCache);
			}

			if (!sourceCanRun) {
				decision.sourceEnvDisabled = true;
			}

			if (!targetCanRun) {
				decision.targetEnvDisabled = true;
			}
		}

		if (decision.sourceEnvDisabled && decision.sourceEnvEditable) {
			decision.alternativeSourceEnv = findAlternativeEnv(userName, permissionGroup, EnvType.SOURCE, sourceEnvId,
					targetEnvId,
					taskType, syncMode, taskId, numOfEntities, reserveInd, maxEntitiesEditable, randomEditable, decision.beEditable, beID,
					userEnvs,deleteBeforeLoad,replaceSequences,cloneInd,refreshReferenceData,versionInd,scheduler,selectionMethod, envDisabledCache, testerPermsCache);
			decision.alternativeSourceEnvExists = decision.alternativeSourceEnv != null;
		}

		if (decision.targetEnvDisabled && decision.targetEnvEditable) {
			decision.alternativeTargetEnv = findAlternativeEnv(userName, permissionGroup, EnvType.TARGET, targetEnvId,
					sourceEnvId,
					taskType, syncMode, taskId, numOfEntities, reserveInd, maxEntitiesEditable, randomEditable, decision.beEditable, beID,
					userEnvs,deleteBeforeLoad,replaceSequences,cloneInd,refreshReferenceData,versionInd,scheduler,selectionMethod, envDisabledCache, testerPermsCache);
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
				fetchTasksForQuery(sql.toString(), new Object[] { userName, groupId, userName }, "admin", userEnvs, userName));

		sql = notCreatedByUser();

		taskList.addAll(
				fetchTasksForQuery(sql.toString(), new Object[] { userName, groupId, userName }, "admin", userEnvs, userName));

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
				"owner", userEnvs, userName));

		boolean hasSourceEnvs = false;
		boolean hasTargetEnvs = false;
		for (Map<String, Object> envType : userEnvs) {
			List<Map<String, Object>> allSourceEnvs = (List<Map<String, Object>>) envType.get("source environments");
			List<Map<String, Object>> allTargetEnvs = (List<Map<String, Object>>) envType.get("target environments");

			if (allSourceEnvs != null) {
				for (Map<String, Object> sourceEnvMap : allSourceEnvs) {
					String envId = String.valueOf(sourceEnvMap.get("environment_id"));
					String roleId = String.valueOf(sourceEnvMap.get("role_id"));

					if ("owner".equalsIgnoreCase(roleId)) {
						StringBuilder sourceOwnerQuery = notCreatedByUser();
						sourceOwnerQuery.append(" AND lower(t.task_type) IN ('extract', 'training', 'generate') ");
						sourceOwnerQuery.append(" AND t.source_environment_id = ? ");

						taskList.addAll(fetchTasksForQuery(sourceOwnerQuery.toString(),
								new Object[] { userName, groupId, userName, envId }, "owner", userEnvs, userName));
					} else { // if user env is returned but he is not the owner means he is a tester in the
								// env
						String sourceTesterQuery = getTesterSourceTasks(envId, roleId);
						log.debug("Owner source tester query: {}", sourceTesterQuery);
						taskList.addAll(fetchTasksForQuery(sourceTesterQuery,
								new Object[] { userName, groupId, userName, envId }, "tester", userEnvs, userName));
					}
				}
				if (!allSourceEnvs.isEmpty()) hasSourceEnvs = true;
			}

			if (allTargetEnvs != null) {
				for (Map<String, Object> targetEnvMap : allTargetEnvs) {
					String envId = String.valueOf(targetEnvMap.get("environment_id"));
					String roleId = String.valueOf(targetEnvMap.get("role_id"));

					if ("owner".equalsIgnoreCase(roleId)) {
						StringBuilder targetOwnerQuery = notCreatedByUser();
						targetOwnerQuery
								.append(" AND lower(t.task_type) IN ('load', 'reserve', 'delete', 'ai_generated') ");
						targetOwnerQuery.append(" AND t.environment_id = ? ");

						taskList.addAll(fetchTasksForQuery(targetOwnerQuery.toString(),
								new Object[] { userName, groupId, userName, envId }, "owner", userEnvs, userName));
					} else { // if user env is returned but he is not the owner means he is a tester in the
								// env
						String targetTesterQuery = getTesterTargetTasks(envId, roleId);
						log.debug("Owner target tester query: {}", targetTesterQuery);
						taskList.addAll(fetchTasksForQuery(targetTesterQuery,
								new Object[] { userName, groupId, userName, envId }, "tester", userEnvs, userName));
					}
				}
				if (!allTargetEnvs.isEmpty()) hasTargetEnvs = true;
			}
		}

		if (hasSourceEnvs) {
			StringBuilder editableSourceBEQuery = notCreatedByUser();
			editableSourceBEQuery.append(" AND (t.task_override_fields -> 'source_environment' ->> 'is_editable')::boolean = true")
			.append(" AND (t.task_override_fields -> 'business_entity' ->> 'is_editable')::boolean = true");
			taskList.addAll(fetchTasksForQuery(editableSourceBEQuery.toString(),
					new Object[] { userName, groupId, userName }, "owner", userEnvs, userName));

			StringBuilder editableSourceQuery = notCreatedByUser();
			editableSourceQuery.append(" AND (t.task_override_fields -> 'source_environment' ->> 'is_editable')::boolean = true")
			.append(" AND (t.task_override_fields -> 'business_entity' ->> 'is_editable')::boolean = false");
			taskList.addAll(fetchTasksForQuery(editableSourceQuery.toString(),
					new Object[] { userName, groupId, userName }, "owner", userEnvs, userName));
		}

		if (hasTargetEnvs) {
			StringBuilder editableTargetBEQuery = notCreatedByUser();
			editableTargetBEQuery.append(" AND (t.task_override_fields -> 'target_environment' ->> 'is_editable')::boolean = true")
			.append(" AND (t.task_override_fields -> 'business_entity'    ->> 'is_editable')::boolean = true");
			taskList.addAll(fetchTasksForQuery(editableTargetBEQuery.toString(),
					new Object[] { userName, groupId, userName }, "owner", userEnvs, userName));

			StringBuilder editableTargetQuery = notCreatedByUser();
			editableTargetQuery.append(" AND (t.task_override_fields -> 'target_environment' ->> 'is_editable')::boolean = true")
			.append(" AND (t.task_override_fields -> 'business_entity' ->> 'is_editable')::boolean = false");
			taskList.addAll(fetchTasksForQuery(editableTargetQuery.toString(),
					new Object[] { userName, groupId, userName }, "owner", userEnvs, userName));
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
				"tester", allUserEnvsTypes, userName));

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
							new Object[] { userName, groupId, userName, envId }, "tester", allUserEnvsTypes, userName));
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
							new Object[] { userName, groupId, userName, envId }, "tester", allUserEnvsTypes, userName));
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
		String permessionGroup, List<Map<String, Object>> userEnvs, String userName) throws Exception {
		List<Map<String, Object>> tasks = new ArrayList<>();
		boolean canCreate = isAllowedToCreate(userName);
		boolean isAdmin = "admin".equalsIgnoreCase(permessionGroup);

		// Phase 1: buffer result set and collect task IDs
		List<Map<String, Object>> bufferedRows = new ArrayList<>();
		List<Long> taskIds = new ArrayList<>();
		try (Db.Rows rows = db(TDM).fetch(query, queryParams)) {
			for (Db.Row row : rows) {
				bufferedRows.add(new HashMap<>(row));
				taskIds.add(((Number) row.get("task_id")).longValue());
			}
		}

		// Phase 2: batch-fetch execution permissions for non-admin (eliminates N+1)
		Map<Long, List<String>> permissionsMap = Collections.emptyMap();
		if (!isAdmin && !taskIds.isEmpty()) {
			permissionsMap = fetchPermissionsBatch(taskIds);
		}
		String roles = fnGetUserRoles(userName);
		String[] currentUserRoles = roles != null ? roles.split(TDM_PARAMETERS_SEPARATOR) : new String[0];

		// Phase 3: per-call caches to avoid redundant DB checks across rows
		Map<String, Boolean> envDisabledCache = new HashMap<>();
		Map<String, TesterPermissions> testerPermsCache = new HashMap<>();

		for (Map<String, Object> row : bufferedRows) {
			Long taskId = null;
			String taskTitle = null;
			try {
				taskId = ((Number) row.get("task_id")).longValue();
				taskTitle = String.valueOf(row.get("task_title"));
				String taskExecutionStatus = String.valueOf(row.get("task_execution_status"));
				String taskType = String.valueOf(row.get("task_type"));
				String syncMode = String.valueOf(row.get("sync_mode"));
				String overrideParams = String.valueOf(row.get("task_override_fields"));
				String sourceEnvName = String.valueOf(row.get("source_env_name"));
				String sourceEnvId = row.get("source_environment_id") == null ? null : String.valueOf(row.get("source_environment_id"));
				String targetEnvId = row.get("target_environment_id") == null ? null : String.valueOf(row.get("target_environment_id"));
				String targetEnvName = String.valueOf(row.get("target_env_name"));
				String beId = String.valueOf(row.get("be_id"));
				String updatedDate = String.valueOf(row.get("task_last_updated_date"));
				String selectionMethod = String.valueOf(row.get("selection_method"));
				String taskScheduler = String.valueOf(row.get("scheduler"));
				String taskCreator = String.valueOf(row.get("task_creator"));
				boolean canEdit = isAdmin || userName.equals(taskCreator);
				boolean versionInd = row.get("version_ind") != null
						&& Boolean.parseBoolean(String.valueOf(row.get("version_ind")));

				boolean cloneInd = row.get("clone_ind") != null
						&& Boolean.parseBoolean(String.valueOf(row.get("clone_ind")));

				boolean deleteBeforeLoad = row.get("delete_before_load") != null
						&& Boolean.parseBoolean(String.valueOf(row.get("delete_before_load")));

				boolean replaceSequences = row.get("replace_sequences") != null
						&& Boolean.parseBoolean(String.valueOf(row.get("replace_sequences")));

				boolean refreshReferenceData = row.get("refresh_reference_data") != null
						&& Boolean.parseBoolean(String.valueOf(row.get("refresh_reference_data")));

				boolean reserveInd = row.get("reserve_ind") != null
						&& Boolean.parseBoolean(String.valueOf(row.get("reserve_ind")));

				Long numOfEntities = row.get("num_of_entities") == null ? 0L
						: ((Number) row.get("num_of_entities")).longValue();

				boolean isPermittedUser = isPermittedUserForExecution(taskId, permessionGroup, sourceEnvId, targetEnvId,
						userName, currentUserRoles, permissionsMap, userEnvs);
				if (!isPermittedUser) {
					continue;
				}
				TaskAvailabilityDecision availability = evaluateTaskAvailability(userName, permessionGroup, taskId, taskType,
						syncMode, overrideParams, beId, sourceEnvId, sourceEnvName, targetEnvId, targetEnvName,
						taskExecutionStatus, numOfEntities, reserveInd, userEnvs, selectionMethod, taskScheduler, versionInd,
						refreshReferenceData, cloneInd, replaceSequences, deleteBeforeLoad, envDisabledCache, testerPermsCache);
				if (!showHoldTaskForUser(availability.holdTask, canEdit)) {
					continue;
				}
				Map<String, Object> taskInfo = buildTaskInfo(taskId, taskTitle, taskType, selectionMethod, syncMode,
						availability.holdTask,
						availability.sourceEnvEditable, availability.targetEnvEditable, row.get("favorite"),
						isPermittedUser, updatedDate, canEdit, canCreate);
				taskInfo.put("remove_source_default_env", availability.removeSourceDefaultEnv);
				taskInfo.put("remove_target_default_env", availability.removeTargetDefaultEnv);
				if (availability.alternativeSourceEnvExists) {
					taskInfo.put("alternative_source_env_id", availability.alternativeSourceEnv.environmentId);
					taskInfo.put("alternative_source_env_name", availability.alternativeSourceEnv.environmentName);
				}
				if (availability.alternativeTargetEnvExists) {
					taskInfo.put("alternative_target_env_id", availability.alternativeTargetEnv.environmentId);
					taskInfo.put("alternative_target_env_name", availability.alternativeTargetEnv.environmentName);
				}
				tasks.add(taskInfo);
			} catch (Exception e) {
				throw new RuntimeException("Error processing task"
						+ " [task_id=" + taskId
						+ ", task_title=" + taskTitle
						+ ", user=" + userName
						+ "]: " + e.getMessage(),
						e);
			}
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
    	.append("OR (t.task_override_fields -> 'source_environment' ->> 'is_editable')::boolean = true) ");
		try {
			TesterPermissions permissions = getTesterPermissions(envId, roleId);

			boolean allowRead = permissions.allowRead;
			boolean allowFreshData = permissions.allowedRequestOfFreshData;
			boolean allowVersioning = permissions.allowedEntityVersioning;
			boolean allowScheduling = permissions.allowedTaskScheduling;
			boolean allowRefresh = permissions.allowedRefreshReferenceData;
			boolean allowRandom = permissions.allowedRandomEntitySelection;
			boolean unlimitedEntities = permissions.allowedNumberOfEntitiesToRead == -1;

			queryBuilderSource.append(" AND lower(t.task_type) IN (")
					.append(buildTaskTypes(envId, false, false, false))
					.append(") ");

			if (allowRead) {
				queryBuilderSource.append(" AND 1 = 1 ");
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
					queryBuilderSource.append(" AND (t.selection_method NOT IN ('R', 'PR') ")
					.append("OR (t.task_override_fields -> 'selection_method' ->> 'is_editable')::boolean = true) ");
;
				}
				
				if (!allowRefresh) {
					queryBuilderSource.append(" AND (t.selection_method <> 'TABLES' AND NOT EXISTS (SELECT 1 FROM ")
							.append(TDMDB_SCHEMA)
							.append(".task_ref_tables trt WHERE trt.task_id = t.task_id)) ");
				}
				if( !unlimitedEntities){
					queryBuilderSource.append(" AND (t.num_of_entities <> -1 ")
							.append("OR (t.task_override_fields -> 'selection_method' -> 'max_entities' ->> 'is_editable')::boolean = true) ");
;

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
    	.append("OR (t.task_override_fields -> 'target_environment' ->> 'is_editable')::boolean = true) ");
		try {
			TesterPermissions permissions = getTesterPermissions(envId, roleId);

			boolean allowWrite = permissions.allowWrite;
			boolean allowReplace = permissions.allowedReplaceSequences;
			boolean allowFreshData = permissions.allowedRequestOfFreshData;
			boolean allowVersioning = permissions.allowedEntityVersioning;
			boolean allowCloning = permissions.allowedCreationOfSyntheticData;
			boolean allowScheduling = permissions.allowedTaskScheduling;
			boolean allowRefresh = permissions.allowedRefreshReferenceData;
			boolean allowRandom = permissions.allowedRandomEntitySelection;
			boolean allowReserve = permissions.allowedNumberOfReservedEntities != 0;
			boolean allowDelete = permissions.allowedDeleteBeforeLoad;
			boolean unlimitedEntities = permissions.allowedNumberOfEntitiesToCopy == -1;

			if (allowWrite) {
				queryBuilderTarget.append(" AND 1 = 1 ");
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
					queryBuilderTarget.append(" AND ( t.selection_method NOT IN ('R', 'PR') ")
							.append("OR (t.task_override_fields -> 'selection_method' ->> 'is_editable')::boolean = true) ");
				}

				if (!allowCloning) {
					queryBuilderTarget.append(" AND t.clone_ind = false ");
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
				
				if(!unlimitedEntities){
					queryBuilderTarget.append(" AND (t.num_of_entities <> -1 ")
							.append("OR (t.task_override_fields -> 'selection_method' -> 'max_entities' ->> 'is_editable')::boolean = true) ");
				}

				queryBuilderTarget.append(" AND lower(t.task_type) IN (")
						.append(buildTaskTypes(envId, true, allowDelete,
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
		try (Db.Rows rows = db(TDM).fetch(sql.toString(), taskId)) {
			for (Db.Row row : rows) {
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

	private static boolean isPermittedUserForExecution(long taskId, String permessionGroup, String sourceEnvId, String targetEnvId,
			String currentUser, String[] currentUserRoles, Map<Long, List<String>> permissionsMap,
			List<Map<String, Object>> userEnvs) {
		if ("admin".equalsIgnoreCase(permessionGroup)) return true;
		if ("owner".equalsIgnoreCase(permessionGroup)) {
			if (isOwnerOfEnv(sourceEnvId, userEnvs) || isOwnerOfEnv(targetEnvId, userEnvs)) return true;
		}
		List<String> permittedUsers = permissionsMap.getOrDefault(taskId, Collections.emptyList());
		for (String permittedUser : permittedUsers) {
			if (permittedUser == null) continue;
			if ("ALL".equalsIgnoreCase(permittedUser)) return true;
			if (permittedUser.equalsIgnoreCase(currentUser)) return true;
			for (String role : currentUserRoles) {
				if (permittedUser.equalsIgnoreCase(role)) return true;
			}
		}
		return false;
	}

	private static Map<Long, List<String>> fetchPermissionsBatch(List<Long> taskIds) {
		Map<Long, List<String>> map = new HashMap<>();
		String sql = "SELECT task_id, permitted_user FROM " + TDMDB_SCHEMA
				+ ".task_exe_permissions WHERE task_id = ANY(?)";
		Long[] ids = taskIds.toArray(new Long[0]);
		try (Db.Rows rows = db(TDM).fetch(sql, (Object) ids)) {
			for (Db.Row row : rows) {
				long tid = ((Number) row.get("task_id")).longValue();
				String pu = row.get("permitted_user") == null ? null : row.get("permitted_user").toString();
				map.computeIfAbsent(tid, k -> new ArrayList<>()).add(pu);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error batch-fetching task execution permissions", e);
		}
		return map;
	}

	public static boolean isPermittedUserForStopResume(long taskExeId) {
		String currentUser = sessionUser().name();
		String permessionGroup = fnGetUserPermissionGroup(currentUser);
		boolean isAdmin = permessionGroup.equalsIgnoreCase("admin");
		if (isAdmin) return true;
		StringBuilder sql = new StringBuilder(200);
		sql.append("SELECT task_executed_by ").append("FROM ").append(TDMDB_SCHEMA)
				.append(".task_execution_list where task_execution_id=?");
		try {
			for (Db.Row row : db(TDM).fetch(sql.toString(), taskExeId)) {
				String executed_by = row.get("task_executed_by").toString().split("##")[0];;
				if (executed_by.equalsIgnoreCase(currentUser)){
					return true;
				}
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
		if (normalizedTaskType.contains("TRAINING") || normalizedTaskType.contains("GENERATE")) return true;
		try { if (currentEnvId  != null && Long.parseLong(currentEnvId.trim())  < 0) return true; } catch (NumberFormatException ignored) {}
		try { if (oppositeEnvId != null && Long.parseLong(oppositeEnvId.trim()) < 0) return true; } catch (NumberFormatException ignored) {}
		return false;
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
	
	
	public static Object fnGetPermissionsForTask(String sourceEnvId, String targetEnvId,
        String userName, String taskType, String syncMode, boolean reserveInd) throws Exception {

		Map<String, Object> response = new HashMap<>();
		Map<String, Object> result = new HashMap<>();

		try {
			if (taskType == null || taskType.trim().isEmpty() || "undefined".equalsIgnoreCase(taskType)) {
				if (hasEnv(sourceEnvId) && !hasEnv(targetEnvId)) {
					taskType = "EXTRACT";
				} else {
					taskType = "LOAD";
				}
			}

			if ("EXTRACT".equalsIgnoreCase(taskType) && hasEnv(targetEnvId)) {
				taskType = "LOAD";
			}

			boolean sourceRequired = requiresSourceCheck(taskType, syncMode, sourceEnvId);
			boolean targetRequired = requiresTargetCheck(taskType, targetEnvId);

			String permissionGroup = fnGetUserPermissionGroup(userName);
			Map<String, Object> taskPermissions = new HashMap<>();

			result.put("user_name", userName);
			result.put("permission_group", permissionGroup);
			result.put("source_environment_id", sourceEnvId);
			result.put("target_environment_id", targetEnvId);
			result.put("task_type", taskType);
			result.put("sync_mode", syncMode);
			result.put("reserve_ind", reserveInd);

			boolean isAdmin = "admin".equalsIgnoreCase(permissionGroup);

			boolean ownerAllowed = false;
			if (!isAdmin && "owner".equalsIgnoreCase(permissionGroup)) {
				ownerAllowed =
						(!sourceRequired || (hasEnv(sourceEnvId) && fnIsOwner(sourceEnvId)))
						&& (!targetRequired || (hasEnv(targetEnvId) && fnIsOwner(targetEnvId)));
			}

			if (isAdmin || ownerAllowed) {
				taskPermissions.put("can_run_task", true);
				taskPermissions.put("access_level", isAdmin ? "admin" : "owner");
				taskPermissions.put("can_read", true);
				taskPermissions.put("can_write", true);
				taskPermissions.put("can_reserve", true);
				taskPermissions.put("can_delete_before_load", true);
				taskPermissions.put("can_replace_sequences", true);
				taskPermissions.put("can_run_reference_tasks", true);
				taskPermissions.put("can_use_random_selection", true);
				taskPermissions.put("can_request_fresh_data", true);
				taskPermissions.put("can_schedule_task", true);
				taskPermissions.put("can_use_entity_versioning", true);
				taskPermissions.put("can_use_clone", true);
				taskPermissions.put("allowed_entities_to_read", -1);
				taskPermissions.put("allowed_entities_to_write", -1);
				taskPermissions.put("allowed_entities_to_reserve", -1);
				taskPermissions.put("max_entities_per_task", -1);
				taskPermissions.put("can_request_unlimited_entities", true);

				result.put("task_permissions", taskPermissions);
				response.put("errorCode", "SUCCESS");
				response.put("message", "Task permissions were retrieved successfully");
				response.put("result", result);
				return response;
			}

			List<Map<String, Object>> userEnvs = fnGetUserEnvs(userName);
			Map<String, TesterPermissions> localPermsCache = new HashMap<>();

			TesterPermissions sourcePerms = sourceRequired
					? getTesterPermissionsForUserEnv(userName, sourceEnvId, EnvType.SOURCE, userEnvs, localPermsCache)
					: null;

			TesterPermissions targetPerms = targetRequired
					? getTesterPermissionsForUserEnv(userName, targetEnvId, EnvType.TARGET, userEnvs, localPermsCache)
					: null;

			long allowedToRead = sourcePerms == null ? 0 : sourcePerms.allowedNumberOfEntitiesToRead;
			long allowedToWrite = targetPerms == null ? 0 : targetPerms.allowedNumberOfEntitiesToCopy;
			long allowedToReserve = targetPerms == null ? 0 : targetPerms.allowedNumberOfReservedEntities;

			long maxEntitiesPerTask;

			if (sourceRequired && targetRequired) {
				long targetLimit = reserveInd
						? maxEntitiesLimit(allowedToWrite, allowedToReserve)
						: allowedToWrite;

				if (allowedToRead == -1 && targetLimit == -1) {
					maxEntitiesPerTask = -1;
				} else if (allowedToRead == -1) {
					maxEntitiesPerTask = targetLimit;
				} else if (targetLimit == -1) {
					maxEntitiesPerTask = allowedToRead;
				} else {
					maxEntitiesPerTask = Math.min(allowedToRead, targetLimit);
				}
			} else if (sourceRequired) {
				maxEntitiesPerTask = allowedToRead;
			} else if (targetRequired) {
				if("RESERVE".equalsIgnoreCase(taskType)) {
					maxEntitiesPerTask = allowedToReserve;
				} else {
					maxEntitiesPerTask = reserveInd
							? maxEntitiesLimit(allowedToWrite, allowedToReserve)
							: allowedToWrite;
				}

			} else {
				maxEntitiesPerTask = 0;
			}
			taskPermissions.put("access_level", "tester");

			taskPermissions.put("can_read", sourcePerms != null && sourcePerms.allowRead);
			taskPermissions.put("can_write", targetPerms != null && targetPerms.allowWrite);
			taskPermissions.put("can_reserve", targetPerms != null && targetPerms.allowedNumberOfReservedEntities != 0);

			taskPermissions.put("allowed_entities_to_read", allowedToRead);
			taskPermissions.put("allowed_entities_to_write", allowedToWrite);
			taskPermissions.put("allowed_entities_to_reserve", allowedToReserve);

			taskPermissions.put("max_entities_per_task", maxEntitiesPerTask);
			taskPermissions.put("can_request_unlimited_entities", maxEntitiesPerTask == -1);

			taskPermissions.put("can_use_random_selection", checkPermission(
					sourceRequired, targetRequired,
					sourcePerms != null && sourcePerms.allowedRandomEntitySelection,
					targetPerms != null && targetPerms.allowedRandomEntitySelection));

			taskPermissions.put("can_request_fresh_data", checkPermission(
					sourceRequired, targetRequired,
					sourcePerms != null && sourcePerms.allowedRequestOfFreshData,
					targetPerms != null && targetPerms.allowedRequestOfFreshData));

			taskPermissions.put("can_schedule_task", checkPermission(
					sourceRequired, targetRequired,
					sourcePerms != null && sourcePerms.allowedTaskScheduling,
					targetPerms != null && targetPerms.allowedTaskScheduling));

			taskPermissions.put("can_run_reference_tasks", checkPermission(
					sourceRequired, targetRequired,
					sourcePerms != null && sourcePerms.allowedRefreshReferenceData,
					targetPerms != null && targetPerms.allowedRefreshReferenceData));

			taskPermissions.put("can_use_entity_versioning", checkPermission(
					sourceRequired, targetRequired,
					sourcePerms != null && sourcePerms.allowedEntityVersioning,
					targetPerms != null && targetPerms.allowedEntityVersioning));

			taskPermissions.put("can_delete_before_load",
					targetPerms != null && targetPerms.allowedDeleteBeforeLoad);

			taskPermissions.put("can_replace_sequences",
					targetPerms != null && targetPerms.allowedReplaceSequences);

			taskPermissions.put("can_use_clone",
					targetPerms != null && targetPerms.allowedCreationOfSyntheticData);

			result.put("task_permissions", taskPermissions);

			response.put("errorCode", "SUCCESS");
			response.put("message", "Task permissions were retrieved successfully");
			response.put("result", result);

		} catch (Exception e) {
			response.put("errorCode", "FAILED");
			response.put("message", e.getMessage());
			response.put("result", null);
		}

		return response;
	}

	private static boolean checkPermission(boolean sourceRequired, boolean targetRequired, boolean sourcePermission, boolean targetPermission) {
    	return (!sourceRequired || sourcePermission) && (!targetRequired || targetPermission);
	}

	private static boolean showHoldTaskForUser(boolean holdTask, boolean canEdit) {
		return !holdTask || canEdit;
	}
	
	public static boolean canUserPerformTaskOperation(String userName, String sourceEnvId, String targetEnvId,
        String taskType, String syncMode, boolean reserveInd, boolean deleteBeforeLoad,
        boolean replaceSequences, boolean cloneInd, boolean refreshReferenceData, boolean versionInd,
        String scheduler, String selectionMethod, long numOfEntities, String operation, boolean enforceEntityLimit, boolean enforceSelectionMethod,
		List<Map<String, Object>> userEnvs) throws Exception {
		return canUserPerformTaskOperation(userName, sourceEnvId, targetEnvId, taskType, syncMode, reserveInd,
				deleteBeforeLoad, replaceSequences, cloneInd, refreshReferenceData, versionInd, scheduler,
				selectionMethod, numOfEntities, operation, enforceEntityLimit, enforceSelectionMethod, userEnvs,
				fnGetUserPermissionGroup(userName), new HashMap<>());
	}

	private static boolean canUserPerformTaskOperation(String userName, String sourceEnvId, String targetEnvId,
        String taskType, String syncMode, boolean reserveInd, boolean deleteBeforeLoad,
        boolean replaceSequences, boolean cloneInd, boolean refreshReferenceData, boolean versionInd,
        String scheduler, String selectionMethod, long numOfEntities, String operation, boolean enforceEntityLimit, boolean enforceSelectionMethod,
		List<Map<String, Object>> userEnvs, String permissionGroup,
		Map<String, TesterPermissions> testerPermsCache) throws Exception {

		if ("admin".equalsIgnoreCase(permissionGroup)) {
			return true;
		}

		boolean sourceRequired = requiresSourceCheck(taskType, syncMode, sourceEnvId);
		boolean targetRequired = requiresTargetCheck(taskType, targetEnvId);

		if ("owner".equalsIgnoreCase(permissionGroup)) {
			List<Map<String, Object>> ownerEnvs = userEnvs != null ? userEnvs : fnGetUserEnvs(userName);
			return (!sourceRequired || (hasEnv(sourceEnvId) && isOwnerOfEnv(sourceEnvId, ownerEnvs)))
					&& (!targetRequired || (hasEnv(targetEnvId) && isOwnerOfEnv(targetEnvId, ownerEnvs)));
		}

		if ("CREATE".equalsIgnoreCase(operation) && !isAllowedToCreate(userName)) {
			return false;
		}

		List<Map<String, Object>> resolvedUserEnvs =
        userEnvs != null ? userEnvs : fnGetUserEnvs(userName);

		TesterPermissions sourcePerms = sourceRequired
				? getTesterPermissionsForUserEnv(userName, sourceEnvId, EnvType.SOURCE, resolvedUserEnvs, testerPermsCache)
				: null;

		TesterPermissions targetPerms = targetRequired
				? getTesterPermissionsForUserEnv(userName, targetEnvId, EnvType.TARGET, resolvedUserEnvs, testerPermsCache)
				: null;

		long allowedToRead = sourcePerms == null ? 0 : sourcePerms.allowedNumberOfEntitiesToRead;
		long allowedToWrite = targetPerms == null ? 0 : targetPerms.allowedNumberOfEntitiesToCopy;
		long allowedToReserve = targetPerms == null ? 0 : targetPerms.allowedNumberOfReservedEntities;

		if (enforceEntityLimit) {
			boolean canRunEntities = true;

			if (sourceRequired && targetRequired) {
				canRunEntities = canTesterRunTaskEntities(
						sourceEnvId, targetEnvId, taskType, numOfEntities,
						allowedToRead, allowedToWrite, allowedToReserve,
						syncMode, reserveInd);
			} else if (sourceRequired) {
				canRunEntities = canTesterRunTaskEntitiesForSide(
						EnvType.SOURCE, sourceEnvId,targetEnvId, taskType, numOfEntities,
						allowedToRead, allowedToWrite, allowedToReserve,
						syncMode, reserveInd);
			} else if (targetRequired) {
				canRunEntities = canTesterRunTaskEntitiesForSide(
						EnvType.TARGET, sourceEnvId,targetEnvId, taskType, numOfEntities,
						allowedToRead, allowedToWrite, allowedToReserve,
						syncMode, reserveInd);
			}

			if (!canRunEntities) {
				return false;
			}
		}


		if (sourceRequired && (sourcePerms == null || !sourcePerms.allowRead)) {
			return false;
		}

		if (targetRequired && (targetPerms == null || !targetPerms.allowWrite)) {
			return false;
		}

		boolean sourceAllowRandom = sourcePerms != null && sourcePerms.allowedRandomEntitySelection;
		boolean targetAllowRandom = targetPerms != null && targetPerms.allowedRandomEntitySelection;

		boolean allowRandom =
				(!sourceRequired || sourceAllowRandom)
				&& (!targetRequired || targetAllowRandom);

		boolean sourceAllowFreshData = sourcePerms != null && sourcePerms.allowedRequestOfFreshData;
		boolean targetAllowFreshData = targetPerms != null && targetPerms.allowedRequestOfFreshData;

		boolean allowFreshData =
				(!sourceRequired || sourceAllowFreshData)
				&& (!targetRequired || targetAllowFreshData);

		boolean sourceAllowScheduling = sourcePerms != null && sourcePerms.allowedTaskScheduling;
		boolean targetAllowScheduling = targetPerms != null && targetPerms.allowedTaskScheduling;

		boolean allowScheduling =
				(!sourceRequired || sourceAllowScheduling)
				&& (!targetRequired || targetAllowScheduling);

		boolean sourceAllowRefreshReference = sourcePerms != null && sourcePerms.allowedRefreshReferenceData;
		boolean targetAllowRefreshReference = targetPerms != null && targetPerms.allowedRefreshReferenceData;

		boolean allowRefreshReference =
				(!sourceRequired || sourceAllowRefreshReference)
				&& (!targetRequired || targetAllowRefreshReference);

		boolean sourceAllowVersioning = sourcePerms != null && sourcePerms.allowedEntityVersioning;
		boolean targetAllowVersioning = targetPerms != null && targetPerms.allowedEntityVersioning;

		boolean allowVersioning =
				(!sourceRequired || sourceAllowVersioning)
				&& (!targetRequired || targetAllowVersioning);


		if (!allowFreshData && "FORCE".equalsIgnoreCase(syncMode)) {
			return false;
		}

		if (!allowScheduling && scheduler != null && !"immediate".equalsIgnoreCase(scheduler)) {
			return false;
		}

		if (!allowRandom && ("R".equalsIgnoreCase(selectionMethod) || "PR".equalsIgnoreCase(selectionMethod)) && enforceSelectionMethod) {
			return false;
		}

		if (!allowRefreshReference && refreshReferenceData) {
			return false;
		}

		if (!allowVersioning && versionInd) {
			return false;
		}

		if (targetPerms != null && !targetPerms.allowedReplaceSequences && replaceSequences) {
			return false;
		}

		if (targetPerms != null && !targetPerms.allowedCreationOfSyntheticData && cloneInd) {
			return false;
		}

		if (targetPerms != null && !targetPerms.allowedDeleteBeforeLoad && deleteBeforeLoad) {
			return false;
		}

		if (reserveInd && (targetPerms == null || targetPerms.allowedNumberOfReservedEntities == 0)) {
			return false;
		}

		return true;
	}

	public static boolean isAllowedTestConnFailure(String envId) throws Exception {
		String userName = sessionUser().name();
		String permessionGroup = fnGetUserPermissionGroup(userName);
		if ("tester".equalsIgnoreCase(permessionGroup)) {

			List<Map<String, Object>> userEnvs = fnGetUserEnvs(userName);

			for (Map<String, Object> envType : userEnvs) {
				for (String key : Arrays.asList("source environments", "target environments")) {
					List<Map<String, Object>> envs = (List<Map<String, Object>>) envType.get(key);
					if (envs == null) {
						continue;
					}
					for (Map<String, Object> envMap : envs) {
						if (envId.equals(String.valueOf(envMap.get("environment_id")))) {
							String roleId = String.valueOf(envMap.get("role_id"));

							return getTesterPermissions(envId, roleId).allowedTestConnFailure;
						}
					}
				}
			}
		}
		return true;
	}

}