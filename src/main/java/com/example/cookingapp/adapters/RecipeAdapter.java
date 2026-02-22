package com.example.cookingapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.R;
import com.example.cookingapp.models.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private Context context;
    private List<Recipe> recipes;
    private OnRecipeClickListener listener;

    public RecipeAdapter(Context context, List<Recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        // Image
        holder.ivRecipeImage.setImageResource(recipe.getImageResId());

        // Name - resolve from string resources dynamically
        int nameResId = context.getResources().getIdentifier(
                recipe.getNameKey(), "string", context.getPackageName());
        holder.tvRecipeName.setText(nameResId != 0 ? context.getString(nameResId) : recipe.getNameKey());

        // Description
        int descResId = context.getResources().getIdentifier(
                recipe.getDescKey(), "string", context.getPackageName());
        holder.tvRecipeDesc.setText(descResId != 0 ? context.getString(descResId) : "");

        // Time
        int timeResId = context.getResources().getIdentifier(
                recipe.getTimeKey(), "string", context.getPackageName());
        String time = timeResId != 0 ? context.getString(timeResId) : "?";
        holder.tvTime.setText(context.getString(R.string.cooking_time, time));

        // Servings
        holder.tvServings.setText(context.getString(R.string.servings, recipe.getServingsKey()));

        // Category badge
        String category = recipe.getCategory();
        String categoryText;
        int badgeColor;
        switch (category) {
            case Recipe.CATEGORY_BREAKFAST:
                categoryText = context.getString(R.string.category_breakfast);
                badgeColor = Color.parseColor("#FF9800");
                break;
            case Recipe.CATEGORY_LUNCH:
                categoryText = context.getString(R.string.category_lunch);
                badgeColor = Color.parseColor("#4CAF50");
                break;
            default: // dinner
                categoryText = context.getString(R.string.category_dinner);
                badgeColor = Color.parseColor("#3F51B5");
                break;
        }
        holder.tvCategory.setText(categoryText);
        holder.tvCategory.getBackground().setTint(badgeColor);

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateData(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipeImage;
        TextView tvRecipeName, tvRecipeDesc, tvTime, tvServings, tvCategory;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeDesc = itemView.findViewById(R.id.tvRecipeDesc);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvServings = itemView.findViewById(R.id.tvServings);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}
