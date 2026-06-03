package com.cletaeats.ui.tracking

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

object TrackingCoordinates {

    /** Destino fijo: 9°58'14.4"N 84°07'44.0"W — UNA Campus Benjamín Núñez, Heredia */
    val CLIENTE_POINT = GeoPoint(9.9707, -84.1289)

    private val PROVINCIA_COORDS = mapOf(
        "heredia"     to GeoPoint(9.9994, -84.1194),
        "alajuela"    to GeoPoint(10.0162, -84.2125),
        "cartago"     to GeoPoint(9.8641, -83.9194),
        "guanacaste"  to GeoPoint(10.6341, -85.4381),
        "liberia"     to GeoPoint(10.6341, -85.4381),
        "puntarenas"  to GeoPoint(9.9765, -84.8383),
        "limon"       to GeoPoint(9.9907, -83.0353),
        "limón"       to GeoPoint(9.9907, -83.0353),
    )
    private val DEFAULT = GeoPoint(9.9281, -84.0907) // San José (default)

    /**
     * Retorna el GeoPoint del restaurante según provincia detectada.
     * Busca primero en [direccion] (más confiable) y luego en [nombre] como fallback.
     */
    fun forRestaurante(nombre: String?, direccion: String? = null): GeoPoint {
        val searchTargets = listOfNotNull(direccion, nombre).map { it.lowercase() }
        if (searchTargets.isEmpty()) return DEFAULT
        return PROVINCIA_COORDS.entries
            .firstOrNull { (keyword, _) -> searchTargets.any { it.contains(keyword) } }
            ?.value ?: DEFAULT
    }

    /** Llama a OSRM y retorna los puntos del polyline, o lista vacía si falla. */
    suspend fun fetchOsrmRoute(from: GeoPoint, to: GeoPoint): List<GeoPoint> =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://router.project-osrm.org/route/v1/driving/" +
                    "${from.longitude},${from.latitude};${to.longitude},${to.latitude}" +
                    "?overview=full&geometries=geojson"
                val json = java.net.URL(url).readText()
                val resp = Gson().fromJson(json, OsrmResponse::class.java)
                resp.routes.firstOrNull()?.geometry?.coordinates
                    ?.map { GeoPoint(it[1], it[0]) } ?: emptyList()
            } catch (e: Exception) {
                Log.e("CletaEats", "OSRM fetch failed: ${e.message}")
                emptyList()
            }
        }

    /** Icono de casa para el marcador de destino del cliente. */
    fun createHouseIcon(context: Context): Drawable {
        val dp = context.resources.displayMetrics.density
        val size = (40 * dp).toInt()
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3D1A0E") // BrownDark
        }
        val w = size.toFloat(); val h = size.toFloat()
        // Techo
        cv.drawPath(Path().apply {
            moveTo(w / 2, 0f); lineTo(0f, h * 0.45f); lineTo(w, h * 0.45f); close()
        }, paint)
        // Paredes
        cv.drawRect(w * 0.1f, h * 0.45f, w * 0.9f, h, paint)
        // Puerta (blanca)
        paint.color = Color.WHITE
        cv.drawRect(w * 0.35f, h * 0.62f, w * 0.65f, h, paint)
        return BitmapDrawable(context.resources, bmp)
    }
}

private data class OsrmResponse(val routes: List<OsrmRoute>)
private data class OsrmRoute(val geometry: OsrmGeometry)
private data class OsrmGeometry(val coordinates: List<List<Double>>)
