package com.zlm.hp.lyrics;

import android.util.Base64;

import com.zlm.hp.lyrics.formats.LyricsFileReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.lyrics.utils.LyricsUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 歌词读管理器
 * Created by zhangliangming on 2018-02-25.
 */

public class LyricsReader {

    /**
     * 时间补偿值,其单位是毫秒，正值表示整体提前，负值相反。这是用于总体调整显示快慢的。
     */
    private long mDefOffset = 0;
    /**
     * 增量
     */
    private long mOffset = 0;

    /**
     * 歌词类型
     */
    private int mLyricsType = LyricsInfo.DYNAMIC;

    /**
     * 歌词文件路径
     */
    private String mLrcFilePath;

    /**
     * 文件hash
     */
    private String mHash;

    /**
     * 原始歌词列表
     */
    private TreeMap<Integer, LyricsLineInfo> mLrcLineInfos;
    /**
     * 原始翻译行歌词列表
     */
    private List<LyricsLineInfo> mTranslateLrcLineInfos;
    /**
     * 原始音译歌词行
     */
    private List<LyricsLineInfo> mTransliterationLrcLineInfos;

    private LyricsInfo mLyricsInfo;

    public LyricsReader() {

    }


    /**
     * 加载歌词数据
     *
     * @param lyricsFile
     */
    public void loadLrc(File lyricsFile) throws Exception {

        this.mLrcFilePath = lyricsFile.getPath();
        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(lyricsFile);
        LyricsInfo lyricsInfo = lyricsFileReader.readFile(lyricsFile);
        parser(lyricsInfo);
    }

    /**
     * @param base64FileContentString 歌词base64文件
     * @param saveLrcFile             要保存的的lrc文件
     */
    public void loadLrc(String base64FileContentString, File saveLrcFile) throws Exception {
        loadLrc(Base64.decode(base64FileContentString, Base64.NO_WRAP), saveLrcFile, saveLrcFile.getName());
    }

    /**
     * @param base64FileContentString 歌词base64文件
     * @param saveLrcFile             要保存的的lrc文件
     * @param fileName                含后缀名的文件名称
     */
    public void loadLrc(String base64FileContentString, File saveLrcFile, String fileName) throws Exception {
        loadLrc(Base64.decode(base64FileContentString, Base64.NO_WRAP), saveLrcFile, fileName);
    }


    /**
     * @param base64ByteArray 歌词base64数组
     * @param saveLrcFile
     */
    public void loadLrc(byte[] base64ByteArray, File saveLrcFile) throws Exception {
        loadLrc(base64ByteArray, saveLrcFile, saveLrcFile.getName());
    }


    /**
     * @param base64ByteArray 歌词base64数组
     * @param saveLrcFile
     * @param fileName
     */
    public void loadLrc(byte[] base64ByteArray, File saveLrcFile, String fileName) throws Exception {
        if (saveLrcFile != null)
            mLrcFilePath = saveLrcFile.getPath();
        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(fileName);
        LyricsInfo lyricsInfo = lyricsFileReader.readLrcText(base64ByteArray, saveLrcFile);
        parser(lyricsInfo);

    }

    /**
     * @param lrcContent  lrc歌词内容
     * @param saveLrcFile 歌词文件保存路径
     * @return
     * @throws Exception
     */
    public void readLrcText(String lrcContent, File saveLrcFile) throws Exception {
        readLrcText(lrcContent, null, saveLrcFile, saveLrcFile.getName());
    }

    /**
     * @param lrcContent      lrc歌词内容
     * @param extraLrcContent 额外歌词内容（翻译歌词、音译歌词）
     * @param saveLrcFile     歌词文件保存路径
     * @return
     * @throws Exception
     */
    public void readLrcText(String lrcContent, String extraLrcContent, File saveLrcFile) throws Exception {
        readLrcText(lrcContent, extraLrcContent, saveLrcFile, saveLrcFile.getName());
    }

    /**
     * @param lrcContent      lrc歌词内容
     * @param extraLrcContent 额外歌词内容（翻译歌词、音译歌词）
     * @param saveLrcFile     歌词文件保存路径
     * @return
     * @throws Exception
     */
    public void readLrcText(String lrcContent, String extraLrcContent, File saveLrcFile, String fileName) throws Exception {
        readLrcText(null, lrcContent, extraLrcContent, saveLrcFile, fileName);
    }

    /**
     * @param dynamicContent  动感歌词内容
     * @param lrcContent      lrc歌词内容
     * @param extraLrcContent 额外歌词内容（翻译歌词、音译歌词）
     * @param saveLrcFile     歌词文件保存路径
     * @return
     * @throws Exception
     */
    public void readLrcText(String dynamicContent, String lrcContent, String extraLrcContent, File saveLrcFile) throws Exception {
        readLrcText(dynamicContent, lrcContent, extraLrcContent, saveLrcFile, saveLrcFile.getName());
    }

    /**
     * @param dynamicContent  动感歌词内容
     * @param lrcContent      lrc歌词内容
     * @param extraLrcContent 额外歌词内容（翻译歌词、音译歌词）
     * @param saveLrcFile     歌词文件保存路径
     * @return
     * @throws Exception
     */
    public void readLrcText(String dynamicContent, String lrcContent, String extraLrcContent, File saveLrcFile, String fileName) throws Exception {
        if (saveLrcFile != null)
            mLrcFilePath = saveLrcFile.getPath();
        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(fileName);
        LyricsInfo lyricsInfo = lyricsFileReader.readLrcText(dynamicContent, lrcContent, extraLrcContent, saveLrcFile.getPath());
        parser(lyricsInfo);
    }


    /**
     * 解析
     *
     * @param lyricsInfo
     */
    private void parser(LyricsInfo lyricsInfo) {
        mLyricsInfo = lyricsInfo;
        mLyricsType = lyricsInfo.getLyricsType();
        Map<String, Object> tags = lyricsInfo.getLyricsTags();
        if (tags.containsKey(LyricsTag.TAG_OFFSET)) {
            mDefOffset = 0;
            try {
                mDefOffset = Long.parseLong((String) tags.get(LyricsTag.TAG_OFFSET));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mDefOffset = 0;
        }
        //默认歌词行
        mLrcLineInfos = lyricsInfo.getLyricsLineInfoTreeMap();
        //翻译歌词集合
        if (lyricsInfo.getTranslateLrcLineInfos() != null && lyricsInfo.getTranslateLrcLineInfos().size() > 0)
            mTranslateLrcLineInfos = LyricsUtils.getTranslateLrc(mLyricsType, mLrcLineInfos, lyricsInfo.getTranslateLrcLineInfos());
        //音译歌词集合
        if (lyricsInfo.getTransliterationLrcLineInfos() != null && lyricsInfo.getTransliterationLrcLineInfos().size() > 0)
            mTransliterationLrcLineInfos = LyricsUtils.getTransliterationLrc(mLyricsType, mLrcLineInfos, lyricsInfo.getTransliterationLrcLineInfos());

    }


    ////////////////////////////////////////////////////////////////////////////////


    public int getLyricsType() {
        return mLyricsType;
    }

    public TreeMap<Integer, LyricsLineInfo> getLrcLineInfos() {
        return mLrcLineInfos;
    }

    public List<LyricsLineInfo> getTranslateLrcLineInfos() {
        return mTranslateLrcLineInfos;
    }

    public List<LyricsLineInfo> getTransliterationLrcLineInfos() {
        return mTransliterationLrcLineInfos;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String mHash) {
        this.mHash = mHash;
    }

    public String getLrcFilePath() {
        return mLrcFilePath;
    }

    public void setLrcFilePath(String mLrcFilePath) {
        this.mLrcFilePath = mLrcFilePath;
    }

    public long getOffset() {
        return mOffset;
    }

    public void setOffset(long offset) {
        this.mOffset = offset;
    }

    public LyricsInfo getLyricsInfo() {
        return mLyricsInfo;
    }

    public void setLyricsType(int mLyricsType) {
        this.mLyricsType = mLyricsType;
    }

    public void setLrcLineInfos(TreeMap<Integer, LyricsLineInfo> mLrcLineInfos) {
        this.mLrcLineInfos = mLrcLineInfos;
    }

    public void setTranslateLrcLineInfos(List<LyricsLineInfo> mTranslateLrcLineInfos) {
        this.mTranslateLrcLineInfos = mTranslateLrcLineInfos;
    }

    public void setTransliterationLrcLineInfos(List<LyricsLineInfo> mTransliterationLrcLineInfos) {
        this.mTransliterationLrcLineInfos = mTransliterationLrcLineInfos;
    }

    public void setLyricsInfo(LyricsInfo lyricsInfo) {
        /**
         * 重新解析歌词
         */
        parser(lyricsInfo);
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * 播放的时间补偿值
     *
     * @return
     */
    public long getPlayOffset() {
        return mDefOffset + mOffset;
    }
}
