package de.terrestris.momo.model.state;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import de.terrestris.shogun2.model.PersistentObject;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@Table
public class MapViewState extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private Integer zoom;

	/**
	 *
	 */
	private Double[] center;

	/**
	 *
	 */
	private Double rotation;

	/**
	 *
	 */
	public MapViewState() {
	}

	/**
	 *
	 * @param zoom
	 * @param center
	 * @param rotation
	 */
	public MapViewState(Integer zoom, Double[] center, Double rotation) {
		super();
		this.zoom = zoom;
		this.center = center;
		this.rotation = rotation;
	}

	/**
	 * @return the zoom
	 */
	public Integer getZoom() {
		return zoom;
	}

	/**
	 * @param zoom the zoom to set
	 */
	public void setZoom(Integer zoom) {
		this.zoom = zoom;
	}

	/**
	 * @return the center
	 */
	public Double[] getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(Double[] center) {
		this.center = center;
	}

	/**
	 * @return the rotation
	 */
	public Double getRotation() {
		return rotation;
	}

	/**
	 * @param rotation the rotation to set
	 */
	public void setRotation(Double rotation) {
		this.rotation = rotation;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 19).appendSuper(super.hashCode())
				.append(getZoom())
				.append(getCenter())
				.append(getRotation())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MapViewState))
			return false;
		MapViewState other = (MapViewState) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getZoom(), other.getZoom())
				.append(getCenter(), other.getCenter())
				.append(getRotation(), other.getRotation())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("zoom", getZoom())
				.append("center", getCenter())
				.append("rotation", getRotation())
				.toString();
	}

}