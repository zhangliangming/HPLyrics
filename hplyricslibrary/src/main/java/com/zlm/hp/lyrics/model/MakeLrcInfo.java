package com.zlm.hp.lyrics.model;

/**
 * 制作歌词信息
 * Created by zhangliangming on 2018-04-01.
 */

public class MakeLrcInfo {

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
    private int status = STATUS_NONE;

    /**
     * 歌词索引，-1是未读，-2是已经完成
     */
    private int lrcIndex = -1;
    /**
     * 行歌词
     */
    private LyricsLineInfo lyricsLineInfo;


    /**
     * 重置
     */
    public void reset() {
        status = STATUS_NONE;
        lrcIndex = -1;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (status != STATUS_FINISH) {
            this.status = status;
        }
    }

    public int getLrcIndex() {
        return lrcIndex;
    }

    public void setLrcIndex(int lrcIndex) {
        this.lrcIndex = lrcIndex;
    }

    public LyricsLineInfo getLyricsLineInfo() {
        return lyricsLineInfo;
    }

    public void setLyricsLineInfo(LyricsLineInfo lyricsLineInfo) {
        this.lyricsLineInfo = lyricsLineInfo;
    }
}
