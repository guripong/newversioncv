package com.example.newversioncv.camerax

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.newversioncv.util.imageToBitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

abstract class BaseImageAnalyzer<T> : ImageAnalysis.Analyzer{

    abstract val graphicOverlay: GraphicOverlay

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image


        mediaImage?.let {
            var inputimg=InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            //InputImage

//            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)

            var bitmap=mediaImage.imageToBitmap()

//            Log.d("AAA"," Bytesbuffer?: $bytes")

            //img.bitmapInternal
            //클래스들의 이해가 필요  image  class는 cropimage 가능함


            detectInImage(InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees))
                .addOnSuccessListener { results ->
                    onSuccess(
                        results,
                        graphicOverlay,
                        it.cropRect,
                    )
                }
                .addOnFailureListener {
                    graphicOverlay.clear()
                    graphicOverlay.postInvalidate()
                    onFailure(it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    abstract fun stop()

    protected abstract fun detectInImage(image: InputImage): Task<T>


    //여기에 원본이미지도 주는게 어떨까? 변경해서 해볼것
    protected abstract fun onSuccess(
        results: T,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    )

    protected abstract fun onFailure(e: Exception)

}