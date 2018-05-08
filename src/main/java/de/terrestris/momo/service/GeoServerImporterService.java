package de.terrestris.momo.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.crs.ReprojectFeatureResults;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import de.terrestris.momo.dao.GeoserverPublisherDao;
import de.terrestris.momo.dao.GeoserverReaderDao;
import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.dao.MomoUserDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.util.importer.TableNameValidator;
import de.terrestris.momo.util.importer.VectorFeatureTypeTransformer;
import de.terrestris.shogun2.dao.ImageFileDao;
import de.terrestris.shogun2.dao.LayerAppearanceDao;
import de.terrestris.shogun2.importer.GeoServerRESTImporter;
import de.terrestris.shogun2.importer.GeoServerRESTImporterException;
import de.terrestris.shogun2.importer.communication.RESTData;
import de.terrestris.shogun2.importer.communication.RESTImport;
import de.terrestris.shogun2.importer.communication.RESTImportTask;
import de.terrestris.shogun2.importer.communication.RESTImportTaskList;
import de.terrestris.shogun2.importer.communication.RESTLayer;
import de.terrestris.shogun2.model.ImageFile;
import de.terrestris.shogun2.model.layer.appearance.LayerAppearance;
import de.terrestris.shogun2.model.layer.source.TileWmsLayerDataSource;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.service.LayerAppearanceService;
import de.terrestris.shogun2.util.data.ResultSet;
import de.terrestris.shogun2.util.enumeration.InterceptorEnum.RuleType;
import de.terrestris.shogun2.util.enumeration.OgcEnum;
import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.model.Response;
import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Service("geoServerImporterService")
public class GeoServerImporterService {

	/**
	 */
	private static final Logger LOG = Logger.getLogger(GeoServerImporterService.class);

	/**
	 * constant values
	 */
	private static final String EXPECTED_CSV_ENCODING = "UTF-8";
	private static char CSVFIELDDIVIDERCHAR = ';';
	private static char CSVQUOTECHAR = '\'';
	private static final String NAME_OF_ABSCISSA = "X";
	private static final String NAME_OF_ORDINATE = "Y";
	private static final String NAME_OF_GEOMETRY = "the_geom";
	private static final String GEOTOOLS_PT_HEADER = NAME_OF_GEOMETRY + ":Point";
	private static final String DEFAULT_WFS_VERSION = "1.1.0";
	private static final String WFS_MAX_FEATURES = "0"; // 0 = no limit
	private static final String WFS_IMPORT_TABLE_PREFIX = "WFS_IMP";

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerImporter")
	private GeoServerRESTImporter importer;

	/**
	 *
	 */
	@Value("${geoserver.baseUrl}")
	private String geoServerBaseUrl;

	/**
	 * The workspace of newly created layers.
	 */
	@Value("${geoserver.importer.workspace}")
	private String targetWorkspace;

	/**
	 *
	 */
	@Value("${geoserver.importer.datastore}")
	private String targetDatastore;

	/**
	 *
	 */
	@Value("#{${geoserver.importer.datastore.connectionParams}}")
	private Map<String,String> targetDatastoreConnParams;

	/**
	 * The URL/path to `geoserver.action`.
	 */
	@Value("${momo.publicInterceptGeoServerAction}")
	private String interceptGeoServerAction;

	/**
	 *
	 */
	@Value("${geoserver.defaultSRS}")
	private String geoServerDefaultSrs;

	/**
	 *
	 */
	@Value("${geoserver.importer.raster.performGdalAddo}")
	private Boolean geoServerImporterPerformGdalAddo;

	/**
	 *
	 */
	@Value("${geoserver.importer.raster.gdalAddoLevels}")
	private String geoServerImporterGdalAddoLevels;

	/**
	 *
	 */
	@Value("${geoserver.importer.raster.performGdalWarp}")
	private Boolean geoServerImporterPerformGdalWarp;

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
	 * The timeout to use in importer HTTP requests.
	 */
	@Value("${geoserver.importer.http.timeout}")
	private int importerHttpTimeout;

	/**
	 *
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoLayerService")
	private MomoLayerService<MomoLayer, MomoLayerDao<MomoLayer>> momoLayerService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoUserService")
	private MomoUserService<MomoUser, MomoUserDao<MomoUser>> momoUserService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("layerAppearanceService")
	private LayerAppearanceService<LayerAppearance, LayerAppearanceDao<LayerAppearance>> layerAppearanceService;

	/**
	 * The InterceptorRuleService to use.
	 */
	@Autowired
	@Qualifier("momoInterceptorRuleService")
	private MomoInterceptorRuleService momoInterceptorRuleService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("imageFileDao")
	private ImageFileDao<ImageFile> imageFileDao;

	/**
	 * The GeoServer reader dao
	 */
	@Autowired
	private GeoserverReaderDao gsReaderDao;

	/**
	 * The GeoServer publisher dao.
	 */
	@Autowired
	private GeoserverPublisherDao gsPublisherDao;

	@Autowired
	@Qualifier("sldService")
	private SldService sldService;

	/**
	 *
	 * @param file
	 * @param fileProjection
	 * @param layerType
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> importGeodataAndCreateLayer(
			MultipartFile file,
			String fileProjection,
			String layerType, HttpServletRequest request
	) throws Exception {
		Map<String, Object> responseMap = new HashMap<String, Object>();

		// 1. Check if a CSV file has been uploaded. If true => transform to shapefile.
		if (StringUtils.containsIgnoreCase(file.getContentType(), "text/csv") ||
				StringUtils.containsIgnoreCase(file.getContentType(), "text/plain")) {
			file = this.createShapeFileForCsv(file);
		}

		// 2. Create the import job.
		RESTImport restImport = null;
		if (layerType.equalsIgnoreCase("vector")) {
			restImport = this.importer.createImportJob(this.targetWorkspace, this.targetDatastore);
		} else if (layerType.equalsIgnoreCase("raster")) {
			restImport = this.importer.createImportJob(this.targetWorkspace, null);
		} else {
			throw new GeoServerRESTImporterException("Invalid layerType given. " +
					"Valid options are: vector, raster");
		}

		// handle import metadata, config and SLD
		String layerConfig = null;
		try {
			layerConfig = this.handleLayerConfig(file);
		} catch (Exception e) {
			LOG.error("Could not handle layer metadata: " + e.getMessage());
		}

		// handle possibly included legend image
		ImageFile image = null;
		HashMap<ImageFile, MultipartFile> fileMap = this.handleStaticLegendImage(file);
		Set<Entry<ImageFile, MultipartFile>> entry = fileMap.entrySet();
		for (Entry<ImageFile, MultipartFile> singleEntry : entry) {
			image = singleEntry.getKey();
			file = singleEntry.getValue();
		}

		// 3. Upload the import file.
		Integer importJobId = restImport.getId();
		RESTImportTaskList importTasks = null;
		importTasks = this.uploadZipFile(importJobId, file, fileProjection);

		if (importTasks == null) {
			throw new GeoServerRESTImporterException("Import task could not be created.");
		}

		// 4. Check if we need to set the SRS of the import layer manually.
		RESTImportTaskList tasksWithoutProjection = new RESTImportTaskList(); //this.checkSrsOfImportTasks(importTasks);

		// 5. Add transform tasks.
		try {
			createTransformTasks(fileProjection, layerType, importJobId, importTasks);
		} catch (GeoServerRESTImporterException gsrie) {
			// TODO what happens if more than one importTasks are contained here?
			tasksWithoutProjection.addAll(importTasks);
		}

		// Redefine broken tasks
		if (tasksWithoutProjection.size() > 0 && StringUtils.isEmpty(fileProjection)) {
			responseMap = ResultSet.error("NO_CRS or invalid CRS (EPSG:404000) detected and "
					+"no fileProjection is given.");
			responseMap.put("importJobId", importJobId);
			responseMap.put("tasksWithoutProjection", tasksWithoutProjection);
			responseMap.put("error", "NO_CRS");
			responseMap.put("layerConfig", layerConfig);
			responseMap.put("legendImageId", image.getId());
			return responseMap;
		}

		// 6. Run the import and create the SHOGun layer
		responseMap = runJobAndCreateLayer(file.getOriginalFilename(), layerType, importJobId, layerConfig, request, image.getId());

		return responseMap;
	}

	private HashMap<ImageFile, MultipartFile> handleStaticLegendImage(MultipartFile file) throws IOException {
		ImageFile fileToPersist = new ImageFile();
		InputStream inputStream =  new BufferedInputStream(file.getInputStream());
		ZipInputStream zis = new ZipInputStream(inputStream);
		File cleanedFile = File.createTempFile(file.getName(), null);
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(cleanedFile));
		ZipEntry entry = zis.getNextEntry();
		byte[] buffer = new byte[1024];
		try {
			while (entry != null) {
				String fileName = entry.getName();
				if (fileName.startsWith("legend.")) {
					ByteArrayOutputStream bos = this.fileToStream(zis, buffer);
					fileToPersist.setFile(bos.toByteArray());
					fileToPersist.setFileName(fileName);
					String suffix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
					fileToPersist.setFileType("image/" + suffix);
					imageFileDao.saveOrUpdate(fileToPersist);
				} else {
					// add entry to the new zip
					ZipEntry zipEntry = new ZipEntry(fileName);
					zos.putNextEntry(zipEntry);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
					zos.closeEntry();
				}
				entry = zis.getNextEntry();
			}
		} finally {
			zos.finish();
			zos.close();
			zis.close();
		}

		final DiskFileItem diskFileItem = new DiskFileItem("file", "application/zip", true, file.getOriginalFilename(), 100000000, cleanedFile.getParentFile());
		InputStream input =  new FileInputStream(cleanedFile);
		OutputStream os = diskFileItem.getOutputStream();
		IOUtils.copy(input, os);

		HashMap<ImageFile, MultipartFile> map = new HashMap<ImageFile, MultipartFile>();
		map.put(fileToPersist, new CommonsMultipartFile(diskFileItem));
		return map;
	}

	private String handleLayerConfig(MultipartFile file) throws IOException {
		String config = null;
		InputStream inputStream =  new BufferedInputStream(file.getInputStream());
		ZipInputStream zis = new ZipInputStream(inputStream);
		ZipEntry entry = zis.getNextEntry();
		byte[] buffer = new byte[1024];
		config = "{";
		while (entry != null) {
			String fileName = entry.getName();
			if (fileName.equalsIgnoreCase("config.json")) {
				ByteArrayOutputStream bos = this.fileToStream(zis, buffer);
				if (config.length() > 1) {
					config += ",";
				}
				String configString = bosToCleanString(bos);
				config += "\"config\": \"" + configString + "\"";
			} else if (fileName.equalsIgnoreCase("metadata.xml")) {
				ByteArrayOutputStream bos = this.fileToStream(zis, buffer);
				if (config.length() > 1) {
					config += ",";
				}
				String metadataString = bosToCleanString(bos);
				config += "\"metadata\": \"" + metadataString + "\"";
			} else if (fileName.toLowerCase().contains(".sld")) {
				ByteArrayOutputStream bos = this.fileToStream(zis, buffer);
				if (config.length() > 1) {
					config += ",";
				}
				String sldString = bosToCleanString(bos);
				config += "\"sld\": \"" + sldString + "\"";
			}
			entry = zis.getNextEntry();
		}
		config += "}";
		return config;
	}

	private String bosToCleanString(ByteArrayOutputStream bos) {
		String cleanString = null;
		cleanString = bos.toString();
		cleanString = cleanString.replaceAll("\\n", "");
		cleanString = cleanString.replaceAll("\\r", "");
		cleanString = cleanString.replaceAll("\"", "\\\\\"");
		return cleanString;
	}


//	private String getLayerNameFromJsonConfig(String layerConfig) throws IOException {
//		ObjectMapper mapper = new ObjectMapper();
//		JsonNode configNode = mapper.readTree(layerConfig);
//		String layername = null;
//		if (configNode.get("config") != null) {
//			JsonNode layerConfigNode = mapper.readTree(configNode.get("config").asText());
//			if (layerConfigNode.get("layername") != null) {
//				layername = layerConfigNode.get("layername").asText();
//			}
//		}
//		return layername;
//	}

	private void handleSLDandLegendFromJsonConfig(String config, MomoLayer layer, HttpServletRequest request) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode configNode = mapper.readTree(config);
		if (configNode.get("config") != null) {
			JsonNode layerConfigNode = mapper.readTree(configNode.get("config").asText());
			if (layerConfigNode.get("legend") != null) {
				JsonNode legendNode = layerConfigNode.get("legend");
				Integer width = legendNode.get("width").asInt();
				Integer height = legendNode.get("height").asInt();
				String format = legendNode.get("format").asText();
				String onlineResource = legendNode.get("onlineResource").asText();
				sldService.updateLegendSrc(layer.getId(), width, height, onlineResource, format, request);
			}
		}
		if (configNode.get("sld") != null) {
			String sld = configNode.get("sld").asText();
			if (!StringUtils.isEmpty(sld)) {
				sldService.publishSLDAsDefault(layer.getId(), sld);
			}
		}
	}

	private ByteArrayOutputStream fileToStream(ZipInputStream zis, byte[] buffer) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int len;
		while ((len = zis.read(buffer)) > 0) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos;
	}

	/**
	 * Generate Shapefile (ZIP) containing points (ONLY!) based on CSV
	 * @param uploadedCsv {@link MultipartFile} which contains CSV
	 * @return {@link MultipartFile} containing ZIP file which shape (POINT)
	 * @throws Exception
	 */
	private MultipartFile createShapeFileForCsv(MultipartFile uploadedCsv) throws Exception {
		MultipartFile createdShapeZip;

		long csvFileSize = uploadedCsv.getSize();
		String csvFileName = uploadedCsv.getOriginalFilename();
		String csvFileContentType = uploadedCsv.getContentType();
		LOG.info("Trying to transform features of " + csvFileName);
		LOG.debug("The Content-Type of the file is " + csvFileContentType + ". The file-size is " + csvFileSize + " bytes.");
		String csvFileNameWithoutExtension = csvFileName;
		if (csvFileNameWithoutExtension.contains(".")) {
			csvFileNameWithoutExtension = csvFileNameWithoutExtension.substring(0, csvFileNameWithoutExtension.lastIndexOf("."));
		}

		Transaction transaction = null;

		try (
				InputStream is = uploadedCsv.getInputStream();
				// TODO get rid of deprecated constructor
				CSVReader reader = new CSVReader(new InputStreamReader(is, EXPECTED_CSV_ENCODING), CSVFIELDDIVIDERCHAR, CSVQUOTECHAR);
		) {
			// HashMap to store the entries of the CSV file in
			// Each line results in an entry of the HashMap
			HashMap<String, HashMap<String, String>> retVals = new HashMap<>();

			String[] nextLine;
			int lineCounter = 0;
			String[] header = null;

			while ((nextLine = reader.readNext()) != null) {
				if (lineCounter == 0) {
					header = nextLine;
					lineCounter++;
					continue;
				}
				HashMap<String, String> inlineObject = new HashMap<String, String>();

				if (header.length != nextLine.length) {
					// this line will be ignored from import
					continue;
				}

				for (int i = 0; i < nextLine.length; i++) {
					inlineObject.put(header[i].trim().replace("'", "").toLowerCase(),
							nextLine[i].trim().replace("'", ""));
				}
				retVals.put("Object_" + lineCounter, inlineObject);
				lineCounter++;
			}

			String schemaName = "IMP_"+ System.currentTimeMillis() + "_" + csvFileNameWithoutExtension;
			if (! TableNameValidator.isValidName(schemaName)){
				schemaName = TableNameValidator.createValidTableName(schemaName);
			}

			// create feature type and feature collection
			SimpleFeatureType featureTypeDefinition = createFeatureType(header, schemaName);
			SimpleFeatureCollection featureCollection = createFeatureCollectionFromCsv(featureTypeDefinition, retVals);

			// Define temporary directories to put the shapefile in
			File tmpDirBase = FileUtils.getTempDirectory();
			String tmpDirBasePath = tmpDirBase.getAbsolutePath();
			File newFile = new File(tmpDirBasePath + "/" +schemaName+".shp");
			Map<String, Serializable> params = new HashMap<>();
			params.put("url", newFile.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);

			// create shapefile using datastore factory
			ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
			ShapefileDataStore shapefileDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
			shapefileDataStore.createSchema(featureTypeDefinition);

			 /*
			 * Write the features to the shapefile
			 */
			transaction = new DefaultTransaction("create");
			String typeName = shapefileDataStore.getTypeNames()[0];
			SimpleFeatureSource featureSource = shapefileDataStore.getFeatureSource(typeName);
			SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

			LOG.debug("Write temporary shapefile -- SHAPE:"+SHAPE_TYPE);
			if (featureSource instanceof SimpleFeatureStore) {
				SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
				featureStore.setTransaction(transaction);
				try {
					featureStore.addFeatures(featureCollection);
					transaction.commit();
				} catch (Exception problem) {
					LOG.debug("Could not add features to shapefile", problem);
					transaction.rollback();
				} finally {
					transaction.close();
				}
				LOG.info("Successfully created shapefile from CSV. Will now pack it to a ZIP file...");
				createdShapeZip = createShapeZip(tmpDirBasePath, schemaName);
			} else {
				throw new Exception("Could not create appropriate shapefile datastore");
			}
		} finally {
			// TODO this log is probably wrong because because we can also come to the finally
			// block when an exception has been catched
			LOG.debug("Created ZIP file containing shapefile based on CSV successfully.");
			IOUtils.closeQuietly(transaction);
		}

		return createdShapeZip;
	}

	/**
	 * Generate multipart file (ZIP) for shapefile
	 * @param tempDir path to temporary directory
	 * @param shapeName Name pof the shapefile
	 * @return MultipartFile representing ZIP file which contains parts of shapefile
	 * @throws IOException
	 */
	private MultipartFile createShapeZip(String tempDir, String shapeName) throws IOException {
		final String basePath = tempDir +"/";
		final String zipFileName = basePath + shapeName + ".zip";
		try (
			FileOutputStream fos = new FileOutputStream(zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);
		) {
			// only "shp", "dbf", "shx" should be included in ZIP file so that the user must set CRS manually
			for (String ending : new String[]{"shp", "dbf", "shx"}) {
				String fileName = basePath + shapeName + "."+ending;
				File file = new File(fileName);
				try (
					FileInputStream fis = new FileInputStream(file);
				) {
					ZipEntry zipEntry = new ZipEntry(shapeName + "."+ending);
					zos.putNextEntry(zipEntry);
					byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						zos.write(bytes, 0, length);
					}

					zos.closeEntry();
				} finally {
					LOG.trace("Added " + fileName + " to ZIP file");
				}
			}
			zos.finish();
		} finally {

			// TODO get rid of warnings. make it better next playday
			final File zipFile = new File(zipFileName);
			final DiskFileItem diskFileItem = new DiskFileItem("file", "application/zip", true, zipFile.getName(), 100000000, zipFile.getParentFile());

			InputStream input =  new FileInputStream(zipFile);
			OutputStream os = diskFileItem.getOutputStream();
			IOUtils.copy(input, os);

			return new CommonsMultipartFile(diskFileItem);
		}
	}

	/**
	 * Helper method creating feature collection
	 * @param featureTypeDefinition {@link SimpleFeatureType} of provided features
	 * @param retVals {@link HashMap} representing features
	 * @return {@link SimpleFeatureCollection} feature collection
	 */
	private SimpleFeatureCollection createFeatureCollectionFromCsv(SimpleFeatureType featureTypeDefinition, HashMap<String, HashMap<String, String>> retVals) {
		ArrayList<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
		PrecisionModel precModel = new PrecisionModel(PrecisionModel.FLOATING);
		GeometryFactory geomFactory = new GeometryFactory(precModel);

		long internalId = 1;
		for (String key : retVals.keySet()) {
			String x = null, y = null;

			HashMap<String, String> geoObject = retVals.get(key);
			x = geoObject.get(NAME_OF_ABSCISSA.toLowerCase());
			y = geoObject.get(NAME_OF_ORDINATE.toLowerCase());

			if (StringUtils.isBlank(x) || StringUtils.isBlank(y)) {
				LOG.warn("Object " + geoObject + " will be ignored from import since one/more coordinate(s) are empty.");
				continue;
			}

			double xVal = Double.NaN, yVal = Double.NaN;
			try {
				xVal = Double.parseDouble(x);
				yVal = Double.parseDouble(y);
			} catch (NumberFormatException nfe) {
				LOG.warn("Object " + geoObject
						+ " will be ignored from import since one/more cordinates don't contain numerical values.");
				continue;
			}

			Point pt = geomFactory.createPoint(new Coordinate(xVal, yVal));
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureTypeDefinition);

			// Iterate over featureType
			List<AttributeDescriptor> attrDescrList = featureTypeDefinition.getAttributeDescriptors();
			for (AttributeDescriptor attrDesc : attrDescrList){
				Name attributeName = attrDesc.getName();
				if ( attributeName.getLocalPart().equalsIgnoreCase(NAME_OF_GEOMETRY) ){
					featureBuilder.add(pt);
				}else{
					String attrVal = geoObject.get(attributeName.getLocalPart().toLowerCase());
					featureBuilder.add(attrVal);
				}
			}

			SimpleFeature feature = featureBuilder.buildFeature( Long.toString(internalId) );
			featureList.add(feature);

			internalId ++;
		}

		SimpleFeatureCollection featureCollection = DataUtilities.collection(featureList);
		return featureCollection;
	}

	/**
	 * Generate feature type based on header information of CSV file
	 * Assumptions:
	 *  * header row available
	 *  * header contains X and Y
	 * @param header String array with header description
	 * @param schemaName Name of shapefile
	 * @return SimpleFeature
	 * @throws SchemaException
	 */
	private SimpleFeatureType createFeatureType(String[] header, String schemaName) throws SchemaException {

		SimpleFeatureType featureTypeDefinition = null;

		StringBuffer headerSb = new StringBuffer();
		boolean containsXY = false;
		for (String part : header) {

			if (part.equalsIgnoreCase(NAME_OF_ORDINATE) || part.equalsIgnoreCase(NAME_OF_ABSCISSA)) {
				containsXY = containsXY || part.equalsIgnoreCase(NAME_OF_ORDINATE)
						|| part.equalsIgnoreCase(NAME_OF_ABSCISSA);
				continue;
			}

			headerSb.append(part.toLowerCase() + ":String");
			headerSb.append(",");
		}

		// Geometry needs to be at the first place
		if (containsXY) {
			headerSb.insert(0, GEOTOOLS_PT_HEADER + ",");
			headerSb = headerSb.deleteCharAt( headerSb.length() - 1);
			featureTypeDefinition = DataUtilities.createType(schemaName, headerSb.toString());
		}
		return featureTypeDefinition;
	}

	/**
	 *
	 * @param importJobId
	 * @param uploadFile
	 * @return
	 * @throws Exception
	 */
	public RESTImportTaskList uploadZipFile(Integer importJobId, MultipartFile uploadFile,
			String fileProjection) throws Exception {
		File file = File.createTempFile("TMP_SHOGUN_UPLOAD_", uploadFile.getOriginalFilename());
		uploadFile.transferTo(file);
		RESTImportTaskList importTaskList = null;

		try {
			importTaskList = this.importer.uploadFile(importJobId, file, fileProjection);
		} finally {
			file.delete();
		}

		return importTaskList;
	}

	/**
	 *
	 * @param restLayer
	 * @param layerName
	 * @param layerDataType
	 * @return
	 */
	public MomoLayer saveLayer(String layerName, String layerDataType) {
		MomoLayer layer = (MomoLayer) applicationContext.getBean("templateTileWMSLayer");

		MomoUser currentUser = momoUserService.getUserBySession();

		TileWmsLayerDataSource source = (TileWmsLayerDataSource) layer.getSource();
		LayerAppearance appearance = layer.getAppearance();

		source.setUrl(this.interceptGeoServerAction);

		String endpoint = this.targetWorkspace + ":" + layerName;
		source.setLayerNames(endpoint);

		appearance.setOpacity(1d);

		layer.setName(layerName);
		layer.setDataType(layerDataType);

		momoLayerService.saveOrUpdate(layer);

		// Set the userpermissions for the newly created layer.
		momoLayerService.addAndSaveUserPermissions(layer, currentUser, Permission.ADMIN);

		// Set the userpermissions for the newly created layer appearance.
		layerAppearanceService.addAndSaveUserPermissions(appearance, currentUser, Permission.ADMIN);

		// Now insert special rules to always modify any OGC requests.
		this.momoInterceptorRuleService.createAllRelevantOgcRules(endpoint, RuleType.MODIFY);

		return layer;
	}

	/**
	 *
	 * @param fileProjection
	 * @param layerType
	 * @param importJobId
	 * @param importTasks
	 * @throws Exception
	 */
	private void createTransformTasks(String fileProjection, String layerType, Integer importJobId, RESTImportTaskList importTasks) throws Exception {

		for (RESTImportTask importTask : importTasks) {
			importTask = this.importer.getRESTImportTask(importJobId, importTask.getId());
			if (importTask.getState().equalsIgnoreCase("NO_CRS")) {
				if (StringUtils.isEmpty(fileProjection)) {
					throw new GeoServerRESTImporterException(
							"Task state is \"NO_CRS\" and no custom projection found.");
				}

				LOG.debug("Try to set CRS definition for import task " + importTask.getId()
						+ " of import job " + importJobId + " to " + fileProjection);

				Integer importTaskId = importTask.getId();
				RESTLayer updateLayer = new RESTLayer();
				updateLayer.setSrs(fileProjection);

				this.importer.updateImportTask(importJobId, importTaskId, updateLayer);
			}

			Integer importTaskId = importTask.getId();

			LOG.debug("Successfully created Task with ID " + importTaskId + " for ImportJob "
					+ importJobId);

			if (layerType.equalsIgnoreCase("raster")) {
				if (StringUtils.isEmpty(fileProjection)) {
					fileProjection = this.importer.getLayer(importJobId, importTaskId).getSrs();
					if (StringUtils.isEmpty(fileProjection)) {
						String errMsg = "Could not determine the projection of the provided dataset, "
								+ "please update the import job "+ importJobId + " with an CRS to use.";
						LOG.debug(errMsg);
						throw new GeoServerRESTImporterException(errMsg);
					}
					if (StringUtils.equalsIgnoreCase(fileProjection, "EPSG:404000")) {
						String errMsg = "No valid CRS could be determined (\"EPSG:404000\"), "
								+ "please update the import job "+ importJobId + " with an CRS to use.";
						LOG.debug(errMsg);
						throw new GeoServerRESTImporterException(errMsg);
					}
				}

				// Calculate image transformation.
				if (this.geoServerImporterPerformGdalWarp) {
					LOG.debug("Perform gdalwarp transform to target SRS during import");

					ArrayList<String> optsGdalWarp = new ArrayList<String>();
					optsGdalWarp.add("-s_srs");
					optsGdalWarp.add(fileProjection);

					optsGdalWarp.add("-t_srs");
					optsGdalWarp.add(this.geoServerDefaultSrs);

					boolean warpTaskSuccess = this.importer.createGdalWarpTask(importJobId, importTaskId, optsGdalWarp);
					if (warpTaskSuccess) {
						LOG.debug("Successfully created the GdalWarpTask.");
					} else {
						LOG.error("Could not create GdalWarpTask.");
					}
				}

				if (this.geoServerImporterPerformGdalAddo) {
					LOG.debug("Perform gdaladdo transform for levels: " + this.geoServerImporterGdalAddoLevels);
					List<String> optsGdalAddo = Arrays.asList(new String[] { "-r", "cubic" });

					String[] levelsStr = StringUtils.split(this.geoServerImporterGdalAddoLevels, ",");
					List<Integer> levelsGdalAddo = new ArrayList<Integer>();
					for (String levelStr : levelsStr) {
						levelsGdalAddo.add(new Integer(levelStr));
					}
					Boolean gdalAddoTaskSuccess = this.importer.createGdalAddOverviewTask(importJobId, importTaskId,
							optsGdalAddo, levelsGdalAddo);
					if (gdalAddoTaskSuccess) {
						LOG.debug("Successfully created the gdalAddoTask.");
					} else {
						LOG.error("Could not create gdalAddoTask.");
					}
				}

			} else if (layerType.equalsIgnoreCase("vector")) {
				LOG.debug("Create ReprojectTransformTask for vector layer");
				Boolean transformTask = this.importer.createReprojectTransformTask(importJobId,
						importTaskId, fileProjection, this.geoServerDefaultSrs);
				if (transformTask) {
					LOG.debug("Successfully created the TransformTask.");
				} else {
					LOG.error("Could not create the TransformTask.");
				}
			}
		}
	}

	/**
	 *
	 * @param layerName
	 * @param layerType
	 * @param importJobId
	 * @param layerConfig
	 * @param request
	 * @param imageId
	 * @param image
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> runJobAndCreateLayer(String layerName, String layerType, Integer importJobId, String layerConfig, HttpServletRequest request, Integer imageId) throws Exception {
		try {
			Map<String, Object> responseMap;

			Boolean respImp = false;
			try {
				HttpUtil.setHttpTimeout(importerHttpTimeout);
				// Run the import job (does not depend on layerType).
				respImp = this.importer.runImportJob(importJobId);
			} finally {
				HttpUtil.resetHttpTimeout();
			}

			if (respImp) {
				LOG.info("Successfully run the Import Job with ID " + importJobId);
			} else {
				LOG.error("Error while running the import job.");
				return ResultSet.error("Error while running the import job.");
			}

			// at this point will persist/return the layer of the first successful import task
			// since addition of multiple layers is not implemented in MM admin yet.
			RESTLayer restLayer = null;
			RESTImportTaskList restImportTasks = this.importer.getRESTImportTasks(importJobId);
			for (RESTImportTask restImportTask : restImportTasks) {
				if (restImportTask.getState().equalsIgnoreCase("COMPLETE")) {
					restLayer = this.importer.getLayer(importJobId, restImportTask.getId());
					break;
				}
				if (restImportTask.getState().equalsIgnoreCase("ERROR")) {
					RESTImportTask importTask = this.importer.getRESTImportTask(importJobId, restImportTask.getId());
					LOG.error("Error while processing task with ID "+ importTask.getId() + ". Error msg:" +
							importTask.getErrorMessage());
					throw new GeoServerRESTImporterException("Could not import dataset. Please contact admin");
				}
			}

			if (restLayer != null) {

				// If we had to override/force the CRS for an uploaded file, we might need to update
				// the CRS in GeoServer.
				if (this.geoServerImporterPerformGdalWarp) {
					updateReferencingForLayer(restLayer);
				}

				String nameToSave = restLayer.getName();
				MomoLayer layer = this.saveLayer(nameToSave, layerType);
				try {
					this.handleSLDandLegendFromJsonConfig(layerConfig, layer, request);
					momoLayerService.saveOrUpdate(layer);
				} catch (Exception e) {
					LOG.error("Could not handle SLD and Legend updates: " + e.getMessage());
				}

				// persist the legend image on the layer, if given
				if (imageId != null) {
					ImageFile img = imageFileDao.findById(imageId);
					if (img != null) {
						// make a usable image url
						String scheme = request.getScheme();
						String serverName = request.getServerName();
						int serverPort = request.getServerPort();
						String path = request.getServletContext().getContextPath();
						String url = scheme + "://" + serverName + ":" + serverPort + path;
						url += "/momoimage/get.action?id=" + imageId;
						layer.setFixLegendUrl(url);
						momoLayerService.saveOrUpdate(layer);
					}
				}

				// removed the following due to incompatibilities in SHOGun with multiple
				// layers having the same name......
//				String uploadedlayerName = this.getLayerNameFromJsonConfig(layerConfig);
//				if (uploadedlayerName != null) {
//					// set the name as given by upload config and return, but dont persist it
//					layer.setName(uploadedlayerName);
//				}
				HashMap<String, Object> returnMap = new HashMap<String, Object>(2);
				returnMap.put("layer", layer);
				returnMap.put("layerConfig", layerConfig);
				responseMap = ResultSet.success(returnMap);
			} else {
				responseMap = ResultSet.error("No layer of imported dataset could be imported.");
			}
			return responseMap;
		} finally {
			try {
				if (layerType.toLowerCase().equals("vector")) {
					this.deleteTemporaryShapeFiles(importJobId);
				}
			} catch (Exception e) {
				LOG.info("Could not delete Layer data on the file system for layer: "
						+ layerName);
			}
		}
	}

	/**
	 *
	 * @param importTasks
	 * @return
	 */
	private RESTImportTaskList checkSrsOfImportTasks(RESTImportTaskList importTasks) {
		RESTImportTaskList tasksWithoutProjection = new RESTImportTaskList();
		for (RESTImportTask importTask : importTasks) {
			if (!importTaskHasCrs(importTask)) {
				tasksWithoutProjection.add(importTask);
				LOG.debug("NO_CRS for importTask " + importTask.getId() + " found.");
			}
		}
		return tasksWithoutProjection;
	}

	/**
	 * TODO: support all error codes:
	 *
	 * PENDING, READY, RUNNING, NO_CRS, NO_BOUNDS, NO_FORMAT, BAD_FORMAT, ERROR, CANCELED, COMPLETE
	 *
	 * @param importTask
	 * @return
	 */
	@SuppressWarnings("static-method")
	private boolean importTaskHasCrs(RESTImportTask importTask) {
		return !importTask.getState().equalsIgnoreCase("NO_CRS");
	}

	/**
	 *
	 * @param restLayer
	 */
	private boolean updateReferencingForLayer(RESTLayer restLayer) {

		LOG.debug("Updating the referencing configuration of layer: " + restLayer.getName());

		boolean success = false;

		it.geosolutions.geoserver.rest.decoder.RESTLayer gsLayer = this.gsReaderDao.getLayer(
				this.targetWorkspace, restLayer.getName());

		if (gsLayer.getTypeString().equalsIgnoreCase("raster")) {
			RESTCoverage gsCoverage = this.gsReaderDao.getCoverage(gsLayer);

			String nativeCrs = gsCoverage.getNativeCRS();
			if (!nativeCrs.equalsIgnoreCase(this.geoServerDefaultSrs)) {
				GSCoverageEncoder coverageEncoder = new GSCoverageEncoder();
				coverageEncoder.setNativeCRS(this.geoServerDefaultSrs);
				coverageEncoder.setSRS(this.geoServerDefaultSrs);
				coverageEncoder.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);

				String coverageStoreName = gsCoverage.getStoreName();
				if (StringUtils.contains(coverageStoreName, this.targetWorkspace)){
					coverageStoreName = StringUtils.replace(coverageStoreName,
							this.targetWorkspace + ":", "");
				}

				success = gsPublisherDao.configureCoverage(coverageEncoder, this.targetWorkspace,
						coverageStoreName, gsCoverage.getName());
			}
		} else if (gsLayer.getTypeString().equalsIgnoreCase("vector")) {
			RESTFeatureType gsFeatureType = this.gsReaderDao.getFeatureType(gsLayer);

			if (!gsFeatureType.getNativeCRS().equalsIgnoreCase(this.geoServerDefaultSrs)) {
				GSFeatureTypeEncoder featureTypeEncoder = new GSFeatureTypeEncoder();
				featureTypeEncoder.setNativeCRS(this.geoServerDefaultSrs);
				featureTypeEncoder.setSRS(this.geoServerDefaultSrs);
				featureTypeEncoder.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);

				String dataStoreName = gsFeatureType.getStoreName();
				if (StringUtils.contains(dataStoreName, this.targetWorkspace)){
					dataStoreName = StringUtils.replace(dataStoreName,
							this.targetWorkspace + ":", "");
				}

				success = gsPublisherDao.configureFeatureType(featureTypeEncoder, this.targetWorkspace,
						dataStoreName, gsFeatureType.getName());
			}
		} else {
			LOG.debug("Unknown layer type given. Could not check if we had to update the CRS.");
		}

		if (success) {
			LOG.debug("Successfully updated the referencing configuration of the layer.");
		} else {
			LOG.error("Could not update the referencing configuration of the layer.");
		}

		return success;
	}

	/**
	 *
	 * Imports the features of a given featureType out of the provided WFS server into
	 * the database and creates a GeoServer and SHOGun layer based on it.
	 *
	 * @param wfsUrl The base URL of the WFS server to fetch the features from,
	 *               e.g. "http://geoserver:8080/geoserver/ows". Required.
	 * @param wfsVersion The WFS version to use, possible values are usually one of
	 *                   1.0.0, 1.1.0 or 2.0.0. If not set, {@link #DEFAULT_WFS_VERSION} will be used.
	 * @param featureTypeName The name of the featureType to fetch and import,
	 *                        e.g. "GDA_Wasser:OG_MESSSTELLEN_NETZ_BESCHRIFTUNG". Required.
	 * @param targetEpsg The EPSG to be used for the imported features, e.g. "EPSG:3857".
	 *                   If not set, {@link #geoServerDefaultSrs} will be used.
	 * @return The created SHOGun layer.
	 * @throws GeoServerRESTImporterException
	 */
	public MomoLayer importWfsAndCreateLayer(String wfsUrl, String wfsVersion,
			String featureTypeName, String targetEpsg) throws GeoServerRESTImporterException {

		WFSDataStore wfsDataStore = null;

		if (StringUtils.isEmpty(wfsVersion)) {
			LOG.debug("No WFS version given, will use the default version " + DEFAULT_WFS_VERSION);
			wfsVersion = DEFAULT_WFS_VERSION;
		}

		if (StringUtils.isEmpty(targetEpsg)) {
			LOG.debug("No targetEpsg given, it will be set to the default one: " +
					this.geoServerDefaultSrs);
			targetEpsg = this.geoServerDefaultSrs;
		}

		try {
			// 1. Create a WFS datastore that contains the given connection params.
			wfsDataStore = this.createWfsDataStore(wfsUrl, wfsVersion);
			// 2. Fetch the features from the given featureType using the store created above.
			SimpleFeatureCollection featureCollection = this.getFeatureCollectionFromDataStore(
					wfsDataStore, featureTypeName, targetEpsg);
			// 3. Persist the featureCollection in the database.
			String tableName = this.importFeatureCollectionToDatabase(featureCollection);
			// 4. Publish the layer based on the DB table in GeoServer.
			this.publishGeoServerLayerFromDbTable(tableName, featureTypeName, targetEpsg);
			// 5. Persist the layer entitiy in SHOGun DB.
			MomoLayer layer = this.saveLayer(tableName.toUpperCase(), "vector");

			return layer;
		} finally {
			if (wfsDataStore != null) {
				wfsDataStore.dispose();
			}
		}
	}

	/**
	 * Creates a {@link #WFSDataStore} based on a WFS base URL and a version. The maximum
	 * number of features to be handled/fetched by this store is limited by {@link WFS_MAX_FEATURES}.
	 *
	 * @param wfsUrl The base URL of the WFS server to fetch the features from.
	 * @param wfsVersion The WFS version to use.
	 * @return The created {@link #WFSDataStore}.
	 * @throws GeoServerRESTImporterException
	 */
	private WFSDataStore createWfsDataStore(String wfsUrl, String wfsVersion)
			throws GeoServerRESTImporterException {

		WFSDataStore wfsDataStore = null;

		try {
			WFSDataStoreFactory wfsFactory = new WFSDataStoreFactory();
			Map<String, Serializable> wfsDataStoreParams = new HashMap<String, Serializable>();

			List<BasicNameValuePair> wfsGetCapabilitiesQueryParams = new ArrayList<BasicNameValuePair>();
			wfsGetCapabilitiesQueryParams.add(new BasicNameValuePair(
					"SERVICE", OgcEnum.ServiceType.WFS.toString()));
			wfsGetCapabilitiesQueryParams.add(new BasicNameValuePair(
					"REQUEST", OgcEnum.OperationType.GET_CAPABILITIES.toString()));
			wfsGetCapabilitiesQueryParams.add(new BasicNameValuePair(
					"VERSION", wfsVersion));
			String wfsGetCapabilitiesQueryString = URLEncodedUtils.format(
					wfsGetCapabilitiesQueryParams, "UTF-8");
			String wfsGetCapabilitiesUrl = wfsUrl + "?" + wfsGetCapabilitiesQueryString;
			wfsDataStoreParams.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", wfsGetCapabilitiesUrl);
			wfsDataStoreParams.put("WFSDataStoreFactory:MAXFEATURES", WFS_MAX_FEATURES);

			LOG.debug("Creating a WFS dataStore based on the following WFS GetCapabilities: " +
					wfsGetCapabilitiesUrl);

			wfsDataStore = wfsFactory.createDataStore(wfsDataStoreParams);

			LOG.debug("Successfully created the WFS dataStore.");
		} catch (IOException e) {
			String errMsg = "Error while creating the WFS dataStore";
			LOG.error(errMsg + ": ", e);
			throw new GeoServerRESTImporterException(errMsg + ".");
		}

		return wfsDataStore;
	}

	/**
	 * Fetches the features from the given featureType of the {@link #DataStore} in the
	 * provided EPSG.
	 *
	 * @param dataStore The {@link #DataStore} to fetch the features from.
	 * @param featureTypeName The featureType to fetch.
	 * @param targetEpsg The EPSG of the returning collection.
	 * @return The {@link #SimpleFeatureCollection} including the features.
	 * @throws GeoServerRESTImporterException
	 */
	private SimpleFeatureCollection getFeatureCollectionFromDataStore(DataStore dataStore,
			String featureTypeName, String targetEpsg) throws GeoServerRESTImporterException {

		SimpleFeatureCollection featureCollection = null;
		ContentFeatureSource source = null;
		try {
			source = (ContentFeatureSource) dataStore.getFeatureSource(featureTypeName);
			featureCollection = source.getFeatures();

			LOG.debug("Successfully fetched " + featureCollection.size() + " features " +
					"from featureType " + featureTypeName);
		} catch (IOException e) {
			String errMsg = "Could not fetch the features from the provided dataStore";
			LOG.error(errMsg + ": ", e);
			throw new GeoServerRESTImporterException(errMsg + ".");
		}

		// Define the target CRS.
		CoordinateReferenceSystem targetCrs;
		try {
			targetCrs = CRS.decode(targetEpsg);
		} catch (FactoryException e) {
			String errMsg = "Could not decode the targetEpsg";
			LOG.error(errMsg + ": ", e);
			throw new GeoServerRESTImporterException(errMsg + ".");
		}

		// Transform the features to target CRS if necessary.
		CoordinateReferenceSystem sourceCrs = featureCollection.getSchema()
				.getGeometryDescriptor().getCoordinateReferenceSystem();
		if (!sourceCrs.equals(targetCrs)) {
			try {
				featureCollection = new ReprojectFeatureResults(
						featureCollection,
						targetCrs
				);
			} catch (NoSuchElementException | IOException | SchemaException |
					TransformException | FactoryException e) {
				String errMsg = "Could not reproject the feature collection";
				LOG.error(errMsg + ": ", e);
				throw new GeoServerRESTImporterException(errMsg + ".");
			}
		}

		return featureCollection;
	}

	/**
	 * Imports a {@link #SimpleFeatureCollection} into the designated import database.
	 * The connection parameters for the connection will be acquired from the
	 * {@link #targetDatastore} in the GeoServer. Note: These parameters may be overridden
	 * by {@link #targetDatastoreConnParams}!
	 *
	 * @param featureCollection The featureCollection to import.
	 * @return The name of the new table the features where imported in.
	 * @throws GeoServerRESTImporterException
	 */
	private String importFeatureCollectionToDatabase(SimpleFeatureCollection featureCollection)
			throws GeoServerRESTImporterException {

		// Read old feature schema.
		SimpleFeatureType schemaOrig = featureCollection.getSchema();
		String origSchemaName = schemaOrig.getTypeName();
		if (origSchemaName.contains(":")) {
			origSchemaName = origSchemaName.replace(":", "_");
		}

		// Create DB schema / table definition with the following table name.
		String schemaNameDB = WFS_IMPORT_TABLE_PREFIX + "_" + System.currentTimeMillis() + "_" + origSchemaName;
		if (!TableNameValidator.isValidName(schemaNameDB)) {
			schemaNameDB = TableNameValidator.createValidTableName(schemaNameDB);
		}
		schemaNameDB = schemaNameDB.toUpperCase();

		VectorFeatureTypeTransformer featureTypeTransform = new VectorFeatureTypeTransformer(
				schemaOrig, schemaNameDB, false);

		RESTDataStore restDataStore = this.gsReaderDao.getDatastore(this.targetWorkspace,
				this.targetDatastore);
		Map<String, String> dbParameters = restDataStore.getConnectionParameters();
		dbParameters.putAll(this.targetDatastoreConnParams);

		String errMsg = "Could not write the features to database";
		JDBCDataStore jdbcDatastore = null;
		try {
			// Create JDBC datastore out of the given connection params.
			jdbcDatastore = (JDBCDataStore) DataStoreFinder.getDataStore(dbParameters);

			if(jdbcDatastore == null) {
				final String message = "Could not create jdbc datastore based on given dbParameters for WFS import. "
						+ "Are you sure that you have all necessary dependencies "
						+ "(like gt-jdbc-oracle or gt-jdbc-postgis)?";
				LOG.error(message);
				throw new Exception(message);
			}

			// Create DB schema.
			featureTypeTransform.createSchema();
			// Apply new schema to features.
			List<SimpleFeature> updatedFeatures = featureTypeTransform.createFeatureList(featureCollection);
			// Write features to database.
			boolean res = featureTypeTransform.toDataStore(jdbcDatastore, updatedFeatures);
			if (res) {
				LOG.info("Succesfully wrote the features to database in table " + schemaNameDB);
			} else {
				throw new GeoServerRESTImporterException(errMsg + ".");
			}
		} catch (Exception e) {
			LOG.error(errMsg + ": ", e);
			throw new GeoServerRESTImporterException(errMsg + ".");
		} finally {
			if (jdbcDatastore != null) {
				jdbcDatastore.dispose();
			}
		}

		return schemaNameDB;
	}

	/**
	 *
	 * @param tableName
	 * @param layerTitle
	 */
	private boolean publishGeoServerLayerFromDbTable(String tableName, String layerTitle,
			String targetEpsg) {
		GSFeatureTypeEncoder gsfte = new GSFeatureTypeEncoder();
		gsfte.setName(tableName.toUpperCase());
		gsfte.setTitle(layerTitle);
		gsfte.setNativeCRS(targetEpsg);
		gsfte.setSRS(targetEpsg);
		gsfte.setProjectionPolicy(ProjectionPolicy.NONE);

		GSLayerEncoder layerEncoder = new GSLayerEncoder();
		layerEncoder.setEnabled(true);

		boolean success = this.gsPublisherDao.publishDBLayer(this.targetWorkspace,
				this.targetDatastore, gsfte, layerEncoder);

		return success;
	}

	/**
	 *
	 * @param layerName
	 * @param layerType
	 * @param importJobId
	 * @param taskId
	 * @param fileProjection
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateCrsForImport(String layerName, String layerType,
			Integer importJobId, Integer taskId, String fileProjection, String layerConfig, HttpServletRequest request, Integer imageId)
			throws Exception {
		RESTLayer updateLayer = new RESTLayer();
		updateLayer.setSrs(fileProjection);

		this.importer.updateImportTask(importJobId, taskId, updateLayer);

		RESTImportTaskList importTaskList = this.importer.getRESTImportTasks(importJobId);
		createTransformTasks(fileProjection, layerType, importJobId, importTaskList);
		return this.runJobAndCreateLayer(layerName, layerType, importJobId, layerConfig, request, imageId);
	}

	/**
	 *
	 * @param importJobId
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public Map<String, Object> deleteImportJob(Integer importJobId)
			throws URISyntaxException, HttpException {
		Map<String, Object> responseMap;

		if (this.importer.deleteImportJob(importJobId)) {
			responseMap = ResultSet.success("Deleted ImportJob " + importJobId);
		} else {
			responseMap = ResultSet.error("Could not delete ImportJob " + importJobId);
		}
		return responseMap;
	}

	/**
	 * @param importJobId
	 * @throws Exception
	 *
	 */
	public void deleteTemporaryShapeFiles(Integer importJobId) throws Exception {
		RESTData data = this.importer.getDataOfImportTask(importJobId, 0);
		String restUrl = this.geoServerBaseUrl;
		restUrl += "/rest/resource/";

		// something like /var/lib/tomcat7/webapps/geoserver/data/uploads/tmp638865446314869143
		String fileUrl = data.getLocation();

		fileUrl = fileUrl.substring(fileUrl.lastIndexOf("uploads/"));
		restUrl += fileUrl;

		Response layerDeleted = HttpUtil.delete(restUrl, this.gsuser, this.gspassword);
		if (layerDeleted.getStatusCode().is2xxSuccessful()) {
			LOG.info("Successfully deleted Layer data on the file system: " + fileUrl);
		} else {
			throw new Exception("Could not delete Layer data on the file system: " + fileUrl);
		}
	}

	/**
	 * @return the gsReaderDao
	 */
	public GeoserverReaderDao getGsReaderDao() {
		return gsReaderDao;
	}

	/**
	 * @param gsReaderDao the gsReaderDao to set
	 */
	public void setGsReaderDao(GeoserverReaderDao gsReaderDao) {
		this.gsReaderDao = gsReaderDao;
	}

	/**
	 * @return the gsPublisherDao
	 */
	public GeoserverPublisherDao getGsPublisherDao() {
		return gsPublisherDao;
	}

	/**
	 * @param gsPublisherDao the gsPublisherDao to set
	 */
	public void setGsPublisherDao(GeoserverPublisherDao gsPublisherDao) {
		this.gsPublisherDao = gsPublisherDao;
	}

}