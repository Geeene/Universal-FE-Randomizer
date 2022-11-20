package random.general;

import org.eclipse.swt.widgets.Display;

import util.DebugPrinter;
import util.recordkeeper.ChangelogBuilder;
import util.recordkeeper.RecordKeeper;

public abstract class Randomizer extends Thread {
	private static final DebugPrinter LOGGER = DebugPrinter.forKey(DebugPrinter.Key.MAIN);
	private RandomizerListener listener = null;

	public void setListener(RandomizerListener listener) {
		this.listener = listener;
	}

	protected void updateStatusString(String string) {
		LOGGER.log(string);
		if (listener != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.onStatusUpdate(string);
				}
			});
		}
	}

	protected void updateProgress(double progress) {
		if (listener != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.onProgressUpdate(progress);
				}
			});
		}
	}

	protected void notifyError(String errorString) {
		LOGGER.log(errorString);
		if (listener != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.onError(errorString);
				}
			});
		}
	}

	protected void notifyCompletion(RecordKeeper rk, ChangelogBuilder cb) {
		if (listener != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.onComplete(rk, cb);
				}
			});
		}
	}
}
