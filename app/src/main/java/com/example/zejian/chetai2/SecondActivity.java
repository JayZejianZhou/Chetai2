package com.example.zejian.chetai2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * Created by zejian on 10/18/2017.
 */

public class SecondActivity extends AppCompatActivity{

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION =200;
    private static String mFileName = null;
    public boolean mStartRecording = true;
    public boolean mStartPlaying = true;
    private MediaRecorder mRecorder= null;

    private MediaPlayer mPlayer = null;

    private AudioManager mAudioManager = null;

    //request permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted =false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    //variables for socket connection
    private TextView textResponse;
    private EditText editTextAddress, editTextPort;
    public boolean mConnect=true;
    private Button Button_connect;

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

    private void startPlaying(){ //A2DP profile to play
        mPlayer = new MediaPlayer();
        mAudioManager.setMode(AudioManager.MODE_IN_CALL);

//        //Establish A2DP connecttion
      //  if(!mAudioManager.isBluetoothA2dpOn())
       // mAudioManager.setBluetoothA2dpOn(true);
//        mAudioManager.stopBluetoothSco();
        //mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC,true);
        //mAudioManager.setRouting(AudioManager.MODE_NORMAL,AudioManager.ROUTE_BLUETOOTH_A2DP,AudioManager.ROUTE_BLUETOOTH);
        //mAudioManager.setBluetoothScoOn(true);
        try{
            mPlayer.setDataSource(mFileName);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
//        mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC,false);
    }

    private void startRecording(){
         mRecorder= new MediaRecorder();
        mRecorder.setAudioChannels(1);//set to mono
        mRecorder.setAudioSamplingRate(8000);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try{
            mRecorder.prepare();
        }catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed");
        }
        Log.e(LOG_TAG,"test if log error occur");
        if(!mAudioManager.isBluetoothScoAvailableOffCall()){
            Log.i(LOG_TAG,"does not support BL --ZZ");
            return;
        }


//        mAudioManager.stopBluetoothSco();
        mAudioManager.startBluetoothSco();//蓝牙录音的关键，启动SCO连接，耳机话筒才起作用

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(LOG_TAG,"detected the intent (ZZ)");
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    Log.e(LOG_TAG, "AudioManager.SCO_AUDIO_STATE_CONNECTED");
                    mAudioManager.setBluetoothScoOn(true);  //打开SCO
                    Log.e(LOG_TAG, "Routing:" + mAudioManager.isBluetoothScoOn());
                    mAudioManager.setMode(AudioManager.STREAM_MUSIC);

                    mRecorder.start();//开始录音
//                    unregisterReceiver(this);  //别遗漏
                }
//                else {//等待一秒后再尝试启动SCO
//                    try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    mAudioManager.startBluetoothSco();
//                    Log.i(LOG_TAG, "再次startBluetoothSco()");
//
//                    Log.e(LOG_TAG,"detected the flag in (ZZ)");
//                }

                //test code to see if SCO connection state has been changed
//                while(AudioManager.SCO_AUDIO_STATE_CONNECTED == state){
//                    mAudioManager.startBluetoothSco();//蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
//                    state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

//                }
            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));


//        mRecorder.start();
    }

    private void stopRecording(){
        mRecorder.stop();
        mRecorder.release();
        mRecorder=null;
//        if(mAudioManager.isBluetoothScoOn()){
//            mAudioManager.setBluetoothScoOn(false);
//            mAudioManager.stopBluetoothSco();
//        }

    }

    public void record_button(View view) {
        Button mButton=(Button)findViewById(R.id.record_button);
        onRecord(mStartRecording);
        if(mStartRecording){
            mButton.setText("Stop recording");
        }else{
            mButton.setText("Start recording");
        }
        mStartRecording =!mStartRecording;
    };

    public void play_button(View view){
        Button mButton=(Button)findViewById(R.id.play_button);
        onPlay(mStartPlaying);
        if(mStartPlaying){
            mButton.setText("Stop playing");
        }else{
            mButton.setText("Start playing");
        }
        mStartPlaying=!mStartPlaying;
    };

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);

        mAudioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        setContentView(R.layout.activity_second);
//Get the connect widget
        editTextAddress = (EditText)findViewById(R.id.editText_ip);
        editTextPort = (EditText)findViewById(R.id.editText_port);
        textResponse = (TextView)findViewById(R.id.sample);
        Button_connect= (Button)findViewById((R.id.button_connect));

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

    public void button_connect_click(View view){
        socket_connect(mConnect);
        if(mConnect){
            Button_connect.setText("Disconnect");
        }
        else{
            Button_connect.setText("Connect");
        }
        mConnect=!mConnect;
    }

    private void socket_connect(Boolean mConnect){
        MyClientTask  myClientTask =  new MyClientTask(
                editTextAddress.getText().toString(),
                Integer.parseInt(editTextPort.getText().toString()));
        myClientTask.execute();
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void>{
        String dstAddress;
        int dstPort;
        String response;

        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0){
            try{
                Socket socket = new Socket(dstAddress,dstPort);
                InputStream inputStream = socket.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead= inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                socket.close();
                response = byteArrayOutputStream.toString("UTF-8");
            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            textResponse.setText(response);
            super.onPostExecute(result);
        }
    }

}
