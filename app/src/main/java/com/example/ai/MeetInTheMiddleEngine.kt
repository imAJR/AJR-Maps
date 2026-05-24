package com.example.ai

import com.example.common.GeoPoint
import kotlin.math.max
import kotlin.math.min

/**
 * Phase 4: Meet-in-the-Middle Feature
 * Calculates a fair, equidistant meeting point between two users based on simulated traffic and distance.
 */
object MeetInTheMiddleEngine {

    fun calculateFairMeetingPoint(user1: GeoPoint, user2: GeoPoint, trafficFactor: Double = 1.0): GeoPoint {
        // Pseudo-algorithm for finding an equidistant point considering traffic.
        // In reality, this would query a routing API (like OSM/GraphHopper) for the exact midpoint
        // in terms of travel time (isochrones).
        
        val midLat = (user1.latitude + user2.latitude) / 2
        val midLon = (user1.longitude + user2.longitude) / 2
        
        // Simulating traffic skew (if trafficFactor > 1.0, user1 has traffic, push midpoint closer to user1)
        val skewedLat = if (trafficFactor > 1.0) midLat + 0.005 else midLat
        val skewedLon = if (trafficFactor > 1.0) midLon + 0.005 else midLon

        return GeoPoint(skewedLat, skewedLon)
    }
}
