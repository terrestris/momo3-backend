package de.terrestris.momo.model.state.geometry;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@Table
public class PointGeometry extends AbstractGeometry {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private Double[] coordinates;

	/**
	 *
	 */
	public PointGeometry() {
	}

	/**
	 *
	 * @param type
	 * @param coordinates
	 */
	public PointGeometry(String type, Double[] coordinates) {
		super();
		this.coordinates = coordinates;
	}

	/**
	 * @return the coordinates
	 */
	public Double[] getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(Double[] coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(47, 61).appendSuper(super.hashCode())
				.append(getCoordinates())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PointGeometry))
			return false;
		PointGeometry other = (PointGeometry) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getCoordinates(), other.getCoordinates())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("coordinates", getCoordinates())
				.toString();
	}
}
