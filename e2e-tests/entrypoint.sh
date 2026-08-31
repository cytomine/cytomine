#!/bin/sh
set -e

mkdir -p /workspace
cp -r /app/* /workspace/
cd /workspace

SELENIUM="${SELENIUM_URL:-http://localhost:4444}"

echo "Waiting for Selenium to be ready."
until wget -qO- "${SELENIUM}/wd/hub/status" 2>/dev/null |grep -q '"ready": true'; do
  echo "Selenium not ready, retrying in 2s..."
  sleep 2
done

echo "Selenium is ready. Waiting 60 seconds to be sure"
sleep 60
exec gradle :e2e-tests:test --no-daemon
