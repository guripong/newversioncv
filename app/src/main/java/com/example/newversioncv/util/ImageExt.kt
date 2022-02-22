package com.example.newversioncv.util


import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

fun Image.imageToBitmap(): Bitmap? {
    val buffer = this.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}

//아래꺼는?
/*
*
만일 RGB 값이 주어졌을 경우, YUV 값은 =>

Y = 0.3R + 0.59G + 0.11B
U = (B-Y) x 0.493
V = (R-Y) x 0.877

반대로 YUV값이 주어졌을 경우, RGB값은 =>

R = Y + 0.956U + 0.621V
G = Y + 0.272U + 0.647V
B = Y + 1.1061U + 1.703V
* */
fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}