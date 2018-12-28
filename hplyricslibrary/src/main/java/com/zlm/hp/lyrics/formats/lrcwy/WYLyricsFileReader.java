package com.zlm.hp.lyrics.formats.lrcwy;

import android.text.TextUtils;
import android.util.Base64;

import com.zlm.hp.lyrics.formats.LyricsFileReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.lyrics.utils.TimeUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 网易歌词读取器
 * @author: zhangliangming
 * @date: 2018-12-27 0:03
 **/
public class WYLyricsFileReader extends LyricsFileReader {
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
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsIfno = new LyricsInfo();
        lyricsIfno.setLyricsFileExt(getSupportFileExt());

        if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    getDefaultCharset()));

            Map<String, Object> lyricsTags = new HashMap<String, Object>();
            String lineInfo = "";
            while ((lineInfo = br.readLine()) != null) {

                // 解析歌词
                parserLineInfos(lyricsIfno,
                        lyricsTags, lineInfo);

            }
            in.close();
            in = null;
            // 设置歌词的标签类
            lyricsIfno.setLyricsTags(lyricsTags);
        }
        return lyricsIfno;
    }

    /**
     * 解析歌词文件
     *
     * @param lyricsIfno
     * @param lyricsTags
     * @param lineInfo
     */
    private void parserLineInfos(LyricsInfo lyricsIfno, Map<String, Object> lyricsTags, String lineInfo) throws Exception {
        if (lineInfo.startsWith(LEGAL_SONGNAME_PREFIX)) {
            int startIndex = LEGAL_SONGNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            //
            lyricsTags.put(LyricsTag.TAG_TITLE,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_SINGERNAME_PREFIX)) {
            int startIndex = LEGAL_SINGERNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_ARTIST,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_OFFSET_PREFIX)) {
            int startIndex = LEGAL_OFFSET_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_OFFSET,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_BY_PREFIX)
                || lineInfo.startsWith(LEGAL_TOTAL_PREFIX)
                || lineInfo.startsWith(LEGAL_AL_PREFIX)) {

            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else {
            if (lineInfo.startsWith(LEGAL_LYRICS_LINE_PREFIX)) {
                //lrc歌词
                lyricsIfno.setLyricsType(LyricsInfo.LRC);
                int startIndex = lineInfo.indexOf("(");
                int endIndex = lineInfo.lastIndexOf(")");
                String lrcContent = lineInfo.substring(startIndex + 1, endIndex);
                lrcContent = new String(Base64.decode(lrcContent, Base64.NO_WRAP));
                parserLrcCom(lyricsIfno, lrcContent);

            } else if (lineInfo.startsWith(LEGAL_DLYRICS_LINE_PREFIX)) {
                lyricsIfno.setLyricsType(LyricsInfo.DYNAMIC);
                //动感歌词
                int startIndex = lineInfo.indexOf("(");
                int endIndex = lineInfo.lastIndexOf(")");
                String dynamicContent = lineInfo.substring(startIndex + 1, endIndex);
                dynamicContent = new String(Base64.decode(dynamicContent, Base64.NO_WRAP));
                parseDynamicLrc(lyricsIfno, dynamicContent);

            } else if (lineInfo.startsWith(LEGAL_EXTRA_LYRICS_PREFIX)) {
                //额外歌词
                int startIndex = lineInfo.indexOf("(");
                int endIndex = lineInfo.lastIndexOf(")");
                String extraLrcContent = lineInfo.substring(startIndex + 1, endIndex);
                extraLrcContent = new String(Base64.decode(extraLrcContent, Base64.NO_WRAP));
                parserExtraLrc(lyricsIfno, extraLrcContent);
            }
        }

    }

    /**
     * 解析额外歌词
     * @param lyricsIfno
     * @param extraLrcContent
     */
    private void parserExtraLrc(LyricsInfo lyricsIfno, String extraLrcContent) {
        // 翻译歌词集合
        List<TranslateLrcLineInfo> translateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();

        // 获取歌词内容
        String lrcContents[] = extraLrcContent.split("\n");
        for (int i = 0; i < lrcContents.length; i++) {
            String lineInfo = lrcContents[i];
                // 翻译行歌词
                TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
                translateLrcLineInfo.setLineLyrics(lineInfo.trim());
                translateLrcLineInfos.add(translateLrcLineInfo);
        }
        // 添加翻译歌词
        if (translateLrcLineInfos.size() > 0) {
            lyricsIfno.setTranslateLrcLineInfos(translateLrcLineInfos);
        }
    }

    @Override
    public LyricsInfo readLrcText(String lrcContent, String extraLrcContent, String lyricsFilePath) throws Exception {
        return readLrcText(null, lrcContent, extraLrcContent, lyricsFilePath);
    }

    @Override
    public LyricsInfo readLrcText(String dynamicContent, String lrcContent, String extraLrcContent, String lyricsFilePath) throws Exception {
        return parserLrc(dynamicContent, lrcContent, extraLrcContent, lyricsFilePath);
    }

    /**
     * 解析歌词，其中动感歌词和lrc歌词只能2个选1个
     *
     * @param dynamicContent  动感歌词内容
     * @param lrcContent      lrc歌词内容
     * @param extraLrcContent 额外歌词内容（翻译歌词、音译歌词）
     * @param lyricsFilePath  歌词文件保存路径
     * @return
     * @throws Exception
     */
    private LyricsInfo parserLrc(String dynamicContent, String lrcContent, String extraLrcContent, String lyricsFilePath) throws Exception {
        LyricsInfo lyricsInfo = new LyricsInfo();
        lyricsInfo.setLyricsFileExt(getSupportFileExt());
        if (!TextUtils.isEmpty(dynamicContent)) {
            lyricsInfo.setLyricsType(LyricsInfo.DYNAMIC);
            //解析动感歌词
            parseDynamicLrc(lyricsInfo, dynamicContent);
        } else if (!TextUtils.isEmpty(lrcContent)) {
            lyricsInfo.setLyricsType(LyricsInfo.LRC);
            //解析lrc歌词
            parserLrcCom(lyricsInfo, lrcContent);
        }
        if (!TextUtils.isEmpty(extraLrcContent)) {
            //解析额外歌词
            parserTranslateLrc(lyricsInfo, extraLrcContent);
        }

        if (!TextUtils.isEmpty(lyricsFilePath)) {
            //保存歌词
            new WYLyricsFileWriter().writer(lyricsInfo, lyricsFilePath);
        }
        return lyricsInfo;
    }

    /**
     * 解析翻译歌词
     *
     * @param lyricsIfno
     * @param translateLrcContent
     */
    private void parserTranslateLrc(LyricsInfo lyricsIfno, String translateLrcContent) throws Exception {

        // 翻译歌词集合
        List<TranslateLrcLineInfo> translateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();

        // 获取歌词内容
        String lrcContents[] = translateLrcContent.split("\n");
        for (int i = 0; i < lrcContents.length; i++) {
            String lineInfo = lrcContents[i];
            //时间标签
            String timeRegex = "\\[\\d+:\\d+.\\d+\\]";
            String timeRegexs = "(" + timeRegex + ")+";
            // 如果含有时间标签，则是歌词行
            Pattern pattern = Pattern.compile(timeRegexs);
            Matcher matcher = pattern.matcher(lineInfo);
            if (matcher.find()) {
                //获取歌词内容
                int timeEndIndex = matcher.end();
                String lineLyrics = lineInfo.substring(timeEndIndex,
                        lineInfo.length()).trim();
                // 翻译行歌词
                TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
                translateLrcLineInfo.setLineLyrics(lineLyrics);

                translateLrcLineInfos.add(translateLrcLineInfo);
            }
        }
        // 添加翻译歌词
        if (translateLrcLineInfos.size() > 0) {
            lyricsIfno.setTranslateLrcLineInfos(translateLrcLineInfos);
        }
    }

    /**
     * 解析动感歌词
     *
     * @param lyricsInfo
     * @param dynamicContent
     */
    private void parseDynamicLrc(LyricsInfo lyricsInfo, String dynamicContent) throws Exception {
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
        Map<String, Object> lyricsTags = new HashMap<String, Object>();
        String lrcContents[] = dynamicContent.split("\n");
        int index = 0;
        for (int i = 0; i < lrcContents.length; i++) {
            String lineInfo = lrcContents[i];
            // 解析动感歌词行
            LyricsLineInfo lyricsLineInfo = parserDynamicLrcLineInfos(lyricsTags,
                    lineInfo);
            if (lyricsLineInfo != null) {
                lyricsLineInfos.put(index, lyricsLineInfo);
                index++;
            }

        }

        // 设置歌词的标签类
        lyricsInfo.setLyricsTags(lyricsTags);
        //
        lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
    }

    /**
     * 解析动感歌词行
     *
     * @param lyricsTags 歌曲标签
     * @param lineInfo   行歌词内容
     */
    private LyricsLineInfo parserDynamicLrcLineInfos(Map<String, Object> lyricsTags, String lineInfo) throws Exception {
        LyricsLineInfo lyricsLineInfo = null;
        if (lineInfo.startsWith(LEGAL_SONGNAME_PREFIX)) {
            int startIndex = LEGAL_SONGNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            //
            lyricsTags.put(LyricsTag.TAG_TITLE,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_SINGERNAME_PREFIX)) {
            int startIndex = LEGAL_SINGERNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_ARTIST,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_OFFSET_PREFIX)) {
            int startIndex = LEGAL_OFFSET_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_OFFSET,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_BY_PREFIX)
                || lineInfo.startsWith(LEGAL_TOTAL_PREFIX)
                || lineInfo.startsWith(LEGAL_AL_PREFIX)) {

            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else {
            //时间标签
            Pattern pattern = Pattern.compile("\\[\\d+,\\d+\\]");
            Matcher matcher = pattern.matcher(lineInfo);
            if (matcher.find()) {
                lyricsLineInfo = new LyricsLineInfo();
                // [此行开始时刻距0时刻的毫秒数,此行持续的毫秒数](0,此字持续的毫秒数)歌(0,此字持续的毫秒数)词(0,此字持续的毫秒数)正(0,此字持续的毫秒数)文
                // 获取行的出现时间和结束时间
                int mStartIndex = matcher.start();
                int mEndIndex = matcher.end();
                String lineTime[] = lineInfo.substring(mStartIndex + 1,
                        mEndIndex - 1).split(",");
                //

                int startTime = Integer.parseInt(lineTime[0]);
                int endTime = startTime + Integer.parseInt(lineTime[1]);
                lyricsLineInfo.setEndTime(endTime);
                lyricsLineInfo.setStartTime(startTime);
                // 获取歌词信息
                String lineContent = lineInfo.substring(mEndIndex,
                        lineInfo.length());

                // 歌词匹配的正则表达式
                String regex = "\\(\\d+,\\d+\\)";
                Pattern lyricsWordsPattern = Pattern.compile(regex);
                Matcher lyricsWordsMatcher = lyricsWordsPattern
                        .matcher(lineContent);

                // 歌词分隔
                String lineLyricsTemp[] = lineContent.split(regex);
                String[] lyricsWords = getLyricsWords(lineLyricsTemp);
                lyricsLineInfo.setLyricsWords(lyricsWords);

                // 获取每个歌词的时间
                int wordsDisInterval[] = new int[lyricsWords.length];
                int index = 0;
                while (lyricsWordsMatcher.find()) {

                    //验证
                    if (index >= wordsDisInterval.length) {
                        throw new Exception("字标签个数与字时间标签个数不相符");
                    }

                    //
                    String wordsDisIntervalStr = lyricsWordsMatcher.group();
                    String wordsDisIntervalStrTemp = wordsDisIntervalStr
                            .substring(wordsDisIntervalStr.indexOf('(') + 1, wordsDisIntervalStr.lastIndexOf(')'));
                    String wordsDisIntervalTemp[] = wordsDisIntervalStrTemp
                            .split(",");
                    wordsDisInterval[index++] = Integer
                            .parseInt(wordsDisIntervalTemp[1]);
                }
                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);

                // 获取当行歌词
                String lineLyrics = lyricsWordsMatcher.replaceAll("");
                lyricsLineInfo.setLineLyrics(lineLyrics);

            }
        }
        return lyricsLineInfo;
    }


    /**
     * 分隔每个歌词
     *
     * @param lineLyricsTemp
     * @return
     */
    private String[] getLyricsWords(String[] lineLyricsTemp) throws Exception {
        String temp[] = null;
        if (lineLyricsTemp.length < 2) {
            return new String[lineLyricsTemp.length];
        }
        //
        temp = new String[lineLyricsTemp.length - 1];
        for (int i = 1; i < lineLyricsTemp.length; i++) {
            temp[i - 1] = lineLyricsTemp[i];
        }
        return temp;
    }


    /**
     * 解析lrc歌词
     *
     * @param lyricsInfo
     * @param lrcContent
     */
    private void parserLrcCom(LyricsInfo lyricsInfo, String lrcContent) throws Exception {
        // 这里面key为该行歌词的开始时间，方便后面排序
        SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
        Map<String, Object> lyricsTags = new HashMap<String, Object>();
        String lrcContents[] = lrcContent.split("\n");
        for (int i = 0; i < lrcContents.length; i++) {
            String lineInfo = lrcContents[i];
            // 解析lrc歌词行
            parserLrcLineInfos(lyricsLineInfosTemp,
                    lyricsTags, lineInfo);

        }
        // 重新封装
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
        int index = 0;
        Iterator<Integer> it = lyricsLineInfosTemp.keySet().iterator();
        while (it.hasNext()) {
            lyricsLineInfos
                    .put(index++, lyricsLineInfosTemp.get(it.next()));
        }
        it = null;
        // 设置歌词的标签类
        lyricsInfo.setLyricsTags(lyricsTags);
        //
        lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
    }

    /**
     * 解析行歌词
     *
     * @param lyricsLineInfosTemp 排序集合
     * @param lyricsTags          歌曲标签
     * @param lineInfo            行歌词内容
     * @throws Exception
     */
    private void parserLrcLineInfos(SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp, Map<String, Object> lyricsTags, String lineInfo) throws Exception {
        LyricsLineInfo lyricsLineInfo = null;
        if (lineInfo.startsWith(LEGAL_SONGNAME_PREFIX)) {
            int startIndex = LEGAL_SONGNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            //
            lyricsTags.put(LyricsTag.TAG_TITLE,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_SINGERNAME_PREFIX)) {
            int startIndex = LEGAL_SINGERNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_ARTIST,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_OFFSET_PREFIX)) {
            int startIndex = LEGAL_OFFSET_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_OFFSET,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_BY_PREFIX)
                || lineInfo.startsWith(LEGAL_TOTAL_PREFIX)
                || lineInfo.startsWith(LEGAL_AL_PREFIX)) {

            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else {
            //时间标签
            String timeRegex = "\\[\\d+:\\d+.\\d+\\]";
            String timeRegexs = "(" + timeRegex + ")+";
            // 如果含有时间标签，则是歌词行
            Pattern pattern = Pattern.compile(timeRegexs);
            Matcher matcher = pattern.matcher(lineInfo);
            if (matcher.find()) {
                Pattern timePattern = Pattern.compile(timeRegex);
                Matcher timeMatcher = timePattern
                        .matcher(matcher.group());
                //遍历时间标签
                while (timeMatcher.find()) {
                    lyricsLineInfo = new LyricsLineInfo();
                    //获取开始时间
                    String startTimeString = timeMatcher.group().trim();
                    int startTime = TimeUtils.parseInteger(startTimeString.substring(startTimeString.indexOf('[') + 1, startTimeString.lastIndexOf(']')));
                    lyricsLineInfo.setStartTime(startTime);
                    //获取歌词内容
                    int timeEndIndex = matcher.end();
                    String lineLyrics = lineInfo.substring(timeEndIndex,
                            lineInfo.length()).trim();
                    lyricsLineInfo.setLineLyrics(lineLyrics);
                    lyricsLineInfosTemp.put(startTime, lyricsLineInfo);
                }
            }
        }
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
