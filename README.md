# CMP - Content Management Platform

CMP - Content Management Platform (Mainly for SMS & USSD Services)

This is a highly configurable & extensible multi telco SMS AND USSD Platform with the following. 
Curently able to handle the following telco protocols;

* [SMPP](https://en.wikipedia.org/wiki/Short_Message_Peer-to-Peer)  v. 3.4 
* [Parlay  X] (https://en.wikipedia.org/wiki/Parlay_X) v 3.1

## Key functionalities


## Prerequisites

1. [JDK version 1.7] (https://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html) for building the project
2. [JDK version 1.8] (https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) to run wildfly
3. [Wildfly (Version 10.1.0.Final)] (https://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip)
4. [JDBC Datasource connector] (https://downloads.mariadb.com/Connectors/java/connector-java-2.4.1/mariadb-java-client-2.4.1.jar) For connecting to Maria DB
5. [Preferably do this on a Linux environment] (https://itsfoss.com/linux-better-than-windows/).
6. [MariaDB - Former Mysql] (https://downloads.mariadb.org/)
7. [Maven 3.3.9] (https://maven.apache.org/docs/3.3.9/release-notes.html)


### Installing Wildfly

Install wildfly.


### Installing MariaDb JDBC driver

$WILDFLY_HOME will henceforth refer to the location of wildfly.

1. Stop wildfly (if started)
2. Create (if not existing), the following folder structure

   `$WILDFLY_HOME/modules/system/layers/base/org/mariadb/main`

3. Copy/Move the Maria JDBC driver jar file downloaded previously to the above location.

4. Create a file `module.xml` and in it the following content.



	`<?xml version="1.0" encoding="UTF-8"?>
		<module xmlns="urn:jboss:module:1.0" name="org.mariadb">
		<resources>
		<resource-root path="mariadb-java-client-2.4.1.jar"/>
		</resources>
		<dependencies>
		<module name="javax.api"/>
		<module name="javax.transaction.api"/>
		</dependencies>
	</module>`

5. Configure wildfly to point to java 1.8 runtime & start Wildfly by executing the script;

	`$WILDFLY_HOME/bin/standalone.sh`

6. After wildfly starts (5-30 seconds), create the admin user by executing the script `$WILDFLY_HOME/bin/add-user.sh` (read and follow the prompts). Remember the user credentials you've added. You'll need them to access the admin console.

7. Connect to jboss-cli by executing `$WILDFLY_HOME/bin/jboss-cli.sh --connect`


8. Add the mariaDb JDBC driver by execuing the command below while connected to the jboss-cli;

		`/subsystem=datasources/jdbc-driver=org.mariadb:add(driver-name=org.mariadb,driver-module-name=org.mariadb,driver-xa-datasource-class-name=org.mariadb.jdbc.Driver)`

   A successful execution shall give the following output;

   `{"outcome" => "success"}`

9. Exit jboss-cli by typing `exit`, open the browser and navigate to the [Admin Console Page] (http://127.0.0.1:9990/console/App.html). When prompted for credentials, enter credentials created in step #7 of this section.

10. [Create a datasource] (https://docs.jboss.org/author/display/WFLY10/DataSource+configuration) with the JNDI name `java:/cmpDS` that points to the database called `cmp`




# Building the project

You'll need to package two archives, clean the project by running;

`mvn clean`

### 1. WAR file
This runs the platform EJB's required to serve telco endpoints as well as expose REST APIs required by telcos and the platform.
	
`mvn clean compile package`

If this runs successfully, the war file `./target/cmp/cmp.war`

Deploy the war file to wildfly.

### 2. Standalone JAR file
This runs non web-exposable & scalable threads. To build these run;

`mvn clean compile assembly:single`

The genrated jar file will be located at

`./target/cmp-jar-with-dependencies.jar`

Or... combine all;

`mvn clean compile package assembly:single`





## Running the platform.

You'll need to start by deploying the war file so the remote beans 
are accessible by the standalone (SE) threads.

If you deployed to wildfly successfully, this sould be it.







