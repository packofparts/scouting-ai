import os, sys, time, math
from dotenv import load_dotenv

#Please create a file named ".env" and set ROBOFLOW_API_KEY to your roboflow api key
_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
_dotenv_path = os.path.join(_SCRIPT_DIR, ".env")
load_dotenv(dotenv_path=_dotenv_path if os.path.exists(_dotenv_path) else ".env")

start = time.time()

# Disable
def blockPrint():
    sys.stdout = open(os.devnull, 'w')

# Restore
def enablePrint():
    sys.stdout = sys.__stdout__
    
#blockPrint()

os.environ["ENABLE_FRAME_DROP_ON_VIDEO_FILE_RATE_LIMITING"] = "True"  
os.environ["TF_ENABLE_ONEDNN_OPTS"]='0'

blockPrint()
from inference import InferencePipeline 
from inference.core.interfaces.stream.sinks import render_boxes
import supervision as sv
import moviepy as mp
enablePrint()

dir_path = ""#os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'temp/trimmed.mp4'))



model_id = "1294-ai-scouting/10"
_default_input_video_path = os.path.join("matches", "match.mp4")
input_video_path = sys.argv[1] if len(sys.argv) > 1 else _default_input_video_path
_default_output_path = os.path.join(_SCRIPT_DIR, "temp", "output.json")
output_path = sys.argv[2] if len(sys.argv) > 2 else _default_output_path

def _resolve_existing_path(path: str) -> str:
    if os.path.isabs(path) and os.path.exists(path):
        return path

    cwd_candidate = os.path.abspath(path)
    if os.path.exists(cwd_candidate):
        return cwd_candidate

    script_candidate = os.path.abspath(os.path.join(_SCRIPT_DIR, path))
    if os.path.exists(script_candidate):
        return script_candidate

    raise FileNotFoundError(
        f"Input video not found: '{path}'. Tried:\n"
        f"- {cwd_candidate}\n"
        f"- {script_candidate}\n"
        "Pass the video path as an argument, e.g. `python detect2.py matches/match.mp4`"
    )

input_video_path = _resolve_existing_path(input_video_path)
output_path = os.path.abspath(output_path)
os.makedirs(os.path.dirname(output_path), exist_ok=True)

target_fps = 10
total_frames = mp.VideoFileClip(input_video_path).n_frames


output = sv.JSONSink(output_path)
output.open()


def json_sink_callback(prediction, video_frame):
    """
    Callback function to process predictions and append to the JSON sink.
    """
    # Convert inference results to supervision Detections object
    detections = sv.Detections.from_inference(prediction)

    # Append the detections (and optional custom data like frame metadata) 
    # to the JSON sink.
    sink_data = {
        "frame_id": video_frame.frame_id,
        #"timestamp": video_frame.timestamp.isoformat(),
    }

    output.append(detections, custom_data=sink_data)

    percent = video_frame.frame_id/total_frames 
    hashtags = round(percent * 10)
    display = round(percent * 1000)/10
    sys.stdout.write(f"\rInferencing... |{"#" * (hashtags) + "-" * (10 - hashtags)}| {display}% ({video_frame.frame_id}/{total_frames} frames)")
    sys.stdout.flush()



pipeline = InferencePipeline.init(
    video_reference=input_video_path,
    model_id=model_id,
    api_key=os.getenv("ROBOFLOW_API_KEY"), 
    on_prediction=json_sink_callback,
    max_fps=target_fps
    #api_url="http://localhost:9001", # Connect to your local inference server
)


pipeline.start()

pipeline.join()

print(" Done!")

output.write_and_close()

print(f"Wrote file (took {time.time() - start} seconds)")


sys.stdout.flush()
