package de.terrestris.momo.model.state.geometry;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.terrestris.shogun2.model.PersistentObject;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type",
		visible = true
)
@JsonSubTypes({
		@Type(value = PointGeometry.class, name = "Point"),
		@Type(value = LineGeometry.class, name = "LineString"),
		@Type(value = PolygonGeometry.class, name = "Polygon")
})
@Inheritance(
		strategy = InheritanceType.TABLE_PER_CLASS
)
public abstract class AbstractGeometry extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private String type;

	/**
	 *
	 */
	public AbstractGeometry() {
	}

	/**
	 *
	 * @param type
	 * @param coordinates
	 */
	public AbstractGeometry(String type) {
		super();
		this.type = type;
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

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 23).appendSuper(super.hashCode())
				.append(getType())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractGeometry))
			return false;
		AbstractGeometry other = (AbstractGeometry) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getType(), other.getType())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("type", getType())
				.toString();
	}
}
