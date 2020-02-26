package com.zlm.hp.lyrics.formats.trc;

import com.zlm.hp.lyrics.formats.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.utils.TimeUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * @Description: trc歌词生成器
 * @author: zhangliangming
 * @date: 2020-02-26 20:35
 **/

public class TrcLyricsFileWriter extends LyricsFileWriter {

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
     * 歌曲长度
     */
    private final static String LEGAL_TOTAL_PREFIX = "[total:";

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

        // 每行歌词内容
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);

            lyricsCom.append("["
                    + TimeUtils.parseMMSSFFString(lyricsLineInfo.getStartTime())
                    + "]");// 添加开始时间

            String[] lyricsWords = lyricsLineInfo.getLyricsWords();
            // 添加每个歌词的时间
            StringBuilder wordsText = new StringBuilder();
            int wordsDisInterval[] = lyricsLineInfo.getWordsDisInterval();
            for (int j = 0; j < wordsDisInterval.length; j++) {
                wordsText.append("<" + wordsDisInterval[j] + ">" + lyricsWords[j]);
            }
            lyricsCom.append(wordsText);
            lyricsCom.append("\n");
        }
        return lyricsCom.toString();
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("trc");
    }

    @Override
    public String getSupportFileExt() {
        return "trc";
    }
}
