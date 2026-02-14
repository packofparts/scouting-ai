@echo off
setlocal enabledelayedexpansion

pushd "%~dp0"


echo Need 2 run Video Downloader? (y/n)
SET /p download=
IF /I "%download%"=="y" (

    call python YouTubeDownloader.py

    IF !ErrorLevel! NEQ 0 (
        echo Video Downloader failed with error code !ErrorLevel!
        EXIT /B !ErrorLevel!
    )
)

echo Need 2 run Detector? (y/n)
SET /p detect=

IF /I "%detect%"=="y" (

    call python detect2.py

    IF !ErrorLevel! NEQ 0 (
        echo Detector failed with error code !ErrorLevel!
        EXIT /B !ErrorLevel!
    )
)


echo Need 2 run Analyzer? (y/n)
SET /p analyze=

IF /I "%analyze%"=="y" (

    echo Enter team numbers of the FRC robots that you are scouting 
    echo 1. in order from closest to furthest away from camera
    echo 2. separated by spaces
    echo 3. with no trailing whitespace 
    echo ------------------------------------
    echo Red Alliance: 
    SET /p red=
    echo Blue Alliance:
    SET /p blue=

    call javac src/*.java json/*.java
    call java -cp "src;json" AIScout !red! !blue!

    IF !ErrorLevel! NEQ 0 (
        echo Analyzer failed with error code !ErrorLevel!
        EXIT /B !ErrorLevel!
    )
    
)

popd

