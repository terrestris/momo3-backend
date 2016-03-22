package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.ProjectApplicationDao;
import de.terrestris.momo.model.ProjectApplication;
import de.terrestris.momo.service.ProjectApplicationService;
import de.terrestris.shogun2.rest.ApplicationRestController;

/**
 * This is a demo controller that demonstrates how SHOGun2 REST controllers
 * can be extended.
 *
 * @author Nils Bühner
 *
 */
@RestController
@RequestMapping("/projectapplications")
public class ProjectApplicationRestController<E extends ProjectApplication, D extends ProjectApplicationDao<E>, S extends ProjectApplicationService<E, D>>
		extends ApplicationRestController<E, D, S> {

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("projectApplicationService")
	public void setService(S service) {
		this.service = service;
	}

}
