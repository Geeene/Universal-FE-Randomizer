package util;

import org.eclipse.swt.widgets.Display;

import util.DebugPrinter.Key;

import java.util.HashMap;
import java.util.Map;

public class DebugPrinterSingleton {

	private static final DebugPrinterSingleton singleton = new DebugPrinterSingleton();

	private static Map<String, DebugListener> listeners = new HashMap<String, DebugListener>();

	public static DebugPrinterSingleton get(){
		return singleton;
	}
	
	public void log(Key label, String output) {
		if (shouldPrintLabel(label)) {
			System.out.println("[" + label.label + "] " + output);
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (DebugListener listener : listeners.values()) {
					listener.logMessage(label.label, output);
				}	
			}
		});
	}
	
	public void error(Key label, String output) {
		System.err.println("[" + label.label + "] " + output);
	}
	
	public void registerListener(DebugListener listener, String key) {
		listeners.put(key, listener);
		listener.logMessage("DebugPrinter", "Registered Listener. Ready to send messages.");
	}
	
	public void unregisterListener(String key) {
		listeners.remove(key);
	}
	
	private Boolean shouldPrintLabel(Key label) {
		switch (label) {
//		case MAIN:
//		case FE9_DATA_FILE_HANDLER_V2:
//			return true;
		default:
			return false;
		}
	}
}