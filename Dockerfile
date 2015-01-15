FROM ubuntu:14.04
MAINTAINER Mikhail Lapshin <mikhail.a.lapshin@gmail.com>

RUN apt-get -qq update && apt-get -qy opejdk-8-jdk
RUN useradd -d /home/fhir -m -s /bin/bash fhir && echo "fhir:fhir"|chpasswd && adduser fhir sudo
RUN echo 'fhir ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers

USER fhir
ENV HOME /home/fhir
RUN cd /home/fhir && mkdir -p /home/fhir/bin && curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /home/fhir/bin/lein && chmod a+x /home/fhir/bin/lein
ENV PATH /home/fhir/bin:$PATH

COPY . /home/fhir/fhirterm

EXPOSE 80

ENV DB_USER fhirterm
ENV DB_PASSWORD fhirterm
ENV DB_NAME fhirterm

CMD sed "s/DB_USER/$DB_USER/g;s/DB_PASSWORD/$DB_PASSWORD/g;s/DB_HOST/$DB_5432_TCP_ADDR/g;s/DB_PORT/$DB_5432_TCP_PORT/g;s/DB_NAME/$DB_NAME/g" \
    etc/docker-config-template.json > config.json \
    && lein run -c config.json start
