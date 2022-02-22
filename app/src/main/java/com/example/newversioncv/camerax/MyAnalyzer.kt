package com.example.newversioncv.camerax

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer


typealias MyListener = (luma: Double) -> Unit

class MyAnalyzer(private val view: GraphicOverlay,private val listener: MyListener) : ImageAnalysis.Analyzer {


    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    private var rectPaint = Paint().apply {
        color = TEXT_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }


    override fun analyze(image: ImageProxy) {

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        view.processCanvas.drawRect(20.0F, 20.0f, 20.0f, 20.0f, rectPaint);

//        Log.d("AAA","야야야")
        val luma = pixels.average()
//            val bm = BitmapFactory.decodeByteArray(data, 0, data.size)
//            val dm = DisplayMetrics()


//            imgView.setMinimumHeight(dm.heightPixels)
//            imgView.setMinimumWidth(dm.widthPixels)
//            imgView.setImageBitmap(bm)
        view.clear()
        view.postInvalidate()

        listener(luma)
//        listener(35.0)

        image.close()
    }


        companion object {
        private const val TEXT_COLOR = Color.RED
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
        private const val ROUND_RECT_CORNER = 8f
    }

}

