#!/bin/bash

#create the input directory
mkdir $2

#create the output directory
mkdir $4

#switch into the docker directory
cd Docker

#build the docker image
docker build --tag ir_deadpool:recent .

#run and start the docker container
docker run -ti -d --name IRgroupDeadpool -p 80:88 -v ${PWD}/..:/prototype/ ir_deadpool:recent

#enter the docker container
docker exec -ti IRgroupDeadpool "/bin/bash"

#modify the rights for the necessary files
chmod 755 prototype/*

#switch into the directory containing the program
cd prototype

#execute the program
my-software.sh -i $2 -o $4
