import os
import sys
import json
import os
import time
from roboflow import Roboflow
from dotenv import load_dotenv

#Please create a file named ".env" and set ROBOFLOW_API_KEY to your roboflow api key
load_dotenv(dotenv_path=".env")

dir_path = "matches/match.mp4"
# Disable
def blockPrint():
    sys.stdout = open(os.devnull, 'w')

# Restore
def enablePrint():
    sys.stdout = sys.__stdout__
    
#blockPrint()

rf = Roboflow(api_key=os.getenv("ROBOFLOW_API_KEY"))
project = rf.workspace().project("1294-ai-scouting")
model = project.version("8").model

model.confidence = 50
model.iou_threshold = 50

start = time.time()
job_id, signed_url, expire_time = model.predict_video(
    dir_path,
    fps=15,
    prediction_type="batch-video",
)

results = model.poll_until_video_results(job_id)

end = time.time()

enablePrint()

parsed = json.dumps(results, indent=4, sort_keys=True)

# Forgot to add this, add tonight
write_path = "temp/output.json"#os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'temp/robotoutput.json'))

f = open(write_path, "w")
f.write(parsed)
f.close()

print(f"Wrote file (took {end - start} seconds)")


sys.stdout.flush()
