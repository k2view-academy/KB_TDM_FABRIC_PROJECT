package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.broadway.model.Actor;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.session.broadway.PopulationArgsActor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"unchecked"})
public class TDMSetBroadwayGlobals implements Actor {
    public static final Log log = Log.a(UserCode.class);

    @Override
    public void action(Data input, Data output, Context ctx) {
        try {

            Object params = input.fields().get("params");

            if (params instanceof Map && params!=null) {
                final LinkedHashMap<String, Object> pm = (LinkedHashMap<String, Object>) params;
                ctx.globals().putAll(pm);
            }
          
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

   
}
