FROM openjdk:8-jre-alpine

ENV LEIN_ROOT 1

RUN apk add --update --no-cache ca-certificates wget bash && \
    wget -q "https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein" \
         -O /usr/local/bin/lein && \
    chmod 0755 /usr/local/bin/lein && \
    lein

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY project.clj /usr/src/app/
COPY profiles.clj /usr/src/app/
RUN mkdir -p /root/.lein \
    && wget -q "https://gist.githubusercontent.com/punnie/22f731f3a08097349b9adee31eeba1c7/raw/44ebf441e405ab344375bc6078a60af9efae94ac/gistfile1.txt" \
            -O /root/.lein/profiles.clj
RUN lein deps

COPY . /usr/src/app
