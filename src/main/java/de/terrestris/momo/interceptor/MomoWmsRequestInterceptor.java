package de.terrestris.momo.interceptor;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.service.MomoLayerService;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.interceptor.MutableHttpServletRequest;
import de.terrestris.shogun2.util.interceptor.WmsRequestInterceptorInterface;

/**
 *
 * @author terrestris GmbH & Co. KG
 *
 */
public class MomoWmsRequestInterceptor extends BaseOgcInterceptor implements WmsRequestInterceptorInterface {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(MomoWmsRequestInterceptor.class);

	/**
	 *
	 */
	@Value("${momo.maskingFeatureType}")
	private String maskingFeatureType;

	/**
	 *
	 */
	@Value("${momo.maskingPropertyName}")
	private String maskingPropertyName;

	/**
	 *
	 */
	@Value("${momo.maskingStyleName}")
	private String maskingStyleName;

	/**
	 *
	 */
	@Value("${role.superAdminRoleName}")
	private String adminRoleName;

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetMap(MutableHttpServletRequest request) {
		LOG.debug("Intercepting MOMO WMS GetMap request");

		String layersParam = request.getParameter("LAYERS");

		// 1. The corresponding SHOGun2 layer (based on URL and layerNames from request)
		MomoLayer layer = momoLayerService.findByUrlAndLayerNames(geoserverInterceptorUrl, layersParam);

		if(layer == null) {
			LOG.warn("Layer from SHOGun2 database is null!?");
			// TODO maybe returning null is not optimal here
			return null;
		}

		return request;
	}

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetCapabilities(
			MutableHttpServletRequest request) {
		return request;
	}

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetFeatureInfo(
			MutableHttpServletRequest request) {
		return request;
	}

	@Override
	public MutableHttpServletRequest interceptDescribeLayer(
			MutableHttpServletRequest request) {
		return request;
	}

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetLegendGraphic(
			MutableHttpServletRequest request) {
		return request;
	}

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetStyles(
			MutableHttpServletRequest request) {
		return request;
	}

	/**
	 * @return the userService
	 */
	public UserService<User, UserDao<User>> getUserService() {
		return userService;
	}

	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService<User, UserDao<User>> userService) {
		this.userService = userService;
	}

	/**
	 * @return the momoLayerService
	 */
	public MomoLayerService<MomoLayer, MomoLayerDao<MomoLayer>> getMomoLayerService() {
		return momoLayerService;
	}

	/**
	 * @param momoLayerService the momoLayerService to set
	 */
	public void setMomoLayerService(MomoLayerService<MomoLayer, MomoLayerDao<MomoLayer>> momoLayerService) {
		this.momoLayerService = momoLayerService;
	}

	/**
	 * @return the maskingFeatureType
	 */
	public String getMaskingFeatureType() {
		return maskingFeatureType;
	}

	/**
	 * @param maskingFeatureType the maskingFeatureType to set
	 */
	public void setMaskingFeatureType(String maskingFeatureType) {
		this.maskingFeatureType = maskingFeatureType;
	}

	/**
	 * @return the maskingPropertyName
	 */
	public String getMaskingPropertyName() {
		return maskingPropertyName;
	}

	/**
	 * @param maskingPropertyName the maskingPropertyName to set
	 */
	public void setMaskingPropertyName(String maskingPropertyName) {
		this.maskingPropertyName = maskingPropertyName;
	}

	/**
	 * @return the maskingStyleName
	 */
	public String getMaskingStyleName() {
		return maskingStyleName;
	}

	/**
	 * @param maskingStyleName the maskingStyleName to set
	 */
	public void setMaskingStyleName(String maskingStyleName) {
		this.maskingStyleName = maskingStyleName;
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

	/**
	 * @return the adminRoleName
	 */
	public String getAdminRoleName() {
		return adminRoleName;
	}

	/**
	 * @param adminRoleName the adminRoleName to set
	 */
	public void setAdminRoleName(String adminRoleName) {
		this.adminRoleName = adminRoleName;
	}

}
