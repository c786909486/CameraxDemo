package com.ckz.library.camera.view

/**
 *@packageName com.ckz.library.camera.view
 *@author kzcai
 *@date 2021/3/11
 */
interface CameraControlView {

    //开始预览
    fun startCamera()

    //拍照
    fun takePhoto(path:String)

    //切换摄像头
    fun switchCamera()

    //开启/关闭闪光灯
    fun openFlash()

    //开始录像
    fun startVideo(path:String)

    //停止录像
    fun stopVideo()

    //点击变焦
    fun touchFocus(x:Float,y:Float)

    //双指放大/缩小
    fun zoom(isZoomIn:Boolean)

    fun onCreate()

    fun onResume()

    fun onPause()

    fun onDestroy()

}