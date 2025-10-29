# Pinned Alpine Image
#FROM andrius/asterisk:alpine-latest-20.5.2
FROM andrius/asterisk@sha256:6180c354ede3874ebec6baf93e94a10b926b3e79b70524fa35ea94881117d77d

# Install only necessary Asterisk modules (Alpine packages)
RUN apk add --no-cache \
  asterisk-sounds-moh \
  asterisk-srtp \
  asterisk-speex

# Expose not previously exposed Ports
EXPOSE 8088
EXPOSE 10000-10100