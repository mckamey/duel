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
import org.duelengine.duel.utils.FileUtil;
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
			configPath = System.getProperty("org.duelengine.duel.staticapps.configPath");
			if (configPath == null || configPath.isEmpty()) {
				log.info("Loading config-path from WEB-INF/web.xml");
				configPath = servletConfig.getInitParameter("config-path");
			}
			if (configPath == null || configPath.isEmpty()) {
				config = null;

			} else {
				File configFile = new File(configPath);
				if (configFile.exists()) {
					log.info("Loading config from file path: "+configFile.getPath());
					config = new ObjectMapper().reader(SiteConfig.class).readValue(configFile);
				} else {
					log.error("File not found from 'config-path' param: "+configPath);
					configPath = null;
				}
			}

		} catch (Throwable ex) {
			log.error("Error loading staticapp config from 'config-path' param: "+configPath, ex);
			config = null;
		}

		if (config == null) {
			try {
				configPath = System.getProperty("org.duelengine.duel.staticapps.configResource");
				if (configPath == null || configPath.isEmpty()) {
					log.info("Loading config-resource from WEB-INF/web.xml");
					configPath = servletConfig.getInitParameter("config-resource");
				}
				log.info("Loading config from resource: "+configPath);
				InputStream stream = getClass().getResourceAsStream(configPath);
				config = new ObjectMapper().reader(SiteConfig.class).readValue(stream);

			} catch (Throwable ex) {
				log.error("Error loading staticapp config from 'config-resource' param: "+configPath, ex);
				config = null;
			}
		}
		if (config == null) {
			// dummy noop config
			config = new SiteConfig();
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
			String servletPath = request.getServletPath();
			SiteViewPage sitePage = route(servletPath);
			if (sitePage == null) {
				log.debug("routing: "+servletPath+" (static)");
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
				log.error("routing: "+servletPath+" view instance missing");
				defaultServlet(request, response);
				return;
			}

			// response headers
			response.setContentType(config.contentType());
			response.setCharacterEncoding(config.encoding());

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
		if (config.views() == null) {
			return null;
		}

		// TODO: expand routing capabilities beyond exact match, default-doc and catch-all

		SiteViewPage page = config.views().get(servletPath.substring(1));
		if (page != null) {
			log.info("routing: "+servletPath);

		} else {
			String aliasedPath = servletPath;

			if ("".equals(FileUtil.getExtension(aliasedPath))) {
				// continue to attempt to resolve with default document
				if (!aliasedPath.endsWith("/")) {
					aliasedPath += '/';
				}
				aliasedPath += DEFAULT_DOC;
				page = config.views().get(aliasedPath.substring(1));
			}

			if (page != null) {
				log.info("routing: "+servletPath+" [as "+aliasedPath+"]");

			} else {
				// continue to attempt to resolve with catch-all
				String ext = FileUtil.getExtension(aliasedPath);
				page = config.views().get("*"+ext);
				if (page != null) {
					log.info("routing: "+servletPath+" [as *"+ext+"]");
				}
			}
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