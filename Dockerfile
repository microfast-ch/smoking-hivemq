# Build Project with Maven
FROM maven:3.6.3-jdk-11 AS MAVEN_TOOL_CHAIN
COPY . /tmp/
WORKDIR /tmp/
RUN mvn install

# Setup HiveMQ with plugin
FROM hivemq/hivemq-ce:2020.2
RUN mkdir -p -m777 /opt/smoker
RUN chown hivemq:hivemq /opt/smoker
COPY --from=MAVEN_TOOL_CHAIN /tmp/target/hivemq-smoker /opt/hivemq/extensions/hivemq-smoker