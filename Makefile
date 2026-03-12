start-dev:
	$(eval FILES := -f compose.yaml)
	$(foreach service,$(filter-out start-dev,$(MAKECMDGOALS)),$(eval FILES += -f docker/compose.dev.$(service).yaml))
	docker compose $(FILES) up -d

stop-dev:
	docker compose down -v

clean:
	sudo $(RM) -rf ./cache/ ./data/

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

# Catch-all rule to prevent "No rule to make target ..." errors
%:
	@:
