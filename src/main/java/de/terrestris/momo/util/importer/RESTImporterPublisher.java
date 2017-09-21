package de.terrestris.momo.util.importer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.terrestris.momo.util.importer.communication.AbstractRESTEntity;
import de.terrestris.momo.util.importer.communication.RESTData;
import de.terrestris.momo.util.importer.communication.RESTImport;
import de.terrestris.momo.util.importer.communication.RESTImportTask;
import de.terrestris.momo.util.importer.communication.RESTImportTaskList;
import de.terrestris.momo.util.importer.communication.RESTLayer;
import de.terrestris.momo.util.importer.communication.RESTTargetDataStore;
import de.terrestris.momo.util.importer.communication.RESTTargetWorkspace;
import de.terrestris.momo.util.importer.transform.RESTGdalAddoTransform;
import de.terrestris.momo.util.importer.transform.RESTGdalTranslateTransform;
import de.terrestris.momo.util.importer.transform.RESTGdalWarpTransform;
import de.terrestris.momo.util.importer.transform.RESTReprojectTransform;
import de.terrestris.momo.util.importer.transform.RESTTransform;
import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.model.Response;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
@Component
public class RESTImporterPublisher {

	/**
	 * The Logger.
	 */
	private final static Logger LOG = Logger.getLogger(RESTImporterPublisher.class);

	/**
	 *
	 */
	private String username;

	/**
	 *
	 */
	private String password;

	/**
	 *
	 */
	private String defaultSRS;

	/**
	 *
	 */
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 *
	 */
	private URI baseUri;

	/**
	 *
	 */
	private static final ContentType APPLICATION_JSON = ContentType.create("application/json");


	public RESTImporterPublisher() {

	}

	/***
	 * 
	 * @param importerBaseURL
	 * @param defaultSRS
	 * @param username
	 * @param password
	 * @throws URISyntaxException
	 */
	public RESTImporterPublisher(String importerBaseURL, String defaultSRS, String username,
			String password) throws URISyntaxException {

		if (StringUtils.isEmpty(importerBaseURL) || StringUtils.isEmpty(defaultSRS) ||
				StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			LOG.error("Missing Constructor arguments. Could not create " +
				"the RESTImporterPublisher.");
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.setSerializationInclusion(Include.NON_NULL);

		this.username = username;
		this.password = password;
		this.defaultSRS = defaultSRS;

		this.mapper = mapper;
		this.baseUri = new URI(importerBaseURL);
	}

	/**
	 *
	 * @param importJob
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws ImporterException
	 */
	public RESTImport createImport(String workSpaceName, String dataStoreName)
			throws URISyntaxException, HttpException, JsonParseException,
			JsonMappingException, IOException, ImporterException {

		RESTImport importJob = new RESTImport();

		RESTTargetWorkspace targetWorkspace = new RESTTargetWorkspace(workSpaceName);
		RESTTargetDataStore targetDataStore = new RESTTargetDataStore(dataStoreName, null);

		importJob.setTargetWorkspace(targetWorkspace);
		importJob.setTargetStore(targetDataStore);

		Response httpResponse = HttpUtil.post(
				this.addEndPoint(""),
				this.asJSON(importJob),
				APPLICATION_JSON,
				this.username,
				this.password
		);

		RESTImport importResult = null;

		try {
			importResult = (RESTImport) this.asEntity(httpResponse.getBody(), RESTImport.class);
		} catch (Exception e) {
			if (httpResponse.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
				String msg = "Import job cannot be created, maybe the importer extension of GeoServer is not installed.";
				LOG.debug(msg, e);
				throw new ImporterException(msg, e);
			}
		}

		return importResult;
	}

	/**
	 * Creates an import job without target datastore (e.g. used for raster layers)
	 *
	 * @param workSpaceName
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws ImporterException
	 */
	public RESTImport createImport(String workSpaceName) throws URISyntaxException, HttpException, JsonParseException, IOException, ImporterException {
		RESTImport importJob = new RESTImport();

		RESTTargetWorkspace targetWorkspace = new RESTTargetWorkspace(workSpaceName);
		importJob.setTargetWorkspace(targetWorkspace);

		Response httpResponse = HttpUtil.post(
				this.addEndPoint(""),
				this.asJSON(importJob),
				APPLICATION_JSON,
				this.username,
				this.password
		);

		if (httpResponse == null) {
			throw new ImporterException("Could not create import job in GeoServer.");
		}

		RESTImport importResult = null;
		try {
			importResult = (RESTImport) this.asEntity(httpResponse.getBody(), RESTImport.class);
		} catch (Exception e) {
			if (httpResponse.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
				String msg = "Import job cannot be created, maybe the importer extension of GeoServer is not installed.";
				LOG.debug(msg, e);
				throw new ImporterException(msg, e);
			}
		}

		return importResult;
	}

	/**
	 *
	 * @param importJobId
	 * @param taskId
	 * @param transformTask
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public boolean createReprojectTransformTask(Integer importJobId, Integer taskId,
			String sourceSrs) throws URISyntaxException, HttpException {

		RESTReprojectTransform transformTask = new RESTReprojectTransform();
		if (StringUtils.isNotEmpty(sourceSrs)) {
			transformTask.setSource(sourceSrs);
		}
		transformTask.setTarget(this.defaultSRS);

		return createTransformTask(importJobId, taskId, transformTask);
	}

	/**
	 * Helper method to create an importer transformTask
	 *
	 * @param importJobId
	 * @param taskId
	 * @param transformTask
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	private boolean createTransformTask(Integer importJobId, Integer taskId, RESTTransform transformTask)
			throws URISyntaxException, HttpException {
		mapper.disable(SerializationFeature.WRAP_ROOT_VALUE);
		Response httpResponse = HttpUtil.post(
				this.addEndPoint(importJobId + "/tasks/" + taskId + "/transforms"),
				this.asJSON(transformTask),
				APPLICATION_JSON,
				this.username,
				this.password
		);
		mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
		if (httpResponse.getStatusCode().equals(HttpStatus.CREATED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param importJobId
	 * @param file
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public RESTImportTaskList uploadFile(Integer importJobId, File file) throws URISyntaxException, HttpException, JsonParseException, JsonMappingException, IOException {

		// multipart POST
		Response httpResponse = HttpUtil.post(
				this.addEndPoint(importJobId + "/tasks"),
				file,
				this.username,
				this.password
		);

		RESTImportTaskList importTaskLists =  null;
		// check, if it is a list of import tasks (for multiple layers)
		try {
			importTaskLists = mapper.readValue(httpResponse.getBody(), RESTImportTaskList.class);
			LOG.info("Imported file "+ file.getName() + " contains data for multiple layers.");
		} catch (Exception e) {
			LOG.info("Imported file "+ file.getName() + " likely contains data for single layer. Will check this now.");
			RESTImportTask helperTask = mapper.readValue(httpResponse.getBody(), RESTImportTask.class);
			if (helperTask != null) {
				importTaskLists = new RESTImportTaskList();
				importTaskLists.add(helperTask);
				LOG.info("Imported file "+ file.getName() + " contains data for a single layers.");
			}
		}
		return importTaskLists;
	}

	/**
	 *
	 * @param importJobId
	 * @param taskId
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public RESTImportTask getRESTImportTask(Integer importJobId, Integer taskId) throws
		URISyntaxException, HttpException, JsonParseException,
		JsonMappingException, IOException, Exception {
		Response httpResponse = HttpUtil.get(
				this.addEndPoint(importJobId + "/tasks/" + taskId),
				this.username,
				this.password
		);

		return (RESTImportTask) this.asEntity(httpResponse.getBody(), RESTImportTask.class);
	}

	/**
	 *
	 * @param importJobId
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public RESTImportTaskList getRESTImportTasks(Integer importJobId) throws
		URISyntaxException, HttpException, JsonParseException,
		JsonMappingException, IOException, Exception {
		Response httpResponse = HttpUtil.get(
				this.addEndPoint(importJobId + "/tasks/"),
				this.username,
				this.password
		);
		return mapper.readValue(httpResponse.getBody(), RESTImportTaskList.class);
	}

	/**
	 *
	 * @param importJobId
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 * @throws HttpException
	 */
	public boolean runImport(Integer importJobId) throws
			UnsupportedEncodingException, URISyntaxException, HttpException {

		Response httpResponse = HttpUtil.post(
				this.addEndPoint(Integer.toString(importJobId)),
				this.username,
				this.password
		);

		if (httpResponse.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param importJobId
	 * @param taskId
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public RESTLayer getLayer(Integer importJobId, Integer taskId) throws
			URISyntaxException, HttpException, JsonParseException,
			JsonMappingException, IOException, Exception {

		Response httpResponse = HttpUtil.get(
				this.addEndPoint(importJobId + "/tasks/" + taskId + "/layer"),
				this.username,
				this.password
		);

		return (RESTLayer) this.asEntity(httpResponse.getBody(), RESTLayer.class);
	}

	/**
	 *
	 * @param importJobId
	 * @param taskId
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public RESTData getDataOfImportTask(Integer importJobId, Integer taskId)
			throws URISyntaxException, HttpException, JsonParseException, JsonMappingException, IOException, Exception {

		final DeserializationFeature unwrapRootValueFeature = DeserializationFeature.UNWRAP_ROOT_VALUE;
		boolean unwrapRootValueFeatureIsEnabled = mapper.isEnabled(unwrapRootValueFeature);

		Response httpResponse = HttpUtil.get(
				this.addEndPoint(importJobId + "/tasks/" + taskId + "/data"),
				this.username,
				this.password
		);

		// we have to disable the feature. otherwise deserialize would not work here
		mapper.disable(unwrapRootValueFeature);

		final RESTData resultEntity = (RESTData) this.asEntity(httpResponse.getBody(), RESTData.class);

		if(unwrapRootValueFeatureIsEnabled) {
			mapper.enable(unwrapRootValueFeature);
		}

		return resultEntity;
	}

	/**
	 *
	 * @param responseBody
	 * @param clazz
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	private AbstractRESTEntity asEntity(byte[] responseBody, Class<?> clazz)
			throws JsonParseException, JsonMappingException, IOException, Exception  {

		AbstractRESTEntity entity = null;

		entity = (AbstractRESTEntity) mapper.readValue(responseBody, clazz);

		return entity;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	private String asJSON(Object entity) {

		String entityJson = null;

		try {
			entityJson = this.mapper.writeValueAsString(entity);
		} catch (Exception e) {
			LOG.error("Could not parse as JSON: " + e.getMessage());
		}

		return entityJson;
	}

	/**
	 *
	 * @param endPoint
	 * @return
	 * @throws URISyntaxException
	 */
	private URI addEndPoint(String endPoint) throws URISyntaxException {

		if (StringUtils.isEmpty(endPoint) || endPoint.equals("/")) {
			return this.baseUri;
		}

		if (this.baseUri.getPath().endsWith("/") || endPoint.startsWith("/")) {
			endPoint = this.baseUri.getPath() + endPoint;
		} else {
			endPoint = this.baseUri.getPath() + "/" + endPoint;
		}

		URI uri = null;

		URIBuilder builder = new URIBuilder();

		builder.setScheme(this.baseUri.getScheme());
		builder.setHost(this.baseUri.getHost());
		builder.setPort(this.baseUri.getPort());
		builder.setPath(endPoint);

		uri = builder.build();
		return uri;
	}

	/**
	 * Create and append importer task for <code>gdaladdo</code>
	 *
	 * @param importJobId
	 * @param importTaskId
	 * @param opts
	 * @param levels
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public boolean createGdalAddOverviewTask(Integer importJobId, Integer importTaskId, List<String> opts, List<Integer> levels) throws URISyntaxException, HttpException {
		RESTGdalAddoTransform transformTask = new RESTGdalAddoTransform();
		if (! opts.isEmpty()) {
			transformTask.setOptions(opts);
		}
		if (! levels.isEmpty()) {
			transformTask.setLevels(levels);;
		}
		return this.createTransformTask(importJobId, importTaskId, transformTask);
	}

	/**
	 * Create and append importer task for <code>gdalwarp</code>
	 *
	 * @param importJobId
	 * @param importTaskId
	 * @param optsGdalWarp
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public boolean createGdalWarpTask(Integer importJobId, Integer importTaskId, List<String> optsGdalWarp) throws URISyntaxException, HttpException {
		RESTGdalWarpTransform transformTask = new RESTGdalWarpTransform();
		if (! optsGdalWarp.isEmpty()){
			transformTask.setOptions(optsGdalWarp);
		}
		return this.createTransformTask(importJobId, importTaskId, transformTask);
	}

	/**
	 * Create and append importer task for <code>gdal_translate</code>
	 *
	 * @param importJobId
	 * @param importTaskId
	 * @param opts
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public boolean createGdalTranslateTask(Integer importJobId, Integer importTaskId, List<String> optsGdalTranslate) throws URISyntaxException, HttpException {
		RESTGdalTranslateTransform transformTask = new RESTGdalTranslateTransform();
		if (! optsGdalTranslate.isEmpty()){
			transformTask.setOptions(optsGdalTranslate);
		}
		return this.createTransformTask(importJobId, importTaskId, transformTask);
	}

	/**
	 * Modify the "srs" definition of the target layer
	 *
	 * @param importJobId
	 * @param importTask
	 * @param sourceSrs
	 *
	 * @return
	 * @throws Exception 
	 */
	public RESTLayer updateSrsForRESTImportTask(Integer importJobId, RESTImportTask importTask, String sourceSrs) throws Exception {
		Integer taskId = importTask.getId();

		RESTLayer updatableLayer = new RESTLayer();
		updatableLayer.setSrs(sourceSrs);

		Response httpResponse = HttpUtil.put(
				this.addEndPoint(importJobId + "/tasks/" + taskId + "/layer"),
				this.asJSON(updatableLayer),
				APPLICATION_JSON,
				this.username,
				this.password
		);

		return (RESTLayer) this.asEntity(httpResponse.getBody(), RESTLayer.class);
	}

	/**
	 * fetch all created Layers of import job
	 *
	 * @param importJobId
	 * @return
	 * @throws Exception 
	 */
	public List<RESTLayer> getAllImportedLayers(Integer importJobId, List<RESTImportTask> tasks) throws Exception {
		ArrayList<RESTLayer> layers = new ArrayList<RESTLayer>();
		for (RESTImportTask task : tasks) {

			RESTImportTask refreshedTask = this.getRESTImportTask(importJobId, task.getId());
			if (refreshedTask.getState().equalsIgnoreCase("COMPLETE")){
				Response httpResponse = HttpUtil.get(
						this.addEndPoint(importJobId + "/tasks/" + task.getId() + "/layer"),
						this.username,
						this.password
				);
				RESTLayer layer = (RESTLayer) this.asEntity(httpResponse.getBody(), RESTLayer.class);

				if (layer != null) {
					layers.add(layer);
				}
			} else if ((tasks.size() == 1) && refreshedTask.getState().equalsIgnoreCase("ERROR")) {
				throw new ImporterException(refreshedTask.getErrorMessage());
			}
		}
		return layers;
	}

	/**
	 * delete an importJob
	 *
	 * @param importJobId
	 * @return
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public boolean deleteImportJob(Integer importJobId) throws URISyntaxException, HttpException{
		Response httpResponse = HttpUtil.delete(
			this.addEndPoint(importJobId.toString()),
			this.username,
			this.password);

		return httpResponse.getStatusCode().equals(HttpStatus.NO_CONTENT);
	}
}
