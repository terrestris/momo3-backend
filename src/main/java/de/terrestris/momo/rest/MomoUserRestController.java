package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.MomoUserDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.service.MomoUserService;
import de.terrestris.shogun2.rest.AbstractRestController;


/**
 *
 * terrestris GmbH & Co. KG
 * @author ahenn
 * @date 06.04.2017
 *
 * @param <E>
 * @param <D>
 * @param <S>
 */
@RestController
@RequestMapping("/momousers")
public class MomoUserRestController<E extends MomoUser, D extends MomoUserDao<E>, S extends MomoUserService<E, D>>
		extends AbstractRestController<E, D, S>  {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoUserRestController() {
		this((Class<E>) MomoUser.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoUserRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/* (non-Javadoc)
	 * @see de.terrestris.shogun2.rest.UserRestController#setService(de.terrestris.shogun2.service.UserService)
	 */
	@Override
	@Autowired
	@Qualifier("momoUserService")
	public void setService(S service) {
		this.service = service;
	}

}
