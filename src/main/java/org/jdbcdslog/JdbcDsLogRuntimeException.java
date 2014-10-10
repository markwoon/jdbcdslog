package org.jdbcdslog;

public class JdbcDsLogRuntimeException extends Exception {
    private static final long serialVersionUID = 2791270426551839139L;

    public JdbcDsLogRuntimeException(String s) {
        super(s);
    }

    public JdbcDsLogRuntimeException(Throwable e) {
        super(e);
    }
}
