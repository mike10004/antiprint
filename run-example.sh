#!/bin/bash

set -e

URL="https://ipleak.net/"
DATA_DIR=example-data-dir
CHROME=$(which google-chrome || chromium-browser)
"${CHROME}" --no-first-run --no-default-browser-check --user-data-dir="${DATA_DIR}" --load-extension=antiprint-extension "${URL}"
