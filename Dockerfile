FROM jetty:9
MAINTAINER Liang Ding <dl88250@gmail.com>

RUN apt-get -y install mysql-server
RUN /etc/init.d/mysql start \
    && mysql -uroot -e "grant all privileges on *.* to 'root'@'%' identified by '';" \
    && mysql -uroot -e "grant all privileges on *.* to 'root'@'localhost' identified by '';"
RUN sed -Ei 's/^(bind-address|log)/#&/' /etc/mysql/my.cnf \
    && echo 'skip-host-cache\nskip-name-resolve' | awk '{ print } $1 == "[mysqld]" && c == 0 { c = 1; system("cat") }' /etc/mysql/my.cnf > /tmp/my.cnf \
    && mv /tmp/my.cnf /etc/mysql/my.cnf

RUN apt-get install -y maven

RUN git clone https://github.com/b3log/symphony /sym
RUN cd /sym && mvn package -Dmaven.test.skip=true
RUN mv /sym/target/symphony.war /var/lib/jetty/webapps/ROOT.war

EXPOSE 8080
CMD ["/usr/bin/mysqld_safe"]
