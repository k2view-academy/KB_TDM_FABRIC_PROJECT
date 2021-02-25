/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Orders.Root;

import java.util.concurrent.atomic.AtomicBoolean;

import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	@type(RootFunction)
	@out(name = "contract_id", type = Double.class, desc = "")
	@out(name = "order_id", type = Double.class, desc = "")
	@out(name = "order_type", type = String.class, desc = "")
	@out(name = "order_date", type = String.class, desc = "")
	@out(name = "order_status", type = String.class, desc = "")
	public static void fnPop_orders(String input) throws Exception {
		// Check the TDM_INSERT_TO_TARGET, TDM_DELETE_BEFORE_LOAD and TDM_SYNC_SOURCE_DATA.
		// Note: if both globals - TDM_SYNC_SOURCE_DATA is false and TDM_DELETE_BEFORE_LOAD - are false, the Init flow needs to set the sync mode to OFF
		
		String luName = getLuType().luName;
		String tdmInsertToTarget = "" +fabric().fetch("SET " + luName +".TDM_INSERT_TO_TARGET").firstValue();
		String tdmSyncSourceData = "" + fabric().fetch("SET " + luName +".TDM_SYNC_SOURCE_DATA").firstValue();
		
		// Check TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA. If the TDM_INSERT_TO_TARGET and TDM_SYNC_SOURCE_DATA are true, then select the data from the source and yield the results
		if(tdmInsertToTarget.equals("true") ) {
			if (tdmSyncSourceData.equals("false")) {
				// If this is the first sync (the instance is not in Fabric) - throw exception
				if (isFirstSync()) {
					throw new Exception("The instance does not exist in Fabric and sync from source is off");
				}
		
			} else // if the TDM_SYNC_SOURCE_DATA is true - select the data from the source and yield the results
			{

				// Indicates if any of the source root table has the instance id
				AtomicBoolean instanceExists = new AtomicBoolean(false);

				//log.info("TEST11- select orders from DB by input: " + input);
				String sql = "SELECT contract_id, order_id, order_type, order_date, order_status FROM main.orders where order_id = ?";

				db("ORDERS_DB").fetch(sql, input).each(row->{
					instanceExists.set(true);
					yield(row.cells());
				});

				if(!instanceExists.get()) {
					throw new Exception("Instance " + getInstanceID() + " is not found in the Source");
				}
			}
		}
	}

	
	
	
	
}
