# CletaEats API Documentation

This document lists the available RESTful endpoints for testing with tools like Postman or Insomnia.

> [!NOTE]
> All endpoints under `/api/admin` and some under `/api/cliente` or `/api/repartidor` require an `Authorization: Bearer <token>` header.

---

## 🔐 Authentication & Users
Endpoints for login and registration.

### Login
*   **URL:** `POST /api/usuarios/login`
*   **Body:**
    ```json
    {
      "username": "admin",
      "password": "123"
    }
    ```

### Register
*   **URL:** `POST /api/usuarios/registrar`
*   **Body:**
    ```json
    {
      "username": "nuevo_usuario",
      "password": "password123",
      "rol": "cliente" 
    }
    ```
    *(Roles: `cliente`, `repartidor`, `admin`)*

---

## 🛠️ Admin Dashboard
Manage entities and view reports. Requires Admin Token.

### Dashboard Stats
*   **URL:** `GET /api/admin/dashboard`

### Clients (Clientes)
*   **List all:** `GET /api/admin/clientes`
*   **Create:** `POST /api/admin/clientes`
    ```json
    {
      "nombre": "Juan Perez",
      "email": "juan@mail.com",
      "telefono": "8888-8888",
      "cedula": "123456789",
      "direccion": "Heredia Centro",
      "tarjeta": "1234-5678-9012",
      "estado": "activo"
    }
    ```
*   **Update:** `PUT /api/admin/clientes`
    ```json
    {
      "id": 1,
      "nombre": "Juan Perez Modificado",
      "estado": "suspendido"
    }
    ```
*   **Delete:** `DELETE /api/admin/clientes/{id}`

### Restaurants (Restaurantes)
*   **List all:** `GET /api/restaurantes`
*   **Create:** `POST /api/restaurantes`
    ```json
    {
      "nombre": "Pizzeria Italiana",
      "cedulaJuridica": "3-101-123456",
      "tipoComida": "Italiana",
      "direccion": "San Francisco, Heredia"
    }
    ```
*   **Update:** `PUT /api/admin/restaurantes`
    ```json
    {
      "id": 1,
      "nombre": "Pizzeria Italiana Actualizada"
    }
    ```
*   **Delete:** `DELETE /api/admin/restaurantes/{id}`

### Delivery Persons (Repartidores)
*   **List all:** `GET /api/admin/repartidores`
*   **Create:** `POST /api/admin/repartidores`
    ```json
    {
      "nombre": "Pedro Repartidor",
      "email": "pedro@mail.com",
      "cedula": "987654321",
      "direccion": "Mercedes Norte",
      "tarjeta": "4321-8765-1092",
      "estado": "disponible"
    }
    ```
*   **Delete:** `DELETE /api/admin/repartidores/{id}`

### Combos / Products
*   **List all:** `GET /api/admin/combos`
*   **List by Restaurant:** `GET /api/admin/combos/{restauranteId}`
*   **Create:** `POST /api/admin/combos`
    ```json
    {
      "restauranteId": 1,
      "numeroCombo": 1,
      "nombre": "Combo Familiar",
      "precio": 8500
    }
    ```
*   **Delete:** `DELETE /api/admin/combos/{id}`

---

## 🛵 Delivery App (Repartidor)
Endpoints used by the mobile app for delivery persons.

### Assigned Orders
*   **URL:** `GET /api/repartidor/pedidos`
*   *(Requires Repartidor Token)*

### Update Order Status
*   **URL:** `PUT /api/repartidor/pedidos/{pedidoId}/estado`
*   **Body:**
    ```json
    {
      "estado": "entregado"
    }
    ```
    *(States: `camino`, `entregado`)*

---

## 👤 Client App (Cliente)
Endpoints used by the mobile app for customers.

### Create Order
*   **URL:** `POST /api/cliente/pedidos`
*   **Body:**
    ```json
    {
      "restauranteId": 1,
      "items": [
        { "comboId": 1, "cantidad": 2, "notas": "Sin cebolla" }
      ]
    }
    ```

### Order History
*   **URL:** `GET /api/cliente/pedidos/historial`
