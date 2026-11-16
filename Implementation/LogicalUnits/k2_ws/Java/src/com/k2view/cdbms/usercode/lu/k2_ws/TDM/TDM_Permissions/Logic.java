/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.TDM.TDM_Permissions;

import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.desc;
import com.k2view.fabric.api.endpoint.Endpoint.*;
import java.util.*;
import com.k2view.cdbms.shared.*;
import static com.k2view.cdbms.usercode.common.TDM.TdmSharedUtils.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.getTDMDBSchema;
import static com.k2view.cdbms.usercode.common.TDM.TDMAudit.SharedLogic.tdmAudit;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@SuppressWarnings({"DefaultAnnotationParam", "unchecked", "rawtypes"})
public class Logic extends WebServiceUserCode{
	public static final String TDM = "TDM";
	private static String selectFabricRoles() { return "select fabric_role from " + getTDMDBSchema() + ".permission_groups_mapping where permission_group=?"; }
	private static String selectPermissionGroupMappings() { return "select * from " + getTDMDBSchema() + ".permission_groups_mapping"; }
	private static String selectPermissionGroupByRole() { return "select permission_group from " + getTDMDBSchema() + ".permission_groups_mapping where fabric_role=?"; }
	private static String insertPermissionGroupMappings() { return "insert into " + getTDMDBSchema() + ".permission_groups_mapping (description, fabric_role, permission_group, created_by, updated_by, creation_date, update_date, can_create_tasks) values (?, ?, ?, ?, ?, NOW(), NOW(), ?)"; }
	private static String updatePermissionGroupMappings() { return "update " + getTDMDBSchema() + ".permission_groups_mapping set description=?, fabric_role=?, permission_group=?, updated_by=?, update_date=NOW(), can_create_tasks=? where fabric_role=?"; }
	private static String deletePermissionGroupMappings() { return "delete from " + getTDMDBSchema() + ".permission_groups_mapping where fabric_role=? RETURNING permission_group"; }
	final static String admin_pg_access_denied_msg = "Access Denied. Please login with administrator privileges and try again";
	private static final Map<String, Integer> PERMISSION_GROUPS = new HashMap() {{
		put("admin", 3);
		put("owner", 2);
		put("tester", 1);
	}};

	@desc("Gets the user permission group according to user Fabric role and its mapping to TDM permission group")
	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = true)
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": \"tester\",\r\n" +
			"  \"errorCode\": \"\",\r\n" +
			"  \"message\": \"\"\r\n" +
			"}")
	public static Object wsGetUserPermissionGroup() throws Exception {
		try {
			String userName = "";
			String permissionGroup = fnGetUserPermissionGroup(userName);
			if (permissionGroup == null || permissionGroup.isEmpty()) {
				return wrapWebServiceResults("WARNING", "No permission group mapping found for the user's Fabric role.", permissionGroup);
			}
			return wrapWebServiceResults("SUCCESS", null, permissionGroup);
		} catch (Throwable t) {
			return wrapWebServiceResults("FAILED", t.getMessage(), null);
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
		db(TDM).fetch(selectPermissionGroupMappings()).forEach(r -> {
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
	public static Object wsGetFabricRoles(String permissionGroupList) throws Exception {
		List<String> roles = new ArrayList<>();
		Db.Rows rolesList = fabric().fetch("list roles;");
		
		for (Db.Row role : rolesList) {
			String roleName = "" + role.get("name");
			if (permissionGroupList!=null&&permissionGroupList.length()>0) {
				String[] permGrpListArr = permissionGroupList.split(",");
				List<Object> permGrpParams = new ArrayList<>();
				for (String permGrp : permGrpListArr) {
					permGrpParams.add(permGrp.trim());
				}
				String permGrpPlaceholders = String.join(",", Collections.nCopies(permGrpParams.size(), "?"));
				String sql = "select count(1) from " + getTDMDBSchema() + ".permission_groups_mapping where fabric_role = ? " +
						"and permission_group in (" + permGrpPlaceholders + ")";
				List<Object> args = new ArrayList<>();
				args.add(roleName);
				args.addAll(permGrpParams);
				Long checkPermGrp = (Long) db(TDM).fetch(sql, args.toArray()).firstValue();
				if (checkPermGrp > 0) {
					roles.add(roleName);
				}
			} else {
				roles.add(roleName);
			}
		}
		if (rolesList != null) {
			rolesList.close();
		}
		return wrapWebServiceResults("SUCCESS", null, roles);
	}


	@desc("Adds a new mapping between a TDM Permission Group and a Fabric role. The relation of Fabric roles and TDM permission groups is many to one. i.e. each Fabric role can be mapped into one TDM Permission Group, but it is possible to map several Fabric roles into a given TDM Permission Group.")
	@webService(path = "", verb = {MethodType.POST}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": null,\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsAddPermissionGroupMapping(boolean can_create_tasks, String description, @param(description="Fabric role") String role, @param(description="Can be populated by 'admin', 'owner', or 'tester'") String permission_group) throws Exception {
		
		String status = "SUCCESS";
		String message = null;
		Map<String, Object> in = new LinkedHashMap<>();
		in.put("can_create_tasks", can_create_tasks);
		in.put("description", description);
		in.put("Fabric_role", role);
		in.put("permission_group", permission_group);
		
		try {

			String permissionGroup = fnGetUserPermissionGroup("");
			if (!"admin".equals(permissionGroup)) {
				status = "FAILED";
				message = admin_pg_access_denied_msg;
				return wrapWebServiceResults(status,message,null);
			}

			String userName = sessionUser().name();
			db(TDM).execute(insertPermissionGroupMappings(), description, role, permission_group, userName, userName, can_create_tasks);
			String activityDesc = "Role '" + role + "' was mapped to permission group '" + permissionGroup + "'.";
			fnInsertActivity("create", "Permission Groups", activityDesc);
			return wrapWebServiceResults("SUCCESS", null, null);
		} catch (Exception e) {
			status = "FAILED";
			message = e.getMessage();
			return wrapWebServiceResults(status, "Can't add new permission group mapping: " + message, null);
		} finally {
			tdmAudit("wsAddPermissionGroupMapping", in, status, message);
		}
	}

	@desc("Updates the mapping between a permission group and Fabric role.")
	@webService(path = "", verb = {MethodType.PUT}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": null,\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsUpdatePermissionGroupMapping(String description, String old_role, String new_role, @param(description="Can be populated by 'admin', 'owner', or 'tester'") String permission_group, boolean can_create_tasks) throws Exception {
		

		String status = "SUCCESS";
		String message = null;
		Map<String, Object> in = new LinkedHashMap<>();
		in.put("description", description);
		in.put("old_role", old_role);
		in.put("new_role", new_role);
		in.put("permission_group", permission_group);
		in.put("can_create_tasks", can_create_tasks);
		try {

			String sessionUserPermissionGroup = (String) ((Map<String, Object>) wsGetUserPermissionGroup()).get("result");
			if (!"admin".equals(sessionUserPermissionGroup)) {
				status = "FAILED";
				message = admin_pg_access_denied_msg;
				return wrapWebServiceResults(status, message, null);
			}
			
			String userName = sessionUser().name(); //assigned to updated by
			String OldMappingPG = (String)db(TDM).fetch("select permission_group from " + getTDMDBSchema() + ".permission_groups_mapping where fabric_role=(?)",old_role).firstValue();
			db(TDM).execute(updatePermissionGroupMappings(), description, new_role, permission_group, userName,can_create_tasks, old_role);
			String activityDesc = "Role '" + old_role + "' was updated to '" + new_role +
                      "' and mapped to permission group '" + permission_group + "'.";
			fnInsertActivity("update", "Permission Groups", activityDesc);
			List<String> usersByFabricRole = (List<String>) ((Map<String, Object>) wsGetUsersByFabricRole(old_role)).get("result");
			List<String> roles = new ArrayList<>();
			roles.add(old_role);										  
			if ("owner".equals(OldMappingPG)) {
				cleanupUsersByPermissionGroup(usersByFabricRole, "owner", "DELETE FROM " + getTDMDBSchema() + ".environment_owners WHERE user_name IN");
				cleanupUserGrouspByPermissionGroup(roles, "owner", "DELETE FROM " + getTDMDBSchema() + ".environment_owners WHERE user_name IN");																											
			} else if ("tester".equals(OldMappingPG)) {
				cleanupUsersByPermissionGroup(usersByFabricRole, "tester", "DELETE FROM " + getTDMDBSchema() + ".environment_role_users WHERE username IN");
				cleanupUserGrouspByPermissionGroup(roles, "tester", "DELETE FROM " + getTDMDBSchema() + ".environment_owners WHERE user_name IN");																							 
			}

			return wrapWebServiceResults("SUCCESS", null, null);
		} catch (Exception e) {
			status = "FAILED";
			message = e.getMessage();
			return wrapWebServiceResults(status, "Can't add new permission group mapping: " + message, null);
		} finally{
			tdmAudit("wsUpdatePermissionGroupMapping", in, status, message);
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
        
		String status = "SUCCESS";
		String message = null;
		Map<String, Object> in = new LinkedHashMap<>();
		in.put("role", role);
		
        try {
			String sessionUserPermissionGroup = (String) ((Map<String, Object>) wsGetUserPermissionGroup()).get("result");
			if (!"admin".equals(sessionUserPermissionGroup)) {
				status = "FAILED";
				message = admin_pg_access_denied_msg;
				return wrapWebServiceResults(status, message, null);
			}
			
            String checkIfAdmin = (String) db(TDM).fetch(selectPermissionGroupByRole(), role).firstValue();
            if("admin".equals(checkIfAdmin)){
				status = "FAILED";
				message = "You are not allowed to remove the permissions group mapping for Admin";
            	return wrapWebServiceResults(status, message, null);
            }
            List<String> usersByFabricRole = (List<String>) ((Map<String, Object>) wsGetUsersByFabricRole(role)).get("result");
            String permissionGroup = (String) db(TDM).fetch(deletePermissionGroupMappings(), role).firstValue();
			String activityDesc = "Role '" + role + "' was deleted from permission group '" + permissionGroup + "'.";
			fnInsertActivity("delete", "Permission Groups", activityDesc);
            List<String> roles = new ArrayList<>();
            roles.add(role);	   
            if ("owner".equals(permissionGroup)) {
                cleanupUsersByPermissionGroup(usersByFabricRole, "owner", "DELETE FROM " + getTDMDBSchema() + ".environment_owners WHERE user_name IN");
                cleanupUserGrouspByPermissionGroup(roles, "owner", "DELETE FROM " + getTDMDBSchema() + ".environment_owners WHERE user_name IN");																							
            } else if ("tester".equals(permissionGroup)) {
                cleanupUsersByPermissionGroup(usersByFabricRole, "tester", "DELETE FROM " + getTDMDBSchema() + ".environment_role_users WHERE username IN");
                cleanupUserGrouspByPermissionGroup(roles, "tester", "DELETE FROM " + getTDMDBSchema() + ".environment_owners WHERE user_name IN");																							 
            }
            return wrapWebServiceResults("SUCCESS", null, null);
        } catch (Exception e) {
			status = "FAILED";
			message = e.getMessage();
            return wrapWebServiceResults(status, "Can't delete new permission group mapping: " + message, null);
    	} finally {
			tdmAudit("wsDeletePermissionGroupMapping", in, status, message);
		}
	}

	private static void cleanupUsersByPermissionGroup(List<String> users, String permissionGroup, String queryPrefix) throws Exception {
		if (users != null && !users.isEmpty()) {
			// check that the user is no longer assigned to the same PG using anther fabric role
			List<String> usersToDelete = new ArrayList<>();
			for (String user : users) {
				if (!permissionGroup.equalsIgnoreCase((String) ((Map<String, Object>) wsGetPermissionGroupByUser(user)).get("result"))) {
					usersToDelete.add(user);
				}
			}
			if (!usersToDelete.isEmpty()) {
				String placeholders = String.join(",", Collections.nCopies(usersToDelete.size(), "?"));
				String deleteUsersSql = queryPrefix + " (" + placeholders + ")";
				db(TDM).execute(deleteUsersSql, usersToDelete.toArray());
			}
		}
	}

	private static void cleanupUserGrouspByPermissionGroup(List<String> users, String permissionGroup, String queryPrefix) throws Exception {
		if (users != null && !users.isEmpty()) {
			// check that the user is no longer assigned to the same PG using anther fabric role
			List<String> usersToDelete = new ArrayList<>();
			for (String user : users) {
				if (!permissionGroup.equalsIgnoreCase((String) ((Map<String, Object>) wsGetPermissionGroupByUser(user)).get("result"))) {
					usersToDelete.add(user);
				}
			}
			if (!usersToDelete.isEmpty()) {
				String placeholders = String.join(",", Collections.nCopies(usersToDelete.size(), "?"));
				String deleteUsersSql = queryPrefix + " (" + placeholders + ") AND user_type = 'GROUP'";
				db(TDM).execute(deleteUsersSql, usersToDelete.toArray());
			}
		}
	}

	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = false)
	public static Object wsGetFabricRolesByUser(String user) throws Exception {
		List<String> roles=new ArrayList<>();
		if (user == null || "".equals(user) || user.equalsIgnoreCase(sessionUser().name())) {
			roles.addAll(sessionUser().roles());}
		else {
			fabric().fetch("list users;").
				forEach(r -> {
					if(user.equals(r.get("user"))) {
						roles.addAll(Arrays.asList(((String) r.get("roles")).split(",")));
					}
				});
		}
		return wrapWebServiceResults("SUCCESS", null, roles);
	}


	@webService(path = "", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON}, elevatedPermission = false)
	public static Object wsGetPermissionGroupByUser(String user) throws Exception {
		String fabricRoles = String.join(",", (List<String>)((Map<String,Object>)wsGetFabricRolesByUser(user)).get("result"));
		Integer weight = fnGetPermissionGroupWeight(fabricRoles) ;
		if (weight == 0) {
			return wrapWebServiceResults("FAILED", "Can't find permission group for the user: " + sessionUser().name() + ".", null);
		} else {
			String permissionGroup = null;
			for (Map.Entry<String, Integer> e : PERMISSION_GROUPS.entrySet()) {
				if (e.getValue().equals(weight)) {
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
		db(TDM).fetch(selectFabricRoles(), permissionGroup).forEach(r -> roles.add((String) r.get("fabric_role")));
		
		List<String> users = new ArrayList<>();
		fabric().fetch("list users;").forEach(r -> {
			for (String role : ((String) r.get("roles")).split(",")) {
				if (roles.contains(role)) {
					users.add((String) r.get("user"));
					break;
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


	@desc("Gets users assigned to a given Fabric role.")
	@webService(path = "getUsersByFabricRole", verb = {MethodType.GET}, version = "1", isRaw = false, isCustomPayload = false, produce = {Produce.XML, Produce.JSON})
	@resultMetaData(mediaType = Produce.JSON, example = "{\r\n" +
			"  \"result\": [\r\n" +
			"    \"admin\"\r\n" +
			"  ],\r\n" +
			"  \"errorCode\": \"SUCCESS\",\r\n" +
			"  \"message\": null\r\n" +
			"}")
	public static Object wsGetUsersByFabricRole(@param(required=true) String fabricRole) throws Exception {
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
			return wrapWebServiceResults("FAILED", e.getMessage(), null);
		}
	}
	
	public static void fnInsertActivity(String action, String entity, String description) throws Exception {
        String userId = sessionUser().name();
        String username = userId;
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String sql = "INSERT INTO " + getTDMDBSchema() + ".activities " +
                "(date, action, entity, user_id, username, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        db(TDM).execute(sql, now, action, entity, userId, username, description);
    }


}
