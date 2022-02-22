package com.example.newversioncv.camerax

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.ScaleGestureDetector

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

import androidx.lifecycle.LifecycleOwner
import com.example.newversioncv.databinding.ActivityMainBinding

import com.example.newversioncv.mlkit.vision.VisionType
//import com.example.newversioncv.mlkit.vision.barcode_scan.BarcodeScannerProcessor
import com.example.newversioncv.mlkit.vision.face_detection.FaceContourDetectionProcessor
import com.example.newversioncv.mlkit.vision.face_detection2.FaceContourDetectionProcessor2
import java.nio.ByteBuffer
//import com.example.newversioncv.mlkit.vision.object_detection.ObjectDetectionProcessor
//import com.example.newversioncv.mlkit.vision.text_recognition.TextRecognitionProcessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(private val context: Context,
                    private val finderView: PreviewView,
                    private val lifecycleOwner: LifecycleOwner,
                    private val graphicOverlay: GraphicOverlay,
                    private val binding: ActivityMainBinding,
) {

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val TAG = "CameraXBasic"
    }

    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null

    // default barcode scanner
    private var analyzerVisionType: VisionType = VisionType.Face

    lateinit var cameraExecutor: ExecutorService
    lateinit var imageCapture: ImageCapture
    lateinit var metrics: DisplayMetrics

    var rotation: Float = 0f
    var cameraSelectorOption = CameraSelector.LENS_FACING_FRONT  //LENS_FACING_FRONT , LENS_FACING_BACK

    init {
        createNewExecutor()
    }


    private fun selectAnalyzer(): ImageAnalysis.Analyzer {
        return when (analyzerVisionType) {
//            VisionType.Object -> ObjectDetectionProcessor(graphicOverlay)
//            VisionType.OCR -> TextRecognitionProcessor(graphicOverlay)

            VisionType.Face2 -> FaceContourDetectionProcessor2(graphicOverlay,binding){ something->
                Log.d("AAA","Face2: $something")
                //얼굴보일때만
                //우상단 좌측눈?

            }
            VisionType.Face -> FaceContourDetectionProcessor(graphicOverlay)
            VisionType.Guri ->MyAnalyzer(graphicOverlay) { luma ->

                //실시간임.. 임시로 하지마
                Log.d("AAA", "ㅎㅎㅎ: $luma")
            }

//            VisionType.Barcode -> BarcodeScannerProcessor(graphicOverlay)
        }
    }
    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setCameraConfig(
        cameraProvider: ProcessCameraProvider?,
        cameraSelector: CameraSelector
    ) {
        try {
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )
            preview?.setSurfaceProvider(
                finderView.surfaceProvider
//                finderView.createSurfaceProvider()
            )


        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }



    private fun setUpPinchToZoom() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(context, listener)
        finderView.setOnTouchListener { _, event ->
            finderView.post {
                scaleGestureDetector.onTouchEvent(event)
            }
            return@setOnTouchListener true
        }
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            Runnable {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build()

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {

                        /*
                        it.setAnalyzer(cameraExecutor, MyAnalyzer { luma ->

                            //실시간임.. 임시로 하지마
                            Log.d("AAA", "ㅎㅎㅎ: $luma")
                        })
                        */
                    /*
                        it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->

                            //실시간임.. 임시로 하지마
                            Log.d("AAA", "Average luminosity: $luma")
                        })*/
                       it.setAnalyzer(cameraExecutor, selectAnalyzer())

                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()

                metrics =  DisplayMetrics().also { finderView.display.getRealMetrics(it) }

                Log.d("AAA","metrics.widthPidexls:$metrics.widthPixels")
                //1600x2560  dpi가286 으로 뱉어냄


                imageCapture =
                    ImageCapture.Builder()
                        .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
//                        .setTargetResolution(Size(1280,720))
                        .build()

                setUpPinchToZoom()
                setCameraConfig(cameraProvider, cameraSelector)

            }, ContextCompat.getMainExecutor(context)
        )
    }

    fun changeCameraSelector() {
        cameraProvider?.unbindAll()
        cameraSelectorOption =
            if (cameraSelectorOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
        graphicOverlay.toggleSelector()
        startCamera()
    }

    fun changeAnalyzer(visionType: VisionType) {
        if (analyzerVisionType != visionType) {
            cameraProvider?.unbindAll()
            analyzerVisionType = visionType
            startCamera()
        }
    }

    fun isHorizontalMode() : Boolean {
        return rotation == 90f || rotation == 270f
    }

    fun isFrontMode() : Boolean {
        return cameraSelectorOption == CameraSelector.LENS_FACING_FRONT
    }

}

//////////////

typealias LumaListener = (luma: Double) -> Unit

private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

//            val bm = BitmapFactory.decodeByteArray(data, 0, data.size)
//            val dm = DisplayMetrics()


//            imgView.setMinimumHeight(dm.heightPixels)
//            imgView.setMinimumWidth(dm.widthPixels)
//            imgView.setImageBitmap(bm)

        listener(luma)

        image.close()
    }
}



