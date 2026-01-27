start-dev:
	$(eval FILES := -f compose.yaml)
	$(foreach service,$(filter-out start-dev,$(MAKECMDGOALS)),$(eval FILES += -f docker/compose.dev.$(service).yaml))
	docker compose $(FILES) up -d --build

stop-dev:
	docker compose down -v

clean:
	sudo $(RM) -rf ./cache/ ./data/

# Catch-all rule to prevent "No rule to make target ..." errors
%:
	@:
