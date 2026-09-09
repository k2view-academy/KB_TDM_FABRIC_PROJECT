package com.k2view.cdbms.usercode.lu.TDM_TableLevel.TablesOrder;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.lu.TDM_TableLevel.TableLevelUtils.Logic.fnGetAllTableDefinitions;
import static com.k2view.cdbms.usercode.lu.TDM_TableLevel.TableLevelUtils.Logic.fnLoadTargetInfoFromRefList;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;
/**
 * Single-file implementation for resolving table load order
 * based on foreign key dependencies across multiple interfaces.
 */
public class Logic extends UserCode {

    public static final String TDM = "TDM";

    public static List<TableMeta> taskTables = new ArrayList<>();
    public static List<TableMeta> deleteTaskTables = new ArrayList<>();
    // Per order, per interface: the tables that haven't run yet, plus the shared batch id of
    // whichever tables of that interface already ran. All three producers of this map
    // (buildTablesOrderForRerun, buildInterfaceTablesList, TableLoadOrderResolver#getTablesByOrder)
    // build the same TablesAndBatchId shape via the shared splitPendingAndBatchId() helper below.
    public static Map<Integer, Map<String, TablesAndBatchId>> tablesOrder = new HashMap<>();
    public static Map<Integer, Map<String, TablesAndBatchId>> tablesOrderForDelete = new HashMap<>();

    public static int maxOrder = -1;
    public static int delMaxOrder = -1;

    /* ============================================================
       TablesAndBatchId
       ============================================================ */
    /**
     * The tables of one interface that still need to run, plus the single batch id shared by
     * whichever tables of that interface already ran (null if none of them have run yet).
     */
    static final class TablesAndBatchId {
        private final List<TableMeta> pendingTables;
        private final String batchId;

        TablesAndBatchId(List<TableMeta> pendingTables, String batchId) {
            this.pendingTables = pendingTables;
            this.batchId = batchId;
        }

        List<TableMeta> getPendingTables() { return pendingTables; }
        String getBatchId() { return batchId; }
    }

    /**
     * Splits one interface's tables into "not yet run" (getBatchId() == null) and the single
     * batch id shared by whichever tables already ran (getBatchId() != null). Business rule:
     * every already-run table of a given interface carries the same batch id, so the first
     * non-null one found is sufficient. This is the one place this rule is implemented — every
     * producer of tablesOrder should build its per-interface groups by calling this.
     */
    private static TablesAndBatchId splitPendingAndBatchId(List<TableMeta> tables) {
        List<TableMeta> pending = new ArrayList<>();
        String batchId = null;
        for (TableMeta t : tables) {
            if (t.getBatchId() == null) {
                pending.add(t);
            } else if (batchId == null) {
                batchId = t.getBatchId();
            }
        }
        return new TablesAndBatchId(pending, batchId);
    }

    /* ============================================================
       TableRef
       ============================================================ */
    static final class TableRef {
        private final String iface;
        private final String schema;
        private final String table;

        public TableRef(String iface, String schema, String table) {
            this.iface = iface;
            this.schema = schema;
            this.table = table;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TableRef)) return false;
            TableRef that = (TableRef) o;
            return Objects.equals(iface, that.iface)
                    && Objects.equals(schema, that.schema)
                    && Objects.equals(table, that.table);
        }

        @Override
        public int hashCode() {
            return Objects.hash(iface, schema, table);
        }

        @Override
        public String toString() {
            return iface + ":" + schema + "." + table;
        }
    }

    /* ============================================================
       TableMeta
       ============================================================ */
    interface TableMeta {
        String getInterface();
        String getSchema();
        String getTableName();
        int getTableOrder();
        String getBatchId();
        Set<TableRef> getForeignKeyDependencies() throws Exception;
        int getOrderByMtables() throws Exception;
    }

    /* ============================================================
       TablesMeta
       ============================================================ */
    static final class TablesMeta implements TableMeta {

        private final String iface;
        private final String schema;
        private final String table;
        private final String luName;
        private final int tableOrder;
        private final String batchId;

        public TablesMeta(String iface, String schema, String table, String luName, int tableOrder, String batchId) {
            this.iface = iface;
            this.schema = schema;
            this.table = table;
            this.luName = luName;
            this.tableOrder = tableOrder;
            this.batchId = batchId;
        }

        public String getInterface() { return iface; }
        public String getSchema() { return schema; }
        public String getTableName() { return table; }
        public String getLuName() { return luName; }
        public int    getTableOrder() { return tableOrder; }
        public String getBatchId() { return batchId; }

        @Override
        public int getOrderByMtables() throws Exception {
            int order = -1;

            Map<String, Object> defs = fnGetAllTableDefinitions(iface, schema, table);

            String tableOrder = defs.get("table_order").toString();

            if (tableOrder != null && !"".equals(tableOrder)) {
                if (orderIsDigit(tableOrder)) {
                    return Integer.parseInt(tableOrder);
                }
            } else {
                // Run flow to get order
                if (!Util.isEmpty(tableOrder)) {
                    Object calculatedOrder = fabric().fetch("broadway TDM_TableLevel." + tableOrder +
                        " interface_name = '" + iface + "', schema_name = '" + schema + "', table_name = '" + table + "'").firstValue();

                    if (calculatedOrder != null && orderIsDigit(calculatedOrder.toString())) {
                        return Integer.parseInt(calculatedOrder.toString());
                    }
                }
            }

            return order;
        }

        private boolean orderIsDigit(String order) {
            for (char c : order.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Set<TableRef> getForeignKeyDependencies() throws Exception {
            Set<TableRef> deps = getForeignKeyDependenciesByCatalog();
            if (deps != null && !deps.isEmpty()) {
                TableRef dep = deps.iterator().next();
                if ("No Catalog".equals(dep.iface)) {
                    deps.clear();
                } else {
                    return deps;
                }
            }

            deps = getForeignKeyDependenciesByJDBC();
            return deps != null ? deps : Collections.emptySet();
        }

        private Set<TableRef> getForeignKeyDependenciesByCatalog() throws Exception {
            Set<TableRef> deps = new HashSet<>();

            Map<String, Object> in = new HashMap<>();
            in.put("childDataPlatform", iface);
            in.put("childSchema", schema);
            in.put("fkTableName", table);
            in.put("origin", "Crawler");

            List<Map<String, Object>> rows =
                    MtableLookup("catalog_relations_info", in, MTable.Feature.caseInsensitive);

            if (rows == null || rows.isEmpty()) {

                //Check if catalog exists
                Map<String, Object> inp = new HashMap<>();
                inp.put("dataPlatform", iface);
                inp.put("schema", schema);
                inp.put("dataset", table);

                List<Map<String, Object>> rows1 =
                    MtableLookup("catalog_field_info", inp, MTable.Feature.caseInsensitive);

                if (rows1 == null || rows1.isEmpty()) {
                    deps.add(new TableRef("No Catalog", "", ""));
                }
                return deps; // empty set
            }

            for (Map<String, Object> r : rows) {
                TableRef parent = new TableRef(
                        r.get("parentDataPlatform").toString(),
                        r.get("parentSchema").toString(),
                        r.get("pkTableName").toString());

                if (!parent.table.equalsIgnoreCase(table)) {
                    deps.add(parent);
                }
            }
            return deps;
        }

        private Set<TableRef> getForeignKeyDependenciesByJDBC() throws Exception {
            String interfaceName = iface;
            String schemaName = schema;
            String tableName = table;
            fabric().execute("set environment = ?", getGlobal("TDM_TAR_ENV_NAME"));
            //Check if the table has different target DB information from RefList Mtable, and use them to get FKs of table
            Map<String, Object> targetInfo = fnLoadTargetInfoFromRefList(iface, schema, table, luName);
            if (targetInfo != null && !targetInfo.isEmpty()) {
                interfaceName = targetInfo.get("target_interface_name").toString();
                schemaName = targetInfo.get("target_schema_name").toString();
                tableName = targetInfo.get("target_ref_table_name").toString();
            }

            Set<TableRef> deps = new HashSet<>();
            try {
                DatabaseMetaData meta = getConnection(interfaceName).getMetaData();
                try (ResultSet rs = meta.getImportedKeys(null, schemaName, tableName)) {
                    while (rs.next()) {
                        String pkSchema = Util.isEmpty(rs.getString("PKTABLE_SCHEM"))
                                ? rs.getString("PKTABLE_CAT")
                                : rs.getString("PKTABLE_SCHEM");

                        String pkTable = rs.getString("PKTABLE_NAME");

                        if (!pkTable.equalsIgnoreCase(tableName)) {
                            deps.add(new TableRef(iface, pkSchema, pkTable));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("DB error on " + tableName, e);
            }
            return deps;
        }

        @Override
        public String toString() {
            return iface + ":" + schema + "." + table;
        }
    }

    /* ============================================================
       Resolver
       ============================================================ */
    public static class TableLoadOrderResolver {

        private final Map<Integer, Map<String, List<TableMeta>>> cache = new HashMap<>();
        private int maxOrder = -1;

        public TableLoadOrderResolver(List<TableMeta> tables) throws Exception {
            Map<TableMeta, Integer> levels = computeLevels(tables);

            for (Map.Entry<TableMeta, Integer> e : levels.entrySet()) {
                int order = e.getValue();
                TableMeta t = e.getKey();

                maxOrder = Math.max(maxOrder, order);

                cache.computeIfAbsent(order, k -> new LinkedHashMap<>())
                     .computeIfAbsent(t.getInterface(), k -> new ArrayList<>())
                     .add(t);
            }
        }

        /**
         * PUBLIC API:
         * Returns, for ONE order, per interface the tables that haven't run yet plus the
         * shared batch id of the ones that already ran — preserving caller-provided interface
         * order.
         */
        public Map<String, TablesAndBatchId> getTablesByOrder(
                int order,
                Set<String> interfaceOrder) {

            Map<String, TablesAndBatchId> result = new LinkedHashMap<>();
            Map<String, List<TableMeta>> data =
                    cache.getOrDefault(order, Collections.emptyMap());

            for (String iface : interfaceOrder) {
                List<TableMeta> tables = data.get(iface);
                if (tables != null && !tables.isEmpty()) {
                    result.put(iface, splitPendingAndBatchId(tables));
                }
            }
            return result;
        }

        public int getMaxOrder() {
            return maxOrder;
        }

        /* ---------- Internal Topological Sort ---------- */
        private Map<TableMeta, Integer> computeLevels(List<TableMeta> tables) throws Exception {

            Map<TableRef, TableMeta> map = tables.stream()
                    .collect(Collectors.toMap(
                            t -> new TableRef(t.getInterface(), t.getSchema(), t.getTableName()),
                            t -> t));

            Map<TableRef, Set<TableRef>> graph = new HashMap<>();
            Map<TableRef, Integer> inDegree = new HashMap<>();
            Map<TableRef, Integer> level = new HashMap<>();
            Set<TableRef> tablesWithPreOrder = new HashSet<>();

            for (TableRef r : map.keySet()) {
                graph.put(r, new HashSet<>());
                inDegree.put(r, 0);
                level.put(r, 0);
            }

            for (TableMeta t : tables) {
                TableRef cur = new TableRef(t.getInterface(), t.getSchema(), t.getTableName());
                // Check if the order of the table is set in the Mtable or we need to used a flow to calcuale it.
                int order = t.getOrderByMtables();
                if (order > -1) {
                    inDegree.put(cur, order);
                    tablesWithPreOrder.add(cur);
                    level.put(cur, order);
                } else {
                    for (TableRef dep : t.getForeignKeyDependencies()) {
                        if (!map.containsKey(dep)) continue;
                        if (graph.get(dep).add(cur)) {
                            inDegree.put(cur, inDegree.get(cur) + 1);
                        }
                    }
                }
            }

            Queue<TableRef> q = new ArrayDeque<>();
            inDegree.forEach((r, d) -> { if (d == 0) q.add(r); });
            tablesWithPreOrder.forEach((r) -> { q.add(r); });

            int visited = 0;
            while (!q.isEmpty()) {
                TableRef r = q.poll();
                visited++;
                for (TableRef d : graph.get(r)) {
                    level.put(d, Math.max(level.get(d), level.get(r) + 1));
                    inDegree.put(d, inDegree.get(d) - 1);
                    if (inDegree.get(d) == 0) q.add(d);
                }
            }

            if (visited != tables.size()) {
                throw new IllegalStateException("Cycle detected");
            }

            Map<TableMeta, Integer> res = new LinkedHashMap<>();
            level.forEach((r, l) -> res.put(map.get(r), l));
            return res;
        }
    }

    /* ============================================================
       Task Loader
       ============================================================ */
    @SuppressWarnings("unchecked")
    public static int prepareTablesOrder(String taskExecutionId, String taskAction) throws Exception {

        if (!Util.isEmpty(taskTables) || taskTables.size() == 0) {
            Set<String> interfaces = new HashSet<>();
            String sql =
                    "SELECT rt.interface_name, rt.schema_name, es.ref_table_name, rt.lu_name, es.table_order, es.batch_id " +
                    "FROM " + TDMDB_SCHEMA + ".TASK_REF_EXE_STATS es, " +
                    TDMDB_SCHEMA + ".TASK_REF_TABLES rt, " +
                    TDMDB_SCHEMA + ".tasks t " +
                    "WHERE rt.task_id = t.task_id " +
                    "AND rt.task_id = es.task_id " +
                    "AND rt.task_ref_table_id = es.task_ref_table_id " +
                    "AND es.task_execution_id = ? and execution_action <> 'Delete' " +
                    "AND es.execution_status != 'completed' " +
                    "ORDER BY rt.interface_name";

            Db.Rows rows = db(TDM).fetch(sql, taskExecutionId);

            Boolean rerunInd = null;
            for (Db.Row r : rows) {

                // Each row carries its own batch_id — use it directly. (Previously this held a
                // single "first batch_id seen" value shared across every row/interface, so every
                // table ended up with the very first interface's batch id instead of its own.)
                String rowBatchId = r.get("batch_id") != null ? r.get("batch_id").toString() : null;

                if (rerunInd == null) {
                    rerunInd = (Integer.parseInt(r.get("table_order").toString()) == -1 ? false : true);
                }
                taskTables.add(new TablesMeta(
                        r.get("interface_name").toString(),
                        r.get("schema_name").toString(),
                        r.get("ref_table_name").toString(),
                        r.get("lu_name").toString(),
                        Integer.parseInt(r.get("table_order").toString()),
                        rowBatchId));

                interfaces.add(r.get("interface_name").toString());
            }

            if (rerunInd) {
                Map<String, Object> result = buildTablesOrderForRerun(taskTables);
                tablesOrder = (Map<Integer, Map<String, TablesAndBatchId>>) result.get("tablesOrder");
                maxOrder = (int)result.get("maxOrder");
                prepareTablesOrderForDelete(taskExecutionId);
            } else {

                if ("extract".equalsIgnoreCase(taskAction) || taskTables.size() == 1) {
                    tablesOrder.put(0, buildInterfaceTablesList(taskTables));
                    tablesOrderForDelete = tablesOrder;
                    maxOrder = 0;
                    delMaxOrder = 0;
                } else {
                    TableLoadOrderResolver resolver =
                            new TableLoadOrderResolver(taskTables);

                    maxOrder = resolver.getMaxOrder();
                    delMaxOrder = maxOrder;
                    for (int i = 0; i <= maxOrder; i++) {
                        tablesOrder.put(i, resolver.getTablesByOrder(i, interfaces));
                        tablesOrderForDelete.put(maxOrder - i, resolver.getTablesByOrder(i, interfaces));
                    }
                }

                persistTableOrder(taskExecutionId);
            }
        }

        return maxOrder;
    }

    public static int getMaxOrder(String executionAction) {
        if ("delete".equalsIgnoreCase(executionAction)) {
            return delMaxOrder;
        }

        return maxOrder;
        
    }

    @SuppressWarnings("unchecked")
    private static void prepareTablesOrderForDelete(String taskExecutionId) throws Exception {
         Set<String> interfaces = new HashSet<>();
            String sql =
                    "SELECT rt.interface_name, rt.schema_name, es.ref_table_name, rt.lu_name, es.table_order, es.batch_id " +
                    "FROM " + TDMDB_SCHEMA + ".TASK_REF_EXE_STATS es, " +
                    TDMDB_SCHEMA + ".TASK_REF_TABLES rt, " +
                    TDMDB_SCHEMA + ".tasks t " +
                    "WHERE rt.task_id = t.task_id " +
                    "AND rt.task_id = es.task_id " +
                    "AND rt.task_ref_table_id = es.task_ref_table_id " +
                    "AND es.task_execution_id = ? and execution_action = 'Delete' " +
                    "AND es.execution_status != 'completed' " +
                    "ORDER BY rt.interface_name";

            Db.Rows rows = db(TDM).fetch(sql, taskExecutionId);
            
            for (Db.Row r : rows) {

                // Each row carries its own batch_id — use it directly. (Previously this held a
                // single "first batch_id seen" value shared across every row/interface, so every
                // table ended up with the very first interface's batch id instead of its own.)
                String rowBatchId = r.get("batch_id") != null ? r.get("batch_id").toString() : null;

                deleteTaskTables.add(new TablesMeta(
                        r.get("interface_name").toString(),
                        r.get("schema_name").toString(),
                        r.get("ref_table_name").toString(),
                        r.get("lu_name").toString(),
                        Integer.parseInt(r.get("table_order").toString()),
                        rowBatchId));

                interfaces.add(r.get("interface_name").toString());
            }

            Map<String, Object> result = buildTablesOrderForRerun(deleteTaskTables);
            tablesOrderForDelete = (Map<Integer, Map<String, TablesAndBatchId>>) result.get("tablesOrder");
    }
    
    private static Map<String, Object> buildTablesOrderForRerun(List<TableMeta> taskTables) {
        Map<String, Object> result = new HashMap<>();

        int maxOrder = -1;
        Map<Integer, Map<String, List<TableMeta>>> rawByOrder = new HashMap<>();

        for (TableMeta table : taskTables) {
            int order = table.getTableOrder();
            if (order > maxOrder) {
                maxOrder = order;
            }

            rawByOrder.computeIfAbsent(order, k -> new LinkedHashMap<>())
                     .computeIfAbsent(table.getInterface(), k -> new ArrayList<>())
                     .add(table);
        }

        Map<Integer, Map<String, TablesAndBatchId>> tablesOfOrder = new HashMap<>();
        for (Map.Entry<Integer, Map<String, List<TableMeta>>> orderEntry : rawByOrder.entrySet()) {
            Map<String, TablesAndBatchId> perInterface = new LinkedHashMap<>();
            for (Map.Entry<String, List<TableMeta>> ifaceEntry : orderEntry.getValue().entrySet()) {
                perInterface.put(ifaceEntry.getKey(), splitPendingAndBatchId(ifaceEntry.getValue()));
            }
            tablesOfOrder.put(orderEntry.getKey(), perInterface);
        }

        delMaxOrder = maxOrder;
        result.put("maxOrder", maxOrder);
        result.put("tablesOrder", tablesOfOrder);
        return result;
    }

    private static void persistTableOrder(String taskExecutionId) throws Exception {
        boolean hasDeleteRows = db(TDM).fetch(
            "SELECT 1 FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats " +
            "WHERE task_execution_id = ? AND LOWER(execution_action) = 'delete' LIMIT 1",
            taskExecutionId
        ).firstValue() != null;

        for (Map.Entry<Integer, Map<String, TablesAndBatchId>> orderEntry : tablesOrder.entrySet()) {
            int order = orderEntry.getKey();
            int deleteOrder = maxOrder - order;
            // Only tables still pending get a persisted order here — tables that already ran
            // (excluded from getPendingTables()) had their order persisted on the run that
            // actually processed them.
            for (TablesAndBatchId info : orderEntry.getValue().values()) {
                for (TableMeta t : info.getPendingTables()) {
                    db(TDM).execute(
                        "UPDATE " + TDMDB_SCHEMA + ".task_ref_exe_stats " +
                        "SET table_order = ? " +
                        "WHERE task_execution_id = ? " +
                        "AND interface_name = ? " +
                        "AND schema_name = ? " +
                        "AND ref_table_name = ? " +
                        "AND LOWER(execution_action) <> 'delete'",
                        order, taskExecutionId,
                        t.getInterface(), t.getSchema(), t.getTableName()
                    );
                    if (hasDeleteRows) {
                        db(TDM).execute(
                            "UPDATE " + TDMDB_SCHEMA + ".task_ref_exe_stats " +
                            "SET table_order = ? " +
                            "WHERE task_execution_id = ? " +
                            "AND interface_name = ? " +
                            "AND schema_name = ? " +
                            "AND ref_table_name = ? " +
                            "AND LOWER(execution_action) = 'delete'",
                            deleteOrder, taskExecutionId,
                            t.getInterface(), t.getSchema(), t.getTableName()
                        );
                    }
                }
            }
        }
    }

    /**
     * Groups tables by interface (order-independent, doesn't require pre-sorted input), then
     * splits each interface's tables into "not yet run" + shared "already ran" batch id via
     * splitPendingAndBatchId(), matching what the other two producers of tablesOrder build.
     */
    private static Map<String, TablesAndBatchId> buildInterfaceTablesList(List<TableMeta> tables) {
        Map<String, List<TableMeta>> grouped = new LinkedHashMap<>();
        for (TableMeta table : tables) {
            grouped.computeIfAbsent(table.getInterface(), k -> new ArrayList<>())
                   .add(table);
        }

        Map<String, TablesAndBatchId> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<TableMeta>> e : grouped.entrySet()) {
            result.put(e.getKey(), splitPendingAndBatchId(e.getValue()));
        }
        return result;
    }

    /**
     * For a given order, returns per interface: the tables that have NOT run yet, plus the
     * single batch id shared by whichever tables of that interface already ran (null if none
     * of that interface's tables have run yet). All the pending/already-ran splitting already
     * happened when tablesOrder was built (see splitPendingAndBatchId) — this just maps that
     * shape into the public output format.
     */
    public static List<Map<String, Object>> getTablesByOrder(Integer order, String executionAction) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, TablesAndBatchId> tablesList = new HashMap<>();

        if("delete".equalsIgnoreCase(executionAction)) {
            if (Util.isEmpty(tablesOrderForDelete)) {
                return null;
            }

            tablesList = tablesOrderForDelete.get(order) == null ? new HashMap<>() : tablesOrderForDelete.get(order);
        } else {
            if (Util.isEmpty(tablesOrder)) {
                throw new RuntimeException("Function prepareTablesOrder should be executed before calling getTablesByOrder");
            }

            tablesList = tablesOrder.get(order) == null ? new HashMap<>() : tablesOrder.get(order);
        }

        for (Map.Entry<String, TablesAndBatchId> entry : tablesList.entrySet()) {
            String interfaceName = entry.getKey();
            TablesAndBatchId info = entry.getValue();

            Map<String, Object> map = new HashMap<>();
            map.put("interface_name", interfaceName);

            List<Map<String, String>> tables = new ArrayList<>();
            for (TableMeta tableData : info.getPendingTables()) {
                Map<String, String> tableMap = new HashMap<>();
                tableMap.put("interface_name", tableData.getInterface());
                tableMap.put("schema_name", tableData.getSchema());
                tableMap.put("table_name", tableData.getTableName());
                tables.add(tableMap);
            }
            map.put("tables_list", tables);
            map.put("batch_id", info.getBatchId());
            result.add(map);
        }
        return result;
    }

    public static void tablesOrderCleanUp() {
        if (!Util.isEmpty(tablesOrder)) tablesOrder.clear();
        if (!Util.isEmpty(tablesOrderForDelete)) tablesOrderForDelete.clear();
        if (!Util.isEmpty(taskTables)) taskTables.clear();
        if (!Util.isEmpty(deleteTaskTables)) deleteTaskTables.clear();
    }

}
