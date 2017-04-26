package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.shogun2.dao.UserGroupDao;

/**
 *
 * @author Nils Bühner
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("momoUserGroupDao")
public class MomoUserGroupDao<E extends MomoUserGroup> extends UserGroupDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public MomoUserGroupDao() {
		super((Class<E>) MomoUserGroup.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected MomoUserGroupDao(Class<E> clazz) {
		super(clazz);
	}
}
