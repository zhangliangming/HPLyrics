package com.zlm.hp.lyrics.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.zlm.hp.lyrics.utils.ColorUtils;
import com.zlm.hplyricslibrary.R;

/**
 * 歌词图片预览视图
 * Created by zhangliangming on 2018-03-18.
 */

public class LrcImgPreView extends View {

    /**
     * 默认歌词画笔
     */
    private Paint mPaint;
    /**
     * 默认画笔颜色
     */
    private int[] mPaintColor = new int[]{
            ColorUtils.parserColor("#00348a"),
            ColorUtils.parserColor("#0080c0"),
            ColorUtils.parserColor("#03cafc")
    };
    /**
     * 高亮歌词画笔
     */
    private Paint mPaintHL;
    //高亮颜色
    private int[] mPaintHLColor = new int[]{
            ColorUtils.parserColor("#82f7fd"),
            ColorUtils.parserColor("#ffffff"),
            ColorUtils.parserColor("#03e9fc")
    };
    /**
     * 轮廓画笔
     */
    private Paint mPaintOutline;

    /**
     * 默认提示文本
     */
    private String mDefText;

    /**
     * 空行高度
     */
    private float mSpaceLineHeight = 30;
    /**
     * 歌词字体大小
     */
    private float mFontSize = 35;

    /**
     * 左右间隔距离
     */
    private float mPaddingLeftOrRight = 15;

    /**
     * 显示默认歌词
     */
    public static final int SHOWDEFLRC = 0;
    /**
     * 显示额外歌词
     */
    public static final int SHOWEXTRALRC = 1;
    /**
     * 显示歌词类型
     */
    private int mShowLrcType = SHOWDEFLRC;


    public LrcImgPreView(Context context) {
        super(context);
        init(context);
    }

    public LrcImgPreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param context
     */
    protected void init(Context context) {
        mDefText = context.getString(R.string.def_text);
        //默认画笔
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mFontSize);

        //高亮画笔
        mPaintHL = new Paint();
        mPaintHL.setDither(true);
        mPaintHL.setAntiAlias(true);
        mPaintHL.setTextSize(mFontSize);

        //轮廓画笔
        mPaintOutline = new Paint();
        mPaintOutline.setDither(true);
        mPaintOutline.setAntiAlias(true);
        mPaintOutline.setColor(Color.BLACK);
        mPaintOutline.setTextSize(mFontSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int viewHeight = getHeight();
        int textHeight = getTextHeight(mPaint);
        float topOrBottom = (viewHeight - 2 * textHeight - mSpaceLineHeight) / 2;

        //绘画高度文本
        String hlText = "♩ ♪ ♫ ♬ ∮";
        float hlTextX = mPaddingLeftOrRight;
        if (mShowLrcType == SHOWEXTRALRC) {
            float textWidth = getTextWidth(mPaintHL, hlText);
            hlTextX = (getWidth() - textWidth) / 2;
        }

        float hlTextY = topOrBottom + getTextHeight(mPaint);
        drawOutline(canvas, mPaintOutline, hlText, hlTextX, hlTextY);
        drawText(canvas, mPaintHL, mPaintHLColor, hlText, hlTextX, hlTextY);

        //绘画文本
        String text = mDefText;
        float textX = mPaddingLeftOrRight;
        if (mShowLrcType == SHOWEXTRALRC) {
            float textWidth = getTextWidth(mPaint, text);
            textX = (getWidth() - textWidth) / 2;
        }
        float textY = viewHeight - topOrBottom;
        drawOutline(canvas, mPaintOutline, text, textX, textY);
        drawText(canvas, mPaint, mPaintColor, text, textX, textY);
    }

    /**
     * 绘画文本
     *
     * @param canvas
     * @param paint       画笔
     * @param paintColors 画笔颜色
     * @param text        文本
     * @param x
     * @param y
     */
    public void drawText(Canvas canvas, Paint paint, int paintColors[], String text, float x, float y) {
        //设置为上下渐变
        LinearGradient linearGradient = new LinearGradient(x, y - getTextHeight(paint), x, y, paintColors, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawText(text, x, y, paint);
    }

    /**
     * 描绘轮廓
     *
     * @param canvas
     * @param text
     * @param x
     * @param y
     */
    public void drawOutline(Canvas canvas, Paint paint, String text, float x, float y) {
        canvas.drawText(text, x - 1, y, paint);
        canvas.drawText(text, x + 1, y, paint);
        canvas.drawText(text, x, y + 1, paint);
        canvas.drawText(text, x, y - 1, paint);
    }


    /**
     * 获取真实的歌词高度
     *
     * @param paint
     * @return
     */
    public int getRealTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) (-fm.leading - fm.ascent + fm.descent);
    }

    /**
     * 获取行歌词高度。用于y轴位置计算
     *
     * @param paint
     * @return
     */
    public int getTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) -(fm.ascent + fm.descent);
    }

    /**
     * 获取文本宽度
     *
     * @param paint
     * @param text
     * @return
     */
    public float getTextWidth(Paint paint, String text) {
        return paint
                .measureText(text);
    }

    /**
     * 设置字体文件
     *
     * @param typeFace
     */
    public void setTypeFace(Typeface typeFace) {
        if (typeFace != null) {
            mPaint.setTypeface(typeFace);
            mPaintHL.setTypeface(typeFace);
            mPaintOutline.setTypeface(typeFace);
        }
    }

    ///////////////////////////////////////////////////////////

    public void setPaintColor(int[] mPaintColor) {
        this.mPaintColor = mPaintColor;
    }

    public void setPaintHLColor(int[] mPaintHLColor) {
        this.mPaintHLColor = mPaintHLColor;
    }

    public void setDefText(String mDefText) {
        this.mDefText = mDefText;
    }

    public void setSpaceLineHeight(float mSpaceLineHeight) {
        this.mSpaceLineHeight = mSpaceLineHeight;
    }

    public void setFontSize(float mFontSize) {
        this.mFontSize = mFontSize;

        mPaint.setTextSize(mFontSize);
        mPaintHL.setTextSize(mFontSize);
        mPaintOutline.setTextSize(mFontSize);
    }

    public void setPaddingLeftOrRight(float mPaddingLeftOrRight) {
        this.mPaddingLeftOrRight = mPaddingLeftOrRight;
    }

    public void setShowLrcType(int mShowLrcType) {
        this.mShowLrcType = mShowLrcType;
    }
}
