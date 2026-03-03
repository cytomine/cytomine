#!/bin/sh
set -e

mkdir -p /workspace
cp -r /app/* /workspace/
cd /workspace

echo "Waiting for Selenium to be ready..."
until wget -q --spider "${SELENIUM_URL:-http://localhost:4444}/wd/hub/status"; do
  echo "Selenium not ready, retrying in 2s..."
  sleep 2
done
echo "Selenium is ready!"

exec gradle :e2e-tests:test --no-daemon
