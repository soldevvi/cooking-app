package com.example.cookingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.adapters.RecipeAdapter;
import com.example.cookingapp.models.Recipe;
import com.example.cookingapp.utils.LocaleHelper;
import com.example.cookingapp.utils.RecipeRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRecipes;
    private RecipeAdapter adapter;
    private TextView tvNoResults;
    private MaterialButton btnLanguage;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;

    private List<Recipe> allRecipes = new ArrayList<>();
    private String currentCategory = null; // null = all
    private String currentSearch = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupFilter();
        setupSearch();
        setupLanguageButton();

    }

    private void initViews() {
        rvRecipes = findViewById(R.id.rvRecipes);
        tvNoResults = findViewById(R.id.tvNoResults);
        btnLanguage = findViewById(R.id.btnLanguage);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        // Show current language on button
        String lang = LocaleHelper.getCurrentLanguageCode(this);
        btnLanguage.setText("🌐 " + lang.toUpperCase());
    }

    private void setupRecyclerView() {
        allRecipes = RecipeRepository.getInstance().getAllRecipes();
        int spanCount = getResources().getConfiguration().screenWidthDp >= 600 ? 2 : 1;
        rvRecipes.setLayoutManager(new GridLayoutManager(this, spanCount));
        adapter = new RecipeAdapter(this, new ArrayList<>(allRecipes), recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        });
        rvRecipes.setAdapter(adapter);
    }

    private void setupFilter() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) {
                currentCategory = null;
            } else if (id == R.id.chipBreakfast) {
                currentCategory = Recipe.CATEGORY_BREAKFAST;
            } else if (id == R.id.chipLunch) {
                currentCategory = Recipe.CATEGORY_LUNCH;
            } else if (id == R.id.chipDinner) {
                currentCategory = Recipe.CATEGORY_DINNER;
            }
            applyFilters();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentSearch = s.toString().trim().toLowerCase();
                applyFilters();
            }
        });
    }

    private void setupLanguageButton() {
        btnLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void applyFilters() {
        List<Recipe> filtered = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            // Category filter
            if (currentCategory != null && !recipe.getCategory().equals(currentCategory)) {
                continue;
            }
            // Search filter
            if (!currentSearch.isEmpty()) {
                int nameResId = getResources().getIdentifier(
                        recipe.getNameKey(), "string", getPackageName());
                String name = nameResId != 0 ? getString(nameResId).toLowerCase() : "";
                if (!name.contains(currentSearch)) continue;
            }
            filtered.add(recipe);
        }
        adapter.updateData(filtered);
        tvNoResults.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvRecipes.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLanguageDialog() {
        String[] languages = {
                getString(R.string.lang_en),
                getString(R.string.lang_ru),
                getString(R.string.lang_de)
        };
        String[] codes = {"en", "ru", "de"};
        String currentCode = LocaleHelper.getCurrentLanguageCode(this);

        int currentIndex = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(currentCode)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.choose_language))
                .setSingleChoiceItems(languages, currentIndex, (dialog, which) -> {
                    LocaleHelper.setLocale(this, codes[which]);
                    dialog.dismiss();
                    recreateWithLocale();
                })
                .show();
    }

    private void recreateWithLocale() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
