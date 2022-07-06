package com.shpakovskiy.cambot.data.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.IBinder
import android.util.Log
import android.util.Size
import android.view.Surface
import com.shpakovskiy.cambot.data.LocalWebSocketServer
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class CameraService : Service() {

    @Inject
    lateinit var webSocketServer: LocalWebSocketServer

    private var previewSize: Size? = null
    private var cameraDevice: CameraDevice? = null
    private var captureRequest: CaptureRequest? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    companion object {
        private const val TAG = "CameraService"
    }

    private var lastImage = System.currentTimeMillis()

    private val imageListener = ImageReader.OnImageAvailableListener { reader ->
        val latestImage = reader?.acquireLatestImage()

        val thisImage = System.currentTimeMillis()
        if (thisImage - lastImage > 80) {
            lastImage = thisImage

            latestImage?.let { image ->
                val imageBuffer = image.planes.first().buffer
                val byteArray = ByteArray(imageBuffer.capacity())
                imageBuffer.get(byteArray)

                val bmpOptions = BitmapFactory.Options()
                val bmp = BitmapFactory.decodeByteArray(
                    byteArray, 0, imageBuffer.capacity(), bmpOptions
                )

                val outputStream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
                bmp.recycle()

                webSocketServer.broadcast(outputStream.toByteArray())

                image.close()
            }
        }

        latestImage?.close()
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(currentCameraDevice: CameraDevice) {
            cameraDevice = currentCameraDevice
            createCaptureSession()
        }

        override fun onDisconnected(currentCameraDevice: CameraDevice) {
            currentCameraDevice.close()
            cameraDevice = null
        }

        override fun onError(currentCameraDevice: CameraDevice, error: Int) {
            currentCameraDevice.close()
            cameraDevice = null
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initCamera()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopCamera()
    }

    @SuppressLint("MissingPermission")
    private fun initCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                previewSize = chooseSupportedSize(cameraId)
                cameraManager.openCamera(cameraId, stateCallback, null)
                break
            }
        }
    }

    private fun chooseSupportedSize(camId: String): Size {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val characteristics = manager.getCameraCharacteristics(camId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSizes = map?.getOutputSizes(SurfaceTexture::class.java)

        Log.d(TAG, "Supported camera resolutions: ${supportedSizes.contentToString()}")

        return Size(640, 480)
    }

    private fun createCaptureSession() {
        try {
            val targetSurfaces = ArrayList<Surface>()

            val requestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {

                    // Configure target surface for background processing (ImageReader)
                    imageReader = ImageReader.newInstance(
                        previewSize!!.width, previewSize!!.height,
                        ImageFormat.JPEG, 2
                    )
                    imageReader!!.setOnImageAvailableListener(imageListener, null)


                    targetSurfaces.add(imageReader!!.surface)
                    addTarget(imageReader!!.surface)

                    // Set some additional parameters for the request
                    set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                    )
                }

            cameraDevice!!.createCaptureSession(
                targetSurfaces,
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (null == cameraDevice) {
                            return
                        }

                        captureSession = cameraCaptureSession
                        try {
                            captureRequest = requestBuilder.build()
                            captureSession!!.setRepeatingRequest(
                                captureRequest!!,
                                null,
                                null
                            )

                        } catch (e: CameraAccessException) {
                            Log.e(TAG, "createCaptureSession", e)
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Log.e(TAG, "createCaptureSession()")
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "createCaptureSession", e)
        }
    }

    private fun stopCamera() {
        try {
            captureSession?.close()
            captureSession = null

            cameraDevice?.close()
            cameraDevice = null

            imageReader?.close()
            imageReader = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}