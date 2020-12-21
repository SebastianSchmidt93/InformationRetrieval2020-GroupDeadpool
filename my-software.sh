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

#copy the topics.xml into the input directory
cp topics.xml $2

#we still need to obtain the data that the program works with, and move it to the input directory as well.

#execute the program
java -jar LucenePrototype.jar -i $2 -o $4
