package com.zlm.hp.lyrics.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.MakeLrcInfo;
import com.zlm.hp.lyrics.utils.LyricsUtils;

/**
 * 歌词制作预览视图
 * Created by zhangliangming on 2018-03-25.
 */

public class MakeLrcPreView extends View {
    /**
     * 默认歌词画笔
     */
    private Paint mPaint;
    /**
     * 默认颜色
     */
    private int mPaintColor = Color.BLACK;
    /**
     * 高亮歌词画笔
     */
    private Paint mPaintHL;
    /**
     * 高亮画笔颜色
     */
    private int mPaintHLColor = Color.BLUE;

    /**
     * 绘画边框
     */
    private Paint mPaintRect;

    /**
     * 歌词字体大小
     */
    private float mFontSize = 35;
    /**
     * 左右间隔距离
     */
    private float mPaddingLeftOrRight = 15;
    /**
     * 制作歌词
     */
    private MakeLrcInfo mMakeLrcInfo;
    /**
     * 绘画边框
     */
    private boolean isPaintRect = true;

    public MakeLrcPreView(Context context) {
        super(context);
        init(context);
    }

    public MakeLrcPreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param context
     */
    protected void init(Context context) {
        //默认画笔
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mFontSize);
        setPaintColor(mPaintColor);

        //高亮画笔
        mPaintHL = new Paint();
        mPaintHL.setDither(true);
        mPaintHL.setAntiAlias(true);
        mPaintHL.setTextSize(mFontSize);
        setPaintHLColor(mPaintHLColor);

        //画边框
        mPaintRect = new Paint();
        mPaintRect.setDither(true);
        mPaintRect.setAntiAlias(true);
        mPaintRect.setStyle(Paint.Style.STROKE);
        mPaintRect.setStrokeWidth(3f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mMakeLrcInfo == null) return;
        LyricsLineInfo lyricsLineInfo = mMakeLrcInfo.getLyricsLineInfo();
        if (lyricsLineInfo == null) return;
        int viewHeight = getHeight();
        int viewWidth = getWidth();
        int textHeight = LyricsUtils.getTextHeight(mPaint);
        float textY = (viewHeight + textHeight) / 2;

        int lrcIndex = mMakeLrcInfo.getLrcIndex();
        int status = mMakeLrcInfo.getStatus();

        //歌词字集合
        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        String lineLyrics = lyricsLineInfo.getLineLyrics();
        float textWidth = LyricsUtils.getTextWidth(mPaint, lineLyrics);
        float textHLWidth = 0;
        //计算高亮宽度
        if (lrcIndex == -1) {
            //未读
            textHLWidth = 0;
        } else if (lrcIndex == -2) {
            textHLWidth = textWidth;
        } else {
            if (lrcIndex < lyricsWords.length) {
                StringBuilder temp = new StringBuilder();
                for (int i = 0; i <= lrcIndex; i++) {
                    temp.append(lyricsWords[i]);
                }
                textHLWidth = LyricsUtils.getTextWidth(mPaint, temp.toString());

            } else {
                textHLWidth = textWidth;
            }
        }
        //画歌词
        float textX = LyricsUtils.getHLMoveTextX(textWidth, textHLWidth, viewWidth, mPaddingLeftOrRight);
        LyricsUtils.drawDynamicText(canvas, mPaint, mPaintHL, lineLyrics, textHLWidth, textX, textY);

        //画边框
        if (status != MakeLrcInfo.STATUS_NONE && isPaintRect) {
            if (status == MakeLrcInfo.STATUS_SELECTED) {
                //选中
                mPaintRect.setColor(Color.RED);
            } else if (status == MakeLrcInfo.STATUS_FINISH) {
                //完成
                mPaintRect.setColor(mPaintHLColor);
            }
            canvas.drawRect(2, 2, viewWidth - 2, viewHeight - 2, mPaintRect);
        }
    }

    public void setPaintColor(int mPaintColor) {
        this.mPaintColor = mPaintColor;
        mPaint.setColor(mPaintColor);
    }

    public void setPaintHLColor(int mPaintHLColor) {
        this.mPaintHLColor = mPaintHLColor;
        mPaintHL.setColor(mPaintHLColor);
    }

    public void setFontSize(float mFontSize) {
        this.mFontSize = mFontSize;
        mPaint.setTextSize(mFontSize);
        mPaintHL.setTextSize(mFontSize);
    }

    public void setPaintRect(boolean paintRect) {
        isPaintRect = paintRect;
    }

    public void setMakeLrcInfo(MakeLrcInfo mMakeLrcInfo) {
        this.mMakeLrcInfo = mMakeLrcInfo;
    }
}
