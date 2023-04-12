#!/usr/bin/env bash

mvn clean package \
&& buildah build -t w4maw/lceng_bot:$1  .\
&& buildah push w4maw/lceng_bot:$1 \
&& buildah tag w4maw/lceng_bot:$1 w4maw/lceng_bot:latest \
&& buildah push w4maw/lceng_bot:latest