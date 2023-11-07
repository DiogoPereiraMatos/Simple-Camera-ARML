package com.simplemobiletools.camera.helpers.ai

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.ar.core.Frame
import com.google.ar.core.Session
import java.nio.ByteBuffer

class ARML(private val session: Session) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }
    override fun analyze(image: ImageProxy) {

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        Log.d("ImageAnalyser", "Average luminosity: $luma")

        image.close()

        //val frame = session.update()
    }
}
