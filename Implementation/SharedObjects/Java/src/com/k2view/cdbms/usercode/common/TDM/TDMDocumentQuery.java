package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.io.IoSession;
import com.k2view.fabric.session.broadway.sourcedocument.DocumentQueryActor;

import static com.k2view.cdbms.shared.user.UserCode.fabric;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.sanitizeDslToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class TDMDocumentQuery extends DocumentQueryActor {
    public static final Log log = Log.a(UserCode.class);
    private static final Set<String> TDM_LUS = new HashSet<>(Arrays.asList("tdm", "tdm_library", "tdm_tablelevel", "k2_ws", "k2_ref"));

    @Override
    public void action(Data input, Data output, Context ctx) throws Exception {
        //log.info("TDMDocumentQuery - Input table: " + ctx.externals().get("table").toString() + ", Input Dist: " + input.get("rowsGeneratorDistribution").toString());
        
        if ("-1".equals(input.get("rowsGeneratorDistribution").toString()) &&
            !TDM_LUS.contains(ctx.externals().get("table").toString().toLowerCase())) {
            Object noOfRecsExernal = tdmDocumentQuery(input, output, ctx);
            //log.info("TDMDocumentQuery - new Dist: " + noOfRecsExernal);
            if (noOfRecsExernal != null) {
                input.put("rowsGeneratorDistribution", noOfRecsExernal);
            }
        }
        super.action(input, output, ctx);
    }

    private Object tdmDocumentQuery(Data input, Data output, Context ctx) throws Exception {
        
        // Don't close the session: it is the flow's shared "fabric" session, Broadway closes it at the end of the flow
        IoSession fabricSession = ctx.ioProvider().createSession("fabric");
        String schema = sanitizeDslToken(ctx.externals().get("schema").toString());
        List<String> mainTables = new ArrayList<String>(Arrays.asList(
            TDMSourceDbQuery.getSetValue(fabricSession, "set " + schema + ".ROOT_TABLE_NAME").toLowerCase()));

        if (mainTables.contains(ctx.externals().get("table").toString().toLowerCase())) {
            return null;
        }
        String globalName =  ctx.externals().get("schema").toString().toLowerCase() + "_" +
            ctx.externals().get("table").toString().toLowerCase() + "_number_of_records";
        Object noOfRecsVal = ctx.globals().get(globalName);
        if (noOfRecsVal == null) {
            String minDist = TDMSourceDbQuery.getSetValue(fabricSession, "set " + schema + ".TABLE_DEFAULT_DISTRIBUTION_MIN");
            String maxDist = TDMSourceDbQuery.getSetValue(fabricSession, "set " + schema + ".TABLE_DEFAULT_DISTRIBUTION_MAX");

            String distJson = "{distribution=uniform,round=true,type=integer,minimum=" + minDist + ",maximum=" + maxDist + "}";
            noOfRecsVal = Json.get().fromJson(distJson);
        }
        return noOfRecsVal;


    }
}
