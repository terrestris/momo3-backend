package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.PrintFormSettingsDao;
import de.terrestris.momo.model.state.PrintFormSettingsState;
import de.terrestris.momo.service.PrintFormSettingsService;
import de.terrestris.shogun2.rest.AbstractRestController;

/**
 *
 * @author Johannes Weskamm
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 * @param <S>
 */
@RestController
@RequestMapping(value = "/rest/printsettings")
public class PrintFormSettingsRestController<E extends PrintFormSettingsState, D extends PrintFormSettingsDao<E>,
		S extends PrintFormSettingsService<E, D>> extends AbstractRestController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public PrintFormSettingsRestController() {
		this((Class<E>) PrintFormSettingsState.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected PrintFormSettingsRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("printFormSettingsService")
	public void setService(S service) {
		this.service = service;
	}

}
