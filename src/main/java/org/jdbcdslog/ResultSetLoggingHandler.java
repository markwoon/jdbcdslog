package org.jdbcdslog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class ResultSetLoggingHandler implements InvocationHandler {
    private ResultSet target = null;
    private int resultCount = 0;

    public ResultSetLoggingHandler(ResultSet target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = null;
        long t1 = 0;
        try {
            t1 = System.nanoTime();
            r = method.invoke(target, args);
        } catch (Throwable e) {
            LogUtils.handleException(e, ResultSetLogger.getLogger(), LogUtils.createLogEntry(method, null, null, null));
        }
        if (ResultSetLogger.isInfoEnabled() && method.getName().equals("next")) {
            long t2 = System.nanoTime();
            long time = t2 - t1;

            String fullMethodName = method.getDeclaringClass().getName() + "." + method.getName();
            ResultSetMetaData md = target.getMetaData();
            StringBuffer s = new StringBuffer(fullMethodName);

            if ((Boolean) r) {
                s.append(" {");
                if (md.getColumnCount() > 0) {
                    s.append(ConfigurationParameters.rdbmsSpecifics.formatParameter(target.getObject(1)));
                }
                for (int i = 2; i <= md.getColumnCount(); i++) {
                    s.append(", ").append(ConfigurationParameters.rdbmsSpecifics.formatParameter(target.getObject(i)));
                }
                s.append("}")
                    .append(" result count : ")
                    .append(++resultCount);
            } else {
                s.append(" Total Results ").append(resultCount);
            }


            if (ConfigurationParameters.showTime) {
                s.append(" ").append(String.format("%.9f", time/1000000000.0)).append(" s.");
            }

            ResultSetLogger.info(s.toString());
        }
        return r;
    }

    static Object wrapByResultSetProxy(ResultSet r) {
        return Proxy.newProxyInstance(r.getClass().getClassLoader(), new Class[] { ResultSet.class }, new ResultSetLoggingHandler(r));
    }

}
