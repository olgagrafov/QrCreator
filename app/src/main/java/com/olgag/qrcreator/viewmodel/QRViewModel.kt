package com.olgag.qrcreator.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.activity.ComponentActivity
import com.olgag.qrcreator.qrgenearator.QRGContents
import com.olgag.qrcreator.qrgenearator.QRGEncoder

class QRViewModel(var context: Context) {
    var qrText: String ?= null
        get() = field
        set(value) { field = value }

    var bit: Bitmap ?= null
        get()  = field
        set(value) { field = value }

    fun CreateBitmapQR(): Bitmap {
        val width: Int
        val height: Int
        val windowManager: WindowManager =  context.getSystemService(ComponentActivity.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = windowManager.currentWindowMetrics
            width = metrics.bounds.width()
            height = metrics.bounds.height()
        } else {
            val display: Display = windowManager.defaultDisplay
            val point: Point = Point()
            display.getSize(point)
            width = point.x
            height = point.y
        }

        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4
        val qrEncoder = QRGEncoder(qrText, null, QRGContents.Type.TEXT, dimen)
        bit = qrEncoder.getBitmap()
        return (qrEncoder.getBitmap())
    }
}
