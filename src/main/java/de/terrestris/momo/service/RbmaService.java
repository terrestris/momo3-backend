package de.terrestris.momo.service;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import de.terrestris.momo.model.tree.RbmaTreeFolder;
import de.terrestris.momo.model.tree.RbmaTreeLeaf;
import de.terrestris.shogun2.dao.FileDao;
import de.terrestris.shogun2.dao.TreeNodeDao;
import de.terrestris.shogun2.model.File;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.service.FileService;
import de.terrestris.shogun2.service.TreeNodeService;

/**
 * This is a demo service that demonstrates how a SHOGun2 service can be
 * extended.
 *
 * @author Nils Bühner
 *
 * @param <E>
 * @param <D>
 */
@Service("rbmaService")
public class RbmaService<E extends TreeNode, D extends TreeNodeDao<E>> extends
		TreeNodeService<E, D> {

	@Autowired
	private FileService<File, FileDao<File>> fileService;

	/**
	 * @throws Exception
	 *
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())")
	public void attachDocumentToNode(RbmaTreeLeaf node, MultipartFile documentUpload) throws Exception {

		InputStream is = null;
		File document = null;

		try {
			is = documentUpload.getInputStream();
			byte[] docByteArray = IOUtils.toByteArray(is);

			// create a new instance (generic)
			document = new File();

			// set binary doc data
			document.setFile(docByteArray);

			// set metadata
			document.setFileType(documentUpload.getContentType());
			document.setFileName(documentUpload.getOriginalFilename());

			// save the document
			fileService.saveOrUpdate(document);

			// attach it to the node and save the node
			node.setDocument(document);
			this.saveOrUpdate((E) node);

		} catch(Exception e) {
			throw new Exception("Could not attach document to node: " + e.getMessage());
		} finally {
			IOUtils.closeQuietly(is);
		}

	}

	/**
	 *
	 * In case of leafs: simply returns the attached doc.
	 * In case of folders: concatenates the docs of all children.
	 *
	 * @param nodeId
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasRole(@configHolder.getDefaultUserRoleName())")
	public File getDocumentOfNode(Integer nodeId) throws Exception {

		File fileToReturn = null;

		// get node from DB
		E node = this.findById(nodeId);

		if(node == null) {
			final String msg = "Node does not exist: " + nodeId;
			LOG.error(msg);
			throw new Exception(msg);
		} else if(node instanceof RbmaTreeFolder) {
			// we have a FOLDER

			RbmaTreeFolder folder = (RbmaTreeFolder) node;
			// TODO concatenate docs of all children by recursion.
			// The final doc should be embedded in a new instance.
			// This seems to be helpful:
			// https://pdfbox.apache.org/
			// http://stackoverflow.com/a/4874334

		} else if(node instanceof RbmaTreeLeaf) {
			// we have a LEAF

			RbmaTreeLeaf leaf = (RbmaTreeLeaf) node;

			fileToReturn = leaf.getDocument();

		} else {
			final String msg = "Unexpected treeNode type!";
			LOG.error(msg);
			throw new Exception(msg);
		}

		return fileToReturn;

	}

	/**
	 * @return the fileService
	 */
	public FileService<File, FileDao<File>> getFileService() {
		return fileService;
	}

	/**
	 * @param fileService the fileService to set
	 */
	public void setFileService(FileService<File, FileDao<File>> fileService) {
		this.fileService = fileService;
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("rbmaDao")
	public void setDao(D dao) {
		super.setDao(dao);
	}
}