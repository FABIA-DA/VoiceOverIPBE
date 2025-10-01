import os
import torch
import csv
from pydub.utils import which
from pydub import AudioSegment
from TTS.api import TTS

# Force Pydub to use Conda-installed FFmpeg
AudioSegment.converter = which("ffmpeg")

os.makedirs("out", exist_ok=True)
os.environ["XDG_CACHE_HOME"] = "/models"  # directory where model is already downloaded

# Get device
device = "cuda" if torch.cuda.is_available() else "cpu"

# Init TTS
tts = TTS("tts_models/de/css10/vits-neon").to(device)

with open("test.csv", newline='', encoding="utf-8") as csvfile:
  first = True
  reader = csv.reader(csvfile, delimiter=";")
  for row in reader:
    if first:
      first = False
      continue
    audio_path  = f"./out/{row[0]}.wav"
    tts.tts_to_file(text=row[2], file_path=audio_path)
    print(f"Created file {audio_path}")
    print("Changing sample rate...")
    sound = AudioSegment.from_file(audio_path)
    sound = sound.set_channels(1).set_frame_rate(8000)
    sound.export(audio_path, format="wav", parameters=["-c:a", "pcm_s16le"])
    print("Done")
print("Done...")