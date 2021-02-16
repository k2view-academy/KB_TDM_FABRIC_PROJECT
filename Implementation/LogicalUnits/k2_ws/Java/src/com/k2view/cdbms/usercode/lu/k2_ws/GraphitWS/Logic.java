/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.GraphitWS;

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
import com.k2view.cdbms.usercode.lu.k2_ws.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import com.k2view.fabric.api.endpoint.Endpoint.*;
import com.k2view.graphIt.script.Scripter;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
@legacy
public class Logic extends WebServiceUserCode {


	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static void GraphitWS1(String Customer_Id, String Case_id) throws Exception {
		// invoke the graphite file in a java web service 
		//Object response = graphit("grSql.graphit",Customer_Id);
		Object response = graphit("grSql.graphit", "Customer_Id", Customer_Id, "Case_Id", Case_id);
	}


	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object grSQL2() throws Exception {
		Map<String, Object> temp = new HashMap<>();
		temp.put("input1",1000);
		temp.put("input2",2463);
		
		
		return graphit("grSql2.graphit", temp);
	}


	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.UNKNOWN})
	public static Object graphitExample2(String i_id) throws Exception {
		String val_brz="Bronze";
		String val_gld="Gold";
		String val_plt="Platinum";
		String customerDB = "Customer";
		
		String CUST_STATUS = "SELECT count(*) FROM SUBSCRIBER where SUBSCRIBER.VIP_STATUS=?";
		String cnt_brz = ludb(customerDB, i_id).fetch(CUST_STATUS,val_brz).firstValue().toString();
		String cnt_gld = ludb(customerDB, i_id).fetch(CUST_STATUS,val_gld).firstValue().toString();
		String cnt_plt = ludb(customerDB, i_id).fetch(CUST_STATUS,val_plt).firstValue().toString();
		
		
		if ((Integer.parseInt(cnt_brz)==0)&&((Integer.parseInt(cnt_gld) !=0)||(Integer.parseInt(cnt_plt) !=0))){
			return graphit("grSqlGldPlus.graphit",i_id);}
		else{
			return graphit("grSqlBrz.graphit",i_id);}
			
		
	}


	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object LambdaGraphit1() throws Exception {
		Map<String, Object> scope = new HashMap<>();
		
		scope.put("times3", (Scripter.F) p->(double)p[0] *3);
		// call graphit with the scope
		return graphit("LambdaGraphit1.graphit", scope);
		
	}






	

}
