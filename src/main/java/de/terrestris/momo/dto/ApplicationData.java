package de.terrestris.momo.dto;

import java.awt.geom.Point2D;
import java.util.List;

/**
 *
 * A class to encapsulate the relevant data of an application (e.g. when it is
 * being created) in a simple/flat way for (JSON-based) exchange between front- and backend.
 *
 * @author Nils Bühner
 *
 */
public class ApplicationData {

	private String name;
	private String description;
	private String language;
	private Boolean isPublic;
	private Boolean isActive;
	private String projection;
	private Point2D.Double center;
	private Integer zoom;
	private Integer layerTree;
	private Integer id;
	private List<Integer> activeTools;

	/**
	 *
	 */
	public ApplicationData() {

	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the isPublic
	 */
	public Boolean getIsPublic() {
		return isPublic;
	}

	/**
	 * @param isPublic the isPublic to set
	 */
	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @return the isActive
	 */
	public Boolean getIsActive() {
		return isActive;
	}

	/**
	 * @param isActive the isActive to set
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * @return the projection
	 */
	public String getProjection() {
		return projection;
	}

	/**
	 * @param projection the projection to set
	 */
	public void setProjection(String projection) {
		this.projection = projection;
	}

	/**
	 * @return the center
	 */
	public Point2D.Double getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(Point2D.Double center) {
		this.center = center;
	}

	/**
	 * @return the zoom
	 */
	public Integer getZoom() {
		return zoom;
	}

	/**
	 * @param zoom the zoom to set
	 */
	public void setZoom(Integer zoom) {
		this.zoom = zoom;
	}

	/**
	 * @return the layerTree
	 */
	public Integer getLayerTree() {
		return layerTree;
	}

	/**
	 * @param layerTree the layerTree to set
	 */
	public void setLayerTree(Integer layerTree) {
		this.layerTree = layerTree;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the activeTools
	 */
	public List<Integer> getActiveTools() {
		return activeTools;
	}

	/**
	 * @param activeTools the activeTools to set
	 */
	public void setActiveTools(List<Integer> activeTools) {
		this.activeTools = activeTools;
	}



}
