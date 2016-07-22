package de.terrestris.momo.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.terrestris.momo.dao.RbmaDao;
import de.terrestris.momo.model.tree.RbmaTreeFolder;
import de.terrestris.momo.model.tree.RbmaTreeLeaf;
import de.terrestris.momo.service.RbmaService;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.rest.TreeNodeRestController;
import de.terrestris.shogun2.util.data.ResultSet;

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

	/**
	 * Attaches a document to a (RBMA leaf) node
	 *
	 * @param uploadedDoc
	 * @return
	 */
	@RequestMapping(value = "/{nodeId}/doc", method = RequestMethod.POST)
	public ResponseEntity<String> uploadFile(
			@PathVariable Integer nodeId,
			@RequestParam("file") MultipartFile uploadedDoc) {

		LOG.debug("Requested to upload an document for node " + nodeId);

		// prepare response
		Map<String, Object> responseMap = new HashMap<String, Object>();
		final HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus responseStatus = HttpStatus.OK;
		String responseMapAsString = null;
		ObjectMapper mapper = new ObjectMapper();

		// we have to return the response-Map as String to be browser conform.
		// as this controller is typically being called by a form.submit() the
		// browser expects a response with the Content-Type header set to
		// "text/html".
		responseHeaders.setContentType(MediaType.TEXT_HTML);

		// 1. check for valid doc
		if (uploadedDoc.isEmpty()) {
			final String msg = "Upload failed. Document " +
					uploadedDoc.getOriginalFilename() + " is empty.";
			LOG.error(msg);
			responseMap = ResultSet.error(msg);
		}

		// 2. check for valid node
		E treeNode = this.service.findById(nodeId);

		// 2.1 check if node is a folder, which would not be allowed
		if(treeNode instanceof RbmaTreeFolder) {
			final String msg = "Documents can not be attached to folders!";
			LOG.error(msg);
			responseMap = ResultSet.error(msg);
		}

		// 2.2 check if node is an RBMA leaf
		if(!(treeNode instanceof RbmaTreeLeaf)) {
			final String msg = "Unexpected treeNode type!";
			LOG.error(msg);
			responseMap = ResultSet.error(msg);
		} else {
			// treeNode is instanceof RbmaTreeLeaf -> Let's go!
			RbmaTreeLeaf rbmaLeaf = (RbmaTreeLeaf) treeNode;

			// attach document
			try {
				this.service.attachDocumentToNode(rbmaLeaf, uploadedDoc);

				LOG.info("Successfully attached a document to a node.");
				responseMap = ResultSet.success(rbmaLeaf);

			} catch (Exception e) {
				final String msg = "Could not attach a document to a node: " + e.getMessage();
				LOG.error(msg);
				responseMap = ResultSet.error(msg);
			}
		}

		// rewrite the response-Map as String
		try {
			responseMapAsString = mapper.writeValueAsString(responseMap);
		} catch (JsonProcessingException e) {
			LOG.error("Error while rewriting the response Map to a String" +
					e.getMessage());
			responseMap = ResultSet.error("Error while rewriting the " +
					"response Map to a String" + e.getMessage());
		}

		return new ResponseEntity<String>(responseMapAsString, responseHeaders, responseStatus);
	}

}
