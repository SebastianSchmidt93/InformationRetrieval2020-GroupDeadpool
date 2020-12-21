FROM openjdk:11

# Copy necessary files into container
RUN mkdir /app
COPY app /app

# Grant permission to run
RUN chmod a=x /app/*

# When running the container, the parameters for the program have to be appended to the "docker run ..."
ENTRYPOINT ["java", "-jar", "/app/ArgumentSearchEngine.jar"]


