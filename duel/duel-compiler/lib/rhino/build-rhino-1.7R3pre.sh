#!/bin/bash
cvs -d ':pserver:anonymous@cvs-mirror.mozilla.org:/cvsroot' co mozilla/js/rhino rhino
cd rhino
ant jar
mv -f build/rhino1_7R3pre/js.jar ../js-1.7R3pre.jar