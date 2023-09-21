package io.oneko.websocket;

import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class ObservableStringLineOutputStream extends OutputStream {

	private final Consumer<String> observer;

	private final StringBuilder lineBuilder = new StringBuilder();
	private final StringBuilder resultStringBuilder = new StringBuilder();

	private long lastFlush = System.currentTimeMillis();
	private final Timer flushTimer = new Timer();
	private TimerTask flushTask = new TimerTask() {
		@Override
		public void run() {
			flush();
		}
	};

	public ObservableStringLineOutputStream(Consumer<String> observer) {
		this.observer = observer;
	}

	@Override
	public synchronized void write(int b) {
		lineBuilder.append((char) b);
		if ((char) b == '\n') {
			resultStringBuilder.append(lineBuilder);
			lineBuilder.delete(0, lineBuilder.length());
			scheduleFlush();
		}
	}

	private void scheduleFlush() {
		if (System.currentTimeMillis() - lastFlush > 200) {
			flushTask.cancel();
			flush();
		} else {
			flushTimer.schedule(buildFlushTask(), 200);
		}
	}

	@Override
	public void flush() {
		if (!resultStringBuilder.isEmpty()) {
			lastFlush = System.currentTimeMillis();
			observer.accept(resultStringBuilder.toString());
			resultStringBuilder.delete(0, resultStringBuilder.length());
		}
	}

	private TimerTask buildFlushTask() {
		flushTask = new TimerTask() {
			@Override
			public void run() {
				flush();
			}
		};
		return flushTask;
	}
}
