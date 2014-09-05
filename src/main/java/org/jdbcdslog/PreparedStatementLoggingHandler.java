package org.jdbcdslog;

import static org.jdbcdslog.Loggers.statementLogger;
import static org.jdbcdslog.Loggers.slowQueryLogger;
import static org.jdbcdslog.ProxyUtils.*;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class PreparedStatementLoggingHandler extends LoggingHandlerSupport {
    protected TreeMap<Integer, Object> parameters = new TreeMap<Integer, Object>();

    protected String sql = null;

    protected static final Set<String> SET_METHODS
            = new HashSet<String>(Arrays.asList("setAsciiStream", "setBigDecimal", "setBinaryStream", "setBoolean", "setByte",
                                                "setBytes", "setCharacterStream", "setDate", "setDouble", "setFloat",
                                                "setInt", "setLong", "setObject", "setShort", "setString",
                                                "setTime", "setTimestamp", "setURL" ));

    protected static final Set<String> EXECUTE_METHODS
            = new HashSet<String>(Arrays.asList("addBatch", "execute", "executeQuery", "executeUpdate" ));

    public PreparedStatementLoggingHandler(PreparedStatement ps, String sql) {
        super(ps);
        this.sql = sql;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = null;
        try {
            long t1 = 0;
            boolean toLog = (statementLogger.isInfoEnabled() || slowQueryLogger.isInfoEnabled()) && EXECUTE_METHODS.contains(method.getName());
            if (toLog) {
                t1 = System.nanoTime();
            }
            r = method.invoke(target, args);

            if (UNWRAP_METHOD_NAME.equals(method.getName())) {
                Class<?> unwrapClass = (Class<?>)args[0];
                if (r == target && unwrapClass.isInstance(proxy)) {
                    r = proxy;      // returning original proxy if it is enough to represent the unwrapped obj
                } else {
                    r = wrapByPreparedStatementProxy(r, sql);
                }
            }

            if (SET_METHODS.contains(method.getName()) && args[0] instanceof Integer) {
                parameters.put((Integer)args[0], args[1]);
            }

            if ("clearParameters".equals(method.getName())) {
                parameters.clear();
            }

            if (toLog) {
                StringBuilder sb = LogUtils.createLogEntry(method, sql, parameters, null);

                long t2 = System.nanoTime();
                long time = t2 - t1;

                LogUtils.appendStackTrace(sb);
                LogUtils.appendElapsedTime(sb, time);

                statementLogger.info(sb.toString());

                if (time/1000000 >= ConfigurationParameters.slowQueryThreshold) {
                    slowQueryLogger.info(sb.toString());
                }
            }
            if (r instanceof ResultSet)
                r = wrapByResultSetProxy((ResultSet) r);
        } catch (Throwable t) {
            LogUtils.handleException(t, statementLogger, LogUtils.createLogEntry(method, sql, parameters, null));
        }
        return r;
    }
}
