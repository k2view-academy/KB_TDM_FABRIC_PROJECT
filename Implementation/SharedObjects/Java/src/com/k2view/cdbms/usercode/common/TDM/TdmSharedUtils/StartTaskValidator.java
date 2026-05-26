package com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils;

import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.shared.user.UserCode.fabric;
import static com.k2view.cdbms.shared.user.UserCode.getGlobal;
import static com.k2view.cdbms.shared.user.UserCode.sessionUser;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.isParamsCoupling;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnTestInterfacesForEnvProduct;
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
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetReservedEntitiesNumber;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserEnvs;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.fnGetUserPermissionGroup;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			throw new Exception("Mandatory Job(s) Down: " + downJobsBuilder + ". Cannot start task.");
		}
	}

	static void validateInputOverrides(Db.Row taskRow,
			Map<OverrideParamKey, Object> inputOverrides) throws TdmValidationException {
		if (inputOverrides.isEmpty())
			return;

		if (inputOverrides.containsKey(null))
			throw new TdmValidationException("invalid input",
					"Invalid override parameter key: null key is not allowed.");

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
			throw new TdmValidationException("override validation",
					"Business parameters cannot be overridden for this task.");
		}
		checkEditable(fields, inputOverrides, OverrideParamKey.RESERVE_IND,
				"Reservation period", "reservation_period");
		checkEditable(fields, inputOverrides, OverrideParamKey.RESERVE_RETENTION_PARAMS,
				"Reservation period", "reservation_period");

		if (inputOverrides.containsKey(OverrideParamKey.NO_OF_ENTITIES)) {
			String[] path = "R".equalsIgnoreCase(taskMethod)
					? new String[] { "selection_method", "random" }
					: new String[] { "selection_method", "max_entities" };
			if (!isEditableAt(fields, path))
				throw new TdmValidationException("override validation",
						"Number of entities cannot be overridden for this task.");
		}

		if (inputOverrides.containsKey(OverrideParamKey.PARAMETERS)) {
			String effectiveMethod = inputOverrides.containsKey(OverrideParamKey.SELECTION_METHOD)
					? (String) inputOverrides.get(OverrideParamKey.SELECTION_METHOD)
					: taskMethod;
			if ("C".equalsIgnoreCase(effectiveMethod)) {
				if (!isEditableAt(fields, "selection_method", "custom_logic", "can_add_params"))
					throw new TdmValidationException("override validation",
							"Parameters cannot be overridden for this task.");
			} else {
				validateBpParametersOverride(fields, taskRow,
						(String) inputOverrides.get(OverrideParamKey.PARAMETERS));
			}
		}

		if (inputOverrides.containsKey(OverrideParamKey.LOGICAL_UNITS)) {
			Object raw = inputOverrides.get(OverrideParamKey.LOGICAL_UNITS);
			if (!(raw instanceof List))
				throw new TdmValidationException("override validation", "LOGICAL_UNITS must be a list.");
			List<?> luList = (List<?>) raw;
			if (luList.isEmpty())
				throw new TdmValidationException("override validation", "LOGICAL_UNITS list must not be empty.");
			for (int i = 0; i < luList.size(); i++) {
				Object entry = luList.get(i);
				if (!(entry instanceof Map))
					throw new TdmValidationException("override validation",
							"LOGICAL_UNITS[" + i + "] must be an object.");
				Map<String, Object> lu = (Map<String, Object>) entry;
				if (lu.get("lu_id") == null)
					throw new TdmValidationException("override validation",
							"LOGICAL_UNITS[" + i + "] is missing required field 'lu_id'.");
				if (lu.get("lu_name") == null)
					throw new TdmValidationException("override validation",
							"LOGICAL_UNITS[" + i + "] is missing required field 'lu_name'.");
				Object workers = lu.get("max_no_of_workers");
				if (workers != null) {
					try {
						if (((Number) workers).intValue() <= 0)
							throw new TdmValidationException("override validation",
									"LOGICAL_UNITS[" + i + "].max_no_of_workers must be a positive integer.");
					} catch (ClassCastException e) {
						throw new TdmValidationException("override validation",
								"LOGICAL_UNITS[" + i + "].max_no_of_workers must be a number.");
					}
				}
			}
		}
	}

	private static void checkEditable(Map<String, Object> fields,
			Map<OverrideParamKey, Object> inputOverrides,
			OverrideParamKey key, String label, String... path) throws TdmValidationException {
		if (inputOverrides.containsKey(key) && !isEditableAt(fields, path))
			throw new TdmValidationException("override validation",
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
			throw new TdmValidationException("override validation",
					"Parameters override is not valid JSON.");
		}
		Map<String, Map<String, Object>> overrideIndex = buildParamRuleIndex(overrideParsed);

		boolean canAddNew = isEditableAt(fields, "selection_method", "business_parameters");

		for (Map.Entry<String, Map<String, Object>> entry : overrideIndex.entrySet()) {
			String field = entry.getKey();
			Map<String, Object> origRule = originalIndex.get(field);
			if (origRule == null) {
				if (!canAddNew)
					throw new TdmValidationException("override validation",
							"Adding new business parameter '" + field + "' is not allowed for this task.");
			} else {
				if (!Boolean.TRUE.equals(origRule.get("is_editable")))
					throw new TdmValidationException("override validation",
							"Business parameter '" + field + "' cannot be overridden for this task.");
			}
		}
	}

	static void verifyUserPermissions(Long taskId, Db.Row taskRow, Map<String, Object> context,
			Map<String, Object> overrideParams, String providedUserName) throws Exception {
		if (!fnValidateParallelExecutions(taskId, overrideParams))
			throw new Exception("Task already running");

		String userName;
		if (providedUserName != null && !providedUserName.isEmpty()) {
			userName = providedUserName;
		} else {
			userName = "TDM.tdmTaskScheduler".equalsIgnoreCase(sessionUser().name())
					? "" + taskRow.get("task_created_by")
					: sessionUser().name();
		}
		List<Map<String, Object>> rolesList = fnGetUserEnvs(userName);
		List<Map<String, Object>> srcRoles = new ArrayList<>();
		List<Map<String, Object>> trgRoles = new ArrayList<>();

		for (Map<String, Object> envType : rolesList) {
			if (envType.get("source environments") != null)
				srcRoles = (List<Map<String, Object>>) (envType.get("source environments"));
			if (envType.get("target environments") != null)
				trgRoles = (List<Map<String, Object>>) (envType.get("target environments"));
		}

		String taskType = "" + taskRow.get("task_type");
		Integer refcount = taskRow.get("refcount") != null ? Integer.parseInt(taskRow.get("refcount").toString()) : 0;
		Integer finalCount = (Integer) context.get("finalCount");
		Map<String, Object> beScope = (Map<String, Object>) context.get("beScope");

		List<Map<String, String>> validationErrors = new ArrayList<>();
		boolean srcValid = false, trgValid = false, srcFound = false, trgFound = false;
		long readLimit = -1L, reserveLimit = -1L, writeLimit = -1L, maxAllowedEntities = -1L;
		String permissionContext = "";

		// --- SOURCE VALIDATION ---
		if (!"reserve".equalsIgnoreCase(taskType)
				&& (!(Boolean) taskRow.get("delete_before_load") || (Boolean) taskRow.get("load_entity"))) {
			if (srcRoles == null || srcRoles.isEmpty())
				throw new Exception("User has no read permissions for this environment");
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
				throw new Exception("User has no write permissions for this environment");
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
										"" + beScope.get("be_id"), userName);
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
									"" + beScope.get("be_id"), userName);
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
			if (!srcFound)
				validationErrors.add(Collections.singletonMap("SourceEnvironment", "No Source Environment found"));
			if (!trgFound)
				validationErrors.add(Collections.singletonMap("TargetEnvironment", "No Target Environment found"));
			throw new TdmValidationException("validation failure", validationErrors.get(validationErrors.size() - 1));
		}
	}

	static void validateConnectivity(Long taskId, Boolean forced, Map<String, Object> context, Db.Row taskRow)
			throws Exception {
		if ("false".equalsIgnoreCase(getGlobal("TDM_SUPPRESS_TEST_CONNECTION"))) {
			try {
				testConnection(forced, context, taskRow);
			} catch (Exception e) {
				throw new TdmWarningException("Test Connection Failed", e.getMessage());
			}
		}
		String msg = fnValidateOverrideSyncMode((Long) context.get("srcEnvId"), (String) context.get("sourceEnvName"),
				(String) taskRow.get("sync_mode"));
		if (!"".equalsIgnoreCase(msg))
			throw new TdmValidationException("validation failure", msg);
	}

	private static void testConnection(Boolean forced, Map<String, Object> context, Db.Row taskRow) throws Exception {
		if (Boolean.TRUE.equals(forced))
			return;

		String taskType = "" + taskRow.get("task_type");
		String syncMode = "" + taskRow.get("sync_mode");
		String sourceEnvName = (String) context.get("sourceEnvName");
		String targetEnvName = (String) context.get("targetEnvName");

		if ("EXTRACT".equalsIgnoreCase(taskType)) {
			testEnvConnection(sourceEnvName);
		} else {
			// Load (and other non-extract) tasks
			if ("OFF".equalsIgnoreCase(syncMode)) {
				testEnvConnection(targetEnvName);
			} else {
				testEnvConnection(sourceEnvName);
				testEnvConnection(targetEnvName);
			}
		}
	}

	private static void testEnvConnection(String envName) throws Exception {
		try {
			fnTestInterfacesForEnvProduct(envName);
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().startsWith("interfaceFailed;")) {
				throw new Exception("The test connection of " + e.getMessage().substring(16)
						+ " failed. Please check the connection details of environment: " + envName);
			}
			throw new Exception("Failed to test interfaces for environment: " + envName);
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
				throw new TdmValidationException("versioningtask", versionValidation.get("errorMessage"));
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
				throw new TdmValidationException("RetentionPeriod", msgs.get("retention"));
			overrideParams.put(OverrideParamKey.DATAFLUX_RETENTION_PARAMS.name(), dataVersionRetentionPeriod);
		} else if (!"reserve".equalsIgnoreCase(taskType)
				&& (!(Boolean) taskRow.get("delete_before_load") || (Boolean) taskRow.get("load_entity"))) {
			Map<String, String> dataRetentionPeriod = new HashMap<>();
			dataRetentionPeriod.put("units", (String) taskRow.get("retention_period_type"));
			dataRetentionPeriod.put("value", String.valueOf(taskRow.get("retention_period_value")));
			Map<String, String> msgs = fnValidateRetentionPeriodParams(dataRetentionPeriod, "retention",
					targetEnvName, versionInd, createdBy);
			if (msgs != null && !msgs.isEmpty())
				throw new TdmValidationException("RetentionPeriod", msgs.get("retention"));
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
					throw new TdmValidationException("ReservationPeriod", msgs.get("reservation"));
				overrideParams.put(OverrideParamKey.RESERVE_RETENTION_PARAMS.name(), reserveRetention);
			} else {
				Map<String, String> dataReservePeriod = new HashMap<>();
				dataReservePeriod.put("units", (String) taskRow.get("reserve_retention_period_type"));
				dataReservePeriod.put("value", String.valueOf(taskRow.get("reserve_retention_period_value")));
				Map<String, String> msgs = fnValidateRetentionPeriodParams(dataReservePeriod, "reserve",
						targetEnvName, false, createdBy);
				if (msgs != null && !msgs.isEmpty())
					throw new TdmValidationException("ReservationPeriod", msgs.get("reservation"));
			}
		}
		String finalMethod = (String) context.get("finalSelectionMethod");
		if ("R".equalsIgnoreCase(finalMethod)) {
			verifyRandomDataAvailability(taskRow, context);
		}
	}

	private static void verifyRandomDataAvailability(Db.Row taskRow, Map<String, Object> context) throws Exception {
		String srcEnv = (String) context.get("sourceEnvName");
		boolean isParamCoupling = isParamsCoupling();

		List<Map<String, Object>> lus = (List<Map<String, Object>>) context.get("logicalUnits");
		if (lus == null || lus.isEmpty()) {
			throw new Exception("No Logical Units defined for this task validation.");
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
			throw new Exception("No root Logical Units defined for this task validation.");
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
			String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
			String detail = errorMsg.contains("does not exist") ? " (Schema/table not found)" : " (DB Error)";
			throw new TdmValidationException(
					"Please ensure the TDM DB contains data for the selected Business Entity and source environment before using random selection.",
					"Logical Unit '" + luName + "' validation error" + detail);
		}
		if (exists == null) {
			throw new TdmValidationException(
					"Please ensure the TDM DB contains data for the selected Business Entity and source environment before using random selection.",
					"Logical Unit '" + luName + "' has no entities for environment '" + srcEnv + "'");
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
