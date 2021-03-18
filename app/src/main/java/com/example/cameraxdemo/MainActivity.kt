package com.example.cameraxdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.ckz.library.camera.CameraPlugin
import com.ckz.library.camera.CameraPreviewActivity
import com.ckz.library.camera.OnMediaTakeListener
import com.ckz.library.camera.utils.showToast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(
            WindowManager.LayoutParams. FLAG_FULLSCREEN ,
            WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main)
    }

    fun openCamera(view:View){
//        startActivityForResult(Intent(this,CameraPreviewActivity::class.java),200)
        CameraPlugin.instance.toCameraPage(this,false,listener = object :OnMediaTakeListener{
            override fun invoke(type: Int, path: String) {
                showToast(path)
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== RESULT_OK&&requestCode==200){
            val path = data?.getStringExtra("imagePath")
            Glide.with(this).load(path).into(iv_image)
        }
    }
}