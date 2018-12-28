package com.zlm.hp.lyrics.model.make;

import com.zlm.hp.lyrics.model.LyricsLineInfo;

import java.util.TreeMap;

/**
 * 制作歌词行数据
 * Created by zhangliangming on 2018-03-29.
 */

public class MakeLrcLineInfo extends MakeLrcInfo {

    /**
     * 每个字时间集合
     */
    private TreeMap<Integer, WordDisInterval> mWordDisIntervals = new TreeMap<Integer, WordDisInterval>();

    private byte[] lock = new byte[0];

    /**
     * 设置当前歌曲索引
     *
     * @param curPlayingTime
     */
    public boolean play(long curPlayingTime) {
        synchronized (lock) {

            int lrcIndex = getLrcIndex();
            lrcIndex++;
            setLrcIndex(lrcIndex);
            int preLrcIndex = lrcIndex - 1;
            if (mWordDisIntervals.containsKey(preLrcIndex)) {
                //设置前一个字的结束时间
                WordDisInterval wordDisInterval = mWordDisIntervals.get(preLrcIndex);
                wordDisInterval.setEndTime((int) curPlayingTime);
                mWordDisIntervals.put(preLrcIndex, wordDisInterval);
            }

            LyricsLineInfo lyricsLineInfo = getLyricsLineInfo();
            //判断是否完成
            if (lrcIndex == lyricsLineInfo.getLyricsWords().length) {

                lrcIndex = lyricsLineInfo.getLyricsWords().length - 1;

                //设置结束时间
                WordDisInterval wordDisInterval = mWordDisIntervals.get(lrcIndex);
                lyricsLineInfo.setEndTime(wordDisInterval.getEndTime());

                setStatus(STATUS_FINISH);
                setLrcIndex(lrcIndex);

                return true;
            }

            //设置当前字的开始时间
            WordDisInterval wordDisInterval = new WordDisInterval();
            wordDisInterval.setStartTime((int) curPlayingTime);
            mWordDisIntervals.put(lrcIndex, wordDisInterval);

            return false;
        }
    }


    /**
     * 行设置
     *
     * @param preLineEndTime 上一行结束时间
     * @param curPlayingTime 当前播放时间
     */
    public void playLine(long preLineEndTime, long curPlayingTime) {
        synchronized (lock) {
            mWordDisIntervals.clear();
            LyricsLineInfo lyricsLineInfo = getLyricsLineInfo();
            lyricsLineInfo.setStartTime((int) preLineEndTime);
            lyricsLineInfo.setEndTime((int) curPlayingTime);
            long dTime = 0;
            if (preLineEndTime < curPlayingTime) {
                dTime = curPlayingTime - preLineEndTime;
            }
            String[] lyricsWords = lyricsLineInfo.getLyricsWords();
            if (lyricsWords != null && lyricsWords.length > 0) {
                long aveTime = dTime / lyricsWords.length;
                for (int i = 0; i < lyricsWords.length; i++) {
                    //设置当前字的开始时间/结束时间
                    long startTime = lyricsLineInfo.getStartTime() + i * aveTime;
                    WordDisInterval wordDisInterval = new WordDisInterval();
                    wordDisInterval.setStartTime((int) startTime);
                    wordDisInterval.setEndTime((int) (startTime + aveTime));
                    mWordDisIntervals.put(i, wordDisInterval);
                }
                setStatus(STATUS_FINISH);
                setLrcIndex(lyricsWords.length - 1);
            }
        }
    }

    /**
     * 设置回滚
     */
    public void back() {
        synchronized (lock) {

            int lrcIndex = getLrcIndex();
            lrcIndex--;
            if (lrcIndex < -1) {
                lrcIndex = -1;
            }
            setLrcIndex(lrcIndex);
            //后退时，删除当前的歌词字时间
            int nextLrcIndex = lrcIndex + 1;
            if (mWordDisIntervals.containsKey(nextLrcIndex)) {
                mWordDisIntervals.remove(nextLrcIndex);
            }
            //
            if (mWordDisIntervals.containsKey(lrcIndex)) {
                WordDisInterval wordDisInterval = mWordDisIntervals.get(lrcIndex);
                wordDisInterval.setEndTime(0);
                mWordDisIntervals.put(lrcIndex, wordDisInterval);
            }
        }
    }

    @Override
    public void reset() {
        synchronized (lock) {
            super.reset();
            mWordDisIntervals.clear();
        }
    }

    /**
     * 获取该行完成后的歌词数据
     *
     * @return
     */
    public LyricsLineInfo getFinishLrcLineInfo() {
        synchronized (lock) {
            if (getStatus() == STATUS_FINISH) {
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
                LyricsLineInfo lyricsLineInfo = getLyricsLineInfo();
                lyricsLineInfo.setStartTime(startTime);
                lyricsLineInfo.setEndTime(endTime);
                lyricsLineInfo.setWordsDisInterval(wDisIntervals);
                return lyricsLineInfo;
            }
            return null;
        }
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
