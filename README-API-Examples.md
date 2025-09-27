# Pigeon Pulse Backend - API Examples

## Arquitectura Multi-Usuario con Palomares (Workspaces)

El backend implementa una arquitectura multi-usuario donde cada usuario puede tener múltiples "palomares" (workspaces) para organizar sus colecciones de palomas. Cada palomar es independiente y tiene sus propias palomas, usuarios colaboradores y permisos.

### Conceptos Clave
- **Usuario**: Persona autenticada con Google OAuth
- **Palomar**: Workspace que contiene una colección de palomas
- **UsuarioPalomar**: Relación entre usuario y palomar con roles (PROPIETARIO, COLABORADOR)
- **JWT Context**: El token JWT incluye `palomarId` y `rol` para contexto de seguridad

## Authentication Flow

### 1. Google OAuth Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "token": "firebase_id_token_here"
}
```

**Flujo Interno:**
1. Verifica token de Firebase
2. Crea/actualiza usuario en Firestore
3. Crea palomar por defecto si no existe
4. Asigna rol PROPIETARIO al usuario
5. Genera JWT con contexto de palomar

**Response:**
```json
{
  "token": "jwt_token_with_palomar_context",
  "usuario": {
    "id": "user_id",
    "nombre": "User Name",
    "email": "user@example.com",
    "telefono": null,
    "domicilio": null
  }
}
```

### 2. JWT Token Structure
```json
{
  "sub": "user_id",
  "palomarId": "palomar_id",
  "rol": "PROPIETARIO",
  "email": "user@example.com",
  "iat": 1640000000,
  "exp": 1640003600
}
```

### 3. Get Current User
```bash
GET /api/auth/me
Authorization: Bearer jwt_token_here
```

### 4. Logout
```bash
POST /api/auth/logout
Authorization: Bearer jwt_token_here
```

## Workspace Management (Palomares)

### List User's Workspaces
```bash
GET /api/palomares
Authorization: Bearer jwt_token_here
```

**Response:**
```json
[
  {
    "id": "palomar_1",
    "nombre": "Mi Palomar Principal",
    "descripcion": "Colección principal de palomas",
    "propietarioId": "user_1",
    "fechaCreacion": "2024-01-15T10:30:00Z"
  }
]
```

### Create New Workspace
```bash
POST /api/palomares
Authorization: Bearer jwt_token_here
Content-Type: application/json

{
  "nombre": "Nuevo Palomar",
  "descripcion": "Colección de palomas de competición"
}
```

### Update Workspace
```bash
PUT /api/palomares/{palomarId}
Authorization: Bearer jwt_token_here
Content-Type: application/json

{
  "nombre": "Palomar Actualizado",
  "descripcion": "Descripción actualizada"
}
```

### Delete Workspace
```bash
DELETE /api/palomares/{palomarId}
Authorization: Bearer jwt_token_here
```

## Palomas Management

### List Palomas (Filtered by Current Workspace)
```bash
GET /api/palomas?estado=Activa en carrera&sexo=Macho&linea=Janssen&search=ESP-2024
Authorization: Bearer jwt_token_here
```

**Nota:** Solo devuelve palomas del workspace actual (extraído del JWT)

### Create Paloma
```bash
POST /api/palomas
Authorization: Bearer jwt_token_here
Content-Type: application/json

{
  "anillo": "ESP-2024-001",
  "año": 2024,
  "sexo": "Macho",
  "color": "Azul Pizarra",
  "linea": "Janssen",
  "estado": "Activa en carrera",
  "padre": "ESP-2022-005",
  "madre": "ESP-2022-012",
  "observaciones": "Paloma de excelente calidad"
}
```

### Update Paloma
```bash
PUT /api/palomas/{id}
Authorization: Bearer jwt_token_here
Content-Type: application/json

{
  "anillo": "ESP-2024-001",
  "año": 2024,
  "sexo": "Macho",
  "color": "Azul Pizarra",
  "linea": "Janssen",
  "estado": "Reproductora",
  "padre": "ESP-2022-005",
  "madre": "ESP-2022-012",
  "observaciones": "Actualizada"
}
```

### Delete Paloma
```bash
DELETE /api/palomas/{id}
Authorization: Bearer jwt_token_here
```

### Genealogy
```bash
GET /api/palomas/{id}/ancestros
GET /api/palomas/{id}/descendientes
Authorization: Bearer jwt_token_here
```

## Reports

### Get Statistics (Current Workspace)
```bash
GET /api/reportes/estadisticas
Authorization: Bearer jwt_token_here
```

### Export Census PDF
```bash
POST /api/reportes/censo/pdf
Authorization: Bearer jwt_token_here
Content-Type: application/json

{
  "nombrePropietario": "Juan Pérez",
  "telefono": "123-456-789",
  "domicilio": "Calle Principal 123",
  "filtros": {
    "año": 2024,
    "linea": "Janssen",
    "estado": "Activa en carrera"
  }
}
```

### Export Census Excel
```bash
POST /api/reportes/censo/excel
Authorization: Bearer jwt_token_here
Content-Type: application/json

{
  "nombrePropietario": "Juan Pérez",
  "telefono": "123-456-789",
  "domicilio": "Calle Principal 123",
  "filtros": {
    "año": 2024,
    "linea": "Janssen",
    "estado": "Activa en carrera"
  }
}
```

## Frontend Integration

### React Context for Workspace Management
```typescript
import React, { createContext, useContext, useState, useEffect } from 'react';

interface Palomar {
  id: string;
  nombre: string;
  descripcion: string;
  propietarioId: string;
  fechaCreacion: string;
}

interface AuthContextType {
  user: any;
  palomar: Palomar | null;
  palomares: Palomar[];
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
  switchPalomar: (palomarId: string) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState(null);
  const [palomar, setPalomar] = useState<Palomar | null>(null);
  const [palomares, setPalomares] = useState<Palomar[]>([]);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

  const login = async (firebaseToken: string) => {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token: firebaseToken }),
    });

    const data = await response.json();
    setToken(data.token);
    setUser(data.usuario);
    localStorage.setItem('token', data.token);

    // Load user's workspaces
    await loadPalomares(data.token);
  };

  const loadPalomares = async (authToken: string) => {
    const response = await fetch('/api/palomares', {
      headers: { 'Authorization': `Bearer ${authToken}` },
    });

    const palomaresData = await response.json();
    setPalomares(palomaresData);

    // Set first palomar as current if none selected
    if (palomaresData.length > 0 && !palomar) {
      setPalomar(palomaresData[0]);
    }
  };

  const switchPalomar = (palomarId: string) => {
    const selectedPalomar = palomares.find(p => p.id === palomarId);
    if (selectedPalomar) {
      setPalomar(selectedPalomar);
      // In a real app, you might want to get a new token with the selected palomar context
    }
  };

  const logout = () => {
    setUser(null);
    setPalomar(null);
    setPalomares([]);
    setToken(null);
    localStorage.removeItem('token');
  };

  return (
    <AuthContext.Provider value={{
      user,
      palomar,
      palomares,
      token,
      login,
      logout,
      switchPalomar
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
```

### API Hook with Workspace Context
```typescript
import { useAuth } from './AuthContext';

const useApi = () => {
  const { token, palomar } = useAuth();

  const apiCall = async (endpoint: string, options: RequestInit = {}) => {
    if (!token) {
      throw new Error('No authentication token');
    }

    const headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers,
    };

    const response = await fetch(`/api${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      // Token expired, redirect to login
      localStorage.removeItem('token');
      window.location.href = '/login';
      throw new Error('Authentication expired');
    }

    if (response.status === 403) {
      throw new Error('Access denied to this workspace');
    }

    return response.json();
  };

  return { apiCall, palomar };
};
```

### Workspace Selector Component
```typescript
import React from 'react';
import { useAuth } from './AuthContext';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

const WorkspaceSelector: React.FC = () => {
  const { palomar, palomares, switchPalomar } = useAuth();

  return (
    <div className="flex items-center space-x-2">
      <span className="text-sm font-medium">Workspace:</span>
      <Select value={palomar?.id} onValueChange={switchPalomar}>
        <SelectTrigger className="w-48">
          <SelectValue placeholder="Select workspace" />
        </SelectTrigger>
        <SelectContent>
          {palomares.map((p) => (
            <SelectItem key={p.id} value={p.id}>
              {p.nombre}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
};

export default WorkspaceSelector;
```

## Security Model

### Roles and Permissions
- **PROPIETARIO**: Control total sobre el palomar (crear, editar, eliminar, gestionar usuarios)
- **COLABORADOR**: Puede ver y editar palomas, generar reportes

### Authorization Checks
```java
// In controllers
PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

if (!palomarContext.puedeEditarPalomas()) {
    return ResponseEntity.forbidden().build();
}

if (!palomarContext.puedeGestionarUsuarios()) {
    return ResponseEntity.forbidden().build();
}
```

## Database Schema (Firestore)

### Collections
- `usuarios`: User profiles
- `palomares`: Workspaces
- `usuarioPalomar`: User-workspace relationships
- `palomas`: Pigeons (filtered by palomarId)

### Indexes Required
- `palomas.palomarId` (for filtering by workspace)
- `usuarioPalomar.usuarioId` (for finding user's workspaces)
- `usuarioPalomar.palomarId` (for finding workspace users)

## Deployment

### Environment Variables
```bash
# Firebase
FIREBASE_CONFIG_PATH=classpath:firebase-service-account.json

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://yourdomain.com
```

### Firebase Service Account
Place `firebase-service-account.json` in `src/main/resources/`

### Running the Application
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`
Swagger UI at `http://localhost:8080/swagger-ui.html`