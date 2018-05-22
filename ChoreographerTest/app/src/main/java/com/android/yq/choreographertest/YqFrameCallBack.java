package com.android.yq.choreographertest;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;

/**
 * Created by yq on 2018/5/22.
 */

public class YqFrameCallBack implements Choreographer.FrameCallback {

    private static final String TAG = "YqFrameCallBack";

    private static YqFrameCallBack sInstance;

    private long mFrameIntervalNanos;

    private static final float deviceRefreshRateMs = 16.6f;

    private static  long lastFrameTimeNanos = 0;//纳秒为单位

    private static  long currentFrameTimeNanos = 0;

    public static YqFrameCallBack getInstance(Context context){
        if (sInstance == null){
            sInstance = new YqFrameCallBack(context);
        }
        return sInstance;
    }

    private YqFrameCallBack(Context context){
        mFrameIntervalNanos = (long)(1000000000 / getRefreshRate(context));
    }

    private static float getRefreshRate(Context context){
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        return display.getRefreshRate();
    }

    public static void start(Context context) {
        Choreographer.getInstance().postFrameCallback(YqFrameCallBack.getInstance(context));
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if(lastFrameTimeNanos == 0){
            lastFrameTimeNanos = frameTimeNanos;
            Choreographer.getInstance().postFrameCallback(this);
            return;
        }
        final long startNanos = System.nanoTime();
        final long jitterNanos = startNanos - frameTimeNanos;
        final long skippedFrames = jitterNanos / mFrameIntervalNanos;
        final long lastFrameOffset = jitterNanos % mFrameIntervalNanos;
        Log.i(TAG,"Missed vsync by " + (jitterNanos * 0.000001f) + " ms which is more than the frame interval of "
                + (mFrameIntervalNanos * 0.000001f) + " ms! Skipping " + skippedFrames + " frames and setting frame time to "
                + (lastFrameOffset * 0.000001f) + " ms in the past.");

        currentFrameTimeNanos = frameTimeNanos;
        float value = (currentFrameTimeNanos-lastFrameTimeNanos)/1000000.0f;

        final int skipFrameCount = skipFrameCount(lastFrameTimeNanos, currentFrameTimeNanos, deviceRefreshRateMs);
        Log.e(TAG,"两次绘制时间间隔value="+value+"  frameTimeNanos="+frameTimeNanos+"  currentFrameTimeNanos="+currentFrameTimeNanos+"  skipFrameCount="+skipFrameCount+"");
        lastFrameTimeNanos=currentFrameTimeNanos;
        Choreographer.getInstance().postFrameCallback(this);
    }

    /**
     *
     *计算跳过多少帧
     * @param start
     * @param end
     * @param devicefreshRate
     * @return
     */
    private  int skipFrameCount(long start,long end,float devicefreshRate){
        int count = 0;
        long diffNs=end-start;
        long diffMs = Math.round(diffNs / 1000000.0f);
        long dev=Math.round(devicefreshRate);
        if(diffMs>dev){
            long skipCount=diffMs/dev;
            count=(int)skipCount;
        }
        return  count;
    }
}
