package de.terrestris.momo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.dao.MomoUserDao;
import de.terrestris.momo.dao.MomoUserGroupDao;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.MomoUserGroup;
import de.terrestris.momo.model.security.EntityPermissionEnvelope;
import de.terrestris.momo.model.security.EntityPermissionTypeEnvelope;
import de.terrestris.shogun2.dao.GenericHibernateDao;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.security.PermissionCollection;
import de.terrestris.shogun2.service.PermissionAwareCrudService;
import javassist.NotFoundException;

@Service
@Qualifier("entityPermissionService")
@Transactional(value = "transactionManager")
public class EntityPermissionService<E extends PersistentObject> {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(EntityPermissionService.class);

	@Autowired
	@Qualifier("momoLayerDao")
	private MomoLayerDao<? extends MomoLayer> momoLayerDao;

	@Autowired
	@Qualifier("momoApplicationDao")
	private MomoApplicationDao<? extends MomoApplication> momoApplicationDao;

	@Autowired
	@Qualifier("momoUserGroupDao")
	private MomoUserGroupDao<? extends MomoUserGroup> momoUserGroupDao;

	@Autowired
	@Qualifier("momoUserDao")
	private MomoUserDao<? extends MomoUser> momoUserDao;

	@Autowired
	@Qualifier("permissionAwareCrudService")
	private PermissionAwareCrudService<E, GenericHibernateDao<E,Integer>> permissionAwareCrudService;

	/**
	 *
	 * @param entityId
	 * @param entityClass
	 * @param targetEntity
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 */
	@PreAuthorize("hasRole(@momoConfigHolder.getEditorRoleName())")
	public EntityPermissionTypeEnvelope getEntityPermission(Integer entityId,
			String targetEntity, Class<?> entityClass) throws ClassNotFoundException, NotFoundException {

		EntityPermissionTypeEnvelope entityPermissionTypeEnvelope = new EntityPermissionTypeEnvelope();

		if (entityClass.isAssignableFrom(MomoLayer.class)) {
			// Get the layer entity by the passed ID.
			MomoLayer layer = momoLayerDao.findById(entityId);

			if (layer == null) {
				throw new NotFoundException("Could not find MomoLayer with ID " + entityId);
			}

			entityPermissionTypeEnvelope.setTargetEntity(layer);
			entityPermissionTypeEnvelope.setType(entityClass.getSimpleName());

			// Get the permissions, either for group or user.
			if (targetEntity.equalsIgnoreCase("MomoGroup")) {
				Set<EntityPermissionEnvelope> permissions = getUserGroupPermissions(layer);
				entityPermissionTypeEnvelope.setPermissions(permissions);
			} else if (targetEntity.equalsIgnoreCase("MomoUser")) {
				Set<EntityPermissionEnvelope> permissions = getUserPermissions(layer);
				entityPermissionTypeEnvelope.setPermissions(permissions);
			} else {
				throw new NotFoundException(targetEntity + " is not a valid targetEntityClass");
			}
		} else if (entityClass.isAssignableFrom(MomoApplication.class)) {
			MomoApplication app = momoApplicationDao.findById(entityId);

			if (app == null) {
				throw new NotFoundException("Could not find MomoApplication with ID " + entityId);
			}

			entityPermissionTypeEnvelope.setTargetEntity(app);
			entityPermissionTypeEnvelope.setType(entityClass.getSimpleName());

			// Get the permissions, either for group or user.
			if (targetEntity.equalsIgnoreCase("MomoGroup")) {
				Set<EntityPermissionEnvelope> permissions = getUserGroupPermissions(app);
				entityPermissionTypeEnvelope.setPermissions(permissions);
			} else if (targetEntity.equalsIgnoreCase("MomoUser")) {
				Set<EntityPermissionEnvelope> permissions = getUserPermissions(app);
				entityPermissionTypeEnvelope.setPermissions(permissions);
			} else {
				throw new NotFoundException(targetEntity + " is not a valid targetEntityClass");
			}
		} else {
			throw new NotFoundException(entityClass + " is not a valid entityClass");
		}

		return entityPermissionTypeEnvelope;
	}

	/**
	 *
	 * @param envelope
	 * @param classNameOfTarget
	 * @param entityIdOfTarget
	 * @param entityNameOfPermissionHolder
	 * @return
	 * @throws Exception
	 */
	public EntityPermissionTypeEnvelope createOrUpdateEntityPermission(EntityPermissionTypeEnvelope envelope,
				String classNameOfTarget, Integer entityIdOfTarget, String entityNameOfPermissionHolder)
						throws Exception {

		PersistentObject targetEntity = envelope.getTargetEntity();
		Set<EntityPermissionEnvelope> permissionEnvelopes = envelope.getPermissions();

		// check if path parameters are in sync with object in provided JSON
		checkPathParametersWithEnvelopeObject(envelope, classNameOfTarget, entityIdOfTarget, entityNameOfPermissionHolder);

		if (targetEntity instanceof MomoLayer) {
			// Get the layer entity by the passed ID.
			MomoLayer layer = momoLayerDao.findById(targetEntity.getId());
			if (layer == null) {
				throw new NotFoundException("Could not find MomoLayer with ID " + targetEntity.getId());
			}

			setPermissionsForEntity(layer, permissionEnvelopes);
		} else if (targetEntity instanceof MomoApplication) {
			MomoApplication app = momoApplicationDao.findById(targetEntity.getId());
			if (app == null) {
				throw new NotFoundException("Could not find MomoApplication with ID " + targetEntity.getId());
			}

			setPermissionsForEntity(app, permissionEnvelopes);
		} else {
			throw new NotFoundException(targetEntity.getClass().getSimpleName() + " is not a valid entityClass");
		}

		return getEntityPermission(targetEntity.getId(), entityNameOfPermissionHolder , targetEntity.getClass());
	}

	/**
	 *
	 * @param envelope
	 * @param classNameOfTarget
	 * @param entityIdOfTarget
	 * @param entityNameOfPermissionHolder
	 * @throws Exception
	 */
	private void checkPathParametersWithEnvelopeObject(EntityPermissionTypeEnvelope envelope, String classNameOfTarget,
			Integer entityIdOfTarget, String entityNameOfPermissionHolder) throws Exception {

		if (entityIdOfTarget.compareTo(envelope.getTargetEntity().getId()) != 0) {
			String msg = "ID of entity " + envelope.getTargetEntity() + " does not match ID "+ entityIdOfTarget + " provided in path.";
			LOG.error(msg);
			throw new Exception(msg);
		}

		final String fullQualifiedClassName = "de.terrestris.momo.model." + classNameOfTarget;
		String providedClassNameInJson = envelope.getTargetEntity().getClass().getName();
		if (!StringUtils.equals(providedClassNameInJson, fullQualifiedClassName)) {
			String msg = "Class name in path " + classNameOfTarget + " does not match with class name  "+ classNameOfTarget + " provided in path.";
			LOG.error(msg);
			throw new Exception(msg);
		}

		final String fullQualifiedClassForEntityPermissionHolder = "de.terrestris.momo.model." + entityNameOfPermissionHolder;
		Set<EntityPermissionEnvelope> permissions = envelope.getPermissions();
		for (EntityPermissionEnvelope permission : permissions) {
			String permissionTargetClassName = permission.getTargetEntity().getClass().getName();
			if (permission.getTargetEntity() != null && !StringUtils.equals(permissionTargetClassName, fullQualifiedClassForEntityPermissionHolder)) {
				String msg = "Class name in path " + entityNameOfPermissionHolder + " does not match with class name  "+ fullQualifiedClassForEntityPermissionHolder + " provided in path.";
				LOG.error(msg);
				throw new Exception(msg);
			}
		}
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	private Set<EntityPermissionEnvelope> getUserGroupPermissions(PersistentObject entity) {

		Map<UserGroup, PermissionCollection> groupPermissions = entity.getGroupPermissions();
		List<? extends MomoUserGroup> momoUserGroups = momoUserGroupDao.findAll();
		Set<EntityPermissionEnvelope> permissionEnvelopes = new HashSet<EntityPermissionEnvelope>();

		for (MomoUserGroup momoUserGroup : momoUserGroups) {
			EntityPermissionEnvelope permissionEnvelope = new EntityPermissionEnvelope();
			permissionEnvelope.setTargetEntity(momoUserGroup);
			permissionEnvelope.setType(momoUserGroup.getClass().getSimpleName());
			permissionEnvelope.setDisplayTitle(momoUserGroup.getName());
			permissionEnvelope.setPermissions(groupPermissions.get(momoUserGroup));
			permissionEnvelopes.add(permissionEnvelope);
		}

		return permissionEnvelopes;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	private Set<EntityPermissionEnvelope> getUserPermissions(PersistentObject entity) {

		Map<User, PermissionCollection> userPermissions = entity.getUserPermissions();
		List<? extends MomoUser> momoUsers = momoUserDao.findAll();
		Set<EntityPermissionEnvelope> permissionEnvelopes = new HashSet<EntityPermissionEnvelope>();
		for (MomoUser momoUser : momoUsers) {
			EntityPermissionEnvelope permissionEnvelope = new EntityPermissionEnvelope();
			permissionEnvelope.setTargetEntity(momoUser);
			permissionEnvelope.setType(momoUser.getClass().getSimpleName());
			permissionEnvelope.setDisplayTitle(momoUser.getFirstName() + " " + momoUser.getLastName());
			permissionEnvelope.setPermissions(userPermissions.get(momoUser));
			permissionEnvelopes.add(permissionEnvelope);
		}

		return permissionEnvelopes;
	}

	/**
	 *
	 * @param entity
	 * @param permissionEnvelopes
	 * @throws NotFoundException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void setPermissionsForEntity(PersistentObject entity, Set<EntityPermissionEnvelope> permissionEnvelopes) throws NotFoundException {

		for (EntityPermissionEnvelope permission : permissionEnvelopes) {
			// Get the permissions, either for group or user.
			PersistentObject targetEntityGroupOrUser = permission.getTargetEntity();
			PermissionCollection permissionsToSet = permission.getPermissions();
			Set<Permission> permissionCollectionSet = null;

			if (permissionsToSet == null) {
				permissionCollectionSet = new HashSet<Permission>();
			} else {
				permissionCollectionSet = permissionsToSet.getPermissions();
			}

			Set<Permission> permissionCollectionSetToRemove = getPermissionsToRemove(permissionCollectionSet);

			Permission[] permissionsArrayToSet = permissionCollectionSet.toArray(new Permission[permissionCollectionSet.size()]);
			Permission[] permissionsArrayToRemove = permissionCollectionSetToRemove.toArray(new Permission[permissionCollectionSetToRemove.size()]);

			if (targetEntityGroupOrUser.getClass().isAssignableFrom(MomoUserGroup.class)) {
				MomoUserGroup momoUserGroup = momoUserGroupDao.findById(targetEntityGroupOrUser.getId());
				if (permissionsArrayToSet.length > 0) {
					permissionAwareCrudService.addAndSaveGroupPermissions((E) entity, momoUserGroup, permissionsArrayToSet);
				}
				if (permissionsArrayToRemove.length > 0) {
					permissionAwareCrudService.removeAndSaveGroupPermissions((E) entity, momoUserGroup, permissionsArrayToRemove);
				}
			} else if (targetEntityGroupOrUser.getClass().isAssignableFrom(MomoUser.class)) {
				MomoUser momoUser = momoUserDao.findById(targetEntityGroupOrUser.getId());
				if (permissionsArrayToSet.length > 0) {
					permissionAwareCrudService.addAndSaveUserPermissions((E) entity, momoUser, permissionsArrayToSet);
				}
				if (permissionsArrayToRemove.length > 0) {
					permissionAwareCrudService.removeAndSaveUserPermissions((E) entity, momoUser, permissionsArrayToRemove);
				}
			} else {
				throw new NotFoundException("Class of targetEntity currently not mapped (Only MomoUser and MomoApplication avaliable).");
			}
		}
	}

	/**
	 *
	 * @param permissionCollectionSet
	 * @return
	 */
	private static Set<Permission> getPermissionsToRemove(Set<Permission> permissionCollectionSet) {

		if (permissionCollectionSet.contains(Permission.ADMIN)) {
			LOG.debug("Requested to set ADMIN permissions.");
			return new HashSet<Permission>();
		}

		Permission[] allPermissions = Permission.values();

		Set<Permission> allPermissionsSet = new HashSet<Permission>(allPermissions.length);

		CollectionUtils.addAll(allPermissionsSet, allPermissions);

		allPermissionsSet.removeAll(permissionCollectionSet);

		return allPermissionsSet;
	}

}
