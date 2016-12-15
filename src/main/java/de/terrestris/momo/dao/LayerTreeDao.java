package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.shogun2.dao.TreeNodeDao;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 *
 * @author Nils Bühner
 * @author Daniel Koch
 *
 * @param <E>
 */
@Repository("layerTreeDao")
public class LayerTreeDao<E extends TreeNode> extends TreeNodeDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public LayerTreeDao() {
		super((Class<E>) TreeNode.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected LayerTreeDao(Class<E> clazz) {
		super(clazz);
	}

}
