package de.terrestris.momo.dao;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.state.ApplicationState;
import de.terrestris.shogun2.dao.GenericHibernateDao;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("applicationStateDao")
public class ApplicationStateDao<E extends ApplicationState> extends GenericHibernateDao<E, Integer> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public ApplicationStateDao() {
		super((Class<E>) ApplicationState.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected ApplicationStateDao(Class<E> clazz) {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	public Set<E>findByWebMapIdAndUserId(Integer webMapId, Integer userId) {

		if (webMapId == null || userId == null) {
			return null;
		}
		Criteria criteria = createDistinctRootEntityCriteria();
		criteria.add(Restrictions.eq("application.id", webMapId));
		criteria.add(Restrictions.eq("owner.id", userId));
		return new HashSet<E>(criteria.list());
	}

}
