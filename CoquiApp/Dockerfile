# Generate WAVs
FROM mambaorg/micromamba:git-c0f93d2 AS builder
WORKDIR /app

# Needed for the model download
ENV COQUI_TOS_AGREED=1

COPY --chown=$MAMBA_USER:$MAMBA_USER environment.yml /tmp/env.yaml
RUN micromamba install -y -n base -f /tmp/env.yaml && \
    micromamba clean --all --yes

# Activate Conda Environment
ARG MAMBA_DOCKERFILE_ACTIVATE=1

COPY --chown=$MAMBA_USER:$MAMBA_USER main.py Phrases.csv ./
RUN python main.py

# Copy the static files to the runtime
FROM alpine:3.23
WORKDIR /app
COPY --from=builder /app/out ./out