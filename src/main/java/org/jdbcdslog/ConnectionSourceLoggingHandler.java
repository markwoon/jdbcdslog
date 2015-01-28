/*
 *  ConnectionSourceLoggingHandler.java
 *
 *  $id$
 *
 * Copyright (C) FIL Limited. All rights reserved
 *
 * This software is the confidential and proprietary information of
 * FIL Limited You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into with FIL Limited.
 */

package org.jdbcdslog;

import static org.jdbcdslog.Loggers.connectionLogger;
import static org.jdbcdslog.ProxyUtils.wrap;

import java.lang.reflect.Method;

/**
 * Logging handler for objects that can directly or indirectly create Connection from.  For example,
 * DataSource, PooledConnection.
 *
 * @author a511990
 */
public class ConnectionSourceLoggingHandler extends LoggingHandlerSupport<Object> {
    public ConnectionSourceLoggingHandler(Object target) {
        super(target);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        try {
            Object r = method.invoke(target, args);

            return wrap(null, r);

        } catch (Throwable t) {
            LogUtils.handleException(t, connectionLogger, LogUtils.createLogEntry(method, null, null, null));
        }
        return null;
    }

}
