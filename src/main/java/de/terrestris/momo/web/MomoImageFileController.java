/**
 * Copyright 2016 - 2017 terrestris GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.terrestris.momo.web;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.terrestris.momo.service.MomoImageFileService;
import de.terrestris.shogun2.dao.ImageFileDao;
import de.terrestris.shogun2.model.ImageFile;
import de.terrestris.shogun2.util.data.ResultSet;
import de.terrestris.shogun2.web.ImageFileController;

/**
 * @author Johannes Weskamm
 *
 */
@Controller
@RequestMapping("/momoimage")
public class MomoImageFileController<E extends ImageFile, D extends ImageFileDao<E>, S extends MomoImageFileService<E, D>>
		extends ImageFileController<E, D, S> {

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoImageFileService")
	public void setService(S service) {
		this.service = service;
	}

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoImageFileController() {
		this((Class<E>) ImageFile.class);
	}

	/**
	 * Constructor that sets the concrete type for this controller.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoImageFileController(Class<E> type) {
		super(type);
	}

	/**
	 * Gets an image from the database by the given id.
	 * Is overridden because of different security policies
	 * between project <-> shogun2, directly calls the dao
	 * to be able to request images through geoserver -> sld
	 *
	 * @return
	 * @throws SQLException
	 */
	@Override
	@RequestMapping(value = "/getThumbnail.action", method=RequestMethod.GET)
	public ResponseEntity<?> getThumbnail(@RequestParam Integer id) {

		final HttpHeaders responseHeaders = new HttpHeaders();
		Map<String, Object> responseMap = new HashMap<>();

		try {
			// try to get the image
			ImageFile image = service.getDao().findById(id);
			if(image == null) {
				throw new Exception("Could not find the image with id " + id);
			}

			byte[] imageBytes = null;

			imageBytes = image.getThumbnail();

			responseHeaders.setContentType(
					MediaType.parseMediaType(image.getFileType()));

			LOG.info("Successfully got the image thumbnail " +
					image.getFileName());

			return new ResponseEntity<>(
					imageBytes, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			final String errorMessage = "Could not get the image thumbnail: "
					+ e.getMessage();

			LOG.error(errorMessage);
			responseMap = ResultSet.error(errorMessage);

			responseHeaders.setContentType(MediaType.APPLICATION_JSON);

			return new ResponseEntity<>(
					responseMap, responseHeaders, HttpStatus.OK);
		}
	}


	/**
	 * Gets a file from the database by the given id
	 *
	 * @return
	 * @throws SQLException
	 */
	@Override
	@RequestMapping(value = "/get.action", method=RequestMethod.GET)
	public ResponseEntity<?> getFile(@RequestParam Integer id) {

		final HttpHeaders responseHeaders = new HttpHeaders();
		Map<String, Object> responseMap = new HashMap<>();

		try {
			// try to get the image
			ImageFile image = service.getDao().findById(id);
			if(image == null) {
				throw new Exception("Could not find the image with id " + id);
			}

			byte[] imageBytes = null;

			imageBytes = image.getFile();

			responseHeaders.setContentType(
					MediaType.parseMediaType(image.getFileType()));

			LOG.info("Successfully got the image " +
					image.getFileName());

			return new ResponseEntity<>(
					imageBytes, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			final String errorMessage = "Could not get the image: "
					+ e.getMessage();

			LOG.error(errorMessage);
			responseMap = ResultSet.error(errorMessage);

			responseHeaders.setContentType(MediaType.APPLICATION_JSON);

			return new ResponseEntity<>(
					responseMap, responseHeaders, HttpStatus.OK);
		}
	}

}
