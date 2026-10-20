@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo  Executing CinePass Cinemas Full JUnit 5 Test Suite
echo ========================================================
call mvnw.cmd test
pause
