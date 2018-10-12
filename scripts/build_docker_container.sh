#!/usr/bin/env bash

# https://github.com/b3log/symphony/pull/779
echo package...
docker run --rm -it -v $(pwd)/..:/project -w /project maven mvn package
echo starting mysql...
docker stop sym-mysql && docker rm sym-mysql
docker run -d --name sym-mysql -v $(pwd)/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=b3log_symphony mysql:5.7 --lower_case_table_names=1 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
echo starting jetty...
docker stop jetty-sym && docker rm jetty-sym
docker run -d --name jetty-sym  -v $(pwd)/../target/symphony.war:/var/lib/jetty/webapps/ROOT.war -p 8080:8080 --link sym-mysql:sym-mysql jetty:9.3.24-jre8-alpine
