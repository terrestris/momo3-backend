package de.terrestris.momo.model.state.style;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
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
public class CircleStyle extends AbstractImageStyle {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private FillStyle fill;

	/**
	 *
	 */
	private Double radius;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private StrokeStyle stroke;

	/**
	 *
	 */
	public CircleStyle() {
	}

	/**
	 *
	 * @param fill
	 * @param radius
	 * @param stroke
	 */
	public CircleStyle(FillStyle fill, Double radius, StrokeStyle stroke) {
		super();
		this.fill = fill;
		this.radius = radius;
		this.stroke = stroke;
	}

	/**
	 * @return the fill
	 */
	public FillStyle getFill() {
		return fill;
	}

	/**
	 * @param fill the fill to set
	 */
	public void setFill(FillStyle fill) {
		this.fill = fill;
	}

	/**
	 * @return the radius
	 */
	public Double getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(Double radius) {
		this.radius = radius;
	}

	/**
	 * @return the stroke
	 */
	public StrokeStyle getStroke() {
		return stroke;
	}

	/**
	 * @param stroke the stroke to set
	 */
	public void setStroke(StrokeStyle stroke) {
		this.stroke = stroke;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 59).appendSuper(super.hashCode())
				.append(getFill())
				.append(getRadius())
				.append(getStroke())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CircleStyle))
			return false;
		CircleStyle other = (CircleStyle) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getFill(), other.getFill())
				.append(getRadius(), other.getRadius())
				.append(getStroke(), other.getStroke())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("fill", getFill())
				.append("radius", getRadius())
				.append("stroke", getStroke())
				.toString();
	}

}
