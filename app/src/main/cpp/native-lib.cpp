#include <jni.h>
#include <string>


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_newversioncv_ui_main_MainActivity_stringFromJNI(JNIEnv* env,jobject /* this */) {
    std::string hello = "C++ 함수 콜 결과";
    return env->NewStringUTF(hello.c_str());
}
