package com.zlm.hp.lyrics.formats.lrcwy;

import android.text.TextUtils;
import android.util.Base64;

import com.zlm.hp.lyrics.formats.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.lyrics.utils.TimeUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Description: 网易歌词生成器
 * @author: zhangliangming
 * @date: 2018-12-27 0:03
 **/
public class WYLyricsFileWriter extends LyricsFileWriter {
    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "[ti:";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "[ar:";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "[offset:";
    /**
     * 歌词上传者
     */
    private final static String LEGAL_BY_PREFIX = "[by:";

    /**
     * 专辑
     */
    private final static String LEGAL_AL_PREFIX = "[al:";

    private final static String LEGAL_TOTAL_PREFIX = "[total:";

    /**
     * lrc歌词 字符串
     */
    public final static String LEGAL_LYRICS_LINE_PREFIX = "wy.lrc";

    /**
     * 动感歌词 字符串
     */
    public final static String LEGAL_DLYRICS_LINE_PREFIX = "wy.dlrc";

    /**
     * 额外歌词
     */
    private final static String LEGAL_EXTRA_LYRICS_PREFIX = "wy.extra.lrc";


    @Override
    public boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath) throws Exception {
        String lyricsContent = getLyricsContent(lyricsIfno);
        return saveLyricsFile(lyricsContent, lyricsFilePath);
    }

    @Override
    public String getLyricsContent(LyricsInfo lyricsIfno) throws Exception {
        StringBuilder lyricsCom = new StringBuilder();
        // 先保存所有的标签数据
        Map<String, Object> tags = lyricsIfno.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object val = entry.getValue();
            if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
                lyricsCom.append(LEGAL_SONGNAME_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom.append(LEGAL_SINGERNAME_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom.append(LEGAL_OFFSET_PREFIX);
            } else if (entry.getKey().equals(LyricsTag.TAG_TOTAL)) {
                lyricsCom.append(LEGAL_TOTAL_PREFIX);
            } else {
                val = "[" + entry.getKey() + ":" + val;
            }
            lyricsCom.append(val + "]\n");
        }
        //判断歌词类型
        if (lyricsIfno.getLyricsType() == LyricsInfo.DYNAMIC) {
            //保存动感歌词
            String dynamicContent = getDynamicContent(lyricsIfno);
            lyricsCom.append(dynamicContent);
        } else {
            //保存lrc歌词
            String lrcContent = getLrcContent(lyricsIfno);
            lyricsCom.append(lrcContent);
        }

        //保存额外歌词
        String extraLrcContent = getExtraLrcContent(lyricsIfno);
        if (!TextUtils.isEmpty(extraLrcContent)) {
            lyricsCom.append(extraLrcContent);
        }
        return lyricsCom.toString();
    }

    /**
     * 获取额外歌词
     *
     * @param lyricsIfno
     * @return
     */
    private String getExtraLrcContent(LyricsInfo lyricsIfno) {
        String result = "";
        List<TranslateLrcLineInfo> translateLrcLineInfos = lyricsIfno.getTranslateLrcLineInfos();
        if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
            StringBuilder lyricsCom = new StringBuilder();
            for (int i = 0; i < translateLrcLineInfos.size(); i++) {
                TranslateLrcLineInfo translateLrcLineInfo = translateLrcLineInfos.get(i);
                lyricsCom.append(translateLrcLineInfo.getLineLyrics() + "\n");
            }
            result += LEGAL_EXTRA_LYRICS_PREFIX + "(" + Base64.encodeToString(lyricsCom.toString().getBytes(), Base64.NO_WRAP) + ")\n";
        }
        return result;
    }

    /**
     * 获取lrc歌词
     *
     * @param lyricsIfno
     * @return
     */
    private String getLrcContent(LyricsInfo lyricsIfno) {
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
        String result = "";
        if (lyricsLineInfos != null && lyricsLineInfos.size() > 0) {
            StringBuilder lyricsCom = new StringBuilder();
            // 将每行歌词，放到有序的map，判断已重复的歌词
            LinkedHashMap<String, List<Integer>> lyricsLineInfoMapResult = new LinkedHashMap<String, List<Integer>>();

            for (int i = 0; i < lyricsLineInfos.size(); i++) {
                LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
                String saveLineLyrics = lyricsLineInfo.getLineLyrics();
                List<Integer> indexs = null;
                // 如果已存在该行歌词，则往里面添加歌词行索引
                if (lyricsLineInfoMapResult.containsKey(saveLineLyrics)) {
                    indexs = lyricsLineInfoMapResult.get(saveLineLyrics);
                } else {
                    indexs = new ArrayList<Integer>();
                }
                indexs.add(i);
                lyricsLineInfoMapResult.put(saveLineLyrics, indexs);
            }
            // 遍历
            for (Map.Entry<String, List<Integer>> entry : lyricsLineInfoMapResult
                    .entrySet()) {
                List<Integer> indexs = entry.getValue();
                // 当前行歌词文本
                String saveLineLyrics = entry.getKey();
                StringBuilder timeText = new StringBuilder();// 时间标签内容

                for (int i = 0; i < indexs.size(); i++) {
                    int key = indexs.get(i);
                    LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(key);
                    // 获取开始时间
                    timeText.append("[" + TimeUtils.parseMMSSFFString(lyricsLineInfo.getStartTime()) + "]");
                }
                lyricsCom.append(timeText.toString() + "");
                lyricsCom.append("" + saveLineLyrics + "\n");
            }
            result += LEGAL_LYRICS_LINE_PREFIX + "(" + Base64.encodeToString(lyricsCom.toString().getBytes(), Base64.NO_WRAP) + ")\n";
        }
        return result;
    }

    /**
     * 获取动感歌词
     *
     * @param lyricsIfno
     * @return
     */
    private String getDynamicContent(LyricsInfo lyricsIfno) {
        String result = "";
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfoTreeMap = lyricsIfno.getLyricsLineInfoTreeMap();
        if (lyricsLineInfoTreeMap != null && lyricsLineInfoTreeMap.size() > 0) {
            StringBuilder lyricsCom = new StringBuilder();
            for (int i = 0; i < lyricsLineInfoTreeMap.size(); i++) {
                LyricsLineInfo lyricsLineInfo = lyricsLineInfoTreeMap.get(i);
                lyricsCom.append("[" + lyricsLineInfo.getStartTime() + "," + (lyricsLineInfo.getEndTime() - lyricsLineInfo.getStartTime()) + "]");
                String[] lyricsWords = lyricsLineInfo.getLyricsWords();
                int[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
                for (int j = 0; j < lyricsWords.length; j++) {
                    lyricsCom.append("(0" + "," + wordsDisInterval[j] + ")" + lyricsWords[j]);
                }
                lyricsCom.append("\n");
            }
            result += LEGAL_DLYRICS_LINE_PREFIX + "(" + Base64.encodeToString(lyricsCom.toString().getBytes(), Base64.NO_WRAP) + ")\n";
        }
        return result;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("lrcwy");
    }

    @Override
    public String getSupportFileExt() {
        return "lrcwy";
    }
}
