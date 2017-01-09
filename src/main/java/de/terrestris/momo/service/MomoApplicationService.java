package de.terrestris.momo.service;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.DocumentTreeDao;
import de.terrestris.momo.dao.LayerTreeDao;
import de.terrestris.momo.dao.MomoApplicationDao;
import de.terrestris.momo.dto.ApplicationData;
import de.terrestris.momo.model.MomoApplication;
import de.terrestris.momo.model.tree.DocumentTreeFolder;
import de.terrestris.momo.model.tree.LayerTreeFolder;
import de.terrestris.momo.util.DocumentTreeFolderComparator;
import de.terrestris.shogun2.dao.ExtentDao;
import de.terrestris.shogun2.dao.LayerDao;
import de.terrestris.shogun2.dao.LayoutDao;
import de.terrestris.shogun2.dao.MapConfigDao;
import de.terrestris.shogun2.dao.MapControlDao;
import de.terrestris.shogun2.dao.MapDao;
import de.terrestris.shogun2.dao.ModuleDao;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.layer.Layer;
import de.terrestris.shogun2.model.layer.util.Extent;
import de.terrestris.shogun2.model.layout.Layout;
import de.terrestris.shogun2.model.map.MapConfig;
import de.terrestris.shogun2.model.map.MapControl;
import de.terrestris.shogun2.model.module.CompositeModule;
import de.terrestris.shogun2.model.module.Map;
import de.terrestris.shogun2.model.module.Module;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.service.ApplicationService;
import de.terrestris.shogun2.service.ExtentService;
import de.terrestris.shogun2.service.LayerService;
import de.terrestris.shogun2.service.LayoutService;
import de.terrestris.shogun2.service.MapConfigService;
import de.terrestris.shogun2.service.MapControlService;
import de.terrestris.shogun2.service.MapService;
import de.terrestris.shogun2.service.ModuleService;
import de.terrestris.shogun2.service.UserService;

/**
 *
 * @author Nils Bühner
 * @see ApplicationService
 *
 */
@Service("momoApplicationService")
public class MomoApplicationService<E extends MomoApplication, D extends MomoApplicationDao<E>>
		extends ApplicationService<E, D> {

	private static final DocumentTreeFolderComparator DOC_TREE_FOLDER_COMPARATOR = new DocumentTreeFolderComparator();

	private static final String BEAN_ID_DEFAULT_MAP = "defaultMap";

	private static final String BEAN_ID_DEFAULT_MAP_CONTAINER = "defaultMapContainer";

	private static final String BEAN_ID_DEFAULT_MAX_EXTENT = "defaultMaxExtent";

	private static final String BEAN_ID_DEFAULT_RESOLUTIONS = "defaultResolutions";

	private static final String BEAN_ID_MAX_RES = "res19";

	private static final String BEAN_ID_MIN_RES = "res01";

	private static final Set<String> DEFAULT_MAP_CONTROLS = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("Attribution", "Zoom", "Rotate", "ScaleLine", "ZoomSlider")));

	private static final List<String> DEFAULT_MAP_CONTAINER_MODULES = Collections.unmodifiableList(Arrays
			.asList("Show meta toolbar button", "Meta info toolbar for the map", "Tools Toolbar", "Gazetteer Grid"));

	private static final List<String> DEFAULT_VIEWPORT_MODULES = Collections
			.unmodifiableList(Arrays.asList("Viewport Header", "Legend Tree", "Gazetteer Grid"));

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	@Qualifier("moduleService")
	private ModuleService<Module, ModuleDao<Module>> moduleService;

	@Autowired
	@Qualifier("mapConfigService")
	private MapConfigService<MapConfig, MapConfigDao<MapConfig>> mapConfigService;

	@Autowired
	@Qualifier("mapService")
	private MapService<Map, MapDao<Map>> mapService;

	@Autowired
	@Qualifier("layoutService")
	private LayoutService<Layout, LayoutDao<Layout>> layoutService;

	@Autowired
	@Qualifier("extentService")
	private ExtentService<Extent, ExtentDao<Extent>> extentService;

	@Autowired
	@Qualifier("mapControlService")
	private MapControlService<MapControl, MapControlDao<MapControl>> mapControlService;

	@Autowired
	@Qualifier("layerService")
	private LayerService<Layer, LayerDao<Layer>> layerService;

	@Autowired
	@Qualifier("userService")
	private UserService<User, UserDao<User>> userService;

	@Autowired
	@Qualifier("docTreeService")
	private DocumentTreeService<DocumentTreeFolder, DocumentTreeDao<DocumentTreeFolder>> docTreeService;

	@Autowired
	@Qualifier("layerTreeService")
	private LayerTreeService<LayerTreeFolder, LayerTreeDao<LayerTreeFolder>> layerTreeService;

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoApplicationService() {
		this((Class<E>) MomoApplication.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoApplicationService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoApplicationDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	/**
	 *
	 * @param isActive
	 * @param isPublic
	 * @param language
	 * @return
	 * @throws Exception
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())")
	public MomoApplication createMomoApplication(ApplicationData applicationData) throws Exception {

		String name = applicationData.getName();
		String description = applicationData.getDescription();
		String language = applicationData.getLanguage();
		Boolean isPublic = applicationData.getIsPublic();
		Boolean isActive = applicationData.getIsActive();

		String projection = applicationData.getProjection();
		Point2D.Double center = applicationData.getCenter();
		Integer zoom = applicationData.getZoom();

		Integer layerTreeId = applicationData.getLayerTree();

		// create a new application
		MomoApplication application = new MomoApplication();

		// set properties
		application.setName(name);
		application.setDescription(description);
		application.setLanguage(Locale.forLanguageTag(language));
		application.setOpen(isPublic);
		application.setActive(isActive);

		LayerTreeFolder layerTreeRootNode = this.layerTreeService.findById(layerTreeId);
		application.setLayerTree(layerTreeRootNode);

		// 1. map config
		MapConfig mapConfig = buildMapConfig(projection, center, zoom);

		// 2. get the map layers from the provided layerTree
		List<Layer> mapLayers = this.layerTreeService.getAllMapLayersFromTreeFolder(layerTreeRootNode);

		// 3. map
		Map map = buildMapModule(mapConfig, mapLayers);

		// 4. map container
		CompositeModule mapContainer = buildMapContainer(map);

		// 5. viewport
		CompositeModule viewport = buildViewport(mapContainer);

		// 6. finalize...
		application.setViewport(viewport);
		dao.saveOrUpdate((E) application);

		// grant admins rights to current user
		User currentUser = userService.getUserBySession();
		addAndSaveUserPermissions((E) application, currentUser, Permission.ADMIN);

		return application;
	}

	/**
	 *
	 * @param appId
	 * @param appName
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName()) or hasPermission(#appId, 'de.terrestris.momo.model.MomoApplication', 'READ')")
	public MomoApplication copyApp(String appId, String appName) throws Exception {
		if (appId == null || appName == null) {
			return null;
		}
		MomoApplication app = dao.findById(Integer.valueOf(appId));
		MomoApplication appCopy = new MomoApplication();

		appCopy.setName(appName);
		appCopy.setDescription(app.getDescription());
		appCopy.setLanguage(app.getLanguage());
		appCopy.setActive(app.getActive());
		appCopy.setOpen(app.getOpen());

		Integer layerTreeId = app.getLayerTree().getId();
		LayerTreeFolder layerTreeRootNode = this.layerTreeService.findById(layerTreeId);
		appCopy.setLayerTree(layerTreeRootNode);

		// 1. map config
		String projection = "EPSG:3857";
		Point2D.Double center = new Point2D.Double(0,0);
		Integer zoom = 0;
		List<Module> modules = app.getViewport().getSubModules();
		for (Module module : modules) {
			if (module.getName().equalsIgnoreCase("Map Container")) {
				CompositeModule appMapContainer = (CompositeModule) module;
				List<Module> subModules = appMapContainer.getSubModules();
				for (Module subModule : subModules) {
					if (Map.class.isAssignableFrom(subModule.getClass())) {
						Map map = (Map) subModule;
						projection = map.getMapConfig().getProjection();
						center = (Point2D.Double) map.getMapConfig().getCenter();
						zoom = map.getMapConfig().getZoom();
					}
				}
			}
		}

		MapConfig mapConfig = buildMapConfig(projection, center, zoom);

		// 2. get the map layers from the provided layerTree
		List<Layer> mapLayers = this.layerTreeService.getAllMapLayersFromTreeFolder(layerTreeRootNode);

		// 3. map
		Map map = buildMapModule(mapConfig, mapLayers);

		// 4. map container
		CompositeModule mapContainer = buildMapContainer(map);

		// 5. viewport
		CompositeModule viewport = buildViewport(mapContainer);

		// 6. finalize...
		appCopy.setViewport(viewport);
		dao.saveOrUpdate((E) appCopy);

		// grant admins rights to current user
		User currentUser = userService.getUserBySession();
		addAndSaveUserPermissions((E) appCopy, currentUser, Permission.ADMIN);

		return appCopy;
	}

	/**
	 *
	 * @param isActive
	 * @param isPublic
	 * @param language
	 * @return
	 * @throws Exception
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())")
	public MomoApplication updateMomoApplication(ApplicationData applicationData) throws Exception {

		String name = applicationData.getName();
		String description = applicationData.getDescription();
		String language = applicationData.getLanguage();
		Boolean isPublic = applicationData.getIsPublic();
		Boolean isActive = applicationData.getIsActive();

		String projection = applicationData.getProjection();
		Point2D.Double center = applicationData.getCenter();
		Integer zoom = applicationData.getZoom();

		// get application
		MomoApplication application = dao.findById(applicationData.getId());

		if(application == null){
			throw new RuntimeException("Couldn't find application with id " + applicationData.getId());
		}

		// set properties
		application.setName(name);
		application.setDescription(description);
		application.setLanguage(Locale.forLanguageTag(language));
		application.setOpen(isPublic);
		application.setActive(isActive);

		List<Module> modules = application.getViewport().getSubModules();
		for (Module module : modules) {
		if (module.getName().equalsIgnoreCase("Map Container")) {
				CompositeModule appMapContainer = (CompositeModule) module;
			List<Module> subModules = appMapContainer.getSubModules();
				for (Module subModule : subModules) {
					if (Map.class.isAssignableFrom(subModule.getClass())) {
						Map map = (Map) subModule;

						// update the mapLayers array of the application
						Integer layerTreeId = application.getLayerTree().getId();
						LayerTreeFolder layerTreeRootNode = this.layerTreeService.findById(layerTreeId);
						List<Layer> mapLayers = this.layerTreeService.getAllMapLayersFromTreeFolder(layerTreeRootNode);
						map.setMapLayers(mapLayers);
						mapService.saveOrUpdate(map);

						// update the mapconfig
						MapConfig mapConfig = map.getMapConfig();
						mapConfig.setCenter(center);
						mapConfig.setProjection(projection);
						mapConfig.setZoom(zoom);
						mapConfigService.getDao().saveOrUpdate(mapConfig);
					}
				}
			}
		}

		dao.saveOrUpdate((E) application);

		return application;
	}

	/**
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasRole(@configHolder.getDefaultUserRoleName())")
	public List<java.util.Map<String, Object>> getDocumentTreeRootNodeInfo(Integer id) throws Exception {
		E app = this.findById(id);

		if(app == null) {
			throw new Exception("Could not find momo app with id " + id);
		}

		List<DocumentTreeFolder> documentRoots = app.getDocumentRootNodes();

		List<java.util.Map<String, Object>> resultList = new ArrayList<java.util.Map<String, Object>>();

		// we only want to publish the ID and the name of the roots here to list them in a grid
		for (DocumentTreeFolder docTreeRoot : documentRoots) {
			java.util.Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("id", docTreeRoot.getId());
			propMap.put("name", docTreeRoot.getText());

			resultList.add(propMap);
		}

		return resultList;
	}

	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())")
	public DocumentTreeFolder createNewDocumentRoot(Integer id, String name) throws Exception {
		E app = this.findById(id);

		if(app == null) {
			throw new Exception("Could not find momo app with id " + id);
		}

		// create a new root node
		DocumentTreeFolder newRootDoc = new DocumentTreeFolder();
		newRootDoc.setRoot(true);
		newRootDoc.setText(name);
		docTreeService.saveOrUpdate(newRootDoc);

		// add it to the existing docs of this app
		List<DocumentTreeFolder> documentRoots = app.getDocumentRootNodes();
		documentRoots.add(newRootDoc);

		Collections.sort(documentRoots, DOC_TREE_FOLDER_COMPARATOR);

		// save the app
		this.saveOrUpdate(app);

		return newRootDoc;
	}

	/**
	 *
	 * @param id
	 * @param docId
	 * @throws Exception
	 */
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())")
	public void deleteDocumentRoot(Integer id, Integer docId) throws Exception {
		E app = this.findById(id);

		if(app == null) {
			throw new Exception("Could not find momo app with id " + id);
		}

		DocumentTreeFolder doc = this.docTreeService.findById(docId);

		List<DocumentTreeFolder> appDocs = app.getDocumentRootNodes();

		if(!appDocs.contains(doc)) {
			throw new Exception("MoMo application " + id + " does not have doc " + docId);
		}

		// remove the doc from the app
		appDocs.remove(doc);

		// delete the doc
		docTreeService.delete(doc);

		// update the app
		this.saveOrUpdate(app);
	}

	/**
	 * @param mapContainer
	 * @return
	 */
	private CompositeModule buildViewport(CompositeModule mapContainer) {
		CompositeModule viewport = new CompositeModule();
		viewport.setXtype("viewport");

		// get layout
		final SimpleExpression isBorderLayout = Restrictions.eq("type", "border");
		final Layout borderLayout = layoutService.getDao().findByUniqueCriteria(isBorderLayout);

		// submodules
		List<Module> vpSubModules = new ArrayList<Module>();
		vpSubModules.add(mapContainer); //add the map
		final ModuleDao<Module> moduleDao = moduleService.getDao();

		for (String moduleName : DEFAULT_VIEWPORT_MODULES) {
			Criterion hasModuleName = Restrictions.eq("name", moduleName);
			vpSubModules.add(moduleDao.findByUniqueCriteria(hasModuleName));
		}

		viewport.setLayout(borderLayout);
		viewport.setSubModules(vpSubModules);

		moduleService.saveOrUpdate(viewport);
		return viewport;
	}

	/**
	 * @param map
	 * @return
	 */
	private CompositeModule buildMapContainer(Map map) {
		CompositeModule mapContainer = (CompositeModule) applicationContext.getBean(BEAN_ID_DEFAULT_MAP_CONTAINER);

		// get layout
		final SimpleExpression isAbsoluteLayout = Restrictions.eq("type", "absolute");
		final Layout absoluteLayout = layoutService.getDao().findByUniqueCriteria(isAbsoluteLayout);

		// submodules
		List<Module> mcSubModules = new ArrayList<Module>();
		mcSubModules.add(map); //add the map

		final ModuleDao<Module> moduleDao = moduleService.getDao();

		// add in correct order
		for (String moduleName : DEFAULT_MAP_CONTAINER_MODULES) {
			Criterion hasModuleName = Restrictions.eq("name", moduleName);
			mcSubModules.add(moduleDao.findByUniqueCriteria(hasModuleName));
		}

		mapContainer.setLayout(absoluteLayout);
		mapContainer.setSubModules(mcSubModules);

		moduleService.saveOrUpdate(mapContainer);
		return mapContainer;
	}

	/**
	 * @param mapConfig
	 * @return
	 */
	private Map buildMapModule(MapConfig mapConfig, List<Layer> mapLayers) {
		Map map = (Map) applicationContext.getBean(BEAN_ID_DEFAULT_MAP);
		map.setMapConfig(mapConfig);

		final Criterion areDefaultMapControl = Restrictions.in("mapControlName", DEFAULT_MAP_CONTROLS);
		final Set<MapControl> mapControls = new HashSet<MapControl>(
				mapControlService.getDao().findByCriteria(areDefaultMapControl));
		map.setMapControls(mapControls);

		map.setMapLayers(mapLayers);

		mapService.saveOrUpdate(map);
		return map;
	}

	/**
	 * @param projection
	 * @param center
	 * @param zoom
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private MapConfig buildMapConfig(String projection, Point2D.Double center, Integer zoom) {
		MapConfig mapConfig = new MapConfig();
		mapConfig.setRotation(0.0);
		mapConfig.setCenter(center);
		mapConfig.setProjection(projection.replace("EPSG:", ""));
		mapConfig.setZoom(zoom);

		// set values from default map config
		// TODO: become more dynamic, e.g. when another projection is used
		mapConfig.setMinResolution((Double) applicationContext.getBean(BEAN_ID_MIN_RES));
		mapConfig.setMaxResolution((Double) applicationContext.getBean(BEAN_ID_MAX_RES));
		mapConfig.setResolutions((List<Double>) applicationContext.getBean(BEAN_ID_DEFAULT_RESOLUTIONS));

		// TODO let the user configure this extent
		Extent maxExtent = (Extent) applicationContext.getBean(BEAN_ID_DEFAULT_MAX_EXTENT);
		extentService.saveOrUpdate(maxExtent);

		mapConfig.setExtent(maxExtent);

		mapConfigService.saveOrUpdate(mapConfig);
		return mapConfig;
	}

}
