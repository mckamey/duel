package org.duelengine.duel.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelView;

/**
 * Simple MVC-style controller for using DUEL views in JAX-RS
 */
public abstract class DuelController {

	protected abstract DuelContext getViewContext();

	/**
	 * Builds a view result
	 */
	protected ViewResult view(Class<? extends DuelView> view) {
		return new ViewResult(view, this.getViewContext());
	}

	/**
	 * Renders an error message to the response
	 */
	protected StreamingOutput error(final Exception ex) {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream stream)
				throws IOException, WebApplicationException {

				new OutputStreamWriter(stream, "UTF-8").write(ex.toString());
			}
		};
	}
}
