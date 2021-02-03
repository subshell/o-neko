FROM azul/zulu-openjdk-alpine:11.0.10-11.45.27 as builder

WORKDIR /app
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM azul/zulu-openjdk-alpine:11.0.10-11.45.27
LABEL maintainer="team-weasel@subshell.com"

RUN java -Xshare:dump

# Install Helm + Helm GCS
RUN apk add --update --no-cache curl ca-certificates git && \
    curl -L https://get.helm.sh/helm-v3.5.0-linux-amd64.tar.gz |tar xvz && \
    mv linux-amd64/helm /usr/bin/helm && \
    chmod +x /usr/bin/helm && \
    helm plugin install https://github.com/hayorov/helm-gcs --version 0.3.9 && \
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
WORKDIR /app
ENTRYPOINT ["java", "-noverify", "-Xshare:on", "-XX:TieredStopAtLevel=1", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]
