package de.terrestris.momo.web;

import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.terrestris.momo.service.GeoServerFontService;
import de.terrestris.shogun2.util.model.Response;

/**
 *
 * terrestris GmbH & Co. KG
 * @author Johannes Weskamm
 *
 */
@Controller
@RequestMapping("/font")
public class GeoServerFontController {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(GeoServerFontController.class);

	/**
	 *
	 */
	@Value("${geoserver.baseUrl}")
	private String geoServerBaseUrl;

	/**
	 *
	 */
	@Value("${geoserver.username}")
	private String geoServerUsername;

	/**
	 *
	 */
	@Value("${geoserver.password}")
	private String geoServerPassword;

	@Autowired
	@Qualifier("geoServerFontService")
	private GeoServerFontService geoServerFontService;

	/**
	 * Controller retrieves all available fonts from GEOSERVER_DATA_DIR/fonts
	 * If you want additional fonts, add them to the GEOSERVER_DATA_DIR/fonts folder
	 *
	 * @return
	 */
	@RequestMapping(value = "/getGeoServerFontList.action", method = {RequestMethod.GET})
	public ResponseEntity<?> getGeoServerFontList(){
		Response response = null;
		try {
			response = geoServerFontService.getGeoServerFontList();
			return new ResponseEntity<byte[]>(response.getBody(), response.getHeaders(), response.getStatusCode());
		} catch (URISyntaxException | HttpException e) {
			LOG.error("Error getting the font-list from geoserver: ", e);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<byte[]>(new byte[0], headers, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Controller retrieves all installed fonts available in GeoServer
	 *
	 * @return
	 */
	@RequestMapping(value = "/getGeoServerInstalledFontList.action", method = {RequestMethod.GET})
	public ResponseEntity<?> getGeoServerInstalledFontList(){
		Response response = null;
		try {
			response = geoServerFontService.getGeoServerInstalledFontList();
			return new ResponseEntity<byte[]>(response.getBody(), response.getHeaders(), response.getStatusCode());
		} catch (URISyntaxException | HttpException e) {
			LOG.error("Error getting the font-list from geoserver: ", e);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<byte[]>(new byte[0], headers, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Controller gets a specific ttf font by the given name
	 *
	 * @return
	 */
	@RequestMapping(value = "/getGeoServerFont.action", method = {RequestMethod.GET})
	public ResponseEntity<?> getGeoServerFont(
			@RequestParam("fontName") String fontName){
		Response response = null;
		try {
			response = geoServerFontService.getGeoServerFont(fontName);
			return new ResponseEntity<byte[]>(response.getBody(), response.getHeaders(), response.getStatusCode());
		} catch (URISyntaxException | HttpException e) {
			LOG.error("Error getting the font-list from geoserver: ", e);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<byte[]>(new byte[0], headers, HttpStatus.NOT_FOUND);
		}
	}
}
