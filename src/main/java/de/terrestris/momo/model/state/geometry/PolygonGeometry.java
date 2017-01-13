package de.terrestris.momo.model.state.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
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
public class PolygonGeometry extends AbstractGeometry {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@ElementCollection
	@Column(length = Integer.MAX_VALUE)
	private List<ArrayList<Double[]>> coordinates = new ArrayList<ArrayList<Double[]>>();

	/**
	 *
	 */
	public PolygonGeometry() {
	}

	/**
	 *
	 * @param type
	 * @param coordinates
	 */
	public PolygonGeometry(String type, List<ArrayList<Double[]>> coordinates) {
		super();
		this.coordinates = coordinates;
	}

	/**
	 * @return the coordinates
	 */
	public List<ArrayList<Double[]>> getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(List<ArrayList<Double[]>> coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 67).appendSuper(super.hashCode())
				.append(getCoordinates())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PolygonGeometry))
			return false;
		PolygonGeometry other = (PolygonGeometry) obj;

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
