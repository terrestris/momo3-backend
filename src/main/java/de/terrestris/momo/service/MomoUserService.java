package de.terrestris.momo.service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import de.terrestris.momo.dao.MomoUserDao;
import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.token.RegistrationToken;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.application.Shogun2ContextUtil;
import de.terrestris.shogun2.util.mail.MailPublisher;
import javassist.NotFoundException;

/**
 *
 * @author Johannes Weskamm
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("momoUserService")
public class MomoUserService<E extends MomoUser, D extends MomoUserDao<E>>
		extends UserService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoUserService() {
		this((Class<E>) MomoUser.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoUserService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Autowired
	private MailPublisher mailPublisher;

	/**
	 *
	 */
	@Autowired
	@Qualifier("userGroupRoleDao")
	private UserGroupRoleDao<UserGroupRole> userGroupRoleDao;

	/**
	 *
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void activateUser(String tokenValue) throws Exception {

		RegistrationToken token = registrationTokenService.findByTokenValue(tokenValue);

		LOG.debug("Trying to activate user account with token: " + tokenValue);

		// throws Exception if token is not valid
		registrationTokenService.validateToken(token);

		// set active=true
		E user = (E) token.getUser();
		user.setActive(true);

		// Add the UserGroupRole for the newly activated user with DEFAULT_USER role.
		if (this.getDefaultUserRole() != null) {
			UserGroupRole userGroupRole = new UserGroupRole();
			userGroupRole.setUser(user);
			userGroupRole.setRole(this.roleService.findByRoleName(
					this.getDefaultUserRole().getName()));

			userGroupRoleDao.saveOrUpdate(userGroupRole);
		}

		// update the user
		dao.saveOrUpdate((E) user);

		// delete the token
		registrationTokenService.deleteTokenAfterActivation(token);

		LOG.info("The user '" + user.getAccountName()
				+ "' has successfully been activated.");
	}

	/**
	 *
	 * @param request
	 * @param email
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 * @throws NotFoundException
	 * @throws AlreadyExistsException
	 */
	public void resendRegistrationTokenMail(HttpServletRequest request, String email)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			UnsupportedEncodingException, URISyntaxException, NotFoundException {

		// Get the user by the provided email address
		User user = dao.findByEmail(email);

		if (user == null) {
			String userNotFoundMsg = "Could not find user with email: '" +
					email + "'";
			LOG.warn(userNotFoundMsg);
			throw new UsernameNotFoundException(userNotFoundMsg);
		}

		if (user.isActive()) {
			String userAlreadyActivatedMsg = "The user is already activated.";
			LOG.warn(userAlreadyActivatedMsg);
			throw new SecurityException(userAlreadyActivatedMsg);
		}

		RegistrationToken registrationToken = registrationTokenService.findByUser(user);

		if (registrationToken == null) {
			String noTokenFoundMsg = "No token found for the requested user.";
			LOG.warn(noTokenFoundMsg);
			throw new NotFoundException(noTokenFoundMsg);
		}

		// Create a thread safe "copy" of the template message
		SimpleMailMessage registrationActivationMsg = new SimpleMailMessage(
				registrationTokenService.getRegistrationMailMessageTemplate());

		// Get the webapp URI
		URI appURI = Shogun2ContextUtil.getApplicationURIFromRequest(request);

		// Build the registration activation link URI
		URI tokenURI = new URIBuilder(appURI)
				.setPath(appURI.getPath() + registrationTokenService.getAccountActivationPath())
				.setParameter("token", registrationToken.getToken())
				.build();

		// Prepare a personalized mail with the given token
		registrationActivationMsg.setTo(email);
		registrationActivationMsg.setText(
				String.format(
						registrationActivationMsg.getText(),
						UriUtils.decode(tokenURI.toString(), "UTF-8")
				)
		);

		// And send the mail
		mailPublisher.sendMail(registrationActivationMsg);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoUserDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

}
