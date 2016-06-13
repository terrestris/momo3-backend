package de.terrestris.momo.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.terrestris.momo.model.security.LayerPermissionCollection;
import de.terrestris.shogun2.model.Territory;
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
	@ManyToOne
	private Territory territory;

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
	 * @return
	 */
	public Territory getTerritory() {
		return territory;
	}

	/**
	 *
	 * @param territory
	 */
	public void setTerritory(Territory territory) {
		this.territory = territory;
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
