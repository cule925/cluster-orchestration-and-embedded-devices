# Base image
FROM alpine:latest

# Installing OpenJDK 17
RUN apk add --no-cache openjdk17

# Setting the work directory
WORKDIR /app

# Copying the JAR file into the Docker image
COPY ./client-server/out/artifacts/client_server_jar/client-server.jar /app/client-server.jar

# Informing the user that port 8080 needs to open
EXPOSE 8080

