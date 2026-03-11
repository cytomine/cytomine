# Local registry address (must match k3s/registries.yaml)
LOCAL_REGISTRY := 172.16.238.4:5000

# Services to build (from compose.override.yaml)
SERVICES := web-ui pims iam app-engine core cbir sam nginx repository

# Kubeconfig for local k3s
KUBECONFIG := $(PWD)/.kube/shared/config

# Helm release name and namespace
HELM_RELEASE := cytomine
HELM_NAMESPACE := cytomine-local

.PHONY: start-dev stop-dev clean build push-local start-infra start-local local stop-local helm-local

start-dev:
	$(eval FILES := -f compose.yaml)
	$(foreach service,$(filter-out start-dev,$(MAKECMDGOALS)),$(eval FILES += -f docker/compose.dev.$(service).yaml))
	docker compose $(FILES) up -d

stop-dev:
	docker compose down -v

clean:
	sudo $(RM) -rf ./cache/ ./data/

# Build all Docker images
build:
	docker compose -f compose.yaml -f compose.override.yaml build

# Start K3s from helm/docker-compose.yaml
start-k3s:
	docker-compose -f ./helm/compose.yaml up -d

# Save docker images to k3s
push-local:
	@for service in $(SERVICES); do \
		echo "Pushing cytomine/$$service:latest to k3s..."; \
		docker save cytomine/$$service:latest | docker exec -i helm-k3s-1 ctr -n k8s.io images import -; \
	done

# Deploy helm cytomine to k3s
helm-install:
	helm upgrade --kubeconfig=./.kube/shared/config -f ./helm/charts/cytomine/example/values.yaml cytomine-platform ./helm/charts/cytomine/ -n cytomine-local --install
