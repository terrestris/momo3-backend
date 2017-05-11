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

import de.terrestris.momo.dao.DocumentTreeDao;
import de.terrestris.momo.model.tree.DocumentTreeFolder;
import de.terrestris.momo.model.tree.DocumentTreeLeaf;
import de.terrestris.momo.service.DocumentTreeService;
import de.terrestris.momo.util.config.MomoConfigHolder;
import de.terrestris.shogun2.model.File;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.rest.TreeNodeRestController;
import de.terrestris.shogun2.util.data.ResultSet;

/**
 *
 * @author Nils Bühner
 *
 */
@RestController
@RequestMapping("/doctree")
public class DocumentTreeRestController<E extends TreeNode, D extends DocumentTreeDao<E>, S extends DocumentTreeService<E, D>>
		extends TreeNodeRestController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public DocumentTreeRestController() {
		this((Class<E>) TreeNode.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected DocumentTreeRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("docTreeService")
	public void setService(S service) {
		this.service = service;
	}

	@Autowired
	@Qualifier("momoConfigHolder")
	private MomoConfigHolder momoConfigHolder;

	/**
	 * Attaches a document to a (doctree leaf) node
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
		if(treeNode instanceof DocumentTreeFolder) {
			final String msg = "Documents can not be attached to folders!";
			LOG.error(msg);
			responseMap = ResultSet.error(msg);
		}

		// 2.2 check if node is an doctree leaf
		if(!(treeNode instanceof DocumentTreeLeaf)) {
			final String msg = "Unexpected treeNode type!";
			LOG.error(msg);
			responseMap = ResultSet.error(msg);
		} else {
			// treeNode is instanceof DocumentTreeLeaf -> Let's go!
			DocumentTreeLeaf docTreeLeaf = (DocumentTreeLeaf) treeNode;

			// attach document
			try {
				this.service.attachDocumentToNode(docTreeLeaf, uploadedDoc);

				LOG.info("Successfully attached a document to a node.");
				responseMap = ResultSet.success(docTreeLeaf);

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

	/**
	 *
	 * @return
	 */
	@RequestMapping(value = "/{nodeId}/doc", method = RequestMethod.GET)
	public ResponseEntity<?> downloadFile(@PathVariable Integer nodeId) {
		final HttpHeaders responseHeaders = new HttpHeaders();

		try {
			// try to get the doc
			File doc = this.service.getDocumentOfNode(nodeId);

			if(doc == null) {
				throw new Exception("There is no document for node: " + nodeId);
			}

			responseHeaders.setContentType(MediaType.parseMediaType(doc.getFileType()));

			LOG.info("Successfully got the doc of a node: " + doc.getFileName());

			return new ResponseEntity<byte[]>(doc.getFile(), responseHeaders, HttpStatus.OK);

		} catch (Exception e) {
			Map<String, Object> responseMap = new HashMap<String, Object>();

			final String errorMessage = "Could not get the document of a node: " + e.getMessage();

			LOG.error(errorMessage);
			responseMap = ResultSet.error(errorMessage);
			responseHeaders.setContentType(MediaType.APPLICATION_JSON);

			return new ResponseEntity<Map<String, Object>>(responseMap, responseHeaders, HttpStatus.NOT_FOUND);
		}
	}

}
