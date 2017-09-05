/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class MomoApplicationPermissionEvaluator<E extends MomoApplication> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoApplicationPermissionEvaluator() {
		this((Class<E>) MomoApplication.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MomoApplicationPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E application, Permission permission) {

		// all users but default users and editors are allowed to create applications
		if (permission.equals(Permission.CREATE) && (application == null || application.getId() == null) &&
				! MomoSecurityUtil.currentUsersHighestRoleIsDefaultUser() && ! MomoSecurityUtil.currentUsersHighestRoleIsEditor()) {
			return true;
		}

		// applications that have been tagged as 'public' shall be visible to everyone
		if (permission.equals(Permission.READ) && application.getOpen() == true) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultMomoPermission(user, application, permission);
	}

}
