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
