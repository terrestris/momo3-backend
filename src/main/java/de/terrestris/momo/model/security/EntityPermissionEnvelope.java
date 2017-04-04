package de.terrestris.momo.model.security;

import javax.persistence.OneToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.security.PermissionCollection;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
public class EntityPermissionEnvelope {

	/**
	 *
	 */
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id"
	)
	@JsonIdentityReference(alwaysAsId = true)
	@JsonTypeInfo(
			use = JsonTypeInfo.Id.CLASS,
//			include = JsonTypeInfo.Id.CUSTOM,
			property = "type"
//			visible = true
	)
//	@JsonSubTypes({
//			@Type(value = MomoUser.class, name = "MomoUser"),
//			@Type(value = MomoUserGroup.class, name = "MomoUserGroup"),
//	})
	private PersistentObject targetEntity;

	/**
	 *
	 */
	private String type;

	/**
	 *
	 */
	private String displayTitle;

	/**
	 *
	 */
	@OneToOne
	private PermissionCollection permissions;

	/**
	 *
	 */
	public EntityPermissionEnvelope() {
	}

	/**
	 *
	 * @param targetEntity
	 * @param type
	 * @param displayTitle
	 * @param permissions
	 */
	public EntityPermissionEnvelope(PersistentObject targetEntity, String type, String displayTitle, PermissionCollection permissions) {
		this.targetEntity = targetEntity;
		this.type = type;
		this.displayTitle = displayTitle;
		this.permissions = permissions;
	}

	/**
	 * @return the targetEntity
	 */
	public PersistentObject getTargetEntity() {
		return targetEntity;
	}

	/**
	 * @param targetEntity the targetEntity to set
	 */
	public void setTargetEntity(PersistentObject targetEntity) {
		this.targetEntity = targetEntity;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the displayTitle
	 */
	public String getDisplayTitle() {
		return displayTitle;
	}

	/**
	 * @param displayTitle the displayTitle to set
	 */
	public void setDisplayTitle(String displayTitle) {
		this.displayTitle = displayTitle;
	}

	/**
	 * @return the permissions
	 */
	public PermissionCollection getPermissions() {
		return permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(PermissionCollection permissions) {
		this.permissions = permissions;
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
		if (!(obj instanceof EntityPermissionEnvelope))
			return false;
		EntityPermissionEnvelope other = (EntityPermissionEnvelope) obj;

		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(getTargetEntity().getId(), other.getTargetEntity().getId())
				.append(getType(), other.getType())
				.append(getDisplayTitle(), other.getDisplayTitle())
				.append(getPermissions(), other.getPermissions())
				.isEquals();
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.appendSuper(super.toString())
			.append("targetEntity", getTargetEntity())
			.append("type", getType())
			.append("displayTitle", getDisplayTitle())
			.append("permissions", getPermissions())
			.toString();
	}
}
