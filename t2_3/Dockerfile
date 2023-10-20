FROM openjdk

#RUN apk update && apk add iproute2 net-tools

ARG JAR_FILE=target/DB_Setup-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Aggiungi uno script di inizializzazione
COPY init.sh /init.sh
RUN chmod +x /init.sh

# Modifica l'entrypoint per eseguire lo script di inizializzazione
ENTRYPOINT ["/init.sh"]
