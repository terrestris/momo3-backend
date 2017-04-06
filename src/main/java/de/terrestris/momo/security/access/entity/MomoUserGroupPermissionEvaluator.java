/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.security.access.entity.UserGroupPermissionEvaluator;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
public class MomoUserGroupPermissionEvaluator<E extends MomoUserGroup> extends UserGroupPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoUserGroupPermissionEvaluator() {
		this((Class<E>) MomoUserGroup.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MomoUserGroupPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ, UPDATE and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E userGroup, Permission permission) {

		final String simpleClassName = getEntityClass().getSimpleName();

		String grantMsg = "Granting %s access on secured object \"%s\" with ID %s";
		String restrictMsg = "Restricting %s access on secured object \"%s\" with ID %s";

		// Always restrict CREATE right for this entity. Only ROLE_SUPERADMIN
		// is allowed to create one.
		if (permission.equals(Permission.CREATE)) {
			LOG.trace(String.format(restrictMsg, permission, simpleClassName, userGroup.getId()));
			return false;
		}

		// Always grant READ right for this entity.
		if (permission.equals(Permission.READ)) {
			LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroup.getId()));
			return true;
		}

		// Grant UPDATE right for this entity, if the user is the owner.
		if (permission.equals(Permission.UPDATE)) {
			if (userGroup.getOwner().getId().equals(user.getId())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroup.getId()));
				return true;
			}
		}

		// Grant DELETE right for this entity, if the user is the owner.
		if (permission.equals(Permission.DELETE)) {
			if (userGroup.getOwner().getId().equals(user.getId())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroup.getId()));
				return true;
			}
		}

		LOG.trace(String.format(restrictMsg, permission, simpleClassName, userGroup.getId()));

		return false;

		// We don't call the parent implementation from SHOGun2 here as we
		// do have an intended override for the getMembers() of the MomoUserGroup
		// present that will cause the parent method to fail.
		// return super.hasPermission(user, userGroup, permission);
	}

}
