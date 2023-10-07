# On utilise une image de base avec Java
FROM amazoncorretto:17-alpine-jdk
RUN apk add --update maven

# On définit le dossier sur lequel on travaille
WORKDIR /app

# On copie tout vers le dossier app
COPY . /app

# On lance les commandes nécessaires
RUN cd Pandemic && mvn clean install -Dmaven.test.skip=true && cd .. && cd PandemicWS && mvn package -Dmaven.test.skip=true && cd target # && java -jar PandemicWS-0.0.1-SNAPSHOT.jar

# On lance le jar à l'execution
CMD ["java", "-jar", "/app/PandemicWS/target/PandemicWS-0.0.1-SNAPSHOT.jar"]





