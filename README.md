# Athenet — API Statistics

Microservicio encargado de la **transmisión y persistencia de marcadores en tiempo real** dentro de la plataforma **Athenet**.

Athenet nace con la visión de impulsar el deporte estudiantil y consolidar la comunidad deportiva a nivel nacional. Para acercar cada encuentro a los espectadores sin recargar la base de datos transaccional, `api-athenet-statistics` desacopla la lectura masiva en vivo del flujo de competencias: consume los eventos emitidos por `api-athenet-competition` en Apache Kafka, persiste el estado vigente de cada partido en MongoDB y lo difunde al instante mediante WebSocket STOMP hacia la web pública.

### Stack

<div align="center">

[![Java](https://img.shields.io/badge/Java_21-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-%236DB33F.svg?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-%23000000.svg?style=for-the-badge&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

</div>

- Java 21 (LTS) con [Spring Boot 4](https://spring.io/projects/spring-boot)
- [Apache Kafka](https://kafka.apache.org/) (consumidor del tópico `marcador`, grupo `estadisticas`)
- [MongoDB 8](https://www.mongodb.com/) para persistencia del último estado por encuentro (`statistics.marcadores`)
- WebSocket + STOMP para difusión en tiempo real (`/ws`)
- Docker y Docker Compose para orquestación local y en EC2

### Desarrollo Local

> **Nota:** Para evitar conflictos de puerto con `api-athenet-competition` (`8082`), este microservicio escucha por defecto en el puerto **`8083`**. El `docker-compose.yml` levanta automáticamente Kafka, `kafka-init` (crea el tópico `marcador` con 1 partición), Kafka UI (`:8090`), MongoDB (`:27017`) y el propio servicio (`:8083`).

Levanta el proyecto en tu entorno local:

1. **Verifica dependencias:**
   ```bash
   docker --version
   docker compose version
   java -version
   ```
   *(Docker es suficiente para levantar todo el stack; Java 21 solo se requiere si ejecutas Maven o tests fuera de contenedor).*

2. **Configura las variables de entorno:**
   Variables disponibles para `api-athenet-statistics`:

   | Variable | Descripción | Valor por defecto |
   | :--- | :--- | :--- |
   | `SERVER_PORT` | Puerto HTTP y WebSocket del servicio. | `8083` |
   | `SPRING_MONGODB_URI` | URI de conexión a MongoDB. | `mongodb://localhost:27017/statistics` |
   | `KAFKA_BOOTSTRAP_SERVERS` | Dirección del broker Kafka (`kafka:29092` en Docker, `localhost:9092` en host). | `localhost:9092` |
   | `KAFKA_TOPIC_MARCADOR` | Nombre del tópico de Kafka con los eventos de marcador. | `marcador` |
   | `ESTADISTICAS_HILOS` | Concurrencia del listener de Kafka (`estadisticas`). | `1` |
   | `STATISTICS_ALLOWED_ORIGINS` | Orígenes permitidos para CORS y STOMP, separados por coma. | `http://localhost:5173` |
   | `AUDIT_ENABLED` | Activa un consumidor secundario (`auditoria`) para trazabilidad de eventos (`true` \| `false`). | `false` |

   > Para desarrollo local con orígenes abiertos (`*`), ejecuta con el perfil `dev`: `SPRING_PROFILES_ACTIVE=dev`.

3. **Inicia el stack completo con Docker Compose:**
   ```bash
   docker compose up -d --build
   ```
   Accesos locales una vez levantado:
   - **REST API:** `http://localhost:8083/marcador/actual` y `http://localhost:8083/marcador/{idEncuentro}`
   - **WebSocket STOMP:** `ws://localhost:8083/ws` (tópicos `/topic/marcador` y `/topic/marcador/{idEncuentro}`)
   - **Kafka UI:** `http://localhost:8090`

   Para detener y limpiar volúmenes locales:
   ```bash
   docker compose down -v
   ```

4. **Ejecuta los tests unitarios:**
   ```bash
   ./mvnw clean test
   ```
   *(En Windows: `mvnw.cmd clean test` con `JAVA_HOME` apuntando a JDK 21).*

### Contrato de Eventos (Tópico `marcador`)

Cada evento publicado con `key = idEncuentro` sigue el contrato:

```json
{
  "idEncuentro": 4,
  "equipoLocal": "Leonas UC",
  "equipoVisita": "Titanes IT",
  "puntosLocal": 2,
  "puntosVisita": 1,
  "tipo": "INICIADO | PUNTO | FINALIZADO"
}
```

##### Dependencias principales:
- spring-boot-starter-web
- spring-boot-starter-websocket
- spring-boot-starter-kafka
- spring-boot-starter-data-mongodb
- lombok
