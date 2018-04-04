package de.terrestris.momo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.PrintFormSettingsDao;
import de.terrestris.momo.model.state.PrintFormSettingsState;
import de.terrestris.shogun2.service.PermissionAwareCrudService;

/**
 *
 * @author Johannes Weskamm
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("printFormSettingsService")
public class PrintFormSettingsService<E extends PrintFormSettingsState, D extends PrintFormSettingsDao<E>>
		extends PermissionAwareCrudService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public PrintFormSettingsService() {
		this((Class<E>) PrintFormSettingsState.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected PrintFormSettingsService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("printFormSettingsDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

}
