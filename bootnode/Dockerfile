FROM openjdk:8-jdk-stretch

COPY build/libs/bootnode-1.0-SNAPSHOT-all.jar /usr/src/bootnode/bootnode.jar
COPY src/main/resources/configuration.yaml /usr/src/bootnode/configuration.yaml
WORKDIR /usr/src/bootnode

CMD ["/bin/sh", "-c", "java $JAVA_OPTS -jar bootnode.jar server configuration.yaml"]