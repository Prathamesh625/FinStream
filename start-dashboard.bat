@echo off
echo ===================================================
echo   Starting FinStream Platform & Monitoring Dashboard
echo ===================================================
echo.
echo Launching Web Dashboard in Default Browser...
start "" "%~dp0frontend-dashboard\index.html"
echo.
echo FinStream UI is running!
echo To run full Docker stack: docker-compose up --build
pause
