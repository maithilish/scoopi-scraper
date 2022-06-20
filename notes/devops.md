## Maven Build


quick builds

    mvn clean verify -DskipTests -P basic,ng
    mvn clean verify -DskipTests -P basic,ng,release
    mvn clean verify -DskipTests -P basic,ng,release,docker

Test and verify

Docker compose file for services for itest is located at scoopi-scraper/src/itestservices. See readme.txt for details.

    mvn test
    mvn verify
 
Release build

	sudo systemctl stop apache2
    cd scoopi-scraper/src/itestservices ; docker-compose up
    mvn clean verify -P basic,ng,release,docker

Skip test and run itests

    mvn test -Dtest=HttpHelperIT.java -DfailIfNoTests=false -P basic
    mvn integration-test -Dtest=zzz.java -DfailIfNoTests=false -P basic

run single itest, but this will not skip unit tests
    
    mvn integration-test -Dit.test=FinEx13KillAfter13SecIT.java -DfailIfNoTests=false -P basic

Test coverage

By default, reports are generated and aggregated in verify phase.

    mvn verify			// test + itest
    
To generate and aggregate for unit tests only, use

    mvn clean test jacoco:report-aggregate

Download javadoc, source

    mvn dependency:resolve -Dclassifier=JavaDoc
    mvn dependency:sources

Check for versions

    mvn versions:display-dependency-updates
    mvn versions:display-plugin-updates

Run from console

Skips exec in all modules except in engine. Error 'Could not resolve dependencies' is thrown if no process-classes otherwise works fine

    mvn process-classes exec:java -Dexec.mainClass="org.codetab.scoopi.Scoopi" \
    -Dexec.cleanupDaemonThreads=false -Dlogback.configurationFile=src/main/resources/logback-dev.xml \
    -Dscoopi.mode=dev -Dexec.skip=true

 
## IDE Run  

Select Scoopi class -> Run As -> Java Application. Edit Run Configuration; add following in Arguments Tab 

Solo

-Dscoopi.mode=dev
-Dscoopi.metrics.server.enable=true
-Dlog4j.configurationFile=src/main/resources/log4j2-dev.xml

Cluster

-Dscoopi.mode=dev
-Dscoopi.cluster.enable=true
-Dscoopi.log.dir=logs/cluster-a
-Dscoopi.cluster.log.path.suffixUid=false
-Dlog4j.configurationFile=src/main/resources/log4j2-dev.xml

To enable metric server

-Dscoopi.metrics.server.enable=true

For cluster create multiple run configs and group them in Launch Group.

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


## Release

update dependencies and plugin to latest version.

merge branch if any and change version in all modules

	mvn versions:set -DnewVersion=0.9.7-beta
	mvn versions:commit   # or versions:revert 

commit and add tag 
	
	git push
	git tag <version>          // add local tag
	git push origin --tags
	
build release 

	mvn clean verify -P basic,ng,release,docker

release profile creates release zip and installs docker image in local repository.
	
create new release in github and attach zip

push image to docker hub

      docker login            // one time
      docker push codetab/scoopi:<version>

## Scoopiw Dashboard 

To view dash board, during dev, in IDE; build scoopiw ng module with

	mvn clean verify -P basic,ng -DskipTests

Optionally, create zip of compiled scoopiw in metric/target/classes/webapp and unzip it after very clean 

	cd metric
	zip -r scoopiw.zip target/classes/webapp
	
If ng is not passed to -P during mvn install or verify, then scoopiw is not compiled. When compiled scoopiw is not available in target/classes/webapp, then browser lists WEB-INF dir instead of dashboard.

## Properties

switch config file to scoopi-dev.properties

-Dscoopi.mode=dev
-Dlogback.configurationFile=src/main/resources/logback-dev.xml

-Dscoopi.waitForHeapDump=true				# profile

scoopi.datastore.configFile=jdoconfig-dev.properties  	# use dev db
 
## Integration Tests

maven failsafe plugin treats all files \*IT.java as integration test.

mvn clean integration-test -Dtest=zzz.java -DfailIfNoTests=false

- compiles all tests 
- copies test resources
- skips unit tests 
- build is not failed because of failure of unit tests
- runs \*IT.java tests

## Eclipse setup

For Scoopi development, eclipse requires some setup.

EclEcmma comes bundled in Eclipse Java IDE.

!!! install eclipse-cs (checkstyle) !!!
import preferences only after cs and emma are installed**

### Install Java JavaDoc and source

for context help, install JavaDoc and sources.

	sudo apt install openjdk-8-jdk openjdk-8-doc openjdk-8-source

JVM location in Ubuntu

  - jvm: /usr/lib/jvm/java-8-openjdk-amd64
  - doc: /usr/lib/jvm/java-8-openjdk-amd64/docs
  - src: /usr/lib/jvm/java-8-openjdk-amd64/src.zip


Go to, Preferences -> Installed JRE -> OpenJdk x.x.x -> Edit and select rt.jar_ and enter

JavaDoc Location - enter JavaDoc URL as file:///usr/lib/jvm/java-8-openjdk-amd64/docs/api
Source Attachment - external location -> path as /usr/lib/jvm/java-8-openjdk-amd64/src.zip


### Add Imports

For static import of AssertJ and Mockito go to Preference -> Java -> Editor -> Content Assist -> Favorites and add New Types

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

 
## Node and Angular

On new machine install nodejs
    
    sudo apt install nodejs
    sudo apt install npm
    
Then, downgrade versions and install angular cli

	sudo npm install -g npm@6.14.4
	hash -r
	npm -v
	sudo npm install -g n   # to downgrade node
	sudo n 10.19.0
	hash -r
	node -v
	
	sudo npm install -g @angular/cli@11.0.2
	
built with 

Dec 2020 - nodjs - v10.19.0, npm - 6.14.4 and angular cli 11.0.2
Oct 2020 - nodjs - v10.19.0, npm - 6.14.4 and angular cli 9.1.8

ng version command run in dir metric/src/main/web/scoopiw shows following message 
	Your global Angular CLI version (9.1.8) is greater than your local
	version (6.0.8). The local Angular CLI version is used.

## Scoopiw dev

Ng dev server

	cd metric/src/main/web/scoopiw/
	ng serve -o 

Minor version upgrade: change versions in package.json and run commands
 
	npm update
	ng update
	
to update devkit, use following 
	
	npm uninstall @angular-devkit/build-angular
	npm install --save-dev @angular-devkit/build-angular@0.1100.2
	
Major version upgrade: 

  - create new app with `ng new scoopiw` in work dir. Choose strict, no routing and style CSS.
  - compare new package.json with old and add any left out such as "bootstrap": "^x.x.x"
  - update angular.json and add "node_modules/bootstrap/dist/css/bootstrap.min.css" to build/styles element.  
  - copy app, assets, favicon.ico, index.html, styles.css from old src folder to new src folder.

	rm -rf node_module 
	npm cache clean --force
	npm install	
	ng serve
	
After errors refactor copy new scoopiw to metric/src/main/web/scoopiw and build

	mvn clean verify -P basic,ng,release,docker -DskipTests

Run scoopi (solo and cluster) in IDE and check webapp.	  

## Scoopi dashboard

scoopiw is in metric module src/main/web dir

install modules

	cd metric/src/main/web/scoopiw
	rm -rf node_modules
	npm install


## Scoopi Metric

Jetty server starts at port 9010. Angular app source is located in metric/src/main/web/scoopiw dir. During dev, in memory datastore is used and in prod build, it fetches data from localhost:9010/api/metrics

mvn package, builds the angular app with 
 
 ng build --prod --build-optimizer 
          --output-path=${project.build.directory}/classes/webapp
          --delete-output-path=false
          
in POM the ng build is in separate profile ng-build which is disabled
for travis ci build in .travis.yml

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

## Sort data files

sort -t '|' -k 1 -k 2 -k 3 -k 4 data*.txt > expected.txt

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

## Jacoco Reports

For Itests, reports are generated only for engine module. It is not possible to acquire the coverage data for the dependent modules.

## Troubleshoot

### Cluster ITest failure in CLI

Cluster IT passes classpath from System.getProperty("java.class.path")) to ProcessBuilder. This classpath is set by failsafe plugin; till 2.18.1 version it use to add target/test-classes and target/classes to classpath but later versions doesn't add engine/target/classes but adds other modules such as config,metrics etc., jars. As a workaround following configuration is set for the plugin so that engine/classes is added to classpath.

	<additionalClasspathElement>${basedir}/target/classes</additionalClasspathElement>
	
To enable System.out.println() output from the process set inheritIO().

	ProcessBuilder pb = new ProcessBuilder(cmd).inheritIO();	
