# 1. 빌드 스테이지: 소스 코드를 컴파일하고 JAR 파일을 만듭니다.
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar

# 2. 실행 스테이지: JRE 환경에서 애플리케이션 실행
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# ✅ [수정됨] ffmpeg를 설치합니다.
RUN apt-get update && \
    apt-get install -y ffmpeg --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT 8080
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

# ✅ Cloud Run이 리스닝 포트를 인식할 수 있도록
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]