package com.example.mediaplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button playMusic;
    private Button pauseMusic;
    private Button nextMusic;
    private Button preciousMusic;
    private TextView mTextView;
    private SeekBar mSeekBar;

    private SimpleDateFormat time = new SimpleDateFormat("m:ss");
    private Handler mHandler = new Handler();
    //绑定服务的intent
    Intent MediaServiceIntent;
    private MediaService.MyBinder mMyBinder;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mMyBinder = (MediaService.MyBinder) service;
            Log.d("MainActivity", "Service 与 Activity已连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniView();
        MediaServiceIntent = new Intent(this, MediaService.class);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else{
        //够了就设置路径等，准备播放
        bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }
}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    bindService(MediaServiceIntent,mServiceConnection,BIND_AUTO_CREATE);
                }else{
                    Toast.makeText(this, "权限不够获取不到音乐，程序将退出", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mMyBinder = (MediaService.MyBinder) service;
            mSeekBar.setMax(mMyBinder.getProgress());
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser){
                        //如果不判断是否来自用户操作进度条，会不断执行下面语句里面的逻辑，然后就会卡顿
                        mMyBinder.seekToPosition(seekBar.getProgress());
                    };
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            mHandler.post(mRunnable);
            Log.d("MainActivity", "Service与Activity已连接");
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private void iniView() {
        playMusic = (Button) findViewById(R.id.play);
        pauseMusic = (Button) findViewById(R.id.pause);
        nextMusic = (Button) findViewById(R.id.next);
        preciousMusic = (Button) findViewById(R.id.precious);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mTextView = (TextView) findViewById(R.id.text1);
        playMusic.setOnClickListener(this);
        pauseMusic.setOnClickListener(this);
        preciousMusic.setOnClickListener(this);
        nextMusic.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                mMyBinder.playMusic();
                break;
            case R.id.pause:
                mMyBinder.pauseMusic();
                break;
            case R.id.next:
                mMyBinder.nextMusic();
                break;
            case R.id.precious:
                mMyBinder.preciousMusic();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //我们的handler发送是定时1000s发送的，如果不关闭，MedPlayer release掉了还在获取getCurrentPosition就会爆IllegalStateException错
        mHandler.removeCallbacks(mRunnable);
        mMyBinder.closeMedia();
        unbindService(mServiceConnection);
    }
    /*
    更新UI的runnable
     */

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mSeekBar.setProgress(mMyBinder.getPlayPosition());
            mTextView.setText(time.format(mMyBinder.getPlayPosition()) + "s");
            mHandler.postDelayed(mRunnable,1000);
        }
    };
}
