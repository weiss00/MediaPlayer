package com.example.mediaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MediaService extends Service {
    private static final String TAG = "MediaService";
    private MyBinder mBinder = new MyBinder();
    //标记当前歌曲的序号
    private int i = 0;
    //歌曲路径
    private String[] musicPath = new String[]{
            Environment.getExternalStorageDirectory() + "/Sounds/a1.mp3",
            Environment.getExternalStorageDirectory() + "/Sounds/a2.mp3",
            Environment.getExternalStorageDirectory() + "/Sounds/a3.mp3",
            Environment.getExternalStorageDirectory() + "/Sounds/a4.mp3"
    };

    public MediaPlayer mMediaPlayer = new MediaPlayer();

    public MediaService() {
        iniMediaPlayerFile(i);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {

        /*
           播放音乐
         */

        public void playMusic() {
            if (!mMediaPlayer.isPlaying()) {
                //如果没开始播放，就开始
                mMediaPlayer.start();
            }
        }
        /*
        暂停播放
         */

        public void pauseMusic() {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        /*
        下一首
         */

        public void nextMusic() {
            if (mMediaPlayer != null && i < 4 && i >= 0) {
                //切换歌曲reset（）很重要，没有会报异常
                mMediaPlayer.reset();
                iniMediaPlayerFile(i + 1);
                //这里的if只要是为了不让歌曲的序号越界，只有四首歌
                if (i == 2) {
                } else {
                    i = i + 1;
                }
                playMusic();
            }
        }

        /*
        上一首
         */
        public void preciousMusic() {
            if (mMediaPlayer != null && i < 4 && i > 0) {
                mMediaPlayer.reset();
                iniMediaPlayerFile(i - 1);
            }
            if (i == 1) {
            } else {
                i = i - 1;
            }
            playMusic();
        }

        public void resetMusic(){
            if (!mMediaPlayer.isPlaying()){
                mMediaPlayer.reset();
                iniMediaPlayerFile(i);
            }
        }

       /* public int getPlayPosition() {
            return mMediaPlayer.getDuration();
        }
        */

        public void closeMedia() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }

        /*
        获取歌曲长度
         */
        public int getProgress() {
            return mMediaPlayer.getDuration();
        }

        /*
        获取播放位置
         */
        public int getPlayPosition(){
            return mMediaPlayer.getCurrentPosition();
        }

        /*
        播放指定位置
         */
        public void seekToPosition(int msec) {
            mMediaPlayer.seekTo(msec);
        }

        /*
        file文件到MediaPlayer对象并且准备播放音频
         */
        }
    private void iniMediaPlayerFile(int dex) {
        //获取文件路径
        try {
            mMediaPlayer.setDataSource(musicPath[dex]);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(TAG, "设置资源，准备阶段出错");
            e.printStackTrace();
        }

    }
}
