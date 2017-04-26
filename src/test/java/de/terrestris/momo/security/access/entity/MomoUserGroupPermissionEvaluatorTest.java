package de.terrestris.momo.security.access.entity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.util.config.MomoConfigHolder;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.helper.IdHelper;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.security.Permission;

/**
 *
 * terrestris GmbH & Co. KG
 *
 * @author Andre Henn
 * @date 18.04.2017
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/test-user-group-roles.xml" })
public class MomoUserGroupPermissionEvaluatorTest {

	private MomoUserGroupPermissionEvaluator<MomoUserGroup> momoUserGroupPermissionEvaluator;

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

	/**
	 *
	 */
	private MomoUser testUser2;

	private MomoUserGroup userGroupToTest;

	@Before
	public void set_up() throws NoSuchFieldException, IllegalAccessException {

		momoUserGroupPermissionEvaluator = new MomoUserGroupPermissionEvaluator<MomoUserGroup>();

		// init security util
		MomoConfigHolder momoConfigHolder = Mockito.mock(MomoConfigHolder.class);
		when(momoConfigHolder.getDefaultUserRoleName()).thenReturn(defaultUserRoleName);
		when(momoConfigHolder.getEditorRoleName()).thenReturn(editorRoleName);
		when(momoConfigHolder.getSubAdminRoleName()).thenReturn(subAdminRoleName);
		when(momoConfigHolder.getSuperAdminRoleName()).thenReturn(superAdminRoleName);
		MomoSecurityUtil.configHolder = momoConfigHolder;

		accessUser = new MomoUser();
		accessUser.setAccountName("Manta");
		IdHelper.setIdOnPersistentObject(accessUser, 1909);

		testUser2 = new MomoUser();
		testUser2.setAccountName("Peter");
		IdHelper.setIdOnPersistentObject(testUser2, 1910);

		userGroupToTest = new MomoUserGroup();
		userGroupToTest.setName("TestGroup");
		IdHelper.setIdOnPersistentObject(userGroupToTest, 190909);
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

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(accessUser, "",
				grantedAuthorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 *
	 */
	private void logoutMockUser() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void hasPermission_shouldAllowCreateForUserWithRoleSuperAdmin() {
		final Permission createPermission = Permission.CREATE;

		final Role roleSuperAdmin = new Role(superAdminRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(roleSuperAdmin);

		loginMockUser(userRoles);

		boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
				createPermission);
		assertTrue("Current ROLE: " + roleSuperAdmin.getName() + " should have " + createPermission.name()
				+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);
	}

	@Test
	public void hasPermission_shouldAllowCreateForUserWithRoleSubAdmin() {
		final Permission createPermission = Permission.CREATE;

		final Role roleSubAdmin = new Role(subAdminRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(roleSubAdmin);

		loginMockUser(userRoles);

		boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
				createPermission);
		assertTrue("Current ROLE: " + roleSubAdmin.getName() + " should have " + createPermission.name()
				+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);
	}

	@Test
	public void hasPermission_shouldDenyCreateForUserWithRoleEditor() {
		final Permission createPermission = Permission.CREATE;

		final Role editorRole = new Role(editorRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(editorRole);

		loginMockUser(userRoles);

		boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
				createPermission);
		assertFalse("Current ROLE: " + editorRole.getName() + " should NOT have " + createPermission.name()
				+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);
	}

	@Test
	public void hasPermission_shouldDenyCreateForUserWithRoleDefaultUser() {
		final Permission createPermission = Permission.CREATE;

		final Role defaultUserRole = new Role(defaultUserRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(defaultUserRole);

		loginMockUser(userRoles);

		boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
				createPermission);
		assertFalse("Current ROLE: " + defaultUserRole.getName() + " should NOT have " + createPermission.name()
				+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);
	}

	@Test
	public void hasPermission_shouldAllowReadForLoggedInUsers() {
		final Permission createPermission = Permission.READ;

		final Role editorRole = new Role(editorRoleName);
		final Role defaultUserRole = new Role(defaultUserRoleName);
		final Role roleSuperAdmin = new Role(superAdminRoleName);
		final Role roleSubAdmin = new Role(subAdminRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(roleSuperAdmin);
		userRoles.add(roleSubAdmin);
		userRoles.add(editorRole);
		userRoles.add(defaultUserRole);

		for (Role currentRole : userRoles) {
			HashSet<Role> currentRoleSet = new HashSet<Role>();
			currentRoleSet.add(currentRole);
			loginMockUser(currentRoleSet);

			boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
					createPermission);
			assertTrue("Current ROLE: " + currentRole.getName() + " should have " + createPermission.name()
					+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);

			logoutMockUser();
		}
	}

	@Test
	public void hasPermission_shouldAllowUpdateForOwnedGroupsOnly() {
		final Permission updatePermission = Permission.UPDATE;

		final Role defaultUserRole = new Role(defaultUserRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(defaultUserRole);

		loginMockUser(userRoles);
		// accessUser is Owner of the group
		userGroupToTest.setOwner(accessUser);

		boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
				updatePermission);
		assertTrue("Current ROLE: " + accessUser.getAccountName() + " should have " + updatePermission.name()
				+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);
	}

	@Test
	public void hasPermission_shouldDenyUpdateForNonOwnedGroups() {
		final Permission updatePermission = Permission.UPDATE;

		final Role defaultUserRole = new Role(defaultUserRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(defaultUserRole);

		loginMockUser(userRoles);
		// accessUser is not owner of the group
		userGroupToTest.setOwner(testUser2);

		boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
				updatePermission);
		assertFalse("Current ROLE: " + accessUser.getAccountName() + " should NOT have " + updatePermission.name()
				+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);
	}

	@Test
	public void hasPermission_shouldAllowDeleteForOwnedGroupsOnly() {
		final Permission deletePermission = Permission.DELETE;

		final Role defaultUserRole = new Role(defaultUserRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(defaultUserRole);

		loginMockUser(userRoles);
		// accessUser is Owner of the group
		userGroupToTest.setOwner(accessUser);

		boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
				deletePermission);
		assertTrue("Current ROLE: " + accessUser.getAccountName() + " should have " + deletePermission.name()
				+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);
	}

	@Test
	public void hasPermission_shouldDenyDeleteForNonOwnedGroups() {
		final Permission deletePermission = Permission.DELETE;

		final Role defaultUserRole = new Role(defaultUserRoleName);
		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(defaultUserRole);

		loginMockUser(userRoles);
		// accessUser is not owner of the group
		userGroupToTest.setOwner(testUser2);

		boolean permissionResult = momoUserGroupPermissionEvaluator.hasPermission(accessUser, userGroupToTest,
				deletePermission);
		assertFalse("Current ROLE: " + accessUser.getAccountName() + " should NOT have " + deletePermission.name()
				+ " permission for Group " + userGroupToTest.getName() + "!", permissionResult);
	}

}
