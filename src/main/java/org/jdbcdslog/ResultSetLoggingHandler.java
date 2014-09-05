package org.jdbcdslog;

import static org.jdbcdslog.Loggers.resultSetLogger;
import static org.jdbcdslog.ProxyUtils.*;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class ResultSetLoggingHandler extends LoggingHandlerSupport {
    private ResultSet targetResultSet = null;
    private int resultCount = 0;
    private long totalFetchTime = 0;

    public ResultSetLoggingHandler(ResultSet target) {
        super(target);
        this.targetResultSet = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = null;
        long t1 = System.nanoTime();

        try {
            r = method.invoke(targetResultSet, args);
        } catch (Throwable e) {
            LogUtils.handleException(e, resultSetLogger, LogUtils.createLogEntry(method, null, null, null));
        }

        if (UNWRAP_METHOD_NAME.equals(method.getName())) {
            Class<?> unwrapClass = (Class<?>)args[0];
            if (r == target && unwrapClass.isInstance(proxy)) {
                r = proxy;      // returning original proxy if it is enough to represent the unwrapped obj
            } else {
                r = wrapByResultSetProxy(targetResultSet);
            }
        }

        if (resultSetLogger.isInfoEnabled() && method.getName().equals("next")) {
            long t2 = System.nanoTime();
            long time = t2 - t1;
            totalFetchTime += time;

            if ((Boolean) r ) {
                ++resultCount;
                if (resultSetLogger.isDebugEnabled()) {

                    ResultSetMetaData md = targetResultSet.getMetaData();
                    StringBuilder sb = new StringBuilder(method.getDeclaringClass().getName()).append(".").append(method.getName()).append(": ");

                    sb.append(" {");
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        if ( i > 1) {
                            sb.append(", ");
                        }
                        sb.append(ConfigurationParameters.rdbmsSpecifics.formatParameter(targetResultSet.getObject(i)));
                    }
                    sb.append("} Row Number: ").append(resultCount);

                    LogUtils.appendStackTrace(sb);
                    LogUtils.appendElapsedTime(sb, time);

                    resultSetLogger.debug(sb.toString());
                }

            } else {

                StringBuilder sb = new StringBuilder(method.getDeclaringClass().getName()).append(".").append(method.getName()).append(": ")
                                        .append(" Total Results: ").append(resultCount)
                                        .append(".  Total fetch time: ").append(String.format("%.9f", totalFetchTime/1000000000.0)).append(" s.");
                totalFetchTime = 0;
                LogUtils.appendStackTrace(sb);
                LogUtils.appendElapsedTime(sb, time);

                resultSetLogger.info(sb.toString());
            }

        }
        return r;
    }

}
