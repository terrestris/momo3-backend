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
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.terrestris.momo.model.security.LayerPermissionCollection;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;

/**
 * @author Nils Bühner
 *
 */
@Entity
@Table
public class MomoUserGroup extends UserGroup {

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
