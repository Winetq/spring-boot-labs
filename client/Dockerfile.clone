FROM ubuntu:18.04 AS clone

RUN apt update
RUN apt install -y git

RUN git clone -b lab4_js https://github.com/Winetq/spring-boot-labs.git

FROM httpd:2.4
COPY --from=clone /spring-boot-labs/. /usr/local/apache2/htdocs/
