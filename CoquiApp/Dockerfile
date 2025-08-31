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

# Pre-download the model inside the environment
RUN micromamba run -p /env python -c "from TTS.utils.manage import ModelManager; ModelManager().download_model('tts_models/de/css10/vits-neon')"

USER root
ENTRYPOINT ["/bin/bash", "-c", "micromamba run -p /env python main.py"]
