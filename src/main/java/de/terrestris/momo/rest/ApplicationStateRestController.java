package de.terrestris.momo.rest;

import java.io.Reader;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.terrestris.momo.dao.ApplicationStateDao;
import de.terrestris.momo.model.state.ApplicationState;
import de.terrestris.momo.service.ApplicationStateService;
import de.terrestris.shogun2.web.AbstractWebController;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 * @param <S>
 */
@RestController
@RequestMapping(value = "/rest/applicationstate")
public class ApplicationStateRestController<E extends ApplicationState, D extends ApplicationStateDao<E>,
		S extends ApplicationStateService<E, D>> extends AbstractWebController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ApplicationStateRestController() {
		this((Class<E>) ApplicationState.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected ApplicationStateRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("applicationStateService")
	public void setService(S service) {
		this.service = service;
	}

	/**
	 * Get the applicationState by its token.
	 *
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.GET)
	public ResponseEntity<E> get(@PathVariable String token) {

		try {
			MultiValueMap<String, String> requestedFilter = new LinkedMultiValueMap<String, String>();
			requestedFilter.add("token", token);
			List<E> entities = this.service.findBySimpleFilter(requestedFilter);

			if (!entities.isEmpty()) {
				E entity = entities.get(0);
				LOG.trace("Found " + entity.getClass().getSimpleName()
						+ " with token " + entity.getToken());
				return new ResponseEntity<E>(entity, HttpStatus.OK);
			} else {
				LOG.trace("No entity for token " + token + " found");
				return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error("Error finding entity with token " + token + ": "
					+ e.getMessage());
			return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Create/save an entity.
	 *
	 * @param entity
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<E> create(@RequestBody E entity) {

		final String simpleClassName = entity.getClass().getSimpleName();
		final String errorMessagePrefix = "Error when saving entity of type "
				+ simpleClassName + ": ";

		// ID value MUST be null to assure that
		// saveOrUpdate will save and not update
		final Integer id = entity.getId();
		if (id != null) {
			LOG.error(errorMessagePrefix + "ID value is set to " + id
					+ ", but MUST be null");
			return new ResponseEntity<E>(HttpStatus.BAD_REQUEST);
		}

		try {
			this.service.saveOrUpdate(entity);
			LOG.trace("Created " + simpleClassName + " with ID " + entity.getId());
			return new ResponseEntity<E>(entity, HttpStatus.CREATED);
		} catch (Exception e) {
			LOG.error(errorMessagePrefix + e.getMessage());
			return new ResponseEntity<E>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Deletes an entity by its token.
	 *
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.DELETE)
	public ResponseEntity<E> delete(@PathVariable String token) {

		try {
			MultiValueMap<String, String> requestedFilter = new LinkedMultiValueMap<String, String>();
			requestedFilter.add("token", token);
			List<E> entities = this.service.findBySimpleFilter(requestedFilter);

			if (!entities.isEmpty()) {
				E entityToDelete = entities.get(0);

				this.service.delete(entityToDelete);

				// extract the original classname from the name of the proxy, which
				// also contains _$$_ and some kind of hash after the original
				// classname
				final String proxyClassName = entityToDelete.getClass().getSimpleName();
				final String simpleClassName = StringUtils.substringBefore(proxyClassName, "_$$_");

				LOG.trace("Deleted " + simpleClassName + " with token " + entityToDelete.getToken());
				return new ResponseEntity<E>(HttpStatus.NO_CONTENT);
			} else {
				LOG.trace("No entity for token " + token + " found");
				return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error("Error deleting entity with token " + token + ": "
					+ e.getMessage());
			return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Updates an entity by its token.
	 *
	 * @param token
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.PUT)
	public ResponseEntity<E> update(@PathVariable String token, HttpServletRequest request) {

		String errorPrefix = "Error updating "
				+ getEntityClass().getSimpleName() + " with token " + token + ": ";

		ObjectMapper objectMapper = new ObjectMapper();
		Reader reader = null;

		try {
			// read and parse the json request body
			reader = request.getReader();
			JsonNode jsonObject = objectMapper.readTree(reader);

			// validate json object
			if (jsonObject == null || !jsonObject.has("token")) {
				LOG.error(errorPrefix
						+ "The JSON body is empty or has no 'token' property.");
				return new ResponseEntity<E>(HttpStatus.BAD_REQUEST);
			}

			// assure that the path variable token equals the payload token
			final String payloadToken = jsonObject.get("token").asText();
			if (!payloadToken.equals(token)) {
				LOG.error(errorPrefix + "Requested to update entity with token "
						+ token + ", but payload token is " + payloadToken);
				return new ResponseEntity<E>(HttpStatus.BAD_REQUEST);
			}

			// get the persisted entity
			MultiValueMap<String, String> requestedFilter = new LinkedMultiValueMap<String, String>();
			requestedFilter.add("token", token);
			List<E> entities = this.service.findBySimpleFilter(requestedFilter);

			if (!entities.isEmpty()) {
				E entity = entities.get(0);

				// we call this transactional method (instead of save or update)
				// to make sure that the possibly modified entity does not
				// get persisted / synced unexpectedly by hibernate
				// (due to FlushMode.AUTO) when another database-related
				// interaction is triggered in the meantime (which could happen
				// for example in a permission evaluation).
				// In other words: Do not get an entity, modify it and save it
				// in a non-transactional way (e.g. controller method), as
				// a possible permission evaluation could trigger an unwanted
				// persist action before the permission was evaluated.
				entity = service.updatePartialWithJsonNode(entity, jsonObject, objectMapper);
				return new ResponseEntity<E>(entity, HttpStatus.OK);
			} else {
				LOG.trace("No entity for token " + token + " found");
				return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(errorPrefix + e.getMessage());
			return new ResponseEntity<E>(HttpStatus.NOT_FOUND);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	/**
	 * Get an entity by id.
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/webmap/{webMapId}", method = RequestMethod.GET)
	public ResponseEntity<Set<E>> getByWebMapIdForCurrentUser(@PathVariable Integer webMapId) {

		try {
			Set<E> entities = this.service.findByWebMapIdForCurrentUser(webMapId);
			return new ResponseEntity<Set<E>>(entities, HttpStatus.OK);
		} catch (Exception e) {
			LOG.error("Error finding application states for webmap with id " + webMapId + ": "
					+ e.getMessage());
			return new ResponseEntity<Set<E>>(HttpStatus.BAD_REQUEST);
		}
	}

}
