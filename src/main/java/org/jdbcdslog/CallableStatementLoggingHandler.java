package org.jdbcdslog;

import static org.jdbcdslog.Loggers.statementLogger;
import static org.jdbcdslog.Loggers.slowQueryLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.TreeMap;

public class CallableStatementLoggingHandler extends PreparedStatementLoggingHandler implements InvocationHandler {

    protected TreeMap<String, Object> namedParameters = new TreeMap<String, Object>();

    public CallableStatementLoggingHandler(CallableStatement ps, String sql) {
        super(ps, sql);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = null;
        try {
            boolean toLog = (statementLogger.isInfoEnabled() || slowQueryLogger.isInfoEnabled()) && EXECUTE_METHODS.contains(method.getName());
            long t1 = 0;
            if (toLog) {
                t1 = System.nanoTime();
            }

            r = method.invoke(target, args);

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
            if (toLog) {
                long t2 = System.nanoTime();
                long time = t2 - t1;

                StringBuilder sb = LogUtils.createLogEntry(method, sql, parameters, namedParameters);

                LogUtils.appendStackTrace(sb);
                LogUtils.appendElapsedTime(sb, time);

                statementLogger.info(sb.toString());

                if (time/1000000 >= ConfigurationParameters.slowQueryThreshold) {
                    slowQueryLogger.info(sb.toString());
                }
            }
            if (r instanceof ResultSet) {
                r = ResultSetLoggingHandler.wrapByResultSetProxy((ResultSet) r);
            }
        } catch (Throwable t) {
            LogUtils.handleException(t, statementLogger, LogUtils.createLogEntry(method, sql, parameters, namedParameters));
        }
        return r;
    }

}
