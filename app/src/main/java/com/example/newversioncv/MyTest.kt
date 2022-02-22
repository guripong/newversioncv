package com.example.newversioncv

import android.app.Application
import com.example.newversioncv.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyTest : Application() {

    override fun onCreate() {
        super.onCreate()

        //모듈등록
        startKoin {

            //androidContext 로 Context를 주입함
            androidContext(this@MyTest)
            //선언한 모듈지정
            modules(viewModelModule)
        }
    }

}