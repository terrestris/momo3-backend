/**
 *
 */
package de.terrestris.momo.security.access.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.service.UserGroupRoleService;
import de.terrestris.momo.util.config.MomoConfigHolder;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.helper.IdHelper;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.security.PermissionCollection;

/**
 *
 * @author Daniel Koch
 * @author Andre Henn
 * @author terrestris GmbH & Co. KG
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:META-INF/spring/test-user-group-roles.xml"
})
public class MomoApplicationPermissionEvaluatorTest {

	@InjectMocks
	private MomoApplicationPermissionEvaluator<MomoApplication> momoApplicationPermissionEvaluator;

	@Value("${role.defaultUserRoleName:}")
	private String defaultUserRoleName;

	@Value("${role.editorRoleName:}")
	private String editorRoleName;

	@Value("${role.subAdminRoleName:}")
	private String subAdminRoleName;

	@Value("${role.superAdminRoleName:}")
	private String superAdminRoleName;

	/**
	 *
	 */
	private MomoUser accessUser;
	private MomoApplication testApplication;
	private MomoUserGroup testGroup;

	@Before
	public void set_up() throws NoSuchFieldException, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		MomoConfigHolder momoConfigHolder = Mockito.mock(MomoConfigHolder.class);
		when(momoConfigHolder.getDefaultUserRoleName()).thenReturn(defaultUserRoleName);
		when(momoConfigHolder.getEditorRoleName()).thenReturn(editorRoleName);
		when(momoConfigHolder.getSubAdminRoleName()).thenReturn(subAdminRoleName);
		when(momoConfigHolder.getSuperAdminRoleName()).thenReturn(superAdminRoleName);
		MomoSecurityUtil.configHolder = momoConfigHolder;

		momoApplicationPermissionEvaluator = new MomoApplicationPermissionEvaluator<MomoApplication>();


		// A User that wants to access the application.
		accessUser = new MomoUser();
		accessUser.setAccountName("Shinji");
		IdHelper.setIdOnPersistentObject(accessUser, 1909);

		// A Group that wants to access the application.
		testGroup = new MomoUserGroup();
		testGroup.setName("BVB");
		IdHelper.setIdOnPersistentObject(testGroup, 19);

		testApplication = new MomoApplication();
		testApplication.setOpen(false);
		IdHelper.setIdOnPersistentObject(testApplication, 191909);
	}

	@After
	public void clean_up() {
		logoutMockUser();
	}

	/**
	 *
	 * @param userRoles
	 */
	private void loginMockUser(Set<Role> userRoles) {
		Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();

		for (Role userRole : userRoles) {
			grantedAuthorities.add(new SimpleGrantedAuthority(userRole.getName()));
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				accessUser, "", grantedAuthorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 *
	 */
	private void logoutMockUser() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void hasPermission_shouldDenyWithoutUserNorGroupPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.READ);
		readPermission.add(Permission.CREATE);
		readPermission.add(Permission.UPDATE);
		readPermission.add(Permission.DELETE);

		for (Permission permission : readPermission) {
			boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, permission);
			assertThat(permissionResult, equalTo(false));
		}
	}

	@Test
	public void hasPermission_shouldAllowReadForUserGrantedFromUserPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.READ);

		PermissionCollection permCollection = new PermissionCollection();
		permCollection.setPermissions(readPermission);
		IdHelper.setIdOnPersistentObject(permCollection, 333);

		HashMap<User, PermissionCollection> userPermissions = new HashMap<User, PermissionCollection>();
		userPermissions.put(accessUser, permCollection);
		testApplication.setUserPermissions(userPermissions);

		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, Permission.READ);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowUpdateForUserGrantedFromUserPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.UPDATE);

		PermissionCollection permCollection = new PermissionCollection();
		permCollection.setPermissions(readPermission);
		IdHelper.setIdOnPersistentObject(permCollection, 333);

		HashMap<User, PermissionCollection> userPermissions = new HashMap<User, PermissionCollection>();
		userPermissions.put(accessUser, permCollection);
		testApplication.setUserPermissions(userPermissions);

		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, Permission.UPDATE);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowDeleteForUserGrantedFromUserPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.DELETE);

		PermissionCollection permCollection = new PermissionCollection();
		permCollection.setPermissions(readPermission);
		IdHelper.setIdOnPersistentObject(permCollection, 333);

		HashMap<User, PermissionCollection> userPermissions = new HashMap<User, PermissionCollection>();
		userPermissions.put(accessUser, permCollection);
		testApplication.setUserPermissions(userPermissions);

		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, Permission.DELETE);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowCreateForUserGrantedFromUserPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.CREATE);

		PermissionCollection permCollection = new PermissionCollection();
		permCollection.setPermissions(readPermission);
		IdHelper.setIdOnPersistentObject(permCollection, 333);

		HashMap<User, PermissionCollection> userPermissions = new HashMap<User, PermissionCollection>();
		userPermissions.put(accessUser, permCollection);
		testApplication.setUserPermissions(userPermissions);

		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, Permission.CREATE);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowDeleteForUserGrantedFromGroupPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.DELETE);

		PermissionCollection permCollection = new PermissionCollection();
		permCollection.setPermissions(readPermission);
		IdHelper.setIdOnPersistentObject(permCollection, 333);

		HashMap<UserGroup, PermissionCollection> groupPermissions = new HashMap<UserGroup, PermissionCollection>();
		groupPermissions.put(testGroup, permCollection);
		testApplication.setGroupPermissions(groupPermissions);

		UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService =
				Mockito.mock(UserGroupRoleService.class);
		when(userGroupRoleService.isUserMemberInUserGroup(accessUser, testGroup)).thenReturn(true);
		momoApplicationPermissionEvaluator.setUserGroupRoleService(userGroupRoleService);

		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, Permission.DELETE);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowUpdateForUserGrantedFromGroupPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.UPDATE);

		PermissionCollection permCollection = new PermissionCollection();
		permCollection.setPermissions(readPermission);
		IdHelper.setIdOnPersistentObject(permCollection, 333);

		HashMap<UserGroup, PermissionCollection> groupPermissions = new HashMap<UserGroup, PermissionCollection>();
		groupPermissions.put(testGroup, permCollection);
		testApplication.setGroupPermissions(groupPermissions);

		UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService =
				Mockito.mock(UserGroupRoleService.class);
		when(userGroupRoleService.isUserMemberInUserGroup(accessUser, testGroup)).thenReturn(true);
		momoApplicationPermissionEvaluator.setUserGroupRoleService(userGroupRoleService);

		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, Permission.UPDATE);
		assertThat(permissionResult, equalTo(true));
	}


	@Test
	public void hasPermission_shouldAllowCreateForUserGrantedFromGroupPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.CREATE);

		PermissionCollection permCollection = new PermissionCollection();
		permCollection.setPermissions(readPermission);
		IdHelper.setIdOnPersistentObject(permCollection, 333);

		HashMap<UserGroup, PermissionCollection> groupPermissions = new HashMap<UserGroup, PermissionCollection>();
		groupPermissions.put(testGroup, permCollection);
		testApplication.setGroupPermissions(groupPermissions);

		UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService =
				Mockito.mock(UserGroupRoleService.class);
		when(userGroupRoleService.isUserMemberInUserGroup(accessUser, testGroup)).thenReturn(true);
		momoApplicationPermissionEvaluator.setUserGroupRoleService(userGroupRoleService);
		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, Permission.CREATE);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowReadForUserGrantedFromGroupPermissions() throws NoSuchFieldException, IllegalAccessException {
		Set<Permission> readPermission = new HashSet<Permission>();
		readPermission.add(Permission.READ);

		PermissionCollection permCollection = new PermissionCollection();
		permCollection.setPermissions(readPermission);
		IdHelper.setIdOnPersistentObject(permCollection, 333);

		HashMap<UserGroup, PermissionCollection> groupPermissions = new HashMap<UserGroup, PermissionCollection>();
		groupPermissions.put(testGroup, permCollection);
		testApplication.setGroupPermissions(groupPermissions);

		UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService =
				Mockito.mock(UserGroupRoleService.class);
		when(userGroupRoleService.isUserMemberInUserGroup(accessUser, testGroup)).thenReturn(true);
		momoApplicationPermissionEvaluator.setUserGroupRoleService(userGroupRoleService);

		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, testApplication, Permission.READ);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowCreateForUsersWithRoleSubAdmin() {
		Role subAdminRole = new Role(subAdminRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(subAdminRole);

		loginMockUser(userRoles);
		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, null, Permission.CREATE);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowCreateForUsersWithRoleSuperAdmin() {
		Role superAdminRole = new Role(superAdminRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(superAdminRole);

		loginMockUser(userRoles);
		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, null, Permission.CREATE);
		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAllowCreateForUsersWithRoleEditor() {
		Role editorRole = new Role(editorRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(editorRole);

		loginMockUser(userRoles);
		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, null, Permission.CREATE);
		assertThat(permissionResult, equalTo(false));
	}

	@Test
	public void hasPermission_shouldAllowCreateForUsersWithRoleDefaultUser() {
		Role defaultUserRole = new Role(defaultUserRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(defaultUserRole);

		loginMockUser(userRoles);
		boolean permissionResult = momoApplicationPermissionEvaluator.hasPermission(accessUser, null, Permission.CREATE);
		assertThat(permissionResult, equalTo(false));
	}

}
