package de.terrestris.momo.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.terrestris.momo.model.security.LayerPermissionCollection;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;

/**
 * @author Nils Bühner
 *
 */
@Entity
public class MomoUserGroup extends UserGroup {

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(MomoUserGroup.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "USERGROUP_ID"))
	@Column(name = "MASK_VALUE")
	private Set<Integer> maskingValues = new HashSet<Integer>();

	/**
	 *
	 */
	@OneToMany
	@MapKeyJoinColumn(name = "USER_ID")
	@JoinTable(
		name="USERLAYERPERMISSIONS",
		joinColumns = @JoinColumn(name = "GROUP_ID"),
		inverseJoinColumns=@JoinColumn(name="LAYERPERMISSIONCOLLECTION_ID"))
	@JsonIgnore
	private Map<User, LayerPermissionCollection> userLayerPermissions = new HashMap<User, LayerPermissionCollection>();

	/**
	 * Default Constructor
	 */
	public MomoUserGroup() {
	}

	/**
	 *
	 */
	@Override
	@Deprecated
	public Set<User> getMembers() {
		LOG.debug("The method getMembers() returns null always. "
				+ "To get the members of a certain group, please make "
				+ "use of the UserGroupRoleService, e.g. findAllUserGroups()");
		return null;
	}

	/**
	 *
	 */
	@Override
	@Deprecated
	public void setMembers(Set<User> users) {
		LOG.debug("Calling the method setMembers() has no effect. "
				+ "To set the members of a certain group, please make "
				+ "use of the UserGroupRoleService.");
	}

	/**
	 * @return the roles
	 */
	@Override
	@Deprecated
	public Set<Role> getRoles() {
		LOG.debug("The method getRoles() returns null always. "
				+ "To get the roles of a certain group, please make "
				+ "use of the UserGroupRoleService, e.g. findAllUserGroupRoles().");
		return null;
	}

	/**
	 * @param roles the roles to set
	 */
	@Override
	@Deprecated
	public void setRoles(Set<Role> roles) {
		LOG.debug("Calling the method setRoles() has no effect. "
				+ "To set the roles of a certain group, please make "
				+ "use of the UserGroupRoleService.");
	}

	/**
	 * @return the maskingValues
	 */
	public Set<Integer> getMaskingValues() {
		return maskingValues;
	}

	/**
	 * @param maskingValues the maskingValues to set
	 */
	public void setMaskingValues(Set<Integer> maskingValues) {
		this.maskingValues = maskingValues;
	}

	/**
	 *
	 * @return
	 */
	public Map<User, LayerPermissionCollection> getUserLayerPermissions() {
		return userLayerPermissions;
	}

	/**
	 *
	 * @param userLayerPermissions
	 */
	public void setUserLayerPermissions(Map<User, LayerPermissionCollection> userLayerPermissions) {
		this.userLayerPermissions = userLayerPermissions;
	}

}
