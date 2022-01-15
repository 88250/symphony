FROM maven:3.8.4-openjdk-11 as MVN_BUILD

WORKDIR /opt/sym/
ADD . /tmp
RUN cd /tmp && mvn package -DskipTests -Pci -q && mv target/symphony/* /opt/sym/ \
&& cp -f /tmp/src/main/resources/docker/* /opt/sym/

FROM openjdk:18-alpine
LABEL maintainer="Liang Ding<845765@qq.com>"

WORKDIR /opt/sym/
COPY --from=MVN_BUILD /opt/sym/ /opt/sym/
RUN apk add --no-cache ca-certificates tzdata ttf-dejavu

ENV TZ=Asia/Shanghai
EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "lib/*:.", "org.b3log.symphony.Server" ]
