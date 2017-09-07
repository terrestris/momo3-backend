/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.model.File;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class FilePermissionEvaluator<E extends File> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public FilePermissionEvaluator() {
		this((Class<E>) File.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected FilePermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E file, Permission permission) {

		// all users with at least role_user are allowed to create files here
		if (permission.equals(Permission.CREATE) && (file == null || file.getId() == null) &&
				MomoSecurityUtil.currentUsersHighestRoleIsEditor() ||
				MomoSecurityUtil.currentUserHasRoleSubAdmin() ||
				MomoSecurityUtil.currentUserIsSuperAdmin()) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultMomoPermission(user, file, permission);
	}

}
