package de.terrestris.momo.model.security;

import java.util.Set;

import javax.persistence.OneToMany;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.terrestris.shogun2.model.PersistentObject;

//@JsonTypeInfo(
//		use = JsonTypeInfo.Id.NAME,
//		include = JsonTypeInfo.As.PROPERTY,
//		property = "type",
//		visible = true
//)
//@JsonSubTypes({
//		@Type(value = MomoLayer.class, name = "MomoLayer"),
//		@Type(value = MomoApplication.class, name = "MomoApplication"),
//})
//@JsonDeserialize(using = EntityPermissionTypeEnvelopeDeserializer.class)
public class EntityPermissionTypeEnvelope {

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
//		@Type(value = MomoApplication.class, name = "MomoApplication"),
//		@Type(value = MomoLayer.class, name = "MomoLayer"),
//})
	private PersistentObject targetEntity;

	/**
	 *
	 */
	private String type;

	/**
	 *
	 */
	@OneToMany
	private Set<EntityPermissionEnvelope> permissions;

	/**
	 *
	 */
	public EntityPermissionTypeEnvelope() {
	}

	/**
	 *
	 * @param targetEntity
	 * @param type
	 * @param permissions
	 */
	public EntityPermissionTypeEnvelope(PersistentObject targetEntity, String type, Set<EntityPermissionEnvelope> permissions) {
		this.targetEntity = targetEntity;
		this.type = type;
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
	 * @return the permissions
	 */
	public Set<EntityPermissionEnvelope> getPermissions() {
		return permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(Set<EntityPermissionEnvelope> permissions) {
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
		if (!(obj instanceof EntityPermissionTypeEnvelope))
			return false;
		EntityPermissionTypeEnvelope other = (EntityPermissionTypeEnvelope) obj;

		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(getTargetEntity().getId(), other.getTargetEntity().getId())
				.append(getType(), other.getType())
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
			.append("permissions", getPermissions())
			.toString();
	}
}
