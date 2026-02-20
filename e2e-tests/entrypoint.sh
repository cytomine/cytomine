#!/bin/sh
set -e
mkdir -p /workspace
cp -r /app/* /workspace/
cd /workspace
exec gradle :e2e-tests:test --no-daemon
