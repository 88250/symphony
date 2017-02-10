FROM ubuntu:16.04
MAINTAINER Liang Ding <dl88250@gmail.com>

RUN echo 'deb http://mirrors.aliyun.com/ubuntu/ xenial main restricted universe multiverse' > /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyun.com/ubuntu/ xenial-security main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyun.com/ubuntu/ xenial-updates main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyun.com/ubuntu/ xenial-proposed main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyun.com/ubuntu/ xenial-backports main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyun.com/ubuntu/ xenial main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyun.com/ubuntu/ xenial-security main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyun.com/ubuntu/ xenial-updates main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyun.com/ubuntu/ xenial-proposed main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyun.com/ubuntu/ xenial-backports main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyuncs.com/ubuntu/ xenial main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyuncs.com/ubuntu/ xenial-security main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyuncs.com/ubuntu/ xenial-updates main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyuncs.com/ubuntu/ xenial-proposed main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb http://mirrors.aliyuncs.com/ubuntu/ xenial-backports main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyuncs.com/ubuntu/ xenial main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyuncs.com/ubuntu/ xenial-security main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyuncs.com/ubuntu/ xenial-updates main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyuncs.com/ubuntu/ xenial-proposed main restricted universe multiverse' >> /etc/apt/sources.list\
 && echo 'deb-src http://mirrors.aliyuncs.com/ubuntu/ xenial-backports main restricted universe multiverse' >> /etc/apt/sources.list

RUN apt-get update && apt-get install -y wget git

RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-linux-x64.tar.gz
RUN tar zxvf jdk-8u111-linux-x64.tar.gz
ENV JAVA_HOME /jdk1.8.0_111
ENV PATH $PATH:$JAVA_HOME/bin
RUN echo '' >> /etc/profile \
 && echo '# JDK' >> /etc/profile \
 && echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile \
 && echo 'export PATH="$PATH:$JAVA_HOME/bin"' >> /etc/profile \
 && echo '' >> /etc/profile \
 && . /etc/profile

RUN wget http://central.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.2.7.v20150116/jetty-distribution-9.2.7.v20150116.tar.gz
RUN tar zxvf jetty-distribution-9.2.7.v20150116.tar.gz && mv /jetty-distribution-9.2.7.v20150116 /jetty && rm -rf /jetty/webapps/*

RUN wget http://apache.fayea.com/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
RUN tar zxvf apache-maven-3.3.9-bin.tar.gz && mv /apache-maven-3.3.9 /maven

RUN git clone https://git.oschina.net/dl88250/symphony /sym;cd /sym && /maven/bin/mvn package -Dmaven.test.skip=true
RUN mv /sym/target/symphony.war /jetty/webapps/ROOT.war

RUN DEBIAN_FRONTEND=noninteractive apt-get -y install mysql-server
RUN service mysql start && mysql -uroot -e "CREATE DATABASE b3log_symphony DEFAULT CHARSET utf8 DEFAULT COLLATE utf8_general_ci;"

WORKDIR /jetty

EXPOSE 8080
CMD ["service", "mysql", "restart"]