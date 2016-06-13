/**
 *
 */
package de.terrestris.momo.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.terrestris.shogun2.model.layer.Layer;

/**
 * @author Nils Bühner
 *
 */
@Entity
@Table
public class MomoLayer extends Layer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private Boolean spatiallyRestricted;

	/**
	 *
	 */
	public MomoLayer() {
	}

	/**
	 * @return the spatiallyRestricted
	 */
	public Boolean getSpatiallyRestricted() {
		return spatiallyRestricted;
	}

	/**
	 * @param spatiallyRestricted the spatiallyRestricted to set
	 */
	public void setSpatiallyRestricted(Boolean spatiallyRestricted) {
		this.spatiallyRestricted = spatiallyRestricted;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 *
	 *      According to
	 *      http://stackoverflow.com/questions/27581/overriding-equals
	 *      -and-hashcode-in-java it is recommended only to use getter-methods
	 *      when using ORM like Hibernate
	 */
	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(29, 11).
				appendSuper(super.hashCode()).
				append(getSpatiallyRestricted()).
				toHashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 *
	 *      According to
	 *      http://stackoverflow.com/questions/27581/overriding-equals
	 *      -and-hashcode-in-java it is recommended only to use getter-methods
	 *      when using ORM like Hibernate
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MomoLayer))
			return false;
		MomoLayer other = (MomoLayer) obj;

		return new EqualsBuilder().
				appendSuper(super.equals(other)).
				append(getSpatiallyRestricted(), other.getSpatiallyRestricted()).
				isEquals();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

}
