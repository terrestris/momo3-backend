package de.terrestris.momo.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.transaction.NotSupportedException;

import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.terrestris.momo.service.MetadataService;
import de.terrestris.shogun2.util.data.ResultSet;
import de.terrestris.shogun2.util.interceptor.InterceptorException;
import javassist.NotFoundException;

/**
 *
 * terrestris GmbH & Co. KG
 * @author kaivolland
 *
 */
@Controller
@RequestMapping("/metadata")
public class MetadataController {

	@Autowired
	@Qualifier("metadataService")
	private MetadataService metadataService;

	@RequestMapping(value = "/csw.action", method = {RequestMethod.POST})
	public @ResponseBody Map<String, Object> cswRequest(
			@RequestParam String xml,
			@RequestParam Integer layerId) {
		try {
			String decodedXML = java.net.URLDecoder.decode(xml, "UTF-8");

			String transactionOperation = this.metadataService.getTransactionOperation(xml);

			String response = this.metadataService.cswRequest(layerId, transactionOperation, decodedXML);
			return ResultSet.success(response);
		} catch (URISyntaxException | HttpException | IOException | NotFoundException | NotSupportedException | InterceptorException e) {
			return ResultSet.error("Error during csw-transaction: " + e.getMessage());
		}
	}

}
