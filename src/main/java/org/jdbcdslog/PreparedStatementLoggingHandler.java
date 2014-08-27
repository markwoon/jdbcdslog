package org.jdbcdslog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class PreparedStatementLoggingHandler implements InvocationHandler {
    protected TreeMap<Integer, Object> parameters = new TreeMap<Integer, Object>();

    protected Object target = null;

    protected String sql = null;

    protected static final Set<String> SET_METHODS
            = new HashSet<String>(Arrays.asList("setAsciiStream", "setBigDecimal", "setBinaryStream", "setBoolean", "setByte",
                                                "setBytes", "setCharacterStream", "setDate", "setDouble", "setFloat",
                                                "setInt", "setLong", "setObject", "setShort", "setString",
                                                "setTime", "setTimestamp", "setURL" ));

    protected static final Set<String> EXECUTE_METHODS
            = new HashSet<String>(Arrays.asList("addBatch", "execute", "executeQuery", "executeUpdate" ));

    public PreparedStatementLoggingHandler(PreparedStatement ps, String sql) {
        target = ps;
        this.sql = sql;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = null;
        try {
            long t1 = 0;
            boolean toLog = (StatementLogger.isInfoEnabled() || SlowQueryLogger.isInfoEnabled()) && EXECUTE_METHODS.contains(method.getName());
            if (toLog) {
                t1 = System.nanoTime();
            }
            r = method.invoke(target, args);
            if (SET_METHODS.contains(method.getName()) && args[0] instanceof Integer) {
                parameters.put((Integer)args[0], args[1]);
            }

            if ("clearParameters".equals(method.getName())) {
                parameters.clear();
            }

            if (toLog) {
                StringBuffer sb = LogUtils.createLogEntry(method, sql, parameters, null);

                long t2 = System.nanoTime();
                long time = t2 - t1;

                LogUtils.appendElapsedTime(sb, time);

                StatementLogger.info(sb.toString());

                if (time/1000000 >= ConfigurationParameters.slowQueryThreshold) {
                    SlowQueryLogger.info(sb.toString());
                }
            }
            if (r instanceof ResultSet)
                r = ResultSetLoggingHandler.wrapByResultSetProxy((ResultSet) r);
        } catch (Throwable t) {
            LogUtils.handleException(t, StatementLogger.getLogger(), LogUtils.createLogEntry(method, sql, parameters, null));
        }
        return r;
    }
}
