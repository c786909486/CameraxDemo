package com.ckz.library.camera.utils

import android.content.Context
import android.widget.Toast

/**
 *@packageName com.ckz.library.camera.utils
 *@author kzcai
 *@date 2021/3/11
 */
fun Context.showToast(msg:String){
    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}