package com.simplemobiletools.camera.qr

import android.content.Context
import android.graphics.*
import android.view.ViewGroup
import com.google.mlkit.vision.barcode.common.Barcode
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import kotlin.math.ceil
import kotlin.math.floor

class QRBoxView(context: Context) : ViewGroup(context) {

	private var mLastBox : Rect? = null
	private var mLastContent : String? = null
	private var mLastCorners : FloatArray? = null

	private var mBoxPaint: Paint
	private var mTextPaint: Paint
	private var mCornerPaint : Paint

	private var mDrawBox = false
	private var mBoxTimeout = 1000L

	init {
		setWillNotDraw(false)
		mBoxPaint = Paint().apply {
			style = Paint.Style.STROKE
			color = context.getProperPrimaryColor()
			strokeWidth = 7.5f
		}
		mTextPaint = Paint().apply {
			style = Paint.Style.STROKE
			color = context.getProperPrimaryColor()
			strokeWidth = 1.5f
			textSize = 15f
		}
		mCornerPaint = Paint().apply {
			style = Paint.Style.STROKE
			color = Color.RED
			strokeWidth = 10f
		}
	}

	fun drawQRBox(barcode : Barcode, qrWidth: Int, qrHeight: Int) {
		//width = 1080, height = 2400
		//qrWidth = 640, qrHeight = 480

		val widthRatio : Float = width.toFloat() / qrHeight.toFloat()
		val heightRatio : Float = height.toFloat() / qrWidth.toFloat()

		mLastBox = barcode.boundingBox?.let {
			Rect(
				floor(it.left * widthRatio).toInt(),
				floor(it.top * heightRatio).toInt(),
				ceil(it.right * widthRatio).toInt(),
				ceil(it.bottom * heightRatio).toInt()
			)
		}

		mLastContent = barcode.displayValue

		val result = ArrayList<Float>()
		for (i in 0..<(barcode.cornerPoints?.size ?: 0)) {
			val p : Point = barcode.cornerPoints!![i]
			val next : Point = barcode.cornerPoints!![(i+1) % barcode.cornerPoints!!.size]

			result.add(p.x.toFloat() * heightRatio - 400f)
			result.add(p.y.toFloat() * heightRatio)
			result.add(next.x.toFloat() * heightRatio - 400f)
			result.add(next.y.toFloat() * heightRatio)
		}
		mLastCorners = result.toFloatArray()

		showBox()
	}

	fun showBox() {
		removeCallbacks(null)
		toggleBox(true)
		postDelayed({
			toggleBox(false)
		}, mBoxTimeout)
	}

	fun toggleBox(show: Boolean) {
		mDrawBox = show
		invalidate()
	}

	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		//canvas.drawLine(600f, 480f, 640f, 480f, mCornerPaint)
		//canvas.drawLine(640f, 440f, 640f, 480f, mCornerPaint)

		if (mDrawBox && mLastBox != null) {
			//canvas.drawRect(mLastBox!!, mBoxPaint)
			canvas.drawLines(mLastCorners!!, mBoxPaint)
			canvas.drawText(mLastContent!!, mLastBox!!.left.toFloat(), mLastBox!!.bottom.toFloat()-20f, mTextPaint)
		}
	}
}
