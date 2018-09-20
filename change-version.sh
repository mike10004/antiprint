#!/bin/bash

set -e

VERSION=$1

if [ -z "${VERSION}" ] ; then
  echo "change-version.sh: exactly one argument is required" >&2
  exit 1
fi

mvn --quiet versions:set -DnewVersion="${VERSION}" -DgenerateBackupPoms=false

MANIFEST_VERSION=$(basename "${VERSION}" -SNAPSHOT)

sed --in-place "s/\"version\": \".\+\",/\"version\": \"${MANIFEST_VERSION}\",/" antiprint-extension/src/main/extension/manifest.json

TMPFILE=$(mktemp --tmpdir selenium-version-XXXXXXXXXX.txt)
mvn --quiet org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=selenium.version -Doutput="${TMPFILE}"
SELENIUM_VERSION=$(cat ${TMPFILE})
mvn --quiet -f extensible-firefox-webdriver/pom.xml versions:set -DnewVersion="${SELENIUM_VERSION}x${VERSION}" -DgenerateBackupPoms=false

rm -f "${TMPFILE}"
