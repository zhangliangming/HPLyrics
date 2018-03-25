package com.zlm.hp.lyrics.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.utils.LyricsUtils;

import java.util.List;
import java.util.TreeMap;

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
     * 初始
     */
    public static final int STATUS_NONE = 0;
    /**
     * 选中
     */
    public static final int STATUS_SELECTED = 1;

    /**
     * 完成
     */
    public static final int STATUS_FINISH = 2;
    /**
     * 状态
     */
    private int mStatus = STATUS_NONE;

    /**
     * 歌词索引，-1是未读，-2是已经完成
     */
    private int mLrcIndex = -1;
    /**
     * 行歌词
     */
    private LyricsLineInfo mLyricsLineInfo;

    /**
     * 每个字时间集合
     */
    private TreeMap<Integer, WordDisInterval> mWordDisIntervals = new TreeMap<Integer, WordDisInterval>();


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
        if (mLyricsLineInfo == null) return;
        int viewHeight = getHeight();
        int viewWidth = getWidth();
        int textHeight = LyricsUtils.getTextHeight(mPaint);
        float textY = (viewHeight + textHeight) / 2;

        //歌词字集合
        String[] lyricsWords = mLyricsLineInfo.getLyricsWords();
        String lineLyrics = mLyricsLineInfo.getLineLyrics();
        float textWidth = LyricsUtils.getTextWidth(mPaint, lineLyrics);
        float textHLWidth = 0;
        //计算高亮宽度
        if (mLrcIndex == -1) {
            //未读
            textHLWidth = 0;
        } else if (mLrcIndex == -2) {
            textHLWidth = textWidth;
        } else {
            if (mLrcIndex < lyricsWords.length) {
                StringBuilder temp = new StringBuilder();
                for (int i = 0; i <= mLrcIndex; i++) {
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
        if (mStatus != STATUS_NONE) {
            if (mStatus == STATUS_SELECTED) {
                //选中
                mPaintRect.setColor(Color.RED);
            } else if (mStatus == STATUS_FINISH) {
                //完成
                mPaintRect.setColor(mPaintHLColor);
            }
            canvas.drawRect(2, 2, viewWidth - 2, viewHeight - 2, mPaintRect);
        }
    }

    /**
     * 设置当前歌曲索引
     *
     * @param curPlayingTime
     */
    public boolean play(long curPlayingTime) {
        //选中
        if (mStatus == STATUS_SELECTED) {
            mLrcIndex++;
            int preLrcIndex = mLrcIndex - 1;
            if (mWordDisIntervals.containsKey(preLrcIndex)) {
                //设置前一个字的结束时间
                WordDisInterval wordDisInterval = mWordDisIntervals.get(preLrcIndex);
                wordDisInterval.setEndTime(curPlayingTime);
                mWordDisIntervals.put(preLrcIndex, wordDisInterval);
            }
            //设置当前字的开始时间
            WordDisInterval wordDisInterval = new WordDisInterval();
            wordDisInterval.setStartTime(curPlayingTime);
            mWordDisIntervals.put(mLrcIndex, wordDisInterval);

            //判断是否完成
            if (mLrcIndex == mLyricsLineInfo.getLyricsWords().length) {

                mStatus = STATUS_FINISH;

                return true;
            }
        }
        return false;
    }

    /**
     * 设置回滚
     */
    public void back() {
        //选中
        if (mStatus == STATUS_SELECTED) {
            mLrcIndex--;
            if (mLrcIndex < -1) {
                mLrcIndex = -1;
            }
            //后退时，删除当前的歌词字时间
            int nextLrcIndex = mLrcIndex + 1;
            if (mWordDisIntervals.containsKey(nextLrcIndex)) {
                mWordDisIntervals.remove(nextLrcIndex);
            }
            //
            if (mWordDisIntervals.containsKey(mLrcIndex)) {
                WordDisInterval wordDisInterval = mWordDisIntervals.get(mLrcIndex);
                wordDisInterval.setEndTime(0);
                mWordDisIntervals.put(mLrcIndex, wordDisInterval);
            }
        }
    }


    /**
     * 重置
     */
    public void reset() {
        mStatus = STATUS_NONE;
        mLrcIndex = -1;
        mWordDisIntervals.clear();
    }

    public void setStatus(int status) {
        if (mStatus != STATUS_FINISH) {
            this.mStatus = status;
        }
    }

    public void setLyricsLineInfo(LyricsLineInfo mLyricsLineInfo) {
        this.mLyricsLineInfo = mLyricsLineInfo;
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

    /**
     * 单个歌词字时间实体类
     *
     * @author zhangliangming
     */
    class WordDisInterval {
        /**
         * 开始时间
         */
        long startTime;
        /**
         * 结束时间
         */
        long endTime;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
    }
}
