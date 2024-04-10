#!/bin/bash

# Default values
source ../../config.sh

# If not declared or empty
if [[ ! -v CLIENT_SERVER_NUMBER || -z $CLIENT_SERVER_NUMBER || $CLIENT_SERVER_NUMBER -le 0 ]]; then
	CLIENT_SERVER_NUMBER=3
fi

if [[ ! -v DEFAULT_SERVER_PORT || -z $DEFAULT_SERVER_PORT ]]; then
	DEFAULT_SERVER_PORT=8080
fi

if [[ ! -v DEFAULT_SERVER_IP || -z $DEFAULT_SERVER_IP ]]; then
	DEFAULT_SERVER_IP=localhost
fi

for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

	default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
	port_i=$((DEFAULT_SERVER_PORT + i))

	if [[ ! -v $default_client_server_port_i || -z ${!default_client_server_port_i} ]]; then
		eval "$default_client_server_port_i=$port_i"
	fi

done

for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

	client_server_reporting_period_i="CLIENT_SERVER_REPORTING_PERIOD_$i"
	
	if [[ ! -v $client_server_reporting_period_i || -z ${!client_server_reporting_period_i} ]]; then
		eval "$client_server_reporting_period_i=3"
	fi	
	
done

for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

	client_server_node_name_i="CLIENT_SERVER_NODE_NAME_$i"
	
	if [[ ! -v $client_server_node_name_i || -z ${!client_server_node_name_i} ]]; then
		eval "$client_server_node_name_i=RaspberryPi-$i"
	fi

done

# The JAR file
CLIENT_SERVER_JAR="../client-server/out/artifacts/client_server_jar/client-server.jar"

# Check if JAR file exists
if [ ! -f "$CLIENT_SERVER_JAR" ]; then
    echo "JAR file not found: '$CLIENT_SERVER_JAR', make sure to build it with Maven!"
    exit 1
fi

echo "Do you want to kill all processes by one keypress? [y/n]"
read KILL_BY_KEYPRESS

echo "Do you want to change the default values? [y/n]"
read CHANGE_CONFIG

if [[ $CHANGE_CONFIG == "y" || $CHANGE_CONFIG == "Y" ]]; then

	echo "Entering configuration, on each entry press enter if you wish to use the default value"

	for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

		echo "Enter the port number to listen to for client-server-$i:"	
		listening_port_i="LISTENING_PORT_$i"
		default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
		read PORT

		if [[ -n $PORT ]]; then
			eval "$listening_port_i=-Dserver.port=$PORT"
			echo "client-server-$i listening on port: $PORT"
		else
			eval "$listening_port_i=-Dserver.port=$default_client_server_port_i"
			echo "client-server-$i listening on port: $default_client_server_port_i"
		fi

	done

	# Enter the IP address of server
	echo "Enter the IP address of server:"
	read IP_ADDRESS

	if [[ -n $IP_ADDRESS ]]; then
		IP_ADDRESS_OF_SERVER=$IP_ADDRESS
		echo "server IP address is: $IP_ADDRESS"
	else
		IP_ADDRESS_OF_SERVER=$DEFAULT_SERVER_IP
		echo "server IP address is: $DEFAULT_SERVER_IP"
	fi

	# Enter the port number of server
	echo "Enter the port number of server:"
	read PORT

	if [[ -n $PORT ]]; then
		PORT_OF_SERVER=$PORT
		echo "server port is: $PORT"
	else
		PORT_OF_SERVER=$DEFAULT_SERVER_PORT
		echo "server port is: $DEFAULT_SERVER_PORT"
		
	fi

else

	echo "Proceeding with default values:"

	for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

		default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
		listening_port_i="LISTENING_PORT_$i"
		eval "$listening_port_i=\"-Dserver.port=${!default_client_server_port_i}\""
		
	done

	IP_ADDRESS_OF_SERVER=$DEFAULT_SERVER_IP
	echo "server IP address is: $DEFAULT_SERVER_IP"

	PORT_OF_SERVER=$DEFAULT_SERVER_PORT
	echo "server port is: $DEFAULT_SERVER_PORT"

fi

# Launch instances of client-server
echo "Starting $CLIENT_SERVER_NUMBER client-server.jar instances..."

for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

	client_server_node_name_i="CLIENT_SERVER_NODE_NAME_$i"
	client_server_reporting_period_i="CLIENT_SERVER_REPORTING_PERIOD_$i"
	listening_port_i="LISTENING_PORT_$i"

	CLIENT_SERVER_LAUNCH="java ${!listening_port_i} -jar $CLIENT_SERVER_JAR ${!client_server_node_name_i} $IP_ADDRESS_OF_SERVER $PORT_OF_SERVER ${!client_server_reporting_period_i} &"
	echo "$CLIENT_SERVER_LAUNCH"
	eval "$CLIENT_SERVER_LAUNCH"

done

if [[ $KILL_BY_KEYPRESS == "y" || $KILL_BY_KEYPRESS == "Y" ]]; then

	echo "Press any key to stop all instances:"
	read -s -n 1

	# Find the PID-s
	PIDS=$(pgrep -f "java.*-jar.*$CLIENT_SERVER_JAR.*")

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

