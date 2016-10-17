/**
 *
 */
package de.terrestris.momo.model.tree;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import de.terrestris.shogun2.model.tree.TreeFolder;

/**
 * @author rieger@terrestris.de
 *
 */
@Entity
@Table
public class DocumentTreeFolder extends TreeFolder {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Column
	private String pdfFile;

	/**
	 * 
	 */
	public DocumentTreeFolder() {

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
