#!/bin/bash

# get a list of running container ids with the word "lambda" in their names
container_ids=$(docker ps -q --filter name=lambda)

# loop through the ids and stop and remove each container
for id in $container_ids; do
    echo "Stopping and removing container: $id"
    docker stop $id
    docker rm $id
done