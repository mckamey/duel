#!/bin/bash

mvn install:install-file \
-Dfile=js.jar \
-DgroupId=rhino \
-DartifactId=js \
-Dversion=1.7R3 \
-Dpackaging=jar

mvn deploy:deploy-file \
-Dversion=1.7R3 \
-DartifactId=js \
-DgroupId=rhino \
-DgeneratePom=true \
-Dpackaging=jar \
-Dfile=duel-compiler/lib/rhino/js.jar \
-Durl=file:///duel-mvn-repo-hg \
-DrepositoryId=mvn.duelengine.org.local
