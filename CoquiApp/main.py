import os
import torch
import csv
from TTS.api import TTS
import numpy as np

os.makedirs("out", exist_ok=True)

# Get device
device = "cuda" if torch.cuda.is_available() else "cpu"

# Init TTS
tts = TTS(model_name="tts_models/multilingual/multi-dataset/xtts_v2").to(device)

with open("Phrases.csv", newline='', encoding="utf-8") as csvfile:
  first = True
  reader = csv.reader(csvfile, delimiter=";")
  for row in reader:
    if first:
      first = False
      continue
    # Sample Rate is already fitting
    audio_path  = f"./out/{row[0]}.sln24"
    formatted = row[1].replace(".", ",")
    wav_array = tts.tts(text=row[1], language="de", speaker="Aaron Dreschner")

    # Scale to -1.0 .. 1.0 max amplitude
    max_amp = np.max(np.abs(wav_array))
    if max_amp > 0:
        wav_array = wav_array / max_amp

    # Convert to int16 for WAV
    wav_int16 = (wav_array * 32767).astype(np.int16)

    #Writing int16 as little endian, signed and 2 bytes
    with open(audio_path, "wb") as f:
      f.write(wav_int16.astype('<i2').tobytes())

    print(f"Created file {audio_path}")
print("Done...")