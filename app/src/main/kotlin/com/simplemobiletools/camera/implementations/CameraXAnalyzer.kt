package com.simplemobiletools.camera.implementations

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.simplemobiletools.commons.activities.BaseSimpleActivity

class CameraXAnalyzer(
	val context : BaseSimpleActivity,
	val listener: CameraXAnalyzerListener
) : ImageAnalysis.Analyzer {

	companion object {
		private const val TAG = "CameraXAnalyzer"
	}

	private val qrScanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder()
		.setBarcodeFormats(
			Barcode.FORMAT_QR_CODE,
		)
		.build()
	)

	init {
	    Log.d(TAG, "Camera analyzer initialized.")
	}

	override fun analyze(imageProxy: ImageProxy) {
		val image = InputImage.fromBitmap(imageProxy.toBitmap(), imageProxy.imageInfo.rotationDegrees)

		qrScanner.process(image)
			.addOnSuccessListener { barcodes ->
				// Task completed successfully
				if (barcodes.isNotEmpty()) {
					listener.onQRCodesDetected(barcodes, image.width, image.height)
				}
			}
			.addOnCompleteListener {
				imageProxy.close()
			}
	}
}
