#!/bin/bash

clear;clear;

BUCKET='s3://duel/'
DISTRO='cf://E3G86IU7BFHQIK'
SOURCE='./duel-js/target/'

# exclude '.DS_Store' and hg files

s3cmd sync \
	--add-header='Vary: Accept-Encoding' \
	--no-mime-magic \
	--guess-mime-type \
	--encoding=UTF-8 \
	--add-encoding-exts=foo \
	--reduced-redundancy \
	--cf-invalidate \
	--acl-public \
	--recursive \
	--exclude '.DS_Store' \
	--exclude 'antrun/*' \
	--exclude 'duel.js' \
	--exclude 'duel-render.js' \
	--exclude 'duel-dom.js' \
	--exclude '.git/*' \
	--exclude '.gitignore' \
	--exclude '.hg/*' \
	--exclude '.hgignore' \
	--exclude '.hgtags' \
	--exclude '*.sh' \
	--exclude 'tomcat/*' \
	${SOURCE} \
	${BUCKET}

#s3cmd cfinvalinfo ${DISTRO}
