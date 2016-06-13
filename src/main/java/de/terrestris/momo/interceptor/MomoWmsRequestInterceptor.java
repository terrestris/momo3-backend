package de.terrestris.momo.interceptor;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.service.GeoServerInterceptorService;
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
	private static final Logger LOG = Logger.getLogger(GeoServerInterceptorService.class);

	/**
	 *
	 */
	@Autowired
	@Qualifier("userService")
	private UserService<User, UserDao<User>> userService;

	/**
	 *
	 */
	@Override
	public MutableHttpServletRequest interceptGetMap(MutableHttpServletRequest request) {
		LOG.debug("Intercepting MOMO WMS GetMap request");

		// we need the following entities:
		// 1. The corresponding SHOGun2 layer (based on URL and layerNames from request)
		// 2. The logged in user
		// 3. The "merged" geometry (territories from all groups of the user)
		// 4. The merged permissions

		// here a rough sketch of the workflow

		// 1. if layer.isSpatiallyRestricted == false -> allow/forward
		// 2. else: "getMergedGeometryOfUsersGroupsTerritories".
		// 3. append CQL filter that restricts to this geometry...

		User currentUser = userService.getUserBySession();
		
		System.out.println("Logged in user: " + currentUser);

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

}
