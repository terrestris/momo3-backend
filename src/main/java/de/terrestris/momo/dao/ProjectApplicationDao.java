package de.terrestris.momo.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.momo.model.ProjectApplication;
import de.terrestris.shogun2.dao.GenericHibernateDao;

@Repository
public class ProjectApplicationDao extends
		GenericHibernateDao<ProjectApplication, Integer> {

	protected ProjectApplicationDao() {
		super(ProjectApplication.class);
	}

}
