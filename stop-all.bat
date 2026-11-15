@echo off
rem Stops chat-app, mcp-server and order-service (the reverse of start-all.bat).
rem Closes each service's window, then kills anything still listening on its port.

setlocal

call :stop "chat-app (8082)" 8082
call :stop "mcp-server (8080)" 8080
call :stop "order-service (8081)" 8081

echo.
echo All services are stopped.
exit /b 0

rem Closes the window titled %1 (and the processes it started),
rem then kills any process still listening on port %2.
:stop
echo Stopping %~1...
taskkill /FI "WINDOWTITLE eq %~1*" /T /F >nul 2>&1
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":%~2 .*LISTENING"') do (
    taskkill /PID %%p /T /F >nul 2>&1
)
exit /b 0
