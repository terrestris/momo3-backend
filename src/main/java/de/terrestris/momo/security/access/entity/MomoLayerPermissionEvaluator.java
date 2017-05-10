/**
 *
 */
package de.terrestris.momo.security.access.entity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.momo.dao.LayerTreeDao;
import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.model.tree.LayerTreeFolder;
import de.terrestris.momo.service.LayerTreeService;
import de.terrestris.momo.service.MomoApplicationService;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.layer.Layer;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class MomoLayerPermissionEvaluator<E extends MomoLayer> extends MomoPersistentObjectPermissionEvaluator<E> {

	@Autowired
	@Qualifier("momoApplicationService")
	private MomoApplicationService<MomoApplication, MomoApplicationDao <MomoApplication>> momoApplicationService;

	@Autowired
	@Qualifier("layerTreeService")
	private LayerTreeService<TreeNode, LayerTreeDao <TreeNode>> layerTreeService;

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoLayerPermissionEvaluator() {
		this((Class<E>) MomoLayer.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MomoLayerPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Always grants right to READ, UPDATE and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E layer, Permission permission) {

		// all users but default users are allowed to create layers
		if (permission.equals(Permission.CREATE) &&
				(layer == null || layer.getId() == null) &&
				! MomoSecurityUtil.currentUsersHighestRoleIsDefaultUser()) {
			return true;
		}

		// permit always read on the osm-wms layer, as its needed in application
		// creation process...
		if (permission.equals(Permission.READ) && layer.getName() != null &&
				layer.getName().equalsIgnoreCase("OSM-WMS GRAY")) {
			return true;
		}

		boolean hasGrantedPermissions = hasDefaultMomoPermission(user, layer, permission);

		if (hasGrantedPermissions) {
			return true;
		}

		// check if layer is contained in any application the user is allowed to see
		List<MomoApplication> momoApplications = momoApplicationService.findAll();
		for (MomoApplication momoApp : momoApplications) {
			Integer layerTreeId = momoApp.getLayerTree().getId();
			LayerTreeFolder layerTreeRootNode = (LayerTreeFolder) layerTreeService.findById(layerTreeId);
			List<Layer> mapLayers = null;
			try {
				mapLayers = layerTreeService.getAllMapLayersFromTreeFolder(layerTreeRootNode);
			} catch (Exception e) {
				LOG.error("Could not fetch maplayers from referenced application. hasPermission will likely return false");
			}

			if (mapLayers != null && mapLayers.contains(layer)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return the momoApplicationService
	 */
	public MomoApplicationService<MomoApplication, MomoApplicationDao<MomoApplication>> getMomoApplicationService() {
		return momoApplicationService;
	}

	/**
	 * @param momoApplicationService the momoApplicationService to set
	 */
	public void setMomoApplicationService(
			MomoApplicationService<MomoApplication, MomoApplicationDao<MomoApplication>> momoApplicationService) {
		this.momoApplicationService = momoApplicationService;
	}

	/**
	 * @return the layerTreeService
	 */
	public LayerTreeService<TreeNode, LayerTreeDao<TreeNode>> getLayerTreeService() {
		return layerTreeService;
	}

	/**
	 * @param layerTreeService the layerTreeService to set
	 */
	public void setLayerTreeService(LayerTreeService<TreeNode, LayerTreeDao<TreeNode>> layerTreeService) {
		this.layerTreeService = layerTreeService;
	}

}
