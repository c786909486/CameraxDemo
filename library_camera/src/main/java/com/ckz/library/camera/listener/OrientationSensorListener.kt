package com.ckz.library.camera.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Handler
import android.view.OrientationEventListener.ORIENTATION_UNKNOWN
import kotlin.math.atan2

/**
 *@packageName com.ckz.library.camera.listener
 *@author kzcai
 *@date 2021/3/16
 */
class OrientationSensorListener:SensorEventListener {

    private val _DATA_X = 0
    private val _DATA_Y = 1
    private val _DATA_Z = 2
    private var rotateHandler: Handler? = null

    constructor(rotateHandler: Handler?) {
        this.rotateHandler = rotateHandler
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val values = event!!.values
        var orientation: Int = ORIENTATION_UNKNOWN
        val X = -values[_DATA_X]
        val Y = -values[_DATA_Y]
        val Z = -values[_DATA_Z]
        val magnitude = X * X + Y * Y
        // Don't trust the angle if the magnitude is small compared to the y value
        // Don't trust the angle if the magnitude is small compared to the y value
        if (magnitude * 4 >= Z * Z) {
            val OneEightyOverPi = 57.29577957855f
            val angle = atan2(-Y.toDouble(), X.toDouble()).toFloat() * OneEightyOverPi
            orientation = 90 - Math.round(angle)
            // normalize to 0 - 359 range
            while (orientation >= 360) {
                orientation -= 360
            }
            while (orientation < 0) {
                orientation += 360
            }
        }

        if (rotateHandler != null) {
            rotateHandler!!.obtainMessage(888, orientation, 0).sendToTarget()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}