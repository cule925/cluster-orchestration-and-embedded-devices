#!/bin/bash

# Default values
source ../../config.sh

# If not declared or empty
if [[ ! -v DEFAULT_SERVER_PORT || -z $DEFAULT_SERVER_PORT ]]; then
	DEFAULT_SERVER_PORT=8080
fi

if [[ ! -v SERVER_REQUEST_PERIOD || -z $SERVER_REQUEST_PERIOD ]]; then
	SERVER_REQUEST_PERIOD=3
fi

# The default listening port
LISTENING_PORT="-Dserver.port=$DEFAULT_SERVER_PORT"

# The JAR file
SERVER_JAR="../server/out/artifacts/server_jar/server.jar"

# Check if JAR file exists
if [ ! -f "$SERVER_JAR" ]; then
    echo "JAR file not found: '$SERVER_JAR', make sure to build it with Maven!"
    exit 1
fi

echo "Do you want to kill all processes by one keypress? [y/n]"
read KILL_BY_KEYPRESS

echo "Do you want to change the default values? [y/n]"
read CHANGE_CONFIG

if [[ $CHANGE_CONFIG == "y" || $CHANGE_CONFIG == "Y" ]]; then

	echo "Entering configuration, on each entry press enter if you wish to use the default value"

	# Enter the port number to listen to for server
	echo "Enter the port number to listen to for server:"
	read PORT

	if [[ -n $PORT ]]; then
		LISTENING_PORT="-Dserver.port=$PORT"
		echo "server listening on port $PORT"
	else
		echo "server listening on port $DEFAULT_SERVER_PORT"
	fi

else

	echo "Proceeding with default values:"
	echo "server listening on port $DEFAULT_SERVER_PORT"

fi

# Launch an instance of a server
echo "Starting 1 server.jar instance..."

SERVER_LAUNCH="java $LISTENING_PORT -jar $SERVER_JAR $SERVER_REQUEST_PERIOD &"
echo "$SERVER_LAUNCH"
eval "$SERVER_LAUNCH"

if [[ $KILL_BY_KEYPRESS == "y" || $KILL_BY_KEYPRESS == "Y" ]]; then

	echo "Press any key to stop the instance:"
	read -s -n 1

	# Find the PID-s
	PIDS=$(pgrep -f "java.*-jar.*$SERVER_JAR.*")

	if [ -n "$PIDS" ]; then
	
    		# Iterate over each PID and kill the corresponding process
    		for PID in $PIDS; do
        		echo "Killing process with PID: $PID"
        		kill -9 "$PID"
    		done
	
	else
    		echo "No matching processes found."
	fi

else
	echo "Process running in background"
fi

