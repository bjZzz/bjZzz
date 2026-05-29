FROM eclipse-temurin:8-jdk AS builder

WORKDIR /build
COPY . .

RUN chmod +x mvnw \
    && ./mvnw clean package -DskipTests -B -q

FROM eclipse-temurin:8-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r nanda && useradd -r -g nanda nanda

COPY --from=builder /build/nanda-boot/target/nanda-boot-*.jar /app/app.jar

USER nanda

EXPOSE 8080

ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/app.jar"]
