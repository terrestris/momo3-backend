package de.terrestris.momo.web;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.terrestris.momo.model.security.EntityPermissionTypeEnvelope;
import de.terrestris.momo.service.EntityPermissionService;
import de.terrestris.shogun2.util.data.ResultSet;

@Controller
//@RequestMapping("/rest/entitypermission")
@RequestMapping("/entitypermission")
public class EntityPermissionController {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(EntityPermissionController.class);

	/**
	 * The autowired service class.
	 */
	@Autowired
	@Qualifier("entityPermissionService")
	private EntityPermissionService service;

	/**
	 *
	 * http://localhost:8080/momo/entitypermission/getEntityPermissions.action?entityId=1&entityClass=MomoApplication&targetEntity=2
	 *
	 * @param entityId
	 * @param targetEntity
	 * @return
	 */
	@RequestMapping(value = "/{entityType}/{entityId}/{targetEntity}", method = RequestMethod.GET)
	private @ResponseBody Map<String, Object> getApplicationEntityPermissions(
			@PathVariable("entityType") String entityType,
			@PathVariable("entityId") Integer entityId,
			@PathVariable("targetEntity") String targetEntity) {

		try {

			Class<?> clazz = Class.forName("de.terrestris.momo.model." + entityType);

			EntityPermissionTypeEnvelope entityPermission = service.getEntityPermission(
					entityId, targetEntity, clazz);
			return ResultSet.success(entityPermission);
		} catch (ClassNotFoundException cnfe) {
			String responseMsg = "Could not find class: " + cnfe.getMessage();
			LOG.error(responseMsg);
			return ResultSet.error(responseMsg);
		} catch (Exception e) {
			String responseMsg = "Error while requesting the entity permission: " + e.getMessage();
			LOG.error(responseMsg);
			return ResultSet.error(responseMsg);
		}
	}

	/**
	 * 
	 * @param envelope
	 * @return
	 */
	@RequestMapping(value = "/{entityType}/{entityId}/{targetEntity}", method = { RequestMethod.PUT, RequestMethod.POST })
	private @ResponseBody Map<String, Object> createOrUpdateEntityPermissions(
			@PathVariable("entityType") String entityType,
			@PathVariable("entityId") Integer entityId,
			@PathVariable("targetEntity") String targetEntity,
			@RequestBody EntityPermissionTypeEnvelope envelope) {
		try {
			EntityPermissionTypeEnvelope entityPermission = service.createOrUpdateEntityPermission(
					envelope, targetEntity);
			return ResultSet.success(entityPermission);
		} catch (Exception e) {
			String responseMsg = "Error while updating the entity permission: " + e.getMessage();
			LOG.error(responseMsg);
			return ResultSet.error(responseMsg);
		}
	}

	/**
	 * @return the service
	 */
	public EntityPermissionService getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(EntityPermissionService service) {
		this.service = service;
	}

}
