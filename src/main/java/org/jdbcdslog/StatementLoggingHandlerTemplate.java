package org.jdbcdslog;

import static org.jdbcdslog.LogUtils.*;
import static org.jdbcdslog.Loggers.*;
import static org.jdbcdslog.ProxyUtils.wrap;

import java.lang.reflect.Method;

import org.slf4j.Logger;

/**
 * Template for statement logging handler, which includes slow query handling, and
 * before/after statement logging
 *
 * @author a511990
 */
public abstract class StatementLoggingHandlerTemplate extends LoggingHandlerSupport {

    public StatementLoggingHandlerTemplate(Object target) {
        super(target);
    }


    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        try {
            boolean needsLog = needsLogging(proxy, method, args);
            long startTimeInNano = 0;
            StringBuilder sb= null;

            doBeforeInvoke(proxy, method, args);

            if (needsLog) {
                startTimeInNano = System.nanoTime();

                sb = new StringBuilder("START: ");      // Reserve space for START: and END:
                sb.append(method.getDeclaringClass().getName()).append(".").append(method.getName()).append(": ");
                prepareLogMessage(sb, proxy, method, args);
                appendStackTrace(sb);

                logBeforeInvoke(proxy, method, args, sb);
            }

            Object result = method.invoke(target, args);

            result = doAfterInvoke(proxy, method, args, result);

            if (needsLog) {
                long elapsedTimeInNano = System.nanoTime() - startTimeInNano;
                sb.setCharAt(0, 'E');
                sb.setCharAt(1, 'N');
                sb.setCharAt(2, 'D');
                sb.setCharAt(3, ':');
                sb.setCharAt(4, ' ');
                sb.setCharAt(5, ' ');
                sb.setCharAt(6, ' ');

                appendElapsedTime(sb, elapsedTimeInNano);

                logAfterInvoke(proxy, method, args, result, elapsedTimeInNano, sb);
            }
            return result;

        } catch (Throwable t) {
            handleException(t, proxy, method, args);
        }
        return null;
    }

    protected void prepareLogMessage(StringBuilder sb, Object proxy, Method method, Object[] args) {
        // do nothing
    }

    protected boolean needsLogging(Object proxy, Method method, Object[] args) {
        return false;
    }

    protected void doBeforeInvoke(Object proxy, Method method, Object[] args) {
    }

    protected void logBeforeInvoke(Object proxy, Method method, Object[] args, StringBuilder sb) {
        if (ConfigurationParameters.logBeforeStatement) {
            getLogger().info(sb.toString());
        }
    }

    protected Object doAfterInvoke(Object proxy, Method method, Object[] args, Object result) {
        return wrap(result);
    }

    protected void logAfterInvoke(Object proxy, Method method, Object[] args, Object result, long elapsedTimeInNano, StringBuilder sb) {

        StringBuilder endMessage = sb;
        if ( ! ConfigurationParameters.logDetailAfterStatement) {
            // replace the log message to a simple message

            endMessage = new StringBuilder("END:    ")
                        .append(method.getDeclaringClass().getName()).append(".").append(method.getName())
                        .append(": ");
            appendStackTrace(endMessage);
            appendElapsedTime(endMessage, elapsedTimeInNano);

        }

        getLogger().info(endMessage.toString());

        if (elapsedTimeInNano/1000000 >= ConfigurationParameters.slowQueryThreshold) {
            getSlowQueryLogger().info(endMessage.toString());
        }

    }

    protected boolean needsSlowOperationLogging(Object proxy, Method method, Object[] args, Object result, long elapsedTimeInNano) {
        return true;
    }

    protected void handleException(Throwable t, Object proxy, Method method, Object[] args) throws Throwable {
        LogUtils.handleException(t, getLogger(), LogUtils.createLogEntry(method, null, null, null));
    }

    protected Logger getLogger() {
        return statementLogger;
    }
    protected Logger getSlowQueryLogger() {
        return slowQueryLogger;
    }

}
