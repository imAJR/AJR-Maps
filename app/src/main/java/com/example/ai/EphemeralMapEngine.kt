package com.example.ai

import com.example.common.GeoPoint
import java.util.Calendar

/**
 * Phase 5: Ephemeral Map Generation
 * Dynamically spawns points of interest (e.g., Tea stalls, Food trucks) that only exist
 * at specific times of day or specific conditions, hiding them otherwise to reduce map clutter.
 */
object EphemeralMapEngine {

    data class EphemeralPOI(val name: String, val location: GeoPoint, val activeHours: IntRange)

    private val tempPOIs = listOf(
        EphemeralPOI("Midnight Tea Stall (بسطة شاي)", GeoPoint(24.7110, 46.6710), 20..28), // 8 PM to 4 AM
        EphemeralPOI("Morning Food Truck (فطور)", GeoPoint(24.7150, 46.6800), 5..10)
    )

    fun getActivePOIs(currentTimeMillis: Long): List<EphemeralPOI> {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        
        return tempPOIs.filter { poi ->
            val extendedHour = if (hourOfDay < 4) hourOfDay + 24 else hourOfDay
            extendedHour in poi.activeHours
        }
    }
}
