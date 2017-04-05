package de.terrestris.momo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.MomoUserGroupDao;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.shogun2.service.UserGroupService;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("momoUserGroupService")
public class MomoUserGroupService<E extends MomoUserGroup, D extends MomoUserGroupDao<E>>
		extends UserGroupService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoUserGroupService() {
		this((Class<E>) MomoUserGroup.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoUserGroupService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoUserGroupDao")
	public void setDao(D dao) {
		this.dao = dao;
	}
}
