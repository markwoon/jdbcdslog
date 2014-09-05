package org.jdbcdslog;

import static org.jdbcdslog.Loggers.connectionLogger;
import static org.jdbcdslog.ProxyUtils.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Base class for JDBC DS Log logging handlers.
 *
 * @author a511990
 */
public class LoggingHandlerSupport implements InvocationHandler {
    protected final static String UNWRAP_METHOD_NAME = "unwrap";

    protected Object target = null;

    public LoggingHandlerSupport(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        try {
            Object r = method.invoke(target, args);

            return wrap(r);

        } catch (Throwable t) {
            LogUtils.handleException(t, connectionLogger, LogUtils.createLogEntry(method, null, null, null));
        }
        return null;
    }

}
