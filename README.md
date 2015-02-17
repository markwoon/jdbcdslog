# What is JdbcDsLog-Exp2
`jdbcdslogexp2` is an fork of [jdbcdslog-exp] that, on top of the features provided by `jdbcdslog-exp`, provides:

* Support vendor-extended JDBC interfaces, so that when calling `unwrap()`, a vendor-interface-proxied object will still be returned, which will allow you to use vendor specific feature, and yet still being logged with JDBC DS Log messages.
* Allow you to choose from inline-parameter-style (as in `jdbcdslog-exp`) or separate-parameter-style (similar to original `jdbcdslog`) in query log.
* More flexible and useful stack trace printing, which allow you to
  * print full stack trace.
  * print first level outside of JDBC DS log call stack (Default behavior).
  * print first level matching a pattern you provide.  This is especially useful if your DB access is going through some framework like Hibernate.  Then you can show the first frame in call stack of your own package.
  * (All above options excludes stack frames of JDBC DS Log)
* Better ResultSet logging
  * Individual row is logged in debug level, with row number.
  * Call to ResultSet#next() after the last row will trigger a summary log message which contains total fetch time and total number of records.
* Can log before and after statement invocations.  Statement logging can be turned off in "after" logging.
* Better handling in batch-operations.
  * `executeBatch()` will now be logged.  You can, optionally, log all statements in batch.
  * `addBatch()` can be logged with less detail or excluded from logging completely.
* Dropped pre-JDK5 support and source code is now cleaner with proper use of JDK5+ features like Generics, for-each loop etc.
* Upgrade to later SLF4J version
* Cleanup logging codes for readability and reduce unnecessary string constructions.
* Provides Connection ID in MDC so user can (optionally) distinguish logs originated from different connectiosn, which is especially useful for application connecting to multiple DBs. (version 2.1)
* Fix misc bugs and problems in original codebase. (Of course, the original function does not change!)

if you want get more information please see the original version of jdbcdslog at <http://code.google.com/p/jdbcdslog/>

# Maven Dependency

    <dependency>
        <groupId>org.jdbcdslog</groupId>
        <artifactId>jdbcdslogexp2</artifactId>
        <version>2.1</version>
    </dependency>




# How To Use
Please see user guide: [JDBC DS Log Exp2 User Guide](https://github.com/adrianshum/jdbcdslog/wiki/User-Guide) for JDBC DS Log Exp2 specific features.

For original user guides, please refer to [Origin JdbcDsLog UserGuide](http://code.google.com/p/jdbcdslog/wiki/UserGuide ) and [JdbcDsLog-exp UserGuide](http://code.google.com/p/jdbcdslog-exp/wiki/UserGuide).

# Support
if you have any questions,welcome to contact me via Email(adrianshum at gmail dot com) or create issues with <https://github.com/adrianshum/jdbcdslog/issues>, I will reply you as soon as possible.
