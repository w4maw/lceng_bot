#!/usr/bin/env bash

mvn clean package \
&& sudo docker build -t w4maw/lceng_bot:$1  .\
&& sudo docker push w4maw/lceng_bot:$1
