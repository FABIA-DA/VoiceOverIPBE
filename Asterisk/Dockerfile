# Alpine Image
FROM andrius/asterisk:alpine-latest-20.5.2

# Install only necessary Asterisk modules (Alpine packages)
RUN apk add --no-cache \
  asterisk-sounds-en \
  asterisk-sounds-moh \
  asterisk-srtp \
  asterisk-speex


# NEW IMAGE
#FROM andrius/asterisk:latest

#USER root

# Install only necessary Asterisk modules
#RUN apt-get update && apt-get install -y --no-install-recommends \
#    asterisk-core-sounds-en \
#    asterisk-moh-opsound-wav \
#    libsrtp2-1 \
# && rm -rf /var/lib/apt/lists/*

# USER asterisk

## Copy your custom Asterisk config
COPY ./asterisk-config/*.conf /etc/asterisk/

# Optional: copy custom sounds (or mount them at runtime instead)
# COPY ./sounds/ /var/lib/asterisk/sounds/custom/
