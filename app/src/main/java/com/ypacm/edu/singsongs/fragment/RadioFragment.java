package com.ypacm.edu.singsongs.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ypacm.edu.singsongs.R;
import com.ypacm.edu.singsongs.fftpack.RealDoubleFFT;

/**
 * Created by DB on 2016/4/6.
 */
public class RadioFragment extends Fragment {

    private Handler handler;
    private ImageView imageView;
    private ImageView pacMan;

    static final int frequency = 8000;
    static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    static final int BLOCK_SIZE = 1 << 10;
    private RecordAudioTask task;


    private boolean startFlag = true;
    private RealDoubleFFT fftTrans;
    public Bitmap bitmap;
    public Canvas canvas;
    private Paint paint;
    private Point point;

    private int width = 1080;
    private int height = 1920;
    private int drawPoint[] = new int[200];
    private int drawCount = 0;

//    private AnimationRunnable runnable = new AnimationRunnable();
//    private int pacmanCount = 0;
//    private int pacmanId[] = {R.drawable.pacman_right3, R.drawable.pacman_right2, R.drawable.pacman_right1, R.drawable.pacman_right0,};

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.radio_fragment, container, false);
        imageView = (ImageView) mView.findViewById(R.id.iv_radio);

        task = new RecordAudioTask();
        task.execute();
        fftTrans = new RealDoubleFFT(BLOCK_SIZE);
        bitmap = Bitmap.createBitmap(width, height / 2, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);


//        pacMan = (ImageView) mView.findViewById(R.id.iv_pacman);
//        pacMan.setX(300);
//        pacMan.setY(height / 4);
//        pacMan.setImageResource(R.drawable.pacman_right3);
//        handler.postDelayed(runnable, 200);

        return mView;
    }

    private class RecordAudioTask extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfig, audioFormat);
                Log.v("bufSize", String.valueOf(bufferSize));
                //录音 MediaRecorder.AudioSource.MIC
                //只录音去掉播放的音乐 MediaRecorder.AudioSource.VOICE_COMMUNICATION
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.VOICE_COMMUNICATION, frequency,
                        channelConfig, audioFormat, bufferSize);

                short[] audioBuffer = new short[BLOCK_SIZE];
                double[] toTrans = new double[BLOCK_SIZE];

                audioRecord.startRecording();

                while (startFlag) {
                    int result = audioRecord.read(audioBuffer, 0, BLOCK_SIZE);
                    int maxn = 0;
                    //audioBuffer为mic采集音频信号，复习数字信号处理，写出自己的FFT
                    for (int i = 0; i < BLOCK_SIZE && i < result; i++) {
                        if (audioBuffer[i] > maxn)
                            maxn = audioBuffer[i];
                    }
                    for (int i = 0; i < BLOCK_SIZE && i < result; i++) {
                        toTrans[i] = (double) audioBuffer[i] / Short.MAX_VALUE;
                        //采集到的音频为short类型，除以short的最大值，归一化
                    }

                    //publishProgress(toTrans);
                    fftTrans.ft(toTrans);
                    publishProgress(toTrans);
                }
                audioRecord.stop();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... values) {
//            canvas.drawColor(Color.BLACK);
            int maxn = 0;
            for (int i = 0; i < values[0].length; i++) {
                if (values[0][i] > maxn) {
                maxn = i * 4;
            }
            int x;
            x = width / values[0].length * (i + 1);
            int downy = (int) (height / 2 - (values[0][i] * 10));
            //频率为 i*frequency/BLOCK_SIZE
                int upy = height / 2;

//                canvas.drawLine(x, downy, x, upy, paint);
            }
            drawPoint[drawCount] = (int) (height / 2 - (maxn ));
            for (int i = 0; i < 200; i++) {
//                canvas.drawPoint(width - i, drawPoint[(drawCount - i + width) % width], paint);
                canvas.drawLine((200 - i) * width / 200, drawPoint[(drawCount - i + 200) % 200], (200 - i - 1) * width / 200, drawPoint[(drawCount - i + 200) % 200], paint);
            }
            drawCount = (drawCount + 1) % 200;
            Log.d("Hz", Integer.toString(maxn));
//            pacMan.setX(300);
//            pacMan.setY((int) (height / 2 - (maxn * 0.5)));
            imageView.invalidate();
        }
    }

//    class AnimationRunnable implements Runnable {
//        @Override
//        public void run() {
//            pacmanCount = (pacmanCount + 1) % 4;
//            pacMan.setImageResource(pacmanId[pacmanCount]);
//            handler.postDelayed(runnable, 200);
//        }
//    }
}
