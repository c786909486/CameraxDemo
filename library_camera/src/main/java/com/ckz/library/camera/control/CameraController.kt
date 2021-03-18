package com.ckz.library.camera.control

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Sensor
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.ckz.library.camera.CameraPreviewActivity
import com.ckz.library.camera.utils.FileSaveUtils
import com.ckz.library.camera.utils.ScreenUtils
import com.ckz.library.camera.utils.showToast
import com.ckz.library.camera.view.CameraControlView
import com.ckz.library.camera.view.CameraResultView
import java.io.File
import java.util.concurrent.Executors
import android.hardware.SensorManager
import android.os.Looper
import android.os.Message

import com.ckz.library.camera.listener.OrientationSensorListener
import kotlin.math.abs
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
import android.media.MediaRecorder
import android.util.Size
import com.ckz.library.camera.utils.ScreenOrientation
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import android.media.CamcorderProfile





/**
 *@packageName com.ckz.library.camera.control
 *@author kzcai
 *@date 2021/3/11
 */

typealias LumaListener = (luma: Double) -> Unit

class CameraController(private val activity: AppCompatActivity,private val mView: CameraResultView):CameraControlView {

    //相机实例
    private var camera: Camera? = null

    //图像捕获用例
    private var imageCapture:ImageCapture?=null

    //图像分析实例
    private var imageAnalyzer:ImageAnalysis?=null

    //录像实例
    private var videoCapture:VideoCapture?=null

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    //cameraView宽高
    private var preViewWidth = 0
    private var preViewHeight = 0
    //重力感应监听
    private var listener: OrientationSensorListener? = null
    //重力感应实例
    private var sm: SensorManager? = null
    private var sensor: Sensor? = null
    private var screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT

    private var cameraFacing = CameraSelector.LENS_FACING_BACK

    var savedFile:File?=null
    //拍摄类型 0照片 1视频
    var mediaType:Int = 0

    //判断屏幕方向
    private var handler: Handler = object :Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what === 888) {
                val orientation = msg.arg1
                if (orientation in 46..134) {
                    if (screenOrientation!=ScreenOrientation.SCREEN_ORIENTATION_REVERSE_LANDSCAPE){
                        screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        mView.onScreenChanged(ScreenOrientation.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                    }

                } else if (orientation in 136..224) {
                    if (screenOrientation!=ScreenOrientation.SCREEN_ORIENTATION_REVERSE_PORTRAIT){
                        screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        mView.onScreenChanged(ScreenOrientation.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
                    }
                } else if (orientation in 226..314) {
                    if (screenOrientation!=ScreenOrientation.SCREEN_ORIENTATION_LANDSCAPE){
                        screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_LANDSCAPE
                        mView.onScreenChanged(ScreenOrientation.SCREEN_ORIENTATION_LANDSCAPE)
                    }

                } else if (orientation in 316..359 || orientation in 1..44) {
                    if (screenOrientation!=ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT){
                        screenOrientation = ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT
                        mView.onScreenChanged(ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT)
                    }
                }
            }
        }
    }



    var previewFinder: PreviewView?=null
    set(value) {
        field = value
        value?.post {
            radio = getRadio()

        }
    }

    /**
     * @Description 初始化重力传感器
     * @Param
     * @return
     **/
    init {
        onCreate()
        sm = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sm!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener =  OrientationSensorListener(handler)
        sm?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
        mView.onFlashChanged(false)
    }

    private var radio = AspectRatio.RATIO_16_9


    /**
     * @Description 开始预览
     * @Param
     * @return
     **/
    @SuppressLint("RestrictedApi")
    override fun startCamera() {
        if (previewFinder==null){
            throw Exception("surfaceHolder is null")
        }
        mView.onCameraSwitched(cameraFacing)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetAspectRatio(radio)
                .build()
                .also {
                    it.setSurfaceProvider(previewFinder?.surfaceProvider)
                }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(radio)
                .setFlashMode(FLASH_MODE_AUTO)
                .build()

//            val mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P)

//            Log.d("videoPreviewSize","\nwidth:${mProfile.videoFrameWidth}\nheight:${mProfile.videoFrameHeight}\nscreenWidth:${preViewWidth}\nscreenHeight:${preViewHeight}")
            videoCapture = VideoCapture.Builder()
                .setTargetAspectRatio(radio)
//                .setVideoFrameRate(25)
//                //bit率  越大视频体积越大
                .setBitRate(3 * 1024 * 1024)
//                .setTargetResolution(Size(1148,2480))
//                .setMaxResolution(Size(preViewWidth,preViewHeight))
//                .setAudioRecordSource(MediaRecorder.AudioSource.MIC)//设置音频源麦克风
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(radio)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
//                        Log.d("imageInfoData", "Average luminosity: $luma")
                    })
                }


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    activity, cameraSelector, preview,imageCapture,videoCapture)


            } catch(exc: Exception) {
                Log.e(CameraPreviewActivity.TAG, "Use case binding failed", exc)
            }


        }, ContextCompat.getMainExecutor(activity))

    }

    /**
     * @Description 拍照
     * @Param
     * @return
     **/
    override fun takePhoto(path:String) {

        val file = File(path)
        if (file.parentFile?.exists() != true){
            file.parentFile?.mkdir()
        }
        val imageFile = activity.getExternalFilesDir(null)?.absolutePath+"/image_temp.jpg"
        val options = ImageCapture.OutputFileOptions.Builder(File(imageFile)).build()
        imageCapture?.takePicture(options,cameraExecutor,object :ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bitmap = FileSaveUtils.fileToBitmap(imageFile,getDegree(imageFile),cameraFacing==CameraSelector.LENS_FACING_FRONT,
                screenOrientation==ScreenOrientation.SCREEN_ORIENTATION_REVERSE_LANDSCAPE || screenOrientation==ScreenOrientation.SCREEN_ORIENTATION_LANDSCAPE)
                val resultBm = FileSaveUtils.clipFullBitmap(activity,bitmap,preViewWidth,preViewHeight)
                val file = FileSaveUtils.bitmap2File(resultBm,path)
                savedFile = file
                activity.runOnUiThread {
                    mediaType = 0
                    mView.onTakePhotoSuccess(path)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                activity.runOnUiThread {
                    activity.showToast(exception.toString())
                }
            }

        })
    }

    /**
     * @Description 切换相机
     * @Param
     * @return
     **/
    override fun switchCamera() {
        when(cameraFacing){
            CameraSelector.LENS_FACING_FRONT->{
                cameraFacing = CameraSelector.LENS_FACING_BACK
            }
            CameraSelector.LENS_FACING_BACK->{
                cameraFacing = CameraSelector.LENS_FACING_FRONT
            }
        }

        startCamera()
    }

    override fun openFlash() {
        val cameraInfo = camera?.cameraInfo
        val cameraControl = camera?.cameraControl

        //获取闪光灯开启还是关闭 0：关，1：开
        val touchState = cameraInfo?.torchState?.value

        if (touchState == TorchState.ON){
            cameraControl?.enableTorch(false)
            mView.onFlashChanged(false)
        } else {
            cameraControl?.enableTorch(true)
            mView.onFlashChanged(true)
        }

    }


    @SuppressLint("RestrictedApi")
    override fun startVideo(path:String) {
        val file = File(path)
        val options = VideoCapture.OutputFileOptions.Builder(file).build()
        videoCapture?.startRecording(options,cameraExecutor,object :VideoCapture.OnVideoSavedCallback{
            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                savedFile = File(path)
                activity.runOnUiThread {
                    mediaType = 1
                    mView.onVideoSuccess(path)
                }
            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                Log.e("videoError",message)
            }

        })
    }

    @SuppressLint("RestrictedApi")
    override fun stopVideo() {
        videoCapture?.stopRecording()
    }

    //点击对焦
    override fun touchFocus(x: Float, y: Float) {
        val factory = previewFinder?.meteringPointFactory
        val point = factory?.createPoint(x, y)!!
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        val future = camera?.cameraControl?.startFocusAndMetering(action)!!
        future.addListener({
            try {
                val result = future.get()!!
                if (result.isFocusSuccessful){
                    //对焦成功
                    Log.d("focusResult","success")

                }else{
                    //对焦失败
                    Log.d("focusResult","fail")
                }
            }catch (e:Exception){
                //对焦失败
                Log.d("focusResult",e.toString())
            }
        },cameraExecutor)

    }

   override fun zoom(isZoomIn:Boolean){
        val control = camera?.cameraControl
        val zoomState = camera?.cameraInfo?.zoomState!!
        val zoomRatio = zoomState.value?.zoomRatio!!
        val maxZoomRatio = zoomState.value?.maxZoomRatio!!
        val minZoomRatio = zoomState.value?.minZoomRatio!!
        if (isZoomIn){
            if (zoomRatio < maxZoomRatio) {
                control?.setZoomRatio((zoomRatio + 0.1).toFloat())
            }
        }else{
            if (zoomRatio > minZoomRatio) {
                control?.setZoomRatio( (zoomRatio - 0.1).toFloat());
            }
        }
    }

    override fun onCreate() {

    }

    override fun onResume() {
        sm?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        sm?.unregisterListener(listener)
    }

    override fun onDestroy() {
        release()
    }


   private fun release(){
        cameraExecutor?.shutdown()
    }

    /**
     * @Description 获取宽高比
     * @Param
     * @return
     **/
    private fun getRadio():Int{
        preViewHeight = previewFinder!!.height
        preViewWidth = previewFinder!!.width
        val screenRatio = previewFinder!!.height / (previewFinder!!.width * 1.0f);
        if (abs(screenRatio - 4.0 / 3.0) <= abs(screenRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9

    }

    private fun getDegree(filePath:String):Float{
        val imageDegree = FileSaveUtils.getOrientationDescription(filePath)
        when(screenOrientation){
            ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT->{
                return imageDegree.toFloat()
            }

            ScreenOrientation.SCREEN_ORIENTATION_LANDSCAPE->{
                return (imageDegree-90).toFloat()
            }

            ScreenOrientation.SCREEN_ORIENTATION_REVERSE_PORTRAIT->{
                return (imageDegree-180).toFloat()
            }

            ScreenOrientation.SCREEN_ORIENTATION_REVERSE_LANDSCAPE->{
                return (imageDegree+90).toFloat()
            }
        }
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()
            val data = ByteArray(remaining())
            get(data)
            return data
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

     fun cancelFile(){
        if (savedFile!=null&&savedFile!!.exists()){
            savedFile!!.delete()
            savedFile = null
        }
    }
}