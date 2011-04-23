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
 * Simple adapter for using DUEL views in JAX-RS
 */
public class ViewResult implements StreamingOutput {

	private final DuelContext context;
	private final DuelView view;
	private final Object data;

	public ViewResult(Class<DuelView> view, Object data, DuelContext context) {

		if (view == null) {
			throw new NullPointerException("view");
		}

		try {
			this.view = view.newInstance();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error instantiating view: "+view.getSimpleName(), ex);
		}

		this.data = data;
		this.context = (context != null) ? context : new DuelContext();
	}

	public ViewResult putExtra(String ident, Object value) {
		this.context.putExtra(ident, value);

		return this;
	}

	public void writeError(Writer output, Exception ex)
		throws IOException {

		output.append("Error:");
		output.append(ex.toString());
	}

	@Override
	public void write(OutputStream stream)
		throws IOException, WebApplicationException {

		Writer output = new OutputStreamWriter(stream, this.context.getFormat().getEncoding());
		this.context.setOutput(output);

		try {
			if (this.data == null) {
				this.view.render(this.context);
			} else {
				this.view.render(this.context, data);
			}

		} catch (Exception ex) {
			this.writeError(output, ex);

 		} finally {
 			output.flush();
			this.context.setOutput(null);
		}
	}
}
