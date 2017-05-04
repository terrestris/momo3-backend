package de.terrestris.momo.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.dto.ApplicationData;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.service.MomoApplicationService;
import de.terrestris.shogun2.util.data.ResultSet;
import de.terrestris.shogun2.web.ApplicationController;

/**
 * @author Kai Volland
 * @author Nils Bühner
 *
 */
@Controller
@RequestMapping("/momoapps")
public class MomoApplicationController<E extends MomoApplication, D extends MomoApplicationDao<E>, S extends MomoApplicationService<E, D>>
		extends ApplicationController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoApplicationController() {
		this((Class<E>) MomoApplication.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoApplicationController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoApplicationService")
	public void setService(S service) {
		this.service = service;
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="create.action", method = RequestMethod.POST)
	public ResponseEntity<?> createMomoApplication(@RequestBody ApplicationData applicationData) {

		E app = null;
		try {
			app = (E) service.createMomoApplication(applicationData);
		} catch (Exception e) {
			final String msg = e.getMessage();
			LOG.error("Could not create MOMO application: " + msg);
			return new ResponseEntity<>(ResultSet.error(msg), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<E>(app, HttpStatus.CREATED);
	}

	/**
	 *
	 * @param appId
	 * @param appName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="copy.action", method = RequestMethod.POST)
	public ResponseEntity<?> copyMomoApplication(
            @RequestParam("appId") String appId, @RequestParam("appName") String appName) {
	        E app = null;
	        Integer appIdInt = Integer.valueOf(appId);
	        try {
	                app = (E) service.copyApp(appIdInt, appName);
	        } catch (Exception e) {
	                final String msg = e.getMessage();
	                LOG.error("Could not copy a layer: " + msg);
	                return new ResponseEntity<>(ResultSet.error(msg), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	        return new ResponseEntity<E>(app, HttpStatus.CREATED);
	}

	/**
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="update.action", method = RequestMethod.POST)
	public ResponseEntity<?> updateMomoApplication(@RequestBody ApplicationData applicationData) {

		E app = null;
		Integer appId = applicationData.getId();
		try {
			app = (E) service.updateMomoApplication(appId, applicationData);
		} catch (Exception e) {
			final String msg = e.getMessage();
			LOG.error("Could not update MOMO application: " + msg);
			return new ResponseEntity<>(ResultSet.error(msg), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<E>(app, HttpStatus.CREATED);
	}
}
