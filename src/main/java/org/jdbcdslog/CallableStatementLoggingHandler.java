package org.jdbcdslog;

import static org.jdbcdslog.Loggers.*;
import static org.jdbcdslog.ProxyUtils.*;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.TreeMap;

public class CallableStatementLoggingHandler extends PreparedStatementLoggingHandler {

    protected TreeMap<String, Object> namedParameters = new TreeMap<String, Object>();

    public CallableStatementLoggingHandler(CallableStatement ps, String sql) {
        super(ps, sql);
    }

    @Override
    protected boolean needsLogging(Object proxy,Method method, Object[] args) {
        return (statementLogger.isInfoEnabled() || slowQueryLogger.isInfoEnabled())
                && EXECUTE_METHODS.contains(method.getName());
    }

    @Override
    protected void appendStatement(StringBuilder sb, Object proxy, Method method, Object[] args) {
        LogUtils.appendSql(sb, sql, parameters, namedParameters);
    }

    @Override
    protected Object doAfterInvoke(Object proxy,Method method, Object[] args, Object result) {
        Object r = result;

        if (UNWRAP_METHOD_NAME.equals(method.getName())) {
            Class<?> unwrapClass = (Class<?>)args[0];
            if (r == target && unwrapClass.isInstance(proxy)) {
                r = proxy;      // returning original proxy if it is enough to represent the unwrapped obj
            } else if (unwrapClass.isInterface() && CallableStatement.class.isAssignableFrom(unwrapClass)) {
                r = wrapByCallableStatementProxy(r, sql);
            }
        }

        if (r instanceof ResultSet) {
            r = wrapByResultSetProxy((ResultSet) r);
        }

        if (SET_METHODS.contains(method.getName())) {
             if (args[0] instanceof Integer) {
                 parameters.put((Integer)args[0], args[1]);
             } else if (args[0] instanceof String) {
                 namedParameters.put((String)args[0], args[1]);
             }
        }
        if ("clearParameters".equals(method.getName())) {
            parameters.clear();
            namedParameters.clear();
        }
        return r;
    }

    @Override
    protected void handleException(Throwable t, Object proxy, Method method, Object[] args) throws Throwable {
        LogUtils.handleException(t, statementLogger, LogUtils.createLogEntry(method, sql, parameters, namedParameters));
    }

}
