#!/bin/sh
set -e

mkdir -p /workspace
cp -r /app/* /workspace/
cd /workspace

SELENIUM="${SELENIUM_URL:-http://localhost:4444}"

echo "Waiting for Selenium to be ready..."
until wget -qO- "${SELENIUM}/wd/hub/status" 2>/dev/null | grep -q 'ready'; do
  echo "Selenium not ready, retrying in 2s..."
  sleep 2
done
# Extra wait for Selenium to fully initialize
sleep 5
echo "Selenium is ready!"

exec gradle :e2e-tests:test --no-daemon
