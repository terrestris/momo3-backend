package de.terrestris.momo.model.state.style;

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
public class IconStyle extends AbstractImageStyle {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private Double[] anchor;

	/**
	 *
	 */
	private Double[] anchorOrigin;

	/**
	 *
	 */
	private String anchorXUnits;

	/**
	 *
	 */
	private String anchorYUnits;

	/**
	 *
	 */
	private Double[] size;

	/**
	 *
	 */
	private String src;

	/**
	 *
	 */
	public IconStyle() {
	}

	/**
	 *
	 * @param anchor
	 * @param origin
	 * @param size
	 * @param src
	 */
	public IconStyle(Double[] anchor, Double[] anchorOrigin, Double[] size, 
			String src, String anchorXUnits, String anchorYUnits) {
		super();
		this.anchor = anchor;
		this.anchorOrigin = anchorOrigin;
		this.anchorXUnits = anchorXUnits;
		this.anchorYUnits = anchorYUnits;
		this.size = size;
		this.src = src;
	}

	/**
	 * @return the anchor
	 */
	public Double[] getAnchor() {
		return anchor;
	}

	/**
	 * @param anchor the anchor to set
	 */
	public void setAnchor(Double[] anchor) {
		this.anchor = anchor;
	}

	/**
	 * @return the anchorOrigin
	 */
	public Double[] getAnchorOrigin() {
		return anchorOrigin;
	}

	/**
	 * @param anchorOrigin the anchorOrigin to set
	 */
	public void setAnchorOrigin(Double[] anchorOrigin) {
		this.anchorOrigin = anchorOrigin;
	}

	/**
	 * @return the anchorXUnits
	 */
	public String getAnchorXUnits() {
		return anchorXUnits;
	}

	/**
	 * @param anchorXUnits the anchorXUnits to set
	 */
	public void setAnchorXUnits(String anchorXUnits) {
		this.anchorXUnits = anchorXUnits;
	}

	/**
	 * @return the anchorYUnits
	 */
	public String getAnchorYUnits() {
		return anchorYUnits;
	}

	/**
	 * @param anchorYUnits the anchorYUnits to set
	 */
	public void setAnchorYUnits(String anchorYUnits) {
		this.anchorYUnits = anchorYUnits;
	}

	/**
	 * @return the size
	 */
	public Double[] getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Double[] size) {
		this.size = size;
	}

	/**
	 * @return the src
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * @param src the src to set
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 53).appendSuper(super.hashCode())
				.append(getAnchor())
				.append(getAnchorOrigin())
				.append(getAnchorXUnits())
				.append(getAnchorYUnits())
				.append(getSize())
				.append(getSrc())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IconStyle))
			return false;
		IconStyle other = (IconStyle) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getAnchor(), other.getAnchor())
				.append(getAnchorOrigin(), other.getAnchorOrigin())
				.append(getAnchorXUnits(), other.getAnchorXUnits())
				.append(getAnchorYUnits(), other.getAnchorYUnits())
				.append(getSize(), other.getSize())
				.append(getSrc(), other.getSrc())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("anchor", getAnchor())
				.append("anchorOrigin", getAnchorOrigin())
				.append("anchorXUnits", getAnchorXUnits())
				.append("anchorYUnits", getAnchorYUnits())
				.append("size", getSize())
				.append("src", getSrc())
				.toString();
	}

}
