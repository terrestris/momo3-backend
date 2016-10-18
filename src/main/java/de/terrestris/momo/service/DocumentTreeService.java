package de.terrestris.momo.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import de.terrestris.momo.dao.DocumentTreeDao;
import de.terrestris.momo.model.tree.DocumentTreeFolder;
import de.terrestris.momo.model.tree.DocumentTreeLeaf;
import de.terrestris.shogun2.dao.FileDao;
import de.terrestris.shogun2.model.File;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.service.FileService;
import de.terrestris.shogun2.service.TreeNodeService;

/**
 *
 * @author Nils Bühner
 *
 * @param <E>
 * @param <D>
 */
@Service("docTreeService")
public class DocumentTreeService<E extends TreeNode, D extends DocumentTreeDao<E>> extends
		TreeNodeService<E, D> {

	@Autowired
	private FileService<File, FileDao<File>> fileService;

	/**
	 * @throws Exception
	 *
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())")
	public void attachDocumentToNode(DocumentTreeLeaf node, MultipartFile documentUpload) throws Exception {

		InputStream is = null;
		File document = node.getDocument();

		if(document == null) {
			// create a new instance
			document = new File();
		}

		try {
			is = documentUpload.getInputStream();
			byte[] docByteArray = IOUtils.toByteArray(is);

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
		} else if(node instanceof DocumentTreeFolder) {
			// we have a FOLDER

			DocumentTreeFolder folder = (DocumentTreeFolder) node;
			// Credits go to:
			// https://pdfbox.apache.org/
			// http://stackoverflow.com/a/4874334

			PDFMergerUtility pdfMerger = new PDFMergerUtility();
			ByteArrayOutputStream docOutputStream = new ByteArrayOutputStream();
			List<byte []> orderedFolderDocs = getAllDocumentsOfFolder(folder);

			for (byte[] doc : orderedFolderDocs) {
				pdfMerger.addSource(new ByteArrayInputStream(doc));
			}

			// merge and write the pdf
			pdfMerger.setDestinationStream(docOutputStream);
			pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

			fileToReturn = new File();
			fileToReturn.setFile(docOutputStream.toByteArray());
			fileToReturn.setFileName(folder.getText() + ".pdf");
			fileToReturn.setFileType("application/pdf");

		} else if(node instanceof DocumentTreeLeaf) {
			// we have a LEAF

			DocumentTreeLeaf leaf = (DocumentTreeLeaf) node;

			fileToReturn = leaf.getDocument();

		} else {
			final String msg = "Unexpected treeNode type!";
			LOG.error(msg);
			throw new Exception(msg);
		}

		return fileToReturn;

	}

	/**
	 *
	 * @param folder
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasRole(@configHolder.getDefaultUserRoleName())")
	private List<byte[]> getAllDocumentsOfFolder(DocumentTreeFolder folder) throws Exception {

		List<byte[]> documentList = new ArrayList<byte[]>();

		List<TreeNode> children = folder.getChildren();

		for (TreeNode treeNode : children) {

			if(treeNode instanceof DocumentTreeFolder) {

				// recursive call
				documentList.addAll(getAllDocumentsOfFolder((DocumentTreeFolder) treeNode));

			} else if(treeNode instanceof DocumentTreeLeaf) {

				// we have a leaf -> add attached doc
				DocumentTreeLeaf leaf = (DocumentTreeLeaf) treeNode;

				final String leafName = leaf.getText();
				final File document = leaf.getDocument();

				if(document != null) {
					documentList.add(document.getFile());
					LOG.debug("Collected the document of node '" + leafName + "' to merge it into one pdf finally.");
				} else {
					LOG.warn("Skipping the merge of document of node '" + leafName
							+ "' as there is no attached document.");
				}

			} else {
				throw new Exception("Unknown node type!");
			}
		}

		return documentList;
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
	@Qualifier("docTreeDao")
	public void setDao(D dao) {
		super.setDao(dao);
	}
}