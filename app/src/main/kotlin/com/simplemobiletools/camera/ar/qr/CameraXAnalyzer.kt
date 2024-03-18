package com.simplemobiletools.camera.ar.qr

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Point
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat.startActivity
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.Result
import com.google.zxing.qrcode.QRCodeReader
import com.simplemobiletools.camera.activities.ArActivity
import com.simplemobiletools.camera.activities.SettingsActivity
import com.simplemobiletools.camera.ar.arml.ARMLParser
import com.simplemobiletools.camera.ar.arml.elements.ARML
import org.xml.sax.InputSource
import java.io.File
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class CameraXAnalyzer(
	val context : Context,
	val view : QRBoxView
) : ImageAnalysis.Analyzer {

	private val options = BarcodeScannerOptions.Builder()
		.setBarcodeFormats(
			Barcode.FORMAT_QR_CODE,
			)
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
					if (barcodes.isEmpty()) {
						//view.toggleBox(false)
					} else {
						processBarcodes(barcodes)
						view.drawQRBox(barcodes.first(), image.width, image.height)
					}
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
					val rawValue = barcode.rawValue

					if (rawValue?.startsWith("arml://", ignoreCase = true) == true) {
						val url = rawValue.substringAfter("arml://")
						Log.d("CameraXAnalyzer", "ARML: $url")

						try {
							/*
							val builderFactory = DocumentBuilderFactory.newInstance()
							val docBuilder = builderFactory.newDocumentBuilder()
							val doc = docBuilder.parse(InputSource(URL("http://" + url).openStream()))
							val armlContent = doc.textContent
							 */
							val armlContent = context.assets.open("armlexamples/$url").readBytes().decodeToString()
							val arml: ARML? = ARMLParser().loads(armlContent)
							if (arml == null) {
								Log.d("CameraXAnalyzer", "Invalid ARML!")
								break
							}

							val validation = arml.validate()
							if (!validation.first) {
								Log.d("CameraXAnalyzer", "Invalid ARML (${validation.second})!")
								break
							}

							Log.d("CameraXAnalyzer", arml.toString())
							launchARActivity(armlContent)
							
						} catch (e : Exception) {
							Log.e("CameraXAnalyzer", "Failed to read ARML!", e)
							break
						}

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

	private fun launchARActivity(armlContent : String) {
		val intent = Intent(context, ArActivity::class.java)
		intent.putExtra("armlContent", armlContent)
		context.startActivity(intent)
	}
}
