package com.example.newversioncv.mlkit.vision.face_detection2

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.example.newversioncv.camerax.BaseImageAnalyzer
import com.example.newversioncv.camerax.BaseImageAnalyzer2
import com.example.newversioncv.camerax.GraphicOverlay
import com.example.newversioncv.databinding.ActivityMainBinding
import com.example.newversioncv.util.imageToBitmap
import com.example.newversioncv.util.toBitmap
import java.io.IOException


typealias MyListener2 = (something: Double) -> Unit

class FaceContourDetectionProcessor2(private val view: GraphicOverlay,
                                     private val binding:ActivityMainBinding,
                                     private val listener: MyListener2) :
    BaseImageAnalyzer2<List<Face>>() {

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)
//    private val ii2

    override val graphicOverlay: GraphicOverlay
        get() = view
    override fun detectInImage(image: InputImage): Task<List<Face>> {
//        ii2 = image

        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }
    lateinit var ip :ImageProxy

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy){
        val mediaImage = imageProxy.image
        ip = imageProxy
//        var bm = mediaImage?.imageToBitmap()
//        var bm = mediaImage?.toBitmap()
//        var w= bm?.width
//        var mw=mediaImage?.width
//        var rd = imageProxy.imageInfo.rotationDegrees
//        Log.d("AAA","mw:$mw")
//        Log.d("AAA","w:$w")
//        Log.d("AAA","rd:$rd")

        mediaImage?.let {
            var inputimg=InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            var w=inputimg.width
            var h=inputimg.height
            Log.d("AAA","inputimg.width:$w  inputimg.height $h")
            //InputImage


            detectInImage(inputimg)
                .addOnSuccessListener { results ->
//                    var bbm=inputimg.bitmapInternal
//                    var w2 = bbm?.width
                    Log.d("AAA","그래픽 오버레이 호출전")
                    onSuccess(
                        results,
                        graphicOverlay,
                        it.cropRect,
                        ip
                    )
                }
                .addOnFailureListener {
                    graphicOverlay.clear()
                    graphicOverlay.postInvalidate()
                    onFailure(it)
                }
                .addOnCompleteListener {
                    Log.d("AAA","그래픽오버레이끝남")
//                    ip.close()
                //                    imageProxy.close()
                }

        }

        Log.d("AAA","어널라이즈끝")
    }

    override fun onSuccess(
        results: List<Face>,
        graphicOverlay: GraphicOverlay,
        rect: Rect,
        ip : ImageProxy
    ) {
        graphicOverlay.clear()

        //얼굴이 2개이상인경우 예외처리가 필요함
        when {
            results.size==1 -> {
                val faceGraphic = FaceContourGraphic2(graphicOverlay, results.first(), rect,binding , ip)
                graphicOverlay.add(faceGraphic)


                /*
                results.forEach {
                    val faceGraphic = FaceContourGraphic2(graphicOverlay, it, rect,binding)
                    graphicOverlay.add(faceGraphic)
                }*/

                listener(100.0)
            }
            results.size>=2 -> {
                ip.close()
                listener(50.0)
            }
            else -> {
                ip.close()
                listener(1.0)
            }
        }

        graphicOverlay.postInvalidate()


    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }

}