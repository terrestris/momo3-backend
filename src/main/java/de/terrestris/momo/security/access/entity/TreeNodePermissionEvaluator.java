/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class TreeNodePermissionEvaluator<E extends TreeNode> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public TreeNodePermissionEvaluator() {
		this((Class<E>) TreeNode.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected TreeNodePermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ, UPDATE and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E entity, Permission permission) {

		// always grant READ right for this entity
		if (permission.equals(Permission.READ)) {
			LOG.trace("Granting READ for TreeNode.");
			return true;
		}

		// always grant CREATE right for this entity
		if (permission.equals(Permission.CREATE)) {
			LOG.trace("Granting CREATE for TreeNode.");
			return true;
		}

		// always grant UPDATE right for this entity
		if (permission.equals(Permission.UPDATE)) {
			LOG.trace("Granting UPDATE for TreeNode.");
			return true;
		}

		// call parent implementation from SHOGun2
		return super.hasPermission(user, entity, permission);
	}

}
