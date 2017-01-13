package de.terrestris.momo.model.state;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.ReadableDateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.resolver.MomoApplicationIdResolver;
import de.terrestris.momo.resolver.UserIdResolver;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.User;

/**
*
* @author Daniel Koch
* @author terrestris GmbH & Co. KG
*
*/
@Entity
@Table
public class ApplicationState extends PersistentObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@Column(unique = true, updatable = false)
	private final String token = UUID.randomUUID().toString();

	/**
	 *
	 */
	private String description;

	/**
	 *
	 */
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
		joinColumns = { @JoinColumn(name = "APPLICATIONSTATE_ID") },
		inverseJoinColumns = { @JoinColumn(name = "MAPLAYER_ID") }
	)
	@OrderColumn(name = "IDX")
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id"
	)
	private List<LayerState> layers;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private MapViewState mapView;

	/**
	 *
	 */
	@OneToOne(cascade = CascadeType.ALL)
	private RedliningState redlining;

	/**
	 *
	 */
	@ManyToOne
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id",
		resolver = UserIdResolver.class
	)
	@JsonIdentityReference(alwaysAsId = true)
	private User owner;

	/**
	 *
	 */
	@ManyToOne
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id",
		resolver = MomoApplicationIdResolver.class
	)
	@JsonIdentityReference(alwaysAsId = true)
	private MomoApplication application;

	/**
	 *
	 */
	public ApplicationState() {
	}

	/**
	 *
	 * @param description
	 * @param layers
	 * @param mapView
	 * @param redlining
	 */
	public ApplicationState(String token, String description,
			TimeReferenceState timeReference, List<LayerState> layers,
			MapViewState mapView, RedliningState redlining, MomoApplication application) {
		super();
		this.description = description;
		this.layers = layers;
		this.mapView = mapView;
		this.redlining = redlining;
		this.application = application;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Overwrite this getter to set the {@link JsonIgnore} value to false
	 * for this subclass.
	 */
	@Override
	@JsonIgnore(false)
	public ReadableDateTime getCreated() {
		return super.getCreated();
	}

	/**
	 * Overwrite this getter to set the {@link JsonIgnore} value to false
	 * for this subclass.
	 */
	@Override
	@JsonIgnore(false)
	public ReadableDateTime getModified() {
		return super.getModified();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the layers
	 */
	public List<LayerState> getLayers() {
		return layers;
	}

	/**
	 * @param layers the layers to set
	 */
	public void setLayers(List<LayerState> layers) {
		this.layers = layers;
	}

	/**
	 * @return the mapView
	 */
	public MapViewState getMapView() {
		return mapView;
	}

	/**
	 * @param mapView the mapView to set
	 */
	public void setMapView(MapViewState mapView) {
		this.mapView = mapView;
	}

	/**
	 * @return the redlining
	 */
	public RedliningState getRedlining() {
		return redlining;
	}

	/**
	 * @param redlining the redlining to set
	 */
	public void setRedlining(RedliningState redlining) {
		this.redlining = redlining;
	}

	/**
	 * @return the owner
	 */
	public User getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(User owner) {
		this.owner = owner;
	}

	/**
	 * @return the application
	 */
	public MomoApplication getApplication() {
		return application;
	}

	/**
	 * @param application the application to set
	 */
	public void setApplication(MomoApplication application) {
		this.application = application;
	}

	@Override
	public int hashCode() {
		// two randomly chosen prime numbers
		return new HashCodeBuilder(41, 37).appendSuper(super.hashCode())
				.append(getToken())
				.append(getDescription())
				.append(getLayers())
				.append(getMapView())
				.append(getRedlining())
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ApplicationState))
			return false;
		ApplicationState other = (ApplicationState) obj;

		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getToken(), other.getToken())
				.append(getDescription(), other.getDescription())
				.append(getLayers(), other.getLayers())
				.append(getMapView(), other.getMapView())
				.append(getRedlining(), other.getRedlining())
				.append(getApplication(), other.getApplication())
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("token", getToken())
				.append("description", getDescription())
				.append("layers", getLayers())
				.append("mapView", getMapView())
				.append("redlining", getRedlining())
				.append("application", getApplication())
				.toString();
	}

}
