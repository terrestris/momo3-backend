/**
 *
 */
package de.terrestris.momo.util;

import java.util.Comparator;

import de.terrestris.momo.model.tree.DocumentTreeFolder;

/**
 * @author Nils Bühner
 *
 */
public class DocumentTreeFolderComparator implements Comparator<DocumentTreeFolder> {

	/**
	 * Sorts a list of {@link DocumentTreeFolder}s from [bb, BB, aa, AA] to [AA,
	 * aa, BB, bb], i.e. uppercase will before lowercase, which is not possible
	 * with default compareTo or compareToIgnoreCase implementations.
	 *
	 * Credits go to http://stackoverflow.com/a/16424868
	 */
	@Override
	public int compare(DocumentTreeFolder doc1, DocumentTreeFolder doc2) {
		final String doc1Name = doc1.getText();
		final String doc2Name = doc2.getText();

		// Case-insensitive check
		int comp = doc1Name.compareToIgnoreCase(doc2Name);

		// If case-insensitive different, no need to check case
		if(comp != 0) {
			return comp;
		}

		return doc1Name.compareTo(doc2Name);
	}

}
