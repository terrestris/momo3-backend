/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.model.tree.DocumentTreeFolder;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Nils Bühner
 * @param <E>
 *
 */
public class DocumentTreeFolderPermissionEvaluator<E extends DocumentTreeFolder> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public DocumentTreeFolderPermissionEvaluator() {
		this((Class<E>) DocumentTreeFolder.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected DocumentTreeFolderPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ this entity.
	 */
	@Override
	public boolean hasPermission(User user, E entity, Permission permission) {

		// always grant READ right for this entity
		if (permission.equals(Permission.READ)) {
			LOG.trace("Granting READ for document tree folder.");
			return true;
		}

		// call parent implementation from SHOGun2
		return super.hasPermission(user, entity, permission);
	}

}
