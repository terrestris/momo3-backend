package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.RbmaDao;
import de.terrestris.momo.service.RbmaService;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.rest.TreeNodeRestController;

/**
 * This is a demo controller that demonstrates how SHOGun2 REST controllers
 * can be extended.
 *
 * @author Nils Bühner
 *
 */
@RestController
@RequestMapping("/rbma")
public class RbmaRestController<E extends TreeNode, D extends RbmaDao<E>, S extends RbmaService<E, D>>
		extends TreeNodeRestController<E, D, S> {

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("rbmaService")
	public void setService(S service) {
		this.service = service;
	}

	/**
	 * Get an entity by id.
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/root", method = RequestMethod.GET)
	public ResponseEntity<E> getRoot() {

		try {
			// TODO get dynamic to determine ID
			E entity = this.service.findById(678);
			LOG.trace("Found " + entity.getClass().getSimpleName()
					+ " with ID " + entity.getId());
			return new ResponseEntity<E>(entity, HttpStatus.OK);
		} catch (Exception e) {
			LOG.error("Error finding rootNode for RBMA:" + e.getMessage());
			return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
		}
	}


}
