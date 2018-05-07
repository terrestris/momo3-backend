package de.terrestris.momo.service;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

	/**
	 *
	 */
	@Value("${geoserver.baseUrl}")
	private String geoServerBaseUrl;

	/**
	 *
	 */
	@Value("${geoserver.username}")
	private String gsuser;

	/**
	 *
	 */
	@Value("${geoserver.password}")
	private String gspassword;


	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName()) or "
			+ "hasPermission(#layerId, 'de.terrestris.momo.model.MomoLayer', 'UPDATE')")
	public void updateSld(Integer layerId, String sldName, String sld) throws Exception {
		LOG.info("Updating " + sldName);

		String gsDefaultStyle = null;

		try {
			gsDefaultStyle = this.getDefaultStyleForLayer(layerId);
		} catch (Exception e) {
			LOG.error("Error while getting default style for layer: " + e.getMessage());
		}

		if (!gsDefaultStyle.equalsIgnoreCase(sldName)) {
			String msg = "Layer styles do not match!";
			LOG.error(msg);
			throw new Exception(msg);
		}

		geoserverPublisherDao.updateStyle(sld, sldName);
	}

	/**
	 *
	 * @param layerId
	 * @return
	 * @throws NotFoundException
	 */
	private String getDefaultStyleForLayer(Integer layerId) throws NotFoundException {
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

		return gsLayer.getDefaultStyle();
	}

	/**
	 *
	 * @param layerId
	 * @param height
	 * @param width
	 * @param imgUrl
	 * @param format
	 * @return
	 * @throws NotFoundException
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName()) or "
			+ "hasPermission(#layerId, 'de.terrestris.momo.model.MomoLayer', 'UPDATE')")
	public void updateLegendSrc(Integer layerId, Integer width, Integer height, String imgUrl, String format, HttpServletRequest request) throws NotFoundException, URISyntaxException, HttpException {
		LOG.info("Updating static legend for layer with id " + layerId);

		MomoLayer layer = momoLayerService.findById(layerId);
		if (layer == null) {
			String msg = "Could not find a layer with ID " + layerId;
			LOG.error(msg);
			throw new NotFoundException(msg);
		}
		if (StringUtils.isEmpty(imgUrl)) {
			layer.setFixLegendUrl(null);
		} else {
			if (imgUrl.indexOf("http") < 0) {
				// make relative URL absolute by adding our host
				String scheme = request.getScheme();
				String serverName = request.getServerName();
				int serverPort = request.getServerPort();
				String path = request.getServletContext().getContextPath();
				String baseUrl = scheme + "://" + serverName + ":" + serverPort + path;
				if (imgUrl.startsWith("/")) {
					imgUrl = baseUrl + imgUrl;
				} else {
					imgUrl = baseUrl + "/" + imgUrl;
				}
			}
			layer.setFixLegendUrl(imgUrl);
		}
		momoLayerService.saveOrUpdate(layer);
	}

	public void publishSLDAsDefault(Integer layerId, String sld) throws Exception {
		LOG.info("Creating SLD for layer with id " + layerId);

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
		String sldName = gsLayer.getDefaultStyle();
		geoserverPublisherDao.updateStyle(sld, sldName);
	}

}
