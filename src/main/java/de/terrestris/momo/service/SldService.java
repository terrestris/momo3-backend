package de.terrestris.momo.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.terrestris.momo.dao.GeoserverPublisherDao;

/**
 *
 * @author Kai Volland
 * @author terrestris GmbH & Co. KG
 *
 */
@Service("sldService")
public class SldService {

	@Autowired
	private GeoserverPublisherDao geoserverPublisherDao;

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(SldService.class);

	public void updateSld(String sldName, String sld) {
		LOG.info("Updating " + sldName);
		geoserverPublisherDao.updateStyle(sld, sldName);
	}

}
