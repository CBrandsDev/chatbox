@echo off
set startPort=8000
set endPort=8005

for /L %%i in (%startPort%,1,%endPort%) do (
    echo Starting Chat Server on port %%i...
    start "Chat Server on port %%i" java ChatServer %%i
)
pause
