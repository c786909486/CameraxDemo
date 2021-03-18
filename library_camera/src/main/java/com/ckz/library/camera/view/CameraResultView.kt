package com.ckz.library.camera.view

import com.ckz.library.camera.utils.ScreenOrientation

/**
 *@packageName com.ckz.library.camera.view
 *@author kzcai
 *@date 2021/3/11
 */
interface CameraResultView {

    fun onTakePhotoSuccess(path:String)

    fun onVideoSuccess(path:String)

    fun onScreenChanged(orientation: ScreenOrientation)

    fun onCameraSwitched(cameraFacing:Int)

    fun onFlashChanged(isOpen:Boolean)
}