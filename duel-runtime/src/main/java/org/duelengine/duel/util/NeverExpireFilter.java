package org.duelengine.duel.util;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Sets cache control to "never" expire.
 *
 * Only use for SHA1-named CDN resources which change name as content changes.
 * 
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html
 * 
 * To mark a response as "never expires," an origin server sends an
 * Expires date approximately one year from the time the response is sent.
 * HTTP/1.1 servers SHOULD NOT send Expires dates more than one year in the future.
 */
public class NeverExpireFilter implements Filter {

	// this just needs to be far out, do not need to worry about leap year
	private static final long ONE_YEAR = 365L * 24L * 60L * 60L * 1000L;

	public void init(FilterConfig config) {}

	public void destroy() {}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		if (response instanceof HttpServletResponse) {
			HttpServletResponse httpResponse = (HttpServletResponse)response;

			// expire one year from now
			long expiryDate = new Date().getTime() + ONE_YEAR;

			// add cache control response headers
			httpResponse.setDateHeader("Expires", expiryDate);
		}

		chain.doFilter(request, response);
	}
}