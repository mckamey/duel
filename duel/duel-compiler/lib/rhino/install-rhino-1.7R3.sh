#!/bin/bash

mvn install:install-file \
	-DgroupId=org.mozilla \
	-DartifactId=rhino \
	-Dversion=1.7R3 \
	-Dpackaging=jar \
	-Dfile=js.jar
