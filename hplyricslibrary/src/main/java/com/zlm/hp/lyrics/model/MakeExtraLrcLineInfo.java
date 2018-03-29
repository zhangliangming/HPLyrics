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
}
