package com.simplemobiletools.camera.implementations

import android.content.Intent
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.simplemobiletools.camera.activities.SceneviewActivity
import com.simplemobiletools.camera.views.QRBoxView
import com.simplemobiletools.commons.activities.BaseSimpleActivity

class CameraXAnalyzer(
	val context : BaseSimpleActivity,
	val view : QRBoxView
) : ImageAnalysis.Analyzer {

	companion object {
		private const val TAG = "CameraXAnalyzer"
	}

	// See API reference for complete list of supported types
	private val barcodeTypeHandlers : HashMap<Int, (Barcode) -> Unit> = HashMap(
		mapOf(
			Pair(Barcode.TYPE_TEXT) { barcode -> handleText(barcode) },
			Pair(Barcode.TYPE_URL) { barcode -> handleURL(barcode) },
			Pair(Barcode.TYPE_WIFI) { barcode -> handleWifi(barcode) },
		)
	)
	private val textExtensionHandlers : HashMap<String, (Barcode, String) -> Unit> = HashMap(
		mapOf(
			Pair("arml") { barcode, url -> handleARML(barcode, url) },
		)
	)

	private val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder()
		.setBarcodeFormats(
			Barcode.FORMAT_QR_CODE,
		)
		.build()
	)

	init {
	    Log.d(TAG, "Camera analyzer initialized.")
	}

	fun onQRClick(xPos: Float, yPos: Float): Boolean {
		val qrCode: Barcode? = view.detectClick(xPos, yPos)
		if (qrCode != null) {
			Log.d(TAG, "QR code clicked: ${qrCode.displayValue}")
			processBarcode(qrCode)
			return true
		}
		return false
	}

	override fun analyze(imageProxy: ImageProxy) {
		val image = InputImage.fromBitmap(imageProxy.toBitmap(), imageProxy.imageInfo.rotationDegrees)

		scanner.process(image)
			.addOnSuccessListener { barcodes ->
				// Task completed successfully
				if (barcodes.isNotEmpty()) {
					view.drawQRBoxes(barcodes, image.width, image.height)
				}
			}
			.addOnCompleteListener {
				imageProxy.close()
			}
	}

	fun processBarcode(barcode : Barcode) {
		barcodeTypeHandlers.getOrElse(barcode.valueType) {
			Log.w(TAG, "Got a barcode of type ${barcode.valueType}: ${barcode.rawValue}. That type is not supported yet. Ignoring...")
			null
		}?.invoke(barcode)
	}

	private fun handleURL(barcode : Barcode) {
		val title = barcode.url!!.title
		val url = barcode.url!!.url
		Log.d(TAG, "URL: $title; $url")
	}

	private fun handleWifi(barcode : Barcode) {
		val ssid = barcode.wifi!!.ssid
		val password = barcode.wifi!!.password
		val type = barcode.wifi!!.encryptionType
		Log.d(TAG, "WIFI: $ssid; $password; $type")
	}

	private fun handleText(barcode : Barcode) {
		val rawValue = barcode.rawValue
		if (rawValue == null) {
			Log.w(TAG, "Got a barcode with no raw value. Ignoring...")
			return
		}

		val prefix = rawValue.substringBefore("://")
		val url = rawValue.substringAfter("://")

		textExtensionHandlers.getOrElse(prefix) {
			Log.w(TAG, "Got TEXT barcode: $rawValue. Don't know what to do with that. Ignoring...")
			null
		}?.invoke(barcode, url)
	}

	private fun handleARML(barcode : Barcode, url : String) {
		Log.d(TAG, "ARML: $url")
		launchARActivity(url)
	}

	private fun launchARActivity(armlPath : String) {
		val intent = Intent(context, SceneviewActivity::class.java)
		intent.putExtra(Intent.EXTRA_TEXT, armlPath)
		context.startActivity(intent)
	}
}
