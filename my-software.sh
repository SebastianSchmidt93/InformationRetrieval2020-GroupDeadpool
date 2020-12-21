#!/bin/bash

# TODO Check input parameters
INPUT=$2
OUTPUT=$4

# switch into the docker directory and copy necessary files
cd Docker
cp -r ../app app

# build the docker image
docker build --tag uh-t1-deadpool:recent .

# run and start the docker container
docker run -ti --rm --name IRgroupDeadpool -p 80:88 -v $INPUT:/input:ro -v $OUTPUT:/output uh-t1-deadpool:recent -i input -o output

# finally, remove copied files
rm -r app
