package com.simplemobiletools.camera.qr

import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.simplemobiletools.camera.activities.SceneviewActivity
import com.simplemobiletools.camera.extensions.config
import com.simplemobiletools.commons.activities.BaseSimpleActivity

class CameraXAnalyzer(
	val context : BaseSimpleActivity,
	val view : QRBoxView
) : ImageAnalysis.Analyzer {

	private val options = BarcodeScannerOptions.Builder()
		.setBarcodeFormats(
			Barcode.FORMAT_QR_CODE,
			)
		.build()

	private val scanner = BarcodeScanning.getClient(options)

	init {
	    Log.d("CameraXAnalyzer", "init")
	}

	@OptIn(ExperimentalGetImage::class) override fun analyze(imageProxy: ImageProxy) {
		Log.d("test_CameraXAnalyzer", "searching...")

		val mediaImage = imageProxy.image
		if (mediaImage != null) {
			val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

			scanner.process(image)
				.addOnSuccessListener { barcodes ->
					// Task completed successfully
					if (barcodes.isEmpty()) {
						Log.d("test_CameraXAnalyzer", "No barcodes found")
						//view.toggleBox(false)
					} else {
						Log.d("CameraXAnalyzer", "Found ${barcodes.size} barcodes")
						view.drawQRBox(barcodes.first(), image.width, image.height) //FIXME: Only draws the first for now
						processBarcodes(barcodes)
					}
				}
				.addOnFailureListener {
					Log.e("CameraXAnalyzer", "Error scanning barcode", it)
				}
				.addOnCompleteListener {
					imageProxy.close()
					Log.d("test_CameraXAnalyzer", "Image closed.")
				}
		}
	}

	private fun processBarcodes(barcodes : List<Barcode>) {
		if (!execute) {
			Log.d("CameraXAnalyzer", "Ignoring...")
			return
		}

		for (barcode in barcodes) {

			// See API reference for complete list of supported types
			when (barcode.valueType) {
				Barcode.TYPE_WIFI -> {
					val ssid = barcode.wifi!!.ssid
					val password = barcode.wifi!!.password
					val type = barcode.wifi!!.encryptionType
					Log.d("CameraXAnalyzer", "WIFI: $ssid; $password; $type")
				}
				Barcode.TYPE_URL -> {
					val title = barcode.url!!.title
					val url = barcode.url!!.url
					Log.d("CameraXAnalyzer", "URL: $title; $url")
				}
				Barcode.TYPE_TEXT -> {
					val rawValue = barcode.rawValue

					if (rawValue?.startsWith("arml://", ignoreCase = true) == true) {
						val url = rawValue.substringAfter("arml://")
						Log.d("CameraXAnalyzer", "ARML: $url")
						launchARActivity(url)
					} else {
						Log.d("CameraXAnalyzer", "TEXT: $rawValue")
					}
				}
				else -> {
					val rawValue = barcode.rawValue
					Log.d("CameraXAnalyzer", "UNKNOWN: $rawValue")
				}
			}
		}
	}

	private var execute : Boolean = true
	private fun launchARActivity(armlPath : String) {
		if (!execute)
			return

		//FIXME: This is a hack to force AR mode and disable error toast. Handle this better
		context.config.forceARMode = true
		execute = false
		val intent = Intent(context, SceneviewActivity::class.java)
		intent.putExtra(Intent.EXTRA_TEXT, armlPath)
		context.startActivity(intent)
	}
}
