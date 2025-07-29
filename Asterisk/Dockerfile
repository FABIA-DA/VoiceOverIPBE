FROM andrius/asterisk:latest

# Install only necessary Asterisk modules (Alpine packages)
RUN apk add --no-cache \
  asterisk-sounds-en \
  asterisk-sounds-moh \
  asterisk-srtp \
  asterisk-speex

# Copy your custom Asterisk config
COPY ./asterisk-config/*.conf /etc/asterisk/

# Optional: copy custom sounds (or mount them at runtime instead)
COPY ./sounds/ /var/lib/asterisk/sounds/custom/
