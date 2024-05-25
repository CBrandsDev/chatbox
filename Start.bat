@echo off
set /p port="Enter the port number to start the server on: "
echo Starting Chat Server on port %port%...
javac ChatServer.java
java ChatServer %port%
pause
