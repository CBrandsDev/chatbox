@echo off
echo Available ports: 8000 to 8005
set /p port="Enter the port number to connect to: "
echo Starting Chat Client...
javac ChatClient.java
java ChatClient 192.168.10.9 %port%
pause
