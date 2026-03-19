package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.io.IoCommand;
import com.k2view.fabric.common.io.IoSession;
import com.k2view.fabric.session.broadway.sourcedbquery.SourceDbQuery;
import java.util.*;

import static com.k2view.fabric.common.Util.safeClose;
import static com.k2view.cdbms.shared.user.UserCode.fabric;
import static com.k2view.cdbms.shared.user.UserCode.getGlobal;
import static com.k2view.cdbms.shared.user.UserCode.ludb;



public class TDMSourceDbQuery extends SourceDbQuery {
    public static final Log log = Log.a(UserCode.class);
    private static final Set<String> TDM_LUS = new HashSet<>(
            Arrays.asList("tdm", "tdm_library", "tdm_tablelevel", "k2_ws", "k2_ref"));

    @Override
    public void action(Data input, Data output, Context ctx) throws Exception {

        // log.info("TDMSourceDbQuery - Input table: " +
        // ctx.externals().get("table").toString() + ", Input Dist: " +
        // input.get("rowsGeneratorDistribution").toString());

        String rowsGen = getQueryFirstResult(ctx, "set ROWS_GENERATOR;", "value", "false");

        if ("true".equalsIgnoreCase(rowsGen)) {
            Object luName = ctx.externals().get("schema");
            //log.info("TDMSourceDbQuery - luName: " + luName);
            if ("-1".equals(input.get("rowsGeneratorDistribution").toString()) &&
                    luName != null && !TDM_LUS.contains(luName.toString().toLowerCase())) {
                Object noOfRecsExernal = tdmSourceDbQuery(input, output, ctx);
                // log.info("TDMSourceDbQuery - new Dist: " + noOfRecsExernal);
                if (noOfRecsExernal != null) {
                    input.put("rowsGeneratorDistribution", noOfRecsExernal);
                }
            }
        }
        super.action(input, output, ctx);
    }

    private Object tdmSourceDbQuery(Data input, Data output, Context ctx) throws Exception {

        IoSession fabricSession = ctx.ioProvider().createSession("fabric");
        List<String> mainTables = new ArrayList<String>(Arrays.asList((""
                + fabricSession.prepareStatement("set " + ctx.externals().get("schema").toString() + ".ROOT_TABLE_NAME")
                        .execute().iterator().next().get("value"))
                .toLowerCase()));

        Object tableName = ctx.externals().get("table");
        if (tableName == null) {
            return null;
        }
        if (mainTables.contains(ctx.externals().get("table").toString().toLowerCase())) {
            return null;
        }
        String globalName = ctx.externals().get("schema").toString().toLowerCase() + "_" +
                ctx.externals().get("table").toString().toLowerCase() + "_number_of_records";
        Object noOfRecsVal = ctx.globals().get(globalName);
        if (noOfRecsVal == null) {
            String minDist = "" + fabricSession
                    .prepareStatement(
                            "set " + ctx.externals().get("schema").toString() + ".TABLE_DEFAULT_DISTRIBUTION_MIN")
                    .execute().iterator().next().get("value");
            String maxDist = "" + fabricSession
                    .prepareStatement(
                            "set " + ctx.externals().get("schema").toString() + ".TABLE_DEFAULT_DISTRIBUTION_MAX")
                    .execute().iterator().next().get("value");

            String distJson = "{distribution=uniform,round=true,type=integer,minimum=" + minDist + ",maximum=" + maxDist
                    + "}";
            noOfRecsVal = Json.get().fromJson(distJson);
        }
        safeClose(fabricSession);
        return noOfRecsVal;

    }

    private String getQueryFirstResult(Context ctx, String query, String columnName, String defaultValue) throws Exception {
        IoSession fabricSession = ctx.ioProvider().createSession("fabric");
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
        safeClose(fabricSession);
        return value;
    }
}
