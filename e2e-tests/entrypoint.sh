#!/bin/sh
set -e
cp -r /app/* /workspace/
cd /workspace
exec gradle :e2e-tests:test --no-daemon
