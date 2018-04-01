package com.zlm.hp.lyrics.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.TranslateLrcLineInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

/**
 * 歌词处理类
 * Created by zhangliangming on 2018-02-23.
 */

public class LyricsUtils {

    /**
     * 获取双行动感歌词图片
     *
     * @param lyricsType         歌词类型
     * @param viewWidth          歌词视图宽度
     * @param viewHeight         歌词视图高度
     * @param textMaxWidth       行歌词最长长度
     * @param lineHeight         行高度
     * @param paddingLeftOrRight 左右间隔大小
     * @param paint              默认画笔
     * @param paintHL            高亮画笔
     * @param paintOutline       轮廓画笔
     * @param paintColor         默认画笔颜色
     * @param paintHLColor       高亮画笔颜色
     * @param defLrcLineInfos    歌词集合
     * @param curPlayingTime     当前播放时间
     * @param playOffset         歌词时间增量
     * @return
     */
    public static Bitmap getDynamicLyricsImage(int lyricsType, int viewWidth, int viewHeight, int textMaxWidth, float lineHeight, float paddingLeftOrRight, Paint paint, Paint paintHL, Paint paintOutline, int[] paintColor, int[] paintHLColor, TreeMap<Integer, LyricsLineInfo> defLrcLineInfos, long curPlayingTime, long playOffset) {
        Bitmap result = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        int textHeight = getTextHeight(paint);
        float topOrBottom = (viewHeight - 2 * textHeight - lineHeight) / 2;
        //当前行号
        int lyricsLineNum = getLineNumber(lyricsType, defLrcLineInfos, curPlayingTime, playOffset);
        //动感歌词
        if (lyricsType == LyricsInfo.DYNAMIC) {
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = getSplitDynamicLyrics(defLrcLineInfos, textMaxWidth, paint);
            //获取分隔后的当前行号
            int splitLyricsLineNum = getSplitDynamicLyricsLineNum(lyricsLineInfos, lyricsLineNum, curPlayingTime, playOffset);
            //获取当前歌词字索引
            int lyricsWordIndex = getLyricsWordIndex(lyricsLineInfos, lyricsLineNum, curPlayingTime, playOffset);
            //获取当前分隔行的字索引
            int splitLyricsWordIndex = getSplitLyricsWordIndex(lyricsLineInfos, lyricsLineNum, curPlayingTime, playOffset);
            //获取当前字的高亮时间
            long lyricsWordHLTime = getDisWordsIndexLenTime(lyricsLineInfos, lyricsLineNum, curPlayingTime, playOffset);
            //获取分隔后的歌词行号
            int splitLyricsRealLineNum = getSplitLyricsRealLineNum(lyricsLineInfos, lyricsLineNum, splitLyricsLineNum);

            //绘画歌词
            List<LyricsLineInfo> splitLyricsLineInfos = lyricsLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
            LyricsLineInfo lyricsLineInfo = splitLyricsLineInfos.get(splitLyricsLineNum);
            //获取行高亮宽度
            float lineLyricsHLWidth = getLineLyricsHLWidth(lyricsType, paint, lyricsLineInfo, splitLyricsWordIndex, lyricsWordHLTime);
            // 当行歌词
            String curLyrics = lyricsLineInfo.getLineLyrics();
            float curLrcTextWidth = getTextWidth(paint, curLyrics);
            // 当前歌词行的x坐标
            float textX = 0;
            // 当前歌词行的y坐标
            float textY = 0;
            if (splitLyricsRealLineNum % 2 == 0) {

                textX = paddingLeftOrRight;
                textY = topOrBottom + getTextHeight(paint);
                float nextLrcTextY = textY + lineHeight + getTextHeight(paint);

                // 画下一句的歌词，该下一句还在该行的分割集合里面
                if (splitLyricsLineNum + 1 < splitLyricsLineInfos.size()) {
                    String lrcRightText = splitLyricsLineInfos.get(
                            splitLyricsLineNum + 1).getLineLyrics();
                    float lrcRightTextWidth = getTextWidth(paint, lrcRightText);
                    float textRightX = viewWidth - lrcRightTextWidth - paddingLeftOrRight;

                    drawOutline(canvas, paintOutline, lrcRightText, textRightX, nextLrcTextY);

                    drawText(canvas, paint, paintColor, lrcRightText, textRightX,
                            nextLrcTextY);

                } else if (lyricsLineNum + 1 < lyricsLineInfos.size()) {
                    // 画下一句的歌词，该下一句不在该行分割歌词里面，需要从原始下一行的歌词里面找
                    List<LyricsLineInfo> nextSplitLyricsLineInfos = lyricsLineInfos.get(lyricsLineNum + 1).getSplitLyricsLineInfos();
                    String lrcRightText = nextSplitLyricsLineInfos.get(0).getLineLyrics();
                    float lrcRightTextWidth = getTextWidth(paint, lrcRightText);
                    float textRightX = viewWidth - lrcRightTextWidth - paddingLeftOrRight;

                    drawOutline(canvas, paintOutline, lrcRightText, textRightX,
                            nextLrcTextY);

                    drawText(canvas, paint, paintColor, lrcRightText, textRightX, nextLrcTextY);
                }

            } else {

                textX = viewWidth - curLrcTextWidth - paddingLeftOrRight;
                float preLrcTextY = topOrBottom + getTextHeight(paint);
                textY = preLrcTextY + lineHeight + getTextHeight(paint);
                // 画下一句的歌词，该下一句还在该行的分割集合里面
                if (splitLyricsLineNum + 1 < splitLyricsLineInfos.size()) {
                    String lrcLeftText = splitLyricsLineInfos.get(
                            splitLyricsLineNum + 1).getLineLyrics();

                    drawOutline(canvas, paintOutline, lrcLeftText, paddingLeftOrRight,
                            preLrcTextY);
                    drawText(canvas, paint, paintColor, lrcLeftText, paddingLeftOrRight,
                            preLrcTextY);

                } else if (lyricsLineNum + 1 < lyricsLineInfos.size()) {
                    // 画下一句的歌词，该下一句不在该行分割歌词里面，需要从原始下一行的歌词里面找
                    List<LyricsLineInfo> nextSplitLyricsLineInfos = lyricsLineInfos.get(lyricsLineNum + 1).getSplitLyricsLineInfos();
                    String lrcLeftText = nextSplitLyricsLineInfos.get(0).getLineLyrics();
                    drawOutline(canvas, paintOutline, lrcLeftText, paddingLeftOrRight,
                            preLrcTextY);
                    drawText(canvas, paint, paintColor, lrcLeftText, paddingLeftOrRight,
                            preLrcTextY);
                }
            }

            //画歌词
            drawOutline(canvas, paintOutline, curLyrics, textX, textY);
            drawDynamicText(canvas, paint, paintHL, paintColor, paintHLColor, curLyrics, lineLyricsHLWidth, textX, textY);


        } else {

            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = getSplitLrcLyrics(defLrcLineInfos, textMaxWidth, paint);
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(lyricsLineNum);
            // 当行歌词
            String curLyrics = lyricsLineInfo.getLineLyrics();
            float curLrcTextWidth = getTextWidth(paint, curLyrics);

            // 当前歌词行的x坐标
            float textX = 0;
            // 当前歌词行的y坐标
            float textY = 0;
            //lrc不做任何处理，直接绘画
            if (lyricsLineNum % 2 == 0) {

                textX = paddingLeftOrRight;
                textY = topOrBottom + getTextHeight(paint);
                float nextLrcTextY = textY + lineHeight + getTextHeight(paint);
                if (lyricsLineNum + 1 < lyricsLineInfos.size()) {
                    //画下一句歌词
                    String lrcRightText = lyricsLineInfos.get(lyricsLineNum + 1).getLineLyrics();
                    float lrcRightTextWidth = getTextWidth(paint, lrcRightText);
                    float textRightX = viewWidth - lrcRightTextWidth - paddingLeftOrRight;

                    drawOutline(canvas, paintOutline, lrcRightText, textRightX,
                            nextLrcTextY);

                    drawText(canvas, paint, paintColor, lrcRightText, textRightX, nextLrcTextY);
                }
            } else {
                textX = viewWidth - curLrcTextWidth - paddingLeftOrRight;
                float preLrcTextY = topOrBottom + getTextHeight(paint);
                textY = preLrcTextY + lineHeight + getTextHeight(paint);
                if (lyricsLineNum + 1 < lyricsLineInfos.size()) {
                    //绘画下一句歌词
                    String lrcLeftText = lyricsLineInfos.get(lyricsLineNum + 1).getLineLyrics();
                    drawOutline(canvas, paintOutline, lrcLeftText, paddingLeftOrRight,
                            preLrcTextY);
                    drawText(canvas, paint, paintColor, lrcLeftText, paddingLeftOrRight,
                            preLrcTextY);
                }
            }

            //画歌词
            drawOutline(canvas, paintOutline, curLyrics, textX, textY);
            drawText(canvas, paintHL, paintHLColor, curLyrics, textX, textY);
        }
        // 保存绘图
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return result;
    }

    /**
     * 获取单行动感歌词和单行额外歌词
     *
     * @param lyricsType         歌词类型
     * @param viewWidth          歌词视图宽度
     * @param viewHeight         歌词视图高度
     * @param lineHeight         行高度
     * @param paddingLeftOrRight 左右间隔大小
     * @param paint              默认画笔
     * @param paintHL            高亮画笔
     * @param paintOutline       轮廓画笔
     * @param paintColor         默认画笔颜色
     * @param paintHLColor       高亮画笔颜色
     * @param lyricsLineInfos    默认歌词集合
     * @param extraLrcLineInfos  额外歌词集合
     * @param extraLrcType       1是翻译，0是音译
     * @param curPlayingTime     当前播放时间
     * @param playOffset         歌词时间增量
     * @return
     */
    public static Bitmap getDynamiAndExtraLyricsImage(int lyricsType, int viewWidth, int viewHeight, float lineHeight, float paddingLeftOrRight, Paint paint, Paint paintHL, Paint paintOutline, int[] paintColor, int[] paintHLColor, TreeMap<Integer, LyricsLineInfo> lyricsLineInfos, List<LyricsLineInfo> extraLrcLineInfos, int extraLrcType, long curPlayingTime, long playOffset) {
        Bitmap result = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        int textHeight = getTextHeight(paint);
        float topOrBottom = (viewHeight - 2 * textHeight - lineHeight) / 2;
        float textY = topOrBottom + getTextHeight(paint);
        //当前行号
        int lyricsLineNum = getLineNumber(lyricsType, lyricsLineInfos, curPlayingTime, playOffset);
        //动感歌词
        if (lyricsType == LyricsInfo.DYNAMIC) {
            //获取当前歌词字索引
            int lyricsWordIndex = getLyricsWordIndex(lyricsLineInfos, lyricsLineNum, curPlayingTime, playOffset);
            //获取当前字的高亮时间
            long lyricsWordHLTime = getDisWordsIndexLenTime(lyricsLineInfos, lyricsLineNum, curPlayingTime, playOffset);
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(lyricsLineNum);
            //获取行高亮宽度
            float lineLyricsHLWidth = getLineLyricsHLWidth(lyricsType, paint, lyricsLineInfo, lyricsWordIndex, lyricsWordHLTime);
            // 当行歌词
            String curLyrics = lyricsLineInfo.getLineLyrics();
            float curLrcTextWidth = getTextWidth(paint, curLyrics);

            // 当前歌词行的x坐标
            float textX = getHLMoveTextX(curLrcTextWidth, lineLyricsHLWidth, viewWidth, paddingLeftOrRight);
            drawOutline(canvas, paintOutline, curLyrics, textX, textY);
            drawDynamicText(canvas, paint, paintHL, paintColor, paintHLColor, curLyrics, lineLyricsHLWidth, textX, textY);

        } else {
            //lrc歌词，直接居中绘画歌词
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(lyricsLineNum);
            // 当行歌词
            String curLyrics = lyricsLineInfo.getLineLyrics();
            float curLrcTextWidth = getTextWidth(paint, curLyrics);
            float textX = (viewWidth - curLrcTextWidth) / 2;
            //画歌词
            drawOutline(canvas, paintOutline, curLyrics, textX, textY);
            drawText(canvas, paintHL, paintHLColor, curLyrics, textX, textY);
        }

        //设置额外歌词的y位置
        float preLrcTextY = topOrBottom + getTextHeight(paint);
        textY = preLrcTextY + lineHeight + getTextHeight(paint);

        //额外歌词索引
        int extraLyricsWordIndex = getExtraLyricsWordIndex(extraLrcLineInfos, lyricsLineNum, curPlayingTime, playOffset);
        //绘画额外歌词
        if (extraLrcType == 1) {

            LyricsLineInfo translateLyricsLineInfo = extraLrcLineInfos.get(lyricsLineNum);
            //翻译
            if (lyricsType == LyricsInfo.DYNAMIC) {
                long translateLyricsWordHLTime = getTranslateLrcDisWordsIndexLenTime(extraLrcLineInfos, lyricsLineNum, curPlayingTime, playOffset);
                float translateLyricsLineHLWidth = getLineLyricsHLWidth(lyricsType, paint, translateLyricsLineInfo, extraLyricsWordIndex, translateLyricsWordHLTime);
                //动感歌词，翻译歌词将以动感歌词的形式绘画
                drawDynamiLyrics(canvas, lyricsType, paint, paintHL, paintOutline, translateLyricsLineInfo, translateLyricsLineHLWidth, viewWidth, extraLyricsWordIndex, translateLyricsWordHLTime, textY, paddingLeftOrRight, paintColor, paintHLColor);
            } else {
                //默认绘画lrc歌词
                String lrcText = translateLyricsLineInfo.getLineLyrics();
                float lrcTextWidth = getTextWidth(paint, lrcText);
                float textX = (viewWidth - lrcTextWidth) / 2;
                drawText(canvas, paintHL, paintHLColor, translateLyricsLineInfo.getLineLyrics(), textX, textY);
            }
        } else if (extraLrcType == 0) {
            //音译
            LyricsLineInfo transliterationLineInfo = extraLrcLineInfos.get(lyricsLineNum);
            //获取当前字的高亮时间
            long lyricsWordHLTime = getDisWordsIndexLenTime(lyricsLineInfos, lyricsLineNum, curPlayingTime, playOffset);
            float transliterationLyricsLineHLWidth = getLineLyricsHLWidth(lyricsType, paint, transliterationLineInfo, extraLyricsWordIndex, lyricsWordHLTime);
            drawDynamiLyrics(canvas, lyricsType, paint, paintHL, paintOutline, transliterationLineInfo, transliterationLyricsLineHLWidth, viewWidth, extraLyricsWordIndex, transliterationLyricsLineHLWidth, textY, paddingLeftOrRight, paintColor, paintHLColor);
        }
        // 保存绘图
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return result;
    }

    /**
     * 绘画单行动感歌词行
     *
     * @param canvas
     * @param lyricsType         歌词类型
     * @param paint              默认画笔
     * @param paintHL            高亮画笔
     * @param paintOutline       轮廓画笔
     * @param lyricsLineInfo     行歌词数据
     * @param lyricsLineHLWidth  歌词行高亮宽度
     * @param viewWidth          视频宽度
     * @param lyricsWordIndex    行歌词字索引
     * @param lyricsWordHLTime   行歌词高亮时间
     * @param textY              y轴位置
     * @param paddingLeftOrRight
     * @param paintColor
     * @param paintHLColor
     */
    public static void drawDynamiLyrics(Canvas canvas, int lyricsType, Paint paint, Paint paintHL, Paint paintOutline, LyricsLineInfo lyricsLineInfo, float lyricsLineHLWidth, int viewWidth, int lyricsWordIndex, float lyricsWordHLTime, float textY, float paddingLeftOrRight, int[] paintColor, int[] paintHLColor) {
        //获取行歌词高亮宽度
        lyricsLineHLWidth = getLineLyricsHLWidth(lyricsType, paint, lyricsLineInfo, lyricsWordIndex, lyricsWordHLTime);
        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = getTextWidth(paint, curLyrics);
        // 当前歌词行的x坐标
        float textX = getHLMoveTextX(curLrcTextWidth, lyricsLineHLWidth, viewWidth, paddingLeftOrRight);
        drawOutline(canvas, paintOutline, curLyrics, textX, textY);
        drawDynamicText(canvas, paint, paintHL, paintColor, paintHLColor, curLyrics, lyricsLineHLWidth, textX, textY);
    }

    /**
     * 获取高亮移动的x位置（注：该方法在歌词不换行时使用）
     *
     * @param curLrcTextWidth    当前行宽度
     * @param lineLyricsHLWidth  当前行高亮歌词宽度
     * @param viewWidth          视图宽度
     * @param paddingLeftOrRight 左右间隔距离
     * @return
     */
    public static float getHLMoveTextX(float curLrcTextWidth, float lineLyricsHLWidth, int viewWidth, float paddingLeftOrRight) {
        float textX = 0;
        if (curLrcTextWidth > viewWidth) {
            if (lineLyricsHLWidth >= viewWidth / 2) {
                if ((curLrcTextWidth - lineLyricsHLWidth) >= viewWidth / 2) {
                    textX = (viewWidth / 2 - lineLyricsHLWidth);
                } else {
                    textX = viewWidth - curLrcTextWidth
                            - paddingLeftOrRight;
                }
            } else {
                textX = paddingLeftOrRight;
            }
        } else {
            // 如果歌词宽度小于view的宽
            textX = (viewWidth - curLrcTextWidth) / 2;
        }
        return textX;
    }


    /**
     * 获取分隔后的歌词的真正行号
     *
     * @param lyricsLineNum
     * @param splitLyricsLineNum
     * @return
     */
    public static int getSplitLyricsRealLineNum(TreeMap<Integer, LyricsLineInfo> lrcLineInfos, int lyricsLineNum, int splitLyricsLineNum) {
        int realLineNum = 0;
        for (int i = 0; i < lrcLineInfos.size(); i++) {
            if (i != lyricsLineNum) {
                realLineNum += lrcLineInfos.get(i).getSplitLyricsLineInfos().size();
            } else if (i == lyricsLineNum) {
                realLineNum += splitLyricsLineNum;
                break;
            }
        }
        return realLineNum;
    }


    /**
     * 获取行歌词高亮的宽度
     *
     * @param paint
     * @param lyricsLineInfo
     * @param lyricsWordIndex
     * @param lyricsWordHLTime
     * @return
     */
    public static float getLineLyricsHLWidth(int lyricsType, Paint paint, LyricsLineInfo lyricsLineInfo, int lyricsWordIndex, float lyricsWordHLTime) {
        float lineLyricsHLWidth = 0;

        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = LyricsUtils.getTextWidth(paint, curLyrics);
        if (lyricsType == LyricsInfo.LRC || lyricsWordIndex == -2) {
            // 整行歌词
            lineLyricsHLWidth = curLrcTextWidth;
        } else {
            if (lyricsWordIndex != -1) {
                String lyricsWords[] = lyricsLineInfo.getLyricsWords();
                int wordsDisInterval[] = lyricsLineInfo
                        .getWordsDisInterval();
                // 当前歌词之前的歌词
                StringBuilder lyricsBeforeWord = new StringBuilder();
                for (int i = 0; i < lyricsWordIndex; i++) {
                    lyricsBeforeWord.append(lyricsWords[i]);
                }
                // 当前歌词字
                String lrcNowWord = lyricsWords[lyricsWordIndex].trim();// 去掉空格
                // 当前歌词之前的歌词长度
                float lyricsBeforeWordWidth = paint
                        .measureText(lyricsBeforeWord.toString());

                // 当前歌词长度
                float lyricsNowWordWidth = paint.measureText(lrcNowWord);

                float len = lyricsNowWordWidth
                        / wordsDisInterval[lyricsWordIndex]
                        * lyricsWordHLTime;
                lineLyricsHLWidth = lyricsBeforeWordWidth + len;
            }
        }

        return lineLyricsHLWidth;
    }


    /**
     * 绘画动感文本
     *
     * @param canvas
     * @param paint   默认画笔
     * @param paintHL 高亮画笔
     * @param text    文本
     * @param hlWidth 高亮宽度
     * @param x
     * @param y
     */
    public static void drawDynamicText(Canvas canvas, Paint paint, Paint paintHL, int[] paintColor, int[] paintHLColor, String text, float hlWidth, float x, float y) {
        canvas.save();

        //设置为上下渐变
        LinearGradient linearGradient = new LinearGradient(x, y - getTextHeight(paint), x, y, paintColor, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawText(text, x, y, paint);
        //设置动感歌词过渡效果
        canvas.clipRect(x, y - getRealTextHeight(paint), x + hlWidth,
                y + getRealTextHeight(paint));

        //设置为上下渐变
        LinearGradient linearGradientHL = new LinearGradient(x, y - getTextHeight(paint), x, y, paintHLColor, null, Shader.TileMode.CLAMP);
        paintHL.setShader(linearGradientHL);
        canvas.drawText(text, x, y, paintHL);
        canvas.restore();
    }

    /**
     * 绘画动感文本
     *
     * @param canvas
     * @param paint   默认画笔
     * @param paintHL 高亮画笔
     * @param text    文本
     * @param hlWidth 高亮宽度
     * @param x
     * @param y
     */
    public static void drawDynamicText(Canvas canvas, Paint paint, Paint paintHL, String text, float hlWidth, float x, float y) {
        canvas.save();
        canvas.drawText(text, x, y, paint);
        //设置动感歌词过渡效果
        canvas.clipRect(x, y - getRealTextHeight(paint), x + hlWidth,
                y + getRealTextHeight(paint));
        canvas.drawText(text, x, y, paintHL);
        canvas.restore();
    }

    /**
     * 描绘轮廓
     *
     * @param canvas
     * @param text
     * @param x
     * @param y
     */
    public static void drawOutline(Canvas canvas, Paint paint, String text, float x, float y) {
        canvas.drawText(text, x - 1, y, paint);
        canvas.drawText(text, x + 1, y, paint);
        canvas.drawText(text, x, y + 1, paint);
        canvas.drawText(text, x, y - 1, paint);
    }


    /**
     * 绘画文本
     *
     * @param canvas
     * @param paint
     * @param paintColor
     * @param text
     * @param x
     * @param y
     */
    public static void drawText(Canvas canvas, Paint paint, int[] paintColor, String text, float x, float y) {
        //设置为上下渐变
        LinearGradient linearGradient = new LinearGradient(x, y - getTextHeight(paint), x, y, paintColor, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawText(text, x, y, paint);
    }

    /**
     * 获取真实的歌词高度
     *
     * @param paint
     * @return
     */
    public static int getRealTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) (-fm.leading - fm.ascent + fm.descent);
    }

    /**
     * 获取行歌词高度。用于y轴位置计算
     *
     * @param paint
     * @return
     */
    public static int getTextHeight(Paint paint) {
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
    public static float getTextWidth(Paint paint, String text) {
        return paint
                .measureText(text);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 通过文件名称获取歌词文件
     *
     * @param fileName          歌词文件名（不含有后缀）
     * @param filePathDirectory 歌词文件所在的歌词文件夹
     * @return
     */
    public static File getLrcFile(String fileName, String filePathDirectory) {
        List<String> lrcExts = LyricsIOUtils.getSupportLyricsExts();
        for (int i = 0; i < lrcExts.size(); i++) {
            String lrcFilePath = filePathDirectory + File.separator + fileName + "." + lrcExts.get(i);
            File lrcFile = new File(lrcFilePath);
            if (lrcFile.exists()) {
                return lrcFile;
            }
        }
        return null;
    }

    /**
     * 从默认歌词中获取翻译歌词所需的歌词字时间(注：不支持lrc歌词获取歌词的字时间)
     *
     * @param lyricsType
     * @param lrcLineInfos
     * @param translateLrcLineInfos
     * @return
     */
    public static List<LyricsLineInfo> getTranslateLrc(int lyricsType, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, List<TranslateLrcLineInfo> translateLrcLineInfos) {
        if (lrcLineInfos == null) return null;
        List<LyricsLineInfo> newLyricsLineInfos = new ArrayList<LyricsLineInfo>();
        for (int i = 0; i < lrcLineInfos.size(); i++) {
            TranslateLrcLineInfo origLyricsLineInfo = translateLrcLineInfos
                    .get(i);
            LyricsLineInfo defLyricsLineInfo = lrcLineInfos.get(i);
            // 构造新的翻译行歌词
            LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
            newLyricsLineInfo.copy(newLyricsLineInfo, defLyricsLineInfo);
            String lineLyrics = origLyricsLineInfo.getLineLyrics();
            newLyricsLineInfo.setLineLyrics(lineLyrics);
            //动感歌词
            if (lyricsType == LyricsInfo.DYNAMIC) {
                String[] newLyricsWords = getLyricsWords(lineLyrics);
                int[] newWordsDisInterval = getWordsDisInterval(defLyricsLineInfo, newLyricsWords);
                newLyricsLineInfo.setLyricsWords(newLyricsWords);
                newLyricsLineInfo.setWordsDisInterval(newWordsDisInterval);
            }
            //
            newLyricsLineInfos.add(newLyricsLineInfo);
        }
        return newLyricsLineInfos;
    }

    /**
     * 获取每个字的时间，翻译歌词的字时间为平均时间
     *
     * @param defLyricsLineInfo
     * @param newLyricsWords
     * @return
     */
    private static int[] getWordsDisInterval(LyricsLineInfo defLyricsLineInfo, String[] newLyricsWords) {
        int[] wordsDisInterval = new int[newLyricsWords.length];
        int sumTime = 0;
        for (int i = 0; i < defLyricsLineInfo.getWordsDisInterval().length; i++) {
            sumTime += defLyricsLineInfo.getWordsDisInterval()[i];
        }
        int avgTime = sumTime / wordsDisInterval.length;
        for (int i = 0; i < wordsDisInterval.length; i++) {
            wordsDisInterval[i] = avgTime;
        }
        return wordsDisInterval;
    }


    /**
     * 从默认歌词中获取音译歌词所需的歌词字时间(注：不支持lrc歌词获取歌词的字时间)
     *
     * @param lyricsType                  歌词类型
     * @param lrcLineInfos                默认歌词集合
     * @param transliterationLrcLineInfos 原始音译歌词
     * @return
     */
    public static List<LyricsLineInfo> getTransliterationLrc(int lyricsType, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, List<LyricsLineInfo> transliterationLrcLineInfos) {
        if (lrcLineInfos == null || lyricsType == LyricsInfo.LRC) return null;
        List<LyricsLineInfo> newLyricsLineInfos = new ArrayList<LyricsLineInfo>();
        for (int i = 0; i < lrcLineInfos.size(); i++) {
            LyricsLineInfo origLyricsLineInfo = transliterationLrcLineInfos.get(i);
            LyricsLineInfo defLyricsLineInfo = lrcLineInfos.get(i);

            //构造新的音译行歌词
            LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
            newLyricsLineInfo.copy(newLyricsLineInfo, defLyricsLineInfo);
            //
            String[] defLyricsWords = defLyricsLineInfo.getLyricsWords();
            String[] origLyricsWords = origLyricsLineInfo.getLyricsWords();
            String[] newLyricsWords = new String[defLyricsWords.length];
            String newLineLyrics = "";
            for (int j = 0; j < defLyricsWords.length; j++) {
                if (defLyricsWords[j].lastIndexOf(" ") != -1) {
                    newLyricsWords[j] = origLyricsWords[j].trim() + " ";
                } else {
                    String origLyricsWordsString = origLyricsWords[j].trim();
                    if (StringUtils.isBlank(origLyricsWordsString)) {
                        newLyricsWords[j] += " ";
                    } else {
                        boolean isWord = origLyricsWordsString.matches("[a-zA-Z]+");
                        if (isWord) {
                            newLyricsWords[j] = origLyricsWords[j].trim() + " ";
                        } else {
                            newLyricsWords[j] = origLyricsWords[j].trim();
                        }
                    }
                }
                newLineLyrics += newLyricsWords[j];
            }

            newLyricsLineInfo.setLyricsWords(newLyricsWords);
            newLyricsLineInfo.setLineLyrics(newLineLyrics);

            newLyricsLineInfos.add(newLyricsLineInfo);
        }
        return newLyricsLineInfos;
    }


    /**
     * 通过行歌词，获取歌词的每个字
     *
     * @param lineLyrics
     * @return
     */
    public static String[] getLyricsWords(String lineLyrics) {
        Stack<String> lrcStack = new Stack<String>();
        if (StringUtils.isBlank(lineLyrics)) {
            lrcStack.add("");
        } else {
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < lineLyrics.length(); i++) {
                char c = lineLyrics.charAt(i);
                if (CharUtils.isChinese(c) || CharUtils.isHangulSyllables(c)
                        || CharUtils.isHiragana(c)) {

                    if (StringUtils.isNotBlank(temp.toString())) {
                        lrcStack.push(temp.toString());
                        //清空
                        temp.delete(0, temp.length());
                    }
                    lrcStack.push(String.valueOf(c));
                } else if (Character.isSpaceChar(c)) {
                    if (StringUtils.isNotBlank(temp.toString())) {
                        lrcStack.push(temp.toString());
                        //清空
                        temp.delete(0, temp.length());
                    }
                    String tw = lrcStack.pop();
                    if (tw != null) {
                        lrcStack.push(tw + String.valueOf(c));
                    }
                } else {
                    temp.append(c);
                }
            }
            //
            if (StringUtils.isNotBlank(temp.toString())) {
                lrcStack.push(temp.toString());
            }
            temp = null;
        }

        //获取字
        String[] lyricsWords = new String[lrcStack.size()];
        Iterator<String> it = lrcStack.iterator();
        int i = 0;
        while (it.hasNext()) {
            lyricsWords[i++] = it.next();
        }
        it = null;
        lrcStack = null;
        return lyricsWords;
    }

    /**
     * 通过播放的进度，获取所唱歌词行数
     *
     * @param lyricsType        歌词类型 LyricsInfo.LRC OR LyricsInfo.DYNAMIC
     * @param lyricsLineTreeMap 歌词集合
     * @param curPlayingTime    当前播放进度
     * @param playOffset        时间补偿值
     * @return
     */
    public static int getLineNumber(int lyricsType, TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, long curPlayingTime, long playOffset) {

        //添加歌词增量
        long newPlayingTime = curPlayingTime + playOffset;
        if (lyricsType == LyricsInfo.LRC) {
            //lrc歌词
            for (int i = 0; i < lyricsLineTreeMap.size(); i++) {

                if (newPlayingTime < lyricsLineTreeMap.get(i).getStartTime()) return 0;

                if (newPlayingTime >= lyricsLineTreeMap.get(i).getStartTime()
                        && i + 1 < lyricsLineTreeMap.size()
                        && newPlayingTime <= lyricsLineTreeMap.get(i + 1).getStartTime()) {
                    return i;
                }
            }
            if (lyricsLineTreeMap.size() > 0) {
                return lyricsLineTreeMap.size() - 1;
            }
        } else if (lyricsType == LyricsInfo.DYNAMIC) {
            //动感歌词
            for (int i = 0; i < lyricsLineTreeMap.size(); i++) {
                if (newPlayingTime >= lyricsLineTreeMap.get(i).getStartTime()
                        && newPlayingTime <= lyricsLineTreeMap.get(i).getEndTime()) {
                    return i;
                }
                if (newPlayingTime > lyricsLineTreeMap.get(i).getEndTime()
                        && i + 1 < lyricsLineTreeMap.size()
                        && newPlayingTime <= lyricsLineTreeMap.get(i + 1).getStartTime()) {
                    return i;
                }
            }
            if (newPlayingTime >= lyricsLineTreeMap.get(lyricsLineTreeMap.size() - 1)
                    .getEndTime()) {
                return lyricsLineTreeMap.size() - 1;
            }
        }
        return 0;
    }


    /**
     * 获取当前时间对应的行歌词文本
     *
     * @param lyricsType     歌词类型 LyricsInfo.LRC OR LyricsInfo.DYNAMIC
     * @param lrcLineInfos   歌词集合
     * @param curPlayingTime 当前播放进度
     * @param playOffset     时间补偿值
     * @return
     */
    public static String getLineLrc(int lyricsType, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, long curPlayingTime, long playOffset) {
        if (lrcLineInfos == null) return null;
        int lyricsLineNum = getLineNumber(lyricsType, lrcLineInfos, curPlayingTime, playOffset);
        if (lyricsLineNum >= lrcLineInfos.size())
            return null;

        LyricsLineInfo lyricsLineInfo = lrcLineInfos.get(lyricsLineNum);

        if (lyricsLineInfo == null)
            return null;
        return lyricsLineInfo.getLineLyrics();
    }

    /**
     * 获取分割后歌词的当前时间对应的行歌词文本
     *
     * @param lyricsType     歌词类型 LyricsInfo.LRC OR LyricsInfo.DYNAMIC
     * @param lrcLineInfos   歌词集合
     * @param curPlayingTime 当前播放进度
     * @param playOffset     时间补偿值
     * @return
     */
    public static String getSplitLineLrc(int lyricsType, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, long curPlayingTime, long playOffset) {
        if (lrcLineInfos == null) return null;
        int lyricsLineNum = getLineNumber(lyricsType, lrcLineInfos, curPlayingTime, playOffset);
        if (lyricsLineNum >= lrcLineInfos.size())
            return null;
        int splitLyricsLineNum = -1;
        if (lyricsType == LyricsInfo.DYNAMIC) {
            splitLyricsLineNum = getSplitDynamicLyricsLineNum(lrcLineInfos, lyricsLineNum, curPlayingTime, playOffset);
        } else {
            splitLyricsLineNum = getSplitLrcLyricsLineNum(lrcLineInfos, lyricsLineNum, curPlayingTime, playOffset);
        }

        List<LyricsLineInfo> splitLyricsLineInfos = lrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
        if (splitLyricsLineNum == -1 || splitLyricsLineNum >= splitLyricsLineInfos.size()) {
            return null;
        }

        LyricsLineInfo lyricsLineInfo = splitLyricsLineInfos.get(splitLyricsLineNum);

        if (lyricsLineInfo == null)
            return null;
        return lyricsLineInfo.getLineLyrics();
    }

    /**
     * 获取当前时间对应的行歌词开始时间
     *
     * @param lyricsType     歌词类型 LyricsInfo.LRC OR LyricsInfo.DYNAMIC
     * @param lrcLineInfos   歌词集合
     * @param curPlayingTime 当前播放进度
     * @param playOffset     时间补偿值
     * @return
     */
    public static int getLineLrcStartTime(int lyricsType, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, long curPlayingTime, long playOffset) {
        if (lrcLineInfos == null) return -1;
        int lyricsLineNum = getLineNumber(lyricsType, lrcLineInfos, curPlayingTime, playOffset);
        if (lyricsLineNum >= lrcLineInfos.size())
            return -1;

        LyricsLineInfo lyricsLineInfo = lrcLineInfos.get(lyricsLineNum);

        if (lyricsLineInfo == null)
            return -1;
        return lyricsLineInfo.getStartTime();
    }

    /**
     * 获取分割后歌词的当前时间对应的行歌词开始时间
     *
     * @param lyricsType     歌词类型 LyricsInfo.LRC OR LyricsInfo.DYNAMIC
     * @param lrcLineInfos   歌词集合
     * @param curPlayingTime 当前播放进度
     * @param playOffset     时间补偿值
     * @return
     */
    public static int getSplitLineLrcStartTime(int lyricsType, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, long curPlayingTime, long playOffset) {
        if (lrcLineInfos == null) return -1;
        int lyricsLineNum = getLineNumber(lyricsType, lrcLineInfos, curPlayingTime, playOffset);
        if (lyricsLineNum >= lrcLineInfos.size())
            return -1;
        int splitLyricsLineNum = -1;
        if (lyricsType == LyricsInfo.DYNAMIC) {
            splitLyricsLineNum = getSplitDynamicLyricsLineNum(lrcLineInfos, lyricsLineNum, curPlayingTime, playOffset);
        } else {
            splitLyricsLineNum = getSplitLrcLyricsLineNum(lrcLineInfos, lyricsLineNum, curPlayingTime, playOffset);
        }

        List<LyricsLineInfo> splitLyricsLineInfos = lrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
        if (splitLyricsLineNum < 0 || splitLyricsLineNum >= splitLyricsLineInfos.size()) {
            return -1;
        }

        LyricsLineInfo lyricsLineInfo = splitLyricsLineInfos.get(splitLyricsLineNum);

        if (lyricsLineInfo == null)
            return -1;
        return lyricsLineInfo.getStartTime();
    }

//////////////////////////////////分割歌词///////////////////////////////////////////

    /**
     * 获取分割lrc歌词
     *
     * @param defLyricsLineTreeMap
     * @param textMaxWidth
     * @param paint
     * @return 对原始歌词集合进行修改，并返回含有分割歌词的集合
     */
    public static TreeMap<Integer, LyricsLineInfo> getSplitLrcLyrics(TreeMap<Integer, LyricsLineInfo> defLyricsLineTreeMap, float textMaxWidth, Paint paint) {
        if (defLyricsLineTreeMap == null) return null;
        TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap = new TreeMap<Integer, LyricsLineInfo>();
        for (int i = 0; i < defLyricsLineTreeMap.size(); i++) {
            LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
            //复制
            lyricsLineInfo.copy(lyricsLineInfo, defLyricsLineTreeMap.get(i));
            //分割歌词
            splitLrcLyrics(lyricsLineInfo, paint, textMaxWidth);

            lyricsLineTreeMap.put(i, lyricsLineInfo);
        }
        return lyricsLineTreeMap;
    }

    /**
     * 获取分割动感歌词
     *
     * @param textMaxWidth 歌词行最大宽度
     * @param paint
     * @return 对原始歌词集合进行修改，并返回含有分割歌词的集合
     */
    public static TreeMap<Integer, LyricsLineInfo> getSplitDynamicLyrics(TreeMap<Integer, LyricsLineInfo> defLyricsLineTreeMap, float textMaxWidth, Paint paint) {
        if (defLyricsLineTreeMap == null) return null;
        TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap = new TreeMap<Integer, LyricsLineInfo>();
        for (int i = 0; i < defLyricsLineTreeMap.size(); i++) {
            LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
            //复制
            lyricsLineInfo.copy(lyricsLineInfo, defLyricsLineTreeMap.get(i));
            //分割歌词
            splitLyrics(lyricsLineInfo, paint, textMaxWidth);

            lyricsLineTreeMap.put(i, lyricsLineInfo);
        }
        return lyricsLineTreeMap;
    }

    /**
     * 分割歌词
     *
     * @param lyricsLineInfo
     * @param paint
     * @param textMaxWidth
     */
    private static void splitLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, float textMaxWidth) {

        final List<LyricsLineInfo> lyricsLineInfos = new ArrayList<LyricsLineInfo>();
        splitLineLyrics(lyricsLineInfo, paint, textMaxWidth, new ForeachListener() {
            @Override
            public void foreach(LyricsLineInfo mLyricsLineInfo) {
                lyricsLineInfos.add(mLyricsLineInfo);
            }
        });

        lyricsLineInfo.setSplitLyricsLineInfos(lyricsLineInfos);

    }

    /**
     * 分割歌词
     *
     * @param lyricsLineInfo
     * @param paint
     * @param textMaxWidth
     * @param foreachListener
     */
    private static void splitLineLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, float textMaxWidth, ForeachListener foreachListener) {
        String lineLyrics = lyricsLineInfo.getLineLyrics().trim();
        // 行歌词数组
        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        // 每行的歌词长度
        int lineWidth = (int) paint.measureText(lineLyrics);
        float maxLineWidth = textMaxWidth;
        if (lineWidth > maxLineWidth) {

            int lyricsWordsWidth = 0;
            //开始索引和结束索引
            int startIndex = 0;
            for (int i = 0; i < lyricsWords.length; i++) {
                // 当前的歌词宽度
                lyricsWordsWidth += (int) paint.measureText(lyricsWords[i]);
                //下一个字的宽度
                int nextLyricsWordWidth = 0;
                if ((i + 1) < lyricsWords.length) {
                    nextLyricsWordWidth = (int) paint.measureText(lyricsWords[(i + 1)]);
                }
                if (lyricsWordsWidth + nextLyricsWordWidth > maxLineWidth) {

                    LyricsLineInfo newLyricsLineInfo = getNewLyricsLineInfo(
                            lyricsLineInfo, startIndex, i);

                    if (newLyricsLineInfo != null && foreachListener != null) {
                        foreachListener.foreach(newLyricsLineInfo);
                    }

                    //
                    lyricsWordsWidth = 0;
                    startIndex = i + 1;
                    if (startIndex == lyricsWords.length) {
                        startIndex = lyricsWords.length - 1;
                    }
                } else if (i == lyricsWords.length - 1) {
                    LyricsLineInfo newLyricsLineInfo = getNewLyricsLineInfo(
                            lyricsLineInfo, startIndex, lyricsWords.length - 1);

                    if (newLyricsLineInfo != null && foreachListener != null) {
                        foreachListener.foreach(newLyricsLineInfo);
                    }
                }
            }

        } else {
            if (foreachListener != null) {
                foreachListener.foreach(lyricsLineInfo);
            }
        }
    }

    /**
     * 分割lrc歌词
     *
     * @param lyricsLineInfo
     * @param paint
     * @param textMaxWidth
     */
    private static void splitLrcLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, float textMaxWidth) {
        List<LyricsLineInfo> lyricsLineInfos = new ArrayList<LyricsLineInfo>();
        String lineLyrics = lyricsLineInfo.getLineLyrics().trim();
        // 每行的歌词长度
        int lineWidth = (int) paint.measureText(lineLyrics);
        float maxLineWidth = textMaxWidth;
        if (lineWidth > maxLineWidth) {

            int lyricsWordsWidth = 0;
            //开始索引和结束索引
            int startIndex = 0;
            for (int i = 0; i < lineLyrics.length(); i++) {
                // 当前的歌词宽度
                lyricsWordsWidth += (int) paint.measureText(lineLyrics.charAt(i) + "");
                //下一个字的宽度
                int nextLyricsWordWidth = 0;
                if ((i + 1) < lineLyrics.length()) {
                    nextLyricsWordWidth = (int) paint.measureText(lineLyrics.charAt(i + 1) + "");
                }
                if (lyricsWordsWidth + nextLyricsWordWidth > maxLineWidth) {

                    LyricsLineInfo newLyricsLineInfo = getNewLrcLyricsLineInfo(
                            lyricsLineInfo, startIndex, i);

                    if (newLyricsLineInfo != null) {
                        lyricsLineInfos.add(newLyricsLineInfo);
                    }

                    //
                    lyricsWordsWidth = 0;
                    startIndex = i + 1;
                    if (startIndex == lineLyrics.length()) {
                        startIndex = lineLyrics.length() - 1;
                    }
                } else if (i == lineLyrics.length() - 1) {
                    LyricsLineInfo newLyricsLineInfo = getNewLrcLyricsLineInfo(
                            lyricsLineInfo, startIndex, lineLyrics.length() - 1);

                    if (newLyricsLineInfo != null) {
                        lyricsLineInfos.add(newLyricsLineInfo);
                    }
                }
            }

        } else {
            lyricsLineInfos.add(lyricsLineInfo);
        }
        lyricsLineInfo.setSplitLyricsLineInfos(lyricsLineInfos);
    }

    /**
     * 根据新歌词的索引和旧歌词数据，构造新的歌词数据
     *
     * @param lyricsLineInfo 旧的行歌词数据
     * @param startIndex     开始歌词索引
     * @param lastIndex      结束歌词索引
     * @return
     */
    private static LyricsLineInfo getNewLrcLyricsLineInfo(
            LyricsLineInfo lyricsLineInfo, int startIndex, int lastIndex) {

        if (lastIndex < 0)
            return null;
        LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
        // 行开始时间
        int lineStartTime = lyricsLineInfo.getStartTime();
        int startTime = lineStartTime;
        StringBuilder lineLyrics = new StringBuilder();
        for (int i = startIndex; i <= lastIndex; i++) {
            lineLyrics.append(lyricsLineInfo.getLineLyrics().charAt(i));
        }
        newLyricsLineInfo.setStartTime(startTime);
        newLyricsLineInfo.setLineLyrics(lineLyrics.toString());

        return newLyricsLineInfo;
    }

    /**
     * 根据新歌词的索引和旧歌词数据，构造新的歌词数据
     *
     * @param lyricsLineInfo 旧的行歌词数据
     * @param startIndex     开始歌词索引
     * @param lastIndex      结束歌词索引
     * @return
     */
    private static LyricsLineInfo getNewLyricsLineInfo(
            LyricsLineInfo lyricsLineInfo, int startIndex, int lastIndex) {

        if (lastIndex < 0)
            return null;
        LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
        // 行开始时间
        int lineStartTime = lyricsLineInfo.getStartTime();
        int startTime = lineStartTime;
        int endTime = 0;
        StringBuilder lineLyrics = new StringBuilder();
        List<String> lyricsWordsList = new ArrayList<String>();
        List<Integer> wordsDisIntervalList = new ArrayList<Integer>();
        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        int[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
        for (int i = 0; i <= lastIndex; i++) {
            if (i < startIndex) {
                startTime += wordsDisInterval[i];
            } else {
                lineLyrics.append(lyricsWords[i]);
                wordsDisIntervalList.add(wordsDisInterval[i]);
                lyricsWordsList.add(lyricsWords[i]);
                endTime += wordsDisInterval[i];
            }
        }
        endTime += startTime;
        //
        String[] newLyricsWords = lyricsWordsList
                .toArray(new String[lyricsWordsList.size()]);
        int newWordsDisInterval[] = getWordsDisIntervalList(wordsDisIntervalList);
        newLyricsLineInfo.setEndTime(endTime);
        newLyricsLineInfo.setStartTime(startTime);
        newLyricsLineInfo.setLineLyrics(lineLyrics.toString());
        newLyricsLineInfo.setLyricsWords(newLyricsWords);
        newLyricsLineInfo.setWordsDisInterval(newWordsDisInterval);

        return newLyricsLineInfo;
    }

    /**
     * 获取每个歌词的时间
     *
     * @param wordsDisIntervalList
     * @return
     */
    private static int[] getWordsDisIntervalList(
            List<Integer> wordsDisIntervalList) {
        int wordsDisInterval[] = new int[wordsDisIntervalList.size()];
        for (int i = 0; i < wordsDisIntervalList.size(); i++) {
            wordsDisInterval[i] = wordsDisIntervalList.get(i);
        }
        return wordsDisInterval;
    }

    /**
     * 获取动感额外歌词
     *
     * @param lrcLineInfos   额外歌词（注：该集合需要含有字和字对应的时间数据）
     * @param mTextMaxWidth
     * @param mExtraLrcPaint
     * @return 对原始歌词集合进行修改，并返回含有分割歌词的集合
     */
    public static List<LyricsLineInfo> getSplitDynamicExtraLyrics(List<LyricsLineInfo> lrcLineInfos, float mTextMaxWidth, Paint mExtraLrcPaint) {
        if (lrcLineInfos == null) return null;
        List<LyricsLineInfo> extraLrcLineInfos = new ArrayList<LyricsLineInfo>();

        for (int i = 0; i < lrcLineInfos.size(); i++) {

            LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
            lyricsLineInfo.copy(lyricsLineInfo, lrcLineInfos.get(i));

            //分隔歌词
            splitLyrics(lyricsLineInfo, mExtraLrcPaint, mTextMaxWidth);
            extraLrcLineInfos.add(lyricsLineInfo);
        }

        return extraLrcLineInfos;
    }

    /**
     * 获取Lrc额外歌词
     *
     * @param translateLrcLineInfos
     * @param mTextMaxWidth
     * @param mExtraLrcPaint
     * @return 对原始歌词集合进行修改，并返回含有分割歌词的集合
     */
    public static List<LyricsLineInfo> getSplitLrcExtraLyrics(List<LyricsLineInfo> translateLrcLineInfos, float mTextMaxWidth, Paint mExtraLrcPaint) {
        if (translateLrcLineInfos == null) return null;
        List<LyricsLineInfo> extraLrcLineInfos = new ArrayList<LyricsLineInfo>();
        for (int i = 0; i < translateLrcLineInfos.size(); i++) {

            LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
            lyricsLineInfo.copy(lyricsLineInfo, translateLrcLineInfos.get(i));

            //分隔歌词
            splitLrcLyrics(lyricsLineInfo, mExtraLrcPaint, mTextMaxWidth);
            extraLrcLineInfos.add(lyricsLineInfo);
        }
        return extraLrcLineInfos;
    }

    /////////////////////////////////////////////////////////////////////////

    /**
     * 获取分割后的动感歌词行索引
     *
     * @param lyricsLineTreeMap
     * @param origLineNumber    原行号
     * @param oldPlayingTime
     * @return
     */
    public static int getSplitDynamicLyricsLineNum(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int origLineNumber, long oldPlayingTime, long playOffset) {
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(origLineNumber);
        List<LyricsLineInfo> lyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        return getSplitLyricsLineNum(lyricsLineInfos, oldPlayingTime, playOffset);
    }

    /**
     * 获取分割后的lrc歌词行索引
     *
     * @param mLrcLineInfos
     * @param mLyricsLineNum
     * @param playProgress
     * @param playOffset
     * @return
     */
    public static int getSplitLrcLyricsLineNum(TreeMap<Integer, LyricsLineInfo> mLrcLineInfos, int mLyricsLineNum, long playProgress, long playOffset) {
        LyricsLineInfo lyrLine = mLrcLineInfos.get(mLyricsLineNum);
        List<LyricsLineInfo> lyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        return getSplitLrcLyricsLineNum(lyricsLineInfos, playProgress, playOffset);
    }

    /**
     * 获取分割后的行索引
     *
     * @param lyricsLineInfos
     * @param playProgress
     * @param playOffset
     * @return
     */
    private static int getSplitLrcLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, long playProgress, long playOffset) {
        //添加歌词增量
        long curPlayingTime = playProgress + playOffset;
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            if (curPlayingTime < lyricsLineInfos.get(i).getStartTime()) return 0;

            if (curPlayingTime >= lyricsLineInfos.get(i).getStartTime()
                    && i + 1 < lyricsLineInfos.size()
                    && curPlayingTime <= lyricsLineInfos.get(i + 1).getStartTime()) {
                return i;
            }
        }
        if (lyricsLineInfos.size() > 0) {
            return lyricsLineInfos.size() - 1;
        }
        return 0;
    }


    /**
     * 获取额外分割歌词索引
     *
     * @param lyricsLineInfos
     * @param origLineNumber
     * @param oldPlayingTime
     * @return
     */
    public static int getSplitExtraLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, int origLineNumber, long oldPlayingTime, long playOffset) {
        LyricsLineInfo lyrLine = lyricsLineInfos.get(origLineNumber);
        List<LyricsLineInfo> newLineInfos = lyrLine.getSplitLyricsLineInfos();
        return getSplitLyricsLineNum(newLineInfos, oldPlayingTime, playOffset);
    }

    /**
     * 获取分割后的行索引
     *
     * @param lyricsLineInfos
     * @param oldPlayingTime
     * @return
     */
    private static int getSplitLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, long oldPlayingTime, long playOffset) {
        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        for (int i = 0; i < lyricsLineInfos.size(); i++) {

            if (curPlayingTime < lyricsLineInfos.get(i).getStartTime()) return 0;

            if (curPlayingTime >= lyricsLineInfos.get(i).getStartTime()
                    && curPlayingTime <= lyricsLineInfos.get(i).getEndTime()) {
                return i;
            }
            if (curPlayingTime > lyricsLineInfos.get(i).getEndTime()
                    && i + 1 < lyricsLineInfos.size()
                    && curPlayingTime <= lyricsLineInfos.get(i + 1).getStartTime()) {
                return i;
            }
        }
        if (curPlayingTime >= lyricsLineInfos.get(lyricsLineInfos.size() - 1)
                .getEndTime()) {
            return lyricsLineInfos.size() - 1;
        }
        return 0;
    }

    /**
     * 获取分割歌词后的歌词字索引
     *
     * @param lyricsLineTreeMap
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public static int getSplitLyricsWordIndex(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return -1;

        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);

        List<LyricsLineInfo> lyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo temp = lyricsLineInfos.get(i);
            int elapseTime = temp.getStartTime();
            if (curPlayingTime < elapseTime) return -1;
            for (int j = 0; j < temp.getLyricsWords().length; j++) {
                elapseTime += temp.getWordsDisInterval()[j];
                if (curPlayingTime <= elapseTime) {
                    return j;
                }
            }
            int endTime = temp.getEndTime();
            if (elapseTime < curPlayingTime && curPlayingTime <= endTime) {
                break;
            }

        }
        //整句已经播放完成
        return -2;
    }

    /**
     * 获取分割歌词后的歌词字索引
     *
     * @param lyricsLineTreeMap
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public static int getLyricsWordIndex(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return -1;

        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        if (curPlayingTime < elapseTime) return -1;

        for (int j = 0; j < lyrLine.getLyricsWords().length; j++) {
            elapseTime += lyrLine.getWordsDisInterval()[j];
            if (curPlayingTime <= elapseTime) {
                return j;
            }
        }

        //整句已经播放完成
        return -2;
    }

    /**
     * 获取分割额外歌词字索引
     *
     * @param lyricsLineInfos
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public static int getSplitExtraLyricsWordIndex(List<LyricsLineInfo> lyricsLineInfos, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return -1;

        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineInfos.get(lyricsLineNum);
        List<LyricsLineInfo> newLyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        for (int i = 0; i < newLyricsLineInfos.size(); i++) {
            LyricsLineInfo temp = newLyricsLineInfos.get(i);
            int elapseTime = temp.getStartTime();
            if (curPlayingTime < elapseTime) return -1;
            for (int j = 0; j < temp.getLyricsWords().length; j++) {
                elapseTime += temp.getWordsDisInterval()[j];
                if (curPlayingTime <= elapseTime) {
                    return j;
                }
            }
            int endTime = temp.getEndTime();
            if (elapseTime < curPlayingTime && curPlayingTime <= endTime) {
                break;
            }

        }
        //整句已经播放完成
        return -2;
    }

    /**
     * 获取额外歌词字索引
     *
     * @param lyricsLineInfos
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public static int getExtraLyricsWordIndex(List<LyricsLineInfo> lyricsLineInfos, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return -1;

        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineInfos.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        if (curPlayingTime < elapseTime) return -1;
        for (int j = 0; j < lyrLine.getLyricsWords().length; j++) {
            elapseTime += lyrLine.getWordsDisInterval()[j];
            if (curPlayingTime <= elapseTime) {
                return j;
            }
        }

        //整句已经播放完成
        return -2;
    }

    /**
     * 获取当前歌词的第几个歌词的播放时间
     *
     * @param lyricsLineNum  行数
     * @param oldPlayingTime
     * @return
     */
    public static long getDisWordsIndexLenTime(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return 0;
        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        if (curPlayingTime < elapseTime) return 0;
        for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
            elapseTime += lyrLine.getWordsDisInterval()[i];
            if (curPlayingTime <= elapseTime) {
                return lyrLine.getWordsDisInterval()[i] - (elapseTime - curPlayingTime);
            }
        }

        return 0;
    }

    /**
     * 获取翻译歌词行的第几个歌词的播放时间
     *
     * @param lyricsLineInfos
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @param playOffset
     * @return
     */
    public static long getTranslateLrcDisWordsIndexLenTime(List<LyricsLineInfo> lyricsLineInfos, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return 0;
        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineInfos.get(lyricsLineNum);
        int elapseTime = lyrLine.getStartTime();
        if (curPlayingTime < elapseTime) return 0;
        for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
            elapseTime += lyrLine.getWordsDisInterval()[i];
            if (curPlayingTime <= elapseTime) {
                return lyrLine.getWordsDisInterval()[i] - (elapseTime - curPlayingTime);
            }
        }
        return 0;
    }

    /**
     * 默认歌词遍历
     */
    private interface ForeachListener {
        /**
         * 遍历
         *
         * @param lyricsLineInfo
         */
        public void foreach(LyricsLineInfo lyricsLineInfo);

    }

}
