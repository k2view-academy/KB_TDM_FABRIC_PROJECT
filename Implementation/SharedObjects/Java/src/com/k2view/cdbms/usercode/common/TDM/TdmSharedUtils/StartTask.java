package com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils;

import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnCreateSummaryRecord;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetActiveTaskForActivation;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetNextTaskExecution;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetTasks;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnInsertActivity;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnIsTaskActive;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnSaveRefExeTablestoTask;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnSaveTaskOverrideParameters;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnStartTaskExecutions;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.canUserPerformTaskOperation;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.requiresSourceCheck;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.requiresTargetCheck;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.validateEnvTypeForUser;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetLogicalUnitsByEnvironmentAndBusinessentity;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getGlobalMaxWorkersLimit;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.OverrideParamKey;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.StartTaskValidator.TdmValidationException;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.StartTaskValidator.TdmWarningException;
import com.k2view.fabric.common.Json;

@SuppressWarnings({ "DefaultAnnotationParam", "unchecked", "rawtypes" })
public class StartTask {

	private static final String TDM = "TDM";
	private static final String TABLES = "TABLES";

	public static Object perform(Long taskId, Boolean forced, Map<OverrideParamKey, Object> inputOverrides)
			throws Exception {
		return perform(taskId, forced, inputOverrides, null);
	}

	public static Object perform(Long taskId, Boolean forced, Map<OverrideParamKey, Object> inputOverrides,
			Long draftExecutionId) throws Exception {
		try {
			StartTaskValidator.checkSystemHealth();
			Db.Row taskRow = fetchTaskData(taskId);
			Map<String, Boolean> taskGlobalsEditability = new HashMap<>();
			if (inputOverrides.containsKey(OverrideParamKey.TASK_GLOBALS)) {
				try (Db.Rows globalsRows = db(TDM).fetch(
						"SELECT global_name, is_editable FROM " + TDMDB_SCHEMA + ".task_globals WHERE task_id = ?", taskId)) {
					for (Db.Row r : globalsRows) {
						taskGlobalsEditability.put(r.get("global_name").toString(),
								Boolean.TRUE.equals(r.get("is_editable")));
					}
				}
			}
			Map<String, Boolean> generateParamsEditability = new HashMap<>();
			if (inputOverrides.containsKey(OverrideParamKey.GENERATE_DATA_PARAMS)) {
				try (Db.Rows genRows = db(TDM).fetch(
						"SELECT param_name, is_editable FROM " + TDMDB_SCHEMA
						+ ".tdm_generate_task_field_mappings WHERE task_id = ?", taskId)) {
					for (Db.Row r : genRows) {
						generateParamsEditability.put(r.get("param_name").toString(),
								Boolean.TRUE.equals(r.get("is_editable")));
					}
				}
			}
			StartTaskValidator.validateInputOverrides(taskRow, inputOverrides, taskGlobalsEditability, generateParamsEditability);

			Map<String, Object> overrideParams = new HashMap<>();
			Map<String, Object> context = resolveTaskContext(taskRow, inputOverrides, overrideParams);
			if (Boolean.TRUE.equals(context.get("envOverridden")))
				overrideParams.put(OverrideParamKey.IMPLICIT_OVERRIDE_LOGICAL_UNITS.name(), context.get("logicalUnits"));

			String userName = UserCode.sessionUser().name();
			if ("TDM.tdmTaskScheduler".equalsIgnoreCase(userName)) {
				userName=fnGetTaskCreatedBy(taskId);
			}
			
			boolean sourceRequired = requiresSourceCheck(String.valueOf(taskRow.get("task_type")), String.valueOf(taskRow.get("sync_mode")), String.valueOf(context.get("srcEnvId")));
			boolean targetRequired = requiresTargetCheck(String.valueOf(taskRow.get("task_type")), String.valueOf(context.get("tarEnvId")));
			if (sourceRequired) {
				String envMsg = validateEnvTypeForUser((Long) context.get("srcEnvId"), (String) context.get("sourceEnvName"), "SOURCE", userName);
				if (!envMsg.isEmpty())
					throw new TdmValidationException("Environment Validation", envMsg);
			}
			if (targetRequired) {
				String envMsg = validateEnvTypeForUser((Long) context.get("tarEnvId"), (String) context.get("targetEnvName"), "TARGET", userName);
				if (!envMsg.isEmpty())
					throw new TdmValidationException("Environment Validation", envMsg);
			}

			boolean canRunTask = canUserPerformTaskOperation(
					userName,
					context.get("srcEnvId") == null ? null : String.valueOf(context.get("srcEnvId")),
					context.get("tarEnvId") == null ? null : String.valueOf(context.get("tarEnvId")),
					String.valueOf(taskRow.get("task_type")),
					String.valueOf(taskRow.get("sync_mode")),
					context.get("reserveInd") != null && Boolean.parseBoolean(String.valueOf(context.get("reserveInd"))),
					taskRow.get("delete_before_load") != null && Boolean.parseBoolean(String.valueOf(taskRow.get("delete_before_load"))),
					taskRow.get("replace_sequences") != null && Boolean.parseBoolean(String.valueOf(taskRow.get("replace_sequences"))),
					taskRow.get("clone_ind") != null && Boolean.parseBoolean(String.valueOf(taskRow.get("clone_ind"))),
					taskRow.get("refresh_reference_data") != null && Boolean.parseBoolean(String.valueOf(taskRow.get("refresh_reference_data"))),
					taskRow.get("version_ind") != null && Boolean.parseBoolean(String.valueOf(taskRow.get("version_ind"))),
					String.valueOf(taskRow.get("scheduler")),
					String.valueOf(context.get("finalSelectionMethod")),
					context.get("finalCount") == null ? 0L : Long.parseLong(String.valueOf(context.get("finalCount"))),
					"RUN",true,true,null
			);

			if (!canRunTask) {
				throw new TdmValidationException(
					"Permissions Validation",
                    "User " + userName + " is not allowed to run this task based on the assigned permissions."
				);
			}

			StartTaskValidator.validateBeforeRun(taskRow, context, inputOverrides, overrideParams);
			StartTaskValidator.verifyUserPermissions(taskId, taskRow, context, overrideParams);
			StartTaskValidator.validateConnectivity(taskId, forced, context, taskRow);
			StartTaskValidator.validateReservedEntityList(forced, taskRow, context, overrideParams);
			StartTaskValidator.validateEnvsForTaskWithDelete(taskRow, context);

			Long taskExecutionId = initiateExecution(taskId, taskRow, context, overrideParams, inputOverrides,
					draftExecutionId);
			return buildSuccessResponse(taskExecutionId);

		} catch (TdmWarningException e) {
			return wrapWebServiceResults("WARNING", e.getCategory(), e.getErrorDetails());
		} catch (TdmValidationException e) {
			return wrapWebServiceResults("FAILED", e.getCategory(), e.getErrorDetails());
		} catch (Exception e) {
			return wrapWebServiceResults("FAILED", e.getMessage(), null);
		}
	}

	private static Db.Row fetchTaskData(Long taskId) throws Exception {
		Db.Row taskRow = fnGetTasks(taskId.toString(), "Active").firstRow();
		if (taskRow == null)
			throw new TdmValidationException("Task Validation","Task not found");
		if (!fnIsTaskActive(taskId))
			throw new TdmValidationException("Task Validation","Task is not active");
		return taskRow;
	}

	private static Map<String, Object> resolveTaskContext(Db.Row taskRow, Map<OverrideParamKey, Object> inputOverrides,
			Map<String, Object> overrideParams) throws Exception {
		Map<String, Object> context = new HashMap<>();

		String taskType = "" + taskRow.get("TASK_TYPE");
		String srcEnv = getVal(inputOverrides, OverrideParamKey.SOURCE_ENVIRONMENT_NAME, taskRow, "source_env_name");
		String tarEnv = getVal(inputOverrides, OverrideParamKey.TARGET_ENVIRONMENT_NAME, taskRow, "environment_name");

		if ("EXTRACT".equalsIgnoreCase(taskType)) {
			tarEnv = srcEnv;
		}

		if ("RESERVE".equalsIgnoreCase(taskType) || "DELETE".equalsIgnoreCase(taskType) || Boolean.TRUE.equals(taskRow.get("in_place_masking_ind"))) {
			srcEnv = tarEnv;
		}
		if (isNullOrEmpty(srcEnv))
			throw new TdmValidationException("Task Configuration Validation",
					"Source environment is not configured for this task.");
		if (isNullOrEmpty(tarEnv))
			throw new TdmValidationException("Task Configuration Validation",
					"Target environment is not configured for this task.");
		context.put("sourceEnvName", srcEnv);
		context.put("targetEnvName", tarEnv);
		context.put("srcEnvId", fetchEnvId(srcEnv));
		context.put("tarEnvId", fetchEnvId(tarEnv));

		if (inputOverrides.get(OverrideParamKey.SOURCE_ENVIRONMENT_NAME) != null)
			overrideParams.put(OverrideParamKey.SOURCE_ENVIRONMENT_NAME.name(), srcEnv);
		if (inputOverrides.get(OverrideParamKey.TARGET_ENVIRONMENT_NAME) != null)
			overrideParams.put(OverrideParamKey.TARGET_ENVIRONMENT_NAME.name(), tarEnv);

		Boolean reserveInd = inputOverrides.containsKey(OverrideParamKey.RESERVE_IND)
				? (Boolean) inputOverrides.get(OverrideParamKey.RESERVE_IND)
				: (Boolean) taskRow.get("reserve_ind");
		context.put("reserveInd", reserveInd);
		if (inputOverrides.containsKey(OverrideParamKey.RESERVE_IND))
			overrideParams.put(OverrideParamKey.RESERVE_IND.name(), reserveInd);

		Map<String, String> taskGlobals = (Map<String, String>) inputOverrides.get(OverrideParamKey.TASK_GLOBALS);
		if (taskGlobals != null)
			overrideParams.put(OverrideParamKey.TASK_GLOBALS.name(), taskGlobals);

		resolveSelectionAndEntities(taskRow, inputOverrides, context, overrideParams);
		resolveTaskScope(taskRow, inputOverrides, context, overrideParams);

		return context;
	}

	private static void resolveSelectionAndEntities(Db.Row taskRow, Map<OverrideParamKey, Object> inputOverrides,
			Map<String, Object> context, Map<String, Object> overrideParams) throws Exception {
		String overrideMethod = (String) inputOverrides.get(OverrideParamKey.SELECTION_METHOD);
		String entitiesList = (String) inputOverrides.get(OverrideParamKey.ENTITY_LIST);
		Integer numEntitiesOverride = null;
		if (inputOverrides.containsKey(OverrideParamKey.NO_OF_ENTITIES)) {
			Object value = inputOverrides.get(OverrideParamKey.NO_OF_ENTITIES);
			if (value == null) {
				throw new TdmValidationException("Invalid input", "NO_OF_ENTITIES override cannot be null/empty.");
			}
			numEntitiesOverride = ((Long) value).intValue();
		}

		String taskMethod = "" + taskRow.get("selection_method");
		if (overrideMethod == null && isNullOrEmpty(taskMethod))
			throw new TdmValidationException("Task Configuration Validation",
					"Selection method is not configured for this task.");
		String finalMethod = (overrideMethod != null) ? overrideMethod : taskMethod;
		Integer finalCount = null;

		// Implicit 'L' when entity list is provided without an explicit method override
		if (entitiesList != null) {
			if (overrideMethod == null) {
				overrideMethod = "L";
				finalMethod = "L";
			} else if (!"L".equalsIgnoreCase(overrideMethod)) {
				throw new TdmValidationException("Selection Method Validation","Entities list provided but method is not 'Entity list'.");
			}
		}

		if (overrideMethod != null) {
			switch (finalMethod.toUpperCase()) {
				case "L":
					if (entitiesList == null || entitiesList.isBlank())
						throw new TdmValidationException("Selection Method Validation","Entities list must be provided for 'Entity list' selection method override.");
					String[] trimmedEntries = Arrays.stream(entitiesList.split(","))
							.map(String::trim).filter(s -> !s.isEmpty()).sorted().toArray(String[]::new);
					if (trimmedEntries.length == 0)
						throw new TdmValidationException("Selection Method Validation","Entities list must be provided for 'Entity list' selection method override.");
					finalCount = trimmedEntries.length;
					overrideParams.put(OverrideParamKey.ENTITY_LIST.name(), String.join(",", trimmedEntries));
					break;
				case "ALL":
					if (numEntitiesOverride != null && numEntitiesOverride != -1)
						throw new TdmValidationException("Selection Method Validation","Number of entities must be 'all' for 'Predefined entity list' selection.");
					finalCount = -1;
					break;
				case "C":
					if (numEntitiesOverride != null) {
						finalCount = numEntitiesOverride;
					} else {
						// No override count — use the task's configured num_of_entities.
						// For Custom Logic, 0 is valid and means "process all entities".
						Object taskCount = taskRow.get("num_of_entities");
						if (taskCount == null)
							throw new TdmValidationException("Selection Method Validation",
									"Number of entities must be provided for 'Custom logic' selection method override.");
						finalCount = Integer.parseInt(taskCount.toString());
					}
					break;
				case "R":
					if (numEntitiesOverride != null) {
						finalCount = numEntitiesOverride;
					} else {
						Object taskCount = taskRow.get("num_of_entities");
						int taskCountVal = taskCount == null ? 0 : Integer.parseInt(taskCount.toString());
						if (taskCountVal <= 0)
							throw new TdmValidationException("Selection Method Validation",
									"Number of entities must be provided for '"
											+ selectionMethodDisplayName(finalMethod)
											+ "' selection method override.");
						finalCount = taskCountVal;
					}
					break;
				default:
					throw new TdmValidationException("Selection Method Validation","Invalid selection method '" + selectionMethodDisplayName(finalMethod)
							+ "' provided for override. Only Entity list, Custom logic, Random and Predefined entity list are allowed.");
			}
		} else {
			if ("L".equalsIgnoreCase(finalMethod)) {
				String list = "" + taskRow.get("selection_param_value");
				if (isNullOrEmpty(list))
					throw new TdmValidationException("Selection Method Validation","Selection method is 'Entity list', but no entity list is defined.");
				finalCount = list.split(",").length;
			}
			// for other task methods, finalCount stays null and is resolved by clone_ind
			// block below
		}

		if (finalCount == null || (Boolean) taskRow.get("clone_ind")) {
			finalCount = (numEntitiesOverride != null) ? numEntitiesOverride
					: Integer.parseInt(taskRow.get("num_of_entities").toString());
		}

		if ("GENERATE".equalsIgnoreCase("" + taskRow.get("selection_method")) && (finalCount == null || finalCount <= 0)) {
			throw new TdmValidationException("Task Configuration Validation",
				"Number of entities must be greater than 0 for rule-based generate tasks.");
		}

		if (overrideMethod != null && !overrideMethod.equalsIgnoreCase(taskMethod))
			overrideParams.put(OverrideParamKey.SELECTION_METHOD.name(), finalMethod);
		if (numEntitiesOverride != null)
			overrideParams.put(OverrideParamKey.NO_OF_ENTITIES.name(), numEntitiesOverride);

		handleSpecialParams(finalMethod, taskRow, inputOverrides, overrideParams);
		context.put("finalSelectionMethod", finalMethod);
		context.put("finalCount", finalCount);
	}

	private static String selectionMethodDisplayName(String code) {
		if (code == null) return code;
		switch (code.toUpperCase()) {
			case "L":   return "Entity list";
			case "ALL": return "Predefined entity list";
			case "C":   return "Custom logic";
			case "R":   return "Random";
			default:    return code;
		}
	}

	private static void handleSpecialParams(String method, Db.Row taskRow,
			Map<OverrideParamKey, Object> inputOverrides, Map<String, Object> overrideParams) throws Exception {
		String m = method.toUpperCase();

		if ("C".equals(m)) {
			String overrideFlow = (String) inputOverrides.get(OverrideParamKey.CUSTOM_LOGIC_FLOW);
			String taskFlow = "" + taskRow.get("selection_param_value");
			String effectiveFlow = (overrideFlow != null && !overrideFlow.isBlank()) ? overrideFlow : taskFlow;
			if (effectiveFlow == null || effectiveFlow.isBlank() || "null".equals(effectiveFlow))
				throw new TdmValidationException("Custom Logic Validation", "Custom logic flow name must be provided.");

			String overrideLuName = (String) inputOverrides.get(OverrideParamKey.CUSTOM_LOGIC_LU_NAME);
			String taskLuName = "" + taskRow.get("custom_logic_lu_name");
			String effectiveLuName = (overrideLuName != null && !overrideLuName.isBlank()) ? overrideLuName
					: taskLuName;
			if (effectiveLuName == null || effectiveLuName.isBlank() || "null".equals(effectiveLuName))
				throw new TdmValidationException("Custom Logic Validation", "Custom logic LU name must be provided.");
			// Handle Custom Logic parameters
			putIfPresent(inputOverrides, OverrideParamKey.CUSTOM_LOGIC_FLOW, overrideParams);
			putIfPresent(inputOverrides, OverrideParamKey.CUSTOM_LOGIC_LU_NAME, overrideParams);
			putIfPresent(inputOverrides, OverrideParamKey.PARAMETERS, overrideParams);

		} else if ("P".equals(m) || "PR".equals(m)) {
			// Handle Business Process / Query parameters
			putIfPresent(inputOverrides, OverrideParamKey.BP_QUERY, overrideParams);
			putIfPresent(inputOverrides, OverrideParamKey.PARAMETERS, overrideParams);

		} else if ("GENERATE".equals(m)) {
			// Handle Synthetic Data Generation parameters (JSON conversion)
			Map<String, Object> genData = (Map<String, Object>) inputOverrides
					.get(OverrideParamKey.GENERATE_DATA_PARAMS);
			if (genData != null) {
				Map<String, Object> processedGenParams = new HashMap<>();
				for (String key : genData.keySet()) {
					Map<String, Object> entry = (Map<String, Object>) genData.get(key);
					if (entry != null && entry.containsKey("value")) {
						processedGenParams.put(key, entry.get("value"));
					}
				}
				overrideParams.put(OverrideParamKey.GENERATE_DATA_PARAMS.name(), processedGenParams);
			}
		}
	}

	private static void resolveTaskScope(Db.Row taskRow, Map<OverrideParamKey, Object> inputOverrides,
			Map<String, Object> context, Map<String, Object> overrideParams) throws Exception {
		String beId = (String) inputOverrides.get(OverrideParamKey.BE_ID);
		List<Map<String, Object>> lus = (List<Map<String, Object>>) inputOverrides.get(OverrideParamKey.LOGICAL_UNITS);
		String finalBeId = (beId != null) ? beId : "" + taskRow.get("be_id");

		putIfPresent(inputOverrides, OverrideParamKey.EXECUTION_NOTE, overrideParams);

		// table level task should skip all the LU Logic
		if ("-1".equals(finalBeId)) {
			Map<String, Object> beScope = new HashMap<>();
			beScope.put("be_id", finalBeId);
			beScope.put("LU List", new ArrayList<>());
			context.put("logicalUnits", new ArrayList<>());
			context.put("finalBeId", finalBeId);
			context.put("beScope", beScope);
			return;
		}

		if (isNullOrEmpty(finalBeId)) {
			throw new TdmValidationException("Task Configuration Validation",
				"Business entity is not configured for this task.");
		}

		if (beId != null) {
			if (taskRow.get("be_id") != null && !"null".equals("" + taskRow.get("be_id")))
				throw new TdmValidationException("Business Entity Validation","BE cannot be overridden.");
			if (lus == null) {
				String taskTypeForEnv = "" + taskRow.get("task_type");
				Long envId = ("load".equalsIgnoreCase(taskTypeForEnv) || "reserve".equalsIgnoreCase(taskTypeForEnv))
						? (Long) context.get("tarEnvId")
						: (Long) context.get("srcEnvId");
				Map<String, Object> r = (Map<String, Object>) fnGetLogicalUnitsByEnvironmentAndBusinessentity(
						Long.parseLong(beId), envId);
				if ("FAILED".equals(r.get("errorCode")))
					throw new TdmValidationException("Business Entity Validation","Failed to fetch logical units for BE " + beId + ": " + r.get("message"));
				lus = (List<Map<String, Object>>) r.get("result");
			}
			if (lus == null || lus.isEmpty())
				throw new TdmValidationException("Business Entity Validation","Logical units cannot be empty.");
			context.put("overrideBeId", beId);
			overrideParams.put(OverrideParamKey.BE_ID.name(), beId);
		}

		// When env is overridden (no BE override), LOGICAL_UNITS carries workers/affinity
		// values only — not the LU scope. Clear lus so the env-override branch below
		// builds the scope from the new environment. inputOverrides.get(LOGICAL_UNITS) is
		// untouched, so per-LU workers/affinity overrides are still applied in step 4,
		// but only to LUs that exist in the new env's executions.
		String taskTypeForEnvCheck = ("" + taskRow.get("task_type")).toUpperCase();
		boolean isExtractType = taskTypeForEnvCheck.equals("EXTRACT") || taskTypeForEnvCheck.equals("GENERATE")
				|| taskTypeForEnvCheck.equals("TRAINING") || taskTypeForEnvCheck.equals("AI_GENERATED");
		boolean isTargetOnlyType = taskTypeForEnvCheck.equals("RESERVE") || taskTypeForEnvCheck.equals("DELETE")
				|| Boolean.TRUE.equals(taskRow.get("in_place_masking_ind"));

		if (beId == null && lus != null) {
			Long newSrcEnvId = (Long) context.get("srcEnvId");
			Long newTarEnvId = (Long) context.get("tarEnvId");
			boolean srcChanged = !isTargetOnlyType && newSrcEnvId != null && !newSrcEnvId.equals(toLong(taskRow.get("source_environment_id")));
			boolean tarChanged = !isExtractType   && newTarEnvId != null && !newTarEnvId.equals(toLong(taskRow.get("environment_id")));
			if (srcChanged || tarChanged)
				lus = null;
		}

		List<String> luIds = new ArrayList<>();
		if (lus == null || lus.isEmpty()) {
			lus = new ArrayList<>();

			Long origSrcEnvId = toLong(taskRow.get("source_environment_id"));
			Long origTarEnvId = toLong(taskRow.get("environment_id"));
			Long newSrcEnvId  = (Long) context.get("srcEnvId");
			Long newTarEnvId  = (Long) context.get("tarEnvId");
			boolean srcChanged = !isTargetOnlyType && newSrcEnvId != null && !newSrcEnvId.equals(origSrcEnvId);
			boolean tarChanged = !isExtractType   && newTarEnvId != null && !newTarEnvId.equals(origTarEnvId);

			if (beId == null && (srcChanged || tarChanged) && finalBeId != null && !"null".equals(finalBeId)) {
				// Env is overridden — derive LU list from the new environment for this BE.
				boolean isSourceTask = isExtractType;

				Long primaryEnvId = isSourceTask ? newSrcEnvId : newTarEnvId;
				Map<String, Object> envResult = (Map<String, Object>)
						fnGetLogicalUnitsByEnvironmentAndBusinessentity(Long.parseLong(finalBeId), primaryEnvId);
				if ("FAILED".equals(envResult.get("errorCode")))
					throw new TdmValidationException("Environment Validation","Failed to fetch LUs for override environment: " + envResult.get("message"));
				lus = new ArrayList<>((List<Map<String, Object>>) envResult.get("result"));

				// LOAD tasks: if the opposite side also changed, keep only LUs present in both envs.
				if (!isSourceTask && srcChanged && tarChanged) {
					Map<String, Object> srcResult = (Map<String, Object>)
							fnGetLogicalUnitsByEnvironmentAndBusinessentity(Long.parseLong(finalBeId), newSrcEnvId);
					if ("SUCCESS".equals(srcResult.get("errorCode"))) {
						Set<String> srcLuIds = ((List<Map<String, Object>>) srcResult.get("result"))
								.stream().map(l -> "" + l.get("lu_id")).collect(Collectors.toSet());
						lus = lus.stream()
								.filter(l -> srcLuIds.contains("" + l.get("lu_id")))
								.collect(Collectors.toList());
					}
				}

				if (lus == null || lus.isEmpty())
					throw new TdmValidationException("Environment Validation","No logical units are available in the override environment");

				for (Map<String, Object> lu : lus)
					luIds.add("" + lu.get("lu_id"));
				context.put("envOverridden", true);
			} else {
				try (Db.Rows rows = db(TDM).fetch(
						"SELECT tlu.lu_id, plu.lu_name, plu.lu_parent_id" +
								" FROM " + TDMDB_SCHEMA + ".tasks_logical_units tlu" +
								" JOIN " + TDMDB_SCHEMA + ".product_logical_units plu ON plu.lu_id = tlu.lu_id" +
								" WHERE tlu.task_id = ?",
						taskRow.get("task_id"))) {
					for (Db.Row r : rows) {
						Map<String, Object> lu = new HashMap<>();
						lu.put("lu_id", r.get("lu_id"));
						lu.put("lu_name", r.get("lu_name"));
						lu.put("lu_parent_id", r.get("lu_parent_id"));
						lus.add(lu);
						luIds.add("" + r.get("lu_id"));
					}
				}
			}
		} else {
			for (Map<String, Object> lu : lus)
				luIds.add("" + lu.get("lu_id"));
		}

		Map<String, Object> beScope = new HashMap<>();
		beScope.put("be_id", finalBeId);
		beScope.put("LU List", luIds);

		context.put("logicalUnits", lus);
		context.put("finalBeId", finalBeId);
		context.put("beScope", beScope);
	}

	private static Long initiateExecution(Long taskId, Db.Row taskRow, Map<String, Object> context,
			Map<String, Object> overrideParams, Map<OverrideParamKey, Object> inputOverrides,
			Long draftExecutionId) throws Exception {

		List<Map<String, Object>> executions = fnGetActiveTaskForActivation(taskId,
				(String) context.get("finalSelectionMethod"),
				(context.containsKey("overrideBeId") || Boolean.TRUE.equals(context.get("envOverridden")))
						? (List) context.get("logicalUnits") : null,
				(Long) context.get("tarEnvId"));
		if (executions == null || executions.isEmpty())
			throw new TdmValidationException("Task Validation","Failed to execute Task");

		String taskType = "" + taskRow.get("task_type");
		boolean hasTarget = !"EXTRACT".equalsIgnoreCase(taskType)
				&& !"GENERATE".equalsIgnoreCase(taskType)
				&& !"TRAINING".equalsIgnoreCase(taskType)
				&& !"AI_GENERATED".equalsIgnoreCase(taskType);
		Long executionEnvId = hasTarget ? (Long) context.get("tarEnvId") : (Long) context.get("srcEnvId");
		String workersKey = hasTarget ? "target_max_workers_per_node" : "source_max_workers_per_node";
		String affinityKey = hasTarget ? "final_target_affinity" : "final_source_affinity";

		// If execution env differs from original task env, reset workers/affinity to
		// new env defaults
		for (Map<String, Object> execution : executions) {
			Long originalEnvId = hasTarget
					? toLong(execution.get("environment_id"))
					: toLong(execution.get("source_environment_id"));
			if (executionEnvId != null && !executionEnvId.equals(originalEnvId)) {
				String q = "SELECT ep.max_number_of_workers, ep.data_center_name " +
						"FROM " + TDMDB_SCHEMA + ".environment_products ep " +
						"JOIN " + TDMDB_SCHEMA + ".product_logical_units plu ON ep.product_id = plu.product_id " +
						"WHERE plu.lu_id = ? AND ep.environment_id = ? AND ep.status = 'Active'";
				Db.Row envDefaults = db(TDM).fetch(q, execution.get("lu_id"), executionEnvId).firstRow();
				if (envDefaults != null && !envDefaults.isEmpty()) {
					execution.put(workersKey, envDefaults.get("max_number_of_workers"));
					execution.put(affinityKey, envDefaults.get("data_center_name"));
				}
			}
		}

		// Per-LU affinity/workers from LOGICAL_UNITS entries take highest precedence.
		// Each LU entry supports: max_no_of_workers (routed to source or target field
		// based on task type via workersKey), source_affinity, target_affinity.
		// Worker values are validated against the global limit; overrides are recorded
		// in overrideParams for audit.
		List<Map<String, Object>> luOverrideList = (List<Map<String, Object>>) inputOverrides
				.get(OverrideParamKey.LOGICAL_UNITS);
		if (luOverrideList != null && !luOverrideList.isEmpty()) {
			int luGlobalLimit = getGlobalMaxWorkersLimit();
			Map<String, Map<String, Object>> luOverrideById = new HashMap<>();
			for (Map<String, Object> luOvr : luOverrideList) {
				Object luId = luOvr.get("lu_id");
				if (luId != null)
					luOverrideById.put(luId.toString(), luOvr);
			}
			List<Map<String, Object>> luOverridesAudit = new ArrayList<>();
			for (Map<String, Object> execution : executions) {
				Object luId = execution.get("lu_id");
				if (luId == null)
					continue;
				Map<String, Object> luOvr = luOverrideById.get(luId.toString());
				if (luOvr == null)
					continue;
				Map<String, Object> luOverrideFields = StartTaskValidator
						.parseLuOverrideFields(execution.get("override_fields"));
				if (luOvr.containsKey("max_no_of_workers") && luOvr.get("max_no_of_workers") != null
						&& !StartTaskValidator.isLuFieldEditable(luOverrideFields, "max_no_of_workers"))
					throw new TdmValidationException("Override Validation",
							"LU '" + luId + "': max_no_of_workers is locked and cannot be overridden.");
				if (luOvr.containsKey("source_affinity")
						&& !StartTaskValidator.isLuFieldEditable(luOverrideFields, "source_affinity"))
					throw new TdmValidationException("Override Validation",
							"LU '" + luId + "': source_affinity is locked and cannot be overridden.");
				if (luOvr.containsKey("target_affinity")
						&& !StartTaskValidator.isLuFieldEditable(luOverrideFields, "target_affinity"))
					throw new TdmValidationException("Override Validation",
							"LU '" + luId + "': target_affinity is locked and cannot be overridden.");
				Map<String, Object> auditEntry = new HashMap<>();
				auditEntry.put("lu_id", luId);
				Object luName = luOvr.get("lu_name");
				if (luName != null)
					auditEntry.put("lu_name", luName);
				boolean hasOverride = false;
				if (luOvr.containsKey("max_no_of_workers") && luOvr.get("max_no_of_workers") != null) {
					int w = ((Number) luOvr.get("max_no_of_workers")).intValue();
					if (w > luGlobalLimit)
						throw new TdmValidationException("Override Validation",
								"LU '" + luId + "': max_no_of_workers (" + w + ") exceeds global limit ("
										+ luGlobalLimit + ").");
					execution.put(workersKey, w);
					auditEntry.put("max_no_of_workers", w);
					hasOverride = true;
				}
				if (luOvr.containsKey("source_affinity")) {
					execution.put("final_source_affinity", luOvr.get("source_affinity"));
					auditEntry.put("source_affinity", luOvr.get("source_affinity"));
					hasOverride = true;
				}
				if (luOvr.containsKey("target_affinity")) {
					execution.put("final_target_affinity", luOvr.get("target_affinity"));
					auditEntry.put("target_affinity", luOvr.get("target_affinity"));
					hasOverride = true;
				}
				if (hasOverride)
					luOverridesAudit.add(auditEntry);
			}
			if (!luOverridesAudit.isEmpty())
				overrideParams.put(OverrideParamKey.LOGICAL_UNITS.name(), luOverridesAudit);
		}

		for (OverrideParamKey procKey : List.of(
				OverrideParamKey.PRE_EXECUTION_PROCESSES_PARAMS,
				OverrideParamKey.POST_EXECUTION_PROCESSES_PARAMS)) {
			List<Map<String, Object>> procOverrides =
					(List<Map<String, Object>>) inputOverrides.get(procKey);
			if (procOverrides != null && !procOverrides.isEmpty()) {
				String procType = procKey == OverrideParamKey.PRE_EXECUTION_PROCESSES_PARAMS ? "pre" : "post";
				StartTaskValidator.validateProcessParamOverrides(taskId, procType, procOverrides);
				overrideParams.put(procKey.name(), procOverrides);
			}
		}

		Long taskExecId = draftExecutionId != null ? draftExecutionId : (Long) fnGetNextTaskExecution(taskId);
		if (taskExecId == null)
			throw new TdmValidationException("Task Validation","Failed to generate task execution ID.");
		Object refcountVal = executions.get(0).get("refcount");
		if ((executions.get(0).get("selection_method") != null
				&& TABLES.equals(executions.get(0).get("selection_method").toString()))
				|| (refcountVal != null && (Long) refcountVal > 0)) {
			List<Map<String, Object>> tableFilterOverrides =
					(List<Map<String, Object>>) inputOverrides.get(OverrideParamKey.TABLE_FILTERS);
			Map<String, Map<String, Object>> resolvedTableFilters =
					resolveTableFilterOverrides(taskId, tableFilterOverrides);
			if (tableFilterOverrides != null && !tableFilterOverrides.isEmpty())
				overrideParams.put(OverrideParamKey.TABLE_FILTERS.name(), tableFilterOverrides);
			Long overrideBeId = (Long) inputOverrides.get(OverrideParamKey.BE_ID);
			Long finalBeId = overrideBeId != null ? overrideBeId: (Long) taskRow.get("be_id");
			fnSaveRefExeTablestoTask((Long) executions.get(0).get("task_id"), finalBeId, taskExecId, resolvedTableFilters,taskType);
		}

		fnStartTaskExecutions(executions, taskExecId, (String) inputOverrides.get(OverrideParamKey.BE_ID),
				(String) context.get("sourceEnvName"), (Long) context.get("tarEnvId"), (Long) context.get("srcEnvId"),
				(String) inputOverrides.get(OverrideParamKey.EXECUTION_NOTE), (String) context.get("targetEnvName"));
		boolean createdByAi = draftExecutionId != null;
		if (!overrideParams.isEmpty() || createdByAi)
			fnSaveTaskOverrideParameters(taskId, overrideParams, taskExecId, createdByAi);
		fnCreateSummaryRecord(executions.get(0), taskExecId, (String) inputOverrides.get(OverrideParamKey.BE_ID),
				(String) context.get("sourceEnvName"), (Long) context.get("tarEnvId"), (Long) context.get("srcEnvId"),
				(String) context.get("targetEnvName"));

		logActivity("" + taskRow.get("task_title"));
		return taskExecId;
	}

	private static String getVal(Map<OverrideParamKey, Object> input, OverrideParamKey key, Db.Row row, String col) {
		String v = (String) input.get(key);
		return (v != null && !v.trim().isEmpty()) ? v : "" + row.get(col);
	}

	private static boolean isNullOrEmpty(String s) {
		return s == null || s.isBlank() || "null".equalsIgnoreCase(s);
	}

	private static Long fetchEnvId(String name) throws Exception {
		Long id = (Long) db(TDM)
				.fetch("select environment_id from " + TDMDB_SCHEMA
						+ ".environments where environment_name=(?) and environment_status = 'Active'", name)
				.firstValue();
		if (id == null)
			throw new TdmValidationException("Environment Validation","Environment '" + name + "' not found or is not active.");
		return id;
	}

	private static Map<String, Map<String, Object>> resolveTableFilterOverrides(
			Long taskId, List<Map<String, Object>> tableFilterOverrides) throws Exception {
		if (tableFilterOverrides == null || tableFilterOverrides.isEmpty())
			return null;
		Map<String, Map<String, Object>> result = new HashMap<>();
		for (Map<String, Object> tableOverride : tableFilterOverrides) {
			String refTableName = (String) tableOverride.get("ref_table_name");
			if (refTableName == null)
				throw new TdmValidationException("Table Level Validation" , "TABLE_FILTERS entry is missing 'ref_table_name'.");
			String interfaceName = (String) tableOverride.get("interface_name");
			String schemaName = (String) tableOverride.get("schema_name");
			List<Map<String, Object>> fields = (List<Map<String, Object>>) tableOverride.get("fields");
			if (fields == null || fields.isEmpty())
				continue;
			StringBuilder queryBuilder = new StringBuilder(
					"SELECT gui_filter, filter_type FROM " + TDMDB_SCHEMA + ".task_ref_tables " +
					"WHERE task_id = ? AND ref_table_name = ?");
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(taskId);
			queryParams.add(refTableName);
			if (interfaceName != null) {
				queryBuilder.append(" AND interface_name = ?");
				queryParams.add(interfaceName);
			}
			if (schemaName != null) {
				queryBuilder.append(" AND schema_name = ?");
				queryParams.add(schemaName);
			}
			Db.Row tableRow = db(TDM).fetch(queryBuilder.toString(), queryParams.toArray()).firstRow();
			if (tableRow == null)
				throw new TdmValidationException("Table Level Validation","Table '" + refTableName + "' not found in task filter definitions.");
			String guiFilterJson = (String) tableRow.get("gui_filter");
			if (guiFilterJson == null || guiFilterJson.isEmpty())
				throw new TdmValidationException("Table Level Validation","Table '" + refTableName + "' has no GUI filter defined.");
			Map<String, Object> guiFilter = (Map<String, Object>) Json.get().fromJson(guiFilterJson);
			Map<String, Object> group = (Map<String, Object>) guiFilter.get("group");
			Map<String, Map<String, Object>> ruleByField = new HashMap<>();
			collectFilterRules(group, ruleByField);
			for (Map<String, Object> fieldOverride : fields) {
				String fieldName = (String) fieldOverride.get("field");
				if (fieldName == null)
					throw new TdmValidationException("Table Level Validation","TABLE_FILTERS override for table '" + refTableName + "' has entry missing 'field'.");
				Map<String, Object> rule = ruleByField.get(fieldName);
				if (rule == null)
					throw new TdmValidationException("Table Level Validation","Field '" + fieldName + "' not found in filter for table '" + refTableName + "'.");
				if (!Boolean.TRUE.equals(rule.get("is_editable")))
					throw new TdmValidationException("Table Level Validation","Field '" + fieldName + "' is not editable in table '" + refTableName + "'.");
				if (fieldOverride.containsKey("condition") && fieldOverride.get("condition") != null)
					rule.put("condition", fieldOverride.get("condition").toString());
				if (fieldOverride.containsKey("value"))
					rule.put("data", fieldOverride.get("value") != null ? fieldOverride.get("value").toString() : null);
			}
			StringBuilder sqlBuilder = new StringBuilder();
			List<String> params = new ArrayList<>();
			buildSqlFromGroup(group, sqlBuilder, params);
			Map<String, Object> resolved = new HashMap<>();
			resolved.put("table_filter", sqlBuilder.toString());
			resolved.put("filter_parameters", String.join(TDM_PARAMETERS_SEPARATOR, params));
			resolved.put("filter_type", tableRow.get("filter_type"));
			result.put(buildTableKey(interfaceName, schemaName, refTableName), resolved);
		}
		return result.isEmpty() ? null : result;
	}

	private static String buildTableKey(String interfaceName, String schemaName, String refTableName) {
		return (interfaceName != null ? interfaceName : "") + "|" +
			   (schemaName    != null ? schemaName    : "") + "|" +
			   (refTableName  != null ? refTableName  : "");
	}

	private static void collectFilterRules(Map<String, Object> group, Map<String, Map<String, Object>> ruleByField) {
		if (group == null) return;
		List<Map<String, Object>> rules = (List<Map<String, Object>>) group.get("rules");
		if (rules == null) return;
		for (Map<String, Object> rule : rules) {
			if (rule.containsKey("group"))
				collectFilterRules((Map<String, Object>) rule.get("group"), ruleByField);
			else if (rule.get("field") != null)
				ruleByField.put((String) rule.get("field"), rule);
		}
	}

	private static void buildSqlFromGroup(Map<String, Object> group, StringBuilder sb, List<String> params) {
		if (group == null) return;
		List<Map<String, Object>> rules = (List<Map<String, Object>>) group.get("rules");
		if (rules == null || rules.isEmpty()) return;
		sb.append("(");
		for (int i = 0; i < rules.size(); i++) {
			if (i > 0) {
				String op = (String) rules.get(i - 1).get("operator");
				sb.append(" ").append(op != null ? op : "AND").append(" ");
			}
			Map<String, Object> rule = rules.get(i);
			if (rule.containsKey("group")) {
				buildSqlFromGroup((Map<String, Object>) rule.get("group"), sb, params);
			} else {
				String field = (String) rule.get("field");
				String condition = rule.get("condition") != null ? rule.get("condition").toString().toUpperCase() : "=";
				String data = rule.get("data") != null ? rule.get("data").toString() : null;
				switch (condition) {
					case "IS NULL":
						sb.append(field).append(" IS NULL");
						break;
					case "IS NOT NULL":
						sb.append(field).append(" IS NOT NULL");
						break;
					case "IN":
					case "NOT IN":
						String[] vals = data != null ? data.split(",") : new String[0];
						sb.append(field).append(" ").append(condition).append(" (");
						for (int j = 0; j < vals.length; j++) {
							if (j > 0) sb.append(", ");
							sb.append("?");
							params.add(vals[j].trim());
						}
						sb.append(")");
						break;
					default:
						sb.append(field).append(" ").append(condition).append(" ?");
						if (data != null) params.add(data);
						break;
				}
			}
		}
		sb.append(")");
	}

	private static void putIfPresent(Map<OverrideParamKey, Object> src, OverrideParamKey key,
			Map<String, Object> target) {
		if (src.get(key) != null)
			target.put(key.name(), src.get(key));
	}

	private static void logActivity(String title) {
		try {
			fnInsertActivity("update", "Tasks", "Execution list of task " + title);
		} catch (Exception e) {
			UserCode.log.error(e.getMessage());
		}
	}

	private static Map<String, Object> buildSuccessResponse(Long id) {
		Map<String, Object> res = new HashMap<>();
		Map<String, Object> m = new HashMap<>();
		m.put("taskExecutionId", id);
		res.put("result", m);
		res.put("errorCode", "SUCCESS");
		res.put("message", null);
		return res;
	}

	private static Long toLong(Object v) {
		return v == null ? null : Long.valueOf(v.toString());
	}

	private static String fnGetTaskCreatedBy(long taskID) throws SQLException {
        String createdBy = "" + db(TDM).fetch("SELECT task_created_by FROM " + TDMDB_SCHEMA + ".tasks WHERE task_id=?", taskID).firstValue();
        String userName = createdBy.split("##")[0];
		return userName;
    }
}
