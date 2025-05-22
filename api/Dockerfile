FROM openjdk:17-jdk

COPY build/libs/*.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]