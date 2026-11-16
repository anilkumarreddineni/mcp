@echo off
rem Starts order-service, mcp-server, mcp-client and chat-ui, each in its own window.
rem Each service waits for the one before it to answer before starting.
rem Close a service's window (or press Ctrl+C in it) to stop it.

setlocal
cd /d "%~dp0"

if "%ANTHROPIC_API_KEY%"=="" (
    echo WARNING: ANTHROPIC_API_KEY is not set. mcp-client will not be able to call Claude.
    echo          Set it first, e.g.  set ANTHROPIC_API_KEY=sk-ant-...
    echo.
)

echo Clearing old log files...
for %%s in (order-service mcp-server mcp-client) do (
    if exist "%~dp0%%s\logs" del /q "%~dp0%%s\logs\*.log*" 2>nul
)
echo.

echo Starting order-service on port 8081...
echo   Log: %~dp0order-service\logs\order-service.log
start "order-service (8081)" /d "%~dp0order-service" cmd /k mvnw.cmd spring-boot:run
call :waitFor http://localhost:8081/orders order-service || goto :failed

echo Starting mcp-server on port 8080...
echo   Log: %~dp0mcp-server\logs\mcp-server.log
start "mcp-server (8080)" /d "%~dp0mcp-server" cmd /k mvnw.cmd spring-boot:run
call :waitFor http://localhost:8080/hello mcp-server || goto :failed

echo Starting mcp-client on port 8082...
echo   Log: %~dp0mcp-client\logs\mcp-client.log
start "mcp-client (8082)" /d "%~dp0mcp-client" cmd /k mvnw.cmd spring-boot:run
call :waitFor http://localhost:8082/api/chat mcp-client || goto :failed

if not exist "%~dp0chat-ui\node_modules" (
    echo Installing chat-ui packages ^(first run only^)...
    pushd "%~dp0chat-ui"
    call npm install || (popd & goto :failed)
    popd
)
echo Starting chat-ui on port 4200...
start "chat-ui (4200)" /d "%~dp0chat-ui" cmd /k npm start
call :waitFor http://localhost:4200/ chat-ui || goto :failed

echo.
echo All services are up. Opening http://localhost:4200
start "" http://localhost:4200
exit /b 0

:failed
echo.
echo A service did not start. Check its window for errors.
exit /b 1

rem Polls %1 every 2 seconds until it answers, for up to 3 minutes.
:waitFor
set /a tries=0
:waitLoop
curl -s -o nul "%~1" && (echo   %~2 is up. & exit /b 0)
set /a tries+=1
if %tries% geq 90 (echo   %~2 did not respond at %~1 within 3 minutes. & exit /b 1)
timeout /t 2 /nobreak >nul
goto :waitLoop
