package com.ckz.library.camera

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.ckz.library.camera.control.CameraController
import com.ckz.library.camera.utils.DisplayUtils
import com.ckz.library.camera.utils.ScreenOrientation
import com.ckz.library.camera.utils.ScreenUtils
import com.ckz.library.camera.view.CameraResultView
import com.ckz.library.camera.widget.CameraTouchView
import com.ckz.library.camera.widget.TakePhotoButton
import com.ckz.library.camera.widget.TakeType
import com.ckz.library.camera.widget.TextureVideoPlayer
import kotlinx.android.synthetic.main.activity_camera_preview.*

/**
 *@packageName com.ckz.library.camera
 *@author kzcai
 *@date 2021/3/11
 */
class CameraPreviewActivity:AppCompatActivity(), CameraResultView {

    companion object{
        val TAG = "CameraPreviewActivity"
    }


    private lateinit var controller: CameraController

    @TakeType.CameraType
    private  var cameraType:  Int = TakeType.IMAGE_AND_VIDEO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            getWindow().setAttributes(lp);
        };

        setContentView(R.layout.activity_camera_preview)
        controller = CameraController(this,this)
        controller.previewFinder = preview_finder
        initView()
        startCamera()
        click()
    }

    private fun startCamera(){
        controller.startCamera()
    }

    private fun initView(){
        marginStatusBar(iv_camera_back,topMargin = 40)
        marginStatusBar(iv_switch_camera,topMargin = 25)
        marginStatusBar(btn_finish,bottomMargin = 20)
        marginStatusBar(btn_take_photo,bottomMargin = 20)
    }

    private fun marginStatusBar(view:View,topMargin:Int=0,bottomMargin:Int = 0){
        val params = view.layoutParams as RelativeLayout.LayoutParams
        val statusBarSize = ScreenUtils.getStatusHeight(this)
        val bottomBarSize = ScreenUtils.getNavigationBarHeight(this)
        if (topMargin!=0){
            params.topMargin = statusBarSize+DisplayUtils.dp2px(this,topMargin.toFloat())
        }
        if (bottomMargin!=0){
            params.bottomMargin = bottomBarSize+DisplayUtils.dp2px(this,bottomMargin.toFloat())
        }

        Log.d("bottomBarSize",bottomBarSize.toString())
        view.layoutParams = params
    }

    /**
     * @Description 点击事
     * @Param
     * @return
     **/
    private fun click(){
        btn_take_photo.setType(cameraType)
        btn_take_photo.setOnProgressTouchListener(object :TakePhotoButton.OnProgressTouchListener{
            override fun onClick(photoButton: TakePhotoButton?) {
                val path = getExternalFilesDir(null)?.absolutePath
                val fileName = System.currentTimeMillis().toString()+"_Img.png"
                controller.takePhoto("$path/${fileName}")
            }

            override fun onLongClick(photoButton: TakePhotoButton?) {
                photoButton?.start()
                val path = getExternalFilesDir(null)?.absolutePath
                val fileName = System.currentTimeMillis().toString()+"_Img.mp4"
                controller.startVideo("$path/${fileName}")
            }

            override fun onFinish() {
                controller.stopVideo()
            }

        })
//        btn_take_photo.setOnClickListener {
//
//        }

        iv_switch_camera.setOnClickListener {
            controller.switchCamera()
//            controller.openFlash()
        }

        iv_camera_back.setOnClickListener {
            onBackPressed()
        }

        btn_finish.setOnClickListener {
            val path = controller.savedFile!!.absolutePath
            val msg = Message()
            val map = HashMap<String,Any>()
            map["path"] = path
            map["type"] = controller.mediaType
            msg.what = 1
            msg.obj = map
            CameraPlugin.instance.handler.sendMessage(msg)
            finish()
        }

        touch_view.setOnViewTouchListener(object :CameraTouchView.OnViewTouchListener{
            override fun handleFocus(x: Float, y: Float) {
                controller.touchFocus(x,y)
            }

            override fun handleZoom(zoom: Boolean) {
                controller.zoom(zoom)
            }

        })
        video_player.setVideoMode(TextureVideoPlayer.CENTER_MODE)
        video_player.setOnVideoPlayingListener(object :TextureVideoPlayer.OnVideoPlayingListener{
            override fun onVideoSizeChanged(vWidth: Int, vHeight: Int) {

            }

            override fun onStart() {

            }

            override fun onPlaying(duration: Int, percent: Int) {

            }

            override fun onPause() {

            }

            override fun onRestart() {

            }

            override fun onPlayingFinish() {

            }

            override fun onTextureDestory() {
                video_player.stop()
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.onDestroy()
        video_player.stop()
    }

    override fun onResume() {
        super.onResume()
        controller.onResume()
        video_player.pause()
    }

    override fun onPause() {
        super.onPause()
        controller.onPause()
        video_player.pause()
    }

    override fun onTakePhotoSuccess(path: String) {
//        iv_photo.visibility = View.VISIBLE
//        preview_finder.visibility =View.GONE
//        touch_view.visibility = View.GONE
        isInPreview(false,false)
        val bitmap = BitmapFactory.decodeFile(path)
        iv_photo.setImageBitmap(bitmap)
//        Glide.with(this).load(path).into(iv_photo)

    }

    override fun onVideoSuccess(path: String) {
        video_player.setUrl(path)
        video_player.play()
        isInPreview(false,true)

    }

    override fun onScreenChanged(orientation: ScreenOrientation) {
         when(orientation){
            ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT-> {
                rotationView(0f)
            }
            ScreenOrientation.SCREEN_ORIENTATION_LANDSCAPE->{
                rotationView(90f)
            }
            ScreenOrientation.SCREEN_ORIENTATION_REVERSE_PORTRAIT->{
                rotationView(180f)
            }
            ScreenOrientation.SCREEN_ORIENTATION_REVERSE_LANDSCAPE->{
                rotationView(-90f)
            }
        }

    }

    private fun rotationView(degree:Float){
        createAni(iv_switch_camera,
            if (iv_switch_camera.tag==null) 0f else iv_switch_camera.tag as Float,degree)
    }

    private fun createAni(view:View,startDegree:Float,degree: Float){
        view.pivotX = view.width.toFloat()/2;
        view.pivotY = view.height.toFloat()/2;
        val animator= ValueAnimator.ofFloat(startDegree,degree);
        animator.duration = 300;
        animator.addUpdateListener( ValueAnimator.AnimatorUpdateListener() {
            view.rotation = it.animatedValue as Float;
            view.tag = it.animatedValue as Float
        });
        animator.start()
    }

    override fun onCameraSwitched(cameraFacing: Int) {

    }

    override fun onFlashChanged(isOpen: Boolean) {

    }

    override fun onBackPressed() {
        if (iv_photo.visibility == View.VISIBLE||rl_video.visibility==View.VISIBLE){
//            iv_photo.visibility = View.GONE
//            touch_view.visibility = View.VISIBLE
//            preview_finder.visibility =View.VISIBLE
            isInPreview(true,false)
        }else{
            super.onBackPressed()
        }
    }

    private fun isInPreview(isIn:Boolean,showVideo:Boolean = false){
        iv_photo.visibility = if (isIn||showVideo) View.GONE else View.VISIBLE
        touch_view.visibility = if (isIn) View.VISIBLE else View.GONE
//        preview_finder.visibility = if (isIn) View.VISIBLE else View.GONE
        rl_video.visibility = if (isIn||!showVideo) View.GONE else View.VISIBLE
        iv_switch_camera.visibility = if (isIn) View.VISIBLE else View.GONE
        btn_take_photo.visibility = if (isIn) View.VISIBLE else View.GONE
        iv_camera_back.visibility = if (isIn) View.GONE else View.VISIBLE
        btn_finish.visibility = if (isIn) View.GONE else View.VISIBLE

        if (isIn){
//            controller.startCamera()
            video_player.stop()
            controller.cancelFile()
        }
    }
}