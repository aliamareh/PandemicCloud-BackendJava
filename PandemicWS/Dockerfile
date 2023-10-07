# Use a base Java image
FROM amazoncorretto:17-alpine-jdk
RUN apk add --update maven

# Set the working directory
WORKDIR /app

# Copy your Java application JAR file to the container
COPY . /app

RUN mvn package -Dmaven.test.skip=true

# Specify the command to run your Java application
CMD ["java", "-jar", "target/PandemicWS-0.0.1-SNAPSHOT.jar"]



