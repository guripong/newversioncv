package com.example.newversioncv.util


import android.os.Environment
import java.io.File

val rootFolder =
    File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        "Macgyver${File.separator}"
    ).apply {
        if (!exists())
            mkdirs()
    }

fun makeTempFile(): File = File.createTempFile("${System.currentTimeMillis()}", ".png", rootFolder)