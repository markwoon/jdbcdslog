/*
 *  Loggers.java
 *
 *  $id$
 *
 * Copyright (C) FIL Limited. All rights reserved
 *
 * This software is the confidential and proprietary information of
 * FIL Limited You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into with FIL Limited.
 */

package org.jdbcdslog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized place for ease of access for JDBC DS Log loggers.
 *
 * @author Adrian Shum
 */
public class Loggers {
    public static final Logger slowQueryLogger = LoggerFactory.getLogger("org.jdbcdslog.SlowQueryLogger");
    public static final Logger connectionLogger = LoggerFactory.getLogger("org.jdbcdslog.ConnectionLogger");
    public static final Logger resultSetLogger = LoggerFactory.getLogger("org.jdbcdslog.ResultSetLogger");
    public static final Logger statementLogger = LoggerFactory.getLogger("org.jdbcdslog.StatementLogger");
}
