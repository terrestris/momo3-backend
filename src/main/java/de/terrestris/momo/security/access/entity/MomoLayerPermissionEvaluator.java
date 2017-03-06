/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.security.access.entity.PersistentObjectPermissionEvaluator;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class MomoLayerPermissionEvaluator<E extends MomoLayer> extends PersistentObjectPermissionEvaluator<E> {

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
	public boolean hasPermission(User user, E entity, Permission permission) {

		// always grant READ right for this entity
		if (permission.equals(Permission.READ)) {
			LOG.trace("Granting READ for layer.");
			return true;
		}

		// always grant CREATE right for this entity
		if (permission.equals(Permission.CREATE)) {
			LOG.trace("Granting CREATE for layer.");
			return true;
		}
		// always grant CREATE right for this entity
		if (permission.equals(Permission.UPDATE)) {
			if (entity.getOwner().getId().equals(user.getId())) {
				LOG.trace("Granting UPDATE for layer.");
				return true;
			}
		}

		// call parent implementation from SHOGun2
		return super.hasPermission(user, entity, permission);
	}

}
