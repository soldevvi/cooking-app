package com.example.cookingapp;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookingapp.utils.LocaleHelper;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long MAX_SPLASH_DURATION = 5000L;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean navigated = false;

    private final Runnable fallbackNavigate = () -> navigateToMain();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        playSplashSound();
    }


    private void playSplashSound() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound);
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0.8f, 0.8f);
                mediaPlayer.setOnCompletionListener(mp -> navigateToMain());
                handler.postDelayed(fallbackNavigate, MAX_SPLASH_DURATION);
                mediaPlayer.start();
            } else {
                handler.postDelayed(fallbackNavigate, 2000L);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing splash sound", e);
            handler.postDelayed(fallbackNavigate, 2000L);
        }
    }

    private void navigateToMain() {
        if (navigated) return;
        navigated = true;
        handler.removeCallbacks(fallbackNavigate);
        releaseMediaPlayer();
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing media player", e);
            }
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(fallbackNavigate);
        releaseMediaPlayer();
        super.onDestroy();
    }
}
