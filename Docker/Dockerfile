FROM openjdk:11

# Install maven
RUN apt-get update
RUN apt-get install -y maven

# Copy necessary files into container
COPY argument-search-engine /usr/local/argument-search-engine

# Build maven-project
WORKDIR /usr/local/argument-search-engine
RUN mvn package
RUN mvn dependency:copy-dependencies

# Grant permission to run
RUN chmod a=x target/argument-search-engine-0.0.1.jar

# When running the container, the parameters for the program have to be appended to the "docker run ..."
ENTRYPOINT ["java", "-jar", "target/argument-search-engine-0.0.1.jar"]


