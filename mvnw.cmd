@echo off
setlocal
if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    set "PATH=C:\Program Files\Java\jdk-21\bin;%PATH%"
)
if exist "C:\Users\Ryan\apache-maven-3.9.9\bin\mvn.cmd" (
    "C:\Users\Ryan\apache-maven-3.9.9\bin\mvn.cmd" %*
) else (
    mvn %*
)
endlocal
