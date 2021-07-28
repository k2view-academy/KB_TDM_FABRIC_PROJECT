/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions;

import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.wrapWebServiceResults;

@SuppressWarnings({"unused", "DefaultAnnotationParam", "unchecked"})
public class Logic extends WebServiceUserCode {

	private static final String TDM_INTERFACE_NAME = "TDM";
	private static final String SELECT_PERMISSION_GROUP = "select permission_group from public.permission_groups_mapping where fabric_role = ANY (string_to_array(?, ','))";
	private static final String SELECT_FABRIC_ROLES = "select fabric_role from public.permission_groups_mapping where permission_group=?";
	private static final String SELECT_PERMISSION_GROUP_MAPPINGS = "select * from public.permission_groups_mapping";
	private static final String INSERT_PERMISSION_GROUP_MAPPINGS = "insert into public.permission_groups_mapping (description, fabric_role, permission_group, created_by, updated_by, creation_date, update_date) values (?, ?, ?, ?, ?, NOW(), NOW())";
	private static final String UPDATE_PERMISSION_GROUP_MAPPINGS = "update public.permission_groups_mapping set description=?, fabric_role=?, permission_group=?, updated_by=?, update_date=NOW() where fabric_role=?";
	private static final String DELETE_PERMISSION_GROUP_MAPPINGS = "delete from public.permission_groups_mapping where fabric_role=? RETURNING permission_group";
	final static String admin_pg_access_denied_msg = "Access Denied. Please login with administrator privileges and try again";
	private static final Map<String, Integer> PERMISSION_GROUPS = new HashMap() {{
		put("admin", 3);
		put("owner", 2);
		put("tester", 1);
	}};

	@desc("Gets the user permission group according to user Fabric role and its mapping to TDM permission group")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": \"tester\",\r\n" +
			"  \"errorCode\": \"\",\r\n" +
			"  \"message\": \"\"\r\n" +
			"}")
	public static Object wsGetUserPermissionGroup() throws Exception {
		try {
			String fabricRoles = String.join(",", sessionUser().roles());

			Integer[] weight = {0};
			db(TDM_INTERFACE_NAME).fetch(SELECT_PERMISSION_GROUP, fabricRoles).forEach(row -> {
				Integer nextWeight = PERMISSION_GROUPS.get(row.get("permission_group"));
				if (nextWeight != null && nextWeight > weight[0]) {
					weight[0] = nextWeight;
				}
			});

			if (weight[0] == 0) {
				return wrapWebServiceResults("FAIL", "Can't find permission group for the user " + sessionUser().name() + ".", null);
			} else {
				String permissionGroup = null;
				for (Map.Entry<String, Integer> e : PERMISSION_GROUPS.entrySet()) {
					if (e.getValue().equals(weight[0])) {
						permissionGroup = e.getKey();
						break;
					}
				}

				return wrapWebServiceResults("SUCCESS", null, permissionGroup);
			}
		} catch (Throwable t) {
			return wrapWebServiceResults("FAIL", t.getMessage(), null);
		}
	}


	@desc("Provides all mappings between TDM permission groups and Fabric roles")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    {\r\n" +
			"      \"updated_by\": \"admin\",\r\n" +
			"      \"description\": \"mapping1\",\r\n" +
			"      \"fabric_role\": \"tdm_testers\",\r\n" +
			"      \"permission_group\": \"tester\",\r\n" +
			"      \"creation_date\": \"2021-04-20 11:24:48.134\",\r\n" +
			"      \"created_by\": \"admin\",\r\n" +
			"      \"update_date\": \"2021-04-20 11:24:48.134\"\r\n" +
			"    },\r\n" +
			"    {\r\n" +
			"      \"updated_by\": \"admin\",\r\n" +
			"      \"description\": \"mapping1\",\r\n" +
			"      \"fabric_role\": \"tdm_qu\",\r\n" +
			"      \"permission_group\": \"owner\",\r\n" +
			"      \"creation_date\": \"2021-04-21 11:22:03.138\",\r\n" +
			"      \"created_by\": \"admin\",\r\n" +
			"      \"update_date\": \"2021-04-21 11:22:03.138\"\r\n" +
			"    }\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetPermissionGroupMappings() throws Exception {
		List result = new ArrayList();
		db(TDM_INTERFACE_NAME).fetch(SELECT_PERMISSION_GROUP_MAPPINGS).forEach(r -> {
			HashMap<String, Object> pgMapping = new HashMap<>();
			r.keySet().forEach(col -> pgMapping.put(col, r.get(col)));
			result.add(pgMapping);
		});
		return wrapWebServiceResults("SUCCESS", null, result);
	}


	@desc("Returns list of all Fabric roles.")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    \"admin\",\r\n" +
			"    \"tdm_role1\",\r\n" +
			"    \"tdm_role2\"\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetFabricRoles() throws Exception {
		List<String> roles = new ArrayList<>();
		fabric().fetch("list roles;").forEach(r -> roles.add((String) r.get("name")));
		return wrapWebServiceResults("SUCCESS", null, roles);
	}


	@desc("Adds a new mapping between a TDM Permission Group and a Fabric role. The relation of Fabric roles and TDM permission groups is many to one. i.e. each Fabric role can be mapped into one TDM Permission Group, but it is possible to map several Fabric roles into a given TDM Permission Group.")
	@webService(path = "", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": null,\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsAddPermissionGroupMapping(String description, @param(description="Fabric role") String role, @param(description="Can be populated by 'admin', 'owner', or 'tester'") String permission_group) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if (!"admin".equals(permissionGroup)) return wrapWebServiceResults("FAIL",admin_pg_access_denied_msg,null);
		try {
			String userName = sessionUser().name();
			db(TDM_INTERFACE_NAME).execute(INSERT_PERMISSION_GROUP_MAPPINGS, description, role, permission_group, userName, userName);
			return wrapWebServiceResults("SUCCESS", null, null);
		} catch (Exception e) {
			return wrapWebServiceResults("FAIL", "Can't add new permission group mapping: " + e.getMessage(), null);
		}
	}

	@desc("Updates the mapping between a permission group and Fabric role.")
	@webService(path = "", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": null,\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdatePermissionGroupMapping(String description, String old_role, String new_role, @param(description="Can be populated by 'admin', 'owner', or 'tester'") String permission_group) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) com.k2view.cdbms.usercode.lu.k2_ws.TDM_Permissions.Logic.wsGetUserPermissionGroup()).get("result");
		if (!"admin".equals(permissionGroup)) return wrapWebServiceResults("FAIL",admin_pg_access_denied_msg,null);
		try {
			String userName = sessionUser().name(); //assigned to updated by
			String OldMappingPG = (String)db(TDM_INTERFACE_NAME).fetch("select permission_group from public.permission_groups_mapping where fabric_role=(?)",old_role).firstValue();
			db(TDM_INTERFACE_NAME).execute(UPDATE_PERMISSION_GROUP_MAPPINGS, description, new_role, permission_group, userName, old_role);

			if("owner".equals(OldMappingPG)){
				List<String> users= (List<String>)((Map<String,Object>)wsGetUsersByFabricRole(old_role)).get("result");

				if(users!=null&&!users.isEmpty()){
					//check that the user is no longer assigned to owner PG using anther fabric role
					Iterator<String> usersIter= users.iterator();
					while(usersIter.hasNext()){
						if(!"owner".equalsIgnoreCase((String)((Map<String,Object>)wsGetPermissionGroupByUser(usersIter.next())).get("result")))
							usersIter.remove();
					}
					if(!users.isEmpty()) {
						users.stream().map(user -> "'" + user + "'").collect(Collectors.toList());

						String deletOwnersSql = "DELETE FROM environment_owners WHERE user_name in (" + String.join(",", users) + ")";
						db(TDM_INTERFACE_NAME).execute(deletOwnersSql);
					}
				}
			}

			return wrapWebServiceResults("SUCCESS", null, null);
		} catch (Exception e) {
			return wrapWebServiceResults("FAIL", "Can't add new permission group mapping: " + e.getMessage(), null);
		}
	}

	@desc("Deletes mapping between permission group and Fabric role.")
	@webService(path = "", verb = {MethodType.DELETE}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": null,\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsDeletePermissionGroupMapping(@param(required=true) String role) throws Exception {
		String permissionGroup = (String) ((Map<String, Object>) wsGetUserPermissionGroup()).get("result");
		if (!"admin".equals(permissionGroup)) {
			return wrapWebServiceResults("FAIL", admin_pg_access_denied_msg, null);
		}
		try {
			String mappingPG = "" + db(TDM_INTERFACE_NAME).fetch(DELETE_PERMISSION_GROUP_MAPPINGS, role).firstValue();
			if ("owner".equals(mappingPG)) {
				List<String> users = (List<String>) ((Map<String, Object>) wsGetUsersByFabricRole(role)).get("result");
				if (users != null && !users.isEmpty()) {
					String usersJoin = String.join("','", users);
					String deleteOwnersSql = "DELETE FROM environment_owners WHERE user_name in ('" + usersJoin + "')";
					db(TDM_INTERFACE_NAME).execute(deleteOwnersSql);
				}
			} else if ("tester".equals(mappingPG)) {
				List<String> users = (List<String>) ((Map<String, Object>) wsGetUsersByFabricRole(role)).get("result");
				if (users != null && !users.isEmpty()) {
					if (!users.isEmpty()) {
						users.stream().map(user -> "'" + user + "'").collect(Collectors.toList());
						String deletTestersSql = "DELETE FROM environment_role_users WHERE username IN ('" + String.join(",", users) + "')";
						db(TDM_INTERFACE_NAME).execute(deletTestersSql);
					}
				}
			}
			return wrapWebServiceResults("SUCCESS", null, null);
		} catch (Exception e) {
			return wrapWebServiceResults("FAIL", "Can't delete new permission group mapping: " + e.getMessage(), null);
		}
	}


	public static Object wsGetFabricRolesByUser(String user) throws Exception {
		List<String> roles=new ArrayList<>();
		fabric().fetch("list users;").
		forEach(r -> {
			if(user.equals(r.get("user"))) {
				roles.addAll(Arrays.asList(((String) r.get("roles")).split(",")));
			}
		});

		return wrapWebServiceResults("SUCCESS", null, roles);
	}


	public static Object wsGetPermissionGroupByUser(String user) throws Exception {
		String fabricRoles = String.join(",", (List<String>)((Map<String,Object>)wsGetFabricRolesByUser(user)).get("result"));

		Integer[] weight = {0};
		db(TDM_INTERFACE_NAME).fetch(SELECT_PERMISSION_GROUP, fabricRoles).forEach(row -> {
			Integer nextWeight = PERMISSION_GROUPS.get(row.get("permission_group"));
			if (nextWeight != null && nextWeight > weight[0]) {
				weight[0] = nextWeight;
			}
		});

		if (weight[0] == 0) {
			return wrapWebServiceResults("FAIL", "Can't find permission group for the user " + sessionUser().name() + ".", null);
		} else {
			String permissionGroup = null;
			for (Map.Entry<String, Integer> e : PERMISSION_GROUPS.entrySet()) {
				if (e.getValue().equals(weight[0])) {
					permissionGroup = e.getKey();
					break;
				}
			}

			return wrapWebServiceResults("SUCCESS", null, permissionGroup);
		}
	}


	@desc("Gets all users by TDM permission group.")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    \"tester2\",\r\n" +
			"    \"tester1\"\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetUsersByPermissionGroup(@param(description = "Expect to be one of the following values: admin, owner, or tester.") String permissionGroup) throws Exception {
		List<String> roles = new ArrayList<>();
		db(TDM_INTERFACE_NAME).fetch(SELECT_FABRIC_ROLES, permissionGroup).forEach(r -> roles.add((String) r.get("fabric_role")));
		
		List<String> users = new ArrayList<>();
		fabric().fetch("list users;").forEach(r -> {
			for (String role : ((String) r.get("roles")).split(",")) {
				if (roles.contains(role)) {
					users.add((String) r.get("user"));
				}
			}
		});

		return wrapWebServiceResults("SUCCESS", null, users);
	}


	@desc("Gets all Fabric roles of current user;")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    \"admin\"\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetUserFabricRoles() throws Exception {
		return wrapWebServiceResults("SUCCESS", null, sessionUser().roles());
	}


	@desc("Gets users assigned to a given fabric role.")
	@webService(path = "getUsersByFabricRole", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    \"admin\"\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetUsersByFabricRole(String fabricRole) throws Exception {
		List<String> users = new ArrayList<>();
		try {
			fabric().fetch("list users;").forEach(r -> {
				for (String role : ((String) r.get("roles")).split(",")) {
					if (fabricRole.equals(role)) {
						users.add((String) r.get("user"));
						break;
					}
				}
			});
			return wrapWebServiceResults("SUCCESS", null, users);
		} catch (Exception e) {
			return wrapWebServiceResults("FAIL", e.getMessage(), null);
		}
	}


}
