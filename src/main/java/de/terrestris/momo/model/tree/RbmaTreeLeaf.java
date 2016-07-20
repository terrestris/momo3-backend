/**
 *
 */
package de.terrestris.momo.model.tree;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import de.terrestris.shogun2.model.tree.TreeNode;

/**
 * @author rieger@terrestris.de
 *
 */
@Entity
@Table
public class RbmaTreeLeaf extends TreeNode {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Column
	private String pdfFile;

	public RbmaTreeLeaf() {

	}

	/**
	 * @return the pdfFile
	 */
	public String getPdfFile() {
		return pdfFile;
	}

	/**
	 * @param pdfFile the pdfFile to set
	 */
	public void setPdfFile(String pdfFile) {
		this.pdfFile = pdfFile;
	}

}
