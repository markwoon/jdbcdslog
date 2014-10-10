package org.jdbcdslog;

import static org.jdbcdslog.Loggers.connectionLogger;
import static org.jdbcdslog.ProxyUtils.*;

import java.lang.reflect.Method;

import org.slf4j.Logger;

public class ConnectionLoggingHandler extends StatementLoggingHandlerTemplate {
    public ConnectionLoggingHandler(Object target) {
        super(target);
    }

    @Override
    protected Logger getLogger() {
        return connectionLogger;
    }

    @Override
    protected Object doAfterInvoke(Object proxy, Method method, Object[] args, Object result) {
        Object r = result;

        if (UNWRAP_METHOD_NAME.equals(method.getName())) {
            Class<?> unwrapClass = (Class<?>) args[0];
            if (r == target && unwrapClass.isInstance(proxy)) {
                r = proxy;      // returning original proxy if it is enough to represent the unwrapped obj
            } else {
                r = wrapByConnectionProxy(r);
            }
        } else if (method.getName().equals("createStatement")) {
            r = wrapByStatementProxy(r);
        } else if (method.getName().equals("prepareCall")) {
            r = wrapByCallableStatementProxy(r, (String) args[0]);
        } else if (method.getName().equals("prepareStatement")) {
            r = wrapByPreparedStatementProxy(r, (String) args[0]);
        } else {
            r = wrap(r);
        }
        return r;
    }
}
