package de.terrestris.momo.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.terrestris.momo.dao.GeoserverPublisherDao;
import de.terrestris.momo.dao.GeoserverReaderDao;
import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.model.layer.source.ImageWmsLayerDataSource;
import de.terrestris.shogun2.service.LayerService;
import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.model.Response;

/**
 *
 * @author Nils Bühner
 * @author Kai Volland
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("momoLayerService")
public class MomoLayerService<E extends MomoLayer, D extends MomoLayerDao<E>>
		extends LayerService<E, D> {

	/**
	 *
	 */
	@Value("${geoserver.baseUrl}")
	private String geoServerBaseUrl;

	/**
	 *
	 */
	@Autowired
	private GeoserverReaderDao gsReaderDao;

	/**
	 *
	 */
	@Autowired
	private GeoserverPublisherDao gsPublisherDao;

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

	@Autowired
	@Qualifier("metadataService")
	private MetadataService metadataService;

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoLayerDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	/**
	 *
	 * @param layerId
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("isAuthenticated()")
	@Transactional(readOnly = true)
	public String getLayerExtent(Integer layerId) throws Exception {
		MomoLayer layer = this.findById(layerId);
		String extent = gsReaderDao.getLayerExtent(layer);
		return extent;
	}

	/**
	 * Finds and returns a layer by it's URL and the layerNames parameter.
	 *
	 * @param url
	 * @param layerNames
	 * @return
	 */
	@PostAuthorize("hasRole(@configHolder.getSuperAdminRoleName()) or hasPermission(returnObject, 'READ')")
	@Transactional(readOnly = true)
	public E findByUrlAndLayerNames(String url, String layerNames) {
		if (url == null || layerNames == null) {
			return null;
		}
		return dao.findByUrlAndLayerNames(url, layerNames);
	}

	/**
	 * Method generates a zipfile for multiple layers by downloading them from
	 * geoserver via wfs / wcs
	 *
	 * @param layerIds
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("isAuthenticated()")
	@Transactional(readOnly = true)
	public byte[] downloadLayers(List<Integer> layerIds) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream finalZip = new ZipOutputStream(baos);

		try {
			for (Integer layerId : layerIds) {
				MomoLayer layer = this.findById(layerId);
				if (layer != null) {

					ImageWmsLayerDataSource source = (ImageWmsLayerDataSource) layer.getSource();
					String name = source.getLayerNames();
					String url;
					String dataType = layer.getDataType();
					boolean isRaster = false;
					if(dataType != null){
						isRaster = dataType.equalsIgnoreCase("raster");
					}
					boolean isVector = !isRaster;

					if (isVector) {
						// do a wfs export
						String outputFormat;
						outputFormat = "SHAPE-ZIP";
						url = geoServerBaseUrl + "?service=WFS&version=1.0.0&request=GetFeature&" +
								"typeName=" + name + "&outputFormat=" + outputFormat;
						LOG.debug("Requesting a vectorlayer for download with request URL: " + url);

						Response response = HttpUtil.get(url);
						if (response.getStatusCode().is2xxSuccessful()) {
							// unzip the files to only have a single zip archive
							byte[] buffer = new byte[1024];
							ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(response.getBody()));
							ZipEntry zipEntry = zis.getNextEntry();
							while(zipEntry != null){
								String fileName = zipEntry.getName();
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								int len;
								while ((len = zis.read(buffer)) > 0) {
									bos.write(buffer, 0, len);
								}
								bos.close();
								finalZip.putNextEntry(new ZipEntry(fileName));
								finalZip.write(bos.toByteArray());
								finalZip.closeEntry();
								zipEntry = zis.getNextEntry();
							}
							zis.closeEntry();
							zis.close();
						} else {
							LOG.error("Error on downloading a vectorlayer named " + layer.getName());
						}
					} else {
						// do a wcs export
						url = geoServerBaseUrl + "?service=WCS&version=2.0.1&request=GetCoverage&" +
								"coverageId=" + name + "&format=geotiff";
						LOG.debug("Requesting a rasterlayer for download with request URL: " + url);

						Response response = HttpUtil.get(url);
						if (response.getStatusCode().is2xxSuccessful()) {
							finalZip.putNextEntry(new ZipEntry(layer.getName() + ".tif"));
							finalZip.write(response.getBody());
							finalZip.closeEntry();
						} else {
							LOG.error("Error on downloading a rasterlayer named " + layer.getName());
						}
					}
					// also add the metadataset from GNOS to the zip
					LOG.debug("Requesting a metadataset for the layer to download");
					String xml = "<?xml version=\"1.0\"?><csw:GetRecordById xmlns:csw=\"http://www.opengis.net/cat/" +
						"csw/2.0.2\" service=\"CSW\" version=\"2.0.2\" outputSchema=\"http://www.isotc211.org/2005/" +
						"gmd\"><csw:Id>" + layer.getMetadataIdentifier() + "</csw:Id>" +
						"<csw:ElementSetName>full</csw:ElementSetName></csw:GetRecordById>";
					try {
						String response = metadataService.cswRequest(layer.getId(), "READ", xml);
						finalZip.putNextEntry(new ZipEntry("Metadata.xml"));
						finalZip.write(response.getBytes());
						finalZip.closeEntry();
					} catch (Exception e) {
						LOG.error("Error while adding metadata to the download zip file");
					}
					// also add the default SLD of the layer
					try {
						String defaultStyleName = getDefaultStyleName(name);
						if (defaultStyleName != null) {
							byte[] defaultStyle = getDefaultStyle(defaultStyleName);
							if (defaultStyle != null) {
								finalZip.putNextEntry(new ZipEntry(defaultStyleName + ".sld"));
								finalZip.write(defaultStyle);
								finalZip.closeEntry();
							}
							// finally add some metadata like the layername or if geoserver uses a static
							// legend image etc. to a separate config file
							ObjectMapper mapper = new ObjectMapper();
							String config = "{\"layername\": \"" + layer.getName() + "\"";
							JsonNode legendNode = getStaticLegend(defaultStyleName);
							if (legendNode != null) {
								config += ",\"legend\": " + legendNode.toString();
							}
							config += "}";
							JsonNode configNode = mapper.readTree(config);
							finalZip.putNextEntry(new ZipEntry("config.json"));
							finalZip.write(configNode.toString().getBytes());
							finalZip.closeEntry();
						}
					} catch (Exception e) {
						LOG.error("Error while generating SLD and config for layer " + layer.getName());
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Error while generating a download zip file for layers with ids " + layerIds.toString());
		} finally {
			IOUtils.closeQuietly(finalZip);
			IOUtils.closeQuietly(baos);
		}
		return baos.toByteArray();
	}

	private JsonNode getStaticLegend(String defaultStyleName) throws URISyntaxException, HttpException {
		JsonNode staticLegend = null;
		String url = geoServerBaseUrl.split("/momo/ows")[0] + "/rest/styles/" + defaultStyleName;
		LOG.debug("Requesting the layers legend configuration with url: " + url);
		Header[] requestHeaders = {new BasicHeader("Accept", "application/json")};
		Response response =  HttpUtil.get(url, gsuser, gspassword, requestHeaders);
		if (response.getStatusCode().is2xxSuccessful()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode node = mapper.readTree(response.getBody());
				JsonNode styleNode = node.get("style");
				staticLegend = styleNode.get("legend");
			} catch (Exception e) {
				LOG.error("Error on getting the legdn config: " + e.getMessage());
			}
		} else {
			LOG.error("Error on getting a legend config");
		}
		return staticLegend;
	}

	/**
	 *
	 * @param name
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	private String getDefaultStyleName(String name) throws URISyntaxException, HttpException {
		String defaultStyleName = null;
		String url = geoServerBaseUrl.split("/momo/ows")[0] + "/rest/layers/" + name;
		LOG.debug("Requesting the layers configuration with url: " + url);
		Header[] requestHeaders = {new BasicHeader("Accept", "application/json")};
		Response response =  HttpUtil.get(url, gsuser, gspassword, requestHeaders);
		if (response.getStatusCode().is2xxSuccessful()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode node = mapper.readTree(response.getBody());
				JsonNode layerNode = node.get("layer");
				JsonNode defaultStyle = layerNode.get("defaultStyle");
				defaultStyleName = defaultStyle.get("name").asText();
			} catch (Exception e) {
				LOG.error("Error on getting the default style name: " + e.getMessage());
			}
		} else {
			LOG.error("Error on getting a default style");
		}
		return defaultStyleName;
	}

	/**
	 *
	 * @param defaultStyleName
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public byte[] getDefaultStyle(String defaultStyleName) throws URISyntaxException, HttpException {
		byte[] defaultStyleByteArray = null;
		String url = geoServerBaseUrl.split("/momo/ows")[0] + "/rest/styles/" + defaultStyleName + ".sld";
		LOG.debug("Requesting the default Style for download with request URL: " + url);
		Header[] sldRequestHeaders = {new BasicHeader("Accept", "application/xml")};
		Response sldResponse =  HttpUtil.get(url, gsuser, gspassword, sldRequestHeaders);
		if (sldResponse.getStatusCode().is2xxSuccessful()) {
			defaultStyleByteArray = sldResponse.getBody();
		} else {
			LOG.error("Error on downloading a default style");
		}
		return defaultStyleByteArray;
	}

	@SuppressWarnings("unchecked")
	public void deleteMomoLayer(MomoLayer layer) {
		try {
			this.gsPublisherDao.unpublishGeoServerLayer(layer, true);
		} catch (Exception e) {
			LOG.error("Error deleting Layer in GeoServer: "
					+ e.getMessage());
		}
		this.delete((E) layer);
	}
}
