package com.shpakovskiy.cambot.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
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

    private var cameraManager: CameraManager? = null
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
        val img = reader?.acquireLatestImage()

        val thisImage = System.currentTimeMillis()
        if (thisImage - lastImage > 100) {
            lastImage = thisImage

            img?.let { image ->
                Log.d(TAG, "New image: $image")

                val yuvToRgbConverter = YuvToRgbConverter(applicationContext)
                val bmp = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                yuvToRgbConverter.yuvToRgb(image, bmp)

                val rotatedBitmap: Bitmap

                val rotateMatrix = Matrix()
                rotateMatrix.postRotate(/*rotation.toFloat()*/0.0F)
                rotatedBitmap = Bitmap.createBitmap(
                    bmp, 0, 0,
                    bmp.width, bmp.height,
                    rotateMatrix, false
                )

                val outputStream = ByteArrayOutputStream()
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
                rotatedBitmap.recycle()

                Log.d("TAG123", "Send image")
                webSocketServer.broadcast(outputStream.toByteArray())
            }
        }

        img?.close()
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
        Log.d(TAG, "onStartCommand")

        // grpcSetup()
        start()

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()

        stopCamera()
    }

    private fun start() {
        initCam()
    }

    @SuppressLint("MissingPermission")
    private fun initCam() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        var camId: String? = null

        for (id in cameraManager!!.cameraIdList) {
            val characteristics = cameraManager!!.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                camId = id
                break
            }
        }


        previewSize = chooseSupportedSize(camId!!)

        cameraManager!!.openCamera(camId, stateCallback, null)
    }

    private fun chooseSupportedSize(camId: String): Size {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val characteristics = manager.getCameraCharacteristics(camId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSizes = map?.getOutputSizes(SurfaceTexture::class.java)

        return supportedSizes?.get(supportedSizes.size / 2 - 2) ?: Size(1, 1)
    }

    private fun createCaptureSession() {
        try {
            val targetSurfaces = ArrayList<Surface>()

            // Prepare CaptureRequest that can be used with CameraCaptureSession
            val requestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {

                    // Configure target surface for background processing (ImageReader)
                    imageReader = ImageReader.newInstance(
                        previewSize!!.width, previewSize!!.height,
                        ImageFormat.YUV_420_888, 2
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

            // Prepare CameraCaptureSession
            cameraDevice!!.createCaptureSession(
                targetSurfaces,
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (null == cameraDevice) {
                            return
                        }

                        //imageReader!!.acquireLatestImage()

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