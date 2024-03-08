package com.simplemobiletools.camera.ar.qr

import androidx.camera.core.ImageProxy
import com.google.zxing.Binarizer
import com.google.zxing.LuminanceSource
import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix
import java.nio.ByteBuffer

class QRImageProxy(imageProxy : ImageProxy) : LuminanceSource(imageProxy.width, imageProxy.height) {

	val buffer = imageProxy.planes[0].buffer
	val data : ByteArray = buffer.toByteArray()
	val lum : ByteArray = data.map{ (it.toInt() and 0xFF).toByte() }.toByteArray()

	override fun getRow(y: Int, row: ByteArray?): ByteArray {
		val dst : ByteArray = if (row == null || row.size < width) ByteArray(width) else row
		return lum.copyInto(dst, startIndex = y * width, endIndex = (y+1) * width)
	}

	override fun getMatrix(): ByteArray {
		return lum
	}

	private fun ByteBuffer.toByteArray(): ByteArray {
		rewind()    // Rewind the buffer to zero
		val data = ByteArray(remaining())
		get(data)   // Copy the buffer into a byte array
		return data // Return the byte array
	}
}

class QRBinarizer(val qrImageProxy: QRImageProxy) : Binarizer(qrImageProxy) {

	override fun getBlackRow(y: Int, row: BitArray?): BitArray {
		val dst = if (row == null || row.size < qrImageProxy.width) BitArray(qrImageProxy.width) else row
		qrImageProxy.getRow(y, null).forEach { dst.appendBit(it.toInt() >= 128) }
		return dst
	}

	override fun getBlackMatrix(): BitMatrix {
		val result = BitMatrix(qrImageProxy.width, qrImageProxy.height)

		for (row in 0..<qrImageProxy.height) {
			result.setRow(row, getBlackRow(row, null))
		}

		return result
	}

	override fun createBinarizer(source: LuminanceSource?): Binarizer {
		if (source !is QRImageProxy) throw IllegalArgumentException("\"source\" argument must be a ImageProxy (androidx.camera.core.ImageProxy)")
		return QRBinarizer(source)
	}
}
