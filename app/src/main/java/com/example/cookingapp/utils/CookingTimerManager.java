package com.example.cookingapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;

import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
//import com.google.android.material.MaterialButton;
import com.example.cookingapp.R;
import com.example.cookingapp.models.TimerConfig;
import com.google.android.material.button.MaterialButton;


import java.util.Locale;


public class CookingTimerManager {

    private static final String TAG = "CookingTimerManager";

    public enum State { IDLE, RUNNING, PAUSED, FINISHED }


    private final Context       context;
    private final TimerConfig   config;

    private CountDownTimer countDownTimer;
    private State          state           = State.IDLE;
    private long           totalMillis;
    private long           remainingMillis;


    private TextView       tvLabel;
    private TextView       tvTime;
    private MaterialButton btnStart;
    private MaterialButton btnPause;
    private MaterialButton btnReset;
    private EditText       etCustomTime;
    private MaterialButton btnSetTime;

    public CookingTimerManager(Context context, TimerConfig config) {
        this.context        = context.getApplicationContext();
        this.config         = config;
        this.totalMillis    = config.getDefaultSeconds() * 1000L;
        this.remainingMillis = totalMillis;
    }
    public void bindViews(TextView tvLabel, TextView tvTime,
                          MaterialButton btnStart, MaterialButton btnPause, MaterialButton btnReset,
                          EditText etCustomTime, MaterialButton btnSetTime) {
        this.tvLabel       = tvLabel;
        this.tvTime        = tvTime;
        this.btnStart      = btnStart;
        this.btnPause      = btnPause;
        this.btnReset      = btnReset;
        this.etCustomTime  = etCustomTime;
        this.btnSetTime    = btnSetTime;

        int labelResId = context.getResources().getIdentifier(
                config.getLabelKey(), "string", context.getPackageName());
        if (tvLabel != null && labelResId != 0) {
            tvLabel.setText(context.getString(labelResId));
        }

        renderTime(remainingMillis);
        updateButtonStates();
        setupListeners();
    }


    private void setupListeners() {
        if (btnStart != null) btnStart.setOnClickListener(v -> start());
        if (btnPause != null) btnPause.setOnClickListener(v -> pause());
        if (btnReset != null) btnReset.setOnClickListener(v -> reset());

        if (config.isEditable() && btnSetTime != null && etCustomTime != null) {
            btnSetTime.setOnClickListener(v -> applyCustomTime());
        }
    }



    public void start() {
        if (state == State.RUNNING) return;
        if (state == State.FINISHED) return;

        if (remainingMillis <= 0) {
            remainingMillis = totalMillis;
        }

        state = State.RUNNING;
        updateButtonStates();

        countDownTimer = new CountDownTimer(remainingMillis, 100) {
            @Override
            public void onTick(long msLeft) {
                remainingMillis = msLeft;
                renderTime(msLeft);
            }

            @Override
            public void onFinish() {
                remainingMillis = 0;
                state = State.FINISHED;
                renderTime(0);
                updateButtonStates();
                playBeep();
            }
        }.start();
    }

    public void pause() {
        if (state != State.RUNNING) return;
        cancelCountDown();
        state = State.PAUSED;
        updateButtonStates();
    }


    public void reset() {
        cancelCountDown();
        state          = State.IDLE;
        remainingMillis = totalMillis;
        renderTime(remainingMillis);
        updateButtonStates();
    }


    private void applyCustomTime() {
        if (etCustomTime == null) return;
        if (state == State.RUNNING) return;

        String input = etCustomTime.getText().toString().trim();
        if (input.isEmpty()) return;

        try {
            int minutes = Integer.parseInt(input);
            if (minutes <= 0 || minutes > 999) return;

            totalMillis    = minutes * 60_000L;
            remainingMillis = totalMillis;
            state          = State.IDLE;
            renderTime(remainingMillis);
            updateButtonStates();
            etCustomTime.setText("");
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid time input: " + input);
        }
    }


    private void renderTime(long msLeft) {
        if (tvTime == null) return;
        long totalSec = msLeft / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));


        if (msLeft > 0 && msLeft < 60_000L) {
            tvTime.setTextColor(Color.parseColor("#D32F2F"));
        } else if (msLeft == 0) {
            tvTime.setTextColor(Color.parseColor("#9E9E9E"));
        } else {
            tvTime.setTextColor(Color.parseColor("#212121"));
        }
    }


    private void updateButtonStates() {
        if (btnStart == null || btnPause == null || btnReset == null) return;
        switch (state) {
            case IDLE:
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnReset.setEnabled(false);
                break;
            case RUNNING:
                btnStart.setEnabled(false);
                btnPause.setEnabled(true);
                btnReset.setEnabled(true);
                break;
            case PAUSED:
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnReset.setEnabled(true);
                break;
            case FINISHED:
                btnStart.setEnabled(false);
                btnPause.setEnabled(false);
                btnReset.setEnabled(true);
                break;
        }

        if (etCustomTime != null) {
            etCustomTime.setEnabled(state == State.IDLE || state == State.PAUSED || state == State.FINISHED);
        }
        if (btnSetTime != null) {
            btnSetTime.setEnabled(state != State.RUNNING);
        }
    }


    private void playBeep() {
        if (config.getBeepResId() == 0) return;
        try {
            MediaPlayer mp = MediaPlayer.create(context, config.getBeepResId());
            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing beep", e);
        }
    }


    private void cancelCountDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }


    public void release() {
        cancelCountDown();
    }

    public State getState() { return state; }
}
