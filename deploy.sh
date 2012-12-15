#!/bin/bash
set -e

# --------------------------------------------------------
# Before using this script, need to ensure
# ~/.gnupg/ contains key corresponding to KEYNAME, and
# ~/.m2/settings.xml contains OSS Sonatype credentials
# --------------------------------------------------------

if [ $1 != '' ]; then
	PASSPHRASE=-Dgpg.passphrase=$1
else
	PASSPHRASE=''
fi

if [ $2 != '' ]; then
	KEYNAME=-Dgpg.keyname=$2
else
	KEYNAME=-Dgpg.keyname=EE82F9AB
fi

clear;clear

cd duel-runtime
mvn clean deploy -DperformRelease=true ${KEYNAME} ${PASSPHRASE}
cd ..

cd duel-compiler
mvn clean deploy -U -DperformRelease=true ${KEYNAME} ${PASSPHRASE}
cd ..

cd duel-maven-plugin
mvn clean deploy -U -DperformRelease=true ${KEYNAME} ${PASSPHRASE}
cd ..

cd duel-staticapps
mvn clean deploy -U -DperformRelease=true ${KEYNAME} ${PASSPHRASE}
cd ..

cd duel-staticapps-maven-plugin
mvn clean deploy -U -DperformRelease=true ${KEYNAME} ${PASSPHRASE}
