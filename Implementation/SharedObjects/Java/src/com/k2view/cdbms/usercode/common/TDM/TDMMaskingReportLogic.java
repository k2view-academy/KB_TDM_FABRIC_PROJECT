/////////////////////////////////////////////////////////////////////////
// TDM Shared - Masking Report
//
// Location in the project:
//   Implementation/SharedObjects/Java/src/com/k2view/cdbms/usercode/common/TDM/TDMMaskingReportLogic.java
//
// Collects, per LU, the (interface, schema) combinations and the source tables
// that are masked during population via the CatalogMaskingMapper /
// CatalogMaskingRecord actors, including, for each table, the list of
// population flows that mask it. Results are cached per LU (per Fabric node)
// and rebuilt automatically when the LU is redeployed (LU version check).
//
// Also populates the TDM DB table task_execution_masking_info: the
// TDMOrchestrator flow calls it once per LU instance, the number of records of
// each masked table is read from the instance's _k2_objects_info table (by the
// population flows that mask the table) and summed into the task execution row.
//
// The implementation lives in SharedObjects (and not in the TDM LU) because the
// TDMOrchestrator flow is shared and runs in the context of the business LU.
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.Broadway;
import com.k2view.broadway.model.Model;
import com.k2view.cdbms.lut.LUType;
import com.k2view.cdbms.lut.TablePopulation;
import com.k2view.cdbms.lut.map.BroadwayFlowMapObject;
import com.k2view.cdbms.shared.Db;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.k2view.cdbms.shared.user.UserCode.db;
import static com.k2view.cdbms.shared.user.UserCode.fabric;
import static com.k2view.cdbms.shared.user.UserCode.getLuType;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getTDMDBSchema;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.sanitizeDslToken;

public class TDMMaskingReportLogic {

    public static final Log log = Log.a(TDMMaskingReportLogic.class);

    // =====================================================================
    // Constants
    // =====================================================================

    private static final Set<String> CATALOG_MASKING_TYPES = Set.of("CatalogMaskingMapper", "CatalogMaskingRecord");
    private static final Set<String> INNER_FLOW_TYPES      = Set.of("InnerFlow");
    private static final Set<String> EXCLUDED_LUS          = Set.of("tdm", "k2_ref", "k2_ws", "tdm_tablelevel", "pipelinelu", "vectors", "tdm_library");

    private static final String INPUT_DATA_PLATFORM = "dataPlatform";
    private static final String INPUT_SCHEMA        = "schema";
    private static final String INPUT_DATASET       = "dataset";
    private static final String INPUT_FLOW_NAME     = "flowName";

    // Shared flow that decides, based on the active rows of the TDMFilterOutTargetTables actor
    // (table_regex / active / lu_name), whether a table is a TDM internal table and must be ignored.
    private static final String FILTER_OUT_TABLES_FLOW = "filterOutTDMTables";

    private static final String TDM    = "TDM";
    private static final String FABRIC = "fabric";

    // LU instance table holding, per populated object, the population that created it
    // and the number of records it wrote for the current instance.
    private static final String K2_OBJECTS_INFO    = "_k2_objects_info";
    private static final String OI_TABLE_NAME      = "table_name";
    private static final String OI_POPULATION_NAME = "object_name";
    private static final String OI_NUM_OF_RECORDS  = "number_of_records";
    private static final String OI_POPULATION_TYPE = "6";

    private static final String MASKING_REPORT_FLAG = "MASKING_REPORT_FLAG";
    private static final String TDM_TASK_EXE_ID     = "TDM_TASK_EXE_ID";

    private static String upsertMaskingInfoSql(String tdmdbSchema) {
        return "INSERT INTO " + tdmdbSchema + ".task_execution_masking_info (" +
                "task_execution_id," +
                "interface_name," +
                "schema_name," +
                "table_name," +
                "catalog_hash," +
                "number_of_records," +
                "sync_time" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (task_execution_id, interface_name, schema_name, table_name) DO UPDATE SET " +
                "number_of_records = task_execution_masking_info.number_of_records + ?, " +
                "sync_time = ?";
    }

    private static String catalogHashSql(String tdmdbSchema) {
        return "select catalog_hash from " + tdmdbSchema + ".tdm_exe_catalog_field_info " +
                "where interface_name = ? and schema_name = ? " +
                "ORDER BY update_date DESC NULLS LAST " +
                "LIMIT 1";
    }

    // =====================================================================
    // Result key: one (interface, schema) combination
    // =====================================================================

    public static final class InterfaceSchema implements Comparable<InterfaceSchema> {

        private final String interfaceName;
        private final String schemaName;

        public InterfaceSchema(String interfaceName, String schemaName) {
            this.interfaceName = Objects.requireNonNull(interfaceName, "interfaceName");
            this.schemaName    = Objects.requireNonNull(schemaName, "schemaName");
        }

        public String getInterfaceName() {
            return interfaceName;
        }

        public String getSchemaName() {
            return schemaName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InterfaceSchema)) return false;
            InterfaceSchema other = (InterfaceSchema) o;
            return interfaceName.equalsIgnoreCase(other.interfaceName)
                && schemaName.equalsIgnoreCase(other.schemaName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(interfaceName.toLowerCase(Locale.ROOT), schemaName.toLowerCase(Locale.ROOT));
        }

        @Override
        public int compareTo(InterfaceSchema o) {
            int c = String.CASE_INSENSITIVE_ORDER.compare(interfaceName, o.interfaceName);
            return c != 0 ? c : String.CASE_INSENSITIVE_ORDER.compare(schemaName, o.schemaName);
        }

        @Override
        public String toString() {      // used as the key when the map is serialized to JSON
            return interfaceName + "." + schemaName;
        }
    }

    // =====================================================================
    // Cache: LU name (lower case) -> result + LU version it was built from
    // NOTE: holds only Strings/InterfaceSchema - never LUType/Flow/Actor
    //       objects - so other LUs' class loaders are not pinned in memory.
    // =====================================================================

    private static final class MaskedTablesCacheEntry {
        final long luVersion;
        final Map<InterfaceSchema, Map<String, List<String>>> result;

        MaskedTablesCacheEntry(long luVersion, Map<InterfaceSchema, Map<String, List<String>>> result) {
            this.luVersion = luVersion;
            this.result = result;
        }
    }

    private static final ConcurrentHashMap<String, MaskedTablesCacheEntry> MASKED_TABLES_CACHE = new ConcurrentHashMap<>();

    // Cache: execution ID -> (interface, schema) -> catalog hash, read once per execution instead of per entity
    private static final int CATALOG_HASH_CACHE_EXECUTIONS = 200;
    private static final Map<String, Map<InterfaceSchema, String>> CATALOG_HASH_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<String, Map<InterfaceSchema, String>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Map<InterfaceSchema, String>> eldest) {
                    return size() > CATALOG_HASH_CACHE_EXECUTIONS;
                }
            });

    private static String cacheKey(String luName) {
        return luName.trim().toLowerCase(Locale.ROOT);
    }

    // =====================================================================
    // Public API
    // =====================================================================

    /**
     * Returns the source tables masked during population (CatalogMaskingMapper/CatalogMaskingRecord actors)
     * of the given LU, grouped by (interface, schema), where each table is mapped to the list of population
     * flows that mask it. Populations without catalog masking are ignored.
     */
    public static Map<InterfaceSchema, Map<String, List<String>>> getLuMaskedTables(String luName) {
        if (Util.isEmpty(luName)) {
            throw new IllegalArgumentException("luName is mandatory");
        }
        if (EXCLUDED_LUS.contains(cacheKey(luName))) {
            return Collections.emptyMap();
        }

        LUType luType = LUType.getTypeByName(luName);
        if (luType == null) {
            throw new IllegalArgumentException("LU '" + luName + "' was not found");
        }

        final long version = luType.getVersion();
        final String key = cacheKey(luName);

        // Fast path: cached and built from the currently deployed LU version
        MaskedTablesCacheEntry entry = MASKED_TABLES_CACHE.get(key);
        if (entry != null && entry.luVersion == version) {
            return entry.result;
        }

        // Slow path: build once; concurrent callers for the same LU wait instead of rebuilding
        entry = MASKED_TABLES_CACHE.compute(key, (k, old) ->
                (old != null && old.luVersion == version)
                        ? old
                        : new MaskedTablesCacheEntry(version, buildMaskedTables(luName, luType)));
        return entry.result;
    }

    /** Removes the cached masked-tables info of an LU from this node's cache (empty luName = all LUs). */
    public static Integer clearCache(String luName) {
        if (Util.isEmpty(luName)) {
            int size = MASKED_TABLES_CACHE.size();
            MASKED_TABLES_CACHE.clear();
            log.info("TDM masked-tables cache cleared for all LUs (" + size + " entries)");
            return size;
        }
        boolean removed = MASKED_TABLES_CACHE.remove(cacheKey(luName)) != null;
        log.info("TDM masked-tables cache for LU " + luName + ": " + (removed ? "removed" : "was not cached"));
        return removed ? 1 : 0;
    }

    /**
     * Populates the TDM DB table task_execution_masking_info for the LU instance that is currently being
     * processed by the TDMOrchestrator flow.
     * <p>
     * For every masked source table of the LU, the number of records loaded for the current instance is read
     * from the instance's {@code _k2_objects_info} table - summing the records of all the population flows
     * that mask that table - and added to the number_of_records of the task execution row.
     * <p>
     * Never throws: masking reporting must not fail the entity load.
     */
    public static void populateTaskExecutionMaskingInfo(String luName, String taskExecutionId) {
        String lu = Util.isEmpty(luName) ? currentLuName() : luName;
        try {
            if (Util.isEmpty(lu)) {
                log.warn("TDMMaskingReport - LU name could not be resolved, task_execution_masking_info is not populated");
                return;
            }
            if (!isMaskingReportEnabled()) {
                return;
            }
            if (!isExtractOrSyncFromSource()) {
                return;
            }

            String taskExecId = resolveTaskExecutionId(taskExecutionId);
            if (Util.isEmpty(taskExecId)) {
                log.warn("TDMMaskingReport - no " + TDM_TASK_EXE_ID + ", task_execution_masking_info is not populated for LU " + lu);
                return;
            }

            Map<InterfaceSchema, Map<String, List<String>>> maskedTables = getLuMaskedTables(lu);
            if (maskedTables.isEmpty()) {
                return;
            }

            Map<String, Long> recordsByPopulation = readPopulatedRecords(lu);
            if (recordsByPopulation.isEmpty()) {
                return;
            }

            String tdmdbSchema = getTDMDBSchema();
            String upsertSql = upsertMaskingInfoSql(tdmdbSchema);
            Timestamp syncTime = new Timestamp(System.currentTimeMillis());

            for (Map.Entry<InterfaceSchema, Map<String, List<String>>> interfaceEntry : maskedTables.entrySet()) {
                InterfaceSchema interfaceSchema = interfaceEntry.getKey();
                String catalogHash = catalogHashOfExecution(taskExecId, tdmdbSchema, interfaceSchema);

                for (Map.Entry<String, List<String>> tableEntry : interfaceEntry.getValue().entrySet()) {
                    long records = sumRecords(recordsByPopulation, tableEntry.getValue());
                    if (records <= 0) {
                        continue;
                    }
                    db(TDM).execute(upsertSql, taskExecId, interfaceSchema.getInterfaceName(),
                            interfaceSchema.getSchemaName(), tableEntry.getKey(), catalogHash, records, syncTime, records, syncTime);
                }
            }
        } catch (Exception e) {
            log.error("TDMMaskingReport - failed to populate task_execution_masking_info (lu=" + lu + ")", e);
        }
    }

    // =====================================================================
    // task_execution_masking_info helpers
    // =====================================================================

    /**
     * Reads the number of records written by every population of the current LU instance from
     * {@code <luName>._k2_objects_info}. The result is keyed both by "&lt;table&gt;.&lt;population&gt;"
     * and by "&lt;population&gt;", so a population can be matched whichever form the masking report holds.
     */
    private static Map<String, Long> readPopulatedRecords(String luName) {
        Map<String, Long> records = new HashMap<>();
        String sql = "select " + OI_TABLE_NAME + ", " + OI_POPULATION_NAME + ", " + OI_NUM_OF_RECORDS +
                " from " + sanitizeDslToken(luName) + "." + K2_OBJECTS_INFO + " where type = " + OI_POPULATION_TYPE;
        try (Db.Rows rows = db(FABRIC).fetch(sql)) {
            for (Db.Row row : rows) {
                String population = asString(row.get(OI_POPULATION_NAME));
                if (Util.isEmpty(population)) {
                    continue;
                }
                String table = asString(row.get(OI_TABLE_NAME));
                long count = asLong(row.get(OI_NUM_OF_RECORDS));
                records.merge(matchKey(population), count, Long::sum);
                if (!Util.isEmpty(table)) {
                    records.merge(matchKey(table + "." + population), count, Long::sum);
                }
            }
        } catch (Exception e) {
            log.warn("TDMMaskingReport - failed to read " + luName + "." + K2_OBJECTS_INFO + ": " + e.getMessage());
        }
        return records;
    }

    /** Sums the number of records of all the populations that mask a single source table. */
    private static long sumRecords(Map<String, Long> recordsByPopulation, List<String> populations) {
        long total = 0;
        for (String population : populations) {
            Long count = recordsByPopulation.get(matchKey(population));
            if (count == null) {
                int dot = population.lastIndexOf('.');
                if (dot > 0) {
                    count = recordsByPopulation.get(matchKey(population.substring(dot + 1)));
                }
            }
            if (count != null) {
                total += count;
            }
        }
        return total;
    }

    /** The catalog hash used by the execution for the (interface, schema) - cached; a missing hash is not cached. */
    private static String catalogHashOfExecution(String taskExecId, String tdmdbSchema, InterfaceSchema interfaceSchema) {
        Map<InterfaceSchema, String> hashes = CATALOG_HASH_CACHE.computeIfAbsent(taskExecId, k -> new ConcurrentHashMap<>());
        String hash = hashes.get(interfaceSchema);
        if (hash == null) {
            hash = fetchCatalogHash(tdmdbSchema, interfaceSchema);
            if (hash != null) {
                hashes.put(interfaceSchema, hash);
            }
        }
        return hash;
    }

    private static String fetchCatalogHash(String tdmdbSchema, InterfaceSchema interfaceSchema) {
        try {
            Object hash = db(TDM).fetch(catalogHashSql(tdmdbSchema),
                    interfaceSchema.getInterfaceName(), interfaceSchema.getSchemaName()).firstValue();
            return hash == null ? null : hash.toString();
        } catch (Exception e) {
            log.warn("TDMMaskingReport - failed to read catalog hash of " + interfaceSchema + ": " + e.getMessage());
            return null;
        }
    }

    private static boolean isMaskingReportEnabled() {
        Object flag = fabricGlobal(MASKING_REPORT_FLAG);
        return flag != null && Boolean.parseBoolean(flag.toString());
    }

    private static boolean isExtractOrSyncFromSource() {
        Object taskType = fabricGlobal("TASK_TYPE");
        if (taskType == null || "EXTRACT".equalsIgnoreCase(taskType.toString())) {
            return true;
        }
        Object syncSourceData = fabricGlobal("TDM_SYNC_SOURCE_DATA");
        return syncSourceData != null && Boolean.parseBoolean(syncSourceData.toString());
    }

    private static String resolveTaskExecutionId(String taskExecutionId) {
        if (!Util.isEmpty(taskExecutionId)) {
            return taskExecutionId;
        }
        Object taskExecId = fabricGlobal(TDM_TASK_EXE_ID);
        return taskExecId == null ? null : taskExecId.toString();
    }

    private static Object fabricGlobal(String name) {
        try {
            return fabric().fetch("set " + name).firstValue();
        } catch (Exception e) {
            log.warn("TDMMaskingReport - failed to read global " + name + ": " + e.getMessage());
            return null;
        }
    }

    private static String currentLuName() {
        try {
            return getLuType() == null ? null : getLuType().luName;
        } catch (Exception e) {
            return null;
        }
    }

    private static String matchKey(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private static long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // =====================================================================
    // Builder
    // =====================================================================

    private static Map<InterfaceSchema, Map<String, List<String>>> buildMaskedTables(String luName, LUType luType) {
        log.info("Building masked-tables info for LU " + luName);

        // InterfaceSchema -> table -> population flows   (sorted via InterfaceSchema.compareTo)
        Map<InterfaceSchema, Map<String, Set<String>>> acc = new TreeMap<>();

        // table (upper case) -> is filtered out by the TDMFilterOutTargetTables actor
        Map<String, Boolean> filteredOutTables = new HashMap<>();

        List<TablePopulation> populations = luType.getPopulationCollection();
        if (populations != null) {
            for (TablePopulation pop : populations) {
                 if (isFilteredOutTable(luName, pop.getTableObject().schemaAndTableName, filteredOutTables)) {
                        continue;   // TDM internal table - listed as active in TDMFilterOutTargetTables
                }
                if (!(pop.getFirstMapObject() instanceof BroadwayFlowMapObject)) {
                    continue;   // non-Broadway population - no CatalogMasking actors
                }
                String popId = pop.getParentObjectName() + "." + pop.getPopulationName();

                List<Model.Actor> masking = new ArrayList<>();
                try (Broadway.Flow flow = ((BroadwayFlowMapObject) pop.getFirstMapObject()).getFlow()) {
                    
                    collectCatalogMaskingActors(flow, luType, new HashSet<>(), masking);
                } catch (Exception e) {
                    log.warn("LU " + luName + ": failed to read flow of population " + popId + ": " + e.getMessage());
                    continue;
                }

                for (Model.Actor m : masking) {
                    String iface   = constInput(m, INPUT_DATA_PLATFORM);
                    String schema  = constInput(m, INPUT_SCHEMA);
                    String dataset = constInput(m, INPUT_DATASET);
                    if (Util.isEmpty(iface) || Util.isEmpty(schema) || Util.isEmpty(dataset)) {
                        log.warn("LU " + luName + ": population " + popId + ", actor " + m.name() +
                                 " has non-constant/empty " + INPUT_DATA_PLATFORM + "/" + INPUT_SCHEMA + "/" +
                                 INPUT_DATASET + " - not reported");
                        continue;
                    }
                   
                    acc.computeIfAbsent(new InterfaceSchema(iface, schema),
                                        k -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                       .computeIfAbsent(dataset, k -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER))
                       .add(popId);
                }
            }
        }

        // Read-only copy: callers can't corrupt the cached data
        Map<InterfaceSchema, Map<String, List<String>>> result = new LinkedHashMap<>();
        acc.forEach((k, tables) -> {
            Map<String, List<String>> tablesCopy = new LinkedHashMap<>();
            tables.forEach((table, pops) -> tablesCopy.put(table, List.copyOf(pops)));
            result.put(k, Collections.unmodifiableMap(tablesCopy));
        });
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns true when the table must be ignored because it is listed as an active row
     * (active = true, for this LU or for ALL_LUS, matching table_regex) of the TDMFilterOutTargetTables actor.
     * <p>
     * The matching logic is delegated to the shared {@value #FILTER_OUT_TABLES_FLOW} flow, which returns the
     * table name only when the table is not filtered out. The decision of every table is evaluated once per
     * build. On any failure the table is kept, so masked tables are never lost because of a filtering error.
     */
    private static boolean isFilteredOutTable(String luName, String tableName, Map<String, Boolean> cache) {
        return cache.computeIfAbsent(matchKey(tableName), k -> {
            try (Db.Rows rows = fabric().fetch("broadway " + sanitizeDslToken(luName) + "." + FILTER_OUT_TABLES_FLOW +
                    " tableName=?, luName=?, RESULT_STRUCTURE=ROW", tableName, luName)) {
                boolean filteredOut = rows == null || rows.firstValue() == null;
                if (filteredOut) {
                    log.info("LU " + luName + ": masked table " + tableName +
                             " is filtered out by the TDMFilterOutTargetTables actor");
                }
                return filteredOut;
            } catch (Exception e) {
                log.warn("LU " + luName + ": failed to check whether table " + tableName +
                         " is filtered out by the TDMFilterOutTargetTables actor: " + e.getMessage());
                return false;
            }
        });
    }

    /** Collects CatalogMaskingMapper/CatalogMaskingRecord actors, recursing into inner flows. */
    private static void collectCatalogMaskingActors(Broadway.Flow flow, LUType luType,
                                                    Set<String> visited, List<Model.Actor> out) {
        visited.add(flow.getFlowName());
        flow.model().levels()
                .flatMap(Model.Level::stages)
                .flatMap(Model.Stage::actors)
                .forEach(actor -> {
                    if (isType(actor, INNER_FLOW_TYPES)) {
                        String inner = constInput(actor, INPUT_FLOW_NAME);
                        if (inner != null && !visited.contains(inner)) {
                            try (Broadway.Flow innerFlow = luType.broadwayFlow(inner)) {
                                collectCatalogMaskingActors(innerFlow, luType, visited, out);
                            } catch (Exception e) {
                                log.info("Failed to read inner flow " + inner + ": " + e.getMessage());
                            }
                        }
                    } else if (isType(actor, CATALOG_MASKING_TYPES)) {
                        out.add(actor);
                    }
                });
    }

    private static boolean isType(Model.Actor a, Set<String> types) {
        if (a == null || a.type() == null) {
            return false;
        }
        return (a.type().name() != null && types.contains(a.type().name())) || (a.type().parentType() != null && types.contains(a.type().parentType()));
    }

    private static String constInput(Model.Actor actor, String name) {
        return actor.type().inputs()
                .filter(p -> name.equals(p.name()))
                .map(Model.Actor.Param::constValue)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(null);
    }
}
