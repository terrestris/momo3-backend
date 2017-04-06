/**
 *
 */
package de.terrestris.momo.security.access.entity;

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
	public boolean hasPermission(User user, E entity, Permission permission) {

		// always grant READ right for this entity
		if (permission.equals(Permission.READ)) {
			LOG.trace("Granting READ for LayerAppearance.");
			return true;
		}

		// always grant CREATE right for this entity
		if (permission.equals(Permission.CREATE)) {
			LOG.trace("Granting CREATE for LayerAppearance.");
			return true;
		}

		// always grant CREATE right for this entity
		if (permission.equals(Permission.UPDATE)) {
			LOG.trace("Granting CREATE for LayerAppearance.");
			return true;
		}

		// call parent implementation from SHOGun2
		return super.hasPermission(user, entity, permission);
	}

}
