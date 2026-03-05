FROM mambaorg/micromamba:git-c0f93d2
WORKDIR /app

# Needed for the model download
ENV COQUI_TOS_AGREED=1
USER root

COPY --chown=$MAMBA_USER:$MAMBA_USER environment.yml /tmp/env.yaml
RUN micromamba install -y -n base -f /tmp/env.yaml && \
    micromamba clean --all --yes

# Activate Conda Environment
ARG MAMBA_DOCKERFILE_ACTIVATE=1
ENV PATH=/env/bin:$PATH

# Install the used model
RUN python -c "from TTS.api import TTS; TTS(model_name='tts_models/de/thorsten/vits')"

EXPOSE 8000

COPY --chown=$MAMBA_USER:$MAMBA_USER main.py ./
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]