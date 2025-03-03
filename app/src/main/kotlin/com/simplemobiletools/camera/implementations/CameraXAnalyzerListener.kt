package com.simplemobiletools.camera.implementations

import com.google.mlkit.vision.barcode.common.Barcode

interface CameraXAnalyzerListener {
	fun onQRCodesDetected(barcodes: List<Barcode>, imageWidth: Int, imageHeight: Int)
}
