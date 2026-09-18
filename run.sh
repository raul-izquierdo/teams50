#!/bin/bash
./mvnw -o -Pfast compile exec:java -Dexec.args="$*"
