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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.dao.PasswordResetTokenDao;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.token.PasswordResetToken;
import de.terrestris.shogun2.service.AbstractUserTokenService;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.application.Shogun2ContextUtil;
import de.terrestris.shogun2.util.mail.MailPublisher;

/**
 *
 * @author Daniel Koch
 * @author Nils Bühner
 *
 */
@Service("momoPasswordResetTokenService")
public class MomoPasswordResetTokenService<E extends PasswordResetToken, D extends PasswordResetTokenDao<E>>
		extends AbstractUserTokenService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoPasswordResetTokenService() {
		this((Class<E>) PasswordResetToken.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoPasswordResetTokenService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Autowired
	private UserService<User, UserDao<User>> userService;

	/**
	 *
	 */
	@Autowired
	private UserDao<User> userDao;

	/**
	 *
	 */
	@Autowired
	private MailPublisher mailPublisher;

	/**
	 * The autowired PasswordEncoder
	 */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 *
	 */
	@Autowired
	@Qualifier("resetPasswordMailMessageTemplate-en")
	private SimpleMailMessage resetPasswordMailMessageTemplate_en;

	/**
	 *
	 */
	@Autowired
	@Qualifier("resetPasswordMailMessageTemplate-mn")
	private SimpleMailMessage resetPasswordMailMessageTemplate_mn;

	/**
	 *
	 */
	@Autowired
	@Qualifier("resetPasswordMailMessageTemplate-de")
	private SimpleMailMessage resetPasswordMailMessageTemplate_de;

	/**
	 *
	 */
	@Autowired
	private String changePasswordPath;

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("passwordResetTokenDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	/**
	 * Builds a concrete instance of this class.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected E buildConcreteInstance(User user, Integer expirationTimeInMinutes) {
		if(expirationTimeInMinutes == null) {
			return (E) new PasswordResetToken(user);
		}
		return (E) new PasswordResetToken(user, expirationTimeInMinutes);
	}

	/**
	 * @param request
	 * @param email
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	@Transactional(readOnly = true)
	public void sendResetPasswordMail(HttpServletRequest request, String email)
			throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			URISyntaxException, UnsupportedEncodingException {

		// get the user by the provided email address
		User user = userDao.findByEmail(email);

		if (user == null) {
			throw new UsernameNotFoundException(
					"Could not find user with email: '" + email + "'");
		}

		// generate and save the unique reset-password token for the user
		PasswordResetToken resetPasswordToken = getValidTokenForUser(user, null);

		// create the reset-password URI that will be send to the user
		URI resetPasswordURI = createResetPasswordURI(request,
				resetPasswordToken);

		String lang = "en";
		if (user.getLanguage() != null) {
			lang = user.getLanguage().toLanguageTag();
		}

		SimpleMailMessage resetPwdMsg = null;
		// Create a thread safe "copy" of the template message, depending on the users language
		if (lang.equals("de")) {
			resetPwdMsg = new SimpleMailMessage(
					getResetPasswordMailMessageTemplate_de()
			);
		} else if (lang.equals("mn")) {
			resetPwdMsg = new SimpleMailMessage(
					getResetPasswordMailMessageTemplate_mn()
			);
		} else {
			resetPwdMsg = new SimpleMailMessage(
					getResetPasswordMailMessageTemplate_en()
			);
		}

		// prepare a personalized mail with the given token
		resetPwdMsg.setTo(email);
		resetPwdMsg.setText(
				String.format(
						resetPwdMsg.getText(),
						user.getFirstName(),
						user.getLastName(),
						UriUtils.decode(resetPasswordURI.toString(), "UTF-8")
				)
		);

		// and send the mail
		mailPublisher.sendMail(resetPwdMsg);

	}

	/**
	 * @param rawPassword
	 * @param token
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void validateTokenAndUpdatePassword(String rawPassword, String token) throws Exception {

		// try to find the provided token
		PasswordResetToken passwordResetToken = findByTokenValue(token);

		// this would throw an exception if the token is not valid
		this.validateToken((E) passwordResetToken);

		// the user's password can be changed now

		// get the user of the provided token
		User user = passwordResetToken.getUser();

		if(!MomoSecurityUtil.isValidPassword(rawPassword)) {
			throw new Exception("The given password does not fulfill the security requirements.");
		}

		// finally update the password (encrypted)
		userService.updatePassword(user, rawPassword);

		// delete the token
		dao.delete((E) passwordResetToken);

		LOG.trace("Deleted the token.");
		LOG.debug("Successfully updated the password.");

	}

	/**
	 *
	 * @param request
	 * @param resetPasswordToken
	 * @return
	 * @throws URISyntaxException
	 */
	@Transactional(readOnly = true)
	private URI createResetPasswordURI(HttpServletRequest request,
			PasswordResetToken resetPasswordToken) throws URISyntaxException {

		// get the webapp URI
		URI appURI = Shogun2ContextUtil.getApplicationURIFromRequest(request);

		// build the change-password URI send to the user
		URI tokenURI = new URIBuilder(appURI)
				.setPath(appURI.getPath() + changePasswordPath)
				.setParameter("token", resetPasswordToken.getToken())
				.build();

		return tokenURI;
	}

	/**
	 * @return the mailPublisher
	 */
	public MailPublisher getMailPublisher() {
		return mailPublisher;
	}

	/**
	 * @param mailPublisher the mailPublisher to set
	 */
	public void setMailPublisher(MailPublisher mailPublisher) {
		this.mailPublisher = mailPublisher;
	}

	/**
	 * @return the passwordEncoder
	 */
	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	/**
	 * @param passwordEncoder the passwordEncoder to set
	 */
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * @return the changePasswordPath
	 */
	public String getChangePasswordPath() {
		return changePasswordPath;
	}

	/**
	 * @param changePasswordPath the changePasswordPath to set
	 */
	public void setChangePasswordPath(String changePasswordPath) {
		this.changePasswordPath = changePasswordPath;
	}

	/**
	 * @return the resetPasswordMailMessageTemplate_en
	 */
	public SimpleMailMessage getResetPasswordMailMessageTemplate_en() {
		return resetPasswordMailMessageTemplate_en;
	}

	/**
	 * @param resetPasswordMailMessageTemplate_en the resetPasswordMailMessageTemplate_en to set
	 */
	public void setResetPasswordMailMessageTemplate_en(SimpleMailMessage resetPasswordMailMessageTemplate_en) {
		this.resetPasswordMailMessageTemplate_en = resetPasswordMailMessageTemplate_en;
	}

	/**
	 * @return the resetPasswordMailMessageTemplate_mn
	 */
	public SimpleMailMessage getResetPasswordMailMessageTemplate_mn() {
		return resetPasswordMailMessageTemplate_mn;
	}

	/**
	 * @param resetPasswordMailMessageTemplate_mn the resetPasswordMailMessageTemplate_mn to set
	 */
	public void setResetPasswordMailMessageTemplate_mn(SimpleMailMessage resetPasswordMailMessageTemplate_mn) {
		this.resetPasswordMailMessageTemplate_mn = resetPasswordMailMessageTemplate_mn;
	}

	/**
	 * @return the resetPasswordMailMessageTemplate_de
	 */
	public SimpleMailMessage getResetPasswordMailMessageTemplate_de() {
		return resetPasswordMailMessageTemplate_de;
	}

	/**
	 * @param resetPasswordMailMessageTemplate_de the resetPasswordMailMessageTemplate_de to set
	 */
	public void setResetPasswordMailMessageTemplate_de(SimpleMailMessage resetPasswordMailMessageTemplate_de) {
		this.resetPasswordMailMessageTemplate_de = resetPasswordMailMessageTemplate_de;
	}

}
