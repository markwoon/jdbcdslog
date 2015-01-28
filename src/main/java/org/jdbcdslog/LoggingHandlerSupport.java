package org.jdbcdslog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Base class for JDBC DS Log logging handlers.
 *
 * @author a511990
 */
public abstract class LoggingHandlerSupport<T> implements InvocationHandler {
    protected final static String UNWRAP_METHOD_NAME = "unwrap";

    protected T target = null;

    public LoggingHandlerSupport(T target) {
        this.target = target;
    }

    public abstract Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
