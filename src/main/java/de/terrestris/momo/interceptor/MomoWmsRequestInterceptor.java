package de.terrestris.momo.interceptor;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.service.MomoLayerService;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.interceptor.MutableHttpServletRequest;
import de.terrestris.shogun2.util.interceptor.WmsRequestInterceptorInterface;

/**
 *
 * @author terrestris GmbH & Co. KG
 *
 */
public class MomoWmsRequestInterceptor implements WmsRequestInterceptorInterface {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(MomoWmsRequestInterceptor.class);

	/**
	 *
	 */
	@Autowired
	@Qualifier("userService")
	private UserService<User, UserDao<User>> userService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoLayerService")
	private MomoLayerService<MomoLayer, MomoLayerDao<MomoLayer>> momoLayerService;

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
	@Value("${momo.publicInterceptGeoServerAction}")
	private String geoserverInterceptorUrl;

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

		if(!layer.getSpatiallyRestricted()) {
			// if the layer is not restricted -> allow
			return request;
		}

		// 2. The logged in user
		User currentUser = userService.getUserBySession();

		if(currentUser == null) {
			LOG.warn("Logged in user is null!?");
			// TODO maybe returning null is not optimal here
			return null;
		}

		// 2.1 Check if logged in user is ADMIN. If yes -> allow everything
		for (Role role : currentUser.getRoles()) {
			if(role.getName().equals(adminRoleName)) {
				LOG.debug("User is admin -> Not intercepting the WMS request anymore!");
				return request;
			}
		}

		// 3. Adapt following request parameters:
		//		- LAYERS
		//		- STYLES
		//		- CQL_FILTER
		Set<UserGroup> userGroups = currentUser.getUserGroups();

		Set<Integer> maskingValues = getMaskingPropertyValues(userGroups);
		String commaSeparatedMaskingValues = StringUtils.join(maskingValues, ",");

		String stylesParam = request.getParameter("STYLES");

		String[] layersParamsArray = new String[]{layersParam, maskingFeatureType};
		String[] stylesParamsArray = new String[]{stylesParam, maskingStyleName};

		if(commaSeparatedMaskingValues.isEmpty()) {
			// set an "invalid" id to avoid that the NOT IN expression can still be parsed but the
			// WMS will not return anything, which should be the case if the layer is restricted,
			// but the current user has not masking values assigned
			commaSeparatedMaskingValues = "-1";
		}

		// We assume that there is no existing CQL_FILTER
		// TODO: Assure that there will really never be a CQL_FILTER in a request
		String cqlFilterParam = "1=1;" + maskingPropertyName + " NOT IN (" + commaSeparatedMaskingValues + ")";

		request.setParameter("LAYERS", layersParamsArray);
		request.setParameter("STYLES", stylesParamsArray);
		request.setParameter("CQL_FILTER", cqlFilterParam);

		return request;
	}

	/**
	 * Extracts the values for the masking from all groups.
	 *
	 * @param userGroups
	 * @return
	 */
	private Set<Integer> getMaskingPropertyValues(Set<UserGroup> userGroups) {
		Set<Integer> allMaskingValues = new HashSet<Integer>();

		for (UserGroup userGroup : userGroups) {
			if(userGroup instanceof MomoUserGroup) {
				final Set<Integer> groupMaskingValues = ((MomoUserGroup) userGroup).getMaskingValues();
				allMaskingValues.addAll(groupMaskingValues);
			}
		}

		return allMaskingValues;
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
