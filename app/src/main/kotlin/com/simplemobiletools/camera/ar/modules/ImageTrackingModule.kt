package com.simplemobiletools.camera.ar.modules

import android.graphics.Bitmap
import android.util.Log
import com.google.ar.core.*
import com.simplemobiletools.camera.ar.SceneController
import com.simplemobiletools.camera.ar.arml.elements.Trackable
import com.simplemobiletools.camera.ar.arml.elements.TrackableConfig
import com.simplemobiletools.camera.extensions.putIfAbsent
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size


class ImageTrackingModule(
	private val sceneController: SceneController,
	private val sceneView: ARSceneView,
	session: Session  // Only used to initialize AugmentedImageDatabase. Just to make sure we have a session...
) : ARTrackingModule {

	companion object {
		private const val TAG = "IMAGE_TRACKING_MODULE"
	}

	private var imageDatabase = AugmentedImageDatabase(session)

	// Trackables waiting for an AnchorNode (Image) go in here vvv
	private val queuedImages : HashMap<String, ArrayList<Trackable>> = HashMap()

	private var isEnabled = false
	override fun isEnabled(): Boolean = isEnabled

	override fun enable() {
		if (isEnabled)
			return

		sceneView.session!!.configure(
			sceneView.session!!.config.apply {
				augmentedImageDatabase = imageDatabase
			}
		)

		isEnabled = true
		Log.d(TAG, "Enabled ImageTracking.")
	}

	override fun disable() {
		if (!isEnabled)
			return

		sceneView.session!!.configure(
			sceneView.session!!.config.apply {
				setAugmentedImageDatabase(null)
			}
		)

		isEnabled = false
		Log.d(TAG, "Disabled ImageTracking.")
	}

	override fun reset() {
		queuedImages.values.forEach { it.clear() }

		imageDatabase = AugmentedImageDatabase(sceneView.session)
		sceneView.session!!.configure(
			sceneView.session!!.config.apply {
				augmentedImageDatabase = imageDatabase
			}
		)
		Log.d(TAG, "Reset ImageTracking.")
	}

	override fun onFrameUpdate(context: SceneController, frame: Frame) {
		if (!isEnabled)
			return

		// OnFrameUpdate: Detect and assign anchors to elements in image queue
		val images = frame.getUpdatedAugmentedImages()
		assignImages(context, images)
	}






	fun addImageToDatabase(path: String, bitmap: Bitmap, width: Double? = null) {
		if (width != null)
			imageDatabase.addImage(path, bitmap, width.toFloat())
		else
			imageDatabase.addImage(path, bitmap)
		Log.d(TAG, "Added Image $path to database")

		sceneView.session!!.configure(
			sceneView.session!!.config.apply {
				augmentedImageDatabase = imageDatabase
			}
		)
	}

	fun isImageQueued(src: String): Boolean {
		return queuedImages.containsKey(src)
	}

	fun addToImageQueue(trackable: Trackable, config: TrackableConfig) {
		queuedImages.putIfAbsent(config.src, ArrayList())
		queuedImages[config.src]!!.putIfAbsent(trackable)
		Log.d(TAG, "Waiting for anchor (Image; src=${config.src}) for ${trackable.toShortString()}")
		return
	}

	fun getQueuedImages(src: String): ArrayList<Trackable>? {
		return queuedImages[src]
	}

	fun clearQueuedImages(src: String) {
		queuedImages[src]?.clear()
	}

	private fun assignImages(context: SceneController, images: Collection<AugmentedImage>) {
		for (img in images) {
			if (img.trackingState != TrackingState.TRACKING) {
				Log.d(TAG, "Detected Image: ${img.name}; idx=${img.index}; tracking=${img.trackingState}")
				continue
			}

			if (!isImageQueued(img.name))
				continue

			val waiting = getQueuedImages(img.name)!!
			if (waiting.isEmpty())
				continue

			Log.d(TAG, "Detected Image: ${img.name}; idx=${img.index}; tracking=${img.trackingState}; size=(${img.extentX},${img.extentZ})")

			// Create google.Anchor from Image
			val anchor = img.createAnchor(img.centerPose)

			for (trackable in waiting) {
				// Create AnchorNode from google.Anchor, and associate it to Anchor (trackable), adding it to the scene
				val anchorNode = AnchorNode(sceneView.engine, anchor).apply {
					transform(
						position = Position(0f, 0f, 0f),
						rotation = Rotation(0f, 0f, 0f),
						scale = Size(1f, 1f, 1f)
					)
				}
				sceneController.setParentNode(trackable, anchorNode)
				sceneView.addChildNode(anchorNode)
				Log.d(TAG, "Assigned anchor to ${trackable.toShortString()}")

				//TODO: Preload assets
				for (asset in trackable.sortedAssets) {
					if (!asset.enabled) continue

					// Call handler
					context.assetHandlers.getOrElse(asset.arElementType) {
						Log.w(TAG, "Got a ${asset.arElementType} asset. That type is not supported yet. Ignoring...")
						null
					}?.invoke(sceneController.getParentNode(trackable)!!, asset)
				}

				// Add RelativeTos referencing this Trackable
				context.propagateNode(trackable)
			}

			// All processed. Clear queue
			clearQueuedImages(img.name)
		}
	}
}
