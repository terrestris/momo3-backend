package de.terrestris.momo.util.serializer;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.terrestris.momo.dao.LayerTreeDao;
import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.dao.MomoUserDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.tree.LayerTreeFolder;
import de.terrestris.momo.security.access.entity.MomoPersistentObjectPermissionEvaluator;
import de.terrestris.momo.service.LayerTreeService;
import de.terrestris.momo.service.MomoApplicationService;
import de.terrestris.momo.service.MomoUserService;
import de.terrestris.momo.util.security.MomoSecurityUtil;
import de.terrestris.shogun2.model.layer.Layer;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.tree.TreeNode;

/**
 *
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @author Daniel Koch
 * @date 31.03.2017
 *
 * Custom serializer for instances of {@link MomoUser}
 */
public class MomoLayerSerializer extends StdSerializer<MomoLayer>{

	private static final long serialVersionUID = 1L;

	@Autowired
	@Qualifier("momoUserService")
	private MomoUserService<MomoUser, MomoUserDao <MomoUser>> momoUserService;

	@Autowired
	@Qualifier("momoApplicationService")
	private MomoApplicationService<MomoApplication, MomoApplicationDao <MomoApplication>> momoApplicationService;

	@Autowired
	@Qualifier("layerTreeService")
	private LayerTreeService<TreeNode, LayerTreeDao <TreeNode>> layerTreeService;

	/**
	 *
	 */
	public MomoLayerSerializer(){
		this(null);
	}

	public MomoLayerSerializer(Class<MomoLayer> t) {
		super(t);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}


	@Override
	public void serialize(MomoLayer momoLayer, JsonGenerator generator, SerializerProvider provider) throws IOException {
		generator.writeStartObject();

		generator.writeNumberField("id", momoLayer.getId());
		generator.writeBooleanField("chartable", momoLayer.getChartable() != null ? momoLayer.getChartable() : false);
		generator.writeStringField("dataType", momoLayer.getDataType() != null ? momoLayer.getDataType() : StringUtils.EMPTY);
		generator.writeStringField("description", momoLayer.getDescription() != null ? momoLayer.getDescription() : StringUtils.EMPTY);
		generator.writeBooleanField("hoverable", momoLayer.getHoverable() != null ? momoLayer.getHoverable() : false);
		generator.writeStringField("metadataIdentifier", momoLayer.getMetadataIdentifier() != null ? momoLayer.getMetadataIdentifier() : StringUtils.EMPTY);
		generator.writeStringField("name", momoLayer.getName());
		generator.writeBooleanField("spatiallyRestricted", momoLayer.getSpatiallyRestricted() != null ? momoLayer.getSpatiallyRestricted() : false);
		generator.writeObjectField("appearance", momoLayer.getAppearance());
		generator.writeObjectField("owner", momoLayer.getOwner());
		generator.writeObjectField("source", momoLayer.getSource());

		boolean readPermissionGrantedFromAnyApplication = false;
		MomoUser currentUser = momoUserService.getUserBySession();
		MomoPersistentObjectPermissionEvaluator<MomoLayer> permissionObjectEvaluator = new MomoPersistentObjectPermissionEvaluator<MomoLayer>();
		boolean userHasReadPermission = permissionObjectEvaluator.hasPermission(currentUser, momoLayer, Permission.READ);

		if (!userHasReadPermission && !MomoSecurityUtil.currentUserIsSuperAdmin()) {
			// check if layer is contained in any application the user is allowed to see
			List<MomoApplication> momoApplications = momoApplicationService.findAll();
			for (MomoApplication momoApp : momoApplications) {
				Integer layerTreeId = momoApp.getLayerTree().getId();
				LayerTreeFolder layerTreeRootNode = (LayerTreeFolder) layerTreeService.findById(layerTreeId);
				List<Layer> mapLayers = null;
				try {
					mapLayers = this.layerTreeService.getAllMapLayersFromTreeFolder(layerTreeRootNode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (mapLayers != null && mapLayers.contains(momoLayer)) {
					readPermissionGrantedFromAnyApplication = true;
				}
			}
		}

		generator.writeBooleanField("readPermissionGrantedFromAnyApplication", readPermissionGrantedFromAnyApplication);
		generator.writeEndObject();
	}

}
