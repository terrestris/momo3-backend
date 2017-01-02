package de.terrestris.momo.service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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
	@Autowired
	@Qualifier("geoServerBaseUrl")
	private String geoServerBaseUrl;

	/**
	 *
	 */
	@Autowired
	private GeoserverReaderDao gsReaderDao;

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
//					boolean isKml = dataType.equalsIgnoreCase("kml");

					if(dataType != null){
						isRaster = dataType.equalsIgnoreCase("raster");
					}

					boolean isVector = !isRaster;

					if (isVector) {
						// do a wfs export
						String outputFormat;
//						if (isKml) {
//							outputFormat = "kml";
//						} else {
							outputFormat = "SHAPE-ZIP";
//						}
						url = geoServerBaseUrl + "?service=WFS&version=1.0.0&request=GetFeature&" +
								"typeName=" + name + "&outputFormat=" + outputFormat;
						LOG.debug("Requesting a vectorlayer for download with request URL: " + url);

						Response response = HttpUtil.get(url);
						if (response.getStatusCode().is2xxSuccessful()) {
//							if (isKml) {
//								finalZip.putNextEntry(new ZipEntry(layer.getName() + ".kml"));
//							} else {
								finalZip.putNextEntry(new ZipEntry(layer.getName() + ".zip"));
//							}
							finalZip.write(response.getBody());
							finalZip.closeEntry();
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

}
