package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.actors.builtin.DbCommand;
import com.k2view.broadway.model.Actor;
import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.io.IoCommand;
import com.k2view.fabric.common.io.IoSession;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.k2view.fabric.common.Util.safeClose;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;


@SuppressWarnings({"unchecked"})
public class StatsLoader implements Actor {
    private static final String QUERY_INSERT = "INSERT INTO " + TDMDB_SCHEMA + ".task_exe_stats_detailed (" +
            "task_execution_id, " +
            "lu_name, " +
            "entity_id, " +
            "target_entity_id, " +
            "table_name, " +
            "stage_name, " +
            "flow_name, " +
            "actor_name, " +
            "creation_date, " +
            "source_count, " +
            "target_count, " +
            "diff, " +
            "suppressed_error_count, " +
            "results" +
            ") VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private IoSession fabricSession;

    @Override
    public void action(Data input, Data output, Context context) throws Exception {
        if (fabricSession == null) {
            fabricSession = context.ioProvider().createSession("fabric");
        }

        String executionId = getQueryFirstResult("set execution_id;", "value", "NO_EXECUTION_ID");
        String entityIid = getQueryFirstResult("set IID;", "value", "NO_IID");
        String targetEntityID = getQueryFirstResult("set TARGET_ENTITY_ID;", "value", "NO_TARGET_IID");
        String luName = getQueryFirstResult("set LU_TYPE;", "value", "NO_LU_TYPE");
        String mainTableName = getQueryFirstResult("set " + luName + ".ROOT_TABLE_NAME", "value", "NO_ROOT_TABLE_NAME");

        // Parse input stats
        Map<String, Object> statsInput = (Map<String, Object>) input.get("stats");
        Map<String, TableStats> stats = new HashMap<>();
        String inputTable = null;
        boolean root = false;
        if (input.get("table") != null) {
            inputTable = (String) input.get("table");
            root = mainTableName.equalsIgnoreCase(inputTable);
        }
        Set<String> specificKeys = new HashSet<>(); // dbload keys
        Set<String> generalKeys = new HashSet<>(); // keys of all db actors 
        
        statsInput.forEach((key, value) -> {
            if (key.contains("insert")) { //Db Load keys 
                specificKeys.add(key);
            } else {
                generalKeys.add(key);
            }
        });
        
        // priority to dbLoad keys that has a tableName suffex 
        boolean hasSpecificStats = !specificKeys.isEmpty();
        Set<String> keysToProcess = hasSpecificStats ? specificKeys : generalKeys;
        final String tableName = inputTable;
        final boolean isRoot = root;
        keysToProcess.forEach(key -> {
            String keyStr = key.toString();
            long longValue = Math.abs((Long) statsInput.get(key));
            TableStats tableStats;
            String prefix = getMatchingPrefix(keyStr);        
            if (prefix != null) {
                tableStats = stats.computeIfAbsent(getTableName(keyStr, prefix), o -> new TableStats());
            } else {
                tableStats = stats.computeIfAbsent(tableName, o -> new TableStats());
            }
        
            assignStatsValue(tableStats, keyStr, longValue, isRoot,prefix);
        }); 

        IoSession session = context.ioProvider().createSession(input.string("interface"));
        IoCommand.Statement statement = session.prepareStatement(QUERY_INSERT);
        try {
            stats.forEach((table, tableStats) -> {
            try {
                statement.execute(
                        executionId,
                        luName,
                        entityIid,
                        targetEntityID,
                        table,
                        null,
                        null,
                        null,
                        new Timestamp(System.currentTimeMillis()),
                        tableStats.exec + tableStats.errors,
                        tableStats.affected,
                        tableStats.exec + tableStats.errors - tableStats.affected,
                        tableStats.exec + tableStats.errors - tableStats.affected,
                        tableStats.errors == 0 ? "OK" : "FAIL"
                );
            } catch (Exception e) {
                throw new RuntimeException("Can't update stats for the table " + table + ".", e);
            }   
            });
        } catch (Exception e) {
            throw new RuntimeException("Can't update stats for the table", e);
        } finally {
            session.close();
            statement.close();
        }

    }

    private String getTableName(String key, String prefix) {
        // remove stats prefix
        String tableNameWithSqlCommand = key.substring(prefix.length());

        // return only table name without LU name
        if (tableNameWithSqlCommand.contains(".")) {
            tableNameWithSqlCommand = tableNameWithSqlCommand.substring(tableNameWithSqlCommand.indexOf('.') + 1);
        } else {
            tableNameWithSqlCommand = tableNameWithSqlCommand.substring(tableNameWithSqlCommand.indexOf('_') + 1);
        }
        return tableNameWithSqlCommand;
    }

    private String getQueryFirstResult(String query, String columnName, String defaultValue) throws Exception {
        String value = defaultValue;
        try (IoCommand.Statement statement = fabricSession.prepareStatement(query)) {
            try (IoCommand.Result result = statement.execute()) {
                Iterator<IoCommand.Row> iterator = result.iterator();
                if (iterator.hasNext()) {
                    IoCommand.Row row = iterator.next();
                    if (iterator.hasNext()) {
                        throw new IllegalArgumentException("Command '" + statement.toString() + "' from 'fabric' session returns multiple response.");
                    }
                    String newValue = (String) row.get(columnName);
                    // Fabric can response with word "empty" in case there is no IID
                    if (!Util.isEmpty(newValue) && !"empty".equals(newValue)) {
                        value = newValue;
                    }
                } else if (defaultValue == null) {
                    throw new IllegalArgumentException("Command '" + statement.toString() + "' from 'fabric' session returns empty response.");
                }
            }
        }
        return value;
    }
    
    private String getMatchingPrefix(String keyStr) {
        if (keyStr.startsWith(DbCommand.STATS_EXECUTION_ROWS_EFFECTED + "_")) {
            return DbCommand.STATS_EXECUTION_ROWS_EFFECTED + "_";
        } else if (keyStr.startsWith(DbCommand.STATS_EXECUTIONS_COUNT + "_")) {
            return DbCommand.STATS_EXECUTIONS_COUNT + "_";
        } else if (keyStr.startsWith(DbCommand.STATS_EXECUTIONS_ERRORS + "_")) {
            return DbCommand.STATS_EXECUTIONS_ERRORS + "_";
        }
        return null;
    }
    
    private void assignStatsValue(TableStats tableStats, String keyStr, long value, boolean root, String prefix) {
        boolean root_dbcommand = root && (prefix == null || prefix.isEmpty());
        if (keyStr.contains(DbCommand.STATS_EXECUTION_ROWS_EFFECTED)) {
            tableStats.affected = root_dbcommand ? (value-1) : value;
        } else if (keyStr.contains(DbCommand.STATS_EXECUTIONS_COUNT)) {
            tableStats.exec = root_dbcommand ? (value-1) : value;
        } else if (keyStr.contains(DbCommand.STATS_EXECUTIONS_ERRORS) || keyStr.startsWith("errors_")) {
            tableStats.errors = value;
        }
    }
    

    @Override
    public void close() {
        safeClose(fabricSession);
        fabricSession = null;
    }

    private class TableStats {
        long exec;
        long affected;
        long errors;
    }
}
