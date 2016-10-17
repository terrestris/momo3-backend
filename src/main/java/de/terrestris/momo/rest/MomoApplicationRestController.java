package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.service.MomoApplicationService;
import de.terrestris.shogun2.rest.ApplicationRestController;

/**
 *
 * @author Nils Bühner
 *
 */
@RestController
@RequestMapping("/momoapps")
public class MomoApplicationRestController<E extends MomoApplication, D extends MomoApplicationDao<E>, S extends MomoApplicationService<E, D>>
		extends ApplicationRestController<E, D, S> {

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoApplicationService")
	public void setService(S service) {
		this.service = service;
	}

}
