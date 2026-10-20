@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo  Launching CinePass Cinemas - Movie Ticket Booking App
echo ========================================================
call mvnw.cmd javafx:run
pause
