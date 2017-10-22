package com.example.zejian.chetai2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.security.PublicKey;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;

/**
 * Created by zejian on 10/18/2017.
 */

public class SecondActivity extends AppCompatActivity{

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION =200;
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder= null;

    private PlayButton mPlaybutton = null;
    private MediaPlayer mPlayer = null;

    BluetoothHeadset mBluetoothHeadset;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private AudioManager mAudioManager = null;

    //request permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted =false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    //self
//    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener(){
//        public void onServiceConnected(int profile, BluetoothProfile proxy){
//            if(profile == BluetoothProfile.HEADSET){
//                mBluetoothHeadset = (BluetoothHeadset) proxy;
//            }
//        }
//        public void onServiceDisconnected(int profile){
//            if(profile == BluetoothProfile.HEADSET){
//                mBluetoothHeadset = null;
//            }
//        }
//
//    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    private  void onRecord(boolean start){
        if(start){
            startRecording();
        } else{
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if(start){
            startPlaying();
        }
        else{
            stopPlaying();
        }
    }

    private void startPlaying(){
        mPlayer = new MediaPlayer();
        try{
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        }
        catch (IOException e ){
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying(){
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording(){
        //Establish connection to the proxy
    //    mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET);


        mRecorder= new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try{
            mRecorder.prepare();
        }catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed");
        }


        mAudioManager.stopBluetoothSco();
        mAudioManager.startBluetoothSco();//蓝牙录音的关键，启动SCO连接，耳机话筒才起作用

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    Log.i(LOG_TAG, "AudioManager.SCO_AUDIO_STATE_CONNECTED");
                    mAudioManager.setBluetoothScoOn(true);  //打开SCO
                    Log.i(LOG_TAG, "Routing:" + mAudioManager.isBluetoothScoOn());
                    mAudioManager.setMode(AudioManager.STREAM_MUSIC);
                    mRecorder.start();//开始录音
                    unregisterReceiver(this);  //别遗漏
                }
 else {//等待一秒后再尝试启动SCO
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mAudioManager.startBluetoothSco();
                    Log.i(LOG_TAG, "再次startBluetoothSco()");

                }

                //test code to see if SCO connection state has been changed
//                while(AudioManager.SCO_AUDIO_STATE_CONNECTED == state){
//                    mAudioManager.startBluetoothSco();//蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
//                    state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

//                }
            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));


        mRecorder.start();
    }

    private void stopRecording(){
//        if(mAudioManager.isBluetoothScoOn()){
//            mAudioManager.setBluetoothScoOn(false);
//            mAudioManager.stopBluetoothSco();
//        }
        mRecorder.stop();//Before stop, you should start first, otherwise it's gonna crash!!--ZZ
        mRecorder.release();
        mRecorder = null;
//        if (mAudioManager.isBluetoothScoOn()) {
//            mAudioManager.setBluetoothScoOn(false);
//            mAudioManager.stopBluetoothSco();
//        }
    }

    class RecordButton extends Button{
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if(mStartRecording){
                    setText("Stop recording");
                }else{
                    setText("Start recording");
                }
                mStartRecording =!mStartRecording;
            }
        };

        public RecordButton(Context ctx){
            super(ctx);
            setText("Start Recording");
            setOnClickListener(clicker);
        }
    }

    //class PlayButton extends android.support.v7.widget.AppCompatButton{
    class PlayButton extends Button{
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if(mStartPlaying){
                    setText("Stop Playing");
                }else {
                    setText("Start playing");
                }
                mStartPlaying =!mStartPlaying;
            }
        };

        public PlayButton(Context ctx){
            super(ctx);
            setText("Start Playing");
            setOnClickListener(clicker);
        }
    }

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);

        mAudioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);



        LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        mPlaybutton = new PlayButton(this);
        ll.addView(mPlaybutton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0
                ));
        setContentView(ll);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mRecorder != null){
            mRecorder.release();
            mRecorder= null;
        }

        if(mPlayer != null){
            mPlayer.release();
            mPlayer=null;
        }
        //close proxy
       // mBluetoothAdapter.closeProfileProxy(mBluetoothHeadset);
    }
}
