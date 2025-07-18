# syntax=docker/dockerfile:1

# Copyright (c) 2009-2022. Authors: see NOTICE file.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

ARG MONGO_VERSION=4.4.18-focal
ARG ENTRYPOINT_SCRIPTS_VERSION=1.3.0
ARG IMAGE_VERSION
ARG IMAGE_REVISION

#######################################################################################
## Stage 1: entrypoint script. Use a multi-stage because COPY --from cannot interpolate variables
FROM cytomine/entrypoint-scripts:${ENTRYPOINT_SCRIPTS_VERSION} AS entrypoint-scripts

## Stage 2: mongo image
FROM mongo:${MONGO_VERSION}

RUN rm /etc/apt/sources.list.d/mongodb*.list \
    && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 656408E390CFB1F5 \
    && apt-get update \
    && apt-get install -y --no-install-recommends cron=3.0pl1-136ubuntu1 \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /docker-entrypoint-cytomine.d/ /data/db/backup
COPY --from=entrypoint-scripts --chmod=774 /cytomine-entrypoint.sh /usr/local/bin/
COPY --from=entrypoint-scripts --chmod=774 /envsubst-on-templates-and-move.sh /docker-entrypoint-cytomine.d/500-envsubst-on-templates-and-move.sh
COPY --chmod=774 files/start-crond.sh /docker-entrypoint-cytomine.d/600-start-crond.sh

# backup and restore scripts
COPY files/backup-cron-job /backup-cron-job
COPY files/cytomine-mongo-backup.sh /usr/local/bin/backup
COPY files/cytomine-mongo-restore.sh /usr/local/bin/restore

RUN chmod +x /usr/local/bin/backup /usr/local/bin/restore /docker-entrypoint-cytomine.d/600-start-crond.sh && \
    chmod 0644 /backup-cron-job && \
    chmod u+s /usr/bin/crontab && \
    touch /data/db/backup/backup.log && \
    chmod 777 /data/db/backup/backup.log && \
    crontab /backup-cron-job

VOLUME ["/data/db"]

ENTRYPOINT ["cytomine-entrypoint.sh", "docker-entrypoint.sh"]
CMD ["mongod"]
