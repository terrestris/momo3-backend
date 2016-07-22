/**
 *
 */
package de.terrestris.momo.model.tree;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.terrestris.shogun2.model.File;
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

	/**
	 *
	 */
	@ManyToOne
	@JoinColumn(name="DOCUMENT_ID")
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id"
	)
	@JsonIdentityReference(alwaysAsId = true)
	private File document;

	/**
	 *
	 */
	public RbmaTreeLeaf() {

	}

	/**
	 * @return the document
	 */
	public File getDocument() {
		return document;
	}

	/**
	 * @param document the document to set
	 */
	public void setDocument(File document) {
		this.document = document;
	}

}
