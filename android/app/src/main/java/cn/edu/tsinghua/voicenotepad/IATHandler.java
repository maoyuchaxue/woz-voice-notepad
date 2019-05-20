package cn.edu.tsinghua.voicenotepad;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;

public class IATHandler implements Runnable {

    private static String TAG = IATHandler.class.getSimpleName();

    private Activity parentActivity;
    private SpeechRecognizer mIat;
    private TTSHandler ttsHandler;
    private boolean ready;

    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
        }
    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            Log.i(TAG, "begin of speech");
        }

        @Override
        public void onError(SpeechError error) {
            ready = true;
        }

        @Override
        public void onEndOfSpeech() {
            Log.i(TAG, "end of speech");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());

            if(isLast) {
                Log.i(TAG, "speech text: " + text);
                ttsHandler.send(text);
                ready = true;
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {}

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };

    IATHandler(Activity parentActivity, TTSHandler ttsHandler) {
        this.parentActivity = parentActivity;
        this.ttsHandler = ttsHandler;
        this.mIat = SpeechRecognizer.createRecognizer(parentActivity, mInitListener);
    }

    @Override
    public void run() {

        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        mIat.setParameter(SpeechConstant.VAD_BOS, "10000");
        mIat.setParameter(SpeechConstant.VAD_EOS, "500");

        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");

        while (true) {
            this.ready = false;
            mIat.startListening(mRecognizerListener);
            while (!this.ready);
        }
    }

    private String getResourcePath(){
        StringBuffer buf = new StringBuffer();
        buf.append(ResourceUtil.generateResourcePath(parentActivity, ResourceUtil.RESOURCE_TYPE.assets, "iat/common.jet"));
        buf.append(";");
        buf.append(ResourceUtil.generateResourcePath(parentActivity, ResourceUtil.RESOURCE_TYPE.assets, "iat/sms_16k.jet"));
        return buf.toString();
    }


}
