package de.terrestris.momo.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.terrestris.momo.service.SldService;
import de.terrestris.shogun2.util.data.ResultSet;

/**
 *
 * terrestris GmbH & Co. KG
 * @author kaivolland
 *
 */
@Controller
@RequestMapping("/sld")
public class SldController {

	/**
	 * The Logger.
	 */
	private static final Logger LOG = Logger.getLogger(SldController.class);

	@Autowired
	@Qualifier("sldService")
	private SldService sldService;

	/**
	 *
	 * @param sldName
	 * @param sld
	 * @param layerId
	 * @return
	 */
	@RequestMapping(value = "/update.action", method = {RequestMethod.POST})
	public @ResponseBody Map<String, Object> updateSld(
			@RequestParam String sldName,
			@RequestParam String sld,
			@RequestParam Integer layerId) {

		try {
			this.sldService.updateSld(layerId, sldName, sld);
			LOG.debug("Updated Sld " + sldName);
			return ResultSet.success("Updated Sld " + sldName);
		} catch (Exception e) {
			LOG.error("Error while updating Sld: " + e.getMessage());
			return ResultSet.error("Error while updating Sld: " + e.getMessage());
		}
	}

	/**
	 *
	 * @param layerId
	 * @param width
	 * @param height
	 * @param imgUrl
	 * @param format
	 * @return
	 */
	@RequestMapping(value = "/updateLegendSrc.action", method = {RequestMethod.POST})
	public @ResponseBody Map<String, Object> updateLegendSrc(
			@RequestParam Integer layerId,
			@RequestParam Integer width,
			@RequestParam Integer height,
			@RequestParam String imgUrl,
			@RequestParam String format,
			HttpServletRequest request) {

		try {
			this.sldService.updateLegendSrc(layerId, width, height, imgUrl, format, request);
			LOG.debug("Successfully updated legend source");
			return ResultSet.success("Legend updated");
		} catch (Exception e) {
			LOG.error("Error while updating legend source: " + e.getMessage());
			return ResultSet.error("Error while updating legend source: " + e.getMessage());
		}
	}

}
