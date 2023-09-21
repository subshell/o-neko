package io.oneko.websocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * An OutputStream implementation that accepts a consumer of the stream's content. The consumer will be called when a
 * line, terminated by \n, has been written to the stream. It will be called at most once in BATCH_INTERVAL milliseconds.
 * If multiple lines have been written in a batch interval, the consumer will receive multiple lines per flush.
 */
public class ObservableStringLineOutputStream extends OutputStream {

	private static final long BATCH_INTERVAL = 200;

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
		if (System.currentTimeMillis() - lastFlush > BATCH_INTERVAL) {
			flushTask.cancel();
			flush();
		} else {
			flushTimer.schedule(buildFlushTask(), BATCH_INTERVAL);
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

	@Override
	public void close() throws IOException {
		super.close();
		flushTimer.cancel();
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
