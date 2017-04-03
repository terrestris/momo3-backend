package de.terrestris.momo.security;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.service.UserGroupRoleService;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.security.Shogun2AuthenticationProvider;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
public class MomoAuthenticationProvider extends Shogun2AuthenticationProvider {

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger
			.getLogger(MomoAuthenticationProvider.class);

	/**
	 *
	 */
	@Autowired
	private UserDao<User> userDao;

	/**
	 *
	 */
	@Autowired
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 *
	 * This method has to be {@link Transactional} to allow that associated entities
	 * can be fetched lazily.
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 *      authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	@Transactional(value="transactionManager", readOnly=true)
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {

		// prepare an exception
		final String exceptionMessage = "User and password do not match.";

		// get username/password
		String accountName = authentication.getName();
		String rawPassword = (String) authentication.getCredentials();

		LOG.debug("Trying to authenticate User '" + accountName + "'");

		MomoUser user = (MomoUser) userDao.findByAccountName(accountName);

		// prepare set of authorities
		Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();

		String encryptedPassword = null;

		if (user == null) {
			LOG.warn("No user for account name '" + accountName + "' could be found.");
			throw new UsernameNotFoundException(exceptionMessage);
		} else if (!user.isActive()) {
			LOG.warn("The user with the account name '" + accountName + "' is not active.");
			throw new DisabledException(exceptionMessage);
		} else {

			encryptedPassword = user.getPassword();

			// check if rawPassword matches the hash from db
			if (getPasswordEncoder().matches(rawPassword, encryptedPassword)) {

				Set<Role> allUserRoles = userGroupRoleService.findAllUserRoles(user);

				// create granted authorities for the security context
				for (Role role : allUserRoles) {
					grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
				}

			} else {
				LOG.warn("The given password for user '" + accountName + "' does not match.");
				throw new BadCredentialsException(exceptionMessage);
			}

		}

		// Create corresponding token to forward in Spring Security's filter
		// chain. We will use the SHOGun2 user as the principal.
		Authentication authResult = null;
		if (grantedAuthorities.isEmpty()) {
			// if the user has no authorities, we will build the
			// UsernamePasswordAuthenticationToken without authorities, which
			// leads to an unauthenticated user, i.e. isAuthenticated() of
			// authenticationToken will return false afterwards.
			LOG.warn("The user '" + accountName + "' has no authorities and will thereby NOT be authenticated.");
			authResult = new UsernamePasswordAuthenticationToken(user, encryptedPassword);
		} else {
			// if we pass some grantedAuthorities, isAuthenticated() of
			// authenticationToken will return true afterwards
			authResult = new UsernamePasswordAuthenticationToken(user, encryptedPassword, grantedAuthorities);

			LOG.debug("The user '" + accountName
					+ "' got the following (explicit) roles: "
					+ StringUtils.join(getRawRoleNames(grantedAuthorities), ", "));
		}

		final boolean isAuthenticated = authResult.isAuthenticated();
		final String authLog = isAuthenticated ? "has succesfully" : "has NOT";
		LOG.info("The user '" + accountName + "' " + authLog  + " been authenticated.");

		return authResult;
	}

	/**
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#supports
	 *      (java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return (UsernamePasswordAuthenticationToken.class
				.isAssignableFrom(authentication));
	}

	/**
	 * @param grantedAuthorities
	 * @return
	 */
	private Set<String> getRawRoleNames(Set<GrantedAuthority> grantedAuthorities) {
		Set<String> grantedRoles = new HashSet<String>();
		for (GrantedAuthority auth : grantedAuthorities) {
			grantedRoles.add(auth.getAuthority());
		}
		return grantedRoles;
	}
}
