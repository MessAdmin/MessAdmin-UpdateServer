/**
 *
 */
package fr.cedrik.messadmin.update.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.NDC;

/**
 * @author C&eacute;drik LIME
 */
public class Log4JListener implements ServletContextListener, ServletRequestListener {

	public Log4JListener() {
	}

	/** {@inheritDoc} */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//org.apache.log4j.PropertyConfigurator.configure("log4j.properties");
	}

	/** {@inheritDoc} */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		LogManager.shutdown();
	}

	/** {@inheritDoc} */
	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		NDC.push(sre.getServletRequest().getRemoteHost());
	}

	/** {@inheritDoc} */
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		NDC.pop();
		if (NDC.getDepth() == 0) {
			NDC.remove();
		}
	}

}
