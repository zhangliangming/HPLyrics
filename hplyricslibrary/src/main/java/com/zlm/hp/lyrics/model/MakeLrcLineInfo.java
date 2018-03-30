package com.zlm.hp.lyrics.model;

import java.util.TreeMap;

/**
 * 制作歌词行数据
 * Created by zhangliangming on 2018-03-29.
 */

public class MakeLrcLineInfo {

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
     * 歌词索引，-1是未读，-2是已经完成
     */
    private int mLrcIndex = -1;
    /**
     * 行歌词
     */
    private LyricsLineInfo mLyricsLineInfo;

    /**
     * 每个字时间集合
     */
    private TreeMap<Integer, WordDisInterval> mWordDisIntervals = new TreeMap<Integer, WordDisInterval>();

    /**
     * 设置当前歌曲索引
     *
     * @param curPlayingTime
     */
    public boolean play(long curPlayingTime) {
        //选中
        if (mStatus == STATUS_SELECTED) {
            mLrcIndex++;
            int preLrcIndex = mLrcIndex - 1;
            if (mWordDisIntervals.containsKey(preLrcIndex)) {
                //设置前一个字的结束时间
                WordDisInterval wordDisInterval = mWordDisIntervals.get(preLrcIndex);
                wordDisInterval.setEndTime((int) curPlayingTime);
                mWordDisIntervals.put(preLrcIndex, wordDisInterval);
            }

            //判断是否完成
            if (mLrcIndex == mLyricsLineInfo.getLyricsWords().length) {

                mLrcIndex = mLyricsLineInfo.getLyricsWords().length - 1;
                mStatus = STATUS_FINISH;

                return true;
            }

            //设置当前字的开始时间
            WordDisInterval wordDisInterval = new WordDisInterval();
            wordDisInterval.setStartTime((int) curPlayingTime);
            mWordDisIntervals.put(mLrcIndex, wordDisInterval);


        }
        return false;
    }

    /**
     * 设置回滚
     */
    public void back() {
        //选中
        if (mStatus == STATUS_SELECTED) {
            mLrcIndex--;
            if (mLrcIndex < -1) {
                mLrcIndex = -1;
            }
            //后退时，删除当前的歌词字时间
            int nextLrcIndex = mLrcIndex + 1;
            if (mWordDisIntervals.containsKey(nextLrcIndex)) {
                mWordDisIntervals.remove(nextLrcIndex);
            }
            //
            if (mWordDisIntervals.containsKey(mLrcIndex)) {
                WordDisInterval wordDisInterval = mWordDisIntervals.get(mLrcIndex);
                wordDisInterval.setEndTime(0);
                mWordDisIntervals.put(mLrcIndex, wordDisInterval);
            }
        }
    }

    public void setLyricsLineInfo(LyricsLineInfo mLyricsLineInfo) {
        this.mLyricsLineInfo = mLyricsLineInfo;
    }


    /**
     * 重置
     */
    public void reset() {
        mStatus = STATUS_NONE;
        mLrcIndex = -1;
        mWordDisIntervals.clear();
    }

    /**
     * 获取该行完成后的歌词数据
     *
     * @return
     */
    public LyricsLineInfo getFinishLrcLineInfo() {
        if (mStatus == STATUS_FINISH) {
            int startTime = 0;
            int endTime = 0;
            int[] wDisIntervals = new int[mWordDisIntervals.size()];
            for (int j = 0; j < mWordDisIntervals.size(); j++) {
                WordDisInterval wordDisInterval = mWordDisIntervals.get(j);
                if (j == 0) {
                    startTime = wordDisInterval.getStartTime();
                }
                if (j == mWordDisIntervals.size() - 1) {
                    endTime = wordDisInterval.getEndTime();

                }
                int time = wordDisInterval.getEndTime()
                        - wordDisInterval.getStartTime();
                wDisIntervals[j] = time;
            }
            mLyricsLineInfo.setStartTime(startTime);
            mLyricsLineInfo.setEndTime(endTime);
            mLyricsLineInfo.setWordsDisInterval(wDisIntervals);
            return mLyricsLineInfo;
        }
        return null;
    }

    public LyricsLineInfo getLyricsLineInfo() {
        return mLyricsLineInfo;
    }

    public int getLrcIndex() {
        return mLrcIndex;
    }

    public void setStatus(int status) {
        if (mStatus != STATUS_FINISH) {
            this.mStatus = status;
        }
    }

    public int getStatus() {
        return mStatus;
    }


    /**
     * 单个歌词字时间实体类
     *
     * @author zhangliangming
     */
    class WordDisInterval {
        /**
         * 开始时间
         */
        int startTime;
        /**
         * 结束时间
         */
        int endTime;

        public int getStartTime() {
            return startTime;
        }

        public void setStartTime(int startTime) {
            this.startTime = startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public void setEndTime(int endTime) {
            this.endTime = endTime;
        }
    }
}
