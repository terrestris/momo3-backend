package de.terrestris.momo.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.tree.DocumentTreeFolder;
import de.terrestris.momo.service.MomoApplicationService;
import de.terrestris.shogun2.rest.ApplicationRestController;

/**
 *
 * @author Nils Bühner
 *
 */
@RestController
@RequestMapping("/momoapps")
public class MomoApplicationRestController<E extends MomoApplication, D extends MomoApplicationDao<E>, S extends MomoApplicationService<E, D>>
		extends ApplicationRestController<E, D, S> {

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoApplicationService")
	public void setService(S service) {
		this.service = service;
	}

	/**
	 * Get all document root nodes of this application
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/documentRoots", method = RequestMethod.GET)
	public ResponseEntity<?> getDocumentRoots(@PathVariable Integer id) {

		try {
			List<Map<String, Object>> docTreeRootNodesInfo = service.getDocumentTreeRootNodeInfo(id);
			return new ResponseEntity<>(docTreeRootNodesInfo, HttpStatus.OK);
		} catch (Exception e) {
			LOG.error("Error finding documentRoots for momo app with ID " + id + ": " + e.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Create a new document root node for this app
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/documentRoots", method = RequestMethod.POST)
	public ResponseEntity<?> createNewDocumentRoot(@PathVariable Integer id, @RequestParam(value="name") String name) {

		try {
			DocumentTreeFolder newRootNode = service.createNewDocumentRoot(id, name);
			return new ResponseEntity<>(newRootNode, HttpStatus.OK);
		} catch (Exception e) {
			HttpStatus httpStatusCode = HttpStatus.NOT_FOUND;
			if(e instanceof AccessDeniedException) {
				httpStatusCode = HttpStatus.FORBIDDEN;
			}
			LOG.error("Error creating a new document roots for momo app with ID " + id + ": " + e.getMessage());
			return new ResponseEntity<>(httpStatusCode);
		}
	}

}
