#!/bin/sh

clear;clear

cd duel-runtime
mvn clean deploy -DperformRelease=true -Dgpg.keyname=EE82F9AB
cd ..

cd duel-compiler
mvn clean deploy -U -DperformRelease=true -Dgpg.keyname=EE82F9AB
cd ..

cd duel-maven-plugin
mvn clean deploy -U -DperformRelease=true -Dgpg.keyname=EE82F9AB
