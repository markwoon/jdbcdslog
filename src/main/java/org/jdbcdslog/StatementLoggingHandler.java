package org.jdbcdslog;

import static org.jdbcdslog.Loggers.statementLogger;
import static org.jdbcdslog.Loggers.slowQueryLogger;
import static org.jdbcdslog.ProxyUtils.*;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class StatementLoggingHandler extends LoggingHandlerSupport {
    protected final static Set<String> EXECUTE_METHODS = new HashSet<String>(Arrays.asList("addBatch", "execute", "executeQuery", "executeUpdate"));

    public StatementLoggingHandler(Statement statement) {
        super(statement);
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

            if (UNWRAP_METHOD_NAME.equals(method.getName())) {
                Class<?> unwrapClass = (Class<?>)args[0];
                if (r == target && unwrapClass.isInstance(proxy)) {
                    r = proxy;      // returning original proxy if it is enough to represent the unwrapped obj
                } else {
                    r = wrapByStatementProxy(r);
                }
            }

            if (r instanceof ResultSet) {
                r = wrapByResultSetProxy((ResultSet) r);
            }
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
