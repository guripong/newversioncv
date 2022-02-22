package com.example.newversioncv.mlkit.vision.face_detection2

import android.R
import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.util.Log
import androidx.annotation.ColorInt
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.example.newversioncv.camerax.GraphicOverlay
import com.example.newversioncv.databinding.ActivityMainBinding
import com.example.newversioncv.util.imageToBitmap
import com.example.newversioncv.util.rotateFlipImage
import com.example.newversioncv.util.scaleImage
import com.example.newversioncv.util.toBitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import android.R.attr.y

import android.R.attr.x
import android.R.attr.y

import android.R.attr.x


class FaceContourGraphic2(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect,
    private val binding: ActivityMainBinding,
    private val ip: ImageProxy
) : GraphicOverlay.Graphic(overlay) {

    private val facePositionPaint: Paint
    private val greenPositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint
//    private val imageProxy2:ImageProxy

    init {
        val selectedColor = Color.WHITE
//        imageProxy2 = imageProxy

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        greenPositionPaint = Paint()
        greenPositionPaint.color = Color.GREEN

        idPaint = Paint()
        idPaint.color = selectedColor
        idPaint.textSize = ID_TEXT_SIZE



        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    private fun Canvas.drawFace(facePosition: Int, @ColorInt selectedColor: Int) {
        val contour = face.getContour(facePosition)
        val path = Path()
        contour?.points?.forEachIndexed { index, pointF ->
            if (index == 0) {
                path.moveTo(
                    translateX(pointF.x),
                    translateY(pointF.y)
                )
            }
            path.lineTo(
                translateX(pointF.x),
                translateY(pointF.y)
            )
        }
        val paint = Paint().apply {
            color = selectedColor
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
        drawPath(path, paint)
    }

    private var rectPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4.0f
    }

    //    @SuppressLint("UnsafeOptInUsageError")
    @SuppressLint("UnsafeOptInUsageError")
    override fun draw(canvas: Canvas?) {

        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )
        canvas?.drawRect(rect, boxPaint)

        val contours = face.allContours

        contours.forEach {
            it.points.forEach { point ->
                //모든점 흰색
                val px = translateX(point.x)
                val py = translateY(point.y)
//                canvas?.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint)
//                canvas?.drawCircle(point.x, point.y, FACE_POSITION_RADIUS, facePositionPaint)
                //이게 좌표변환 안된것임
            }
        }

        // face
//        canvas?.drawFace(FaceContour.FACE, Color.BLUE)

        //전면카메라의경우에는 이게 오른눈임
        val contour2 = face.getContour(FaceContour.LEFT_EYE)

        var x1: Float = 0f
        var x2: Float = 0f
        var y1: Float = 0f
        var y2: Float = 0f

        contour2?.points?.forEachIndexed { index, point ->
            val px = translateX(point.x)
            val py = translateY(point.y)
            if (index == 4) {
                //y좌표 위가 y2

                y2 = translateY(point.y) - 100f
            } else if (index == 12) {
                y1 = translateY(point.y) + 100f
            } else if (index === 0) {
                x2 = translateX(point.x) + 100f
                var temp = point.x
                Log.d("AAA","오초점:$temp")
                canvas?.drawCircle(px, py, FACE_POSITION_RADIUS, greenPositionPaint)
//                canvas?.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint)
            } else if (index == 8) {
                x1 = translateX(point.x) - 100f
                var temp = point.x
                Log.d("AAA","왼흰점:$temp")
                canvas?.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint)
            }
//            Log.d("AAA", "[index=$index] px:$px py:$py")
        }

        var tx2 = contour2?.points?.get(0)?.x?.let { translateX(it) }
        var ox2 = contour2?.points?.get(0)?.x
        var oy2 = contour2?.points?.get(4)?.y
        var ox1 = contour2?.points?.get(8)?.x
        var oy1 = contour2?.points?.get(12)?.y
        /*
        *
        *     oy2 ox1    ox2
        *
        *     oy1
        * */
//        binding.leftEyeView.setImageBitmap(bitmap)

        //오른눈 크롭임 ,, 앞쪽이라 왼쪽오른쪽눈이 바뀜
        Log.d("AAA", "[[box] x1:$x1 x2:$x2 y1:$y1 y2:$y2 ]")
        Log.d("AAA", "오리지날 좌표:ox2:$ox2 tx2:$tx2  ox1:$ox1  oy1:$oy1 oy2:$oy2")
        canvas?.drawRect(x1, y2, x2, y1, rectPaint);


        ip.image?.let {
//            Log.d("AAA", "들어오나 확인 $it")

            var bm = it.toBitmap()


//            var rd = ip.imageInfo.rotationDegrees
//            Log.d("AAA","rd 값: $rd")
//            var bm2 = bm.rotateFlipImage(rd.toFloat(),false)


            var bm2 = bm.rotateFlipImage(0f, true)
            var w = bm2?.width

            Log.d("AAA", "들어오나 확인 $it  bm2 width:$w")
            //grayversion


            if (bm2 != null && ox1 != null && oy2 != null && ox2 != null && oy1 != null) {

                var src = Mat()
                Utils.bitmapToMat(bm2, src)  //bitmap을 mat으로
                var x = 0
                var y = oy2 - 20
                var width = 480
//                var width = (ox2 - ox1) + 80 //좌우반전때매 뒤집혀버렸어
                var height = oy1 - oy2 + 40
                if(y<0){
                    y=0f
                }
                if(y+height>640){
                    height = 640 - y
                }

                Log.d("AAA","Rect전 x:$x y:$y width:$width height:$height")
                val roirect = org.opencv.core.Rect(
                    x.toInt(), y.toInt(), width.toInt(),
                    height.toInt()
                )
//                Log.d("AAA","Rect후 roirect:$roirect")
//                Log.d("AAA","Roi전")
                var dst = Mat(src, roirect)
//                Log.d("AAA","Roi후")
                val bitmap2 =
                    Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(dst, bitmap2)

                src.release()
                dst.release()

//                binding.leftEyeView.setImageBitmap(makeGray(bitmap2))
                binding.leftEyeView.setImageBitmap(makeGray(bm2))
                binding.rightEyeView.setImageBitmap(makeGray(bitmap2))
            }


        }


//        Log.d("AAA", contour2?.points.toString())


        // left eye
        canvas?.drawFace(FaceContour.LEFT_EYE, Color.RED)
//        canvas?.drawFace(FaceContour.LEFT_EYEBROW_TOP, Color.RED) //왼위눈썹
//        canvas?.drawFace(FaceContour.LEFT_EYEBROW_BOTTOM, Color.CYAN) //왼아래눈썹

        // right eye
        canvas?.drawFace(FaceContour.RIGHT_EYE, Color.RED)
//        canvas?.drawFace(FaceContour.RIGHT_EYEBROW_TOP, Color.GREEN) //오른위눈썹
//        canvas?.drawFace(FaceContour.RIGHT_EYEBROW_BOTTOM, Color.GRAY) //오른아래눈썹


//        // nose
//        canvas?.drawFace(FaceContour.NOSE_BOTTOM, Color.LTGRAY)
//        canvas?.drawFace(FaceContour.NOSE_BRIDGE, Color.MAGENTA)
//
//        // rip
//        canvas?.drawFace(FaceContour.LOWER_LIP_BOTTOM, Color.WHITE)
//        canvas?.drawFace(FaceContour.LOWER_LIP_TOP, Color.YELLOW)
//        canvas?.drawFace(FaceContour.UPPER_LIP_BOTTOM, Color.GREEN)
//        canvas?.drawFace(FaceContour.UPPER_LIP_TOP, Color.CYAN)
        ip.close()
        Log.d("AAA", "그래픽오버레이 그리기 끝")
        Log.d("AAA", "========================================")
    }

    fun makeGray(bitmap: Bitmap): Bitmap {
//        Log.d("AAA","makegray시작")
//        var src = Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1)
        var src = Mat()
        var dst = Mat()

//        Log.d("AAA","src,dst 끝")
        Utils.bitmapToMat(bitmap, src)
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY)

        //ALPHA_8
        //https://developer.android.com/reference/android/graphics/Bitmap.Config
        // Bitmap.Config 종류들

        val bitmap2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bitmap2)

        src.release()
        dst.release()
//        Log.d("AAA","makegray끝")
        return bitmap2
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 4.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}