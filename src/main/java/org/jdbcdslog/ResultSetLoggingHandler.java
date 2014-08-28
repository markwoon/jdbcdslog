package org.jdbcdslog;

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
            LogUtils.handleException(e, ResultSetLogger.getLogger(), LogUtils.createLogEntry(method, null, null, null));
        }
        if (ResultSetLogger.isInfoEnabled() && method.getName().equals("next")) {
            long t2 = System.nanoTime();
            long time = t2 - t1;
            totalFetchTime += time;

            String fullMethodName = method.getDeclaringClass().getName() + "." + method.getName();
            ResultSetMetaData md = target.getMetaData();
            StringBuilder sb = new StringBuilder(fullMethodName).append(": ");

            if ((Boolean) r) {
                sb.append(" {");
                if (md.getColumnCount() > 0) {
                    sb.append(ConfigurationParameters.rdbmsSpecifics.formatParameter(target.getObject(1)));
                }
                for (int i = 2; i <= md.getColumnCount(); i++) {
                    sb.append(", ").append(ConfigurationParameters.rdbmsSpecifics.formatParameter(target.getObject(i)));
                }
                sb.append("}")
                    .append(" Row Number: ")
                    .append(++resultCount);

            } else {
                sb.append(" Total Results: ").append(resultCount)
                        .append(".  Total fetch time: ").append(String.format("%.9f", totalFetchTime/1000000000.0)).append(" s.");
                totalFetchTime = 0;
            }

            LogUtils.appendStackTrace(sb);
            LogUtils.appendElapsedTime(sb, time);

            ResultSetLogger.info(sb.toString());
        }
        return r;
    }

    static Object wrapByResultSetProxy(ResultSet r) {
        return Proxy.newProxyInstance(r.getClass().getClassLoader(), new Class[] { ResultSet.class }, new ResultSetLoggingHandler(r));
    }

}
