package com.lyy.guohe.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Audio {
    private static final String TAG = "Audio";

    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord mAudioRecord;//用于获取数据
    private final Object mLock;//为了使用wait函数
    private Handler myHandler;//用于向主线程传递数据

    private boolean isGetVoiceRun;

    public Audio(Handler handler) {
        /*
         * 初始化。
         * 这里我选择了把handler传递过来，应该也可以在主线程中把handler设为static直接使用。
         */
        mLock = new Object();
        myHandler = handler;
    }

    public void getNoiseLevel() {

        if (isGetVoiceRun) {
            Log.e(TAG, "还在录着呢");
            return;
        }


        //新建一个线程，录音并处理数据
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                        AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);

                isGetVoiceRun = true;

                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    //r是实际读取的数据长度，一般而言r会小于buffersize
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    // 将 buffer 内容取出，进行平方和运算
                    for (short aBuffer : buffer) {
                        v += aBuffer * aBuffer;
                    }
                    // 平方和除以数据总长度，得到音量大小。
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
                    Log.d(TAG, "分贝值:" + volume);

                    Message msg = new Message();
                    //bundle是一个key-value对，读数据的时候我写出key就可以读到对应的value
                    Bundle b = new Bundle();
                    //key设为"sound"，value设为分贝值，保留两位小数
                    b.putDouble("sound", volume);
                    msg.setData(b);
                    myHandler.sendMessage(msg);
                    // 大概一秒十次
                    synchronized (mLock) {
                        try {
                            mLock.wait(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
    }

    public void cancel() {
        if (mAudioRecord != null) {
            try {
                isGetVoiceRun = false;
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}