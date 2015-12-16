package de.terrestris.momo.service;

import org.springframework.stereotype.Service;
import de.terrestris.shogun2.service.AbstractExtDirectCrudService;

import de.terrestris.momo.model.ProjectApplication;

@Service("projectApplicationService")
public class ProjectApplicationService extends
	AbstractExtDirectCrudService<ProjectApplication> {
}
