package org.duelengine.duel.rs;

import java.util.Map;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelView;

/**
 * Simple adapter for rendering DUEL views in JAX-RS
 */
public class ViewResult implements StreamingOutput {

	private final DuelContext context;
	private final DuelView view;

	public ViewResult(Class<? extends DuelView> view, DuelContext context) {

		if (view == null) {
			throw new NullPointerException("view");
		}
		try {
			this.view = view.newInstance();
		} catch (Exception ex) {
			throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
		}

		this.context = (context != null) ? context : new DuelContext();
	}

	public ViewResult data(Object data) {
		this.context.setData(data);

		return this;
	}

	public ViewResult extras(Map<String, ?> extras) {
		this.context.putExtras(extras);

		return this;
	}

	public ViewResult extra(String ident, Object value) {
		this.context.putExtra(ident, value);

		return this;
	}

	@Override
	public void write(OutputStream stream)
		throws IOException, WebApplicationException {

		Writer output = new OutputStreamWriter(stream, this.context.getFormat().getEncoding());

		try {
			this.view.render(this.context.setOutput(output));
 			output.flush();

		} catch (Exception ex) {
			throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);

 		} finally {
			this.context.setOutput(null);
		}
	}
}
