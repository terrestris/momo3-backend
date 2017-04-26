package de.terrestris.momo.security.access.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.service.UserGroupRoleService;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.security.PermissionCollection;
import de.terrestris.shogun2.security.access.entity.PersistentObjectPermissionEvaluator;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
public class MomoPersistentObjectPermissionEvaluator<E extends PersistentObject> extends PersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoPersistentObjectPermissionEvaluator() {
		this((Class<E>) PersistentObject.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	public MomoPersistentObjectPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Autowired
	@Qualifier("userGroupRoleService")
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 *
	 * @param user
	 * @param entity
	 * @param permission
	 * @return
	 */
	protected boolean hasDefaultMomoPermission(User user, E entity, Permission permission) {
		boolean hasPermission = false;

		if (entity == null) {
			return false;
		}

		MomoUser momoUser = (MomoUser) user;

		final String simpleClassName = getEntityClass().getSimpleName();

		String grantMsg = "Granting %s access on secured object \"%s\" with ID %s";
		String restrictMsg = "Restricting %s access on secured object \"%s\" with ID %s";

		Map<User, PermissionCollection> permissionsForUser = entity.getUserPermissions();
		if (permissionsForUser != null) {
			PermissionCollection userPermissions = permissionsForUser.get(momoUser);
			if (userPermissions != null && userPermissions.getPermissions().contains(permission)) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, entity.getId()));
				hasPermission = true;
			}
		}

		if (!hasPermission) {
			Map<UserGroup, PermissionCollection> userGroupPermissionsOfCurrentUser = entity.getGroupPermissions();
			if (userGroupPermissionsOfCurrentUser != null) {
				for (UserGroup group : userGroupPermissionsOfCurrentUser.keySet()) {
					PermissionCollection permissionsForGroup = userGroupPermissionsOfCurrentUser.get(group);
					if (permissionsForGroup != null) {
						if (permissionsForGroup.getPermissions().contains(permission)) {
							LOG.trace(String.format(grantMsg, permission, simpleClassName, entity.getId()));
							hasPermission = true;
						}
					}
				}
			}
		}

		LOG.trace(String.format(restrictMsg, permission, simpleClassName, entity.getId()));
		return hasPermission;
	}

	/**
	 *
	 * @param userId
	 * @param groupPermissionsMap
	 * @return
	 */
	@Override
	protected PermissionCollection extractGroupPermissions(User user,
			Map<UserGroup, PermissionCollection> groupPermissionsMap) {

		Set<Permission> aggregatedGroupPermissions = new HashSet<Permission>();

		Set<UserGroup> userGroupsWithPermissions = groupPermissionsMap.keySet();

		for (UserGroup userGroup : userGroupsWithPermissions) {

			Set<MomoUser> userGroupMembers = userGroupRoleService.findAllUserGroupMembers((MomoUserGroup) userGroup);
			if (userGroupMembers.contains(user)) {
				Set<Permission> groupPermissions = groupPermissionsMap.get(userGroup).getPermissions();
				aggregatedGroupPermissions.addAll(groupPermissions);
			}

		}

		return new PermissionCollection(aggregatedGroupPermissions);
	}

	/**
	 * @return the userGroupRoleService
	 */
	public UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> getUserGroupRoleService() {
		return userGroupRoleService;
	}

	/**
	 * @param userGroupRoleService the userGroupRoleService to set
	 */
	public void setUserGroupRoleService(
			UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService) {
		this.userGroupRoleService = userGroupRoleService;
	}

}
