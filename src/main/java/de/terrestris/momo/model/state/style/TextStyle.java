package de.terrestris.momo.model.state.style;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
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
public class TextStyle extends PersistentObject {

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
	private String font;

	/**
	 *
	 */
	private Integer offsetX;

	/**
	 *
	 */
	private Integer offsetY;

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
	@OneToOne(cascade = CascadeType.ALL)
	private StrokeStyle stroke;

	/**
	 *
	 */
	private String text;

	/**
	 *
	 */
	private String textAlign;

	/**
	 *
	 */
	private String textBaseline;

	/**
	 *
	 */
	public TextStyle() {
	}

	/**
	 *
	 * @param fill
	 * @param font
	 * @param offsetX
	 * @param offsetY
	 * @param rotation
	 * @param scale
	 * @param stroke
	 * @param text
	 * @param textAlign
	 * @param textBaseline
	 */
	public TextStyle(FillStyle fill, String font, Integer offsetX,
			Integer offsetY, Double rotation, Double scale, StrokeStyle stroke,
			String text, String textAlign, String textBaseline) {
		super();
		this.fill = fill;
		this.font = font;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.rotation = rotation;
		this.scale = scale;
		this.stroke = stroke;
		this.text = text;
		this.textAlign = textAlign;
		this.textBaseline = textBaseline;
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
	 * @return the font
	 */
	public String getFont() {
		return font;
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(String font) {
		this.font = font;
	}

	/**
	 * @return the offsetX
	 */
	public Integer getOffsetX() {
		return offsetX;
	}

	/**
	 * @param offsetX the offsetX to set
	 */
	public void setOffsetX(Integer offsetX) {
		this.offsetX = offsetX;
	}

	/**
	 * @return the offsetY
	 */
	public Integer getOffsetY() {
		return offsetY;
	}

	/**
	 * @param offsetY the offsetY to set
	 */
	public void setOffsetY(Integer offsetY) {
		this.offsetY = offsetY;
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

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the textAlign
	 */
	public String getTextAlign() {
		return textAlign;
	}

	/**
	 * @param textAlign the textAlign to set
	 */
	public void setTextAlign(String textAlign) {
		this.textAlign = textAlign;
	}

	/**
	 * @return the textBaseline
	 */
	public String getTextBaseline() {
		return textBaseline;
	}

	/**
	 * @param textBaseline the textBaseline to set
	 */
	public void setTextBaseline(String textBaseline) {
		this.textBaseline = textBaseline;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 3).appendSuper(super.hashCode())
				.append(getFill())
				.append(getFont())
				.append(getOffsetX())
				.append(getOffsetY())
				.append(getRotation())
				.append(getScale())
				.append(getStroke())
				.append(getText())
				.append(getTextAlign())
				.append(getTextBaseline())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TextStyle))
			return false;
		TextStyle other = (TextStyle) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getFill(), other.getFill())
				.append(getFont(), other.getFont())
				.append(getOffsetX(), other.getOffsetX())
				.append(getOffsetY(), other.getOffsetY())
				.append(getRotation(), other.getRotation())
				.append(getScale(), other.getScale())
				.append(getStroke(), other.getStroke())
				.append(getText(), other.getText())
				.append(getTextAlign(), other.getTextAlign())
				.append(getTextBaseline(), other.getTextBaseline())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("fill", getFill())
				.append("font", getFont())
				.append("offsetX", getOffsetX())
				.append("offsetY", getOffsetY())
				.append("rotation", getRotation())
				.append("scale", getScale())
				.append("stroke", getStroke())
				.append("text", getText())
				.append("textAlign", getTextAlign())
				.append("textBaseline", getTextBaseline())
				.toString();
	}

}
