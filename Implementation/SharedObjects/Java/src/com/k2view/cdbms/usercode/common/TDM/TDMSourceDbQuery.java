package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.io.IoSession;
import com.k2view.fabric.session.broadway.sourcedbquery.SourceDbQuery;

import static com.k2view.cdbms.shared.user.UserCode.fabric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class TDMSourceDbQuery extends SourceDbQuery {
    public static final Log log = Log.a(UserCode.class);
    private static final Set<String> TDM_LUS = new HashSet<>(Arrays.asList("tdm", "tdm_library", "tdm_tablelevel", "k2_ws", "k2_ref"));

    @Override
    public void action(Data input, Data output, Context ctx) throws Exception {
        //log.info("TDMSourceDbQuery - Input table: " + ctx.externals().get("table").toString() + ", Input Dist: " + input.get("rowsGeneratorDistribution").toString());
        
        if ("-1".equals(input.get("rowsGeneratorDistribution").toString()) &&
            !TDM_LUS.contains(ctx.externals().get("table").toString().toLowerCase())) {
            Object noOfRecsExernal = tdmSourceDbQuery(input, output, ctx);
            //log.info("TDMSourceDbQuery - new Dist: " + noOfRecsExernal);
            if (noOfRecsExernal != null) {
                input.put("rowsGeneratorDistribution", noOfRecsExernal);
            }
        }
        super.action(input, output, ctx);
    }

    private Object tdmSourceDbQuery(Data input, Data output, Context ctx) throws Exception {
        
        IoSession fabricSession = ctx.ioProvider().createSession("fabric");
        List<String> mainTables = new ArrayList<String>(Arrays.asList(("" + fabricSession.
            prepareStatement("set " + ctx.externals().get("schema").toString() + ".ROOT_TABLE_NAME").execute().iterator().next().get("value")).toLowerCase()));
            
        if (mainTables.contains(ctx.externals().get("table").toString().toLowerCase())) {
            return null;
        }
        String globalName =  ctx.externals().get("schema").toString().toLowerCase() + "_" +
            ctx.externals().get("table").toString().toLowerCase() + "_number_of_records";
        Object noOfRecsVal = ctx.globals().get(globalName);
        if (noOfRecsVal == null) {
            String minDist = "" + fabricSession.
                prepareStatement("set " + ctx.externals().get("schema").toString() + ".TABLE_DEFAULT_DISTRIBUTION_MIN").execute().iterator().next().get("value");
            String maxDist = "" + fabricSession.
                prepareStatement("set " + ctx.externals().get("schema").toString() + ".TABLE_DEFAULT_DISTRIBUTION_MAX").execute().iterator().next().get("value");
      
            String distJson = "{distribution=uniform,round=true,type=integer,minimum=" + minDist + ",maximum=" + maxDist + "}";
            noOfRecsVal = Json.get().fromJson(distJson);
        }
        fabricSession.close();
        return noOfRecsVal;


    }
}
