package de.terrestris.momo.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.terrestris.momo.dao.GeoserverPublisherDao;
import de.terrestris.momo.dao.GeoserverReaderDao;
import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.model.layer.source.TileWmsLayerDataSource;
import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.model.Response;
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
	public Response updateLegendSrc(Integer layerId, Integer width, Integer height, String imgUrl, String format) throws NotFoundException, URISyntaxException, HttpException {

		String gsDefaultStyle = null;

		try {
			gsDefaultStyle = this.getDefaultStyleForLayer(layerId);
		} catch (Exception e) {
			LOG.error("Error while getting default style for layer: " + e.getMessage());
		}

		try {
			Map<String, Object> legendMap = new HashMap<>();
			Map<String, Map<String, Object>> resultMap = new HashMap<>();
			Map<String, Object> styleMap = new HashMap<>();

			// first we check if the static legend shall be removed, this can be detected by
			// empty values in imgUrl and format. when empty, we need to post an empty legend object
			if (!StringUtils.isEmpty(imgUrl)) {
				// add a static legend
				// TODO move this to properties file
				imgUrl = "http://momo-shogun:8080/momo" + imgUrl;
				// fix to avoid "not supported format" error in GeoServer since a check against a valid
				// image extension will be performed by PUTting
				// s. https://github.com/geoserver/geoserver/blob/22e3c7a2adc3bd5f40cf9a675081e32a95e37fa7/src/web/wms/src/main/java/org/geoserver/wms/web/data/ExternalGraphicPanel.java#L100
				imgUrl += "&format=.";
				imgUrl += format.split("/")[1];
				legendMap.put("format", format);
				legendMap.put("height", height);
				legendMap.put("width", width);
				legendMap.put("onlineResource", imgUrl);
			}

			styleMap.put("legend", legendMap);
			resultMap.put("style", styleMap);

			ObjectMapper mapperObj = new ObjectMapper();
			String legendSrcJson = null;
			legendSrcJson = mapperObj.writeValueAsString(resultMap);

			String url = geoServerBaseUrl.split("/momo/ows")[0] + "/rest/styles/" + gsDefaultStyle;
			LOG.info("Start updating legend source...");

			return HttpUtil.put(url, legendSrcJson, ContentType.APPLICATION_JSON, gsuser, gspassword);

		} catch (Exception e) {
			LOG.error("Could not update legend source for layer: " + e.getMessage());
			return null;
		}

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
