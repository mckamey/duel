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
	
	private final String DEFAULT_ENCODING = "UTF-8";

	private final DuelContext context;
	private final DuelView view;
	private final Object data;
	private String encoding;

	public ViewResult(DuelView view) {
		this(view, null, null);
	}

	public ViewResult(DuelView view, DuelContext context) {
		this(view, context, null);
	}

	public ViewResult(DuelView view, Object data) {
		this(view, null, data);
	}

	public ViewResult(DuelView view, DuelContext context, Object data) {
		if (view == null) {
			throw new NullPointerException("view");
		}
		if (context == null) {
			context = new DuelContext();
		}

		this.context = context;
		this.view = view;
		this.data = data;
	}

	public String getEncoding() {
		if (this.encoding == null) {
			return DEFAULT_ENCODING;
		}
		return this.encoding;
	}

	public void setEncoding(String value) {
		this.encoding = value;
	}

	public void writeError(Writer output, Exception ex)
		throws IOException {

		output.append("Error:");
		output.append(ex.toString());
	}

	@Override
	public void write(OutputStream stream)
		throws IOException, WebApplicationException {

		Writer output = new OutputStreamWriter(stream, this.getEncoding());
		this.context.setOutput(output);

		try {
			if (this.data == null) {
				this.view.render(this.context);
			} else {
				this.view.render(this.context, data);
			}

		} catch (Exception ex) {
			writeError(output, ex);

		} finally {
			output.flush();
		}
	}
}
