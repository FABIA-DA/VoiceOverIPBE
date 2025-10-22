FROM mambaorg/micromamba@sha256:5e3b9c781f71c5d715a18216cdc9ce778bc974f478c6757a6e853a11d05fad12

WORKDIR /app

# Copy envornment file
COPY environment.yml .

USER root
# Install OS dependencies
RUN apt-get update && apt-get install -y --no-install-recommends espeak \
 && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /env && chown $MAMBA_USER /env
USER $MAMBA_USER

# Create Conda environment with only conda packages
RUN micromamba env create --copy -p /env --file environment.yml
RUN micromamba install -y -p /env -c conda-forge ffmpeg libopus lame \
    && micromamba clean --all --yes

# Make the dependencies accessible
ENV PATH=/env/bin:$PATH
ARG MAMBA_DOCKERFILE_ACTIVATE=1

# Copy the rest of the app
COPY --chmod=777 . .

EXPOSE 8000

USER root

# Download the model into the image
ENV XDG_CACHE_HOME=/models
RUN mkdir -p /models
RUN micromamba run -p /env python -c "from TTS.api import TTS; TTS(model_name='tts_models/de/css10/vits-neon')"

# Run the app
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]