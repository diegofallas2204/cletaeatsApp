# CletaEats - Aplicación Móvil Android

Aplicación móvil desarrollada nativamente para Android enfocada en la plataforma **CletaEats**. 
Este cliente permite realizar el registro, la autenticación y las órdenes de comida conectados a una API Backend propietaria de manera remota. 

## 🛠️ Tecnologías y Arquitectura

* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose (con soporte legado para vistas XML).
* **Consumo de API:** Retrofit2 junto con Gson Converter para serializar/deserializar JSON.
* **Componentes Android:** MVVM y Lifecycle ViewModel Compose.
* **Seguridad:** JSON Web Tokens (JWT) para mantener la sesión y autorización de rutas.
* **Compatibilidad de compilación:** Java 21 (SDK y JVM Target 21) - Orientado al API 35 (Android 15 aprox.).

## 📝 Notas de Versión e Instrucciones de Uso

Actualmente, **solo está implementada la interfaz de aplicación para el rol de CLIENTES**.

> **⚠️ IMPORTANTE - REGISTRO / LOGIN:** 
Para asegurar el correcto funcionamiento sin errores de roles cruzados, se recomienda registrarse e iniciar sesión utilizando el rol/cuenta de usuario proporcionado para clientes de la siguiente manera:
> * **Usuario:** cliente1
> * **Contraseña:** pass1234

## 💳 Proceso de Pago en Pedidos

Al realizar un pedido, se debe ingresar un método de pago. Debido a que las validaciones estrictas tipo "Luhn" no están conectadas temporalmente:
- Ingresa un **Número de tarjeta de 16 dígitos** exactos (Ej. `1234567812345678`).
- Ingresa un **CVV de 3 dígitos** exactos (Ej. `123`).
Sigue estrictamente el formato del formulario para evitar excepciones a nivel de UI. 

## 🛵 Visualización de Pedidos

Una vez efectuado exitosamente el pedido y completado el pago:
- Ve hasta **el final de la pantalla**, realiza "scroll" hacia abajo para poder visualizar tu listado de pedidos realizados.

## 🌐 Conexión Backend 

La aplicación no funciona de manera aislada (offline). Consume un ecosistema en la nube con las siguientes características:
* **Host de la API:** Railway (Infraestructura como servicio).
* **Motor de Base de datos:** MySQL.
* **Autorización:** Todo llamado al sistema usa **JWT** generado desde la base en Railway.

## 🚀 Cómo Empezar

1. Clona el repositorio.
2. Abre la carpeta `CEapp` en **Android Studio**.
3. Deja sincronizar el proyecto en `Gradle` (*Nota: Requieres Java 21 Toolchain instalado*).
4. Ejecuta en cualquier dispositivo físico o emulador con **API Mínima 24** (Android 7).
