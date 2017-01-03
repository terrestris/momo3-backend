package de.terrestris.momo.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.momo.service.MomoLayerService;
import de.terrestris.shogun2.util.data.ResultSet;
import de.terrestris.shogun2.web.LayerController;

/**
 * @author Kai Volland
 *
 */
@Controller
@RequestMapping("/momolayers")
public class MomoLayerController<E extends MomoLayer, D extends MomoLayerDao<E>, S extends MomoLayerService<E, D>>
		extends LayerController<E, D, S> {

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("momoLayerService")
	public void setService(S service) {
		this.service = service;
	}
	
	/**
	 *
	 * @return
	 */
	@RequestMapping(value = "/getLayerExtent.action", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getLayerExtent(
			@RequestParam("layerId") Integer layerId) {

		try {
			String extent = this.service.getLayerExtent(layerId);
			return ResultSet.success(extent);
		} catch (Exception e) {
			return ResultSet.error("Could not get the layers extent: " +
					e.getMessage());
		}
	}

	/**
	 *
	 * @return
	 */
	@RequestMapping(value = "/download.action", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<byte[]> downloadLayer(
			@RequestParam("layerIds") List<Integer> layerIds) {

		try {
			byte[] bytes = this.service.downloadLayers(layerIds);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", "application/zip");
			headers.add("Content-Disposition", "attachment; filename=momo-download.zip");
			return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
		} catch (Exception e) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<byte[]>(new byte[0], headers, HttpStatus.NOT_FOUND);
		}
	}

}
