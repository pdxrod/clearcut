# The Clearcut Framework

No warranties are given for this software's suitability for
any purpose, security, safety or anything else - the software
is distributed AS IS and is governed by the Gnu Public License:
[http://www.gnu.org/licenses/gpl-3.0.txt](http://www.gnu.org/licenses/gpl-3.0.txt)


Clearcut is a simple dependency injection, object-relational mapping
and logging framework for Java and which avoids the complexity
of XML configuration files.

I wrote it because I had spare time once I started
creating web applications in Ruby on Rails. The only third
party framework included is JUnit, and the JDBC drivers for
MySQL and M$SQL. I avoided Hibernate, Spring and Log4J among
other examples of large and over-complex Java frameworks.

The files are arranged in the Maven convention (src/main/java
and src/test/java) but I found I could do without Maven itself.
The tests are run by finding the test classes
using FIND and SED, venerable Unix commands available on all platforms
including Windows (using Cygwin). See run.this.


See [http://martinfowler.com/articles/injection.html](http://martinfowler.com/articles/injection.html) for
an explanation of dependency injection, and
[http://springframework.org/](http://springframework.org/)  for this pattern's most well-known
implementation.


To build and test this framework, you need a version of Unix.
If using Windows, download Cygwin from http://www.cygwin.com/
and run the Cygwin shell. CD into the folder containing this
file and type

./run.this

This builds and tests the framework. You may need to install
MySQL [mysql.com](http://mysql.com) and run the script build.mysql in the
dat/ folder in the folder containing this file. If using
Microsoft SQL Server, run build.sqlserver and remove the
semicolon at the beginning of the 'm$_url' line in file app.ini.


You can also test the web application.
For example, using the Geronimo application server:

cp app.ini /opt/geronimo-1.0/clearcut-example-app.ini

export JAVA_HOME=/usr/bin

cd /opt/geronimo-1.0/bin

java -jar server.jar &

- this starts Geronimo
(other application servers, eg. Tomcat, are similar).

Point web browser at

localhost:8080/console/portal/apps/apps_all         

Log in (User: system Password: manager)

Install new application

Choose clearcut-example.war from the bin/ folder below this one

Click 'Install'

Wait a few seconds

Point web browser at

localhost:8080/clearcut-example
