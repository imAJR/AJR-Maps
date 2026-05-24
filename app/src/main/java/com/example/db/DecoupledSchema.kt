package com.example.db

/**
 * Phase 6: Decoupled Identity Database Schema Reference
 * This file demonstrates the architectural requirement where User Personally Identifiable Information (PII)
 * is strictly decoupled from their Location/Telemetry data.
 */
object DecoupledSchema {

    // ==========================================
    // DATABASE 1: IDENTITY DB (Strictly PII)
    // Stored heavily encrypted. Never queried alongside location data.
    // ==========================================
    data class AuthUser(
        val internalUuid: String, // Random UUID generated locally
        val authProvider: AuthProvider,
        val emailOrPhoneHash: String, // Hashed, never plain text
        val anonymousAvatarName: String // e.g. "Desert Fox 99"
    )

    enum class AuthProvider {
        GOOGLE_WEB,
        HUAWEI_ID,
        HONOR_ID,
        MAGIC_LINK_OTP,
        GUEST_NINJA
    }

    // ==========================================
    // DATABASE 2: SPATIAL DB (Telemetry & Tracks)
    // Keyed ONLY by a rotating anonymous token, NOT the User's UUID.
    // ==========================================
    data class SpatialTrack(
        val rotatingSessionToken: String, // Regenerated every 24h or per "Ninja" toggle
        val startPointLatFuzzed: Double,
        val startPointLonFuzzed: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val pathPolylineEncrypted: String, // E2E Encrypted binary string
        val timestamp: Long
    )

    // ==========================================
    // DATABASE 3: FAVORITES & E2E (End-to-End)
    // ==========================================
    data class EncryptedFavorite(
        val displayAlias: String, // e.g., "Work", "Home" (Masked via PrivacyUtils)
        val encryptedCoordinates: String // Only decryptable by on-device private key
    )
}
