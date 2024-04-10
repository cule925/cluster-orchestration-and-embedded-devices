#!/bin/bash

# Default values
source ../../config.sh

# If not declared or empty
if [[ ! -v CLIENT_NUMBER || -z $CLIENT_NUMBER || $CLIENT_NUMBER -le 0 ]]; then
	CLIENT_NUMBER=12
fi

if [[ ! -v CLIENT_SERVER_NUMBER || -z $CLIENT_SERVER_NUMBER ]]; then
	CLIENT_SERVER_NUMBER=3
fi

if [[ ! -v CLIENTS_PER_CLIENT_SERVER || -z $CLIENTS_PER_CLIENT_SERVER ]]; then
	CLIENTS_PER_CLIENT_SERVER=4
fi

MAX_POSSIBLE_CLIENT_NUMBER=$((CLIENT_SERVER_NUMBER * CLIENTS_PER_CLIENT_SERVER))

# Do not allow the client number to excede the maximum possible client number
if [ "$CLIENT_NUMBER" -gt "$MAX_POSSIBLE_CLIENT_NUMBER" ]; then
	CLIENT_NUMBER=$MAX_POSSIBLE_CLIENT_NUMBER
fi

for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

	default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
	
	if [[ ! -v $default_client_server_port_i || -z ${!default_client_server_port_i} ]]; then
		eval "$default_client_server_port_i=localhost"
	fi
	
done

for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

	default_client_server_ip_i="DEFAULT_CLIENT_SERVER_IP_$i"
	
	if [[ ! -v $default_client_server_ip_i || -z ${!default_client_server_ip_i} ]]; then
		eval "$default_client_server_ip_i=localhost"
	fi
	
done

for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

	default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
	port_i=$((DEFAULT_SERVER_PORT + i))

	if [[ ! -v $default_client_server_port_i || -z ${!default_client_server_port_i} ]]; then
		eval "$default_client_server_port_i=$port_i"
	fi

done

for i in $(seq 1 $CLIENT_NUMBER); do

	client_reporting_period_i="CLIENT_REPORTING_PERIOD_$i"
	
	if [[ ! -v $client_reporting_period_i || -z ${!client_reporting_period_i} ]]; then
		eval "$client_reporting_period_i=3"
	fi
	
done

for i in $(seq 1 $CLIENT_NUMBER); do

	client_reporting_period_i="CLIENT_REPORTING_PERIOD_$i"
	
	if [[ ! -v $client_reporting_period_i || -z ${!client_reporting_period_i} ]]; then
		eval "$client_reporting_period_i=3"
	fi	
	
done

for i in $(seq 1 $CLIENT_NUMBER); do

	client_node_name_i="CLIENT_NODE_NAME_$i"
	
	if [[ ! -v $client_node_name_i || -z ${!client_node_name_i} ]]; then
		eval "$client_node_name_i=S-ESP32-$i"
	fi
	
done

for i in $(seq 1 $CLIENT_NUMBER); do

	sensor_list_i="SENSOR_LIST_$i"
	
	if [[ ! -v $sensor_list_i || -z ${!sensor_list_i} ]]; then
		eval "$sensor_list_i=./setup/sensor-list.txt"
	fi
	
done

# The JAR file
CLIENT_JAR="../client/out/artifacts/client_jar/client.jar"

# Check if JAR file exists
if [ ! -f "$CLIENT_JAR" ]; then
    echo "JAR file not found: '$CLIENT_JAR', make sure to build it with Maven!"
    exit 1
fi

echo "Do you want to kill all processes by one keypress? [y/n]"
read KILL_BY_KEYPRESS

echo "Do you want to change the default values? [y/n]"
read CHANGE_CONFIG

if [[ $CHANGE_CONFIG == "y" || $CHANGE_CONFIG == "Y" ]]; then

	echo "Entering configuration, on each entry press enter if you wish to use the default value"

	for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

		echo "Enter the IP address of client-server-$i:"
		default_client_server_ip_i="DEFAULT_CLIENT_SERVER_IP_$i"
		ip_address_of_client_server_i="IP_ADDRESS_OF_CLIENT_SERVER_$i"
		read $ip_address_of_client_server_i

		if [[ -n ${!ip_address_of_client_server_i} ]]; then
			echo "client-server-$i IP address is: ${!ip_address_of_client_server_i}"
		else
			eval "$ip_address_of_client_server_i=${!default_client_server_ip_i}"
			echo "client-server-$i IP address is: ${!default_client_server_ip_i}"
		fi

		echo "Enter the port of client-server-$i:"
		default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
		port_of_client_server_i="PORT_OF_CLIENT_SERVER_$i"
		read $port_of_client_server_i

		if [[ -n ${!port_of_client_server_i} ]]; then
			echo "client-server-$i port is: ${!port_of_client_server_i}"
		else
			eval "$port_of_client_server_i=${!default_client_server_port_i}"
			echo "client-server-$i port is: ${!default_client_server_port_i}"
		fi

	done
	
	#for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

		

	#done

else

	echo "Proceeding with default values:"

	for i in $(seq 1 $CLIENT_SERVER_NUMBER); do

		default_client_server_ip_i="DEFAULT_CLIENT_SERVER_IP_$i"
		ip_address_of_client_server_i="IP_ADDRESS_OF_CLIENT_SERVER_$i"
		
		default_client_server_port_i="DEFAULT_CLIENT_SERVER_PORT_$i"
		port_of_client_server_i="PORT_OF_CLIENT_SERVER_$i"
		
		eval "$ip_address_of_client_server_i=${!default_client_server_ip_i}"
		echo "client-server-$i IP address is: ${!default_client_server_ip_i}"
		
		eval "$port_of_client_server_i=${!default_client_server_port_i}"
		echo "client-server-$i port is: ${!default_client_server_port_i}"
		
	done

fi

# Launch instances of client
echo "Starting $CLIENT_NUMBER client.jar instances..."

c_n=$CLIENT_NUMBER
cs_n=$CLIENT_SERVER_NUMBER
cpcs_n=$CLIENTS_PER_CLIENT_SERVER
c_i=1

for ((cs_i = 1; cs_i <= cs_n; cs_i++)); do

	for ((cpcs_i = 1; cpcs_i <= cpcs_n; cpcs_i++, c_i++)); do

		# Start client
		client_node_name_i="CLIENT_NODE_NAME_$c_i"
		ip_address_of_client_server_i="IP_ADDRESS_OF_CLIENT_SERVER_$cs_i"
		port_of_client_server_i="PORT_OF_CLIENT_SERVER_$cs_i"
		sensor_list_i="SENSOR_LIST_$c_i"
		client_reporting_period_i="CLIENT_REPORTING_PERIOD_$c_i"
		
		CLIENT_LAUNCH="java -jar $CLIENT_JAR ${!client_node_name_i} ${!ip_address_of_client_server_i} ${!port_of_client_server_i} ${!sensor_list_i} ${!client_reporting_period_i} &"
		echo "$CLIENT_LAUNCH"
		eval "$CLIENT_LAUNCH"
		
		if [[ $c_i -ge $c_n ]]; then
			break
		fi

	done

	if [[ $c_i -ge $c_n ]]; then
		break
	fi

done

if [[ $KILL_BY_KEYPRESS == "y" || $KILL_BY_KEYPRESS == "Y" ]]; then

	echo "Press any key to stop all instances:"
	read -s -n 1

	# Find the PID-s
	PIDS=$(pgrep -f "java.*-jar.*$CLIENT_JAR.*")

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

