# Java Resolver Tool

The Java Resolver tool is a tool for creating data lineage of Java application
using static analysis techniques.

The Java Resolver tool uses Symbolic Analysis library to:
- handle data flow propagation through Java application
- Identify sources and sinks of a data through JDBC and I/O APIs

The Java Resolver tool implements plugins for Symbolic Analysis library
for few frameworks/APIs (Spring JDBC, MyBatis, Kafka) to identify
data sources and sinks inside of these frameworks. 

## Requirements

- Java JDK 1.8
- Apache Maven (recommended/tested 3.3.9)
- Graphviz (recommended/tested 2.40.1)

## Build
To build runnable JAR `target/resolver.jar` run following command:

    mvn clean compile assembly:single

We strongly recommend running tests for the Java Resolver tool running:

    mvn test

As tests can run long time, we provide TestNG suites for each framework.
This can be useful to verify that requested features are working before trying to use it.
The test suite files are located in subdirectories of `test/resources/tests`
and can be run using command:

    mvn test -DtestSuite=<fileName>

## Usage
    java -jar target/resolver.jar [OPTIONS]
    
    [OPTIONS]
        [--entry <className> <methodName>]
            # Specify the entry class method of an analysed program.
            # Use fully qualified name of class.
            # Only methods without arguments and standard Java main are supported.
        [--application-jar <fileName>]
            # Specify the application JAR file to be analysed by the tool.
        [--library-jar <fileName>]
            # Specify the library JAR file to be analysed by the tool.
            # All application dependencies should be specified.
        [--application-package <package>]
            # Specify application package.
            # All classes out of that package will not be analysed because of optimizations.
        [--output-directory <directoryName>]
            # Specify the directory where the results will be stored.
        [--help]
            # Display usage and exit.

## Result Graphs
The data lineage of an input program is represented by a flow graph, where
nodes are data sources and sinks and oriented edges are between pair of nodes
between which the data flows.

Each node contains few attributes that specify some data lineage details,
like SQL statement in `db_statement_desc`, database configuration url in
`db_connection_desc` and credentials in `db_connection_user_desc`
or used stream in `stream_location_desc` (file name or "System.in"/...).

## Supported APIs and Frameworks

The Java Resolver tool supports:
- JDBC API
- I/O API
- Spring JDBC Framework
- MyBatis Framework
- Kafka Framework

First two (JDBC and I/O APIs) are supported by the usage of Symbolic Analysis library
and for the rest plugin for Symbolic Analysis library is implemented. 

## Support for other data processing frameworks

We implemented support for three data processing frameworks, but in general,
any framework can be handled. It can be done by implementing a new plugin for
Symbolic analysis library.

Before the implementation of new plugin we strongly recomend to make familiar
with already implemented plugins and other classes used in that plugins:
- `JdbcTemplateAnalysisPlugin` and `JdbcTemplateHandler` can be inspiration
 for the approach of callbacks.
- `MyBatisAnalysisPlugin` can be inspiration, when plugin should work with
external files or annotations.
    - `MyBatisAnnotationMapperSqlReader` - working with annotations
    - `MyBatisXmlMapperSqlReader` - working with external XML files