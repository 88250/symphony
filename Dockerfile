FROM maven:3-jdk-8-alpine
LABEL maintainer="Liang Ding<d@b3log.org>"

WORKDIR /opt/sym
ADD . /tmp

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone \
    && cd /tmp && mvn package -DskipTests -Pci && mv target/symphony/* /opt/sym/ \
    && cp -f /tmp/src/main/resources/docker/* /opt/sym/WEB-INF/classes/ \
    && rm -rf /tmp/* && rm -rf ~/.m2

EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "WEB-INF/lib/*:WEB-INF/classes", "org.b3log.symphony.Starter" ]
