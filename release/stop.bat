@echo off
set port=8088
for /f "tokens=1-5" %%i in ('netstat -ano^|findstr ":%port%"') do (
    echo kill the process %%m who use the port 
    taskkill /pid %%m -t -f
    goto q
)
:q