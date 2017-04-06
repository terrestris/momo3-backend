/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.model.MomoApplication;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class ApplicationPermissionEvaluator<E extends MomoApplication> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public ApplicationPermissionEvaluator() {
		this((Class<E>) MomoApplication.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected ApplicationPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E entity, Permission permission) {

		// always grant READ right for this entity
		if (permission.equals(Permission.READ)) {
			LOG.trace("Granting READ for application.");
			return true;
		}

		// always grant CREATE right for this entity
		if (permission.equals(Permission.CREATE)) {
			LOG.trace("Granting CREATE for application.");
			return true;
		}

		// call parent implementation from SHOGun2
		return super.hasPermission(user, entity, permission);
	}

}
