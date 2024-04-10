#!/bin/bash

# Default values
DEFAULT_PORT=8080
EXPOSED_PORT=$DEFAULT_PORT

DEFAULT_IMAGE_NAME="client-server-arm64"

# The JAR file
CLIENT_SERVER_JAR="./client-server/out/artifacts/client_server_jar/client-server.jar"

# Check if running as root
if [ $UID -ne 0 ]; then
    echo "You must run as root. Exiting..."
    exit
fi

# Create builder instance
docker buildx create --platform linux/arm64 --name arm64-builder

# Use the builder instance
docker buildx use arm64-builder

echo "Do you want to generate a new Dockerfile [y/n]:"
read GEN_DOCKERFILE

if [[ $GEN_DOCKERFILE == "y" || $GEN_DOCKERFILE == "Y" ]]; then

	echo "Enter the port to be exposed:"
	read PORT
	
	if [[ -n $PORT ]]; then
		EXPOSED_PORT=$PORT
	fi

	echo "Exposing port: $EXPOSED_PORT"
	echo "Generating Dockerfile..."

	# Create Dockerfile using heredoc with indentation
	cat > Dockerfile <<-EOF
	# Base image
	FROM alpine:latest

	# Installing OpenJDK 17
	RUN apk add --no-cache openjdk17

	# Setting the work directory
	WORKDIR /app

	# Copying the JAR file into the Docker image
	COPY $CLIENT_SERVER_JAR /app/client-server.jar

	# Informing the user that port $DEFAULT_PORT needs to open
	EXPOSE $EXPOSED_PORT
	
	EOF

else
	echo "Using old Dockerfile..."
fi

# Build the Docker image
docker buildx build --platform linux/arm64 --tag $DEFAULT_IMAGE_NAME --load .

# If you want to push the image
echo "Do you want to push the image to the repo [y/n]?"
read PUSH_IMG

if [[ $PUSH_IMG == "y" || $PUSH_IMG == "Y" ]]; then

	# Login to Docker Hub
	docker login
	
	echo "Your Docker Hub username:"
	read USERNAME
	
	# Tag the image
	docker tag $DEFAULT_IMAGE_NAME $USERNAME/$DEFAULT_IMAGE_NAME
	
	# Push the image
	sudo docker push $USERNAME/$DEFAULT_IMAGE_NAME
	
	# Remove the tag
	docker rmi $USERNAME/$DEFAULT_IMAGE_NAME

fi

# Change to default
docker buildx use default
docker buildx rm arm64-builder
