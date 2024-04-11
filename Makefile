.PHONY: start-layers test-layers kill-layers push-img build-docker-client-img

TEST_PROJ_DIR := application/layer-control/
RPI_OS_DIR := raspberry-pi-and-kubernetes/
DOCKER_BUILD_DIR := application/

# Start layers on localhost
start-layers:
	$(MAKE) -C $(TEST_PROJ_DIR) start-layers

# Test layers on localhost
test-layers:
	$(MAKE) -C $(TEST_PROJ_DIR) test-layers

# Kill layers
kill-layers:
	$(MAKE) -C $(TEST_PROJ_DIR) kill-layers

# Push Raspberry Pi OS to SD card
push-img:
	$(MAKE) -C $(RPI_OS_DIR) push-img

# Build Docker image
build-docker-client-img:
	$(MAKE) -C $(DOCKER_BUILD_DIR) build-docker-client-img

