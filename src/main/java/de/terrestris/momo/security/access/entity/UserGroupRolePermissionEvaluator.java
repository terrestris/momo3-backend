/**
 *
 */
package de.terrestris.momo.security.access.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.service.UserGroupRoleService;
import de.terrestris.momo.util.config.MomoConfigHolder;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.dao.RoleDao;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.service.RoleService;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
public class UserGroupRolePermissionEvaluator<E extends UserGroupRole> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public UserGroupRolePermissionEvaluator() {
		this((Class<E>) UserGroupRole.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected UserGroupRolePermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Autowired
	@Qualifier("userGroupRoleService")
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoConfigHolder")
	private MomoConfigHolder momoConfigHolder;

	/**
	 *
	 */
	@Autowired
	@Qualifier("roleService")
	private RoleService<Role, RoleDao<Role>> roleService;

	/**
	 * Always grants right to READ, UPDATE and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E userGroupRole, Permission permission) {

		final String simpleClassName = getEntityClass().getSimpleName();

		String grantMsg = "Granting %s access on secured object \"%s\" with ID %s";
		String restrictMsg = "Restricting %s access on secured object \"%s\" with ID %s";

		MomoUserGroup userGroup = userGroupRole.getGroup();
		String subAdminRoleName = momoConfigHolder.getSubAdminRoleName();

		// Grant CREATE right for this entity, if:
		//   * If the user has role ROLE_SUPERADMIN OR
		//   * The user has role ROLE_SUBADMIN for the specified group OR
		//   * The user is referenced in the UserGroupRole
		if (permission.equals(Permission.CREATE)) {
			// Deny CREATE right for this entity, if:
			//   * The user is default user only.
			boolean isDefaultUser = MomoSecurityUtil.currentUserIsUserInAnyGroup();
			if (isDefaultUser) {
				LOG.trace(String.format(restrictMsg, permission, simpleClassName, userGroupRole.getId()));
				return false;
			}

			if (user.equals(userGroupRole.getUser()) ||
					userGroupRoleService.hasUserRoleInGroup((MomoUser) user, userGroup,
							roleService.findByRoleName(subAdminRoleName))) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroupRole.getId()));
				return true;
			}
		}

		// Right READ (for role ROLE_SUPERADMIN always).
		if (permission.equals(Permission.READ)) {

			// Grant READ right for this entity, if:
			//   * The user is referenced in the UserGroupRole
			if (user.equals(userGroupRole.getUser())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroupRole.getId()));
				return true;
			}

			// Grant READ right for this entity, if:
			//   * The user has role ROLE_SUBADMIN for the specified group
			if (userGroupRoleService.hasUserRoleInGroup((MomoUser) user, userGroup,
					roleService.findByRoleName(subAdminRoleName))) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroupRole.getId()));
				return true;
			}

			// Grant READ right for this entity, if:
			//   * The user has role ROLE_SUBADMIN (globally) AND
			//   * No group is assigned to the UserGroupRole
			if (userGroupRole.getGroup() == null) {
				boolean isSuperAdmin = MomoSecurityUtil.currentUserIsSuperAdmin();
				if (isSuperAdmin) {
					LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroupRole.getId()));
					return true;
				}
			}
		}

		// Always restrict UPDATE right for this entity. Only ROLE_SUPERADMIN
		// is allowed to update one.
		if (permission.equals(Permission.UPDATE)) {
			// Deny UPDATE right for this entity, if:
			//   * The user is default user only.
			boolean isDefaultUser = MomoSecurityUtil.currentUserIsUserInAnyGroup();
			if (isDefaultUser) {
				LOG.trace(String.format(restrictMsg, permission, simpleClassName, userGroupRole.getId()));
				return false;
			}

			if (user.equals(userGroupRole.getUser()) ||
					userGroupRoleService.hasUserRoleInGroup((MomoUser) user, userGroup,
							roleService.findByRoleName(subAdminRoleName))) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroupRole.getId()));
				return true;
			}
		}

		// Grant DELETE right for this entity, if:
		//   * If the user has role ROLE_SUPERADMIN OR
		//   * The user has role ROLE_SUBADMIN for the specified group OR
		//   * The user is referenced in the UserGroupRole
		if (permission.equals(Permission.DELETE)) {
			if (user.equals(userGroupRole.getUser()) ||
					userGroupRoleService.hasUserRoleInGroup((MomoUser) user, userGroup,
							roleService.findByRoleName(subAdminRoleName))) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroupRole.getId()));
				return true;
			}
		}

		LOG.trace(String.format(restrictMsg, permission, simpleClassName, userGroupRole.getId()));

		return false;

		// We don't call the parent implementation from SHOGun2 here as we
		// do have an intended override for the getMembers() of the MomoUserGroup
		// present that will cause the parent method to fail.
		// return super.hasPermission(user, userGroupRole, permission);
	}

	/**
	 * @param momoConfigHolder the momoConfigHolder to set
	 */
	public void setMomoConfigHolder(MomoConfigHolder momoConfigHolder) {
		this.momoConfigHolder = momoConfigHolder;
	}

	/**
	 * @param userGroupRoleService the userGroupRoleService to set
	 */
	public void setUserGroupRoleService(
			UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService) {
		this.userGroupRoleService = userGroupRoleService;
	}

	/**
	 * @param roleService the roleService to set
	 */
	public void setRoleService(RoleService<Role, RoleDao<Role>> roleService) {
		this.roleService = roleService;
	}

}
