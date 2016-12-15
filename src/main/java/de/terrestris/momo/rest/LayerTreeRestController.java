package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.LayerTreeDao;
import de.terrestris.momo.service.LayerTreeService;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.rest.TreeNodeRestController;

/**
 *
 * @author Nils Bühner
 * @author Daniel Koch
 *
 */
@RestController
@RequestMapping("/layertree")
public class LayerTreeRestController<E extends TreeNode, D extends LayerTreeDao<E>, S extends LayerTreeService<E, D>>
		extends TreeNodeRestController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public LayerTreeRestController() {
		this((Class<E>) TreeNode.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected LayerTreeRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("layerTreeService")
	public void setService(S service) {
		this.service = service;
	}

}
