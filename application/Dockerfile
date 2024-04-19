# Base image
FROM alpine:latest

# Installing OpenJDK 17
RUN apk add --no-cache openjdk17

# Setting the work directory
WORKDIR /app

# Copying the JAR file into the Docker image
COPY ./client-server/out/artifacts/client_server_jar/client-server.jar /app/client-server.jar

# Setting the environment variables
ENV NODE_NAME RaspberryPi
ENV SERVER_IP localhost
ENV SERVER_PORT 8080
ENV REPORTING_PERIOD 1

# Informing the user that port 8080 needs to open
EXPOSE 8080

# The command that will be executed upon running an instance
CMD java -jar client-server.jar ${NODE_NAME} ${SERVER_IP} ${SERVER_PORT} ${REPORTING_PERIOD}

