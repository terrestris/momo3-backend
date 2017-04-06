package de.terrestris.momo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.shogun2.model.Role;
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
	 *
	 * @param user
	 * @return
	 */
	public Set<Role> findAllUserRoles(MomoUser user) {
		Set<Role> allUserRoles = new HashSet<Role>();

		if (user != null) {
			List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(user);

			for (UserGroupRole userGroupRole : userGroupRoles) {
				Role userRole = userGroupRole.getRole();
				allUserRoles.add(userRole);
			}
		}

		return allUserRoles;
	}

	/**
	 *
	 * @param user
	 * @return
	 */
	public Set<MomoUserGroup> findAllUserGroups(MomoUser user) {
		Set<MomoUserGroup> allUserGroups = new HashSet<MomoUserGroup>();

		if (user != null) {
			List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(user);

			for (UserGroupRole userGroupRole : userGroupRoles) {
				MomoUserGroup userGroup = userGroupRole.getGroup();
				allUserGroups.add(userGroup);
			}
		}

		return allUserGroups;
	}

	/**
	 *
	 * @param userGroup
	 * @return
	 */
	public Set<Role> findAllUserGroupRoles(MomoUserGroup userGroup) {
		Set<Role> allUserGroupRoles = new HashSet<Role>();

		if (userGroup != null) {
			List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(userGroup);

			for (UserGroupRole userGroupRole : userGroupRoles) {
				Role groupRole = userGroupRole.getRole();
				allUserGroupRoles.add(groupRole);
			}
		}

		return allUserGroupRoles;
	}

	/**
	 *
	 * @param userGroup
	 * @return
	 */
	public Set<MomoUser> findAllUserGroupMembers(MomoUserGroup userGroup) {
		Set<MomoUser> allUserGroupMembers = new HashSet<MomoUser>();

		if (userGroup != null) {
			List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(userGroup);

			for (UserGroupRole userGroupRole : userGroupRoles) {
				MomoUser userGroupMember = userGroupRole.getUser();
				allUserGroupMembers.add(userGroupMember);
			}
		}

		return allUserGroupMembers;
	}

	/**
	 *
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(MomoUser user) {
		if (user == null) {
			return null;
		}

		return (List<UserGroupRole>) this.dao.findAllWhereFieldEquals(
				"user", user);
	}

	/**
	 *
	 * @param userGroup
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(MomoUserGroup userGroup) {
		if (userGroup == null) {
			return null;
		}

		return (List<UserGroupRole>) this.dao.findAllWhereFieldEquals(
				"group", userGroup);
	}

	/**
	 *
	 * @param role
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(Role role) {
		if (role == null ) {
			return null;
		}

		return (List<UserGroupRole>) this.dao.findAllWhereFieldEquals(
				"role", role);
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(MomoUser user, MomoUserGroup userGroup) {
		if (user == null || userGroup == null) {
			return null;
		}

		final Criterion and = Restrictions.and(
				Restrictions.eq("user", user),
				Restrictions.eq("group", userGroup)
		);

		return (List<UserGroupRole>) this.dao.findByCriteria(and);
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 * @param role
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(MomoUser user, MomoUserGroup userGroup, Role role) {
		if (user == null || userGroup == null || role == null) {
			return null;
		}

		final Criterion and = Restrictions.and(
				Restrictions.eq("user", user),
				Restrictions.eq("group", userGroup),
				Restrictions.eq("role", role)
		);

		return (List<UserGroupRole>) this.dao.findByCriteria(and);
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 * @return
	 */
	public boolean isUserMemberInUserGroup(MomoUser user, MomoUserGroup userGroup) {
		if (user == null || userGroup == null) {
			return false;
		}

		final Criterion and = Restrictions.and(
				Restrictions.eq("user", user),
				Restrictions.eq("group", userGroup)
		);

		return (this.dao.getTotalCount(and).longValue() > 0);
	}

	/**
	 * TODO Check if correct security annotation
	 * TODO Add role for new user
	 *
	 * @param user
	 * @param userGroup
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("isAuthenticated()")
	public void addUserToGroup(MomoUser user, MomoUserGroup userGroup, Role role) {
		// TODO add isnull check
		if (this.isUserMemberInUserGroup(user, userGroup)) {
			LOG.trace("User with ID " + user.getId() + " is already a member of "
					+ "group with ID " + userGroup.getId());
			return;
		}

		UserGroupRole userGroupRole = new UserGroupRole(user, userGroup);

		this.dao.saveOrUpdate((E) userGroupRole);
	}

	/**
	 * TODO Check if correct security annotation
	 *
	 * @param user
	 * @param userGroup
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("isAuthenticated()")
	public void removeUserFromGroup(MomoUser user, MomoUserGroup userGroup) {
		// TODO add isnull check
		if (this.isUserMemberInUserGroup(user, userGroup) == false) {
			LOG.trace("User with ID " + user.getId() + " is not a member of "
					+ "group with ID " + userGroup.getId());
			return;
		}

		List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(user, userGroup);

		for (UserGroupRole userGroupRole : userGroupRoles) {
			this.dao.delete((E) userGroupRole);
		}
	}

}
