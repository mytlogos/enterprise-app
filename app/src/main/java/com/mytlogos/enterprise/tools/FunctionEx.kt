package com.mytlogos.enterprise.tools;

public interface FunctionEx<T, R> {
    R apply(T t) throws Exception;
}
