package de.terrestris.momo.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.TreeSet;

import javax.transaction.NotSupportedException;

import org.apache.http.HttpException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.terrestris.momo.dao.MomoLayerDao;
import de.terrestris.momo.model.MomoLayer;
import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.interceptor.InterceptorException;
import de.terrestris.shogun2.util.interceptor.OgcXmlUtil;
import de.terrestris.shogun2.util.model.Response;
import javassist.NotFoundException;

/**
 *
 * @author Kai Volland
 * @author terrestris GmbH & Co. KG
 *
 */
@Service("metadataService")
public class MetadataService {

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoNetworkCswUrl")
	private String cswUrl;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoNetworkUsername")
	private String username;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoNetworkPassword")
	private String password;

	/**
	 *
	 */
	@Autowired
	@Qualifier("momoLayerService")
	private MomoLayerService<MomoLayer, MomoLayerDao<MomoLayer>> momoLayerService;

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(MetadataService.class);

	/**
	 *
	 * @param body
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws NotFoundException
	 */
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName()) or "
			+ "hasPermission(#layerId, 'de.terrestris.momo.model.MomoLayer', #transactionOperation)")
	public String cswRequest(Integer layerId, String transactionOperation, String xml) throws
			URISyntaxException, HttpException, IOException, NotFoundException {
		ContentType contentType = ContentType.APPLICATION_XML.withCharset("UTF-8");

		if (!transactionOperation.equalsIgnoreCase("CREATE") && layerId != null) {
			MomoLayer layer = momoLayerService.findById(layerId);
			
			if (layer == null) {
				String msg = "Could not find a layer with ID " + layerId;
				LOG.error(msg);
				throw new NotFoundException(msg);
			}
			if (!transactionOperation.equalsIgnoreCase("CREATE")) {
				String metaDataIdentifier = layer.getMetadataIdentifier();
				
				boolean isValidRecord = isValidRecord(metaDataIdentifier);
				
				if (!isValidRecord) {
					String msg = "UUID is not valid";
					LOG.error(msg);
					throw new NotFoundException(msg);
				}
			}
		}

		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		Response response = HttpUtil.post(cswUrl, xml, contentType, credentials);

		LOG.debug("Sending new CSW Request");
		return new String(response.getBody(), "UTF-8");
	}

	/**
	 *
	 * @param recordId
	 * @return
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public boolean isValidRecord(String recordId) throws URISyntaxException, HttpException,
			JsonProcessingException, IOException {

		LOG.trace("Trying to verify the given UUID.");

		// Get the record as JSON.
		String getRecordUrl = cswUrl + "?request=GetRecordById&service=CSW&version=2.0.2&"
				+ "elementSetName=brief&_content_type=json&id=" + recordId;

		LOG.trace("Sending CSW GetRecordById Request for: " + recordId);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		Response response = HttpUtil.get(getRecordUrl, credentials);

		ObjectMapper om = new ObjectMapper();
		JsonNode responseNode = om.readTree(response.getBody());
		JsonNode briefRecord = responseNode.get("csw:BriefRecord");

		if (briefRecord != null && briefRecord.get("dc:identifier") != null) {
			LOG.trace("The UUID is valid.");
			return recordId.equalsIgnoreCase(briefRecord.get("dc:identifier").textValue());
		}

		LOG.trace("The UUID is NOT valid.");
		return false;
	}

	/**
	 *
	 * @param decodedXML
	 * @return
	 * @throws NotFoundException
	 * @throws NotSupportedException
	 * @throws IOException
	 * @throws InterceptorException
	 * @throws Exception
	 */
	public String getTransactionOperation(String decodedXML) throws NotFoundException,
			NotSupportedException, IOException, InterceptorException {

		LOG.trace("Trying to detect the CSW Transaction operation from a CSW Transaction XML string.");

		TreeSet<String> transactionOperationList = new TreeSet<String>();
		Document doc = OgcXmlUtil.getDocumentFromString(decodedXML);
		NodeList resultList = OgcXmlUtil.getPathInDocumentAsNodeList(doc, "//Transaction/*");
		for (int i = 0; i < resultList.getLength(); i++) {
			Node result = resultList.item(i);
			String nodeName = result.getNodeName();

			if (nodeName.contains("Insert")) {
				transactionOperationList.add("CREATE");
			} else if (nodeName.contains("Update")) {
				transactionOperationList.add("UPDATE");
			} else if (nodeName.contains("Delete")) {
				transactionOperationList.add("DELETE");
			}
		}

		if (transactionOperationList.isEmpty()) {
			throw new NotFoundException("Could not detect the transaction type of the given CSW Transaction.");
		}

		if (transactionOperationList.size() > 1) {
			throw new NotSupportedException("The given CSV Transaction specifies more than one "
					+ " single operation. This is currently not supported.");
		}

		LOG.trace("Got the following CSW Transaction operation: " + transactionOperationList.first());

		return transactionOperationList.first();
	}

}
