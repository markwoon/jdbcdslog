package org.jdbcdslog;

import static org.jdbcdslog.Loggers.resultSetLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class ResultSetLoggingHandler implements InvocationHandler {
    private ResultSet target = null;
    private int resultCount = 0;
    private long totalFetchTime = 0;

    public ResultSetLoggingHandler(ResultSet target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = null;
        long t1 = System.nanoTime();

        try {
            r = method.invoke(target, args);
        } catch (Throwable e) {
            LogUtils.handleException(e, resultSetLogger, LogUtils.createLogEntry(method, null, null, null));
        }
        if (resultSetLogger.isInfoEnabled() && method.getName().equals("next")) {
            long t2 = System.nanoTime();
            long time = t2 - t1;
            totalFetchTime += time;

            if ((Boolean) r ) {
                ++resultCount;
                if (resultSetLogger.isDebugEnabled()) {

                    ResultSetMetaData md = target.getMetaData();
                    StringBuilder sb = new StringBuilder(method.getDeclaringClass().getName()).append(".").append(method.getName()).append(": ");

                    sb.append(" {");
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        if ( i > 1) {
                            sb.append(", ");
                        }
                        sb.append(ConfigurationParameters.rdbmsSpecifics.formatParameter(target.getObject(i)));
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

    static Object wrapByResultSetProxy(ResultSet r) {
        return Proxy.newProxyInstance(r.getClass().getClassLoader(), new Class[] { ResultSet.class }, new ResultSetLoggingHandler(r));
    }

}
