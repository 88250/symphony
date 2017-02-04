FROM mysql:5.7
MAINTAINER Liang Ding <dl88250@gmail.com>

RUN apt-get update

RUN mkdir /jdk && cd /jdk
RUN apt-get install -y wget git
RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-linux-x64.tar.gz
RUN tar zxvf jdk-8u111-linux-x64.tar.gz
ENV JAVA_HOME /jdk/jdk1.8.0_111
ENV PATH $PATH:$JAVA_HOME/bin

RUN service mysql restart && mysql -uroot -e "CREATE DATABASE b3log_symphony DEFAULT CHARSET utf8 DEFAULT COLLATE utf8_general_ci;"

RUN cd /
RUN wget http://central.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.0.v20161208/jetty-distribution-9.4.0.v20161208.tar.gz
RUN tar zxvf jetty-distribution-9.4.0.v20161208.tar.gz && mv /jetty-distribution-9.4.0.v20161208 /jetty

RUN wget http://apache.fayea.com/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
RUN tar zxvf apache-maven-3.3.9-bin.tar.gz && mv /apache-maven-3.3.9 /maven
ENV PATH $PATH:$/maven/bin

RUN git clone https://github.com/b3log/symphony /sym
RUN cd /sym && mvn package -Dmaven.test.skip=true
RUN mv /sym/target/symphony.war /jetty/webapps/ROOT.war

EXPOSE 8080