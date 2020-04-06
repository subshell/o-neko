FROM alpine:3.11.5

RUN apk update && \
  apk upgrade && \
  apk add --no-cache bash git xmlstarlet grep nodejs-npm

RUN git config --global user.email "weasel@subshell.com" && \
  git config --global user.name "Release Weasel"

WORKDIR /workspace
ENTRYPOINT ["./release/release.sh"]
