#!/bin/bash
mvn install:install-file -Dfile=js.jar -DgroupId=rhino -DartifactId=js -Dversion=1.7R3 -Dpackaging=jar
