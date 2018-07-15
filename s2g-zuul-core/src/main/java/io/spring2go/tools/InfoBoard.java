package io.spring2go.tools;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

import io.spring2go.tools.common.StatusInfo;
import io.spring2go.tools.servlet.EnvServlet;
import io.spring2go.tools.servlet.HystrixServlet;
import io.spring2go.tools.servlet.PropsServlet;
import io.spring2go.tools.servlet.StaticServlet;
import io.spring2go.tools.servlet.StatusServlet;
import io.spring2go.tools.stat.JvmStatsReporter;

public class InfoBoard {
	private Logger logger = LoggerFactory.getLogger(InfoBoard.class);

	private String appName;
	private Server server;

	private volatile boolean running = false;

	private StatusInfo statusInfo;
	private JvmStatsReporter jvmStatsReporter;

	public InfoBoard(String appName, int port) {
		this.appName = appName;
		this.server = new Server(port);
		this.statusInfo = new StatusInfo();
		this.jvmStatsReporter = new JvmStatsReporter(appName, 60000);
		this.jvmStatsReporter.addStatsHandler(statusInfo);
		this.jvmStatsReporter.start();
		try {
			ServletContextHandler handler = new ServletContextHandler();
			handler.setContextPath("/");

			handler.setSessionHandler(new SessionHandler());

			handler.addServlet(EnvServlet.class, "/api/env");
			handler.addServlet(PropsServlet.class, "/api/props");
			handler.addServlet(StaticServlet.class, "/*");
			handler.addServlet(HystrixServlet.class, "/breaker");
            handler.addServlet(HystrixMetricsStreamServlet.class, "/hystrix.stream");
			handler.addServlet(new ServletHolder(new StatusServlet(statusInfo)), "/api/status");

			server.setHandler(handler);

		} catch (Exception e) {
			logger.error("start jetty error!", e);
		}

	}

	public synchronized void start() {
		if (!running) {
			try {
				server.start();
				running = true;
			} catch (Exception e) {
				logger.error("Exception in Starting " + this.getClass().getSimpleName(), e);
			}
		}
	}

	public synchronized void shutdown() {
		if (running) {
			try {
				server.stop();
				running = false;
			} catch (Exception e) {
				logger.error("Exception in Stopping " + this.getClass().getSimpleName(), e);
				if (!server.isStarted() && !server.isStarting())
					running = false;
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

}
