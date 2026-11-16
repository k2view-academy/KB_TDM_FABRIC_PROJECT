package com.k2view.cdbms.usercode.common.TDM;

import com.k2view.broadway.model.Actor;
import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.fabric.common.Json;
import com.k2view.fabric.common.Log;
import com.k2view.fabric.common.io.IoCommand;
import com.k2view.fabric.common.io.IoSession;

import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TDMSetSessionGlobals implements Actor {
    public static final Log log = Log.a(UserCode.class);

    @Override
    public void action(Data input, Data output, Context ctx) {
		try {
			// Don't close the session: it is the flow's shared "fabric" session and the globals set here are for
			// the next actors. Broadway closes it at the end of the flow. Close only the statements.
			IoSession fabricSession = ctx.ioProvider().createSession("fabric");
			String globals = "" + input.get("sessionGlobals");
			//log.info("TDMSetSessionGlobals - globals: " + globals);
			Map globalsInput = Json.get().fromJson(globals, Map.class);
		
			if (globalsInput != null && !(globalsInput.isEmpty())) {
				//log.info("TDMSetSessionGlobals - globalsInput: " + globalsInput);
				//ctx.globals().putAll(globalsInput);
				globalsInput.forEach((key, value) -> {
					String keyStr = String.valueOf(key);
					if (!keyStr.matches("[A-Za-z0-9_.]+")) {
						throw new RuntimeException("Invalid session global name: " + keyStr);
					}
					//log.info("TDMSetSessionGlobals - setting "+keyStr+"='"+value+ "'");
					String query = "set " + keyStr + "=?";
                    try {
                        try (IoCommand.Statement statement = fabricSession.prepareStatement(query)) {
                            statement.execute(String.valueOf(value));
                        }
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
