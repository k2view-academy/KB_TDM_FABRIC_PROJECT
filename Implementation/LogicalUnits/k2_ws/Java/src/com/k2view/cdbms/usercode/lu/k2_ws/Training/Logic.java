/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.Training;

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
import com.k2view.fabric.api.endpoint.Endpoint.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {


	@desc("Retrieve Customer info from Customer")
	@webService(path = "test/getCustomerDetails", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetCustomer(String i_id) throws Exception {
		String sql = "SELECT CUSTOMER_ID, SSN, FIRST_NAME, LAST_NAME FROM CUSTOMER";
		Db.Rows rows = ludb("Customer", i_id).fetch(sql);
		return rows;
	}


	@desc("Retrieve Customer info from Customer")
	@webService(path = "test/getCustomerDetails", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "2", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static Object wsGetCustomer2(String i_id, String i_vipStatus) throws Exception {
		String sql = "select cust.FIRST_NAME||' '||cust.LAST_NAME CUSTOMER_NAME, cont.CONTRACT_ID,cont.CONTRACT_DESCRIPTION," +
		        "sub.SUBSCRIBER_ID,sub.MSISDN,sub.IMSI,sub.SIM,sub.SUBSCRIBER_TYPE,sub.VIP_STATUS " +
				"from CUSTOMER cust, CONTRACT cont, SUBSCRIBER sub where cont.CONTRACT_ID=sub.SUBSCRIBER_ID and sub.VIP_STATUS=?";
		
		Db.Rows rows = ludb("Customer", i_id).fetch(sql, i_vipStatus);
		
		return rows;
	}


	@desc("Delete from Subscriber table base on Subscriber ID and MSISDN")
	@webService(path = "", verb = {MethodType.POST, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "\"info\":[{\"subscriber_id\":\"\", \"msisdn\":\"\"},{\"subscriber_id2\":\"\",\"msisdn2\":\"\"}]")
	public static Object wsDeleteSub(String i_id, List<Map<String,String>> i_info) throws Exception {
		Map<String,String> result = new HashMap<>();
		String name ="";
		String message="";
		
		// Parse the input map to extract the values for the delete condition 
		
		//log.info(i_info.toString());
		if ( i_info != null && i_info.size() >0 ){
			
			for (int i = 0; i < i_info.size(); i++) {
				
				//Map<String,String> m = i_info.get.(i);
				 if (i_info.get(i) != null){
					 
					String subId =i_info.get(i).get("subscriber_id");
					String msisdn =i_info.get(i).get("msisdn");
					
				// Validate that input is not empty or wasn't exceed to number of object array
					
					    if (msisdn!=null && !msisdn.isEmpty()){
								
				// Delete from SUBSCRIBER table based on SUBSCRIBER_ID and MSISDN	
							fabric().execute("Begin");
							String sql = "DELETE FROM SUBSCRIBER WHERE SUBSCRIBER_ID=? AND MSISDN=? ";
							ludb("Customer", i_id).execute(sql,subId,msisdn);
							fabric().execute("Commit");
						}
					else{
		//Missing values to process 
							i=i_info.size()+1;
						    name="Error";
						    message="MISSING VALUES"; 	
						    //result.put(name, message);
						   //return result;
						}
					
					}
			}
			name="Complete";
			message="DELETED ALL";
		} else {
		
		// Input can not be processed, not data to parse
			
		 name="Not Started";
		 message="No input received";
		//message=Integer.toString(i_info.size());
		}
		
		result.put(name, message);
		return result;
	}


	@desc("Insert inot CASES tables")
	@webService(path = "", verb = {MethodType.POST, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "\"info\":[{\"activity_id\":\"\", \"case_id\":\"\",\"case_date\":\"\",case_type\":\"\",\"status\":\"\"}")
	public static Object wsInsertCases(String i_id, List<Map<String,String>> i_info) throws Exception {
		Map<String,String> result = new HashMap<>();
		String name ="";
		String message="";
		
		// Parse the input map to extract the values for the delete condition 

		//log.info(i_id);
		//log.info(i_info.toString());
		if ( i_info != null && i_info.size() >0 ){
			
			for (int i = 0; i < i_info.size(); i++) {
				
				//Map<String,String> m = i_info.get.(i);
				 if (i_info.get(i) != null){
					 
					String activity_id =i_info.get(i).get("activity_id");
					String case_id =i_info.get(i).get("case_id");
					String case_date =i_info.get(i).get("case_date");
					String case_type =i_info.get(i).get("case_type");
					String status =i_info.get(i).get("status");
					
				// Validate that input is not empty or wasn't exceed to number of object array
					
					    if (activity_id!=null && !activity_id.isEmpty()){
								
				// Insert into CASES table 	
							fabric().execute("Begin");
							String sql = "insert into cases (activity_id,case_id,case_date,case_type,status) values (?,?,?,?,?)";
							ludb("Customer", i_id).execute(sql,activity_id,case_id,case_date,case_type,status);
							fabric().execute("Commit");
						}
					else{
		//Missing values to process 
							i=i_info.size()+1;
						    name="Error";
						    message="MISSING VALUES"; 	
						    //result.put(name, message);
						   //return result;
						}
					
					}
			}
			name="Complete";
			message="INSERTED ALL";
		} else {
		
		// Input can not be processed, not data to parse
			
		 name="Not Started";
		 message="NO INPUT RECIEVED";
		//message=Integer.toString(i_info.size());
		}
		
		result.put(name, message);
		return result;
	}


	@webService(path = "", verb = {MethodType.GET, MethodType.POST, MethodType.PUT, MethodType.DELETE}, version = "1", isRaw = false, produce = {Produce.XML, Produce.JSON})
	public static String wsGraphitExample2(String i_id, String val) throws Exception {
		String CUST_STATUS = "SELECT count(*) FROM SUBSCRIBER where SUBSCRIBER.VIP_STATUS=?";
		String cnt = ludb("Customer", i_id).fetch(CUST_STATUS,val).firstValue().toString();
		log.info(cnt);
		return cnt;
		
		
		
		
		
	}


	@webService(path = "", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = true, produce = {Produce.XML, Produce.JSON})
		public static Object wsInsertCasesCustomPayloadXML() throws Exception {
		Map<String,String> result = new HashMap<>();
				String name ="";
				String message="";
				String sql = "insert into cases (activity_id,case_id,case_date,case_type,status) values (?,?,?,?,?)";
		
				try{
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware(true);
					DocumentBuilder documentBuilder = factory.newDocumentBuilder();
					Document doc = documentBuilder.parse(WebServiceUserCode.request().getInputStream());
					doc.getDocumentElement().normalize();
		
					Element iidEelem = (Element) doc.getDocumentElement();
					String iid = iidEelem.getAttribute("iid");
					//log.info("iid "+ iid);
		
					NodeList nList = doc.getElementsByTagName("case");
					if ( nList != null && nList.getLength() >0 ) {
						for (int i = 0; i < nList.getLength(); i++) {
		
							Node nNode = nList.item(i);
		
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		
								Element elem = (Element) nNode;
		
								String caseId = elem.getAttribute("id");
		
								Node node1 = elem.getElementsByTagName("activity_id").item(0);
								String activityId = node1.getTextContent();
		
								Node node2 = elem.getElementsByTagName("case_date").item(0);
								String caseDate = node2.getTextContent();
		
								Node node3 = elem.getElementsByTagName("case_type").item(0);
								String caseType = node3.getTextContent();
		
								Node node4 = elem.getElementsByTagName("status").item(0);
								String status = node3.getTextContent();
		
								/*
								log.info("case ID: " + caseId);
								log.info("activity_id: " + activityId);
								log.info("case_date: " + caseDate);
								log.info("case_type: " + caseType);
								log.info("status: " + status);
								*/
		
								if (activityId != null && !activityId.isEmpty()) {
		
									// Insert into CASES table
									fabric().execute("Begin");
									ludb("Customer", iid).execute(sql, activityId, caseId, caseDate, caseType, status);
									fabric().execute("Commit");
								}
							}
						}
						name="Complete";
						message="INSERTED ALL";
					} else {
						name="Error";
						message="MISSING VALUES";
					}
				} catch (Exception e) {
					log.error("",e);
				
				}
				result.put(name, message);
				return result;
	}



}
