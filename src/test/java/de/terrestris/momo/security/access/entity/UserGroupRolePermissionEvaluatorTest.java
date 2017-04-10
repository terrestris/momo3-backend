/**
 *
 */
package de.terrestris.momo.security.access.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.service.UserGroupRoleService;
import de.terrestris.momo.util.config.MomoConfigHolder;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.dao.RoleDao;
import de.terrestris.shogun2.helper.IdHelper;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.service.RoleService;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:META-INF/spring/test-user-group-roles.xml"
})
public class UserGroupRolePermissionEvaluatorTest {

	@InjectMocks
	private UserGroupRolePermissionEvaluator<UserGroupRole> userGroupRolePermissionEvaluator;

	@Mock
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	@Mock
	private RoleService<Role, RoleDao<Role>> roleService;

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

	private MomoUser ugrUser;

	private MomoUserGroup ugrGroup;

	@Before
	public void set_up() throws NoSuchFieldException, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		MomoConfigHolder momoConfigHolder = Mockito.mock(MomoConfigHolder.class);
		when(momoConfigHolder.getDefaultUserRoleName()).thenReturn(defaultUserRoleName);
		when(momoConfigHolder.getEditorRoleName()).thenReturn(editorRoleName);
		when(momoConfigHolder.getSubAdminRoleName()).thenReturn(subAdminRoleName);
		when(momoConfigHolder.getSuperAdminRoleName()).thenReturn(superAdminRoleName);

		userGroupRolePermissionEvaluator = new UserGroupRolePermissionEvaluator<UserGroupRole>();
		userGroupRolePermissionEvaluator.setMomoConfigHolder(momoConfigHolder);
		userGroupRolePermissionEvaluator.setUserGroupRoleService(userGroupRoleService);
		userGroupRolePermissionEvaluator.setRoleService(roleService);

		MomoSecurityUtil.configHolder = momoConfigHolder;

		ugrUser = new MomoUser();
		ugrGroup = new MomoUserGroup();
		IdHelper.setIdOnPersistentObject(ugrUser, 42);
		IdHelper.setIdOnPersistentObject(ugrGroup, 43);

		// A User that wants to access the entity.
		accessUser = new MomoUser();
		accessUser.setAccountName("Shinji");
		IdHelper.setIdOnPersistentObject(accessUser, 1909);
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

	/**
	 * ROLE_USER
	 */

	@Test
	public void hasPermission_shouldAlwaysDenyCRUDForDefaultUserIfNotOwned() throws NoSuchFieldException, IllegalAccessException {

		Permission[] permissions = new Permission[] {
			Permission.CREATE,
			Permission.READ,
			Permission.UPDATE,
			Permission.DELETE
		};

		// Prepare a UserGroupRole
		final Role ugrRole = new Role(defaultUserRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, ugrRole);

		IdHelper.setIdOnPersistentObject(ugrRole, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(ugrRole);

		loginMockUser(userRoles);

		for (Permission permission : permissions) {
			boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(accessUser, ugr, permission);
			assertThat(permissionResult, equalTo(false));
		}
	}

	@Test
	public void hasPermission_shouldGrantReadForDefaultUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.READ;

		// prepare a user that
		final Role role = new Role(defaultUserRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldDenyUpdateForDefaultUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.UPDATE;

		// prepare a user that
		final Role role = new Role(defaultUserRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(false));
	}

	@Test
	public void hasPermission_shouldDenyCreateForDefaultUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.CREATE;

		// prepare a user that
		final Role role = new Role(defaultUserRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(false));
	}

	@Test
	public void hasPermission_shouldGrantDeleteForDefaultUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.DELETE;

		// prepare a user that
		final Role role = new Role(defaultUserRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	/**
	 * ROLE_EDITOR
	 */

	@Test
	public void hasPermission_shouldAlwaysDenyCRUDForEditorUserIfNotOwned() throws NoSuchFieldException, IllegalAccessException {

		Permission[] permissions = new Permission[] {
			Permission.CREATE,
			Permission.READ,
			Permission.UPDATE,
			Permission.DELETE
		};

		// Prepare a UserGroupRole
		final Role ugrRole = new Role(editorRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, ugrRole);

		IdHelper.setIdOnPersistentObject(ugrRole, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(ugrRole);

		loginMockUser(userRoles);

		for (Permission permission : permissions) {
			boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(accessUser, ugr, permission);
			assertThat(permissionResult, equalTo(false));
		}
	}

	@Test
	public void hasPermission_shouldGrantReadForEditorUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.READ;

		// prepare a user that
		final Role role = new Role(editorRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldGrantCreateForEditorUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.CREATE;

		// prepare a user that
		final Role role = new Role(editorRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldGrantUpdateForEditorUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.UPDATE;

		// prepare a user that
		final Role role = new Role(editorRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldGrantDeleteForEditorUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.DELETE;

		// prepare a user that
		final Role role = new Role(editorRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	/**
	 * ROLE_SUBADMIN
	 */

	@Test
	public void hasPermission_shouldAlwaysDenyCRUDForSubAdminUserIfNotOwned() throws NoSuchFieldException, IllegalAccessException {

		Permission[] permissions = new Permission[] {
			Permission.CREATE,
			Permission.READ,
			Permission.UPDATE,
			Permission.DELETE
		};

		// Prepare a UserGroupRole
		final Role ugrRole = new Role(subAdminRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, ugrRole);

		IdHelper.setIdOnPersistentObject(ugrRole, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(ugrRole);

		loginMockUser(userRoles);

		for (Permission permission : permissions) {
			boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(accessUser, ugr, permission);
			assertThat(permissionResult, equalTo(false));
		}
	}

	@Test
	public void hasPermission_shouldGrantReadForSubAdminUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.READ;

		// prepare a user that
		final Role role = new Role(subAdminRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldGrantCreateForSubAdminUserIfOwned() throws NoSuchFieldException, IllegalAccessException {

		Permission readPermission = Permission.CREATE;

		// prepare a user that
		final Role role = new Role(subAdminRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldGrantUpdateForSubAdminUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.UPDATE;

		// prepare a user that
		final Role role = new Role(subAdminRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldGrantDeleteForSubAdminUserIfOwned() throws NoSuchFieldException, IllegalAccessException {
		Permission readPermission = Permission.DELETE;

		// prepare a user that
		final Role role = new Role(subAdminRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, readPermission);

		assertThat(permissionResult, equalTo(true));
	}

	@Test
	public void hasPermission_shouldAlwaysGrantCRUDForSubAdminUserIfGroupSubAdmin() throws NoSuchFieldException, IllegalAccessException {

		Permission[] permissions = new Permission[] {
			Permission.CREATE,
			Permission.READ,
			Permission.UPDATE,
			Permission.DELETE
		};

		// Prepare a UserGroupRole
		final Role ugrSubAdminRole = new Role(subAdminRoleName);
		final MomoUserGroup momoUserGroupIsSubAdmin = new MomoUserGroup();
		momoUserGroupIsSubAdmin.setName("BVB");
		final Role ugrEditorRole = new Role(editorRoleName);
		final MomoUserGroup momoUserGroupIsNotSubAdmin = new MomoUserGroup();
		momoUserGroupIsSubAdmin.setName("BVB II");

		final MomoUser notUgrUser = new MomoUser();

		final UserGroupRole isSubAdminUgr = new UserGroupRole(notUgrUser, momoUserGroupIsSubAdmin, ugrSubAdminRole);
		final UserGroupRole isNotSubAdminUgr = new UserGroupRole(ugrUser, momoUserGroupIsNotSubAdmin, ugrEditorRole);

		IdHelper.setIdOnPersistentObject(isSubAdminUgr, 44);
		IdHelper.setIdOnPersistentObject(isNotSubAdminUgr, 45);
		IdHelper.setIdOnPersistentObject(momoUserGroupIsSubAdmin, 46);
		IdHelper.setIdOnPersistentObject(momoUserGroupIsNotSubAdmin, 47);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(ugrSubAdminRole);

		loginMockUser(userRoles);

		for (Permission permission : permissions) {
			boolean permissionResult1 = userGroupRolePermissionEvaluator.hasPermission(ugrUser, isSubAdminUgr, permission);
			boolean permissionResult2 = userGroupRolePermissionEvaluator.hasPermission(ugrUser, isNotSubAdminUgr, permission);
			assertThat(permissionResult1, equalTo(true));
			assertThat(permissionResult2, equalTo(true));

			boolean permissionResult3 = userGroupRolePermissionEvaluator.hasPermission(ugrUser, isNotSubAdminUgr, permission);
			boolean permissionResult4 = userGroupRolePermissionEvaluator.hasPermission(ugrUser, isSubAdminUgr, permission);
			assertThat(permissionResult3, equalTo(true));
			assertThat(permissionResult4, equalTo(true));
		}
	}

	@Test
	public void hasPermission_shouldAlwaysGrantCRUDForSubAdminUserIfGroupIsNull() throws NoSuchFieldException, IllegalAccessException {

		Permission[] permissions = new Permission[] {
			Permission.CREATE,
			Permission.READ,
			Permission.UPDATE,
			Permission.DELETE
		};

		// prepare a user that
		final Role role = new Role(subAdminRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, null, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		for (Permission permission : permissions) {
			boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, permission);
			assertThat(permissionResult, equalTo(true));
		}
	}

	@Test
	public void hasPermission_shouldAlwaysGrantCRUDForSupAdminUser() throws NoSuchFieldException, IllegalAccessException {

		Permission[] permissions = new Permission[] {
			Permission.CREATE,
			Permission.READ,
			Permission.UPDATE,
			Permission.DELETE
		};

		// prepare a user that
		final Role role = new Role(superAdminRoleName);
		final UserGroupRole ugr = new UserGroupRole(ugrUser, ugrGroup, role);

		IdHelper.setIdOnPersistentObject(role, 44);
		IdHelper.setIdOnPersistentObject(ugr, 45);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(role);

		loginMockUser(userRoles);

		for (Permission permission : permissions) {
			boolean permissionResult = userGroupRolePermissionEvaluator.hasPermission(ugrUser, ugr, permission);
			assertThat(permissionResult, equalTo(true));
		}
	}
}
