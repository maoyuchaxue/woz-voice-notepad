package cn.edu.tsinghua.voicenotepad;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;

import java.io.OutputStream;
import java.net.Socket;

public class TTSHandler implements Runnable {

    private static String TAG = TTSHandler.class.getSimpleName();
    private Socket voiceSocket;
    private String serverIP;
    private Activity parentActivity;
    private SpeechSynthesizer mTts;
    public static String voicerName = "xiaoyan";
    private String mEngineType = SpeechConstant.TYPE_LOCAL;

    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
        }
    };

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {}

        @Override
        public void onSpeakPaused() {}

        @Override
        public void onSpeakResumed() {}

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {}

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {}

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.i(TAG, "speaking finished");
            } else {
                Log.e(TAG, error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			/*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
				byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
				Log.e("MscSpeechLog", "buf is =" + buf);
			}*/
        }
    };

    public TTSHandler(Activity parentActivity) {
        this.parentActivity = parentActivity;
        mTts = SpeechSynthesizer.createSynthesizer(parentActivity, mTtsInitListener);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicerName);

        mTts.setParameter(SpeechConstant.SPEED, "50");
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }


    private String getResourcePath(){
        StringBuffer buf = new StringBuffer();
        buf.append(ResourceUtil.generateResourcePath(parentActivity, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        buf.append(";");
        buf.append(ResourceUtil.generateResourcePath(parentActivity, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + voicerName + ".jet"));
        return buf.toString();
    }

    @Override
    public void run() {
        try {
            voiceSocket = new Socket(serverIP, 9000);
            while (!voiceSocket.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte buf[] = new byte[2048];
        int cur = 0;
        while (true) {
            int read = 0;
            try {
                read = voiceSocket.getInputStream().read(buf, cur, 512);

                if (read > 0) {
                    Log.i(TAG, "speaking read :" + new String(buf));
                }

                for (int i = cur-1; i < cur+read-1; i++) {
                    if (i >= 0 && buf[i] == (byte)'/' && buf[i+1] == (byte)'/') {
                        // "//" refers to the end of one message
                        byte newBuf[] = new byte[i];
                        for (int j = 0; j < i; j++) {
                            newBuf[j] = buf[j];
                        }

                        String str = new String(newBuf);
                        tts(str);
                        for (int j = i+2; j < cur+read; j++) {
                            buf[j-i-2] = buf[j];
                        }
                        cur -= i+2;
                    }
                }

                cur += read;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void tts(String msg) {
        Log.i(TAG, "speaking " + msg);

        if (mTts.isSpeaking()) {
            mTts.stopSpeaking();
        }

        mTts.startSpeaking(msg, mTtsListener);
    }

    public void send(final String msg) {
        Log.i(TAG, "sending " + msg);
        new Thread() {
            @Override
            public void run() {
                try {
                    OutputStream outputStream = voiceSocket.getOutputStream();
                    outputStream.write(msg.getBytes());
                    outputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
