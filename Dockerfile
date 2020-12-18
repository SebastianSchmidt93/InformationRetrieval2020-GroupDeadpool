FROM openjdk:11
RUN mkdir /input-dir
RUN mkdir /output-dir
RUN mkdir /prototype
COPY ./src/LucenePrototype.jar /prototype
#ENTRYPOINT /prototype/LucenePrototype.jar -i /input-dir -o /output-dir


