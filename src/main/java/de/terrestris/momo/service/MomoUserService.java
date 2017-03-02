package de.terrestris.momo.service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import de.terrestris.shogun2.dao.UserDao;
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
public class MomoUserService<E extends User, D extends UserDao<E>>
		extends UserService<E, D> {

	@Autowired
	private MailPublisher mailPublisher;

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
}
