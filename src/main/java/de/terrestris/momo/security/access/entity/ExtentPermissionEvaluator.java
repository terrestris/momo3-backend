/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.layer.util.Extent;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class ExtentPermissionEvaluator<E extends Extent> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public ExtentPermissionEvaluator() {
		this((Class<E>) Extent.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected ExtentPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E extent, Permission permission) {

		// all users but default users and editors are allowed to create extents
		if (permission.equals(Permission.CREATE) && (extent == null || extent.getId() == null) &&
				! MomoSecurityUtil.currentUsersHighestRoleIsDefaultUser() && ! MomoSecurityUtil.currentUsersHighestRoleIsEditor()) {
			return true;
		}

		// always allow read on extents...
		if (permission.equals(Permission.READ)) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultMomoPermission(user, extent, permission);
	}

}
