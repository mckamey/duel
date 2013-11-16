package org.duelengine.duel.staticapps;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Sets cache control to "never" cache & enables cross-origin access.
 */
public class NeverCacheFilter implements Filter {

	public void init(FilterConfig config) {}

	public void destroy() {}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (response instanceof HttpServletResponse) {
			HttpServletResponse httpResponse = (HttpServletResponse)response;

			httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
			httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
			httpResponse.setDateHeader("Expires", 0L); // HTTP 1.1 clients & proxies

			// add header to enable cross-origin access
			httpResponse.setHeader("Access-Control-Allow-Origin", "*");
		}

		chain.doFilter(request, response);
	}
}
