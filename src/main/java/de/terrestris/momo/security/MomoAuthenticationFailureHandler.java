package de.terrestris.momo.security;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
public class MomoAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Autowired
	private String redirectPathLoginError;

	@Autowired
	private String redirectPathForDisabledUser;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		ServletContext ctx = request.getSession().getServletContext();
		String contextPath = ctx.getContextPath();

		if (exception instanceof DisabledException) {
			response.sendRedirect(contextPath + redirectPathForDisabledUser);
		} else {
			response.sendRedirect(contextPath + redirectPathLoginError);
		}
	}
}
