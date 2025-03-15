# Práctica 1 - Chat con Sockets en Java

## 📌 Requisitos
- **JDK 1.8**
- **Apache Maven**
- **Eclipse 2023**

## 🚀 Instalación y ejecución
El proyecto está configurado para ejecutarse con **Maven**.

### 1️⃣ Compilar el proyecto
```sh
mvn clean package
```

### 2️⃣ Ejecutar el servidor
Para iniciar el servidor en el puerto **1500**:
```sh
mvn exec:java@server
```

### 3️⃣ Ejecutar clientes
Cada cliente debe ejecutarse en una terminal:
```sh
mvn exec:java@client
```

### 📜 Generar la documentación Javadoc
```sh
ant javadoc
```
