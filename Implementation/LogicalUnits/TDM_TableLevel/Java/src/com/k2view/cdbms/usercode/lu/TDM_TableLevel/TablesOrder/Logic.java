package com.k2view.cdbms.usercode.lu.TDM_TableLevel.TablesOrder;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import com.k2view.cdbms.shared.*;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.mtable.MTable;
import com.k2view.cdbms.shared.user.UserCode;

import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.MtableLookup;
import static com.k2view.cdbms.usercode.lu.TDM_TableLevel.TableLevelUtils.Logic.fnGetAllTableDefinitions;
import static com.k2view.cdbms.usercode.lu.TDM_TableLevel.TableLevelUtils.Logic.fnLoadTargetInfoFromRefList;
/**
 * Single-file implementation for resolving table load order
 * based on foreign key dependencies across multiple interfaces.
 */
public class Logic extends UserCode {

    public static final String TDM = "TDM";

    public static List<TableMeta> taskTables = new ArrayList<>();
    public static Map<Integer, Map<String, List<TableMeta>>> tablesOrder = new HashMap<>();
    public static int maxOrder = -1;

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

        public TablesMeta(String iface, String schema, String table, String luName) {
            this.iface = iface;
            this.schema = schema;
            this.table = table;
            this.luName = luName;
        }

        public String getInterface() { return iface; }
        public String getSchema() { return schema; }
        public String getTableName() { return table; }
        public String getLuName() { return luName; }

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

        private boolean orderIsDigit (String order) {
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
                TableRef dep =  deps.iterator().next();
                if ("No Catalog".equals(dep.iface)) {
                    deps.clear();
                } 
            } else {
                return deps;
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

                if (rows1 == null || rows1.isEmpty()){
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
                try (ResultSet rs = meta.getImportedKeys(null, schemaName, table)) {
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
         * Returns tables for ONE order, grouped by interface,
         * preserving caller-provided interface order.
         */
        public Map<String, List<TableMeta>> getTablesByOrder(
                int order,
                Set<String> interfaceOrder) {

            Map<String, List<TableMeta>> result = new LinkedHashMap<>();
            Map<String, List<TableMeta>> data =
                    cache.getOrDefault(order, Collections.emptyMap());

            for (String iface : interfaceOrder) {
                List<TableMeta> tables = data.get(iface);
                if (tables != null && !tables.isEmpty()) {
                    result.put(iface, new ArrayList<>(tables));
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
            tablesWithPreOrder.forEach((r) -> {q.add(r); });
            
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
    public static int prepareTablesOrder(String taskExecutionId, String taskAction) throws Exception {
        

        if (!Util.isEmpty(taskTables) || taskTables.size() == 0) {
            Set<String> interfaces = new HashSet<>();
            String sql =
                    "SELECT rt.interface_name, rt.schema_name, es.ref_table_name, rt.lu_name " +
                    "FROM " + TDMDB_SCHEMA + ".TASK_REF_EXE_STATS es, " +
                    TDMDB_SCHEMA + ".TASK_REF_TABLES rt, " +
                    TDMDB_SCHEMA + ".tasks t " +
                    "WHERE rt.task_id = t.task_id " +
                    "AND rt.task_id = es.task_id " +
                    "AND rt.task_ref_table_id = es.task_ref_table_id " +
                    "AND es.task_execution_id = ? and execution_action <> 'Delete'";

            Db.Rows rows = db(TDM).fetch(sql, taskExecutionId);

            for (Db.Row r : rows) {
                
                taskTables.add(new TablesMeta(
                        r.get("interface_name").toString(),
                        r.get("schema_name").toString(),
                        r.get("ref_table_name").toString(),
                        r.get("lu_name").toString()));

                interfaces.add(r.get("interface_name").toString());
            }

            if ("extract".equalsIgnoreCase(taskAction) || taskTables.size() == 1) {
                tablesOrder.put(0, buildInterfaceTablesList(taskTables));
                maxOrder = 0;
            } else {
                TableLoadOrderResolver resolver =
                        new TableLoadOrderResolver(taskTables);

                maxOrder = resolver.getMaxOrder();
                for (int i = 0; i <= maxOrder; i++) {
                    tablesOrder.put(i, resolver.getTablesByOrder(i, interfaces));
                }
            }

            persistTableOrder(taskExecutionId);
        }

        return maxOrder;
    }

    private static void persistTableOrder(String taskExecutionId) throws Exception {
        boolean hasDeleteRows = db(TDM).fetch(
            "SELECT 1 FROM " + TDMDB_SCHEMA + ".task_ref_exe_stats " +
            "WHERE task_execution_id = ? AND LOWER(COALESCE(execution_action, '')) = 'delete' LIMIT 1",
            taskExecutionId
        ).firstValue() != null;

        for (Map.Entry<Integer, Map<String, List<TableMeta>>> orderEntry : tablesOrder.entrySet()) {
            int order = orderEntry.getKey();
            int deleteOrder = maxOrder - order;
            for (List<TableMeta> tableMetas : orderEntry.getValue().values()) {
                for (TableMeta t : tableMetas) {
                    db(TDM).execute(
                        "UPDATE " + TDMDB_SCHEMA + ".task_ref_exe_stats " +
                        "SET table_order = ? " +
                        "WHERE task_execution_id = ? " +
                        "AND interface_name = ? " +
                        "AND schema_name = ? " +
                        "AND ref_table_name = ? " +
                        "AND LOWER(COALESCE(execution_action, '')) <> 'delete'",
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
                            "AND LOWER(COALESCE(execution_action, '')) = 'delete'",
                            deleteOrder, taskExecutionId,
                            t.getInterface(), t.getSchema(), t.getTableName()
                        );
                    }
                }
            }
        }
    }

    private static Map<String, List<TableMeta>> buildInterfaceTablesList(List<TableMeta> taskTables) {
        Map<String, List<TableMeta>> result = new HashMap<>();

        for (TableMeta table : taskTables) {
            result.computeIfAbsent(table.getInterface(), k -> new ArrayList<>()).add(table);
        }
        return result;
    }
    public static List<Map<String, Object>> getTablesByOrder(Integer order) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (Util.isEmpty(tablesOrder)) {
            throw new RuntimeException("Function prepareTablesOrder should be executed before calling getTablesByOrder");
        }

        Map<String, List<TableMeta>> tablesList = tablesOrder.get(order);
        for (String interfaceName : tablesList.keySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("interface_name", interfaceName);
            List<Map<String, String>> tables = new ArrayList<>();
            for (TableMeta tableData : tablesList.get(interfaceName)) {
                Map<String, String> tableMap = new HashMap<>();

                tableMap.put("interface_name", tableData.getInterface());
                tableMap.put("schema_name", tableData.getSchema());
                tableMap.put("table_name", tableData.getTableName());

                tables.add(tableMap);
            }
            map.put("tables_list", tables);
            result.add(map);
        }
        return result;
    }

    public static void tablesOrderCleanUp() {
        if (!Util.isEmpty(tablesOrder)) tablesOrder.clear();
        if (!Util.isEmpty(taskTables)) taskTables.clear();
    }

}
