package com.example.newversioncv.ui.main
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.newversioncv.ui.main.MainViewModel
import com.example.newversioncv.R
import com.example.newversioncv.databinding.ActivityMainBinding
import com.example.newversioncv.ui.base.BaseActivity
import com.example.newversioncv.camerax.CameraManager
import com.example.newversioncv.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.lang.NullPointerException

class MainActivity : BaseActivity() {
//    private lateinit var binding: ActivityMainBinding
    private val binding by binding<ActivityMainBinding>(R.layout.activity_main)

    //lazy한 방식으로 의존성 주입
    private val viewModel: MainViewModel by viewModels()

    private lateinit var cameraManager: CameraManager

    //외부함수 c++함수
    external fun stringFromJNI(): String


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        var TAG="AAA"
        init {
            System.loadLibrary("newversioncv")
//            System.loadLibrary("native-lib")
            System.loadLibrary("opencv_java4");
            val isIntialized = OpenCVLoader.initDebug()
            if(isIntialized){
                Log.d(TAG, "OpenCV loaded");
            }
            else{
                Log.d(TAG, "Unable to load OpenCV");
            }
            Log.d(TAG, "isIntialized = $isIntialized")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createCameraManager()

        //강제 portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //코틀린 탑바 제거
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        binding.sampleText.text = stringFromJNI()

//        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        binding.lifecycleOwner = this
//        binding.viewModel = model

        binding.apply{
            lifecycleOwner = this@MainActivity
            viewModel = this@MainActivity.viewModel
            initViewModel()
        }

        if (allPermissionsGranted()) {
            cameraManager.startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun initViewModel() {

        /*
        viewModel.apply {
            onItemSelectedEvent.observe(::getLifecycle) {
                cameraManager.changeAnalyzer(it)
            }
            onFabButtonEvent.observe(::getLifecycle) {
                it?.let {
                    binding.fabFinder.transform()
                    cameraManager.changeCameraSelector()
                }
            }
            onShutterButtonEvent.observe(::getLifecycle) {
                it?.let { takePicture() }
            }
        }
        */

        viewModel.apply {
            onItemSelectedEvent.observe(::getLifecycle) {
                cameraManager.changeAnalyzer(it)
            }

            onShutterButtonEvent.observe(::getLifecycle) {
                it?.let {
                    takePicture()
                }

            }
            onHideImgViewEvent.observe(::getLifecycle){
                it?.let { hideImgView() }
            }
            onFabButtonEvent.observe(::getLifecycle) {
                it?.let {
                    binding.fabFinder.transform()
                    cameraManager.changeCameraSelector()
                }
            }
        }
    }



    private fun setBitmapToImgView(bitmap1:Bitmap){
        Log.d("AAA","setBitmapToImgView 안쪽시작 mainactivity")
        binding.imgView.visibility = View.VISIBLE;

//        Toast.makeText(this, "비트맵까진 잘옴", Toast.LENGTH_SHORT).show()

        Log.d("AAA","bitmap1: $bitmap1")
        val w: Int = bitmap1.width
        val h: Int = bitmap1.height
        Log.d("AAA", "bitmap1:width x height:$w x $h")
        binding.imgView.setImageBitmap(bitmap1)
    }

    private fun hideImgView(){
        Log.d("AAA","hideimgView 호출")
        binding.imgView.visibility = View.INVISIBLE;
        Toast.makeText(this, "hideimgView호출", Toast.LENGTH_SHORT).show()
    }

    private fun takePicture(){
        // shutter effect
        Toast.makeText(this, "사진찍을게", Toast.LENGTH_SHORT).show()
        setOrientationEvent()

        cameraManager.imageCapture.takePicture(
            cameraManager.cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi",
                    "UnsafeOptInUsageError"
                )
                override fun onCaptureSuccess(image: ImageProxy) {
                    Log.d("AAA","takePicture성공")
                    image.image?.let {
                          Log.d("AAA","takePicture안의 이미지프록시")
//                        imageToBitmapSaveGallery(it)

                        imgToBitmap(it)

                    }

                    image.close()
                    super.onCaptureSuccess(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.d("AAA","캡처실패")

                }
            })


        Log.d("AAA","takePicture끝")

    }

    private fun imgToBitmap(image: Image) {
        Log.d("AAA","imageToBitmap 호출")

        var abc=image.imageToBitmap()
            ?.rotateFlipImage(
                cameraManager.rotation,
                cameraManager.isFrontMode()
            )
            ?.scaleImage(
                binding.previewViewFinder,
                cameraManager.isHorizontalMode()
            )
            ?.let { bitmap ->


                Log.d("AAA","bitmap까지 뱉어냄 null 이아님")
//                val w: Int = bitmap.width
//                val h: Int = bitmap.height
//                Log.d("AAA", "width x height:$w x $h")
//                binding.setbitmap(bitmap)
                runOnUiThread {

                    setBitmapToImgView(makeGray(bitmap))
                }

            }

    }

    fun makeGray(bitmap: Bitmap) : Bitmap {
        Log.d("AAA","makegray시작")
//        var src = Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1)
        var src = Mat()
        var dst = Mat()

        Log.d("AAA","src,dst 끝")
        Utils.bitmapToMat(bitmap,src)
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY)

        //ALPHA_8
        //https://developer.android.com/reference/android/graphics/Bitmap.Config
        // Bitmap.Config 종류들

        val bitmap2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bitmap2)

        src.release()
        dst.release()
        Log.d("AAA","makegray끝")
        return bitmap2
    }

    private fun imageToBitmapSaveGallery(image: Image) {
        Log.d("AAA","여기에러났냐1")
        image.imageToBitmap()
            ?.rotateFlipImage(
                cameraManager.rotation,
                cameraManager.isFrontMode()
            )
            ?.scaleImage(
                binding.previewViewFinder,
                cameraManager.isHorizontalMode()
            )
            ?.let { bitmap ->
                Log.d("AAA","여기에러났냐2")
                binding.graphicOverlayFinder.processCanvas.drawBitmap(
                    bitmap,
                    0f,
                    bitmap.getBaseYByView(
                        binding.graphicOverlayFinder,
                        cameraManager.isHorizontalMode()
                    ),
                    Paint().apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                    })
                Log.d("AAA","여기에러났냐3")
                //갤러리 writing 권한 문제가 발생...
                binding.graphicOverlayFinder.processBitmap.saveToGallery(this@MainActivity)
            }
    }


    private fun setOrientationEvent() {
        val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation: Float = when (orientation) {
                    in 45..134 -> 270f
                    in 135..224 -> 180f
                    in 225..314 -> 90f
                    else -> 0f
                }
                cameraManager.rotation = rotation
            }
        }
        orientationEventListener.enable()
    }


    private fun createCameraManager() {
        //카메라매니저 생성자에 다른거도 넣어줘
        // 크롭할 뷰
        cameraManager = CameraManager(
            this,
            binding.previewViewFinder,
            this,
            binding.graphicOverlayFinder,
            binding,
        )
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        //슈퍼콜 왜 추가해야해?
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
//                cameraManager.startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }


}