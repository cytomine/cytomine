#!/bin/sh
set -e

# Substitute environment variables in configuration.json
envsubst < /app/configuration.json.template > /app/configuration.json

exec "$@"
