package de.terrestris.momo.service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.dao.MomoUserDao;
import de.terrestris.momo.dao.MomoUserGroupDao;
import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.dao.GenericHibernateDao;
import de.terrestris.shogun2.dao.RegistrationTokenDao;
import de.terrestris.shogun2.dao.RoleDao;
import de.terrestris.shogun2.dao.UserGroupDao;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.security.PermissionCollection;
import de.terrestris.shogun2.model.token.RegistrationToken;
import de.terrestris.shogun2.service.PermissionAwareCrudService;
import de.terrestris.shogun2.service.RoleService;
import de.terrestris.shogun2.service.UserService;
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
	 *
	 */
	@Autowired
	@Qualifier("momoLayerDao")
	private MomoLayerDao<MomoLayer> layerDao;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoApplicationDao")
	private MomoApplicationDao<MomoApplication> applicationDao;

	/**
	 * Role service
	 */
	@Autowired
	@Qualifier("roleService")
	protected RoleService<Role, RoleDao<Role>> roleService;

	@Autowired
	@Qualifier("permissionAwareCrudService")
	private PermissionAwareCrudService<PersistentObject, GenericHibernateDao<PersistentObject, Integer>> permissionAwareCrudService;

	@Autowired
	@Qualifier("momoRegistrationTokenService")
	private MomoRegistrationTokenService<RegistrationToken, RegistrationTokenDao<RegistrationToken>> momoRegistrationTokenService;

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
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void activateUser(String tokenValue) throws Exception {

		RegistrationToken token = momoRegistrationTokenService.findByTokenValue(tokenValue);

		LOG.debug("Trying to activate user account with token: " + tokenValue);

		// throws Exception if token is not valid
		momoRegistrationTokenService.validateToken(token);

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
		momoRegistrationTokenService.deleteTokenAfterActivation(token);

		LOG.info("The user '" + user.getAccountName()
				+ "' has successfully been activated.");
	}

	/**
	 * Registers a new user. Initially, the user will be inactive. An email with
	 * an activation link will be sent to the user.
	 *
	 * @param user A user with an UNencrypted password (!)
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@Override
	public E registerUser(E user, HttpServletRequest request) throws Exception {

		String email = user.getEmail();

		// check if a user with the email already exists
		E existingUser = dao.findByEmail(email);

		if(existingUser != null) {
			final String errorMessage = "User with eMail '" + email + "' already exists.";
			LOG.info(errorMessage);
			throw new Exception(errorMessage);
		}

		user = (E) this.persistNewUser(user, true);

		// create a token for the user and send an email with an "activation" link
		momoRegistrationTokenService.sendRegistrationActivationMail(request, user);

		return user;
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
	@Transactional(readOnly = true)
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

		RegistrationToken registrationToken = momoRegistrationTokenService.findByUser(user);

		if (registrationToken == null) {
			String noTokenFoundMsg = "No token found for the requested user.";
			LOG.warn(noTokenFoundMsg);
			throw new NotFoundException(noTokenFoundMsg);
		}

		momoRegistrationTokenService.sendRegistrationActivationMail(request, user);
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
	public void updateUser(Integer userId, String firstName,String lastName,String email,String telephone,String department,
			String profileImage, String language, HashMap<String,String> permissions) {
		if (userId == null) {
			throw new IllegalArgumentException("No userId to update given");
		}
		E updatingUser = getUserBySession();
		E userToUpdate = findById(userId);

		if (!updatingUser.equals(userToUpdate)) {
			throw new AccessDeniedException("Users can only update themselves");
		}

		userToUpdate.setFirstName(firstName);
		userToUpdate.setLastName(lastName);
		userToUpdate.setEmail(email);
		userToUpdate.setTelephone(telephone);
		userToUpdate.setDepartment(department);
		userToUpdate.setProfileImage(profileImage);
		userToUpdate.setLanguage(new Locale(language));

		dao.saveOrUpdate(userToUpdate);

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
						sendPermissionChangeMail(superadmin, wantedRole, group, userToUpdate);
					}
				} else if (wantedRole.equals("ROLE_EDITOR") ||
						wantedRole.equals("ROLE_USER") ||
						wantedRole.equals("REMOVE")) {
					//send mail to subadmin, or, if not available, to the superadmin
					sendMailToSubadminOrSuperadmin(subadminForGroup, superadmin, wantedRole, group, userToUpdate);
				}
			}
		}
	}

	/**
	 * Deletes a user and changes ownership of its entities to superadmin
	 * @throws UnavailableException
	 *
	 */
	public void deleteUser(Integer userId) throws UnavailableException {

		LOG.info("Trying to delete a user");

		E deletingUser = getUserBySession();
		E userToDelete = findById(userId);
		if (userToDelete == null) {
			throw new RuntimeException("User to delete could not be found");
		}

		// check if current user may delete the user
		Set<Role> rolesOfDeletingUser = userGroupRoleService.findAllUserRoles(deletingUser);
		Set<Role> rolesOfUserToDelete = userGroupRoleService.findAllUserRoles(userToDelete);
		Role adminRole = roleService.findByRoleName("ROLE_ADMIN");
		MomoUser adminUser = null;
		List<E> allUsers = findAll();
		for (MomoUser user : allUsers) {
			Set<Role> roles = userGroupRoleService.findAllUserRoles(user);
			if (roles.contains(adminRole)) {
				adminUser = user;
				break;
			}
		}

		if (adminUser == null) {
			throw new UnavailableException("Could not find the superadmin, aborting");
		}

		boolean deletingUserIsAdmin = rolesOfDeletingUser.contains(adminRole);
		boolean userToDeleteIsAdmin = rolesOfUserToDelete.contains(adminRole);
		if (!userToDelete.equals(deletingUser) &&
				!deletingUserIsAdmin) {
			throw new AccessDeniedException("Access is denied");
		}

		if (userToDeleteIsAdmin) {
			throw new AccessDeniedException("The Superadmin may not be deleted");
		}

		List<UserGroupRole> userGroupRoles = userGroupRoleService.findUserGroupRolesBy(userToDelete);
		for (UserGroupRole userGroupRole : userGroupRoles) {
			userGroupRoleService.delete(userGroupRole);
			LOG.debug("Deleted a user group role");
		}

		// Delete all remaining PermissionCollections for this user, e.g. webmap
		Map<PersistentObject, PermissionCollection> entityPermissionCollectionsForUser =
				dao.findAllUserPermissionsOfUser(userToDelete);
		Set<PersistentObject> entitiesWithPermissions = entityPermissionCollectionsForUser.keySet();

		for (PersistentObject persistentObject : entitiesWithPermissions) {
			// INFO: The hashcode of the persistentObject differs here, thats why we cannot use a call like
			// PermissionCollection permissionsOnEntity = entityPermissionCollectionsForUser.get(persistentObject);
			// to get the collection. So we get it by matching classname and id
			Set<Entry<PersistentObject, PermissionCollection>> entries = entityPermissionCollectionsForUser.entrySet();
			for (Entry<PersistentObject, PermissionCollection> entry : entries) {
				if (entry.getKey().getId().equals(persistentObject.getId()) &&
					entry.getKey().getClass().equals(persistentObject.getClass())) {
					PermissionCollection permissionsOnEntity = entry.getValue();
					Set<Permission> permissionsSet = permissionsOnEntity.getPermissions();
					Permission[] permissionsArray = permissionsSet.toArray(new Permission[permissionsSet.size()]);
					permissionAwareCrudService.removeAndSaveUserPermissions(persistentObject, userToDelete, permissionsArray);
					LOG.debug("Removed a permission collection for the user");
					break;
				}
			}
		}

		// remove all registration token entries
		RegistrationToken token = registrationTokenService.findByUser(userToDelete);
		if (token != null) {
			registrationTokenService.deleteTokenAfterActivation(token);
			LOG.debug("Deleted a RegistrationToken of a user");
		}

		// reown all layers of user
		final SimpleExpression isOwner = Restrictions.eq("owner", userToDelete);
		List<MomoLayer> usersLayers = layerDao.findByCriteria(isOwner);
		for (MomoLayer momoLayer : usersLayers) {
			momoLayer.setOwner(adminUser);
			LOG.info("A layer ownership has been moved to the superadmin");
		}

		// reown all applications of user
		List<MomoApplication> usersApplications = applicationDao.findByCriteria(isOwner);
		for (MomoApplication momoApp : usersApplications) {
			momoApp.setOwner(adminUser);
			LOG.info("An application ownership has been moved to the superadmin");
		}

		// reown all applications of user
		List<MomoUserGroup> usersGroups = momoUserGroupDao.findByCriteria(isOwner);
		for (MomoUserGroup momoGroup : usersGroups) {
			momoGroup.setOwner(adminUser);
			LOG.info("A group ownership has been moved to the superadmin");
		}

		dao.delete((E) userToDelete);
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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
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
	 *
	 * @param oldPassword
	 * @param newPassword
	 * @return
	 * @throws Exception 
	 */
	@PreAuthorize("isAuthenticated()")
	public boolean updatePassword(String oldPassword, String newPassword) throws Exception {
		E currentUser = this.getUserBySession();

		String encodedOldPassword = currentUser.getPassword();
		if(passwordEncoder.matches(oldPassword, encodedOldPassword) && MomoSecurityUtil.isValidPassword(newPassword)) {
			this.updatePassword(currentUser, newPassword);
			return true;
		}

		return false;
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
	 * @return the momoRegistrationTokenService
	 */
	public MomoRegistrationTokenService<RegistrationToken, RegistrationTokenDao<RegistrationToken>> getMomoRegistrationTokenService() {
		return momoRegistrationTokenService;
	}

	/**
	 * @param momoRegistrationTokenService the momoRegistrationTokenService to set
	 */
	public void setMomoRegistrationTokenService(
			MomoRegistrationTokenService<RegistrationToken, RegistrationTokenDao<RegistrationToken>> momoRegistrationTokenService) {
		this.momoRegistrationTokenService = momoRegistrationTokenService;
	}

}
