#!/bin/bash

# Default values
DEFAULT_PORT=8080
EXPOSED_PORT=$DEFAULT_PORT

DEFAULT_PLATFORM=arm64

DEFAULT_IMAGE_NAME="client-server"
DEFAULT_IMAGE_TAG="latest"

DEFAULT_NODE_NAME=RaspberryPi
DEFAULT_SERVER_IP=localhost
DEFAULT_SERVER_PORT=8080
DEFAULT_REPORTING_PERIOD=5

# The JAR file
CLIENT_SERVER_JAR="./client-server/out/artifacts/client_server_jar/client-server.jar"

# Check if running as root
# if [ $UID -ne 0 ]; then
#    echo "You must run as root. Exiting..."
#    exit
# fi

echo "Building for ARM64 or AMD64? [arm64/amd64]"
read PLATFORM

if [[ $PLATFORM != "arm64" && $PLATFORM != "amd64" ]]; then
	PLATFORM=$DEFAULT_PLATFORM
fi
echo "Platform chosen: $PLATFORM"

# Create builder instance
docker buildx create --platform linux/$PLATFORM --name $PLATFORM-builder

# Use the builder instance
docker buildx use $PLATFORM-builder

echo "Do you want to generate a new Dockerfile [y/n]:"
read GEN_DOCKERFILE

if [[ $GEN_DOCKERFILE == "y" || $GEN_DOCKERFILE == "Y" ]]; then

	echo "Enter the port to be exposed:"
	read PORT
	
	if [[ -n $PORT ]]; then
		EXPOSED_PORT=$PORT
	fi
	echo "Exposing port: $EXPOSED_PORT"
	
	echo "Enter the node name:"
	read NODE_NAME
	
	if [[ -z $NODE_NAME ]]; then
		NODE_NAME=$DEFAULT_NODE_NAME
	fi
	echo "Node name: $NODE_NAME"
	
	echo "Enter the server IP:"
	read SERVER_IP
	
	if [[ -z $SERVER_IP ]]; then
		SERVER_IP=$DEFAULT_SERVER_IP
	fi
	echo "Server IP: $SERVER_IP"
	
	echo "Enter the server port:"
	read SERVER_PORT
	
	if [[ -z $SERVER_PORT ]]; then
		SERVER_PORT=$DEFAULT_SERVER_PORT
	fi
	echo "Server port: $SERVER_PORT"
	
	echo "Enter the reporting period:"
	read REPORTING_PERIOD
	
	if [[ -z $REPORTING_PERIOD ]]; then
		REPORTING_PERIOD=$DEFAULT_REPORTING_PERIOD
	fi
	echo "Reporting period: $REPORTING_PERIOD"

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

	# Setting the environment variables
	ENV NODE_NAME $NODE_NAME
	ENV SERVER_IP $SERVER_IP
	ENV SERVER_PORT $SERVER_PORT
	ENV REPORTING_PERIOD $REPORTING_PERIOD

	# Informing the user that port $DEFAULT_PORT needs to open
	EXPOSE $EXPOSED_PORT
	
	# The command that will be executed upon running an instance
	CMD java -jar client-server.jar \${NODE_NAME} \${SERVER_IP} \${SERVER_PORT} \${REPORTING_PERIOD}
	
	EOF

else
	echo "Using old Dockerfile..."
fi

echo "Enter the image name:"
read IMAGE_NAME
	
if [[ -z $IMAGE_NAME ]]; then
	IMAGE_NAME="$DEFAULT_IMAGE_NAME-$PLATFORM"
fi
echo "Image name: $IMAGE_NAME"

echo "Enter the image tag:"
read IMAGE_TAG
	
if [[ -z $IMAGE_TAG ]]; then
	IMAGE_TAG=$DEFAULT_IMAGE_TAG
fi
echo "Image tag: $IMAGE_TAG"

# Build the Docker image
docker buildx build --platform linux/$PLATFORM --tag $IMAGE_NAME:$IMAGE_TAG --load .

# If you want to push the image
echo "Do you want to push the image to the repo [y/n]?"
read PUSH_IMG

if [[ $PUSH_IMG == "y" || $PUSH_IMG == "Y" ]]; then

	# Login to Docker Hub
	docker login
	
	echo "Your Docker Hub username:"
	read USERNAME
	
	# Tag the image
	docker tag $IMAGE_NAME:$IMAGE_TAG $USERNAME/$IMAGE_NAME:$IMAGE_TAG
	
	# Push the image
	docker push $USERNAME/$IMAGE_NAME:$IMAGE_TAG
	
	# Remove the tag
	docker rmi $USERNAME/$IMAGE_NAME:$IMAGE_TAG

fi

# Change to default
docker buildx use default
docker buildx rm $PLATFORM-builder
