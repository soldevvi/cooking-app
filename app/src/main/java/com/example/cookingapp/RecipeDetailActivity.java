package com.example.cookingapp;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cookingapp.models.Recipe;
import com.example.cookingapp.utils.LocaleHelper;
import com.example.cookingapp.utils.RecipeRepository;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;


public class RecipeDetailActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailActivity";

    // Voice playback
    private MediaPlayer voicePlayer;
    private boolean isVoicePlaying = false;
    private MaterialButton btnVoice;

    // Video playback
    private VideoView videoView;
    private int videoPosition = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        int recipeId = getIntent().getIntExtra("recipe_id", -1);
        Recipe recipe = RecipeRepository.getInstance().getRecipeById(recipeId);

        if (recipe == null) { finish(); return; }

        setupToolbar(recipe);
        bindData(recipe);
        setupVoice(recipe);
        setupVideo(recipe);
    }

    // ─────────────────────────────────────────────
    //  Toolbar
    // ─────────────────────────────────────────────
    private void setupToolbar(Recipe recipe) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        int nameResId = getResources().getIdentifier(recipe.getNameKey(), "string", getPackageName());
        String name = nameResId != 0 ? getString(nameResId) : recipe.getNameKey();
        collapsingToolbar.setTitle(name);
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
    }

    // ─────────────────────────────────────────────
    //  Bind static data
    // ─────────────────────────────────────────────
    private void bindData(Recipe recipe) {
        ImageView ivImage = findViewById(R.id.ivDetailImage);
        ivImage.setImageResource(recipe.getImageResId());

        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        switch (recipe.getCategory()) {
            case Recipe.CATEGORY_BREAKFAST:
                tvCategory.setText(getString(R.string.category_breakfast));
                tvCategory.getBackground().setTint(Color.parseColor("#FF9800"));
                break;
            case Recipe.CATEGORY_LUNCH:
                tvCategory.setText(getString(R.string.category_lunch));
                tvCategory.getBackground().setTint(Color.parseColor("#4CAF50"));
                break;
            default:
                tvCategory.setText(getString(R.string.category_dinner));
                tvCategory.getBackground().setTint(Color.parseColor("#3F51B5"));
                break;
        }

        int timeResId = getResources().getIdentifier(recipe.getTimeKey(), "string", getPackageName());
        String time = timeResId != 0 ? getString(timeResId) : "?";
        ((TextView) findViewById(R.id.tvDetailTime)).setText(getString(R.string.cooking_time, time));
        ((TextView) findViewById(R.id.tvDetailServings)).setText(getString(R.string.servings, recipe.getServingsKey()));

        // Ingredients
        TextView tvIngredients = findViewById(R.id.tvIngredients);
        int ingResId = getResources().getIdentifier(recipe.getIngredientsKey(), "string", getPackageName());
        if (ingResId != 0) {
            StringBuilder sb = new StringBuilder();
            for (String line : getString(ingResId).split("\\\\n")) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("• ").append(line.trim());
            }
            tvIngredients.setText(sb.toString());
        }

        // Steps
        TextView tvSteps = findViewById(R.id.tvSteps);
        int stepsResId = getResources().getIdentifier(recipe.getStepsKey(), "string", getPackageName());
        if (stepsResId != 0) tvSteps.setText(getString(stepsResId).replace("\\n", "\n"));
    }


    private void setupVoice(Recipe recipe) {
        View cardVoice = findViewById(R.id.cardVoice);
        btnVoice = findViewById(R.id.btnVoice);

        if (!recipe.hasVoice()) {
            cardVoice.setVisibility(View.GONE);
            return;
        }

        cardVoice.setVisibility(View.VISIBLE);

        btnVoice.setOnClickListener(v -> {
            if (isVoicePlaying) {
                stopVoice();
            } else {
                startVoice(recipe);
            }
        });
    }

    private void startVoice(Recipe recipe) {
        stopVoice();


        String langCode = LocaleHelper.getCurrentLanguageCode(this);

        if (!langCode.equals("ru") && !langCode.equals("de")) langCode = "en";

        String resName = recipe.getVoiceResPrefix() + langCode;
        int resId = getResources().getIdentifier(resName, "raw", getPackageName());

        if (resId == 0) {
            Log.w(TAG, "Voice resource not found: " + resName + ", falling back to EN");
            resId = getResources().getIdentifier(
                    recipe.getVoiceResPrefix() + "en", "raw", getPackageName());
        }

        if (resId == 0) {
            Log.e(TAG, "No voice resource found for recipe " + recipe.getId());
            return;
        }

        try {
            voicePlayer = MediaPlayer.create(this, resId);
            if (voicePlayer != null) {
                voicePlayer.setVolume(1.0f, 1.0f);
                voicePlayer.setOnCompletionListener(mp -> {
                    isVoicePlaying = false;
                    btnVoice.setText(getString(R.string.btn_play_voice));
                    releaseVoicePlayer();
                });
                voicePlayer.start();
                isVoicePlaying = true;
                btnVoice.setText(getString(R.string.btn_stop_voice));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing voice", e);
        }
    }

    private void stopVoice() {
        if (voicePlayer != null) {
            try {
                if (voicePlayer.isPlaying()) voicePlayer.stop();
            } catch (Exception ignored) {}
            releaseVoicePlayer();
        }
        isVoicePlaying = false;
        if (btnVoice != null) btnVoice.setText(getString(R.string.btn_play_voice));
    }

    private void releaseVoicePlayer() {
        if (voicePlayer != null) {
            try { voicePlayer.release(); } catch (Exception ignored) {}
            voicePlayer = null;
        }
    }


    private void setupVideo(Recipe recipe) {
        View cardVideo = findViewById(R.id.cardVideo);
        videoView = findViewById(R.id.videoView);
        MaterialButton btnVideoPlay = findViewById(R.id.btnVideoPlay);
        MaterialButton btnVideoStop = findViewById(R.id.btnVideoStop);

        if (!recipe.hasVideo()) {
            cardVideo.setVisibility(View.GONE);
            return;
        }

        cardVideo.setVisibility(View.VISIBLE);


        String videoUri = "android.resource://" + getPackageName() + "/" + recipe.getVideoResId();
        videoView.setVideoURI(Uri.parse(videoUri));


        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);

            videoView.seekTo(1);
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Video error: what=" + what + " extra=" + extra);
            cardVideo.setVisibility(View.GONE);
            return true;
        });

        btnVideoPlay.setOnClickListener(v -> {
            if (!videoView.isPlaying()) {
                videoView.start();
            }
        });

        btnVideoStop.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                videoView.seekTo(0);
                videoPosition = 0;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (videoView != null && videoView.isPlaying()) {
            videoPosition = videoView.getCurrentPosition();
            videoView.pause();
        }

        stopVoice();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (videoView != null && videoPosition > 0) {
            videoView.seekTo(videoPosition);
        }
    }

    @Override
    protected void onDestroy() {
        releaseVoicePlayer();
        if (videoView != null) videoView.stopPlayback();
        super.onDestroy();
    }
}
