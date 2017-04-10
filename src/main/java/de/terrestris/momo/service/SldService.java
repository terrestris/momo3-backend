package de.terrestris.momo.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.GeoserverPublisherDao;
import de.terrestris.momo.dao.GeoserverReaderDao;
import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.model.layer.source.TileWmsLayerDataSource;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import javassist.NotFoundException;

/**
 *
 * @author Kai Volland
 * @author terrestris GmbH & Co. KG
 *
 */
@Service("sldService")
public class SldService {

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(SldService.class);

	@Autowired
	private GeoserverPublisherDao geoserverPublisherDao;

	@Autowired
	private GeoserverReaderDao geoserverReaderDao;

	@Autowired
	@Qualifier("momoLayerService")
	private MomoLayerService<MomoLayer, MomoLayerDao<MomoLayer>> momoLayerService;

	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName()) or "
			+ "hasPermission(#layerId, 'de.terrestris.momo.model.MomoLayer', 'UPDATE')")
	public void updateSld(Integer layerId, String sldName, String sld) throws Exception {
		LOG.info("Updating " + sldName);

		MomoLayer layer = momoLayerService.findById(layerId);

		if (layer == null) {
			String msg = "Could not find a layer with ID " + layerId;
			LOG.error(msg);
			throw new NotFoundException(msg);
		}
		String layerName = null;

		String layerSourceType = layer.getSource().getType();

		if (layerSourceType.equalsIgnoreCase("TileWMS")) {
			TileWmsLayerDataSource layerSource = (TileWmsLayerDataSource) layer.getSource();
			layerName = layerSource.getLayerNames();
		}

		if (layerName == null) {
			String msg = "Could not detect the layerName of layer with ID " + layerId;
			LOG.error(msg);
			throw new NotFoundException(msg);
		}

		RESTLayer gsLayer = geoserverReaderDao.getLayer(layerName.split(":")[0], layerName.split(":")[1]);

		String gsDefaultStyle = gsLayer.getDefaultStyle();

		if (!gsDefaultStyle.equalsIgnoreCase(sldName)) {
			String msg = "Layer styles do not match!";
			LOG.error(msg);
			throw new Exception(msg);
		}

		geoserverPublisherDao.updateStyle(sld, sldName);
	}

}
