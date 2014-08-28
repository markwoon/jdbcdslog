###What is jdbcdslog-exp2
jdbcdslog-exp is an fork of [jdbcdslog-exp] that, on top of the features provided by jdbcdslog-exp, provides:

* Support vendor-extended JDBC interfaces, so that when calling `unwrap()`, a vendor-interface-proxied object will still be returned, which will allow you to use vendor specific feature, and yet still being logged with JDBC DS Log messages (*_In progress_*)
* Allow you to choose from inline-parameter-style (as in jdbcdslog-exp) or separate-parameter-style (similar to original jdbcdslog) in query log
* More flexible and useful stack trace printing, which allow you to
  * print full stack trace
  * print first level outside of JDBC DS log call stack
  * print first level matching a pattern you provide.  This is especially useful if your DB access is going through some framework like Hibernate.  Then you can show the first frame in call stack of your own package.
  * (All above options is excluding stack trace frame of JDBC DS Log)
* Better ResultSet logging
  * Individual row is logged in debug level, with row number
  * Call to ResultSet#next() after the last row will trigger a summary log message which contains total fetch time and total number of records.
* Dropped pre-JDK5 support and source code is now cleaner with proper use of JDK5+ features like Generics, for-each loop etc.
* Upgrade to latest SLF4J version, and cleanup of logging codes to be more readable and reduce unnecessary
* Fix misc bugs and problems in original codebase
(Of course, the original function does not change!)

if you want get more infos please see the original version of jdbcdslog at <http://code.google.com/p/jdbcdslog/>

### Maven Dependency
    <dependency>
        <groupId>jdbcdslog-exp2</groupId>
        <artifactId>jdbcdslog-exp2</artifactId>
        <version>2.0-SNAPSHOT</version>
    </dependency>



if you like the __native version__ ([jdbcdslog](http://code.google.com/p/jdbcdslog)) and use maven, please change the version to __1.0.5.1__, i post it into maven central repo to support for maven.

### SVN Link
if you love svn please via [jdbcdslog-exp in googlecode](http://code.google.com/p/jdbcdslog-exp/) or checkout

    svn checkout http://jdbcdslog-exp.googlecode.com/svn/trunk/ jdbcdslog-exp-read-only

### How To Use
please see user guide: [Origin JdbcDsLog UserGuide](http://code.google.com/p/jdbcdslog/wiki/UserGuide ) and [JdbcDsLog-exp UserGuide](http://code.google.com/p/jdbcdslog-exp/wiki/UserGuide)

### Support
if you have any questions,welcome to contact me via Email(adrianshum@gmail.com) or create issues with <https://github.com/adrianshum/jdbcdslog/issues>, I will reply you as soon as possible.
