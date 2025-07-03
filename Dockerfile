ARG BASE_IMAGE=azul/zulu-openjdk-alpine:24
ARG JAR_FILE=target/java24.jar

FROM ${BASE_IMAGE} as builder
WORKDIR /aot
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

RUN java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -jar app.jar || true
RUN java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -jar app.jar || true


FROM ${BASE_IMAGE}
COPY --from=builder /aot/app.aot /app.aot
ARG JAR_FILE
COPY ${JAR_FILE} /application.jar

ENTRYPOINT ["java", "-XX:AOTCache=/app.aot", "-Dspring.profiles.active=lab", "-Dfile.encoding=UTF8", "-jar", "/application.jar"]

