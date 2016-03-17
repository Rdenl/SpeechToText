package com.example.speechtotext;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.speechkit.SpeechKit;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by test5 on 17.03.16.
 */
public class YandexFragment extends Fragment implements RecognizerListener {

    private static final int REQUEST_PERMISSION_CODE = 1;

    private ProgressBar progressBar;
    private TextView currentStatus;
    private TextView recognitionResult;

    private Recognizer recognizer;

    public YandexFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpeechKit.getInstance().configure(getContext(), Constants.YANDEX_API_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.yandex_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button startBtn = (Button) view.findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndStartRecognizer();
            }
        });

        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRecognizer();
            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.voice_power_bar);
        currentStatus = (TextView) view.findViewById(R.id.current_state);
        recognitionResult = (TextView) view.findViewById(R.id.result);
    }

    @Override
    public void onPause() {
        super.onPause();
        resetRecognizer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PERMISSION_GRANTED) {
            createAndStartRecognizer();
        } else {
            updateStatus("Record audio permission was not granted");
        }
    }

    private void resetRecognizer() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer = null;
        }
    }

    @Override
    public void onRecordingBegin(Recognizer recognizer) {
        updateStatus("Recording begin");
    }

    @Override
    public void onSpeechDetected(Recognizer recognizer) {
        updateStatus("Speech detected");
    }

    @Override
    public void onSpeechEnds(Recognizer recognizer) {
        updateStatus("Speech ends");
    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {
        updateStatus("Recording done");
    }

    @Override
    public void onSoundDataRecorded(Recognizer recognizer, byte[] bytes) {
    }

    @Override
    public void onPowerUpdated(Recognizer recognizer, float power) {
        updateProgress((int) (power * progressBar.getMax()));
    }

    @Override
    public void onPartialResults(Recognizer recognizer, Recognition recognition, boolean b) {

        updateStatus("Partial results " + recognition.getBestResultText());
/*
        Toast.makeText(getContext(), recognition.getHypotheses().length + "", Toast.LENGTH_SHORT).show();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < recognition.getHypotheses().length; i++) {
            for (int j = 0; j < recognition.getHypotheses()[i].getWords().length; j++) {
                text.append(recognition.getHypotheses()[i].getWords()[j].toString());
                text.append(" ");
            }
            text.append("/n");
        }
        recognitionResult.setText(text);*/
    }

    @Override
    public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {
        updateResult(recognition.getBestResultText());
        updateProgress(0);
    }

    @Override
    public void onError(Recognizer recognizer, ru.yandex.speechkit.Error error) {
        if (error.getCode() == ru.yandex.speechkit.Error.ERROR_CANCELED) {
            updateStatus("Cancelled");
            updateProgress(0);
        } else {
            updateStatus("Error occurred " + error.getString());
            resetRecognizer();
        }
    }

    private void createAndStartRecognizer() {
        final Context context = getContext();
        if (context == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(context, RECORD_AUDIO) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
        } else {
            // Reset the current recognizer.
            resetRecognizer();
            // To create a new recognizer, specify the language, the model - a scope of recognition to get the most appropriate results,
            // set the listener to handle the recognition events.
            recognizer = Recognizer.create(Recognizer.Language.RUSSIAN, Recognizer.Model.NOTES, YandexFragment.this);
            // Don't forget to call start on the created object.
            recognizer.start();
        }
    }

    private void updateResult(String text) {
        recognitionResult.setText(text);
    }

    private void updateStatus(final String text) {
        currentStatus.setText(text);
    }

    private void updateProgress(int progress) {
        progressBar.setProgress(progress);
    }
}
