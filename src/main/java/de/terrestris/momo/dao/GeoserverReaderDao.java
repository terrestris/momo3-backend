/**
 *
 */
package de.terrestris.momo.dao;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTBoundingBox;
import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTLayer.Type;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;

import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.model.layer.source.ImageWmsLayerDataSource;

/**
 *
 */
public class GeoserverReaderDao extends GeoServerRESTReader {

	/**
	 * the logger
	 */
	private static final Logger LOGGER = Logger.getLogger(GeoserverReaderDao.class);

	/**
	 * the URL to the geoserver instance
	 */
	private String geoserverRestUrl;

	/**
	 * the workspace name of the geoserver instance
	 */
	private String workspace;

	/**
	 * the datastore name of the geoserver instance
	 */
	private String datastore;

	/**
	 * Super constructor works so far for the moment
	 *
	 * @param restUrl
	 * @param username
	 * @param password
	 * @throws MalformedURLException
	 */
	public GeoserverReaderDao(String restUrl, String username, String password)
			throws MalformedURLException {
		super(restUrl, username, password);

		this.geoserverRestUrl = restUrl;

		LOGGER.debug("Created a Geoserver Reader-DAO for " + this.geoserverRestUrl);
	}

	/**
	 * Get the data extent for the given layer
	 *
	 * @param layer
	 * @return
	 * @throws Exception
	 */
	public String getLayerExtent(MomoLayer layer) throws Exception {
		String bbox = "";
		ImageWmsLayerDataSource source = (ImageWmsLayerDataSource) layer.getSource();
		String name = source.getLayerNames();
		if (name.contains(":")) {
			name = name.split(":")[1];
		}
		// only handle layers that are available in our geoserver
		if (!source.getUrl().toLowerCase().contains("geoserver.action")) {
			return "";
		}
		RESTLayer restLayer = this.getLayer(workspace, name);
		if (restLayer == null) {
			throw new Exception("Could not find the RESTLayer for name " + name);
		}

		RESTBoundingBox restBbox;
		Type type = restLayer.getType();
		if (type.equals(Type.VECTOR)) {
			RESTFeatureType restFeatureType = this.getFeatureType(restLayer);
			if (restFeatureType == null) {
				throw new Exception("Could not find the RESTFeatureType for layer " + name);
			}
			restBbox = restFeatureType.getNativeBoundingBox();
		} else if (type.equals(Type.RASTER)){
			RESTCoverage restCoverage = this.getCoverage(restLayer);
			if (restCoverage == null) {
				throw new Exception("Could not find the RESTCoverage for layer " + name);
			}
			restBbox = restCoverage.getNativeBoundingBox();
		} else {
			throw new Exception("Could not determine the layertype for layer " + name);
		}

		if (restBbox.getCRS() != "EPSG:3857") {
			CoordinateReferenceSystem sourceCRS;
			// for geographic projections, we need to switch axes
			if (restBbox.getCRS().equalsIgnoreCase("EPSG:4326")) {
				sourceCRS = CRS.decode(restBbox.getCRS(), true);
			} else {
				sourceCRS = CRS.decode(restBbox.getCRS());
			}

			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857");
			MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

			Coordinate ll = new Coordinate(restBbox.getMinX(), restBbox.getMinY());
			Coordinate ur = new Coordinate(restBbox.getMaxX(), restBbox.getMaxY());
			Coordinate transformedLl = JTS.transform(ll, null, transform);
			Coordinate transformedUr = JTS.transform(ur, null, transform);

			bbox =
				String.valueOf(transformedLl.x) + ", " +
				String.valueOf(transformedLl.y) + ", " +
				String.valueOf(transformedUr.x) + ", " +
				String.valueOf(transformedUr.y);
		} else {
			bbox =
				String.valueOf(restBbox.getMinX()) + ", " +
				String.valueOf(restBbox.getMinY()) + ", " +
				String.valueOf(restBbox.getMaxX()) + ", " +
				String.valueOf(restBbox.getMaxY());
		}

		return bbox;
	}

	/**
	 *
	 */
	public boolean layerExists(String qualifiedFeatureType) {
		boolean layerExists = false;

		String unqualifiedFeatureTypeName = unqualifyFeatureTypeName(qualifiedFeatureType);

		RESTLayer layer = this.getLayer(this.workspace, unqualifiedFeatureTypeName);

		if(layer != null){
			layerExists = true;
		}

		return layerExists ;
	}

	/**
	 *
	 * @param qualifiedFeatureType
	 * @return
	 */
	public static String unqualifyFeatureTypeName(String qualifiedFeatureType) {
		String newFeatureTypeName = qualifiedFeatureType;
		if (qualifiedFeatureType.contains(":")) {
			int beginIndex = qualifiedFeatureType.indexOf(":") + 1;
			newFeatureTypeName = qualifiedFeatureType.substring(beginIndex);
		}
		return newFeatureTypeName;
	}

	/**
	 * @return the geoserverRestUrl
	 */
	public String getGeoserverRestUrl() {
		return geoserverRestUrl;
	}

	/**
	 * @return the workspace
	 */
	public String getWorkspace() {
		return workspace;
	}


	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}


	/**
	 * @return the datastore
	 */
	public String getDatastore() {
		return datastore;
	}


	/**
	 * @param datastore the datastore to set
	 */
	public void setDatastore(String datastore) {
		this.datastore = datastore;
	}
}
