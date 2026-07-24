# Usa uma imagem oficial do Java 17 leve (Alpine)
FROM eclipse-temurin:17-jdk-alpine

# Define a pasta de trabalho dentro do contêiner
WORKDIR /app

# Copia os arquivos do projeto para o contêiner
COPY . .

# Dá permissão de execução para o empacotador do Maven
RUN chmod +x ./mvnw

# Compila o projeto pulando os testes para ser mais rápido
RUN ./mvnw clean package -DskipTests

# Expõe a porta 8080 que a nossa API usa
EXPOSE 8080

# Comando para iniciar a aplicação compilada
CMD ["java", "-jar", "target/api-0.0.1-SNAPSHOT.jar"]