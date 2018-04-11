package de.terrestris.momo.service;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpException;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
	@Value("${geoserver.baseUrl}")
	private String geoServerBaseUrl;

	/**
	 *
	 */
	@Value("${momo.publicInterceptGeoServerAction}")
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

			String url = printservletBaseUrl + "print/" + printApp + "/report." + format;
			ContentType contentType = ContentType.APPLICATION_JSON;
			response = HttpUtil.post(url, replacedTree.toString(), contentType);
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
			ArrayNode arrNode = new ArrayNode(null);
			while (elements.hasNext()) {
				JsonNode newNode = replaceInterceptorUrlsInJson(elements.next());
				arrNode.add(newNode);
			}
			jsonNode = arrNode;
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

	public Response interceptStatus(HttpServletRequest request, String printApp, String identifier) throws UnsupportedEncodingException, URISyntaxException, HttpException {
		identifier = identifier.split(".json")[0];
		String url = printservletBaseUrl + "print/" + printApp + "/status/" + identifier + ".json";
		Response response = HttpUtil.get(url);
		return response;
	}

	public Response interceptPrintDownload(HttpServletRequest request, String printApp, String identifier) throws URISyntaxException, HttpException {
		identifier = identifier.split(".json")[0];
		String url = printservletBaseUrl + "print/" + printApp + "/report/" + identifier;
		Response response = HttpUtil.get(url);
		return response;
	}
}
