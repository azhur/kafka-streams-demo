FROM azul/zulu-openjdk-alpine:11
COPY target/dependency-jars /run/dependency-jars
ADD target/application.jar /run/application.jar

ENTRYPOINT java -jar run/application.jar -D exec.mainClass="io.demo.kafka.streams.Launch"