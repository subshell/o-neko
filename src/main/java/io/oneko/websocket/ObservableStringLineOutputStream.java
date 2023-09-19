package io.oneko.websocket;

import java.io.OutputStream;
import java.util.function.Consumer;

public class ObservableStringLineOutputStream extends OutputStream {

    private final Consumer<String> observer;
    private final StringBuilder stringBuilder = new StringBuilder();

    public ObservableStringLineOutputStream(Consumer<String> observer) {
        this.observer = observer;
    }

    @Override
    public void write(int b) {
        stringBuilder.append((char) b);
        if ((char) b == '\n') {
            flush();
        }
    }

    @Override
    public void flush() {
        if (!stringBuilder.isEmpty()) {
            observer.accept(stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());
        }
    }

}
