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
public class RegularShapeStyle extends AbstractImageStyle {

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
	private Integer points;

	/**
	 *
	 */
	private Double radius;

	/**
	 * Sic!
	 */
	private Double radius2;

	/**
	 *
	 */
	private Double angle;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private StrokeStyle stroke;

	/**
	 *
	 */
	public RegularShapeStyle() {
	}

	/**
	 *
	 * @param fill
	 * @param points
	 * @param radius
	 * @param radius2
	 * @param angle
	 * @param stroke
	 */
	public RegularShapeStyle(FillStyle fill, Integer points, Double radius,
			Double radius2, Double angle, StrokeStyle stroke) {
		super();
		this.fill = fill;
		this.points = points;
		this.radius = radius;
		this.radius2 = radius2;
		this.angle = angle;
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
	 * @return the points
	 */
	public Integer getPoints() {
		return points;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(Integer points) {
		this.points = points;
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
	 * @return the radius2
	 */
	public Double getRadius2() {
		return radius2;
	}

	/**
	 * @param radius2 the radius2 to set
	 */
	public void setRadius2(Double radius2) {
		this.radius2 = radius2;
	}

	/**
	 * @return the angle
	 */
	public Double getAngle() {
		return angle;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setAngle(Double angle) {
		this.angle = angle;
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
		return new HashCodeBuilder(41, 2).appendSuper(super.hashCode())
				.append(getFill())
				.append(getPoints())
				.append(getRadius())
				.append(getRadius2())
				.append(getAngle())
				.append(getStroke())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RegularShapeStyle))
			return false;
		RegularShapeStyle other = (RegularShapeStyle) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getFill(), other.getFill())
				.append(getPoints(), other.getPoints())
				.append(getRadius(), other.getRadius())
				.append(getRadius2(), other.getRadius2())
				.append(getAngle(), other.getAngle())
				.append(getStroke(), other.getStroke())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("fill", getFill())
				.append("points", getPoints())
				.append("radius", getRadius())
				.append("radius2", getRadius2())
				.append("angle", getAngle())
				.append("stroke", getStroke())
				.toString();
	}

}
