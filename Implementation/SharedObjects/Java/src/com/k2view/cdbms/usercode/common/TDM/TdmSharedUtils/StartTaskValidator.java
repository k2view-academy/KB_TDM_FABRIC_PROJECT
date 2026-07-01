package com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils;

import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.shared.user.UserCode.fabric;
import static com.k2view.cdbms.shared.user.UserCode.getGlobal;
import static com.k2view.cdbms.shared.user.UserCode.sessionUser;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.isParamsCoupling;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnGetNumberOfMatchingEntities;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnTestInterfacesForEnvProduct;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnValidateReservedEntities;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.isAllowedTestConnFailure;
import static com.k2view.cdbms.usercode.common.TDM.TaskManagmentUtils.SharedLogic.isPermittedUserForExecution;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnGetExecutionProcessParams;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateExtractNoRetention;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateNumberOfCopyEntities;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateNumberOfReadEntities;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateNumberOfReserveEntities;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateOverrideSyncMode;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateParallelExecutions;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateRetentionPeriodParams;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateSourceEnvForTask;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateTargetEnvForTask;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateVersionExecIdAndGetDetails;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.fnValidateVersionExecIdNotExpired;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.validateParams;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetReservedEntitiesNumber;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserEnvs;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserPermissionGroup;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.k2view.cdbms.lut.LUType;
import com.k2view.cdbms.lut.LudbJobs;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.OverrideParamKey;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;

@SuppressWarnings({ "DefaultAnnotationParam", "unchecked" })
class StartTaskValidator {

	private static final String TDM = "TDM";

	static void checkSystemHealth() throws Exception {
		try {
			ensureRequiredJobsAreRunning();
		} catch (Exception e) {
			throw new TdmValidationException("Mandatory Job(s) Down!", e.getMessage());
		}
	}

	private static void ensureRequiredJobsAreRunning() throws Exception {
		List<LudbJobs.LudbJob> jobList = LUType.getTypeByName(TDM).ludbUserJobs;
		StringBuilder downJobsBuilder = new StringBuilder();

		for (LudbJobs.LudbJob job : jobList) {
			String executionMode = Util.rte(() -> "" + job.executionMode);
			String activeInd = Util.rte(() -> "" + job.active);

			if ("true".equalsIgnoreCase(activeInd) && "automatically".equalsIgnoreCase(executionMode)) {
				String functionName = Util.rte(() -> "" + job.functionName);
				String uid = Util.rte(() -> "" + job.uid);

				Db.Row jobDetails = fabric()
						.fetch("jobstatus user_job 'TDM." + functionName + "' WITH UID='" + uid + "'").firstRow();
				String jobStatus = "" + jobDetails.get("Status");

				boolean isJobDown = !"IN_PROCESS".equalsIgnoreCase(jobStatus) &&
						!"SCHEDULED".equalsIgnoreCase(jobStatus) &&
						!"WAITING".equalsIgnoreCase(jobStatus);

				if (isJobDown) {
					if ("tdmExecuteTask".equalsIgnoreCase(functionName)
							|| "fnCheckMigrateAndUpdateTDMDB".equalsIgnoreCase(functionName)) {
						if (downJobsBuilder.length() > 0)
							downJobsBuilder.append(", ");
						downJobsBuilder.append(functionName);
						UserCode.log
								.error("Critical Job Down: " + functionName + ". Details: " + jobDetails.get("Notes"));
					}
				}
			}
		}

		if (downJobsBuilder.length() > 0) {
			throw new TdmValidationException("Job Validation","Mandatory Job(s) Down: " + downJobsBuilder + ". Cannot start task.");
		}
	}

	static void validateExecutionInputs(Long taskId, Db.Row taskRow, Map<OverrideParamKey, Object> inputOverrides) throws Exception {
		Map<String, Boolean> taskGlobalsEditability = loadTaskGlobalsEditability(taskId, inputOverrides);
		Map<String, Boolean> generateParamsEditability = new HashMap<>();
		Set<String> nullEditableParams = new HashSet<>();
		loadGenerateParamsData(taskId, taskRow, inputOverrides, generateParamsEditability, nullEditableParams);
		validateInputOverrides(taskRow, inputOverrides, taskGlobalsEditability, generateParamsEditability);
		validateNullGenerateParams(inputOverrides, nullEditableParams);
	}

	private static Map<String, Boolean> loadTaskGlobalsEditability(Long taskId,
			Map<OverrideParamKey, Object> inputOverrides) throws Exception {
		Map<String, Boolean> result = new HashMap<>();
		if (inputOverrides.containsKey(OverrideParamKey.TASK_GLOBALS)) {
			try (Db.Rows rows = db(TDM).fetch(
					"SELECT global_name, is_editable FROM " + TDMDB_SCHEMA + ".task_globals WHERE task_id = ?", taskId)) {
				for (Db.Row r : rows)
					result.put(r.get("global_name").toString(), Boolean.TRUE.equals(r.get("is_editable")));
			}
		}
		return result;
	}

	private static void loadGenerateParamsData(Long taskId, Db.Row taskRow,
			Map<OverrideParamKey, Object> inputOverrides,
			Map<String, Boolean> editabilityOut, Set<String> nullEditableOut) throws Exception {
		boolean isGenerate = "GENERATE".equalsIgnoreCase("" + taskRow.get("selection_method"));
		if (!isGenerate && !inputOverrides.containsKey(OverrideParamKey.GENERATE_DATA_PARAMS)) return;
		try (Db.Rows rows = db(TDM).fetch(
				"SELECT param_name, is_editable, param_value FROM " + TDMDB_SCHEMA
				+ ".tdm_generate_task_field_mappings WHERE task_id = ?", taskId)) {
			for (Db.Row r : rows) {
				boolean editable = Boolean.TRUE.equals(r.get("is_editable"));
				editabilityOut.put(r.get("param_name").toString(), editable);
				Object pv = r.get("param_value");
				if (isGenerate && editable && (pv == null || pv.toString().trim().isEmpty()))
					nullEditableOut.add(r.get("param_name").toString());
			}
		}
	}

	private static void validateNullGenerateParams(Map<OverrideParamKey, Object> inputOverrides,
			Set<String> nullEditableParams) throws TdmValidationException {
		if (nullEditableParams.isEmpty()) return;
		Map<String, Object> genOverrides = (Map<String, Object>) inputOverrides.get(OverrideParamKey.GENERATE_DATA_PARAMS);
		for (String paramName : nullEditableParams) {
			Object overrideValue = null;
			if (genOverrides != null && genOverrides.containsKey(paramName)) {
				Map<String, Object> override = (Map<String, Object>) genOverrides.get(paramName);
				if (override != null) overrideValue = override.get("value");
			}
			if (overrideValue == null || (overrideValue instanceof String && ((String) overrideValue).trim().isEmpty())) {
				throw new TdmValidationException("Generate Parameter Validation",
					"Generate parameter '" + paramName + "' has no stored value and must be provided before execution.");
			}
		}
	}

	static void validateInputOverrides(Db.Row taskRow,
			Map<OverrideParamKey, Object> inputOverrides,
			Map<String, Boolean> taskGlobalsEditability,
			Map<String, Boolean> generateParamsEditability) throws TdmValidationException {
		if (inputOverrides.isEmpty())
			return;

		if (inputOverrides.containsKey(null))
			throw new TdmValidationException("Override Validation",
					"Invalid override parameter key: null key is not allowed.");

		for (OverrideParamKey key : inputOverrides.keySet()) {
			if (SharedLogic.INTERNAL_ONLY_OVERRIDE_KEYS.contains(key))
				throw new TdmValidationException("Override Validation",
						key.name() + " is a system-internal parameter and cannot be provided as an override.");
		}

		Object overrideFieldsRaw = taskRow.get("task_override_fields");
		String overrideFieldsJson = overrideFieldsRaw != null ? overrideFieldsRaw.toString() : null;
		boolean hasConfig = overrideFieldsJson != null && !overrideFieldsJson.isBlank()
				&& !overrideFieldsJson.equals("{}");
		Map<String, Object> fields = hasConfig ? Json.get().fromJson(overrideFieldsJson) : Collections.emptyMap();
		String taskMethod = "" + taskRow.get("selection_method");

		checkEditable(fields, inputOverrides, OverrideParamKey.SOURCE_ENVIRONMENT_NAME,
				"Source environment", "source_environment");
		checkEditable(fields, inputOverrides, OverrideParamKey.TARGET_ENVIRONMENT_NAME,
				"Target environment", "target_environment");
		checkEditable(fields, inputOverrides, OverrideParamKey.BE_ID,
				"Business entity", "business_entity");
		checkEditable(fields, inputOverrides, OverrideParamKey.SELECTION_METHOD,
				"Selection method", "selection_method");
		checkEditable(fields, inputOverrides, OverrideParamKey.ENTITY_LIST,
				"Entity list", "selection_method", "entity_list");
		checkEditable(fields, inputOverrides, OverrideParamKey.CUSTOM_LOGIC_FLOW,
				"Custom logic", "selection_method", "custom_logic");
		checkEditable(fields, inputOverrides, OverrideParamKey.CUSTOM_LOGIC_LU_NAME,
				"Custom logic", "selection_method", "custom_logic");
		if (inputOverrides.containsKey(OverrideParamKey.BP_QUERY)
				&& !isEditableAt(fields, "selection_method", "business_parameters")
				&& !hasAnyEditableParam(taskRow)) {
			throw new TdmValidationException("Override Validation",
					"Business parameters cannot be overridden for this task.");
		}
		checkEditable(fields, inputOverrides, OverrideParamKey.RESERVE_IND,
				"Reservation period", "reservation_period");
		checkEditable(fields, inputOverrides, OverrideParamKey.RESERVE_RETENTION_PARAMS,
				"Reservation period", "reservation_period");

		if (inputOverrides.containsKey(OverrideParamKey.NO_OF_ENTITIES)) {
			String effectiveMethod = inputOverrides.containsKey(OverrideParamKey.SELECTION_METHOD)
					? (String) inputOverrides.get(OverrideParamKey.SELECTION_METHOD)
					: taskMethod;
			if ("L".equalsIgnoreCase(effectiveMethod))
				throw new TdmValidationException("Override Validation",
						"NO_OF_ENTITIES is not applicable when the selection method is 'Entity List'.");
			String[] path = new String[] { "selection_method", "max_entities" };
			if (!isEditableAt(fields, path))
				throw new TdmValidationException("Override Validation",
						"Number of entities cannot be overridden for this task.");
		}

		if (inputOverrides.containsKey(OverrideParamKey.PARAMETERS)) {
			String effectiveMethod = inputOverrides.containsKey(OverrideParamKey.SELECTION_METHOD)
					? (String) inputOverrides.get(OverrideParamKey.SELECTION_METHOD)
					: taskMethod;
			if ("C".equalsIgnoreCase(effectiveMethod)) {
				validateCustomLogicParametersOverride(fields, taskRow,
						(String) inputOverrides.get(OverrideParamKey.PARAMETERS));
			} else {
				validateBpParametersOverride(fields, taskRow,
						(String) inputOverrides.get(OverrideParamKey.PARAMETERS));
			}
		}

		if (inputOverrides.containsKey(OverrideParamKey.LOGICAL_UNITS)) {
			Object raw = inputOverrides.get(OverrideParamKey.LOGICAL_UNITS);
			if (!(raw instanceof List))
				throw new TdmValidationException("Override Validation", "LOGICAL_UNITS must be a list.");
			List<?> luList = (List<?>) raw;
			if (luList.isEmpty())
				throw new TdmValidationException("Override Validation", "LOGICAL_UNITS list must not be empty.");
			for (int i = 0; i < luList.size(); i++) {
				Object entry = luList.get(i);
				if (!(entry instanceof Map))
					throw new TdmValidationException("Override Validation",
							"LOGICAL_UNITS[" + i + "] must be an object.");
				Map<String, Object> lu = (Map<String, Object>) entry;
				if (lu.get("lu_id") == null)
					throw new TdmValidationException("Override Validation",
							"LOGICAL_UNITS[" + i + "] is missing required field 'lu_id'.");
				if (lu.get("lu_name") == null)
					throw new TdmValidationException("Override Validation",
							"LOGICAL_UNITS[" + i + "] is missing required field 'lu_name'.");
				Object workers = lu.get("max_no_of_workers");
				if (workers != null) {
					try {
						if (((Number) workers).intValue() <= 0)
							throw new TdmValidationException("Override Validation",
									"LOGICAL_UNITS[" + i + "].max_no_of_workers must be a positive integer.");
					} catch (ClassCastException e) {
						throw new TdmValidationException("Override Validation",
								"LOGICAL_UNITS[" + i + "].max_no_of_workers must be a number.");
					}
				}
			}
		}

		if (inputOverrides.containsKey(OverrideParamKey.TASK_GLOBALS)) {
			validateTaskGlobalsOverride(fields, inputOverrides, taskGlobalsEditability);
		}

		if (inputOverrides.containsKey(OverrideParamKey.GENERATE_DATA_PARAMS)) {
			validateGenerateParamsOverride(fields, inputOverrides, generateParamsEditability);
		}
	}

	private static void validateGenerateParamsOverride(
			Map<String, Object> overrideFields,
			Map<OverrideParamKey, Object> inputOverrides,
			Map<String, Boolean> generateParamsEditability) throws TdmValidationException {

		Object raw = inputOverrides.get(OverrideParamKey.GENERATE_DATA_PARAMS);
		if (!(raw instanceof Map)) return;

		boolean canAddNew = isEditableAt(overrideFields, "selection_method", "generate_data_params", "can_add_params");
		Map<String, Object> overrideParams = (Map<String, Object>) raw;
		for (String paramName : overrideParams.keySet()) {
			Object entry = overrideParams.get(paramName);
			if (!(entry instanceof Map)) {
				throw new TdmValidationException("Override Validation",
					"Generate parameter '" + paramName + "' must be an object with a 'value' field.");
			}
			Map<String, Object> entryMap = (Map<String, Object>) entry;
			if (!entryMap.containsKey("value")) {
				throw new TdmValidationException("Override Validation",
					"Generate parameter '" + paramName + "' is missing the 'value' field.");
			}
			Object value = entryMap.get("value");
			if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
				throw new TdmValidationException("Override Validation",
					"Generate parameter '" + paramName + "' must have a non-empty value.");
			}
			if (generateParamsEditability.containsKey(paramName)) {
				if (!Boolean.TRUE.equals(generateParamsEditability.get(paramName))) {
					throw new TdmValidationException("Override Validation",
						"Generate parameter '" + paramName + "' is locked and cannot be overridden for this task.");
				}
			} else {
				if (!canAddNew) {
					throw new TdmValidationException("Override Validation",
						"Generate parameter '" + paramName + "' is not defined on this task and runtime parameter addition is not enabled.");
				}
			}
		}
	}

	private static void validateTaskGlobalsOverride(
			Map<String, Object> overrideFields,
			Map<OverrideParamKey, Object> inputOverrides,
			Map<String, Boolean> taskGlobalsEditability) throws TdmValidationException {

		Object raw = inputOverrides.get(OverrideParamKey.TASK_GLOBALS);
		if (!(raw instanceof Map)) return;

		Map<String, ?> overrideGlobals = (Map<String, ?>) raw;
		boolean canAddNew = isEditableAt(overrideFields, "task_globals");

		for (String globalName : overrideGlobals.keySet()) {
			if (taskGlobalsEditability.containsKey(globalName)) {
				if (!Boolean.TRUE.equals(taskGlobalsEditability.get(globalName))) {
					throw new TdmValidationException("Override Validation",
							"Task global '" + globalName + "' is locked and cannot be overridden for this task.");
				}
			} else {
				if (!canAddNew) {
					throw new TdmValidationException("Override Validation",
							"Task global '" + globalName + "' is not defined on this task and runtime variable addition is not enabled.");
				}
			}
		}
	}

	private static void checkEditable(Map<String, Object> fields,
			Map<OverrideParamKey, Object> inputOverrides,
			OverrideParamKey key, String label, String... path) throws TdmValidationException {
		if (inputOverrides.containsKey(key) && !isEditableAt(fields, path))
			throw new TdmValidationException("Override Validation",
					label + " cannot be overridden for this task.");
	}

	private static boolean isEditableAt(Map<String, Object> fields, String... path) {
		if (fields.isEmpty())
			return false;
		Map<String, Object> node = fields;
		for (int i = 0; i < path.length - 1; i++) {
			node = (Map<String, Object>) node.get(path[i]);
			if (node == null)
				return false;
		}
		Map<String, Object> leaf = (Map<String, Object>) node.get(path[path.length - 1]);
		if (leaf == null)
			return false;
		return Boolean.TRUE.equals(leaf.get("is_editable"));
	}

	private static boolean hasAnyEditableParam(Db.Row taskRow) {
		Object paramsRaw = taskRow.get("parameters");
		if (paramsRaw == null)
			return false;
		String paramsJson = paramsRaw.toString();
		if (paramsJson.isBlank())
			return false;
		try {
			Map<String, Object> params = Json.get().fromJson(paramsJson);
			return hasEditableRule(params);
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean hasEditableRule(Map<String, Object> node) {
		if (node == null)
			return false;
		if (Boolean.TRUE.equals(node.get("is_editable")))
			return true;
		Object rules = node.get("rules");
		if (rules instanceof List) {
			for (Object rule : (List<?>) rules) {
				if (rule instanceof Map && hasEditableRule((Map<String, Object>) rule))
					return true;
			}
		}
		Object group = node.get("group");
		if (group instanceof Map)
			return hasEditableRule((Map<String, Object>) group);
		return false;
	}

	private static Map<String, Map<String, Object>> buildParamRuleIndex(Map<String, Object> node) {
		Map<String, Map<String, Object>> index = new HashMap<>();
		if (node == null)
			return index;
		Object rules = node.get("rules");
		if (rules instanceof List) {
			for (Object r : (List<?>) rules) {
				if (r instanceof Map) {
					Map<String, Object> rule = (Map<String, Object>) r;
					Object field = rule.get("field");
					if (field != null)
						index.put(field.toString(), rule);
					Object nested = rule.get("group");
					if (nested instanceof Map)
						index.putAll(buildParamRuleIndex((Map<String, Object>) nested));
				}
			}
		}
		Object group = node.get("group");
		if (group instanceof Map)
			index.putAll(buildParamRuleIndex((Map<String, Object>) group));
		return index;
	}

	private static void validateBpParametersOverride(Map<String, Object> fields,
			Db.Row taskRow, String overrideParamsJson) throws TdmValidationException {
		if (overrideParamsJson == null || overrideParamsJson.isBlank())
			return;

		Map<String, Map<String, Object>> originalIndex = Collections.emptyMap();
		Object origRaw = taskRow.get("parameters");
		if (origRaw != null && !origRaw.toString().isBlank()) {
			try {
				Map<String, Object> origParsed = Json.get().fromJson(origRaw.toString());
				originalIndex = buildParamRuleIndex(origParsed);
			} catch (Exception ignored) {
			}
		}

		Map<String, Object> overrideParsed;
		try {
			overrideParsed = Json.get().fromJson(overrideParamsJson);
		} catch (Exception e) {
			throw new TdmValidationException("Override Validation",
					"Parameters override is not valid JSON.");
		}
		Map<String, Map<String, Object>> overrideIndex = buildParamRuleIndex(overrideParsed);

		boolean canAddNew = isEditableAt(fields, "selection_method", "business_parameters");

		for (Map.Entry<String, Map<String, Object>> entry : overrideIndex.entrySet()) {
			String field = entry.getKey();
			Map<String, Object> origRule = originalIndex.get(field);
			if (origRule == null) {
				if (!canAddNew)
					throw new TdmValidationException("Override Validation",
							"Adding new business parameter '" + field + "' is not allowed for this task.");
			} else {
				if (!Boolean.TRUE.equals(origRule.get("is_editable"))) {
					Map<String, Object> overrideRule = entry.getValue();
					if (bpValueChanged(origRule, overrideRule))
						throw new TdmValidationException("Override Validation",
								"Business parameter '" + field + "' cannot be overridden for this task.");
				}
			}
		}
	}

	private static boolean bpValueChanged(Map<String, Object> orig, Map<String, Object> override) {
		Object origData = orig.get("data");
		Object overrideData = override.get("data");
		Object origCond = orig.get("condition");
		Object overrideCond = override.get("condition");

		String origDataStr = origData == null ? null : Json.get().toJson(origData);
		String overrideDataStr = overrideData == null ? null : Json.get().toJson(overrideData);

		return !Objects.equals(origDataStr, overrideDataStr)
				|| !Objects.equals(origCond, overrideCond);
	}

	private static void validateCustomLogicParametersOverride(Map<String, Object> fields,
			Db.Row taskRow, String overrideParamsJson) throws TdmValidationException {
		if (overrideParamsJson == null || overrideParamsJson.isBlank())
			return;

		Map<String, Map<String, Object>> originalIndex = new HashMap<>();
		Object origRaw = taskRow.get("parameters");
		if (origRaw != null && !origRaw.toString().isBlank()) {
			try {
				Map<String, Object> origParsed = Json.get().fromJson(origRaw.toString());
				Object inputs = origParsed.get("inputs");
				if (inputs instanceof List) {
					for (Object item : (List<?>) inputs) {
						if (item instanceof Map) {
							Map<String, Object> param = (Map<String, Object>) item;
							Object name = param.get("name");
							if (name != null)
								originalIndex.put(name.toString(), param);
						}
					}
				}
			} catch (Exception ignored) {}
		}

		Map<String, Object> overrideParsed;
		try {
			overrideParsed = Json.get().fromJson(overrideParamsJson);
		} catch (Exception e) {
			throw new TdmValidationException("Override Validation",
					"Parameters override is not valid JSON.");
		}

		Object overrideInputsRaw = overrideParsed.get("inputs");
		if (!(overrideInputsRaw instanceof List))
			return;

		boolean canAddNew = isEditableAt(fields, "selection_method", "custom_logic", "can_add_params");

		Set<String> overrideNames = new HashSet<>();
		for (Object item : (List<?>) overrideInputsRaw) {
			if (item instanceof Map) {
				Object n = ((Map<?, ?>) item).get("name");
				if (n != null) overrideNames.add(n.toString());
			}
		}

		for (Map.Entry<String, Map<String, Object>> e : originalIndex.entrySet()) {
			if (!Boolean.TRUE.equals(e.getValue().get("is_editable")) && !overrideNames.contains(e.getKey()))
				throw new TdmValidationException("Override Validation",
						"Locked parameter '" + e.getKey() + "' must be included in the parameters override.");
		}

		for (Object item : (List<?>) overrideInputsRaw) {
			if (!(item instanceof Map)) continue;
			Map<String, Object> overrideParam = (Map<String, Object>) item;
			Object nameObj = overrideParam.get("name");
			if (nameObj == null) continue;
			String name = nameObj.toString();

			Map<String, Object> origParam = originalIndex.get(name);
			if (origParam == null) {
				if (!canAddNew)
					throw new TdmValidationException("Override Validation",
							"Adding new parameter '" + name + "' is not allowed for this task.");
			} else {
				if (!Boolean.TRUE.equals(origParam.get("is_editable"))) {
					if (clValueChanged(origParam, overrideParam))
						throw new TdmValidationException("Override Validation",
								"Parameter '" + name + "' cannot be overridden for this task.");
				}
			}
		}
	}

	static void validateProcessParamOverrides(Long taskId, String processType,
			List<Map<String, Object>> procOverrides) throws TdmValidationException {
		for (Map<String, Object> entry : procOverrides) {
			Object pidObj = entry.get("process_id");
			if (pidObj == null)
				throw new TdmValidationException("Override Validation",
						"Each " + processType + " process override entry must include 'process_id'.");
			long processId = ((Number) pidObj).longValue();

			Map<String, Map<String, Object>> storedIndex = new HashMap<>();
			boolean processEditable = false;
			String procName = null;
			String luName = null;
			try {
				String sql = "SELECT process_name, lu_name, parameters FROM " + TDMDB_SCHEMA
						+ ".tasks_exe_process WHERE task_id = ? AND process_id = ? AND process_type = ?";
				try (Db.Rows dbRows = db(TDM).fetch(sql, taskId, processId, processType)) {
					for (Db.Row dbRow : dbRows) {
						procName = dbRow.get("process_name") != null ? dbRow.get("process_name").toString() : null;
						luName = dbRow.get("lu_name") != null ? dbRow.get("lu_name").toString() : null;
						
						Object raw = dbRow.get("parameters");
						if (raw != null && !raw.toString().isBlank()) {
							Map<String, Object> parsed = Json.get().fromJson(raw.toString());
							processEditable = Boolean.TRUE.equals(parsed.get("is_editable"));
							Object inputsRaw = parsed.get("inputs");
							if (inputsRaw instanceof List) {
								for (Object item : (List<?>) inputsRaw) {
									if (item instanceof Map) {
										Map<String, Object> param = (Map<String, Object>) item;
										Object name = param.get("name");
										if (name != null)
											storedIndex.put(name.toString(), param);
									}
								}
							}
						}
						break;
					}
				}
			} catch (Exception e) {
				throw new TdmValidationException("Override Validation",
						"Failed to read stored parameters for process " + processId + ": " + e.getMessage());
			}

			// Build mandatory and all-param name sets from the Broadway flow definition
			final Set<String> mandatoryNames = new HashSet<>();
			final Set<String> flowParamNames = new HashSet<>();
			if (procName != null) {
				Map<String, String> processRec = new HashMap<>();
				processRec.put("luName", luName);
				processRec.put("processName", procName);
				for (Map<String, Object> editor : getProcessEditors(processType, processRec)) {
					Object editorObj = editor.get("editor");
					if (editorObj instanceof Map) {
						Object eName = ((Map<?, ?>) editorObj).get("name");
						if (eName != null) {
							flowParamNames.add(eName.toString());
							if (Boolean.TRUE.equals(editor.get("mandatory")))
								mandatoryNames.add(eName.toString());
						}
					}
				}
			}

			Object overrideDeltaRaw = entry.get("parameter_overrides");
			if (!(overrideDeltaRaw instanceof List))
				continue;

			if (!processEditable)
				throw new TdmValidationException("Override Validation",
						processType + " process " + processId + " is not editable and cannot be overridden.");

			for (Object item : (List<?>) overrideDeltaRaw) {
				if (!(item instanceof Map)) continue;
				Map<String, Object> overrideParam = (Map<String, Object>) item;
				Object nameObj = overrideParam.get("name");
				if (nameObj == null) continue;
				String name = nameObj.toString();

				if (!storedIndex.containsKey(name) && !flowParamNames.contains(name))
					throw new TdmValidationException("Override Validation",
							"Unknown parameter '" + name + "' for " + processType + " process " + processId + ".");

				if (mandatoryNames.contains(name) && isBpValueEmpty(overrideParam.get("value")))
					throw new TdmValidationException("Override Validation",
							"Mandatory parameter '" + name + "' in " + processType
									+ " execution process " + processId + " cannot be set to empty.");
			}
		}
	}

	private static boolean clValueChanged(Map<String, Object> orig, Map<String, Object> override) {
		Object origVal = orig.get("value");
		Object overrideVal = override.get("value");
		String origStr = origVal == null ? null : Json.get().toJson(origVal);
		String overrideStr = overrideVal == null ? null : Json.get().toJson(overrideVal);
		return !Objects.equals(origStr, overrideStr);
	}

	static void verifyUserPermissions(Long taskId, Db.Row taskRow, Map<String, Object> context,
			Map<String, Object> overrideParams) throws Exception {

		String userName = "TDM.tdmTaskScheduler".equalsIgnoreCase(sessionUser().name())
				? "" + taskRow.get("task_created_by")
				: sessionUser().name();

		String permissionGroup = fnGetUserPermissionGroup(userName);
		String srcEnvIdStr = context.get("srcEnvId") != null ? context.get("srcEnvId").toString() : null;
		String tarEnvIdStr = context.get("tarEnvId") != null ? context.get("tarEnvId").toString() : null;
		if (!isPermittedUserForExecution(taskId, permissionGroup, srcEnvIdStr, tarEnvIdStr))
			throw new TdmValidationException("Permissions Validation", "User is not permitted to execute this task");
		
		if (!fnValidateParallelExecutions(taskId, overrideParams))
			throw new TdmValidationException("Task Validation Failure","This task is already running with the same execution parameters.");
		String taskType = "" + taskRow.get("task_type");
		Integer refcount = taskRow.get("refcount") != null ? Integer.parseInt(taskRow.get("refcount").toString()) : 0;
		Integer finalCount = (Integer) context.get("finalCount");
		Map<String, Object> beScope = (Map<String, Object>) context.get("beScope");

		List<Map<String, String>> validationErrors = new ArrayList<>();
		boolean srcValid = false, trgValid = false, srcFound = false, trgFound = false;
		long readLimit = -1L, reserveLimit = -1L, writeLimit = -1L, maxAllowedEntities = -1L;
		String permissionContext = "";
		List<Map<String, Object>> rolesList = fnGetUserEnvs(userName);
		List<Map<String, Object>> srcRoles = new ArrayList<>();
		List<Map<String, Object>> trgRoles = new ArrayList<>();

		for (Map<String, Object> envType : rolesList) {
			if (envType.get("source environments") != null)
				srcRoles = (List<Map<String, Object>>) (envType.get("source environments"));
			if (envType.get("target environments") != null)
				trgRoles = (List<Map<String, Object>>) (envType.get("target environments"));
		}
		// --- SOURCE VALIDATION ---
		if (!"reserve".equalsIgnoreCase(taskType)
				&& (!(Boolean) taskRow.get("delete_before_load") || (Boolean) taskRow.get("load_entity"))) {
			if (srcRoles == null || srcRoles.isEmpty())
				throw new TdmValidationException("Permissions Validation","User has no read permissions for this environment");
			for (Map<String, Object> role : srcRoles) {
				if (context.get("sourceEnvName").equals(role.get("environment_name"))) {
					srcFound = true;
					if ("tester".equalsIgnoreCase(fnGetUserPermissionGroup(userName))) {
						readLimit = (long) fnValidateNumberOfReadEntities(role.get("role_id").toString(),
								(String) context.get("sourceEnvName"));
						permissionContext = "read";
					}
					Map<String, String> errs = fnValidateSourceEnvForTask(beScope, refcount,
							(String) context.get("finalSelectionMethod"), (String) taskRow.get("sync_mode"),
							(Boolean) taskRow.get("version_ind"), taskType, role, taskId, readLimit);
					if (readLimit != -1 && (finalCount > readLimit))
						errs.put("Number of entity", "Exceeds read permission");
					else if (errs.isEmpty()) {
						srcValid = true;
						break;
					}
					validationErrors.add(errs);
				}
			}
		} else {
			srcValid = true;
		}

		// --- TARGET VALIDATION ---
		if ("load".equalsIgnoreCase(taskType) || "reserve".equalsIgnoreCase(taskType)) {
			if (trgRoles == null || trgRoles.isEmpty())
				throw new TdmValidationException("Permissions Validation","User has no write permissions for this environment");
			for (Map<String, Object> role : trgRoles) {
				if (context.get("targetEnvName").equals(role.get("environment_name"))) {
					trgFound = true;
					if ("tester".equalsIgnoreCase(fnGetUserPermissionGroup(userName))) {
						reserveLimit = (long) fnValidateNumberOfReserveEntities(role.get("role_id").toString(),
								(String) context.get("targetEnvName"));
						writeLimit = (long) fnValidateNumberOfCopyEntities(role.get("role_id").toString(),
								(String) context.get("targetEnvName"));

						if ("load".equalsIgnoreCase(taskType)) {
							if ((Boolean) context.get("reserveInd")) {
								Long reserved = fnGetReservedEntitiesNumber("" + role.get("environment_id"),
										"" + beScope.get("be_id"), sessionUser().name());
								maxAllowedEntities = min(readLimit, min((reserveLimit - reserved), writeLimit));
								permissionContext = "read write reserve";
							} else if (!(Boolean) taskRow.get("load_entity")
									&& (Boolean) taskRow.get("delete_before_load")) {
								maxAllowedEntities = writeLimit;
								permissionContext = "write";
							} else {
								maxAllowedEntities = min(writeLimit, readLimit);
								permissionContext = "read write";
							}
						} else {
							Long reserved = fnGetReservedEntitiesNumber("" + role.get("environment_id"),
									"" + beScope.get("be_id"), sessionUser().name());
							maxAllowedEntities = reserveLimit - reserved;
							permissionContext = "reserve";
						}
					}
					Map<String, String> errs = fnValidateTargetEnvForTask(beScope, refcount,
							(String) context.get("finalSelectionMethod"), (Boolean) taskRow.get("version_ind"),
							(Boolean) taskRow.get("replace_sequences"), (Boolean) taskRow.get("delete_before_load"),
							taskType, (Boolean) context.get("reserveInd"), finalCount, role,
							(Boolean) taskRow.get("clone_ind"), (String) taskRow.get("sync_mode"), taskId,
							maxAllowedEntities);
					if (maxAllowedEntities != -1 && (finalCount > maxAllowedEntities))
						errs.put("Number of entity", "Exceeds " + permissionContext + " permission");
					else if (errs.isEmpty()) {
						trgValid = true;
						break;
					}
					validationErrors.add(errs);
				}
			}
		} else {
			trgValid = true;
		}

		if (!srcValid || !trgValid) {
			if (!srcValid && !srcFound) {
				validationErrors.add(Collections.singletonMap("SourceEnvironment", "No Source Environment found"));
			}
			if (!trgValid && !trgFound) {
				validationErrors.add(Collections.singletonMap("TargetEnvironment", "No Target Environment found"));
			}
			
			throw new TdmValidationException("Validation Failure", validationErrors.get(validationErrors.size() - 1).values().iterator().next());
		}
	}

	static void validateConnectivity(Long taskId, Boolean forced, Map<String, Object> context, Db.Row taskRow)
			throws Exception {
		if ("false".equalsIgnoreCase(getGlobal("TDM_SUPPRESS_TEST_CONNECTION"))) {
			try {
				testConnection(forced, context, taskRow);
			} catch (TdmWarningException | TdmValidationException e) {
				throw e;
			} catch (Exception e) {
				throw new TdmValidationException("Test Connection Failed", e.getMessage());
			}
		}
		String msg = fnValidateOverrideSyncMode((Long) context.get("srcEnvId"), (String) context.get("sourceEnvName"),
				(String) taskRow.get("sync_mode"));
		if (!"".equalsIgnoreCase(msg))
			throw new TdmValidationException("Validation Failure", msg);
	}

	// In case of task with delete and it is not a versioning task, the source and target environments must be different.
	static void validateEnvsForTaskWithDelete(Db.Row taskRow, Map<String, Object> context) throws TdmValidationException {
		
		if ((Boolean) taskRow.get("delete_before_load") && (Long) context.get("srcEnvId") == (Long) context.get("tarEnvId")) {
			if (("TABLES".equalsIgnoreCase(context.get("finalSelectionMethod").toString()) && !(Boolean)(taskRow.get("in_place_masking_ind"))) ||
				(!"TABLES".equalsIgnoreCase(context.get("finalSelectionMethod").toString()) &&
				"load".equalsIgnoreCase(taskRow.get("task_type").toString()) &&
				(Boolean) taskRow.get("version_ind") == false)) {
				throw new TdmValidationException("Invalid Task Configuration", "For Task with Delete, the source and target Environments cannot be the same");
			}
		}
	}
	
	static void validateReservedEntityList(Boolean forced, Db.Row taskRow,
			Map<String, Object> context, Map<String, Object> overrideParams) throws Exception {
		if (Boolean.TRUE.equals(forced))
			return;

		String taskType = "" + taskRow.get("task_type");
		if ("EXTRACT".equalsIgnoreCase(taskType))
			return;
		if (Boolean.TRUE.equals(taskRow.get("replace_sequences")))
			return;
		if (Boolean.TRUE.equals(taskRow.get("clone_ind")))
			return;

		String finalMethod = (String) context.get("finalSelectionMethod");
		if (!"L".equalsIgnoreCase(finalMethod))
			return;

		String filteroutReserved = taskRow.get("filterout_reserved") != null
				? "" + taskRow.get("filterout_reserved")
				: "NA";
		if ("NA".equalsIgnoreCase(filteroutReserved))
			return;

		Object entityListRaw = overrideParams.get(OverrideParamKey.ENTITY_LIST.name());
		String entitiesListStr = entityListRaw != null
				? entityListRaw.toString()
				: "" + taskRow.get("selection_param_value");
		if (entitiesListStr == null || "null".equals(entitiesListStr) || entitiesListStr.isBlank())
			return;

		Long tarEnvId = (Long) context.get("tarEnvId");
		if (tarEnvId == null)
			return;

		String beId = (String) context.get("finalBeId");
		ArrayList<String> entities = new ArrayList<>(Arrays.asList(entitiesListStr.split(",")));

		Map<String, Object> validation = fnValidateReservedEntities(
				beId, tarEnvId.toString(), entities, filteroutReserved);

		List<Map<String, Object>> reservedList = (List<Map<String, Object>>) validation.get("listOfEntities");
		if (reservedList != null && !reservedList.isEmpty()) {
			List<String> entityIds = new ArrayList<>();
			for (Map<String, Object> entity : reservedList) {
				entityIds.add((String) entity.get("entity_id"));
			}
			throw new TdmWarningException("The task contains reserved entities",
					"The task contains reserved entities: " + String.join(",", entityIds));
		}
	}

	private static void testConnection(Boolean forced, Map<String, Object> context, Db.Row taskRow)throws Exception {

		if (Boolean.TRUE.equals(forced)) {
			return;
		}

		String taskType = "" + taskRow.get("task_type");
		String syncMode = "" + taskRow.get("sync_mode");

		String sourceEnvName = (String) context.get("sourceEnvName");
		String targetEnvName = (String) context.get("targetEnvName");

		Long sourceEnvId = (Long) context.get("srcEnvId");
		Long targetEnvId = (Long) context.get("tarEnvId");

		if ("EXTRACT".equalsIgnoreCase(taskType)) {
			testEnvConnection(sourceEnvName, sourceEnvId);
		} else if ("OFF".equalsIgnoreCase(syncMode)) {
			testEnvConnection(targetEnvName, targetEnvId);
		} else {
			testEnvConnection(sourceEnvName, sourceEnvId);
			testEnvConnection(targetEnvName, targetEnvId);
		}
	}

	private static void testEnvConnection(String envName, Long envId) throws Exception {
		try {
			fnTestInterfacesForEnvProduct(envName);

		} catch (Exception e) {
			String msg;

			if (e.getMessage() != null && e.getMessage().startsWith("interfaceFailed;")) {
				msg = "The test connection of " + e.getMessage().substring(16)
						+ " failed. Please check the connection details of environment: " + envName;
			} else {
				msg = "Failed to test interfaces for environment: " + envName;
			}

			if (isAllowedTestConnFailure(String.valueOf(envId))) {
				throw new TdmWarningException("Test Connection Failed", msg);
			}

			throw new TdmValidationException("Test Connection Failed", msg);
		}
	}

	
	
	static void validateBeforeRun(Db.Row taskRow, Map<String, Object> context,
			Map<OverrideParamKey, Object> inputOverrides, Map<String, Object> overrideParams) throws Exception {
		// 1. Check Extract No Retention rule — use override retention type if provided
		Map<String, String> dataVersionRetentionPeriod = (Map<String, String>) inputOverrides
				.get(OverrideParamKey.DATAFLUX_RETENTION_PARAMS);
		String effectiveRetentionType = (dataVersionRetentionPeriod != null
				&& dataVersionRetentionPeriod.get("units") != null)
						? dataVersionRetentionPeriod.get("units")
						: "" + taskRow.get("retention_period_type");
		String extractError = fnValidateExtractNoRetention(
				"" + taskRow.get("task_type"),
				(String) context.get("finalSelectionMethod"),
				effectiveRetentionType);
		if (!"".equalsIgnoreCase(extractError))
			throw new TdmValidationException("Invalid Task Configuration", extractError);

		// 2. Validate data version execution ID
		Long dataVersionExecId = (Long) inputOverrides.get(OverrideParamKey.SELECTED_VERSION_TASK_EXE_ID);
		if (dataVersionExecId != null) {
			Map<String, String> versionValidation = fnValidateVersionExecIdAndGetDetails(dataVersionExecId,
					(Map<String, Object>) context.get("beScope"), (String) context.get("sourceEnvName"));
			if (versionValidation.get("errorMessage") != null)
				throw new TdmValidationException("Data Version Validation", versionValidation.get("errorMessage"));
			String expiryMsg = fnValidateVersionExecIdNotExpired(dataVersionExecId);
			if (!expiryMsg.isEmpty())
				throw new TdmValidationException("Data Version Validation", expiryMsg);
			overrideParams.put(OverrideParamKey.SELECTED_VERSION_TASK_EXE_ID.name(), dataVersionExecId);
		}

		// 3. Validate retention period params
		String taskType = "" + taskRow.get("task_type");
		boolean versionInd = (Boolean) taskRow.get("version_ind");
		String createdBy = "" + taskRow.get("task_created_by");
		String targetEnvName = (String) context.get("targetEnvName");
		if (dataVersionRetentionPeriod != null) {
			Map<String, String> msgs = fnValidateRetentionPeriodParams(dataVersionRetentionPeriod, "retention",
					targetEnvName, versionInd, createdBy);
			if (msgs != null && !msgs.isEmpty())
				throw new TdmValidationException("Retention Period Validation", msgs.get("retention"));
			overrideParams.put(OverrideParamKey.DATAFLUX_RETENTION_PARAMS.name(), dataVersionRetentionPeriod);
		} else if (!"reserve".equalsIgnoreCase(taskType)
				&& (!(Boolean) taskRow.get("delete_before_load") || (Boolean) taskRow.get("load_entity"))) {
			Map<String, String> dataRetentionPeriod = new HashMap<>();
			dataRetentionPeriod.put("units", (String) taskRow.get("retention_period_type"));
			dataRetentionPeriod.put("value", String.valueOf(taskRow.get("retention_period_value")));
			Map<String, String> msgs = fnValidateRetentionPeriodParams(dataRetentionPeriod, "retention",
					targetEnvName, versionInd, createdBy);
			if (msgs != null && !msgs.isEmpty())
				throw new TdmValidationException("Retention Period Validation", msgs.get("retention"));
		}

		// 4. Validate reserve retention period params
		Boolean reserveInd = (Boolean) context.get("reserveInd");
		if (reserveInd) {
			Map<String, String> reserveRetention = (Map<String, String>) inputOverrides
					.get(OverrideParamKey.RESERVE_RETENTION_PARAMS);
			if (reserveRetention != null) {
				Map<String, String> msgs = fnValidateRetentionPeriodParams(reserveRetention, "reserve",
						targetEnvName, false, createdBy);
				if (msgs != null && !msgs.isEmpty())
					throw new TdmValidationException("Reservation Period Validation", msgs.get("reservation"));
				overrideParams.put(OverrideParamKey.RESERVE_RETENTION_PARAMS.name(), reserveRetention);
			} else {
				Map<String, String> dataReservePeriod = new HashMap<>();
				dataReservePeriod.put("units", (String) taskRow.get("reserve_retention_period_type"));
				dataReservePeriod.put("value", String.valueOf(taskRow.get("reserve_retention_period_value")));
				Map<String, String> msgs = fnValidateRetentionPeriodParams(dataReservePeriod, "reserve",
						targetEnvName, false, createdBy);
				if (msgs != null && !msgs.isEmpty())
					throw new TdmValidationException("Reservation Period Validation", msgs.get("reservation"));
			}
		}
		String finalMethod = (String) context.get("finalSelectionMethod");
		if ("R".equalsIgnoreCase(finalMethod)) {
			verifyRandomDataAvailability(taskRow, context);
		}
		if ("P".equalsIgnoreCase(finalMethod) || "PR".equalsIgnoreCase(finalMethod)) {
			validateBpParamValues(taskRow, inputOverrides);
			validateBpEntityCount(taskRow, context, inputOverrides);
		}
		if ("C".equalsIgnoreCase(finalMethod)) {
			validateMandatoryCustomLogicParams(taskRow, inputOverrides);
		}
		validateProcessMandatoryParams(taskRow, inputOverrides);

		// 5. Validate effective version is set when in load-data-version mode
		if (versionInd && "OFF".equalsIgnoreCase("" + taskRow.get("sync_mode")) && !"TABLES".equalsIgnoreCase(finalMethod)) {
			Long effectiveVersionId = (Long) inputOverrides.get(OverrideParamKey.SELECTED_VERSION_TASK_EXE_ID);
			boolean isOverrideVersion = effectiveVersionId != null;
			if (!isOverrideVersion) {
				Object savedVersionId = taskRow.get("selected_version_task_exe_id");
				effectiveVersionId = savedVersionId != null ? Long.parseLong("" + savedVersionId) : null;
			}
			if (effectiveVersionId == null || effectiveVersionId == 0) {
				throw new TdmValidationException("Data Version Validation", "Data version selection is mandatory.");
			}
			// Expiry is already checked in section 2 for override versions; check here for task-row versions
			if (!isOverrideVersion) {
				String expiryMsg = fnValidateVersionExecIdNotExpired(effectiveVersionId);
				if (!expiryMsg.isEmpty())
					throw new TdmValidationException("Data Version Validation", expiryMsg);
			}
		}
	}

	private static void verifyRandomDataAvailability(Db.Row taskRow, Map<String, Object> context) throws Exception {
		String srcEnv = (String) context.get("sourceEnvName");
		boolean isParamCoupling = isParamsCoupling();

		List<Map<String, Object>> lus = (List<Map<String, Object>>) context.get("logicalUnits");
		if (lus == null || lus.isEmpty()) {
			throw new TdmValidationException("Selection Method Validation","No Logical Units defined for this task validation.");
		}

		// Find the first root LU (no parent) and validate only that one
		Map<String, Object> rootLu = null;
		for (Map<String, Object> lu : lus) {
			if (lu.get("lu_parent_id") == null) {
				rootLu = lu;
				break;
			}
		}
		if (rootLu == null) {
			throw new TdmValidationException("Selection Method Validation","No root Logical Units defined for this task validation.");
		}

		String luName = (String) rootLu.get("lu_name");

		// Resolve table and column names per LU
		String luParamsTable = isParamCoupling
				? luName.toLowerCase() + ".fabric_tdm_root"
				: TDMDB_SCHEMA + "." + luName.toLowerCase() + "_params";
		String envColumn = isParamCoupling ? "source_env" : "source_environment";

		String checkSql = "SELECT 1 FROM " + luParamsTable + " WHERE " + envColumn + " = ? LIMIT 1";

		Object exists;
		try {
			exists = db(TDM).fetch(checkSql, srcEnv).firstValue();
		} catch (Exception e) {
			UserCode.log.error("TDM StartTask Validation failed for " + luName + ": " + e.getMessage());			
			throw new TdmValidationException(
					"Selection Method Validation","Please ensure the TDM DB contains data for the selected Business Entity and source environment before using random selection.");
		}
		if (exists == null) {
			throw new TdmValidationException(
					"Selection Method Validation","Please ensure the TDM DB contains data for the selected Business Entity and source environment before using random selection.");
		}
	}

	private static void validateBpEntityCount(Db.Row taskRow, Map<String, Object> context,
			Map<OverrideParamKey, Object> inputOverrides) throws Exception {
		// Validate num_of_entities: must be > 0 or -1 (ALL)
		Object noEntitiesOverride = inputOverrides.get(OverrideParamKey.NO_OF_ENTITIES);
		Object effectiveNumEntities = (noEntitiesOverride != null && !noEntitiesOverride.toString().isBlank()
				&& !"null".equals(noEntitiesOverride.toString()))
				? noEntitiesOverride : taskRow.get("num_of_entities");
		if (effectiveNumEntities != null) {
			long numEntities = Long.parseLong(effectiveNumEntities.toString());
			if (numEntities != -1 && numEntities < 1) {
				throw new TdmValidationException("Business Parameters Validation",
						"Number of entities must be greater than 0 or ALL.");
			}
		}

		// Effective BP query: GUI override takes precedence over task definition
		Object overrideBpQueryRaw = inputOverrides.get(OverrideParamKey.BP_QUERY);
		String taskBpQuery = "" + taskRow.get("selection_param_value");
		String effectiveBpQuery = (overrideBpQueryRaw != null
				&& !overrideBpQueryRaw.toString().isBlank()
				&& !"null".equals(overrideBpQueryRaw.toString()))
						? overrideBpQueryRaw.toString() : taskBpQuery;

		// Validate that parameters are configured (not empty / no rules)
		if (effectiveBpQuery == null || effectiveBpQuery.isBlank()
				|| "null".equals(effectiveBpQuery) || "()".equals(effectiveBpQuery.trim())) {
			throw new TdmValidationException("Business Parameters Validation",
					"Business Parameters selection requires at least one configured parameter, and all parameter values must be populated.");
		}

		// Effective parameters JSON (used by fnGetNumberOfMatchingEntities)
		Object overrideParamsRaw = inputOverrides.get(OverrideParamKey.PARAMETERS);
		String queryJson = (overrideParamsRaw != null
				&& !overrideParamsRaw.toString().isBlank()
				&& !"null".equals(overrideParamsRaw.toString()))
						? overrideParamsRaw.toString() : "" + taskRow.get("parameters");

		String sourceEnvName = (String) context.get("sourceEnvName");
		String targetEnvName = (String) context.get("targetEnvName");
		Object beIdRaw = taskRow.get("be_id");
		if (beIdRaw == null || "null".equals(beIdRaw.toString())) {
			beIdRaw = inputOverrides.get(OverrideParamKey.BE_ID);
		}
		if (beIdRaw == null)
			throw new TdmValidationException("Business Parameters Validation", "Business entity ID is missing.");
		Long beId = Long.parseLong(beIdRaw.toString());		
		String filteroutReserved = taskRow.get("filterout_reserved") != null
				? "" + taskRow.get("filterout_reserved") : "NA";

		// Validate at least one entity matches the business parameters
		try {
			Object response = fnGetNumberOfMatchingEntities(
					effectiveBpQuery, queryJson, sourceEnvName, targetEnvName, beId, filteroutReserved, false);
			// fnGetNumberOfMatchingEntities wraps its result via wrapWebServiceResults → Map with "result" key
			Map<String, Object> responseMap = (Map<String, Object>) response;
			Object countObj = responseMap != null ? responseMap.get("result") : null;
			long count = countObj != null ? Long.parseLong(countObj.toString()) : 0L;
			if (count == 0) {
				throw new TdmValidationException("Business Parameters Validation",
						"No entities match the configured business parameters. Please adjust the parameters and try again.");
			}
		} catch (TdmValidationException e) {
			throw e;
		} catch (Exception e) {
			UserCode.log.warn("Business parameters entity count check failed: " + e.getMessage());
			throw new TdmValidationException("Business Parameters Validation",
					"Failed to validate business parameters: " + e.getMessage());
		}
	}

	private static void validateMandatoryCustomLogicParams(
			Db.Row taskRow, Map<OverrideParamKey, Object> inputOverrides) throws TdmValidationException {
		Object paramOverride = inputOverrides.get(OverrideParamKey.PARAMETERS);
		String effectiveParams = (paramOverride != null && !paramOverride.toString().isBlank())
				? paramOverride.toString()
				: (taskRow.get("parameters") != null ? taskRow.get("parameters").toString() : null);
		if (effectiveParams == null || effectiveParams.isBlank())
			return;

		String overrideFlow   = (String) inputOverrides.get(OverrideParamKey.CUSTOM_LOGIC_FLOW);
		String overrideLuName = (String) inputOverrides.get(OverrideParamKey.CUSTOM_LOGIC_LU_NAME);
		String effectiveFlow   = (overrideFlow   != null && !overrideFlow.isBlank())   ? overrideFlow
				: "" + taskRow.get("selection_param_value");
		String effectiveLuName = (overrideLuName != null && !overrideLuName.isBlank()) ? overrideLuName
				: "" + taskRow.get("custom_logic_lu_name");

		try {
			String error = validateParams("C", effectiveParams, effectiveLuName, effectiveFlow);
			if (error != null && !error.isBlank())
				throw new TdmValidationException("Mandatory Params Validation", error);
		} catch (TdmValidationException e) {
			throw e;
		} catch (Exception e) {
			throw new TdmValidationException("Mandatory Params Validation",
					"Failed to validate custom logic parameters: " + e.getMessage());
		}
	}

	private static final Set<String> NULL_VALUE_OPERATORS =
			Set.of("is_null", "is_not_null", "is_empty", "is_not_empty");

	private static void validateBpParamValues(
			Db.Row taskRow, Map<OverrideParamKey, Object> inputOverrides) throws TdmValidationException {
		Object paramOverride = inputOverrides.get(OverrideParamKey.PARAMETERS);
		String effectiveParams = (paramOverride != null && !paramOverride.toString().isBlank())
				? paramOverride.toString()
				: (taskRow.get("parameters") != null ? taskRow.get("parameters").toString() : null);
		if (effectiveParams == null || effectiveParams.isBlank())
			return;

		try {
			Map<String, Object> root = Json.get().fromJson(effectiveParams);
			List<String> errors = new ArrayList<>();
			collectBpValueErrors(root, errors);
			if (!errors.isEmpty())
				throw new TdmValidationException("Business Parameters Validation",
						"Business parameter(s) have empty values: " + String.join("; ", errors));
		} catch (TdmValidationException e) {
			throw e;
		} catch (Exception ignored) {
			// malformed JSON is already caught by validateBpEntityCount
		}
	}

	private static void collectBpValueErrors(Map<String, Object> node, List<String> errors) {
		if (node == null)
			return;
		Object rulesRaw = node.get("rules");
		if (!(rulesRaw instanceof List))
			return;
		for (Object r : (List<?>) rulesRaw) {
			if (!(r instanceof Map))
				continue;
			Map<String, Object> rule = (Map<String, Object>) r;
			if (rule.containsKey("rules")) {
				collectBpValueErrors(rule, errors);
			} else {
				Object field    = rule.get("field");
				Object operator = rule.get("operator");
				Object data     = rule.get("data");
				if (field == null)
					continue;
				String op = operator != null ? operator.toString().toLowerCase() : "";
				if (!NULL_VALUE_OPERATORS.contains(op) && isBpValueEmpty(data))
					errors.add("'" + field + "' has no value");
			}
		}
	}

	private static boolean isBpValueEmpty(Object v) {
		if (v == null)
			return true;
		if (v instanceof String)
			return ((String) v).isBlank();
		if (v instanceof List)
			return ((List<?>) v).isEmpty();
		return false;
	}
	
	private static List<Map<String, Object>> getProcessEditors(String processType, Map<String, String> processRec) {
		try {
			List<HashMap<String, Object>> processList = fnGetExecutionProcessParams(processType, new ArrayList<>(List.of(processRec)));
			if (processList == null || processList.isEmpty()) return Collections.emptyList();
			List<Map<String, Object>> editors = (List<Map<String, Object>>) processList.get(0).get("editors");
			return editors != null ? editors : Collections.emptyList();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private static void validateProcessMandatoryParams(Db.Row taskRow,
			Map<OverrideParamKey, Object> inputOverrides) throws TdmValidationException {
		long taskId = ((Number) taskRow.get("task_id")).longValue();
		String sql = "SELECT process_id, process_name, process_type, lu_name, parameters FROM "
				+ TDMDB_SCHEMA + ".tasks_exe_process WHERE task_id = ?";
		try (Db.Rows rows = db(TDM).fetch(sql, taskId)) {
			for (Db.Row row : rows) {
				String processName = row.get("process_name") != null
						? row.get("process_name").toString()
						: null;
				String processType = row.get("process_type") != null
						? row.get("process_type").toString()
						: null;

				String luName = row.get("lu_name") != null
						? row.get("lu_name").toString()
						: null;
				if (processName == null)
					continue;

				// Build stored-value map from the parameters JSON
				Map<String, Object> storedValues = new HashMap<>();
				Object raw = row.get("parameters");
				if (raw != null && !raw.toString().isBlank()) {
					Map<String, Object> parsed = Json.get().fromJson(raw.toString());
					Object inputsRaw = parsed.get("inputs");
					if (inputsRaw instanceof List) {
						for (Object item : (List<?>) inputsRaw) {
							if (item instanceof Map) {
								Map<String, Object> p = (Map<String, Object>) item;
								Object name = p.get("name");
								if (name != null)
									storedValues.put(name.toString(), p.get("value"));
							}
						}
					}
				}

				// Apply execution-time overrides — they take precedence over stored values
				if (inputOverrides != null) {
					long processId = ((Number) row.get("process_id")).longValue();
					for (OverrideParamKey procKey : List.of(
							OverrideParamKey.PRE_EXECUTION_PROCESSES_PARAMS,
							OverrideParamKey.POST_EXECUTION_PROCESSES_PARAMS)) {
						List<Map<String, Object>> procOverrides = (List<Map<String, Object>>) inputOverrides
								.get(procKey);
						if (procOverrides == null)
							continue;
						for (Map<String, Object> entry : procOverrides) {
							Object pidObj = entry.get("process_id");
							if (pidObj == null || ((Number) pidObj).longValue() != processId)
								continue;
							List<Map<String, Object>> paramOverrides = (List<Map<String, Object>>) entry
									.get("parameter_overrides");
							if (paramOverrides == null)
								continue;
							for (Map<String, Object> override : paramOverrides) {
								Object oName = override.get("name");
								if (oName != null)
									storedValues.put(oName.toString(), override.get("value"));
							}
						}
					}
				}

				// Get mandatory param definitions from the Broadway flow definition
				Map<String, String> processRec = new HashMap<>();
				processRec.put("luName", luName);
				processRec.put("processName", processName);
				
				for (Map<String, Object> editor : getProcessEditors(processType ,processRec)) {
					if (!Boolean.TRUE.equals(editor.get("mandatory")))
						continue;
					Object editorObj = editor.get("editor");
					if (!(editorObj instanceof Map))
						continue;
					String paramName = (String) ((Map<?, ?>) editorObj).get("name");
					if (paramName == null)
						continue;
					if (isBpValueEmpty(storedValues.get(paramName))) {
						throw new TdmValidationException("Process Params Validation",
								"Mandatory parameter '" + paramName + "' in " + processType
										+ " process '" + processName + "' has no value.");
					}
				}
			}
		} catch (TdmValidationException e) {
			throw e;
		} catch (Exception e) {
			throw new TdmValidationException("Process Params Validation",
					"Failed to validate process parameters: " + e.getMessage());
		}
	}

	static Map<String, Object> parseLuOverrideFields(Object raw) {
		if (raw == null)
			return null;
		String json = raw.toString().trim();
		if (json.isEmpty() || "null".equals(json))
			return null;
		try {
			return (Map<String, Object>) Json.get().fromJson(json);
		} catch (Exception e) {
			return null;
		}
	}

	static boolean isLuFieldEditable(Map<String, Object> overrideFields, String fieldName) {
		if (overrideFields == null)
			return false;
		return Boolean.TRUE.equals(overrideFields.get(fieldName));
	}

	static class TdmWarningException extends Exception {
		private final Object errorDetails;

		public TdmWarningException(String category, Object errorDetails) {
			super(category);
			this.errorDetails = errorDetails;
		}

		public String getCategory() {
			return super.getMessage();
		}

		public Object getErrorDetails() {
			return errorDetails;
		}
	}

	static class TdmValidationException extends Exception {
		private final Object errorDetails;

		public TdmValidationException(String category, Object errorDetails) {
			super(category);
			this.errorDetails = errorDetails;
		}

		public String getCategory() {
			return super.getMessage();
		}

		public Object getErrorDetails() {
			return errorDetails;
		}
	}
}
