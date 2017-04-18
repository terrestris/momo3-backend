package de.terrestris.momo.service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import de.terrestris.momo.dao.MomoUserDao;
import de.terrestris.momo.dao.MomoUserGroupDao;
import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.shogun2.dao.RoleDao;
import de.terrestris.shogun2.dao.UserGroupDao;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;
import de.terrestris.shogun2.model.token.RegistrationToken;
import de.terrestris.shogun2.service.RoleService;
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

	@Autowired
	@Qualifier("userGroupRoleService")
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("userGroupDao")
	private UserGroupDao<UserGroup> userGroupDao;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoUserGroupDao")
	private MomoUserGroupDao<MomoUserGroup> momoUserGroupDao;

	/**
	 * Role service
	 */
	@Autowired
	protected RoleService<Role, RoleDao<Role>> roleService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplate-en")
	private SimpleMailMessage changePermissionsMailMessageTemplate_en;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplate-mn")
	private SimpleMailMessage changePermissionsMailMessageTemplate_mn;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplate-de")
	private SimpleMailMessage changePermissionsMailMessageTemplate_de;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplateForUser-en")
	private SimpleMailMessage changePermissionsMailMessageTemplateForUser_en;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplateForUser-mn")
	private SimpleMailMessage changePermissionsMailMessageTemplateForUser_mn;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplateForUser-de")
	private SimpleMailMessage changePermissionsMailMessageTemplateForUser_de;

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
	 * Updates the users personal credentials and, if a change in permissions
	 * is made, will contact a subadmin / superadmin to make the appropriate changes
	 *
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param telephone
	 * @param department
	 * @param profileImage
	 * @param language
	 * @param permissions
	 */
	@SuppressWarnings("unchecked")
	public void updateUser(String firstName,String lastName,String email,String telephone,String department,
			String profileImage, String language, HashMap<String,String> permissions) {
		E user = getUserBySession();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setTelephone(telephone);
		user.setDepartment(department);
		user.setProfileImage(profileImage);
		user.setLanguage(new Locale(language));

		dao.saveOrUpdate(user);

		// now handle the permission changes
		Role subadminRole = roleService.findByRoleName("ROLE_SUBADMIN");
		Role superadminRole = roleService.findByRoleName("ROLE_ADMIN");
		MomoUser superadmin = null;
		List<MomoUser> allUsers = (List<MomoUser>) dao.findAll();
		for (MomoUser momoUser : allUsers) {
			Set<Role> roles = userGroupRoleService.findAllUserRoles(momoUser);
			if (roles.contains(superadminRole)) {
				superadmin = momoUser;
			}
		}

		for (Entry<String, String> entry : permissions.entrySet()) {
			Integer groupId = Integer.valueOf(entry.getKey());
			String wantedRole = entry.getValue();

			// 1.) If user wants to become subadmin -> ask the superadmin for permission
			// 2.) If user wants to become editor -> ask the subadmin for permission
			// 3.) If user wants to become user -> ask the subadmin for permission
			// 4.) If user removed all rights in group -> ask the subadmin for permission
			// 4.) If no subadmin found for case 3., 4. and 5. -> ask the superadmin for permission
			MomoUserGroup group = momoUserGroupDao.findById(groupId);

			if (group != null) {
				MomoUser subadminForGroup = null;
				Set<MomoUser> momoUsers = userGroupRoleService.findAllUserGroupMembers(group);
				for (MomoUser momoUser : momoUsers) {
					if (userGroupRoleService.hasUserRoleInGroup(momoUser, group, subadminRole)) {
						subadminForGroup = momoUser;
					}
				}
				if (wantedRole.equals("ROLE_SUBADMIN")) {
					//sendmail to superadmin
					if (superadmin != null) {
						sendPermissionChangeMail(superadmin, wantedRole, group, user);
					}
				} else if (wantedRole.equals("ROLE_EDITOR") ||
						wantedRole.equals("ROLE_USER") ||
						wantedRole.equals("REMOVE")) {
					//send mail to subadmin, or, if not available, to the superadmin
					sendMailToSubadminOrSuperadmin(subadminForGroup, superadmin, wantedRole, group, user);
				}
			}
		}
	}

	/**
	 * Sends mail to a subadmin of the group and, if not available, to the
	 * superadmin as fallback
	 *
	 * @param subadminForGroup
	 * @param superadmin
	 * @param wantedRole
	 * @param group
	 * @param user
	 */
	public void sendMailToSubadminOrSuperadmin(MomoUser subadminForGroup, MomoUser superadmin,
			String wantedRole, MomoUserGroup group, MomoUser user) {
		if (subadminForGroup != null) {
			//sendmail to subadmin
			sendPermissionChangeMail(subadminForGroup, wantedRole, group, user);
		} else if (superadmin != null) {
			//sendmail to superadmin
			sendPermissionChangeMail(superadmin, wantedRole, group, user);
		} else {
			throw new RuntimeException("Could neither find a subadmin, nor a superadmin!");
		}
	}

	/**
	 * Sends the final mail to the subadmin or superadmin with the request
	 * for changed permissions. Language of the receiver is taken into account
	 *
	 * @param receiver
	 * @param wantedRole
	 * @param group
	 * @param user
	 */
	public void sendPermissionChangeMail(MomoUser receiver, String wantedRole,
			MomoUserGroup group, MomoUser user) {

		String lang = "en";
		if (receiver.getLanguage() != null) {
			lang = receiver.getLanguage().toLanguageTag();
		}
		String email = receiver.getEmail();
		if (email == null) {
			throw new RuntimeException("User has no mailadress attached, cancelled sending of mail");
		}

		SimpleMailMessage changePermissionMailTemplateMsg = null;
		// Create a thread safe "copy" of the template message, depending on the users language
		if (lang.equals("de")) {
			changePermissionMailTemplateMsg = new SimpleMailMessage(
					getChangePermissionsMailMessageTemplate_de()
			);
		} else if (lang.equals("mn")) {
			changePermissionMailTemplateMsg = new SimpleMailMessage(
					getChangePermissionsMailMessageTemplate_mn()
			);
		} else {
			changePermissionMailTemplateMsg = new SimpleMailMessage(
					getChangePermissionsMailMessageTemplate_en()
			);
		}

		// Prepare a personalized mail in the correct language
		changePermissionMailTemplateMsg.setTo(email);
		changePermissionMailTemplateMsg.setText(
				String.format(
						changePermissionMailTemplateMsg.getText(),
						receiver.getFirstName(),
						receiver.getLastName(),
						group.getName(),
						wantedRole,
						user.getFirstName(),
						user.getLastName(),
						user.getEmail(),
						user.getDepartment(),
						user.getTelephone()
				)
		);
		// and send the mail
		mailPublisher.sendMail(changePermissionMailTemplateMsg);
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

	/**
	 * @return the changePermissionsMailMessageTemplate_en
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplate_en() {
		return changePermissionsMailMessageTemplate_en;
	}

	/**
	 * @return the changePermissionsMailMessageTemplate_mn
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplate_mn() {
		return changePermissionsMailMessageTemplate_mn;
	}

	/**
	 * @return the changePermissionsMailMessageTemplate_de
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplate_de() {
		return changePermissionsMailMessageTemplate_de;
	}

	/**
	 * @return the changePermissionsMailMessageTemplateForUser_en
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplateForUser_en() {
		return changePermissionsMailMessageTemplateForUser_en;
	}

	/**
	 * @return the changePermissionsMailMessageTemplateForUser_mn
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplateForUser_mn() {
		return changePermissionsMailMessageTemplateForUser_mn;
	}

	/**
	 * @return the changePermissionsMailMessageTemplateForUser_de
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplateForUser_de() {
		return changePermissionsMailMessageTemplateForUser_de;
	}

}
