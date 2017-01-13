package de.terrestris.momo.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.shogun2.converter.PersistentObjectIdResolver;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.service.UserService;

public class UserIdResolver<E extends User, D extends UserDao<E>, S extends UserService<E, D>>
		extends PersistentObjectIdResolver<E, D, S> {

	@Override
	@Autowired
	@Qualifier("userService")
	public void setService(S service) {
		this.service = service;
	}

}
