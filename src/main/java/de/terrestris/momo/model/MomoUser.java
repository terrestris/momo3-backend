package de.terrestris.momo.model;

import java.util.Set;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.terrestris.momo.util.serializer.MomoUserSerializer;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;

/**
 *
 * @author Daniel Koch
 * @author Andre Henn
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@JsonSerialize(using = MomoUserSerializer.class)
public class MomoUser extends User {

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(MomoUser.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor
	 */
	public MomoUser() {
	}

	/**
	 *
	 */
	private String department;

	/**
	 *
	 */
	private String telephone;

	/**
	 *
	 */
	private String profileImage;

	/**
	 *
	 */
	@Override
	@Deprecated
	public Set<UserGroup> getUserGroups() {
		LOG.warn("The method getUserGroups() returns null always. "
				+ "To get the groups of a certain user, please make "
				+ "use of the UserGroupRoleService, e.g. findAllUserGroupMembers()");
		return null;
	}

	/**
	 *
	 */
	@Override
	@Deprecated
	public void setUserGroups(Set<UserGroup> userGroups) {
		LOG.warn("Calling the method setUserGroups() has no effect. "
				+ "To set the members of a certain group, please make "
				+ "use of the UserGroupRoleService.");
	}

	/**
	 * @return the roles
	 */
	@Override
	@Deprecated
	public Set<Role> getRoles() {
		LOG.warn("The method getRoles() returns null always. "
				+ "To get the roles of a certain user, please make "
				+ "use of the UserGroupRoleService, e.g. findAllUserRoles()");
		return null;
	}

	/**
	 * @param roles the roles to set
	 */
	@Override
	@Deprecated
	public void setRoles(Set<Role> roles) {
		LOG.warn("Calling the method setRoles() has no effect. "
				+ "To set the roles of a certain user, please make "
				+ "use of the UserGroupRoleService.");
	}

	/**
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}

	/**
	 * @param department the department to set
	 */
	public void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * @return the telephone
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * @param telephone the telephone to set
	 */
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	/**
	 * @return the profileImage
	 */
	public String getProfileImage() {
		return profileImage;
	}

	/**
	 * @param profileImage the profileImage to set
	 */
	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

}
