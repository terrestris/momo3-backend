/**
 *
 */
package de.terrestris.momo.security.access.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import de.terrestris.momo.dao.LayerTreeDao;
import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.tree.LayerTreeFolder;
import de.terrestris.momo.service.LayerTreeService;
import de.terrestris.momo.service.MomoApplicationService;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class LayerTreeFolderPermissionEvaluator<E extends LayerTreeFolder> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public LayerTreeFolderPermissionEvaluator() {
		this((Class<E>) LayerTreeFolder.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected LayerTreeFolderPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	@Autowired
	@Qualifier("momoApplicationService")
	private MomoApplicationService<MomoApplication, MomoApplicationDao <MomoApplication>> momoApplicationService;

	@Autowired
	@Qualifier("layerTreeService")
	private LayerTreeService<TreeNode, LayerTreeDao <TreeNode>> layerTreeService;

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E entity, Permission permission) {

		// always grant READ right for this entity
		if (permission.equals(Permission.READ)) {
			LOG.trace("Granting READ for TreeNode.");
			return true;
		}

		// always grant CREATE right for this entity
		if (permission.equals(Permission.CREATE)) {
			LOG.trace("Granting CREATE for TreeNode.");
			return true;
		}

		// for UPDATE and DELETE
		// check if LayerTreeFolder is contained in any application the user is allowed to see.
		// this includes also the applications tagged as 'open'
		List<MomoApplication> momoApplications = momoApplicationService.findAll();
		for (MomoApplication momoApp : momoApplications) {
			List<LayerTreeFolder> allFolders;
			try {
				allFolders = getAllLayerTreeFolders(momoApp.getLayerTree());
				if (allFolders.contains(entity)) {
					// got a match for this leaf in a (READ) allowed application
					MomoApplicationPermissionEvaluator<MomoApplication> pe =
							new MomoApplicationPermissionEvaluator<MomoApplication>();
					// check if the user has the UPDATE permission on the application for this layertreefolder
					// and if so, grant the permission (may be delete for tree removal or update for rename)
					// to the layertreefolder entity
					Permission permissionToTest = Permission.UPDATE;
					boolean hasPermissionOnTreeLeaf = pe.hasPermission(user, momoApp, permissionToTest);
					if (hasPermissionOnTreeLeaf) {
						return true;
					}
				}
			} catch (Exception e) {
				// quiet
			}
		}

		// call parent implementation from SHOGun2
		return super.hasPermission(user, entity, permission);
	}

	@Transactional(readOnly = true)
	private List<LayerTreeFolder> getAllLayerTreeFolders(LayerTreeFolder treeFolder) throws Exception {
		List<LayerTreeFolder> layerTreeFolderList = new ArrayList<LayerTreeFolder>();
		if (treeFolder == null) {
			return layerTreeFolderList;
		}

		List<TreeNode> children = treeFolder.getChildren();

		for (TreeNode treeNode : children) {

			if (treeNode instanceof LayerTreeFolder) {
				layerTreeFolderList.add((LayerTreeFolder) treeNode);
				// recursive call
				layerTreeFolderList.addAll(getAllLayerTreeFolders((LayerTreeFolder) treeNode));
			}
		}

		return layerTreeFolderList;
	}

}
