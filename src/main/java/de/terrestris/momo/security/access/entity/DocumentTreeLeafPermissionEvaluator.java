/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.model.tree.DocumentTreeLeaf;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Nils Bühner
 * @param <E>
 *
 */
public class DocumentTreeLeafPermissionEvaluator<E extends DocumentTreeLeaf> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public DocumentTreeLeafPermissionEvaluator() {
		this((Class<E>) DocumentTreeLeaf.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected DocumentTreeLeafPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ this entity.
	 */
	@Override
	public boolean hasPermission(User user, E entity, Permission permission) {

		// always grant READ right for this entity
		if (permission.equals(Permission.READ)) {
			LOG.trace("Granting READ for document tree leaf.");
			return true;
		}

		// call parent implementation from SHOGun2
		return super.hasPermission(user, entity, permission);
	}

}
