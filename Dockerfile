FROM gradle:8.14.4-jdk17-alpine AS builder
WORKDIR /app

COPY build/libs/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

COPY --from=builder --chown=appuser:appuser /app/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/application/ ./

ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Seoul

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]