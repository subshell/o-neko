FROM azul/zulu-openjdk-alpine:17.0.15-17.58-jre as builder

WORKDIR /app
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM azul/zulu-openjdk-alpine:17.0.15-17.58-jre
LABEL maintainer="team-weasel@subshell.com"

ARG HELM_VERSION="v3.18.3"
ARG HELM_GCS_VERSION="0.4.2"

RUN java -Xshare:dump

# Install Helm + Helm GCS
RUN apk add --update --no-cache curl ca-certificates git && \
    curl -L "https://get.helm.sh/helm-$HELM_VERSION-linux-amd64.tar.gz" | tar xvz && \
    mv linux-amd64/helm /usr/bin/helm && \
    chmod +x /usr/bin/helm && \
    helm plugin install https://github.com/hayorov/helm-gcs --version $HELM_GCS_VERSION && \
    rm -rf linux-amd64 && \
    apk del curl git && \
    rm -f /var/cache/apk/*

# Add O-Neko
WORKDIR /app

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

ENV JDK_JAVA_OPTIONS ""

ENTRYPOINT ["java", "-noverify", "-Xshare:on", "-XX:TieredStopAtLevel=1", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]
