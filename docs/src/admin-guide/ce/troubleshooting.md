---
title: Troubleshooting
---

# Troubleshooting Cytomine

## "_Network error during installation_"

If a previous installation of Cytomine exists, network-related errors may occur during the installation of the new version.

```bash
Cytomine-community-edition/docker-compose.yml: the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion
[+] Running 121/121
 ✔ pims-cache Pulled                                                              22.8s
 ✔ app-engine Pulled                                                              14.4s
 ✔ pims Pulled                                                                    38.6s
 ✔ registry Pulled                                                                21.3s
 ✔ core Pulled                                                                    12.0s
 ✔ postgis Pulled                                                                 23.5s
 ✔ web_ui Pulled                                                                  15.9s
 ✔ mongo Pulled                                                                   14.4s
 ✔ nginx Pulled                                                                   13.2s
[+] Running 1/1
 ✘ Network cytomine-ce_cytomine-network  Error                                     0.0s
failed to create network cytomine-ce_cytomine-network: Error response from daemon: invalid pool request: Pool overlaps with other one on this address space
```

To solve this issue, you will have to remove the old cytomine network:

```bash
docker network rm cytomine-ce_cytomine-network
```

Afterward, you will need to rerun the Docker Compose command:

```bash
docker compose up -d
```

> This issue occurs because the current version uses static IP assignment for containers, whereas the previous version assigned IPs dynamically.
