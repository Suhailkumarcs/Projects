@echo off
setlocal enabledelayedexpansion

REM Build script: compiles Java sources and creates notebook.jar with Main-Class manifest

set "APPDIR=%~dp0"
set "SRC_DIR=%APPDIR%src"
set "BUILD_DIR=%APPDIR%build"
set "DIST_DIR=%APPDIR%dist"

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

echo Compiling...
javac -encoding UTF-8 -d "%BUILD_DIR%" "%SRC_DIR%"\notebook.java "%SRC_DIR%"\project.java
if errorlevel 1 exit /b 1

echo Creating JAR...
cd /d "%BUILD_DIR%"

REM Create temporary manifest file
echo Main-Class: project> "%APPDIR%manifest.mf"

jar -cvfm "%DIST_DIR%"\notebook.jar "%APPDIR%manifest.mf" .
if errorlevel 1 exit /b 1

del /q "%APPDIR%manifest.mf"

echo Done.
echo Output: %DIST_DIR%\notebook.jar
endlocal

