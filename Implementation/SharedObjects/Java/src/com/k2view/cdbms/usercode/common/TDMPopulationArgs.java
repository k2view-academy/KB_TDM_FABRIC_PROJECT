package com.k2view.cdbms.usercode.common;

import java.util.Map;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.broadway.model.Model;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.session.broadway.PopulationArgsActor;
import com.k2view.fabric.common.Json;

@SuppressWarnings({"unchecked"})
public class TDMPopulationArgs extends PopulationArgsActor {
    public static final Log log = Log.a(UserCode.class);

    @Override
    public void action(Data input, Data output, Context ctx) {
        super.action(input, output, ctx);
        try {
            populateTdmArgs(input, output, ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void populateTdmArgs(Data input, Data output, Context ctx) throws Exception {
        try {
            //log.info("running TDM population args!!");
            String params = "" + ctx.ioProvider().createSession("fabric").prepareStatement("set GENERATE_DATA_PARAMS").execute().iterator().next().get("value");
            
            Map<String,Object> paramsData = Json.get().fromJson(params, Map.class);
            if (paramsData != null && !(paramsData.isEmpty())) {
             /* paramsData.forEach((key, value) -> {
                    log.info("key: " + key + ", value: " + value);
                });*/
                
                ctx.globals().putAll(paramsData);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
