FROM ubuntu:16.10
MAINTAINER Liang Ding <dl88250@gmail.com>

RUN apt-get update

RUN mkdir /jdk && cd /jdk
RUN apt-get install -y wget
RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-linux-x64.tar.gz
RUN tar zxvf jdk-8u111-linux-x64.tar.gz
ENV JAVA_HOME /jdk/jdk1.8.0_111
ENV PATH $PATH:$JAVA_HOME/bin

RUN apt-get -y install mysql-server

#RUN /etc/init.d/mysql start \
#    && mysql -uroot -e "grant all privileges on *.* to 'root'@'%' identified by '';" \
#    && mysql -uroot -e "grant all privileges on *.* to 'root'@'localhost' identified by '';"
#RUN sed -Ei 's/^(bind-address|log)/#&/' /etc/mysql/my.cnf \
#    && echo 'skip-host-cache\nskip-name-resolve' | awk '{ print } $1 == "[mysqld]" && c == 0 { c = 1; system("cat") }' /etc/mysql/my.cnf > /tmp/my.cnf \
#    && mv /tmp/my.cnf /etc/mysql/my.cnf

RUN service mysql start && mysql -uroot -e "CREATE DATABASE `b3log_symphony` DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;"


RUN cd /
RUN wget http://central.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.0.v20161208/jetty-distribution-9.4.0.v20161208.tar.gz
RUN tar zxvf jetty-distribution-9.4.0.v20161208.tar.gz && mv /jetty-distribution-9.4.0.v20161208 /jetty

RUN apt-get install -y maven

RUN git clone https://github.com/b3log/symphony /sym
RUN cd /sym && mvn package -Dmaven.test.skip=true
RUN mv /sym/target/symphony.war /jetty/webapps/ROOT.war

EXPOSE 8080
CMD ["/usr/bin/mysqld_safe"]
