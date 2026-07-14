	# Local registry address (must match k3s/registries.yaml)
LOCAL_REGISTRY := 172.16.238.4:5000

# Services to build (from compose.override.yaml)
SERVICES := web-ui pims iam app-engine core cbir sam repository e2e-tests

.PHONY: start-dev stop-dev clean build push-local

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

start-docker-gpu:
	docker compose -f compose.yaml -f compose.override.yaml -f docker/compose.gpu.k3s.yaml up -d

# Start K3s from helm/docker-compose.yaml
start-k3s:
	docker compose -f ./helm/compose.yaml up -d

# Save docker images to k3s
push-local:
	@docker build -f ./e2e-tests/Dockerfile . -t cytomine/e2e-tests:latest
	@for service in $(SERVICES); do \
		echo "Pushing cytomine/$$service:latest to k3s..."; \
		docker save cytomine/$$service:latest | docker exec -i helm-k3s-1 ctr -n k8s.io images import -; \
	done
	@echo "Deleting all pods to pick up new images..."
	@kubectl --kubeconfig=./.kube/shared/config delete pods --all -n cytomine-local

# Deploy helm cytomine to k3s
helm-install:
	helm upgrade --kubeconfig=./.kube/shared/config -f ./helm/charts/cytomine/example/values.yaml cytomine-platform ./helm/charts/cytomine/ -n cytomine-local --install

doctor:
	@echo "Checking required tools..."
	@command -v docker >/dev/null 2>&1 || { echo "docker: NOT FOUND"; exit 1; } && echo "docker: OK"
	@command -v kubectl >/dev/null 2>&1 || { echo "kubectl: NOT FOUND"; exit 1; } && echo "kubectl: OK"
	@command -v helm >/dev/null 2>&1 || { echo "helm: NOT FOUND"; exit 1; } && echo "helm: OK"
	@echo "All required tools are installed"

init-secrets:
	@echo "Create .kube/secrets/ folder"
	@mkdir -p ./.kube/secrets/
	@if [ ! -f ./dockerauth.json ]; then \
		echo "File not found: ./dockerauth.json"; \
		echo "You should copy your ~/.docker/config.json here as dockerauth.json"; \
		exit 1; \
	fi
	@echo "Reading dockerauth.json and creating secret..."
	@SECRET=$$(base64 -w 0 ./dockerauth.json) && sed "s|<SECRET>|$$SECRET|g" helm/docker-image-pull-secrets.yaml > ./.kube/secrets/docker-image-pull-secrets.yaml
	@echo "Created .kube/secrets/docker-image-pull-secrets.yaml"
	@echo "You're good to go"

start-k3s-cluster:
	$(eval FILES := -f helm/compose.yaml)
	$(if $(wildcard .kube/secrets/registries.yaml),$(eval FILES += -f helm/docker/compose.registries.yaml))
	$(if $(wildcard .kube/secrets/lsaai-secrets.yaml),$(eval FILES += -f helm/docker/compose.lsaai.yaml))
	$(if $(wildcard .kube/secrets/docker-image-pull-secrets.yaml),$(eval FILES += -f helm/docker/compose.docker-pull.yaml))
	docker compose $(FILES) up

deploy-helm:
	@helm upgrade --kubeconfig=./.kube/shared/config -f ./helm/charts/cytomine/example/values.yaml cytomine-platform ./helm/charts/cytomine/ -n cytomine-local --install

run-e2e:
	sudo mkdir -p ./data/dataset/test-project/IMAGES
	sudo mkdir -p ./data/dataset/test-project/METADATA
	sudo cp ./e2e-tests/src/test/resources/image.xml ./data/dataset/test-project/METADATA/image.xml
	sudo cp ./e2e-tests/src/test/resources/policy.xml ./data/dataset/test-project/METADATA/policy.xml
	sudo cp ./e2e-tests/src/test/resources/dataset.xml ./data/dataset/test-project/METADATA/dataset.xml
	sudo cp ./e2e-tests/src/test/resources/observer.xml ./data/dataset/test-project/METADATA/observer.xml
	sudo cp ./e2e-tests/src/test/resources/observation.xml ./data/dataset/test-project/METADATA/observation.xml
	sudo cp ./e2e-tests/src/test/resources/staining.xml ./data/dataset/test-project/METADATA/staining.xml
	sudo cp ./e2e-tests/src/test/resources/sample.xml ./data/dataset/test-project/METADATA/sample.xml

	sudo cp ./e2e-tests/src/test/resources/cat.png ./data/dataset/test-project/IMAGES/cat.png

	kubectl --kubeconfig=./.kube/shared/config -n cytomine-local create job --from=cronjob/pims-import pims-import-local
	kubectl --kubeconfig=./.kube/shared/config -n cytomine-local wait --for=condition=complete job/pims-import-local --timeout=120s

	SPRING_PROFILES_ACTIVE=local-k3s CYTOMINE_ADMIN_PASSWORD=$$(kubectl --kubeconfig=./.kube/shared/config -n cytomine-local get secrets cytomine-admin-iam-secret -o json | jq -r .data.password | base64 -d -) ./gradlew :e2e-tests:test || true

	kubectl --kubeconfig=./.kube/shared/config -n cytomine-local delete job pims-import-local

# Catch-all rule to prevent "No rule to make target ..." errors
%:
	@:
