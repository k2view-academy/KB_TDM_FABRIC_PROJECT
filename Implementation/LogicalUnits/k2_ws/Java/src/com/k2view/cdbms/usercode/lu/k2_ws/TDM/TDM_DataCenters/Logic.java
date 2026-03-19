/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_DataCenters;

import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.resultMetaData;
import com.k2view.fabric.api.endpoint.Endpoint.webService;

import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getFabricResponse;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({ "DefaultAnnotationParam", "unchecked" })
public class Logic extends WebServiceUserCode {

	@desc("Gets the list of the Data Centers defined in the Fabric cluster.")
	@webService(path = "dataCenters", verb = {
			MethodType.GET }, version = "1", isRaw = false, isCustomPayload = false, produce = { Produce.XML,
					Produce.JSON })
	@resultMetaData(mediaType = Produce.JSON, example = """
						{
			  "result": [
			    {
			      "notes": "local node",
			      "effective_ip": "10.176.10.6",
			      "node_id": "ziv95demo-k2view",
			      "dc": "LOCAL_DC",
			      "logical_ids": "",
			      "status": "ALIVE"
			    }
			  ],
			  "errorCode": "SUCCESS",
			  "message": null
			}
						""")

	public static Object wsGetDataCenters() throws Exception {

		final String IID_FINDER_JOB = "iidfinder_job";

		List<Map<String, Object>> rawData = (List<Map<String, Object>>) getFabricResponse("clusterstatus;");

		if (rawData != null) {
			for (Map<String, Object> node : rawData) {
				Object logicalIdsObj = node.get("logical_ids");

				if (logicalIdsObj instanceof String) {
					String logicalIds = (String) logicalIdsObj;

					// Process the string to remove the specific job
					String cleanedIds = Arrays.stream(logicalIds.split(","))
							.map(String::trim)
							.filter(id -> !id.equalsIgnoreCase(IID_FINDER_JOB))
							.collect(Collectors.joining(","));

					node.put("logical_ids", cleanedIds);
				}
			}
		}

		return wrapWebServiceResults("SUCCESS", null, rawData);
	}

}
