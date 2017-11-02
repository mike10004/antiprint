#!/bin/bash

PROG="pack-extension"

if [ -z "${CHROME_EXECUTABLE}" ] ; then
  CHROME=$(which google-chrome || which chromium-browser)
  if [ -z "${CHROME}" ] ; then
    echo "${PROG}: chrome is not installed or executable is not on PATH" >&2
    exit 1
  fi
else
  CHROME="${CHROME_EXECUTABLE}"
fi

if [ ! -f "${CHROME}" ] ; then
  echo "${PROG}: not found: ${CHROME}" >&2
  exit 1
fi

KEYFILE=$1

EXT_SRC_DIRNAME="antiprint-extension"
DEFAULT_KEY="${PWD}/${EXT_SRC_DIRNAME}.pem"

if [ -n "${KEYFILE}" -a -f "${KEYFILE}" ] ; then
  KEYARG="--pack-extension-key=${KEYFILE}"
else
  if [ -f "${DEFAULT_KEY}" ] ; then
    echo "${PROG}: reusing existing key ${DEFAULT_KEY}" >&2
    KEYARG="--pack-extension-key=${DEFAULT_KEY}"
  else
    echo "${PROG}: new key file will be generated at ${DEFAULT_KEY}" >&2
  fi
fi

CRX_PATH="${PWD}/${EXT_SRC_DIRNAME}.crx"

rm -vf "${CRX_PATH}"

"${CHROME}" --disable-gpu --no-sandbox --pack-extension="${PWD}/${EXT_SRC_DIRNAME}" "${KEYARG}"
STATUS=$?
if [ ${STATUS} -ne 0 ] ; then
  echo "${PROG}: ${CHROME} exited with status ${STATUS}"
  exit ${STATUS}
fi

if [ ! -f "${CRX_PATH}" ] ; then
  echo "${PROG}: extension was not created but Chrome exited clean" >&2
  exit 2
fi

echo "${CRX_PATH}"
