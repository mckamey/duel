#!/bin/bash

# exclude '.DS_Store' and build files
s3cmd sync \
	--cf-invalidate \
	--acl-public \
	--recursive \
	--exclude '.DS_Store' \
	--exclude 'antrun/*' \
	--exclude 'duel.js' \
	--exclude 'duel-render.js' \
	--exclude 'duel-dom.js' \
	./duel-js/target/ \
	s3://duel/

# s3cmd cfinvalinfo cf://E3G86IU7BFHQIK
