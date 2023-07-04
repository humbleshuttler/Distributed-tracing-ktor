#!/bin/bash

endpoint="http://localhost:8080/"  # Replace with your actual endpoint

clients=50             # Default number of clients
duration=60            # Default duration in seconds

# Check if number of clients is provided as a command-line argument
if [[ ! -z $1 ]]; then
    clients=$1
fi

# Loop for each client
for ((i=0; i<$clients; i++))
do
    # Generate a random requests per second rate within the range of 1 to 10
    requests_per_second=$(shuf -i 1-10 -n 1)
    
    # Run each client in the background
    (
        total_requests=$(echo "$requests_per_second * $duration" | bc)  # Calculate the total number of requests for each client
        
        # Loop to send requests for each client
        for ((j=0; j<$total_requests; j++))
        do
            curl -s $endpoint >/dev/null &
            
            # Generate a random delay between 0.1 and 1.0 seconds for each request
            delay=$(awk -v min=0.1 -v max=1.0 'BEGIN{srand(); print min+rand()*(max-min)}')
            sleep $delay
        done
        
        # Wait for any remaining requests for each client to complete
        wait
    ) &
done

# Wait for all clients to complete
wait

