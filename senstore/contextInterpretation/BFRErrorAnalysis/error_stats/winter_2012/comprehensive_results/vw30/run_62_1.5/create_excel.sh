#!/bin/sh

i=0
while [ $i != 508 ]
do
	mv "./position$i" "./position$i.xls"
	i=`expr $i + 1`
done
