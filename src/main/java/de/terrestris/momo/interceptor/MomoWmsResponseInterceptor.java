package de.terrestris.momo.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.dao.ImageFileDao;
import de.terrestris.shogun2.model.ImageFile;
import de.terrestris.shogun2.util.interceptor.MutableHttpServletRequest;
import de.terrestris.shogun2.util.interceptor.WmsResponseInterceptorInterface;
import de.terrestris.shogun2.util.model.Response;

/**
 *
 * @author terrestris GmbH & Co. KG
 *
 */
public class MomoWmsResponseInterceptor implements WmsResponseInterceptorInterface {

	/**
	 * The Logger.
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MomoWmsResponseInterceptor.class);

	/**
	 *
	 */
	@Value("${momo.maskingColorHex}")
	private String maskingColorHex;

	/**
	 *
	 */
	@Value("${momo.maskingColorTolerance}")
	private int maskingColorTolerance;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoLayerDao")
	private MomoLayerDao<MomoLayer> momoLayerDao;

	/**
	 *
	 */
	@Value("${momo.publicInterceptGeoServerAction}")
	protected String geoserverInterceptorUrl;

	/**
	 *
	 */
	@Autowired
	@Qualifier("imageFileDao")
	private ImageFileDao<ImageFile> imageFileDao;

	/**
	 *
	 */
	@Override
	public Response interceptGetMap(MutableHttpServletRequest mutableRequest, Response response) {

		// This code has been taken on hold as it currently modifies all
		// wms responses and makes e.g. raster data look ugly due to
		// wrong set transparencies. In the future this should be applied
		// on layers that really need to be restricted and the tolerance
		// value should be investigated further, as it currently also matches
		// gray values even if the color to match is a kind of pink...

//		byte[] inputBytes = response.getBody();
//
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		byte[] outputBytes = null;
//
//		try {
//			Color maskingColor = Color.decode(maskingColorHex);
//			BufferedImage bufferedInputImage = GraphicsUtil.byteArrayToImage(inputBytes);
//
//			Image transparentImage = GraphicsUtil.makeColorTransparent(bufferedInputImage, maskingColor, maskingColorTolerance);
//
//			BufferedImage bufferedOutputImage = GraphicsUtil.imageToBufferedImage(transparentImage);
//
//			// TODO handle format (png) dynamically!?
//			ImageIO.write(bufferedOutputImage, "png", outputStream);
//			outputBytes = outputStream.toByteArray();
//
//			LOG.trace("Successfully intercepted/processed image response data (GetMap)");
//		} catch (Exception e) {
//			LOG.error("Could not intercept/process image response data (GetMap): " + e.getMessage());
//		}
//
//		if(outputBytes != null) {
//			response.setBody(outputBytes);
//		}

		return response;
	}

	@Override
	public Response interceptGetCapabilities(MutableHttpServletRequest mutableRequest, Response response) {
		// TODO Auto-generated method stub
		return response;
	}

	@Override
	public Response interceptGetFeatureInfo(MutableHttpServletRequest mutableRequest, Response response) {
		// TODO Implement logic to consider the masking layer by GFI response
		// (e.g. all objects outside of allowed bbox shouldn't be queryable)
		return response;
	}

	@Override
	public Response interceptDescribeLayer(MutableHttpServletRequest mutableRequest, Response response) {
		// TODO Auto-generated method stub
		return response;
	}

	@Override
	public Response interceptGetLegendGraphic(MutableHttpServletRequest mutableRequest, Response response) {
		LOG.debug("Intercepting MOMO WMS GetLegendGraphic response");
		String layersParam = mutableRequest.getParameter("LAYER");
		if (StringUtils.isEmpty(layersParam)) {
			layersParam = mutableRequest.getParameter("layer");
		}

		MomoLayer layer = null;
		layer = momoLayerDao.findByUrlAndLayerNames(geoserverInterceptorUrl, layersParam);

		if(layer == null) {
			LOG.warn("Layer from SHOGun2 database is null!?");
			return response;
		}

		if (!StringUtils.isEmpty(layer.getFixLegendUrl())) {
			String url = layer.getFixLegendUrl();
			Integer id = Integer.parseInt(url.split("id=")[1]);
			ImageFile image = imageFileDao.findById(id);
			if (image != null) {
				response.setBody(image.getFile());
				HttpStatus statusCode = HttpStatus.OK;
				response.setStatusCode(statusCode);
			}
			LOG.debug("Returning a fixed legend image with URI: " + url);
		}
		return response;
	}

	@Override
	public Response interceptGetStyles(MutableHttpServletRequest mutableRequest, Response response) {
		// TODO Auto-generated method stub
		return response;
	}

	/**
	 * @return the maskingColorHex
	 */
	public String getMaskingColorHex() {
		return maskingColorHex;
	}

	/**
	 * @param maskingColorHex the maskingColorHex to set
	 */
	public void setMaskingColorHex(String maskingColorHex) {
		this.maskingColorHex = maskingColorHex;
	}

	/**
	 * @return the maskingColorTolerance
	 */
	public int getMaskingColorTolerance() {
		return maskingColorTolerance;
	}

	/**
	 * @param maskingColorTolerance the maskingColorTolerance to set
	 */
	public void setMaskingColorTolerance(int maskingColorTolerance) {
		this.maskingColorTolerance = maskingColorTolerance;
	}

	/**
	 * @return the geoserverInterceptorUrl
	 */
	public String getGeoserverInterceptorUrl() {
		return geoserverInterceptorUrl;
	}

	/**
	 * @param geoserverInterceptorUrl the geoserverInterceptorUrl to set
	 */
	public void setGeoserverInterceptorUrl(String geoserverInterceptorUrl) {
		this.geoserverInterceptorUrl = geoserverInterceptorUrl;
	}

}
