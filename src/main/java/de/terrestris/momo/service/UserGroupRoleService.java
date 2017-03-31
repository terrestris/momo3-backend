package de.terrestris.momo.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.shogun2.service.PermissionAwareCrudService;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("userGroupRoleService")
public class UserGroupRoleService<E extends UserGroupRole, D extends UserGroupRoleDao<E>>
		extends PermissionAwareCrudService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public UserGroupRoleService() {
		this((Class<E>) UserGroupRole.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected UserGroupRoleService(Class<E> entityClass) {
		super(entityClass);
	}
	
	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("userGroupRoleDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(UserGroupRoleService.class);
	
	

}
