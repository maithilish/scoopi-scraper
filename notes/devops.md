## Project Build

``` BASH

mvn test
mvn verify
 
mvn test -Dtest=HttpHelperIT.java -P basic
mvn integration-test -Dtest=HttpHelperIT.java -P basic

# skip test and run itests
mvn integration-test -Dtest=zzz.java -DfailIfNoTests=false -P basic

mvn test jacoco:report					# without itest
mvn verify jacoco:report				# with itest
mvn JavaDoc:JavaDoc

# add -DskipTests to skip all tests

# to create release zip
mvn clean verify -P basic,ng,release

# to create release zip and docker image
mvn clean verify -P basic,ng,release,docker

mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
mvn dependency:resolve -Dclassifier=JavaDoc		# download javadoc
mvn dependency:sources					# download source

# run, skips exec in all modules except in engine
mvn process-classes exec:java -Dexec.mainClass="org.codetab.scoopi.Scoopi" -Dexec.cleanupDaemonThreads=false -Dlogback.configurationFile=src/main/resources/logback-dev.xml -Dscoopi.mode=dev -Dexec.skip=true

```

### Run as Java Application

Select Scoopi class -> Run As -> Java Application
Go to Run Configuration and select Scoopi, in Arguments Tab enter following in VM arguments text box

-Dlogback.configurationFile=src/main/resources/logback-dev.xml
-Dscoopi.mode=dev

### Scoopiw in IDE

To view dash board while running scoopi in IDE, build scoopiw ng module with

	mvn clean verify -P basic,ng -DskipTests

Optionally, create zip of scoopiw and unzip it after very clean 

	cd metric
	zip -r scoopiw.zip target/classes/webapp
	
if ng is not passed to -P during mvn install or verify, then scoopiw is not compiled and WEB-INF dir is listed instead of dashboard.

### Properties

switch config file to scoopi-dev.properties

-Dscoopi.mode=dev
-Dlogback.configurationFile=src/main/resources/logback-dev.xml

-Dscoopi.waitForHeapDump=true				# profile

scoopi.datastore.configFile=jdoconfig-dev.properties  	# use dev db

  
### Docker Services for ITest 

Docker compose file for services for itest is located at src/test/itestresources. 
See readme.txt for details.

 
### Integration Tests

maven failsafe plugin treats all files \*IT.java as integration test.

mvn clean integration-test -Dtest=zzz.java -DfailIfNoTests=false

- compiles all tests 
- copies test resources
- skips unit tests 
- build is not failed because of failure of unit tests
- runs \*IT.java tests

## Eclipse setup

For Scoopi development, eclipse requires some setup.

**! ! !  install eclipse-cs and ecl-emma
import preferences only after cs and emma are installed**

### Install Java JavaDoc and source

for context help, install JavaDoc and sources.

    dnf list java-1.8.0-openjdk*
    dnf install java-1.8.0-openjdk-javadoc
    dnf install java-1.8.0-openjdk-src

- in Fedora, source src.zip is installed under /usr/lib/jvm/jdk<xxx>/

- use alternatives to find location of JavaDoc  `alternatives --list`

Go to, _Preferences -> Installed JRE -> OpenJdk x.x.x -> Edit and select rt.jar_ and enter

JavaDoc Location - enter JavaDoc URL as file:///etc/alternatives/JavaDocdir

Source Attachment - external location -> path as /usr/lib/jvm/jdk1.8.0_xxx/src.zip

to know src.zip location use

    rpm -ql java-1.8.0-openjdk-src

ubuntu
javadoc - url - file:///usr/share/doc/openjdk-8-jre-headless/api
source - external location - /usr/lib/jvm/java-8-openjdk-amd64/src.zip

### Add Imports

For static import of AssertJ and Mockito go to, _Static import - Preference -> Java -> Editor -> Content Assist -> Favorites_ and add New Types

    org.mockito.Mockito
    org.mockito.BDDMockito
    org.mockito.Matchers
    org.mockito.ArgumentMatchers
    org.assertj.core.api.Assertions
    java.util.Objects
    java.util.stream.Collectors

### change author name

edit eclipse.ini and add `-Duser.name=<user name>`

### Shorten Package Name

Preferences > Java > Appearance
  Abbreviate Package Names: org.codetab.scoopi=s

### M2E plugin

to download JavaDoc and sources, go to  Project Context Menu-> Maven

- check Download Artifact sources
- check Download Artifact JavaDoc
- uncheck Download repository index on startup

or download manually

    mvn dependency:sources dependency:resolve -Dclassifier=JavaDoc

in case, _doc location not set error_ is thrown then
Build Path -> Configure Build Path -> Libraries remove extra entries of M2_REPO and update maven. Only JRE and Maven Dependencies entries are enough.

### Setup CheckStyle

go to Preferences -> Checkstyle and slide to right side end, click New and enter
 - type - External Configuration
 - name - Scoopi Checks
 - location - /eclipse-workspace/ /scoopi/src/main/resources/eclipse/scoopi_checks.xml

new Check Configuration is created and select it in project checkstyle setup. Next, edit and modify Scoopi Checks.

 - JavaDoc comments - disable all modules
 - Class design - disable Design for extension (forces for JavaDoc for extension)
 - Class design - disable Final Class
   (forces class singleton with private constructor to be final and final class
    can't be mocked)
  - Filters - suppression filter and set file to
   /eclipse-workspace/scoopi/src/main/resources/eclipse/suppressions.xml

about suppression configuration

- relative path not allowed, abs path is required
- enable Purge Checkstyle Cache button in toolbar, purge cache after any changes to suppressions.xml

in project properties select Scoopi Checks module and activate CheckStyle for the project.

### Code Style - Formatter

For Checkstyle compliant formatter, import scoopi/src/main/resources/eclipse/formatter.xml
which creates Eclipse-cs Scoopi formatter profile.

To import, go to _Preferences -> Java -> Code Style -> Formatter -> Import_

In project Properties -> Java Code Style -> Formatter, set Active Profile to Eclipse-cs Scoopi

For XML files, change format options in Preferences -> XML -> XML file -> Editor
  - line width - 72
  - uncheck format comments
  - indent using spaces - Indention Size 4

### Debug perspective

To  allot maximum screen space to variables and editors reorganize the Debug perspective.

 - if view is dropped to top bar of area, view will be added to that area.
 - if view is dropped on area then area is resized to accommodate new view.

to split bottom row into two, drag n drop Debug view to Console view area.
to remove Outline view, close it.
to split editor area into two, drag n drop Variables and Breakpoints view to editor area.

customize the perspective to remove toolbar items.

In debugger, you will get line number error, even when line number generation is enabled in Preferences -> Java -> Compiler option. Disable this error in Java -> Debug option.

to debug XML objects such as Fields in Locator see https://goo.gl/JEukUP

### Exclude web dir from errors

in metric module 

Project > Properties > Resource > Resource Filters > Add...
Filter type = Exclude all.
Applies to = File and Folders (check all children recursive)
File and Folder Attributes: Project Relative Path matches src/main/web/scoopiw

### Save Actions

Pref -> Java -> Editor -> Save Actions

Enable Perform the selected action
 - Format Source Code 
    - Edited lines
 - Organize imports

Additional Actions -> Configure

Code Organizing:
 - Remove trailing white spaces on all lines
Code Style: 
 - Add final modifier to method parameters
 - Add final modifier to local variables
Missing Code:
 - Add missing '@Override' annotations
 - Add missing '@Override' annotations to implementations of interface methods
 - Add missing '@Deprecated' annotations
Unnecessary Code:
 - Remove unnecessary casts
 - Remove unused imports

### Run Configuration

In Eclipse, add new run configuration, go to Run As -> Maven Build and enter

Main 	-> Base directory - ${project_loc:scoopi}
Goals 	-> process-classes exec:java -Dexec.mainClass="org.codetab.scoopi.Scoopi" 
	    -Dlogback.configurationFile=src/main/resources/logback-dev.xml 
	    -Dscoopi.mode=dev -Dexec.cleanupDaemonThreads=false -Dexec.skip=true

skips exec in all modules except engine module. 

Dao Enhance

Main 	-> Base directory - ${project_loc:dao}
Goals 	-> clean test-compile datanucleus:enhance
Resolve Workspace Artifacts - check (otherwise mvn tries to download modules)


### Run Configuration Variables

For Run As - Java Application, the arguments:

- Program Arguments
  - space delimited values
  - get from args[] in main method

- VM Arguments
  - -Dxyz=abcd
  - use System.getProperty("xyz") to get value

- Environment Variables
  - variable + value
  - use System.getenv(key) to get value
 
## Node.js and Angular

On new machine install nodejs and angular cli

    curl --silent --location https://rpm.nodesource.com/setup_10.x | sudo bash -
    yum install nodejs

    npm install -g @angular/cli

### Scoopi dashboard

scoopiw is in metric module src/main/web dir

install modules

	cd metric/src/main/web/scoopiw
	rm -rf node_modules
	npm install


### Scoopi Metric

Jetty server starts at port 9010. Angular app source is located in metric/src/main/web/scoopiw dir. During dev, in memory datastore is used and in prod build, it fetches data from localhost:9010/api/metrics

mvn package, builds the angular app with 
 
 ng build --prod --build-optimizer 
          --output-path=${project.build.directory}/classes/webapp
          --delete-output-path=false
          
in POM the ng build is in separate profile ng-build which is disabled
for travis ci build in .travis.yml

### Model Generation

to generate model java files from schema files, run src/main/scripts/schemagen.sh from project base dir. Beans are validated against the schema. It generates the file in target dir and after verification it has to copied to model dir in src.

## Versions

Given a version number MAJOR.MINOR.PATCH, increment the:

    MAJOR version when you make incompatible API changes,
    MINOR version when you add functionality in a backwards-compatible manner, and
    PATCH version when you make backwards-compatible bug fixes.

And yes, 1.0 should be stable. All releases should be stable, unless they're marked alpha, beta, or RC.

- Use Alphas for known-broken-and-incomplete.
- Betas for known-broken.
- RCs for "try it; you'll probably spot things we missed".
- Anything without one of these should (ideally, of course) be tested, known good, have an up to date manual, etc.

Precedence Example:

1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta
  < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0.

0.9.0-beta -> 0.9.0-rc.1 -> 0.9.0-rc.2 -> 1.0.0


## Externalise messages

defs and devdefs folders in resources dir are flattened after running
eclipse:eclispe. To make them hierarchical, add then folders to exclude list. Select resource folder and go to Build Path -> Configure Exclusion Filters and add defs/examples and devdefs/jsoup to exclude.

don't try to externalise model classes !!!

for internationalization add  messages_LANG_COUNTRY.properties such
as messages_fr_CA.properties and run app with

java -Duser.language=fr -Duser.country=CA Default

## WINE

windows test is not really essential, but just to clear any doubt about bad coding, prefer to test scoopi with wine.

install wine

    dnf install wine

download openjdk_devel windows installer (msi file) from  developers.redhat.com and install it with wine

     wine msiexec /i java-1.8.0-openjdk-xxx.redhat.windows.x86_64.msi

unzip scoopi-xxx-production.zip and cp the scoopi folder to wine directory

     cp -rf scoopi-xxx ~/.wine/drive_c/scoopi

run scoopi with wine

     wine java -cp "c:\\scoopi\scoopi-xxx.jar;c:\\scoopi\lib\*;c:\\scoopi\config;c:\\scoopi\defs" org.codetab.scoopi.Scoopi

to run scoopi.bat, get into window command prompt and run bat file

     wine cmd
     C:\scoopi-0.9.0-beta>scoopi.bat


## Docker local build

for io.fabric8:docker-maven-plugin prefix is docker

list goals 

     mvn docker:help
     
build image and add image to local image repository

     mvn docker:build
         
## Github

clone and create new project in workspace

	git clone http://github.com/maithilish/scoopi
	cd scoopi
	mvn eclipse:eclipse

remove latest commit

	git reset --hard <commit>
	git push --force

travis maven and build steps
 
 - https://docs.travis-ci.com/user/languages/java/#Projects-Using-Maven
 - https://docs.travis-ci.com/user/customizing-the-build/#Customizing-the-Build-Step
     
## Release

merge branch if any and change version in all modules

	mvn versions:set -DnewVersion=0.9.7-beta
	mvn versions:commit   # or versions:revert 

commit and add tag 
	
	git push
	git tag <version>          // add local tag
	git push origin --tags
	
build release 

	mvn clean verify -P basic,release

release profile creates release zip and installs docker image in local repository.
	
create new release in github and attach zip

push image to docker hub

      docker login            // one time
      docker push codetab/scoopi:<version>
    



