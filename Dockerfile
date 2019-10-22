FROM azul/zulu-openjdk-alpine:11
MAINTAINER team-weasel@subshell.com

RUN java -Xshare:dump

ARG JAR_FILE
ADD ${JAR_FILE} app.jar

ENTRYPOINT exec java $JAVA_OPTS -Xshare:on -Djava.security.egd=file:/dev/./urandom -jar app.jar