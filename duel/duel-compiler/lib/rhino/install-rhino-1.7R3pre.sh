#!/bin/bash
mvn install:install-file -Dfile=js-1.7R3pre.jar -DgroupId=rhino -DartifactId=js -Dversion=1.7R3 -Dpackaging=jar
