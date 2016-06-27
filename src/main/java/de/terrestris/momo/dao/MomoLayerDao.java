package de.terrestris.momo.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.dao.LayerDao;

/**
 *
 * @author Nils Bühner
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("momoLayerDao")
public class MomoLayerDao<E extends MomoLayer> extends LayerDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public MomoLayerDao() {
		super((Class<E>) MomoLayer.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected MomoLayerDao(Class<E> clazz) {
		super(clazz);
	}


	/**
	 * Finds and returns a momo layer by the url and the layernames of its
	 * source.
	 *
	 * @param url
	 * @param layerNames
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public E findByUrlAndLayerNames(String url, String layerNames) {
		if (url == null || layerNames == null) {
			return null;
		}
		Criteria criteria = createDistinctRootEntityCriteria();
		criteria.createAlias("source", "s");
		criteria.add(Restrictions.eq("s.url", url));
		criteria.add(Restrictions.eq("s.layerNames", layerNames));
		return (E) criteria.uniqueResult();
	}

}
