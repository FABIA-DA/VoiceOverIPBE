import os

import torch
from TTS.api import TTS
from fastapi import FastAPI, Response, status
from pydantic import BaseModel
from pydub.utils import which
from pydub import AudioSegment

class ConversionRequest(BaseModel):
    text: str
    fileName: str

# Force Pydub to use Conda-installed FFmpeg
AudioSegment.converter = which("ffmpeg")

output_dir = "/app/out"
os.makedirs(output_dir, exist_ok=True)

app = FastAPI()

device = "cuda" if torch.cuda.is_available() else "cpu"

tts = TTS("tts_models/de/css10/vits-neon").to(device)


@app.post("/convert", status_code=status.HTTP_204_NO_CONTENT)
async def convert(request: ConversionRequest):
    print(f"Converting text to speech, saving to {request.fileName}.wav")
    audio_path = f"{output_dir}/{request.fileName}.wav"
    tts.tts_to_file(
        text=request.text,
        file_path=audio_path,
    )

    sound = AudioSegment.from_file(audio_path)
    sound = sound.set_channels(1).set_frame_rate(8000)
    sound.export(audio_path, format="wav", parameters=["-c:a", "pcm_s16le"])

    return Response(status_code=status.HTTP_204_NO_CONTENT)


@app.get("/healthcheck", status_code=status.HTTP_200_OK)
async def healthcheck():
    return Response(status_code=status.HTTP_200_OK)
