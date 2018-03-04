package com.zlm.hp.lyrics.interfaces;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * 歌词视图接口
 * Created by zhangliangming on 2018-03-04.
 */

public interface ILrcView {

    /**
     * 初始化
     *
     * @param context
     */
    public void viewInit(Context context);

    /**
     * view视图加载完成
     */
    public void viewLoadFinish();

    /**
     * view的draw歌词调用方法
     *
     * @param canvas
     * @return
     */
    public void onViewDrawLrc(Canvas canvas);

    /**
     * view的onTouchEvent调用方法
     *
     * @param event
     * @return
     */
    public boolean onViewTouchEvent(MotionEvent event);

    /**
     * 更新视图
     *
     * @param playProgress
     */
    public void updateView(int playProgress);

}
