/**
 *
 */
package de.terrestris.momo.dao;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.zaxxer.hikari.HikariDataSource;

import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.util.importer.RESTImporterPublisher;
import de.terrestris.momo.util.importer.communication.RESTData;
import de.terrestris.shogun2.model.layer.source.ImageWmsLayerDataSource;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.HTTPUtils;
import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageStore;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTLayer.Type;

/**
 *
 */
public class GeoserverPublisherDao extends GeoServerRESTPublisher {

	/**
	 * the logger
	 */
	private static final Logger LOGGER = Logger.getLogger(GeoserverPublisherDao.class);

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
	 * The reader dao
	 */
	@Autowired
	private GeoserverReaderDao gsReaderDao;

	@Autowired
	@Qualifier("geoServerUsername")
	private String gsuser;

	@Autowired
	@Qualifier("geoServerPassword")
	private String gspassword;

	/**
	 * The SQL datasource of the geoserver vector data
	 */
	@Autowired
	@Qualifier("geoserverDataSource")
	private HikariDataSource geoserverDataSource;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerDataDir")
	private String geoServerDataDir;

	@Autowired
	@Qualifier("geoServerRESTImporterPublisher")
	private RESTImporterPublisher publisher;

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(GeoserverPublisherDao.class);

	/**
	 * Super constructor works so far for the moment
	 *
	 * @param restUrl
	 * @param username
	 * @param password
	 * @throws MalformedURLException
	 */
	public GeoserverPublisherDao(String restUrl, String username, String password)
			throws MalformedURLException {
		super(restUrl, username, password);

		this.geoserverRestUrl = restUrl;

		LOGGER.debug("Created a Geoserver Publisher for " + this.geoserverRestUrl);
	}

	/**
	 *
	 * @param layer
	 * @return
	 * @throws Exception
	 */
	public boolean unpublishGeoServerLayer(MomoLayer layer, boolean deleteDataset)
			throws Exception {
		boolean success = false;

		ImageWmsLayerDataSource source = (ImageWmsLayerDataSource) layer.getSource();
		String name = source.getLayerNames();
		if (name.contains(":")) {
			name = name.split(":")[1];
		}
		RESTLayer restLayer = gsReaderDao.getLayer(workspace, name);
		if (restLayer == null) {
			throw new Exception("Could not find the RESTLayer for name " + name);
		}

		Type type = restLayer.getType();
		if (type.equals(Type.VECTOR)) {
			RESTFeatureType restFeatureType = gsReaderDao.getFeatureType(restLayer);
			if (restFeatureType == null) {
				throw new Exception("Could not find the RESTFeatureType for layer " + name);
			}

			RESTDataStore ds = gsReaderDao.getDatastore(restFeatureType);
			String storename = ds.getName();
			success = this.unpublishFeatureType(workspace, storename, name);

			if (success && deleteDataset) {
				dropTableOfVectorData(restFeatureType);
			}

		} else if (type.equals(Type.RASTER)) {
			RESTCoverage restCoverage = gsReaderDao.getCoverage(restLayer);
			if (restCoverage == null) {
				throw new Exception("Could not find the RESTCoverage for layer " + name);
			}
			RESTCoverageStore ds = gsReaderDao.getCoverageStore(restCoverage);
			String storename = ds.getName();

			success = this.unpublishCoverage(workspace, storename, name);
			// Only remove datastores when we have rasters, as these have
			// their own datastore (coveragestore)
			if (success) {
				success = this.removeCoverageStore(workspace, storename, true);
			}

			if (deleteDataset) {
				String restUrl = this.geoserverRestUrl;
				restUrl += "/rest/resource/";
				// delete the rasterfiles from the geoservers data-dir
				String fileUrl = ds.getURL(); // file:uploads/tmp3985519674065752302/DOP20.tif
				if (StringUtils.isEmpty(fileUrl) || !fileUrl.startsWith("uploads")) {
					LOG.info("No data will be deleted.");
					return success;
				}
				fileUrl = fileUrl.substring(5);
				fileUrl = fileUrl.substring(0, fileUrl.lastIndexOf("/"));
				restUrl += fileUrl;
				boolean layerDeleted = HTTPUtils.delete(restUrl, this.gsuser, this.gspassword);
				if (!layerDeleted) {
					throw new Exception(
							"Could not delete MomoLayer data on the file system: " + fileUrl);
				} else {
					LOG.info("Successfully deleted MomoLayer data on the file system: " + fileUrl);
				}
			}
		} else {
			throw new Exception("Could not determine the layertype for layer " + name);
		}

		// remove the style in GeoServer (assuming that we'll only have one
		// default style)
		String layerStyle = restLayer.getDefaultStyle();
		boolean removedStyle = this.removeStyle(layerStyle);

		if (!removedStyle) {
			LOG.error("Could not remove the style " + layerStyle + " of layer " + name);
		}

		return success;
	}

	/**
	 *
	 * @param restFeatureType
	 */
	private void dropTableOfVectorData(RESTFeatureType restFeatureType) {
		// it seems that the native name is the name of the underlying table
		String tableName = restFeatureType.getNativeName();

		Connection c = null;
		Statement stmt = null;

		try {
			c = geoserverDataSource.getConnection();
			stmt = c.createStatement();

			String query = "DROP TABLE IF EXISTS \"" + tableName + "\" CASCADE";

			int result = stmt.executeUpdate(query);

			if (result != 0) {
				throw new Exception(
						"Unexpected status code after executing " + "DROP statement: " + result);
			}

			LOG.debug("Successfully dropped table " + tableName
					+ " from the GeoServer vector data database.");

		} catch (Exception e) {
			LOG.error("Could not delete underlying table " + tableName
					+ " of vector data in the GS DB: " + e.getMessage());
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(c);
		}

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

	/**
	 * @param gsReaderDao the gsReaderDao to set
	 */
	public void setGsReaderDao(GeoserverReaderDao gsReaderDao) {
		this.gsReaderDao = gsReaderDao;
	}

	/**
	 *
	 * @return
	 */
	public HikariDataSource getGeoserverDataSource() {
		return geoserverDataSource;
	}

	/**
	 *
	 * @param geoserverDataSource
	 */
	public void setGeoserverDataSource(HikariDataSource geoserverDataSource) {
		this.geoserverDataSource = geoserverDataSource;
	}

	/**
	 *
	 * @return
	 */
	public String getGeoServerDataDir() {
		return geoServerDataDir;
	}

	/**
	 *
	 * @param geoServerDataDir
	 */
	public void setGeoServerDataDir(String geoServerDataDir) {
		this.geoServerDataDir = geoServerDataDir;
	}

	/**
	 *
	 * @return
	 */
	public GeoserverReaderDao getGsReaderDao() {
		return gsReaderDao;
	}

	/**
	 *
	 * @param geoserverRestUrl
	 */
	public void setGeoserverRestUrl(String geoserverRestUrl) {
		this.geoserverRestUrl = geoserverRestUrl;
	}

	/**
	 * @param importJobId
	 * @throws Exception
	 *
	 */
	public void deleteTemporaryShapeFiles(Integer importJobId) throws Exception {
		RESTData data = this.publisher.getDataOfImportTask(importJobId, 0);
		String restUrl = this.geoserverRestUrl;
		restUrl += "/rest/resource/";

		String fileUrl = data.getLocation(); // something like /var/lib/tomcat7/webapps/geoserver/data/uploads/tmp638865446314869143

		fileUrl = fileUrl.substring(fileUrl.lastIndexOf("uploads/"));
		restUrl += fileUrl;

		boolean layerDeleted = HTTPUtils.delete(restUrl, this.gsuser, this.gspassword);
		if (!layerDeleted) {
			throw new Exception("Could not delete MomoLayer data on the file system: " + fileUrl);
		} else {
			LOG.info("Successfully deleted MomoLayer data on the file system: " + fileUrl);
		}
	}
}
