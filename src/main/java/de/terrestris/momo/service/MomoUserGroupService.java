package de.terrestris.momo.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.MomoUserDao;
import de.terrestris.momo.dao.MomoUserGroupDao;
import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.util.config.MomoConfigHolder;
import de.terrestris.shogun2.dao.RoleDao;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.service.RoleService;
import de.terrestris.shogun2.service.UserGroupService;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("momoUserGroupService")
public class MomoUserGroupService<E extends MomoUserGroup, D extends MomoUserGroupDao<E>>
		extends UserGroupService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoUserGroupService() {
		this((Class<E>) MomoUserGroup.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoUserGroupService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoUserGroupDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	@Autowired
	@Qualifier("momoUserService")
	private MomoUserService<MomoUser, MomoUserDao<MomoUser>> momoUserService;

	@Autowired
	@Qualifier("userGroupRoleService")
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 * Role service
	 */
	@Autowired
	@Qualifier("roleService")
	protected RoleService<Role, RoleDao<Role>> roleService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoConfigHolder")
	private MomoConfigHolder momoConfigHolder;

	/**
	 * Override in order to set the owner and correct class..
	 */
	@Override
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())"
			+ " or (#e.id == null and hasPermission(#e, 'CREATE'))"
			+ " or (#e.id != null and hasPermission(#e, 'UPDATE'))")
	public void saveOrUpdate(E e) {
		// set owner in create mode
		if (e.getId() == null) {
			MomoUser user = momoUserService.getUserBySession();
			if (user != null) {
				e.setOwner(user);
				// also grant creator the subadmin role to this group
				// so that he will be able to edit it afterwards
				String subAdminRoleName = momoConfigHolder.getSubAdminRoleName();
				Role subadminRole = roleService.findByRoleName(subAdminRoleName);
				UserGroupRole userGroupRole = new UserGroupRole(user, e, subadminRole);
				userGroupRoleService.saveOrUpdate(userGroupRole);
			}
		}
		dao.saveOrUpdate(e);
	}

	/**
	 * Override in order to handle deletion correctly...
	 * TODO: what we really need to delete?
	 */
	@Override
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())"
			+ " or (#e.id != null and hasPermission(#e, 'DELETE'))")
	public void delete(E e) {
		Set<MomoUser> members = userGroupRoleService.findAllUserGroupMembers(e);
		for (MomoUser momoUser : members) {
			userGroupRoleService.removeUserPermissionsFromGroup(momoUser, e);
			userGroupRoleService.removeUserFromGroup(momoUser, e);
		}

		dao.delete(e);
	}
}
