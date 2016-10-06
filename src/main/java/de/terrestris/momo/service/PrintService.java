package de.terrestris.momo.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.model.Response;

/**
*
* @author Johannes Weskamm
* @author terrestris GmbH & Co. KG
*
*/
@Service("printService")
public class PrintService {

	/**
	 *
	 */
	@Autowired
	@Qualifier("printservletBaseUrl")
	private String printservletBaseUrl;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerBaseUrl")
	private String geoServerBaseUrl;

	/**
	 *
	 */
	@Autowired
	@Qualifier("publicInterceptGeoServerAction")
	private String publicInterceptGeoServerAction;

	/**
	 * The Logger
	 */
	private static final Logger LOG =
			Logger.getLogger(PrintService.class);

	/**
	 * intercept the print payload, replace relative interceptor urls
	 * with absolute urls
	 *
	 * @param printSpec
	 * @param request
	 * @param printApp
	 * @param format
	 * @return
	 */
	public Response interceptPrint(String printSpec, HttpServletRequest request, String printApp,
			String format) {
		Response response = null;
		try {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonTree = mapper.readTree(printSpec);

			JsonNode replacedTree = replaceInterceptorUrlsInJson(jsonTree);
			replacedTree = removeCustomVersionParam(jsonTree);

			List<NameValuePair> queryParams = new ArrayList<NameValuePair>(1);
			queryParams.add(new BasicNameValuePair("spec",replacedTree.toString()));
			String url = printservletBaseUrl + "print/" + printApp + "/buildreport." + format;
			response = HttpUtil.post(url, queryParams);
		} catch (Exception e) {
			LOG.error("Error on intercepting a print request: " + e.getMessage(), e);
		}
		return response;
	}

	/**
	 * Method recursively searches for a relative interceptor url and
	 * replaces it with the geoserver base url
	 *
	 * @param jsonNode
	 * @return
	 */
	private JsonNode replaceInterceptorUrlsInJson(JsonNode jsonNode) {

		if (jsonNode.isArray()) {
			Iterator<JsonNode> elements = jsonNode.elements();
			while (elements.hasNext()) {
				replaceInterceptorUrlsInJson(elements.next());
			}
		} else if (jsonNode.isObject()) {
			Iterator<String> fieldNames = jsonNode.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				JsonNode childNode = jsonNode.get(fieldName);
				JsonNode newJsonNode = replaceInterceptorUrlsInJson(childNode);

				if(!childNode.equals(newJsonNode)) {
					((ObjectNode)jsonNode).replace(fieldName, newJsonNode);
				}

			}
		} else if (jsonNode.isTextual()) {
			String value = jsonNode.asText();
			if (value.contains(publicInterceptGeoServerAction)) {
				String replacedString = value.replace(
						publicInterceptGeoServerAction, geoServerBaseUrl);
				jsonNode = new TextNode(replacedString);
			}
		}
		return jsonNode;
	}

	/**
	 * Method removes the VERSION parameter from a layers customParams,
	 * as this breaks the printservlet, which assumes a verison per default
	 *
	 * @param jsonTree
	 * @return
	 */
	private JsonNode removeCustomVersionParam(JsonNode jsonTree) {
		List<JsonNode> customParamsNodes = jsonTree.findValues("customParams");
		for (JsonNode customParamsNode : customParamsNodes) {
			if (customParamsNode != null) {
				JsonNode versionNode = customParamsNode.findValue("VERSION");
				if (versionNode != null) {
					JsonNode value = ((ObjectNode)customParamsNode).remove("VERSION");
					if (value != null) {
						LOG.debug("Removed an uneccessary VERSION parameter in customParams");
					}
				}
			}
		}
		return jsonTree;
	}
}
