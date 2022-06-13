package me.operon.controllerblockwe;

public class Logger {
	private java.util.logging.Logger actualLog;
	private ControllerBlock parent;

	public Logger(ControllerBlock c, String n) {
		parent = c;
		actualLog = java.util.logging.Logger.getLogger(n);
	}

	public void info(String msg) {
		actualLog.info(parent.getDescription().getName() + ": " + msg);
	}

	public void warning(String msg) {
		actualLog.warning(parent.getDescription().getName() + ": " + msg);
	}

	public void severe(String msg) {
		actualLog.severe(parent.getDescription().getName() + ": " + msg);
	}

	public void debug(String msg) { // Why is this unused? ~Techzune
	}
}