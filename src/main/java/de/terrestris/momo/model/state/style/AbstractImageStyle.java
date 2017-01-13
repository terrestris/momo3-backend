package de.terrestris.momo.model.state.style;

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
		@Type(value = CircleStyle.class, name = "circle"),
		@Type(value = IconStyle.class, name = "icon"),
		@Type(value = RegularShapeStyle.class, name = "regularShape")
})
@Inheritance(
		strategy = InheritanceType.TABLE_PER_CLASS
)
public abstract class AbstractImageStyle extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This property is used internally for Jackson's objectMapper only and may
	 * be used while parsing the response. It is not an ol3 element.
	 */
	private String type;

	/**
	 *
	 */
	private Double opacity;

	/**
	 *
	 */
	private boolean rotateWithView;

	/**
	 *
	 */
	private Double rotation;

	/**
	 *
	 */
	private Double scale;

	/**
	 *
	 */
	private boolean snapToPixel;

	/**
	 *
	 */
	public AbstractImageStyle() {
	}

	/**
	 *
	 * @param opacity
	 * @param rotateWithView
	 * @param rotation
	 * @param scale
	 * @param snapToPixel
	 */
	public AbstractImageStyle(Double opacity, boolean rotateWithView, Double rotation,
			Double scale, boolean snapToPixel) {
		super();
		this.opacity = opacity;
		this.rotateWithView = rotateWithView;
		this.rotation = rotation;
		this.scale = scale;
		this.snapToPixel = snapToPixel;
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
	 * @return the opacity
	 */
	public Double getOpacity() {
		return opacity;
	}

	/**
	 * @param opacity the opacity to set
	 */
	public void setOpacity(Double opacity) {
		this.opacity = opacity;
	}

	/**
	 * @return the rotateWithView
	 */
	public boolean isRotateWithView() {
		return rotateWithView;
	}

	/**
	 * @param rotateWithView the rotateWithView to set
	 */
	public void setRotateWithView(boolean rotateWithView) {
		this.rotateWithView = rotateWithView;
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

	/**
	 * @return the scale
	 */
	public Double getScale() {
		return scale;
	}

	/**
	 * @param scale the scale to set
	 */
	public void setScale(Double scale) {
		this.scale = scale;
	}

	/**
	 * @return the snapToPixel
	 */
	public boolean isSnapToPixel() {
		return snapToPixel;
	}

	/**
	 * @param snapToPixel the snapToPixel to set
	 */
	public void setSnapToPixel(boolean snapToPixel) {
		this.snapToPixel = snapToPixel;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 17).appendSuper(super.hashCode())
				.append(getType())
				.append(getOpacity())
				.append(isRotateWithView())
				.append(getRotation())
				.append(getScale())
				.append(isSnapToPixel())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractImageStyle))
			return false;
		AbstractImageStyle other = (AbstractImageStyle) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getType(), other.getType())
				.append(getOpacity(), other.getOpacity())
				.append(isRotateWithView(), other.isRotateWithView())
				.append(getRotation(), other.getRotation())
				.append(getScale(), other.getScale())
				.append(isSnapToPixel(), other.isSnapToPixel())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("type", getType())
				.append("opacity", getOpacity())
				.append("rotateWithView", isRotateWithView())
				.append("rotation", getRotation())
				.append("scale", getScale())
				.append("snapToPixel", isSnapToPixel())
				.toString();
	}
}
