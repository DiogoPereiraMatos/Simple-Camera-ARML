package com.simplemobiletools.camera.ar.qr

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.Result
import com.google.zxing.qrcode.QRCodeReader

class CameraXAnalyzer() : ImageAnalysis.Analyzer {

	private val options = BarcodeScannerOptions.Builder()
		.setBarcodeFormats(
			Barcode.FORMAT_QR_CODE,
			Barcode.FORMAT_AZTEC)
		.build()

	private val scanner = BarcodeScanning.getClient(options)

	private val qrReader = QRCodeReader()

	@OptIn(ExperimentalGetImage::class) override fun analyze(imageProxy: ImageProxy) {

		try {
			val result: Result? = qrReader.decode(BinaryBitmap(QRBinarizer(QRImageProxy(imageProxy))))
			if (result != null) {
				Log.d("CameraXAnalyzer", "ZXING Found ${result.text}")
			}
		} catch (e : NotFoundException) {
			// Ignore
		}

		val mediaImage = imageProxy.image
		if (mediaImage != null) {
			val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
			val result = scanner.process(image)
				.addOnSuccessListener { barcodes ->
					// Task completed successfully
					processBarcodes(barcodes)
				}
				.addOnFailureListener {
					// Task failed with an exception
					// Ignore.
				}
		}

		imageProxy.close()
	}

	private fun processBarcodes(barcodes : List<Barcode>) {
		for (barcode in barcodes) {

			val bounds = barcode.boundingBox
			val corners = barcode.cornerPoints

			val rawValue = barcode.rawValue

			// See API reference for complete list of supported types
			when (barcode.valueType) {
				Barcode.TYPE_WIFI -> {
					val ssid = barcode.wifi!!.ssid
					val password = barcode.wifi!!.password
					val type = barcode.wifi!!.encryptionType
					Log.d("CameraXAnalyzer", "WIFI: $ssid")
				}
				Barcode.TYPE_URL -> {
					val title = barcode.url!!.title
					val url = barcode.url!!.url
					Log.d("CameraXAnalyzer", "URL: $url")
				}
				Barcode.TYPE_TEXT -> {
					if (rawValue?.startsWith("arml://", ignoreCase = true) == true) {
						val url = rawValue.substringAfter("arml://")
						Log.d("CameraXAnalyzer", "ARML: $url")
					} else {
						Log.d("CameraXAnalyzer", "TEXT: $rawValue")
					}
				}
				else -> {
					Log.d("CameraXAnalyzer", "UNKNOWN: $rawValue")
				}
			}
		}
	}
}
