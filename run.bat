@echo off

pushd "%~dp0"

echo Welcome to The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base)!

setlocal enabledelayedexpansion

echo Need 2 run Video Downloader? (y/n)
SET /p download=
IF /I "%download%"=="y" (

    echo Enter Video URL:
    SET /p URL=

    DEL "matches\match.mp4" 2>nul
    call yt-dlp -o "matches/match.mp4"  -f "bestvideo[height=720][ext=mp4]" --cookies "cookies" "!URL!"

    IF !ErrorLevel! NEQ 0 (
        echo Video Downloader failed with error code !ErrorLevel!
        EXIT /B !ErrorLevel!
    )
)

echo Need 2 run Detector? (y/n)
SET /p detect=

IF /I "%detect%"=="y" (

    call python3.12 detect2.py

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
    echo 4. enter no_show if a team fails to make a good faith effort in attending the match
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

