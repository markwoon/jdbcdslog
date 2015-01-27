package org.jdbcdslog;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log-related Meta Data for connection
 */
public class LogMetaData {
    // Currently using an integer as ID, but can be changed later with other strategy
    private static final AtomicInteger idCounter= new AtomicInteger(0);

    private String connectionId;

    public static LogMetaData create() {
        int id = idCounter.incrementAndGet();
        return new LogMetaData(String.valueOf(id));
    }

    private LogMetaData(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getConnectionId() {
        return connectionId;
    }

}
