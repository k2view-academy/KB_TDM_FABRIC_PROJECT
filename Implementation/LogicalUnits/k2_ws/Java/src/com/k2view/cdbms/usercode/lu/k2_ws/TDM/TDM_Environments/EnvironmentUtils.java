package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_Environments;

//import com.k2view.cdbms.FabricEncryption.FabricEncryption;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Log;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.shared.user.UserCode.sessionUser;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.TaskValidationsUtils.SharedLogic.validateMaxWorkers;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getGlobalMaxWorkersLimit;

@SuppressWarnings({ "DefaultAnnotationParam" })
public class EnvironmentUtils {
    private static final Log log = Log.a(UserCode.class);
    private static final String TDM = "TDM";
    private static final String schema = TDMDB_SCHEMA;

    public static String fnGetInterval(String interval) {
        if ("Day".equals(interval)) {
            return "1 day";
        } else if ("Week".equals(interval)) {
            return "1 week";
        } else if ("Month".equals(interval)) {
            return "1 month";
        } else if ("3Month".equals(interval)) {
            return "3 month";
        } else if ("Year".equals("interval")) {
            return "1 year";
        } else {
            return "1 month";
        }
    }

    public static Object fnExtractExecutionStatus(Map<String, List<Map<String, Object>>> executionsStatusGroup) {
        Map<String, Integer> executionsStatus = new HashMap<>();
        executionsStatus.put("failed", 0);
        executionsStatus.put("pending", 0);
        executionsStatus.put("paused", 0);
        executionsStatus.put("stopped", 0);
        executionsStatus.put("running", 0);
        executionsStatus.put("completed", 0);

        out: for (Map.Entry<String, List<Map<String, Object>>> entry : executionsStatusGroup.entrySet()) {
            List<Map<String, Object>> group = entry.getValue();
            for (Map<String, Object> execution : group) {
                if ("FAILED".equals(execution.get("execution_status").toString().toUpperCase())) {
                    executionsStatus.put("failed", executionsStatus.get("failed") + 1);
                    continue out;
                }
            }
            for (Map<String, Object> execution : group) {
                if ("PENDING".equals(execution.get("execution_status").toString().toUpperCase())) {
                    executionsStatus.put("pending", executionsStatus.get("pending") + 1);
                    continue out;
                }
            }
            for (Map<String, Object> execution : group) {
                if ("PAUSED".equals(execution.get("execution_status").toString().toUpperCase())) {
                    executionsStatus.put("paused", executionsStatus.get("paused") + 1);
                    continue out;
                }
            }
            for (Map<String, Object> execution : group) {
                if ("stopped".equals(execution.get("execution_status").toString().toUpperCase())) {
                    executionsStatus.put("stopped", executionsStatus.get("stopped") + 1);
                    continue out;
                }
            }

            Boolean runningFound = false;
            for (Map<String, Object> execution : group) {
                if (execution.get("execution_status") == null)
                    continue;
                if ("RUNNING".equals(execution.get("execution_status").toString().toUpperCase())
                        || "EXECUTING".equals(execution.get("execution_status").toString().toUpperCase()) ||
                        "STARTED".equals(execution.get("execution_status").toString().toUpperCase())
                        || "STARTEXECUTIONREQUESTED".equals(execution.get("execution_status").toString())) {
                    executionsStatus.put("running", executionsStatus.get("running") + 1);
                    runningFound = true;
                    break;
                }
            }
            if (runningFound == true) {
                continue out;
            }

            executionsStatus.put("completed", executionsStatus.get("completed") + 1);
        }

        return executionsStatus;
    }

    public static void fnUpdateEnvironmentDate(Long envId) {
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        try {
            String sql = "UPDATE " + schema + ".environments SET " +
                    "environment_last_updated_date=(?)," +
                    "environment_last_updated_by=(?) " +
                    "WHERE environment_id = " + envId;
            String username = sessionUser().name();
            db(TDM).execute(sql, now, username);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static Long fnAddProcutToEnvironment(long envId, Long product_id, String data_center_name,
            String product_version, Integer max_number_of_workers) throws Exception {

        String msg = validateMaxWorkers(max_number_of_workers);
        if (!"".equalsIgnoreCase(msg)) {
            throw new RuntimeException(msg);
        }
        String sql = "INSERT INTO " + schema + ".environment_products " +
                "(environment_id, product_id, data_center_name, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, max_number_of_workers) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING environment_product_id";
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String username = sessionUser().name();
        Db.Row row = db(TDM).fetch(sql,
                envId,
                product_id,
                data_center_name,
                product_version,
                username,
                now,
                now,
                username,
                "Active",
                max_number_of_workers).firstRow();
        return Long.parseLong(row.get("environment_product_id").toString());
    }

    public static void fnUpdateProductToEnvironment(Long environment_product_id, String data_center_name,
            String product_version, Integer max_number_of_workers) throws Exception {

        String msg = validateMaxWorkers(max_number_of_workers);
        if (!"".equalsIgnoreCase(msg)) {
            throw new RuntimeException(msg);
        }

        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String sql = "UPDATE " + schema + ".environment_products SET " +
                "data_center_name=(?)," +
                "product_version=(?)," +
                "last_updated_date=(?)," +
                "last_updated_by=(?)," +
                "max_number_of_workers=(?) " +
                "WHERE environment_product_id = " + environment_product_id;
        String username = sessionUser().name();
        db(TDM).execute(sql, data_center_name, product_version, now, username, max_number_of_workers);
    }

    public static void fnUpdateEnvironmentRolesPermissions(Long environment_id, String type, boolean value) {
        try {
            String sql = "UPDATE " + schema + ".environment_roles SET " +
                    "" + type + "=(?)" +
                    "WHERE environment_id = " + environment_id;
            db(TDM).execute(sql, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static void fnInsertActivity(String action, String entity, String description) throws Exception {
        String userId = sessionUser().name();
        String username = userId;
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String sql = "INSERT INTO " + schema + ".activities " +
                "(date, action, entity, user_id, username, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        db(TDM).execute(sql, now, action, entity, userId, username, description);
    }

    private static String validateMaxWorkers(Integer requestedWorkers) {
        int maxLimit;

        try {
            maxLimit = getGlobalMaxWorkersLimit();
        } catch (Exception e) {
            return "Error: Could not retrieve the global max workers limit from configuration.";
        }

        if (requestedWorkers != null && requestedWorkers > maxLimit) {
            return String.format(
                    "Validation failed: The requested number of workers (%d) exceeds the maximum allowed for execution, as defined in the Fabric configuration (%d).",
                    requestedWorkers,
                    maxLimit);
        }

        return "";
    }

}
