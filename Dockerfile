FROM maven:3-jdk-8-alpine as MVN_BUILD

WORKDIR /opt/sym/
ADD . /tmp
RUN cd /tmp && mvn package -DskipTests -Pci && mv target/symphony/* /opt/sym/ \
    && cp -f /tmp/src/main/resources/docker/* /opt/sym/WEB-INF/classes/ \
    && rm -rf /tmp/* && rm -rf ~/.m2

FROM openjdk:8-alpine
LABEL maintainer="Liang Ding<d@b3log.org>"

WORKDIR /opt/sym/
COPY --from=MVN_BUILD /opt/sym/ /opt/sym/
RUN apk add --no-cache ca-certificates tzdata ttf-dejavu

ENV TZ=Asia/Shanghai
EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "WEB-INF/lib/*:WEB-INF/classes", "org.b3log.symphony.Starter" ]
