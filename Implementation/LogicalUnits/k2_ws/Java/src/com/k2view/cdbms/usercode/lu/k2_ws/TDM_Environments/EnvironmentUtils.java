package com.k2view.cdbms.usercode.lu.k2_ws.TDM_Environments;

import com.k2view.cdbms.FabricEncryption.FabricEncryption;
import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Log;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.getFabricResponse;
import static com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetFabricRolesByUser;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class EnvironmentUtils {
    static final Log log = Log.a(UserCode.class);
    static final String schema = "public";

    static List<Map<String, Object>> fnGetEnvsByuser(String userId) throws Exception {
        List<Map<String, Object>> rowsList = new ArrayList<>();
		
		String fabricRoles = String.join(",", (List<String>)((Map<String,Object>)wsGetFabricRolesByUser(userId)).get("result"));
        //get the environments where the user is the owner
        String query1 = "select *, " +
                "CASE when env.allow_read = true and env.allow_write = true THEN 'BOTH' when env.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, 'owner' as role_id, 'owner' as assignment_type " +
                "from environments env, environment_owners o " +
                "where env.environment_id = o.environment_id " +
                "and (o.user_id = (?) or o.user_id = ANY(string_to_array(?, ',')))" +
                "and env.environment_status = 'Active'";

        log.info("fnGetEnvsByuser - query 1 for user id " + userId + "is: " + query1);
        Db.Rows rows = db("TDM").fetch(query1, userId, fabricRoles);

        List<String> columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            rowsList.add(rowMap);
        }

        String envIds = "(";
        if (!rowsList.isEmpty()) {
            for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
            envIds = envIds.substring(0, envIds.length() - 1);
        }
        envIds += ")";

        //get the environments where the user is assigned to a role by their username
        String query2 = "select *, " +
                "CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
                "from environments env, environment_roles r, environment_role_users u " +
                "where env.environment_id = r.environment_id " +
                "and lower(r.role_status) = 'active' " +
                "and r.role_id = u.role_id " +
                "and u.user_id = (?) " +
				"and u.user_type = 'ID' " +
                "and env.environment_status = 'Active'";
        // remove the list of environments returned by query 1;
        query2 += "()".equals(envIds) ? "" : "and env.environment_id not in " + envIds;
        rows = db("TDM").fetch(query2, userId);

        log.info("fnGetEnvsByuser - query 2 for user id " + userId + "is: " + query2);

        columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            rowsList.add(rowMap);
        }

        envIds = "(";
        if (!rowsList.isEmpty()) {
            for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
            envIds = envIds.substring(0, envIds.length() - 1);
        }
        envIds += ")";

        //get the environments where the user id is one of the Fabric Roles
        String query3 = "select *, " +
                "CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type, r.role_id, 'user' as assignment_type " +
                "from environments env, environment_roles r, environment_role_users u " +
                "where env.environment_id = r.environment_id " +
                "and lower(r.role_status) = 'active' " +
                "and r.role_id = u.role_id " +
                "and u.user_id = ANY(string_to_array(?, ',')) " +
				"and u.user_type = 'GROUP' " +
                "and env.environment_status = 'Active'";
        // remove the list of environments returned by query 1+2;
        query3 += "()".equals(envIds) ? "" : "and env.environment_id not in " + envIds;
        rows = db("TDM").fetch(query3, fabricRoles);

        log.info("fnGetEnvsByuser - query 3 for Fabric Roles < " + fabricRoles + "> is: " + query3);

        columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            rowsList.add(rowMap);
        }

        envIds = "(";
        if (!rowsList.isEmpty()) {
            for (Map<String, Object> row : rowsList) envIds += row.get("environment_id") + ",";
            envIds = envIds.substring(0, envIds.length() - 1);
        }
        envIds += ")";

        //get the environments where the user is assigned to a role by 'ALL' assignment
        String query4 = "select *, " +
                "CASE when r.allow_read = true and r.allow_write = true THEN 'BOTH' when r.allow_write = true THEN 'TARGET' ELSE 'SOURCE' END environment_type " +
                ", r.role_id, 'all' as assignment_type " +
                "from environments env, environment_roles r, environment_role_users u " +
                "where env.environment_id = r.environment_id " +
                "and lower(r.role_status) = 'active' " +
                "and r.role_id = u.role_id " +
                "and lower(u.username) = 'all' " +
                "and env.environment_status = 'Active'";
        // remove the list of environments returned by queries 1+2+3;
        query4 += "()".equals(envIds) ? "" : "and env.environment_id not in " + envIds;
        rows = db("TDM").fetch(query4);

        log.info(" fnGetEnvsByuser - query 4 (get ALL roles) is: " + query4);

        columnNames = rows.getColumnNames();
        for (Db.Row row : rows) {
            ResultSet resultSet = row.resultSet();
            Map<String, Object> rowMap = new HashMap<>();
            for (String columnName : columnNames) {
                rowMap.put(columnName, resultSet.getObject(columnName));
            }
            rowsList.add(rowMap);
        }
        return rowsList;
    }

    static String fnGetInterval(String interval) {
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

    //checks if the registered user is an owner of the given environment
    static Boolean fnIsOwner(String envId) throws Exception {
        List<Map<String, Object>> envsList = (List<Map<String, Object>>) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Environments.Logic.wsGetListOfEnvsByUser()).get("result");
        boolean found = false;
        for (Map<String, Object> envsGroup : envsList) {
            Map.Entry<String, Object> entry = envsGroup.entrySet().iterator().next();
            List<Map<String, Object>> groupByType = (List<Map<String, Object>>) entry.getValue();
            for (Map<String, Object> env : groupByType) {
                if ("owner".equals(env.get("role_id").toString())) {
                    if (env.get("environment_id").toString().equals(envId)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        return found;
    }

    static Object fnExtractExecutionStatus(Map<String, List<Map<String, Object>>> executionsStatusGroup) {
        Map<String, Integer> executionsStatus = new HashMap<>();
        executionsStatus.put("failed", 0);
        executionsStatus.put("pending", 0);
        executionsStatus.put("paused", 0);
        executionsStatus.put("stopped", 0);
        executionsStatus.put("running", 0);
        executionsStatus.put("completed", 0);

        out:
        for (Map.Entry<String, List<Map<String, Object>>> entry : executionsStatusGroup.entrySet()) {
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
                if (execution.get("execution_status") == null) continue;
                if ("RUNNING".equals(execution.get("execution_status").toString().toUpperCase()) || "EXECUTING".equals(execution.get("execution_status").toString().toUpperCase()) ||
                        "STARTED".equals(execution.get("execution_status").toString().toUpperCase()) || "STARTEXECUTIONREQUESTED".equals(execution.get("execution_status").toString())) {
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

    static void fnUpdateEnvironmentDate(Long envId) {
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        try {
            String sql = "UPDATE \"" + schema + "\".environments SET " +
                    "environment_last_updated_date=(?)," +
                    "environment_last_updated_by=(?) " +
                    "WHERE environment_id = " + envId;
            String username = (String) ((Map) ((List) getFabricResponse("set username")).get(0)).get("value");
            db("TDM").execute(sql, now, username);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    static Long fnAddProcutToEnvironment(long envId, Long product_id, String data_center_name, String product_version) throws Exception {
        String sql = "INSERT INTO \"" + schema + "\".environment_products " +
                "(environment_id, product_id, data_center_name, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING environment_product_id";
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String username = (String) ((Map) ((List) getFabricResponse("set username")).get(0)).get("value");
        Db.Row row = db("TDM").fetch(sql,
                envId,
                product_id,
                data_center_name,
                product_version,
                username,
                now,
                now,
                username,
                "Active").firstRow();
        return Long.parseLong(row.get("environment_product_id").toString());
    }

    static void fnAddProductInterfacesEnvironment(Long envId, Long product_id, Long env_product_id, List<Map<String, Object>> interfaces) throws Exception {
        if (interfaces == null) return;

        for (Map<String, Object> _interface : interfaces) {
            //k2viewAPIs.getDataFromAPI("envEncryptDBConnPwd",[{"key" : "password", "value" : interface.db_password"}]
            String decryptPwd = FabricEncryption.decrypt(_interface.get("db_password").toString());
            //replace regular text password with encrypted password
            _interface.put("db_password", decryptPwd);

            String sql = "INSERT INTO \"" + schema + "\".environment_product_interfaces " +
                    "(environment_id, product_id, db_host, db_port, db_user, db_password, db_schema, db_connection_string,env_product_interface_id,interface_name,interface_type,env_product_interface_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
            db("TDM").execute(sql,
                    envId,
                    product_id,
                    _interface.get("db_host"),
                    _interface.get("db_port"),
                    _interface.get("db_user"),
                    _interface.get("db_password"),
                    _interface.get("db_schema"),
                    _interface.get("db_connection_string"),
                    env_product_id,
                    _interface.get("interface_name"),
                    _interface.get("interface_type"),
                    "Active");
        }
    }

    static void fnUpdateProductToEnvironment(Long environment_product_id, String data_center_name, String product_version) throws Exception {
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String sql = "UPDATE \"" + schema + "\".environment_products SET " +
                "data_center_name=(?)," +
                "product_version=(?)," +
                "last_updated_date=(?)," +
                "last_updated_by=(?) " +
                "WHERE environment_product_id = " + environment_product_id;
        String username = (String) ((Map) ((List) getFabricResponse("set username")).get(0)).get("value");
        db("TDM").execute(sql, data_center_name, product_version, now, username);
    }

    static void fnUpdateProductInterfacesEnvironment(Long envId, long product_id, Long env_product_id, List<Map<String, Object>> interfaces) throws Exception {
        if (interfaces == null) return;
        for (Map<String, Object> _interface : interfaces) {
            if (_interface.get("update").toString() == "false" || _interface.get("passwordDecrypt").toString() != "false") {
                String decryptPwd = FabricEncryption.decrypt(_interface.get("db_password").toString());
                //replace regular text password with encrypted password
                _interface.put("db_password", decryptPwd);
            }
            //check: env_product_interface_status
            if (_interface.get("newInterface").toString() == "true" && _interface.get("env_product_interface_status").toString() == "null") {
                String sql = "INSERT INTO \"" + schema + "\".environment_product_interfaces " +
                        "(environment_id, product_id, db_host, db_port, db_user, db_password, db_schema, db_connection_string,env_product_interface_id,interface_name,interface_type,env_product_interface_status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
                db("TDM").execute(sql, envId, product_id, _interface.get("db_host"),
                        _interface.get("db_port"), _interface.get("db_user"),
                        _interface.get("db_password"), _interface.get("db_schema"),
                        _interface.get("db_connection_string"), env_product_id,
                        _interface.get("interface_name"), _interface.get("interface_type"), "Active");
            } else if (_interface.get("deleted").toString() == "true") {
                String sql = "DELETE FROM \"" + schema + "\".environment_product_interfaces WHERE ( env_product_interface_id = (?) AND interface_name = (?))";
                db("TDM").execute(sql, _interface.get("env_product_interface_id"), _interface.get("interface_name"));

            } else {
                String sql = "UPDATE \"" + schema + "\".environment_product_interfaces SET " +
                        "db_host=(?)," +
                        "db_port=(?)," +
                        "db_user=(?)," +
                        "db_password=(?)," +
                        "db_schema=(?)," +
                        "db_connection_string=(?) " +
                        "WHERE env_product_interface_id = " + _interface.get("env_product_interface_id") + " AND interface_name = " + "\"" + _interface.get("interface_name") + "\"";
                db("TDM").execute(sql,
                        _interface.get("db_host"), _interface.get("db_port"),
                        _interface.get("db_user"), _interface.get("db_password"),
                        _interface.get("db_schema"), _interface.get("db_connection_string"));
            }
        }
    }


    static void fnUpdateEnvironmentRolesPermissions(Long environment_id, String type, boolean value) {
        try {
            String sql = "UPDATE \"" + schema + "\".environment_roles SET " +
                    "" + type + "=(?)" +
                    "WHERE environment_id = " + environment_id;
            db("TDM").execute(sql, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    static void fnInsertActivity(String action, String entity, String description) throws Exception {
        String userId = (String) ((Map) ((List) getFabricResponse("set username")).get(0)).get("value");
        String username = userId;
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String sql = "INSERT INTO \"" + schema + "\".activities " +
                "(date, action, entity, user_id, username, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        db("TDM").execute(sql, now, action, entity, userId, username, description);
    }

}
