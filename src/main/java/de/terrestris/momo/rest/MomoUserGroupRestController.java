package de.terrestris.momo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.momo.dao.MomoUserGroupDao;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.service.MomoUserGroupService;
import de.terrestris.shogun2.rest.UserGroupRestController;

/**
 * 
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 * @param <S>
 */
@RestController
@RequestMapping("/momousergroups")
public class MomoUserGroupRestController<E extends MomoUserGroup, D extends MomoUserGroupDao<E>, S extends MomoUserGroupService<E, D>>
		extends UserGroupRestController<E, D, S> {

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoUserGroupService")
	public void setService(S service) {
		this.service = service;
	}

}
