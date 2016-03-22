package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.ProjectApplication;
import de.terrestris.shogun2.dao.ApplicationDao;
import de.terrestris.shogun2.model.Application;

/**
 * This is a demo DAO that demonstrates how a SHOGun2 DAO can be extended.
 *
 * @author Nils Bühner
 *
 * @param <E>
 */
@Repository("projectApplicationDao")
public class ProjectApplicationDao<E extends Application> extends ApplicationDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public ProjectApplicationDao() {
		super((Class<E>) ProjectApplication.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected ProjectApplicationDao(Class<E> clazz) {
		super(clazz);
	}

}
