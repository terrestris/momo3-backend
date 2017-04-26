package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.MomoUser;
import de.terrestris.shogun2.dao.UserDao;

/**
 *
 * @author Nils Bühner
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("momoUserDao")
public class MomoUserDao<E extends MomoUser> extends UserDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public MomoUserDao() {
		super((Class<E>) MomoUser.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected MomoUserDao(Class<E> clazz) {
		super(clazz);
	}
}
