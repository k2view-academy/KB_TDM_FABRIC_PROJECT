package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_ExecutionMonitor;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnGetReferenceDetailedData;
import static com.k2view.cdbms.usercode.common.TDM.TaskExecutionUtils.SharedLogic.fnMigrateStatusWs;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_Tasks.Logic.getTaskHistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.*;


@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends WebServiceUserCode {

    private static final String TDM = "TDM";
    private static final Set<String> TERMINAL_STATUSES = Set.of("completed", "failed", "stopped");

    @desc("""
        Returns task execution monitoring details in one call by merging:
        - task/{taskExeId}/history
        - migrateStatusWs
        - extractrefstats

        Only taskExecutionId is required. migrateStatus's batch IDs are derived from the history
        rows instead of being passed in: every history row with a non-terminal execution_status
        (i.e. not completed/failed/stopped) and a non-empty fabric_execution_id is queried via
        migrateStatusWs, using run mode "S".

        """)
    @webService(
        path = "executionMonitorDetails",
        verb = {MethodType.POST},
        version = "1",
        isRaw = false,
        isCustomPayload = false,
        produce = {Produce.XML, Produce.JSON},
        elevatedPermission = true
    )
    @resultMetaData(mediaType = Produce.JSON, example = """
        {
          "result": {
            "history": {
              "summary": {},
              "result": [],
              "errorCode": "SUCCESS",
              "message": null
            },
            "migrateStatus": {
              "result": [],
              "errorCode": "SUCCESS",
              "message": null
            },
            "referenceStats": {
              "result": [],
              "errorCode": "SUCCESS",
              "message": null
            }
          },
          "errorCode": "SUCCESS",
          "message": null
        }
        """)
    public static Object wsGetExecutionMonitorDetails(
            @param(required = true, description = "Task execution ID used to derive History, Migrate Status and Reference Statistics")
            Long taskExecutionId) throws Exception {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> combinedResult = new HashMap<>();

        if (taskExecutionId == null) {
            response.put("result", combinedResult);
            response.put("errorCode", "FAILED");
            response.put("message", "taskExecutionId is required.");
            return response;
        }

        Map<String, Object> history = executeHistorySection(taskExecutionId);
        List<String> runningBatchIds = deriveRunningBatchIds(history);
        Map<String, Object> migrateStatus = executeBatchStatusSection(runningBatchIds, List.of("S"));
        Map<String, Object> referenceStats = executeReferenceStatsSection(taskExecutionId);

        combinedResult.put("history", history);
        combinedResult.put("migrateStatus", migrateStatus);
        combinedResult.put("referenceStats", referenceStats);

        boolean success =
                isSuccessful(history)
                && isSuccessful(migrateStatus)
                && isSuccessful(referenceStats);

        response.put("result", combinedResult);
        response.put("errorCode", success ? "SUCCESS" : "FAILED");
        response.put("message", success ? null : "One or more monitor sections failed.");
        return response;
    }

    private static Map<String, Object> executeHistorySection(Long taskExecutionId) {
        try {
            return (Map<String, Object>) getTaskHistory(taskExecutionId);
        } catch (Exception e) {
            log.error("Failed to get task history for execution " + taskExecutionId, e);
            return failedSection(e.getMessage());
        }
    }

    private static Map<String, Object> executeBatchStatusSection(List<String> batchIds, List<String> runMode) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (batchIds.isEmpty()) {
                response.put("result", null);
                response.put("errorCode", "SUCCESS");
                response.put("message", null);
                return response;
            }

            List<HashMap<String, Object>> results = new ArrayList<>();
            for (String batchId : batchIds) {
                results.add(fnMigrateStatusWs(batchId, runMode));
            }

            response.put("result", results);
            response.put("errorCode", "SUCCESS");
            response.put("message", null);
        } catch (Exception e) {
            log.error("Failed to get migrate status", e);
            response.put("result", null);
            response.put("errorCode", "FAILED");
            response.put("message", e.getMessage());
        }
        return response;
    }

    private static List<String> deriveRunningBatchIds(Map<String, Object> history) {
        List<String> batchIds = new ArrayList<>();
        Object rows = history.get("result");
        if (rows instanceof List<?>) {
            for (Object rowObj : (List<?>) rows) {
                if (rowObj instanceof Map) {
                    Map<?, ?> row = (Map<?, ?>) rowObj;
                    Object fabricExecId = row.get("fabric_execution_id");
                    if (fabricExecId != null && !fabricExecId.toString().isEmpty()
                            && !isTerminalStatus(row.get("execution_status"))) {
                        batchIds.add(fabricExecId.toString());
                    }
                }
            }
        }
        return batchIds;
    }

    private static boolean isTerminalStatus(Object status) {
        return status != null && TERMINAL_STATUSES.contains(status.toString().toLowerCase());
    }

    private static Map<String, Object> executeReferenceStatsSection(Long taskExecutionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Object data = fnGetReferenceDetailedData(String.valueOf(taskExecutionId));
            response.put("result", data);
            response.put("errorCode", "SUCCESS");
            response.put("message", null);
        } catch (Exception e) {
            log.error("Failed to get reference statistics for execution " + taskExecutionId, e);
            response.put("result", null);
            response.put("errorCode", "FAILED");
            response.put("message", e.getMessage());
        }
        return response;
    }

    private static boolean isSuccessful(Map<String, Object> section) {
        return section != null && "SUCCESS".equals(section.get("errorCode"));
    }

    private static Map<String, Object> failedSection(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("result", null);
        response.put("errorCode", "FAILED");
        response.put("message", message);
        return response;
    }
}
