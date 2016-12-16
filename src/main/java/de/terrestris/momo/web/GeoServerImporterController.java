package de.terrestris.momo.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
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
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;

import de.terrestris.momo.service.GeoServerImporterService;
import de.terrestris.momo.util.importer.ImporterException;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.data.ResultSet;

/**
 *
 * terrestris GmbH & Co. KG
 * @author danielkoch
 * @author ahenn
 * @date 31.03.2016
 *
 */
@Controller
@RequestMapping("/import")
public class GeoServerImporterController {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(GeoServerImporterController.class);

	/**
	 * The autowired service class.
	 */
	private GeoServerImporterService service;

	@Autowired
	@Qualifier("userService")
	private UserService<User, UserDao<User>> userService;

	/**
	 *
	 * @param file
	 * @param fileProjection
	 * @param layerName
	 * @param layerType
	 * @param layerDescription
	 * @param layerOpacity
	 * @param layerHoverTemplate
	 * @return
	 */
	@RequestMapping(value = "/create-layer.action", method = {RequestMethod.POST})
	public ResponseEntity<?> createLayer(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "fileProjection", required = false) String fileProjection,
			@RequestParam("layerName") String layerName,
			@RequestParam("dataType") String dataType,
			@RequestParam("layerDescription") String layerDescription,
			@RequestParam("layerOpacity") Double layerOpacity,
			@RequestParam("layerHoverTemplate") String layerHoverTemplate) {

		LOG.debug("Requested to create a layer from geo-file(s).");

		Map<String, Object> responseMap = null;
		final HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus responseStatus = HttpStatus.OK;
		String responseMapAsString = null;
		ObjectMapper mapper = new ObjectMapper();

		// we have to return the response-Map as String to be browser conform.
		// as this controller is typically being called by a form.submit() the
		// browser expects a response with the Content-Type header set to
		// "text/html".
		responseHeaders.setContentType(MediaType.TEXT_HTML);

		try {
			if (file.isEmpty()) {
				LOG.error("Upload failed. File " + file + " is empty.");
				throw new ImporterException("File " +file.getOriginalFilename() + " is empty.");
			}

			responseMap = this.service.importGeodataAndCreateLayer(
					file,
					fileProjection,
					layerName,
					dataType,
					layerDescription,
					layerOpacity,
					layerHoverTemplate
			);

		} catch (ImporterException | URISyntaxException | HttpException | IOException | JSchException e) {
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseMap = ResultSet.error("Could not upload the file: " + e.getMessage());
		} finally {
			// rewrite the response-Map as String
			try {
				responseMapAsString = mapper.writeValueAsString(responseMap);
			} catch (JsonProcessingException e) {
				responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				LOG.error("Error while rewriting the response Map to a String" +
						e.getMessage());
				responseMap = ResultSet.error("Error while rewriting the " +
						"response map to a String" + e.getMessage());
			}
		}
		return new ResponseEntity<String>(responseMapAsString, responseHeaders, responseStatus);
	}

	/**
	 *
	 * @param layerName
	 * @param layerType
	 * @param layerDescription
	 * @param layerOpacity
	 * @param layerHoverTemplate
	 * @param importJobId
	 * @param taskId
	 * @param fileProjection
	 * @return
	 */
	@RequestMapping(value = "/update-crs-for-import.action", method = {RequestMethod.POST})
	public ResponseEntity<?> updateCrsForImport(
			@RequestParam("layerName") String layerName,
			@RequestParam("dataType") String dataType,
			@RequestParam("layerDescription") String layerDescription,
			@RequestParam("layerOpacity") Double layerOpacity,
			@RequestParam("layerHoverTemplate") String layerHoverTemplate,
			@RequestParam("importJobId") Integer importJobId,
			@RequestParam("taskId") Integer taskId,
			@RequestParam(value = "fileProjection") String fileProjection){

		Map<String, Object> responseMap = null;
		ResponseEntity<String> response;

		try {
			responseMap = this.service.updateCrsForImport(
					layerName,
					dataType,
					layerDescription,
					layerOpacity,
					layerHoverTemplate,
					importJobId, taskId, fileProjection);
		} catch (URISyntaxException | HttpException | IOException | ImporterException | JSchException e) {
			LOG.error("updateCrsForImport throwed an exception. Error was: " + e.getMessage());
			responseMap = ResultSet.error("Could not upload the file: " + e.getMessage());
		} finally {
			response = createResponseEntity(responseMap);
		}
		return response;
	}

	/**
	 *
	 * @param importJobId
	 * @return
	 */
	@RequestMapping(value = "/deleteImportJob.action", method = {RequestMethod.POST})
	public ResponseEntity<?> deleteImportJob(
			@RequestParam("importJobId") Integer importJobId){

			Map<String, Object> responseMap = null;
			ResponseEntity<String> response;
			try {
				responseMap = this.service.deleteImportJob(importJobId);
			} catch (URISyntaxException | HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				response = createResponseEntity(responseMap);
			}
			return response;

	}

	/**
	 * @param service the service to set
	 */
	@Autowired
	@Qualifier("geoServerImporterService")
	public void setService(GeoServerImporterService service) {
		this.service = service;
	}

	/**
	 *
	 * @param responseMap
	 * @return
	 */
	private ResponseEntity<String> createResponseEntity(Map<String, Object> responseMap){
		final HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus responseStatus = HttpStatus.OK;
		String responseMapAsString = null;
		ObjectMapper mapper = new ObjectMapper();

		// rewrite the response-Map as String
		try {
			responseMapAsString = mapper.writeValueAsString(responseMap);
		} catch (JsonProcessingException e) {
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			LOG.error("Error while rewriting the response Map to a String" +
					e.getMessage());
			responseMap = ResultSet.error("Error while rewriting the " +
					"response map to a String" + e.getMessage());
		}
		return new ResponseEntity<String>(responseMapAsString, responseHeaders, responseStatus);
	}

}
