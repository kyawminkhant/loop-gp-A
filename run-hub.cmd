@echo off
setlocal
cd /d "%~dp0"
call "modules\finance\mvnw.cmd" -f "modules\product\pom.xml" javafx:run
if errorlevel 1 pause
endlocal
