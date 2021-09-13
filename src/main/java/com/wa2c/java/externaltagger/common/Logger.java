package com.wa2c.java.externaltagger.common;

import java.lang.management.ManagementFactory;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class Logger {
	/** Logger. */
	final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("Logger");
	/** Debug */
	final static boolean isDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");

	static {
		// set log level
		ConsoleHandler ch = new ConsoleHandler();
		if (isDebug) {
			ch.setLevel(Level.ALL);
		} else {
			ch.setLevel(Level.OFF);
		}
		logger.setLevel( Level.ALL );
		logger.addHandler(ch);
		//logger.setUseParentHandlers(false);

		// HTMLUnit
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	}

	/**
	 * Debug log.
	 * @param obj
	 */
	public static void d(Object obj) {
		if (!isDebug)
			return;
		if (obj == null)
			return;

		logger.config(obj.toString());
	}

	/**
	 * Info log.
	 * @param obj object.
	 */
	public static void i(Object obj) {
		if (obj == null)
			return;

		logger.info(obj.toString());
	}

	/**
	 * Warning log.
	 * @param obj
	 */
	public static void w(Object obj) {
		if (obj == null)
			return;

		logger.warning(obj.toString());
	}

	/**
	 * Error log.
	 * @param obj
	 */
	public static void e(Object obj) {
		if (obj == null)
			return;

		logger.severe(obj.toString());
	}

}
