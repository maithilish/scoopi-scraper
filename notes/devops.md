## Maven build

to switch default conf files :

- for dev, use -Dscoopi.mode=dev 
     - uses scoopi-dev.properties instead of default scoopi.properties
     - to use dev db, in scoopi-dev.properties, scoopi.datastore.configFile is set to jdoconfig-dev.properties 

- for logback-dev.xml use -Dlogback.configurationFile=src/main/resources/logback-dev.xml, default logback.xml

### Maven commands

to add new run configuration, go to Run As -> Maven Build and enter

     Main -> Base directory - ${project_loc:scoopi}
     Goals -> process-classes exec:java -Dexec.mainClass="org.codetab.scoopi.Scoopi" -Dexec.cleanupDaemonThreads=false -Dlogback.configurationFile=src/main/resources/logback-dev.xml -Dscoopi.mode=dev

maven dev run

     mvn process-classes exec:java -Dexec.mainClass="org.codetab.scoopi.Scoopi" -Dexec.cleanupDaemonThreads=false -Dlogback.configurationFile=src/main/resources/logback-dev.xml -Dscoopi.mode=dev

for profile memory and cpu

      mvn process-classes exec:java -Dexec.mainClass="org.codetab.scoopi.Scoopi" -Dexec.cleanupDaemonThreads=false -Dlogback.configurationFile=src/main/resources/logback-dev.xml -Dscoopi.mode=dev -Dscoopi.waitForHeapDump=true

compile, enhance JDO classes and run tests

    mvn test

run tests and Integration tests

    mvn verify

skip tests and run integration tests (itests)

    mvn integration-test -Dtest=zzz.java -DfailIfNoTests=false -P production

generate coverage report

only tests
      
      mvn clean test jacoco:report
      	or
      mvn clean verify
include itest
                          
      mvn clean verify jacoco:report

find selector

    mvn exec:java -Dexec.mainClass="org.codetab.scoopi.util.FindSelector"
       -Dexec.args="fileName 'selector' "

selector example

     mvn exec:java -Dexec.mainClass="org.codetab.scoopi.util.FindSelector"
   -Dexec.args="src/main/resources/devdefs/mc/parse-locator-file/itc-quote.html 'div#mktdet_2 div:matchesOwn(^P/E$)' "

generate JavaDoc

    mvn JavaDoc:JavaDoc

to know dependency updates

    mvn versions:display-dependency-updates

download JavaDoc and source

    mvn dependency:resolve -Dclassifier=JavaDoc
    mvn dependency:sources
    
## Docker Mariadb DB 

Mariadb docker container is used for dev and integration tests. Unit tests doesn't require db. 
 
    $ cd src/main/db
    $ docker-compose up 

This creates three databases, scoopidev, scoopitest and scoopi with required user and privileges. The scoopi database is used to test the release and it is not for production as container data is deleted on shutdown.

To login, use

    $ mysql -p -u foo -h 127.0.0.1 -P 3306 scoopidev
 
## Eclipse setup

For Scoopi development, eclipse requires some setup.

**! ! !  install eclipse-cs and ecl-emma
import preferences only after cs and emma are installed**

#### install and attach Java JavaDoc and source

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

#### add imports

For static import of AssertJ and Mockito go to, _Static import - Preference -> Java -> Editor -> Content Assist -> Favorites_ and add New Types

    org.mockito.Mockito
    org.mockito.BDDMockito
    org.mockito.Matchers
    org.mockito.ArgumentMatchers
    org.assertj.core.api.Assertions
    java.util.Objects
    java.util.stream.Collectors

#### change author name

edit eclipse.ini and add `-Duser.name=<user name>`

#### M2E plugin

to download JavaDoc and sources, go to  Project Context Menu-> Maven

- check Download Artifact sources
- check Download Artifact JavaDoc
- uncheck Download repository index on startup

or download manually

    mvn dependency:sources dependency:resolve -Dclassifier=JavaDoc

in case, _doc location not set error_ is thrown then
Build Path -> Configure Build Path -> Libraries remove extra entries of M2_REPO and update maven. Only JRE and Maven Dependencies entries are enough.

#### Setup CheckStyle

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


#### Code Style - Formatter

For Checkstyle compliant formatter, import scoopi/src/main/resources/eclipse/formatter.xml
which creates Eclipse-cs Scoopi formatter profile.

To import, go to _Preferences -> Java -> Code Style -> Formatter -> Import_

In project Properties -> Java Code Style -> Formatter, set Active Profile to Eclipse-cs Scoopi

For XML files, change format options in Preferences -> XML -> XML file -> Editor
  - line width - 72
  - uncheck format comments
  - indent using spaces - Indention Size 4

#### Debug perspective

To  allot maximum screen space to variables and editors reorganize the Debug perspective.

 - if view is dropped to top bar of area, view will be added to that area.
 - if view is dropped on area then area is resized to accommodate new view.   

to split bottom row into two, drag n drop Debug view to Console view area.
to remove Outline view, close it.
to split editor area into two, drag n drop Variables and Breakpoints view to editor area.

customize the perspective to remove toolbar items.

In debugger, you will get line number error, even when line number generation is enabled in Preferences -> Java -> Compiler option. Disable this error in Java -> Debug option.

to debug XML objects such as Fields in Locator see https://goo.gl/JEukUP

#### Exclude web dir from errors

Project > Properties > Resource > Resource Filters > Add...
Filter type = Exclude all.
Applies to = File and Folders (check all children recursive)
File and Folder Attributes: Project Relative Path matches src/main/web/scoopiw

## Node.js and Angular

On new machine install nodejs and angular cli

    curl --silent --location https://rpm.nodesource.com/setup_10.x | sudo bash -
    yum install nodejs

    npm install -g @angular/cli

## Scoopi dashboard

install modules

	cd src/main/web/scoopiw
	rm -rf node_modules
	npm install


## Scoopi Metric

Jetty server starts at port 9010. Angular app source is located at src/main/web/scoopiw dir. During dev, in memory datastore is used and in prod build, it fetches data from localhost:9010/api/metrics

mvn package, builds the angular app with 
 
 ng build --prod --build-optimizer 
          --output-path=${project.build.directory}/classes/webapp
          --delete-output-path=false
          
in POM the ng build is in separate profile ng-build which is disabled
for travis ci build in .travis.yml             

## Model Generation

to generate model java files from schema files, run src/main/scripts/schemagen.sh from project base dir. Beans are validated against the schema. It generates the file in target dir and after verification it has to copied to model dir in src.


## Integration Tests

maven failsafe plugin treats all files \*IT.java as integration test.

mvn clean integration-test -Dtest=zzz.java -DfailIfNoTests=false

this ensures
 - all tests are compiled
 - test resources are copied
 - unit tests are skipped  (as no such file named zzz.java)
 - build is not failed because of failure of unit tests
 - \*IT.java tests are run


## Find selector or xpath from chrome

open the page in chrome, select any text, select Inspect from context menu
in Inspector pane, right click on element and select Copy where you
can copy selector and xpath and paste it to editor. In selector or xpath use
single quotes instead of double quotes

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

# Docker local build

for io.fabric8:docker-maven-plugin prefix is docker

list goals 

     mvn docker:help
     
build image and add image to local image repository

     mvn docker:build
          
# Github

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
     
Github release

merge branch if any

change version in pom.xml and commit
          
     git tag <version>          // add local tag
     git push origin --tags
     
create new release in github and attach zip
     
Docker image release

build image

      mvn clean verify docker:build

push image

      docker login            // one time
      mvn docker:push

# Design and coding notes

Apart from model classes, as far as possible, try to use primitives as validation of method param is not required for primitives.

As XML schema allows optional elements, JAXB uses wrappers for primitives since primitives can't be null. XJC generated models will have Long, Integer etc.

Validate param for null or illegal argument (not required for private methods). validate IllegalState for state vars, injected state vars (not required for private methods).

## Coding notes

- for string join use Util.join(), for delimited join use String.join()
- in steps, if exception is recoverable then catch and handle it otherwise if unrecoverable throw StepRunException (unchecked exception)
- in steps, while throwing StepRunException don't log or add activity as tasks' exception handling takes care of it.
- in steps, for StepRunException don't use getLabeled() as task will handle label,  for others use getLabeled() to create message which prefixes label. If message is used only with logger, then use {} and getLabel()

#### JavaDoc guidelines

  - add /** and press enter to generate JavaDoc comments
  - remove any non-JavaDoc comments generated by eclipse
  - for methods that doesn't do anything add do nothing
  - for overridden method, add JavaDoc comments
  - use @see to link any project classes and also for java or external classes and methods
    - @see in text creates inline link
    - if used after tags (param,return) then added in See also section
  - use @throws both for checked and unchecked exception.
  - In method signature, add only the checked exceptions. Mention unchecked exceptions in JavaDoc with @throws

#### Tricky errors

###### XML namespace

  getNamespaceURI() on document returns null but for jaxb elementNSImpl it returns uri.
  better option is to use lookupNamespaceURI() with null for default ns.

###### JDO

  JDODataStoreException - Exception thrown flushing changes to datastore

  Set detachable as true in package.jdo for the class.

  In case detached object is persisted again and detachable is not true for the class then instead of update jdo inserts new object. If this results in constraint violation then jdo throws NullPointerExcetpion.
  
###### Jackson

find field in node or child nodes

	findValue(String fieldName) - JsonNode or null 
	findPath(String fieldName) - JsonNode or MissingNode

find direct field 

	get(String fieldName) - JsonNode or null
	path(String fieldName) - JsonNode or MissingNode

set nodes

	set(fieldName, node) - add field and set node 
	setAll(node) - add node directly 
