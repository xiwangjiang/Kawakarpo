package com.yeutsau.kawakarpo.core.testing

class NativeLib {

    /**
     * A native method that is implemented by the 'testing' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'testing' library on application startup.
        init {
            System.loadLibrary("testing")
        }
    }
}