package com.example.newversioncv.ui.main

import android.graphics.Bitmap
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.isInvisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.newversioncv.R
import com.example.newversioncv.mlkit.vision.VisionType

class MainViewModel : ViewModel(){
    private var number = MutableLiveData<Int>()

    val onShutterButtonEvent: MutableLiveData<Unit?> = MutableLiveData()
    val onHideImgViewEvent:  MutableLiveData<Unit?> = MutableLiveData()
    val onFabButtonEvent: MutableLiveData<Unit?> = MutableLiveData()
    val onItemSelectedEvent: MutableLiveData<VisionType> = MutableLiveData()

    init {
        number.value = 0

    }


    fun increase() {
        number.value = number.value?.plus(1)
    }
    fun getNumber(): MutableLiveData<Int> {
        return number
    }

    fun onGuriClicked(view: View) {
         postVisionType(VisionType.Guri)
        number.value=1
    }

    fun onFaceClicked(view: View) {
        postVisionType(VisionType.Face)
        number.value=0
    }
    fun onFace2Clicked(view: View) {
        postVisionType(VisionType.Face2)
        number.value=2
    }

    fun onClickFabButton(view: View) {
        onFabButtonEvent.postValue(Unit)
    }



    fun onHideImgView(imgview:View){
        Log.d("AAA","뷰모델 onHideImgView");
//        imgview.visibility = View.INVISIBLE;
        onHideImgViewEvent.postValue(Unit)
    }

    private fun postVisionType(type: VisionType) {
        onItemSelectedEvent.postValue(type)
    }

    fun onClickShutter(view: View) {
        onShutterButtonEvent.postValue(Unit)
    }


}