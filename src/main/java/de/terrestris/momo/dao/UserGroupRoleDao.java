package de.terrestris.momo.dao;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.shogun2.dao.GenericHibernateDao;
import de.terrestris.shogun2.model.User;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("userGroupRoleDao")
public class UserGroupRoleDao<E extends UserGroupRole> extends GenericHibernateDao<E, Integer> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public UserGroupRoleDao() {
		super((Class<E>) UserGroupRole.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected UserGroupRoleDao(Class<E> clazz) {
		super(clazz);
	}

	/**
	 *
	 * @param user
	 * @return
	 */
	public List<E> findUserRoles(User user) {

		if (user == null) {
			return null;
		}

		Criterion criterion = Restrictions.eq("user", user);
		return (List<E>) this.findByCriteria(criterion);
	}

}
