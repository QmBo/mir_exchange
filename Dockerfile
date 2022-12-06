FROM openjdk:11-jdk-oracle
WORKDIR mir-exchange
ADD target/mir-exchange.jar app.jar
ENTRYPOINT java -jar app.jar