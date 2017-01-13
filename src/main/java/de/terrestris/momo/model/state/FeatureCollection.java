package de.terrestris.momo.model.state;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.terrestris.shogun2.model.PersistentObject;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@Table
public class FeatureCollection extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private String type;

	/**
	 *
	 */
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
		joinColumns = { @JoinColumn(name = "MAPFEATURECOLLECTION_ID") },
		inverseJoinColumns = { @JoinColumn(name = "MAPFEATURE_ID") }
	)
	@OrderColumn(name = "IDX")
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id"
	)
	private List<Feature> features;

	/**
	 *
	 */
	public FeatureCollection() {
	}

	/**
	 *
	 * @param type
	 * @param features
	 */
	public FeatureCollection(String type, List<Feature> features) {
		super();
		this.type = type;
		this.features = features;
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
	 * @return the features
	 */
	public List<Feature> getFeatures() {
		return features;
	}

	/**
	 * @param features the features to set
	 */
	public void setFeatures(List<Feature> features) {
		this.features = features;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 47).appendSuper(super.hashCode())
				.append(getType())
				.append(getFeatures())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FeatureCollection))
			return false;
		FeatureCollection other = (FeatureCollection) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getType(), other.getType())
				.append(getFeatures(), other.getFeatures())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("type", getType())
				.append("type", getFeatures())
				.toString();
	}

}
