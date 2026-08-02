@echo off
setlocal
cd /d "%~dp0"
echo Preparing all LOOP modules...
call "modules\finance\mvnw.cmd" -DskipTests install
if errorlevel 1 goto failed

echo Starting the LOOP Team Hub...
call "modules\finance\mvnw.cmd" -f "modules\product\pom.xml" javafx:run
if errorlevel 1 goto failed
goto finished

:failed
echo.
echo LOOP could not be started. Review the Maven error above.
pause

:finished
endlocal
