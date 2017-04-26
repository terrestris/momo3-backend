package de.terrestris.momo.security.access.entity;

import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Nils Bühner
 *
 */
public class MomoAlwaysDenyCrudPermissionEvaluator<E extends PersistentObject> extends
		MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoAlwaysDenyCrudPermissionEvaluator() {
		this((Class<E>) PersistentObject.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MomoAlwaysDenyCrudPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E entity, Permission permission) {
		return false;
	}

}
