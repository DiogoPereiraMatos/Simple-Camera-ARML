package com.simplemobiletools.camera.helpers

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.mlkit.vision.barcode.common.Barcode
import com.simplemobiletools.camera.activities.SceneviewActivity

class QRCodeHandler(
    private val context: Context,
) {

	companion object {
		private const val TAG = "QRCodeHandler"
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
