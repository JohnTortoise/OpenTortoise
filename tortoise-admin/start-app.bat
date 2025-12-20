@echo off
echo Starting tortoise-admin...
echo.

REM Method 1: Use spring.config.import to load .env as properties file
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.config.import=optional:file:.env[.properties]"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Application failed to start!
    echo Trying alternative method...
    echo.
    goto alternative
)

exit /b 0

:alternative
REM Method 2: Directly pass all properties from .env file
echo Trying alternative startup method...
setlocal enabledelayedexpansion

REM Read .env and build properties string
set "PROPS="
for /f "usebackq tokens=1,2 delims==" %%i in (".env") do (
    set "key=%%i"
    set "value=%%j"

    REM Skip comments and empty lines
    echo !key! | findstr "^#" >nul
    if errorlevel 1 (
        if not "!key!"=="" (
            REM Remove quotes
            if "!value:~0,1!"=="^"" set "value=!value:~1!"
            if "!value:~-1!"=="^"" set "value=!value:~0,-1!"

            REM Add to properties
            set "PROPS=!PROPS! --!key!=!value!"
        )
    )
)

echo Starting with properties: !PROPS!
mvn spring-boot:run -Dspring-boot.run.arguments="!PROPS!"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] All startup methods failed!
    pause
)