package io.jona.framework;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Table {

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    private static final AtomicLong idGenerator = new AtomicLong(System.currentTimeMillis());
    protected long nextId() {
        return idGenerator.getAndIncrement();
    }
    protected abstract String getInsert();
    protected abstract String getUpdate();
    protected abstract String getDelete();
    protected abstract Object[] getValues();
}
