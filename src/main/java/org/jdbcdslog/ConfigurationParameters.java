package org.jdbcdslog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationParameters {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationParameters.class);
    private static Properties props;

    static long slowQueryThreshold = Long.MAX_VALUE;
    static boolean logText = false;
    static Boolean showTime = false;
    static boolean printStackTrace = false;
    static boolean printFullStackTrace = false;
    static String printStackTracePattern = null;
    static boolean inlineQueryParams = true;
    static RdbmsSpecifics rdbmsSpecifics = new OracleRdbmsSpecifics(); // oracle is default db.
    static boolean logBeforeStatement = false;
    static boolean logDetailAfterStatement = true;

    static {
        ClassLoader loader = ConfigurationParameters.class.getClassLoader();
        InputStream in = null;
        try {
            in = loader.getResourceAsStream("jdbcdslog.properties");
            props = new Properties(System.getProperties());
            if (in != null){
                props.load(in);
            }

            initSlowQueryThreshold();
            initLogText();
            initPrintStackTrace();
            initPrintFullStackTrace();
            initPrintStackTracePattern();
            initShowTime();
            initInlineQueryParams();
            initRdbmsSpecifics();
            initLogBeforeStatement();
            initLogDetailAfterStatement();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }
    }

    /* init parameters start. */
    private static void initSlowQueryThreshold() {
        String sSlowQueryThreshold = props.getProperty("jdbcdslog.slowQueryThreshold");
        if (sSlowQueryThreshold != null && isLong(sSlowQueryThreshold)) {
            slowQueryThreshold = Long.parseLong(sSlowQueryThreshold);
        }
        if (slowQueryThreshold == -1) {
            slowQueryThreshold = Long.MAX_VALUE;
        }
    }

    private static void initLogText() {
        logText = "true".equalsIgnoreCase(props.getProperty("jdbcdslog.logText", "false"));
    }

    private static void initPrintStackTrace() {
        printStackTrace = "true".equalsIgnoreCase(props.getProperty("jdbcdslog.printStackTrace", "false"));
    }

    private static void initPrintFullStackTrace() {
        printFullStackTrace = "true".equalsIgnoreCase(props.getProperty("jdbcdslog.printFullStackTrace", "false"));
    }

    private static void initPrintStackTracePattern() {
        printStackTracePattern = props.getProperty("jdbcdslog.printStackTracePattern", "");
    }


    private static void initShowTime() {
        showTime = "true".equalsIgnoreCase(props.getProperty("jdbcdslog.showTime", "false"));
    }

    private static void initInlineQueryParams() {
        inlineQueryParams = ("true".equalsIgnoreCase(props.getProperty("jdbcdslog.inlineQueryParams", "true"))) ;
    }

    private static void initRdbmsSpecifics() {
        String driverName = props.getProperty("jdbcdslog.driverName");
        if ("oracle".equalsIgnoreCase(driverName)) {
            // no op. since = default db. and skip next if statement,maybe better.
        } else if ("mysql".equalsIgnoreCase(driverName)) {
            rdbmsSpecifics = new MySqlRdbmsSpecifics();
        } else if ("sqlserver".equalsIgnoreCase(driverName)) {
            rdbmsSpecifics = new SqlServerRdbmsSpecifics();
        }
    }


    private static void initLogBeforeStatement() {
        logBeforeStatement = "true".equalsIgnoreCase(props.getProperty("jdbcdslog.logBeforeStatement", "false"));
    }

    private static void initLogDetailAfterStatement() {
        if ( ! logBeforeStatement) {
            logDetailAfterStatement = true;
        } else {
            logDetailAfterStatement = "true".equalsIgnoreCase(props.getProperty("jdbcdslog.logDetailAfterStatement", "true"));
        }
    }


    /* init parameters end. */

    public static void setLogText(boolean alogText) {
        logText = alogText;
    }

    private static boolean isLong(String sSlowQueryThreshold) {
        try {
            Long.parseLong(sSlowQueryThreshold);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
