package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.service.UserGroupRoleService;
import de.terrestris.shogun2.rest.AbstractRestController;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@RestController
@RequestMapping("/usergrouproles")
public class UserGroupRoleRestController <E extends UserGroupRole, D extends UserGroupRoleDao<E>, S extends UserGroupRoleService<E, D>>
		extends AbstractRestController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public UserGroupRoleRestController() {
		this((Class<E>) UserGroupRole.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected UserGroupRoleRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("userGroupRoleService")
	public void setService(S service) {
		this.service = service;
	}

}
