#!/usr/bin/env bash

cp $1 data.txt
echo "Generating data for 2^$2 times $1"
for ((n=0;n<$2;n++))
do
	cat data.txt data.txt > /tmp/temp
	mv /tmp/temp data.txt
done