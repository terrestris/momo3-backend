package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.shogun2.dao.TreeNodeDao;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 *
 * @author Nils Bühner
 *
 * @param <E>
 */
@Repository("docTreeDao")
public class DocumentTreeDao<E extends TreeNode> extends TreeNodeDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public DocumentTreeDao() {
		super((Class<E>) TreeNode.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected DocumentTreeDao(Class<E> clazz) {
		super(clazz);
	}

}
