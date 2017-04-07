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
	@Override
	@Deprecated
	public Set<UserGroup> getUserGroups() {
		LOG.warn("Please do not use this method.");
		return null;
	}

	/**
	 *
	 */
	@Override
	@Deprecated
	public void setUserGroups(Set<UserGroup> userGroups) {
		LOG.warn("Please do not use this method.");
	}

	/**
	 * @return the roles
	 */
	@Override
	@Deprecated
	public Set<Role> getRoles() {
		LOG.warn("Please do not use this method.");
		return null;
	}

	/**
	 * @param roles the roles to set
	 */
	@Override
	@Deprecated
	public void setRoles(Set<Role> roles) {
		LOG.warn("Please do not use this method.");
	}

}
