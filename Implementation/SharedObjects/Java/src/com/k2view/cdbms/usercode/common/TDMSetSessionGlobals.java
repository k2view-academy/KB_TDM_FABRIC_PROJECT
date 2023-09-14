package com.k2view.cdbms.usercode.common;

import java.sql.SQLException;
import java.util.Map;

import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.broadway.model.Model;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Log;
import com.k2view.broadway.model.Actor;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Util;
import com.k2view.fabric.common.io.IoSession;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.setGlobals;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TDMSetSessionGlobals implements Actor {
    public static final Log log = Log.a(UserCode.class);

    @Override
    public void action(Data input, Data output, Context ctx) {
		try {
			IoSession fabricSession = ctx.ioProvider().createSession("fabric");
			String globals = "" + input.get("sessionGlobals");
			//log.info("TDMSetSessionGlobals - globals: " + globals);
			Map globalsInput = Json.get().fromJson(globals, Map.class);
		
			if (globalsInput != null && !(globalsInput.isEmpty())) {
				//log.info("TDMSetSessionGlobals - globalsInput: " + globalsInput);
				//ctx.globals().putAll(globalsInput);
				globalsInput.forEach((key, value) -> {
			
					//log.info("TDMSetSessionGlobals - setting "+key+"='"+value+ "'");
					String query = "set " + key + "='" + value + "'";
                    try {
                        fabricSession.prepareStatement(query).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
						throw new RuntimeException(e);
                    }
				});
			} 
    	} catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
