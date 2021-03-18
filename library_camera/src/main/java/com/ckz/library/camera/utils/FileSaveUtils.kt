package com.ckz.library.camera.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.utils.Exif
import androidx.camera.core.internal.compat.workaround.ExifRotationAvailability
import androidx.camera.core.internal.utils.ImageUtil
import androidx.camera.core.internal.utils.ImageUtil.CodecFailedException
import androidx.camera.core.internal.utils.ImageUtil.CodecFailedException.FailureType
import java.io.*
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import android.graphics.Bitmap
import android.media.ExifInterface
import android.util.Log
import android.graphics.BitmapFactory
import android.widget.HorizontalScrollView


/**
 *@packageName com.ckz.library.camera.utils
 *@author kzcai
 *@date 2021/3/12
 */
object FileSaveUtils {

    fun image2Bitmap(image: Image): Bitmap? {
        val yBuffer = image.planes[0].buffer // Y
        val vuBuffer = image.planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /**
     * @Description 文件转换bitmap
     * @Param
     * @return
     **/
    fun fileToBitmap(filePath: String,degree:Float,mirror:Boolean = false,isHorizontal:Boolean = false): Bitmap {
//        val degree = getOrientationDescription(filePath)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inSampleSize = 2
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(filePath,options)
        val matrix = Matrix()
        matrix.setRotate(degree)
        if (mirror){
            if (isHorizontal){
                matrix.postScale(1f, -1f);   //镜像垂直翻转
            }else{
                matrix.postScale(-1f, 1f);   //镜像水平翻转
            }
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    fun clipFullBitmap(context: Context, bitmap: Bitmap, width: Int, height: Int): Bitmap {
        Log.d("clipSize", "width:${width};height:${height}")
        val sw = width
        val sh = height
        val screenRadio = sh.toFloat() / sw.toFloat()
        val bitmapRadio = bitmap.height.toFloat() / bitmap.width.toFloat()
//        Log.d("bitmapSize","width:${bitmap.width}\nheight:${bitmap.height}")
        if (bitmap.width<bitmap.height){
            var top = 0
            var bottom = bitmap.height
            var middle = bitmap.width.toFloat() / 2
            val pw = bitmap.height.toFloat() / screenRadio
            var left = (middle - pw / 2).toInt()
            var right = (middle + pw / 2).toInt()

            if (bitmapRadio>screenRadio){
                left = 0
                right = bitmap.width
                middle = bitmap.height.toFloat()/2
                val ph = bitmap.width.toFloat()*screenRadio
                top = (middle-ph/2).toInt()
                bottom = (middle+ph/2).toInt()
            }

            return Bitmap.createBitmap(bitmap, if (left<0) 0 else left, top, right-left, bottom-top)
        }else{
            var left = 0
            var right = bitmap.width
            var middle = bitmap.height.toFloat() / 2
            val ph = bitmap.width.toFloat() / screenRadio
            var top = (middle - ph / 2).toInt()
            var bottom = (middle + ph / 2).toInt()

            if (1f/bitmapRadio>screenRadio){
                top = 0
                bottom = bitmap.height
                middle = bitmap.width.toFloat()/2
                val pw = screenRadio*bitmap.height.toFloat()
                left = (middle-pw/2).toInt()
                right = (middle+pw/2).toInt()
            }

//            Log.d("radioSize","screenRadio:${screenRadio}\nbitmapRadio${1f/bitmapRadio}")

            return Bitmap.createBitmap(bitmap, if (left<0) 0 else left, top,right-left, bottom-top)
//            return bitmap
        }
    }

    fun bitmap2File(bitmap: Bitmap, path: String):File? {
        try {
            val file = File(path)
            val bos = BufferedOutputStream(FileOutputStream(file))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
            bos.flush()
            bos.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun getOrientationDescription(filePath: String): Int {
        val exif = ExifInterface(filePath)
        val direction = exif.getAttribute(ExifInterface.TAG_ORIENTATION)//
        var degree = 0
        direction?.apply {
            Log.d("imageOrientation",this)
            degree = when (this.toInt()) {
                3 -> 180
                5 -> 270
                6 -> 90
                7 -> 90
                8 -> 270
                else -> 0
            }
        }

        return degree

    }
}