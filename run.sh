#!/bin/bash

#Shoutout to Gemini for converting this from run.bat to run.sh!!!
cd "$(dirname "$0")"

read -p "Need 2 run Video Downloader? (y/n): " download
if [[ "$download" =~ ^[Yy]$ ]]; then
    python3 YouTubeDownloader.py
    if [ $? -ne 0 ]; then
        echo "Video Downloader failed with error code $?"
        exit $?
    fi
fi

read -p "Need 2 run Detector? (y/n): " detect
if [[ "$detect" =~ ^[Yy]$ ]]; then
    python3 detect2.py
    if [ $? -ne 0 ]; then
        echo "Detector failed with error code $?"
        exit $?
    fi
fi

read -p "Need 2 run Analyzer? (y/n): " analyze
if [[ "$analyze" =~ ^[Yy]$ ]]; then
    echo "Enter team numbers of the FRC robots that you are scouting"
    echo "1. in order from closest to furthest away from camera"
    echo "2. separated by spaces"
    echo "3. with no trailing whitespace"
    echo "------------------------------------"
    read -p "Red Alliance: " red
    read -p "Blue Alliance: " blue

    javac src/*.java json/*.java
    
    java -cp "src:json" AIScout $red $blue

    if [ $? -ne 0 ]; then
        echo "Analyzer failed with error code $?"
        exit $?
    fi
fi