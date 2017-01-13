package de.terrestris.momo.model.state;

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
public class LayerState extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private String identifier;

	/**
	 *
	 */
	private boolean visible;

	/**
	 *
	 */
	public LayerState() {
	}

	/**
	 *
	 * @param identifier
	 * @param visible
	 */
	public LayerState(String identifier, boolean visible) {
		super();
		this.identifier = identifier;
		this.visible = visible;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 31).appendSuper(super.hashCode())
				.append(getIdentifier())
				.append(isVisible())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LayerState))
			return false;
		LayerState other = (LayerState) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getIdentifier(), other.getIdentifier())
				.append(isVisible(), other.isVisible())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("identifier", getIdentifier())
				.append("visible", isVisible())
				.toString();
	}

}
