# CletaEats — Aplicación Móvil Android
**Amanda Zamora y Diego Fallas**

Aplicación móvil nativa para Android que implementa una plataforma de delivery de comida universitaria. Soporta dos roles de usuario (Cliente y Repartidor), funciona en modo **offline-first** con SQLite local y sincroniza automáticamente con una API REST al recuperar conexión.

---

## 🛠️ Tecnologías y Arquitectura

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Estado | Compose State hoisted en Composable raíz |
| Red | Retrofit 2 + OkHttp + Gson |
| Base de datos local | SQLiteOpenHelper (tablas: `pedidos`, `pending_actions`, `restaurantes`, `combos`, `tarjetas`) |
| Almacenamiento nube | `CloudPedidoStorage` — in-memory simulado |
| Sincronización | `SyncManager` (singleton) + `SyncWorker` (WorkManager, 15 min) |
| Mapas | OSMdroid — OpenStreetMap |
| Auth | JWT — `TokenManager` + `SessionManager` (SharedPreferences) |
| Backend | Spring Boot en Railway — `https://cletaeatsbe-production.up.railway.app/` |
| Compatibilidad | Java 21 / SDK target API 35 (Android 15) / mínimo API 24 (Android 7) |

---

## 🚀 Cómo Empezar

1. Clonar el repositorio.
2. Abrir la carpeta `CEapp` en **Android Studio Hedgehog** o superior.
3. Dejar que Gradle sincronice el proyecto *(requiere Java 21 Toolchain)*.
4. Ejecutar en cualquier dispositivo físico o emulador con **API mínima 24**.

---

## 👤 Roles de Usuario

### Cliente
- Explorar restaurantes, filtrar por categoría y buscar por nombre
- Agregar combos al carrito y realizar pedidos (efectivo o tarjeta)
- Ver historial de pedidos con filtros: **Activos / Entregados / Cancelados**
- Cancelar pedidos con reflejo inmediato en la UI
- Seguimiento del estado del pedido en tiempo real
- Valorar pedidos entregados (1–5 estrellas)

### Repartidor
- Ver pedidos disponibles con actualización automática cada 5 segundos
- Aceptar pedidos, confirmar retiro y confirmar entrega desde el mapa
- Historial con filtros: **Activos / Entregados / Cancelados**
- Si el cliente cancela un pedido activo, el repartidor ve una tarjeta de cancelación en el mapa y el pedido pasa al filtro Cancelados automáticamente

---

## 💾 Sistema de Almacenamiento (Panel de Control — Cliente)

Accesible desde el ícono ☁ en la barra superior del cliente. Permite cambiar el modo de almacenamiento para pruebas:

| Toggle | Comportamiento |
|--------|---------------|
| **Modo API** (predeterminado) | Pedidos van directo a la API REST en Railway |
| **Modo Local / Offline** | Pedidos se guardan en SQLite; se sincronizan al reconectar |
| **Nube Forzada** | Pedidos van a memoria simulada (se pierden al cerrar la app) |
| **Expansión de Disco** | Local hasta el umbral (5/10/20 pedidos), luego desborda a nube |

> **Nota:** La nube es simulada en memoria (`CloudPedidoStorage`). Los pedidos guardados en nube desaparecen al reiniciar la app — comportamiento intencional para demostrar el concepto de desbordamiento.

---

## 🔄 Sincronización Offline

- Las acciones pendientes se encolan en SQLite (`pending_actions`) y se procesan en orden **FIFO** al reconectar.
- Acciones soportadas: `CREATE_ORDER`, `CANCEL_ORDER`, `UPDATE_ORDER_STATUS`, `ASSIGN_ORDER`, `SAVE_CARD`, `DELETE_CARD`.
- Si una acción falla, la cola se detiene para mantener consistencia.
- La sincronización se activa automáticamente al detectar conexión (además del ciclo de WorkManager cada 15 min).

---

## 💳 Proceso de Pago

Al realizar un pedido se solicita un método de pago:
- **Número de tarjeta:** exactamente 16 dígitos (ej. `1234567812345678`)
- **CVV:** exactamente 3 dígitos (ej. `123`)

---

## 📄 Documentación adicional

- `README_CletaEats.docx` — Manual de usuario completo y guía de pruebas funcionales (15 casos)
- `extras/api_documentation.md` — Documentación de endpoints del backend
- `extras/cletaeats_db_railway.sql` — Script de la base de datos en Railway
