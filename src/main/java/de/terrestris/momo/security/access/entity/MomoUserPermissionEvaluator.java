/**
 *
 */
package de.terrestris.momo.security.access.entity;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.util.config.MomoConfigHolder;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.security.access.entity.UserPermissionEvaluator;


/**
 *
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @date 05.04.2017
 *
 * @param <E>
 */
public class MomoUserPermissionEvaluator<E extends MomoUser> extends UserPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoUserPermissionEvaluator() {
		this((Class<E>) MomoUser.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MomoUserPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoConfigHolder")
	private MomoConfigHolder momoConfigHolder;

	/**
	 * Always grants right to READ, UPDATE and DELETE an user.
	 */
	@Override
	public boolean hasPermission(User user, E momoUser, Permission permission) {

		final String simpleClassName = getEntityClass().getSimpleName();

		String grantMsg = "Granting %s access on secured object \"%s\" with ID %s";
		String restrictMsg = "Restricting %s access on secured object \"%s\" with ID %s";

		// Always restrict CREATE right for this entity. Users can only be created by themselves via email.
		if (permission.equals(Permission.CREATE)) {
			LOG.trace(String.format(restrictMsg, permission, simpleClassName, momoUser.getId()));
			return false;
		}

		// Always grant READ right for this entity.
		if (permission.equals(Permission.READ)) {
			// each user can read its own props
			if (user.equals(momoUser)) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, momoUser.getId()));
				return true;
			}

			// always allow to read all users for an user that has at least the following role: ROLE_EDITOR
			Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext()
					.getAuthentication().getAuthorities();
			for (GrantedAuthority grantedAuthority : authorities) {
				if (grantedAuthority.getAuthority().equalsIgnoreCase(momoConfigHolder.getEditorRoleName()) ||
						grantedAuthority.getAuthority().equalsIgnoreCase(momoConfigHolder.getSubAdminRoleName())) {
					LOG.trace(String.format(grantMsg, permission, simpleClassName, momoUser.getId()));
					return true;
				}
			}
		}

		// Grant UPDATE right for this entity only for user.
		if (permission.equals(Permission.UPDATE)) {
			if (momoUser.getId().equals(user.getId())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, momoUser.getId()));
				return true;
			}
		}

		// Grant DELETE right for this entity
		// - user want's to remove his account
		if (permission.equals(Permission.DELETE)) {
			if (momoUser.getId().equals(user.getId())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, momoUser.getId()));
				return true;
			}
		}

		LOG.trace(String.format(restrictMsg, permission, simpleClassName, momoUser.getId()));
		return false;

		// We don't call the parent implementation from SHOGun2 here as we
		// do have an intended override for the getMembers() of the MomoUserGroup
		// present that will cause the parent method to fail.
		// return super.hasPermission(user, userGroup, permission);
	}

}
