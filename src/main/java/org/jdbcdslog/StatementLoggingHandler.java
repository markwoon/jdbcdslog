package org.jdbcdslog;

import static org.jdbcdslog.Loggers.*;
import static org.jdbcdslog.ProxyUtils.*;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class StatementLoggingHandler extends StatementLoggingHandlerTemplate<Statement> {
    protected final static Set<String> EXECUTE_METHODS = new HashSet<String>(Arrays.asList("addBatch", "execute", "executeQuery", "executeUpdate", "executeBatch"));
    protected StringBuilder batchStatements = null;

    public StatementLoggingHandler(LogMetaData logMetaData, Statement statement) {
        super(logMetaData, statement);
    }

    @Override
    protected boolean needsLogging(Object proxy,Method method, Object[] args) {
        return (statementLogger.isInfoEnabled() || slowQueryLogger.isInfoEnabled())
                && EXECUTE_METHODS.contains(method.getName());
    }

    @Override
    protected void appendStatement(StringBuilder sb, Object proxy, Method method, Object[] args) {
        LogUtils.appendSql(sb, (args == null || args.length == 0) ? null : args[0].toString(), null, null);
    }

    @Override
    protected void doAddBatch(Object proxy, Method method, Object[] args) {
        if (this.batchStatements == null) {
            this.batchStatements = new StringBuilder();
        } else {
            this.batchStatements.append("\n\t");
        }
        appendStatement(batchStatements, proxy, method, args);
        this.batchStatements.append(';');

    }

    @Override
    protected void appendBatchStatements(StringBuilder sb) {
        if (this.batchStatements != null) {
            sb.append(batchStatements);
            this.batchStatements = null;
        }
    }

    @Override
    protected Object doAfterInvoke(Object proxy,Method method, Object[] args, Object result) {
        Object r = result;

        if (UNWRAP_METHOD_NAME.equals(method.getName())) {
            Class<?> unwrapClass = (Class<?>)args[0];
            if (r == target && unwrapClass.isInstance(proxy)) {
                r = proxy;      // returning original proxy if it is enough to represent the unwrapped obj
            } else if (unwrapClass.isInterface() && Statement.class.isAssignableFrom(unwrapClass)) {
                r = wrapByStatementProxy(logMetaData, (Statement)r);
            }
        }

        if (r instanceof ResultSet) {
            r = wrapByResultSetProxy(logMetaData, (ResultSet) r);
        }

        return r;
    }

    @Override
    protected void handleException(Throwable t, Object proxy, Method method, Object[] args) throws Throwable {
        LogUtils.handleException(t,
                statementLogger,
                LogUtils.createLogEntry(logMetaData, method, (args == null || args.length == 0) ? null : args[0].toString(), null, null));
    }

}
