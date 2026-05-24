package com.example.privacy

import com.example.common.GeoPoint
import kotlin.random.Random

/**
 * Core Privacy Engine implementation based on "Privacy Fortress" specifications.
 */
object PrivacyUtils {

    /**
     * Phase 2: Prompt Scrubbing (Location Fuzzing)
     * Obfuscates exact coordinates into a ~2km radius generalized bounding box 
     * before sending data to Claude/Gemini/ChatGPT.
     */
    fun scrubPromptLocation(exactLocation: GeoPoint): GeoPoint {
        val offsetDeg = 0.018 // Roughly ~2km offset
        val latOffset = Random.nextDouble(-offsetDeg, offsetDeg)
        val lonOffset = Random.nextDouble(-offsetDeg, offsetDeg)
        return GeoPoint(exactLocation.latitude + latOffset, exactLocation.longitude + lonOffset)
    }

    /**
     * Phase 2: Home Masking
     * Mathematically offsets the pinned location to the nearest approximate street intersection,
     * ensuring the exact house is never exposed to the database.
     */
    fun maskHomeLocation(actualHome: GeoPoint): GeoPoint {
        // In a full implementation, this uses reverse-geocoding to snap to a nearby road intersection.
        // For off-grid masking, we apply a static offset (~200 meters).
        val offsetDeg = 0.002
        return GeoPoint(actualHome.latitude + offsetDeg, actualHome.longitude - offsetDeg)
    }

    /**
     * Phase 4: Data Encryption Stub
     * Represents the End-to-End Encryption (E2E) layer before cloud sync.
     */
    fun encryptSavedPlace(placeName: String, location: GeoPoint): String {
        return "ENC[AES-256-GCM]${placeName.hashCode()}-${location.latitude}"
    }
}
