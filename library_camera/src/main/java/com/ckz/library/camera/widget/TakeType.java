package com.ckz.library.camera.widget;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author kzcai
 * @packageName com.ckz.library.camera.widget
 * @date 2021/3/18
 */
public class TakeType {

    public static final int ONLY_IMAGE = 1;
    public static final int ONLY_VIDEO = 2;
    public static final int IMAGE_AND_VIDEO = 3;

    @IntDef({ONLY_IMAGE, ONLY_VIDEO,IMAGE_AND_VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface CameraType {
    }

}
