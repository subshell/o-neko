FROM azul/zulu-openjdk-alpine:11-jre as builder

WORKDIR /app
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM azul/zulu-openjdk-alpine:11-jre
LABEL maintainer="team-weasel@subshell.com"

WORKDIR /app

RUN java -Xshare:dump

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

ENV JDK_JAVA_OPTIONS ""
WORKDIR /app
ENTRYPOINT ["java", "-noverify", "-Xshare:on", "-XX:TieredStopAtLevel=1", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]
