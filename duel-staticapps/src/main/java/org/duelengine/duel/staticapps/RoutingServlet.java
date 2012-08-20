package org.duelengine.duel.staticapps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.duelengine.duel.CDNLinkInterceptor;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelView;
import org.duelengine.duel.FormatPrefs;
import org.duelengine.duel.LinkInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingServlet extends HttpServlet {
	private static final Logger log = LoggerFactory.getLogger(RoutingServlet.class);

	private static final long serialVersionUID = 8465487004837241467L;
	private final static String DEFAULT_DOC = "index.html";

	private SiteConfig config;
	private FormatPrefs format;
	private LinkInterceptor linkInterceptor;

	@Override
	public void init(ServletConfig servletConfig)
			throws ServletException {

		super.init(servletConfig);

		String configPath = null;
		try {
			// load from config file
			configPath = servletConfig.getInitParameter("config-path");
			if (configPath == null || configPath.isEmpty()) {
				config = null;

			} else {
				File configFile = new File(configPath);
				if (configFile.exists()) {
					log.info("Loading config from file path: "+configFile.getPath());
					config = new ObjectMapper().reader(SiteConfig.class).readValue(configFile);
				}
			}

		} catch (Exception ex) {
			log.error("Error loading staticapp config from 'config-path' param in WEB-INF/web.xml: "+configPath, ex);
			config = null;
		}

		if (config == null) {
			try {
				configPath = servletConfig.getInitParameter("config-resource");
				log.info("Loading config from resource: "+configPath);
				InputStream stream = getServletContext().getResourceAsStream(configPath);
				config = new ObjectMapper().reader(SiteConfig.class).readValue(stream);

			} catch (Exception ex) {
				log.error("Error loading staticapp config from 'config-resource' param in WEB-INF/web.xml: "+configPath, ex);
				config = new SiteConfig();
			}
		}

		String devModeOverride = servletConfig.getInitParameter("dev-mode-override");
		if (devModeOverride != null && !devModeOverride.isEmpty()) {
			log.info("dev-mode-override="+devModeOverride);
			config.isDevMode( Boolean.parseBoolean(devModeOverride) );
		}

		format = new FormatPrefs()
			.setEncoding(config.encoding())
			.setIndent(config.isDevMode() ? "\t" : "")
			.setNewline(config.isDevMode() ? "\n" : "");

		try {
			String bundleName = config.cdnMap();
			ResourceBundle cdnBundle =
				(bundleName == null) || bundleName.isEmpty() ? null :
				ResourceBundle.getBundle(bundleName, Locale.ROOT);

			linkInterceptor = new CDNLinkInterceptor(
				config.cdnHost(),
				cdnBundle,
				config.isDevMode());

		} catch (URISyntaxException ex) {
			log.error("CDN URI Error", ex);

			linkInterceptor = new LinkInterceptor() {
				@Override
				public String transformURL(String url) {
					return url+"#CDN-ERROR";
				}
			};
		}
	}

	/**
	 * Service the request
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			// response headers
			response.setContentType(config.contentType());
			response.setCharacterEncoding(config.encoding());

			SiteViewPage sitePage = route(request.getServletPath());
			if (sitePage == null) {
				defaultServlet(request, response);
				return;
			}

			DuelContext context = new DuelContext()
				.setFormat(format)
				.setLinkInterceptor(linkInterceptor)
				.setData(sitePage.data())
				.setOutput(response.getWriter());
	
			if (sitePage.extras() != null) {
				// ambient client-side data
				context.putExtras(sitePage.extras());
			}

			DuelView view = sitePage.viewInstance(config.serverPrefix(), Thread.currentThread().getContextClassLoader());
			if (view == null) {
				defaultServlet(request, response);
				return;
			}

			// response body
			view.render(context);

		} catch (Exception ex) {
			try {
				ex.printStackTrace(response.getWriter());
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			} catch (IOException ex2) {
				ex2.printStackTrace();
			}
		}
	}

	/**
	 * Use the config to statically route the request to the view.
	 * @param servletPath
	 * @return
	 */
	private SiteViewPage route(String servletPath) {
		log.info("routing: "+servletPath);

		if (config.views() == null) {
			return null;
		}

		// TODO: expand routing capabilities beyond exact match and default doc
		
		SiteViewPage page = config.views().get(servletPath.substring(1));
		if (page == null && servletPath.endsWith("/")) {
			// continue to attempt to resolve with default document
			log.info("routing: "+servletPath+DEFAULT_DOC);
			page = config.views().get(servletPath.substring(1)+DEFAULT_DOC);
		}

		return page;
	}

	/**
	 * Pass the request onto the default servlet
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void defaultServlet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		getServletContext().getNamedDispatcher("default").forward(request, response);
	}
}