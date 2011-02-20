cvs -d ':pserver:anonymous@cvs-mirror.mozilla.org:/cvsroot' co mozilla/js/rhino rhino
cd rhino
ant jar
mvn install:install-file -Dfile=build/rhino1_7R3pre/js.jar -DgroupId=rhino -DartifactId=js -Dversion=1.7R3 -Dpackaging=jar
