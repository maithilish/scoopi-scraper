## Maven setup

Minimum POM

refer: JDO Tools Guide
enhancer refer: JDO Enhancer Guide

``` XML
	
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.datanucleus</groupId>
            <artifactId>datanucleus-accessplatform-jdo-rdbms</artifactId>
            <version>5.2.2</version>
            <type>pom</type>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-dbcp2</artifactId>
            <version>2.7.0</version>
        </dependency>
        <dependency>
            <groupId>org.mariadb.jdbc</groupId>
            <artifactId>mariadb-java-client</artifactId>
            <version>2.2.5</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.datanucleus</groupId>
                <artifactId>datanucleus-maven-plugin</artifactId>
                <version>5.2.1</version>
                <configuration>
                    <props>src/main/resources/jdoconfig.properties</props>
                    <log4jConfiguration>src/main/resources/log4j.properties</log4jConfiguration>
                </configuration>
                <executions>
                    <execution>
                        <phase>process-classes</phase>
                        <goals>
                            <goal>enhance</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
	
```

## Configuration files

schema file: src/main/resources/package.jdo

refer: JDO Mapping Guide
 

``` XML

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE jdo PUBLIC "-//Sun Microsystems, Inc.//DTD Java Data Objects Metadata 2.0//EN"
                     "http://java.sun.com/dtd/jdo_2_0.dtd">

<jdo>
    <package name="in.ex.model">
        <class name="Person" detachable="true">
            <field name="id" primary-key="true"
                value-strategy="INCREMENT" />
            <field name="name" />
        </class>
    </package>
</jdo>

```

datastore config:
src/main/resources/jdoconfig.properties or datanucleus.properties

refer: JDO Persistence Guide   


``` Properties
	
## mariadb
javax.jdo.option.ConnectionDriverName=org.mariadb.jdbc.Driver
javax.jdo.option.ConnectionURL=jdbc:mariadb://localhost:3306/scoopi
javax.jdo.option.ConnectionUserName=foo
javax.jdo.option.ConnectionPassword=bar
javax.jdo.option.Mapping=mysql

javax.jdo.PersistenceManagerFactoryClass=org.datanucleus.api.jdo.JDOPersistenceManagerFactory

## common
datanucleus.cache.level2.type=weak
datanucleus.persistenceByReachabilityAtCommit=false
datanucleus.metadata.validate=false
datanucleus.generateSchema.database.mode=none
datanucleus.schema.autoCreateAll=true

datanucleus.connectionPoolingType=DBCP2
datanucleus.connectionPool.maxIdle=10
datanucleus.connectionPool.minIdle=10
datanucleus.connectionPool.maxActive=10
datanucleus.connectionPool.maxWait=60

datanucleus.connectionPool.testSQL=SELECT count(*) FROM INFORMATION_SCHEMA.TABLES
	
```

logging: src/main/resources/log4j.properties

``` Properties
	
# Define the destination and format of our logging
log4j.appender.A1=org.apache.log4j.FileAppender
log4j.appender.A1.File=logs/datanucleus.log
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%d{HH:mm:ss,SSS} (%t) %-5p [%c] - %m%n

# DataNucleus Categories
log4j.category.DataNucleus.JDO=INFO, A1
log4j.category.DataNucleus.Cache=INFO, A1
log4j.category.DataNucleus.MetaData=INFO, A1
log4j.category.DataNucleus.General=INFO, A1
log4j.category.DataNucleus.Transaction=INFO, A1
log4j.category.DataNucleus.Connection=INFO, A1
log4j.category.DataNucleus.Datastore=DEBUG, A1
log4j.category.DataNucleus.ValueGeneration=DEBUG, A1
log4j.category.DataNucleus.Persistence=DEBUG, A1

log4j.category.DataNucleus.Enhancer=INFO, A1
log4j.category.DataNucleus.SchemaTool=INFO, A1
	
```






