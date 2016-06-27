package de.terrestris.momo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.service.LayerService;

/**
 *
 * @author Nils Bühner
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("momoLayerService")
public class MomoLayerService<E extends MomoLayer, D extends MomoLayerDao<E>>
		extends LayerService<E, D> {


	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoLayerDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	/**
	 * Finds and returns a layer by it's URL and the layerNames parameter.
	 *
	 * @param url
	 * @param layerNames
	 * @return
	 */
	@PostAuthorize("hasRole(@configHolder.getSuperAdminRoleName()) or hasPermission(returnObject, 'READ')")
	public E findByUrlAndLayerNames(String url, String layerNames) {
		if (url == null || layerNames == null) {
			return null;
		}
		return dao.findByUrlAndLayerNames(url, layerNames);
	}

}
