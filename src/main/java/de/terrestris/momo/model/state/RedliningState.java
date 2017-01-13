package de.terrestris.momo.model.state;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import de.terrestris.momo.model.state.style.Style;
import de.terrestris.shogun2.model.PersistentObject;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@Table
public class RedliningState extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private FeatureCollection features;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private Style pointStyle;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private Style lineStyle;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private Style polygonStyle;

	/**
	 *
	 */
	public RedliningState() {
	}

	/**
	 *
	 * @param features
	 */
	public RedliningState(FeatureCollection features) {
		super();
		this.features = features;
	}

	/**
	 * @return the pointStyle
	 */
	public Style getPointStyle() {
		return pointStyle;
	}

	/**
	 * @param pointStyle the pointStyle to set
	 */
	public void setPointStyle(Style pointStyle) {
		this.pointStyle = pointStyle;
	}

	/**
	 * @return the lineStyle
	 */
	public Style getLineStyle() {
		return lineStyle;
	}

	/**
	 * @param lineStyle the lineStyle to set
	 */
	public void setLineStyle(Style lineStyle) {
		this.lineStyle = lineStyle;
	}

	/**
	 * @return the polygonStyle
	 */
	public Style getPolygonStyle() {
		return polygonStyle;
	}

	/**
	 * @param polygonStyle the polygonStyle to set
	 */
	public void setPolygonStyle(Style polygonStyle) {
		this.polygonStyle = polygonStyle;
	}

	/**
	 * @return the features
	 */
	public FeatureCollection getFeatures() {
		return features;
	}

	/**
	 * @param features the features to set
	 */
	public void setFeatures(FeatureCollection features) {
		this.features = features;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 43).appendSuper(super.hashCode())
				.append(getFeatures())
				.append(getPointStyle())
				.append(getLineStyle())
				.append(getPolygonStyle())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RedliningState))
			return false;
		RedliningState other = (RedliningState) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getFeatures(), other.getFeatures())
				.append(getPointStyle(), other.getPointStyle())
				.append(getLineStyle(), other.getLineStyle())
				.append(getPolygonStyle(), other.getPolygonStyle())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("features", getFeatures())
				.append("pointStyle", getPointStyle())
				.append("lineStyle", getLineStyle())
				.append("polygonStyle", getPolygonStyle())
				.toString();
	}

}