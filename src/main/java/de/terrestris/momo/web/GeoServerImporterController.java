package de.terrestris.momo.web;

import java.net.URISyntaxException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.service.GeoServerImporterService;
import de.terrestris.shogun2.importer.GeoServerRESTImporterException;
import de.terrestris.shogun2.util.data.ResultSet;

/**
 *
 * terrestris GmbH & Co. KG
 * @author danielkoch
 * @author ahenn
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


	/**
	 *
	 * @param file
	 * @param fileProjection
	 * @param dataType
	 * @return
	 */
	@RequestMapping(value = "/create-layer.action", method = {RequestMethod.POST})
	public ResponseEntity<Map<String, Object>> createLayer(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "fileProjection", required = false) String fileProjection,
			@RequestParam("dataType") String dataType,
			HttpServletRequest request) {

		LOG.debug("Requested to create a layer from geo-file(s).");

		Map<String, Object> responseMap;
		final HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus responseStatus = HttpStatus.OK;
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);

		try {
			if (file.isEmpty()) {
				LOG.error("Upload failed. File " + file + " is empty.");
				throw new GeoServerRESTImporterException("File " +
						file.getOriginalFilename() + " is empty.");
			}

			responseMap = this.service.importGeodataAndCreateLayer(
					file,
					fileProjection,
					dataType,
					request
			);
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseMap = ResultSet.error("Could not upload the file: " + e.getMessage());
		}

		return new ResponseEntity<>(responseMap, responseHeaders, responseStatus);
	}

	/**
	 *
	 * @param wfsUrl The base URL of the WFS server to fetch the features from,
	 *               e.g. "http://geoserver:8080/geoserver/ows". Required.
	 * @param wfsVersion The WFS version to use, possible values are usually one of
	 *                   1.0.0, 1.1.0 or 2.0.0. Optional.
	 * @param featureType The name of the featureType to fetch and import, e.g.
	 *                    "GDA_Wasser:OG_MESSSTELLEN_NETZ_BESCHRIFTUNG". Required.
	 * @param targetEpsg The EPSG to be used for the imported features, e.g. "EPSG:3857".
	 *                   Optional.
	 * @return
	 */
	@RequestMapping(value = "/wfs.action", method = {RequestMethod.POST})
	public ResponseEntity<Map<String, Object>> importWfs(
			@RequestParam(value = "wfsUrl", required = true) String wfsUrl,
			@RequestParam(value = "wfsVersion", required = false) String wfsVersion,
			@RequestParam(value = "featureType", required = true) String featureType,
			@RequestParam(value = "targetEpsg", required = false) String targetEpsg) {

		LOG.debug("Requested to import featureType " + featureType + " from WFS server " + wfsUrl +
				" (in version " + wfsVersion + " and projection " + targetEpsg + ") as new layer.");

		Map<String, Object> responseMap;
		final HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus responseStatus = HttpStatus.OK;
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);

		try {
			MomoLayer createdLayer = this.service.importWfsAndCreateLayer(
					wfsUrl, wfsVersion, featureType, targetEpsg);

			responseMap = ResultSet.success(createdLayer);
		} catch (Exception e) {
			String errMsg = "Error while creating a layer from WFS GetCapabilities";
			LOG.error(errMsg + ": ", e);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseMap = ResultSet.error(errMsg  + ".");
		}

		return new ResponseEntity<>(responseMap, responseHeaders, responseStatus);
	}

	/**
	 *
	 * @param layerName
	 * @param dataType
	 * @param importJobId
	 * @param taskId
	 * @param fileProjection
	 * @return
	 */
	@RequestMapping(value = "/update-crs-for-import.action", method = {RequestMethod.POST})
	public ResponseEntity<Map<String, Object>> updateCrsForImport(
			@RequestParam("layerName") String layerName,
			@RequestParam("dataType") String dataType,
			@RequestParam("importJobId") Integer importJobId,
			@RequestParam("taskId") Integer taskId,
			@RequestParam(value = "fileProjection") String fileProjection,
			@RequestParam(value = "layerConfig") String layerConfig,
			@RequestParam(value = "imageId") Integer imageId,
			HttpServletRequest request) {

		Map<String, Object> responseMap;
		final HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus responseStatus = HttpStatus.OK;
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);

		try {
			responseMap = this.service.updateCrsForImport(
					layerName,
					dataType,
					importJobId,
					taskId,
					fileProjection,
					layerConfig,
					request,
					imageId);
		} catch (Exception e) {
			LOG.error("updateCrsForImport has thrown an exception. Error was: " + e.getMessage());
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseMap = ResultSet.error("Could not upload the file: " + e.getMessage());
		}
		return new ResponseEntity<>(responseMap, responseHeaders, responseStatus);
	}

	/**
	 *
	 * @param importJobId
	 * @return
	 */
	@RequestMapping(value = "/delete-import-job.action", method = {RequestMethod.POST})
	public ResponseEntity<?> deleteImportJob(@RequestParam("importJobId") Integer importJobId) {

		Map<String, Object> responseMap;
		final HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus responseStatus = HttpStatus.OK;
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);

		try {
			responseMap = this.service.deleteImportJob(importJobId);
		} catch (URISyntaxException | HttpException e) {
			LOG.error("deleteImportJob has thrown an exception. Error was: " + e.getMessage());
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseMap = ResultSet.error("Could not upload the file: " + e.getMessage());
		}

		return new ResponseEntity<>(responseMap, responseHeaders, responseStatus);
	}

	/**
	 * @param service the service to set
	 */
	@Autowired
	@Qualifier("geoServerImporterService")
	public void setService(GeoServerImporterService service) {
		this.service = service;
	}

}