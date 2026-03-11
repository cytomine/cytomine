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

# Push images to local registry (starts registry first if needed)
push-local:
	@echo "Starting local registry..."
	docker compose up -d registry
	@sleep 2
	@echo "Tagging and pushing images to local registry..."
	@for service in $(SERVICES); do \
		echo "Pushing $$service..."; \
		docker tag cytomine/$$service:latest $(LOCAL_REGISTRY)/cytomine/$$service:latest; \
		docker push $(LOCAL_REGISTRY)/cytomine/$$service:latest; \
	done
	@echo "All images pushed to $(LOCAL_REGISTRY)"

# Start infrastructure services (databases, k3s, registry)
start-infra:
	@echo "Starting infrastructure services..."
	docker compose up -d mongo postgis registry redis pims-cache iam-db app-engine-db k3s
	@echo "Waiting for k3s to be ready..."
	@until KUBECONFIG=$(KUBECONFIG) kubectl get nodes >/dev/null 2>&1; do \
		echo "Waiting for k3s..."; \
		sleep 3; \
	done
	@echo "k3s is ready"

# Deploy helm chart to local k3s
helm-local:
	@echo "Creating namespace..."
	-KUBECONFIG=$(KUBECONFIG) kubectl create namespace $(HELM_NAMESPACE)
	KUBECONFIG=$(KUBECONFIG) kubectl apply -f k3s/cytomine-local-ns.yaml
	@echo "Installing/upgrading helm chart..."
	KUBECONFIG=$(KUBECONFIG) helm upgrade --install $(HELM_RELEASE) ./helm/charts/cytomine \
		-f ./helm/charts/cytomine/local/values.yaml \
		--set images.app_engine=$(LOCAL_REGISTRY)/cytomine/app-engine:latest \
		--set images.core=$(LOCAL_REGISTRY)/cytomine/core:latest \
		--set images.pims=$(LOCAL_REGISTRY)/cytomine/pims:latest \
		--set images.web_ui=$(LOCAL_REGISTRY)/cytomine/web-ui:latest \
		--set images.cbir=$(LOCAL_REGISTRY)/cytomine/cbir:latest \
		--set images.sam=$(LOCAL_REGISTRY)/cytomine/sam:latest \
		--set images.nginx=$(LOCAL_REGISTRY)/cytomine/nginx:latest \
		--set images.repository=$(LOCAL_REGISTRY)/cytomine/repository:latest \
		--set images.iam=$(LOCAL_REGISTRY)/cytomine/iam:latest \
		--set images.pullPolicy=Always \
		-n $(HELM_NAMESPACE) \
		--wait --timeout 10m
	@echo "Helm deployment complete"

# Start local k3s environment with helm
start-local: start-infra helm-local
	@echo "Local k3s environment is running"
	@echo "Access Cytomine at http://cytomine.local (add to /etc/hosts: 127.0.0.1 cytomine.local)"

# Full local workflow: build, push, and start
local: build push-local start-local
	@echo "Local environment ready!"

# Stop local k3s environment
stop-local:
	@echo "Uninstalling helm release..."
	-KUBECONFIG=$(KUBECONFIG) helm uninstall $(HELM_RELEASE) -n $(HELM_NAMESPACE) 2>/dev/null
	@echo "Stopping docker compose services..."
	docker compose down

# Catch-all rule to prevent "No rule to make target ..." errors
%:
	@:
