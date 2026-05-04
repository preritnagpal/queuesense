FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# 🔥 IMPORTANT FIX
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/queuesense-0.0.1-SNAPSHOT.jar"]
