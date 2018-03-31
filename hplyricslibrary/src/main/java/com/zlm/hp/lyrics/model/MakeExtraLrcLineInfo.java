package com.zlm.hp.lyrics.model;


/**
 * 制作额外歌词行数据
 * Created by zhangliangming on 2018-03-29.
 */

public class MakeExtraLrcLineInfo {
    /**
     * 该行歌词
     */
    private String lineLyrics;

    /**
     * 该行额外歌词
     */
    private String extraLineLyrics;

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
     * 音译歌词
     */
    public static final int TRANSLITERATIONLRC = 0;

    /**
     * 翻译歌词
     */
    public static final int TRANSLATELRC = 1;

    /**
     * 额外歌词类型
     */
    private int extraLyricsType = TRANSLATELRC;

    public String getLineLyrics() {
        return lineLyrics;
    }

    public void setLineLyrics(String lineLyrics) {
        this.lineLyrics = lineLyrics;
    }

    public String getExtraLineLyrics() {
        return extraLineLyrics;
    }

    public void setExtraLineLyrics(String extraLineLyrics) {
        this.extraLineLyrics = extraLineLyrics;
    }

    public int getExtraLyricsType() {
        return extraLyricsType;
    }

    public void setExtraLyricsType(int extraLyricsType) {
        this.extraLyricsType = extraLyricsType;
    }

    /**
     * 重置
     */
    public void reset() {
        mStatus = STATUS_NONE;
        extraLineLyrics = "";
    }

    public void setStatus(int status) {
        if (mStatus != STATUS_FINISH) {
            this.mStatus = status;
        }
    }

    public int getStatus() {
        return mStatus;
    }

}
