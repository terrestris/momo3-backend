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
public class Style extends PersistentObject {

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
	@OneToOne(cascade = CascadeType.ALL)
	private AbstractImageStyle image;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private StrokeStyle stroke;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private TextStyle text;

	/**
	 *
	 */
	private Integer zIndex;

	/**
	 *
	 */
	public Style() {
	}

	/**
	 *
	 * @param fill
	 * @param image
	 * @param stroke
	 * @param text
	 * @param zIndex
	 */
	public Style(FillStyle fill, AbstractImageStyle image, StrokeStyle stroke,
			TextStyle text, Integer zIndex) {
		super();
		this.fill = fill;
		this.image = image;
		this.stroke = stroke;
		this.text = text;
		this.zIndex = zIndex;
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
	 * @return the image
	 */
	public AbstractImageStyle getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(AbstractImageStyle image) {
		this.image = image;
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
	public TextStyle getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(TextStyle text) {
		this.text = text;
	}

	/**
	 * @return the zIndex
	 */
	public Integer getZIndex() {
		return zIndex;
	}

	/**
	 * @param zIndex the zIndex to set
	 */
	public void setZIndex(Integer zIndex) {
		this.zIndex = zIndex;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 5).appendSuper(super.hashCode())
				.append(getFill())
				.append(getImage())
				.append(getStroke())
				.append(getText())
				.append(getZIndex())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Style))
			return false;
		Style other = (Style) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getFill(), other.getFill())
				.append(getImage(), other.getImage())
				.append(getStroke(), other.getStroke())
				.append(getText(), other.getText())
				.append(getZIndex(), other.getZIndex())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("fill", getFill())
				.append("image", getImage())
				.append("stroke", getStroke())
				.append("text", getText())
				.append("zIndex", getZIndex())
				.toString();
	}

}