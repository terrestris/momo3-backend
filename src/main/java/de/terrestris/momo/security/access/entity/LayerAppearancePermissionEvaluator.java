/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.layer.appearance.LayerAppearance;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class LayerAppearancePermissionEvaluator<E extends LayerAppearance> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public LayerAppearancePermissionEvaluator() {
		this((Class<E>) LayerAppearance.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected LayerAppearancePermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ, UPDATE and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E appearance, Permission permission) {

		// all users but default users are allowed to create layers and their appearances
		if (permission.equals(Permission.CREATE) && (appearance == null || appearance.getId() == null) &&
				! MomoSecurityUtil.currentUsersHighestRoleIsDefaultUser()) {
			return true;
		}

		return hasDefaultMomoPermission(user, appearance, permission);
	}

}
