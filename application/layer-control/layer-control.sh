#!/bin/bash

# Default values
source ../../config.sh

TEST_START_CLIENT_SCRIPT="start-client-layer.sh"
TEST_START_CLIENT_SERVER_SCRIPT="start-client-server-layer.sh"
TEST_START_SERVER_SCRIPT="start-server-layer.sh"
CLIENT_JAR_NAME="client.jar"
CLIENT_SERVER_JAR_NAME="client-server.jar"
SERVER_JAR_NAME="server.jar"

# Input for client layer script
client_layer_script() {
	
	# Extract first argument
	local script=$1
	
	# Double the size
	local client_server_number_x2=$((2 * CLIENT_SERVER_NUMBER))
	
	# Extract client-server ips and ports
	local client_server_ips_and_ports="${@:2:client_server_number_x2}"
	
	# Substitute spaces with newlines
	"./$script" << EOF
	n
	y
	$(printf "%s\n" "${client_server_ips_and_ports// /$'\n'}")
EOF

}

# Input for client-server layer script
client_server_layer_script() {

	# Extract first argument
	local script=$1
	
	# Extract client-server ports into one variable that seperates the argument with spaces
	local client_server_ports="${@:2:CLIENT_SERVER_NUMBER}"
	
	# Increment argument index
	local arg_index=$((2 + CLIENT_SERVER_NUMBER))
	
	# Extract next argument
	local server_ip="${!arg_index}"
	
	# Increment argument index
	arg_index=$((arg_index + 1))
	
	# Extract next argument
	local server_port="${!arg_index}"
	
	# Substitute spaces with newlines
	"./$script" << EOF
	n
	y
	$(printf "%s\n" "${client_server_ports// /$'\n'}")
	$server_ip
	$server_port
EOF

}

# Input for server layer script
server_layer_script() {
	
	# Extract arguments
	local script=$1
	local server_port=$2
	
	"./$script" << EOF
	n
	y
	$server_port
EOF

}

kill_layer() {

	for jar_file in "$@"; do

		echo "Killing $jar_file"

		# Find the PID-s
		pids=$(pgrep -f "java.*-jar.*/$jar_file.*")

		if [ -n "$pids" ]; then
	
    			# Iterate over each PID and kill the corresponding process
	    		for pid in $pids; do
	        		echo "Killing process with PID: $pid"
	        		kill -9 "$pid"
	    		done
	
		else
		    	echo "No matching processes found."
		fi
	
	done

}

# The command
COMMAND=$1
echo "Command selected: $COMMAND"

if [[ $COMMAND == "start-layers" ]]; then

	echo "Do you want to kill all processes by one keypress? [y/n]"
	read KILL_BY_KEYPRESS

	# Enter server information
	echo "Enter server port (the IP address is localhost):"
	read PORT
	
	if [[ -n $PORT ]]; then
		SERVER_PORT=$PORT
	else
		SERVER_PORT=$DEFAULT_SERVER_PORT
	fi
	echo "server port is: $SERVER_PORT"
	
	# Enter client-server-1 information
	echo "Enter client-server-1 IP address:"
	read IP_ADDRESS
	
	if [[ -n $IP_ADDRESS ]]; then
		CLIENT_SERVER_IP_1=$IP_ADDRESS
	else
		CLIENT_SERVER_IP_1=$DEFAULT_CLIENT_SERVER_IP_1
	fi
	echo "client-server-1 IP address is: $CLIENT_SERVER_IP_1"
	
	echo "Enter client-server-1 port:"
	read PORT
	
	if [[ -n $PORT ]]; then
		CLIENT_SERVER_PORT_1=$PORT
	else
		CLIENT_SERVER_PORT_1=$DEFAULT_CLIENT_SERVER_PORT_1
	fi
	echo "client-server-1 port is: $CLIENT_SERVER_PORT_1"

	# Enter client-server-2 information
	echo "Enter client-server-2 IP address:"
	read IP_ADDRESS
	
	if [[ -n $IP_ADDRESS ]]; then
		CLIENT_SERVER_IP_2=$IP_ADDRESS
	else
		CLIENT_SERVER_IP_2=$DEFAULT_CLIENT_SERVER_IP_2
	fi
	echo "client-server-2 IP address is: $CLIENT_SERVER_IP_2"
	
	echo "Enter client-server-2 port:"
	read PORT
	
	if [[ -n $PORT ]]; then
		CLIENT_SERVER_PORT_2=$PORT
	else
		CLIENT_SERVER_PORT_2=$DEFAULT_CLIENT_SERVER_PORT_2
	fi
	echo "client-server-2 port is: $CLIENT_SERVER_PORT_2"

	# Enter client-server-3 information
	echo "Enter client-server-3 IP address:"
	read IP_ADDRESS
	
	if [[ -n $IP_ADDRESS ]]; then
		CLIENT_SERVER_IP_3=$IP_ADDRESS
	else
		CLIENT_SERVER_IP_3=$DEFAULT_CLIENT_SERVER_IP_3
	fi
	echo "client-server-3 IP address is: $CLIENT_SERVER_IP_3"
	
	echo "Enter client-server-3 port:"
	read PORT
	
	if [[ -n $PORT ]]; then
		CLIENT_SERVER_PORT_3=$PORT
	else
		CLIENT_SERVER_PORT_3=$DEFAULT_CLIENT_SERVER_PORT_3
	fi
	echo "client-server-3 port is: $CLIENT_SERVER_PORT_3"
	
	# Generate ports and add them to the array
	client_server_ips_and_ports=()
	for i in $(seq 1 $CLIENT_SERVER_NUMBER); do
		client_server_ip_i="CLIENT_SERVER_IP_$i"
		client_server_port_i="CLIENT_SERVER_PORT_$i"
		client_server_ips_and_ports+=("${!client_server_ip_i}")
		client_server_ips_and_ports+=("${!client_server_port_i}")
	done
	
	# Start the top and bottom layer in the background
	server_layer_script $TEST_START_SERVER_SCRIPT $SERVER_PORT
	client_layer_script $TEST_START_CLIENT_SCRIPT "${client_server_ips_and_ports[@]}"
	
	if [[ $KILL_BY_KEYPRESS == "y" || $KILL_BY_KEYPRESS == "Y" ]]; then

		echo "Press any key to stop all instances:"
		read -s -n 1

		# Kill each layer
		kill_layer $SERVER_JAR_NAME $CLIENT_JAR_NAME

	else
		echo "Processes are running in background, run 'make kill_network' to kill them all"
	fi
	
elif [[ $COMMAND == "test-layers" ]]; then

	echo "Do you want to kill all processes by one keypress? [y/n]"
	read KILL_BY_KEYPRESS

	echo "Testing locally with default values, visit http://$DEFAULT_SERVER_IP:$DEFAULT_SERVER_PORT"
	
	# Generate ports and add them to the array
	default_client_server_ports=()
	for i in $(seq 1 $CLIENT_SERVER_NUMBER); do
		default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
		default_client_server_ports+=("${!default_client_server_port_i}")
	done
	
	# Generate ports and add them to the array
	default_client_server_ips_and_ports=()
	for i in $(seq 1 $CLIENT_SERVER_NUMBER); do
		default_client_server_ip_i="DEFAULT_CLIENT_SERVER_IP_$i"
		default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
		default_client_server_ips_and_ports+=("${!default_client_server_ip_i}")
		default_client_server_ips_and_ports+=("${!default_client_server_port_i}")
	done
	
	# Start all three scripts in background
	server_layer_script $TEST_START_SERVER_SCRIPT $DEFAULT_SERVER_PORT
	client_server_layer_script $TEST_START_CLIENT_SERVER_SCRIPT "${default_client_server_ports[@]}" $DEFAULT_SERVER_IP $DEFAULT_SERVER_PORT
	client_layer_script $TEST_START_CLIENT_SCRIPT "${default_client_server_ips_and_ports[@]}"
	
	if [[ $KILL_BY_KEYPRESS == "y" || $KILL_BY_KEYPRESS == "Y" ]]; then

		echo "Press any key to stop all instances:"
		read -s -n 1

		# Kill each layer
		kill_layer $SERVER_JAR_NAME $CLIENT_SERVER_JAR_NAME $CLIENT_JAR_NAME

	else
		echo "Processes are running in background, run 'make kill_network' to kill them all"
	fi

elif [[ $COMMAND == "kill-layers" ]]; then

	echo "Do you wish to kill the client layer? [y/n]"
	read KILL_LAYER
	
	if [[ $KILL_LAYER == "y" || $KILL_BY_KEYPRESS == "Y" ]]; then
		kill_layer $CLIENT_JAR_NAME
	fi
	
	echo "Do you wish to kill the client-server layer? [y/n]"
	read KILL_LAYER
	
	if [[ $KILL_LAYER == "y" || $KILL_BY_KEYPRESS == "Y" ]]; then
		kill_layer $CLIENT_SERVER_JAR_NAME
	fi
	
	echo "Do you wish to kill the server layer? [y/n]"
	read KILL_LAYER
	
	if [[ $KILL_LAYER == "y" || $KILL_BY_KEYPRESS == "Y" ]]; then
		kill_layer $SERVER_JAR_NAME
	fi

fi

