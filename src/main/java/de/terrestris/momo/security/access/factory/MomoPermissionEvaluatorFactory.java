/**
 *
 */
package de.terrestris.momo.security.access.factory;

import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.tree.DocumentTreeFolder;
import de.terrestris.momo.model.tree.DocumentTreeLeaf;
import de.terrestris.momo.security.access.entity.ApplicationPermissionEvaluator;
import de.terrestris.momo.security.access.entity.DocumentTreeFolderPermissionEvaluator;
import de.terrestris.momo.security.access.entity.DocumentTreeLeafPermissionEvaluator;
import de.terrestris.momo.security.access.entity.LayerAppearancePermissionEvaluator;
import de.terrestris.momo.security.access.entity.MomoLayerPermissionEvaluator;
import de.terrestris.momo.security.access.entity.MomoUserGroupPermissionEvaluator;
import de.terrestris.momo.security.access.entity.MomoUserPermissionEvaluator;
import de.terrestris.momo.security.access.entity.TreeNodePermissionEvaluator;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.layer.appearance.LayerAppearance;
import de.terrestris.shogun2.model.tree.TreeNode;
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
		if(MomoLayer.class.isAssignableFrom(entityClass)) {
			return new MomoLayerPermissionEvaluator();
		}
		if(LayerAppearance.class.isAssignableFrom(entityClass)) {
			return new LayerAppearancePermissionEvaluator();
		}
		if(MomoApplication.class.isAssignableFrom(entityClass)) {
			return new ApplicationPermissionEvaluator();
		}
		if(TreeNode.class.isAssignableFrom(entityClass)) {
			return new TreeNodePermissionEvaluator();
		}
		if(MomoUserGroup.class.isAssignableFrom(entityClass)) {
			return new MomoUserGroupPermissionEvaluator();
		}
		if(MomoUser.class.isAssignableFrom(entityClass)) {
			return new MomoUserPermissionEvaluator();
		}

		// call SHOGun2 implementation otherwise
		return super.getEntityPermissionEvaluator(entityClass);

	}

}
