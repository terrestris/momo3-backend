package de.terrestris.momo.model.state.style;

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
public class StrokeStyle extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private String color;

	/**
	 *
	 */
	private String lineCap;

	/**
	 *
	 */
	private Integer[] lineDash;

	/**
	 *
	 */
	private String lineJoin;

	/**
	 *
	 */
	private Integer miterLimit;

	/**
	 *
	 */
	private Integer width;

	/**
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * @return the lineCap
	 */
	public String getLineCap() {
		return lineCap;
	}

	/**
	 * @param lineCap the lineCap to set
	 */
	public void setLineCap(String lineCap) {
		this.lineCap = lineCap;
	}

	/**
	 * @return the lineDash
	 */
	public Integer[] getLineDash() {
		return lineDash;
	}

	/**
	 * @param lineDash the lineDash to set
	 */
	public void setLineDash(Integer[] lineDash) {
		this.lineDash = lineDash;
	}

	/**
	 * @return the lineJoin
	 */
	public String getLineJoin() {
		return lineJoin;
	}

	/**
	 * @param lineJoin the lineJoin to set
	 */
	public void setLineJoin(String lineJoin) {
		this.lineJoin = lineJoin;
	}

	/**
	 * @return the miterLimit
	 */
	public Integer getMiterLimit() {
		return miterLimit;
	}

	/**
	 * @param miterLimit the miterLimit to set
	 */
	public void setMiterLimit(Integer miterLimit) {
		this.miterLimit = miterLimit;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}

	/**
	 *
	 */
	public StrokeStyle() {
	}

	/**
	 *
	 * @param color
	 * @param lineCap
	 * @param lineDash
	 * @param lineJoin
	 * @param miterLimit
	 * @param width
	 */
	public StrokeStyle(String color, String lineCap, Integer[] lineDash,
			String lineJoin, Integer miterLimit, Integer width) {
		super();
		this.color = color;
		this.lineCap = lineCap;
		this.lineDash = lineDash;
		this.lineJoin = lineJoin;
		this.miterLimit = miterLimit;
		this.width = width;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 13).appendSuper(super.hashCode())
				.append(getColor())
				.append(getLineCap())
				.append(getLineDash())
				.append(getLineJoin())
				.append(getMiterLimit())
				.append(getWidth())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StrokeStyle))
			return false;
		StrokeStyle other = (StrokeStyle) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getColor(), other.getColor())
				.append(getLineCap(), other.getLineCap())
				.append(getLineDash(), other.getLineDash())
				.append(getLineJoin(), other.getLineJoin())
				.append(getMiterLimit(), other.getMiterLimit())
				.append(getWidth(), other.getWidth())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("color", getColor())
				.append("lineCap", getLineCap())
				.append("lineDash", getLineDash())
				.append("lineJoin", getLineJoin())
				.append("miterLimit", getMiterLimit())
				.append("width", getWidth())
				.toString();
	}

}
