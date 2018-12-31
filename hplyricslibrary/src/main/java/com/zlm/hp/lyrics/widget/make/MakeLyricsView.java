package com.zlm.hp.lyrics.widget.make;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.make.MakeLrcLineInfo;
import com.zlm.hp.lyrics.utils.ColorUtils;
import com.zlm.hp.lyrics.utils.LyricsUtils;
import com.zlm.hplyricslibrary.R;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @Description: 制作歌词
 * @author: zhangliangming
 * @date: 2018-12-23 17:08
 **/
public class MakeLyricsView extends View {

    /**
     * 制作歌词列表
     */
    private List<MakeLrcLineInfo> mMakeLrcLineInfos;

    /**
     * 默认歌词画笔
     */
    private Paint mPaint;
    /**
     * 默认画笔颜色
     */
    private int[] mPaintColors = new int[]{
            ColorUtils.parserColor("#555555"),
            ColorUtils.parserColor("#555555")
    };
    /**
     * 高亮歌词画笔
     */
    private Paint mPaintHL;
    //高亮颜色
    private int[] mPaintHLColors = new int[]{
            ColorUtils.parserColor("#0288d1"),
            ColorUtils.parserColor("#0288d1")
    };
    /**
     * 轮廓画笔
     */
    private Paint mPaintOutline;

    /**
     * 空行高度
     */
    private float mSpaceLineHeight = 60;
    /**
     * 歌词字体大小
     */
    private float mFontSize = 30;

    private byte[] lock = new byte[0];

    /**
     * 默认提示文本
     */
    private String mDefText;

    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;
    /**
     *
     */
    private Scroller mScroller;

    /**
     * Y轴移动的时间
     */
    private int mDuration = 250;

    /**
     * 是否直接拦截
     */
    private boolean mIsTouchIntercept = false;

    /**
     * 记录手势
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    //用于判断拦截
    private int mInterceptX = 0;
    private int mInterceptY = 0;
    /**
     * 触摸最后一次的坐标
     */
    private int mLastY;
    /**
     * 歌词在Y轴上的偏移量
     */
    private float mOffsetY = 0;
    /**
     * 视图y中间
     */
    private float mCentreY = 0;

    /**
     * 当前歌词的所在行数
     */
    private int mLyricsLineNum = 0;

    /**
     * 画线
     */
    private Paint mPaintLine;

    /**
     * 画线颜色
     */
    private int mPaintLineColor = Color.WHITE;

    /**
     * 画提示框
     */
    private Paint mPaintRect;

    /**
     * 画提示框线颜色
     */
    private int mPaintRectColor = ColorUtils.parserColor("#0288d1");

    /**
     * 线字体大小
     */
    private float mLineFontSize = 30;

    /**
     * 当前制作歌词的所在行数
     */
    private int mMakeLyricsLineNum = 0;

    /**
     * 因为选择歌词导致的视图滑动
     */
    private boolean isSelectScroll = false;

    /**
     * 因为fling导致的视图滑动
     */
    private boolean isFlingScroll = false;

    /**
     * 滑动事件
     */
    private OnScrollListener mOnScrollListener;

    public MakeLyricsView(Context context) {
        super(context);
        init(context);
    }

    public MakeLyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MakeLyricsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //初始化
        mScroller = new Scroller(context, new LinearInterpolator());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

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

        //画线
        mPaintLine = new Paint();
        mPaintLine.setDither(true);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStyle(Paint.Style.FILL);
        mPaintLine.setTextSize(mLineFontSize);

        //画提示框
        mPaintRect = new Paint();
        mPaintRect.setStrokeWidth(2f);
        mPaintRect.setStyle(Paint.Style.STROKE);
        mPaintRect.setDither(true);
        mPaintRect.setAntiAlias(true);
        mPaintRect.setColor(mPaintRectColor);
        mPaintRect.setTextSize(mFontSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onDrawView(canvas);
    }

    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void onDrawView(Canvas canvas) {
        synchronized (lock) {
            if (mMakeLrcLineInfos == null || mMakeLrcLineInfos.size() == 0) {
                //绘画默认文本
                String defText = getDefText();
                float textWidth = LyricsUtils.getTextWidth(mPaint, defText);
                int textHeight = LyricsUtils.getTextHeight(mPaint);
                float hlWidth = textWidth / 2;
                float x = (getWidth() - textWidth) / 2;
                float y = (getHeight() + textHeight) / 2;
                LyricsUtils.drawOutline(canvas, mPaintOutline, defText, x, y);
                LyricsUtils.drawDynamicText(canvas, mPaint, mPaintHL, mPaintColors, mPaintHLColors, defText, hlWidth, x, y);

            } else {
                onDrawLrcView(canvas);
            }
        }
    }

    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void onDrawLrcView(Canvas canvas) {
        Paint paint = mPaint;
        Paint paintHL = mPaintHL;
        float spaceLineHeight = mSpaceLineHeight;
        int lyricsLineNum = mLyricsLineNum;
        float lineHeight = LyricsUtils.getTextHeight(paint) + spaceLineHeight;
        float lineAtHeightY = lineHeight * lyricsLineNum;
        int[] paintHLColors = mPaintHLColors;
        int[] paintColors = mPaintColors;

        //获取中间位置
        mCentreY = (getHeight() + LyricsUtils.getTextHeight(paintHL)) * 0.5f + lineAtHeightY - mOffsetY;

        //画当前行歌词
        if (lyricsLineNum < mMakeLrcLineInfos.size()) {
            MakeLrcLineInfo curMakeLrcLineInfo = mMakeLrcLineInfos.get(lyricsLineNum);
            if (mCentreY > lineHeight || mCentreY + spaceLineHeight < getHeight()) {
                if (curMakeLrcLineInfo.getStatus() == MakeLrcLineInfo.STATUS_FINISH) {
                    drawUpAndDownLrc(canvas, paintHL, paintHLColors, mCentreY, curMakeLrcLineInfo);
                } else {
                    drawCurLrc(canvas, paint, paintHL, paintColors, paintHLColors, mCentreY, curMakeLrcLineInfo);
                }
            }
        }

        //画当前行上面的歌词
        for (int i = lyricsLineNum - 1, j = 1; i >= 0; i--, j++) {
            MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(i);
            float lineY = mCentreY - j * lineHeight;
            //超出上视图
            if (lineY < lineHeight) {
                break;
            }
            //超出下视图
            if (lineY + spaceLineHeight > getHeight()) {
                continue;
            }
            if (makeLrcLineInfo.getStatus() == MakeLrcLineInfo.STATUS_FINISH) {
                drawUpAndDownLrc(canvas, paintHL, paintHLColors, lineY, makeLrcLineInfo);
            } else {
                drawUpAndDownLrc(canvas, paint, paintColors, lineY, makeLrcLineInfo);
            }
        }


        // 画当前歌词下面的歌词
        for (int i = lyricsLineNum + 1, j = 1; i < mMakeLrcLineInfos.size(); i++, j++) {
            MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(i);
            float lineY = mCentreY + j * lineHeight;
            //超出上视图
            if (lineY < lineHeight) {
                continue;
            }
            //超出下视图
            if (lineY + spaceLineHeight > getHeight()) {
                break;
            }
            if (makeLrcLineInfo.getStatus() == MakeLrcLineInfo.STATUS_FINISH) {
                drawUpAndDownLrc(canvas, paintHL, paintHLColors, lineY, makeLrcLineInfo);
            } else {
                drawUpAndDownLrc(canvas, paint, paintColors, lineY, makeLrcLineInfo);
            }
        }

        drawIndicator(canvas);
    }

    /**
     * 画指示线
     *
     * @param canvas
     */
    private void drawIndicator(Canvas canvas) {
        //画线
        int lineH = 2;
        int linePadding = 20;
        float lineY = (getHeight() - lineH) / 2;
        float lineLeft = linePadding;
        float lineR = getWidth() - linePadding;
        LinearGradient linearGradientHL = new LinearGradient(lineLeft, lineY + lineH, lineR, lineY + lineH, new int[]{ColorUtils.parserColor(mPaintLineColor, 255), ColorUtils.parserColor(mPaintLineColor, 0), ColorUtils.parserColor(mPaintLineColor, 0), ColorUtils.parserColor(mPaintLineColor, 255)}, new float[]{0f, 0.2f, 0.8f, 1f}, Shader.TileMode.CLAMP);
        mPaintLine.setShader(linearGradientHL);
        canvas.drawRect(lineLeft, lineY, lineR, lineY + lineH, mPaintLine);
    }

    /**
     * 绘画上面歌词
     *
     * @param canvas
     * @param paint
     * @param paintColors
     * @param y
     * @param makeLrcLineInfo
     */
    private void drawUpAndDownLrc(Canvas canvas, Paint paint, int[] paintColors, float y, MakeLrcLineInfo makeLrcLineInfo) {
        String text = makeLrcLineInfo.getLyricsLineInfo().getLineLyrics();
        float textWidth = LyricsUtils.getTextWidth(paint, text);
        float textX = (getWidth() - textWidth) * 0.5f;
        LyricsUtils.drawText(canvas, paint, paintColors, text, textX, y);
    }


    /**
     * 绘画当前行歌词
     *
     * @param paint
     * @param paintHL
     * @param paintColors
     * @param paintHLColors
     * @param y
     * @param makeLrcLineInfo
     */
    private void drawCurLrc(Canvas canvas, Paint paint, Paint paintHL, int[] paintColors, int[] paintHLColors, float y, MakeLrcLineInfo makeLrcLineInfo) {
        String text = makeLrcLineInfo.getLyricsLineInfo().getLineLyrics();
        float textWidth = LyricsUtils.getTextWidth(paint, text);
        float textHeight = LyricsUtils.getTextHeight(paint);
        float textX = (getWidth() - textWidth) * 0.5f;

        int rectLeft = -1;
        int rectWidth = 0;
        //
        float hlWidth = 0;
        //获取制作歌词索引
        int index = makeLrcLineInfo.getLrcIndex();
        if (index == -2) {
            //完成
            rectLeft = -1;
            hlWidth = textWidth;
        } else if (index != -1) {
            String[] lyricsWords = makeLrcLineInfo.getLyricsLineInfo().getLyricsWords();
            //制作中
            rectWidth = (int) LyricsUtils.getTextWidth(paint, lyricsWords[index]);
            int sumHLWidth = 0;
            for (int i = 0; i <= index; i++) {
                sumHLWidth += LyricsUtils.getTextWidth(paint, lyricsWords[i]);
            }
            hlWidth = sumHLWidth;
            rectLeft = (int) (hlWidth - rectWidth);
            rectWidth = (int) LyricsUtils.getTextWidth(paint, lyricsWords[index].trim());
        }
        LyricsUtils.drawDynamicText(canvas, paint, paintHL, paintColors, paintHLColors, text, hlWidth, textX, y);

        float spaceLineHeight = mSpaceLineHeight;
        float lineHeight = LyricsUtils.getTextHeight(paint) + spaceLineHeight;
        //绘画制作歌词指示器
        if (rectLeft != -1) {

            Rect rect = new Rect();
            rect.top = (int) y - (int) lineHeight / 2;
            rect.bottom = (int) y + (int) textHeight;
            rect.left = (int) (textX + rectLeft);
            rect.right = rect.left + rectWidth;

            canvas.drawRect(rect, mPaintRect);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        obtainVelocityTracker(event);
        int actionId = event.getAction();
        switch (actionId) {
            case MotionEvent.ACTION_DOWN:

                mLastY = (int) event.getY();
                mInterceptX = (int) event.getX();
                mInterceptY = (int) event.getY();


                break;
            case MotionEvent.ACTION_MOVE:
                int curX = (int) event.getX();
                int curY = (int) event.getY();
                int deltaX = mInterceptX - curX;
                int deltaY = mInterceptY - curY;

                if (mIsTouchIntercept || (Math.abs(deltaY) > mTouchSlop && Math.abs(deltaX) < mTouchSlop)) {
                    mIsTouchIntercept = true;

                    int dy = mLastY - curY;

                    if (!mScroller.isFinished()) mScroller.forceFinished(true);

                    //上越界
                    float finalY = mOffsetY + dy;

                    if (finalY < getTopOverScrollHeightY()) {
                        dy = (int) (getTopOverScrollHeightY() - mOffsetY);
                    }
                    //下越界
                    if (finalY > getBottomOverScrollHeightY()) {
                        dy = (int) (getBottomOverScrollHeightY() - mOffsetY);
                    }

                    mScroller.startScroll(0, mScroller.getFinalY(), 0, dy, 0);
                    invalidateView();

                }

                mLastY = curY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                int yVelocity = (int) velocityTracker.getYVelocity();
                int xVelocity = (int) velocityTracker.getXVelocity();

                if (Math.abs(yVelocity) > mMinimumVelocity) {

                    if (!mScroller.isFinished()) mScroller.forceFinished(true);

                    int startX = 0;
                    int startY = (int) mOffsetY;
                    int velocityX = -xVelocity;
                    int velocityY = -yVelocity;
                    int minX = 0;
                    int maxX = 0;

                    //
                    int lrcSumHeight = (int) getBottomOverScrollHeightY();
                    int minY = 0;
                    int maxY = lrcSumHeight;

                    isFlingScroll = true;
                    mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                    invalidateView();

                } else if (actionId == MotionEvent.ACTION_UP) {

                    mMakeLyricsLineNum = getScrollLrcLineNum(mScroller.getFinalY());
                    scrollToMakeLine(mMakeLyricsLineNum);
                }

                releaseVelocityTracker();

                mIsTouchIntercept = false;
                mLastY = 0;
                mInterceptX = 0;
                mInterceptY = 0;

                break;
            default:
        }

        return true;
    }

    /**
     * 滑动到制作歌词行
     */
    private void scrollToMakeLine(int makeLyricsLineNum) {
        synchronized (lock) {
            isSelectScroll = true;
            mLyricsLineNum = makeLyricsLineNum;
            float lineHeight = LyricsUtils.getTextHeight(mPaint) + mSpaceLineHeight;
            float lineAtHeightY = lineHeight * mLyricsLineNum;
            int deltaYTemp = (int) (lineAtHeightY - mScroller.getFinalY());
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaYTemp, mDuration);
            invalidateView();
        }
    }

    /**
     * 获取底部越界
     *
     * @return
     */
    private float getBottomOverScrollHeightY() {
        if (mMakeLrcLineInfos == null || mMakeLrcLineInfos.size() == 0) return 0;
        float lineHeight = LyricsUtils.getTextHeight(mPaint) + mSpaceLineHeight;
        return mMakeLrcLineInfos.size() * lineHeight;
    }

    /**
     * 获取顶部越界高度
     *
     * @return
     */
    private float getTopOverScrollHeightY() {
        return 0;
    }


    /**
     * @param event
     */

    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {

            mVelocityTracker = VelocityTracker.obtain();

        }

        mVelocityTracker.addMovement(event);

    }


    /**
     * 释放
     */
    private void releaseVelocityTracker() {

        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;

        }

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        synchronized (lock) {
            // 更新当前的X轴偏移量
            if (mScroller.computeScrollOffset()) { // 返回true代表正在模拟数据，false 已经停止模拟数据
                mOffsetY = mScroller.getCurrY();
                if (mOnScrollListener != null) {
                    if (!isSelectScroll) {
                        //获取滚动行
                        mMakeLyricsLineNum = getScrollLrcLineNum(mOffsetY);
//                        if (mMakeLyricsLineNum != mLyricsLineNum || (mMakeLyricsLineNum == 0 && mLyricsLineNum == 0)) {
                        int curPlayTime = mOnScrollListener.getScrollStopPlayTime();
                        if (curPlayTime > 0) {
                            handleLinePlayTime(curPlayTime, mMakeLyricsLineNum);
                        }
//                        }
                    }
                }
            } else {

                if (isFlingScroll) {
                    isFlingScroll = false;

                    //处理多一次滑动数据
                    //获取滚动行
                    mMakeLyricsLineNum = getScrollLrcLineNum(mScroller.getFinalY());
                    int curPlayTime = mOnScrollListener.getScrollStopPlayTime();
                    if (curPlayTime > 0) {
                        handleLinePlayTime(curPlayTime, mMakeLyricsLineNum);
                    }
                    //及时刷新
                    invalidateView();

                    //还原到制作歌词行
                    scrollToMakeLine(mMakeLyricsLineNum);
                }

                if (isSelectScroll) {

                    if (mOnScrollListener != null) {
                        //歌词滚动结束，如果当前已制作歌词行的开始时间比当前播放时间大，则需要清空已制作的数据
                        if (mMakeLrcLineInfos != null && mMakeLrcLineInfos.size() > 0 && mMakeLyricsLineNum < mMakeLrcLineInfos.size()) {
                            int curPlayProgress = mOnScrollListener.getCurPlayTime();
                            int curMakeLyricsLineNum = mMakeLyricsLineNum - 1;
                            if (curMakeLyricsLineNum >= 0 && curMakeLyricsLineNum < mMakeLrcLineInfos.size()) {
                                MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(curMakeLyricsLineNum);
                                int endTime = makeLrcLineInfo.getLyricsLineInfo().getEndTime();
                                if (endTime > curPlayProgress && makeLrcLineInfo.getStatus() == MakeLrcLineInfo.STATUS_FINISH) {
                                    mOnScrollListener.seekTo(endTime);
                                }
                            }
                        }
                    }
                }
                isSelectScroll = false;

            }
            invalidateView();
        }
    }

    /**
     * 进度条滑动导致歌词制作不正确，需要清空比当前播放时间大并且已制作歌词
     *
     * @param progress
     */
    public void seekTo(int progress) {
        synchronized (lock) {
            if (mMakeLrcLineInfos != null && mMakeLrcLineInfos.size() > 0) {
                int makeLyricsLineNum = -1;
                for (int i = mMakeLrcLineInfos.size() - 1; i >= 0; i--) {
                    MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(i);
                    int startTime = makeLrcLineInfo.getLyricsLineInfo().getStartTime();
                    if (startTime > progress) {
                        makeLyricsLineNum = i;
                        makeLrcLineInfo.reset();
                    }
                }
                //及时刷新
                invalidateView();

                //界面跳转
                if (makeLyricsLineNum != -1) {
                    mMakeLyricsLineNum = makeLyricsLineNum;
                    scrollToMakeLine(makeLyricsLineNum);
                }
            }
            invalidateView();
        }
    }

    /**
     * 行歌词回滚
     */
    public void lineLrcBack() {
        synchronized (lock) {
            if (mMakeLrcLineInfos != null && mMakeLyricsLineNum != -1 && mMakeLyricsLineNum < mMakeLrcLineInfos.size()) {
                MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(mMakeLyricsLineNum);
                makeLrcLineInfo.back();

                //滚动视图到当前制作歌词行
                if (mMakeLyricsLineNum != mLyricsLineNum) {
                    scrollToMakeLine(mMakeLyricsLineNum);
                }
            }
            invalidateView();
        }
    }

    /**
     * 行歌词播放
     *
     * @param currentPosition 当前播放进度
     */
    public void lineLrcPlay(int currentPosition) {
        synchronized (lock) {
            if (mMakeLrcLineInfos != null && mMakeLyricsLineNum != -1 && mMakeLyricsLineNum < mMakeLrcLineInfos.size()) {
                MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(mMakeLyricsLineNum);
                boolean isLineFinish = makeLrcLineInfo.play(currentPosition);
                if (isLineFinish) {
                    //滚动下一歌词行
                    mMakeLyricsLineNum++;

                }
                if (mMakeLyricsLineNum != mLyricsLineNum) {
                    scrollToMakeLine(mMakeLyricsLineNum);
                }
            }
            invalidateView();
        }
    }

    /**
     * 处理行播放歌词时间
     *
     * @param curPlayTime       当前播放时间
     * @param makeLyricsLineNum 当前滑动歌词行
     */
    private void handleLinePlayTime(int curPlayTime, int makeLyricsLineNum) {
        synchronized (lock) {
            //注：滑动过程中存在快进进度条时，会出现curPlayTime小于startTime的情况，需要在进度条做处理
            //判断当前滑动行之前是否有未完成歌词：标记完成
            int startTime = 0;
            int upIndex = -1;
            for (int i = makeLyricsLineNum - 1; i >= 0; i--) {
                MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(i);
                if (makeLrcLineInfo.getStatus() == MakeLrcLineInfo.STATUS_FINISH) {
                    startTime = makeLrcLineInfo.getLyricsLineInfo().getEndTime();
                    if (startTime < curPlayTime) {
                        upIndex = i;
                        upIndex++;
                        break;
                    }
                } else if (i == 0) {
                    upIndex = i;
                    break;
                }
            }
            //标记完成
            if (upIndex != -1 && makeLyricsLineNum != upIndex) {
                int count = makeLyricsLineNum - upIndex;
                int avgTime = (curPlayTime - startTime) / count;
                for (int i = upIndex, j = 0; i < makeLyricsLineNum; i++, j++) {
                    int lineStartTime = startTime + j * avgTime;
                    int lineEndTime = lineStartTime + avgTime;

                    MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(i);
                    makeLrcLineInfo.playLine(lineStartTime, lineEndTime);
                }
            }


            //判断当前滑动行及后面是否有已完成歌词：清空
            for (int i = makeLyricsLineNum; i < mMakeLrcLineInfos.size(); i++) {
                MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(i);
                makeLrcLineInfo.reset();
            }
        }
    }

    /**
     * @param y
     * @return
     */
    private int getScrollLrcLineNum(float y) {
        if (mMakeLrcLineInfos != null && mMakeLrcLineInfos.size() > 0) {
            int sumHeight = 0;
            float lineHeight = LyricsUtils.getTextHeight(mPaint) + mSpaceLineHeight;
            for (int i = 0; i < mMakeLrcLineInfos.size(); i++) {
                sumHeight += lineHeight;
                if (sumHeight > y) {
                    return i;
                }
            }
            return mMakeLrcLineInfos.size();
        }
        return 0;
    }

    /**
     * 设置制作歌词内容
     *
     * @param lrcComText 制作歌词内容文本
     */
    public void setMakeLrcComText(String lrcComText, boolean isInvalidateView) {
        synchronized (lock) {
            initLrcData(isInvalidateView);
            if (!TextUtils.isEmpty(lrcComText)) {
                String lrcComTexts[] = lrcComText.split("\n");
                for (int i = 0; i < lrcComTexts.length; i++) {
                    String lineLyrics = lrcComTexts[i];
                    if (TextUtils.isEmpty(lineLyrics)) {
                        continue;
                    }

                    //设置每行歌词内容
                    LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
                    lyricsLineInfo.setLineLyrics(lineLyrics);

                    //获取该行歌词的字数组
                    String[] mLyricsWords = LyricsUtils.getLyricsWords(lineLyrics);
                    lyricsLineInfo.setLyricsWords(mLyricsWords);

                    //添加数据
                    MakeLrcLineInfo makeLrcLineInfo = new MakeLrcLineInfo();
                    makeLrcLineInfo.setLyricsLineInfo(lyricsLineInfo);
                    mMakeLrcLineInfos.add(makeLrcLineInfo);
                }
            }
            //刷新
            if (isInvalidateView) {
                invalidateView();
            }
        }
    }

    /**
     * 重置数据
     */
    public void initLrcData(boolean isInvalidateView) {
        if (mMakeLrcLineInfos != null) mMakeLrcLineInfos.clear();
        else mMakeLrcLineInfos = new ArrayList<MakeLrcLineInfo>();
        mLyricsLineNum = 0;
        mMakeLyricsLineNum = 0;
        mScroller.setFinalY(0);
        mOffsetY = 0;
        mCentreY = 0;
        //刷新
        if (isInvalidateView) {
            invalidateView();
        }
    }

    /**
     * @throws
     * @Description: 刷新视图
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-04-21 9:24
     */
    public synchronized void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    public String getDefText() {
        return mDefText;
    }

    public void setDefText(String defText) {
        this.mDefText = defText;
    }

    public void setPaintColors(int[] paintColors) {
        this.mPaintColors = paintColors;
    }

    public void setPaintHLColors(int[] paintHLColors) {
        this.mPaintHLColors = paintHLColors;
    }

    public void setSpaceLineHeight(float spaceLineHeight) {
        this.mSpaceLineHeight = spaceLineHeight;
    }

    public void setFontSize(float fontSize) {
        this.mFontSize = fontSize;

        mPaint.setTextSize(mFontSize);
        mPaintHL.setTextSize(mFontSize);
        mPaintOutline.setTextSize(mFontSize);
        mPaintRect.setTextSize(mFontSize);
    }

    public void setPaintLineColor(int paintLineColor) {
        this.mPaintLineColor = paintLineColor;
        mPaintLine.setColor(mPaintLineColor);
    }

    public void setPaintRectColor(int paintRectColor) {
        this.mPaintRectColor = paintRectColor;
        mPaintRect.setColor(mPaintRectColor);
    }

    public void setLineFontSize(float lineFontSize) {
        this.mLineFontSize = lineFontSize;
        mPaintLine.setTextSize(mLineFontSize);
    }

    public List<MakeLrcLineInfo> getMakeLrcLineInfos() {
        return mMakeLrcLineInfos;
    }

    /**
     * @return
     */
    public LyricsInfo getFinishLyricsInfo() {
        if (mMakeLrcLineInfos != null && mMakeLrcLineInfos.size() > 0) {
            boolean isFinish = true;
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfoTreeMap = new TreeMap<Integer, LyricsLineInfo>();
            for (int i = 0; i < mMakeLrcLineInfos.size(); i++) {
                MakeLrcLineInfo makeLrcLineInfo = mMakeLrcLineInfos.get(i);

                if (makeLrcLineInfo.getStatus() != MakeLrcLineInfo.STATUS_FINISH) {
                    isFinish = false;
                    break;
                } else {
                    LyricsLineInfo lyricsLineInfo = makeLrcLineInfo.getFinishLrcLineInfo();
                    lyricsLineInfoTreeMap.put(i, lyricsLineInfo);
                }
            }
            if (isFinish) {
                LyricsInfo lyricsInfo = new LyricsInfo();
                lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfoTreeMap);
                return lyricsInfo;
            }
        }
        return null;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    /**
     * 歌词滚动
     */
    public interface OnScrollListener {
        /**
         * 获取滑动停止时的播放时间
         *
         * @return
         */
        int getScrollStopPlayTime();

        /**
         * 获取当前的播放进度
         */
        int getCurPlayTime();

        /**
         * 歌词滑动时，需要校验当前的播放进度
         *
         * @param seektoProgress
         */
        void seekTo(int seektoProgress);
    }
}
