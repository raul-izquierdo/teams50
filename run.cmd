@echo off
mvnw.cmd -o -Pfast compile exec:java -Dexec.args="%*"
