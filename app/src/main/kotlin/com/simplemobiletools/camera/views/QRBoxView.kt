package com.simplemobiletools.camera.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.ViewGroup
import com.google.mlkit.vision.barcode.common.Barcode
import com.simplemobiletools.commons.extensions.darkenColor
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.extensions.getProperPrimaryColor

class QRBoxView(context: Context) : ViewGroup(context) {

	companion object {
		private const val TAG = "QRBoxView"
		private const val BOX_TIMEOUT_MS = 100L //ms
		private const val DEBUG_MODE = true
	}

	private var lastBoxes: HashMap<Barcode, RectF> = HashMap() //Keep list that is never empty, for tap detection
	private val detectedBoxes: HashMap<Barcode, RectF> = HashMap()

	private val onTimeout = Runnable {
		lastBoxes.clear() //So user can't select QR after timeout
		invalidate() //Redraw (remove boxes)
	}

	private var debug_imageBox : RectF? = null
	private val debug_detectedBoxes : ArrayList<RectF> = ArrayList()
	private var debug_click: Pair<Float, Float>? = null

	private val mBoxPaint: Paint = Paint().apply {
		style = Paint.Style.STROKE
		color = context.getProperPrimaryColor().getContrastColor()
		strokeWidth = 7.5f
	}

	private var mTextPaint: Paint = Paint().apply {
		style = Paint.Style.STROKE
		color = context.getProperPrimaryColor().getContrastColor().darkenColor()
		strokeWidth = 1.5f
		textSize = 30f
	}

	private var mRedPaint: Paint = Paint().apply {
		style = Paint.Style.STROKE
		color = Color.RED
		strokeWidth = 5f
	}

	init {
		setWillNotDraw(false)
	}

	fun drawQRBoxes(barcodes: List<Barcode>, imageWidth: Int, imageHeight: Int) {
		// FIXME: For some reason qrWidth and qrHeight are switched
		val actualWidth = imageHeight
		val actualHeight = imageWidth
		debug_imageBox = RectF(0f, 0f, actualWidth.toFloat(), actualHeight.toFloat())

		val widthRatio: Float = width.toFloat() / actualWidth.toFloat()
		val heightRatio: Float = height.toFloat() / actualHeight.toFloat()

		detectedBoxes.clear()
		for (barcode in barcodes) {
			detectedBoxes[barcode] = barcode.boundingBox?.let { box ->
				debug_detectedBoxes.add(
					RectF(
						box.left.toFloat(),
						box.top.toFloat(),
						box.right.toFloat(),
						box.bottom.toFloat()
					)
				)

				// FIXME: There's something wrong with the horizontal coordinates; box doesn't cover the whole QR. Also doesn't work very well with rotation
				RectF(
					box.left.toFloat() * widthRatio,
					box.top.toFloat() * heightRatio,
					box.right.toFloat() * widthRatio,
					box.bottom.toFloat() * heightRatio
				)
			} ?: return
		}

		invalidate()
	}

	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		detectedBoxes.forEach { (barcode, box) ->
			canvas.drawRect(box, mBoxPaint)
			barcode.displayValue?.let { canvas.drawText(it, box.left, box.top - mTextPaint.textSize - 5f, mTextPaint) }
		}
		lastBoxes = detectedBoxes.clone() as HashMap<Barcode, RectF>
		detectedBoxes.clear()

		if (DEBUG_MODE)
			drawDebugStuff(canvas)

		// Reset timeout
		removeCallbacks(onTimeout)
		postDelayed(onTimeout, BOX_TIMEOUT_MS)
	}

	private fun drawDebugStuff(canvas: Canvas) {
		if (debug_click != null) {
			canvas.drawCircle(debug_click!!.first, debug_click!!.second, 20f, mRedPaint)
		}

		if (debug_imageBox != null) {
			canvas.drawRect(debug_imageBox!!, mRedPaint)
		}

		debug_detectedBoxes.forEach { box ->
			canvas.drawRect(box, mRedPaint)
		}
		debug_detectedBoxes.clear()
	}

	fun detectClick(x: Float, y: Float): Barcode? {
		debug_click = Pair(x, y)
		lastBoxes.forEach { (barcode, box) ->
			if (box.contains(x, y))
				return barcode
		}
		return null
	}
}
