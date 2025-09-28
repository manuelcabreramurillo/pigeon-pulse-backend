# 🚀 Desarrollo Local - Pigeon Pulse Backend

Guía para ejecutar el backend localmente con configuración de variables de entorno.

## 📋 Prerrequisitos

- **Java 21** instalado
- **Maven 3.6+** instalado
- **Proyecto Firebase** configurado
- **Cuenta Google Cloud** con service account

## 🔧 Configuración Inicial

### 1. Clonar y configurar el proyecto

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/pigeon-pulse.git
cd pigeon-pulse/backend

# Copiar archivo de variables de entorno
cp .env.example .env
```

### 2. Configurar Firebase

#### Crear Service Account:
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Selecciona tu proyecto Firebase
3. Ve a "IAM & Admin" > "Service Accounts"
4. Crea nueva service account con rol "Firebase Admin SDK Administrator"
5. Genera nueva key JSON
6. **Guarda el archivo JSON de forma segura**

#### Configurar variables en `.env`:
```bash
# Firebase Configuration
FIREBASE_PROJECT_ID=tu-firebase-project-id
FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account","project_id":"tu-project-id",...}
```

### 3. Configurar JWT Secret

```bash
# JWT Configuration
JWT_SECRET=tu_clave_jwt_muy_larga_para_desarrollo_123456789
```

## ▶️ Ejecutar la aplicación

### Opción 1: Con Maven (Recomendado)

```bash
# Compilar y ejecutar
mvn spring-boot:run
```

### Opción 2: Con Maven Wrapper

```bash
# En Linux/Mac
./mvnw spring-boot:run

# En Windows
mvnw.cmd spring-boot:run
```

### Opción 3: Compilar y ejecutar JAR

```bash
# Compilar
mvn clean package -DskipTests

# Ejecutar
java -jar target/pigeon-pulse-backend-0.0.1-SNAPSHOT.jar
```

## 🔍 Verificar que funciona

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 2. API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### 3. H2 Console (solo desarrollo)
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:mem:pigeondb
- **Username**: sa
- **Password**: (vacío)

## 🐛 Solución de Problemas

### Error: "Firebase service account not found"
- ✅ Verifica que el archivo `.env` existe
- ✅ Confirma que `FIREBASE_SERVICE_ACCOUNT_JSON` contiene un JSON válido
- ✅ Asegúrate de que no hay comillas extra alrededor del JSON

### Error: "Port already in use"
```bash
# Cambiar puerto en .env
PORT=8081
```

### Error: "JWT token invalid"
- ✅ Verifica que `JWT_SECRET` sea una cadena larga y segura
- ✅ Confirma que no contiene caracteres especiales problemáticos

### Error: "CORS policy"
- ✅ Verifica que `CORS_ALLOWED_ORIGINS` incluya tu URL de frontend
- ✅ Para desarrollo: `http://localhost:5173,http://localhost:3000`

## 🔄 Desarrollo con Frontend

### Ejecutar ambos proyectos:

```bash
# Terminal 1 - Backend
cd backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend
npm install
npm run dev
```

### URLs de desarrollo:
- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:8080
- **API Base**: http://localhost:8080/api

## 📝 Variables de Entorno

### Archivo `.env` completo:

```bash
# Server Configuration
PORT=8080

# Firebase Configuration
FIREBASE_PROJECT_ID=tu-firebase-project-id
FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account",...}

# JWT Configuration
JWT_SECRET=tu_jwt_secret_muy_largo
JWT_EXPIRATION=86400000

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# Logging
LOG_LEVEL=DEBUG
SPRING_SECURITY_LOG_LEVEL=DEBUG
```

## 🚀 Deploy a Producción

Cuando estés listo para producción:

1. ✅ Configura las variables en Render
2. ✅ Usa `FIREBASE_SERVICE_ACCOUNT_JSON` con el JSON completo
3. ✅ Configura `CORS_ALLOWED_ORIGINS` con tu dominio de frontend
4. ✅ Cambia `LOG_LEVEL` a `INFO` o `WARN`

## 📞 Soporte

Si tienes problemas:
1. Revisa los logs de la aplicación
2. Verifica la configuración de Firebase
3. Confirma que todas las variables de entorno están configuradas
4. Revisa la documentación de [Spring Boot](https://spring.io/projects/spring-boot) y [Firebase Admin](https://firebase.google.com/docs/admin/setup)