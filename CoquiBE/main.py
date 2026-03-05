import os
import torch
import torchaudio
from TTS.api import TTS
from fastapi import FastAPI, Response, status, HTTPException
from fastapi.exceptions import RequestValidationError
from pydantic import BaseModel
import numpy as np
from starlette.responses import PlainTextResponse


class ConversionRequest(BaseModel):
    text: str
    file_name: str

output_dir = "/app/out"
os.makedirs(output_dir, exist_ok=True)

device = "cuda" if torch.cuda.is_available() else "cpu"
tts = TTS(model_name="tts_models/de/thorsten/vits").to(device)
tts.synthesizer.tts_model.length_scale = 1.5
app = FastAPI()

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc: RequestValidationError):
    message = "Validation errors:"
    for error in exc.errors():
        message += f"\nField: {error['loc']}, Error: {error['msg']}"
    print(message)
    return PlainTextResponse(message, status_code=400)

def write_sln24(filename, wav_array, original_sr):
    target_sr = 24000

    # Convert to tensor
    if isinstance(wav_array, list):
        wav_array = np.array(wav_array)

    if isinstance(wav_array, np.ndarray):
        wav_tensor = torch.from_numpy(wav_array).float()
    else:
        wav_tensor = wav_array.float()

    # Resample if needed
    if original_sr != target_sr:
        resampler = torchaudio.transforms.Resample(
            orig_freq=original_sr,
            new_freq=target_sr
        )
        wav_tensor = resampler(wav_tensor)

    # Normalize
    max_amp = torch.max(torch.abs(wav_tensor))
    if max_amp > 0:
        wav_tensor = wav_tensor / max_amp

    # Convert to 16-bit signed PCM
    wav_int16 = (wav_tensor.numpy() * 32767).astype(np.int16)

    # Write int16 as little endian, signed, 2 bytes
    with open(filename, "wb") as f:
        f.write(wav_int16.astype('<i2').tobytes())

@app.post("/convert", status_code=status.HTTP_204_NO_CONTENT)
async def convert(request: ConversionRequest):
    print(f"Converting text to speech, saving to {request.file_name}.sln24")
    audio_path = f"{output_dir}/{request.file_name}.sln24"
    wav_array = tts.tts(text=request.text)

    # Needs resampling due to Asterisks file formats
    write_sln24(audio_path, wav_array, original_sr=tts.synthesizer.output_sample_rate)

    return Response(status_code=status.HTTP_204_NO_CONTENT)


@app.get("/healthcheck", status_code=status.HTTP_200_OK)
async def healthcheck():
    if tts is None:
        raise HTTPException(status_code=503)
    return Response(status_code=status.HTTP_200_OK)
