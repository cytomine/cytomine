#! /usr/bin/env bash

SCRIPT_PATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
VERSION=$(cd $SCRIPT_PATH && cd .. && python -c "from pims import __version__; print(__version__)")
NAMESPACE=cytomineuliege

build_without_plugins() {
  TAG="v${VERSION}"
  # PIMS core
  docker build -f ../docker/backend.dockerfile \
  -t ${NAMESPACE}/pims:${TAG} \
  ..
  # PIMS worker
  docker build -f ../docker/worker.dockerfile \
  --build-arg FROM_NAMESPACE=$NAMESPACE \
  --build-arg FROM_VERSION=$TAG \
  -t ${NAMESPACE}/pims-worker:${TAG} \
  ..
}

build_with_community_plugins() {
  PLUGIN_CSV=$(cat ./plugin-list.csv)
  TAG="v${VERSION}-community-plugins"
  # PIMS core
  docker build -f ../docker/backend.dockerfile \
  --build-arg PLUGIN_CSV="${PLUGIN_CSV}" \
  -t ${NAMESPACE}/pims:${TAG} \
  ..
  # PIMS worker
  docker build -f ../docker/worker.dockerfile \
  --build-arg FROM_NAMESPACE=$NAMESPACE \
  --build-arg FROM_VERSION=$TAG \
  -t ${NAMESPACE}/pims-worker:${TAG} \
  ..
}

build_without_plugins
build_with_community_plugins
