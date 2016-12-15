/**
 *
 */
package de.terrestris.momo.model.tree;

import javax.persistence.Entity;
import javax.persistence.Table;

import de.terrestris.shogun2.model.tree.TreeFolder;

/**
 * @author Nils Bühner
 *
 */
@Entity
@Table
public class LayerTreeFolder extends TreeFolder {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public LayerTreeFolder() {
		this.setExpanded(true);
	}

}
