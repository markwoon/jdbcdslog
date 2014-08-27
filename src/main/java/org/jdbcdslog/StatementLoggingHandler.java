package org.jdbcdslog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StatementLoggingHandler implements InvocationHandler {
    protected Object targetStatement = null;

    protected final static Set<String> EXECUTE_METHODS = new HashSet<String>(Arrays.asList("addBatch", "execute", "executeQuery", "executeUpdate"));

    public StatementLoggingHandler(Statement statement) {
        targetStatement = statement;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = null;
        try {
            boolean toLog = (StatementLogger.isInfoEnabled() || SlowQueryLogger.isInfoEnabled()) && EXECUTE_METHODS.contains(method.getName());
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

                LogUtils.appendElapsedTime(sb, time);

                StatementLogger.info(sb.toString());

                if (time/1000000 >= ConfigurationParameters.slowQueryThreshold) {
                    SlowQueryLogger.info(sb.toString());
                }

            }
        } catch (Throwable t) {
            LogUtils.handleException(t, StatementLogger.getLogger(), LogUtils.createLogEntry(method, args[0].toString(), null, null));
        }
        return r;
    }

}
