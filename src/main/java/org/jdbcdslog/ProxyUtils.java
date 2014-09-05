/*
 *  ProxyUtils.java
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.PooledConnection;
import javax.sql.XAConnection;

/**
 * @author a511990
 */
public class ProxyUtils {
    private static Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    /**
     * Find out all interfaces from clazz that is compatible with requiredInterface,
     * and generate proxy base on that.
     *
     * @param clazz
     * @param requiredInterface
     * @param invocationHandler
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T proxyForCompatibleInterfaces(Class<?> clazz, Class<T> requiredInterface, InvocationHandler invocationHandler) {
        // TODO: can cache clazz+iface vs compatibleInterfaces to avoid repetitive lookup
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(),
                                        findCompatibleInterfaces(clazz, requiredInterface),
                                        invocationHandler);
    }

    /**
     * Find all interfaces of clazz that is-a requiredInterface.
     *
     * @param clazz
     * @param requiredInterface
     * @return
     */
    public static Class<?>[] findCompatibleInterfaces(Class<?> clazz, Class<?> requiredInterface) {
        ArrayList<Class<?>> interfaces = new ArrayList<Class<?>>();
        for ( ; ! clazz.equals(Object.class) ; clazz = clazz.getSuperclass()) {
            for (Class<?> iface : clazz.getInterfaces()) {
                if (requiredInterface.isAssignableFrom(iface)) {
                    interfaces.add(iface);
                }
            }
        }
        return interfaces.toArray(EMPTY_CLASS_ARRAY);
    }


    public static Statement wrapByStatementProxy(Object r) {
        return ProxyUtils.proxyForCompatibleInterfaces(r.getClass(), Statement.class, new StatementLoggingHandler((Statement) r));
    }

    public static PreparedStatement wrapByPreparedStatementProxy(Object r, String sql) {
        return ProxyUtils.proxyForCompatibleInterfaces(r.getClass(), PreparedStatement.class, new PreparedStatementLoggingHandler((PreparedStatement) r, sql));
    }

    public static CallableStatement wrapByCallableStatementProxy(Object r, String sql) {
        return ProxyUtils.proxyForCompatibleInterfaces(r.getClass(), CallableStatement.class, new CallableStatementLoggingHandler((CallableStatement) r, sql));
    }

    public static Connection wrapByConnectionProxy(Object r) {
        return ProxyUtils.proxyForCompatibleInterfaces(r.getClass(), Connection.class, new ConnectionLoggingHandler(r));
    }

    public static ResultSet wrapByResultSetProxy(ResultSet r) {
        return ProxyUtils.proxyForCompatibleInterfaces(r.getClass(), ResultSet.class, new ResultSetLoggingHandler(r));
    }

    public static XAConnection wrapByXaConnection(XAConnection con) {
        return ProxyUtils.proxyForCompatibleInterfaces(con.getClass(), XAConnection.class, new ConnectionSourceLoggingHandler(con));
    }

    public static PooledConnection wrapByPooledConnection(PooledConnection con) {
        return ProxyUtils.proxyForCompatibleInterfaces(con.getClass(), PooledConnection.class, new ConnectionSourceLoggingHandler(con));
    }

    public static Object wrapByConnectionSourceProxy(Object r, Class<?> interf) {
        return ProxyUtils.proxyForCompatibleInterfaces(r.getClass(), interf, new ConnectionSourceLoggingHandler(r));
    }


    /**
     * Convenient helper to wrap object base on its type.
     *
     * @param r
     * @param args
     * @return
     * @throws Exception
     */
    public static Object wrap(Object r, Object...args) throws Exception {
        if (r instanceof Connection) {
            return wrapByConnectionProxy(r);
        } else if (r instanceof CallableStatement) {
            return wrapByCallableStatementProxy(r, (String) args[0]);
        } else if (r instanceof PreparedStatement) {
            return wrapByPreparedStatementProxy(r, (String) args[0]);
        } else if (r instanceof Statement) {
            return wrapByStatementProxy(r);
        } else if (r instanceof ResultSet) {
            return wrapByResultSetProxy((ResultSet) r);
        } else {
            return r;
        }
    }


}
