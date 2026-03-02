#!/bin/sh
set -e

# /app is read-only in restricted namespace, so we create a workspace for test output
mkdir -p /workspace/build
cd /app

# Run tests with build output redirected to writable /workspace
exec gradle :e2e-tests:test \
  --no-daemon \
  --project-cache-dir=/workspace/.gradle \
  -Dorg.gradle.project.buildDir=/workspace/build
