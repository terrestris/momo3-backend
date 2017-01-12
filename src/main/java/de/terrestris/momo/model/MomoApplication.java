package de.terrestris.momo.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.terrestris.momo.model.tree.DocumentTreeFolder;
import de.terrestris.momo.model.tree.LayerTreeFolder;
import de.terrestris.shogun2.model.Application;
import de.terrestris.shogun2.model.module.Module;

/**
 *
 * @author Nils Bühner
 *
 */
@Entity
public class MomoApplication extends Application {

	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@ManyToMany
	@JoinTable(
		name = "MOMOAPPS_DOCROOTNODES",
		joinColumns = { @JoinColumn(name = "MOMOAPP_ID") },
		inverseJoinColumns = { @JoinColumn(name = "DOCROOTNODE_ID") }
	)
	@OrderColumn(name = "IDX")
	@JsonIdentityInfo(
			generator = ObjectIdGenerators.PropertyGenerator.class,
			property = "id"
			)
	@JsonIdentityReference(alwaysAsId = true)
	private List<DocumentTreeFolder> documentRootNodes = new ArrayList<DocumentTreeFolder>();

	/**
	 *
	 */
	@ManyToOne
	@JoinColumn(name="LAYERTREE_ROOTNODE_ID")
	private LayerTreeFolder layerTree;

	/**
	 *
	 */
	@ManyToMany
	@JoinTable(
		name = "MOMOAPPS_ACTIVETOOLS",
		joinColumns = { @JoinColumn(name = "MOMOAPP_ID") },
		inverseJoinColumns = { @JoinColumn(name = "ACTIVETOOL_ID") }
	)
	@OrderColumn(name = "IDX")
	private List<Module> activeTools = new ArrayList<Module>();

	/**
	 *
	 */
	public MomoApplication() {
	}

	/**
	 * @return the documentRootNodes
	 */
	public List<DocumentTreeFolder> getDocumentRootNodes() {
		return documentRootNodes;
	}

	/**
	 * @param documentRootNodes the documentRootNodes to set
	 */
	public void setDocumentRootNodes(List<DocumentTreeFolder> documentRootNodes) {
		this.documentRootNodes = documentRootNodes;
	}

	/**
	 * @return the layerTree
	 */
	public LayerTreeFolder getLayerTree() {
		return layerTree;
	}

	/**
	 * @param layerTree the layerTree to set
	 */
	public void setLayerTree(LayerTreeFolder layerTree) {
		this.layerTree = layerTree;
	}

	/**
	 * @return the activeTools
	 */
	public List<Module> getActiveTools() {
		return activeTools;
	}

	/**
	 * @param activeTools the activeTools to set
	 */
	public void setActiveTools(List<Module> activeTools) {
		this.activeTools = activeTools;
	}

}
