package de.terrestris.momo.util.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.util.config.MomoConfigHolder;

/**
 *
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @date 06.04.2017
 *
 */
@Component
public class MomoSecurityUtil {

	public static MomoConfigHolder configHolder;

	/**
	 *
	 * @return
	 */
	public static boolean currentUserIsSuperAdmin(){
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		final Object principal = authentication.getPrincipal();

		if (principal instanceof MomoUser) {
			for (GrantedAuthority authority : authorities) {
				if(authority.getAuthority().equals(configHolder.getSuperAdminRoleName())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 *
	 * @return
	 */
	public static boolean currentUserHasRoleSubAdmin(){
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		final Object principal = authentication.getPrincipal();

		if (principal instanceof MomoUser) {
			for (GrantedAuthority authority : authorities) {
				if(authority.getAuthority().equals(configHolder.getSubAdminRoleName())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 *
	 * @return
	 */
	public static boolean currentUsersHighestRoleIsEditor(){
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		final Object principal = authentication.getPrincipal();

		if (principal instanceof MomoUser) {
			for (GrantedAuthority authority : authorities) {
				if(authority.getAuthority().equals(configHolder.getEditorRoleName())) {
					return !MomoSecurityUtil.currentUserHasRoleSubAdmin();
				}
			}
		}

		return false;
	}

	/**
	 *
	 * @return
	 */
	public static boolean currentUsersHighestRoleIsDefaultUser(){
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		final Object principal = authentication.getPrincipal();

		if (principal instanceof MomoUser) {
			for (GrantedAuthority authority : authorities) {
				// if user's highest role is ROLE_USER, the list of authorities will always have one single element
				if(authority.getAuthority().equals(configHolder.getDefaultUserRoleName()) && authorities.size() == 1) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @param configHolder the configHolder to set
	 */
	@Autowired
	@Qualifier("momoConfigHolder")
	public void setConfigHolder(MomoConfigHolder configHolder) {
		MomoSecurityUtil.configHolder = configHolder;
	}

}
