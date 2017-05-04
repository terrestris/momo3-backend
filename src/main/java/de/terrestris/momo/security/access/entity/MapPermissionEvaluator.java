/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.module.Map;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class MapPermissionEvaluator<E extends Map> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MapPermissionEvaluator() {
		this((Class<E>) Map.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MapPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E map, Permission permission) {

		// all users but default users and editors are allowed to create Map
		if (permission.equals(Permission.CREATE) && (map == null || map.getId() == null) &&
				! MomoSecurityUtil.currentUsersHighestRoleIsDefaultUser() && ! MomoSecurityUtil.currentUsersHighestRoleIsEditor()) {
			return true;
		}

		// always allow read on map...
		if (permission.equals(Permission.READ)) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultMomoPermission(user, map, permission);
	}

}
