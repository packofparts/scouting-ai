# scouting-ai
# Welcome to The P.A.C.K.!

The P.A.C.K (Predictive, Analytical, and Competitive Knowledge-base) is designed efficiently gather, analyze, and visualize the locations of FRC Robots in the FIRST Robotics Competitions. 

Take PNW District 2025 Sammamish Event, Lower Bracket, Round 2, Match 6, For example:
<iframe width="500" height="300" src="https://www.youtube.com/embed/XZDd_Yerab0" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

Here is The P.A.C.K's detected path of team 1294. Pink lines denote robot movement during the autonomous period, while cyan lines denote the tele-operated period:
<image src="samples/path.png" width="500" controls></image>

# Quick Setup Instructions
1. Sign up for [Roboflow](https://roboflow.com) (or log into, if you're already a step ahead :P).

2. Create a workspace and go to settings -> api keys. Create and copy a private api key. 

3. Clone this repo. CD into scouting-ai folder and run 

(Windows)
```
./setup.bat
```
(MacOS)
```
chmod +x setup.bat

./setup.sh
```
depending on the OS.

4. A file called .env will show up, along with other folders. Paste the Roboflow api key there. 

5. Get [pip](https://bootstrap.pypa.io/get-pip.py)! Copy [the contents](https://bootstrap.pypa.io/get-pip.py) into a new python file. Run the file with a preferred python interpreter. Please use the same interpreter for all steps below. 

**⚠️ The Python interpreter version must be >= 3.9 or <= 3.12. If needed, please obtain Python 3.12.8 [here](https://www.python.org/downloads/release/python-3128/).**

6. Install the necessary libraries by running
```
pip install -r requirements.txt
```
. 

7. Calibrate the field! Inside src/AIScout.java, modify the constants TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, and BOTTOM_RIGHT to match with the **relative coordinates** of the corners of the game field. For example:

<image src="samples/fieldCalibration.png" width="500" controls></image>

8. Run

(Windows)
```
./run.bat
```
(MacOS)
```
chmod +x run.sh

./run.sh
```
depending on the OS. Follow the instructions and run the video downloader, detector, and analyzer. 

For the video downloader specifically, enter the link to the YouTube video of the match (preferrably WIDE) to be analyzed. 

9. Finally, run src/Visualization.java, and enter the team number to visualize its robot paths!