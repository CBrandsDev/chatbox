
@echo off
set /p port="Enter the port number to connect to: "
echo Starting Chat Client...
javac ChatClient.java
java ChatClient 192.168.10.9 %port%
pause
