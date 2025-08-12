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
RUN micromamba env create --copy -p /env --file environment.yml && \
    micromamba clean --all --yes

# Make the dependencies accessible
ENV PATH=/env/bin:$PATH
ARG MAMBA_DOCKERFILE_ACTIVATE=1

# Copy the rest of the app
COPY --chmod=777 . .

# Download the model into the image
RUN python -c "from TTS.utils.manage import ModelManager; ModelManager().download_model('tts_models/de/css10/vits-neon')"

USER root
ENTRYPOINT ["/bin/bash", "-c", "chown -R $MAMBA_USER:$MAMBA_USER /app/out && su $MAMBA_USER -c 'micromamba run -p /env python main.py'"]
# ENTRYPOINT ["/bin/bash", "-c", "micromamba run -p /env python main.py"]