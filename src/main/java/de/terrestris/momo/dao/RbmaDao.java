package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.shogun2.dao.TreeNodeDao;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 * This is a demo DAO that demonstrates how a SHOGun2 DAO can be extended.
 *
 * @author Nils Bühner
 *
 * @param <E>
 */
@Repository("rbmaDao")
public class RbmaDao<E extends TreeNode> extends TreeNodeDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public RbmaDao() {
		super((Class<E>) TreeNode.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected RbmaDao(Class<E> clazz) {
		super(clazz);
	}

}
