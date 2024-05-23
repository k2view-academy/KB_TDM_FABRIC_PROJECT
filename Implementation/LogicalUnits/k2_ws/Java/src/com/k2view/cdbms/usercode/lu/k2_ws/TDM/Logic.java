/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.fabric.api.endpoint.Endpoint.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedGlobals.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDM;

@SuppressWarnings({"DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {
    public static Map<String,String> fnValidateInterfaceDetails(String Interface){
        Map<String, String> details = new HashMap<>();
        try {
            details = getCustomProperties(Interface);
        } catch (Exception e) {
            log.error("Getting connection details failed due to " + e.getMessage());
            throw new RuntimeException(e);
        }
        return details;
        
    }
}