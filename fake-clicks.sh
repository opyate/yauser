#!/bin/bash

RANGE=100
while(true)
do

	number=$RANDOM
	let "number %= $RANGE"
	mysql -e "use yauser; insert into clicks (sort_c, yauserurl, xdatetime, referrer) values (0, 1, now(), \"10.3.2.$number\");"
done
