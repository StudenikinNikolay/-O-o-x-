FROM openjdk:17-jdk-alpine

ARG SERVER_PORT=8080
ARG JAR_FILE=target/fileadmin-1.0.jar

ENV SERVER_PORT=${SERVER_PORT}

RUN mkdir -p /app/jars /app/lib /app/META-INF
COPY ${JAR_FILE} /app/jars/app.jar

RUN unzip /app/jars/app.jar -d /app/jars \
    && cp -rf /app/jars/BOOT-INF/lib/* /app/lib \
    && cp -rf /app/jars/META-INF/* /app/META-INF \
    && cp -rf /app/jars/BOOT-INF/classes/* /app

EXPOSE ${SERVER_PORT}

CMD ["sh","-c","java -cp app:app/lib/* -Dserver.port=${SERVER_PORT} edu.diploma.FileApplication"]