package de.terrestris.momo.model.security;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.terrestris.shogun2.model.layer.Layer;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.security.PermissionCollection;

/**
 * @author Nils Bühner
 *
 */
@Entity
@Table
public class LayerPermissionCollection extends PermissionCollection {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	private Layer layer;

	/**
	 * Explicitly adding the default constructor as this is important, e.g. for
	 * Hibernate: http://goo.gl/3Cr1pw
	 */
	public LayerPermissionCollection() {
	}

	/**
	 *
	 * @param permissions
	 */
	public LayerPermissionCollection(Layer layer, Set<Permission> permissions) {
		super(permissions);
		this.layer = layer;
	}

	/**
	 *
	 * @return
	 */
	public Layer getLayer() {
		return layer;
	}

	/**
	 *
	 * @param layer
	 */
	public void setLayer(Layer layer) {
		this.layer = layer;
	}

}
