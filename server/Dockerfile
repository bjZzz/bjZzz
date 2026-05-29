FROM eclipse-temurin:8-jdk AS builder

WORKDIR /build
COPY . .

# .mvn/maven.config 会加载 Windows 本地仓库路径 (D:\.m2)，在 Linux 容器内导致依赖无法正确解析
RUN rm -f .mvn/maven.config \
    && chmod +x mvnw \
    && ./mvnw clean package -DskipTests -B -q -s .mvn/settings.docker.xml

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
