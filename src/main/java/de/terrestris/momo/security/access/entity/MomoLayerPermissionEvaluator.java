/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class MomoLayerPermissionEvaluator<E extends MomoLayer> extends MomoPersistentObjectPermissionEvaluator<E> {
	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoLayerPermissionEvaluator() {
		this((Class<E>) MomoLayer.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MomoLayerPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ, UPDATE and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E layer, Permission permission) {

		// all users but default users are allowed to create layers
		if (permission.equals(Permission.CREATE) &&
				(layer == null || layer.getId() == null) &&
				! MomoSecurityUtil.currentUsersHighestRoleIsDefaultUser()) {
			return true;
		}

		// permit always read on the osm-wms layer, as its needed in application
		// creation process...
		if (permission.equals(Permission.READ) && layer.getName() != null &&
				layer.getName().equalsIgnoreCase("OSM-WMS GRAY")) {
			return true;
		}

		return hasDefaultMomoPermission(user, layer, permission);
	}

}
