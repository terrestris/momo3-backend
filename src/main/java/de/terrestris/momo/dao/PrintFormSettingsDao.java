package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.state.PrintFormSettingsState;
import de.terrestris.shogun2.dao.GenericHibernateDao;

/**
 *
 * @author Johannes Weskamm
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("printFormSettingsDao")
public class PrintFormSettingsDao<E extends PrintFormSettingsState> extends GenericHibernateDao<E, Integer> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public PrintFormSettingsDao() {
		super((Class<E>) PrintFormSettingsState.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected PrintFormSettingsDao(Class<E> clazz) {
		super(clazz);
	}

}
