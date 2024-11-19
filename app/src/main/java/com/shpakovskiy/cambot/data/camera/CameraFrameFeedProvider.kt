package com.shpakovskiy.cambot.data.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.util.Size
import android.view.Surface
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("MissingPermission")
class CameraFrameFeedProvider @Inject constructor(
    private val context: Context
) {
    private var previewSize = Size(640, 480) // TODO

    // private var cameraDevice: CameraDevice? = null
    private var captureRequest: CaptureRequest? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    private suspend fun getCameraDevice(): CameraDevice? = suspendCoroutine { continuation ->
        (context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager)?.let { cameraManager ->
            cameraManager.cameraIdList.firstOrNull { cameraId ->
                cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }?.let { cameraId ->
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(device: CameraDevice) = continuation.resume(device)

                    override fun onDisconnected(device: CameraDevice) {
                        device.close()
                        continuation.resume(null)
                    }

                    override fun onError(device: CameraDevice, error: Int) {
                        device.close()
                        continuation.resume(null)
                    }
                }, null)
            }
        }
    }

    suspend fun startCaptureSession(): Flow<Bitmap> = callbackFlow {

        getCameraDevice()?.startCaptureSession(previewSize, ::trySend)

        awaitClose { cancel() }
    }

    private fun CameraDevice.startCaptureSession(previewSize: Size, onBitmap: (Bitmap) -> Unit) {
        val targetSurfaces = mutableListOf<Surface>()

        val requestBuilder = createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
            imageReader = ImageReader.newInstance(
                previewSize.width, previewSize.height, ImageFormat.JPEG, 2
            ).apply {
                setOnImageAvailableListener({ reader ->
                    reader?.acquireLatestImage()?.use { image ->
                        val imageBuffer = image.planes.first().buffer
                        val byteArray = ByteArray(imageBuffer.capacity())
                        imageBuffer.get(byteArray)

                        val bitmap = BitmapFactory.decodeByteArray(
                            byteArray, 0, imageBuffer.capacity()
                        )

                        onBitmap(bitmap)

                        // val outputStream = ByteArrayOutputStream()
                        // bmp.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
                        // bmp.recycle()
                    }
                }, null)
                targetSurfaces.add(surface)
                addTarget(surface)
            }
        }

        createCaptureSession(
            targetSurfaces,
            object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    captureSession = cameraCaptureSession

                    captureRequest = requestBuilder.build().apply {
                        cameraCaptureSession.setRepeatingRequest(
                            this, null, null
                        )
                    }
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {

                }
            }, null
        )
    }

    private fun stopCamera() {
        try {
            captureSession?.close()
            captureSession = null

            // TODO
            // cameraDevice?.close()
            // cameraDevice = null

            imageReader?.close()
            imageReader = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}