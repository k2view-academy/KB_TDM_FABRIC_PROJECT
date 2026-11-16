package com.k2view.cdbms.usercode.common.TDM.TDMAudit;

import java.util.*;
import java.sql.*;
import java.math.*;

import static com.k2view.cdbms.shared.user.UserCode.audit;
import static com.k2view.cdbms.shared.user.UserCode.fabric;
import static com.k2view.cdbms.shared.user.UserCode.getLuType;
import static com.k2view.cdbms.shared.user.UserCode.log;

import java.io.*;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.sync.*;
import com.k2view.fabric.auditing.FabricAuditing;
import com.k2view.fabric.common.Json;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.lut.FunctionDef;
import com.k2view.cdbms.shared.session.AuditProxy;
import com.k2view.fabric.session.Session;

public class SharedLogic {

    private static final Set<String> MASK = Set.of("password", "token", "secret", "apikey");
    private static final int MAX_LEN = 4000;

    public static void tdmAudit(String event, Map<String, ?> input, String status, String error) {
               
        Map<String, String> args = new LinkedHashMap<>();
        if (input != null) input.forEach((k, v) -> { if (k != null) args.put(k, auditValue(k, v)); });
        args.put("status", status);
        if (error != null) args.put("error", truncate(error));

        try {
            Session session = FunctionDef.functionContext().session();   // capture it on the WS thread
            Thread t = new Thread(() -> AuditProxy.userEvent(event, args, session), "tdm-audit");
            t.start();
            t.join(2000);                                                // wait at most 2 seconds
        } catch (Exception e) {
            log.warn("Audit failed for {}", event, e);
        }
    }
    private static String auditValue(String key, Object v) {
        if (v == null) return "";
        String lk = key.toLowerCase();
        if (MASK.stream().anyMatch(lk::contains)) return "****";
        if (v instanceof String s) return truncate(s);
        if (v instanceof Map || v instanceof Collection || v.getClass().isArray())
            return truncate(Json.get().toJson(v));         // lists and maps become JSON
        return truncate(String.valueOf(v));
    }

    private static String truncate(String s) {
        return s.length() > MAX_LEN ? s.substring(0, MAX_LEN) + "...[truncated]" : s;
    }
}

