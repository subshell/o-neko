FROM alpine:3.11.5

WORKDIR /workspace

RUN apk update && \
  apk upgrade && \
  apk add --no-cache bash git nodejs-npm maven

RUN git config --global user.email "weasel@subshell.com" && \
  git config --global user.name "Release Weasel"

ENTRYPOINT ["./release/release.sh"]
