package com.example.cookingapp;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cookingapp.models.Recipe;
import com.example.cookingapp.models.TimerConfig;
import com.example.cookingapp.utils.CookingTimerManager;
import com.example.cookingapp.utils.LocaleHelper;
import com.example.cookingapp.utils.RecipeRepository;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;


public class RecipeDetailActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailActivity";

    // Voice
    private MediaPlayer    voicePlayer;
    private boolean        isVoicePlaying = false;
    private MaterialButton btnVoice;

    // Video
    private VideoView videoView;
    private int       videoPosition = 0;


    private final List<CookingTimerManager> timerManagers = new ArrayList<>();

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
        setupTimers(recipe);
    }

    // ─────────────────────────────────────────────────────
    //  Toolbar
    // ─────────────────────────────────────────────────────
    private void setupToolbar(Recipe recipe) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        CollapsingToolbarLayout ctl = findViewById(R.id.collapsingToolbar);
        int nameRes = getResources().getIdentifier(recipe.getNameKey(), "string", getPackageName());
        ctl.setTitle(nameRes != 0 ? getString(nameRes) : recipe.getNameKey());
        ctl.setCollapsedTitleTextColor(Color.WHITE);
        ctl.setExpandedTitleColor(Color.WHITE);
    }

    // ─────────────────────────────────────────────────────
    //  Static data
    // ─────────────────────────────────────────────────────
    private void bindData(Recipe recipe) {
        ((ImageView) findViewById(R.id.ivDetailImage)).setImageResource(recipe.getImageResId());

        TextView tvCat = findViewById(R.id.tvDetailCategory);
        switch (recipe.getCategory()) {
            case Recipe.CATEGORY_BREAKFAST:
                tvCat.setText(getString(R.string.category_breakfast));
                tvCat.getBackground().setTint(Color.parseColor("#FF9800")); break;
            case Recipe.CATEGORY_LUNCH:
                tvCat.setText(getString(R.string.category_lunch));
                tvCat.getBackground().setTint(Color.parseColor("#4CAF50")); break;
            default:
                tvCat.setText(getString(R.string.category_dinner));
                tvCat.getBackground().setTint(Color.parseColor("#3F51B5")); break;
        }

        int timeRes = getResources().getIdentifier(recipe.getTimeKey(), "string", getPackageName());
        String time = timeRes != 0 ? getString(timeRes) : "?";
        ((TextView) findViewById(R.id.tvDetailTime)).setText(getString(R.string.cooking_time, time));
        ((TextView) findViewById(R.id.tvDetailServings)).setText(getString(R.string.servings, recipe.getServingsKey()));

        TextView tvIng = findViewById(R.id.tvIngredients);
        int ingRes = getResources().getIdentifier(recipe.getIngredientsKey(), "string", getPackageName());
        if (ingRes != 0) {
            StringBuilder sb = new StringBuilder();
            for (String line : getString(ingRes).split("\\\\n")) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("• ").append(line.trim());
            }
            tvIng.setText(sb.toString());
        }

        TextView tvSteps = findViewById(R.id.tvSteps);
        int stepsRes = getResources().getIdentifier(recipe.getStepsKey(), "string", getPackageName());
        if (stepsRes != 0) tvSteps.setText(getString(stepsRes).replace("\\n", "\n"));
    }


    private void setupTimers(Recipe recipe) {
        View cardTimers = findViewById(R.id.cardTimers);
        LinearLayout container = findViewById(R.id.timersContainer);

        if (!recipe.hasTimers()) {
            cardTimers.setVisibility(View.GONE);
            return;
        }

        cardTimers.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (TimerConfig config : recipe.getTimers()) {
            // Inflate виджет таймера
            View timerView = inflater.inflate(R.layout.item_timer, container, false);

            // Найти views внутри виджета
            TextView       tvLabel   = timerView.findViewById(R.id.tvTimerLabel);
            TextView       tvTime    = timerView.findViewById(R.id.tvTimerTime);
            MaterialButton btnStart  = timerView.findViewById(R.id.btnTimerStart);
            MaterialButton btnPause  = timerView.findViewById(R.id.btnTimerPause);
            MaterialButton btnReset  = timerView.findViewById(R.id.btnTimerReset);
            View           editSect  = timerView.findViewById(R.id.editableSection);
            EditText       etCustom  = timerView.findViewById(R.id.etTimerCustomTime);
            MaterialButton btnSetTime = timerView.findViewById(R.id.btnTimerSetTime);

            // Показать/скрыть секцию редактирования
            editSect.setVisibility(config.isEditable() ? View.VISIBLE : View.GONE);

            // Создать и привязать менеджер таймера
            CookingTimerManager manager = new CookingTimerManager(this, config);
            manager.bindViews(tvLabel, tvTime, btnStart, btnPause, btnReset,
                    config.isEditable() ? etCustom : null,
                    config.isEditable() ? btnSetTime : null);

            timerManagers.add(manager);
            container.addView(timerView);
        }
    }

    // ─────────────────────────────────────────────────────
    //  Voice
    // ─────────────────────────────────────────────────────
    private void setupVoice(Recipe recipe) {
        View cardVoice = findViewById(R.id.cardVoice);
        btnVoice = findViewById(R.id.btnVoice);
        if (!recipe.hasVoice()) { cardVoice.setVisibility(View.GONE); return; }
        cardVoice.setVisibility(View.VISIBLE);
        btnVoice.setOnClickListener(v -> { if (isVoicePlaying) stopVoice(); else startVoice(recipe); });
    }

    private void startVoice(Recipe recipe) {
        stopVoice();
        String lang = LocaleHelper.getCurrentLanguageCode(this);
        if (!lang.equals("ru") && !lang.equals("de")) lang = "en";
        String resName = recipe.getVoiceResPrefix() + lang;
        int resId = getResources().getIdentifier(resName, "raw", getPackageName());
        if (resId == 0) resId = getResources().getIdentifier(recipe.getVoiceResPrefix() + "en", "raw", getPackageName());
        if (resId == 0) return;
        try {
            voicePlayer = MediaPlayer.create(this, resId);
            if (voicePlayer != null) {
                voicePlayer.setVolume(1f, 1f);
                voicePlayer.setOnCompletionListener(mp -> { isVoicePlaying = false; btnVoice.setText(R.string.btn_play_voice); releaseVoicePlayer(); });
                voicePlayer.start();
                isVoicePlaying = true;
                btnVoice.setText(R.string.btn_stop_voice);
            }
        } catch (Exception e) { Log.e(TAG, "voice error", e); }
    }

    private void stopVoice() {
        if (voicePlayer != null) { try { if (voicePlayer.isPlaying()) voicePlayer.stop(); } catch (Exception ignored) {} releaseVoicePlayer(); }
        isVoicePlaying = false;
        if (btnVoice != null) btnVoice.setText(R.string.btn_play_voice);
    }

    private void releaseVoicePlayer() { if (voicePlayer != null) { try { voicePlayer.release(); } catch (Exception ignored) {} voicePlayer = null; } }


    private void setupVideo(Recipe recipe) {
        View cardVideo = findViewById(R.id.cardVideo);
        videoView = findViewById(R.id.videoView);
        MaterialButton btnPlay = findViewById(R.id.btnVideoPlay);
        MaterialButton btnStop = findViewById(R.id.btnVideoStop);
        if (!recipe.hasVideo()) { cardVideo.setVisibility(View.GONE); return; }
        cardVideo.setVisibility(View.VISIBLE);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + recipe.getVideoResId()));
        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoView);
        videoView.setMediaController(mc);
        videoView.setOnPreparedListener(mp -> { mp.setLooping(false); videoView.seekTo(1); });
        videoView.setOnErrorListener((mp, w, e) -> { cardVideo.setVisibility(View.GONE); return true; });
        btnPlay.setOnClickListener(v -> { if (!videoView.isPlaying()) videoView.start(); });
        btnStop.setOnClickListener(v -> { if (videoView.isPlaying()) { videoView.pause(); videoView.seekTo(0); videoPosition = 0; } });
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) { videoPosition = videoView.getCurrentPosition(); videoView.pause(); }
        stopVoice();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && videoPosition > 0) videoView.seekTo(videoPosition);
    }

    @Override
    protected void onDestroy() {

        for (CookingTimerManager m : timerManagers) m.release();
        timerManagers.clear();
        releaseVoicePlayer();
        if (videoView != null) videoView.stopPlayback();
        super.onDestroy();
    }
}
