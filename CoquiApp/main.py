import torch
#import sounddevice as sd
import csv
from TTS.api import TTS

# Get device
device = "cuda" if torch.cuda.is_available() else "cpu"

# Init TTS
tts = TTS("tts_models/de/css10/vits-neon").to(device)

# wav = tts.tts(text="Grüß Gott und willkomen bei Fabia!")
#
# sd.play(wav)
# sd.wait()

# Text to speech to a file
#tts.tts_to_file(text="Grüß Gott und willkomen bei Fabia!", file_path="output.wav")

with open("Phrases.csv", newline='', encoding="utf-8") as csvfile:
  first = True
  reader = csv.reader(csvfile, delimiter=";")
  for row in reader:
    if first:
      first = False
      continue
    audio_path  = f"./out/{row[0]}.wav"
    tts.tts_to_file(text=row[2], file_path=audio_path)