FROM openjdk:21-jdk-slim
WORKDIR mir-exchange
ADD target/mir-exchange.jar mirexchange.jar
ENV TZ="Asia/Almaty"
ENTRYPOINT java -jar mirexchange.jar