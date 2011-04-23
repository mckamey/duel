package org.duelengine.duel.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelView;

/**
 * MVC-style controller for using DUEL views in JAX-RS
 */
public abstract class DuelController {

	protected abstract DuelContext getViewContext();

	/**
	 * Renders a view to the response.
	 */
	protected StreamingOutput view(Class<DuelView> view) {
		return this.view(view, null, null);
	}

	/**
	 * Renders a view to the response after binding with data.
	 */
	protected StreamingOutput view(Class<DuelView> view, Object data) {
		return this.view(view, data, null);
	}

	/**
	 * Renders a view to the response after binding with data and extra ambient values.
	 */
	protected StreamingOutput view(Class<DuelView> view, Object data, Map<String, ?> extras) {
		try {
			DuelContext context = this.getViewContext();
			if (extras != null) {
				for (String ident : extras.keySet()) {
					context.putExtra(ident, extras.get(ident));
				}
			}
			return new ViewResult(view, data, context);

		} catch (Exception ex) {
			ex.printStackTrace();
			return this.error(ex);
		}
	}

	/**
	 * Renders an error message.
	 */
	protected StreamingOutput error(final Exception ex) {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream stream) throws IOException, WebApplicationException {
				new OutputStreamWriter(stream, "UTF-8").write(ex.toString());
			}
		};
	}
}
