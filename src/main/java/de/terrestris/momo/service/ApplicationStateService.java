package de.terrestris.momo.service;

import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.terrestris.momo.dao.ApplicationStateDao;
import de.terrestris.momo.model.state.ApplicationState;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.service.PermissionAwareCrudService;
import de.terrestris.shogun2.service.UserService;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("applicationStateService")
public class ApplicationStateService<E extends ApplicationState, D extends ApplicationStateDao<E>>
		extends PermissionAwareCrudService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ApplicationStateService() {
		this((Class<E>) ApplicationState.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected ApplicationStateService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("applicationStateDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	@Autowired
	@Qualifier("userService")
	private UserService<User, UserDao<User>> userService;

	/**
	 *
	 * @param e
	 * @return
	 */
	@Override
	@PreAuthorize("hasRole(@configHolder.getDefaultUserRoleName())")
	public void saveOrUpdate(E e) {
		if (e.getId() == null) {
			User currentUser = userService.getUserBySession();
			e.setOwner(currentUser);
			this.addAndSaveUserPermissions(e, currentUser, Permission.ADMIN);
		}
		dao.saveOrUpdate(e);
	}

	/**
	 *
	 * @param e
	 */
	@Override
	@PreAuthorize("hasRole(@configHolder.getDefaultUserRoleName())")
	public void delete(E e) {
		this.removeAndSaveUserPermissions(e, e.getOwner(), Permission.ADMIN);
		dao.delete(e);
	}

	/**
	 *
	 * @param webMapId
	 * @return
	 */
	@PreAuthorize("hasRole(@configHolder.getDefaultUserRoleName())")
	@Transactional(readOnly = true)
	public Set<E> findByWebMapIdForCurrentUser(Integer webMapId) {
		User currentUser = userService.getUserBySession();
		return dao.findByWebMapIdAndUserId(webMapId, currentUser.getId());
	}

	/**
	 *
	 * @param owner
	 * @return
	 */
	@PreAuthorize("hasRole(@configHolder.getDefaultUserRoleName())")
	@Transactional(readOnly = true)
	public List<E> findAllWorkstatesOfUser(User owner) {
		SimpleExpression eqOwner = Restrictions.eq("owner", owner);
		return dao.findByCriteria(eqOwner);
	}

}
