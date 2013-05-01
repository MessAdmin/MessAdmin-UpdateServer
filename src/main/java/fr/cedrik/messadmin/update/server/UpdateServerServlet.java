/**
 *
 */
package fr.cedrik.messadmin.update.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author C&eacute;drik LIME
 */
@SuppressWarnings("serial")
public class UpdateServerServlet extends HttpServlet {
	protected static final String HEADER_IFMODSINCE   = "If-Modified-Since";
	protected static final String HEADER_LASTMOD      = "Last-Modified";
	protected static final String HEADER_CACHECONTROL = "Cache-Control";
	protected static final String HEADER_EXPIRES      = "Expires";

	protected static final String ROBOTS_TXT = "/robots.txt";

	protected Logger log = Logger.getLogger(this.getClass());

	public UpdateServerServlet() {
	}

	/** {@inheritDoc} */
	@Override
	public void init() throws ServletException {
		super.init();
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		super.destroy();
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (ROBOTS_TXT.equals(request.getPathInfo())) {
			// serve robots.txt file
			InputStream in = getServletContext().getResourceAsStream(ROBOTS_TXT);
			if (in != null) {
				setCache(response, 30*24);
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("text/plain");
				response.setContentLength(in.available());
				ServletOutputStream out = response.getOutputStream();
				copyAndClose(in, out);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, ROBOTS_TXT);
			}
		} else {
			// Helps debugging... :-)
			doHead(request, response);
		}
	}
	protected void copy(InputStream input, OutputStream output) throws IOException {
		int nRead;
		byte[] buffer = new byte[512];//FIXME magic number
		while ((nRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, nRead);
		}
		output.flush();
	}

	protected void copyAndClose(InputStream input, OutputStream output) throws IOException {
		try {
			copy(input, output);
		} finally {
			closeQuietly(input);
			closeQuietly(output);
		}
	}

	private static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ignore) {
		}
	}

	/**
	 * {@inheritDoc}
	 * Pre-flight request
	 */
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		@SuppressWarnings("unused")
		String requestedMethod  = request.getHeader("Access-Control-Request-Method");
		String requestedHeaders = request.getHeader("Access-Control-Request-Headers");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD");
		response.setHeader("Allow", "GET, HEAD, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", requestedHeaders);
		response.setHeader("Access-Control-Max-Age", "" + 30*24*3600);
		setCache(response, 30*24);

		response.setStatus(HttpServletResponse.SC_OK);//HttpServletResponse.SC_NO_CONTENT
	}

	/** {@inheritDoc} */
	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		log.info(path);
		response.setHeader("Access-Control-Allow-Origin", "*");// This is the critical part
		setCache(response, 24);

		if (getServletContext().getResource(path) != null) {
			response.setStatus(HttpServletResponse.SC_OK);//HttpServletResponse.SC_NO_CONTENT
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		response.setContentLength(0);
	}

	protected void setCache(HttpServletResponse response, int nHours) {
		// <strong>NOTE</strong> - This header will be overridden
		// automatically if a <code>RequestDispatcher.forward()</code> call is
		// ultimately invoked.
		response.setHeader(HEADER_CACHECONTROL, "public,max-age="+nHours*3600); // HTTP 1.1
		response.setDateHeader(HEADER_EXPIRES, System.currentTimeMillis() + nHours*3600*1000);
	}
}
