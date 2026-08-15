# GeoSismo Supervisor (Android)

App nativa Android para **ingenieros expertos que validan el trabajo de
campo** dentro de GeoSismo UD. Es una app separada de "GeoSismo Captura"
(la de ciudadanos e ingenieros de campo), pero usa exactamente el mismo
backend PHP/MySQL — ningún dato se duplica.

## Fase 1 (esta entrega): Aprobar / rechazar voluntarios

Hoy en día, cuando alguien se postula como ingeniero voluntario desde la
app de campo o la web, su solicitud queda en estado `pendiente` y **no
existía ninguna forma de aprobarla salvo entrando a phpMyAdmin a mano**.
Esta app resuelve exactamente eso:

- Login exclusivo para cuentas con `rol = admin`.
- Lista de todas las postulaciones pendientes (nombre, correo, profesión,
  tarjeta profesional, teléfono, fecha).
- Botones **Aprobar** / **Rechazar** con diálogo de confirmación antes de
  ejecutar (para que no sea un toque accidental).
- Insignia con el número de pendientes visible en el inicio.

En cuanto apruebas a alguien aquí, esa persona puede entrar de inmediato
al Panel de Ingeniero en la app de campo — es la misma tabla de usuarios.

## Cómo se conecta con el resto del sistema

```
                    ┌─────────────────────┐
                    │   Backend PHP/MySQL  │   (uno solo, sin cambios
                    │   (tu hosting)        │    de estructura de datos)
                    └──────────┬───────────┘
             ┌──────────────────┼──────────────────┐
             │                  │                   │
     ┌───────▼────────┐ ┌───────▼────────┐ ┌────────▼─────────┐
     │  Web GeoSismo    │ │ GeoSismo Captura│ │ GeoSismo Supervisor│
     │  UD (navegador)  │ │ (Android, campo) │ │  (Android, admin)  │
     └──────────────────┘ └──────────────────┘ └─────────────────────┘
      ciudadanos,          ciudadanos,           SOLO rol admin
      ingenieros, admin     ingenieros voluntarios
```

## Backend: 1 archivo nuevo, cero cambios a lo existente

Se agregó `api/admin.php` a tu proyecto PHP ya existente. Solo necesitas
subir ese archivo a tu servidor (misma carpeta `api/` donde ya están
`auth.php`, `reportes.php`, etc.). No requiere tocar `config/database.php`
ni el esquema de la base de datos — usa las tablas `sismo_usuarios` que
ya tienes.

Endpoints que agrega:

| Endpoint | Acción | Quién puede usarlo |
|---|---|---|
| `api/admin.php?accion=voluntarios_pendientes` | Lista postulaciones sin resolver | Solo `admin` |
| `api/admin.php` (POST) `accion=resolver_voluntario` | Aprueba o rechaza (`usuario_id`, `decision`) | Solo `admin` |
| `api/admin.php?accion=conteo_pendientes` | Número de pendientes (para el badge) | Solo `admin` |

Ya lo probé de punta a punta contra una base de datos real: rechaza sin
sesión (401), rechaza con rol equivocado (403), aprueba correctamente, y
bloquea que se resuelva dos veces la misma postulación (409).

## Cómo compilarlo (sin instalar nada) — GitHub Actions

Exactamente el mismo procedimiento que ya usaste con GeoSismo Captura:

1. Crea un **repositorio nuevo y separado** en GitHub (ej. `geosismo-supervisor`)
   — te recomiendo uno distinto al de la app de campo, para no mezclarlas.
2. Descomprime este `.zip` y arrastra **todo el contenido** de la carpeta
   `GeoSismoSupervisor` a la pantalla "Upload files" de ese repositorio
   (Add file → Upload files). Recuerda: si usas Mac, revela los archivos
   ocultos (Cmd+Shift+.) para que la carpeta `.github` se incluya.
3. Commit changes.
4. Ve a la pestaña **Actions** — la compilación empieza sola. Cuando
   termine en verde ✓, baja el `.apk` desde "Artifacts".
5. Antes de instalar, entra a la pantalla de login y escribe la URL de tu
   servidor (la misma que usa GeoSismo Captura).

## Usuario de prueba

El usuario admin que ya viene en tu base de datos de ejemplo:
- **Correo:** `admin@geosismo.ud`
- **Contraseña:** `password123`

Si intentas entrar con una cuenta que no sea `admin` (por ejemplo un
ciudadano o un ingeniero de campo), la app te lo dice explícitamente y no
te deja pasar — es intencional, esta app es solo para supervisión.

## Qué falta (Fases 2 y 3, para cuando las necesites)

- Auditoría de evaluaciones técnicas (certificar/objetar el trabajo de
  los ingenieros de campo).
- Gestión de usuarios (cambiar roles, revocar acceso).
- Moderación de reportes (cerrar, reabrir, eliminar duplicados/falsos).
- Exportar datos a CSV.
- Centro de datos ampliado con filtros.

La pantalla de inicio ya tiene una tarjeta "🔒 Próximamente" con estas
funciones, lista para activarse cuando construyamos cada fase.

## Estructura del proyecto

```
GeoSismoSupervisor/
├── app/src/main/java/co/edu/udistrital/geosismo/supervisor/
│   ├── LoginActivity.kt / HomeActivity.kt / SettingsActivity.kt
│   ├── VoluntariosPendientesActivity.kt   → pantalla principal de esta fase
│   ├── data/SessionManager.kt              → sesión persistida (rol admin)
│   ├── network/                             → Retrofit + cookie jar (mismo patrón que Captura)
│   ├── repository/AdminRepository.kt        → login + acciones de administración
│   └── ui/VoluntarioPendienteAdapter.kt     → lista de postulaciones
└── app/src/main/res/                        → mismo sistema de diseño, acento azul
```
