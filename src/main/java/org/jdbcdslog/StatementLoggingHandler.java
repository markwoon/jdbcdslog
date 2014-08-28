package org.jdbcdslog;

import static org.jdbcdslog.Loggers.statementLogger;
import static org.jdbcdslog.Loggers.slowQueryLogger;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class StatementLoggingHandler implements InvocationHandler {
    protected final static Set<String> EXECUTE_METHODS = new HashSet<String>(Arrays.asList("addBatch", "execute", "executeQuery", "executeUpdate"));

    protected Object targetStatement = null;

    public StatementLoggingHandler(Statement statement) {
        targetStatement = statement;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = null;
        try {
            boolean toLog = (statementLogger.isInfoEnabled() || slowQueryLogger.isInfoEnabled()) && EXECUTE_METHODS.contains(method.getName());
            long t1 = 0;
            if (toLog)
                t1 = System.nanoTime();
            r = method.invoke(targetStatement, args);
            if (r instanceof ResultSet)
                r = ResultSetLoggingHandler.wrapByResultSetProxy((ResultSet) r);
            if (toLog) {
                long t2 = System.nanoTime();
                long time = t2 - t1;

                StringBuilder sb = LogUtils.createLogEntry(method, args == null ? null : args[0].toString(), null, null);

                LogUtils.appendStackTrace(sb);
                LogUtils.appendElapsedTime(sb, time);

                statementLogger.info(sb.toString());

                if (time/1000000 >= ConfigurationParameters.slowQueryThreshold) {
                    slowQueryLogger.info(sb.toString());
                }

            }
        } catch (Throwable t) {
            LogUtils.handleException(t, statementLogger, LogUtils.createLogEntry(method, args[0].toString(), null, null));
        }
        return r;
    }

}
