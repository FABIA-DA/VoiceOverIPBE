import os
import torch
import csv
from pydub import AudioSegment
from TTS.api import TTS

os.makedirs("out", exist_ok=True)

# Get device
device = "cuda" if torch.cuda.is_available() else "cpu"

# Init TTS
tts = TTS("tts_models/de/css10/vits-neon").to(device)

with open("Phrases.csv", newline='', encoding="utf-8") as csvfile:
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
    sound = sound.set_frame_rate(8000)
    sound.export(audio_path, format="wav")
    print("Done")
print("Done...")