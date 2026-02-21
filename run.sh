#!/bin/bash

cd "$(dirname "$0")"

echo "Welcome to The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base)!"

read -p "Need 2 run Video Downloader? (y/n) " download
if [[ "$download" =~ ^[Yy]$ ]]; then

    read -p "Enter Video URL: " URL

    rm -f "matches/match.mp4"
    yt-dlp -o "matches/match.mp4" -f "bestvideo[height=720][ext=mp4]" --cookies "cookies" "$URL"

    if [ $? -ne 0 ]; then
        echo "Video Downloader failed with error code $?"
        exit $?
    fi
fi

read -p "Need 2 run Detector? (y/n) " detect
if [[ "$detect" =~ ^[Yy]$ ]]; then
    python3.12 detector.py

    if [ $? -ne 0 ]; then
        echo "Detector failed with error code $?"
        exit $?
    fi
fi

read -p "Need 2 run Analyzer? (y/n) " analyze
if [[ "$analyze" =~ ^[Yy]$ ]]; then

    echo "Enter team numbers of the FRC robots that you are scouting"
    echo "1. in order from closest to furthest away from camera"
    echo "2. separated by spaces"
    echo "3. with no trailing whitespace"
    echo "4. enter no_show if a team fails to make a good faith effort in attending the match"
    echo "------------------------------------"
    read -p "Team numbers of robots towards the left side of the field:" left
    read -p "Team numbers of robots towards the right side of the field:" right

    javac src/*.java json/*.java
    java -cp "src:json" AIScout $left $right

    if [ $? -ne 0 ]; then
        echo "Analyzer failed with error code $?"
        exit $?
    fi
fi