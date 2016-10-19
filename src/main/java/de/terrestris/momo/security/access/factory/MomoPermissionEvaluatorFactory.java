/**
 *
 */
package de.terrestris.momo.security.access.factory;

import de.terrestris.momo.model.tree.DocumentTreeFolder;
import de.terrestris.momo.model.tree.DocumentTreeLeaf;
import de.terrestris.momo.security.access.entity.DocumentTreeFolderPermissionEvaluator;
import de.terrestris.momo.security.access.entity.DocumentTreeLeafPermissionEvaluator;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.security.access.entity.PersistentObjectPermissionEvaluator;
import de.terrestris.shogun2.security.access.factory.EntityPermissionEvaluatorFactory;

/**
 *
 * This class has to be configured to be used as the permissionEvaluator (of
 * SHOGun2) in the security XML of this project.
 *
 * @author Nils Bühner
 *
 */
public class MomoPermissionEvaluatorFactory<E extends PersistentObject> extends EntityPermissionEvaluatorFactory<E> {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PersistentObjectPermissionEvaluator<E> getEntityPermissionEvaluator(
			final Class<E> entityClass) {

		if(DocumentTreeFolder.class.isAssignableFrom(entityClass)) {
			return new DocumentTreeFolderPermissionEvaluator();
		}
		if(DocumentTreeLeaf.class.isAssignableFrom(entityClass)) {
			return new DocumentTreeLeafPermissionEvaluator();
		}

		// call SHOGun2 implementation otherwise
		return super.getEntityPermissionEvaluator(entityClass);

	}

}
