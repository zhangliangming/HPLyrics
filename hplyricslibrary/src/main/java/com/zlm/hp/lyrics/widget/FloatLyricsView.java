package com.zlm.hp.lyrics.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

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
public class FloatLyricsView extends AbstractLrcView {


    public FloatLyricsView(Context context) {
        super(context);
        init(context);
    }

    public FloatLyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

        //加载完成后回调
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                viewLoadFinish();
            }
        });
    }

    @Override
    protected void onDrawLrcView(Canvas canvas) {
        drawFloatLrcView(canvas);
    }

    @Override
    protected void updateView(long playProgress) {
        updateFloatLrcView(playProgress);
    }

    /**
     * view加载完成
     */
    private void viewLoadFinish() {

        //设置歌词的最大宽度
        int textMaxWidth = getWidth() / 3 * 2;
        setTextMaxWidth(textMaxWidth);

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
        int extraLrcStatus = getExtraLrcStatus();
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
        LyricsReader lyricsReader = getLyricsReader();
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        int lyricsLineNum = getLyricsLineNum();
        int splitLyricsLineNum = getSplitLyricsLineNum();
        int splitLyricsWordIndex = getSplitLyricsWordIndex();
        float lyricsWordHLTime = getLyricsWordHLTime();
        Paint paint = getPaint();
        Paint paintHL = getPaintHL();
        Paint paintOutline = getPaintOutline();
        int[] paintColors = getPaintColors();
        int[] paintHLColors = getPaintHLColors();
        float spaceLineHeight = getSpaceLineHeight();
        float paddingLeftOrRight = getPaddingLeftOrRight();


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
        LyricsReader lyricsReader = getLyricsReader();
        Paint paint = getPaint();
        Paint paintHL = getPaintHL();
        Paint paintOutline = getPaintOutline();
        Paint extraLrcPaint = getExtraLrcPaint();
        Paint extraLrcPaintHL = getExtraLrcPaintHL();
        Paint extraLrcPaintOutline = getExtraLrcPaintOutline();
        int[] paintColors = getPaintColors();
        int[] paintHLColors = getPaintHLColors();
        int extraLrcStatus = getExtraLrcStatus();
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        int lyricsLineNum = getLyricsLineNum();
        int lyricsWordIndex = getLyricsWordIndex();
        int extraLyricsWordIndex = getExtraLyricsWordIndex();
        float lyricsWordHLTime = getLyricsWordHLTime();
        float translateLyricsWordHLTime = getTranslateLyricsWordHLTime();
        float extraLrcSpaceLineHeight = getExtraLrcSpaceLineHeight();
        float paddingLeftOrRight = getPaddingLeftOrRight();
        int translateDrawType = getTranslateDrawType();
        List<LyricsLineInfo> translateLrcLineInfos = getTranslateLrcLineInfos();
        List<LyricsLineInfo> transliterationLrcLineInfos = getTransliterationLrcLineInfos();

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

        LyricsReader lyricsReader = getLyricsReader();
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        int lyricsLineNum = LyricsUtils.getLineNumber(lyricsReader.getLyricsType(), lrcLineInfos, playProgress, lyricsReader.getPlayOffset());
        setLyricsLineNum(lyricsLineNum);
        updateSplitData(playProgress);
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
     * 设置高亮颜色
     *
     * @param paintHLColor
     */
    public void setPaintHLColor(int[] paintHLColor) {
        setPaintHLColor(paintHLColor, false);
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
     * 设置空行高度
     *
     * @param spaceLineHeight
     */
    public void setSpaceLineHeight(float spaceLineHeight) {
        setSpaceLineHeight(spaceLineHeight, false);
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
     * 设置歌词解析器
     *
     * @param lyricsReader
     */
    public void setLyricsReader(LyricsReader lyricsReader) {
        super.setLyricsReader(lyricsReader);
        if (lyricsReader != null && lyricsReader.getLyricsType() == LyricsInfo.DYNAMIC) {
            int extraLrcType = getExtraLrcType();
            //翻译歌词以动感歌词形式显示
            if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH || extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
                setTranslateDrawType(AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC);
            }
        } else {
            setLrcStatus(AbstractLrcView.LRCSTATUS_NONSUPPORT);
        }
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
     */
    public void setFontSize(float fontSize) {
        setFontSize(fontSize, false);
    }


    /**
     * 设置额外字体大小
     *
     * @param extraLrcFontSize
     */
    public void setExtraLrcFontSize(float extraLrcFontSize) {
        setExtraLrcFontSize(extraLrcFontSize, false);
    }

}
