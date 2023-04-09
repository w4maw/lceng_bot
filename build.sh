#!/usr/bin/env bash

mvn clean package \
&& buildah build -t w4maw/lceng_bot:$1  .\
&& buildah push w4maw/lceng_bot:$1
