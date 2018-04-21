package com.zlm.hp.lyrics.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.utils.LyricsUtils;

import java.util.List;
import java.util.TreeMap;

/**
 * @Description: 双行歌词，支持翻译（该歌词在这里只以动感歌词的形式显示）和音译歌词（注：不支持lrc歌词的显示）
 * @author: zhangliangming
 * @date: 2018-04-21 11:43
 **/
public class FloatLyricsView extends LinearLayout {
    /**
     *
     */
    private FloatAbstractLrcView mAbstractLrcView;

    public FloatLyricsView(Context context) {
        super(context);
        mAbstractLrcView = new FloatAbstractLrcView(context);
        init(context);
    }

    public FloatLyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAbstractLrcView = new FloatAbstractLrcView(context, attrs);
        init(context);
    }

    /**
     * @throws
     * @Description: 初始
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-04-21 9:08
     */
    private void init(Context context) {

        //添加歌词视图
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(Color.TRANSPARENT);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        addView(mAbstractLrcView, 0, layoutParams);

        //加载完成后回调
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                viewLoadFinish();
            }
        });
    }

    /**
     * view加载完成
     */
    private void viewLoadFinish() {

        //设置歌词的最大宽度
        int textMaxWidth = getWidth() / 3 * 2;
        mAbstractLrcView.setTextMaxWidth(textMaxWidth);

        //字体大小
        float fontSize = getHeight() / 4;
        float spaceLineHeight = fontSize / 2;

        //设置额外歌词字体大小和空行高度
        float extraLrcFontSize = fontSize;
        float extraLrcSpaceLineHeight = spaceLineHeight;

        //设置额外歌词字体大小和空行高度
        setFontSize(fontSize);
        setSpaceLineHeight(spaceLineHeight);

        setExtraLrcFontSize(extraLrcFontSize);
        setExtraLrcSpaceLineHeight(extraLrcSpaceLineHeight);


    }

    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void drawFloatLrcView(Canvas canvas) {
        int extraLrcStatus = mAbstractLrcView.getExtraLrcStatus();
        //绘画歌词
        if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC) {
            //只显示默认歌词
            drawDynamicLyrics(canvas);
        } else {
            //显示翻译歌词 OR 音译歌词
            drawDynamiAndExtraLyrics(canvas);
        }

    }

    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void drawDynamicLyrics(Canvas canvas) {
        //获取数据
        LyricsReader lyricsReader = mAbstractLrcView.getLyricsReader();
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mAbstractLrcView.getLrcLineInfos();
        int lyricsLineNum = mAbstractLrcView.getLyricsLineNum();
        int splitLyricsLineNum = mAbstractLrcView.getSplitLyricsLineNum();
        int splitLyricsWordIndex = mAbstractLrcView.getSplitLyricsWordIndex();
        float lyricsWordHLTime = mAbstractLrcView.getLyricsWordHLTime();
        Paint paint = mAbstractLrcView.getPaint();
        Paint paintHL = mAbstractLrcView.getPaintHL();
        Paint paintOutline = mAbstractLrcView.getPaintOutline();
        int[] paintColors = mAbstractLrcView.getPaintColors();
        int[] paintHLColors = mAbstractLrcView.getPaintHLColors();
        float spaceLineHeight = mAbstractLrcView.getSpaceLineHeight();
        float paddingLeftOrRight = mAbstractLrcView.getPaddingLeftOrRight();


        // 先设置当前歌词，之后再根据索引判断是否放在左边还是右边
        List<LyricsLineInfo> splitLyricsLineInfos = lrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
        LyricsLineInfo lyricsLineInfo = splitLyricsLineInfos.get(splitLyricsLineNum);
        //获取行歌词高亮宽度
        float lineLyricsHLWidth = LyricsUtils.getLineLyricsHLWidth(lyricsReader.getLyricsType(), paint, lyricsLineInfo, splitLyricsWordIndex, lyricsWordHLTime);
        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = LyricsUtils.getTextWidth(paint, curLyrics);
        // 当前歌词行的x坐标
        float textX = 0;
        // 当前歌词行的y坐标
        float textY = 0;
        int splitLyricsRealLineNum = LyricsUtils.getSplitLyricsRealLineNum(lrcLineInfos, lyricsLineNum, splitLyricsLineNum);
        float topPadding = (getHeight() - spaceLineHeight - 2 * LyricsUtils.getTextHeight(paint)) / 2;
        if (splitLyricsRealLineNum % 2 == 0) {

            textX = paddingLeftOrRight;
            textY = topPadding + LyricsUtils.getTextHeight(paint);
            float nextLrcTextY = textY + spaceLineHeight + LyricsUtils.getTextHeight(paint);

            // 画下一句的歌词，该下一句还在该行的分割集合里面
            if (splitLyricsLineNum + 1 < splitLyricsLineInfos.size()) {
                String lrcRightText = splitLyricsLineInfos.get(
                        splitLyricsLineNum + 1).getLineLyrics();
                float lrcRightTextWidth = LyricsUtils.getTextWidth(paint, lrcRightText);
                float textRightX = getWidth() - lrcRightTextWidth - paddingLeftOrRight;

                LyricsUtils.drawOutline(canvas, paintOutline, lrcRightText, textRightX, nextLrcTextY);

                LyricsUtils.drawText(canvas, paint, paintColors, lrcRightText, textRightX,
                        nextLrcTextY);

            } else if (lyricsLineNum + 1 < lrcLineInfos.size()) {
                // 画下一句的歌词，该下一句不在该行分割歌词里面，需要从原始下一行的歌词里面找
                List<LyricsLineInfo> nextSplitLyricsLineInfos = lrcLineInfos.get(lyricsLineNum + 1).getSplitLyricsLineInfos();
                String lrcRightText = nextSplitLyricsLineInfos.get(0).getLineLyrics();
                float lrcRightTextWidth = LyricsUtils.getTextWidth(paint, lrcRightText);
                float textRightX = getWidth() - lrcRightTextWidth - paddingLeftOrRight;

                LyricsUtils.drawOutline(canvas, paintOutline, lrcRightText, textRightX,
                        nextLrcTextY);

                LyricsUtils.drawText(canvas, paint, paintColors, lrcRightText, textRightX, nextLrcTextY);
            }

        } else {

            textX = getWidth() - curLrcTextWidth - paddingLeftOrRight;
            float preLrcTextY = topPadding + LyricsUtils.getTextHeight(paint);
            textY = preLrcTextY + spaceLineHeight + LyricsUtils.getTextHeight(paint);

            // 画下一句的歌词，该下一句还在该行的分割集合里面
            if (splitLyricsLineNum + 1 < splitLyricsLineInfos.size()) {
                String lrcLeftText = splitLyricsLineInfos.get(
                        splitLyricsLineNum + 1).getLineLyrics();

                LyricsUtils.drawOutline(canvas, paintOutline, lrcLeftText, paddingLeftOrRight,
                        preLrcTextY);
                LyricsUtils.drawText(canvas, paint, paintColors, lrcLeftText, paddingLeftOrRight,
                        preLrcTextY);

            } else if (lyricsLineNum + 1 < lrcLineInfos.size()) {
                // 画下一句的歌词，该下一句不在该行分割歌词里面，需要从原始下一行的歌词里面找
                List<LyricsLineInfo> nextSplitLyricsLineInfos = lrcLineInfos.get(lyricsLineNum + 1).getSplitLyricsLineInfos();
                String lrcLeftText = nextSplitLyricsLineInfos.get(0).getLineLyrics();
                LyricsUtils.drawOutline(canvas, paintOutline, lrcLeftText, paddingLeftOrRight,
                        preLrcTextY);
                LyricsUtils.drawText(canvas, paint, paintColors, lrcLeftText, paddingLeftOrRight,
                        preLrcTextY);
            }
        }
        //画歌词
        LyricsUtils.drawOutline(canvas, paintOutline, curLyrics, textX, textY);
        LyricsUtils.drawDynamicText(canvas, paint, paintHL, paintColors, paintHLColors, curLyrics, lineLyricsHLWidth, textX, textY);
    }


    /**
     * 绘画歌词和额外歌词
     *
     * @param canvas
     */
    private void drawDynamiAndExtraLyrics(Canvas canvas) {
        //获取数据
        LyricsReader lyricsReader = mAbstractLrcView.getLyricsReader();
        Paint paint = mAbstractLrcView.getPaint();
        Paint paintHL = mAbstractLrcView.getPaintHL();
        Paint paintOutline = mAbstractLrcView.getPaintOutline();
        Paint extraLrcPaint = mAbstractLrcView.getExtraLrcPaint();
        Paint extraLrcPaintHL = mAbstractLrcView.getExtraLrcPaintHL();
        Paint extraLrcPaintOutline = mAbstractLrcView.getExtraLrcPaintOutline();
        int[] paintColors = mAbstractLrcView.getPaintColors();
        int[] paintHLColors = mAbstractLrcView.getPaintHLColors();
        int extraLrcStatus = mAbstractLrcView.getExtraLrcStatus();
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mAbstractLrcView.getLrcLineInfos();
        int lyricsLineNum = mAbstractLrcView.getLyricsLineNum();
        int lyricsWordIndex = mAbstractLrcView.getLyricsWordIndex();
        int extraLyricsWordIndex = mAbstractLrcView.getExtraLyricsWordIndex();
        float lyricsWordHLTime = mAbstractLrcView.getLyricsWordHLTime();
        float translateLyricsWordHLTime = mAbstractLrcView.getTranslateLyricsWordHLTime();
        float extraLrcSpaceLineHeight = mAbstractLrcView.getExtraLrcSpaceLineHeight();
        float paddingLeftOrRight = mAbstractLrcView.getPaddingLeftOrRight();
        int translateDrawType = mAbstractLrcView.getTranslateDrawType();
        List<LyricsLineInfo> translateLrcLineInfos = mAbstractLrcView.getTranslateLrcLineInfos();
        List<LyricsLineInfo> transliterationLrcLineInfos = mAbstractLrcView.getTransliterationLrcLineInfos();

        //
        float topPadding = (getHeight() - extraLrcSpaceLineHeight - LyricsUtils.getTextHeight(paint) - LyricsUtils.getTextHeight(extraLrcPaint)) / 2;
        // 当前歌词行的y坐标
        float lrcTextY = topPadding + LyricsUtils.getTextHeight(paint);
        //额外歌词行的y坐标
        float extraLrcTextY = lrcTextY + extraLrcSpaceLineHeight + LyricsUtils.getTextHeight(extraLrcPaint);

        LyricsLineInfo lyricsLineInfo = lrcLineInfos.get(lyricsLineNum);
        //获取行歌词高亮宽度
        float lineLyricsHLWidth = LyricsUtils.getLineLyricsHLWidth(lyricsReader.getLyricsType(), paint, lyricsLineInfo, lyricsWordIndex, lyricsWordHLTime);
        //画默认歌词
        LyricsUtils.drawDynamiLyrics(canvas, lyricsReader.getLyricsType(), paint, paintHL, paintOutline, lyricsLineInfo, lineLyricsHLWidth, getWidth(), lyricsWordIndex, lyricsWordHLTime, lrcTextY, paddingLeftOrRight, paintColors, paintHLColors);

        //显示翻译歌词
        if (lyricsReader.getLyricsType() == LyricsInfo.DYNAMIC && extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC && translateDrawType == AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC) {

            LyricsLineInfo translateLyricsLineInfo = translateLrcLineInfos.get(lyricsLineNum);
            float extraLyricsLineHLWidth = LyricsUtils.getLineLyricsHLWidth(lyricsReader.getLyricsType(), extraLrcPaint, translateLyricsLineInfo, extraLyricsWordIndex, translateLyricsWordHLTime);
            //画翻译歌词
            LyricsUtils.drawDynamiLyrics(canvas, lyricsReader.getLyricsType(), extraLrcPaint, extraLrcPaintHL, extraLrcPaintOutline, translateLyricsLineInfo, extraLyricsLineHLWidth, getWidth(), extraLyricsWordIndex, translateLyricsWordHLTime, extraLrcTextY, paddingLeftOrRight, paintColors, paintHLColors);

        } else {
            LyricsLineInfo transliterationLineInfo = transliterationLrcLineInfos.get(lyricsLineNum);
            float extraLyricsLineHLWidth = LyricsUtils.getLineLyricsHLWidth(lyricsReader.getLyricsType(), extraLrcPaint, transliterationLineInfo, extraLyricsWordIndex, lyricsWordHLTime);
            //画音译歌词
            LyricsUtils.drawDynamiLyrics(canvas, lyricsReader.getLyricsType(), extraLrcPaint, extraLrcPaintHL, extraLrcPaintOutline, transliterationLineInfo, extraLyricsLineHLWidth, getWidth(), extraLyricsWordIndex, lyricsWordHLTime, extraLrcTextY, paddingLeftOrRight, paintColors, paintHLColors);

        }

    }


    /**
     * 更新歌词视图
     *
     * @param playProgress
     */
    private void updateFloatLrcView(long playProgress) {

        LyricsReader lyricsReader = mAbstractLrcView.getLyricsReader();
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mAbstractLrcView.getLrcLineInfos();
        int lyricsLineNum = LyricsUtils.getLineNumber(lyricsReader.getLyricsType(), lrcLineInfos, playProgress, lyricsReader.getPlayOffset());
        mAbstractLrcView.setLyricsLineNum(lyricsLineNum);
        mAbstractLrcView.updateSplitData(playProgress);
    }

    /**
     * 获取额外歌词类型
     *
     * @return
     */
    public int getExtraLrcType() {
        return mAbstractLrcView.getExtraLrcType();
    }

    /**
     * 获取额外歌词的显示状态
     *
     * @return
     */
    public int getExtraLrcStatus() {
        return mAbstractLrcView.getExtraLrcStatus();
    }

    /**
     * 获取歌词状态
     *
     * @return
     */
    public int getLrcStatus() {
        return mAbstractLrcView.getLrcStatus();
    }

    /**
     * 初始歌词数据
     */
    public void initLrcData() {
        mAbstractLrcView.initLrcData();
    }

    /**
     * 设置歌词状态
     *
     * @param lrcStatus
     */
    public void setLrcStatus(int lrcStatus) {
        mAbstractLrcView.setLrcStatus(lrcStatus);
    }

    /**
     * 设置默认颜色
     *
     * @param paintColor
     */
    public void setPaintColor(int[] paintColor) {
        setPaintColor(paintColor, false);
    }

    /**
     * 设置默认颜色
     *
     * @param paintColor       至少两种颜色
     * @param isInvalidateView 是否更新视图
     */
    public void setPaintColor(int[] paintColor, boolean isInvalidateView) {
        mAbstractLrcView.setPaintColor(paintColor, isInvalidateView);
    }


    /**
     * 设置高亮颜色
     *
     * @param paintHLColor
     */
    public void setPaintHLColor(int[] paintHLColor) {
        setPaintHLColor(paintHLColor, false);
    }

    /**
     * 设置高亮颜色
     *
     * @param paintHLColor     至少两种颜色
     * @param isInvalidateView 是否更新视图
     */
    public void setPaintHLColor(int[] paintHLColor, boolean isInvalidateView) {
        mAbstractLrcView.setPaintHLColor(paintHLColor, isInvalidateView);
    }

    /**
     * 设置字体文件
     *
     * @param typeFace
     */
    public void setTypeFace(Typeface typeFace) {
        setTypeFace(typeFace, false);
    }

    /**
     * 设置字体文件
     *
     * @param typeFace
     * @param isInvalidateView 是否更新视图
     */
    public void setTypeFace(Typeface typeFace, boolean isInvalidateView) {
        mAbstractLrcView.setTypeFace(typeFace, isInvalidateView);
    }

    /**
     * 设置额外歌词事件
     *
     * @param extraLyricsListener
     */
    public void setExtraLyricsListener(AbstractLrcView.ExtraLyricsListener extraLyricsListener) {
        mAbstractLrcView.setExtraLyricsListener(extraLyricsListener);
    }

    /**
     * 设置搜索歌词事件
     *
     * @param searchLyricsListener
     */
    public void setSearchLyricsListener(AbstractLrcView.SearchLyricsListener searchLyricsListener) {
        mAbstractLrcView.setSearchLyricsListener(searchLyricsListener);
    }

    /**
     * 歌词播放
     *
     * @param playProgress
     */
    public void play(int playProgress) {
        mAbstractLrcView.play(playProgress);
    }

    /**
     * 歌词暂停
     */
    public void pause() {
        mAbstractLrcView.pause();
    }

    /**
     * 快进
     *
     * @param playProgress
     */
    public void seekto(int playProgress) {
        mAbstractLrcView.seekto(playProgress);
    }

    /**
     * 唤醒
     */
    public void resume() {
        mAbstractLrcView.resume();
    }

    /**
     * 获取歌词播放器状态
     *
     * @return
     */
    public int getLrcPlayerStatus() {
        return mAbstractLrcView.getLrcPlayerStatus();
    }

    /**
     * 获取分隔歌词的开始时间
     *
     * @param playProgress
     * @return
     */
    public int getSplitLineLrcStartTime(int playProgress) {
        return mAbstractLrcView.getSplitLineLrcStartTime(playProgress);
    }

    /**
     * 获取行歌词的开始时间
     *
     * @param playProgress
     * @return
     */
    public int getLineLrcStartTime(int playProgress) {
        return mAbstractLrcView.getLineLrcStartTime(playProgress);
    }

    /**
     * 获取分隔行的歌词内容
     *
     * @param playProgress
     * @return
     */
    public String getSplitLineLrc(int playProgress) {
        return mAbstractLrcView.getSplitLineLrc(playProgress);
    }

    /**
     * 获取行的歌词内容
     *
     * @param playProgress
     * @return
     */
    public String getLineLrc(int playProgress) {
        return mAbstractLrcView.getLineLrc(playProgress);
    }

    /**
     * 设置空行高度
     *
     * @param spaceLineHeight
     */
    public void setSpaceLineHeight(float spaceLineHeight) {
        setSpaceLineHeight(spaceLineHeight, false);
    }

    /**
     * 设置空行高度
     *
     * @param spaceLineHeight
     * @param isInvalidateView 是否更新视图
     */
    public void setSpaceLineHeight(float spaceLineHeight, boolean isInvalidateView) {
        mAbstractLrcView.setSpaceLineHeight(spaceLineHeight, isInvalidateView);
    }

    /**
     * 设置额外空行高度
     *
     * @param extraLrcSpaceLineHeight
     */
    public void setExtraLrcSpaceLineHeight(float extraLrcSpaceLineHeight) {
        setExtraLrcSpaceLineHeight(extraLrcSpaceLineHeight, false);
    }

    /**
     * 设置额外空行高度
     *
     * @param extraLrcSpaceLineHeight
     * @param isInvalidateView        是否更新视图
     */
    public void setExtraLrcSpaceLineHeight(float extraLrcSpaceLineHeight, boolean isInvalidateView) {
        mAbstractLrcView.setExtraLrcSpaceLineHeight(extraLrcSpaceLineHeight, isInvalidateView);
    }

    /**
     * 设置额外歌词的显示状态
     *
     * @param extraLrcStatus
     */
    public void setExtraLrcStatus(int extraLrcStatus) {
        mAbstractLrcView.setExtraLrcStatus(extraLrcStatus);
    }

    /**
     * 设置歌词解析器
     *
     * @param lyricsReader
     */
    public void setLyricsReader(LyricsReader lyricsReader) {
        mAbstractLrcView.setLyricsReader(lyricsReader);
        if (lyricsReader != null && lyricsReader.getLyricsType() == LyricsInfo.DYNAMIC) {
            int extraLrcType = mAbstractLrcView.getExtraLrcType();
            //翻译歌词以动感歌词形式显示
            if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH || extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
                mAbstractLrcView.setTranslateDrawType(AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC);
            }
        } else {
            mAbstractLrcView.setLrcStatus(AbstractLrcView.LRCSTATUS_NONSUPPORT);
        }
    }

    /**
     *
     * @return
     */
    public LyricsReader getLyricsReader() {
        return mAbstractLrcView.getLyricsReader();
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param extraFontSize 额外歌词字体
     */
    public void setSize(int fontSize, int extraFontSize) {
        setSize(fontSize, extraFontSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param extraFontSize 额外歌词字体
     * @param isReloadData  是否重新加载数据及刷新界面
     */
    public void setSize(int fontSize, int extraFontSize, boolean isReloadData) {
        mAbstractLrcView.setSize(fontSize, extraFontSize, isReloadData);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     */
    public void setFontSize(float fontSize) {
        setFontSize(fontSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param isReloadData 是否重新加载数据及刷新界面
     */
    public void setFontSize(float fontSize, boolean isReloadData) {
        mAbstractLrcView.setFontSize(fontSize, isReloadData);
    }

    /**
     * 设置额外字体大小
     *
     * @param extraLrcFontSize
     */
    public void setExtraLrcFontSize(float extraLrcFontSize) {
        setExtraLrcFontSize(extraLrcFontSize, false);
    }

    /**
     * 设置额外字体大小
     *
     * @param extraLrcFontSize
     * @param isReloadData     是否重新加载数据及刷新界面
     */
    public void setExtraLrcFontSize(float extraLrcFontSize, boolean isReloadData) {
        mAbstractLrcView.setExtraLrcFontSize(extraLrcFontSize, isReloadData);
    }

    public FloatAbstractLrcView getmAbstractLrcView() {
        return mAbstractLrcView;
    }

    /**
     * @Description: 双行歌词抽象类
     * @author: zhangliangming
     * @date: 2018-04-21 11:46
     **/
    private class FloatAbstractLrcView extends AbstractLrcView {

        public FloatAbstractLrcView(Context context) {
            super(context);
        }

        public FloatAbstractLrcView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onDrawLrcView(Canvas canvas) {
            drawFloatLrcView(canvas);
        }

        @Override
        protected void updateView(long playProgress) {
            updateFloatLrcView(playProgress);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }
    }

}
