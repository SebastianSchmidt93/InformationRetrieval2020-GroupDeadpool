#!/bin/bash
#input directory
# $2  
#output directory
# $3

cd Docker
docker build --tag ir_deadpool:recent .
docker run -ti --name IRgroupDeadpool -p 80:88 -v ${PWD}/Files_To_Index/:/input-dir ir_deadpool:recent
#docker exec -ti IRgroupDeadpool "/bin/bash"


#erstelle den Ordner für input und output
#Packe beide Ordner in den Docker Container
#mounte die Dateien für den Input Ordner in den Docker-Container
#gehe in den Container, führe das Programm dort drin aus

#-i input directory -o output directory
