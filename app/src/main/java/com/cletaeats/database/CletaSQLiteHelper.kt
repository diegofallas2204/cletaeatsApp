package com.cletaeats.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.cletaeats.network.RestauranteItem
import com.cletaeats.network.PedidoItem
import com.cletaeats.network.MetodoPago

class CletaSQLiteHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        "cletaeats.db",
        null,
        2
    ) {

    companion object {
        private const val TABLE_RESTAURANTES = "restaurantes"
        private const val TABLE_PEDIDOS = "pedidos"
        private const val TABLE_TARJETAS = "tarjetas"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_RESTAURANTES (
                id INTEGER PRIMARY KEY,
                nombre TEXT,
                cedula_juridica TEXT,
                direccion TEXT,
                tipo_comida TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_PEDIDOS (
                id INTEGER PRIMARY KEY,
                restauranteNombre TEXT,
                total REAL,
                estado TEXT,
                fechaPedido TEXT,
                notas TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $TABLE_TARJETAS (
                id INTEGER PRIMARY KEY,
                clienteId INTEGER,
                numeroTarjeta TEXT,
                fechaVencimiento TEXT,
                cvv TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESTAURANTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PEDIDOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TARJETAS")
        onCreate(db)
    }

    fun guardarRestaurantes(lista: List<RestauranteItem>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_RESTAURANTES, null, null)
            lista.forEach { rest ->
                val values = ContentValues().apply {
                    put("id", rest.id)
                    put("nombre", rest.nombre)
                    put("cedula_juridica", rest.cedulaJuridica)
                    put("direccion", rest.direccion)
                    put("tipo_comida", rest.tipoComida)
                }
                db.insert(TABLE_RESTAURANTES, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerRestaurantes(): List<RestauranteItem> {
        val lista = mutableListOf<RestauranteItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RESTAURANTES", null)
        while (cursor.moveToNext()) {
            lista.add(
                RestauranteItem(
                    id = cursor.getInt(0),
                    nombre = cursor.getString(1),
                    cedulaJuridica = cursor.getString(2),
                    direccion = cursor.getString(3),
                    tipoComida = cursor.getString(4)
                )
            )
        }
        cursor.close()
        return lista
    }

    fun guardarPedidos(lista: List<PedidoItem>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PEDIDOS, null, null)
            lista.forEach { pedido ->
                val values = ContentValues().apply {
                    put("id", pedido.id)
                    put("restauranteNombre", pedido.restauranteNombre)
                    put("total", pedido.total)
                    put("estado", pedido.estado)
                    put("fechaPedido", pedido.fechaPedido)
                    put("notas", pedido.notas)
                }
                db.insert(TABLE_PEDIDOS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerPedidos(): List<PedidoItem> {
        val lista = mutableListOf<PedidoItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PEDIDOS", null)
        while (cursor.moveToNext()) {
            lista.add(
                PedidoItem(
                    id = cursor.getInt(0),
                    restauranteNombre = cursor.getString(1),
                    total = cursor.getDouble(2),
                    estado = cursor.getString(3),
                    fechaPedido = cursor.getString(4),
                    notas = cursor.getString(5)
                )
            )
        }
        cursor.close()
        return lista
    }

    fun guardarTarjetas(lista: List<MetodoPago>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_TARJETAS, null, null)
            lista.forEach { tarjeta ->
                val values = ContentValues().apply {
                    put("id", tarjeta.id ?: 0)
                    put("clienteId", tarjeta.clienteId ?: 0)
                    put("numeroTarjeta", tarjeta.numeroTarjeta)
                    put("fechaVencimiento", tarjeta.fechaVencimiento)
                    put("cvv", tarjeta.cvv)
                }
                db.insert(TABLE_TARJETAS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerTarjetas(): List<MetodoPago> {
        val lista = mutableListOf<MetodoPago>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TARJETAS", null)
        while (cursor.moveToNext()) {
            val idVal = cursor.getInt(0)
            val id = if (idVal == 0) null else idVal
            val clientIdVal = cursor.getInt(1)
            val clientId = if (clientIdVal == 0) null else clientIdVal
            lista.add(
                MetodoPago(
                    id = id,
                    clienteId = clientId,
                    numeroTarjeta = cursor.getString(2),
                    fechaVencimiento = cursor.getString(3),
                    cvv = cursor.getString(4)
                )
            )
        }
        cursor.close()
        return lista
    }
}