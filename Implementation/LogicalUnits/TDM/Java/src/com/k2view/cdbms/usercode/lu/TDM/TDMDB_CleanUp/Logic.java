/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDMDB_CleanUp;

import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.type;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.UserJob;

@SuppressWarnings({"DefaultAnnotationParam"})
public class Logic extends UserCode {


	@desc("The function will be  called by user job to clean up the TDMDB tables based on retention period given in TDMDB table tdm_general_parameters")
	@type(UserJob)
	public static void TDMDB_CleanUp() throws Exception {
		String broadwayCommand = "broadway TDM.TDMCleanUp";
		fabric().execute(broadwayCommand);
	}
	public static final String TDM = "TDM";
}
