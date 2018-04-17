package de.terrestris.momo.service;

import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.model.Response;

/**
 *
 * @author Johannes Weskamm
 * @author terrestris GmbH & Co. KG
 *
 */
@Service("geoServerFontService")
public class GeoServerFontService {

	/**
	 */
	private static final Logger LOG = Logger.getLogger(GeoServerFontService.class);

	/**
	 *
	 */
	@Value("${geoserver.baseUrl}")
	private String geoServerBaseUrl;

	/**
	 *
	 */
	@Value("${geoserver.username}")
	private String gsuser;

	/**
	 *
	 */
	@Value("${geoserver.password}")
	private String gspassword;

	/**
	 * Get the list of available fonts in GeoServer
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public Response getGeoServerFontList() throws URISyntaxException, HttpException{
		String url = geoServerBaseUrl.split("/momo/ows")[0] + "/rest/resource/fonts";
		LOG.info("Loading fonts from REST path " + url);
		Header[] requestHeaders = {new BasicHeader("Accept", "application/json")};
		return HttpUtil.get(url, gsuser, gspassword, requestHeaders);
	}

	/**
	 * Get a specific font by the given name
	 * @param fontName
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public Response getGeoServerFont(String fontName) throws URISyntaxException, HttpException {
		String url = geoServerBaseUrl.split("/momo/ows")[0] + "/rest/resource/fonts/" + fontName;
		LOG.info("Loading single font from REST path " + url);
		Header[] requestHeaders = {new BasicHeader("Accept", "application/octet-stream")};
		return HttpUtil.get(url, gsuser, gspassword, requestHeaders);
	}

	/**
	 * Get the list of installed fonts in GeoServer
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public Response getGeoServerInstalledFontList() throws URISyntaxException, HttpException{
		String url = geoServerBaseUrl.split("/momo/ows")[0] + "/rest/fonts.json";
		LOG.info("Loading installed fonts from REST path " + url);
		Header[] requestHeaders = {new BasicHeader("Accept", "application/json")};
		return HttpUtil.get(url, gsuser, gspassword, requestHeaders);
	}

}