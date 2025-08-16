#1st Layer
FROM amazoncorretto:17-alpine-jdk as builder
WORKDIR extracted
ADD ./build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract


#2nd Layer
FROM amazoncorretto:17-alpine-jdk
LABEL authors="Kevin"

WORKDIR application

#Copy from exploded fat JAR file
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]