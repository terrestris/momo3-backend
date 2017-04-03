package de.terrestris.momo.model.security;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.Role;

/**
 *
 * @author Daniel Koch
 * @author André Henn
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@Table
public class UserGroupRole extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@OneToOne
	MomoUser user;

	/**
	 *
	 */
	@OneToOne
	MomoUserGroup group;

	/**
	 *
	 */
	@OneToOne
	Role role;

	/**
	 * Default Constructor
	 */
	public UserGroupRole() {
	}

	/**
	 * 
	 * @param user
	 * @param userGroup
	 */
	public UserGroupRole(MomoUser user, MomoUserGroup userGroup) {
		this.user = user;
		this.group = userGroup;
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 * @param role
	 */
	public UserGroupRole(MomoUser user, MomoUserGroup userGroup, Role role) {
		this.user = user;
		this.group = userGroup;
		this.role = role;
	}

	/**
	 * @return the user
	 */
	public MomoUser getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(MomoUser user) {
		this.user = user;
	}

	/**
	 * @return the group
	 */
	public MomoUserGroup getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(MomoUserGroup group) {
		this.group = group;
	}

	/**
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 *
	 *      According to
	 *      http://stackoverflow.com/questions/27581/overriding-equals
	 *      -and-hashcode-in-java it is recommended only to use getter-methods
	 *      when using ORM like Hibernate
	 */
	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(23, 167)
				.appendSuper(super.hashCode())
				.append(getUser())
				.append(getGroup())
				.append(getRole())
				.toHashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 *
	 *      According to
	 *      http://stackoverflow.com/questions/27581/overriding-equals
	 *      -and-hashcode-in-java it is recommended only to use getter-methods
	 *      when using ORM like Hibernate
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserGroupRole))
			return false;
		UserGroupRole other = (UserGroupRole) obj;

		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(getUser(), other.getUser())
				.append(getGroup(), other.getGroup())
				.append(getRole(), other.getRole())
				.isEquals();
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.appendSuper(super.toString())
			.append("user", getUser())
			.append("group", getGroup())
			.append("role", getRole())
			.toString();
	}

}
