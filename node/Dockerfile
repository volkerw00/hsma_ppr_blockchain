FROM openjdk:8-jdk-stretch

COPY build/libs/node-1.0-SNAPSHOT-all.jar /usr/src/node/node.jar
COPY src/main/resources/configuration.yaml /usr/src/node/configuration.yaml
WORKDIR /usr/src/node

CMD ["/bin/sh", "-c", "java $JAVA_OPTS -jar node.jar server configuration.yaml"]