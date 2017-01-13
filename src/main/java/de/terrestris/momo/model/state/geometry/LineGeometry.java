package de.terrestris.momo.model.state.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
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
public class LineGeometry extends AbstractGeometry {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@ElementCollection
	private List<Double[]> coordinates = new ArrayList<Double[]>();

	/**
	 *
	 */
	public LineGeometry() {
	}

	/**
	 *
	 * @param type
	 * @param coordinates
	 */
	public LineGeometry(String type, List<Double[]> coordinates) {
		super();
		this.coordinates = coordinates;
	}

	/**
	 * @return the coordinates
	 */
	public List<Double[]> getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(List<Double[]> coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 7).appendSuper(super.hashCode())
				.append(getCoordinates())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LineGeometry))
			return false;
		LineGeometry other = (LineGeometry) obj;

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
