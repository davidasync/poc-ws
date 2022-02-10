FROM bellsoft/liberica-openjdk-alpine-musl:17 as builder
WORKDIR application
COPY . .
RUN ./gradlew bootjar
RUN java -Djarmode=layertools -jar build/libs/*-SNAPSHOT.jar extract

FROM bellsoft/liberica-openjdk-alpine-musl:17
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
