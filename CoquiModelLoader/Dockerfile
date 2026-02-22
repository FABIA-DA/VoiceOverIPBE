FROM python:3.13-slim@sha256:3de9a8d7aedbb7984dc18f2dff178a7850f16c1ae7c34ba9d7ecc23d0755e35f

ARG HF_TOKEN
ENV HF_TOKEN=${HF_TOKEN}

# Install Hugging Face Hub
RUN pip install huggingface_hub

# If the Hugging Face Token is set, there is automatic auth for faster download
# Downloads the xtts-v2 model
RUN python -c "from huggingface_hub import snapshot_download; snapshot_download('coqui/XTTS-v2', local_dir='/model')"

CMD ["echo", "Coqui Model loaded"]