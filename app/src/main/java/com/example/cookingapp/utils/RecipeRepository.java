package com.example.cookingapp.utils;

import com.example.cookingapp.R;
import com.example.cookingapp.models.Recipe;
import com.example.cookingapp.models.TimerConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class RecipeRepository {

    private static RecipeRepository instance;
    private List<Recipe> recipes;

    private RecipeRepository() {
        recipes = new ArrayList<>();
        loadRecipes();
    }

    public static RecipeRepository getInstance() {
        if (instance == null) instance = new RecipeRepository();
        return instance;
    }

    private void loadRecipes() {


        List<TimerConfig> omeletteTimers = Arrays.asList(
                new TimerConfig(
                        "timer_omelette_heat",
                        60,
                        R.raw.timer_beep_2,
                        true
                ),
                new TimerConfig(
                        "timer_omelette_fry",
                        180,
                        R.raw.timer_beep_1,
                        false
                )
        );

        recipes.add(new Recipe(
                1, "recipe1_name", "recipe1_desc", Recipe.CATEGORY_BREAKFAST,
                "recipe1_time", 2, "recipe1_ingredients", "recipe1_steps",
                R.mipmap.img_omelette,
                "recipe1_voice_", 0,
                omeletteTimers
        ));


        recipes.add(new Recipe(
                2, "recipe2_name", "recipe2_desc", Recipe.CATEGORY_LUNCH,
                "recipe2_time", 4, "recipe2_ingredients", "recipe2_steps",
                R.mipmap.img_caesar,
                null, 0,
                Collections.emptyList()
        ));


        List<TimerConfig> carbonaraTimers = Collections.singletonList(
                new TimerConfig(
                        "timer_carbonara_pasta", // labelKey
                        480,
                        R.raw.timer_beep_1,
                        true
                )
        );

        recipes.add(new Recipe(
                3, "recipe3_name", "recipe3_desc", Recipe.CATEGORY_DINNER,
                "recipe3_time", 4, "recipe3_ingredients", "recipe3_steps",
                R.mipmap.img_carbonara,
                null, R.raw.recipe3_video,
                carbonaraTimers
        ));
    }

    public List<Recipe> getAllRecipes() { return new ArrayList<>(recipes); }

    public List<Recipe> getRecipesByCategory(String category) {
        List<Recipe> filtered = new ArrayList<>();
        for (Recipe r : recipes) if (r.getCategory().equals(category)) filtered.add(r);
        return filtered;
    }

    public Recipe getRecipeById(int id) {
        for (Recipe r : recipes) if (r.getId() == id) return r;
        return null;
    }
}
