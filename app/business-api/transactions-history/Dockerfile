FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS build

WORKDIR /workspace/app
EXPOSE 3100

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x ./mvnw
# Convert CRLF to LF
RUN sed -i 's/\r$//' ./mvnw
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM mcr.microsoft.com/openjdk/jdk:21-mariner

ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app


RUN curl -LJ -o /app/applicationinsights-agent-3.5.4.jar https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.5.4/applicationinsights-agent-3.5.4.jar
COPY applicationinsights.json /app

EXPOSE 8080

ENTRYPOINT ["java","-javaagent:/app/applicationinsights-agent-3.5.4.jar","-noverify", "-XX:MaxRAMPercentage=70", "-XX:+UseParallelGC", "-XX:ActiveProcessorCount=2", "-cp","app:app/lib/*","com.microsoft.openai.samples.assistant.business.TransactionsHistoryApplication"]