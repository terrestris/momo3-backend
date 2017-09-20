package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.service.MomoLayerService;
import de.terrestris.shogun2.rest.AbstractRestController;

/**
 *
 * @author Nils Bühner
 * @author Daniel Koch
 *
 */
@RestController
@RequestMapping("/momolayers")
public class MomoLayerRestController<E extends MomoLayer, D extends MomoLayerDao<E>, S extends MomoLayerService<E, D>>
		extends AbstractRestController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoLayerRestController() {
		this((Class<E>) MomoLayer.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoLayerRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoLayerService")
	public void setService(S service) {
		this.service = service;
	}

	@Override
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<E> delete(@PathVariable int id) {
		try {
			LOG.info("Trying to delete MomoLayer with ID " + id);
			MomoLayer entityToDelete = this.service.findById(id);
			this.service.deleteMomoLayer(entityToDelete);
			LOG.info("Deleted MomoLayer with ID " + id);
			return new ResponseEntity<E>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			LOG.error("Error deleting MomoLayer with ID " + id + ": "
					+ e.getMessage());
			return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
		}
	}
}
