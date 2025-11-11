# 🏦 Sistema Bancario con Microservicios

Este proyecto implementa un sistema bancario modular utilizando arquitectura de microservicios, con comunicación asíncrona mediante RabbitMQ.

## 📋 Descripción

El sistema está compuesto por dos microservicios independientes:

- **ms-banco-cliente** (Puerto: 8081) - Gestión de clientes
- **ms-banco-cuenta** (Puerto: 8082) - Gestión de cuentas bancarias

## 🛠 Tecnologías Utilizadas

- **Java 21** - Lenguaje de programación
- **Spring Boot 3.x** - Framework principal
- **Gradle** - Gestión de dependencias y build
- **MySQL** - Base de datos relacional
- **RabbitMQ** - Mensajería asíncrona
- **Docker** - Contenerización de servicios
- **H2 Database** - Base de datos en memoria para desarrollo
- **Postman** - Cliente para pruebas

## 📦 Estructura del Proyecto

```
banco-microservicios/
├── ms-banco-cliente/
│   ├── src/
│   ├── build.gradle
│   └── application.properties
├── ms-banco-cuenta/
│   ├── src/
│   ├── build.gradle
│   └── application.properties
├── BaseDatos.sql
└── README.md
```

## ⚙️ Configuración

### Prerrequisitos

- Java 21
- Gradle 7.6+
- Docker
- MySQL 8.0+

### Base de Datos

[Script de base de datos](BaseDatos.sql)

### Servicios Externos

#### RabbitMQ con Docker
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

**Panel de administración:** http://localhost:15672
- Usuario: `guest`
- Contraseña: `guest`

#### Configuración de Microservicios

**ms-banco-cliente (application.properties)**
```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/db_banco
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
```

**ms-banco-cuenta (application.properties)**
```properties
server.port=8082
spring.datasource.url=jdbc:mysql://localhost:3306/db_banco
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
```

## 🚀 Ejecución

### Opción 1: Ejecutar con Gradle

```bash
# Ejecutar ms-banco-cliente
cd ms-banco-cliente
./gradlew bootRun

# Ejecutar ms-banco-cuenta (en otra terminal)
cd ms-banco-cuenta
./gradlew bootRun
```

### Opción 2: Construir y ejecutar JARs

```bash
# Construir ambos proyectos
./gradlew build

# Ejecutar los JARs generados
java -jar ms-banco-cliente/build/libs/ms-banco-cliente-0.0.1-SNAPSHOT.jar
java -jar ms-banco-cuenta/build/libs/ms-banco-cuenta-0.0.1-SNAPSHOT.jar
```

## 🔄 Comunicación entre Microservicios

Los microservicios se comunican de forma asíncrona mediante RabbitMQ:

- **Eventos:** Cuando se crea un cliente en `ms-banco-cliente`, se publica un evento
- **Cola:** `clientes.queue`
- **Exchange:** `cliente-creado-exchange`

### Configuración de Reintentos

```properties
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=2000
spring.rabbitmq.listener.simple.retry.max-attempts=3
```

## 🗃️ Base de Datos

### Configuración JPA
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Consola H2 (Solo desarrollo)
Se usa para pruebas de integración
- **ms-banco-cliente:** http://localhost:8081/h2-console
- **ms-banco-cuenta:** http://localhost:8082/h2-console

## 📊 Endpoints Principales

### ms-banco-cliente (Puerto 8081)
```
GET  /api/clientes          - Listar todos los clientes
POST /api/clientes          - Crear nuevo cliente
GET  /api/clientes/{id}     - Obtener cliente por ID
PUT  /api/clientes/{id}     - Actualizar cliente
DELETE /api/clientes/{id}   - Eliminar cliente
```

### ms-banco-cuenta (Puerto 8082)
```
GET  /api/cuentas           - Listar todas las cuentas
POST /api/cuentas           - Crear nueva cuenta
GET  /api/cuentas/{id}      - Obtener cuenta por ID
PUT  /api/cuentas/{id}      - Actualizar cuenta
DELETE /api/cuentas/{id}    - Eliminar cuenta
```

## 🚀 Pruebas con postman

Importar la colección de postman para realizar pruebas de los end points: 

- [Banco-Api.postman_collection.json](Banco-Api.postman_collection.json)

## 🐛 Troubleshooting

### Problemas comunes y soluciones

1. **Conexión a RabbitMQ falla:**
   - Verificar que el contenedor Docker esté corriendo: `docker ps`
   - Verificar credenciales en application.properties

2. **Error de conexión a MySQL:**
   - Asegurar que MySQL esté ejecutándose
   - Verificar que la base de datos `db_banco` exista

## 🧪 Testing

```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests de integración
./gradlew integrationTest
```

## 📈 Monitoreo

### Logs Configurados
- Nivel DEBUG para SQL de Hibernate
- Nivel TRACE para binders de SQL
- Logs de RabbitMQ habilitados

## 👨‍💻 Autor

**Joel Ontuña** - [GitHub](https://github.com/JoelOntuDeveloper)

---

**Nota:** Asegúrate de tener todos los servicios (MySQL, RabbitMQ) ejecutándose antes de iniciar los microservicios.