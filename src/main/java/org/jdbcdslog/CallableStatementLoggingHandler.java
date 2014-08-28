package org.jdbcdslog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.TreeMap;

public class CallableStatementLoggingHandler extends PreparedStatementLoggingHandler implements InvocationHandler {

    protected static Logger logger = LoggerFactory.getLogger(CallableStatementLoggingHandler.class);

    protected TreeMap<String, Object> namedParameters = new TreeMap<String, Object>();

    public CallableStatementLoggingHandler(CallableStatement ps, String sql) {
        super(ps, sql);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = "invoke() ";
        logger.debug("invoke() method = {}", method);

        Object r = null;
        try {
            boolean toLog = (StatementLogger.isInfoEnabled() || SlowQueryLogger.isInfoEnabled()) && EXECUTE_METHODS.contains(method.getName());
            long t1 = 0;
            if (toLog) {
                t1 = System.nanoTime();
            }

            logger.debug(methodName + "before method call..");
            r = method.invoke(target, args);
            logger.debug(methodName + "after method call. result = {}", r);

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

                StatementLogger.info(sb.toString());

                if (time/1000000 >= ConfigurationParameters.slowQueryThreshold) {
                    SlowQueryLogger.info(sb.toString());
                }
            }
            if (r instanceof ResultSet)
                r = ResultSetLoggingHandler.wrapByResultSetProxy((ResultSet) r);
        } catch (Throwable t) {
            LogUtils.handleException(t, StatementLogger.getLogger(), LogUtils.createLogEntry(method, sql, parameters, namedParameters));
        }
        return r;
    }

}
