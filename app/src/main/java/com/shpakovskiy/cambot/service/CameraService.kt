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
import java.io.ByteArrayOutputStream
import kotlin.math.absoluteValue

class CameraService : Service() {

    private var cameraManager: CameraManager? = null
    private var previewSize: Size? = null
    private var cameraDevice: CameraDevice? = null
    private var captureRequest: CaptureRequest? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    companion object {
        private const val TAG = "CameraService"
    }

    private val imageListener = ImageReader.OnImageAvailableListener { reader ->
        val img = reader?.acquireLatestImage()

        // Log.d(TAG, "Got image: " + img?.width + " x " + img?.height + " Time: ${System.nanoTime()}")

        //val image: Image = imageReader!!.acquireLatestImage()
        if (System.nanoTime() % 10L == 0L) {
            // Toast.makeText(App.context(), "Trying to take picture...", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Trying to get picture...")

            img?.let { image ->
                Log.d(TAG, "New image: $image")

                /*
                val bmp = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                val ostream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, ostream)

                LocalWebSocketServer.shared?.broadcast(ostream.toByteArray())
                 */

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

                val ostream = ByteArrayOutputStream()
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream)

                LocalWebSocketServer.shared?.broadcast(ostream.toByteArray())
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
        Log.d(TAG, "start")

        initCam(320, 200)
        //initCam(1, 1)
    }

    @SuppressLint("MissingPermission")
    private fun initCam(width: Int, height: Int) {
        // Toast.makeText(App.context(), "initCam", Toast.LENGTH_SHORT).show()

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


        previewSize = chooseSupportedSize(camId!!, width, height)

        cameraManager!!.openCamera(camId, stateCallback, null)
    }

    private fun chooseSupportedSize(
        camId: String,
        textureViewWidth: Int,
        textureViewHeight: Int
    ): Size {

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Get all supported sizes for TextureView
        val characteristics = manager.getCameraCharacteristics(camId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSizes = map?.getOutputSizes(SurfaceTexture::class.java)

        // We want to find something near the size of our TextureView
        val texViewArea = textureViewWidth * textureViewHeight
        val texViewAspect = textureViewWidth.toFloat() / textureViewHeight.toFloat()

        val nearestToFurthestSz = supportedSizes?.sortedWith(compareBy(
            // First find something with similar aspect
            {
                val aspect = if (it.width < it.height) it.width.toFloat() / it.height.toFloat()
                else it.height.toFloat() / it.width.toFloat()
                (aspect - texViewAspect).absoluteValue
            },
            // Also try to get similar resolution
            {
                (texViewArea - it.width * it.height).absoluteValue
            }
        ))

        if (nearestToFurthestSz!!.isNotEmpty())
            return nearestToFurthestSz[0]

        //return Size(320, 200)
        return Size(1, 1)
    }

    private fun createCaptureSession() {
        try {
            // Prepare surfaces we want to use in capture session
            val targetSurfaces = ArrayList<Surface>()

            // Prepare CaptureRequest that can be used with CameraCaptureSession
            val requestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {

                    // Configure target surface for background processing (ImageReader)
                    imageReader = ImageReader.newInstance(
                        previewSize!!.width, previewSize!!.getHeight(),
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
                            // Now we can start capturing
                            captureRequest = requestBuilder.build()
                            captureSession!!.setRepeatingRequest(
                                captureRequest!!,
//                                captureCallback,
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