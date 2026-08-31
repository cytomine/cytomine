#!/bin/sh
set -e

# Substitute environment variables in configuration.json

if [ "$NODE_ENV" = "development" ]; then
  envsubst < /app/scripts/configuration.json > /app/public/configuration.json
else
  # Write to /tmp to support read-only root filesystems (k8s)
  envsubst < /app/configuration.json.template > /tmp/configuration.json
fi

exec "$@"
