package de.terrestris.momo.interceptor;

import de.terrestris.shogun2.util.interceptor.MutableHttpServletRequest;
import de.terrestris.shogun2.util.interceptor.WfsRequestInterceptorInterface;

/**
 * This class demonstrates how to implement the WfsRequestInterceptorInterface.
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
public class MomoWfsRequestInterceptor extends BaseOgcInterceptor implements WfsRequestInterceptorInterface {

	@Override
	public MutableHttpServletRequest interceptGetCapabilities(
			MutableHttpServletRequest request) {
		// always forbid
		return forbidRequest(request);
	}

	@Override
	public MutableHttpServletRequest interceptDescribeFeatureType(
			MutableHttpServletRequest request) {
		// depend on read rights to layer
		return canGetLayerOrForbidRequest(request);
	}

	@Override
	public MutableHttpServletRequest interceptGetFeature(
			MutableHttpServletRequest request) {
		// depend on read rights to layer
		return canGetLayerOrForbidRequest(request);
	}

	@Override
	public MutableHttpServletRequest interceptLockFeature(
			MutableHttpServletRequest request) {
		// depend on write rights to layer TODO
		return canGetLayerOrForbidRequest(request);
	}

	@Override
	public MutableHttpServletRequest interceptTransaction(
			MutableHttpServletRequest request) {
		// depend on write rights to layer TODO
		return canGetLayerOrForbidRequest(request);
	}

}
