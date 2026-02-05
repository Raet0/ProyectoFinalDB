# Sistema de Gestión de Podcasts con Redis

<div align="center">
    <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg" alt="Redis" height="48" />
    <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" alt="Spring Boot" height="48" />
    <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/angular/angular-original.svg" alt="Angular" height="48" />
</div>

**Universidad Politécnica Salesiana**  
**Gestión de Bases de Datos - Bases de Datos No Relacionales**

**Docente:** Germán Parra  
**Fecha:** 4 de febrero de 2026


## Integrantes del Equipo

| Nombre | Actividades Realizadas | Tiempo Dedicado |
|--------|------------------------|----------------|
| **Rafael Prieto** | Diseño del modelo de datos, implementación del backend (Spring Boot + Redis), configuración de Redis, desarrollo de servicios y controladores, integración backend-frontend | 25 horas |
| **Adrian Lazo** | Desarrollo del frontend (Angular), diseño de interfaces de usuario, implementación de componentes (Dashboard, Create, Detail), integración con API REST | 22 horas |
| **John Serrano** | Diseño de reportes, implementación de consultas complejas en Redis, validación de datos, testing de endpoints, documentación técnica | 20 horas |
| **Matias Sinchi** | Configuración del entorno de desarrollo, carga de datos de prueba (DataSeeder), implementación de filtros y ordenamiento, pruebas de integración | 18 horas |

**Total:** 85 horas

---

## 1. PROBLEMA ELEGIDO: Podcast

### 1.1 Descripción del Problema

Se requiere un sistema que permita registrar y gestionar información de podcasts con las siguientes características:

**Datos del Podcast:**
- Tema general
- Tema del día
- Categoría del tema
- Fecha del episodio
- Audio (URL del archivo MP3)
- Locutor principal: mail, nickname, país de origen, fotografía
- Locutores invitados: mail, nickname, país de origen, fotografía

**Reportes Requeridos:**
1. Podcast con mayores reproducciones
2. Lista de podcasts ordenada por fecha o número de reproducciones
3. Locutores invitados con mayor número de participaciones
4. Locutores por país

### 1.2 Justificación Técnica

**¿Por qué Redis como base de datos NoSQL?**

Redis (Remote Dictionary Server) fue seleccionado por las siguientes razones técnicas:

1. **Tipo de Base de Datos:** Almacén clave-valor con estructuras de datos avanzadas (ZSET, SET, String)

2. **Rendimiento:** Almacenamiento en memoria con tiempos de respuesta sub-milisegundo, capaz de procesar millones de operaciones por segundo

3. **Estructuras de Datos Nativas:**
   - **String/JSON:** Para almacenar objetos completos de podcasts
   - **Sorted Sets (ZSET):** Para rankings de reproducciones y participaciones con operaciones O(log N)
   - **Sets:** Para índices de locutores por país

4. **Ventajas específicas para el problema:**
   - Rankings en tiempo real sin recalcular ordenamientos
   - Consultas "Top N" en O(log N) en lugar de full table scans
   - Índices secundarios mantenidos automáticamente
   - Operaciones atómicas para contadores (sin condiciones de carrera)

5. **Simplicidad:** Integración nativa con Spring Boot mediante Spring Data Redis

**Comparación con alternativas:**
- vs. MySQL/PostgreSQL: Redis es 10-100x más rápido para rankings y contadores, pero sacrifica queries complejas
- vs. MongoDB: Redis supera en rendimiento para rankings, MongoDB es mejor para esquemas variables
- vs. Cassandra: Redis es más simple y suficiente para datasets en memoria

---

## 2. GUÍA DEL MANEJADOR DE BASES DE DATOS: REDIS

### 2.1 Características Principales

**Redis** es una base de datos NoSQL de tipo clave-valor en memoria, de código abierto, utilizada como:
- Base de datos
- Cache
- Message broker

**Características destacadas:**
- **Velocidad:** Todas las operaciones en memoria RAM (latencias < 1ms)
- **Persistencia opcional:** RDB (snapshots) y AOF (append-only file)
- **Replicación:** Arquitectura maestro-esclavo
- **Alta disponibilidad:** Redis Sentinel para failover automático
- **Clustering:** Particionamiento automático de datos
- **Transacciones:** Operaciones atómicas con MULTI/EXEC
- **Pub/Sub:** Sistema de mensajería integrado

### 2.2 Estructuras de Datos

#### 2.2.1 Strings
Tipo básico, almacena cadenas binarias (hasta 512 MB). Puede contener texto, números o JSON.

**Comandos principales:**
```redis
SET key value
GET key
INCR key
DEL key
```

#### 2.2.2 Sorted Sets (ZSET)
Conjuntos ordenados donde cada miembro tiene un score asociado. **Estructura clave para nuestro sistema.**

**Comandos principales:**
```redis
ZADD key score member           # Agregar con puntuación
ZINCRBY key increment member    # Incrementar puntuación
ZREVRANGE key start stop        # Top N descendente
ZSCORE key member               # Obtener puntuación
ZREM key member                 # Eliminar miembro
```

**Ejemplo:**
```redis
ZADD ranking:reproducciones 150 "podcast-1"
ZINCRBY ranking:reproducciones 1 "podcast-1"  # Ahora 151
ZREVRANGE ranking:reproducciones 0 9  # Top 10
```

**Complejidad:** O(log N) para inserción, actualización y eliminación

#### 2.2.3 Sets
Colecciones no ordenadas de strings únicos. Ideales para índices.

**Comandos principales:**
```redis
SADD key member        # Agregar elemento
SMEMBERS key           # Obtener todos
SISMEMBER key member   # Verificar pertenencia
SREM key member        # Eliminar
```

**Ejemplo:**
```redis
SADD indice:pais:colombia "Juan Pérez"
SMEMBERS indice:pais:colombia
```

**Complejidad:** O(1) para SADD, SISMEMBER, SREM

### 2.3 Modelo de Datos en Redis

**Patrón de claves:**
```
entidad:tipo:identificador
```

**Modelo del proyecto:**
```
podcast:data:{id}              → String (JSON del podcast)
ranking:reproducciones         → ZSET (id → reproducciones)
ranking:invitados              → ZSET (nickname → participaciones)
indice:pais:{pais_lowercase}   → SET (nicknames)
```

**Ejemplo de datos:**
```redis
# Datos principales
podcast:data:podcast-1 → {
  "id": "podcast-1",
  "temaGeneral": "Tecnología",
  "temaDia": "El futuro de la IA",
  "categoria": "Tecnología",
  "fecha": "2026-01-15",
  "audioUrl": "https://...",
  "locutorPrincipal": {...},
  "invitados": [...]
}

# Ranking de reproducciones
ranking:reproducciones
├── podcast-3 → 150
├── podcast-1 → 89
└── podcast-7 → 45

# Ranking de invitados
ranking:invitados
├── "Ana Martínez" → 12
├── "Carlos López" → 8
└── "María García" → 5

# Índice por país
indice:pais:colombia → {"Juan Pérez", "Ana Martínez"}
```

### 2.4 Lenguaje de Consulta (Comandos)

**Operaciones Básicas:**
```redis
# Crear/Actualizar
SET podcast:data:1 '{"id":"1","titulo":"..."}'

# Leer
GET podcast:data:1

# Eliminar
DEL podcast:data:1

# Verificar existencia
EXISTS podcast:data:1

# Listar con patrón
KEYS podcast:data:*
```

**Rankings (ZSET):**
```redis
# Inicializar
ZADD ranking:reproducciones 0 podcast-1

# Registrar reproducción
ZINCRBY ranking:reproducciones 1 podcast-1

# Top 10 más reproducidos
ZREVRANGE ranking:reproducciones 0 9 WITHSCORES

# Obtener reproducciones de un podcast
ZSCORE ranking:reproducciones podcast-1
```

**Índices (SET):**
```redis
# Agregar locutor a país
SADD indice:pais:colombia "Juan Pérez"

# Obtener locutores de un país
SMEMBERS indice:pais:colombia
```

---

## 3. MODELO DE DATOS DISEÑADO

### 3.1 Diagrama Conceptual

```
PODCAST
├── id: String (UUID)
├── temaGeneral: String
├── temaDia: String
├── categoria: String
├── fecha: String (ISO-8601)
├── audioUrl: String
├── locutorPrincipal: LOCUTOR
└── invitados: List<LOCUTOR>

LOCUTOR
├── nickname: String
├── mail: String
├── pais: String
└── fotografiaUrl: String
```

### 3.2 Implementación en Redis

#### Almacenamiento Principal (String/JSON)
```java
// Guardar podcast completo
redisTemplate.opsForValue().set("podcast:data:" + id, podcastObject);

// Recuperar podcast
Object podcast = redisTemplate.opsForValue().get("podcast:data:" + id);
```

#### Ranking de Reproducciones (ZSET)
```java
// Inicializar en 0
redisTemplate.opsForZSet().addIfAbsent("ranking:reproducciones", id, 0);

// Registrar reproducción
redisTemplate.opsForZSet().incrementScore("ranking:reproducciones", id, 1);

// Top 10
Set<Object> top = redisTemplate.opsForZSet().reverseRange("ranking:reproducciones", 0, 9);
```

#### Ranking de Invitados (ZSET)
```java
// Incrementar participaciones
redisTemplate.opsForZSet().incrementScore("ranking:invitados", nickname, 1);

// Top 10 invitados
Set<Object> top = redisTemplate.opsForZSet().reverseRange("ranking:invitados", 0, 9);
```

#### Índice por País (SET)
```java
// Agregar locutor al país
redisTemplate.opsForSet().add("indice:pais:" + pais.toLowerCase(), nickname);

// Obtener locutores del país
Set<Object> locutores = redisTemplate.opsForSet().members("indice:pais:" + pais);
```

### 3.3 Operaciones CRUD

**CREATE:**
```java
public void crearPodcast(PodcastDTO dto) {
    PodCast entidad = mapper.toEntity(dto);
    
    // 1. Guardar datos principales
    redisTemplate.opsForValue().set(KEY_DATA + id, entidad);
    
    // 2. Inicializar ranking
    redisTemplate.opsForZSet().addIfAbsent(KEY_VIEWS, id, 0);
    
    // 3. Indexar invitados
    for (Locutor invitado : entidad.getInvitados()) {
        redisTemplate.opsForZSet().incrementScore(KEY_PARTICIPACIONES, invitado.getNickname(), 1);
        redisTemplate.opsForSet().add(KEY_PAIS + invitado.getPais(), invitado.getNickname());
    }
    
    // 4. Indexar locutor principal
    redisTemplate.opsForSet().add(KEY_PAIS + locutor.getPais(), locutor.getNickname());
}
```

**READ:**
```java
public PodcastDTO obtenerPodcast(String id) {
    Object data = redisTemplate.opsForValue().get(KEY_DATA + id);
    PodcastDTO dto = mapper.toDTO(data);
    
    // Agregar vistas desde ranking
    Double score = redisTemplate.opsForZSet().score(KEY_VIEWS, id);
    dto.setVistas(score != null ? score.intValue() : 0);
    
    return dto;
}
```

**UPDATE (Reproducción):**
```java
public void registrarReproduccion(String id) {
    // Operación atómica
    redisTemplate.opsForZSet().incrementScore(KEY_VIEWS, id, 1);
}
```

**DELETE:**
```java
public void eliminarPodcast(String id) {
    // 1. Obtener datos para limpieza
    Object data = redisTemplate.opsForValue().get(KEY_DATA + id);
    PodCast podcast = mapper.toEntity(mapper.toDTO(data));
    
    // 2. Limpiar índices de invitados (evitar datos huérfanos)
    for (Locutor invitado : podcast.getInvitados()) {
        redisTemplate.opsForZSet().incrementScore(KEY_PARTICIPACIONES, invitado.getNickname(), -1);
        Double score = redisTemplate.opsForZSet().score(KEY_PARTICIPACIONES, invitado.getNickname());
        if (score != null && score <= 0) {
            redisTemplate.opsForZSet().remove(KEY_PARTICIPACIONES, invitado.getNickname());
        }
    }
    
    // 3. Eliminar datos principales
    redisTemplate.delete(KEY_DATA + id);
    redisTemplate.opsForZSet().remove(KEY_VIEWS, id);
}
```

---

## 4. CONFIGURACIÓN DEL MANEJADOR

### 4.1 Instalación de Redis

**Docker (Recomendado):**
```bash
docker pull redis:latest
docker run -d --name redis-podcast -p 6379:6379 \
  redis redis-server --requirepass 162003
```

**Verificación:**
```bash
docker exec -it redis-podcast redis-cli
> AUTH 162003
> PING
PONG
```

### 4.2 Configuración Backend (Spring Boot)

**Dependencias (build.gradle.kts):**
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}
```

**Configuración (application.yaml):**
```yaml
spring:
  application:
    name: podcast-back
  data:
    redis:
      host: localhost
      port: 6379
      password: 162003
      database: 1
      timeout: 6000ms
```

**Clase de Configuración (RedisConfig.java):**
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // Serializadores para legibilidad
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
```

### 4.3 Verificación

```java
@RestController
public class RedisTestRunner {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/")
    public String prueba() {
        try {
            redisTemplate.opsForValue().set("test:connection", "OK");
            String value = redisTemplate.opsForValue().get("test:connection");
            return "✅ Redis operativo: " + value;
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}
```

---

## 5. ELECCIÓN DEL FRAMEWORK DE DESARROLLO

### 5.1 Backend: Spring Boot 3.5.10

**Justificación:**
- **Ecosistema robusto:** Spring Data Redis para integración nativa
- **Autoconfiguration:** Configuración automática de RedisTemplate
- **RESTful por defecto:** Controladores con anotaciones simples
- **Inyección de dependencias:** Gestión automática de beans
- **Serialización JSON:** Jackson integrado
- **Producción ready:** Actuator, logging, monitoring

**Arquitectura:**
```
potcast-back/
├── controller/  → API REST (endpoints)
├── service/     → Lógica de negocio
├── model/       → Entidades (PodCast, Locutor)
├── dtos/        → Data Transfer Objects
├── mappers/     → Conversión Entity ↔ DTO
└── config/      → Configuración de Redis
```

### 5.2 Frontend: Angular 20.3.0

**Justificación:**
- **Framework completo:** Componentes, routing, HTTP client integrados
- **TypeScript:** Tipado estático reduce bugs
- **Standalone Components:** Sin módulos NgModule (simplificación)
- **RxJS:** Observables para eventos asíncronos
- **Tailwind CSS:** Diseño rápido con utilidades

**Arquitectura:**
```
Potcast/src/app/
├── models/      → Interfaces (Podcast, Locutor)
├── services/    → HTTP Client (PodcastService)
└── pages/
    ├── dashboard/       → Listado + reportes
    ├── create-podcast/  → Formulario de creación
    └── podcast-detail/  → Detalle + reproductor
```

### 5.3 Arquitectura General

```
┌─────────────────────────────────┐
│   ANGULAR FRONTEND (SPA)        │
│      localhost:4200              │
│                                  │
│   Dashboard / Create / Detail   │
│             ↓                    │
│      PodcastService              │
│       (HTTP Client)              │
└──────────┬──────────────────────┘
           │ REST API (JSON)
┌──────────▼──────────────────────┐
│   SPRING BOOT BACKEND            │
│      localhost:8080              │
│                                  │
│   PodcastController              │
│           ↓                      │
│   PodcastService                 │
│           ↓                      │
│   RedisTemplate                  │
└──────────┬──────────────────────┘
           │ Redis Protocol
┌──────────▼──────────────────────┐
│     REDIS SERVER                 │
│      localhost:6379              │
│                                  │
│   • podcast:data:*               │
│   • ranking:reproducciones       │
│   • ranking:invitados            │
│   • indice:pais:*                │
└──────────────────────────────────┘
```

---

## 6. CARGA DE DATOS Y CONSULTAS

### 6.1 DataSeeder

**Carga automática de 100 podcasts de prueba** mediante `CommandLineRunner`:

```java
@Component
public class DataSeeder implements CommandLineRunner {
    @Autowired
    private PodcastService podcastService;

    @Override
    public void run(String... args) {
        System.out.println("🌱 Iniciando carga de 100 podcasts...");
        
        for (int i = 1; i <= 100; i++) {
            PodcastDTO podcast = generarPodcastAleatorio(i);
            podcastService.crearPodcast(podcast);
        }
        
        // Simular reproducciones aleatorias (5-104 por podcast)
        Random random = new Random();
        for (int i = 1; i <= 100; i++) {
            int reproducciones = random.nextInt(100) + 5;
            for (int j = 0; j < reproducciones; j++) {
                podcastService.registrarReproduccion("podcast-" + i);
            }
        }
        
        System.out.println("✅ 100 Podcasts cargados!");
    }
}
```

**Datos generados:**
- 10 categorías (Tecnología, Negocios, Salud, etc.)
- 40 hosts diferentes
- 10 países
- 100 temas variados
- URLs de audio reales (SoundHelix)
- Fotografías generadas (DiceBear API)
- 0-3 invitados aleatorios por podcast
- Fechas en últimos 60 días

### 6.2 Consultas Implementadas

#### 1. Listar Todos los Podcasts
**Endpoint:** `GET /api/podcasts`

```java
public List<PodcastDTO> listarTodos() {
    Set<String> keys = redisTemplate.keys(KEY_DATA + "*");
    List<Object> objects = redisTemplate.opsForValue().multiGet(keys);
    
    List<PodcastDTO> lista = new ArrayList<>();
    for (Object obj : objects) {
        PodcastDTO dto = mapper.toDTO(obj);
        Double score = redisTemplate.opsForZSet().score(KEY_VIEWS, dto.getId());
        dto.setVistas(score != null ? score.intValue() : 0);
        lista.add(dto);
    }
    return lista;
}
```

#### 2. Obtener Podcast por ID
**Endpoint:** `GET /api/podcasts/{id}`

```java
public PodcastDTO obtenerPodcast(String id) {
    Object data = redisTemplate.opsForValue().get(KEY_DATA + id);
    if (data == null) return null;
    
    PodcastDTO dto = mapper.toDTO(data);
    Double score = redisTemplate.opsForZSet().score(KEY_VIEWS, id);
    dto.setVistas(score != null ? score.intValue() : 0);
    
    return dto;
}
```

#### 3. Crear Podcast
**Endpoint:** `POST /api/podcasts`

Ver implementación completa en sección 3.3 (CRUD - CREATE).

#### 4. Eliminar Podcast
**Endpoint:** `DELETE /api/podcasts/{id}`

Ver implementación completa en sección 3.3 (CRUD - DELETE).

#### 5. Registrar Reproducción
**Endpoint:** `POST /api/podcasts/{id}/play`

```java
public void registrarReproduccion(String id) {
    redisTemplate.opsForZSet().incrementScore(KEY_VIEWS, id, 1);
}
```

#### 6. Reporte: Podcasts Más Reproducidos
**Endpoint:** `GET /api/podcasts/reportes/top-views`

```java
public List<PodcastDTO> reporteMasReproducido() {
    Set<Object> topIds = redisTemplate.opsForZSet().reverseRange(KEY_VIEWS, 0, 9);
    
    List<PodcastDTO> topPodcasts = new ArrayList<>();
    for (Object idObj : topIds) {
        PodcastDTO dto = obtenerPodcast(idObj.toString());
        if (dto != null) topPodcasts.add(dto);
    }
    return topPodcasts;
}
```

#### 7. Reporte: Invitados Más Frecuentes
**Endpoint:** `GET /api/podcasts/reportes/top-invitados`

```java
public Set<Object> reporteInvitadosMasFrecuentes() {
    return redisTemplate.opsForZSet().reverseRange(KEY_PARTICIPACIONES, 0, 9);
}
```

#### 8. Reporte: Locutores por País
**Endpoint:** `GET /api/podcasts/reportes/pais/{pais}`

```java
public Set<Object> reporteLocutoresPorPais(String pais) {
    return redisTemplate.opsForSet().members(KEY_PAIS_LOCUTOR + pais.toLowerCase());
}
```

#### 9. Reporte: Ordenar por Fecha
**Endpoint:** `GET /api/podcasts/reportes/ordenado-por-fecha?orden=desc`

```java
public List<PodcastDTO> listarPorFecha(String orden) {
    List<PodcastDTO> todos = listarTodos();
    
    if ("desc".equalsIgnoreCase(orden)) {
        todos.sort((a, b) -> {
            LocalDate fechaA = LocalDate.parse(a.getFecha());
            LocalDate fechaB = LocalDate.parse(b.getFecha());
            return fechaB.compareTo(fechaA);
        });
    } else {
        todos.sort((a, b) -> {
            LocalDate fechaA = LocalDate.parse(a.getFecha());
            LocalDate fechaB = LocalDate.parse(b.getFecha());
            return fechaA.compareTo(fechaB);
        });
    }
    return todos;
}
```

#### 10. Reporte: Ordenar por Reproducciones
**Endpoint:** `GET /api/podcasts/reportes/ordenado-por-reproducciones?orden=desc`

```java
public List<PodcastDTO> listarPorReproduciones(String orden) {
    List<PodcastDTO> todos = listarTodos();
    
    if ("desc".equalsIgnoreCase(orden)) {
        todos.sort((a, b) -> Integer.compare(b.getVistas(), a.getVistas()));
    } else {
        todos.sort((a, b) -> Integer.compare(a.getVistas(), b.getVistas()));
    }
    return todos;
}
```

---

## 7. APLICACIÓN INFORMÁTICA

### 7.1 Características Implementadas

#### Backend
- ✅ API RESTful completa (10 endpoints)
- ✅ Conexión nativa a Redis con RedisTemplate
- ✅ Manejo de índices secundarios (ZSET, SET)
- ✅ Limpieza de datos huérfanos al eliminar
- ✅ Carga automática de 100 podcasts de prueba
- ✅ CORS configurado para desarrollo
- ✅ Arquitectura en capas (Controller → Service → Redis)

#### Frontend
- ✅ Dashboard con estadísticas en tiempo real
- ✅ Listado con filtros múltiples (host, país, categoría)
- ✅ Ordenamiento dinámico (fecha, reproducciones)
- ✅ Hero card del podcast más reproducido
- ✅ Vista de detalle con reproductor de audio
- ✅ Formulario de creación con validaciones
- ✅ Registro automático de reproducciones al dar play
- ✅ Diseño responsive con Tailwind CSS
- ✅ Navegación SPA sin recargas

### 7.2 Capturas de Funcionalidad

**Dashboard Principal:**
- Estadísticas: Total podcasts, reproducciones, promedio
- Hero card: Podcast más reproducido con imagen
- Sidebar: Lista completa con filtros
- Reportes: Top invitados, locutores por país, ordenamientos

**Formulario de Creación:**
- Campos validados (título, email, audio URL)
- Selección de categoría y país
- Feedback visual al crear

**Vista de Detalle:**
- Reproductor de audio funcional
- Información completa del episodio
- Lista de invitados con avatares
- Registro automático de reproducción

### 7.3 Comandos de Ejecución

**Redis:**
```bash
docker run -d --name redis-podcast -p 6379:6379 \
  redis redis-server --requirepass 162003
```

**Backend:**
```bash
cd potcast-back
./gradlew bootRun
# Servidor en http://localhost:8080
```

**Frontend:**
```bash
cd Potcast
npm install
npm start
# Aplicación en http://localhost:4200
```

**Verificación:**
- Redis: `docker exec -it redis-podcast redis-cli` → `AUTH 162003` → `PING`
- Backend: http://localhost:8080/ → debe retornar "✅ Redis operativo"
- Frontend: http://localhost:4200/ → debe mostrar el dashboard

---

## 8. CONCLUSIONES

### 8.1 Logros Técnicos

1. **Implementación exitosa de Redis:** Se demostró que Redis es una solución eficiente para sistemas que requieren rankings dinámicos y consultas de alto rendimiento. Las operaciones sobre Sorted Sets (`ZSET`) permiten mantener top-N actualizados en O(log N), evitando costosos ordenamientos en cada consulta.

2. **Diseño de modelo de datos NoSQL híbrido:** Se diseñó un modelo que combina almacenamiento de documentos JSON (datos principales) con índices secundarios usando Sets y rankings usando ZSets, aprovechando las fortalezas de cada estructura de datos de Redis.

3. **Integración Spring Boot + Redis:** Spring Data Redis simplifica significativamente la conexión y operaciones con Redis, permitiendo trabajar con objetos Java sin escribir comandos Redis manualmente. La serialización JSON con Jackson es transparente.

4. **Aplicación full-stack funcional:** Se desarrolló una aplicación web completa con frontend moderno (Angular 20), backend robusto (Spring Boot 3) y base de datos NoSQL (Redis), demostrando la viabilidad de arquitecturas desacopladas.

5. **Reportes en tiempo real:** Los reportes solicitados (top reproducciones, invitados frecuentes, locutores por país) se implementaron eficientemente, con tiempos de respuesta sub-segundo incluso con 100+ podcasts en memoria.

### 8.2 Ventajas de Redis Identificadas

**Ventajas:**
- **Velocidad extrema:** Latencias < 1ms para todas las operaciones en memoria
- **Simplicidad de comandos:** ZADD, ZINCRBY, ZREVRANGE son intuitivos vs SQL complejo
- **Operaciones atómicas:** ZINCRBY es thread-safe sin necesidad de locks
- **Rankings automáticos:** ZSET mantiene el orden por score sin recalcular
- **Escalabilidad horizontal:** Redis Cluster permite particionar datos

**Limitaciones:**
- **Restricción de memoria:** Todo el dataset debe caber en RAM (costoso para datasets masivos)
- **Falta de consultas complejas:** No soporta JOINs ni agregaciones SQL avanzadas
- **Mantenimiento manual de índices:** Los índices secundarios deben actualizarse en el código
- **Persistencia limitada:** RDB/AOF agregan latencia; pérdida de datos en crashes sin persistencia

### 8.3 Lecciones Aprendidas

1. **Diseño de claves:** Una convención clara (`entidad:tipo:id`) es crucial para organización. Usar prefijos consistentes facilita debugging con `KEYS` o `SCAN`.

2. **Índices secundarios manuales:** A diferencia de SQL, los índices en Redis (Sets, ZSets) deben mantenerse manualmente. Es crítico decrementar contadores al eliminar para evitar datos huérfanos.

3. **Serialización:** Jackson facilita almacenar objetos complejos en Redis, pero aumenta tamaño. Para datasets grandes, considerar formatos binarios (Protocol Buffers, MessagePack).

4. **Trade-offs NoSQL:** Redis sacrifica flexibilidad de queries por velocidad. Para análisis complejos (ej: "promedio de reproducciones por categoría en Q1 2026"), combinar con SQL (arquitectura poliglota) es más apropiado.

5. **Testing con Redis:** La naturaleza en memoria facilita tests rápidos. Se puede usar un servidor Redis embebido para tests unitarios sin Docker.

### 8.4 Aplicabilidad Real

**Casos de uso ideales para Redis:**
- Leaderboards/rankings de videojuegos (similar a nuestro sistema)
- Sistemas de caché para APIs web
- Contadores de visitas en tiempo real (analytics)
- Sesiones de usuario (e-commerce)
- Rate limiting de APIs
- Colas de tareas con Redis Streams
- Chat en tiempo real (Pub/Sub)

**Limitaciones para este proyecto:**
- Si el catálogo crece a millones de podcasts, Redis en un solo servidor no es viable (limitación de RAM). Solución: Redis Cluster o arquitectura híbrida (PostgreSQL para datos + Redis para rankings/cache).
- Para análisis históricos complejos (ej: tendencias de categorías por trimestre), SQL sería más apropiado.

### 8.5 Mejoras Futuras

1. **Persistencia RDB/AOF:** Configurar snapshots periódicos para evitar pérdida de datos en fallos del servidor.

2. **Cache con TTL:** Implementar Time To Live en consultas de listados completos para reducir carga.

3. **RediSearch:** Integrar módulo RediSearch para búsquedas full-text en títulos y descripciones.

4. **Paginación:** Implementar paginación en listados para mejorar rendimiento con datasets grandes.

5. **Autenticación JWT:** Agregar autenticación de usuarios y roles (admin, locutor, oyente).

6. **Redis Cluster:** Migrar a cluster para sharding horizontal y alta disponibilidad en producción.

7. **Monitoring:** Implementar Redis Monitor y métricas con Spring Boot Actuator para optimización.

### 8.6 Conclusión Final

Redis demostró ser una excelente elección para este sistema de gestión de podcasts, cumpliendo todos los requisitos funcionales con excelente rendimiento. La combinación de estructuras de datos especializadas (ZSET para rankings, SET para índices) permitió implementar consultas complejas de forma eficiente y elegante.

El proyecto validó que Redis es ideal para aplicaciones que priorizan velocidad de lectura, rankings dinámicos y contadores en tiempo real. Sin embargo, para sistemas con consultas analíticas complejas o datasets que excedan la memoria disponible, una arquitectura poliglota (Redis + PostgreSQL/MongoDB) sería más apropiada.

La integración con Spring Boot y Angular fue fluida, demostrando que el ecosistema de herramientas modernas facilita el desarrollo de aplicaciones NoSQL profesionales con arquitecturas desacopladas y escalables.

---

## 9. REFERENCIAS BIBLIOGRÁFICAS

1. **Redis Documentation** (2026). Redis Labs. https://redis.io/docs/

2. Carlson, J. L. (2013). *Redis in Action*. Manning Publications.

3. **Spring Data Redis Reference** (2026). VMware. https://docs.spring.io/spring-data/redis/reference/

4. **Angular Documentation** (2026). Google LLC. https://angular.dev/

5. Sadalage, P. J., & Fowler, M. (2012). *NoSQL Distilled: A Brief Guide to the Emerging World of Polyglot Persistence*. Addison-Wesley Professional.

6. **Redis University** (2026). RU101: Introduction to Redis Data Structures. https://university.redis.com/

7. **Spring Boot Reference** (2026). VMware. Spring Boot 3.5.x Reference Documentation. https://docs.spring.io/spring-boot/


---

## ANEXOS

### A. Estructura del Proyecto

```
ProyectoFinalDB/
├── potcast-back/              # Backend Spring Boot
│   ├── src/main/java/
│   │   └── com/example/potcast_back/
│   │       ├── config/        # RedisConfig
│   │       ├── controller/    # PodcastController
│   │       ├── service/       # PodcastService
│   │       ├── model/         # PodCast, Locutor
│   │       ├── dtos/          # PodcastDTO
│   │       └── mappers/       # PodcastMapper
│   └── src/main/resources/
│       └── application.yaml
│
└── Potcast/                   # Frontend Angular
    ├── src/app/
    │   ├── models/            # podcast.model.ts
    │   ├── services/          # podcast.ts
    │   └── pages/
    │       ├── dashboard/
    │       ├── create-podcast/
    │       └── podcast-detail/
    └── package.json
```

### B. Endpoints API REST

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/` | Test de conexión a Redis |
| GET | `/api/podcasts` | Listar todos los podcasts |
| POST | `/api/podcasts` | Crear nuevo podcast |
| GET | `/api/podcasts/{id}` | Obtener podcast por ID |
| DELETE | `/api/podcasts/{id}` | Eliminar podcast |
| POST | `/api/podcasts/{id}/play` | Registrar reproducción |
| GET | `/api/podcasts/reportes/top-views` | Top 10 más reproducidos |
| GET | `/api/podcasts/reportes/top-invitados` | Top 10 invitados frecuentes |
| GET | `/api/podcasts/reportes/pais/{pais}` | Locutores por país |
| GET | `/api/podcasts/reportes/ordenado-por-fecha?orden=desc` | Ordenar por fecha |
| GET | `/api/podcasts/reportes/ordenado-por-reproducciones?orden=desc` | Ordenar por vistas |

### C. Comandos Redis Clave

```redis
# Strings (JSON)
SET podcast:data:podcast-1 '{...}'
GET podcast:data:podcast-1
DEL podcast:data:podcast-1
KEYS podcast:data:*

# Sorted Sets (Rankings)
ZADD ranking:reproducciones 0 podcast-1
ZINCRBY ranking:reproducciones 1 podcast-1
ZREVRANGE ranking:reproducciones 0 9 WITHSCORES
ZSCORE ranking:reproducciones podcast-1

# Sets (Índices)
SADD indice:pais:colombia "Juan Pérez"
SMEMBERS indice:pais:colombia
```

---

