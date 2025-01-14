package com.simplemobiletools.camera.ar.modules

import android.graphics.Bitmap
import android.util.Log
import com.google.ar.core.*
import com.simplemobiletools.camera.ar.SceneController
import com.simplemobiletools.camera.ar.arml.elements.Trackable
import com.simplemobiletools.camera.ar.arml.elements.TrackableConfig
import com.simplemobiletools.camera.ar.putIfAbsent
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages

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






	fun addImageToDatabase(path: String, bitmap: Bitmap) {
		imageDatabase.addImage(path, bitmap)
		Log.d(TAG, "Added Image $path to database")

		sceneView.session!!.configure(
			sceneView.session!!.config.apply {
				augmentedImageDatabase = imageDatabase
			}
		)
	}

	fun isImageKnown(src: String): Boolean {
		return queuedImages.containsKey(src)
	}

	fun addToImageQueue(trackable: Trackable, config: TrackableConfig) {
		queuedImages.putIfAbsent(config.src, ArrayList())
		queuedImages[config.src]!!.putIfAbsent(trackable)
		Log.d(TAG, "Waiting for anchor (Image; src=${config.src}) for Trackable(id=${trackable.id})")
		return
	}

	fun getQueuedImages(src: String): ArrayList<Trackable>? {
		return queuedImages[src]
	}

	fun clearQueuedImages(src: String) {
		queuedImages[src]?.clear()
	}

	private fun assignImages(context: SceneController, images: Collection<AugmentedImage>) {
		images.forEach { img ->
			if (img.trackingState != TrackingState.TRACKING) {
				Log.d(TAG, "Detected Image: ${img.name}; idx=${img.index}; tracking=${img.trackingState}")
				return@forEach
			}

			if (isImageKnown(img.name))
				return@forEach

			val waiting = getQueuedImages(img.name)!!
			if (waiting.isEmpty())
				return@forEach

			Log.d(TAG, "Detected Image: ${img.name}; idx=${img.index}; tracking=${img.trackingState}")

			// Create google.Anchor from Image
			val anchor = img.createAnchor(img.centerPose)

			waiting.forEach { trackable ->
				// Create AnchorNode from google.Anchor, and associate it to Anchor (trackable) adding it to the scene
				sceneController.addToScene(trackable, anchor)
				Log.d(TAG, "Assigned anchor to Trackable(id=${trackable.id})")

				trackable.sortedAssets.forEach {
					if (!it.enabled) return@forEach
					context.assetHandlers.getOrElse(it.arElementType) {
						Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet.")
						null
					}?.invoke(sceneController.getAnchorNode(trackable)!!, it)
				}

				// Add RelativeTos referencing this Trackable
				context.copyAnchorNode(trackable)
			}

			// All processed. Clear queue
			clearQueuedImages(img.name)
		}
	}
}
