package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.MomoApplication;
import de.terrestris.shogun2.dao.ApplicationDao;

/**
 *
 * @author Nils Bühner
 *
 * @param <E>
 */
@Repository("momoApplicationDao")
public class MomoApplicationDao<E extends MomoApplication> extends ApplicationDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public MomoApplicationDao() {
		super((Class<E>) MomoApplication.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected MomoApplicationDao(Class<E> clazz) {
		super(clazz);
	}

}
