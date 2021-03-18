package com.ckz.library.camera

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.ckz.library.camera.widget.TakeType
import java.security.AccessControlContext

/**
 *@packageName com.ckz.library.camera
 *@author kzcai
 *@date 2021/3/18
 */

typealias OnMediaTakeListener = (type:Int,path:String) -> Unit

class CameraPlugin {

    companion object {
        val instance: CameraPlugin by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CameraPlugin() }
    }

     val handler =  object :Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{
                    val map = msg.obj as HashMap<String,Any>
                    val path = map["path"] as String
                    val type = map["type"] as Int
                    listener?.invoke(type,path)
                }
            }
        }
    }


    fun toCameraPage(context:Context, showTime:Boolean= false, @TakeType.CameraType takeType:Int = TakeType.IMAGE_AND_VIDEO, listener:OnMediaTakeListener?):CameraPlugin{
        val intent = Intent(context,CameraPreviewActivity::class.java)
        intent.putExtra("showTime",showTime)
        intent.putExtra("takeType",takeType)
        context.startActivity(intent)
        this.listener = listener
        return this
    }

    private var listener:OnMediaTakeListener?=null
}