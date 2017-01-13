/**
 *
 */
package de.terrestris.momo.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.terrestris.momo.service.PrintService;
import de.terrestris.shogun2.util.http.HttpUtil;
import de.terrestris.shogun2.util.model.Response;

/**
 * @author Johannes Weskamm
 *
 */
@Controller
@RequestMapping("/print")
public class PrintController {

	/**
	 *
	 */
	@Autowired
	@Qualifier("printservletBaseUrl")
	private String printservletBaseUrl;

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(PrintController.class);

	@Autowired
	@Qualifier("printService")
	private PrintService service;

	/**
	 * Forwarding the apps.json request
	 *
	 * @param jsonp
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/print/apps.json", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<byte[]> getApps(@RequestParam("jsonp") String jsonp) {
		LOG.debug("Requested to intercept a print 'apps' request");
		String url = printservletBaseUrl + "print/apps.json?jsonp=" + jsonp;
		try {
			Response response = HttpUtil.get(url);
			return new ResponseEntity(response.getBody(), response.getHeaders(), response.getStatusCode());
		} catch (Exception e) {
			LOG.error("Error intercepting a print 'apps' request: ", e);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity("", headers, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Forwarding the capabilities.json request
	 *
	 * @param jsonp
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/print/{printApp}/capabilities.json", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<byte[]> getCapabilities(@RequestParam("jsonp") String jsonp,
			@PathVariable("printApp") String printApp) {
		LOG.debug("Requested to intercept a print 'capabilities' request");

		String url = printservletBaseUrl + "print/" + printApp + "/capabilities.json?jsonp=" + jsonp;
		try {
			Response response = HttpUtil.get(url);
			return new ResponseEntity(response.getBody(), response.getHeaders(), response.getStatusCode());
		} catch (Exception e) {
			LOG.error("Error intercepting a print 'capabilities' request: ", e);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity("", headers, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Forwarding the report.pdf request, intercepting payload to replace
	 * interceptor urls with absolute urls
	 *
	 * @param printSpec
	 * @param request
	 * @param printApp
	 * @return
	 */
	@RequestMapping(value = "/print/{printApp}/report.{format}", method = {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody ResponseEntity<byte[]> intercept(@RequestBody String printSpec,
			HttpServletRequest request, @PathVariable("printApp") String printApp,
			@PathVariable("format") String format) {
		LOG.debug("Requested to intercept a print 'report' request");

		try {
			Response response = service.interceptPrint(printSpec, request, printApp, format);
			return new ResponseEntity<byte[]>(response.getBody(), response.getHeaders(), response.getStatusCode());
		} catch (Exception e) {
			LOG.error("Error intercepting a print 'buildreport' request: ", e);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<byte[]>(new byte[0], headers, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 *
	 * @param printSpec
	 * @param request
	 * @param printApp
	 * @param format
	 * @return
	 */
	@RequestMapping(value = "/print/{printApp}/report/{identifier}", method = {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody ResponseEntity<byte[]> interceptReportDownload(
			HttpServletRequest request, @PathVariable("printApp") String printApp,
			@PathVariable("identifier") String identifier) {
		LOG.debug("Requested to intercept a print 'download' request");

		try {
			Response response = service.interceptPrintDownload(request, printApp, identifier);
			return new ResponseEntity<byte[]>(response.getBody(), response.getHeaders(), response.getStatusCode());
		} catch (Exception e) {
			LOG.error("Error intercepting a print 'download' request: ", e);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<byte[]>(new byte[0], headers, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 *
	 * @param request
	 * @param printApp
	 * @param identifier
	 * @return
	 */
	@RequestMapping(value = "/print/{printApp}/status/{identifier}", method = {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody ResponseEntity<byte[]> interceptStatus(
			HttpServletRequest request, @PathVariable("printApp") String printApp,
			@PathVariable("identifier") String identifier) {
		LOG.debug("Requested to intercept a 'status' request");

		try {
			Response response = service.interceptStatus(request, printApp, identifier);
			return new ResponseEntity<byte[]>(response.getBody(), response.getHeaders(), response.getStatusCode());
		} catch (Exception e) {
			LOG.error("Error intercepting a print 'status' request: ", e);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<byte[]>(new byte[0], headers, HttpStatus.NOT_FOUND);
		}
	}

}

