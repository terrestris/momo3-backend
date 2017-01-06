package de.terrestris.momo.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.model.Response;

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
	 */
	public String cswRequest(String xml) throws URISyntaxException, HttpException, IOException {
		ContentType contentType = ContentType.APPLICATION_XML;
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		Response response = HttpUtil.post(cswUrl, xml, contentType, credentials);
		LOG.debug("Sending new CSW Request");
		return new String(response.getBody(), "UTF-8");
	}


}
