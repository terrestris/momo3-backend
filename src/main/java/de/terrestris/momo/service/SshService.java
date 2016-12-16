package de.terrestris.momo.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import de.terrestris.shogun2.util.data.ResultSet;

/**
*
* @author Johannes Weskamm
* @author terrestris GmbH & Co. KG
*
*/
@Service("sshService")
public class SshService {

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerBaseURL")
	private String remoteIp;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerSshUserName")
	private String user;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerSshPort")
	private String port;

	/**
	 *
	 */
	@Autowired
	@Qualifier("geoServerSshKeyPath")
	private String keyPath;

	/**
	 * The Logger
	 */
	private static final Logger LOG =
			Logger.getLogger(SshService.class);

	/**
	 * Sets up a JSch session used to connect to a remote machine
	 *
	 * @return
	 * @throws JSchException
	 */
	public Session getSession() throws JSchException {

		JSch jsch = new JSch();
		Session session;

		// add pubkey
		jsch.addIdentity(keyPath);

		// Open a Session to remote SSH server and Connect.
		// Set User and IP of the remote host and SSH port.
		int portInt = Integer.parseInt(port);
		session = jsch.getSession(user, remoteIp, portInt);

		// When we do SSH to a remote host for the 1st time or if key at the remote host
		// changes, we will be prompted to confirm the authenticity of remote host.
		// This check feature is controlled by StrictHostKeyChecking ssh parameter.
		// By default StrictHostKeyChecking  is set to yes as a security measure.
		session.setConfig("StrictHostKeyChecking", "no");

		LOG.debug("An SSH session has been created");

		return session;

	}

	/**
	 * Executes a command on the remote machine, which is given as String parameter
	 *
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws JSchException
	 */
	public Map<String, Object> execCommand(String command) throws IOException, JSchException {

		boolean executionSuccessful = false;

		ChannelExec channelExec = null;
		Session session = null;
		InputStream in = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String output = "";
		String errString = "";

		try {
			session = getSession();
			session.connect();

			// create the execution channel over the session
			channelExec = (ChannelExec) session.openChannel("exec");

			// Set the command to execute on the channel and execute the command
			channelExec.setCommand(command);
			channelExec.connect();

			// Get an InputStream from this channel and read messages, generated
			// by the executing command, from the remote side.
			in = channelExec.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			// set the error stream
			channelExec.setErrStream(baos);

			String line;
			while ((line = reader.readLine()) != null) {
				output += line;
			}

			// Command execution completed here.

			errString = new String(baos.toByteArray(), StandardCharsets.UTF_8);
			// Retrieve the exit status of the executed command
			int exitStatus = channelExec.getExitStatus();
			if (exitStatus > 0 || errString.length() > 0) {
				LOG.error("Error on execution: " + errString + ". Exit Code was " + exitStatus);
			} else {
				executionSuccessful = true;
			}
		} finally {
			//Disconnect the Session and exec, close streams
			if (channelExec != null) {
				channelExec.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}

			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(baos);
		}

		if (executionSuccessful) {
			return ResultSet.success(output);
		} else {
			return ResultSet.error(errString);
		}
	}
}
