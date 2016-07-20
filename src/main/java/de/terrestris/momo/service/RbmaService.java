package de.terrestris.momo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.terrestris.shogun2.dao.TreeNodeDao;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.service.TreeNodeService;

/**
 * This is a demo service that demonstrates how a SHOGun2 service can be
 * extended.
 *
 * @author Nils Bühner
 *
 * @param <E>
 * @param <D>
 */
@Service("rbmaService")
public class RbmaService<E extends TreeNode, D extends TreeNodeDao<E>> extends
		TreeNodeService<E, D> {

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("rbmaDao")
	public void setDao(D dao) {
		super.setDao(dao);
	}
}