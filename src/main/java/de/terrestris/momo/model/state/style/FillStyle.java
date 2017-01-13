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
public class FillStyle extends PersistentObject {

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
	public FillStyle() {
	}

	/**
	 *
	 * @param color
	 */
	public FillStyle(String color) {
		super();
		this.color = color;
	}

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

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 11).appendSuper(super.hashCode())
				.append(getColor())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FillStyle))
			return false;
		FillStyle other = (FillStyle) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getColor(), other.getColor())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("color", getColor())
				.toString();
	}

}
