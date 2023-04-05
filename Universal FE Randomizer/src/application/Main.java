package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import ui.MainView;
import util.DebugListener;
import util.DebugPrinter;

public class Main {

	static Display mainDisplay;
	static MainView mainView;

	public static void main(String[] args) throws IOException {

		/* Instantiate Display object, it represents SWT session */
		mainDisplay = new Display();

		mainView = new MainView(mainDisplay);

		File f = new File("C:/users/Marvin/Desktop/Yune/log.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		DebugPrinter.registerListener(new DebugListener() {

			@Override
			public void logMessage(String category, String message) {
				try {
					bw.append(String.format("[%s] %s%n", category, message));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "sysout");

		while (!mainView.mainShell.isDisposed()) {
			if (!mainDisplay.readAndDispatch())
				mainDisplay.sleep();
		}

		/* Dispose the display */
		mainDisplay.dispose();
	}

}
