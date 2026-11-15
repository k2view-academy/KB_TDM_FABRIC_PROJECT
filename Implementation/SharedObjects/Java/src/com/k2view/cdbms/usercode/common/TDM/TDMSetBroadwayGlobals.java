package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.broadway.model.Actor;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.io.IoCommand;
import com.k2view.fabric.common.io.IoSession;
import com.k2view.fabric.session.broadway.PopulationArgsActor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getClassificationGeneratorMap;

@SuppressWarnings({"unchecked"})
public class TDMSetBroadwayGlobals implements Actor {
    public static final Log log = Log.a(UserCode.class);

    public static final String SYSTEM_BY_INTERFACE_MAP_KEY = "TDM_SYSTEM_BY_INTERFACE_MAP";
    public static final String CLASSIFICATION_GENERATOR_MAP_KEY = "TDM_CLASSIFICATION_GENERATOR_MAP";

    @Override
    public void action(Data input, Data output, Context ctx) {
        try {
            Object params = input.fields().get("params");
            setGlobals(ctx, params instanceof Map ? (LinkedHashMap<String, Object>) params : null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Populates ctx.globals() with params (if given) plus the two catalog lookup maps TDMMasking
     * needs. Package-private so TDMMasking can call it directly for Broadway contexts - e.g. a
     * per-table "<table>.population" flow - that get their own separate, empty ctx.globals() from
     * whatever orchestrator normally invokes this actor. TDM_TASK_EXE_ID is read from the Fabric
     * session global when not already present, since it isn't always passed via params.
     */
    static void setGlobals(Context ctx, Map<String, Object> params) throws Exception {
        if (params != null) {
            ctx.globals().putAll(params);
        }
        if (ctx.globals().get("TDM_TASK_EXE_ID") == null) {
            Object taskExecId = fetchFabricGlobal(ctx, "TDM_TASK_EXE_ID");
            if (taskExecId != null) {
                ctx.globals().put("TDM_TASK_EXE_ID", taskExecId);
            }
        }
        if (ctx.globals().get("LU_TYPE") == null) {
            Object taskExecId = fetchFabricGlobal(ctx, "LU_TYPE");
            if (taskExecId != null) {
                ctx.globals().put("LU_TYPE", taskExecId);
            }
        }

        if (ctx.globals().get("MASKING_REPORT_FLAG") == null) {
            Object taskExecId = fetchFabricGlobal(ctx, "MASKING_REPORT_FLAG");
            if (taskExecId != null) {
                ctx.globals().put("MASKING_REPORT_FLAG", taskExecId);
            }
        }
        // Uses ctx.ioProvider() (not the db(TDM) UserCode helper) because Broadway actors
        // don't run with the UserCode function context bound, so db(TDM) NPEs here.
        // The schema is likewise resolved via resolveTdmdbSchema rather than
        // SharedLogic.getTDMDBSchema() directly, since that call is also UserCode-context-bound.
        ctx.globals().put(SYSTEM_BY_INTERFACE_MAP_KEY, buildSystemByInterfaceMap(ctx, resolveTdmdbSchema(ctx)));
        ctx.globals().put(CLASSIFICATION_GENERATOR_MAP_KEY, getClassificationGeneratorMap());
    }

    // Fallback schema resolved from TDMDB_SCHEMA - same for the whole cluster, so cached once per JVM.
    private static volatile String fallbackTdmdbSchema;

    /**
     * Returns the TDM DB schema. Prefers the TDM_TASK_EXE_SCHEMA global set once per task execution
     * by TdmExecuteTask; outside a task execution that global is empty, so falls back to the
     * TDMDB_SCHEMA global with ${ClusterID} replaced - the same logic as the getTDMDBSchema flow.
     */
    static String resolveTdmdbSchema(Context ctx) {
        Object taskSchema = fetchFabricGlobal(ctx, "TDM_TASK_EXE_SCHEMA");
        if (taskSchema != null) {
            return taskSchema.toString();
        }
        String result = fallbackTdmdbSchema;
        if (result == null) {
            Object schemaObj = fetchFabricGlobal(ctx, "TDMDB_SCHEMA");
            if (schemaObj == null) {
                return null;
            }
            result = schemaObj.toString();
            if (result.contains("${ClusterID}")) {
                Object clusterId = fetchFabricFirstValue(ctx, "clusterid");
                result = result.replace("${ClusterID}", clusterId != null ? clusterId.toString() : "");
            }
            fallbackTdmdbSchema = result;
        }
        return result;
    }

    // Don't close the "fabric" session in these helpers: it is the flow's shared session, Broadway closes it at the
    // end of the flow. Close only the Statement/Result.
    private static Object fetchFabricFirstValue(Context ctx, String command) {
        try (IoCommand.Statement statement = ctx.ioProvider().createSession("fabric").prepareStatement(command);
             IoCommand.Result result = statement.execute()) {
            return result.iterator().next().first();
        } catch (Exception e) {
            return null;
        }
    }

    /** Reads a Fabric session global (e.g. "set TDM_TASK_EXE_ID") via ctx.ioProvider(), not the
     * thread-bound fabric() UserCode helper, since Broadway actors don't run with that bound.
     * Package-private so TDMMasking can also use it directly (e.g. to check TASK_TYPE). */
    static Object fetchFabricGlobal(Context ctx, String name) {
        try (IoCommand.Statement statement = ctx.ioProvider().createSession("fabric").prepareStatement("set " + name);
             IoCommand.Result result = statement.execute()) {
            Object value = result.iterator().next().get("value");
            return value != null && !value.toString().isEmpty() && !"0".equals(value.toString()) ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, String> buildSystemByInterfaceMap(Context ctx, String tdmdbSchema) throws Exception {
        Map<String, String> systemByInterface = new HashMap<>();
        String productsSql = "SELECT product_name, related_interfaces FROM " + tdmdbSchema
                + ".products WHERE product_status = 'Active'";
        try (IoSession session = ctx.ioProvider().createSession("TDM")) {
            try (IoCommand.Statement statement = session.prepareStatement(productsSql)) {
                try (IoCommand.Result result = statement.execute()) {
                    Iterator<IoCommand.Row> iterator = result.iterator();
                    while (iterator.hasNext()) {
                        IoCommand.Row row = iterator.next();
                        String productName = row.get("product_name") != null ? row.get("product_name").toString() : null;
                        Object rawInterfaces = row.get("related_interfaces");
                        List<String> interfaces = new ArrayList<>();

                        if (rawInterfaces instanceof java.sql.Array) {
                            String[] interfaceArray = (String[]) ((java.sql.Array) rawInterfaces).getArray();
                            interfaces = Arrays.asList(interfaceArray);
                        } else if (rawInterfaces instanceof List) {
                            interfaces = (List<String>) rawInterfaces;
                        }

                        for (String interfaceName : interfaces) {
                            systemByInterface.put(interfaceName.trim().toUpperCase(), productName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("buildSystemByInterfaceMap - failed to fetch products/related_interfaces", e);
            throw e;
        }
        return systemByInterface;
    }

}
