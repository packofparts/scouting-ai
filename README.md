# scouting-ai
**Thank you, [NimbleValley](https://github.com/NimbleValley), [NimbleValley's blog](https://blog.roboflow.com/robot-path-mapping/), and [NimbleValley's auto-scout program](https://github.com/NimbleValley/auto-scout), for inspiration and making this project possible!!!**
# Welcome to The P.A.C.K.!

The P.A.C.K (Predictive, Analytical, and Competitive Knowledge-base) is designed efficiently gather, analyze, and visualize the locations of FRC Robots in the FIRST Robotics Competitions. 

Take PNW District 2025 Sammamish Event, Lower Bracket, Round 2, Match 6, For example:

[![Watch the video](https://img.youtube.com/vi/XZDd_Yerab0/0.jpg)](https://www.youtube.com/watch?v=XZDd_Yerab0)

Here is The P.A.C.K's detected path of team 1294. Pink lines denote robot movement during the autonomous period, while cyan lines denote the tele-operated period:
<image src="samples/path.png" width="500" controls></image>

# Quick Setup Instructions
1. Sign up for [Roboflow](https://roboflow.com) (or log into, if you're already a step ahead :P).

2. Create a workspace and go to settings -> api keys. Create and copy a private api key. 

3. Clone this repo. CD into scouting-ai folder and run these commands, depending on the OS:

(Windows)
```
./setup.bat
```
(Unix-like)
```
chmod +x setup.bat

./setup.sh
```

4. A file called .env will show up, along with other folders. Paste the Roboflow api key there. 

5. Get [pip](https://bootstrap.pypa.io/get-pip.py)! Copy [the contents](https://bootstrap.pypa.io/get-pip.py) into a new python file. Run the file with a preferred python interpreter. Please use the same interpreter for all steps below. It is also highly recommended to set up a virtual environment to avoid dependency collisions.

**⚠️ The Python interpreter version must be >= 3.9 or <= 3.12. If needed, please obtain Python 3.12.8 [here](https://www.python.org/downloads/release/python-3128/).**

6. Install the necessary libraries by running this command:
```
pip install -r requirements.txt
```

7. Calibrate the field! Inside src/AIScout.java, modify the constants TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, and RED_ON_LEFT to match with the **relative coordinates** of the corners and orientation of the game field. For example:

<image src="samples/fieldCalibration.png" width="500" controls></image>

8. Run these commands, depending on the OS. Follow the instructions and run the video downloader, detector, and analyzer. For the video downloader specifically, enter the link to the YouTube video of the match (preferrably WIDE) to be analyzed. 

(Windows)
```
./run.bat
```
(Unix-like)
```
chmod +x run.sh

./run.sh
```

9. Finally, run src/Visualization.java, and enter the team number to visualize its robot paths!

# Credits
1. Shoutout to [NimbleValley](https://github.com/NimbleValley)!

2. Contributing Writer. (Aug 29, 2024). Mapping Robot Paths in Robotics Competitions with Computer Vision. Roboflow Blog: https://blog.roboflow.com/robot-path-mapping/ (NimbleValley's blog)

3. https://github.com/NimbleValley/auto-scout (NimbleValley's auto-scout repo)

4. https://github.com/KevinStern/software-and-algorithms/blob/master/src/main/java/blogspot/software_and_algorithms/stern_library/optimization/HungarianAlgorithm.java (HungarianAlgorithm.java)

5. https://github.com/DevCTx/YouTubeDownloader (YouTubeVideoDownloader.py, modified)

6. https://github.com/stleary/JSON-java. (All files in /json folder)

7. All match videos and images used in this repository are sourced from [PNW District 2025 Sammamish Event, Lower Bracket, Round 2, Match 6 of the FIRST Robotics Competition](https://www.youtube.com/watch?v=XZDd_Yerab0). 

8. The field image used for visualization (field.png) is sourced from [the FRC game manual](https://firstfrc.blob.core.windows.net/frc2025/Manual/2025GameManual.pdf). 

9. *Some AI assistance is used in this project.*

# See You at Worlds!