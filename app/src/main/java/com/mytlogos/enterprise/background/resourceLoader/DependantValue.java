package com.mytlogos.enterprise.background.resourceLoader;

import java.util.Objects;

public class DependantValue {
    private final Object value;
    private final Runnable runnable;
    private final int intId;
    private final String stringId;
    private final NetworkLoader<Integer> integerLoader;
    private final NetworkLoader<String> stringLoader;

    public DependantValue(Object value, Runnable runnable) {
        this(value, runnable, 0, null);
    }

    public DependantValue(Object value, int intId, NetworkLoader<Integer> integerLoader) {
        this(value, null, intId, integerLoader, null, null);
    }

    public DependantValue(Object value, String stringId, NetworkLoader<String> stringLoader) {
        this(value, null, 0, null, stringId, stringLoader);
    }

    public DependantValue(Object value) {
        this(value, null, 0, null, null, null);
    }

    public DependantValue(Object value, Runnable runnable, int intId, NetworkLoader<Integer> integerLoader) {
        this(value, runnable, intId, integerLoader, null, null);
    }

    public DependantValue(Object value, Runnable runnable, String stringId, NetworkLoader<String> stringLoader) {
        this(value, runnable, 0, null, stringId, stringLoader);
    }

    private DependantValue(Object value, Runnable runnable, int intId, NetworkLoader<Integer> integerLoader, String stringId, NetworkLoader<String> stringLoader) {
        this.value = value;
        this.runnable = runnable;
        this.intId = intId;
        this.integerLoader = integerLoader;
        this.stringId = stringId;
        this.stringLoader = stringLoader;
    }

    DependantValue(int intId, NetworkLoader<Integer> integerLoader) {
        this(null, null, intId, integerLoader, null, null);
    }

    DependantValue(int intId) {
        this(null, null, intId, null, null, null);
    }

    DependantValue(String stringId, NetworkLoader<String> stringLoader) {
        this(null, null, 0, null, stringId, stringLoader);
    }

    DependantValue(String stringId) {
        this(null, null, 0, null, stringId, null);
    }

    NetworkLoader<Integer> getIntegerLoader() {
        return integerLoader;
    }

    NetworkLoader<String> getStringLoader() {
        return stringLoader;
    }

    Object getValue() {
        return value;
    }

    Runnable getRunnable() {
        return runnable;
    }

    int getIntId() {
        return intId;
    }

    String getStringId() {
        return stringId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DependantValue that = (DependantValue) o;

        if (intId != that.intId) return false;
        if (!Objects.equals(value, that.value)) return false;
        return Objects.equals(stringId, that.stringId);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + intId;
        result = 31 * result + (stringId != null ? stringId.hashCode() : 0);
        return result;
    }
}
