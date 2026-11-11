# Etapa 1: Compilación (Build)
# Usamos Maven con JDK 21 para compilar (compatible con <java.version>17</java.version> en pom.xml)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar la caché de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y empaquetamos target/app.jar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de Ejecución (Runtime)
# Usamos JRE en lugar de JDK para reducir el tamaño y no ejecutamos como root
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/uploads \
    && chown -R spring:spring /app
USER spring:spring

# Copiamos el JAR generado (<finalName>app</finalName> en pom.xml)
COPY --from=build /app/target/app.jar app.jar

# Render inyecta la variable PORT (por defecto 10000); localmente usa 80
EXPOSE 80 10000

# Parámetros de optimización de memoria para contenedores
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
