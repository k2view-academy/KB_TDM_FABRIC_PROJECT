/////////////////////////////////////////////////////////////////////////
// LU Globals
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Customer;

import com.k2view.cdbms.usercode.common.SharedGlobals;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;

public class Globals extends SharedGlobals {

	@category("TDM")
	public static final String ROOT_TABLE_NAME = "CUSTOMER";

	@category("TDM")
	public static String ROOT_COLUMN_NAME = "CUSTOMER_ID";

	@category("DATA MANUFACTURING")
	public static String NUM_OF_CONTRACTS = "3";

	@category("DATA MANUFACTURING")
	public static String NUM_OF_ACTIVITIES = "2";


	@category("DATA MANUFACTURING")
	public static String NUM_OF_OPEN_CASES_PER_ACTIVITY = "1";
	@category("DATA MANUFACTURING")
	public static String NUM_OF_CLOSED_CASES_PER_ACTIVITY = "1";

	


}
