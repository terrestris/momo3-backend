package de.terrestris.momo.model.state;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import de.terrestris.momo.model.state.geometry.AbstractGeometry;
import de.terrestris.momo.model.state.style.Style;
import de.terrestris.shogun2.converter.PropertyValueConverter;
import de.terrestris.shogun2.model.PersistentObject;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Entity
@Table
public class Feature extends PersistentObject {

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
	@OneToOne(cascade = CascadeType.ALL)
	private AbstractGeometry geometry;

	/**
	 *
	 */
	@ElementCollection
	@MapKeyColumn(name = "PROPERTY")
	@Column(name = "VALUE")
	@CollectionTable(
			name = "MAPFEATURE_PROPERTIES",
			joinColumns = @JoinColumn(name = "MAPFEATURE_ID")
	)
	@Convert(
			converter = PropertyValueConverter.class,
			attributeName="value"
	)
	private Map<String, Object> properties;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private Style style;

	/**
	 *
	 */
	public Feature() {
	}

	/**
	 *
	 * @param type
	 * @param geometry
	 * @param properties
	 */
	public Feature(String type, AbstractGeometry geometry,
			Map<String, Object> properties, Style style) {
		super();
		this.type = type;
		this.geometry = geometry;
		this.properties = properties;
		this.style = style;
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
	 * @return the geometry
	 */
	public AbstractGeometry getGeometry() {
		return geometry;
	}

	/**
	 * @param geometry the geometry to set
	 */
	public void setGeometry(AbstractGeometry geometry) {
		this.geometry = geometry;
	}

	/**
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	/**
	 * @return the style
	 */
	public Style getStyle() {
		return style;
	}

	/**
	 * @param style the style to set
	 */
	public void setStyle(Style style) {
		this.style = style;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 29).appendSuper(super.hashCode())
				.append(getType())
				.append(getGeometry())
				.append(getProperties())
				.append(getStyle())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Feature))
			return false;
		Feature other = (Feature) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getType(), other.getType())
				.append(getGeometry(), other.getGeometry())
				.append(getProperties(), other.getProperties())
				.append(getStyle(), other.getStyle())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("type", getType())
				.append("geometry", getGeometry())
				.append("properties", getProperties())
				.append("style", getStyle())
				.toString();
	}

}