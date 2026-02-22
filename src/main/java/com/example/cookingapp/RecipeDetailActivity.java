package com.example.cookingapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cookingapp.models.Recipe;
import com.example.cookingapp.utils.LocaleHelper;
import com.example.cookingapp.utils.RecipeRepository;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class RecipeDetailActivity extends AppCompatActivity {

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

        if (recipe == null) {
            finish();
            return;
        }

        setupToolbar(recipe);
        bindData(recipe);
    }

    private void setupToolbar(Recipe recipe) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        int nameResId = getResources().getIdentifier(
                recipe.getNameKey(), "string", getPackageName());
        String name = nameResId != 0 ? getString(nameResId) : recipe.getNameKey();
        collapsingToolbar.setTitle(name);
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
    }

    private void bindData(Recipe recipe) {
        // Image
        ImageView ivImage = findViewById(R.id.ivDetailImage);
        ivImage.setImageResource(recipe.getImageResId());

        // Category badge
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        String category = recipe.getCategory();
        switch (category) {
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

        // Time
        TextView tvTime = findViewById(R.id.tvDetailTime);
        int timeResId = getResources().getIdentifier(recipe.getTimeKey(), "string", getPackageName());
        String time = timeResId != 0 ? getString(timeResId) : "?";
        tvTime.setText(getString(R.string.cooking_time, time));

        // Servings
        TextView tvServings = findViewById(R.id.tvDetailServings);
        tvServings.setText(getString(R.string.servings, recipe.getServingsKey()));

        // Ingredients
        TextView tvIngredients = findViewById(R.id.tvIngredients);
        int ingResId = getResources().getIdentifier(recipe.getIngredientsKey(), "string", getPackageName());
        if (ingResId != 0) {
            String raw = getString(ingResId);
            // Convert \n literal (from string resources) to actual newlines with bullets
            StringBuilder sb = new StringBuilder();
            String[] lines = raw.split("\\\\n");
            for (String line : lines) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("• ").append(line.trim());
            }
            tvIngredients.setText(sb.toString());
        }

        // Steps
        TextView tvSteps = findViewById(R.id.tvSteps);
        int stepsResId = getResources().getIdentifier(recipe.getStepsKey(), "string", getPackageName());
        if (stepsResId != 0) {
            String raw = getString(stepsResId);
            tvSteps.setText(raw.replace("\\n", "\n"));
        }
    }
}
