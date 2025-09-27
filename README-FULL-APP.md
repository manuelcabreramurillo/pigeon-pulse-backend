# Pigeon Pulse Backend

Backend completo para la aplicación Pigeon Pulse, sistema de gestión de palomas mensajeras.

## Arquitectura

- **Framework**: Spring Boot 3.x con Java 21
- **Base de Datos**: Firebase Firestore (NoSQL)
- **Autenticación**: Google OAuth 2.0 con JWT tokens
- **Documentación**: OpenAPI/Swagger
- **Patrón**: Arquitectura REST con capas (Controller, Service, Repository)

## Características Principales

### ✅ Autenticación con Google OAuth
- Login con Google
- JWT tokens para sesiones
- Protección de endpoints

### ✅ Gestión de Palomas (CRUD)
- Crear, leer, actualizar, eliminar palomas
- Filtros por estado, sexo, línea, búsqueda
- Validación de datos

### ✅ Genealogía
- Árbol genealógico ascendente (ancestros)
- Descendencia (hijos)
- Relaciones padre-madre auto-referenciadas

### ✅ Reportes y Censos
- Estadísticas generales
- Exportación a PDF
- Exportación a Excel
- Filtros personalizados

### ✅ Documentación OpenAPI
- Swagger UI disponible en `/swagger-ui.html`
- Documentación completa de endpoints

## Estructura del Proyecto

```
src/main/java/com/pigeonpulse/
├── config/
│   ├── FirebaseConfig.java          # Configuración Firebase
│   └── SecurityConfig.java          # Configuración Spring Security
├── controller/
│   ├── AuthController.java          # Endpoints de autenticación
│   ├── PalomaController.java        # CRUD de palomas
│   └── ReportesController.java      # Reportes y censos
├── dto/
│   ├── AuthResponse.java            # Respuesta de login
│   ├── UsuarioDTO.java              # DTO de usuario
│   └── PalomaDTO.java               # DTO de paloma
├── model/
│   ├── Usuario.java                 # Entidad Usuario
│   └── Paloma.java                  # Entidad Paloma
├── repository/
│   ├── FirebaseRepository.java      # Repositorio base Firestore
│   ├── UsuarioRepository.java       # Repositorio Usuario
│   └── PalomaRepository.java        # Repositorio Paloma
├── service/
│   ├── UsuarioService.java          # Lógica Usuario
│   └── PalomaService.java           # Lógica Paloma
└── security/
    └── JwtUtil.java                 # Utilidades JWT
```

## Modelos de Datos

### Usuario
```java
public class Usuario {
    @DocumentId
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private String domicilio;
    private String googleId;
    private LocalDateTime fechaCreacion;
}
```

### Paloma
```java
public class Paloma {
    @DocumentId
    private String id;
    private String anillo;      // Único, obligatorio
    private Integer año;        // Obligatorio
    private String sexo;        // Macho/Hembra/Indefinido
    private String color;
    private String linea;
    private String estado;      // Activa/Reproductora/Otra
    private String padre;       // Anillo del padre
    private String madre;       // Anillo de la madre
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private String peso;
    private String altura;
    private String usuarioId;   // Propietario
}
```

## Endpoints API

### Autenticación
```
POST /api/auth/login          # Login con Google token
GET  /api/auth/me             # Usuario actual
POST /api/auth/logout         # Logout
```

### Palomas
```
GET    /api/palomas              # Listar con filtros
POST   /api/palomas              # Crear paloma
GET    /api/palomas/{id}         # Obtener por ID
PUT    /api/palomas/{id}         # Actualizar
DELETE /api/palomas/{id}         # Eliminar
GET    /api/palomas/{id}/ancestros    # Ancestros
GET    /api/palomas/{id}/descendientes # Descendientes
```

### Reportes
```
GET    /api/reportes/estadisticas    # Estadísticas
POST   /api/reportes/censo/pdf       # Censo PDF
POST   /api/reportes/censo/excel     # Censo Excel
```

## Configuración

### Firebase
1. Crear proyecto en Firebase Console
2. Habilitar Firestore Database
3. Crear service account y descargar JSON
4. Colocar `firebase-service-account.json` en `src/main/resources/`

### Google OAuth
1. Configurar OAuth 2.0 en Google Cloud Console
2. Agregar credenciales al frontend
3. El backend valida tokens automáticamente

### Variables de Entorno
```properties
# application.properties
server.port=8080
firebase.config.path=classpath:firebase-service-account.json
jwt.secret=your-secret-key
jwt.expiration=86400000
cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

## Tecnologías Utilizadas

- **Spring Boot 3.2.0**: Framework principal
- **Java 21**: Lenguaje con records y expresiones lambda
- **Firebase Admin SDK**: Conexión a Firestore
- **Spring Security**: Autenticación y autorización
- **JWT**: Tokens de sesión
- **OpenAPI/Swagger**: Documentación
- **iText**: Generación PDF
- **Apache POI**: Generación Excel
- **Maven**: Gestión de dependencias

## Buenas Prácticas Implementadas

- ✅ Records para DTOs
- ✅ Lambdas y Optional
- ✅ Validación con Bean Validation
- ✅ Manejo global de errores
- ✅ CORS configurado
- ✅ Logging estructurado
- ✅ Documentación completa
- ✅ Arquitectura en capas
- ✅ Inyección de dependencias
- ✅ Principios SOLID

## Ejecución

### Prerrequisitos
- Java 21+
- Maven 3.6+
- Firebase project configurado

### Comandos
```bash
# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run

# Acceder
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

## Integración con Frontend

El backend está diseñado para trabajar con el frontend React existente. Los endpoints están protegidos con JWT y requieren autenticación para operaciones de datos.

### Ejemplo de Consumo
```javascript
// Login con Google
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ token: googleToken })
});

// Usar JWT en requests
const palomas = await fetch('/api/palomas', {
  headers: { 'Authorization': `Bearer ${jwtToken}` }
});
```

## Próximas Mejoras (V2)

- [ ] Caché con Redis
- [ ] Tests unitarios e integración
- [ ] Notificaciones push
- [ ] API de imágenes para palomas
- [ ] Métricas y monitoreo
- [ ] Contenedores Docker
- [ ] CI/CD pipeline

## Contribución

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agrega nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver archivo `LICENSE` para más detalles.