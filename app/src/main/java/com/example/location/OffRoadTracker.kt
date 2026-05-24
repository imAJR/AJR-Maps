package com.example.location

import com.example.common.GeoPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Phase 4: Kashta (Off-Road) Mode
 * Specialized tracker that records a breadcrumb trail without relying on the internet.
 * Essential for desert trekking and returning safely.
 */
class OffRoadTracker {
    
    private val recordedBreadcrumbs = mutableListOf<GeoPoint>()
    private var isRecording = false

    fun startRecording() {
        isRecording = true
        recordedBreadcrumbs.clear()
    }

    fun addBreadcrumb(point: GeoPoint) {
        if (isRecording) {
            recordedBreadcrumbs.add(point)
        }
    }

    fun stopRecordingAndSave(outputDir: File): File? {
        isRecording = false
        if (recordedBreadcrumbs.isEmpty()) return null

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(outputDir, "Kashta_Track_$timestamp.gpx")
        
        // Simulating GPX local file creation for complete offline survival
        file.bufferedWriter().use { out ->
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            out.write("<gpx version=\"1.1\">\n")
            out.write("  <trk>\n")
            out.write("    <trkseg>\n")
            recordedBreadcrumbs.forEach { pt ->
                out.write("      <trkpt lat=\"${pt.latitude}\" lon=\"${pt.longitude}\"></trkpt>\n")
            }
            out.write("    </trkseg>\n")
            out.write("  </trk>\n")
            out.write("</gpx>\n")
        }
        
        return file
    }

    fun getActiveTrack(): List<GeoPoint> = recordedBreadcrumbs.toList()
}
