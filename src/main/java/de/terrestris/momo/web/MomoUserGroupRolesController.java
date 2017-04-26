package de.terrestris.momo.web;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.service.UserGroupRoleService;
import de.terrestris.shogun2.util.data.ResultSet;
import de.terrestris.shogun2.web.AbstractWebController;

/**
 * @author Johannes Weskamm
 *
 */
@Controller
@RequestMapping("/momousergrouproles")
public class MomoUserGroupRolesController<E extends UserGroupRole, D extends UserGroupRoleDao<E>, S extends UserGroupRoleService<E, D>>
		extends AbstractWebController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoUserGroupRolesController() {
		this((Class<E>) UserGroupRole.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoUserGroupRolesController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * Updates a set of userGroupRoles
	 * @param token
	 * @return
	 * @return
	 * @return
	 * @throws URISyntaxException
	 */
	@RequestMapping(value = "/update.action", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> update(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody List<Map<String, String>> permissions) {
		try {
			service.updateUserGroupRole(permissions);
			LOG.info("UserGroupRoles updated successfully");
			return ResultSet.success("The UserGroupRoles have been updated");
		} catch (Exception e) {
			LOG.error("Could not update the UserGroupRoles: " + e.getMessage());
			return ResultSet.error("An error has occurred during your request.");
		}
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("userGroupRoleService")
	public void setService(S service) {
		this.service = service;
	}
}
