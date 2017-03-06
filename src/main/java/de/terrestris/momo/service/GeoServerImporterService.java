package de.terrestris.momo.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jcraft.jsch.JSchException;

import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.util.importer.ImporterException;
import de.terrestris.momo.util.importer.RESTImporterPublisher;
import de.terrestris.momo.util.importer.communication.RESTImport;
import de.terrestris.momo.util.importer.communication.RESTImportTask;
import de.terrestris.momo.util.importer.communication.RESTImportTaskList;
import de.terrestris.momo.util.importer.communication.RESTLayer;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.layer.appearance.LayerAppearance;
import de.terrestris.shogun2.model.layer.source.TileWmsLayerDataSource;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.data.ResultSet;
import de.terrestris.shogun2.util.enumeration.InterceptorEnum.RuleType;

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
	 *
	 */
	@Autowired
	@Qualifier("geoServerRESTImporterPublisher")
	private RESTImporterPublisher publisher;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerBaseUrl")
	private String geoServerBaseUrl;

	/**
	 * The workspace of newly created layers.
	 */
	@Autowired
	@Qualifier("geoServerDefaultWorkspace")
	private String geoServerWorkspace;

	/**
	 * The URL/path to `geoserver.action`.
	 */
	@Autowired
	@Qualifier("publicInterceptGeoServerAction")
	private String interceptGeoServerAction;

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
	@Qualifier("geoServerDefaultSrs")
	private String geoServerDefaultSrs;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerDefaultWorkspace")
	private String geoServerDefaultWorkspace;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerDefaultDatastore")
	private String geoServerDefaultDatastore;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerImporterPerformGdalAddo")
	private Boolean geoServerImporterPerformGdalAddo;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerImporterGdalAddoLevels")
	private String geoServerImporterGdalAddoLevels;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerImporterPerformGdalWarp")
	private Boolean geoServerImporterPerformGdalWarp;

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
	private MomoLayerService<MomoLayer, MomoLayerDao<MomoLayer>> layerService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("userService")
	private UserService<User, UserDao<User>> userService;

	/**
	 * The ssh service
	 */
	@Autowired
	@Qualifier("sshService")
	SshService sshService;

//	/**
//	 * The SQL datasource of the geoserver vector data
//	 */
//	@Autowired
//	@Qualifier("geoServerDataSource")
//	private DataSource geoServerDataSource;

//	/**
//	 * The reader dao
//	 */
//	@Autowired
//	private GeoserverReaderDao gsReaderDao;

	/**
	 *
	 * @param workSpaceName
	 * @param dataStoreName
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws ImporterException
	 */
	public RESTImport createImportJob(String workSpaceName, String dataStoreName) throws JsonParseException,
			JsonMappingException, URISyntaxException, HttpException, IOException, ImporterException {

		return publisher.createImport(workSpaceName, dataStoreName);
	}

	/**
	 *
	 * @param workSpaceName
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws ImporterException
	 */
	public RESTImport createImportJob(String workSpaceName) throws JsonParseException, JsonMappingException,
			URISyntaxException, HttpException, IOException, ImporterException {

		return publisher.createImport(workSpaceName);
	}

	/**
	 *
	 * @param importJobId
	 * @param taskId
	 * @param targetSrs
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
//	private boolean createReprojectTransformTask(Integer importJobId, Integer taskId, String sourceSrs)
//			throws URISyntaxException, HttpException {
//		return this.publisher.createReprojectTransformTask(importJobId, taskId, sourceSrs);
//	}

	/**
	 *
	 * @param importJobId
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public Boolean runImportJob(Integer importJobId)
			throws UnsupportedEncodingException, URISyntaxException, HttpException {

		return publisher.runImport(importJobId);
	}

	/**
	 *
	 * @param importJobId
	 * @param taskId
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws ImporterException
	 */
	public List<RESTLayer> getRESTLayers(Integer importJobId, List<RESTImportTask> importTasks)
			throws JsonParseException, JsonMappingException, URISyntaxException, HttpException, IOException, ImporterException {
		return publisher.getAllImportedLayers(importJobId, importTasks);
	}

	/**
	 *
	 * @param importJobId
	 *            ID of import job
	 * @param taskId
	 *            ID of task
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 */
	public RESTLayer getRESTLayer(Integer importJobId, Integer taskId)
			throws JsonParseException, JsonMappingException, URISyntaxException, HttpException, IOException {

		return publisher.getLayer(importJobId, taskId);
	}

	/**
	 *
	 * @param importJobId
	 * @param uploadFile
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws ImporterException
	 */
	public RESTImportTaskList uploadZipFile(Integer importJobId, MultipartFile uploadFile)
			throws IllegalStateException, IOException, URISyntaxException, HttpException, ImporterException {

		//TODO use Tempfile instead of File (e.g. for windows)
		File file = new File("/tmp/" + uploadFile.getOriginalFilename());
		uploadFile.transferTo(file);

		RESTImportTaskList importTaskList = this.publisher.uploadFile(importJobId, file);

		file.delete();
		return importTaskList;
	}

	/**
	 *
	 * @param restLayer
	 * @param layerHoverTemplate
	 * @param layerOpacity
	 * @param layerDescription
	 * @param layerName
	 * @param importJobId
	 * @param diskUsage
	 * @return
	 */
	public MomoLayer saveLayer(RESTLayer restLayer, String layerName,
			String layerDescription, Double layerOpacity,
			String layerHoverTemplate, String layerDataType, Integer importJobId) {

		MomoLayer layer = (MomoLayer)
				applicationContext.getBean("templateTileWMSLayer");

		User currentUser = userService.getUserBySession();

		TileWmsLayerDataSource source = (TileWmsLayerDataSource) layer.getSource();
		LayerAppearance appearance = layer.getAppearance();

		source.setUrl(this.interceptGeoServerAction);

		String endpoint = this.geoServerWorkspace + ":" + restLayer.getName();
		source.setLayerNames(endpoint);

		//appearance.setName(restLayer.getTitle());
		appearance.setOpacity(layerOpacity);

		layer.setName(layerName);
		layer.setSpatiallyRestricted(true);
		layer.setDataType(layerDataType);
		layer.setHoverable(true);

		layer.setOwner(currentUser);

		layerService.saveOrUpdate(layer);
		layerService.addAndSaveUserPermissions(layer, currentUser, Permission.ADMIN);

		// Now insert special rules to always modify any OGC requests:
		this.momoInterceptorRuleService.createAllRelevantOgcRules(endpoint, RuleType.MODIFY);

		// remove temporary unzipped shape
//		if("Vector".equalsIgnoreCase(layerDataType)) {
//			try {
//				deleteTemporaryShapeFiles(layer, importJobId);
//			} catch (Exception e) {
//				LOG.error("Could not delete temp unzipped shape: " + e.getMessage());
//			}
//		}

		// We cannot delete import jobs currently as the REST API for
		// imports does not support (yet) the HTTP DELETE method
//		try {
//			publisher.deleteImportJob(importJobId);
//			LOG.debug("Deleted import job " + importJobId);
//		} catch (URISyntaxException | HttpException e) {
//			LOG.error("Could not delete import job with id: " +
//					importJobId + ":" + e.getMessage());
//		}

		return layer;
	}

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
	 * @throws ImporterException
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws JSchException
	 * @throws Exception
	 */
	public Map<String, Object> importGeodataAndCreateLayer(MultipartFile file, String fileProjection, String layerName,
			String layerType, String layerDescription, double layerOpacity, String layerHoverTemplate) throws JsonParseException, JsonMappingException, URISyntaxException, HttpException, IOException, ImporterException, JSchException {

		Map<String, Object> responseMap = new HashMap<String, Object>();

		RESTImport resp = null;
		if (layerType.equalsIgnoreCase("raster")) {
			LOG.debug("Create import job for raster data (target workspace WITHOUT target datastore)");
			resp = this.createImportJob(this.geoServerDefaultWorkspace);
		} else {
			LOG.debug("Create import job for vector data (target workspace and target datastore)");
			resp = this.createImportJob(this.geoServerDefaultWorkspace, this.geoServerDefaultDatastore);
		}

		Integer importJobId = resp.getId();
		LOG.info("Successfully created the ImportJob with ID " + importJobId);

		RESTImportTaskList importTasks = null;
		try {
			LOG.debug("Upload file " + file.getName() + " to ImportJob " + importJobId);
			importTasks = this.uploadZipFile(importJobId, file);
		} catch (ImporterException ie) {
			LOG.debug("Uploading file content to ImportJob " + importJobId + " throwed an exception.", ie);
			throw ie;
		}

		if (importTasks == null) {
			throw new ImporterException("Import task could not be created.");
		}

		RESTImportTaskList tasksWithoutProjection = this.checkSrsOfImportTasks(importTasks);

		// Redefine broken tasks
		if(tasksWithoutProjection.size() > 0 && StringUtils.isEmpty(fileProjection)){
			responseMap = ResultSet.error("NO_CRS detected and no fileProjection given.");
			responseMap.put("importJobId", importJobId);
			responseMap.put("tasksWithoutProjection", tasksWithoutProjection);
			responseMap.put("error", "NO_CRS");
			return responseMap;
		}

		return processTasks(fileProjection, layerName, layerType,
				layerDescription, layerOpacity, layerHoverTemplate,
				importJobId, importTasks);
	}

	/**
	 * @param fileProjection
	 * @param layerName
	 * @param layerType
	 * @param layerDescription
	 * @param layerOpacity
	 * @param layerHoverTemplate
	 * @param importJobId
	 * @param importTasks
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws ImporterException
	 * @throws JSchException
	 * @throws Exception
	 */
	private Map<String, Object> processTasks(String fileProjection,
			String layerName, String layerType, String layerDescription,
			double layerOpacity, String layerHoverTemplate,
			Integer importJobId, RESTImportTaskList importTasks) throws JsonParseException, JsonMappingException, URISyntaxException, HttpException, IOException, ImporterException, JSchException {
		Map<String, Object> responseMap;

		for (RESTImportTask importTask : importTasks) {
			importTask = this.publisher.getRESTImportTask(importJobId, importTask.getId());
			if (importTask.getState().equalsIgnoreCase("NO_CRS")) {

				if(fileProjection.equals("")){
					throw new ImporterException("Task state is \"NO_CRS\" and no custom projection found.");
				}

				LOG.debug("Try to set CRS definition for import task " + importTask.getId() + " of import job "
						+ importJobId + " to " + fileProjection);
				this.publisher.updateSrsForRESTImportTask(importJobId, importTask, fileProjection);
			}

			Integer importTaskId = importTask.getId();
			LOG.info("Successfully created Task with ID " + importTaskId + " for ImportJob " + importJobId);

			// skip transformation as this would reproject everything to 32648, which should get
			// displayed on the clientside in 3857, which makes no sense for layers that are not
			// spatially contained in mongolia
//			createTransformTask(fileProjection, layerType, importJobId,
//					importTaskId);
		}

		responseMap = runJobAndCreateLayer(layerName, layerType,
				layerDescription, layerOpacity, layerHoverTemplate, importJobId);

		return responseMap;
	}

	/**
	 * @param layerName
	 * @param layerType
	 * @param layerDescription
	 * @param layerOpacity
	 * @param layerHoverTemplate
	 * @param importJobId
	 * @return
	 * @throws ImporterException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws JSchException
	 * @throws Exception
	 */
	private Map<String, Object> runJobAndCreateLayer(String layerName,
			String layerType, String layerDescription, double layerOpacity,
			String layerHoverTemplate, Integer importJobId) throws ImporterException, URISyntaxException, HttpException, JsonParseException, JsonMappingException, IOException, JSchException {
		Map<String, Object> responseMap;
		// start import job (does not depend on layerType)
		Boolean respImp = this.runImportJob(importJobId);
		LOG.info("Successfully run the Import Job with ID " + importJobId);
		if (!respImp) {
			LOG.error("Could not create layer.");
		}

		// at this point will persist/return the layer of the first successful import task
		// since addition of multiple layers is not implemented in MM admin yet.
		RESTLayer restLayer = null;
		RESTImportTaskList restTasks = this.getRESTTasks(importJobId);
		for (RESTImportTask task : restTasks) {

			if (task.getState().equalsIgnoreCase("COMPLETE")){
				restLayer = this.getRESTLayer(importJobId, task.getId());
				break;
			}
		}

		if (restLayer != null) {
			MomoLayer layer = this.saveLayer(restLayer, layerName, layerDescription, layerOpacity, layerHoverTemplate, layerType, importJobId);

			layerService.saveOrUpdate(layer);

			responseMap = ResultSet.success(layer);
		} else {
			responseMap = ResultSet.error("No layer of imported dataset could be imported.");
		}
		return responseMap;
	}

	/**
	 * @param fileProjection
	 * @param layerType
	 * @param importJobId
	 * @param importTaskId
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
//	private void createTransformTask(String fileProjection, String layerType,
//			Integer importJobId, Integer importTaskId)
//			throws URISyntaxException, HttpException, JsonParseException, JsonMappingException, IOException {
//		if (layerType.equalsIgnoreCase("raster")) {
//
//			Boolean doTransformation = true;
//
//			if(StringUtils.isEmpty(fileProjection)){
//				fileProjection = this.publisher.getLayer(importJobId, importTaskId).getSrs();
//				if(StringUtils.isEmpty(fileProjection)){
//					LOG.info("Can't add transformation to importTask " + importTaskId +
//							" as source srs and no custom fileProjection is given.");
//					doTransformation = false;
//				}
//			}
//
//			// calculate image transformation
//			if (this.geoServerImporterPerformGdalWarp && doTransformation) {
//				LOG.info("Perform gdalwarp transform to target SRS during import");
//				ArrayList<String> optsGdalWarp = new ArrayList<String>();
//				optsGdalWarp.add("-t_srs");
//				optsGdalWarp.add(this.geoServerDefaultSrs);
//
//				optsGdalWarp.add("-s_srs");
//				optsGdalWarp.add(fileProjection);
//
//				if (this.createGdalWarpTask(importJobId, importTaskId, optsGdalWarp)) {
//					LOG.info("Successfully created the gdal_warp task");
//				} else {
//					LOG.error("Could not create gdal_warp task.");
//				}
//			}
//
//			if (this.geoServerImporterPerformGdalAddo) {
//				LOG.info("Perform gdaladdo transform for levels: " + this.geoServerImporterGdalAddoLevels);
//				List<String> optsGdalAddo = Arrays.asList(new String[] { "-r", "cubic" });
//
//				String[] levelsStr = StringUtils.split(this.geoServerImporterGdalAddoLevels, ",");
//				List<Integer> levelsGdalAddo = new ArrayList<Integer>();
//				for (String levelStr : levelsStr) {
//					levelsGdalAddo.add(new Integer(levelStr));
//				}
//				Boolean gdalAddoTaskSuccess = this.createGdalAddOverviewTask(importJobId, importTaskId,
//						optsGdalAddo, levelsGdalAddo);
//				if (!gdalAddoTaskSuccess) {
//					LOG.error("Could not create gdalAddoTask task.");
//				}
//				LOG.info("Successfully created the gdalAddoTasks");
//			}
//
//		} else if (layerType.equalsIgnoreCase("vector")) {
//			LOG.info("Create ReprojectTransformTask for vector layer");
//			Boolean transformTask = this.createReprojectTransformTask(importJobId, importTaskId, fileProjection);
//			if (!transformTask) {
//				LOG.error("Could not create transform task.");
//			}
//			LOG.info("Successfully created the TransformTask");
//		}
//	}


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

	private boolean importTaskHasCrs(RESTImportTask importTask){
		return !importTask.getState().equalsIgnoreCase("NO_CRS");
	}

	/**
	 * Return all import tasks for given import job
	 * @param importJobId ID of import job
	 * @return instance of {@link RESTImportTaskList} containing all import tasks
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	private RESTImportTaskList getRESTTasks(Integer importJobId) throws JsonParseException, JsonMappingException, URISyntaxException, HttpException, IOException {
		return this.publisher.getRESTImportTasks(importJobId);
	}

	/**
	 *
	 * @param importJobId
	 * @param importTaskId
	 * @param optsGdalWarp
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public boolean createGdalWarpTask(Integer importJobId, Integer importTaskId, List<String> optsGdalWarp)
			throws URISyntaxException, HttpException {
		return this.publisher.createGdalWarpTask(importJobId, importTaskId, optsGdalWarp);
	}

	/**
	 *
	 * @param importJobId
	 * @param importTaskId
	 * @param opts
	 * @param levels
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public boolean createGdalAddOverviewTask(Integer importJobId, Integer importTaskId, List<String> opts,
			List<Integer> levels) throws URISyntaxException, HttpException {
		return this.publisher.createGdalAddOverviewTask(importJobId, importTaskId, opts, levels);
	}

	/**
	 *
	 * @param importJobId
	 * @param importTaskId
	 * @param opts
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public boolean createGdalTranslateTask(Integer importJobId, Integer importTaskId, List<String> opts)
			throws URISyntaxException, HttpException {
		return this.publisher.createGdalTranslateTask(importJobId, importTaskId, opts);
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
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws JSchException
	 * @throws ImporterException
	 * @throws Exception
	 */
	public Map<String, Object> updateCrsForImport(String layerName,
			String layerType, String layerDescription, double layerOpacity,
			String layerHoverTemplate, Integer importJobId, Integer taskId,
			String fileProjection) throws JsonParseException, JsonMappingException, URISyntaxException, HttpException, IOException, ImporterException, JSchException {
		RESTImportTask importTask = this.publisher.getRESTImportTask(importJobId, taskId);
		this.publisher.updateSrsForRESTImportTask(importJobId, importTask, fileProjection);
		// skip transformation as this would reproject everything to 32648, which should get
		// displayed on the clientside in 3857, which makes no sense for layers that are not
		// spatially contained in mongolia
//		createTransformTask(fileProjection, layerType, importJobId, importTask.getId());
		return this.runJobAndCreateLayer(layerName, layerType, layerDescription, layerOpacity, layerHoverTemplate, importJobId);
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

		if (this.publisher.deleteImportJob(importJobId)) {
			responseMap = ResultSet.success("Deleted ImportJob " + importJobId);
		} else {
			responseMap = ResultSet.error("Could not delete ImportJob " + importJobId);
		}
		return responseMap;
	}

//	/**
//	 * @param layer
//	 * @param importJobId
//	 * @throws Exception
//	 *
//	 */
//	private void deleteTemporaryShapeFiles(MomoLayer layer, Integer importJobId) throws Exception {
//
//		RESTData data = publisher.getDataOfImportTask(importJobId, 0);
//
//		String fileUrl = data.getLocation(); // something like /var/lib/tomcat7/webapps/geoserver/data/uploads/tmp638865446314869143
//		String command = "sudo rm -R " + fileUrl + ";";
//		try {
//			Map<String, Object> result = sshService.execCommand(command);
//			if (result.get("success").equals(false)) {
//				throw new RuntimeException(result.get("message").toString());
//			} else {
//				LOG.debug("Successfully deleted the temporary vector shapefile " +
//						"on the geoserver machine in " + fileUrl);
//			}
//		} catch (JSchException | IOException e1) {
//			throw new RuntimeException(e1.getMessage());
//		}
//
//	}

}
