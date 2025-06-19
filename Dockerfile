FROM openjdk:21-jdk-slim
WORKDIR /mir-exchange
COPY target/mir-exchange.jar mir-exchange.jar
ENV TZ="Asia/Almaty"
ENTRYPOINT ["java", "-jar", "mir-exchange.jar"]