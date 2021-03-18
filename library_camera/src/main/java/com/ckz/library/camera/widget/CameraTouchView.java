package com.ckz.library.camera.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.core.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by CKZ on 2017/8/11.
 */

public class CameraTouchView extends View {
    /**
     * 动画时长
     */
    private static final int ANIM_MILS = 600;
    /**
     * 动画每多久刷新一次
     */
    private static final int ANIM_UPDATE = 30;
    private Paint mPaint = new Paint();//画笔
    private int lineColor = Color.GREEN;//默认颜色
    private RectF rectF = new RectF();
    private float paintWidth = 5.0f;
    /**
     * focus size
     */
    private int focusSize = 200;
    /**
     * 上一次两指距离
     */
    private float oldDist = 1f;

    private int lineSize = focusSize / 4;

    private float scale;
    public CameraTouchView(Context context) {
        this(context,null);
    }

    public CameraTouchView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CameraTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint.setColor(lineColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeMiter(paintWidth);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        if (event.getPointerCount() == 1 && action == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            setFocusPoint(x, y);
            if (listener != null) {
                listener.handleFocus(x, y);
            }
        } else if (event.getPointerCount() >= 2){
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        if (this.listener != null) {
                            this.listener.handleZoom(true);
                        }
                    } else if (newDist < oldDist) {
                        if (this.listener != null) {
                            this.listener.handleZoom(false);
                        }
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    /**
     * 计算两点触控距离
     * @param event
     * @return
     */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 设置当前触摸点
     * @param x
     * @param y
     */
    private void setFocusPoint(float x, float y) {
        rectF.set(x - focusSize, y - focusSize, x + focusSize, y + focusSize);
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f,0.5f);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                scale = (float) valueAnimator.getAnimatedValue();
                invalidate();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scale = 0;
                        postInvalidate();
                    }
                },1000);
            }
        });
        animator.start();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (scale != 0) {
            float centerX = rectF.centerX();
            float centerY = rectF.centerY();
            canvas.scale(scale, scale, centerX, centerY);
            canvas.drawRect(rectF, mPaint);
            canvas.drawLine(rectF.left, centerY, rectF.left + lineSize, centerY, mPaint);
            canvas.drawLine(rectF.right, centerY, rectF.right - lineSize, centerY, mPaint);
            canvas.drawLine(centerX, rectF.top, centerX, rectF.top + lineSize, mPaint);
            canvas.drawLine(centerX, rectF.bottom, centerX, rectF.bottom - lineSize, mPaint);
        }
    }


    private OnViewTouchListener listener;

    public void setOnViewTouchListener(OnViewTouchListener listener) {
        this.listener = listener;
    }

//    public void removeOnZoomListener() {
//        this.listener = null;
//    }

    public interface OnViewTouchListener {
        /**
         * 对焦
         * @param x
         * @param y
         */
        void handleFocus(float x, float y);

        /**
         * 缩放
         * @param zoom true放大反之
         */
        void handleZoom(boolean zoom);

    }

}
