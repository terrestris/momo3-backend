package de.terrestris.momo.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.service.MomoApplicationService;
import de.terrestris.shogun2.converter.PersistentObjectIdResolver;

public class MomoApplicationIdResolver<E extends MomoApplication, D extends MomoApplicationDao<E>, S extends MomoApplicationService<E, D>>
		extends PersistentObjectIdResolver<E, D, S> {

	@Override
	@Autowired
	@Qualifier("momoApplicationService")
	public void setService(S service) {
		this.service = service;
	}

}
