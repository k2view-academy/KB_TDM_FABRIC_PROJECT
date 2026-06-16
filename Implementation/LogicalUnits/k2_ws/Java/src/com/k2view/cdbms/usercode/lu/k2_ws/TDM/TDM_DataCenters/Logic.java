/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_DataCenters;

import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.getFabricResponse;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.wrapWebServiceResults;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.MethodType;
import com.k2view.fabric.api.endpoint.Endpoint.Produce;
import com.k2view.fabric.api.endpoint.Endpoint.resultMetaData;
import com.k2view.fabric.api.endpoint.Endpoint.webService;

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

		List<Map<String, Object>> clusterNodes = (List<Map<String, Object>>) getFabricResponse("clusterstatus;");

		if (clusterNodes == null) {
			return wrapWebServiceResults("SUCCESS", null, null);
		}

		// Strip the internal IID finder job from logical_ids — it is not a user-facing
		// affinity option
		for (Map<String, Object> node : clusterNodes) {
			Object logicalIdsObj = node.get("logical_ids");

			if (logicalIdsObj instanceof String) {
				String logicalIds = (String) logicalIdsObj;

				String cleanedIds = Arrays.stream(logicalIds.split(","))
						.map(String::trim)
						.filter(id -> !id.equalsIgnoreCase(IID_FINDER_JOB))
						.collect(Collectors.joining(","));

				node.put("logical_ids", cleanedIds);
			}
		}

		// clusterstatus; returns one row per Fabric node. Collapse to one entry per DC
		// so callers receive a clean list regardless of how many nodes share a DC.
		List<Map<String, Object>> dataCenters = clusterNodes.stream()
				.collect(Collectors.groupingBy(node -> (String) node.get("dc")))
				.entrySet().stream()
				.map(dcGroup -> {
					List<Map<String, Object>> nodesInDc = dcGroup.getValue();
					Map<String, Object> dcEntry = new LinkedHashMap<>(nodesInDc.get(0));

					// A DC is considered ALIVE if at least one of its nodes is reachable
					boolean isDcAlive = nodesInDc.stream()
							.anyMatch(n -> "ALIVE".equalsIgnoreCase((String) n.get("status")));
					dcEntry.put("status", isDcAlive ? "ALIVE" : nodesInDc.get(0).get("status"));

					// Union the logical IDs from all nodes in the DC; individual node IDs may
					// differ
					String mergedLogicalIds = nodesInDc.stream()
							.map(n -> (String) n.getOrDefault("logical_ids", ""))
							.flatMap(ids -> Arrays.stream(ids.split(",")))
							.map(String::trim)
							.filter(id -> !id.isEmpty())
							.distinct()
							.collect(Collectors.joining(","));
					dcEntry.put("logical_ids", mergedLogicalIds);

					return dcEntry;
				})
				.collect(Collectors.toList());

		return wrapWebServiceResults("SUCCESS", null, dataCenters);
	}

}
