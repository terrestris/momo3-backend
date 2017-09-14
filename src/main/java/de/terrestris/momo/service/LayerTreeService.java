package de.terrestris.momo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.terrestris.momo.dao.LayerTreeDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.model.tree.LayerTreeFolder;
import de.terrestris.momo.model.tree.LayerTreeLeaf;
import de.terrestris.shogun2.model.layer.Layer;
import de.terrestris.shogun2.model.tree.TreeFolder;
import de.terrestris.shogun2.model.tree.TreeNode;
import de.terrestris.shogun2.service.TreeNodeService;

/**
 *
 * @author Nils Bühner
 * @author Daniel Koch
 *
 * @param <E>
 * @param <D>
 */
@Service("layerTreeService")
public class LayerTreeService<E extends TreeNode, D extends LayerTreeDao<E>> extends
		TreeNodeService<E, D> {

	/**
	 * In case of a folder: Delete from "bottom up" by stepping down to the
	 * children and removing them first.
	 *
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	@Override
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName()) or hasPermission(#e, 'DELETE')")
	public void delete(E e) {

		if(e instanceof TreeFolder) {
			List<E> children = (List<E>) ((TreeFolder) e).getChildren();
			for (E childNode : children) {
				this.delete(childNode);
			}
		}

		dao.delete(e);
	}

	/**
	 *
	 * @param treeFolder
	 * @return
	 * @throws Exception
	 */
	@Transactional(readOnly = true)
	public List<Layer> getAllMapLayersFromTreeFolder(LayerTreeFolder treeFolder) throws Exception {

		List<Layer> mapLayerList = new ArrayList<Layer>();
		if (treeFolder == null) {
			return mapLayerList;
		}

		List<TreeNode> children = treeFolder.getChildren();

		for (TreeNode treeNode : children) {

			if (treeNode instanceof LayerTreeFolder) {

				// recursive call
				mapLayerList.addAll(getAllMapLayersFromTreeFolder((LayerTreeFolder) treeNode));

			} else if (treeNode instanceof LayerTreeLeaf) {

				// we have a leaf -> add attached mapLayer
				LayerTreeLeaf leaf = (LayerTreeLeaf) treeNode;

				final MomoLayer mapLayer = leaf.getLayer();

				mapLayerList.add(mapLayer);

			} else {
				throw new Exception("Unknown node type!");
			}
		}

		return mapLayerList;
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("layerTreeDao")
	public void setDao(D dao) {
		super.setDao(dao);
	}
}