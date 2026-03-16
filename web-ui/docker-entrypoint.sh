#!/bin/sh
set -e

# Substitute environment variables in configuration.json
# Write to /tmp to support read-only root filesystems (k8s)
envsubst < /app/configuration.json.template > /tmp/configuration.json

exec "$@"
