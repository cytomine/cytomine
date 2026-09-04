#! /usr/bin/env bash

set -e

CONCURRENCY="${WEB_CONCURRENCY:-4}"
LOG_LEVEL="${LOG_LEVEL:-INFO}"

# If there's a prestart.sh script in the /app directory or other path specified, run it before starting
PRE_START_PATH=${PRE_START_PATH:-/app/prestart.sh}
echo "Checking for script in $PRE_START_PATH"
if [ -f $PRE_START_PATH ] ; then
    echo "Running script $PRE_START_PATH"
    . "$PRE_START_PATH"
else
    echo "There is no script $PRE_START_PATH"
fi

celery -A pims.tasks.worker worker -l ${LOG_LEVEL} -Q pims-import-queue -c ${CONCURRENCY}
