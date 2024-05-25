@echo off
set startPort=8000
set endPort=8005

for /L %%i in (%startPort%,1,%endPort%) do (
    echo Starting Chat Server on port %%i...
    start /b java ChatServer %%i
)
echo All servers started. Press any key to close all.
pause >nul
```
</rewritten_file><|eot_id|>
